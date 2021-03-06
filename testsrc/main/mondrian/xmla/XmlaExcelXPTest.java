/*
// $Id: //open/mondrian-release/3.1/testsrc/main/mondrian/xmla/XmlaExcelXPTest.java#2 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2002-2009 Julian Hyde and others
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.xmla;

import mondrian.olap.Util;
import mondrian.test.DiffRepository;
import mondrian.tui.*;

import org.custommonkey.xmlunit.XMLAssert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Properties;

/**
 * Test suite for compatibility of Mondrian XMLA with Excel XP.
 * Simba (the maker of the O2X bridge) supplied captured request/response
 * soap messages between Excel XP and SQL Server. These form the
 * basis of the output files in the  excel_XP directory.
 *
 * @author Richard M. Emberson
 * @version $Id: //open/mondrian-release/3.1/testsrc/main/mondrian/xmla/XmlaExcelXPTest.java#2 $
 */
public class XmlaExcelXPTest extends XmlaBaseTestCase {

    private static final boolean DEBUG = false;

    protected String getSessionId(Action action) {
        return getSessionId("XmlaExcel2000Test", action);
    }

    protected String generateExpectedString(String nos, Properties props)
        throws Exception
    {
        String expectedFileName = getOutFileName(nos);

        String expectedStr = fileToString(expectedFileName);
        if (props != null) {
            // YES, duplicate the above
            String sessionId = getSessionId(Action.QUERY);
            if (sessionId != null) {
                props.put(SESSION_ID_PROP, sessionId);
            }
            expectedStr = Util.replaceProperties(
                expectedStr, Util.toMap(props));
        }
        return expectedStr;
    }

    static class Callback implements XmlaRequestCallback {
        String MY_SESSION_ID = "my_session_id";

        Callback() {
        }

        public void init(ServletConfig servletConfig) throws ServletException {
        }

        public boolean processHttpHeader(
            HttpServletRequest request,
            HttpServletResponse response,
            Map<String, Object> context)
            throws Exception
        {
            String expect = request.getHeader(XmlaRequestCallback.EXPECT);
            if ((expect != null)
                && expect.equalsIgnoreCase(
                    XmlaRequestCallback.EXPECT_100_CONTINUE))
            {
                XmlaRequestCallback.Helper.generatedExpectResponse(
                    request, response, context);
                return false;
            } else {
                return true;
            }
        }

        public void preAction(
            HttpServletRequest request,
            Element[] requestSoapParts,
            Map<String, Object> context)
            throws Exception
        {
        }

        private void setSessionId(Map<String, Object> context) {
            context.put(
                MY_SESSION_ID,
                getSessionId("XmlaExcel2000Test", Action.CREATE));
        }

        public String generateSessionId(Map<String, Object> context) {
            setSessionId(context);
            return (String) context.get(MY_SESSION_ID);
        }

        public void postAction(
            HttpServletRequest request,
            HttpServletResponse response,
            byte[][] responseSoapParts,
            Map<String, Object> context)
            throws Exception
        {
        }
    }


    public XmlaExcelXPTest() {
    }

    public XmlaExcelXPTest(String name) {
        super(name);
    }

    protected Class<? extends XmlaRequestCallback> getServletCallbackClass() {
        return Callback.class;
    }

    protected DiffRepository getDiffRepos() {
        return DiffRepository.lookup(XmlaExcelXPTest.class);
    }

    // good 3/28
    public void test01() throws Exception {
        helperTest("01", false);
    }

    // BeginSession
    // good 3/28
    public void test02() throws Exception {
        helperTest("02", false);
    }
    // good 3/28
    public void test03() throws Exception {
        helperTest("03", true);
    }
    // good 3/28
    public void test04() throws Exception {
        helperTest("04", true);
    }
    // good 3/28
    public void test05() throws Exception {
        helperTest("05", true);
    }
    // good 3/28
    public void test06() throws Exception {
        helperTest("06", true);
    }

    // BeginSession
    // good 3/28
    public void test07() throws Exception {
        helperTest("07", false);
    }
    // good 3/28
    public void test08() throws Exception {
        helperTest("08", true);
    }
    // good 3/28
    public void test09() throws Exception {
        helperTest("09", true);
    }
    // good 3/28
    public void test10() throws Exception {
        helperTest("10", true);
    }
    // good 3/28
    public void test11() throws Exception {
        helperTest("11", true);
    }
    // good 3/28
    public void test12() throws Exception {
        helperTest("12", true);
    }
    // good 3/28
    public void test13() throws Exception {
        helperTest("13", true);
    }
    // good 3/28
    public void test14() throws Exception {
        helperTest("14", true);
    }
    // good 3/28
    public void test15() throws Exception {
        helperTest("15", true);
    }
    // good 3/28
    public void test16() throws Exception {
        helperTest("16", true);
    }
    // good 3/28
    public void test17() throws Exception {
        helperTest("17", true);
    }
    // The slicerAxis is empty in Mondrian by not empty in SQLServer.
    // The xml schema returned by SQL Server is not the version 1.0
    // schema returned by Mondrian.
    // Values are correct.
    public void _test18() throws Exception {
        helperTest("18", true);
    }
    // good 3/28
    public void test19() throws Exception {
        helperTest("19", true);
    }
    // good 3/28
    public void test20() throws Exception {
        helperTest("20", true);
    }
    // Same issue as test18: slicerAxis
    public void _test21() throws Exception {
        helperTest("21", true);
    }
    // Same issue as test18: slicerAxis
    public void _test22() throws Exception {
        helperTest("22", true);
    }
    // good 3/28
    public void test23() throws Exception {
        helperTest("23", true);
    }
    public void test24() throws Exception {
        helperTest("24", true);
    }

