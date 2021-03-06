/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/Annotation.java#1 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2009-2009 Julian Hyde and others
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/

package mondrian.olap;

/**
 * User-defined property on a metadata element.
 *
 * @see mondrian.olap.Annotated
 *
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/Annotation.java#1 $
 * @author jhyde
 */
public interface Annotation {
    /**
     * Returns the name of this annotation. Must be unique within its element.
     *
     * @return Annotation name
     */
    String getName();

    /**
     * Returns the value of this annotation. Usually a string.
     *
     * @return Annotation value
     */
    Object getValue();
}

// End Annotation.java
