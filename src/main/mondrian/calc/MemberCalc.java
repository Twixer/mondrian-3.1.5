/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/calc/MemberCalc.java#2 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2006-2007 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.calc;

import mondrian.olap.Evaluator;
import mondrian.olap.Member;

/**
 * Expression which yields a {@link Member}.
 *
 * <p>When implementing this interface, it is convenient to extend
 * {@link mondrian.calc.impl.AbstractMemberCalc}, but it is not required.

 * @author jhyde
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/calc/MemberCalc.java#2 $
 * @since Sep 26, 2005
 */
public interface MemberCalc extends Calc {
    /**
     * Evaluates this expression to yield a member.
     *
     * <p>May return the null member (see
     * {@link mondrian.olap.Hierarchy#getNullMember()}) but never null.
     *
     * @param evaluator Evaluation context
     * @return a member
     */
    Member evaluateMember(Evaluator evaluator);
}

// End MemberCalc.java
