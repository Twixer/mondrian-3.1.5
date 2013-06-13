/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2008-2009 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.spi.impl;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Implementation of {@link mondrian.spi.Dialect} for the Firebird database.
 *
 * @author jhyde
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/spi/impl/FirebirdDialect.java#4 $
 * @since Nov 23, 2008
 */
public class FirebirdDialect extends JdbcDialectImpl {

    public static final JdbcDialectFactory FACTORY =
        new JdbcDialectFactory(
            FirebirdDialect.class,
            DatabaseProduct.FIREBIRD);

    /**
     * Creates a FirebirdDialect.
     *
     * @param connection Connection
     */
    public FirebirdDialect(Connection connection) throws SQLException {
        super(connection);
    }

    public boolean allowsAs() {
        return false;
    }

    @Override
    protected String generateOrderByNullsLast(String expr, boolean ascending) {
        return generateOrderByNullsLastAnsi(expr, ascending);
    }
}

// End FirebirdDialect.java
