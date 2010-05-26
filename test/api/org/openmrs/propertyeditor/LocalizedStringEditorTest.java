package org.openmrs.propertyeditor;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.LocalizedString;
import org.openmrs.test.Verifies;

public class LocalizedStringEditorTest {
	
	/**
	 * @see {@link LocalizedStringEditor#getAsText()}
	 * 
	 */
	@Test
	@Verifies(value = "should return a serialized string when editor has a value", method = "getAsText()")
	public void getAsText_shouldReturnASerializedStringWhenEditorHasAValue() throws Exception {
		LocalizedStringEditor editor = new LocalizedStringEditor();
		LocalizedString ls = new LocalizedString();
		ls.setUnlocalizedValue("Favorite Color");
		Map<Locale, String> variants = new LinkedHashMap<Locale, String>();
		variants.put(new Locale("en", "UK"), "Favourite Colour");
		variants.put(new Locale("fr"), "Couleur pr¨¦f¨¦r¨¦e");
		ls.setVariants(variants);
		editor.setValue(ls);
		Assert.assertEquals("i18n:v1;unlocalized:Favorite Color;en_UK:Favourite Colour;fr:Couleur pr¨¦f¨¦r¨¦e;", editor
		        .getAsText());
	}
	
	/**
	 * @see {@link LocalizedStringEditor#getAsText()}
	 * 
	 */
	@Test
	@Verifies(value = "should return empty string when editor has a null value", method = "getAsText()")
	public void getAsText_shouldReturnEmptyStringWhenEditorHasANullValue() throws Exception {
		LocalizedStringEditor editor = new LocalizedStringEditor();
		editor.setValue(null);
		Assert.assertEquals("", editor.getAsText());
	}
	
	
	/**
	 * @see {@link LocalizedStringEditor#setAsText(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should set value to null if given empty string", method = "setAsText(String)")
	public void setAsText_shouldSetValueToNullIfGivenEmptyString() throws Exception {
		LocalizedStringEditor editor = new LocalizedStringEditor();
		editor.setAsText("");
		Assert.assertNull(editor.getValue());
	}
	
	/**
	 * @see {@link LocalizedStringEditor#setAsText(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should set value to null if given null value", method = "setAsText(String)")
	public void setAsText_shouldSetValueToNullIfGivenNullValue() throws Exception {
		LocalizedStringEditor editor = new LocalizedStringEditor();
		editor.setAsText(null);
		Assert.assertNull(editor.getValue());
	}

	/**
     * @see {@link LocalizedStringEditor#setAsText(String)}
     * 
     */
    @Test
    @Verifies(value = "should set value to the localized string object with the specified string", method = "setAsText(String)")
    public void setAsText_shouldSetValueToTheLocalizedStringObjectWithTheSpecifiedString() throws Exception {
    	LocalizedStringEditor editor = new LocalizedStringEditor();
		String text = "i18n:v1;unlocalized:Favorite Color;en_UK:Favourite Colour;fr:Couleur pr¨¦f¨¦r¨¦e;";
		editor.setAsText(text);
		LocalizedString actual = (LocalizedString) editor.getValue();
		
		Assert.assertEquals("Favorite Color", actual.getUnlocalizedValue());
		Assert.assertSame(2, actual.getVariants().entrySet().size());
		Iterator<Map.Entry<Locale, String>> it = actual.getVariants().entrySet().iterator();
		Map.Entry<Locale, String> entry = it.next();
		Assert.assertEquals(new Locale("fr"), entry.getKey());
		Assert.assertEquals("Couleur pr¨¦f¨¦r¨¦e", entry.getValue());
		entry = it.next();
		Assert.assertEquals(new Locale("en", "UK"), entry.getKey());
		Assert.assertEquals("Favourite Colour", entry.getValue());
    }
}