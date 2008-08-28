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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Program;
import org.openmrs.api.CohortService;
import org.openmrs.api.ReportService;
import org.openmrs.api.context.Context;
import org.openmrs.report.EvaluationContext;
import org.openmrs.report.Parameter;
import org.openmrs.report.ReportData;
import org.openmrs.report.ReportSchema;
import org.openmrs.report.impl.TsvReportRenderer;
import org.openmrs.reporting.PatientCharacteristicFilter;
import org.openmrs.reporting.PatientSearch;
import org.openmrs.reporting.PatientSearchReportObject;
import org.openmrs.reporting.ProgramStatePatientFilter;
import org.openmrs.test.testutil.BaseContextSensitiveTest;
import org.openmrs.xml.OpenmrsCycleStrategy;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.load.Persister;

/**
 *
 */
public class PepfarReportFromXml extends BaseContextSensitiveTest {

	Log log = LogFactory.getLog(getClass());
	
	DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");

	Map<Parameter, Object> getUserEnteredParameters(Collection<Parameter> params) throws ParseException {
		Map<Parameter, Object> ret = new HashMap<Parameter, Object>();
		if (params != null) {
			for (Parameter p : params) {
				if (p.getName().equals("report.startDate"))
					ret.put(p, ymd.parse("2007-09-01"));
				else if (p.getName().equals("report.endDate"))
					ret.put(p, ymd.parse("2007-09-30"));
			}
		}
		return ret;
	}

	@Test
	public void shouldFromXml() throws Exception {
		executeDataSet("org/openmrs/test/report/include/PepfarReportTest.xml");

		StringBuilder xml = new StringBuilder();
		xml.append("<reportSchema id=\"1\">\n");
		xml.append("    <name>PEPFAR report</name>\n");
		xml.append("	<description>\n");
		xml.append("		Sample monthly PEPFAR report, modelled after the lesotho one\n");
		xml.append("	</description>\n");
		xml.append("	<parameters class=\"java.util.ArrayList\">\n");
		xml.append("		<parameter clazz=\"java.util.Date\"><name>report.startDate</name><label>When does the report period start?</label></parameter>/>\n");
		xml.append("		<parameter clazz=\"java.util.Date\"><name>report.endDate</name><label>When does the report period end?</label></parameter>\n");
		xml.append("		<parameter clazz=\"org.openmrs.Location\"><name>report.location</name><label>For which clinic is this report?</label></parameter>\n");
		xml.append("	</parameters>\n");
		xml.append("	<dataSets class=\"java.util.ArrayList\">\n");
		xml.append("		<dataSetDefinition class=\"org.openmrs.report.CohortDataSetDefinition\" name=\"Cohorts\">\n");
		xml.append("			<strategies class=\"java.util.LinkedHashMap\">\n");
		xml.append("				<entry>\n");
		xml.append("					<string>1.a</string>\n");
		xml.append("					<cohortDefinition class=\"org.openmrs.reporting.PatientSearch\">\n");
		xml.append("						<specification>[Male]</specification>\n");
		xml.append("					</cohortDefinition>\n");
		xml.append("				</entry>\n");
		xml.append("				<entry>\n");
		xml.append("					<string>1.b</string>\n");
		xml.append("					<cohortDefinition class=\"org.openmrs.reporting.PatientSearch\">\n");
		xml.append("						<specification>[Male] and [Adult] and [EnrolledOnDate|untilDate=${report.startDate-1d}]</specification>\n");
		xml.append("					</cohortDefinition>\n");
		xml.append("				</entry>\n");
		xml.append("			</strategies>\n");
		xml.append("		</dataSetDefinition>\n");
		xml.append("	</dataSets>\n");
		xml.append("</reportSchema>\n");
		System.out.println("xml\n" + xml);
		
		// Try to get HIV PROGRAM, or else, just the first program
		Program hivProgram = Context.getProgramWorkflowService().getProgram("HIV PROGRAM");
		if (hivProgram == null)
			hivProgram = Context.getProgramWorkflowService().getProgram(1);
		assertNotNull("Need at least one program defined to run this test", hivProgram);
		
		// Make sure we have all required PatientSearches
		if (Context.getReportObjectService().getPatientSearch("Male") == null) {
			PatientSearch ps = PatientSearch.createFilterSearch(PatientCharacteristicFilter.class);
			ps.addArgument("gender", "m", String.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("Male", ps));
		}
		if (Context.getReportObjectService().getPatientSearch("Female") == null) {
			PatientSearch ps = PatientSearch.createFilterSearch(PatientCharacteristicFilter.class);
			ps.addArgument("gender", "f", String.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("Female", ps));
		}
		if (Context.getReportObjectService().getPatientSearch("Adult") == null) {
			PatientSearch ps = PatientSearch.createFilterSearch(PatientCharacteristicFilter.class);
			ps.addArgument("minAge", "15", Integer.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("Adult", ps));
		}
		if (Context.getReportObjectService().getPatientSearch("Child") == null) {
			PatientSearch ps = PatientSearch.createFilterSearch(PatientCharacteristicFilter.class);
			ps.addArgument("maxAge", "15", Integer.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("Child", ps));
		}
		if (Context.getReportObjectService().getPatientSearch("EnrolledOnDate") == null) {
			PatientSearch ps = PatientSearch.createFilterSearch(ProgramStatePatientFilter.class);
			ps.addArgument("program", hivProgram.getProgramId().toString(), Integer.class);
			ps.addArgument("untilDate", "${date}", Date.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("EnrolledOnDate", ps));
		}
		
		Serializer serializer = new Persister(new OpenmrsCycleStrategy());
		ReportSchema schema = serializer.read(ReportSchema.class, xml.toString());

		log.info("Creating EvaluationContext");
		EvaluationContext evalContext = new EvaluationContext();
		
		for (Map.Entry<Parameter, Object> e : getUserEnteredParameters(schema.getReportParameters()).entrySet()) {
			log.info("adding parameter value " + e.getKey());
			evalContext.addParameterValue(e.getKey(), e.getValue());
		}

		ReportService rs = (ReportService) Context.getService(ReportService.class);
		ReportData data = rs.evaluate(schema, null, evalContext);
		
		TsvReportRenderer renderer = new TsvReportRenderer();
		System.out.println("Rendering output as TSV:");
		renderer.render(data, null, System.out);
	}

