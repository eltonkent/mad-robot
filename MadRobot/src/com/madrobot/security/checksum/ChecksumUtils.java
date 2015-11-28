package com.madrobot.security.checksum;

import java.util.zip.Adler32;

/**
 * Collection of checksum algorithms
 * 
 * @author elton.stephen.kent
 * 
 */
public class ChecksumUtils {

	/**
	 * Calculate CRC16 value of given byt
	 * 
	 * @param aByte
	 * @return
	 */
	public static int getCRC16(byte aByte) {
		int a, b;
		int value = 0;
		a = aByte;
		for (int count = 7; count >= 0; count--) {
			a = a << 1;
			b = (a >>> 8) & 1;
			if ((value & 0x8000) != 0) {
				value = ((value << 1) + b) ^ 0x1021;
			} else {
				value = (value << 1) + b;
			}
		}
		value = value & 0xffff;
		return value;
	}

	/**
	 * Luhn Algorithm implementation.
	 * 
	 * <p>
	 * Luhn Algorithm is used as a simple checksum for a variety of
	 * identification numbers like credit card numbers, IMEI numbers, SSN etc.
	 * <a href="http://en.wikipedia.org/wiki/Luhn_algorithm">Luhn</a> validation
	 * </p>
	 * 
	 * @param number
	 *            to validate using luhn algorithm
	 * @return true if the checksum passes (ie) Result%10 ==0 ||Result%5==0.
	 */
	public static boolean getLuhnMod10(String number) {
		final int[][] sumTable = { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 },
				{ 0, 2, 4, 6, 8, 1, 3, 5, 7, 9 } };
		int sum = 0, flip = 0;

		for (int i = number.length() - 1; i >= 0; i--, flip++)
			sum += sumTable[flip & 0x1][number.charAt(i) - '0'];
		System.out.println("Luhn Sum " + sum + " Result " + (sum % 10 == 0));
		return (sum % 10 == 0) || (sum % 5 == 0);
	}

	/**
	 * BSD Checksum implementation
	 * 
	 * @param data
	 * @return
	 */
	public static int getBSD(byte[] data) {
		int checksum = 0;
		for (int i = 0; i < data.length; i++) {
			checksum = (checksum >> 1) + ((checksum & 1) << 15);
			checksum += i;
			checksum &= 0xffff;
		}
		return checksum;
	}

	/**
	 * Calculates the CRC32 value
	 * 
	 * @param data
	 * @return CRC 32 value
	 */
	public static int getCRC32(final byte[] data) {
		return getCRC32(data, 0, data.length);
	}

	/**
	 * Calculates the CRC32 value. This function doesn't use a table for reasons
	 * of memory saving.
	 * 
	 * @param data
	 *            the byte array
	 * @param offset
	 *            the offset of the start of the data in the array
	 * @param count
	 *            the count of bytes
	 * @return the CRC32 value
	 */
	public static int getCRC32(final byte[] data, int offset, int count) {
		/* CRC32 calculations */
		final int CRC32_POLYNOMIAL = 0xEDB88320;
		int crc = 0xFFFFFFFF;
		while (count-- != 0) {
			int t = (crc ^ data[offset++]) & 0xFF;
			for (int i = 8; i > 0; i--) {
				if ((t & 1) == 1) {
					t = (t >>> 1) ^ CRC32_POLYNOMIAL;
				} else {
					t >>>= 1;
				}
			}
			crc = (crc >>> 8) ^ t;
		}
		return crc ^ 0xFFFFFFFF;
	}

	/**
	 * Compute Adler32 for the given array of bytes.
	 * 
	 * @param buf
	 * @return
	 */
	public static long getAdler32(byte[] buf) {
		Adler32 adler = new Adler32();
		adler.update(buf);
		return adler.getValue();
	}

}
