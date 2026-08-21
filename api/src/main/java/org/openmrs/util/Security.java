/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * OpenMRS's security class deals with the hashing of passwords.
 */
public class Security {

	/**
	 * encryption settings
	 */
	private static final Logger log = LoggerFactory.getLogger(Security.class);
	
	private static final Random RANDOM = new SecureRandom();

	private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int PBKDF2_DEFAULT_ITERATIONS = 600000; 
    private static final int PBKDF2_KEY_LENGTH = 256;
    private static final int BOOTSTRAP_MAX_PASSWORD_LENGTH = 20;

	private Security() {
	}

	/**
	 * Compare the given hash and the given string-to-hash to see if they are equal. The
	 * string-to-hash is usually of the form password + salt. <br>
	 * <br>
	 * This should be used so that this class can compare against the new correct hashing algorithm
	 * and the old incorrect hashing algorithm.
	 *
	 * @param hashedPassword a stored password that has been hashed previously
	 * @param passwordToHash a string to encode/hash and compare to hashedPassword
	 * @return true/false whether the two are equal
	 * @since 1.5
	 * <strong>Should</strong> match strings hashed with incorrect sha1 algorithm
	 * <strong>Should</strong> match strings hashed with sha1 algorithm
	 * <strong>Should</strong> match strings hashed with sha512 algorithm and 128 characters salt
	 */
	public static boolean hashMatches(String hashedPassword, String passwordToHash) {
		if (hashedPassword == null || passwordToHash == null) {
			throw new APIException("password.cannot.be.null", (Object[]) null);
		}
		
		return hashedPassword.equals(encodeString(passwordToHash))
			|| hashedPassword.equals(encodeStringSHA1(passwordToHash))
			|| hashedPassword.equals(incorrectlyEncodeString(passwordToHash));
	}

	/**
     * Generates a deterministic bootstrap password for a user.
     * 
     * The password is derived from the user's UUID and the system pepper using PBKDF2.
     * The UUID acts as a per-user salt, ensuring uniqueness per user, while the pepper
     * (stored in runtime.properties) provides system-wide secrecy.
     * 
     * The resulting password is Base64-encoded and truncated to a user-friendly length
     * of {@value #BOOTSTRAP_MAX_PASSWORD_LENGTH} characters.
     * 
     * @param user the user for whom to generate the bootstrap password
     * @return the generated bootstrap password (a user-friendly string)
     * @throws APIException if user is null, has no UUID, or the pepper is not configured
     * @since 2.8.9
     */
	public static String generateBootstrapPassword(org.openmrs.User user) {
		if (user == null) {
			throw new APIException("bootstrap.user.null", (Object[]) null);
		}
		
		String uuid = user.getUuid();
		if (!StringUtils.hasText(uuid)) {
			throw new APIException("bootstrap.user.uuid.missing", (Object[]) null);
		}
		
		String pepper = getBootstrapPepper();
		if (!StringUtils.hasText(pepper)) {
			throw new APIException("bootstrap.pepper.missing", (Object[]) null);
		}
		
		String input = uuid + pepper;
		int iterations = getBootstrapIterations();
		
		return generateDeterministicHash(input, uuid, iterations);  
	}

	/**
	 * Generates a deterministic hash using PBKDF2 with the default iteration count.
	 * 
	 * This is a convenience method that calls
	 * {@link #generateDeterministicHash(String, String, int)} with the default
	 * iterations ({@value #PBKDF2_DEFAULT_ITERATIONS}).
	 * 
	 * @param input the input string to derive from (typically user UUID + system pepper)
	 * @param salt  the per-user salt (typically the user's UUID)
	 * @return the derived hash as a Base64 encoded string, truncated to a user-friendly length
	 * @throws APIException if input or salt is null
	 * @since 2.8.9
	 */
	public static String generateDeterministicHash(String input, String salt) {
		return generateDeterministicHash(input, salt, PBKDF2_DEFAULT_ITERATIONS);
	}

