package org.openmrs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashMap;
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
		variants.put(new Locale("fr"), "Couleur pr__e");
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
		variants.put(new Locale("fr"), "Couleur pr__e");
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
		variants.put(new Locale("fr"), "Couleur pr__e");
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
		variants.put(new Locale("fr"), "Couleur pr__e");
		ls.setVariants(variants);
		
		String expected = "Couleur pr__e";
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
		variants.put(new Locale("fr"), "Couleur pr__e");
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
		variants.put(new Locale("fr"), "Couleur pr__e");
		ls.setVariants(variants);
		
		String expected = "Favorite Color";
		String actual = ls.getValue(null);
		
		assertEquals(expected, actual);
	}
	
}
