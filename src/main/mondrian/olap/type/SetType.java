/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/type/SetType.java#3 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2005-2009 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.olap.type;

import mondrian.olap.*;

import java.util.List;

/**
 * Set type.
 *
 * @author jhyde
 * @since Feb 17, 2005
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/type/SetType.java#3 $
 */
public class SetType implements Type {

    private final Type elementType;
    private final String digest;

    /**
     * Creates a type representing a set of elements of a given type.
     *
     * @param elementType The type of the elements in the set, or null if not
     *   known
     */
    public SetType(Type elementType) {
        if (elementType != null) {
            assert elementType instanceof MemberType
                || elementType instanceof TupleType;
        }
        this.elementType = elementType;
        this.digest = "SetType<" + elementType + ">";
    }

    public int hashCode() {
        return digest.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof SetType) {
            SetType that = (SetType) obj;
            return Util.equals(this.elementType, that.elementType);
        } else {
            return false;
        }
    }

    public String toString() {
        return digest;
    }

    /**
     * Returns the type of the elements of this set.
     *
     * @return the type of the elements in this set
     */
    public Type getElementType() {
        return elementType;
    }

    public boolean usesDimension(Dimension dimension, boolean definitely) {
        if (elementType == null) {
            return definitely;
        }
        return elementType.usesDimension(dimension, definitely);
    }

    public Dimension getDimension() {
        return elementType == null
            ? null
            : elementType.getDimension();
    }

    public Hierarchy getHierarchy() {
        return elementType == null
            ? null
            : elementType.getHierarchy();
    }

    public Level getLevel() {
        return elementType == null
            ? null
            : elementType.getLevel();
    }

    /**
     * Returns the dimensionality of this SetType. If set contains members,
     * returns 1, otherwise returns the width of the tuples.
     *
     * @return Dimensionality of this SetType
     */
    public int getArity() {
        return elementType instanceof TupleType
            ? ((TupleType) elementType).elementTypes.length
            : 1;
    }

    public Type computeCommonType(Type type, int[] conversionCount) {
        if (!(type instanceof SetType)) {
            return null;
        }
        SetType that = (SetType) type;
        final Type mostGeneralElementType =
            this.getElementType().computeCommonType(
                that.getElementType(), conversionCount);
        if (mostGeneralElementType == null) {
            return null;
        }
        return new SetType(mostGeneralElementType);
    }

    public boolean isInstance(Object value) {
        if (!(value instanceof List)) {
            return false;
        }
        List list = (List) value;
        for (Object o : list) {
            if (!elementType.isInstance(o)) {
                return false;
            }
        }
        return true;
    }
}

// End SetType.java
