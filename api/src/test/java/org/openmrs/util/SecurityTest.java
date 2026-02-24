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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.util.StringUtils;

/**
 * Tests the methods on the {@link Security} class
 */
public class SecurityTest {
	
	private static final int HASH_LENGTH = 128;
	
	/**
	 * @see Security#encodeString(String)
	 */
	@Test
	public void encodeString_shouldEncodeStringsTo128Characters() {
		String hash = Security.encodeString("test" + "c788c6ad82a157b712392ca695dfcf2eed193d7f");
		assertEquals(HASH_LENGTH, hash.length());
	}
	
	/**
	 * @see Security#encodeString(String)
	 */
	@Test
	public void encodeString_shouldEncodeStringsToXCharactersWithXCharactersSalt() {
		String hash = Security.encodeString("test" + Security.getRandomToken());
		assertEquals(HASH_LENGTH, hash.length());
	}
	
	/**
	 * @see Security#hashMatches(String,String)
	 */
	@Test
	public void hashMatches_shouldMatchStringsHashedWithSha1Algorithm() {
		assertTrue(Security.hashMatches("4a1750c8607d0fa237de36c6305715c223415189", "test"
		        + "c788c6ad82a157b712392ca695dfcf2eed193d7f"));
	}
	
	/**
	 * @see Security#hashMatches(String,String)
	 */
	@Test
	public void hashMatches_shouldMatchStringsHashedWithSha512AlgorithmAnd128CharactersSalt() {
		String password = "1d1436658853aceceadd72e92f1ae9089a0000fbb38cea519ce34eae9f28523930ecb212177dbd607d83dc275fde3e9ca648deb557d503ad0bcd01a955a394b2";
		String passwordToHash = "test"
		        + "0d7bb319434295261601202e14494b959cdd69c6ceb54ee3890e176ae780ce9edf797f48afde5f39906a6bd75b8a5feeac8f5339615acf7429c7dda85220d329";
		assertTrue(Security.hashMatches(password, passwordToHash));
	}
	
	/**
	 * @see Security#hashMatches(String,String)
	 */
	@Test
	public void hashMatches_shouldMatchStringsHashedWithIncorrectSha1Algorithm() {
		assertTrue(Security.hashMatches("4a1750c8607dfa237de36c6305715c223415189", "test"
		        + "c788c6ad82a157b712392ca695dfcf2eed193d7f"));
	}
	
