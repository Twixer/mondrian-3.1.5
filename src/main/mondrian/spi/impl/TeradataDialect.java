/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2008-2009 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.spi.impl;

import mondrian.spi.Dialect;

import java.util.List;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Implementation of {@link mondrian.spi.Dialect} for the Teradata database.
 *
 * @author jhyde
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/spi/impl/TeradataDialect.java#2 $
 * @since Nov 23, 2008
 */
public class TeradataDialect extends JdbcDialectImpl {

    public static final JdbcDialectFactory FACTORY =
        new JdbcDialectFactory(
            TeradataDialect.class,
            DatabaseProduct.TERADATA);

    /**
     * Creates a TeradataDialect.
     *
     * @param connection Connection
     */
    public TeradataDialect(Connection connection) throws SQLException {
        super(connection);
    }

    public boolean requiresAliasForFromQuery() {
        return true;
    }

    public String generateInline(
        List<String> columnNames,
        List<String> columnTypes,
        List<String[]> valueList)
    {
        String fromClause = null;
        if (valueList.size() > 1) {
            // In Teradata, "SELECT 1,2" is valid but "SELECT 1,2 UNION
            // SELECT 3,4" gives "3888: SELECT for a UNION,INTERSECT or
            // MINUS must reference a table."
            fromClause = " FROM (SELECT 1 a) z ";
        }
        return generateInlineGeneric(
            columnNames, columnTypes, valueList, fromClause, true);
    }

    public boolean supportsGroupingSets() {
        return true;
    }

    public NullCollation getNullCollation() {
        return NullCollation.NEGINF;
    }

    public String generateOrderItem(
        String expr, boolean nullable, boolean ascending)
    {
        if (nullable && ascending) {
            return "CASE WHEN " + expr + " IS NULL THEN 1 ELSE 0 END, " + expr
                + " ASC";
        } else {
            return super.generateOrderItem(expr, nullable, ascending);
        }
    }

    public boolean requiresUnionOrderByOrdinal() {
        return true;
    }
}

// End TeradataDialect.java
