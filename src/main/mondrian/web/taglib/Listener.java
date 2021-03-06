/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/web/taglib/Listener.java#2 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2002-2002 Kana Software, Inc.
// Copyright (C) 2002-2007 Julian Hyde and others
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
//
// Julian Hyde, 28 March, 2002
*/
package mondrian.web.taglib;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * <code>Listener</code> creates and destroys a {@link ApplResources} at the
 * appropriate times in the servlet's life-cycle.
 *
 * <p>NOTE: This class must not depend upon any non-standard packages (such as
 * <code>javax.transform</code>) because it is loaded when Tomcat starts, not
 * when the servlet is loaded. (This might be a bug in Tomcat 4.0.3, because
 * it worked in 4.0.1. But anyway.)
 *
 * @version $Id: //open/mondrian-release/3.1/src/main/mondrian/web/taglib/Listener.java#2 $
 */

public class Listener implements ServletContextListener {

    ApplicationContext applicationContext;

    public Listener() {
    }

    public void contextInitialized(ServletContextEvent event) {
        Class clazz;
        try {
            clazz = Class.forName("mondrian.web.taglib.ApplResources");
        } catch (ClassNotFoundException e) {
            throw new Error(
                "Received [" + e.toString() + "] while initializing servlet");
        }
        Object o = null;
        try {
            o = clazz.newInstance();
        } catch (InstantiationException e) {
            throw new Error(
                "Received [" + e.toString() + "] while initializing servlet");
        } catch (IllegalAccessException e) {
            throw new Error(
                "Received [" + e.toString() + "] while initializing servlet");
        }
        applicationContext = (ApplicationContext) o;
        applicationContext.init(event);
    }

    public void contextDestroyed(ServletContextEvent event) {
        if (applicationContext != null) {
            applicationContext.destroy(event);
        }
    }

    interface ApplicationContext {
        void init(ServletContextEvent event);
        void destroy(ServletContextEvent event);
    }
}

// End Listener.java
