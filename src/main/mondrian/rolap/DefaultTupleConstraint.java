/*
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2004-2005 TONBELLER AG
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.rolap;

import mondrian.olap.Evaluator;
import mondrian.rolap.sql.TupleConstraint;
import mondrian.rolap.sql.MemberChildrenConstraint;
import mondrian.rolap.sql.SqlQuery;
import mondrian.rolap.aggmatcher.AggStar;

/**
 * TupleConstraint which does not restrict the result.
 *
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/rolap/DefaultTupleConstraint.java#2 $
 */
public class DefaultTupleConstraint implements TupleConstraint {

    private static final TupleConstraint instance =
        new DefaultTupleConstraint();

    /** we have no state, so all instances are equal */
    private static final Object cacheKey = new Object();

    protected DefaultTupleConstraint() {
    }

    public void addConstraint(
        SqlQuery sqlQuery,
        RolapCube baseCube,
        AggStar aggStar)
    {
    }

    public void addLevelConstraint(
        SqlQuery query,
        RolapCube baseCube,
        AggStar aggStar,
        RolapLevel level)
    {
    }

    public MemberChildrenConstraint getMemberChildrenConstraint(
        RolapMember parent)
    {
        return DefaultMemberChildrenConstraint.instance();
    }

    public String toString() {
        return "DefaultTupleConstraint";
    }

    public Object getCacheKey() {
        return cacheKey;
    }

    public static TupleConstraint instance() {
        return instance;
    }

    public Evaluator getEvaluator() {
        return null;
    }

}

// End DefaultTupleConstraint.java
