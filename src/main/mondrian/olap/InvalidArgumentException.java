/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2005-2007 Julian Hyde and others
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.olap;

/**
 * Exception which indicates that an argument is invalid
 *
 * @author Thiyagu
 * @since April 5, 2007
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/InvalidArgumentException.java#2 $
 */
public class InvalidArgumentException extends MondrianException {
    /**
     * Creates a InvalidArgumentException.
     *
     * @param message Localized error message
     */
    public InvalidArgumentException(String message) {
        super(message);
    }
}

// End InvalidArgumentException.java