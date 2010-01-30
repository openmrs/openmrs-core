package org.openmrs.reporting;

import org.openmrs.reporting.export.AllReportingExportTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	CachingPatientFilterTest.class,
	AllReportingExportTests.class,
	PatientFilterTest.class,
	ReportObjectServiceTest.class
})
public class AllReportingTests{}