	/**
	 * @see Security#decrypt(String)
	 */
	@Test
	public void decrypt_shouldDecryptShortAndLongText() {
		final Decoder base64 = Base64.getDecoder();
		// use specific IV and Key
		byte[] initVector = base64.decode("9wyBUNglFCRVSUhMfsTa3Q==");
		byte[] secretKey = base64.decode("dTfyELRrAICGDwzjHDjuhw==");
		
		// perform decryption
		String expected = "this is fantasmic";
		String encrypted = encryptWithGcm(expected, initVector, secretKey);
		String actual = Security.decrypt(encrypted, initVector, secretKey);
		assertTrue(OpenmrsUtil.nullSafeEquals(expected, actual));
		
		expected = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus porta sapien ac nisi imperdiet posuere. Ma"
		        + "ecenas nec felis ac enim posuere semper. In arcu turpis, elementum nec auctor id, pretium sed tortor. Quisque "
		        + "sit amet erat ante. Praesent metus dui, porttitor non volutpat eu, porta sed ante. Fusce quis dignissim nisl. "
		        + "Vivamus id massa in nisl sollicitudin iaculis ac ut odio. Morbi et sapien non massa ultricies commodo. Nunc se"
		        + "mper, nulla a pellentesque adipiscing, urna nisl vulputate lacus, non rutrum nulla mauris at tortor. Quisque m"
		        + "olestie, velit nec vehicula tempor, mi eros fermentum ipsum, ut ullamcorper nisl sem at risus. Nam varius nunc"
		        + " sit amet velit blandit gravida sed vel purus. Nam ac justo ut metus elementum vehicula ac non ante. Aliquam p"
		        + "ellentesque semper mauris ut pulvinar.Duis et orci nisi. Mauris tempor consequat felis, vel consequat diam con"
		        + "sequat vitae. Donec eget dolor quis nulla lobortis vestibulum. Quisque vel ipsum in sapien egestas blandit. Pr"
		        + "aesent malesuada tellus nec sapien blandit sit amet molestie magna consequat. Pellentesque quis tempus urna. Q"
		        + "uisque ut nibh ut tellus hendrerit rhoncus. Aenean ultricies lorem eu sem condimentum at consectetur magna dig"
		        + "nissim. Nam porta lobortis consequat. Suspendisse congue, tellus quis sodales blandit, augue massa interdum se"
		        + "m, vel suscipit ipsum risus vitae massa. Quisque ipsum tellus, gravida sed suscipit non, ultricies eu augue. E"
		        + "tiam consequat consequat massa a accumsan. Quisque rhoncus nisi lectus, vel ultrices sapien. Aenean a felis fe"
		        + "lis, sit amet vestibulum lorem. Cras ut fermentum magna.Quisque vel erat eget eros bibendum convallis vitae a "
		        + "augue. Maecenas posuere ullamcorper quam, ac ullamcorper eros egestas at. Nulla ipsum purus, venenatis ac dign"
		        + "issim in, bibendum eget enim. Nulla rhoncus dui eu augue egestas in tempus augue congue. Suspendisse potenti. "
		        + "Aenean faucibus felis ut leo venenatis congue lacinia felis tempor. Phasellus fermentum nisl venenatis quam mo"
		        + "lestie fermentum euismod metus pretium. Duis facilisis pharetra vehicula. Class aptent taciti sociosqu ad lito"
		        + "ra torquent per conubia nostra, per inceptos himenaeos. Donec aliquet feugiat sapien, vitae tristique nisl lac"
		        + "inia non. Fusce eros dolor, egestas et auctor vel, aliquam ac lorem. In lacinia cursus pretium.Nulla vitae nis"
		        + "i vitae magna varius posuere. Curabitur non dui at odio scelerisque mattis a a risus. Suspendisse augue lacus,"
		        + " pulvinar vitae fringilla tempor, adipiscing vel velit. Suspendisse lorem dui, eleifend vel rhoncus ac, porta "
		        + "sed odio. Maecenas eget pellentesque ligula. Cras vitae auctor justo. Duis at massa vitae risus semper element"
		        + "um. Proin at magna et augue volutpat tincidunt nec sed erat. Quisque id sapien tortor, ut gravida erat. Vivamu"
		        + "s dictum, enim non sodales laoreet, ante libero suscipit erat, ac tristique purus eros sed augue. Quisque magn"
		        + "a mi, varius ac accumsan aliquam, aliquam id risus. Phasellus dignissim dictum massa, ac consequat risus venen"
		        + "atis in. Morbi imperdiet bibendum sem, eu mollis urna aliquet a. In ac augue vitae ante ultrices sollicitudin "
		        + "vel sed elit. Nunc fringilla vestibulum egestas. Duis risus lorem, varius a vulputate at, blandit vel lectus. "
		        + "Sed mollis, ipsum nec fringilla accumsan, risus nibh iaculis ligula, non tristique nibh tortor vitae sem. Null"
		        + "a facilisi. In id lectus vitae felis elementum lobortis. Aenean et nisi orci.Nam mi lorem, posuere non auctor "
		        + "sed, accumsan eu magna. Fusce sit amet tellus augue. Nunc eleifend, justo id pharetra hendrerit, urna augue ul"
		        + "tricies mi, sed fringilla arcu libero quis nulla. Maecenas tristique auctor cursus. Curabitur venenatis lacus "
		        + "non leo aliquet ornare. Praesent justo turpis, dictum eu dictum convallis, faucibus sit amet erat. Praesent se"
		        + "d dui id enim euismod interdum. Integer sed fermentum neque. Curabitur enim nunc, euismod adipiscing iaculis e"
		        + "get, tincidunt vel nunc. Nullam at neque sem, rutrum aliquet elit. In et velit enim, tempus mollis nunc. Sed s"
		        + "it amet quam justo. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur convallis dolor non lig"
		        + "ula fermentum imperdiet.";
		
		encrypted = encryptWithGcm(expected, initVector, secretKey);
		
		actual = Security.decrypt(encrypted, initVector, secretKey);
		assertTrue(OpenmrsUtil.nullSafeEquals(expected, actual));
	}
	
