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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.ReportService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * Tests the small class ReportSchemaXml and its database accesses
 */
public class ReportSchemaXmlTest extends BaseContextSensitiveTest {

	Log log = LogFactory.getLog(getClass());

	/**
	 * Set up the database with the initial dataset before every test method
	 * in this class.
	 * 
	 * 
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		// Comment out when running test on underlying database instead of in-memory database.
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/report/include/ReportSchemaXmlTest-initialData.xml");
		
		authenticate();
	}
	 
	/**
	 * Saves a new ReportSchemaXml in database.  Gets it.  Then Deletes it.
	 * Tests for successful save, get, and delete.
	 * 
	 * @throws Exception
	 *
	 */
	@Test
	public void shouldSaveGetDeleteReportSchema() throws Exception {
		
		StringBuilder xml = new StringBuilder();
		xml.append("<reportSchema id=\"1\" reportSchemaId=\"1\">\n");
		xml.append("    <name>PEPFAR report</name>\n");
		xml.append("	<description>\n");
		xml.append("		Sample monthly PEPFAR report, modeled after the lesotho one\n");
		xml.append("	</description>\n");
		xml.append("	<parameters class=\"java.util.ArrayList\">\n");
		xml.append("		<parameter clazz=\"java.util.Date\"><name>report.startDate</name><label>When does the report period start?</label></parameter>/>\n");
		xml.append("		<parameter clazz=\"java.util.Date\"><name>report.endDate</name><label>When does the report period end?</label></parameter>\n");
		xml.append("		<parameter clazz=\"org.openmrs.Location\"><name>report.location</name><label>For which clinic is this report?</label></parameter>\n");
		xml.append("	</parameters>\n");
		xml.append("	<dataSets class=\"java.util.ArrayList\">\n");
		xml.append("		<dataSetDefinition class=\"org.openmrs.dataset.CohortDataSetDefinition\" name=\"Cohorts\">\n");
		xml.append("			<strategies class=\"java.util.LinkedHashMap\">\n");
		xml.append("				<entry>\n");
		xml.append("					<string>1.a</string>\n");
		xml.append("					<cohort class=\"org.openmrs.reporting.PatientSearch\">\n");
		xml.append("						<specification>[Male]</specification>\n");
		xml.append("					</cohort>\n");
		xml.append("				</entry>\n");
		xml.append("				<entry>\n");
		xml.append("					<string>1.b</string>\n");
		xml.append("					<cohort class=\"org.openmrs.reporting.PatientSearch\">\n");
		xml.append("						<specification>[Male] and [Adult] and [EnrolledOnDate|untilDate=${report.startDate - 1d}]</specification>\n");
		xml.append("					</cohort>\n");
		xml.append("				</entry>\n");
		xml.append("			</strategies>\n");
		xml.append("		</dataSetDefinition>\n");
		xml.append("	</dataSets>\n");
		xml.append("</reportSchema>\n");
		
		// create and check the report schema object
		ReportSchemaXml reportSchemaXml = new ReportSchemaXml();
		reportSchemaXml.setXml(xml.toString());
		
		//assertEquals(new Integer(1), reportSchemaXml.getReportSchemaId());
		assertEquals(xml.toString(), reportSchemaXml.getXml());

		ReportService rs = (ReportService) Context.getService(ReportService.class);
		rs.createReportSchemaXml(reportSchemaXml);
		
		Context.clearSession();
		
		ReportSchemaXml reportSchemaXmlFromDB = rs.getReportSchemaXml(1);

		assertNotNull("The schema xml was not saved correctly, none found in the db", reportSchemaXmlFromDB);
		
		assertEquals(xml.toString(), reportSchemaXmlFromDB.getXml());

		assertEquals(new Integer(1), reportSchemaXmlFromDB.getReportSchemaId());
		
		// assertTrue("The saved object and the actual object are not calling themselves equal", reportSchemaXml.equals(reportSchemaXmlFromDB));
		
		// delete the just created report schema xml object
		rs.deleteReportSchemaXml(reportSchemaXmlFromDB);
		
		// try to fetch that deleted xml object, expect null
		ReportSchemaXml deletedXml = rs.getReportSchemaXml(1);
		assertNull("The deleted xml object should be null", deletedXml);
		
	}
	
