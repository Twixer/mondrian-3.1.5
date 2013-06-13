/*
// $Id: //open/mondrian-release/3.1/src/main/mondrian/xmla/impl/DefaultSaxWriter.java#3 $
// This software is subject to the terms of the Eclipse Public License v1.0
// Agreement, available at the following URL:
// http://www.eclipse.org/legal/epl-v10.html.
// Copyright (C) 2005-2009 Julian Hyde
// All Rights Reserved.
// You must accept the terms of that agreement to use this software.
*/
package mondrian.xmla.impl;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import mondrian.xmla.SaxWriter;
import mondrian.util.ArrayStack;

import org.eigenbase.xom.XMLUtil;
import org.eigenbase.xom.XOMUtil;
import org.xml.sax.Attributes;

/**
 * Default implementation of {@link SaxWriter}.
 *
 * @author jhyde
 * @author Gang Chen
 * @since 27 April, 2003
 */
public class DefaultSaxWriter implements SaxWriter {
    /** Inside the tag of an element. */
    private static final int STATE_IN_TAG = 0;
    /** After the tag at the end of an element. */
    private static final int STATE_END_ELEMENT = 1;
    /** After the tag at the start of an element. */
    private static final int STATE_AFTER_TAG = 2;
    /** After a burst of character data. */
    private static final int STATE_CHARACTERS = 3;

    private final PrintWriter writer;
    private int indent;
    private String indentStr = "  ";
    private final ArrayStack<String> stack = new ArrayStack<String>();
    private int state = STATE_END_ELEMENT;


    /**
     * Creates a DefaultSaxWriter writing to an {@link java.io.OutputStream}.
     */
    public DefaultSaxWriter(OutputStream stream) {
        this(new OutputStreamWriter(stream));
    }

    public DefaultSaxWriter(OutputStream stream, String xmlEncoding)
        throws UnsupportedEncodingException
    {
        this(new OutputStreamWriter(stream, xmlEncoding));
    }

    /**
     * Creates a <code>SAXWriter</code> writing to a {@link java.io.Writer}.
     *
     * <p>If <code>writer</code> is a {@link java.io.PrintWriter},
     * {@link #DefaultSaxWriter(java.io.OutputStream)} is preferred.
     */
    public DefaultSaxWriter(Writer writer) {
        this(new PrintWriter(writer), 0);
    }

    /**
     * Creates a DefaultSaxWriter writing to a {@link java.io.PrintWriter}.
     *
     * @param writer
     * @param initialIndent
     */
    public DefaultSaxWriter(PrintWriter writer, int initialIndent) {
        this.writer = writer;
        this.indent = initialIndent;
    }

    private void _startElement(
        String namespaceURI,
        String localName,
        String qName,
        Attributes atts)
    {
        _checkTag();
        if (indent > 0) {
            writer.println();
        }
        for (int i = 0; i < indent; i++) {
            writer.write(indentStr);
        }
        indent++;
        writer.write('<');
        writer.write(qName);
        for (int i = 0; i < atts.getLength(); i++) {
            XMLUtil.printAtt(writer, atts.getQName(i), atts.getValue(i));
        }
        state = STATE_IN_TAG;
    }

    private void _checkTag() {
        if (state == STATE_IN_TAG) {
            state = STATE_AFTER_TAG;
            writer.print(">");
        }
    }

    private void _endElement(
        String namespaceURI,
        String localName,
        String qName)
    {
        indent--;
        if (state == STATE_IN_TAG) {
            writer.write("/>");
        } else {
            if (state != STATE_CHARACTERS) {
                writer.println();
                for (int i = 0; i < indent; i++) {
                    writer.write(indentStr);
                }
            }
            writer.write("</");
            writer.write(qName);
            writer.write('>');
        }
        state = STATE_END_ELEMENT;
    }

