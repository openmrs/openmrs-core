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

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
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
	@Test
	public void shouldSerialization() throws Exception {
		executeDataSet("org/openmrs/report/include/RowPerProgramEnrollment.xml");
		
		EvaluationContext evalContext = new EvaluationContext();
		PatientSearch kids = PatientSearch.createFilterSearch(PatientCharacteristicFilter.class);
		kids.addArgument("maxAge", "3", Integer.class);
		Cohort kidsCohort = Context.getCohortService().evaluate(kids, evalContext);
		
		RowPerProgramEnrollmentDataSetDefinition definition = new RowPerProgramEnrollmentDataSetDefinition();
		definition.setName("Row per enrollment");
		//commenting this out because serializing PatientSearches is not yet implemented
		//definition.setFilter(kids);
		Set<Program> programs = new HashSet<Program>();
		programs.add(Context.getProgramWorkflowService().getProgram(1));
		definition.setPrograms(programs);
		
		ReportSchema rs = new ReportSchema();
		rs.setName("Testing row-per-obs");
		rs.setDescription("Tesing RowPerObsDataSet*");
		rs.addDataSetDefinition(definition);
		
		Serializer serializer = OpenmrsUtil.getSerializer();
		StringWriter writer = new StringWriter();
		
		serializer.write(rs, writer);
		//System.out.println("xml =\n" + writer.toString());
		
		rs = (ReportSchema) serializer.read(ReportSchema.class, writer.toString());
		//System.out.println("deserialized as name=" + rs.getName());
		//System.out.println("deserialized with " + rs.getDataSetDefinitions().size() + " data set definitions");
		
		//System.out.println("Evaluating...");
		ReportData data = Context.getReportService().evaluate(rs, kidsCohort, evalContext);
		//System.out.println("Result=");
		//new TsvReportRenderer().render(data, null, System.out);
	}
	
}
