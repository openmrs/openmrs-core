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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Order;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

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
	 * @see OrderDAO#getHighestOrderId()
	 * @verifies return the highest order id
	 */
	@Test
	public void getHighestOrderId_shouldReturnTheHighestOrderId() throws Exception {
		//call the method twice and ensure that the sequence works as expected
		Assert.assertEquals(12, dao.getHighestOrderId().intValue());
		Assert.assertEquals(12, dao.getHighestOrderId().intValue());
	}

	/**
     * @see OrderDAO#isActivatedInDatabase(Order)
     * @verifies return value from database ignoring session
     */
    @Test
    public void isActivatedInDatabase_shouldReturnValueFromDatabaseIgnoringSession() throws Exception {
	    Order o = Context.getOrderService().getOrder(1);
	    Assert.assertTrue(o.isActivated());
	    o.setActivatedBy(null);
	    o.setDateActivated(null);
	    
	    Assert.assertTrue(dao.isActivatedInDatabase(o));
    }
	
}
