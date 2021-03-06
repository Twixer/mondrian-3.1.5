/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/fun/UdfResolver.java#4 $
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
import mondrian.spi.UserDefinedFunction;
import mondrian.calc.*;
import mondrian.calc.impl.GenericCalc;
import mondrian.calc.impl.AbstractListCalc;
import mondrian.mdx.ResolvedFunCall;

import java.util.List;

/**
 * Resolver for user-defined functions.
 *
 * @author jhyde
 * @since 2.0
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/fun/UdfResolver.java#4 $
 */
public class UdfResolver implements Resolver {
    private final UserDefinedFunction udf;
    private static final String[] emptyStringArray = new String[0];

    public UdfResolver(UserDefinedFunction udf) {
        this.udf = udf;
    }
    public String getName() {
        return udf.getName();
    }

    public String getDescription() {
        return udf.getDescription();
    }

    public String getSignature() {
        Type[] parameterTypes = udf.getParameterTypes();
        int[] parameterCategories = new int[parameterTypes.length];
        for (int i = 0; i < parameterCategories.length; i++) {
            parameterCategories[i] = TypeUtil.typeToCategory(parameterTypes[i]);
        }
        Type returnType = udf.getReturnType(parameterTypes);
        int returnCategory = TypeUtil.typeToCategory(returnType);
        return getSyntax().getSignature(
            getName(),
            returnCategory,
            parameterCategories);
    }

    public Syntax getSyntax() {
        return udf.getSyntax();
    }

    public FunDef getFunDef() {
        Type[] parameterTypes = udf.getParameterTypes();
        int[] parameterCategories = new int[parameterTypes.length];
        for (int i = 0; i < parameterCategories.length; i++) {
            parameterCategories[i] = TypeUtil.typeToCategory(parameterTypes[i]);
        }
        Type returnType = udf.getReturnType(parameterTypes);
        return new UdfFunDef(parameterCategories, returnType);
    }

    public FunDef resolve(
        Exp[] args,
        Validator validator,
        List<Conversion> conversions)
    {
        final Type[] parameterTypes = udf.getParameterTypes();
        if (args.length != parameterTypes.length) {
            return null;
        }
        int[] parameterCategories = new int[parameterTypes.length];
        Type[] castArgTypes = new Type[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Type parameterType = parameterTypes[i];
            final Exp arg = args[i];
            final Type argType = arg.getType();
            final int parameterCategory =
                TypeUtil.typeToCategory(parameterType);
            if (!validator.canConvert(
                i, arg, parameterCategory, conversions))
            {
                return null;
            }
            parameterCategories[i] = parameterCategory;
            if (!parameterType.equals(argType)) {
                castArgTypes[i] =
                    FunDefBase.castType(argType, parameterCategory);
            }
        }
        final Type returnType = udf.getReturnType(castArgTypes);
        return new UdfFunDef(parameterCategories, returnType);
    }

    public boolean requiresExpression(int k) {
        return false;
    }

    public String[] getReservedWords() {
        final String[] reservedWords = udf.getReservedWords();
        return reservedWords == null ? emptyStringArray : reservedWords;
    }

    /**
     * Adapter which converts a {@link UserDefinedFunction} into a
     * {@link FunDef}.
     */
    private class UdfFunDef extends FunDefBase {
        private Type returnType;

        public UdfFunDef(int[] parameterCategories, Type returnType) {
            super(
                UdfResolver.this,
                TypeUtil.typeToCategory(returnType),
                parameterCategories);
            this.returnType = returnType;
        }

        public Type getResultType(Validator validator, Exp[] args) {
            return returnType;
        }

