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

import org.openmrs.GlobalProperty;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.util.StringUtils;

/**
 * OpenMRS's security class deals with the hashing of passwords.
 */
public class Security implements GlobalPropertyListener {

	/**
	 * encryption settings
	 */
	private static final Logger log = LoggerFactory.getLogger(Security.class);

	private static final String SHA_512 = "SHA-512";

	private static final Random RANDOM = new SecureRandom();

	private static final int MAX_MEMORY_KB = 1048576; // 1GB in KB

	private static final int MAX_ITERATIONS = 10;

	private static final int MAX_PARALLELISM = 8;

	private static final int MAX_SALT_LENGTH = 32;
	private static final int MAX_HASH_LENGTH = 48; // Max with 16-byte salt that fits VARCHAR(128)

	private static volatile int cachedSaltLength;
	private static volatile int cachedHashLength;
	private static volatile int cachedParallelism;
	private static volatile int cachedMemory;
	private static volatile int cachedIterations;

	private static volatile Argon2PasswordEncoder argon2Encoder;

	private Security() {
	}

	/**
	 * Loads the Argon2 configuration from global properties with proxy privilege,
	 * following the same pattern as {@link LocaleUtility}. Uses null-check for retry
	 * instead of a latching flag, so a failed read is retried on the next call.
	 */
	private static void loadConfiguration() {
		if (cachedSaltLength != 0) {
			return;
		}
		synchronized (Security.class) {
			if (cachedSaltLength != 0) {
				return;
			}
			if (!Context.isSessionOpen()) {
				applyDefaults();
				return;
			}
			try {
				Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
				AdministrationService adminService = Context.getAdministrationService();
				cachedSaltLength = parseIntProperty(
					adminService.getGlobalProperty(OpenmrsConstants.GP_ARGON2_SALT_LENGTH, "16"), 16);
				cachedHashLength = parseIntProperty(
					adminService.getGlobalProperty(OpenmrsConstants.GP_ARGON2_HASH_LENGTH, "32"), 32);
				cachedParallelism = parseIntProperty(
					adminService.getGlobalProperty(OpenmrsConstants.GP_ARGON2_PARALLELISM, "1"), 1);
				cachedMemory = parseIntProperty(
					adminService.getGlobalProperty(OpenmrsConstants.GP_ARGON2_MEMORY, "65536"), 65536);
				cachedIterations = parseIntProperty(
					adminService.getGlobalProperty(OpenmrsConstants.GP_ARGON2_ITERATIONS, "3"), 3);

				if (!isPhcLengthSafe(cachedHashLength, cachedSaltLength)) {
					log.warn("Loaded Argon2 configuration (hashLength={}, saltLength={}) would exceed VARCHAR(128) limit, adjusting to safe values",
						cachedHashLength, cachedSaltLength);
					cachedHashLength = calculateSafeHashLength(cachedSaltLength);
				}

				Context.getAdministrationService().addGlobalPropertyListener(new Security());
			} catch (Exception e) {
				log.warn("Unable to load Argon2 configuration from global properties: {}", e.getMessage());
				log.debug("Exception caught while loading Argon2 configuration", e);
				applyDefaults();
			} finally {
				Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
			}
		}
	}

	private static void applyDefaults() {
		cachedSaltLength = 16;
		cachedHashLength = 32;
		cachedParallelism = 1;
		cachedMemory = 65536;
		cachedIterations = 3;
	}

	/**
	 * Helper method to parse integer properties with validation.
	 */
	private static int parseIntProperty(String value, int defaultValue) {
		try {
			if (value != null) {
				int parsed = Integer.parseInt(value.trim());
				if (parsed > 0) {
					return parsed;
				}
			}
		} catch (NumberFormatException e) {
			log.warn("Invalid integer value: {}, using default: {}", value, defaultValue);
		}
		return defaultValue;
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

		if (hashedPassword.startsWith("$argon2id$")) {
			return getArgon2Encoder().matches(passwordToHash, hashedPassword);
		}

		return hashedPassword.equals(encodeString(passwordToHash, SHA_512))
			|| hashedPassword.equals(encodeStringSHA1(passwordToHash))
			|| hashedPassword.equals(incorrectlyEncodeString(passwordToHash));
	}

