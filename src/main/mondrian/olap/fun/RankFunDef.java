/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/fun/RankFunDef.java#3 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2005-2009 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/

package mondrian.olap.fun;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.calc.*;
import mondrian.calc.impl.*;
import mondrian.rolap.RolapUtil;
import mondrian.mdx.ResolvedFunCall;

import java.util.*;
import java.io.PrintWriter;

/**
 * Definition of the <code>RANK</code> MDX function.
 *
 * @author Richard Emberson
 * @since 17 January, 2005
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/fun/RankFunDef.java#3 $
 */
public class RankFunDef extends FunDefBase {
    static final boolean debug = false;
    static final ReflectiveMultiResolver Resolver = new ReflectiveMultiResolver(
            "Rank",
            "Rank(<Tuple>, <Set> [, <Calc Expression>])",
            "Returns the one-based rank of a tuple in a set.",
            new String[]{"fitx", "fitxn", "fimx", "fimxn"},
            RankFunDef.class);

    public RankFunDef(FunDef dummyFunDef) {
        super(dummyFunDef);
    }

    public Calc compileCall(ResolvedFunCall call, ExpCompiler compiler) {
        switch (call.getArgCount()) {
        case 2:
            return compileCall2(call, compiler);
        case 3:
            return compileCall3(call, compiler);
        default:
            throw Util.newInternal("invalid arg count " + call.getArgCount());
        }
    }

    public Calc compileCall3(ResolvedFunCall call, ExpCompiler compiler) {
        final Type type0 = call.getArg(0).getType();
        final ListCalc listCalc =
        	compiler.compileList(call.getArg(1));
        final SetType setType = (SetType)call.getArg(1).getType();
        final boolean isTupleList = setType.getElementType() instanceof TupleType;
        final Calc keyCalc =
        	compiler.compileScalar(call.getArg(2), true);
        final Calc sortedListCalc =
        	new SortedListCalc(call, listCalc, keyCalc, isTupleList);
        final ExpCacheDescriptor cacheDescriptor =
        	new ExpCacheDescriptor(
        			call, sortedListCalc, compiler.getEvaluator());
        if (type0 instanceof TupleType) {
            final TupleCalc tupleCalc =
            	compiler.compileTuple(call.getArg(0));
            return new Rank3TupleCalc(call, tupleCalc, keyCalc, cacheDescriptor);
        } else {
            final MemberCalc memberCalc =
            	compiler.compileMember(call.getArg(0));
            return new Rank3MemberCalc(call, memberCalc, keyCalc, cacheDescriptor);
        }
    }

    public Calc compileCall2(ResolvedFunCall call, ExpCompiler compiler) {
        final boolean tuple = call.getArg(0).getType() instanceof TupleType;
        final Exp listExp = call.getArg(1);
        ListCalc listCalc0 = compiler.compileList(listExp);
        Calc listCalc1 = new RankedListCalc(listCalc0, tuple);
        final Calc listCalc;
        if (MondrianProperties.instance().EnableExpCache.get()) {
            final ExpCacheDescriptor key = new ExpCacheDescriptor(
                    listExp, listCalc1, compiler.getEvaluator());
            listCalc = new CacheCalc(listExp, key);
        } else {
            listCalc = listCalc1;
        }
        if (tuple) {
            final TupleCalc tupleCalc =
                    compiler.compileTuple(call.getArg(0));
            return new Rank2TupleCalc(call, tupleCalc, listCalc);
        } else {
            final MemberCalc memberCalc =
                    compiler.compileMember(call.getArg(0));
            return new Rank2MemberCalc(call, memberCalc, listCalc);
        }
    }

    private static class Rank2TupleCalc extends AbstractIntegerCalc {
        private final TupleCalc tupleCalc;
        private final Calc listCalc;

        public Rank2TupleCalc(
            ResolvedFunCall call, TupleCalc tupleCalc, Calc listCalc)
        {
            super(call, new Calc[] {tupleCalc, listCalc});
            this.tupleCalc = tupleCalc;
            this.listCalc = listCalc;
        }

