/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/fun/CustomizedFunctionTable.java#2 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2008-2009 Julian Hyde and others
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.olap.fun;

import java.util.*;

import mondrian.olap.*;

/**
 * Interface to build a customized function table, selecting functions from the
 * set of supported functions in an instance of {@link BuiltinFunTable}.
 *
 * @author Rushan Chen
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/olap/fun/CustomizedFunctionTable.java#2 $
 */
public class CustomizedFunctionTable extends FunTableImpl {

    Set<String> supportedBuiltinFunctions;
    Set<FunDef> specialFunctions;

    public CustomizedFunctionTable(Set<String> builtinFunctions) {
        supportedBuiltinFunctions = builtinFunctions;
        this.specialFunctions = new HashSet<FunDef>();
    }

    public CustomizedFunctionTable(
        Set<String> builtinFunctions,
        Set<FunDef> specialFunctions)
    {
        this.supportedBuiltinFunctions = builtinFunctions;
        this.specialFunctions = specialFunctions;
    }

    public void defineFunctions(Builder builder) {
        final FunTable builtinFunTable = BuiltinFunTable.instance();

        // Includes all the keywords form builtin function table
        for (String reservedWord : builtinFunTable.getReservedWords()) {
            builder.defineReserved(reservedWord);
        }

        // Add supported builtin functions
        for (Resolver resolver : builtinFunTable.getResolvers()) {
            if (supportedBuiltinFunctions.contains(resolver.getName())) {
                builder.define(resolver);
            }
        }

        // Add special function definitions
        for (FunDef funDef : specialFunctions) {
            builder.define(funDef);
        }
    }
}

// End CustomizedFunctionTable.java
