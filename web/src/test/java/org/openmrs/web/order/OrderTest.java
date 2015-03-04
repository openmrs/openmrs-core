/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