	/**
	 * This method will hash <code>strToEncode</code> using the preferred algorithm. Currently,
	 * OpenMRS's preferred algorithm is SHA-512. This method is used for activation key hashing
	 * where deterministic, unsalted hashing is required.
	 *
	 * @param strToEncode string to encode
	 * @return the SHA-512 hash of a given string
	 * @since 1.5
	 */
	public static String encodeString(String strToEncode) throws APIException {
		return encodeString(strToEncode, SHA_512);
	}

	/**
	 * Encode a credential (password or secret answer) using Argon2id.
	 * This is the credential encoding path, separate from the activation key path.
	 *
	 * @param strToEncode credential string to encode
	 * @return the Argon2id hash of the credential
	 * @since 2.8.10
	 */
	public static String encodeCredential(String strToEncode) throws APIException {
		return getArgon2Encoder().encode(strToEncode);
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

	private static Argon2PasswordEncoder getArgon2Encoder() {
		loadConfiguration();
		if (argon2Encoder == null) {
			synchronized (Security.class) {
				if (argon2Encoder == null) {
					argon2Encoder = new Argon2PasswordEncoder(
						cachedSaltLength,
						cachedHashLength,
						cachedParallelism,
						cachedMemory,
						cachedIterations
					);
				}
			}
		}
		return argon2Encoder;
	}

	/**
	 * Resets the cached Argon2 encoder and configuration values.
	 * This is a package-private method intended for testing purposes only.
	 * It forces the encoder to be recreated with the current configuration
	 * on the next call to {@link #encodeCredential(String)}.
	 */
	static void resetEncoder() {
		synchronized (Security.class) {
			argon2Encoder = null;
			cachedSaltLength = 0;
			cachedHashLength = 0;
			cachedParallelism = 0;
			cachedMemory = 0;
			cachedIterations = 0;
		}
	}

	/**
	 * Calculates whether the given hash and salt lengths will produce a PHC string
	 * that fits within the VARCHAR(128) database column.
	 *
	 * PHC format: $argon2id$v=19$m=X,t=Y,p=Z$SALT$HASH
	 * Header length is computed from actual parameter values rather than assumed.
	 */
	private static boolean isPhcLengthSafe(int hashLength, int saltLength) {
		int headerLength = computePhcHeaderLength(cachedMemory, cachedIterations, cachedParallelism);
		int saltEncodedLength = (int) Math.ceil(saltLength / 3.0) * 4;
		int hashEncodedLength = (int) Math.ceil(hashLength / 3.0) * 4;
		int totalLength = headerLength + saltEncodedLength + hashEncodedLength;
		return totalLength <= 128;
	}

	/**
	 * Calculates the maximum safe hash length for a given salt length to fit in VARCHAR(128).
	 */
	private static int calculateSafeHashLength(int saltLength) {
		int headerLength = computePhcHeaderLength(cachedMemory, cachedIterations, cachedParallelism);
		int saltEncodedLength = (int) Math.ceil(saltLength / 3.0) * 4;
		int availableForHash = 128 - headerLength - saltEncodedLength;
		return Math.max(4, (availableForHash / 4) * 3); // Minimum 4 bytes
	}

	/**
	 * Computes the actual PHC header length: $argon2id$v=19$m=X,t=Y,p=Z$
	 */
	private static int computePhcHeaderLength(int memory, int iterations, int parallelism) {
		// "$argon2id$v=19$m=" + memory + ",t=" + iterations + ",p=" + parallelism + "$"
		return "$argon2id$v=19$m=".length()
			+ String.valueOf(memory).length()
			+ ",t=".length()
			+ String.valueOf(iterations).length()
			+ ",p=".length()
			+ String.valueOf(parallelism).length()
			+ "$".length();
	}

	/**
	 * GlobalPropertyListener implementation to cache Argon2 configuration values
	 * and avoid database reads on every password operation.
	 */
	@Override
	public boolean supportsPropertyName(String propertyName) {
		return OpenmrsConstants.GP_ARGON2_SALT_LENGTH.equals(propertyName)
			|| OpenmrsConstants.GP_ARGON2_HASH_LENGTH.equals(propertyName)
			|| OpenmrsConstants.GP_ARGON2_PARALLELISM.equals(propertyName)
			|| OpenmrsConstants.GP_ARGON2_MEMORY.equals(propertyName)
			|| OpenmrsConstants.GP_ARGON2_ITERATIONS.equals(propertyName);
	}

	@Override
	public void globalPropertyChanged(GlobalProperty newValue) {
		if (newValue == null || newValue.getPropertyValue() == null) {
			return;
		}

		String propertyName = newValue.getProperty();
		try {
			int value = Integer.parseInt(newValue.getPropertyValue().trim());
			if (value <= 0) {
				log.warn("Invalid value for global property '{}': {}, must be > 0, ignoring", propertyName, value);
				return;
			}

			int maxValue = getMaxValueForProperty(propertyName);
			if (maxValue > 0 && value > maxValue) {
				log.warn("Value for global property '{}': {} exceeds maximum of {}, clamping",
					propertyName, value, maxValue);
				value = maxValue;
			}

			if (OpenmrsConstants.GP_ARGON2_HASH_LENGTH.equals(propertyName) ||
			    OpenmrsConstants.GP_ARGON2_SALT_LENGTH.equals(propertyName)) {
				int newHashLength = OpenmrsConstants.GP_ARGON2_HASH_LENGTH.equals(propertyName) ?
					value : cachedHashLength;
				int newSaltLength = OpenmrsConstants.GP_ARGON2_SALT_LENGTH.equals(propertyName) ?
					value : cachedSaltLength;

				if (!isPhcLengthSafe(newHashLength, newSaltLength)) {
					int safeHashLength = calculateSafeHashLength(newSaltLength);
					if (OpenmrsConstants.GP_ARGON2_HASH_LENGTH.equals(propertyName)) {
						log.warn("Hash length {} with salt length {} would exceed VARCHAR(128) limit, clamping to {}",
							value, newSaltLength, safeHashLength);
						value = safeHashLength;
					} else {
						log.warn("Salt length {} with hash length {} would exceed VARCHAR(128) limit, reducing hash length",
							value, newHashLength);
						synchronized (Security.class) {
							cachedSaltLength = value;
							cachedHashLength = calculateSafeHashLength(value);
							argon2Encoder = null;
							return;
						}
					}
				}
			}

			synchronized (Security.class) {
				if (OpenmrsConstants.GP_ARGON2_SALT_LENGTH.equals(propertyName)) {
					cachedSaltLength = value;
				} else if (OpenmrsConstants.GP_ARGON2_HASH_LENGTH.equals(propertyName)) {
					cachedHashLength = value;
				} else if (OpenmrsConstants.GP_ARGON2_PARALLELISM.equals(propertyName)) {
					cachedParallelism = value;
				} else if (OpenmrsConstants.GP_ARGON2_MEMORY.equals(propertyName)) {
					cachedMemory = value;
				} else if (OpenmrsConstants.GP_ARGON2_ITERATIONS.equals(propertyName)) {
					cachedIterations = value;
				}
				argon2Encoder = null;
			}
		} catch (NumberFormatException e) {
			log.warn("Invalid numeric value for global property '{}': {}, ignoring", propertyName, newValue.getPropertyValue());
		}
	}

	@Override
	public void globalPropertyDeleted(String propertyName) {
		synchronized (Security.class) {
			if (OpenmrsConstants.GP_ARGON2_SALT_LENGTH.equals(propertyName)) {
				cachedSaltLength = 16;
			} else if (OpenmrsConstants.GP_ARGON2_HASH_LENGTH.equals(propertyName)) {
				cachedHashLength = 32;
			} else if (OpenmrsConstants.GP_ARGON2_PARALLELISM.equals(propertyName)) {
				cachedParallelism = 1;
			} else if (OpenmrsConstants.GP_ARGON2_MEMORY.equals(propertyName)) {
				cachedMemory = 65536;
			} else if (OpenmrsConstants.GP_ARGON2_ITERATIONS.equals(propertyName)) {
				cachedIterations = 3;
			}
			argon2Encoder = null;
		}
	}

	private static int getMaxValueForProperty(String key) {
		if (OpenmrsConstants.GP_ARGON2_MEMORY.equals(key)) {
			return MAX_MEMORY_KB;
		} else if (OpenmrsConstants.GP_ARGON2_ITERATIONS.equals(key)) {
			return MAX_ITERATIONS;
		} else if (OpenmrsConstants.GP_ARGON2_PARALLELISM.equals(key)) {
			return MAX_PARALLELISM;
		} else if (OpenmrsConstants.GP_ARGON2_HASH_LENGTH.equals(key)) {
			return MAX_HASH_LENGTH;
		} else if (OpenmrsConstants.GP_ARGON2_SALT_LENGTH.equals(key)) {
			return MAX_SALT_LENGTH;
		}
		return -1;
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
