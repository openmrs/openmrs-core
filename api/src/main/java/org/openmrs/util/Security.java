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
import java.net.IDN;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

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

/**
 * OpenMRS's security class deals with the hashing of passwords.
 */
public class Security {

	/**
	 * encryption settings
	 */
	private static final Logger log = LoggerFactory.getLogger(Security.class);
	
	private static final Random RANDOM = new SecureRandom();

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

	/**
	 * Validates outbound URLs used in server-side requests to reduce SSRF risk.
	 * Only http/https are allowed. If {@link OpenmrsConstants#GP_SECURITY_ALLOWED_OUTBOUND_URL_HOSTS}
	 * is set, the host must match the allowlist; otherwise, private/local/reserved
	 * addresses are blocked.
	 *
	 * <p>In blacklist mode (no allowlist configured), the hostname is resolved once and the
	 * validated addresses are returned. Callers MUST use {@link #buildSafeUrl(URL, InetAddress)}
	 * with one of the returned addresses to open the connection so the hostname is never
	 * re-resolved, preventing DNS-rebinding / TOCTOU attacks. In allowlist mode an empty list
	 * is returned and callers may use the original URL directly.</p>
	 *
	 * @param url the URL to validate
	 * @return resolved {@link InetAddress} list in blacklist mode; empty list in allowlist mode
	 * @throws SecurityException if the URL is unsafe for server-side requests
	 */
	public static List<InetAddress> validateUrlForServerRequest(URL url) {
		if (url == null) {
			throw new APIException("url.cannot.be.null", (Object[]) null);
		}

		String protocol = url.getProtocol();
		if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol)) {
			throw new SecurityException("Only http/https URLs are allowed for server-side requests");
		}

		String host = url.getHost();
		if (!StringUtils.hasText(host)) {
			throw new SecurityException("URL host is required for server-side requests");
		}

		String normalizedHost = normalizeHost(host);
		if (!StringUtils.hasText(normalizedHost)) {
			throw new SecurityException("URL host is required for server-side requests");
		}

		Set<String> allowedHosts = getAllowedOutboundUrlHosts();
		if (!allowedHosts.isEmpty() && !isHostAllowed(normalizedHost, allowedHosts)) {
			throw new SecurityException("URL host is not in the allowed list");
		}

		// Always resolve DNS once and return the addresses so every caller connects via the
		// resolved IP, preventing DNS-rebinding / TOCTOU in both allowlist and blacklist modes.
		// In blacklist mode the addresses are also checked against private/reserved ranges.
		return resolveAndValidatePublicAddresses(normalizedHost, allowedHosts.isEmpty());
	}

	/**
	 * Builds a URL replacing the hostname in {@code original} with the numeric IP of
	 * {@code resolvedAddress}. Use with the return value of {@link #validateUrlForServerRequest}
	 * to open connections without re-resolving DNS.
	 *
	 * @param original        the original URL
	 * @param resolvedAddress one address returned by {@link #validateUrlForServerRequest}
	 * @return a new URL whose host is the numeric IP address
	 * @throws MalformedURLException if the reconstructed URL is invalid
	 */
	public static URL buildSafeUrl(URL original, InetAddress resolvedAddress) throws MalformedURLException {
		return new URL(original.getProtocol(), resolvedAddress.getHostAddress(), original.getPort(), original.getFile());
	}

	private static Set<String> getAllowedOutboundUrlHosts() {
		String configuredHosts = Context.getRuntimeProperties()
		        .getProperty(OpenmrsConstants.GP_SECURITY_ALLOWED_OUTBOUND_URL_HOSTS);
		if (!StringUtils.hasText(configuredHosts) && Context.isSessionOpen()) {
			try {
				configuredHosts = Context.getAdministrationService()
				        .getGlobalProperty(OpenmrsConstants.GP_SECURITY_ALLOWED_OUTBOUND_URL_HOSTS, "");
			}
			catch (Exception e) {
				log.debug("Unable to read allowed outbound URL hosts global property", e);
			}
		}

		if (!StringUtils.hasText(configuredHosts)) {
			return Collections.emptySet();
		}

		Set<String> hosts = new LinkedHashSet<>();
		for (String entry : configuredHosts.split(",")) {
			String normalized = normalizeAllowListEntry(entry);
			if (StringUtils.hasText(normalized)) {
				hosts.add(normalized);
			}
		}
		return hosts;
	}

	private static String normalizeAllowListEntry(String entry) {
		if (!StringUtils.hasText(entry)) {
			return "";
		}

		String trimmed = entry.trim();
		boolean allowSubdomains = trimmed.startsWith(".");
		String value = allowSubdomains ? trimmed.substring(1) : trimmed;
		String host = value;

		if (value.contains("://")) {
			try {
				URL url = new URL(value);
				if (StringUtils.hasText(url.getHost())) {
					host = url.getHost();
				}
			}
			catch (MalformedURLException e) {
				// fall back to the provided value
			}
		} else if (value.contains("/")) {
			host = value.substring(0, value.indexOf('/'));
		}

		String normalized = normalizeHost(host);
		if (!StringUtils.hasText(normalized)) {
			return "";
		}

		return allowSubdomains ? "." + normalized : normalized;
	}

	private static String normalizeHost(String host) {
		String normalized = host.trim().toLowerCase(Locale.ROOT);
		if (normalized.startsWith("[") && normalized.endsWith("]")) {
			normalized = normalized.substring(1, normalized.length() - 1);
		}
		if (normalized.endsWith(".")) {
			normalized = normalized.substring(0, normalized.length() - 1);
		}
		if (normalized.contains(":")) {
			return normalized;
		}
		try {
			return IDN.toASCII(normalized);
		}
		catch (IllegalArgumentException e) {
			return normalized;
		}
	}

	private static boolean isHostAllowed(String host, Set<String> allowedHosts) {
		for (String allowed : allowedHosts) {
			if (allowed.startsWith(".")) {
				String suffix = allowed.substring(1);
				if (host.equals(suffix) || host.endsWith("." + suffix)) {
					return true;
				}
			} else if (host.equals(allowed)) {
				return true;
			}
		}
		return false;
	}

	private static List<InetAddress> resolveAndValidatePublicAddresses(String host, boolean enforceBlacklist) {
		InetAddress[] addresses;
		try {
			addresses = InetAddress.getAllByName(host);
		}
		catch (UnknownHostException e) {
			// Fail closed: an unresolvable host is not safe to connect to.
			throw new SecurityException("URL host could not be resolved: " + host, e);
		}

		List<InetAddress> validated = new ArrayList<>(addresses.length);
		for (InetAddress address : addresses) {
			if (enforceBlacklist && isPrivateOrLocalAddress(address)) {
				throw new SecurityException("URL host resolves to a private or local address");
			}
			validated.add(address);
		}
		return validated;
	}

	private static boolean isPrivateOrLocalAddress(InetAddress address) {
		if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
		        || address.isSiteLocalAddress() || address.isMulticastAddress()) {
			return true;
		}

		byte[] bytes = address.getAddress();
		if (bytes.length == 4) {
			int b0 = bytes[0] & 0xFF;
			int b1 = bytes[1] & 0xFF;
			int b2 = bytes[2] & 0xFF;
			return isPrivateOrLocalIpv4(b0, b1, b2);
		} else if (bytes.length == 16) {
			int first = bytes[0] & 0xFF;
			if ((first & 0xFE) == 0xFC) {
				return true; // fc00::/7
			}
			if (isIpv4MappedOrCompatible(bytes)) {
				int b0 = bytes[12] & 0xFF;
				int b1 = bytes[13] & 0xFF;
				int b2 = bytes[14] & 0xFF;
				return isPrivateOrLocalIpv4(b0, b1, b2);
			}
		}

		return false;
	}

	private static boolean isPrivateOrLocalIpv4(int b0, int b1, int b2) {
		if (b0 == 0) {
			return true; // 0.0.0.0/8
		}
		if (b0 == 100 && (b1 & 0xC0) == 0x40) {
			return true; // 100.64.0.0/10
		}
		if (b0 == 192 && b1 == 0 && b2 == 0) {
			return true; // 192.0.0.0/24
		}
		if (b0 == 192 && b1 == 0 && b2 == 2) {
			return true; // 192.0.2.0/24
		}
		if (b0 == 198 && (b1 == 18 || b1 == 19)) {
			return true; // 198.18.0.0/15
		}
		if (b0 == 198 && b1 == 51 && b2 == 100) {
			return true; // 198.51.100.0/24
		}
		if (b0 == 203 && b1 == 0 && b2 == 113) {
			return true; // 203.0.113.0/24
		}
		if (b0 >= 240) {
			return true; // 240.0.0.0/4
		}
		return false;
	}

	private static boolean isIpv4MappedOrCompatible(byte[] bytes) {
		boolean allZero = true;
		for (int i = 0; i < 12; i++) {
			if (bytes[i] != 0) {
				allZero = false;
				break;
			}
		}
		if (allZero) {
			return true; // ::w.x.y.z
		}

		for (int i = 0; i < 10; i++) {
			if (bytes[i] != 0) {
				return false;
			}
		}
		return (bytes[10] == (byte) 0xFF && bytes[11] == (byte) 0xFF);
	}

}
