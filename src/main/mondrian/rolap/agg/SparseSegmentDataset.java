/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/rolap/agg/SparseSegmentDataset.java#2 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2002-2002 Kana Software, Inc.
// Copyright (C) 2002-2007 Julian Hyde and others
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
//
// jhyde, 21 March, 2002
*/
package mondrian.rolap.agg;

import mondrian.olap.Util;
import mondrian.rolap.CellKey;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A <code>SparseSegmentDataset</code> is a means of storing segment values
 * which is suitable when few of the combinations of keys have a value present.
 *
 * <p>The storage requirements are as follows. Key is 1 word for each
 * dimension. Hashtable entry is 3 words. Value is 1 word. Total space is (4 +
 * d) * v. (May also need hash table to ensure that values are only stored
 * once.)</p>
 *
 * <p>NOTE: This class is not synchronized.</p>
 *
 * @author jhyde
 * @since 21 March, 2002
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/rolap/agg/SparseSegmentDataset.java#2 $
 */
class SparseSegmentDataset implements SegmentDataset {
    private final Map<CellKey, Object> values = new HashMap<CellKey, Object>();

    SparseSegmentDataset(Segment segment) {
        Util.discard(segment);
    }

    public Object get(CellKey pos) {
        return values.get(pos);
    }

    public void put(CellKey key, Object value) {
        values.put(key, value);
    }

    public Iterator<Map.Entry<CellKey, Object>> iterator() {
        return values.entrySet().iterator();
    }

    public double getBytes() {
        // assume a slot, key, and value are each 4 bytes
        return values.size() * 12;
    }
}

// End SparseSegmentDataset.java
