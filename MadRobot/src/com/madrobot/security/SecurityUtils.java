package com.madrobot.security;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.UUID;

public class SecurityUtils {

	/**
	 * Converts a array of bytes to hex
	 * 
	 * @param data
	 * @return
	 */
	public static String convertToHex(final byte[] data) {
		final StringBuilder buf = new StringBuilder();
		final int mask = 0x0F, ten = 10, nine = 9, shiftLength = 4;
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> shiftLength) & mask;
			int twoHalfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= nine)) {
					buf.append((char) ('0' + halfbyte));
				} else {
					buf.append((char) ('a' + (halfbyte - ten)));
				}
				halfbyte = data[i] & mask;
			} while (twoHalfs++ < 1);
		}
		return buf.toString();
	}

	/**
	 * Retrieves the private key from the specified keystore.
	 * 
	 * @param keystore
	 *            the path to the keystore file
	 * @param keystorePass
	 *            the password that protects the keystore file
	 * @param keyAlias
	 *            the alias under which the private key is stored
	 * @param keyPass
	 *            the password protecting the private key
	 * @return the private key from the specified keystore
	 * @throws GeneralSecurityException
	 *             if the keystore cannot be loaded
	 * @throws IOException
	 *             if the file cannot be accessed
	 */
	public static PrivateKey getPrivateKeyFromKeystore(String keystore, String keystorePass,
			String keyAlias, String keyPass) throws IOException, GeneralSecurityException {

		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		FileInputStream keyStream = null;
		try {
			keyStream = new FileInputStream(keystore);
			keyStore.load(keyStream, keystorePass.toCharArray());
			return (PrivateKey) keyStore.getKey(keyAlias, keyPass.toCharArray());
		} finally {
			if (keyStream != null) {
				keyStream.close();
			}
		}
	}

	/**
	 * Treats the provided long as unsigned and converts it to a string.
	 * 
	 * @param
	 */
	public static String unsignedLongToString(long value) {
		if (value >= 0) {
			return Long.toString(value);
		} else {
			// Split into two unsigned halves. As digits are printed out from
			// the bottom half, move data from the top half into the bottom
			// half
			int max_dig = 20;
			char[] cbuf = new char[max_dig];
			int radix = 10;
			int dst = max_dig;
			long top = value >>> 32;
			long bot = value & 0xffffffffl;
			bot += (top % radix) << 32;
			top /= radix;
			while (bot > 0 || top > 0) {
				cbuf[--dst] = Character.forDigit((int) (bot % radix), radix);
				bot = (bot / radix) + ((top % radix) << 32);
				top /= radix;
			}
			return new String(cbuf, dst, max_dig - dst);
		}
	}

	/**
	 * Get a uuid as byte array
	 * 
	 * @param uuid
	 * @return
	 */
	public static byte[] asByteArray(UUID uuid) {
		long msb = uuid.getMostSignificantBits();
		long lsb = uuid.getLeastSignificantBits();
		byte[] buffer = new byte[16];

		for (int i = 0; i < 8; i++)
			buffer[i] = (byte) (msb >>> 8 * (7 - i));
		for (int i = 8; i < 16; i++)
			buffer[i] = (byte) (lsb >>> 8 * (7 - i));

		return buffer;
	}
}