	/**
	 * @see Security#encrypt(String)
	 */
	@Test
	public void encrypt_shouldEncryptShortAndLongText() {
		// small text
		String expected = "a";
		String encrypted = Security.encrypt(expected);
		assertTrue(StringUtils.hasText(encrypted));
		String actual = Security.decrypt(encrypted);
		assertTrue(OpenmrsUtil.nullSafeEquals(expected, actual));
		
		// long text
		expected = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus porta sapien ac nisi imperdiet posuere. Maecenas nec felis ac enim posuere semper. In arcu turpis, elementum nec auctor id, pretium sed tortor. Quisque sit amet erat ante. Praesent metus dui, porttitor non volutpat eu, porta sed ante. Fusce quis dignissim nisl. Vivamus id massa in nisl sollicitudin iaculis ac ut odio. Morbi et sapien non massa ultricies commodo. Nunc semper, nulla a pellentesque adipiscing, urna nisl vulputate lacus, non rutrum nulla mauris at tortor. Quisque molestie, velit nec vehicula tempor, mi eros fermentum ipsum, ut ullamcorper nisl sem at risus. Nam varius nunc sit amet velit blandit gravida sed vel purus. Nam ac justo ut metus elementum vehicula ac non ante. Aliquam pellentesque semper mauris ut pulvinar."
		        + "Duis et orci nisi. Mauris tempor consequat felis, vel consequat diam consequat vitae. Donec eget dolor quis nulla lobortis vestibulum. Quisque vel ipsum in sapien egestas blandit. Praesent malesuada tellus nec sapien blandit sit amet molestie magna consequat. Pellentesque quis tempus urna. Quisque ut nibh ut tellus hendrerit rhoncus. Aenean ultricies lorem eu sem condimentum at consectetur magna dignissim. Nam porta lobortis consequat. Suspendisse congue, tellus quis sodales blandit, augue massa interdum sem, vel suscipit ipsum risus vitae massa. Quisque ipsum tellus, gravida sed suscipit non, ultricies eu augue. Etiam consequat consequat massa a accumsan. Quisque rhoncus nisi lectus, vel ultrices sapien. Aenean a felis felis, sit amet vestibulum lorem. Cras ut fermentum magna."
		        + "Quisque vel erat eget eros bibendum convallis vitae a augue. Maecenas posuere ullamcorper quam, ac ullamcorper eros egestas at. Nulla ipsum purus, venenatis ac dignissim in, bibendum eget enim. Nulla rhoncus dui eu augue egestas in tempus augue congue. Suspendisse potenti. Aenean faucibus felis ut leo venenatis congue lacinia felis tempor. Phasellus fermentum nisl venenatis quam molestie fermentum euismod metus pretium. Duis facilisis pharetra vehicula. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Donec aliquet feugiat sapien, vitae tristique nisl lacinia non. Fusce eros dolor, egestas et auctor vel, aliquam ac lorem. In lacinia cursus pretium."
		        + "Nulla vitae nisi vitae magna varius posuere. Curabitur non dui at odio scelerisque mattis a a risus. Suspendisse augue lacus, pulvinar vitae fringilla tempor, adipiscing vel velit. Suspendisse lorem dui, eleifend vel rhoncus ac, porta sed odio. Maecenas eget pellentesque ligula. Cras vitae auctor justo. Duis at massa vitae risus semper elementum. Proin at magna et augue volutpat tincidunt nec sed erat. Quisque id sapien tortor, ut gravida erat. Vivamus dictum, enim non sodales laoreet, ante libero suscipit erat, ac tristique purus eros sed augue. Quisque magna mi, varius ac accumsan aliquam, aliquam id risus. Phasellus dignissim dictum massa, ac consequat risus venenatis in. Morbi imperdiet bibendum sem, eu mollis urna aliquet a. In ac augue vitae ante ultrices sollicitudin vel sed elit. Nunc fringilla vestibulum egestas. Duis risus lorem, varius a vulputate at, blandit vel lectus. Sed mollis, ipsum nec fringilla accumsan, risus nibh iaculis ligula, non tristique nibh tortor vitae sem. Nulla facilisi. In id lectus vitae felis elementum lobortis. Aenean et nisi orci."
		        + "Nam mi lorem, posuere non auctor sed, accumsan eu magna. Fusce sit amet tellus augue. Nunc eleifend, justo id pharetra hendrerit, urna augue ultricies mi, sed fringilla arcu libero quis nulla. Maecenas tristique auctor cursus. Curabitur venenatis lacus non leo aliquet ornare. Praesent justo turpis, dictum eu dictum convallis, faucibus sit amet erat. Praesent sed dui id enim euismod interdum. Integer sed fermentum neque. Curabitur enim nunc, euismod adipiscing iaculis eget, tincidunt vel nunc. Nullam at neque sem, rutrum aliquet elit. In et velit enim, tempus mollis nunc. Sed sit amet quam justo. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur convallis dolor non ligula fermentum imperdiet.";
		encrypted = Security.encrypt(expected);
		assertTrue(StringUtils.hasText(encrypted));
		actual = Security.decrypt(encrypted);
		assertTrue(OpenmrsUtil.nullSafeEquals(expected, actual));
		
		// foreign text
		expected = "傑里米 (Jeremy), 潔儀 (Kitty) and 贏 (Win) like encryption :-D";
		encrypted = Security.encrypt(expected);
		assertTrue(StringUtils.hasText(encrypted));
		actual = Security.decrypt(encrypted);
		assertTrue(OpenmrsUtil.nullSafeEquals(expected, actual));
	}
	
