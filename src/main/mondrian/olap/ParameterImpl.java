/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/ParameterImpl.java#3 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2000-2002 Kana Software, Inc.
// Copyright (C) 2001-2009 Julian Hyde and others
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
//
// leonardk, 10 January, 2000
*/

package mondrian.olap;
import mondrian.olap.type.*;
import mondrian.mdx.MemberExpr;
import mondrian.calc.*;
import mondrian.calc.impl.GenericCalc;
import mondrian.calc.impl.AbstractMemberListCalc;

import java.util.List;

/**
 * Implementation of {@link Parameter}.
 *
 * @author jhyde
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/ParameterImpl.java#3 $
 * @since Jul 22, 2006
 */
public class ParameterImpl
    implements Parameter, ParameterCompilable
{
    private final String name;
    private String description;
    private Exp defaultExp;
    private Type type;
    private ParameterSlot slot = new ParameterSlot() {
        Object value;
        public Object getCachedDefaultValue() {
            throw new UnsupportedOperationException();
        }

        public Calc getDefaultValueCalc() {
            throw new UnsupportedOperationException();
        }

        public int getIndex() {
            throw new UnsupportedOperationException();
        }

        public Parameter getParameter() {
            return ParameterImpl.this;
        }

        public Object getParameterValue() {
            return value;
        }

        public void setCachedDefaultValue(Object value) {
            throw new UnsupportedOperationException();
        }

        public void setParameterValue(Object value) {
            this.value = value;
        }
    };

    public ParameterImpl(
        String name,
        Exp defaultExp,
        String description,
        Type type)
    {
        this.name = name;
        this.defaultExp = defaultExp;
        this.description = description;
        this.type = type;
        assert defaultExp != null;
        assert type instanceof StringType
            || type instanceof NumericType
            || type instanceof MemberType;
    }

    public Scope getScope() {
        return Scope.Statement;
    }

    public Type getType() {
        return type;
    }

    public Exp getDefaultExp() {
        return defaultExp;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        if (slot == null) {
            // query has not been resolved yet, so it's not possible for the
            // parameter to have a value
            return null;
        } else {
            return slot.getParameterValue();
        }
    }

    public void setValue(Object value) {
        if (value instanceof MemberExpr) {
            slot.setParameterValue(((MemberExpr) value).getMember());
        } else if (value instanceof Literal) {
            slot.setParameterValue(((Literal) value).getValue());
        } else {
            slot.setParameterValue(value);
        }
    }

    public String getDescription() {
        return description;
    }

    // For the purposes of type inference and expression substitution, a
    // parameter is atomic; therefore, we ignore the child member, if any.
    public Object[] getChildren() {
        return null;
    }

    /**
     * Returns whether this parameter is equal to another, based upon name,
     * type and value
     */
    public boolean equals(Object other) {
        if (!(other instanceof ParameterImpl)) {
            return false;
        }
        ParameterImpl that = (ParameterImpl) other;
        return that.getName().equals(this.getName())
            && that.defaultExp.equals(this.defaultExp);
    }

    public int hashCode() {
        return Util.hash(getName().hashCode(), defaultExp.hashCode());
    }

    /**
     * Returns whether the parameter can be modified.
     */
    public boolean isModifiable() {
        return true;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(Type type) {
        assert type instanceof StringType
            || type instanceof NumericType
            || type instanceof MemberType
            || (type instanceof SetType
                && ((SetType) type).getElementType() instanceof MemberType)
            : type;
        this.type = type;
    }

    public void setDefaultExp(Exp defaultExp) {
        assert defaultExp != null;
        this.defaultExp = defaultExp;
    }

    public Calc compile(ExpCompiler compiler) {
        final ParameterSlot slot = compiler.registerParameter(this);
        if (this.slot != null) {
            // save previous value
            slot.setParameterValue(this.slot.getParameterValue());
        }
        this.slot = slot;
        if (type instanceof SetType) {
            return new MemberListParameterCalc(slot);
        } else {
            return new ParameterCalc(slot);
        }
    }

    /**
     * Compiled expression which yields the value of a scalar, member, level,
     * hierarchy or dimension parameter.
     *
     * <p>It uses a slot which has a unique id within the execution environment.
     *
     * @see MemberListParameterCalc
     */
    private static class ParameterCalc
        extends GenericCalc
    {
        private final ParameterSlot slot;

        /**
         * Creates a ParameterCalc.
         *
         * @param slot Slot
         */
        public ParameterCalc(ParameterSlot slot) {
            super(new DummyExp(slot.getParameter().getType()), new Calc[0]);
            this.slot = slot;
        }

        public Object evaluate(Evaluator evaluator) {
            Object value = evaluator.getParameterValue(slot);
            if (slot.getParameterValue() == null) {
                // save value if not set (setting the default value)
                slot.setParameterValue(value);
            }
            return value;
        }
    }

    /**
     * Compiled expression which yields the value of parameter whose type is
     * a list of members.
     *
     * <p>It uses a slot which has a unique id within the execution environment.
     *
     * @see ParameterCalc
     */
    private static class MemberListParameterCalc
        extends AbstractMemberListCalc
    {
        private final ParameterSlot slot;

        /**
         * Creates a MemberListParameterCalc.
         *
         * @param slot Slot
         */
        public MemberListParameterCalc(ParameterSlot slot) {
            super(new DummyExp(slot.getParameter().getType()), new Calc[0]);
            this.slot = slot;
        }

        public List<Member> evaluateMemberList(Evaluator evaluator) {
            List<Member> value =
                (List<Member>) evaluator.getParameterValue(slot);
            if (slot.getParameterValue() == null) {
                // save value if not set (setting the default value)
                slot.setParameterValue(value);
            }
            return value;
        }
    }
}

// End ParameterImpl.java
