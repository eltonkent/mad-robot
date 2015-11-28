package com.madrobot.di.plist;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * A NSString contains a string.
 */
public class NSString extends NSObject {

	private String content;

	/**
	 * Creates an NSString from its binary representation.
	 * 
	 * @param bytes
	 *            The binary representation.
	 * @param encoding
	 *            The encoding of the binary representation, the name of a
	 *            supported charset.
	 * @see java.lang.String
	 * @throws UnsupportedEncodingException
	 */
	public NSString(byte[] bytes, String encoding) throws UnsupportedEncodingException {
		content = new String(bytes, encoding);
	}

	/**
	 * Creates a NSString from a string.
	 * 
	 * @param string
	 *            The string that will be contained in the NSString.
	 */
	public NSString(String string) {
		try {
			content = new String(string.getBytes("UTF-8"), "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof NSString))
			return false;
		return content.equals(((NSString) obj).content);
	}

	@Override
	public int hashCode() {
		return content.hashCode();
	}

	/**
	 * The textual representation of this NSString.
	 * 
	 * @return The NSString's contents.
	 */
	@Override
	public String toString() {
		return content;
	}

	@Override
	void toXML(StringBuilder xml, int level) {
		indent(xml, level);
		xml.append("<string>");
		// According to http://www.w3.org/TR/REC-xml/#syntax node values must
		// not
		// contain the characters < or &. Also the > character should be
		// escaped.
		if (content.contains("&") || content.contains("<") || content.contains(">")) {
			xml.append("<![CDATA[");
			xml.append(content.replaceAll("]]>", "]]]]><![CDATA[>"));
			xml.append("]]>");
		} else {
			xml.append(content);
		}
		xml.append("</string>");
	}

	private static CharsetEncoder asciiEncoder, utf16beEncoder;

	@Override
	public void toBinary(BinaryPropertyListWriter out) throws IOException {
		CharBuffer charBuf = CharBuffer.wrap(content);
		int kind;
		ByteBuffer byteBuf;
		synchronized (NSString.class) {
			if (asciiEncoder == null)
				asciiEncoder = Charset.forName("ASCII").newEncoder();
			else
				asciiEncoder.reset();

			if (asciiEncoder.canEncode(charBuf)) {
				kind = 0x5; // standard ASCII
				byteBuf = asciiEncoder.encode(charBuf);
			} else {
				if (utf16beEncoder == null)
					utf16beEncoder = Charset.forName("UTF-16BE").newEncoder();
				else
					utf16beEncoder.reset();

				kind = 0x6; // UTF-16-BE
				byteBuf = utf16beEncoder.encode(charBuf);
			}
		}
		byte[] bytes = new byte[byteBuf.remaining()];
		byteBuf.get(bytes);
		out.writeIntHeader(kind, content.length());
		out.write(bytes);
	}
}
