package com.otsi.retail.gateway.util;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RSAUtil {

	private static String pubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApcFVlWr1VQzgo4pqJYIPoSmdu2VGHv2gnupRs4eCVJTajzSnLORVs/DBSKg0qIaBdiFnmjB7agn1XiRb1ekgwC7Zys4fzIJlW53knV280jDvQ7uBn7NiIp+NUY0ZPZqk2Uh1YD9VhzS8y+wWmjTw6qdmr0ZtzQrmH1R0HHk3WHXLaOF/7rWpo7UOZtXTJfMeTpQ9Jvz/Lv8aS/oSLunY+6Xtf6qClAEg1zt7fvkMoFfiXL9mJXvDlqSFNx2sUaiZ/UL7e40aWMpuJihmUGvMp2Z97RtzXZekNk+2q4s7wg/cOnQcuJSD/yuh2HFoC2B0IOgtXk5Q+RXfMWhQoaImWQIDAQAB";
	private static String pvtKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQClwVWVavVVDOCjimolgg+hKZ27ZUYe/aCe6lGzh4JUlNqPNKcs5FWz8MFIqDSohoF2IWeaMHtqCfVeJFvV6SDALtnKzh/MgmVbneSdXbzSMO9Du4Gfs2Iin41RjRk9mqTZSHVgP1WHNLzL7BaaNPDqp2avRm3NCuYfVHQceTdYdcto4X/utamjtQ5m1dMl8x5OlD0m/P8u/xpL+hIu6dj7pe1/qoKUASDXO3t++QygV+Jcv2Yle8OWpIU3HaxRqJn9Qvt7jRpYym4mKGZQa8ynZn3tG3Ndl6Q2T7arizvCD9w6dBy4lIP/K6HYcWgLYHQg6C1eTlD5Fd8xaFChoiZZAgMBAAECggEBAKHGNtxU/sqafei/n+epr81wi5SpPC3lBk3zjff1artYaJPaJMuIsyii8lIScQqF9HayFaaEaP9OZt8SB1uYY7GOSFxKnvh/z6MCUG1SXoaa4fquV83hSwdVx5xvZNIeS4QR1xUv2y7Rxu4UJt8sinHLHko1J9c2KvErAQQFPoIjKnxbMe/bSuYRTUN28QYI0FYcaaazQwr5YZOl/1nLBaYhst7xuT2je3N6wjjfOLHPWzZUaU7wgTfwAVA8rDwxuKpXQZRcDQOEh7lDrMnV3jQb8myU+BsjNAlBCZixa4SRDjPqgy1A+RFqHeyWfX1xcLldqvjfeFf+LTtRA5w+Nw0CgYEA3DV5Kv09ge1tHGeqBoWNyYrJFGb/bFyCsjODls81jR+KdRmsfbSzToBnVhfmtCbXXb1yhSHYtoAeucR10lldg+FjuU1uHdIY5Q84Mu3XR32yhCqslWcKCoL6j+7dCMkYBM3vYEhHwX/9KduUiNomjaTqy7blMTNZkNmu7TnLBRMCgYEAwLIiU3+J39VCFaeA6ygCyN9LY+hfzKF0t0oKiyTQEAdRozqFUACUkPEHbMkN9fctsKk2J7/sKi3UypgrXp14DpKAQYNoZ0P9H0sTKB7auHkAEcmLKiaVao0qkDP9R60yskphQVUagFxrsdq4rLMJmYhZ8hRLuvG7Hc8DHHi6EGMCgYBfcH2XeDJUQYbsrLEwKmoNU61avlktqdqrSVBa6GuZQnZL0ljEErEz8MrYsXs30S2+XwFnWggG8PhgIxm9lSXGpsUF5t0253wKqtH8oMCRJ5VbWvN5vTLI44OdJjV+PUm/q1F4NVuELeiXX6e8uG/FLBjnOdmTvWGwOehqwIWRKQKBgFx+pR9zXtkEYr5GMwmtoPiOxn9kcWemMIIyljEIMJ8hBDzXwFoQjT+tkqTTNVJVnabo8kfORixQ541/0YGPEKveApZv31OCPSQiQ0XE2bQm0LO5DIXlE1+b90xUyET5jBYswdY8ZYYr1r3+gRrUrcz2uEYNG/TWR70dOx8VjCqFAoGAVkw9VpDOVWyCK7f7o+OH3bhM4+74Skv8wrkC3C3hGQsCi/0+X+p0Lv55k+UQVHD8RAQEARakFpAvg+V2ZscJwaKyHYMe9jrfQXSd5n+o+FsfNGPzHwH0LgNyNYu7u560fpvGRmGQvVfbBXmNp6VfKh/xYLjxcyQAeAWGB3vyCiE=";

	private PrivateKey privateKey;
	private PublicKey publicKey;

	public RSAUtil() throws NoSuchAlgorithmException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048);
		KeyPair pair = keyGen.generateKeyPair();
		this.publicKey = pair.getPublic();
		this.privateKey = pair.getPrivate();
	}

	public static PublicKey getPublicKey(String base64PublicKey) {
		PublicKey publicKey = null;
		try {
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(base64PublicKey.getBytes()));
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			publicKey = keyFactory.generatePublic(keySpec);
			return publicKey;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return publicKey;
	}

	public static PrivateKey getPrivateKey(String base64PrivateKey) {
		PrivateKey privateKey = null;
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64PrivateKey.getBytes()));
		KeyFactory keyFactory = null;
		try {
			keyFactory = KeyFactory.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		try {
			privateKey = keyFactory.generatePrivate(keySpec);
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return privateKey;
	}

	public static byte[] encrypt(String data, String publicKey) throws BadPaddingException, IllegalBlockSizeException,
			InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(publicKey));
		return cipher.doFinal(data.getBytes());
	}

	public static String decrypt(byte[] data, PrivateKey privateKey) throws NoSuchPaddingException,
			NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		return new String(cipher.doFinal(data));
	}

	public static String decrypt(String data, String base64PrivateKey) throws IllegalBlockSizeException,
			InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
		return decrypt(Base64.getDecoder().decode(data.getBytes()), getPrivateKey(base64PrivateKey));
	}

	public static String decrypt(String data) throws IllegalBlockSizeException,
			InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
		return decrypt(data, pvtKey);
	}

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException {
		test();
	}

	public static void test()
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException {
		try {

			String str = "{\r\n" + "    \"domainType\": \"None\",\r\n" + "    \"id\": 5465,\r\n"
					+ "    \"name\": \"string\",\r\n" + "    \"placeholder\": \"string\",\r\n"
					+ "    \"selectedValue\": \"string\",\r\n" + "    \"type\": \"string\",\r\n"
					+ "    \"fef\":\"frfrgg\",\r\n" + "        \"string\"\r\n" + "    ]\r\n" + "}";
			String encryptedString = Base64.getEncoder().encodeToString(encrypt(str, pubKey));
			System.out.println(encryptedString);
			// String encodedKey = "i7vYEUoWlA97gWF9nq7HM
			// Bd37Ti7DlzG8iY8Pr3Cq1ASc4g7NXiLhLQoBnlJFkVqUFAp6szk6zjL6u0Cq/cZ8wmv0mH9tq8E3AjVix9ggSDhH/mhYB2/eNI1cC86f7bUzc5R5qItiSwZsPjtY1obb82HeAbqLgpBXW2rXkxSP4:";
			String decryptedString = RSAUtil.decrypt(encryptedString);
			System.out.println("decrypted string:" +decryptedString);
		} catch (NoSuchAlgorithmException e) {
			System.err.println(e.getMessage());
		}

	}
}
