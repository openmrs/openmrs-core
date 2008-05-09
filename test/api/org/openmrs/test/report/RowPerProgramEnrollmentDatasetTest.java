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

import java.io.StringWriter;

import org.openmrs.Cohort;
import org.openmrs.api.DataSetService;
import org.openmrs.api.context.Context;
import org.openmrs.report.EvaluationContext;
import org.openmrs.report.ReportData;
import org.openmrs.report.ReportSchema;
import org.openmrs.report.RowPerProgramEnrollmentDataSetDefinition;
import org.openmrs.report.impl.TsvReportRenderer;
import org.openmrs.reporting.PatientCharacteristicFilter;
import org.openmrs.reporting.PatientSearch;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsUtil;
import org.simpleframework.xml.Serializer;

/**
 * TODO: create a test database and test against that
 */
public class RowPerProgramEnrollmentDatasetTest extends BaseContextSensitiveTest {
    
    /**
     * TODO: fix this so it uses asserts instead of printing to stdout
     * 
     * @throws Exception
     */
    public void testSerialization() throws Exception {
    	initializeInMemoryDatabase();
		authenticate();
		
		EvaluationContext evalContext = new EvaluationContext();
		DataSetService service = Context.getDataSetService();
		PatientSearch kids = PatientSearch.createFilterSearch(PatientCharacteristicFilter.class);
		kids.addArgument("maxAge", "3", Integer.class);
		Cohort kidsCohort = Context.getCohortService().evaluate(kids, evalContext);

		RowPerProgramEnrollmentDataSetDefinition definition = new RowPerProgramEnrollmentDataSetDefinition();
		definition.setName("Row per enrollment");
		//commenting this out because serializing PatientSearches is not yet implemented
		//definition.setFilter(kids);
		definition.getPrograms().add(Context.getProgramWorkflowService().getProgram(1));
		
		ReportSchema rs = new ReportSchema();
		rs.setName("Testing row-per-obs");
		rs.setDescription("Tesing RowPerObsDataSet*");
		rs.addDataSetDefinition(definition);
		
		Serializer serializer = OpenmrsUtil.getSerializer();
		StringWriter writer = new StringWriter();
		
		serializer.write(rs, writer);
		System.out.println("xml =\n" + writer.toString());
		
		rs = (ReportSchema) serializer.read(ReportSchema.class, writer.toString());
		System.out.println("deserialized as name=" + rs.getName());
		System.out.println("deserialized with " + rs.getDataSetDefinitions().size() + " data set definitions");
		
		System.out.println("Evaluating...");
		ReportData data = Context.getReportService().evaluate(rs, kidsCohort, evalContext);
		System.out.println("Result=");
		new TsvReportRenderer().render(data, null, System.out);
    }
    
}
