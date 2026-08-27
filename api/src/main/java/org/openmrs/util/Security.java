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
import java.util.Properties;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.spring.LegacyOpenmrsPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

/**
 * OpenMRS's security class deals with the hashing of passwords.
 */
public class Security {

	/**
	 * encryption settings
	 */
	private static final Logger log = LoggerFactory.getLogger(Security.class);

	private static final Random RANDOM = new SecureRandom();

	private static final String SHA512 = "SHA-512";

	private static final String SHA1 = "SHA-1";

	/**
	 * Names of the Argon2 work-factor runtime properties (set in
	 * openmrs-runtime.properties, not in the database).
	 */
	private static final String ARGON2_MEMORY_PROPERTY = "security.argon2.memory";

	private static final String ARGON2_ITERATIONS_PROPERTY = "security.argon2.iterations";

	private static final String ARGON2_PARALLELISM_PROPERTY = "security.argon2.parallelism";

	private static final String ARGON2_SALT_LENGTH_PROPERTY = "security.argon2.saltLength";

	private static final String ARGON2_HASH_LENGTH_PROPERTY = "security.argon2.hashLength";

	private static final int MAX_MEMORY_KB = 1048576;

	private static final int MAX_ITERATIONS = 10;

	private static final int MAX_PARALLELISM = 8;

	private static final int MAX_SALT_LENGTH = 32;

	// Cached Argon2 configuration values; a null field means "not yet loaded". The
	// values are read once from the runtime properties (which are fixed for the
	// lifetime of a running server) into this cache, so the fallback encoder built
	// here always agrees with the Spring argon2PasswordEncoder bean created by
	// createArgon2PasswordEncoder().
	private static volatile Integer cachedSaltLength;
	private static volatile Integer cachedHashLength;
	private static volatile Integer cachedParallelism;
	private static volatile Integer cachedMemory;
	private static volatile Integer cachedIterations;

	// required so we can hash passwords at startup.
	private static final PasswordEncoder FALLBACK_ENCODER = new LegacyOpenmrsPasswordEncoder();
	
	/**
	 * Private constructor: this class offers a static API only. The Spring
	 * {@code argon2PasswordEncoder} bean is created via
	 * {@link #createArgon2PasswordEncoder()}.
	 */
	private Security() {
	}

	static PasswordEncoder getPasswordEncoder() {
		if (!ServiceContext.isInstantiated()
				|| ServiceContext.getInstance().getApplicationContext() == null) {
			return FALLBACK_ENCODER;
		}
		return Context.getRegisteredComponent("openmrsPasswordEncoder", PasswordEncoder.class);
	}

	/**
	 * Encodes a password using the configured {@code openmrsPasswordEncoder} and returns the
	 * full encoded value to persist.
	 *
	 * @param strToEncode {@code password + salt} to encode
	 * @return the encoded value to store
	 * @since 2.8.10
	 */
	public static String encodePassword(String strToEncode) {
		return getPasswordEncoder().encode(strToEncode);
	}

	/**
	 * Checks a raw password against a stored encoded password using the configured
	 * {@code PasswordEncoder}.
	 *
	 * @param storedEncodedPassword the stored encoded password
	 * @param rawPassword the raw password, with the salt already concatenated
	 * @return true if the password matches
	 * @since 2.8.10
	 */
	public static boolean checkPassword(String storedEncodedPassword, String rawPassword) {
		if (rawPassword == null || storedEncodedPassword == null) {
			return false;
		}
		return getPasswordEncoder().matches(rawPassword, storedEncodedPassword);
	}

	/**
	 * Loads the Argon2 work factors from the runtime properties into the static cache.
	 * The values are read once, because runtime properties are fixed for the lifetime of a
	 * running server and do not change on a running system (unlike global properties).
	 */
	private static void loadArgon2ConfigIfNecessary() {
		if (cachedSaltLength == null || cachedHashLength == null || cachedParallelism == null
			|| cachedMemory == null || cachedIterations == null) {
			synchronized (Security.class) {
				if (cachedSaltLength == null || cachedHashLength == null || cachedParallelism == null
					|| cachedMemory == null || cachedIterations == null) {
					Properties runtime = Context.getRuntimeProperties();
					cachedSaltLength = getIntProperty(runtime, ARGON2_SALT_LENGTH_PROPERTY, 16, 8, MAX_SALT_LENGTH);
					cachedHashLength = getIntProperty(runtime, ARGON2_HASH_LENGTH_PROPERTY, 32, 4, Integer.MAX_VALUE);
					// Parallelism is read first so that the memory floor below can honour
					// the encoder's requirement that memory >= 8 * parallelism.
					cachedParallelism = getIntProperty(runtime, ARGON2_PARALLELISM_PROPERTY, 1, 1, MAX_PARALLELISM);
					int minimumMemory = 8 * cachedParallelism;
					cachedMemory = getIntProperty(runtime, ARGON2_MEMORY_PROPERTY, 65536, minimumMemory, MAX_MEMORY_KB);
					cachedIterations = getIntProperty(runtime, ARGON2_ITERATIONS_PROPERTY, 3, 1, MAX_ITERATIONS);
				}
			}
		}
	}

