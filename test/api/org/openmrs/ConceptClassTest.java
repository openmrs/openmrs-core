package org.openmrs;


import org.junit.Assert;
import org.junit.Test;
import org.openmrs.serialization.LocalizedStringSerializer;
import org.openmrs.serialization.OpenmrsSerializer;
import org.openmrs.test.Verifies;

/**
 * This class tests the all of the {@link ConceptClass} non-trivial object methods.
 * 
 * @see ConceptClass
 */
public class ConceptClassTest {
	
	/**
	 * @see {@link ConceptClass#getName()}
	 * 
	 */
	@Test
	@Verifies(value = "should return unlocalized name when no localization is added", method = "getName()")
	public void getName_shouldReturnUnlocalizedNameWhenNoLocalizationIsAdded() throws Exception {
		ConceptClass classes = new ConceptClass();
		String expected = "Test";
		OpenmrsSerializer serializer = new LocalizedStringSerializer();
		LocalizedString ls = serializer.deserialize(expected, LocalizedString.class);
		classes.setLocalizedName(ls);
		Assert.assertEquals(expected, classes.getName());
	}
	
	/**
	 * @see {@link ConceptClass#setName(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should set unlocalized name correctly", method = "setName(String)")
	public void setName_shouldSetUnlocalizedNameCorrectly() throws Exception {
		ConceptClass classes = new ConceptClass();
		String name = "Test";
		classes.setName(name);
		Assert.assertEquals(name, classes.getLocalizedName().getUnlocalizedValue());
	}
}