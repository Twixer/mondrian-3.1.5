/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/fun/MemberHierarchyFunDef.java#2 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2006-2007 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.olap.fun;

import mondrian.calc.Calc;
import mondrian.calc.ExpCompiler;
import mondrian.calc.MemberCalc;
import mondrian.calc.impl.AbstractHierarchyCalc;
import mondrian.mdx.ResolvedFunCall;
import mondrian.olap.Exp;
import mondrian.olap.Hierarchy;
import mondrian.olap.Evaluator;
import mondrian.olap.Member;

/**
 * Definition of the <code>&lt;Member&gt;.Hierarchy</code> MDX builtin function.
 *
 * @author jhyde
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/fun/MemberHierarchyFunDef.java#2 $
 * @since Mar 23, 2006
 */
public class MemberHierarchyFunDef extends FunDefBase {
    static final MemberHierarchyFunDef instance = new MemberHierarchyFunDef();

    private MemberHierarchyFunDef() {
        super("Hierarchy", "Returns a member's hierarchy.", "phm");
    }

    public Calc compileCall(ResolvedFunCall call, ExpCompiler compiler) {
        final MemberCalc memberCalc =
                compiler.compileMember(call.getArg(0));
        return new CalcImpl(call, memberCalc);
    }

    public static class CalcImpl extends AbstractHierarchyCalc {
        private final MemberCalc memberCalc;

        public CalcImpl(Exp exp, MemberCalc memberCalc) {
            super(exp, new Calc[] {memberCalc});
            this.memberCalc = memberCalc;
        }

        public Hierarchy evaluateHierarchy(Evaluator evaluator) {
            Member member = memberCalc.evaluateMember(evaluator);
            return member.getHierarchy();
        }
    }
}

// End MemberHierarchyFunDef.java
