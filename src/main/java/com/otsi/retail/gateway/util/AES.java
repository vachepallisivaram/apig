package com.otsi.retail.gateway.util;

import java.security.InvalidParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AES {

	private static Logger logger = LogManager.getLogger(AES.class);

	static String KEY_AES = "0000000000000000";

	final static String aesSecretKey = "23KAVfsyYqk+hxye3/LDM59Ts8hTiAs=";

	private static SecretKey secretKey;

	// 32 bit secret key
	// private static String key = "23KAVfsyYqk+hxna3/LXM56Ts8hTiEs=";
	private static String key = "ONhvh7OmkFJYBvZfk4rNIt3Wwxm4c4U=";

	public static SecretKey getAESKey() throws NoSuchAlgorithmException {
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(256, SecureRandom.getInstanceStrong());
		return keyGen.generateKey();
	}

	public static SecretKey generateAESKey(int keysize) throws InvalidParameterException {
		try {
			if (Cipher.getMaxAllowedKeyLength("AES") < keysize) {
				// this may be an issue if unlimited crypto is not installed
				throw new InvalidParameterException("Key size of " + keysize + " not supported in this runtime");
			}

			final KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(keysize);
			return keyGen.generateKey();
		} catch (final NoSuchAlgorithmException e) {
			// AES functionality is a requirement for any Java SE runtime
			throw new IllegalStateException("AES should always be present in a Java SE runtime", e);
		}
	}

	public static String encodeAESKeyToBase64(final SecretKey aesKey) throws IllegalArgumentException {
		if (!aesKey.getAlgorithm().equalsIgnoreCase("AES")) {
			throw new IllegalArgumentException("Not an AES key");
		}

		final byte[] keyData = aesKey.getEncoded();
		final String encodedKey = Base64.getEncoder().encodeToString(keyData);
		return encodedKey;
	}

	// AES key derived from a password
	public static SecretKey getKeyFromPassword(String password, String salt)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
		SecretKey originalKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
		return originalKey;
	}

	public static byte[] getRandomNonce() {
		byte[] nonce = new byte[16];
		new SecureRandom().nextBytes(nonce);
		return nonce;
	}

	public static String encrypt(final String strToEncrypt) {
		try {
			Key secretKey = new SecretKeySpec(key.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			// Cipher cipher = Cipher.getInstance("AES");

			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes()));
		} catch (Exception e) {
			System.out.println("Error while encrypting: " + e.toString());
		}
		return null;
	}

	public static String decrypt(String value, String key) {
		try {
			byte[] ivs = KEY_AES.getBytes("UTF-8");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "AES");
			AlgorithmParameterSpec paramSpec = new IvParameterSpec(ivs);
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, paramSpec);
			return new String(cipher.doFinal(Base64.getDecoder().decode(value)));
		} catch (Exception e) {
			logger.error("exception while decrypt for content " + value, e.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid request");
		}
	}

	public static String decrypt(String value) {
		return decrypt(value, aesSecretKey);
	}

	private String keyToString() throws NoSuchAlgorithmException {
		SecretKey secretKey = getAESKey();
		SecretKeySpec mAesKey = new SecretKeySpec(secretKey.getEncoded(), "AES");
		return encodeAESKeyToBase64(mAesKey);
	}

	public static void main(String args[]) throws NoSuchAlgorithmException, InvalidKeySpecException {
		// String encryptedString = "uvKaQ7OrSSr4DL44zpQxEw==";
		String encryptedString = "IvthG6be0I5vsAZdWkua4EON4SX9oUkkIFckco5S4r8UVT75SH3Rrigi+zg5qXGE";

		String decryptedString = AES.decrypt(encryptedString);
		System.out.println("decrypted:" + decryptedString);
	}

}
