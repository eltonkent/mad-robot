package com.madrobot.di.plist;

import java.io.IOException;

/**
 * Abstract interface for any object contained in a property list. The names and
 * functions of the various objects orient themselves towards Apple's Cocoa API.
 */
public abstract class NSObject {

	/**
	 * The newline character used for generating the XML output. This constant
	 * will be different depending on the operating system on which you use this
	 * library.
	 */
	final static String NEWLINE = System.getProperty("line.separator");

	/**
	 * The identation character used for generating the XML output. This is the
	 * tabulator character.
	 */
	final static String INDENT = "\t";

	/**
	 * Generates the XML representation of the object (without XML headers or
	 * enclosing plist-tags).
	 * 
	 * @param xml
	 *            The StringBuilder onto which the XML representation is
	 *            appended.
	 * @param level
	 *            The indentation level of the object.
	 */
	abstract void toXML(StringBuilder xml, int level);

	/**
	 * Assigns IDs to all the objects in this NSObject subtree.
	 * 
	 * @param out
	 *            The writer object that handles the binary serialization.
	 */
	void assignIDs(BinaryPropertyListWriter out) {
		out.assignID(this);
	}

	/**
	 * Generates the binary representation of the object.
	 * 
	 * @param out
	 *            The output stream to serialize the object to.
	 */
	abstract void toBinary(BinaryPropertyListWriter out) throws IOException;

	/**
	 * Generates a valid XML property list including headers using this object
	 * as root.
	 * 
	 * @return The XML representation of the property list including XML header
	 *         and doctype information.
	 */
	public String toXMLPropertyList() {
		StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		xml.append(NSObject.NEWLINE);
		xml.append("<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">");
		xml.append(NSObject.NEWLINE);
		xml.append("<plist version=\"1.0\">");
		xml.append(NSObject.NEWLINE);
		toXML(xml, 0);
		xml.append(NSObject.NEWLINE);
		xml.append("</plist>");
		return xml.toString();
	}

	/**
	 * Helper method that adds correct identation to the xml output. Calling
	 * this method will add <code>level</code> number of tab characters to the
	 * <code>xml</code> string.
	 * 
	 * @param xml
	 *            The string builder for the XML document.
	 * @param level
	 *            The level of identation.
	 */
	void indent(StringBuilder xml, int level) {
		for (int i = 0; i < level; i++)
			xml.append(INDENT);
	}
}
