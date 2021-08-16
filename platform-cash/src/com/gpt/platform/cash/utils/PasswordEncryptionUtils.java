package com.gpt.platform.cash.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.DigestException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.hazelcast.util.Base64;

/**
 * @deprecated just inject {@link com.gpt.component.password.services.IPasswordUtils}
 *
 */
@Deprecated
public class PasswordEncryptionUtils {
	private static final String ALGORITMO = "AES/ECB/PKCS5Padding";
	
	private static final String ALGORITMO_CBC = "AES/CBC/PKCS5Padding";
	
	private static SecretKeySpec skeySpec;
	
	private static PasswordEncryptionUtils encryptionUtils = new PasswordEncryptionUtils();
	
	private PasswordEncryptionUtils() {}
	
	public static PasswordEncryptionUtils getInstance() {
        return encryptionUtils;
    }

	public byte[] encrypt(byte[] rawdata, byte[] key)throws NoSuchAlgorithmException, NoSuchPaddingException,InvalidKeyException, IllegalBlockSizeException,BadPaddingException, IOException{
		byte[] raw = Arrays.copyOf(key, 16);
		skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance(ALGORITMO);
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		
		return Base64.encode(cipher.doFinal(rawdata));
	}

	public byte[] decrypt(byte[] rawdata, byte[] key)throws InvalidKeyException, IllegalBlockSizeException,BadPaddingException, UnsupportedEncodingException,NoSuchAlgorithmException, NoSuchPaddingException,InvalidAlgorithmParameterException{
		byte[] raw = Arrays.copyOf(key, 16);
		skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance(ALGORITMO);
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		
		return cipher.doFinal(Base64.decode(rawdata));
	}
	
	public static void main(String[] args) throws Exception {
//		String encrypt = new String(PasswordEncryptionUtils.getInstance().encrypt("12345".getBytes(), "EsoVlUbqSwUNrh9&".getBytes()));
//		System.out.println(encrypt);
//		System.out.println(new String(PasswordEncryptionUtils.getInstance().decrypt(encrypt.getBytes(), "EsoVlUbqSwUNrh9&".getBytes())));
		
//		String secret = "Renber";
//		String cipherText = "U2FsdGVkX1+tsmZvCEFa/iGeSA0K7gvgs9KXeZKwbCDNCs2zPo+BXjvKYLrJutMK+hxTwl/hyaQLOaD7LLIRo2I5fyeRMPnroo6k8N9uwKk=";
//
//		byte[] cipherData = org.apache.commons.codec.binary.Base64.decodeBase64(cipherText.getBytes());
//		byte[] saltData = Arrays.copyOfRange(cipherData, 8, 16);
//
//		MessageDigest md5 = MessageDigest.getInstance("MD5");
//		final byte[][] keyAndIV = GenerateKeyAndIV(32, 16, 1, saltData, secret.getBytes(StandardCharsets.UTF_8), md5);
//		SecretKeySpec key = new SecretKeySpec(keyAndIV[0], "AES");
//		IvParameterSpec iv = new IvParameterSpec(keyAndIV[1]);
//
//		byte[] encrypted = Arrays.copyOfRange(cipherData, 16, cipherData.length);
//		Cipher aesCBC = Cipher.getInstance("AES/CBC/PKCS5Padding");
//		aesCBC.init(Cipher.DECRYPT_MODE, key, iv);
//		byte[] decryptedData = aesCBC.doFinal(encrypted);
//		String decryptedText = new String(decryptedData, StandardCharsets.UTF_8);
//
//		System.out.println(decryptedText);
		
		System.out.println(new String(PasswordEncryptionUtils.getInstance().decryptCBC("U2FsdGVkX1+S7GyLoNBQ6fiEVtXdspk3rFSe6oKvlxo=".getBytes(), "MxSjQPwRtivikC5#".getBytes())) );
	}
	
	public byte[] decryptCBC(byte[] rawdata, byte[] secretKey)throws InvalidKeyException, IllegalBlockSizeException,BadPaddingException, UnsupportedEncodingException,NoSuchAlgorithmException, NoSuchPaddingException,InvalidAlgorithmParameterException{
		byte[] cipherData= Base64.decode(rawdata);
		byte[] saltData = Arrays.copyOfRange(cipherData, 8, 16);

		MessageDigest md5 = MessageDigest.getInstance("MD5");
		final byte[][] keyAndIV = GenerateKeyAndIV(32, 16, 1, saltData, secretKey, md5);
		SecretKeySpec key = new SecretKeySpec(keyAndIV[0], "AES");
		IvParameterSpec iv = new IvParameterSpec(keyAndIV[1]);

		byte[] encrypted = Arrays.copyOfRange(cipherData, 16, cipherData.length);
		Cipher aesCBC = Cipher.getInstance(ALGORITMO_CBC);
		aesCBC.init(Cipher.DECRYPT_MODE, key, iv);
		byte[] decryptedData = aesCBC.doFinal(encrypted);
		String decryptedText = new String(decryptedData, StandardCharsets.UTF_8);
		
		return decryptedText.getBytes();
	}
	
	public static byte[][] GenerateKeyAndIV(int keyLength, int ivLength, int iterations, byte[] salt, byte[] password, MessageDigest md) {

	    int digestLength = md.getDigestLength();
	    int requiredLength = (keyLength + ivLength + digestLength - 1) / digestLength * digestLength;
	    byte[] generatedData = new byte[requiredLength];
	    int generatedLength = 0;

	    try {
	        md.reset();

	        // Repeat process until sufficient data has been generated
	        while (generatedLength < keyLength + ivLength) {

	            // Digest data (last digest if available, password data, salt if available)
	            if (generatedLength > 0)
	                md.update(generatedData, generatedLength - digestLength, digestLength);
	            md.update(password);
	            if (salt != null)
	                md.update(salt, 0, 8);
	            md.digest(generatedData, generatedLength, digestLength);

	            // additional rounds
	            for (int i = 1; i < iterations; i++) {
	                md.update(generatedData, generatedLength, digestLength);
	                md.digest(generatedData, generatedLength, digestLength);
	            }

	            generatedLength += digestLength;
	        }

	        // Copy key and IV into separate byte arrays
	        byte[][] result = new byte[2][];
	        result[0] = Arrays.copyOfRange(generatedData, 0, keyLength);
	        if (ivLength > 0)
	            result[1] = Arrays.copyOfRange(generatedData, keyLength, keyLength + ivLength);

	        return result;

	    } catch (DigestException e) {
	        throw new RuntimeException(e);

	    } finally {
	        // Clean out temporary data
	        Arrays.fill(generatedData, (byte)0);
	    }
	}
}
