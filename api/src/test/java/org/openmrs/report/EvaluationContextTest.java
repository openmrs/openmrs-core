/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

/**
 *
 */
public class EvaluationContextTest {
	
	@Test
	public void shouldEvaluateExpression() throws Exception {
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		Date startingDate = df.parse("2007-01-10 10:30:17");
		
		String expression0 = "${report.d1}";
		String expression1 = "${report.d1-15d}";
		String expression2 = "${report.d1+3w}";
		String expression3 = "${report.d1-12m}";
		String expression4 = "${report.d1-1y}";
		String expression5 = "${report.d1+37d}";
		String expression6 = "${report.d1-10w}";
		String expression7 = "${report.doesNotExist}";
		String expression8 = "${report.gender}";
		String expression9 = "report.gender";
		String expression10 = "hello ${report.gender} person";
		String expression11 = "This report is from ${report.d1} to ${report.d1+3w} for ${report.gender} patients";
		
		EvaluationContext context = new EvaluationContext();
		context.addParameterValue(new Parameter("report.d1", "", Date.class, ""), startingDate);
		context.addParameterValue(new Parameter("report.gender", "", String.class, ""), "male");
		
		assertEquals(context.evaluateExpression(expression0), df.parse("2007-01-10 10:30:17"));
		assertEquals(context.evaluateExpression(expression1), df.parse("2006-12-26 10:30:17"));
		assertEquals(context.evaluateExpression(expression2), df.parse("2007-01-31 10:30:17"));
		assertEquals(context.evaluateExpression(expression3), df.parse("2006-01-10 10:30:17"));
		assertEquals(context.evaluateExpression(expression4), df.parse("2006-01-10 10:30:17"));
		assertEquals(context.evaluateExpression(expression5), df.parse("2007-02-16 10:30:17"));
		assertEquals(context.evaluateExpression(expression6), df.parse("2006-11-01 10:30:17"));
		assertNotSame(context.evaluateExpression(expression6), df.parse("2006-11-01 00:00:00"));
		
		try {
			context.evaluateExpression(expression7);
			fail("Missing required parameters should throw an exception");
		}
		catch (Exception e) {
			assertEquals(e.getClass(), ParameterException.class);
		}
		
		assertEquals(context.evaluateExpression(expression8).toString(), "male");
		assertEquals(context.evaluateExpression(expression9).toString(), "report.gender");
		assertEquals(context.evaluateExpression(expression10).toString(), "hello male person");
		assertEquals(context.evaluateExpression(expression11).toString(),
		    "This report is from 2007-01-10 10:30:17 to 2007-01-31 10:30:17 for male patients");
	}
	
}
