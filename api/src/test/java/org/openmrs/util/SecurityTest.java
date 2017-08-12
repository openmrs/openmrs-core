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

import java.util.Base64;
import java.util.Base64.Decoder;

import org.junit.Assert;
import org.junit.Test;
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
		Assert.assertEquals(HASH_LENGTH, hash.length());
	}
	
	/**
	 * @see Security#encodeString(String)
	 */
	@Test
	public void encodeString_shouldEncodeStringsToXCharactersWithXCharactersSalt() {
		String hash = Security.encodeString("test" + Security.getRandomToken());
		Assert.assertEquals(HASH_LENGTH, hash.length());
	}
	
	/**
	 * @see Security#hashMatches(String,String)
	 */
	@Test
	public void hashMatches_shouldMatchStringsHashedWithSha1Algorithm() {
		Assert.assertTrue(Security.hashMatches("4a1750c8607d0fa237de36c6305715c223415189", "test"
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
		Assert.assertTrue(Security.hashMatches(password, passwordToHash));
	}
	
	/**
	 * @see Security#hashMatches(String,String)
	 */
	@Test
	public void hashMatches_shouldMatchStringsHashedWithIncorrectSha1Algorithm() {
		Assert.assertTrue(Security.hashMatches("4a1750c8607dfa237de36c6305715c223415189", "test"
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
		String encrypted = "GnMz8qETyKMv+edLpYqWfBhR+lX6JlkocNGwHhmhXSY=";
		String actual = Security.decrypt(encrypted, initVector, secretKey);
		Assert.assertTrue(OpenmrsUtil.nullSafeEquals(expected, actual));
		
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
		
		encrypted = "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"
		        + "t9mHrJ+FI181rG5bdf62ZsSuziuQ==";
		
		actual = Security.decrypt(encrypted, initVector, secretKey);
		Assert.assertTrue(OpenmrsUtil.nullSafeEquals(expected, actual));
	}
	
	/**
	 * @see Security#encrypt(String)
	 */
	@Test
	public void encrypt_shouldEncryptShortAndLongText() {
		// small text
		String expected = "a";
		String encrypted = Security.encrypt(expected);
		Assert.assertTrue(StringUtils.hasText(encrypted));
		String actual = Security.decrypt(encrypted);
		Assert.assertTrue(OpenmrsUtil.nullSafeEquals(expected, actual));
		
		// long text
		expected = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus porta sapien ac nisi imperdiet posuere. Maecenas nec felis ac enim posuere semper. In arcu turpis, elementum nec auctor id, pretium sed tortor. Quisque sit amet erat ante. Praesent metus dui, porttitor non volutpat eu, porta sed ante. Fusce quis dignissim nisl. Vivamus id massa in nisl sollicitudin iaculis ac ut odio. Morbi et sapien non massa ultricies commodo. Nunc semper, nulla a pellentesque adipiscing, urna nisl vulputate lacus, non rutrum nulla mauris at tortor. Quisque molestie, velit nec vehicula tempor, mi eros fermentum ipsum, ut ullamcorper nisl sem at risus. Nam varius nunc sit amet velit blandit gravida sed vel purus. Nam ac justo ut metus elementum vehicula ac non ante. Aliquam pellentesque semper mauris ut pulvinar."
		        + "Duis et orci nisi. Mauris tempor consequat felis, vel consequat diam consequat vitae. Donec eget dolor quis nulla lobortis vestibulum. Quisque vel ipsum in sapien egestas blandit. Praesent malesuada tellus nec sapien blandit sit amet molestie magna consequat. Pellentesque quis tempus urna. Quisque ut nibh ut tellus hendrerit rhoncus. Aenean ultricies lorem eu sem condimentum at consectetur magna dignissim. Nam porta lobortis consequat. Suspendisse congue, tellus quis sodales blandit, augue massa interdum sem, vel suscipit ipsum risus vitae massa. Quisque ipsum tellus, gravida sed suscipit non, ultricies eu augue. Etiam consequat consequat massa a accumsan. Quisque rhoncus nisi lectus, vel ultrices sapien. Aenean a felis felis, sit amet vestibulum lorem. Cras ut fermentum magna."
		        + "Quisque vel erat eget eros bibendum convallis vitae a augue. Maecenas posuere ullamcorper quam, ac ullamcorper eros egestas at. Nulla ipsum purus, venenatis ac dignissim in, bibendum eget enim. Nulla rhoncus dui eu augue egestas in tempus augue congue. Suspendisse potenti. Aenean faucibus felis ut leo venenatis congue lacinia felis tempor. Phasellus fermentum nisl venenatis quam molestie fermentum euismod metus pretium. Duis facilisis pharetra vehicula. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Donec aliquet feugiat sapien, vitae tristique nisl lacinia non. Fusce eros dolor, egestas et auctor vel, aliquam ac lorem. In lacinia cursus pretium."
		        + "Nulla vitae nisi vitae magna varius posuere. Curabitur non dui at odio scelerisque mattis a a risus. Suspendisse augue lacus, pulvinar vitae fringilla tempor, adipiscing vel velit. Suspendisse lorem dui, eleifend vel rhoncus ac, porta sed odio. Maecenas eget pellentesque ligula. Cras vitae auctor justo. Duis at massa vitae risus semper elementum. Proin at magna et augue volutpat tincidunt nec sed erat. Quisque id sapien tortor, ut gravida erat. Vivamus dictum, enim non sodales laoreet, ante libero suscipit erat, ac tristique purus eros sed augue. Quisque magna mi, varius ac accumsan aliquam, aliquam id risus. Phasellus dignissim dictum massa, ac consequat risus venenatis in. Morbi imperdiet bibendum sem, eu mollis urna aliquet a. In ac augue vitae ante ultrices sollicitudin vel sed elit. Nunc fringilla vestibulum egestas. Duis risus lorem, varius a vulputate at, blandit vel lectus. Sed mollis, ipsum nec fringilla accumsan, risus nibh iaculis ligula, non tristique nibh tortor vitae sem. Nulla facilisi. In id lectus vitae felis elementum lobortis. Aenean et nisi orci."
		        + "Nam mi lorem, posuere non auctor sed, accumsan eu magna. Fusce sit amet tellus augue. Nunc eleifend, justo id pharetra hendrerit, urna augue ultricies mi, sed fringilla arcu libero quis nulla. Maecenas tristique auctor cursus. Curabitur venenatis lacus non leo aliquet ornare. Praesent justo turpis, dictum eu dictum convallis, faucibus sit amet erat. Praesent sed dui id enim euismod interdum. Integer sed fermentum neque. Curabitur enim nunc, euismod adipiscing iaculis eget, tincidunt vel nunc. Nullam at neque sem, rutrum aliquet elit. In et velit enim, tempus mollis nunc. Sed sit amet quam justo. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur convallis dolor non ligula fermentum imperdiet.";
		encrypted = Security.encrypt(expected);
		Assert.assertTrue(StringUtils.hasText(encrypted));
		actual = Security.decrypt(encrypted);
		Assert.assertTrue(OpenmrsUtil.nullSafeEquals(expected, actual));
		
		// foreign text
		expected = "傑里米 (Jeremy), 潔儀 (Kitty) and 贏 (Win) like encryption :-D";
		encrypted = Security.encrypt(expected);
		Assert.assertTrue(StringUtils.hasText(encrypted));
		actual = Security.decrypt(encrypted);
		Assert.assertTrue(OpenmrsUtil.nullSafeEquals(expected, actual));
	}
	
}
