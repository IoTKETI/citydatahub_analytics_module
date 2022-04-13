package com.vaiv.analyticsManager.common.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;


@SuppressWarnings("restriction")
public class EncryptionUtil {

	private static final Logger logger = LoggerFactory.getLogger(EncryptionUtil.class);
	private static String key = "@@OBIDICS-2017@@";
	
	/**
	 * hex to byte[] : 16진수 문자열을 바이트 배열로 변환한다.
	 * @param hex
	 * @return
	 * @throws Exception
	 */
	public static byte[] hexToByteArray(String hex) throws Exception {
		
		if (hex == null || hex.length() == 0) {
			return null;
		}
		
		byte[] ba = new byte[hex.length() / 2];
		
		for (int i = 0; i < ba.length; i++) {
			ba[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		}
		
		return ba;
	}
	
	/**
	 * byte[] to hex : unsigned byte(바이트) 배열을 16진수 문자열로 바꾼다.
	 * @param ba
	 * @return
	 * @throws Exception
	 */
	public static String byteArrayToHex(byte[] ba) throws Exception {
		
		if (ba == null || ba.length == 0) {
			return null;
		}
		
		StringBuffer sb = new StringBuffer(ba.length * 2);
		String hexNumber;
		
		for (int x = 0; x < ba.length; x++) {
			hexNumber = "0" + Integer.toHexString(0xff & ba[x]);
			sb.append(hexNumber.substring(hexNumber.length() - 2));
		}
		
		return sb.toString();
	}
	
	/**
	 * AES 방식의 암호화
	 * @param message
	 * @return
	 * @throws Exception
	 */
	public static String encrypt(String message) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
		
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		
		byte[] encrypted = cipher.doFinal(message.getBytes());
		
		return byteArrayToHex(encrypted);
	}
	
	/**
	 * AES 방식의 복호화
	 * @param encrypted
	 * @return
	 * @throws Exception
	 */
	public static String decrypt(String encrypted) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
		
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		
		byte[] original = cipher.doFinal(hexToByteArray(encrypted));
		
		String originalString = new String(original);
		
		return originalString;
	}
	
	/**
	 * Base64 형식으로 인코딩
	 * @param userData
	 * @return
	 * @throws Exception
	 */
	public static String encodeBase64(String userData) throws Exception {
		return Base64.encode(userData.getBytes());
	}
	
	/**
	 * Base64 형식으로 된 문자열 디코딩
	 * @param encoded
	 * @return
	 * @throws Exception
	 */
	public static String decodeBase64(String encoded) throws Exception {
		return new String(Base64.decode(encoded));
	}
	
	/**
	 * String을 MD5로 변환하는 함수
	 * @param str
	 * @return
	 * @throws Exception
	 */
	public static String encryptMD5(String str) throws Exception {

		StringBuffer sb = new StringBuffer();
		String apiKey = "";
		
        try{
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            
            byte[] bytData = str.getBytes();	
            md.update(bytData);
            byte[] digest = md.digest();
            sb.setLength(0);
            for( int i = 0; i < digest.length; i++ ) {
                sb.append(Integer.toString((digest[i] & 0xf0) >> 4, 16)); 
                sb.append(Integer.toString(digest[i] & 0x0f, 16));
            }
            apiKey = sb.toString();
        }catch(Exception e){
            //e.printStackTrace();
        	logger.info("== EncryptMD5 ERROR ==");
            sb = null;
            apiKey = "";
        }
        sb = null;

		return apiKey;
 	}
	
	/**
	 * Seed 값을 가지고 MD5로 변환하는 함수
	 * Seed 값이 없거나 null일 경우 random 숫자를 호출한다.
	 * 
	 * @param Seed
	 * @param str
	 * @return
	 * @throws Exception
	 */
	public static String encryptMD5(String Seed,String str) throws Exception {

		StringBuffer sb = new StringBuffer();
		String apiKey = "";
		
        try{
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            
            if( Seed==null || "".equals(Seed) ){
            	Seed = Integer.toString((int)(Math.random()*(100000*10)));
            }
            str=Seed+str;
            
            byte[] bytData = str.getBytes();	
            md.update(bytData);
            byte[] digest = md.digest();
            sb.setLength(0);
            for( int i = 0; i < digest.length; i++ ) { 
                sb.append(Integer.toString((digest[i] & 0xf0) >> 4, 16)); 
                sb.append(Integer.toString(digest[i] & 0x0f, 16));
            }
            apiKey = sb.toString();
        }catch(Exception e){
            //e.printStackTrace();
        	logger.info("== EncryptMD5 ERROR ==");
            sb = null;
            apiKey = "";
        }
        sb = null;

		return apiKey;
 	}
	
	/**
	 * SHA-256 암호화
	 * @param str
	 * @return
	 */
	public static String encryptSHA256(String str){
		String SHA = ""; 
		
		try{
			MessageDigest sh = MessageDigest.getInstance("SHA-256"); 

			sh.update(str.getBytes()); 
			byte byteData[] = sh.digest();
			StringBuffer sb = new StringBuffer(); 

			for(int i = 0 ; i < byteData.length ; i++){
				sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
			}
			SHA = sb.toString();

		}catch(NoSuchAlgorithmException e){
			logger.info("== EncryptSHA256 ERROR ==");
			//e.printStackTrace(); 
			SHA = null; 
		}
		return SHA;
	}
	
	/**
	 * SHA-256으로 암호화된 sessionid 반환
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unused")
	public static String sha256Encoder(HttpServletRequest request) {
		logger.info("--- sha256Encoder ---");
		HttpSession session = request.getSession();
		String sessionId = session.getId();
		StringBuffer state = null;
		String encodingType = "SHA-256";

		try {
			MessageDigest digest = MessageDigest.getInstance(encodingType);
			byte[] hash = digest.digest(sessionId.getBytes("UTF-8"));
			state = new StringBuffer();
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1)state.append('0');
				state.append(hex);
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
		}

		return state.toString();
	}
	
	
	
}
