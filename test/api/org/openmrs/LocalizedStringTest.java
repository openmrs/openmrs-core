package org.openmrs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;
import org.openmrs.test.Verifies;

/**
 * This class tests all of the {@link LocalizedString} non-trivial object methods.
 * 
 * @see LocalizedString
 */
public class LocalizedStringTest {
	
	/**
	 * @see {@link LocalizedString#deserialize(String)}
	 */
	@Test
	@Verifies(value = "should not fail if given s doesnt contains variants", method = "deserialize(String)")
	public void deserialize_shouldNotFailIfGivenSDoesntContainsVariants() throws Exception {
		String s = "Favorite Color";
		LocalizedString expected = new LocalizedString();
		expected.setUnlocalizedValue("Favorite Color");
		LocalizedString actual = LocalizedString.deserialize(s);
		assertNull(actual.getVariants());
		assertEquals(expected, actual);
	}
	
	/**
	 * @see {@link LocalizedString#deserialize(String)}
	 */
	@Test
	@Verifies(value = "should return null if given s is empty", method = "deserialize(String)")
	public void deserialize_shouldReturnNullIfGivenSIsEmpty() throws Exception {
		String s = "";
		assertNull(LocalizedString.deserialize(s));
	}
	
	/**
	 * @see {@link LocalizedString#deserialize(String)}
	 */
	@Test
	@Verifies(value = "should return null if given s is null", method = "deserialize(String)")
	public void deserialize_shouldReturnNullIfGivenSIsNull() throws Exception {
		String s = null;
		assertNull(LocalizedString.deserialize(s));
	}
	
	/**
	 * @see {@link LocalizedString#deserialize(String)}
	 */
	@Test
	@Verifies(value = "should deserialize correctly if given s contains variants", method = "deserialize(String)")
	public void deserialize_shouldDeserializeCorrectlyIfGivenSContainsVariants() throws Exception {
		String s = "Favorite Color^v1^en_UK:Favourite Colour;fr:Couleur pr¨¦f¨¦r¨¦e";
		LocalizedString expected = new LocalizedString();
		expected.setUnlocalizedValue("Favorite Color");
		Map<Locale, String> variants = new HashMap<Locale, String>();
		variants.put(new Locale("en", "UK"), "Favourite Colour");
		variants.put(new Locale("fr"), "Couleur pr¨¦f¨¦r¨¦e");
		expected.setVariants(variants);
		LocalizedString actual = LocalizedString.deserialize(s);
		assertEquals(expected, actual);
	}
	
	/**
	 * @see {@link LocalizedString#serialize(LocalizedString)}
	 */
	@Test
	@Verifies(value = "should not fail if given localizedString hasnt variants", method = "serialize(LocalizedString)")
	public void serialize_shouldNotFailIfGivenLocalizedStringHasntVariants() throws Exception {
		LocalizedString ls = new LocalizedString();
		ls.setUnlocalizedValue("Favorite Color");
		assertNull(ls.getVariants());
		String expected = "Favorite Color";
		String actual = LocalizedString.serialize(ls);
		assertEquals(expected, actual);
	}
	
	/**
	 * @see {@link LocalizedString#serialize(LocalizedString)}
	 */
	@Test
	@Verifies(value = "should return null if given localizedString is null", method = "serialize(LocalizedString)")
	public void serialize_shouldReturnNullIfGivenLocalizedStringIsNull() throws Exception {
		assertNull(LocalizedString.serialize(null));
	}
	
	/**
	 * @see {@link LocalizedString#serialize(LocalizedString)}
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
		String expected = "Favorite Color^v1^en_UK:Favourite Colour;fr:Couleur pr¨¦f¨¦r¨¦e";
		String actual = LocalizedString.serialize(ls);
		assertEquals(expected, actual);
	}
	
	/**
	 * @see {@link LocalizedString#equals(Object)}
	 */
	@Test
	@Verifies(value = "should confirm two new LocalizedString objects are equal", method = "equals(Object)")
	public void equals_shouldConfirmTwoNewLocalizedStringObjectsAreEqual() throws Exception {
		LocalizedString ls = new LocalizedString();
		assertEquals(ls, ls);
	}
	
	/**
	 * @see {@link LocalizedString#equals(Object)}
	 */
	@Test
	@Verifies(value = "should not fail if given obj has null unlocalized value", method = "equals(Object)")
	public void equals_shouldNotFailIfGivenObjHasNullUnlocalizedValue() throws Exception {
		LocalizedString left = new LocalizedString();
		left.setUnlocalizedValue("Favorite Color");
		LocalizedString right = new LocalizedString();
		assertFalse(left.equals(right));
	}
	
	/**
	 * @see {@link LocalizedString#equals(Object)}
	 */
	@Test
	@Verifies(value = "should not fail if given obj has null variants", method = "equals(Object)")
	public void equals_shouldNotFailIfGivenObjHasNullVariants() throws Exception {
		LocalizedString left = new LocalizedString();
		Map<Locale, String> variants = new HashMap<Locale, String>();
		variants.put(new Locale("en", "UK"), "Favourite Colour");
		variants.put(new Locale("fr"), "Couleur pr¨¦f¨¦r¨¦e");
		left.setUnlocalizedValue("Favorite Color");
		left.setVariants(variants);
		
		LocalizedString right = new LocalizedString();
		right.setUnlocalizedValue("Favorite Color");
		
		assertFalse(left.equals(right));
	}
	
