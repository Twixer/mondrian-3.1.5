/*
// $Id: //open/mondrian-release/3.1/testsrc/main/mondrian/test/TestContext.java#5 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2002-2002 Kana Software, Inc.
// Copyright (C) 2002-2009 Julian Hyde and others
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
//
// jhyde, 29 March, 2002
*/
package mondrian.test;

import junit.framework.Assert;
import junit.framework.ComparisonFailure;

import mondrian.calc.*;
import mondrian.olap.*;
import mondrian.olap.Connection;
import mondrian.olap.DriverManager;
import mondrian.olap.Member;
import mondrian.olap.Position;
import mondrian.olap.Cell;
import mondrian.olap.Axis;
import mondrian.olap.fun.FunUtil;
import mondrian.resource.MondrianResource;
import mondrian.rolap.RolapConnectionProperties;
import mondrian.rolap.RolapUtil;
import mondrian.spi.impl.FilterDynamicSchemaProcessor;
import mondrian.spi.Dialect;
import mondrian.spi.DialectManager;
import mondrian.util.DelegatingInvocationHandler;

import javax.sql.DataSource;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;
import java.lang.reflect.*;

import org.olap4j.*;
import org.olap4j.impl.CoordinateIterator;

/**
 * <code>TestContext</code> is a singleton class which contains the information
 * necessary to run mondrian tests (otherwise we'd have to pass this
 * information into the constructor of TestCases).
 *
 * <p>The singleton instance (retrieved via the {@link #instance()} method)
 * contains a connection to the FoodMart database, and runs expressions in the
 * context of the <code>Sales</code> cube.
 *
 * <p>Using the {@link DelegatingTestContext} subclass, you can create derived
 * classes which use a different connection or a different cube.
 *
 * @author jhyde
 * @since 29 March, 2002
 * @version $Id: //open/mondrian-release/3.1/testsrc/main/mondrian/test/TestContext.java#5 $
 */
public class TestContext {
    private static TestContext instance; // the singleton
    private PrintWriter pw;

    /**
     * Connection to the FoodMart database. Set on the first call to
     * {@link #getFoodMartConnection}.
     */
    private Connection foodMartConnection;
    private Dialect dialect;

    protected static final String nl = Util.nl;
    private static final String indent = "                ";
    private static final String lineBreak = "\"," + nl + "\"";
    private static final String lineBreak2 = "\\\\n\"" + nl + indent + "+ \"";
    private static final String lineBreak3 = "\\n\"" + nl + indent + "+ \"";
    private static final Pattern LineBreakPattern =
        Pattern.compile("\r\n|\r|\n");
    private static final Pattern TabPattern = Pattern.compile("\t");
    private static final String[] AllDims = {
        "[Measures]",
        "[Store]",
        "[Store Size in SQFT]",
        "[Store Type]",
        "[Time]",
        "[Product]",
        "[Promotion Media]",
        "[Promotions]",
        "[Customers]",
        "[Education Level]",
        "[Gender]",
        "[Marital Status]",
        "[Yearly Income]"
    };
    private static String unadulteratedFoodMartSchema;

    /**
     * Retrieves the singleton (instantiating if necessary).
     */
    public static synchronized TestContext instance() {
        if (instance == null) {
            instance = new TestContext();
        }
        return instance;
    }

    /**
     * Creates a TestContext.
     */
    protected TestContext() {
        // Run all tests in the US locale, not the system default locale,
        // because the results all assume the US locale.
        MondrianResource.setThreadLocale(Locale.US);

        this.pw = new PrintWriter(System.out, true);
    }

    /**
     * Returns the connect string by which the unit tests can talk to the
     * FoodMart database.
     *
     * <p>In the base class, the result is the same as the static method
     * {@link #getDefaultConnectString}. If a derived class overrides
     * {@link #getFoodMartConnectionProperties()}, the result of this method
     * will change also.
     */
    public final String getConnectString() {
        return getFoodMartConnectionProperties().toString();
    }

    /**
     * Constructs a connect string by which the unit tests can talk to the
     * FoodMart database.
     *
     * The algorithm is as follows:<ul>
     * <li>Starts with {@link MondrianProperties#TestConnectString}, if it is
     *     set.</li>
     * <li>If {@link MondrianProperties#FoodmartJdbcURL} is set, this
     *     overrides the <code>Jdbc</code> property.</li>
     * <li>If the <code>catalog</code> URL is unset or invalid, it assumes that
     *     we are at the root of the source tree, and references
     *     <code>demo/FoodMart.xml</code></li>.
     * </ul>
     */
    public static String getDefaultConnectString() {
        String connectString =
            MondrianProperties.instance().TestConnectString.get();
        final Util.PropertyList connectProperties;
        if (connectString == null || connectString.equals("")) {
            connectProperties = new Util.PropertyList();
            connectProperties.put("Provider", "mondrian");
        } else {
             connectProperties = Util.parseConnectString(connectString);
        }
        String jdbcURL = MondrianProperties.instance().FoodmartJdbcURL.get();
        if (jdbcURL != null) {
            connectProperties.put("Jdbc", jdbcURL);
        }
        String jdbcUser = MondrianProperties.instance().TestJdbcUser.get();
        if (jdbcUser != null) {
            connectProperties.put("JdbcUser", jdbcUser);
        }
        String jdbcPassword =
            MondrianProperties.instance().TestJdbcPassword.get();
        if (jdbcPassword != null) {
            connectProperties.put("JdbcPassword", jdbcPassword);
        }

        // Find the catalog. Use the URL specified in the connect string, if
        // it is specified and is valid. Otherwise, reference FoodMart.xml
        // assuming we are at the root of the source tree.
        URL catalogURL = null;
        String catalog = connectProperties.get("catalog");
        if (catalog != null) {
            try {
                catalogURL = new URL(catalog);
            } catch (MalformedURLException e) {
                // ignore
            }
        }
        if (catalogURL == null) {
            // Works if we are running in root directory of source tree
            File file = new File("demo/FoodMart.xml");
            if (!file.exists()) {
                // Works if we are running in bin directory of runtime env
                file = new File("../demo/FoodMart.xml");
            }
            try {
                catalogURL = Util.toURL(file);
            } catch (MalformedURLException e) {
                throw new Error(e.getMessage());
            }
        }
        connectProperties.put("catalog", catalogURL.toString());
        return connectProperties.toString();
    }

    /**
     * Returns the connection to run queries.
     *
     * <p>By default, returns a connection to the FoodMart database.
     */
    public Connection getConnection() {
        return getFoodMartConnection();
    }

    public synchronized void flushSchemaCache() {
        // it's pointless to flush the schema cache if we
        // have a handle on the connection object already
        clearConnection();
        getConnection().getCacheControl(null).flushSchemaCache();
    }