	@Test
	public void shouldBooleansInPatientSearch() throws Exception {
		executeDataSet("org/openmrs/test/report/include/ReportTests-patients.xml");
		
		// Make sure we have all required PatientSearches
		if (Context.getReportObjectService().getPatientSearch("Male") == null) {
			PatientSearch ps = PatientSearch.createFilterSearch(PatientCharacteristicFilter.class);
			ps.addArgument("gender", "m", String.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("Male", ps));
		}
		if (Context.getReportObjectService().getPatientSearch("Female") == null) {
			PatientSearch ps = PatientSearch.createFilterSearch(PatientCharacteristicFilter.class);
			ps.addArgument("gender", "f", String.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("Female", ps));
		}
		if (Context.getReportObjectService().getPatientSearch("Adult") == null) {
			PatientSearch ps = PatientSearch.createFilterSearch(PatientCharacteristicFilter.class);
			ps.addArgument("minAge", "15", Integer.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("Adult", ps));
		}
		if (Context.getReportObjectService().getPatientSearch("Child") == null) {
			PatientSearch ps = PatientSearch.createFilterSearch(PatientCharacteristicFilter.class);
			ps.addArgument("maxAge", "15", Integer.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("Child", ps));
		}
		
		EvaluationContext evalContext = new EvaluationContext();
		evalContext.addParameterValue(new Parameter("report.startDate", "Start Date", Date.class, null), ymd.parse("2007-09-01"));
		evalContext.addParameterValue(new Parameter("report.endDate", "End Date", Date.class, null), ymd.parse("2007-09-30"));

		CohortService cs = Context.getCohortService();
		
		PatientSearch male = new PatientSearch();
		PatientSearch female = new PatientSearch();
		PatientSearch maleAndFemale = new PatientSearch();
		PatientSearch maleOrFemale = new PatientSearch();
		male.setSpecificationString("[Male]");
		female.setSpecificationString("[Female]");
		maleAndFemale.setSpecificationString("[Male] and [Female]");
		maleOrFemale.setSpecificationString("[Male] or [Female]");		
		int numMale = cs.evaluate(male, evalContext).size();
		int numFemale = cs.evaluate(female, evalContext).size();
		int numMaleAndFemale = cs.evaluate(maleAndFemale, evalContext).size();
		int numMaleOrFemale = cs.evaluate(maleOrFemale, evalContext).size();
		assertEquals("AND should be zero", 0, numMaleAndFemale);
		assertEquals("OR should be the sum", numMale + numFemale, numMaleOrFemale);
		
		PatientSearch complex1 = new PatientSearch();
		complex1.setSpecificationString("([Male] and [Child]) or ([Female] and [Adult])");
		assertNotSame("Should not be zero", 0, cs.evaluate(complex1, evalContext).size());

		PatientSearch complex2 = new PatientSearch();
		PatientSearch complex3 = new PatientSearch();
		complex2.setSpecificationString("[Male] or [Female]");
		complex3.setSpecificationString("(([Male] and [Child]) or [Female])");
		// this assertion will fail 15 years after 2008-07-01 because the birthdates are
		// set to that in the dataset for the two "children"
		assertNotSame("Complex2 and Complex3 should be different sizes", cs.evaluate(complex2, evalContext).size(), cs.evaluate(complex3, evalContext).size());
	}
}