	/**
	 * @see {@link LocalizedString#equals(Object)}
	 */
	@Test
	@Verifies(value = "should not fail if given obj is null", method = "equals(Object)")
	public void equals_shouldNotFailIfGivenObjIsNull() throws Exception {
		LocalizedString left = new LocalizedString();
		assertFalse(left.equals(null));
	}
	
	/**
	 * @see {@link LocalizedString#equals(Object)}
	 */
	@Test
	@Verifies(value = "should not fail if unlocalized value is null", method = "equals(Object)")
	public void equals_shouldNotFailIfUnlocalizedValueIsNull() throws Exception {
		LocalizedString left = new LocalizedString();
		LocalizedString right = new LocalizedString();
		right.setUnlocalizedValue("Favorite Color");
		assertFalse(left.equals(right));
	}
	
	/**
	 * @see {@link LocalizedString#equals(Object)}
	 */
	@Test
	@Verifies(value = "should not fail if variants is null", method = "equals(Object)")
	public void equals_shouldNotFailIfVariantsIsNull() throws Exception {
		LocalizedString left = new LocalizedString();
		left.setUnlocalizedValue("Favorite Color");
		
		LocalizedString right = new LocalizedString();
		Map<Locale, String> variants = new HashMap<Locale, String>();
		variants.put(new Locale("en", "UK"), "Favourite Colour");
		variants.put(new Locale("fr"), "Couleur pr¨¦f¨¦r¨¦e");
		right.setUnlocalizedValue("Favorite Color");
		right.setVariants(variants);
		
		assertFalse(left.equals(right));
	}
	
	/**
	 * @see {@link LocalizedString#getValue(Locale)}
	 */
	@Test
	@Verifies(value = "should return a string when variant match locale", method = "getValue(Locale)")
	public void getValue_shouldReturnAStringWhenVariantMatchLocale() throws Exception {
		LocalizedString ls = new LocalizedString();
		ls.setUnlocalizedValue("Favorite Color");
		Map<Locale, String> variants = new HashMap<Locale, String>();
		variants.put(new Locale("en", "UK"), "Favourite Colour");
		variants.put(new Locale("fr"), "Couleur pr¨¦f¨¦r¨¦e");
		ls.setVariants(variants);
		
		String expected = "Favourite Colour";
		String actual = ls.getValue(new Locale("en", "UK"));
		
		assertEquals(expected, actual);
	}
	
	/**
	 * @see {@link LocalizedString#getValue(Locale)}
	 */
	@Test
	@Verifies(value = "should return a string when variant match locale with language only", method = "getValue(Locale)")
	public void getValue_shouldReturnAStringWhenVariantMatchLocaleWithLanguageOnly() throws Exception {
		LocalizedString ls = new LocalizedString();
		ls.setUnlocalizedValue("Favorite Color");
		Map<Locale, String> variants = new HashMap<Locale, String>();
		variants.put(new Locale("en", "UK"), "Favourite Colour");
		variants.put(new Locale("fr"), "Couleur pr¨¦f¨¦r¨¦e");
		ls.setVariants(variants);
		
		String expected = "Couleur pr¨¦f¨¦r¨¦e";
		String actual = ls.getValue(new Locale("fr", "FR"));
		
		assertEquals(expected, actual);
	}
	
	/**
	 * @see {@link LocalizedString#getValue(Locale)}
	 */
	@Test
	@Verifies(value = "should return unlocalized value when no variant matching locale or locale with language only", method = "getValue(Locale)")
	public void getValue_shouldReturnUnlocalizedValueWhenNoVariantMatchingLocaleOrLocaleWithLanguageOnly() throws Exception {
		LocalizedString ls = new LocalizedString();
		ls.setUnlocalizedValue("Favorite Color");
		Map<Locale, String> variants = new HashMap<Locale, String>();
		variants.put(new Locale("en", "UK"), "Favourite Colour");
		variants.put(new Locale("fr"), "Couleur pr¨¦f¨¦r¨¦e");
		ls.setVariants(variants);
		
		String expected = "Favorite Color";
		String actual = ls.getValue(new Locale("it", "IT"));
		
		assertEquals(expected, actual);
	}
	
	/**
	 * @see {@link LocalizedString#getValue(Locale)}
	 */
	@Test
	@Verifies(value = "should return unlocalized value if locale is null", method = "getValue(Locale)")
	public void getValue_shouldReturnUnlocalizedValueIfLocaleIsNull() throws Exception {
		LocalizedString ls = new LocalizedString();
		ls.setUnlocalizedValue("Favorite Color");
		Map<Locale, String> variants = new HashMap<Locale, String>();
		variants.put(new Locale("en", "UK"), "Favourite Colour");
		variants.put(new Locale("fr"), "Couleur pr¨¦f¨¦r¨¦e");
		ls.setVariants(variants);
		
		String expected = "Favorite Color";
		String actual = ls.getValue(null);
		
		assertEquals(expected, actual);
	}
	
}
