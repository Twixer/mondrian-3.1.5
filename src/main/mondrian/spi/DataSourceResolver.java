/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/spi/DataSourceResolver.java#1 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2009-2009 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.spi;

import javax.sql.DataSource;

/**
 * Plugin class that resolves data source name to {@link javax.sql.DataSource}
 * object.
 *
 * <p>The property
 * {@link mondrian.olap.MondrianProperties#DataSourceResolverClass} determines
 * which class to use. The default implementation is
 * {@link mondrian.spi.impl.JndiDataSourceResolver}.
 *
 * @author jhyde
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/spi/DataSourceResolver.java#1 $
 */
public interface DataSourceResolver {

    /**
     * Converts a data source name to a JDBC data source object.
     *
     * @param dataSourceName Data source name
     * @return JDBC data source, or null if not found
     * @throws Exception on error
     */
    DataSource lookup(String dataSourceName) throws Exception;

}

// End DataSourceResolver.java
