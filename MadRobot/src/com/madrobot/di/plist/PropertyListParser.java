package com.madrobot.di.plist;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

/**
 * This class provides methods to parse property lists. It can handle files,
 * input streams and byte arrays. All known property list formats are supported.
 * 
 * This class also provides methods to save and convert property lists.
 * 
 */
public class PropertyListParser {

	/**
	 * Objects are unneccesary.
	 */
	private PropertyListParser() {
		/** empty **/
	}

	/**
	 * Reads all bytes from an InputStream and stores them in an array, up to a
	 * maximum count.
	 * 
	 * @param in
	 *            The InputStream pointing to the data that should be stored in
	 *            the array.
	 * @param max
	 *            The maximum number of bytes to read.
	 **/
	protected static byte[] readAll(InputStream in, int max) throws IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		while (max > 0) {
			int n = in.read();
			if (n == -1)
				break; // EOF
			buf.write(n);
			max--;
		}
		return buf.toByteArray();
	}

	/**
	 * Parses a property list from a file.
	 * 
	 * @param f
	 *            The property list file.
	 * @return The root object in the property list. This is usually a
	 *         NSDictionary but can also be a NSArray.
	 * @throws Exception
	 *             If an error occurred while parsing.
	 */
	public static NSObject parse(File f) throws Exception {
		FileInputStream fis = new FileInputStream(f);
		String magicString = new String(readAll(fis, 8), 0, 8);
		fis.close();
		if (magicString.startsWith("bplist")) {
			return BinaryPropertyListParser.parse(f);
		} else if (magicString.startsWith("<?xml")) {
			return XMLPropertyListParser.parse(f);
		} else if (magicString.startsWith("(") || magicString.startsWith("{")) {
			return ASCIIPropertyListParser.parse(f);
		} else {
			throw new UnsupportedOperationException(
					"The given data is not a valid property list. For supported format see http://code.google.com/p/plist");
		}
	}

	/**
	 * Parses a property list from a byte array.
	 * 
	 * @param bytes
	 *            The property list data as a byte array.
	 * @return The root object in the property list. This is usually a
	 *         NSDictionary but can also be a NSArray.
	 * @throws Exception
	 *             If an error occurred while parsing.
	 */
	public static NSObject parse(byte[] bytes) throws Exception {
		String magicString = new String(bytes, 0, 8);
		if (magicString.startsWith("bplist")) {
			return BinaryPropertyListParser.parse(bytes);
		} else if (magicString.startsWith("<?xml")) {
			return XMLPropertyListParser.parse(bytes);
		} else if (magicString.startsWith("(") || magicString.startsWith("{")) {
			return ASCIIPropertyListParser.parse(bytes);
		} else {
			throw new UnsupportedOperationException(
					"The given data is not a valid property list. For supported format see http://code.google.com/p/plist");
		}
	}

	/**
	 * Parses a property list from an InputStream.
	 * 
	 * @param is
	 *            The InputStream delivering the property list data.
	 * @return The root object of the property list. This is usually a
	 *         NSDictionary but can also be a NSArray.
	 * @throws Exception
	 *             If an error occurred while parsing.
	 */
	public static NSObject parse(InputStream is) throws Exception {
		if (is.markSupported()) {
			is.mark(10);
			String magicString = new String(readAll(is, 8), 0, 8);
			is.reset();
			if (magicString.startsWith("bplist")) {
				return BinaryPropertyListParser.parse(is);
			} else if (magicString.startsWith("<?xml")) {
				return XMLPropertyListParser.parse(is);
			} else if (magicString.startsWith("(") || magicString.startsWith("{")) {
				return ASCIIPropertyListParser.parse(is);
			} else {
				throw new UnsupportedOperationException(
						"The given data is not a valid property list.");
			}
		} else {
			// Now we have to read everything, because if one parsing method
			// fails
			// the whole InputStream is lost as we can't reset it
			return parse(readAll(is, Integer.MAX_VALUE));
		}
	}

	/**
	 * Saves a property list with the given object as root into a XML file.
	 * 
	 * @param root
	 *            The root object.
	 * @param out
	 *            The output file.
	 * @throws IOException
	 *             When an error occurs during the writing process.
	 */
	public static void saveAsXML(NSObject root, File out) throws IOException {
		OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(out), "UTF-8");
		w.write(root.toXMLPropertyList());
		w.close();
	}

	/**
	 * Converts a given property list file into the OS X and iOS XML format.
	 * 
	 * @param in
	 *            The source file.
	 * @param out
	 *            The target file.
	 * @throws Exception
	 *             When an error occurs during parsing or converting.
	 */
	public static void convertToXml(File in, File out) throws Exception {
		NSObject root = parse(in);
		saveAsXML(root, out);
	}

	/**
	 * Saves a property list with the given object as root into a binary file.
	 * 
	 * @param root
	 *            The root object.
	 * @param out
	 *            The output file.
	 * @throws IOException
	 *             When an error occurs during the writing process.
	 */
	public static void saveAsBinary(NSObject root, File out) throws IOException {
		BinaryPropertyListWriter.write(out, root);
	}

	/**
	 * Converts a given property list file into the OS X and iOS binary format.
	 * 
	 * @param in
	 *            The source file.
	 * @param out
	 *            The target file.
	 * @throws Exception
	 *             When an error occurs during parsing or converting.
	 */
	public static void convertToBinary(File in, File out) throws Exception {
		NSObject root = parse(in);
		saveAsBinary(root, out);
	}
}
