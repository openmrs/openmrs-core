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
package org.openmrs.web.order;

import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.ExpressionEvaluator;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Order;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockPageContext;

/**
 * A few web specific tests against the {@link Order} class.
 */
public class OrderTest extends BaseWebContextSensitiveTest {
	
	/**
	 * This test makes sure that the {@link Order#isDiscontinued()} is the one getting called
	 * instead of a method like {@link Order#getDiscontinued()}. The latter was using the date
	 * object and looking at several attributes at one point instead of looking at the boolean
	 * 'discontinued' attribute.
	 * 
	 * @throws ELException
	 */
	@Test
	public void getDiscontinued_shouldGetDiscontinuedProperty() throws ELException {
		Order order = new Order();
		MockPageContext pageCtxt = new MockPageContext();
		pageCtxt.setAttribute("order", order);
		ExpressionEvaluator evtr = pageCtxt.getExpressionEvaluator();
		
		order.setDiscontinued(true);
		Object result = evtr.evaluate("${order.discontinued}", Boolean.class, null, null);
		Assert.assertNotNull(result);
		Assert.assertTrue(result instanceof Boolean);
		Assert.assertTrue((Boolean) result);
		
		order.setDiscontinued(false);
		result = evtr.evaluate("${order.discontinued}", Boolean.class, null, null);
		Assert.assertNotNull(result);
		Assert.assertTrue(result instanceof Boolean);
		Assert.assertFalse((Boolean) result);
	}
	
}
