/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/fun/NamedSetCurrentOrdinalFunDef.java#2 $
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
import mondrian.calc.impl.AbstractIntegerCalc;
import mondrian.mdx.ResolvedFunCall;
import mondrian.mdx.NamedSetExpr;
import mondrian.olap.*;
import mondrian.resource.MondrianResource;

/**
 * Definition of the <code>&lt;Named Set&gt;.CurrentOrdinal</code> MDX builtin
 * function.
 *
 * @author jhyde
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/fun/NamedSetCurrentOrdinalFunDef.java#2 $
 * @since Oct 19, 2008
 */
public class NamedSetCurrentOrdinalFunDef extends FunDefBase {
    static final NamedSetCurrentOrdinalFunDef instance =
        new NamedSetCurrentOrdinalFunDef();

    private NamedSetCurrentOrdinalFunDef() {
        super(
            "CurrentOrdinal",
            "Returns the ordinal of the current iteration through a named set.",
            "pix");
    }

    public Exp createCall(Validator validator, Exp[] args) {
        assert args.length == 1;
        final Exp arg0 = args[0];
        if (!(arg0 instanceof NamedSetExpr)) {
            throw MondrianResource.instance().NotANamedSet.ex();
        }
        return super.createCall(validator, args);
    }

    public Calc compileCall(ResolvedFunCall call, ExpCompiler compiler) {
        final Exp arg0 = call.getArg(0);
        assert arg0 instanceof NamedSetExpr : "checked this in createCall";
        final NamedSetExpr namedSetExpr = (NamedSetExpr) arg0;
        return new AbstractIntegerCalc(call, new Calc[0]) {
            public int evaluateInteger(Evaluator evaluator) {
                return namedSetExpr.getEval(evaluator).currentOrdinal();
            }
        };
    }
}

// End NamedSetCurrentOrdinalFunDef.java
