package org.openmrs.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;

public class Security {

	public static Log log = LogFactory.getLog("org.openmrs.util.Security");
	
    /**
     * @param string to encode
     * @return the SHA-1 encryption of a given string
     */
    public static String encodeString(String strToEncode) throws APIException {
    	MessageDigest md;
    	try {
    		md = MessageDigest.getInstance("SHA1");
    	}
    	catch (NoSuchAlgorithmException e) {
			// Yikes! Can't encode password...what to do?
    		log.error(e);
			throw new APIException("System cannot find password encryption algorithm");
    	}
		byte[] input = strToEncode.getBytes(); //TODO: pick a specific character encoding, don't rely on the platform default
		return hexString(md.digest(input));
    }
	
    /**
     * @param Byte array to convert to HexString
     * @return Hexidecimal based string
     */
    
	private static String hexString(byte[] b) {
		if (b == null || b.length < 1)
			return "";
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			s.append(Integer.toHexString(b[i] & 0xFF));
		}
		return new String(s);
	}

    /**
     * Returns a secure random token.
     */
    public static String getRandomToken() throws APIException {
    	Random rng = new Random();
    	return encodeString(Long.toString(System.currentTimeMillis()) 
    			+ Long.toString(rng.nextLong()));
    }
	
}