	/**
	 * Reads an integer-valued runtime property, clamping it to the given bounds and
	 * falling back to the default when the value is missing or not a valid integer.
	 */
	private static int getIntProperty(Properties runtime, String name, int defaultValue, int min, int max) {
		String raw = runtime.getProperty(name);
		if (raw == null) {
			return defaultValue;
		}
		try {
			int value = Integer.parseInt(raw.trim());
			if (value < min) {
				log.warn("Runtime property '{}' = {} is below the minimum of {}, clamping to minimum", name, value, min);
				return min;
			}
			if (value > max) {
				log.warn("Runtime property '{}' = {} exceeds the maximum of {}, clamping to maximum", name, value, max);
				return max;
			}
			return value;
		} catch (NumberFormatException e) {
			log.warn("Invalid integer value for runtime property '{}': '{}', using default: {}", name, raw, defaultValue);
			return defaultValue;
		}
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

		int phcIndex = hashedPassword.indexOf("$argon2id$");
		if (phcIndex >= 0) {
			String phc = phcIndex == 0 ? hashedPassword : hashedPassword.substring(phcIndex);
			return getArgon2Encoder().matches(passwordToHash, phc);
		}

		return hashedPassword.equals(encodeString(passwordToHash, SHA512))
			|| hashedPassword.equals(encodeStringSHA1(passwordToHash))
			|| hashedPassword.equals(incorrectlyEncodeString(passwordToHash));
	}

	/**
	 * This method will hash <code>strToEncode</code> using the preferred algorithm. Currently,
	 * OpenMRS's preferred algorithm is hard-coded to be SHA-512.
	 *
	 * @param strToEncode string to encode
	 * @return the SHA-512 encryption of a given string
	 * <strong>Should</strong> encode strings to 128 characters
	 */
	public static String encodeString(String strToEncode) throws APIException {
		return encodeString(strToEncode, SHA512);
	}

	/**
	 * This method will hash <code>strToEncode</code> using Argon2id for password encoding.
	 * This method should only be used for password storage, not for deterministic hashing
	 * (e.g., activation keys, secret answers).
	 *
	 * @param strToEncode string to encode
	 * @return the Argon2id encryption of a given string
	 * @since 2.8.10
	 */
	public static String encodeStringArgon2(String strToEncode) throws APIException {
		return getArgon2Encoder().encode(strToEncode);
	}

	/**
	 * This method will hash <code>strToEncode</code> using the old SHA-1 algorithm.
	 *
	 * @param strToEncode string to encode
	 * @return the SHA-1 encryption of a given string
	 */
	private static String encodeStringSHA1(String strToEncode) throws APIException {
		return encodeString(strToEncode, SHA1);
	}

	/**
	 * Returns an Argon2PasswordEncoder configured from the cached runtime-property values.
	 * <p>
	 * The Spring {@code argon2PasswordEncoder} bean (see applicationContext-service.xml) is
	 * created through {@link #createArgon2PasswordEncoder()} from the same values, so the
	 * encoder used here always agrees with the bean. Making Argon2 the default for any
	 * password encoder then is purely a matter of Spring configuration.
	 */
	private static Argon2PasswordEncoder getArgon2Encoder() {
		loadArgon2ConfigIfNecessary();
		return new Argon2PasswordEncoder(cachedSaltLength, cachedHashLength, cachedParallelism, cachedMemory, cachedIterations);
	}

	/**
	 * Spring factory method used to create the {@code argon2PasswordEncoder} bean from the
	 * Argon2 work-factor runtime properties (see applicationContext-service.xml). Uses the
	 * same configuration as {@link #encodeStringArgon2(String)}.
	 *
	 * @return an Argon2PasswordEncoder configured from the runtime properties
	 * @since 2.8.10
	 */
	public static Argon2PasswordEncoder createArgon2PasswordEncoder() {
		return getArgon2Encoder();
	}

	/**
	 * Resets the cached Argon2 configuration values.
	 * This is a package-private method intended for testing purposes only.
	 * It forces the configuration to be re-read from the runtime properties
	 * on the next call to {@link #encodeStringArgon2(String)}.
	 *
	 * @since 2.8.10
	 */
	static void resetEncoder() {
		synchronized (Security.class) {
			cachedSaltLength = null;
			cachedHashLength = null;
			cachedParallelism = null;
			cachedMemory = null;
			cachedIterations = null;
		}
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
