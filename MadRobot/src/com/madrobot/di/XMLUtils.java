/*******************************************************************************
 * Copyright (c) 2011 MadRobot.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *  Elton Kent - initial API and implementation
 ******************************************************************************/
package com.madrobot.di;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.AttributeSet;
import android.util.Xml;

/**
 * XML helper utilities XMLUtility.java
 * 
 * @author Elton Kent
 */
public final class XMLUtils {
	/**
	 * Get the xml as a AttributeSet
	 * 
	 * @param is
	 *            XML InputStream
	 * @return AttributeSet representation of the xml stream
	 * @throws XmlPullParserException
	 */
	public static AttributeSet getAttributeSet(InputStream is) throws XmlPullParserException {
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(is, null);
		AttributeSet set = Xml.asAttributeSet(parser);
		return set;

	}

	/**
	 * Loads an XML stream into a Document instance
	 * 
	 * @param is
	 * @return XML file loaded in a document
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Document loadDoc(InputStream is) throws ParserConfigurationException,
			SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(is);
	}

	private XMLUtils() {
	}

	/**
	 * Escapes an XML text element.
	 * <p>
	 * <code><</code> becomes <code>& lt;</code><br/>
	 * <code>></code> becomes <code>& gt;</code><br/>
	 * and so on..
	 * </p>
	 * 
	 * @param text
	 *            the text data
	 * @return the escaped text
	 */
	public static String escapeXMLText(String text) {
		int length = text.length();
		StringBuilder buff = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			char ch = text.charAt(i);
			switch (ch) {
			case '<':
				buff.append("&lt;");
				break;
			case '>':
				buff.append("&gt;");
				break;
			case '&':
				buff.append("&amp;");
				break;
			case '\'':
				buff.append("&apos;");
				break;
			case '\"':
				buff.append("&quot;");
				break;
			case '\r':
			case '\n':
			case '\t':
				buff.append(ch);
				break;
			default:
				if (ch < ' ' || ch > 127) {
					buff.append("&#x").append(Integer.toHexString(ch)).append(';');
				} else {
					buff.append(ch);
				}
			}
		}
		return buff.toString();
	}

	public static String unescapeXml(String str) {
		str = str.replaceAll("&amp;", "&");
		str = str.replaceAll("&lt;", "<");
		str = str.replaceAll("&gt;", ">");
		str = str.replaceAll("&quot;", "\"");
		str = str.replaceAll("&apos;", "'");
		return str;
	}

	/**
	 * Remove any xml tags from a String. Same as HtmlW's method.
	 * 
	 * @param str
	 *            input string
	 * @return text with all xml tags removed.
	 */
	public static String removeXml(String str) {
		int sz = str.length();
		StringBuffer buffer = new StringBuffer(sz);
		boolean inString = false;
		boolean inTag = false;
		for (int i = 0; i < sz; i++) {
			char ch = str.charAt(i);
			if (ch == '<') {
				inTag = true;
			} else if (ch == '>') {
				inTag = false;
				continue;
			}
			if (!inTag) {
				buffer.append(ch);
			}
		}
		return buffer.toString();
	}

	private static XmlPullParserFactory sFactory;

	/**
	 * Build and return a new {@link XmlPullParser} with the given
	 * {@link InputStream} assigned to it.
	 */
	public static XmlPullParser newPullParser(InputStream input) throws XmlPullParserException {
		if (sFactory == null) {
			sFactory = XmlPullParserFactory.newInstance();
		}
		final XmlPullParser parser = sFactory.newPullParser();
		parser.setInput(input, null);
		return parser;
	}
}