        public int evaluateInteger(Evaluator evaluator) {
            // Get member or tuple.
            // If the member is null (or the tuple contains a null member)
            // the result is null (even if the list is null).
            final Member[] members = tupleCalc.evaluateTuple(evaluator);
            if (members == null) {
                return IntegerNull;
            }
            assert !tupleContainsNullMember(members);

            // Get the set of members/tuples.
            // If the list is empty, MSAS cannot figure out the type of the
            // list, so returns an error "Formula error - dimension count is
            // not valid - in the Rank function". We will naturally return 0,
            // which I think is better.
            RankedTupleList rankedTupleList =
                (RankedTupleList)listCalc.evaluate(evaluator);
            if (rankedTupleList == null) {
                return 0;
            }

            // Find position of member in list. -1 signifies not found.
            final int i = rankedTupleList.indexOf(members);
            // Return 1-based rank. 0 signifies not found.
            return i + 1;
        }
    }

    private static class Rank2MemberCalc extends AbstractIntegerCalc {
        private final MemberCalc memberCalc;
        private final Calc listCalc;

        public Rank2MemberCalc(
            ResolvedFunCall call, MemberCalc memberCalc, Calc listCalc)
        {
            super(call, new Calc[] {memberCalc, listCalc});
            this.memberCalc = memberCalc;
            this.listCalc = listCalc;
        }

        public int evaluateInteger(Evaluator evaluator) {
            // Get member or tuple.
            // If the member is null (or the tuple contains a null member)
            // the result is null (even if the list is null).
            final Member member = memberCalc.evaluateMember(evaluator);
            if (member == null
                || member.isNull())
            {
                return IntegerNull;
            }
            // Get the set of members/tuples.
            // If the list is empty, MSAS cannot figure out the type of the
            // list, so returns an error "Formula error - dimension count is
            // not valid - in the Rank function". We will naturally return 0,
            // which I think is better.
            RankedMemberList rankedMemberList =
                (RankedMemberList) listCalc.evaluate(evaluator);
            if (rankedMemberList == null) {
                return 0;
            }

            // Find position of member in list. -1 signifies not found.
            final int i = rankedMemberList.indexOf(member);
            // Return 1-based rank. 0 signifies not found.
            return i + 1;
        }
    }

    private static class Rank3TupleCalc extends AbstractIntegerCalc {
        private final TupleCalc tupleCalc;
        private final Calc sortCalc;
        private final ExpCacheDescriptor cacheDescriptor;

        public Rank3TupleCalc(
            ResolvedFunCall call,
            TupleCalc tupleCalc,
            Calc sortCalc,
            ExpCacheDescriptor cacheDescriptor)
        {
            super(call, new Calc[] {tupleCalc, sortCalc});
            this.tupleCalc = tupleCalc;
            this.sortCalc = sortCalc;
            this.cacheDescriptor = cacheDescriptor;
        }

        public int evaluateInteger(Evaluator evaluator) {
            Member[] members = tupleCalc.evaluateTuple(evaluator);
            if (members == null) {
                return IntegerNull;
            }
            assert !tupleContainsNullMember(members);

            // Evaluate the list (or retrieve from cache).
            // If there is an exception while calculating the
            // list, propagate it up.
            final SortResult sortResult = (SortResult)
                    evaluator.getCachedResult(cacheDescriptor);
            if (debug) {
                sortResult.print(new PrintWriter(System.out));
            }
            
            if (sortResult.isEmpty()) {
                // If list is empty, the rank is null.
                return IntegerNull;
            }
            
            // First try to find the member in the cached SortResult
            Integer rank = sortResult.rankOf(members);
            if (rank != null) {
            	return rank;
            }
            
            // member is not seen before, now compute the value of the tuple.
            final Evaluator evaluator2 = evaluator.push(members);
            Object value = sortCalc.evaluate(evaluator2);
            
            if (value == RolapUtil.valueNotReadyException) {
                // The value wasn't ready, so quit now... we'll be back.
                return 0;
            }
            
            // If value is null, it won't be in the values array.
            if (value == Util.nullValue) {
                return sortResult.values.length + 1;
            }

            // Look for the ranked value in the array.
            int j = FunUtil.searchValuesDesc(sortResult.values, value);
            if (j < 0) {
                // Value not found. Flip the result to find the
                // insertion point.
                j = -(j + 1);
            }
            return j + 1; // 1-based            
        }
    }

