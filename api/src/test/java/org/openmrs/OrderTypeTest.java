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
	 * @see org.openmrs.OrderType#setJavaClass(Class)
	 */
	@Test
	@Verifies(value = "should set java class as String", method = "setJavaClass(Class)")
	public void setJavaClass_shouldSetJavaClassAsString() throws Exception {
		//Create a new OrderType
		OrderType orderType = new OrderType();
		
		//Test with Integer class
		Class clazz = Integer.class;
		
		orderType.setJavaClass(clazz);
		Assert.assertEquals("java.lang.Integer", orderType.getJavaClass());
	}
	
	/**
	 * @see OrderType#getJavaClassAsClass()
	 */
	@Test
	@Verifies(value = "should get java class String as class", method = "getJavaClassAsClass()")
	public void setJavaClass_shouldGetJavaClassStringAsClass() throws Exception {
		//Create a new OrderType
		OrderType orderType = new OrderType();
		
		//Test with Integer class
		Class clazz = Integer.class;
		
		orderType.setJavaClass(clazz.getName());
		Assert.assertEquals(clazz, orderType.getJavaClassAsClass());
	}
}
