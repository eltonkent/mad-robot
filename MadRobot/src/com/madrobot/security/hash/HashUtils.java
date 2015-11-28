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
package com.madrobot.security.hash;

/**
 * One stop class for the various Hash implementations.
 * 
 * @author elton.kent
 * 
 */
public final class HashUtils {

	/**
	 * Perform a MD5 checksum on the given byte array
	 * 
	 * @param data
	 * @return Hash array of 16 bytes
	 */
	public static byte[] doMD5(byte[] data) {
		final MD5 md5 = new MD5(data);
		return md5.Final();
	}

	/**
	 * Perform a MD5 checksum on the given object
	 * 
	 * @param object
	 * @return Hash array of 16 bytes
	 */
	public static byte[] doMD5(Object object) {
		final MD5 md5 = new MD5(object);
		return md5.Final();
	}

	/**
	 * Perform a MD5 checksum on the given byte array
	 * 
	 * @param data
	 * @return String representation of the MD5 checksum
	 */
	public static String doMD5asHEX(final byte[] data) {
		final MD5 md5 = new MD5(data);
		return md5.asHex();
	}

	/**
	 * Perform a MD5 checksum on the given object
	 * 
	 * @param object
	 * @return String representation of the MD5 checksum
	 */
	public static String doMD5asHEX(Object object) {
		final MD5 md5 = new MD5(object);
		return md5.asHex();
	}

	/**
	 * Do a Jenkins hash on the given byte array
	 * 
	 * @param data
	 * @return
	 */
	public static long doJenkinsHash(final byte[] data) {
		final JenkinsHash hash = new JenkinsHash();
		return hash.hash(data);
	}

}
