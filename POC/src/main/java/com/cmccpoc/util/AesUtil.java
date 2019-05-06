package com.cmccpoc.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class AesUtil {

	/** 
	 * 鍔犲瘑 
	 *  
	 * @param content 闇�瑕佸姞瀵嗙殑鍐呭 
	 * @param password  鍔犲瘑瀵嗙爜 
	 * @return 
	 */  
	public static byte[] encrypt(String content, String password) {
	        try {             
//	                KeyGenerator kgen = KeyGenerator.getInstance("AES");  
//	                kgen.init(128).init(128, new SecureRandom(password.getBytes()));  
//	                SecretKey secretKey = kgen.generateKey();  
//	                byte[] enCodeFormat = secretKey.getEncoded();  
	                SecretKeySpec key = new SecretKeySpec(password.getBytes(), "AES");  
	                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
	                byte[] byteContent = content.getBytes();  
	                cipher.init(Cipher.ENCRYPT_MODE, key);
	                byte[] result = cipher.doFinal(byteContent);  
	                return result; // 鍔犲瘑  
	        } catch (NoSuchAlgorithmException e) {  
	                e.printStackTrace();  
	        } catch (NoSuchPaddingException e) {  
	                e.printStackTrace();  
	        } catch (InvalidKeyException e) {  
	                e.printStackTrace();  
//	        } catch (UnsupportedEncodingException e) {  
//	                e.printStackTrace();  
	        } catch (IllegalBlockSizeException e) {  
	                e.printStackTrace();  
	        } catch (BadPaddingException e) {  
	                e.printStackTrace();  
	        }  
	        return null;  
	}

	/**瑙ｅ瘑 
	 * @param content  寰呰В瀵嗗唴瀹� 
	 * @param password 瑙ｅ瘑瀵嗛挜 
	 * @return 
	 */  
	public static byte[] decrypt(byte[] content, String password) {
	        try {  
	                 //KeyGenerator kgen = KeyGenerator.getInstance("AES");  
	                 //kgen.init(128, new SecureRandom(password.getBytes()));  
	                 //SecretKey secretKey = kgen.generateKey();  
	                 //byte[] enCodeFormat = secretKey.getEncoded();  
	                 SecretKeySpec key = new SecretKeySpec(password.getBytes(), "AES");              
	                 Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
	                cipher.init(Cipher.DECRYPT_MODE, key);
	                byte[] result = cipher.doFinal(content);  
	                return result; // 鍔犲瘑  
	        } catch (NoSuchAlgorithmException e) {
	                e.printStackTrace();  
	        } catch (NoSuchPaddingException e) {  
	                e.printStackTrace();  
	        } catch (InvalidKeyException e) {  
	                e.printStackTrace();  
	        } catch (IllegalBlockSizeException e) {  
	                e.printStackTrace();  
	        } catch (BadPaddingException e) {  
	                e.printStackTrace();  
	        }  
	        return null;  
	}  
}
