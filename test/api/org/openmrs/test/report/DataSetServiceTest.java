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
package org.openmrs.test.report;

import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.api.DataSetService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.result.Result;
import org.openmrs.logic.util.LogicCriteriaBuilder;
import org.openmrs.report.CohortDataSetDefinition;
import org.openmrs.report.DataSet;
import org.openmrs.report.EvaluationContext;
import org.openmrs.report.RowPerObsDataSetDefinition;
import org.openmrs.reporting.PatientCharacteristicFilter;
import org.openmrs.reporting.PatientSearch;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * Test class that tries to run a portion of the 
 */
public class DataSetServiceTest extends BaseContextSensitiveTest {

	private static Log log = LogFactory.getLog(DataSetServiceTest.class);
	
   	/**
   	 * 
   	 */
   	String [] expressions = {
			// "TOKEN(CD4 COUNT).AND()", 
			"TOKEN(CD4 COUNT).ASOF(30/10/2007)", 
			"TOKEN(CD4 COUNT).AFTER(20/10/2007)", 
			"TOKEN(CD4 COUNT).BEFORE(20/10/2007)", 
			"TOKEN(CD4 COUNT).CONTAINS(something)", 
			"TOKEN(CD4 COUNT).EQUALS(something)", 
			"TOKEN(CD4 COUNT).EXISTS()", 
			"TOKEN(CD4 COUNT).FIRST()", 
			"TOKEN(CD4 COUNT).GT(something)", 
			"TOKEN(CD4 COUNT).LAST()", 
			"TOKEN(CD4 COUNT).LT(something)", 
			//"TOKEN(CD4 COUNT).NOT()", 
			//"TOKEN(CD4 COUNT).OR()", 
			"TOKEN(CD4 COUNT).WITHIN()"
   	};
    
    /**
     * Auto generated method comment
     * 
     * @throws Exception
     */
    public void testShouldLogicCriteriaBuilder() throws Exception { 
    	
		for(int i=0; i<expressions.length; i++) { 	
			log.info("Expression: " + expressions[i]);
			String expression = expressions[i];			
			String [] criterion = expression.split("\\.");			
			String token = criterion[0];
			String operatorOperand = criterion[1];			
			log.info("Token: " + token);
			log.info("Operand: " + LogicCriteriaBuilder.extractOperand(operatorOperand));			
		}
    }
    
    
    /**
     * Auto generated method comment
     * 
     * @throws Exception
     */
    public void testShouldSerialize() throws Exception { 
    	LogicCriteriaBuilder.serialize("TOKEN(CD4 COUNT).AFTER(04/12/2006).LAST()");
    	
    }
    
    /**
     * Auto generated method comment
     * 
     * @throws Exception
     */
    public void testShouldDeserialize() throws Exception { 
    	
    	LogicCriteria criteria = new LogicCriteria("CD4 COUNT");
    	criteria.after(new Date()).last();
    	
    	log.info("Deserialized: " + LogicCriteriaBuilder.deserialize(criteria));
    }
    
    /**
     * 
     * Auto generated method comment
     * 
     * @throws Exception
    public void testShouldLogicCriteriaParser() throws Exception { 
    	authenticate();
    	//LogicCriteria lc = new LogicCriteria("CD4 COUNT").before(new Date());
    	
    	String [] criterias = { 
			//"TOKEN(CD4 COUNT).AND()", 
			"TOKEN(CD4 COUNT).ASOF(10/30/2007)", 
			"TOKEN(CD4 COUNT).AFTER(10/20/2007)", 
			"TOKEN(CD4 COUNT).BEFORE(10/30/2007)", 
			"TOKEN(CD4 COUNT).CONTAINS(something)", 
			"TOKEN(CD4 COUNT).EQUALS(something)", 
			"TOKEN(CD4 COUNT).EXISTS()", 
			"TOKEN(CD4 COUNT).FIRST()", 
			"TOKEN(CD4 COUNT).GT(something)", 
			"TOKEN(CD4 COUNT).LAST()", 
			"TOKEN(CD4 COUNT).LT(something)", 
			//"TOKEN(CD4 COUNT).NOT()", 
			//"TOKEN(CD4 COUNT).OR()", 
			"TOKEN(CD4 COUNT).WITHIN()"		
    	};
    	
    	LogicCriteria criteria = new LogicCriteria("CD4 COUNT");
    	
    	
    	
    	// Cannot handle AND, OR, NOT
    	for (int i=0; i<criterias.length; i++) { 
    		LogicCriteriaParser.serialize(criterias[i]);
    	}
    
    	
    	
    }
     */

    /*
	public void testShouldDataSetService() throws Exception {
		authenticate();
		
		log.error("Testing");
		
		PatientSet patients = getPatients();
		LogicContext context = new LogicContext(patients);
		
		
		
		LogicCriteria criteria = new LogicCriteria("PROGRAM ENROLLMENT");
		LogicDataSource dataSource = new ProgramDataSource();
		Map<Integer, Result> results = dataSource.read(context, patients, criteria);
		
		printResults(results);
		
		
		
		
		
	}*/
	
	
	/**
	 * Auto generated method comment
	 * 
	 * @param results
	 */
	public void printResults(Map<Integer, Result> results) { 
		
		log.error("Results: " + results);
		
		for(Integer id : results.keySet()) { 
			Result result = results.get(id);
			log.info("Id: " + id + " " + result.get(id) + " " + result.getDatatype());
		}		
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @return
	 */
	public Cohort getPatients() { 
		Cohort patients = new Cohort();
		
		patients.addMember(new Integer(13015));
		patients.addMember(new Integer(13648));
//		patients.add(new Integer(13863));
//		patients.add(new Integer(13932));
//		patients.add(new Integer(13968));
//		patients.add(new Integer(14449));
//		patients.add(new Integer(14698));
//		patients.add(new Integer(14755));
//		patients.add(new Integer(14804));
//		patients.add(new Integer(15919));
		
		return patients;
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @throws Exception
	 */
	public void testShouldMultipleDataSets() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/test/report/include/ReportTests-patients.xml");
		authenticate();
		EvaluationContext evalContext = new EvaluationContext();
		DataSetService service = Context.getDataSetService();
		PatientSearch kids = PatientSearch.createFilterSearch(PatientCharacteristicFilter.class);
		// TODO: fix this so that it won't fail in 10 years
		kids.addArgument("maxAge", "10", Integer.class);

		CohortDataSetDefinition def1 = new CohortDataSetDefinition();
		def1.setName("Cohorts");
		def1.addStrategy("kids", kids);
		DataSet<Object> data1 = service.evaluate(def1, null, evalContext);
		System.out.println("---Males---");
		for (Map<String, Object> row : data1) {
			for (Map.Entry<String, Object> e : row.entrySet())
				System.out.println(e.getKey() + " -> " + e.getValue());
			System.out.println();
		}
		
		RowPerObsDataSetDefinition def2 = new RowPerObsDataSetDefinition();
		def2.setFilter(kids);
		def2.getQuestions().add(Context.getConceptService().getConcept(5089));
		DataSet<Object> data2 = service.evaluate(def2, null, evalContext);
		int count = 0;
		for (Map<String, Object> row : data2) {
			++count;
			if (count > 0)
				continue;
			for (Map.Entry<String, Object> e : row.entrySet())
				System.out.println(e.getKey() + " -> " + e.getValue());
			System.out.println();
		}
		System.out.println("count = " + count);
	}
	
}
