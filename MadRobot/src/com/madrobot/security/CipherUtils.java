package com.madrobot.security;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class CipherUtils {

	public static String encryptAEStoHEX(String input, String key)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException {
		byte[] desKeyData = key.getBytes();
		SecretKeySpec secretKey = new SecretKeySpec(desKeyData, "AES");
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] cipherText = c.doFinal(input.getBytes());
		String retValue = new String(HexUtils.toHexString(cipherText));
		return retValue;
	}

	public static String decryptAESfromHEX(String input, String key)
			throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException {

		byte[] inputBytes;
		try {
			inputBytes = HexUtils.hexStringToByteArray(input);
		} catch (Exception e) {
			throw new IllegalArgumentException("The input hex is not valid.");
		}

		byte[] desKeyData = key.getBytes();
		SecretKeySpec secretKey = new SecretKeySpec(desKeyData, "AES");
		// get cipher object for password-based encryption
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.DECRYPT_MODE, secretKey);
		// Decrypt the ciphertext
		byte[] cleartext = c.doFinal(inputBytes);
		String retValue = new String(cleartext);
		return retValue;
	}
}
