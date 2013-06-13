/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/gui/validate/Messages.java#3 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2008-2009 Pentaho
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.gui.validate;

/**
 * Message provider. Extracted interface from <code>mondrian.gui.I18n</code>.
 *
 * @author mlowery
 */
public interface Messages {
    /**
     * Returns the string with given key.
     *
     * @param stringId key
     * @param defaultValue default if key does not exist
     * @return message
     */
    String getString(
        String stringId,
        String defaultValue);

    /**
     * Returns the string with given key with substitutions.
     *
     * @param stringId Key
     * @param defaultValue default if key does not exist
     * @param args arguments to substitute
     * @return message
     */
    String getFormattedString(
        String stringId,
        String defaultValue,
        Object... args);
}

// End Messages.java
