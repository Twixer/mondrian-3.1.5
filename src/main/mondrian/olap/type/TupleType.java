/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/type/TupleType.java#3 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2005-2009 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.olap.type;

import mondrian.olap.*;
import mondrian.resource.MondrianResource;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * Tuple type.
 *
 * @author jhyde
 * @since Feb 17, 2005
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/type/TupleType.java#3 $
 */
public class TupleType implements Type {
    public final Type[] elementTypes;
    private final String digest;

    /**
     * Creates a type representing a tuple whose fields are the given types.
     *
     * @param elementTypes Array of types of the members in this tuple
     */
    public TupleType(Type[] elementTypes) {
        assert elementTypes != null;
        this.elementTypes = elementTypes.clone();

        final StringBuilder buf = new StringBuilder();
        buf.append("TupleType<");
        int k = 0;
        for (Type elementType : elementTypes) {
            if (k++ > 0) {
                buf.append(", ");
            }
            buf.append(elementType);
        }
        buf.append(">");
        digest = buf.toString();
    }

    public String toString() {
        return digest;
    }

    public boolean equals(Object obj) {
        if (obj instanceof TupleType) {
            TupleType that = (TupleType) obj;
            return Arrays.equals(this.elementTypes, that.elementTypes);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return digest.hashCode();
    }

    public boolean usesDimension(Dimension dimension, boolean definitely) {
        for (Type elementType : elementTypes) {
            if (elementType.usesDimension(dimension, definitely)) {
                return true;
            }
        }
        return false;
    }

    public Dimension getDimension() {
        throw new UnsupportedOperationException();
    }

    public Hierarchy getHierarchy() {
        throw new UnsupportedOperationException();
    }

    public Level getLevel() {
        throw new UnsupportedOperationException();
    }

    public Type getValueType() {
        for (Type elementType : elementTypes) {
            if (elementType instanceof MemberType) {
                MemberType memberType = (MemberType) elementType;
                Dimension dimension = memberType.getDimension();
                if (dimension != null && dimension.isMeasures()) {
                    return memberType.getValueType();
                }
            }
        }
        return new ScalarType();
    }

    public Type computeCommonType(Type type, int[] conversionCount) {
        if (type instanceof ScalarType) {
            return getValueType().computeCommonType(type, conversionCount);
        }
        if (type instanceof MemberType) {
            return commonTupleType(
                new TupleType(new Type[]{type}),
                conversionCount);
        }
        if (!(type instanceof TupleType)) {
            return null;
        }
        return commonTupleType(type, conversionCount);
    }

    public boolean isInstance(Object value) {
        if (!(value instanceof Object[])) {
            return false;
        }
        Object[] objects = (Object[]) value;
        if (objects.length != elementTypes.length) {
            return false;
        }
        for (int i = 0; i < objects.length; i++) {
            if (!elementTypes[i].isInstance(objects[i])) {
                return false;
            }
        }
        return true;
    }

    private Type commonTupleType(Type type, int[] conversionCount) {
        TupleType that = (TupleType) type;

        if (this.elementTypes.length < that.elementTypes.length) {
            return createCommonTupleType(that, conversionCount);
        }
        return that.createCommonTupleType(this, conversionCount);
    }

    private Type createCommonTupleType(TupleType that, int[] conversionCount) {
        final List<Type> elementTypes = new ArrayList<Type>();
        for (int i = 0; i < this.elementTypes.length; i++) {
            Type commonType = this.elementTypes[i].computeCommonType(
                    that.elementTypes[i], conversionCount);
            elementTypes.add(commonType);
            if (commonType == null) {
                return null;
            }
        }
        if (elementTypes.size() < that.elementTypes.length) {
            for (int i = elementTypes.size();
                i < that.elementTypes.length; i++)
            {
                elementTypes.add(new ScalarType());
            }
        }
        return new TupleType(
            elementTypes.toArray(new Type[elementTypes.size()]));
    }

    /**
     * Checks that there are no duplicate dimensions in a list of member types.
     * If so, the member types will form a valid tuple type.
     * If not, throws {@link mondrian.olap.MondrianException}.
     *
     * @param memberTypes Array of member types
     */
    public static void checkDimensions(MemberType[] memberTypes) {
        for (int i = 0; i < memberTypes.length; i++) {
            MemberType memberType = memberTypes[i];
            for (int j = 0; j < i; j++) {
                MemberType member1 = memberTypes[j];
                final Dimension dimension = memberType.getDimension();
                final Dimension dimension1 = member1.getDimension();
                if (dimension != null && dimension == dimension1) {
                    throw MondrianResource.instance().DupDimensionsInTuple.ex(
                            dimension.getUniqueName());
                }
            }
        }
    }
}

// End TupleType.java
