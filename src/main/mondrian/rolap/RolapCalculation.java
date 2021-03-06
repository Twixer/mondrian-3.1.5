/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/rolap/RolapCalculation.java#1 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2009-2009 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.rolap;

import mondrian.calc.Calc;

/**
 * Entry in the evaluation context that indicates a calculation that needs to
 * be performed before we get to the atomic stored cells.
 *
 * <p>Most of the time it's just members that are calculated, but calculated
 * tuples arise when the slicer is a set of tuples.
 *
 * <p>The evaluator uses this interface to efficiently expand a member or tuple
 * from the evaluation context. When evaluated, a calculation will change one or
 * more elements of the context (calculated members just change one, but tuple
 * sets in the slicer can change more than one).
 *
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/rolap/RolapCalculation.java#1 $
 * @author jhyde
 * @since May 15, 2009
 */
interface RolapCalculation {
    /**
     * Pushes this calculated member or tuple onto the stack of evaluation
     * contexts, sets the context to the default member of the hierarchy,
     * and returns the evaluator containing the new context.
     *
     * @param evaluator Current evaluator
     * @return New evaluator
     */
    RolapEvaluator pushSelf(RolapEvaluator evaluator);

    /**
     * Returns the solve order of this calculation. Identifies which order
     * calculations are expanded.
     *
     * @return Solve order
     */
    int getSolveOrder();

    /**
     * Returns the ordinal of this calculation; to resolve ties.
     *
     * @param cube Current cube
     * @return Ordinal or calculation
     */
    int getDimensionOrdinal(RolapCube cube);

    /**
     * Returns whether this calculation is a member is computed from a
     * {@code WITH MEMBER} clause in an MDX query.
     */
    boolean isCalculatedInQuery();

    /**
     * Returns the compiled expression to evaluate the scalar value of the
     * current cell. This method will be called frequently, so the
     * implementation should probably compile once and cache the result.
     *
     * @param root Root evaluation context
     * @return Compiled scalar expression
     */
    Calc getCompiledExpression(RolapEvaluatorRoot root);

    /**
     * Returns whether this calculation contains an aggregate function.
     *
     * @return Whether this calculation contains an aggregate function.
     */
    boolean containsAggregateFunction();
}

// End RolapCalculation.java