    private static class Rank3MemberCalc extends AbstractIntegerCalc {
        private final MemberCalc memberCalc;
        private final Calc sortCalc;
        private final ExpCacheDescriptor cacheDescriptor;

        public Rank3MemberCalc(
            ResolvedFunCall call,
            MemberCalc memberCalc,
            Calc sortCalc,
            ExpCacheDescriptor cacheDescriptor)
        {
            super(call, new Calc[] {memberCalc, sortCalc});
            this.memberCalc = memberCalc;
            this.sortCalc = sortCalc;
            this.cacheDescriptor = cacheDescriptor;
        }

        public int evaluateInteger(Evaluator evaluator) {
            Member member = memberCalc.evaluateMember(evaluator);
            if (member == null || member.isNull()) {
                return IntegerNull;
            }

            // Evaluate the list (or retrieve from cache).
            // If there was an exception while calculating the
            // list, propagate it up.
            final SortResult sortResult = (SortResult)
                    evaluator.getCachedResult(cacheDescriptor);
            if (debug) {
                sortResult.print(new PrintWriter(System.out));
            }
            if (sortResult.isEmpty()) {
                // If list is empty, the rank is null.
                return IntegerNull;
            }
            
            // First try to find the member in the cached SortResult
            Integer rank = sortResult.rankOf(member);
            if (rank != null) {
            	return rank;
            }
            
            // member is not seen before, now compute the value of the tuple.
            final Evaluator evaluator2 = evaluator.push(member);
            Object value = sortCalc.evaluate(evaluator2);
            
            if (value == RolapUtil.valueNotReadyException) {
                // The value wasn't ready, so quit now... we'll be back.
                return 0;
            }
            
            // If value is null, it won't be in the values array.
            if (value == Util.nullValue) {
                return sortResult.values.length + 1;
            }

            // Look for the ranked value in the array.
            int j = FunUtil.searchValuesDesc(sortResult.values, value);
            if (j < 0) {
                // Value not found. Flip the result to find the
                // insertion point.
                j = -(j + 1);
            }
            return j + 1; // 1-based
        }
    }

    /**
     * Calc which evaluates an expression to form a list of tuples
     * Evaluates a scalar expression at each tuple, then sorts the list of
     * values. The result is a value of type {@link SortResult}, and can be
     * used to implement the <code>Rank</code> function efficiently.
     */
    private static class SortedListCalc extends AbstractCalc {
        private final ListCalc listCalc;
        private final Calc keyCalc;

        /**
         * Creates a SortCalc.
         *
         * @param exp Source expression
         * @param listCalc Compiled expression to compute the list
         * @param keyCalc Compiled expression to compute the sort key
         * @param tupleList Whether is tuple list
         */
        public SortedListCalc(
            Exp exp,
            ListCalc listCalc,
            Calc keyCalc,
            boolean tupleList) 
        {
            super(exp, new Calc[] {listCalc, keyCalc});
            this.listCalc = listCalc;
            this.keyCalc = keyCalc;
        }

