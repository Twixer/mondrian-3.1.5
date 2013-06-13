/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/mdx/NamedSetExpr.java#3 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2006-2009 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.mdx;

import mondrian.olap.*;
import mondrian.olap.type.Type;
import mondrian.olap.type.SetType;
import mondrian.calc.*;
import mondrian.calc.impl.AbstractTupleIterCalc;
import mondrian.calc.impl.AbstractMemberIterCalc;

import java.util.List;

/**
 * Usage of a {@link mondrian.olap.NamedSet} in an MDX expression.
 *
 * @author jhyde
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/mdx/NamedSetExpr.java#3 $
 * @since Sep 26, 2005
 */
public class NamedSetExpr extends ExpBase implements Exp {
    private final NamedSet namedSet;

    /**
     * Creates a usage of a named set.
     *
     * @param namedSet namedSet
     * @pre NamedSet != null
     */
    public NamedSetExpr(NamedSet namedSet) {
        Util.assertPrecondition(namedSet != null, "namedSet != null");
        this.namedSet = namedSet;
    }

    /**
     * Returns the named set.
     *
     * @post return != null
     */
    public NamedSet getNamedSet() {
        return namedSet;
    }

    public String toString() {
        return namedSet.getUniqueName();
    }

    public NamedSetExpr clone() {
        return new NamedSetExpr(namedSet);
    }

    public int getCategory() {
        return Category.Set;
    }

    public Exp accept(Validator validator) {
        // A set is sometimes used in more than one cube. So, clone the
        // expression and re-validate every time it is used.
        //
        // But keep the expression wrapped in a NamedSet, so that the
        // expression is evaluated once per query. (We don't want the
        // expression to be evaluated context-sensitive.)
        NamedSet namedSet2 = namedSet.validate(validator);
        if (namedSet2 == namedSet) {
            return this;
        }
        return new NamedSetExpr(namedSet2);
    }

    public Calc accept(ExpCompiler compiler) {
        // This is a deliberate breach of the usual rules for interpreting
        // acceptable result styles. Usually the caller gets to call the shots:
        // the callee iterates over the acceptable styles and implements in the
        // first style it is able to. But in this case, we return iterable if
        // the caller can handle it, even if it isn't the caller's first choice.
        // This is because the .current and .currentOrdinal functions only
        // work correctly on iterators.
        final List<ResultStyle> styleList =
            compiler.getAcceptableResultStyles();
        if (!styleList.contains(ResultStyle.ITERABLE)
            && !styleList.contains(ResultStyle.ANY))
        {
            return null;
        }

        if (((SetType) getType()).getArity() != 1) {
            return new AbstractTupleIterCalc(
                this,
                new Calc[]{/* todo: compile namedSet.getExp() */})
            {
                public Iterable<Member[]> evaluateTupleIterable(
                    Evaluator evaluator)
                {
                    final Evaluator.NamedSetEvaluator eval = getEval(evaluator);
                    return eval.evaluateTupleIterable();
                }

                public boolean dependsOn(Dimension dimension) {
                    // Given that a named set is never re-evaluated within the
                    // scope of a query, effectively it's independent of all
                    // dimensions.
                    return false;
                }
            };
        } else {
            return new AbstractMemberIterCalc(
                 this,
                new Calc[]{/* todo: compile namedSet.getExp() */})
            {
                public Iterable<Member> evaluateMemberIterable(
                    Evaluator evaluator)
                {
                     final Evaluator.NamedSetEvaluator eval =
                         getEval(evaluator);
                    return eval.evaluateMemberIterable();
                }

                public boolean dependsOn(Dimension dimension) {
                    // Given that a named set is never re-evaluated within the
                    // scope of a query, effectively it's independent of all
                    // dimensions.
                    return false;
                }
            };
        }
    }

    public Evaluator.NamedSetEvaluator getEval(Evaluator evaluator) {
        return evaluator.getNamedSetEvaluator(namedSet, true);
    }

    public Object accept(MdxVisitor visitor) {
        Object o = visitor.visit(this);
        namedSet.getExp().accept(visitor);
        return o;
    }

    public Type getType() {
        return namedSet.getType();
    }

}

// End NamedSetExpr.java
