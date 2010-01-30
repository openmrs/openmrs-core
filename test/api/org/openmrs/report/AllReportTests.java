package org.openmrs.report;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	EvaluationContextTest.class,
	PatientSearchParameterTest.class,
	PatientSearchTest.class,
	PepfarReportFromXmlTest.class,
	PepfarReportSerializationTest.class,
	PepfarReportTest.class,
	ReportSchemaXmlNonContextTest.class,
	ReportSchemaXmlTest.class,
	RowPerObsDatasetTest.class,
	RowPerProgramEnrollmentDatasetTest.class
})
public class AllReportTests{}