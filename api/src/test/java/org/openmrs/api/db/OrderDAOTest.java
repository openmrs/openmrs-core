/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api.db;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsConstants;

/**
 * Contains specific to the Order DAO layer
 */
public class OrderDAOTest extends BaseContextSensitiveTest {
	
	private OrderDAO dao = null;
	
	/**
	 * Run this before each unit test in this class.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		
		if (dao == null)
			dao = (OrderDAO) applicationContext.getBean("orderDAO");
	}
	
	/**
	 * @see {@link OrderDAO#getNewOrderNumber()}
	 */
	@Test
	@Verifies(value = "should return the next available order number", method = "getNewOrderNumber()")
	public void getNewOrderNumber_shouldReturnTheNextAvailableOrderNumber() throws Exception {
		//call the method twice and ensure that the sequence works as expected
		String orderNumber1 = dao.getNewOrderNumber();
		Integer nextNumber = Integer.valueOf(orderNumber1.substring(OpenmrsConstants.ORDER_NUMBER_DEFAULT_PREFIX.length())) + 1;
		String expectedOrderNumber = OpenmrsConstants.ORDER_NUMBER_DEFAULT_PREFIX.concat(nextNumber.toString());
		
		Assert.assertEquals(expectedOrderNumber, dao.getNewOrderNumber());
	}
	
	/**
	 * @see {@link OrderDAO#getNewOrderNumber()}
	 */
	@Test
	@Verifies(value = "should always return unique orderNumbers when called multiple times without saving orders", method = "getNewOrderNumber()")
	public void getNewOrderNumber_shouldAlwaysReturnUniqueOrderNumbersWhenCalledMultipleTimesWithoutSavingOrders()
	        throws Exception {
		Set<String> uniqueOrderNumbers = new HashSet<String>(50);
		for (int i = 0; i < 50; i++) {
			uniqueOrderNumbers.add(dao.getNewOrderNumber());
		}
		//since we used a set we should have the size as 50 indicating that there were no duplicates
		Assert.assertEquals(50, uniqueOrderNumbers.size());
	}
}
