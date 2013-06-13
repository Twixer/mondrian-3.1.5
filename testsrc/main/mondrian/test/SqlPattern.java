/*
// $Id: //open/mondrian-release/3.1/testsrc/main/mondrian/test/SqlPattern.java#2 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2004-2009 Julian Hyde and others
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.test;

import mondrian.olap.Util;
import mondrian.spi.Dialect;

import java.util.Set;

/**
 * Pattern for a SQL statement (or fragment thereof) expected to be produced
 * during the course of running a test.
 *
 * <p>A pattern contains a dialect. This allows a test to run against different
 * dialects.
 *
 * @see mondrian.spi.Dialect
 *
 * @version $Id: //open/mondrian-release/3.1/testsrc/main/mondrian/test/SqlPattern.java#2 $
 * @author jhyde
*/
public class SqlPattern {
    private final String sql;
    private final String triggerSql;
    private final Set<Dialect.DatabaseProduct> databaseProducts;

    /**
     * Creates a pattern which applies to a collection of dialects
     * and is triggered by the first N characters of the expected statement.
     *
     * @param databaseProducts Set of dialects
     * @param sql SQL statement
     * @param startsWithLen Length of prefix of statement to consider
     */
    public SqlPattern(
        Set<Dialect.DatabaseProduct> databaseProducts,
        String sql,
        int startsWithLen)
    {
        this(databaseProducts, sql, sql.substring(0, startsWithLen));
    }

    /**
     * Creates a pattern which applies to one or more dialects
     * and is triggered by the first N characters of the expected statement.
     *
     * @param databaseProduct Dialect
     * @param sql SQL statement
     * @param startsWithLen Length of prefix of statement to consider
     */
    public SqlPattern(
        Dialect.DatabaseProduct databaseProduct,
        final String sql,
        final int startsWithLen)
    {
        this(databaseProduct, sql, sql.substring(0, startsWithLen));
    }

    /**
     * Creates a pattern which applies to one or more dialects.
     *
     * @param databaseProduct Dialect
     * @param sql SQL statement
     * @param triggerSql Prefix of SQL statement which triggers a match; null
     *                   means whole statement
     */
    public SqlPattern(
        Dialect.DatabaseProduct databaseProduct,
         final String sql,
         final String triggerSql)
    {
        this(Util.enumSetOf(databaseProduct), sql, triggerSql);
    }

    /**
     * Creates a pattern which applies a collection of dialects.
     *
     * @param databaseProducts Set of dialects
     * @param sql SQL statement
     * @param triggerSql Prefix of SQL statement which triggers a match; null
     *                   means whole statement
     */
    public SqlPattern(
        Set<Dialect.DatabaseProduct> databaseProducts,
        String sql,
        String triggerSql)
    {
        this.databaseProducts = databaseProducts;
        this.sql = sql;
        this.triggerSql = triggerSql != null ? triggerSql : sql;
    }

    public static SqlPattern getPattern(
        Dialect.DatabaseProduct d,
        SqlPattern[] patterns)
    {
        if (d == Dialect.DatabaseProduct.UNKNOWN) {
            return null;
        }
        for (SqlPattern pattern : patterns) {
            if (pattern.hasDatabaseProduct(d)) {
                return pattern;
            }
        }
        return null;
    }

    public boolean hasDatabaseProduct(Dialect.DatabaseProduct databaseProduct) {
        return databaseProducts.contains(databaseProduct);
    }

    public String getSql() {
        return sql;
    }

    public String getTriggerSql() {
        return triggerSql;
    }
}

// End SqlPattern.java