        public boolean dependsOn(Dimension dimension) {
            return anyDependsButFirst(getCalcs(), dimension);
        }

        
        public Object evaluate(Evaluator evaluator) {        	
            // Create a new evaluator so we don't corrupt the given one.
            final Evaluator evaluator2 = evaluator.push(false);

            // Construct an array containing the value of the expression
            // for each member.
            
            List members = (List) listCalc.evaluate(evaluator2);
            
            assert members != null;
            if (members.isEmpty()) {
                return new SortResult(null, null);
            }
            RuntimeException exception = null;
            TreeMap<Object, Integer> uniqueValueCounterMap = 
            	new TreeMap<Object, Integer>(FunUtil.DescendingValueComparator.instance);
            
            boolean isMember = false;
            Map<Member[], Object> valueMap = new HashMap<Member[], Object>();            
            
            Integer valueCounter;
            
            if (members.get(0) instanceof Member) {
            	isMember = true;
            }
            
            if (isMember) {
                for (Object o : members) {
                    Member[] tmpMember = new Member[1];
                    tmpMember[0] = (Member) o;
                    evaluator2.setContext(tmpMember);
                    final Object keyValue = keyCalc.evaluate(evaluator2);
                    if (keyValue instanceof RuntimeException) {
                        if (exception == null) {
                            exception = (RuntimeException) keyValue;
                        }
                    } else if (Util.isNull(keyValue)) {
                        // nothing to do
                    } else {
                    	if (uniqueValueCounterMap.containsKey(keyValue)) {
                    		// Update the counter on how many times this keyValue has been seen
                    		valueCounter = uniqueValueCounterMap.get(keyValue) + 1;
                    	} else {
                    		// First time seeing this keyValue
                    		valueCounter = 1;
                    	}
                    	uniqueValueCounterMap.put(keyValue, valueCounter);
                    	valueMap.put(tmpMember, keyValue);
                    }
                }
            } else {
                for (Object o : members) {
                    evaluator2.setContext((Member[]) o);
                    final Object keyValue = keyCalc.evaluate(evaluator2);
                    if (keyValue instanceof RuntimeException) {
                        if (exception == null) {
                            exception = (RuntimeException) keyValue;
                        }
                    } else if (Util.isNull(keyValue)) {
                        // nothing to do
                    } else {
                    	if (uniqueValueCounterMap.containsKey(keyValue)) {
                    		// Update the counter on how many times this keyValue has been seen
                    		valueCounter = uniqueValueCounterMap.get(keyValue) + 1;
                    	} else {
                    		// First time seeing this keyValue
                    		valueCounter = 1;
                    	}
                    	uniqueValueCounterMap.put(keyValue, valueCounter);
                    	valueMap.put((Member[]) o, keyValue);
                    }
                }
            }
                        
            // If there were exceptions, quit now... we'll be back.
            if (exception != null) {
                return exception;
            }
            
            int numValues = valueMap.keySet().size();
            
            Object[] allValuesSorted = new Object[numValues];            	

            // Now build the sorted array containing all keyValues
            // And update the counter to the rank
            Integer currentOrdinal = 0;
            Integer valueCount = 0;
            TreeMap<Object, Integer> uniqueValueRankMap = 
            	new TreeMap<Object, Integer>(FunUtil.DescendingValueComparator.instance);
            
            for (Object keyValue : uniqueValueCounterMap.keySet()) {
            	valueCount = uniqueValueCounterMap.get(keyValue);
            	// Because uniqueValueCounterMap is already sorted
            	// so the reconstructed allValuesSorted is guaranteed to be sorted. 
            	for (int i = 0; i < valueCount; i ++) {
            		allValuesSorted[currentOrdinal + i] = keyValue;
            	}
            	uniqueValueRankMap.put(keyValue, currentOrdinal+1);
        		currentOrdinal += valueCount;
            }
            
            // Build a member/tuple to rank map
            SortResult sortResult;
            int oneBasedRank;
        	Map<List<Member>, Integer> rankMap = new HashMap<List<Member>, Integer>();
            
        	for (Member[] memberKey: valueMap.keySet()) {
        		oneBasedRank = uniqueValueRankMap.get(valueMap.get(memberKey));                	
        		rankMap.put(Arrays.asList(memberKey), oneBasedRank);
        	}            	
        	sortResult = new SortResult(allValuesSorted, rankMap);

        	return sortResult;
        }        
    }

    /**
     * Holder for the result of sorting a set of values.
     * It provides simple interface to look up the rank for a member or a tuple.
     */
    private static class SortResult {
        /**
         * Whether the list of tuples was empty.
         * If this is the case, the rank will always be null.
         *
        /**
         * All values in sorted order; Duplicates are not removed.
         * E.g. Set (15,15,5,0)
         *  10 should be ranked 3. 
         *  
         * Null values are not present: they would be at the end, anyway.
         */
        final Object[] values;

        /**
         * The precomputed rank associated with all members(or tuples)
         */
        final Map<List<Member>, Integer> rankMap;
        
        /**
         * Type of elements sorted, whether they are members or tuples.
         */
        final boolean isMemberResultSet;
        
        /**
         * Temporary structure to create lookup key for members.
         */
        List<Member> tmpList = new ArrayList<Member>(1);
               
