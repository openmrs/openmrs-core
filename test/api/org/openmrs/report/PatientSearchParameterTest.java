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
package org.openmrs.report;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.PatientSearch;
import org.openmrs.reporting.PatientSearchReportObject;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * Tests backwards compatibility with org.openmrs.reporting.* framework 
 * with the new org.openmrs.cohort.*
 *  
 */
public class PatientSearchParameterTest extends BaseContextSensitiveTest {

	private static Log log = LogFactory.getLog(PatientSearchParameterTest.class);
	protected static final String INITIAL_REPORT_OBJECTS_XML = "org/openmrs/report/include/PatientSearchParameterTest.xml";
	     
    /**
	 * Tests {@link PatientSearch#getParameters()} to see if parameter names evaluate to a value
	 * determined in an EvaluationContext
	 * 
	 * 1. Create PatientSearch, add SearchArguments as Parameters, and persist in the database.
	 * 2. Create an EvaluationContext to assign values to the PatientSearch Parameters
	 * 3. Retrieve the PatientSearch from the database and test if its Parameters evaluate
	 * to the EvaluationContext Parameter values.
	 * 
	 * @throws Exception
	 */
    @Test
	public void shouldPatientSearchParameter() throws Exception {
    	executeDataSet(INITIAL_REPORT_OBJECTS_XML);
    	 	
    	// test variables
    	String testName = "Dr. Giffy";
    	String salutation = "Hello";
    	Date today = new Date();
    	// DateFormat used in EvaluationContext
		DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");

    	// create a PatientSearch
    	PatientSearch ps = new PatientSearch();
    	List<Object> parsedComposition = new ArrayList<Object>();
    	parsedComposition.add(Integer.valueOf(1));
    	parsedComposition.add(Integer.valueOf(2));
    	parsedComposition.add(Integer.valueOf(3));
    	ps.setParsedComposition(parsedComposition);

    	 
    	// Either add a PatientSearch Parameter with dollar sign included as below ... or
    	ps.addParameter(new Parameter("${test.name}", "TestName", String.class, null));
    	ps.addParameter(new Parameter("${test.salutation}", "TestSalutation", String.class, null));
    	ps.addParameter(new Parameter("${test.date}", "TestDate", Date.class, null));

    	// ... or add PatientSearch SearchArgument with dollar sign included as below    	  
    	//ps.addArgument("TestName", "${test.name}", String.class);
    	//ps.addArgument("TestSalutation", "${test.salutation}", String.class);
    	//ps.addArgument("TestDate", "${test.date}", String.class);
    	
    	// Save the PatientSearch in the database
		PatientSearchReportObject ro = new PatientSearchReportObject();
		ro.setName("testReportObject1");
		ro.setDescription("OldCohortFrameworkTest testPatientSearchParameter");
		ro.setPatientSearch(ps);
		Integer newId = Context.getReportObjectService().createReportObject(ro);
		
		// Create an EvaluationContext with the test variables as parameters
		EvaluationContext context = new EvaluationContext();
		// EvaluationContext Parameter must *not* use the dollar sign.
		context.addParameterValue(new Parameter("test.name", "TestName", String.class, "WrongName"), testName);
		context.addParameterValue(new Parameter("test.salutation", "TestSalutation", String.class, "WrongSalutation"), salutation);
		context.addParameterValue(new Parameter("test.date", "TestDate", java.util.Date.class, "WrongDate"), today);
		
		// Retrieve the PatientSearch from the database
    	PatientSearch ps2 = ((PatientSearchReportObject) Context.getReportObjectService().getReportObject(newId)).getPatientSearch();

    	// Test if the PatientSearch parameters evaluate to the test variable values
    	List<Parameter> parameters = ps2.getParameters();
    	for (Parameter p : parameters) {
            if ( "TestName".equals(p.getLabel()) ) {
                    assertEquals(testName, context.evaluateExpression(ps2.getArgumentValue(p.getName())).toString());
                    log.info("Testing Parameters: " + context.evaluateExpression(p.getName()).toString() + " " + testName);
            }
            else if ( "TestSalutation".equals(p.getLabel()) ) {
                    assertEquals(salutation, context.evaluateExpression(ps2.getArgumentValue(p.getName())).toString());
                    log.info("Testing Parameters: " + context.evaluateExpression(p.getName()).toString() + " " + salutation);
            }
            else if ( "TestDate".equals(p.getLabel()) ) {
                    assertEquals(df.format(today), df.format((Date)context.evaluateExpression(ps2.getArgumentValue(p.getName()))));
                    log.info("Testing Parameters: " + context.evaluateExpression(p.getName()).toString() + " " + df.format(today));
            }
            else {
                    log.info("Testing Parameters: " + context.evaluateExpression(p.getName()).toString() + " " + p.getName());
            }
    	}
    }
    
}
