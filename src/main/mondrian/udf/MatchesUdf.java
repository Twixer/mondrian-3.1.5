/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/udf/MatchesUdf.java#2 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2006-2009 Julian Hyde and others
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.udf;

import mondrian.olap.*;
import mondrian.olap.type.*;
import mondrian.spi.UserDefinedFunction;
import mondrian.util.*;

import java.util.*;
import java.util.regex.*;

/**
 * User-defined function <code>MATCHES</code>.
 *
 * @author schoi
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/udf/MatchesUdf.java#2 $
 */
public class MatchesUdf implements UserDefinedFunction {

    public Object execute(Evaluator evaluator, Argument[] arguments) {
        Object arg0 = arguments[0].evaluateScalar(evaluator);
        Object arg1 = arguments[1].evaluateScalar(evaluator);

        return Boolean.valueOf(Pattern.matches((String)arg1, (String)arg0));
    }

    public String getDescription() {
        return "Returns true if the string matches the regular expression.";
    }

    public String getName() {
        return "MATCHES";
    }

    public Type[] getParameterTypes() {
        return new Type[] {
            new StringType(),
            new StringType()
        };
    }

    public String[] getReservedWords() {
        // This function does not require any reserved words.
        return null;
    }

    public Type getReturnType(Type[] parameterTypes) {
        return new BooleanType();
    }

    public Syntax getSyntax() {
        return Syntax.Infix;
    }

}

// End MatchesUdf.java
