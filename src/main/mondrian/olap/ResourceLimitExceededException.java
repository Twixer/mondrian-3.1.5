/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/ResourceLimitExceededException.java#2 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2004-2005 TONBELLER AG
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.olap;

/**
 * Exception which indicates some resource limit was exceeded.
 *
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/ResourceLimitExceededException.java#2 $
 */
public class ResourceLimitExceededException
    extends ResultLimitExceededException
{
    /**
     * Creates a ResourceLimitExceededException
     *
     * @param message Localized message
     */
    public ResourceLimitExceededException(String message) {
        super(message);
    }
}

// End ResourceLimitExceededException.java