        public Calc compileCall(ResolvedFunCall call, ExpCompiler compiler) {
            final Exp[] args = call.getArgs();
            Calc[] calcs = new Calc[args.length];
            UserDefinedFunction.Argument[] expCalcs =
                new UserDefinedFunction.Argument[args.length];
            for (int i = 0; i < args.length; i++) {
                Exp arg = args[i];
                final Calc calc = compiler.compileAs(
                    arg,
                    castType(arg.getType(), parameterCategories[i]),
                    ResultStyle.ANY_LIST);
                final Calc scalarCalc = compiler.compileScalar(arg, true);
                final ListCalc listCalc;
                final IterCalc iterCalc;
                if (arg.getType() instanceof SetType) {
                    listCalc = compiler.compileList(arg, true);
                    iterCalc = compiler.compileIter(arg);
                } else {
                    listCalc = null;
                    iterCalc = null;
                }
                expCalcs[i] = new CalcExp(calc, scalarCalc, listCalc, iterCalc);
            }

            // Clone the UDF, because some UDFs use member variables as state.
            UserDefinedFunction udf2 =
                Util.createUdf(
                    udf.getClass(), udf.getName());
            if (call.getType() instanceof SetType) {
                return new ListCalcImpl(call, calcs, udf2, expCalcs);
            } else {
                return new ScalarCalcImpl(call, calcs, udf2, expCalcs);
            }
        }
    }

    /**
     * Expression that evaluates a scalar user-defined function.
     */
    private static class ScalarCalcImpl extends GenericCalc {
        private final Calc[] calcs;
        private final UserDefinedFunction udf;
        private final UserDefinedFunction.Argument[] args;

        public ScalarCalcImpl(
            ResolvedFunCall call,
            Calc[] calcs,
            UserDefinedFunction udf,
            UserDefinedFunction.Argument[] args)
        {
            super(call);
            this.calcs = calcs;
            this.udf = udf;
            this.args = args;
        }

        public Calc[] getCalcs() {
            return calcs;
        }

        public Object evaluate(Evaluator evaluator) {
            return udf.execute(evaluator, args);
        }

        public boolean dependsOn(Dimension dimension) {
            // Be pessimistic. This effectively disables expression caching.
            return true;
        }
    }

    /**
     * Expression that evaluates a list user-defined function.
     */
    private static class ListCalcImpl extends AbstractListCalc {
        private final UserDefinedFunction udf;
        private final UserDefinedFunction.Argument[] args;

        public ListCalcImpl(
            ResolvedFunCall call,
            Calc[] calcs,
            UserDefinedFunction udf,
            UserDefinedFunction.Argument[] args)
        {
            super(call, calcs);
            this.udf = udf;
            this.args = args;
        }

        public List evaluateList(Evaluator evaluator) {
            return (List) udf.execute(evaluator, args);
        }

        public boolean dependsOn(Dimension dimension) {
            // Be pessimistic. This effectively disables expression caching.
            return true;
        }
    }

    /**
     * Wrapper around a {@link Calc} to make it appear as an {@link Exp}.
     * Only the {@link #evaluate(mondrian.olap.Evaluator)}
     * and {@link #evaluateScalar(mondrian.olap.Evaluator)} methods are
     * supported.
     */
    private static class CalcExp implements UserDefinedFunction.Argument {
        private final Calc calc;
        private final Calc scalarCalc;
        private final IterCalc iterCalc;
        private final ListCalc listCalc;

        /**
         * Creates a CalcExp.
         *
         * @param calc Compiled expression
         * @param scalarCalc Compiled expression that evaluates to a scalar
         * @param listCalc Compiled expression that evaluates an MDX set to
         *     a java list
         * @param iterCalc Compiled expression that evaluates an MDX set to
         *     a java iterable
         */
        public CalcExp(
            Calc calc,
            Calc scalarCalc,
            ListCalc listCalc,
            IterCalc iterCalc)
        {
            this.calc = calc;
            this.scalarCalc = scalarCalc;
            this.listCalc = listCalc;
            this.iterCalc = iterCalc;
        }

        public Type getType() {
            return calc.getType();
        }

        public Object evaluate(Evaluator evaluator) {
            return calc.evaluate(evaluator);
        }

        public Object evaluateScalar(Evaluator evaluator) {
            return scalarCalc.evaluate(evaluator);
        }

        public List evaluateList(Evaluator eval) {
            if (listCalc == null) {
                throw new RuntimeException("Expression is not a set");
            }
            return listCalc.evaluateList(eval);
        }

        public Iterable evaluateIterable(Evaluator eval) {
            if (iterCalc == null) {
                throw new RuntimeException("Expression is not a set");
            }
            return iterCalc.evaluateIterable(eval);
        }
    }
}

// End UdfResolver.java