    protected void helperTest(String nos, boolean doSessionId)
        throws Exception
    {
        if (doSessionId) {
            getSessionId(Action.CREATE);
        }
        Properties props = new Properties();
        doTest(nos, props);
    }

    /////////////////////////////////////////////////////////////////////////
    // expect
    /////////////////////////////////////////////////////////////////////////
    public void testExpect01() throws Exception {
        helperTestExpect("01", false);
    }
    public void testExpect02() throws Exception {
        helperTestExpect("02", false);
    }
    public void testExpect03() throws Exception {
        helperTestExpect("03", true);
    }
    public void testExpect04() throws Exception {
        helperTestExpect("04", true);
    }
    public void testExpect05() throws Exception {
        helperTestExpect("05", true);
    }
    public void testExpect06() throws Exception {
        helperTestExpect("06", true);
    }

    protected void helperTestExpect(String nos, boolean doSessionId)
        throws Exception
    {
        if (doSessionId) {
            getSessionId(Action.CREATE);
        }
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("POST");
        req.setContentType("text/xml");
        req.setHeader(
            XmlaRequestCallback.EXPECT,
            XmlaRequestCallback.EXPECT_100_CONTINUE);

        Properties props = new Properties();
        doTest(req, nos, props);
    }

    /////////////////////////////////////////////////////////////////////////
    // helper
    /////////////////////////////////////////////////////////////////////////

    public void doTest(
        MockHttpServletRequest req,
        String nos,
        Properties props)
        throws Exception
    {
        String requestText = generateRequestString(nos, props);

        MockHttpServletResponse res = new MockHttpServletResponse();
        res.setCharacterEncoding("UTF-8");

        Servlet servlet = getServlet(getTestContext());
        servlet.service(req, res);

        int statusCode = res.getStatusCode();
        if (statusCode == HttpServletResponse.SC_OK) {
            byte[] bytes = res.toByteArray();
            String expectedStr = generateExpectedString(nos, props);
            Document expectedDoc = XmlUtil.parseString(expectedStr);
            validate(bytes, expectedDoc);

        } else if (statusCode == HttpServletResponse.SC_CONTINUE) {
            // remove the Expect header from request and try again
if (DEBUG) {
System.out.println("Got CONTINUE");
}

            req.clearHeader(XmlaRequestCallback.EXPECT);
            req.setBodyContent(requestText);

            servlet.service(req, res);

            statusCode = res.getStatusCode();
            if (statusCode == HttpServletResponse.SC_OK) {
                byte[] bytes = res.toByteArray();
                String expectedStr = generateExpectedString(nos, props);
                Document expectedDoc = XmlUtil.parseString(expectedStr);
                validate(bytes, expectedDoc);

            } else {
                fail("Bad status code: " + statusCode);
            }
        } else {
            fail("Bad status code: " + statusCode);
        }
    }

    public void doTest(String nos, Properties props) throws Exception {
        String requestText = generateRequestString(nos, props);
        Document reqDoc = XmlUtil.parseString(requestText);

        Servlet servlet = getServlet(getTestContext());
        byte[] bytes = XmlaSupport.processSoapXmla(reqDoc, servlet);

        String expectedStr = generateExpectedString(nos, props);

        Document expectedDoc = XmlUtil.parseString(expectedStr);
        validate(bytes, expectedDoc);
    }

    protected void validate(byte[] bytes, Document expectedDoc)
        throws Exception
    {
if (DEBUG) {
        String response = new String(bytes);
        System.out.println("response=" + response);
}
        if (XmlUtil.supportsValidation()) {
            if (XmlaSupport.validateSoapXmlaUsingXpath(bytes)) {
if (DEBUG) {
                System.out.println("XML Data is Valid");
}
            }
        }

        Document gotDoc = XmlUtil.parse(bytes);
        String gotStr =
            XmlUtil.toString(replaceLastSchemaUpdateDate(gotDoc), true);
        String expectedStr =
            XmlUtil.toString(replaceLastSchemaUpdateDate(expectedDoc), true);

if (DEBUG) {
System.out.println("GOT:\n" + gotStr);
System.out.println("EXPECTED:\n" + expectedStr);
System.out.println("XXXXXXX");
}
        gotStr = Util.maskVersion(gotStr);
        gotStr = getTestContext().upgradeActual(gotStr);
        XMLAssert.assertXMLEqual(expectedStr, gotStr);
    }

    protected String generateRequestString(String nos, Properties props)
        throws Exception
    {
        String reqFileName = "excel_XP_" + nos + "_in.xml";
if (DEBUG) {
System.out.println("reqFileName=" + reqFileName);
}
        String requestText = fileToString(reqFileName);
        if (props != null) {
            String sessionId = getSessionId(Action.QUERY);
            if (sessionId != null) {
                props.put(SESSION_ID_PROP, sessionId);
            }
            requestText =
                Util.replaceProperties(requestText, Util.toMap(props));
        }
if (DEBUG) {
System.out.println("requestText=" + requestText);
}
        return requestText;
    }

    protected String getOutFileName(String nos) {
        return "excel_XP_" + nos + "_out.xml";
    }
}

// End XmlaExcelXPTest.java