    public synchronized void clearConnection() {
        if (foodMartConnection != null) {
            try {
                foodMartConnection.getDataSource().getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            foodMartConnection = null;
        }
    }

    /**
     * Returns a connection to the FoodMart database.
     */
    public synchronized Connection getFoodMartConnection() {
        if (foodMartConnection == null) {
            foodMartConnection =
                DriverManager.getConnection(
                    getFoodMartConnectionProperties(),
                    null);
        }
        return foodMartConnection;
    }

    /**
     * Returns a connection to the FoodMart database
     * with a dynamic schema processor and disables use of RolapSchema Pool.
     */
    public synchronized final Connection getFoodMartConnection(
        Class dynProcClass)
    {
        Util.PropertyList properties = getFoodMartConnectionProperties();
        properties.put(
            RolapConnectionProperties.DynamicSchemaProcessor.name(),
            dynProcClass.getName());
        properties.put(
            RolapConnectionProperties.UseSchemaPool.name(),
            "false");
        return DriverManager.getConnection(properties, null, null);
    }

    public Util.PropertyList getFoodMartConnectionProperties() {
        final Util.PropertyList propertyList =
            Util.parseConnectString(getDefaultConnectString());
        if (MondrianProperties.instance().TestHighCardinalityDimensionList
            .get() != null
            && propertyList.get(
            RolapConnectionProperties.DynamicSchemaProcessor.name()) == null)
        {
            propertyList.put(
                RolapConnectionProperties.DynamicSchemaProcessor.name(),
                HighCardDynamicSchemaProcessor.class.getName());
        }
        return propertyList;
    }

    /**
     * Returns a connection to the FoodMart database
     * with an inline schema.
     */
    public synchronized Connection getFoodMartConnection(
        String catalogContent)
    {
        Util.PropertyList properties = getFoodMartConnectionProperties();
        properties.put(
            RolapConnectionProperties.CatalogContent.name(),
            catalogContent);
        return DriverManager.getConnection(properties, null, null);
    }

    /**
     * Returns a connection to the FoodMart database with an inline schema and
     * a given role.
     */
    public synchronized Connection getFoodMartConnection(
        String catalogContent,
        String role)
    {
        Util.PropertyList properties = getFoodMartConnectionProperties();
        properties.put(
            RolapConnectionProperties.CatalogContent.name(),
            catalogContent);
        properties.put(
            RolapConnectionProperties.Role.name(),
            role);
        return DriverManager.getConnection(properties, null, null);
    }

    /**
     * Returns a connection to the FoodMart database, optionally not from the
     * schema pool.
     *
     * @param useSchemaPool If false, use a fresh connection, not one from the
     *   schema pool
     */
    public synchronized Connection getFoodMartConnection(
        boolean useSchemaPool)
    {
        Util.PropertyList properties = getFoodMartConnectionProperties();
        properties.put(
            RolapConnectionProperties.UseSchemaPool.name(),
            useSchemaPool ? "true" : "false");
        return DriverManager.getConnection(properties, null, null);
    }

    /**
     * Returns a the XML of the foodmart schema with added parameters and cube
     * definitions.
     */
    public static String getFoodMartSchema(
        String parameterDefs,
        String cubeDefs,
        String virtualCubeDefs,
        String namedSetDefs,
        String udfDefs,
        String roleDefs)
    {
        // First, get the unadulterated schema.
        String s = getRawFoodMartSchema();

        // Add parameter definitions, if specified.
        if (parameterDefs != null) {
            int i = s.indexOf("<Dimension name=\"Store\">");
            s = s.substring(0, i)
                + parameterDefs
                + s.substring(i);
        }

        // Add cube definitions, if specified.
        if (cubeDefs != null) {
            int i =
                s.indexOf(
                    "<Cube name=\"Sales\" defaultMeasure=\"Unit Sales\">");
            s = s.substring(0, i)
                + cubeDefs
                + s.substring(i);
        }

        // Add virtual cube definitions, if specified.
        if (virtualCubeDefs != null) {
            int i = s.indexOf(
                "<VirtualCube name=\"Warehouse and Sales\" "
                + "defaultMeasure=\"Store Sales\">");
            s = s.substring(0, i)
                + virtualCubeDefs
                + s.substring(i);
        }

        // Add named set definitions, if specified. Schema-level named sets
        // occur after <Cube> and <VirtualCube> and before <Role> elements.
        if (namedSetDefs != null) {
            int i = s.indexOf("<Role");
            if (i < 0) {
                i = s.indexOf("</Schema>");
            }
            s = s.substring(0, i)
                + namedSetDefs
                + s.substring(i);
        }

        // Add definitions of roles, if specified.
        if (roleDefs != null) {
            int i = s.indexOf("<UserDefinedFunction");
            if (i < 0) {
                i = s.indexOf("</Schema>");
            }
            s = s.substring(0, i)
                + roleDefs
                + s.substring(i);
        }

        // Add definitions of user-defined functions, if specified.
        if (udfDefs != null) {
            int i = s.indexOf("</Schema>");
            s = s.substring(0, i)
                + udfDefs
                + s.substring(i);
        }
        return s;
    }

    /**
     * Returns the definition of the "FoodMart" schema as stored in
     * {@code FoodMart.xml}.
     *
     * @return XML definition of the FoodMart schema
     */
    public static String getRawFoodMartSchema() {
        synchronized (SnoopingSchemaProcessor.class) {
            if (unadulteratedFoodMartSchema == null) {
                instance().getFoodMartConnection(
                    SnoopingSchemaProcessor.class);
                unadulteratedFoodMartSchema =
                    SnoopingSchemaProcessor.catalogContent;
            }
        }

        return unadulteratedFoodMartSchema;
    }

    /**
     * Returns a the XML of the foodmart schema, adding dimension definitions
     * to the definition of a given cube.
     */
    public String getFoodMartSchemaSubstitutingCube(
        String cubeName,
        String dimensionDefs,
        String memberDefs)
    {
        return getFoodMartSchemaSubstitutingCube(
            cubeName, dimensionDefs, null, memberDefs, null);
    }

    /**
     * Returns a the XML of the foodmart schema, adding dimension definitions
     * to the definition of a given cube.
     */
    public String getFoodMartSchemaSubstitutingCube(
        String cubeName,
        String dimensionDefs,
        String measureDefs,
        String memberDefs,
        String namedSetDefs)
    {
        // First, get the unadulterated schema.
        String s = getRawFoodMartSchema();

        // Search for the <Cube> or <VirtualCube> element.
        int h = s.indexOf("<Cube name=\"" + cubeName + "\"");
        int end;
        if (h < 0) {
            h = s.indexOf("<VirtualCube name=\"" + cubeName + "\"");
            if (h < 0) {
                throw new RuntimeException("cube '" + cubeName + "' not found");
            } else {
                end = s.indexOf("</VirtualCube", h);
            }
        } else {
            end = s.indexOf("</Cube>", h);
        }

        // Add dimension definitions, if specified.
        if (dimensionDefs != null) {
            int i = s.indexOf("<Dimension ", h);
            s = s.substring(0, i)
                + dimensionDefs
                + s.substring(i);
        }

        // Add measure definitions, if specified.
        if (measureDefs != null) {
            int i = s.indexOf("<Measure", h);
            if (i < 0 || i > end) {
                i = end;
            }
            s = s.substring(0, i)
                + measureDefs
                + s.substring(i);
        }

        // Add calculated member definitions, if specified.
        if (memberDefs != null) {
            int i = s.indexOf("<CalculatedMember", h);
            if (i < 0 || i > end) {
                i = end;
            }
            s = s.substring(0, i)
                + memberDefs
                + s.substring(i);
        }

        if (namedSetDefs != null) {
            int i = s.indexOf("<NamedSet", h);
            if (i < 0 || i > end) {
                i = end;
            }
            s = s.substring(0, i)
                + namedSetDefs
                + s.substring(i);
        }

        return s;
    }

    /**
     * Executes a query.
     *
     * @param queryString Query string
     */
    public Result executeQuery(String queryString) {
        Connection connection = getConnection();
        queryString = upgradeQuery(queryString);
        Query query = connection.parseQuery(queryString);
        final Result result = connection.execute(query);

        // If we're deep testing, check that we never return the dummy null
        // value when cells are null. TestExpDependencies isn't the perfect
        // switch to enable this, but it will do for now.
        if (MondrianProperties.instance().TestExpDependencies.booleanValue()) {
            assertResultValid(result);
        }
        return result;
    }

    /**
     * Executes a query using olap4j.
     */
    public CellSet executeOlap4jQuery(String queryString) throws SQLException {
        OlapConnection connection = getOlap4jConnection();
        queryString = upgradeQuery(queryString);
        OlapStatement stmt = connection.createStatement();
        final CellSet cellSet = stmt.executeOlapQuery(queryString);

        // If we're deep testing, check that we never return the dummy null
        // value when cells are null. TestExpDependencies isn't the perfect
        // switch to enable this, but it will do for now.
        if (MondrianProperties.instance().TestExpDependencies.booleanValue()) {
            assertCellSetValid(cellSet);
        }
        return cellSet;
    }

    /**
     * Checks that a {@link Result} is valid.
     *
     * @param result Query result
     */
    private void assertResultValid(Result result) {
        for (Cell cell : cellIter(result)) {
            final Object value = cell.getValue();

            // Check that the dummy value used to represent null cells never
            // leaks into the outside world.
            Assert.assertNotSame(value, Util.nullValue);
            Assert.assertFalse(
                value instanceof Number
                && ((Number) value).doubleValue() == FunUtil.DoubleNull);

            // Similarly empty values.
            Assert.assertNotSame(value, Util.EmptyValue);
            Assert.assertFalse(
                value instanceof Number
                && ((Number) value).doubleValue() == FunUtil.DoubleEmpty);

            // Cells should be null if and only if they are null or empty.
            if (cell.getValue() == null) {
                Assert.assertTrue(cell.isNull());
            } else {
                Assert.assertFalse(cell.isNull());
            }
        }

        // There should be no null members.
        for (Axis axis : result.getAxes()) {
            for (Position position : axis.getPositions()) {
                for (Member member : position) {
                    Assert.assertNotNull(member);
                }
            }
        }
    }

    /**
     * Checks that a {@link CellSet} is valid.
     *
     * @param cellSet Cell set
     */
    private void assertCellSetValid(CellSet cellSet) {
        for (org.olap4j.Cell cell : cellIter(cellSet)) {
            final Object value = cell.getValue();

            // Check that the dummy value used to represent null cells never
            // leaks into the outside world.
            Assert.assertNotSame(value, Util.nullValue);
            Assert.assertFalse(
                value instanceof Number
                && ((Number) value).doubleValue() == FunUtil.DoubleNull);

            // Similarly empty values.
            Assert.assertNotSame(value, Util.EmptyValue);
            Assert.assertFalse(
                value instanceof Number
                && ((Number) value).doubleValue() == FunUtil.DoubleEmpty);

            // Cells should be null if and only if they are null or empty.
            if (cell.getValue() == null) {
                Assert.assertTrue(cell.isNull());
            } else {
                Assert.assertFalse(cell.isNull());
            }
        }

        // There should be no null members.
        for (CellSetAxis axis : cellSet.getAxes()) {
            for (org.olap4j.Position position : axis.getPositions()) {
                for (org.olap4j.metadata.Member member : position.getMembers())
                {
                    Assert.assertNotNull(member);
                }
            }
        }
    }

    /**
     * Returns an iterator over cells in a result.
     */
    static Iterable<Cell> cellIter(final Result result) {
        return new Iterable<Cell>() {
            public Iterator<Cell> iterator() {
                int[] axisDimensions = new int[result.getAxes().length];
                int k = 0;
                for (Axis axis : result.getAxes()) {
                    axisDimensions[k++] = axis.getPositions().size();
                }
                final CoordinateIterator
                    coordIter = new CoordinateIterator(axisDimensions);
                return new Iterator<Cell>() {
                    public boolean hasNext() {
                        return coordIter.hasNext();
                    }

                    public Cell next() {
                        final int[] ints = coordIter.next();
                        return result.getCell(ints);
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    /**
     * Returns an iterator over cells in an olap4j cell set.
     */
    static Iterable<org.olap4j.Cell> cellIter(final CellSet cellSet) {
        return new Iterable<org.olap4j.Cell>() {
            public Iterator<org.olap4j.Cell> iterator() {
                int[] axisDimensions = new int[cellSet.getAxes().size()];
                int k = 0;
                for (CellSetAxis axis : cellSet.getAxes()) {
                    axisDimensions[k++] = axis.getPositions().size();
                }
                final CoordinateIterator
                    coordIter = new CoordinateIterator(axisDimensions);
                return new Iterator<org.olap4j.Cell>() {
                    public boolean hasNext() {
                        return coordIter.hasNext();
                    }

                    public org.olap4j.Cell next() {
                        final int[] ints = coordIter.next();
                        final List<Integer> list =
                            new AbstractList<Integer>() {
                                public Integer get(int index) {
                                    return ints[index];
                                }

                                public int size() {
                                    return ints.length;
                                }
                            };
                        return cellSet.getCell(
                            list);
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    /**
     * Executes a query, and asserts that it throws an exception which contains
     * the given pattern.
     *
     * @param queryString Query string
     * @param pattern Pattern which exception must match
     */
    public void assertQueryThrows(String queryString, String pattern) {
        Throwable throwable;
        try {
            Result result = executeQuery(queryString);
            Util.discard(result);
            throwable = null;
        } catch (Throwable e) {
            throwable = e;
        }
        checkThrowable(throwable, pattern);
    }

    /**
     * @deprecated Use {@link #assertQueryThrows(String, String)}.
     */
    public final void assertThrows(String queryString, String pattern) {
        assertQueryThrows(queryString, pattern);
    }

    /**
     * Executes an expression, and asserts that it gives an error which contains
     * a particular pattern. The error might occur during parsing, or might
     * be contained within the cell value.
     */
    public void assertExprThrows(String expression, String pattern) {
        Throwable throwable = null;
        try {
            String cubeName = getDefaultCubeName();
            if (cubeName.indexOf(' ') >= 0) {
                cubeName = Util.quoteMdxIdentifier(cubeName);
            }
            expression = Util.replace(expression, "'", "''");
            Result result = executeQuery(
                "with member [Measures].[Foo] as '"
                + expression
                + "' select {[Measures].[Foo]} on columns from "
                + cubeName);
            Cell cell = result.getCell(new int[]{0});
            if (cell.isError()) {
                throwable = (Throwable) cell.getValue();
            }
        } catch (Throwable e) {
            throwable = e;
        }
        checkThrowable(throwable, pattern);
    }

    /**
     * Returns the name of the default cube.
     *
     * <p>Tests which evaluate scalar expressions, such as
     * {@link #assertExprReturns(String, String)}, generate queries against this
     * cube.
     *
     * @return the name of the default cube
     */
    public String getDefaultCubeName() {
        return "Sales";
    }

    /**
     * Executes the expression in the context of the cube indicated by
     * <code>cubeName</code>, and returns the result as a Cell.
     *
     * @param expression The expression to evaluate
     * @return Cell which is the result of the expression
     */
    public Cell executeExprRaw(String expression) {
        return executeExprRaw(expression, getDefaultCubeName());
    }

    /**
     * Executes the expression in the default cube, and returns the result as
     * a Cell.
     *
     * @param expression The expression to evaluate
     * @param cubeName Cube name
     * @return Cell which is the result of the expression
     */
    public Cell executeExprRaw(String expression, String cubeName) {
        if (cubeName.indexOf(' ') >= 0) {
            cubeName = Util.quoteMdxIdentifier(cubeName);
        }
        final String queryString =
            "with member [Measures].[Foo] as "
            + Util.singleQuoteString(expression)
            + " select {[Measures].[Foo]} on columns from " + cubeName;
        Result result = executeQuery(queryString);
        return result.getCell(new int[]{0});
    }

    /**
     * Executes an expression and asserts that it returns a given result.
     */
    public void assertExprReturns(String expression, String expected) {
        final Cell cell = executeExprRaw(expression);
        if (expected == null) {
            expected = ""; // null values are formatted as empty string
        }
        assertEqualsVerbose(expected, cell.getFormattedValue());
    }

    /**
     * Executes a query with a given expression on an axis, and asserts that it
     * returns the expected string.
     */
    public void assertAxisReturns(
        String expression,
        String expected)
    {
        Axis axis = executeAxis(expression);
        assertEqualsVerbose(
            expected,
            upgradeActual(toString(axis.getPositions())));
    }

    /**
     * Massages the actual result of executing a query to handle differences in
     * unique names betweeen old and new behavior.
     *
     * <p>Even though the new naming is not enabled by default, reference logs
     * should be in terms of the new naming.
     *
     * @see mondrian.olap.MondrianProperties#SsasCompatibleNaming
     *
     * @param actual Actual result
     * @return Expected result massaged for backwards compatibility
     */
    public String upgradeActual(String actual) {
        if (!MondrianProperties.instance().SsasCompatibleNaming.get()) {
            actual = Util.replace(
                actual,
                "[Time.Weekly]",
                "[Time].[Weekly]");
            actual = Util.replace(
                actual,
                "[All Time.Weeklys]",
                "[All Weeklys]");
            actual = Util.replace(
                actual,
                "<HIERARCHY_NAME>Time.Weekly</HIERARCHY_NAME>",
                "<HIERARCHY_NAME>Weekly</HIERARCHY_NAME>");

            // for a few tests in SchemaTest
            actual = Util.replace(
                actual,
                "[Store.MyHierarchy]",
                "[Store].[MyHierarchy]");
            actual = Util.replace(
                actual,
                "[All Store.MyHierarchys]",
                "[All MyHierarchys]");
            actual = Util.replace(
                actual,
                "[Store2].[All Store2s]",
                "[Store2].[Store].[All Stores]");
            actual = Util.replace(
                actual,
                "[Store Type 2.Store Type 2].[All Store Type 2.Store Type 2s]",
                "[Store Type 2].[All Store Type 2s]");
            actual = Util.replace(
                actual,
                "[TIME.CALENDAR]",
                "[TIME].[CALENDAR]");
        }
        return actual;
    }

    /**
     * Massages an MDX query to handle differences in
     * unique names betweeen old and new behavior.
     *
     * <p>The main difference addressed is with level naming. The problem
     * arises when dimension, hierarchy and level have the same name:<ul>
     *
     * <li>In old behavior, the [Gender].[Gender] represents the Gender level,
     * and [Gender].[Gender].[Gender] is invalid.
     *
     * <li>In new behavior, [Gender].[Gender] represents the Gender hierarchy,
     * and [Gender].[Gender].[Gender].members represents the Gender level.
     * </ul></p>
     *
     * <p>So, {@code upgradeQuery("[Gender]")} returns
     * "[Gender].[Gender]" for old behavior,
     * "[Gender].[Gender].[Gender]" for new behavior.</p>
     *
     * @see mondrian.olap.MondrianProperties#SsasCompatibleNaming
     *
     * @param queryString Original query
     * @return Massaged query for backwards compatibility
     */
    public String upgradeQuery(String queryString) {
        if (MondrianProperties.instance().SsasCompatibleNaming.get()) {
            String[] names = {
                "[Gender]",
                "[Education Level]",
                "[Marital Status]",
                "[Store Type]",
                "[Yearly Income]",
            };
            for (String name : names) {
                queryString = Util.replace(
                    queryString,
                    name + "." + name,
                    name + "." + name + "." + name);
            }
            queryString = Util.replace(
                queryString,
                "[Time.Weekly].[All Time.Weeklys]",
                "[Time].[Weekly].[All Weeklys]");
        }
        return queryString;
    }

    /**
     * Compiles a scalar expression in the context of the default cube.
     *
     * @param expression The expression to evaluate
     * @param scalar Whether the expression is scalar
     * @return String form of the program
     */
    public String compileExpression(String expression, final boolean scalar) {
        String cubeName = getDefaultCubeName();
        if (cubeName.indexOf(' ') >= 0) {
            cubeName = Util.quoteMdxIdentifier(cubeName);
        }
        final String queryString;
        if (scalar) {
            queryString =
                "with member [Measures].[Foo] as "
                + Util.singleQuoteString(expression)
                + " select {[Measures].[Foo]} on columns from " + cubeName;
        } else {
            queryString =
                "SELECT {" + expression + "} ON COLUMNS FROM " + cubeName;
        }
        Connection connection = getConnection();
        Query query = connection.parseQuery(queryString);
        final Exp exp;
        if (scalar) {
            exp = query.formulas[0].getExpression();
        } else {
            exp = query.axes[0].getSet();
        }
        final Calc calc = query.compileExpression(exp, scalar, null);
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        final CalcWriter calcWriter = new CalcWriter(pw);
        calc.accept(calcWriter);
        pw.flush();
        return sw.toString();
    }

    /**
     * Executes a set expression which is expected to return 0 or 1 members.
     * It is an error if the expression returns tuples (as opposed to members),
     * or if it returns two or more members.
     *
     * @param expression Expression string
     * @return Null if axis returns the empty set, member if axis returns one
     *   member. Throws otherwise.
     */
    public Member executeSingletonAxis(String expression) {
        final String cubeName = getDefaultCubeName();
        Result result = executeQuery(
                "select {" + expression + "} on columns from " + cubeName);
        Axis axis = result.getAxes()[0];
        switch (axis.getPositions().size()) {
        case 0:
            // The mdx "{...}" operator eliminates null members (that is,
            // members for which member.isNull() is true). So if "expression"
            // yielded just the null member, the array will be empty.
            return null;
        case 1:
            // Java nulls should never happen during expression evaluation.
            Position position = axis.getPositions().get(0);
            Util.assertTrue(position.size() == 1);
            Member member = position.get(0);
            Util.assertTrue(member != null);
            return member;
        default:
            throw Util.newInternal(
                "expression " + expression
                + " yielded " + axis.getPositions().size() + " positions");
        }
    }

    /**
     * Executes a query with a given expression on an axis, and returns the
     * whole axis.
     */
    public Axis executeAxis(String expression) {
        Result result = executeQuery(
            "select {" + expression
            + "} on columns from " + getDefaultCubeName());
        return result.getAxes()[0];
    }

    /**
     * Executes a query with a given expression on an axis, and asserts that it
     * throws an error which matches a particular pattern. The expression is
     * evaulated against the default cube.
     */
    public void assertAxisThrows(
        String expression,
        String pattern)
    {
        Throwable throwable = null;
        Connection connection = getConnection();
        try {
            final String cubeName = getDefaultCubeName();
            final String queryString =
                    "select {" + expression + "} on columns from " + cubeName;
            Query query = connection.parseQuery(queryString);
            connection.execute(query);
        } catch (Throwable e) {
            throwable = e;
        }
        checkThrowable(throwable, pattern);
    }

    public static void checkThrowable(Throwable throwable, String pattern) {
        if (throwable == null) {
            Assert.fail("query did not yield an exception");
        }
        String stackTrace = getStackTrace(throwable);
        if (stackTrace.indexOf(pattern) < 0) {
            Assert.fail(
                "query's error does not match pattern '" + pattern
                + "'; error is [" + stackTrace + "]");
        }
    }

    /**
     * Returns the output writer.
     */
    public PrintWriter getWriter() {
        return pw;
    }

    /**
     * Executes a query and checks that the result is a given string.
     */
    public void assertQueryReturns(String query, String desiredResult) {
        Result result = executeQuery(query);
        String resultString = toString(result);
        if (desiredResult != null) {
            assertEqualsVerbose(
                desiredResult,
                upgradeActual(resultString));
        }
    }

    /**
     * Executes a very simple query.
     *
     * <p>This forces the schema to be loaded and performs a basic sanity check.
     * If this is a negative schema test, causes schema validation errors to be
     * thrown.
     */
    public void assertSimpleQuery() {
        assertQueryReturns(
            "select from [Sales]",
            "Axis #0:\n"
            + "{}\n"
            + "266,773");
    }

    /**
     * Checks that an actual string matches an expected string.
     *
     * <p>If they do not, throws a {@link junit.framework.ComparisonFailure} and
     * prints the difference, including the actual string as an easily pasted
     * Java string literal.
     */
    public static void assertEqualsVerbose(
        String expected,
        String actual)
    {
        assertEqualsVerbose(expected, actual, true, null);
    }

    /**
     * Checks that an actual string matches an expected string.
     *
     * <p>If they do not, throws a {@link ComparisonFailure} and prints the
     * difference, including the actual string as an easily pasted Java string
     * literal.
     *
     * @param expected Expected string
     * @param actual Actual string
     * @param java Whether to generate actual string as a Java string literal
     * if the values are not equal
     * @param message Message to display, optional
     */
    public static void assertEqualsVerbose(
        String expected,
        String actual,
        boolean java,
        String message)
    {
        assertEqualsVerbose(
            fold(expected), actual, java, message);
    }

    /**
     * Checks that an actual string matches an expected string.
     *
     * <p>If they do not, throws a {@link ComparisonFailure} and prints the
     * difference, including the actual string as an easily pasted Java string
     * literal.
     *
     * @param safeExpected Expected string, where all line endings have been
     * converted into platform-specific line endings
     * @param actual Actual string
     * @param java Whether to generate actual string as a Java string literal
     * if the values are not equal
     * @param message Message to display, optional
     */
    public static void assertEqualsVerbose(
        SafeString safeExpected,
        String actual,
        boolean java,
        String message)
    {
        String expected = safeExpected == null ? null : safeExpected.s;
        if ((expected == null) && (actual == null)) {
            return;
        }
        if ((expected != null) && expected.equals(actual)) {
            return;
        }
        if (message == null) {
            message = "";
        } else {
            message += nl;
        }
        message +=
            "Expected:" + nl + expected + nl
            + "Actual:" + nl + actual + nl;
        if (java) {
            message += "Actual java:" + nl + toJavaString(actual) + nl;
        }
        throw new ComparisonFailure(message, expected, actual);
    }

    private static String toJavaString(String s) {
        // Convert [string with "quotes" split
        // across lines]
        // into ["string with \"quotes\" split\n"
        //                 + "across lines
        //
        s = Util.replace(s, "\"", "\\\"");
        s = LineBreakPattern.matcher(s).replaceAll(lineBreak2);
        s = TabPattern.matcher(s).replaceAll("\\\\t");
        s = "\"" + s + "\"";
        String spurious = nl + indent + "+ \"\"";
        if (s.endsWith(spurious)) {
            s = s.substring(0, s.length() - spurious.length());
        }
        if (s.indexOf(lineBreak3) >= 0) {
            s = "fold(" + nl + indent + s + ")";
        }
        return s;
    }

    /**
     * Checks that an actual string matches an expected pattern.
     * If they do not, throws a {@link ComparisonFailure} and prints the
     * difference, including the actual string as an easily pasted Java string
     * literal.
     */
    public void assertMatchesVerbose(
        Pattern expected,
        String actual)
    {
        Util.assertPrecondition(expected != null, "expected != null");
        if (expected.matcher(actual).matches()) {
            return;
        }
        String s = actual;

        // Convert [string with "quotes" split
        // across lines]
        // into ["string with \"quotes\" split" + nl +
        // "across lines
        //
        s = Util.replace(s, "\"", "\\\"");
        s = LineBreakPattern.matcher(s).replaceAll(lineBreak);
        s = TabPattern.matcher(s).replaceAll("\\\\t");
        s = "\"" + s + "\"";
        final String spurious = " + " + nl + "\"\"";
        if (s.endsWith(spurious)) {
            s = s.substring(0, s.length() - spurious.length());
        }
        String message =
            "Expected pattern:" + nl + expected + nl
            + "Actual: " + nl + actual + nl
            + "Actual java: " + nl + s + nl;
        throw new ComparisonFailure(message, expected.pattern(), actual);
    }

    /**
     * Converts a {@link Throwable} to a stack trace.
     */
    public static String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    /**
     * Converts a {@link mondrian.olap.Result} to text in traditional format.
     *
     * <p>For more exotic formats, see
     * {@link org.olap4j.layout.CellSetFormatter}.
     *
     * @param result Query result
     * @return Result as text
     */
    public static String toString(Result result) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        result.print(pw);
        pw.flush();
        return sw.toString();
    }

    /**
     * Returns a test context whose {@link #getOlap4jConnection()} method always
     * returns the same connection object, and which has an active
     * {@link org.olap4j.Scenario}, thus enabling writeback.
     *
     * @return Test context with active scenario
     */
    public TestContext withScenario() {
        return new DelegatingTestContext(this)
        {
            OlapConnection connection;

            public OlapConnection getOlap4jConnection() throws SQLException {
                if (connection == null) {
                    connection = super.getOlap4jConnection();
                    connection.setScenario(
                        connection.createScenario());
                }
                return connection;
            }
        };
    }

    /**
     * Converts a set of positions into a string. Useful if you want to check
     * that an axis has the results you expected.
     */
    public static String toString(List<Position> positions) {
        StringBuilder buf = new StringBuilder();
        int i = 0;
        for (Position position : positions) {
            if (i > 0) {
                buf.append(nl);
            }
            if (position.size() != 1) {
                buf.append("{");
            }
            for (int j = 0; j < position.size(); j++) {
                Member member = position.get(j);
                if (j > 0) {
                    buf.append(", ");
                }
                buf.append(member.getUniqueName());
            }
            if (position.size() != 1) {
                buf.append("}");
            }
            i++;
        }
        return buf.toString();
    }

    /**
     * Wrapper around a string that indicates that all line endings have been
     * converted to platform-specific line endings.
     *
     * @see TestContext#fold
     */
    public static class SafeString {
        public final String s;

        private SafeString(String s) {
            this.s = s;
        }
    }

    /**
     * Converts a string constant into platform-specific line endings.
     *
     * @param string String where line endings are represented as linefeed "\n"
     * @return String where all linefeeds have been converted to
     * platform-specific (CR+LF on Windows, LF on Unix/Linux)
     */
    public static SafeString fold(String string) {
        if (!nl.equals("\n")) {
            string = Util.replace(string, "\n", nl);
        }
        if (string == null) {
            return null;
        } else {
            return new SafeString(string);
        }
    }

    /**
     * Reverses the effect of {@link #fold}; converts platform-specific line
     * endings in a string info linefeeds.
     *
     * @param string String where all linefeeds have been converted to
     * platform-specific (CR+LF on Windows, LF on Unix/Linux)
     * @return String where line endings are represented as linefeed "\n"
     */
    public static String unfold(String string) {
        if (!nl.equals("\n")) {
            string = Util.replace(string, nl, "\n");
        }
        if (string == null) {
            return null;
        } else {
            return string;
        }
    }

    public synchronized Dialect getDialect() {
        if (dialect == null) {
            dialect = getDialectInternal();
        }
        return dialect;
    }

    private Dialect getDialectInternal() {
        DataSource dataSource = getConnection().getDataSource();
        return DialectManager.createDialect(dataSource, null);
    }

    /**
     * Creates a dialect without using a connection.
     *
     * @return dialect of an Access persuasion
     */
    public static Dialect getFakeDialect()
    {
        final DatabaseMetaData metaData =
            (DatabaseMetaData) Proxy.newProxyInstance(
                null,
                new Class<?>[] {DatabaseMetaData.class},
                new DelegatingInvocationHandler() {
                    public boolean supportsResultSetConcurrency(
                        int type, int concurrency)
                    {
                        return false;
                    }
                    public String getDatabaseProductName() {
                        return "Access";
                    }
                    public String getIdentifierQuoteString() {
                        return "\"";
                    }
                    public String getDatabaseProductVersion() {
                        return "1.0";
                    }
                    public boolean isReadOnly() {
                        return true;
                    }
                }
            );
        final java.sql.Connection connection =
            (java.sql.Connection) Proxy.newProxyInstance(
                null,
                new Class<?>[] {java.sql.Connection.class},
                new DelegatingInvocationHandler() {
                    public DatabaseMetaData getMetaData() {
                        return metaData;
                    }
                }
            );
        return DialectManager.createDialect(null, connection);
    }

    /**
     * Checks that expected SQL equals actual SQL.
     * Performs some normalization on the actual SQL to compensate for
     * differences between dialects.
     */
    public void assertSqlEquals(
        String expectedSql,
        String actualSql,
        int expectedRows) throws Exception
    {
        // if the actual SQL isn't in the current dialect we have some
        // problems... probably with the dialectize method
        assertEqualsVerbose(actualSql, dialectize(actualSql));

        String transformedExpectedSql = removeQuotes(dialectize(expectedSql));
        String transformedActualSql = removeQuotes(actualSql);

        Assert.assertEquals(transformedExpectedSql, transformedActualSql);

        checkSqlAgainstDatasource(actualSql, expectedRows);
    }

    private static String removeQuotes(String actualSql) {
        String transformedActualSql = actualSql.replaceAll("`", "");
        transformedActualSql = transformedActualSql.replaceAll("\"", "");
        return transformedActualSql;
    }

    /**
     * Converts a SQL string into the current dialect.
     *
     * <p>This is not intended to be a general purpose method: it looks for
     * specific patterns known to occur in tests, in particular "=as=" and
     * "fname + ' ' + lname".
     *
     * @param sql SQL string in generic dialect
     * @return SQL string converted into current dialect
     */
    private String dialectize(String sql) {
        final String search = "fname \\+ ' ' \\+ lname";
        final Dialect dialect = getDialect();
        final Dialect.DatabaseProduct databaseProduct =
            dialect.getDatabaseProduct();
        switch (databaseProduct) {
        case MYSQL:
            // Mysql would generate "CONCAT(...)"
            sql = sql.replaceAll(
                search,
                "CONCAT(`customer`.`fname`, ' ', `customer`.`lname`)");
            break;
        case POSTGRESQL:
        case ORACLE:
        case LUCIDDB:
        case TERADATA:
            sql = sql.replaceAll(
                search,
                "`fname` || ' ' || `lname`");
            break;
        case DERBY:
            sql = sql.replaceAll(
                search,
                "`customer`.`fullname`");
            break;
        case INGRES:
            sql = sql.replaceAll(
                search,
                "fullname");
            break;
        case DB2:
        case DB2_AS400:
        case DB2_OLD_AS400:
            sql = sql.replaceAll(
                search,
                "CONCAT(CONCAT(`customer`.`fname`, ' '), `customer`.`lname`)");
            break;
        }

        if (dialect.getDatabaseProduct() == Dialect.DatabaseProduct.ORACLE) {
            // " + tableQualifier + "
            sql = sql.replaceAll(" =as= ", " ");
        } else {
            sql = sql.replaceAll(" =as= ", " as ");
        }
        return sql;
    }

    private void checkSqlAgainstDatasource(
        String actualSql,
        int expectedRows)
        throws Exception
    {
        Util.PropertyList connectProperties = getFoodMartConnectionProperties();

        java.sql.Connection jdbcConn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            String jdbcDrivers =
                connectProperties.get(
                    RolapConnectionProperties.JdbcDrivers.name());
            if (jdbcDrivers != null) {
                RolapUtil.loadDrivers(jdbcDrivers);
            }
            final String jdbcDriversProp =
                MondrianProperties.instance().JdbcDrivers.get();
            RolapUtil.loadDrivers(jdbcDriversProp);

            jdbcConn = java.sql.DriverManager.getConnection(
                connectProperties.get(RolapConnectionProperties.Jdbc.name()),
                connectProperties.get(
                    RolapConnectionProperties.JdbcUser.name()),
                connectProperties.get(
                    RolapConnectionProperties.JdbcPassword.name()));
            stmt = jdbcConn.createStatement();

            if (RolapUtil.SQL_LOGGER.isDebugEnabled()) {
                StringBuffer sqllog = new StringBuffer();
                sqllog.append("mondrian.test.TestContext: executing sql [");
                if (actualSql.indexOf('\n') >= 0) {
                    // SQL appears to be formatted as multiple lines. Make it
                    // start on its own line.
                    sqllog.append("\n");
                }
                sqllog.append(actualSql);
                sqllog.append(']');
                RolapUtil.SQL_LOGGER.debug(sqllog.toString());
            }

            long startTime = System.currentTimeMillis();
            rs = stmt.executeQuery(actualSql);
            long time = System.currentTimeMillis();
            final long execMs = time - startTime;
            Util.addDatabaseTime(execMs);

            RolapUtil.SQL_LOGGER.debug(", exec " + execMs + " ms");

            int rows = 0;
            while (rs.next()) {
                rows++;
            }

            Assert.assertEquals("row count", expectedRows, rows);
        } catch (SQLException e) {
            throw new Exception(
                "ERROR in SQL - invalid for database: "
                + connectProperties.get(RolapConnectionProperties.Jdbc.name())
                + "\n" + actualSql,
                e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e1) {
                // ignore
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception e1) {
                // ignore
            }
            try {
                if (jdbcConn != null) {
                    jdbcConn.close();
                }
            } catch (Exception e1) {
                // ignore
            }
        }
    }

    /**
     * Asserts that an MDX set-valued expression depends upon a given list of
     * dimensions.
     */
    public void assertSetExprDependsOn(String expr, String dimList) {
        // Construct a query, and mine it for a parsed expression.
        // Use a fresh connection, because some tests define their own dims.
        final Connection connection = getFoodMartConnection();
        final String queryString =
                "SELECT {" + expr + "} ON COLUMNS FROM [Sales]";
        final Query query = connection.parseQuery(queryString);
        query.resolve();
        final Exp expression = query.getAxes()[0].getSet();

        // Build a list of the dimensions which the expression depends upon,
        // and check that it is as expected.
        checkDependsOn(query, expression, dimList, false);
    }

    /**
     * Asserts that an MDX member-valued depends upon a given list of
     * dimensions.
     */
    public void assertMemberExprDependsOn(String expr, String dimList) {
        assertSetExprDependsOn("{" + expr + "}", dimList);
    }

    /**
     * Asserts that an MDX expression depends upon a given list of dimensions.
     */
    public void assertExprDependsOn(String expr, String dimList) {
        // Construct a query, and mine it for a parsed expression.
        // Use a fresh connection, because some tests define their own dims.
        final Connection connection = getFoodMartConnection();
        final String queryString =
            "WITH MEMBER [Measures].[Foo] AS "
            + Util.singleQuoteString(expr)
            + " SELECT FROM [Sales]";
        final Query query = connection.parseQuery(queryString);
        query.resolve();
        final Formula formula = query.getFormulas()[0];
        final Exp expression = formula.getExpression();

        // Build a list of the dimensions which the expression depends upon,
        // and check that it is as expected.
        checkDependsOn(query, expression, dimList, true);
    }

    private void checkDependsOn(
        final Query query,
        final Exp expression,
        String expectedDimList,
        final boolean scalar)
    {
        final Calc calc =
            query.compileExpression(
                expression,
                scalar,
                scalar ? null : ResultStyle.ITERABLE);
        final Dimension[] dimensions = query.getCube().getDimensions();
        StringBuilder buf = new StringBuilder("{");
        int dependCount = 0;
        for (Dimension dimension : dimensions) {
            if (calc.dependsOn(dimension)) {
                if (dependCount++ > 0) {
                    buf.append(", ");
                }
                buf.append(dimension.getUniqueName());
            }
        }
        buf.append("}");
        String actualDimList = buf.toString();
        Assert.assertEquals(expectedDimList, actualDimList);
    }

    /**
     * Creates a TestContext which is based on a variant of the FoodMart
     * schema, which parameter, cube, named set, and user-defined function
     * definitions added.
     *
     * @param parameterDefs Parameter definitions. If not null, the string is
     *   is inserted into the schema XML in the appropriate place for
     *   parameter definitions.
     * @param cubeDefs Cube definition(s). If not null, the string is
     *   is inserted into the schema XML in the appropriate place for
     *   cube definitions.
     * @param virtualCubeDefs Definitions of virtual cubes. If not null, the
     *   string is inserted into the schema XML in the appropriate place for
     *   virtual cube definitions.
     * @param namedSetDefs Definitions of named sets. If not null, the string
     *   is inserted into the schema XML in the appropriate place for
     *   named set definitions.
     * @param udfDefs Definitions of user-defined functions. If not null, the
     *   string is inserted into the schema XML in the appropriate place for
     *   UDF definitions.
     * @param roleDefs Definitions of roles
     * @return TestContext which reads from a slightly different hymnbook
     */
    public static TestContext create(
        final String parameterDefs,
        final String cubeDefs,
        final String virtualCubeDefs,
        final String namedSetDefs,
        final String udfDefs,
        final String roleDefs)
    {
        return new TestContext() {
            public Util.PropertyList getFoodMartConnectionProperties() {
                final String schema = getFoodMartSchema(
                    parameterDefs, cubeDefs, virtualCubeDefs, namedSetDefs,
                    udfDefs, roleDefs);
                Util.PropertyList properties =
                    super.getFoodMartConnectionProperties();
                properties.put(
                    RolapConnectionProperties.CatalogContent.name(),
                    schema);
                return properties;
            }
        };
    }

    /**
     * Creates a TestContext which contains the given schema text.
     *
     * @return TestContext which contains the given schema
     */
    public static TestContext create(final String schema) {
        return new TestContext() {
            public Util.PropertyList getFoodMartConnectionProperties() {
                Util.PropertyList properties =
                    super.getFoodMartConnectionProperties();
                properties.put(
                    RolapConnectionProperties.CatalogContent.name(),
                    schema);
                return properties;
            }
        };
    }

    /**
     * Creates a TestContext, adding hierarchy definitions to a cube definition.
     *
     * @param cubeName Name of a cube in the schema (cube must exist)
     * @param dimensionDefs String defining dimensions, or null
     * @return TestContext with modified cube defn
     */
    public static TestContext createSubstitutingCube(
        final String cubeName,
        final String dimensionDefs)
    {
        return createSubstitutingCube(cubeName, dimensionDefs, null);
    }

    /**
     * Creates a TestContext, adding hierarchy and calculated member definitions
     * to a cube definition.
     *
     * @param cubeName Name of a cube in the schema (cube must exist)
     * @param dimensionDefs String defining dimensions, or null
     * @param memberDefs String defining calculated members, or null
     * @return TestContext with modified cube defn
     */
    public static TestContext createSubstitutingCube(
        final String cubeName,
        final String dimensionDefs,
        final String memberDefs)
    {
        return createSubstitutingCube(
            cubeName, dimensionDefs, null, memberDefs, null);
    }


    /**
     * Creates a TestContext, adding hierarchy and calculated member definitions
     * to a cube definition.
     *
     * @param cubeName Name of a cube in the schema (cube must exist)
     * @param dimensionDefs String defining dimensions, or null
     * @param measureDefs String defining measures, or null
     * @param memberDefs String defining calculated members, or null
     * @param namedSetDefs String defining named set definitions, or null
     * @return TestContext with modified cube defn
     */
    public static TestContext createSubstitutingCube(
        final String cubeName,
        final String dimensionDefs,
        final String measureDefs,
        final String memberDefs,
        final String namedSetDefs)
    {
        return new TestContext() {
            public Util.PropertyList getFoodMartConnectionProperties() {
                final String schema =
                    getFoodMartSchemaSubstitutingCube(
                        cubeName, dimensionDefs,
                        measureDefs, memberDefs, namedSetDefs);
                Util.PropertyList properties =
                    super.getFoodMartConnectionProperties();
                properties.put(
                    RolapConnectionProperties.CatalogContent.name(),
                    schema);
                return properties;
            }
        };
    }

    /**
     * Returns a TestContext similar to this one, but using the given role.
     *
     * @param roleName Role name
     * @return Test context with the given role
     */
    public TestContext withRole(final String roleName) {
        return new DelegatingTestContext(this) {
            public Util.PropertyList getFoodMartConnectionProperties() {
                Util.PropertyList properties =
                    context.getFoodMartConnectionProperties();
                properties.put(
                    RolapConnectionProperties.Role.name(),
                    roleName);
                return properties;
            }
        };
    }

    /**
     * Returns a TestContext similar to this one, but using the given cube as
     * default for tests such as {@link #assertExprReturns(String, String)}.
     *
     * @param cubeName Cube name
     * @return Test context with the given default cube
     */
    public TestContext withCube(final String cubeName) {
        return new DelegatingTestContext(this) {
            public String getDefaultCubeName() {
                return cubeName;
            }
        };
    }

    /**
     * Generates a string containing all dimensions except those given.
     * Useful as an argument to {@link #assertExprDependsOn(String, String)}.
     *
     * @return string containing all dimensions except those given
     */
    public static String allDimsExcept(String ... dims) {
        for (String dim : dims) {
            assert contains(AllDims, dim) : "unknown dimension " + dim;
        }
        StringBuilder buf = new StringBuilder("{");
        int j = 0;
        for (String dim : AllDims) {
            if (!contains(dims, dim)) {
                if (j++ > 0) {
                    buf.append(", ");
                }
                buf.append(dim);
            }
        }
        buf.append("}");
        return buf.toString();
    }

    public static boolean contains(String[] a, String s) {
        for (String anA : a) {
            if (anA.equals(s)) {
                return true;
            }
        }
        return false;
    }

    public static String allDims() {
        return allDimsExcept();
    }

    /**
     * Creates a FoodMart connection with "Ignore=true" and returns the list
     * of warnings in the schema.
     *
     * @return Warnings encountered while loading schema
     */
    public List<Exception> getSchemaWarnings() {
        final Connection connection =
            new DelegatingTestContext(this) {
                public Util.PropertyList getFoodMartConnectionProperties() {
                    final Util.PropertyList propertyList =
                        super.getFoodMartConnectionProperties();
                    propertyList.put(
                        RolapConnectionProperties.Ignore.name(),
                        "true");
                    return propertyList;
                }
            }
            .getFoodMartConnection();
        return connection.getSchema().getWarnings();
    }

    public OlapConnection getOlap4jConnection() throws SQLException {
        try {
            Class.forName("mondrian.olap4j.MondrianOlap4jDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver not found");
        }
        String connectString = getConnectString();
        if (connectString.startsWith("Provider=mondrian; ")) {
            connectString =
                connectString.substring("Provider=mondrian; ".length());
        }
        final java.sql.Connection connection =
            java.sql.DriverManager.getConnection(
                "jdbc:mondrian:" + connectString);
        return ((OlapWrapper) connection).unwrap(OlapConnection.class);
    }

    /**
     * Tests whether the database is valid. Allows tests that depend on optional
     * databases to figure out whether to proceed.
     *
     * @return whether a database is present and correct
     */
    public boolean databaseIsValid() {
        try {
            Connection connection = getConnection();
            String cubeName = getDefaultCubeName();
            if (cubeName.indexOf(' ') >= 0) {
                cubeName = Util.quoteMdxIdentifier(cubeName);
            }
            Query query = connection.parseQuery("select from " + cubeName);
            Result result = connection.execute(query);
            Util.discard(result);
            connection.close();
            return true;
        } catch (RuntimeException e) {
            Util.discard(e);
            return false;
        }
    }

    public static class SnoopingSchemaProcessor
        extends FilterDynamicSchemaProcessor
    {
        private static String catalogContent;

        protected String filter(
            String schemaUrl,
            Util.PropertyList connectInfo,
            InputStream stream) throws Exception
        {
            catalogContent = super.filter(schemaUrl, connectInfo, stream);
            return catalogContent;
        }
    }

    /**
     * Schema processor that flags dimensions as high-cardinality if they
     * appear in the list of values in the
     * {@link MondrianProperties#TestHighCardinalityDimensionList} property.
     * It's a convenient way to run the whole suite against high-cardinality
     * dimensions without modifying FoodMart.xml.
     */
    public static class HighCardDynamicSchemaProcessor
        extends FilterDynamicSchemaProcessor
    {
        protected String filter(
            String schemaUrl, Util.PropertyList connectInfo, InputStream stream)
            throws Exception
        {
            String s = super.filter(schemaUrl, connectInfo, stream);
            final String highCardDimensionList =
                MondrianProperties.instance()
                    .TestHighCardinalityDimensionList.get();
            if (highCardDimensionList != null
                && !highCardDimensionList.equals(""))
            {
                for (String dimension : highCardDimensionList.split(",")) {
                    final String match =
                        "<Dimension name=\"" + dimension + "\"";
                    s = s.replaceAll(
                        match, match + " highCardinality=\"true\"");
                }
            }
            return s;
        }
    }
}

// End TestContext.java
