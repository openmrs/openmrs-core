package org.openmrs;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;

/**
 * This class tests the all of the {@link FieldType} non-trivial object methods.
 * 
 * @see FieldType
 */
public class FieldTypeTest {
	
	/**
	 * @see {@link FieldType#getName()}
	 */
	@Test
	@Verifies(value = "should return unlocalized name when no localization is added", method = "getName()")
	public void getName_shouldReturnUnlocalizedNameWhenNoLocalizationIsAdded() throws Exception {
		FieldType type = new FieldType();
		String expected = "Test";
		LocalizedString ls = LocalizedString.valueOf(expected);
		type.setLocalizedName(ls);
		Assert.assertEquals(expected, type.getName());
	}
	
	/**
	 * @see {@link FieldType#setName(String)}
	 */
	@Test
	@Verifies(value = "should set unlocalized name correctly", method = "setName(String)")
	public void setName_shouldSetUnlocalizedNameCorrectly() throws Exception {
		FieldType type = new FieldType();
		String name = "Test";
		type.setName(name);
		Assert.assertEquals(name, type.getLocalizedName().getUnlocalizedValue());
	}
}