    private void _characters(char ch[], int start, int length) {
        _checkTag();

        // Display the string, quoting in <![CDATA[ ... ]]> if necessary,
        // or using XML escapes as a last result.
        String s = new String(ch, start, length);
        if (XOMUtil.stringHasXMLSpecials(s)) {
            XMLUtil.stringEncodeXML(s, writer);
/*
            if (s.indexOf("]]>") < 0) {
                writer.print("<![CDATA[");
                writer.print(s);
                writer.print("]]>");
            } else {
                XMLUtil.stringEncodeXML(s, writer);
            }
*/
        } else {
            writer.print(s);
        }

        state = STATE_CHARACTERS;
    }


    //
    // Simplifying methods

    public void characters(String s) {
        if (s != null && s.length() > 0) {
            _characters(s.toCharArray(), 0, s.length());
        }
    }

    public void element(String tagName, String... attributes) {
        startElement(tagName, attributes);
        endElement();
    }

    public void startElement(String tagName) {
        _startElement(null, null, tagName, EmptyAttributes);
        stack.add(tagName);
    }

    public void startElement(String tagName, String... attributes) {
        _startElement(null, null, tagName, new StringAttributes(attributes));
        stack.add(tagName);
    }

    public void endElement() {
        String tagName = (String) stack.pop();
        _endElement(null, null, tagName);
    }

    public void startDocument() {
        if (stack.size() != 0) {
            throw new IllegalStateException("Document already started");
        }
    }

    public void endDocument() {
        if (stack.size() != 0) {
            throw new IllegalStateException(
                "Document may have unbalanced elements");
        }
        writer.flush();
    }

    public void completeBeforeElement(String tagName) {
        if (stack.indexOf(tagName) == -1) {
            return;
        }

        String currentTagName  = (String) stack.peek();
        while (!tagName.equals(currentTagName)) {
            _endElement(null, null, currentTagName);
            stack.pop();
            currentTagName = (String) stack.peek();
        }
    }

    public void verbatim(String text) {
        _checkTag();
        writer.print(text);
    }

    public void flush() {
        writer.flush();
    }

    static private final Attributes EmptyAttributes = new Attributes() {
        public int getLength() {
            return 0;
        }

        public String getURI(int index) {
            return null;
        }

        public String getLocalName(int index) {
            return null;
        }

        public String getQName(int index) {
            return null;
        }

        public String getType(int index) {
            return null;
        }

        public String getValue(int index) {
            return null;
        }

        public int getIndex(String uri, String localName) {
            return 0;
        }

        public int getIndex(String qName) {
            return 0;
        }

        public String getType(String uri, String localName) {
            return null;
        }

        public String getType(String qName) {
            return null;
        }

        public String getValue(String uri, String localName) {
            return null;
        }

        public String getValue(String qName) {
            return null;
        }
    };

    /**
     * List of SAX attributes based upon a string array.
     */
    public static class StringAttributes implements Attributes {
        private final String[] strings;

        public StringAttributes(String[] strings) {
            this.strings = strings;
        }

        public int getLength() {
            return strings.length / 2;
        }

        public String getURI(int index) {
            return null;
        }

        public String getLocalName(int index) {
            return null;
        }

        public String getQName(int index) {
            return strings[index * 2];
        }

        public String getType(int index) {
            return null;
        }

        public String getValue(int index) {
            return strings[index * 2 + 1];
        }

        public int getIndex(String uri, String localName) {
            return -1;
        }

        public int getIndex(String qName) {
            final int count = strings.length / 2;
            for (int i = 0; i < count; i++) {
                String string = strings[i * 2];
                if (string.equals(qName)) {
                    return i;
                }
            }
            return -1;
        }

        public String getType(String uri, String localName) {
            return null;
        }

        public String getType(String qName) {
            return null;
        }

        public String getValue(String uri, String localName) {
            return null;
        }

        public String getValue(String qName) {
            final int index = getIndex(qName);
            if (index < 0) {
                return null;
            } else {
                return strings[index * 2 + 1];
            }
        }
    }

}

// End DefaultSaxWriter.java
