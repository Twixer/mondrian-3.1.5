/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/recorder/PrintStreamRecorder.java#2 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2005-2009 Julian Hyde and others
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.recorder;

import mondrian.olap.Util;

import java.io.PrintStream;

/**
 * Implementation of {@link MessageRecorder} simply writes messages to
 * PrintStreams.
 *
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/recorder/PrintStreamRecorder.java#2 $
 */
public class PrintStreamRecorder extends AbstractRecorder {
    private final PrintStream err;
    private final PrintStream out;

    public PrintStreamRecorder() {
        this(System.out, System.err);
    }

    public PrintStreamRecorder(final PrintStream out, final PrintStream err) {
        this.out = out;
        this.err = err;
    }

    protected void recordMessage(
        final String msg,
        final Object info,
        final MsgType msgType)
    {
        PrintStream ps;
        String prefix;
        switch (msgType) {
        case INFO:
            prefix = "INFO: ";
            ps = out;
            break;
        case WARN:
            prefix = "WARN: ";
            ps = out;
            break;
        case ERROR:
            prefix = "ERROR: ";
            ps = err;
            break;
        default:
            throw Util.unexpected(msgType);
        }
        String context = getContext();

        ps.print(prefix);
        ps.print(context);
        ps.print(": ");
        ps.println(msg);
    }
}

// End PrintStreamRecorder.java
