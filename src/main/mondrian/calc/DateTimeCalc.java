/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/calc/DateTimeCalc.java#2 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2006-2009 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.calc;

import mondrian.olap.Evaluator;

import java.util.Date;

/**
 * Compiled expression whose result is a {@link Date}, representing an MDX
 * DateTime value.
 *
 * <p>When implementing this interface, it is convenient to extend
 * {@link mondrian.calc.impl.AbstractDateTimeCalc}, but it is not required.
 *
 * @author jhyde
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/calc/DateTimeCalc.java#2 $
 * @since Sep 26, 2005
 */
public interface DateTimeCalc extends Calc {
    /**
     * Evaluates this expression to yield a {@link Date} value.
     *
     * @param evaluator Evaluation context
     * @return evaluation result
     */
    Date evaluateDateTime(Evaluator evaluator);
}

// End DateTimeCalc.java
