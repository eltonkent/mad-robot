package com.madrobot.di.plist;

import java.io.IOException;

/**
 * A UID. Only found in binary property lists that are keyed archives.
 */
public class UID extends NSObject {

	private byte[] bytes;
	private String name;

	public UID(String name, byte[] bytes) {
		this.name = name;
		this.bytes = bytes;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public String getName() {
		return name;
	}

	/**
	 * There is no XML representation specified for UIDs. In this implementation
	 * UIDs are represented as strings in the XML output.
	 * 
	 * @param xml
	 *            The xml StringBuilder
	 * @param level
	 *            The indentation level
	 */
	@Override
	void toXML(StringBuilder xml, int level) {
		indent(xml, level);
		xml.append("<string>");
		xml.append(new String(bytes));
		xml.append("</string>");
	}

	@Override
	void toBinary(BinaryPropertyListWriter out) throws IOException {
		out.write(0x80 + bytes.length - 1);
		out.write(bytes);
	}
}
