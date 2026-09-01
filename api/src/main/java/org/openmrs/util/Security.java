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
import java.util.HashMap;
import java.util.Map;
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

	// Names of the Argon2 work-factor runtime properties (set in
	// openmrs-runtime.properties, not in the database). The same names are used by the
	// applicationContext-service.xml placeholder placeholders for the argon2PasswordEncoder
	// bean, so the bean and Security.encodeStringArgon2 always use the same values.
	private static final String ARGON2_MEMORY_PROPERTY = "security.argon2.memory";

	private static final String ARGON2_ITERATIONS_PROPERTY = "security.argon2.iterations";

	private static final String ARGON2_PARALLELISM_PROPERTY = "security.argon2.parallelism";

	private static final String ARGON2_SALT_LENGTH_PROPERTY = "security.argon2.saltLength";

	private static final String ARGON2_HASH_LENGTH_PROPERTY = "security.argon2.hashLength";

	// Safe bounds for the Argon2id work factors. The defaults follow the OWASP recommendation
	// for Argon2id (m=19456 KB, t=2, p=1) and can be overridden per-installation.
	private static final int DEFAULT_MEMORY_KB = 19456;

	private static final int MAX_MEMORY_KB = 1048576;

	private static final int DEFAULT_ITERATIONS = 2;

	private static final int MAX_ITERATIONS = 10;

	private static final int DEFAULT_PARALLELISM = 1;

	private static final int MAX_PARALLELISM = 8;

	private static final int DEFAULT_SALT_LENGTH = 16;

	private static final int MAX_SALT_LENGTH = 32;

	private static final int DEFAULT_HASH_LENGTH = 32;

	private static final int MIN_HASH_LENGTH = 4;

	// map to LoginCredential.hbm.xml's users.password column (varchar(128)); an Argon2 PHC
	// string must never exceed it, or the value gets truncated and can never verify again.
	private static final int MAX_PASSWORD_COLUMN_LENGTH = 128;

	// Cached Argon2id encoder; built lazily once from the runtime properties (which are fixed
	// for the lifetime of a running server) and reused so the encoder is not rebuilt per call.
	private static volatile Argon2PasswordEncoder argon2Encoder;

	// required so we can hash passwords at startup.
	private static final PasswordEncoder FALLBACK_ENCODER = new LegacyOpenmrsPasswordEncoder();
	
	/**
	 * Private constructor: this class offers a static API only. The Spring
	 * {@code openmrsPasswordEncoder} bean is created in applicationContext-service.xml.
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
	 * Resolves the Argon2 work factors from the given runtime properties, clamping each value
	 * to a safe range. Runtime properties are fixed for the lifetime of a running server, so
	 * the resolved values are read once and cached rather than re-read per call.
	 *
	 * @param runtime the runtime properties to read from
	 * @return the resolved and clamped work factors
	 */
	static Argon2Config resolveArgon2Config(Properties runtime) {
		// Parallelism is resolved first so that the memory floor below can honour the
		// encoder's requirement that memory >= 8 * parallelism.
		int parallelism = getIntProperty(runtime, ARGON2_PARALLELISM_PROPERTY, DEFAULT_PARALLELISM, 1, MAX_PARALLELISM);
		int memory = getIntProperty(runtime, ARGON2_MEMORY_PROPERTY, DEFAULT_MEMORY_KB, DEFAULT_MEMORY_KB, MAX_MEMORY_KB);
		int iterations = getIntProperty(runtime, ARGON2_ITERATIONS_PROPERTY, DEFAULT_ITERATIONS, DEFAULT_ITERATIONS, MAX_ITERATIONS);
		int saltLength = getIntProperty(runtime, ARGON2_SALT_LENGTH_PROPERTY, DEFAULT_SALT_LENGTH, 8, MAX_SALT_LENGTH);
		int hashLength = getIntProperty(runtime, ARGON2_HASH_LENGTH_PROPERTY, DEFAULT_HASH_LENGTH, MIN_HASH_LENGTH, MAX_PASSWORD_COLUMN_LENGTH);
		hashLength = clampHashLengthToColumnLimit(hashLength, saltLength, memory, iterations, parallelism);
		return new Argon2Config(saltLength, hashLength, parallelism, memory, iterations);
	}

	/**
	 * Reads an integer-valued runtime property, clamping it to the given bounds and falling
	 * back to the default when the value is missing or not a valid integer.
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
		}
		catch (NumberFormatException e) {
			log.warn("Invalid integer value for runtime property '{}': '{}', using default: {}", name, raw, defaultValue);
			return defaultValue;
		}
	}

	// Length of the "{argon2}" id prefix that OpenmrsDelegatingPasswordEncoder prepends to the
	// PHC string before it is persisted (encode() returns "{argon2}" + phc when upgraded). The
	// clamp below must leave room for it, or the stored value overflows the column even though
	// the bare PHC string fits.
	private static final int ENCODER_ID_PREFIX_LENGTH = 8;

	/**
	 * Caps the Argon2 hash length so the full value persisted to the {@value
	 * #MAX_PASSWORD_COLUMN_LENGTH} character {@code users.password} column always fits. When
	 * the encoder is used in upgraded (prefixed) mode the stored value is "{argon2}" plus the
	 * PHC string, so the budget is the column length minus the prefix. The total length of the
	 * PHC string depends on the work factors (the header) and the salt as well as the hash, so
	 * the ceiling must be computed jointly. Without it, a too-large hash is silently truncated
	 * on a non-strict database and can never verify again, locking the account out.
	 */
	private static int clampHashLengthToColumnLimit(int hashLength, int saltLength, int memory, int iterations, int parallelism) {
		String header = "$argon2id$v=19$m=" + memory + ",t=" + iterations + ",p=" + parallelism + "$";
		int saltBase64 = unpaddedBase64Length(saltLength);
		int maxHashBase64 = MAX_PASSWORD_COLUMN_LENGTH - ENCODER_ID_PREFIX_LENGTH - header.length() - saltBase64 - 1;
		if (maxHashBase64 <= 0) {
			return MIN_HASH_LENGTH;
		}
		int maxHash = maxHashBase64 * 6 / 8;
		if (hashLength > maxHash) {
			log.warn("Runtime property '{}' = {} would overflow the {} character password column, clamping to {}", ARGON2_HASH_LENGTH_PROPERTY, hashLength, MAX_PASSWORD_COLUMN_LENGTH, maxHash);
			return maxHash;
		}
		return hashLength;
	}

	private static int unpaddedBase64Length(int byteLength) {
		return (4 * byteLength + 2) / 3;
	}

	/**
	 * Immutable holder for the resolved Argon2 work factors.
	 */
	static final class Argon2Config {

		final int saltLength;

		final int hashLength;

		final int parallelism;

		final int memory;

		final int iterations;

		Argon2Config(int saltLength, int hashLength, int parallelism, int memory, int iterations) {
			this.saltLength = saltLength;
			this.hashLength = hashLength;
			this.parallelism = parallelism;
			this.memory = memory;
			this.iterations = iterations;
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
	 * Returns the single cached Argon2PasswordEncoder, built from the runtime properties on
	 * first use. The encoder is reused rather than rebuilt per call.
	 */
	static Argon2PasswordEncoder getArgon2Encoder() {
		if (argon2Encoder == null) {
			synchronized (Security.class) {
				if (argon2Encoder == null) {
					Argon2Config config = resolveArgon2Config(Context.getRuntimeProperties());
					argon2Encoder = new Argon2PasswordEncoder(config.saltLength, config.hashLength, config.parallelism, config.memory, config.iterations);
				}
			}
		}
		return argon2Encoder;
	}

	/**
	 * Spring factory method used to create the {@code argon2PasswordEncoder} bean (see
	 * applicationContext-service.xml). Each work factor is wired from its
	 * {@code security.argon2.*} runtime-property placeholder and clamped to a safe range here,
	 * so the bean cannot overflow the {@code users.password} column either.
	 *
	 * @param saltLength the salt length in bytes
	 * @param hashLength the hash length in bytes
	 * @param parallelism the parallelism
	 * @param memory the memory cost in KiB
	 * @param iterations the number of iterations
	 * @return an Argon2PasswordEncoder configured from the given work factors
	 * @since 2.8.10
	 */
	public static Argon2PasswordEncoder createArgon2PasswordEncoder(int saltLength, int hashLength, int parallelism, int memory, int iterations) {
		Properties props = new Properties();
		props.setProperty(ARGON2_SALT_LENGTH_PROPERTY, Integer.toString(saltLength));
		props.setProperty(ARGON2_HASH_LENGTH_PROPERTY, Integer.toString(hashLength));
		props.setProperty(ARGON2_PARALLELISM_PROPERTY, Integer.toString(parallelism));
		props.setProperty(ARGON2_MEMORY_PROPERTY, Integer.toString(memory));
		props.setProperty(ARGON2_ITERATIONS_PROPERTY, Integer.toString(iterations));
		Argon2Config config = resolveArgon2Config(props);
		return new Argon2PasswordEncoder(config.saltLength, config.hashLength, config.parallelism, config.memory, config.iterations);
	}

	/**
	 * Resets the cached Argon2 encoder so it is rebuilt from the runtime properties on the next
	 * call. Package-private and intended for testing only.
	 */
	static void resetEncoder() {
		synchronized (Security.class) {
			argon2Encoder = null;
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
