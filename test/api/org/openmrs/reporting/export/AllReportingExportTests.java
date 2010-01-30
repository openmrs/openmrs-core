package org.openmrs.reporting.export;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	DataExportTest.class,
	RowPerObsDataExportTest.class
})
public class AllReportingExportTests{}