package org.openmrs.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;
import org.openmrs.LocalizedString;
import org.openmrs.test.Verifies;

public class LocalizedStringUtilTest {
	
	/**
	 * @see {@link LocalizedStringUtil#deserialize(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should deserialize correctly if given s contains variants", method = "deserialize(String)")
	public void deserialize_shouldDeserializeCorrectlyIfGivenSContainsVariants() throws Exception {
		String s = "i18n:v1;unlocalized:Favorite Color;en_UK:Favourite Colour;fr:Couleur pr¨¦f¨¦r¨¦e;";
		LocalizedString expected = new LocalizedString();
		expected.setUnlocalizedValue("Favorite Color");
		Map<Locale, String> variants = new HashMap<Locale, String>();
		variants.put(new Locale("en", "UK"), "Favourite Colour");
		variants.put(new Locale("fr"), "Couleur pr¨¦f¨¦r¨¦e");
		expected.setVariants(variants);
		LocalizedString actual = LocalizedStringUtil.deserialize(s);
		assertEquals(expected, actual);
	}
	
	/**
	 * @see {@link LocalizedStringUtil#deserialize(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should not fail if given s doesnt contains variants", method = "deserialize(String)")
	public void deserialize_shouldNotFailIfGivenSDoesntContainsVariants() throws Exception {
		String s = "Favorite Color";
		LocalizedString actual = LocalizedStringUtil.deserialize(s);
		assertNull(actual.getVariants());
		LocalizedString expected = new LocalizedString();
		expected.setUnlocalizedValue("Favorite Color");
		assertEquals(expected, actual);
	}
	
	/**
	 * @see {@link LocalizedStringUtil#deserialize(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should return null if given s is empty", method = "deserialize(String)")
	public void deserialize_shouldReturnNullIfGivenSIsEmpty() throws Exception {
		String s = "";
		assertNull(LocalizedStringUtil.deserialize(s));
	}
	
	/**
	 * @see {@link LocalizedStringUtil#deserialize(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should return null if given s is null", method = "deserialize(String)")
	public void deserialize_shouldReturnNullIfGivenSIsNull() throws Exception {
		String s = null;
		assertNull(LocalizedStringUtil.deserialize(s));
	}
	
	/**
	 * @see {@link LocalizedStringUtil#deserialize(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should unescape correctly if given s contains escaped delimiter", method = "deserialize(String)")
	public void deserialize_shouldUnescapeCorrectlyIfGivenSContainsEscapedDelimiter() throws Exception {
		String s = "i18n:v1;unlocalized:Favorite \\: Color;en_UK:Favourite Colour;fr:Couleur \\; pr¨¦f¨¦r¨¦e;";
		LocalizedString actual = LocalizedStringUtil.deserialize(s);
		assertEquals("Favorite : Color", actual.getUnlocalizedValue());
		assertSame(2, actual.getVariants().size());
		assertEquals("Couleur ; pr¨¦f¨¦r¨¦e", actual.getVariants().get(new Locale("fr")));
	}
	
	/**
	 * @see {@link LocalizedStringUtil#serialize(LocalizedString)}
	 * 
	 */
	@Test
	@Verifies(value = "should escape correctly if given localizedString has a name including delimiter", method = "serialize(LocalizedString)")
	public void serialize_shouldEscapeCorrectlyIfGivenLocalizedStringHasANameIncludingDelimiter() throws Exception {
		LocalizedString ls = new LocalizedString();
		ls.setUnlocalizedValue("Favorite : Color");
		Map<Locale, String> variants = new LinkedHashMap<Locale, String>();
		variants.put(new Locale("en", "UK"), "Favourite Colour");
		variants.put(new Locale("fr"), "Couleur ; pr¨¦f¨¦r¨¦e");
		ls.setVariants(variants);
		String expected = "i18n:v1;unlocalized:Favorite \\: Color;en_UK:Favourite Colour;fr:Couleur \\; pr¨¦f¨¦r¨¦e;";
		String actual = LocalizedStringUtil.serialize(ls);
		assertEquals(expected, actual);
	}
	
	/**
	 * @see {@link LocalizedStringUtil#serialize(LocalizedString)}
	 * 
	 */
	@Test
	@Verifies(value = "should not fail if given localizedString hasnt variants", method = "serialize(LocalizedString)")
	public void serialize_shouldNotFailIfGivenLocalizedStringHasntVariants() throws Exception {
		LocalizedString ls = new LocalizedString();
		ls.setUnlocalizedValue("Favorite Color");
		assertNull(ls.getVariants());
		String expected = "Favorite Color";
		String actual = LocalizedStringUtil.serialize(ls);
		assertEquals(expected, actual);
	}
	
	/**
	 * @see {@link LocalizedStringUtil#serialize(LocalizedString)}
	 * 
	 */
	@Test
	@Verifies(value = "should return null if given localizedString is null", method = "serialize(LocalizedString)")
	public void serialize_shouldReturnNullIfGivenLocalizedStringIsNull() throws Exception {
		assertNull(LocalizedStringUtil.serialize(null));
	}
	
	/**
	 * @see {@link LocalizedStringUtil#serialize(LocalizedString)}
	 * 
	 */
	@Test
	@Verifies(value = "should serialize correctly if given localizedString has variants", method = "serialize(LocalizedString)")
	public void serialize_shouldSerializeCorrectlyIfGivenLocalizedStringHasVariants() throws Exception {
		LocalizedString ls = new LocalizedString();
		ls.setUnlocalizedValue("Favorite Color");
		Map<Locale, String> variants = new LinkedHashMap<Locale, String>();
		variants.put(new Locale("en", "UK"), "Favourite Colour");
		variants.put(new Locale("fr"), "Couleur pr¨¦f¨¦r¨¦e");
		ls.setVariants(variants);
		String expected = "i18n:v1;unlocalized:Favorite Color;en_UK:Favourite Colour;fr:Couleur pr¨¦f¨¦r¨¦e;";
		String actual = LocalizedStringUtil.serialize(ls);
		assertEquals(expected, actual);
	}
}