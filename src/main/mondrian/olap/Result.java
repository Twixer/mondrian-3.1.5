/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/Result.java#2 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2001-2002 Kana Software, Inc.
// Copyright (C) 2001-2005 Julian Hyde and others
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
//
// jhyde, 6 August, 2001
*/

package mondrian.olap;

import java.io.PrintWriter;

/**
 * A <code>Result</code> is the result of running an MDX query. See {@link
 * Connection#execute}.
 *
 * @author jhyde
 * @since 6 August, 2001
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/Result.java#2 $
 */
public interface Result {
    /** Returns the query which generated this result. */
    Query getQuery();
    /** Returns the non-slicer axes. */
    Axis[] getAxes();
    /** Returns the slicer axis. */
    Axis getSlicerAxis();
    /** Returns the cell at a given set of coordinates. For example, in a result
     * with 4 columns and 6 rows, the top-left cell has coordinates [0, 0],
     * and the bottom-right cell has coordinates [3, 5]. */
    Cell getCell(int[] pos);
    void print(PrintWriter pw);
    void close();
}

// End Result.java
