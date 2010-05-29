package org.openmrs;


import org.junit.Assert;
import org.junit.Test;
import org.openmrs.serialization.LocalizedStringSerializer;
import org.openmrs.serialization.OpenmrsSerializer;
import org.openmrs.test.Verifies;

/**
 * This class tests the all of the {@link OrderType} non-trivial object methods.
 * 
 * @see OrderType
 */
public class OrderTypeTest {
	
	/**
	 * @see {@link OrderType#getName()}
	 * 
	 */
	@Test
	@Verifies(value = "should return unlocalized name when no localization is added", method = "getName()")
	public void getName_shouldReturnUnlocalizedNameWhenNoLocalizationIsAdded() throws Exception {
		OrderType type = new OrderType();
		String expected = "Lunch Order";
		OpenmrsSerializer serializer = new LocalizedStringSerializer();
		LocalizedString ls = serializer.deserialize(expected, LocalizedString.class);
		type.setLocalizedName(ls);
		Assert.assertEquals(expected, type.getName());
	}
	
	/**
	 * @see {@link OrderType#setName(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should set unlocalized name correctly", method = "setName(String)")
	public void setName_shouldSetUnlocalizedNameCorrectly() throws Exception {
		OrderType type = new OrderType();
		String name = "Lunch Order";
		type.setName(name);
		Assert.assertEquals(name, type.getLocalizedName().getUnlocalizedValue());
	}
}