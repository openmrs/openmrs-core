package org.openmrs;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;

/**
 * This class tests all methods that are not getter or setters in the {@link org.openmrs.OrderType} java object
 * this test class for {@link org.openmrs.OrderType}
 *
 * @see org.openmrs.OrderType
 */
public class OrderTypeTest {
	
	/**
	 * @see org.openmrs.OrderType#getJavaClassObject()
	 */
	@Test
	@Verifies(value = "should get java class String as class", method = "getJavaClassObject()")
	public void setJavaClass_shouldGetJavaClassObject() throws Exception {
		//Create a new OrderType
		OrderType orderType = new OrderType();
		
		//Test with Integer class
		Class clazz = Integer.class;
		
		orderType.setJavaClass(clazz.getName());
		Assert.assertEquals(clazz, orderType.getJavaClassObject());
	}
}