	@Test
	public void encrypt_shouldUseRandomIvPerEncryption() {
		String expected = "same text";
		String encrypted1 = Security.encrypt(expected);
		String encrypted2 = Security.encrypt(expected);
		
		assertTrue(StringUtils.hasText(encrypted1));
		assertTrue(StringUtils.hasText(encrypted2));
		assertTrue(!encrypted1.equals(encrypted2));
		
		String actual1 = Security.decrypt(encrypted1);
		String actual2 = Security.decrypt(encrypted2);
		assertTrue(OpenmrsUtil.nullSafeEquals(expected, actual1));
		assertTrue(OpenmrsUtil.nullSafeEquals(expected, actual2));
	}
	
	@Test
	public void decrypt_shouldUseIvPrefixedCiphertext() {
		String expected = "iv prefix check";
		String encrypted = Security.encrypt(expected);
		
		byte[] combined = Base64.getDecoder().decode(encrypted);
		assertTrue(combined.length > 1 + 12 + 16);
		assertEquals(1, combined[0]);
		
		byte[] initVector = Arrays.copyOfRange(combined, 1, 13);
		byte[] cipherText = Arrays.copyOfRange(combined, 13, combined.length);
		String cipherOnly = Base64.getEncoder().encodeToString(cipherText);
		
		String actual = Security.decrypt(cipherOnly, initVector, Security.getSavedSecretKey());
		assertTrue(OpenmrsUtil.nullSafeEquals(expected, actual));
	}

	private static String encryptWithGcm(String text, byte[] initVector, byte[] secretKey) {
		try {
			Cipher cipher = Cipher.getInstance(OpenmrsConstants.ENCRYPTION_CIPHER_CONFIGURATION);
			SecretKeySpec secret = new SecretKeySpec(secretKey, OpenmrsConstants.ENCRYPTION_KEY_SPEC);
			GCMParameterSpec spec = new GCMParameterSpec(128, initVector);
			cipher.init(Cipher.ENCRYPT_MODE, secret, spec);
			byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(encrypted);
		}
		catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
}