	/**
	 * Creates a ReportSchemaXml such as in {@link #testSaveGetDeleteReportSchema()}, 
	 * then changes it, updates it in the database, and tests to see if the update is successful.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldUpdateReportSchemaXml() throws Exception {
		
		StringBuilder xml = new StringBuilder();
		xml.append("<reportSchema id=\"2\" reportSchemaId=\"2\">\n");
		xml.append("    <name>PEPFAR report</name>\n");
		xml.append("	<description>\n");
		xml.append("		Sample monthly PEPFAR report, modeled after the lesotho one\n");
		xml.append("	</description>\n");
		xml.append("	<parameters class=\"java.util.ArrayList\">\n");
		xml.append("		<parameter clazz=\"java.util.Date\"><name>report.startDate</name><label>When does the report period start?</label></parameter>/>\n");
		xml.append("		<parameter clazz=\"java.util.Date\"><name>report.endDate</name><label>When does the report period end?</label></parameter>\n");
		xml.append("		<parameter clazz=\"org.openmrs.Location\"><name>report.location</name><label>For which clinic is this report?</label></parameter>\n");
		xml.append("	</parameters>\n");
		xml.append("	<dataSets class=\"java.util.ArrayList\">\n");
		xml.append("		<dataSetDefinition class=\"org.openmrs.dataset.CohortDataSetDefinition\" name=\"Cohorts\">\n");
		xml.append("			<strategies class=\"java.util.LinkedHashMap\">\n");
		xml.append("				<entry>\n");
		xml.append("					<string>1.a</string>\n");
		xml.append("					<cohort class=\"org.openmrs.reporting.PatientSearch\">\n");
		xml.append("						<specification>[Male]</specification>\n");
		xml.append("					</cohort>\n");
		xml.append("				</entry>\n");
		xml.append("				<entry>\n");
		xml.append("					<string>1.b</string>\n");
		xml.append("					<cohort class=\"org.openmrs.reporting.PatientSearch\">\n");
		xml.append("						<specification>[Male] and [Adult]</specification>\n");
		xml.append("					</cohort>\n");
		xml.append("				</entry>\n");
		xml.append("			</strategies>\n");
		xml.append("		</dataSetDefinition>\n");
		xml.append("	</dataSets>\n");
		xml.append("</reportSchema>\n");
		
		// create and check the report schema object
		ReportSchemaXml reportSchemaXml = new ReportSchemaXml();
		reportSchemaXml.setXml(xml.toString());
		assertEquals(xml.toString(), reportSchemaXml.getXml());

		ReportService rs = (ReportService) Context.getService(ReportService.class);
		rs.updateReportSchemaXml(reportSchemaXml);

		
		ReportSchemaXml reportSchemaXmlFromDB = rs.getReportSchemaXml(2);
		// Get an extra object with the same id just to mess things up.
		ReportSchemaXml reportSchemaXmlJodion = rs.getReportSchemaXml(2);

		
		//assertTrue("The saved object and the actual object are not calling themselves equal", reportSchemaXml.equals(reportSchemaXmlFromDB));

		assertEquals(xml.toString(), reportSchemaXmlFromDB.getXml());

		// Create a slightly different xml.
		StringBuilder xml2 = new StringBuilder();
		xml2.append("<reportSchema id=\"2\" reportSchemaId=\"2\">\n");
		xml2.append("    <name>PEPFAR report updated</name>\n");
		xml2.append("	<description>\n");
		xml2.append("		Sample monthly PEPFAR report changed again\n");
		xml2.append("	</description>\n");
		xml2.append("	<parameters class=\"java.util.ArrayList\">\n");
		xml2.append("		<parameter clazz=\"java.util.Date\"><name>report.startDate</name><label>When does the report period start?</label></parameter>/>\n");
		xml2.append("		<parameter clazz=\"java.util.Date\"><name>report.endDate</name><label>When does the report period end?</label></parameter>\n");
		xml2.append("		<parameter clazz=\"org.openmrs.Location\"><name>report.location</name><label>For which clinic is this report?</label></parameter>\n");
		xml2.append("	</parameters>\n");
		xml2.append("	<dataSets class=\"java.util.ArrayList\">\n");
		xml2.append("		<dataSetDefinition class=\"org.openmrs.dataset.CohortDataSetDefinition\" name=\"Cohorts\">\n");
		xml2.append("			<strategies class=\"java.util.LinkedHashMap\">\n");
		xml2.append("				<entry>\n");
		xml2.append("					<string>1.a</string>\n");
		xml2.append("					<cohort class=\"org.openmrs.reporting.PatientSearch\">\n");
		xml2.append("						<specification>[Male]</specification>\n");
		xml2.append("					</cohort>\n");
		xml2.append("				</entry>\n");
		xml2.append("				<entry>\n");
		xml2.append("					<string>1.b</string>\n");
		xml2.append("					<cohort class=\"org.openmrs.reporting.PatientSearch\">\n");
		xml2.append("						<specification>[Male] and [Adult]</specification>\n");
		xml2.append("					</cohort>\n");
		xml2.append("				</entry>\n");
		xml2.append("				<entry>\n");
		xml2.append("					<string>1.c</string>\n");
		xml2.append("					<cohort class=\"org.openmrs.reporting.PatientSearch\">\n");
		xml2.append("						<specification>[Adult]</specification>\n");
		xml2.append("					</cohort>\n");
		xml2.append("				</entry>\n");
		xml2.append("			</strategies>\n");
		xml2.append("		</dataSetDefinition>\n");
		xml2.append("	</dataSets>\n");
		xml2.append("</reportSchema>\n");
		
		// Update the ReportSchemaXml with a different [name, description, and] xml.
		reportSchemaXmlFromDB.setXml(xml2.toString());
		//String newName = "PEPFAR Report with a new name.";
		//String newDescription = "PEPFAR Report with a new description.";
		String newName = "PEPFAR report updated";
		String newDescription = "Sample monthly PEPFAR report changed again";
		reportSchemaXmlFromDB.setName(newName);
		reportSchemaXmlFromDB.setDescription(newDescription);
		rs.updateReportSchemaXml(reportSchemaXmlFromDB);

		// Retrieve the updated ReportSchemaXml from database.
		ReportSchemaXml reportSchemaXmlUpdateFromDB = rs.getReportSchemaXml(reportSchemaXmlFromDB.getReportSchemaId());
		
		// Were the [name, description, and] xml really updated?
		assertEquals(xml2.toString(), reportSchemaXmlUpdateFromDB.getXml());
		assertEquals(newName, reportSchemaXmlUpdateFromDB.getName());
		assertEquals(newDescription, reportSchemaXmlUpdateFromDB.getDescription());
	}
	
}