        public SortResult(Object[] values, Map<List<Member>, Integer> rankMap) {
            this.values = values;
            this.rankMap = rankMap;            
            if (rankMap != null && !rankMap.isEmpty()) {
            	List<Member> anyKey = (List<Member>)rankMap.keySet().toArray()[0];
            	if (anyKey.size() == 1) {
            		isMemberResultSet = true;
            	} else {
            		isMemberResultSet = false;
            	}
            } else {
            	isMemberResultSet = false;
            }
        }

        public boolean isEmpty() {
        	if (values == null) {
        		return true;
        	} else {
        		return false;
        	}
        }
        
        public Integer rankOf(Member member) {
        	if (rankMap == null || !isMemberResultSet) {
        		return null;
        	} else {
        		tmpList.clear();
        		tmpList.add(member);
        		return rankMap.get(tmpList);
        	}        	
        }
        
        public Integer rankOf(Member[] tuple) {        	
        	if (rankMap == null || isMemberResultSet) {
        		return null;
        	} else {
        		return rankMap.get(Arrays.asList(tuple));
        	}
        }
        
        public void print(PrintWriter pw) {
            if (values == null) {
                pw.println("SortResult: empty");
            } else {
                pw.println("SortResult {");
                for (int i = 0; i < values.length; i++) {
                    if (i > 0) {
                        pw.println(",");
                    }
                    Object value = values[i];
                    pw.print(value);
                }
                pw.println("}");
            }
            pw.flush();
        }
    }

    /**
     * Expression which evaluates an expression to form a list of tuples.
     * The result is a value of type
     * {@link mondrian.olap.fun.RankFunDef.RankedMemberList} or
     * {@link mondrian.olap.fun.RankFunDef.RankedTupleList}, or
     * null if the list is empty.
     */
    private static class RankedListCalc extends AbstractCalc {
        private final ListCalc listCalc;
        private final boolean tuple;

        /**
         * Creates a RankedListCalc.
         *
         * @param listCalc Compiled expression to compute the list
         * @param tuple Whether elements of the list are tuples (as opposed to
         * members)
         */
        public RankedListCalc(ListCalc listCalc, boolean tuple) {
            super(new DummyExp(listCalc.getType()), new Calc[] {listCalc});
            this.listCalc = listCalc;
            this.tuple = tuple;
        }

        public Object evaluate(Evaluator evaluator) {
            // Construct an array containing the value of the expression
            // for each member.
            if (tuple) {
                List<Member[]> tupleList =
                    ((TupleListCalc) listCalc).evaluateTupleList(evaluator);
                assert tupleList != null;
                return new RankedTupleList(tupleList);
            } else {
                List<Member> memberList =
                    ((MemberListCalc) listCalc).evaluateMemberList(evaluator);
                assert memberList != null;
                return new RankedMemberList(memberList);
            }
        }
    }

    /**
     * Data structure which contains a list and can return the position of an
     * element in the list in O(log N).
     */
    static class RankedMemberList {
        Map<Member, Integer> map = new HashMap<Member, Integer>();

        RankedMemberList(List<Member> members) {
            int i = -1;
            for (final Member member : members) {
                ++i;
                final Integer value = map.put(member, i);
                if (value != null) {
                    // The list already contained a value for this key -- put
                    // it back.
                    map.put(member, value);
                }
            }
        }

        int indexOf(Member m) {
            Integer integer = map.get(m);
            if (integer == null) {
                return -1;
            } else {
                return integer;
            }
        }
    }
    /**
     * Data structure which contains a list and can return the position of an
     * element in the list in O(log N).
     */
    static class RankedTupleList {
        final Map<List<Member>, Integer> map =
            new HashMap<List<Member>, Integer>();

        RankedTupleList(List<Member[]> tupleList) {
            int i = -1;
            for (final Member[] tupleMembers : tupleList) {
                ++i;
                final List<Member> key = Arrays.asList(tupleMembers);
                final Integer value = map.put(key, i);
                if (value != null) {
                    // The list already contained a value for this key -- put
                    // it back.
                    map.put(key, value);
                }
            }
        }

        int indexOf(Member[] tupleMembers) {
            final List<Member> key = Arrays.asList(tupleMembers);
            Integer integer = map.get(key);
            if (integer == null) {
                return -1;
            } else {
                return integer;
            }
        }
    }    
}

// End RankFunDef.java