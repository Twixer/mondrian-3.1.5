/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/xmla/XmlaResponse.java#2 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2005-2006 Julian Hyde and others
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.xmla;


/**
 * XML/A response interface.
 *
 * @author Gang Chen
 */
public interface XmlaResponse {

    /**
     * Report XML/A error (not SOAP fault).
     */
    public void error(Throwable t);

    /**
     * Get helper for writing XML document.
     */
    public SaxWriter getWriter();
}

// End XmlaResponse.java
