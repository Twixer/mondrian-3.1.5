/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/QueryCanceledException.java#2 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2004-2005 TONBELLER AG
// Copyright (C) 2005-2007 Julian Hyde and others
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.olap;

/**
 * Exception which indicates that a query was canceled by an end-user.
 *
 * <p>See also {@link mondrian.olap.QueryTimeoutException}, which indicates that
 * a query was canceled automatically due to a timeout.
 *
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/QueryCanceledException.java#2 $
 */
public class QueryCanceledException extends ResultLimitExceededException {
    /**
     * Creates a QueryCanceledException.
     *
     * @param message Localized error message
     */
    public QueryCanceledException(String message) {
        super(message);
    }
}

// End QueryCanceledException.java
