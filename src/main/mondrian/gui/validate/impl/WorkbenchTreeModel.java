/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/gui/validate/impl/WorkbenchTreeModel.java#2 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2008-2009 Pentaho
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.gui.validate.impl;

import mondrian.gui.SchemaTreeModel;
import mondrian.gui.validate.TreeModel;

/**
 * Implementation of <code>TreeModel</code> for Workbench.
 *
 * @author mlowery
 */
public class WorkbenchTreeModel implements TreeModel {

    private SchemaTreeModel schemaTreeModel;

    public WorkbenchTreeModel(SchemaTreeModel schemaTreeModel) {
        super();
        this.schemaTreeModel = schemaTreeModel;
    }

    public Object getChild(Object parent, int index) {
        return schemaTreeModel.getChild(parent, index);
    }

    public int getChildCount(Object parent) {
        return schemaTreeModel.getChildCount(parent);
    }

    public Object getRoot() {
        return schemaTreeModel.getRoot();
    }

}

// End WorkbenchTreeModel.java
