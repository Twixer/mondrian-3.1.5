/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/fun/HierarchyCurrentMemberFunDef.java#2 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2006-2009 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.olap.fun;

import mondrian.calc.Calc;
import mondrian.calc.ExpCompiler;
import mondrian.calc.HierarchyCalc;
import mondrian.calc.impl.AbstractMemberCalc;
import mondrian.mdx.ResolvedFunCall;
import mondrian.olap.*;

/**
 * Definition of the <code>&lt;Hierarchy&gt;.CurrentMember</code> MDX
 * builtin function.
 *
 * @author jhyde
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/fun/HierarchyCurrentMemberFunDef.java#2 $
 * @since Mar 23, 2006
 */
public class HierarchyCurrentMemberFunDef extends FunDefBase {
    static final HierarchyCurrentMemberFunDef instance =
            new HierarchyCurrentMemberFunDef();

    private HierarchyCurrentMemberFunDef() {
        super(
            "CurrentMember",
            "Returns the current member along a hierarchy during an iteration.",
            "pmh");
    }

    public Calc compileCall(ResolvedFunCall call, ExpCompiler compiler) {
        final HierarchyCalc hierarchyCalc =
                compiler.compileHierarchy(call.getArg(0));
        return new CalcImpl(call, hierarchyCalc);
    }

    public static class CalcImpl extends AbstractMemberCalc {
        private final HierarchyCalc hierarchyCalc;

        public CalcImpl(Exp exp, HierarchyCalc hierarchyCalc) {
            super(exp, new Calc[] {hierarchyCalc});
            this.hierarchyCalc = hierarchyCalc;
        }

        protected String getName() {
            return "CurrentMember";
        }

        public Member evaluateMember(Evaluator evaluator) {
            Hierarchy hierarchy =
                    hierarchyCalc.evaluateHierarchy(evaluator);
            Member member = evaluator.getContext(hierarchy.getDimension());
            // If the dimension has multiple hierarchies, and the current
            // member belongs to a different hierarchy, then this hierarchy
            // reverts to its default member.
            //
            // For example, if the current member of the [Time] dimension
            // is [Time.Weekly].[2003].[Week 4], then the current member
            // of the [Time.Monthly] hierarchy is its default member,
            // [Time.Monthy].[All].
            if (member.getHierarchy() != hierarchy) {
                member = hierarchy.getDefaultMember();
            }
            return member;
        }

        public boolean dependsOn(Dimension dimension) {
            return hierarchyCalc.getType().usesDimension(dimension, false);
        }
    }
}

// End HierarchyCurrentMemberFunDef.java