		/**
	 * Generates a deterministic hash using PBKDF2.
	 * 
	 * This method is used for bootstrap password generation, where deterministic output is required
	 * for a given user (same user → same password). The UUID serves as a per-user salt, while the
	 * pepper (system secret) is concatenated into the input string. This ensures that even if the
	 * pepper is compromised, each user's password remains unique due to the per-user salt.
	 * 
	 * Unlike {@link #encodeString(String)}, which uses SHA-512, this method uses PBKDF2 with a high
	 * iteration count, making it suitable for password derivation.
	 * 
	 * @param input      the input string to derive from (typically user UUID + system pepper)
	 * @param salt       the per-user salt (typically the user's UUID) to make each derivation unique
	 * @param iterations the number of PBKDF2 iterations (use 0 to apply the default)
	 * @return the derived hash as a Base64 encoded string, truncated to a user-friendly length
	 * @throws APIException if input or salt is null
	 * @since 2.8.9
	 */
	public static String generateDeterministicHash(String input, String salt, int iterations) {
		if (input == null || salt == null) {
			throw new APIException("bootstrap.input.null", (Object[]) null);
		}
		
		int actualIterations = iterations > 0 ? iterations : PBKDF2_DEFAULT_ITERATIONS;
		
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
			KeySpec spec = new PBEKeySpec(
				input.toCharArray(),
				salt.getBytes(StandardCharsets.UTF_8),
				actualIterations,
				PBKDF2_KEY_LENGTH
			);
			byte[] derived = factory.generateSecret(spec).getEncoded();
			
			String base64 = Base64.getEncoder().withoutPadding().encodeToString(derived);
			String cleaned = base64.replaceAll("[0OIl]", "");
			return cleaned.substring(0, Math.min(cleaned.length(), BOOTSTRAP_MAX_PASSWORD_LENGTH));
			
		} catch (GeneralSecurityException e) {
			log.error("Failed to generate deterministic hash", e);
			throw new APIException("bootstrap.hash.generation.failed", null, e);
		}
	}

	    /**
     * Gets the pepper used for bootstrap password generation.
     * 
     *The pepper is read from the runtime property "openmrs.bootstrap.pepper".
     * 
     * @return the pepper, or null if not configured
     * @since 2.8.9
     */
		public static String getBootstrapPepper() {
		return Context.getRuntimeProperties()
			.getProperty("openmrs.bootstrap.pepper");
	}

	/**
     * Gets the configured number of PBKDF2 iterations for bootstrap password generation.
     * 
     * Reads from global property "security.bootstrap.iterations".
     * Falls back to default (600,000) if not configured or invalid.
     * 
     * @return the number of iterations (always > 0)
     * @since 2.8.9
     */
    public static int getBootstrapIterations() {
		String value = Context.getRuntimeProperties().getProperty("openmrs.bootstrap.iterations");
		if (StringUtils.hasText(value)) {
			try {
				int iterations = Integer.parseInt(value.trim());
				if (iterations > 0) {
					return iterations;
				}
				log.warn("Invalid bootstrap iterations value (must be > 0): {}, using default: {}", 
						value, PBKDF2_DEFAULT_ITERATIONS);
			} catch (NumberFormatException e) {
				log.warn("Invalid bootstrap iterations format: {}, using default: {}", 
						value, PBKDF2_DEFAULT_ITERATIONS);
			}
		}
		return PBKDF2_DEFAULT_ITERATIONS;
	}

	/**
	 * Validates a password against a user's bootstrap password.
	 * 
	 * @param user the user
	 * @param password the password to validate
	 * @return true if the password matches the user's bootstrap password and the user has not been forced to change
	 * @since 2.8.9
	 */
	public static boolean validateBootstrapPassword(org.openmrs.User user, String password) {
		if (user == null || password == null) {
			return false;
		}
		
		// If the user has already been forced to change, the bootstrap password is no longer valid
		if (isBootstrapPasswordExpired(user)) {
			return false;
		}
		
		try {
			String expected = generateBootstrapPassword(user);
			return MessageDigest.isEqual(
				expected.getBytes(StandardCharsets.UTF_8),
				password.getBytes(StandardCharsets.UTF_8)
			);
		} catch (APIException e) {
			log.debug("Failed to validate bootstrap password: {}", e.getMessage());
			return false;
		}
	}

	/**
     * Checks if a user's bootstrap password has expired.
     * 
     * Bootstrap passwords are expired if the user has the "forcePassword" user property set to "true".
     * 
     * @param user the user
     * @return true if the bootstrap password is expired
     * @since 2.8.9
     */
    public static boolean isBootstrapPasswordExpired(org.openmrs.User user) {
        if (user == null) {
            return false;
        }
        String forcePassword = user.getUserProperty(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD);
        return "true".equals(forcePassword);
    }

	/**
     * Forces a user to change their password on next login.
     * 
     * Sets the "forcePassword" user property to "true".
     * The user will be redirected to change password on next login.
     * 
     * @param user the user whose password change should be forced
     * @since 2.8.9
     */
    public static void forcePasswordChange(org.openmrs.User user) {
        if (user != null) {
            user.setUserProperty(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD, "true");
        }
    }

	/**
	 /**
	 * This method will hash <code>strToEncode</code> using the preferred algorithm. Currently,
	 * OpenMRS's preferred algorithm is hard coded to be SHA-512.
	 *
	 * @param strToEncode string to encode
	 * @return the SHA-512 encryption of a given string
	 * <strong>Should</strong> encode strings to 128 characters
	 */
	public static String encodeString(String strToEncode) throws APIException {
		return encodeString(strToEncode, "SHA-512");
	}

	/**
	 * This method will hash <code>strToEncode</code> using the old SHA-1 algorithm.
	 *
	 * @param strToEncode string to encode
	 * @return the SHA-1 encryption of a given string
	 */
	private static String encodeStringSHA1(String strToEncode) throws APIException {
		return encodeString(strToEncode, "SHA-1");
	}

	private static String encodeString(String strToEncode, String algorithm) {
		return hexString(digest(strToEncode.getBytes(StandardCharsets.UTF_8), algorithm));
	}

	private static byte[] digest(byte[] input, String algorithm) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance(algorithm);
		}
		catch (NoSuchAlgorithmException e) {
			// Yikes! Can't encode password...what to do?
			log.error("Can't encode password because the given algorithm: " + algorithm + " was not found! (fail)", e);
			throw new APIException("system.cannot.find.encryption.algorithm", null, e);
		}

		return md.digest(input);
	}

	/**
	 * Convenience method to convert a byte array to a string
	 *
	 * @param block Byte array to convert to HexString
	 * @return Hexadecimal string encoding the byte array
	 */
	private static String hexString(byte[] block) {
		StringBuilder buf = new StringBuilder();
		char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		int high;
		int low;
		for (byte aBlock : block) {
			high = ((aBlock & 0xf0) >> 4);
			low = (aBlock & 0x0f);
			buf.append(hexChars[high]);
			buf.append(hexChars[low]);
		}

		return buf.toString();
	}

	/**
	 * This method will hash <code>strToEncode</code> using SHA-1 and the incorrect hashing method
	 * that sometimes dropped out leading zeros.
	 *
	 * @param strToEncode string to encode
	 * @return the SHA-1 encryption of a given string
	 */
	private static String incorrectlyEncodeString(String strToEncode) throws APIException {
		return incorrectHexString(digest(strToEncode.getBytes(StandardCharsets.UTF_8), "SHA-1"));
	}

	/**
	 * This method used to be the simple hexString method, however, as pointed out in ticket
	 * http://dev.openmrs.org/ticket/1178, it was not working correctly. Authenticated still needs
	 * to occur against both this method and the correct hex string, so this wrong implementation
	 * will remain until we either force users to change their passwords, or we just decide to
	 * invalidate them.
	 *
	 * @param b the byte array to encode
	 * @return the old possibly less than 40 characters hashed string
	 */
	private static String incorrectHexString(byte[] b) {
		if (b == null || b.length < 1) {
			return "";
		}
		StringBuilder s = new StringBuilder();
		for (byte aB : b) {
			s.append(Integer.toHexString(aB & 0xFF));
		}
		return new String(s);
	}

	/**
	 * This method will generate a random string
	 *
	 * @return a secure random token.
	 */
	public static String getRandomToken() throws APIException {
		byte[] token = new byte[64];
		RANDOM.nextBytes(token);
		return hexString(digest(token, "SHA-512"));
	}

	/**
	 * encrypt text to a string with specific initVector and secretKey; rarely used except in
	 * testing and where specifically necessary
	 *
	 * @see #encrypt(String)
	 *
	 * @param text string to be encrypted
	 * @param initVector custom init vector byte array
	 * @param secretKey custom secret key byte array
	 * @return encrypted text
	 * @since 1.9
	 */
	public static String encrypt(String text, byte[] initVector, byte[] secretKey) {
		IvParameterSpec initVectorSpec = new IvParameterSpec(initVector);
		SecretKeySpec secret = new SecretKeySpec(secretKey, OpenmrsConstants.ENCRYPTION_KEY_SPEC);
		byte[] encrypted;
		String result;

		try {
			Cipher cipher = Cipher.getInstance(OpenmrsConstants.ENCRYPTION_CIPHER_CONFIGURATION);
			cipher.init(Cipher.ENCRYPT_MODE, secret, initVectorSpec);
			encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
			result = new String(Base64.getEncoder().encode(encrypted), StandardCharsets.UTF_8);
		}
		catch (GeneralSecurityException e) {
			throw new APIException("could.not.encrypt.text", null, e);
		}

		return result;
	}

	/**
	 * encrypt text using stored initVector and securityKey
	 *
	 * @param text the text to encrypt
	 * @return encrypted text
	 * @since 1.9
	 * <strong>Should</strong> encrypt short and long text
	 *
	 * @deprecated As of version 2.4.0, this method is not referenced in openmrs-core or any other projects under the
	 * GitHub OpenMRS organisation.
	 */
	@Deprecated
	public static String encrypt(String text) {
		return Security.encrypt(text, Security.getSavedInitVector(), Security.getSavedSecretKey());
	}

	/**
	 * decrypt text to a string with specific initVector and secretKey; rarely used except in
	 * testing and where specifically necessary
	 *
	 * @see #decrypt(String)
	 *
	 * @param text text to be decrypted
	 * @param initVector custom init vector byte array
	 * @param secretKey custom secret key byte array
	 * @return decrypted text
	 * @since 1.9
	 */
	public static String decrypt(String text, byte[] initVector, byte[] secretKey) {
		IvParameterSpec initVectorSpec = new IvParameterSpec(initVector);
		SecretKeySpec secret = new SecretKeySpec(secretKey, OpenmrsConstants.ENCRYPTION_KEY_SPEC);
		String decrypted;

		try {
			Cipher cipher = Cipher.getInstance(OpenmrsConstants.ENCRYPTION_CIPHER_CONFIGURATION);
			cipher.init(Cipher.DECRYPT_MODE, secret, initVectorSpec);
			byte[] original = cipher.doFinal(Base64.getDecoder().decode(text));
			decrypted = new String(original, StandardCharsets.UTF_8);
		}
		catch (GeneralSecurityException e) {
			throw new APIException("could.not.decrypt.text", null, e);
		}

		return decrypted;
	}

	/**
	 * decrypt text using stored initVector and securityKey
	 *
	 * @param text text to be decrypted
	 * @return decrypted text
	 * @since 1.9
	 * <strong>Should</strong> decrypt short and long text
	 *
	 * @deprecated As of version 2.4.0, this method is not referenced in openmrs-core or any other projects under the
	 * GitHub OpenMRS organisation.
	 */
	@Deprecated
	public static String decrypt(String text) {
		return Security.decrypt(text, Security.getSavedInitVector(), Security.getSavedSecretKey());
	}

	/**
	 * retrieve the stored init vector from runtime properties
	 *
	 * @return stored init vector byte array
	 * @since 1.9
	 */
	public static byte[] getSavedInitVector() {
		String initVectorText = Context.getRuntimeProperties().getProperty(
			OpenmrsConstants.ENCRYPTION_VECTOR_RUNTIME_PROPERTY, OpenmrsConstants.ENCRYPTION_VECTOR_DEFAULT);

		if (StringUtils.hasText(initVectorText)) {
			return Base64.getDecoder().decode(initVectorText);
		}

		throw new APIException("no.encryption.initialization.vector.found", (Object[]) null);
	}

	/**
	 * generate a new cipher initialization vector; should only be called once in order to not
	 * invalidate all encrypted data
	 *
	 * @return a random array of 16 bytes
	 * @since 1.9
	 */
	public static byte[] generateNewInitVector() {
		// initialize the init vector with 16 random bytes
		byte[] initVector = new byte[16];
		RANDOM.nextBytes(initVector);

		return initVector;
	}

	/**
	 * retrieve the secret key from runtime properties
	 *
	 * @return stored secret key byte array
	 * @since 1.9
	 */
	public static byte[] getSavedSecretKey() {
		String keyText = Context.getRuntimeProperties().getProperty(OpenmrsConstants.ENCRYPTION_KEY_RUNTIME_PROPERTY,
			OpenmrsConstants.ENCRYPTION_KEY_DEFAULT);

		if (StringUtils.hasText(keyText)) {
			return Base64.getDecoder().decode(keyText);
		}

		throw new APIException("no.encryption.secret.key.found", (Object[]) null);
	}

	/**
	 * generate a new secret key; should only be called once in order to not invalidate all
	 * encrypted data
	 *
	 * @return generated secret key byte array
	 * @since 1.9
	 */
	public static byte[] generateNewSecretKey() {
		// Get the KeyGenerator
		KeyGenerator kgen;
		try {
			kgen = KeyGenerator.getInstance(OpenmrsConstants.ENCRYPTION_KEY_SPEC);
		}
		catch (NoSuchAlgorithmException e) {
			throw new APIException("could.not.generate.cipher.key", null, e);
		}
		kgen.init(128); // 192 and 256 bits may not be available

		// Generate the secret key specs.
		SecretKey skey = kgen.generateKey();

		return skey.getEncoded();
	}

}
