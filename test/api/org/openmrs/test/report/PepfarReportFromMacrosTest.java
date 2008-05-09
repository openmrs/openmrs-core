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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.Program;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.ReportService;
import org.openmrs.api.context.Context;
import org.openmrs.report.EvaluationContext;
import org.openmrs.report.Parameter;
import org.openmrs.report.ReportData;
import org.openmrs.report.ReportSchema;
import org.openmrs.report.impl.TsvReportRenderer;
import org.openmrs.reporting.DrugOrderFilter;
import org.openmrs.reporting.EncounterPatientFilter;
import org.openmrs.reporting.ObsPatientFilter;
import org.openmrs.reporting.PatientCharacteristicFilter;
import org.openmrs.reporting.PatientSearch;
import org.openmrs.reporting.PatientSearchReportObject;
import org.openmrs.reporting.ProgramStatePatientFilter;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.xml.OpenmrsCycleStrategy;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.load.Persister;
import org.springframework.util.StringUtils;

/**
 *
 */
public class PepfarReportFromMacrosTest extends BaseContextSensitiveTest {

	DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");


	/**
	 * Auto generated method comment
	 * 
	 * @throws Exception
	 */
	public void testFromMacros() throws Exception {
		initializeInMemoryDatabase();
		//executeDataSet("org/openmrs/test/report/include/PepfarReportFromMacrosTest.xml");
		authenticate();
		
		long startTime = System.currentTimeMillis();
		authenticate();
		System.out.println("Setting up required patient searches...");
		setupPatientSearches();
		Properties macros = getMacros();
		System.out.println("Retrieved macros: " + macros);
		String xml = generateReportSchemaXml(getReportIndicators());
		System.out.println("Constructed starting xml: " + xml);
		xml = explodeReportSchemaMacros(xml, getMacros());
		System.out.println("Exploded macros in xml: " + xml);
		Serializer serializer = new Persister(new OpenmrsCycleStrategy());
		ReportSchema schema = serializer.read(ReportSchema.class, xml.toString());
		System.out.println("Serialized ReportSchema...");
		EvaluationContext evalContext = new EvaluationContext();
		for (Map.Entry<Parameter, Object> e : getUserEnteredParameters(schema.getReportParameters()).entrySet()) {
			System.out.println("adding parameter value " + e.getKey());
			evalContext.addParameterValue(e.getKey(), e.getValue());
		}
		System.out.println("Created EvaluationContext...");
		ReportService rs = (ReportService) Context.getService(ReportService.class);
		ReportData data = rs.evaluate(schema, null, evalContext);
		System.out.println("Rendering output to file:");
		renderData(data);
		System.out.println("Test run in " + (System.currentTimeMillis() - startTime) + " ms");
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param data
	 */
	public void renderData(ReportData data) throws Exception {
		TsvReportRenderer renderer = new TsvReportRenderer();
		renderer.render(data, null, System.out);
	}

	/**
	 * Auto generated method comment
	 * 
	 * @param params
	 * @return
	 * @throws ParseException
	 */
	public Map<Parameter, Object> getUserEnteredParameters(Collection<Parameter> params) throws ParseException {
		Map<Parameter, Object> ret = new HashMap<Parameter, Object>();
		if (params != null) {
			for (Parameter p : params) {
				if (p.getName().equals("report.startDate"))
					ret.put(p, ymd.parse("2007-01-01"));
				else if (p.getName().equals("report.endDate"))
					ret.put(p, ymd.parse("2007-12-31"));
			}
		}
		return ret;
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @return
	 */
	public Properties getReportIndicators() {
		Properties props = new Properties();
		
		// Box 1, HIV Care
		props.put("1.A", "@Male@ and @AdultAtStart@ and @EnrolledInHivCareBeforeStart@");
		props.put("1.B", "@Female@ and (not @PregnantAtStart@) and @AdultAtStart@ and @EnrolledInHivCareBeforeStart@");
		props.put("1.C", "@Female@ and @PregnantAtStart@ and @AdultAtStart@ and @EnrolledInHivCareBeforeStart@");
		props.put("1.D", "@Male@ and @ChildAtStart@ and @EnrolledInHivCareBeforeStart@");
		props.put("1.E", "@Female@ and @ChildAtStart@ and @EnrolledInHivCareBeforeStart@");
		props.put("1.F", "@EnrolledInHivCareBeforeStart@");
		
		props.put("1.G", "@Male@ and @AdultAtEnd@ and @EnrolledInHivCareDuring@");
		props.put("1.H", "@Female@ and (not @PregnantAtEnd@) and @AdultAtEnd@ and @EnrolledInHivCareDuring@");
		props.put("1.I", "@Female@ and @PregnantAtEnd@ and @AdultAtEnd@ and @EnrolledInHivCareDuring@");
		props.put("1.J", "@Male@ and @ChildAtEnd@ and @EnrolledInHivCareDuring@");
		props.put("1.K", "@Female@ and @ChildAtEnd@ and @EnrolledInHivCareDuring@");
		props.put("1.L", "@EnrolledInHivCareDuring@");
		
		props.put("1.M", "@Male@ and @AdultAtEnd@ and @EnrolledInHivCareByEnd@");
		props.put("1.N", "@Female@ and (not @PregnantAtEnd@) and @AdultAtEnd@ and @EnrolledInHivCareByEnd@");
		props.put("1.O", "@Female@ and @PregnantAtEnd@ and @AdultAtEnd@ and @EnrolledInHivCareByEnd@");
		props.put("1.P", "@Male@ and @ChildAtEnd@ and @EnrolledInHivCareByEnd@");
		props.put("1.Q", "@Female@ and @ChildAtEnd@ and @EnrolledInHivCareByEnd@");
		props.put("1.R", "@EnrolledInHivCareByEnd@");
		
		props.put("1.S", "@EnrolledInHivCareByEnd@ and @ActiveNotOnARTAtEnd@");
		
		// Box 2, ART Care
		props.put("2.A", "@Male@ and @AdultAtStart@ and @ARTstartBeforeStart@");
		props.put("2.B", "@Female@ and (not @PregnantAtStart@) and @AdultAtStart@ and @ARTstartBeforeStart@");
		props.put("2.C", "@Female@ and @PregnantAtStart@ and @AdultAtStart@ and @ARTstartBeforeStart@");
		props.put("2.D", "@Male@ and @ChildAtStart@ and @ARTstartBeforeStart@");
		props.put("2.E", "@Female@ and @ChildAtStart@ and @ARTstartBeforeStart@");
		props.put("2.F", "@ARTstartBeforeStart@");
		
		props.put("2.G", "@Male@ and @AdultAtEnd@ and @ARTstartDuring@");
		props.put("2.H", "@Female@ and (not @PregnantAtEnd@) and @AdultAtEnd@ and @ARTstartDuring@");
		props.put("2.I", "@Female@ and @PregnantAtEnd@ and @AdultAtEnd@ and @ARTstartDuring@");
		props.put("2.J", "@Male@ and @ChildAtEnd@ and @ARTstartDuring@");
		props.put("2.K", "@Female@ and @ChildAtEnd@ and @ARTstartDuring@");
		props.put("2.L", "@ARTstartDuring@");
		
		props.put("2.M", "@Male@ and @AdultAtEnd@ and @ARTstartByEnd@");
		props.put("2.N", "@Female@ and (not @PregnantAtEnd@) and @AdultAtEnd@ and @ARTstartByEnd@");
		props.put("2.O", "@Female@ and @PregnantAtEnd@ and @AdultAtEnd@ and @ARTstartByEnd@");
		props.put("2.P", "@Male@ and @ChildAtEnd@ and @ARTstartByEnd@");
		props.put("2.Q", "@Female@ and @ChildAtEnd@ and @ARTstartByEnd@");
		props.put("2.R", "@ARTstartByEnd@");

		props.put("2.S", "@ARTstartByEnd@ and @TransferredInByEnd@");
		props.put("2.T", "@RegimenMissedDuring@ and @RegimenReceived1MonthPrior@");
		props.put("2.U", "@ARTstartDuring@ and @CD4During@");
		
		// Box 3, Change in CD4 count and adherence to ART for 6-month and 12-month cohorts
		props.put("3.B", "@ARTstartExactly6MonthsPrior@");
		props.put("3.C", "@ARTstartExactly6MonthsPrior@ and @CD4Exactly6MonthsPrior@");
		
		props.put("3.E", "@ARTstart6MonthsBeforeStart@");
		props.put("3.F", "@ARTstart6MonthsBeforeStart@ and @CD4ByEnd@");
		props.put("3.H", "@ARTstart6MonthsBeforeStart@ and (not @RegimenMissedByEnd@)");
		
		props.put("3.J", "@ARTstartExactly12MonthsPrior@");
		props.put("3.K", "@ARTstartExactly12MonthsPrior@ and @CD4Exactly12MonthsPrior@");
		
		props.put("3.M", "@ARTstart12MonthsBeforeStart@");
		props.put("3.N", "@ARTstart12MonthsBeforeStart@ and @CD4ByEnd@");
		props.put("3.P", "@ARTstart12MonthsBeforeStart@ and (not @RegimenMissedByEnd@)");

		// Box 4.1, ARV Regimens - adult 1st line
		props.put("4.1.A", "@Male@ and @AdultAtStart@ and @Regimend4T30-3TC-NVPAtEnd@");
		props.put("4.1.B", "@Male@ and @AdultAtStart@ and @Regimend4T40-3TC-NVPAtEnd@");
		props.put("4.1.C", "@Male@ and @AdultAtStart@ and @Regimend4T30-3TC-EFVAtEnd@");
		props.put("4.1.D", "@Male@ and @AdultAtStart@ and @Regimend4T40-3TC-EFVAtEnd@");
		props.put("4.1.E", "@Male@ and @AdultAtStart@ and @RegimenZDV-3TC-NVPAtEnd@");
		props.put("4.1.F", "@Male@ and @AdultAtStart@ and @RegimenZDV-3TC-EFVAtEnd@");
		props.put("4.1.I", "@Male@ and @AdultAtStart@ and @RegimenAnyAdultFirstLineARVAtEnd@");
		
		props.put("4.1.J", "@Female@ and @AdultAtStart@ and @Regimend4T30-3TC-NVPAtEnd@");
		props.put("4.1.K", "@Female@ and @AdultAtStart@ and @Regimend4T40-3TC-NVPAtEnd@");
		props.put("4.1.L", "@Female@ and @AdultAtStart@ and @Regimend4T30-3TC-EFVAtEnd@");
		props.put("4.1.M", "@Female@ and @AdultAtStart@ and @Regimend4T40-3TC-EFVAtEnd@");
		props.put("4.1.N", "@Female@ and @AdultAtStart@ and @RegimenZDV-3TC-NVPAtEnd@");
		props.put("4.1.O", "@Female@ and @AdultAtStart@ and @RegimenZDV-3TC-EFVAtEnd@");
		props.put("4.1.R", "@Female@ and @AdultAtStart@ and @RegimenAnyAdultFirstLineARVAtEnd@");
		
		props.put("4.1.S", "@AdultAtStart@ and @RegimenAnyAdultFirstLineARVAtEnd@");
		
		// Box 4.2, ARV Regimens - children 1st line	
		props.put("4.2.A", "@Male@ and @ChildAtEnd@ and @Regimend4T-3TC-NVPAtEnd@");
		props.put("4.2.B", "@Male@ and @ChildAtEnd@ and @Regimend4T-3TC-EFVAtEnd@");
		props.put("4.2.C", "@Male@ and @ChildAtEnd@ and @RegimenZDV-3TC-NVPAtEnd@");
		props.put("4.2.D", "@Male@ and @ChildAtEnd@ and @RegimenZDV-3TC-EFVAtEnd@");
		
		props.put("4.2.I", "@Male@ and @ChildAtEnd@ and @RegimenAnyChildFirstLineARVAtEnd@");
		props.put("4.2.J", "(@Male@ and @AdultAtStart@ and @RegimenAnyAdultFirstLineARVAtEnd@) or (@Male@ and @ChildAtEnd@ and @RegimenAnyChildFirstLineARVAtEnd@)");

		props.put("4.2.K", "@Female@ and @ChildAtEnd@ and @Regimend4T-3TC-NVPAtEnd@");
		props.put("4.2.L", "@Female@ and @ChildAtEnd@ and @Regimend4T-3TC-EFVAtEnd@");
		props.put("4.2.M", "@Female@ and @ChildAtEnd@ and @RegimenZDV-3TC-NVPAtEnd@");
		props.put("4.2.N", "@Female@ and @ChildAtEnd@ and @RegimenZDV-3TC-EFVAtEnd@");

		props.put("4.2.S", "@Female@ and @ChildAtEnd@ and @RegimenAnyChildFirstLineARVAtEnd@");
		props.put("4.2.T", "(@Female@ and @AdultAtStart@ and @RegimenAnyAdultFirstLineARVAtEnd@) or (@Female@ and @ChildAtEnd@ and @RegimenAnyChildFirstLineARVAtEnd@)");

		props.put("4.2.U", "@ChildAtEnd@ and @RegimenAnyChildFirstLineARVAtEnd@");
		props.put("4.2.V", "(@AdultAtStart@ and @RegimenAnyAdultFirstLineARVAtEnd@) or (@ChildAtEnd@ and @RegimenAnyChildFirstLineARVAtEnd@)");
		
		// Box 4.3, ARV Regimens - adults 2nd line	
		props.put("4.3.A", "@Male@ and @AdultAtStart@ and @RegimenZDV-ddl-LPV/rAtEnd@");
		props.put("4.3.B", "@Male@ and @AdultAtStart@ and @RegimenABC-ddl-LPV/rAtEnd@");
		props.put("4.3.C", "@Male@ and @AdultAtStart@ and @RegimenTDF-ddl-LPV/rAtEnd@");
		props.put("4.3.H", "@Male@ and @AdultAtStart@ and @RegimenAnyAdultSecondLineARVAtEnd@");

		props.put("4.3.I", "@Female@ and @AdultAtStart@ and @RegimenZDV-ddl-LPV/rAtEnd@");
		props.put("4.3.J", "@Female@ and @AdultAtStart@ and @RegimenABC-ddl-LPV/rAtEnd@");
		props.put("4.3.K", "@Female@ and @AdultAtStart@ and @RegimenTDF-ddl-LPV/rAtEnd@");
		props.put("4.3.P", "@Female@ and @AdultAtStart@ and @RegimenAnyAdultSecondLineARVAtEnd@");
		
		props.put("4.3.Q", "@AdultAtStart@ and @RegimenAnyAdultSecondLineARVAtEnd@");
				
		// Box 4.4, ARV Regimens - children 2nd line	
		props.put("4.4.A", "@Male@ and @ChildAtEnd@ and @RegimenABC-ddl-LPV/rAtEnd@");
		props.put("4.4.B", "@Male@ and @ChildAtEnd@ and @RegimenZDV-ddl-LPV/rAtEnd@");
		props.put("4.4.C", "@Male@ and @ChildAtEnd@ and @RegimenTDF-ddl-LPV/rAtEnd@");
		
		props.put("4.4.H", "@Male@ and @ChildAtEnd@ and @RegimenAnyChildSecondLineARVAtEnd@");
		props.put("4.4.I", "(@Male@ and @AdultAtStart@ and @RegimenAnyAdultSecondLineARVAtEnd@) or (@Male@ and @ChildAtEnd@ and @RegimenAnyChildSecondLineARVAtEnd@)");
		props.put("4.4.J", "(@Male@ and @AdultAtStart@ and @RegimenAnyAdultSecondLineARVAtEnd@) or (@Male@ and @ChildAtEnd@ and @RegimenAnyChildSecondLineARVAtEnd@) or (@Male@ and @AdultAtStart@ and @RegimenAnyAdultFirstLineARVAtEnd@) or (@Male@ and @ChildAtEnd@ and @RegimenAnyChildFirstLineARVAtEnd@)");

		props.put("4.4.K", "@Female@ and @ChildAtEnd@ and @RegimenABC-ddl-LPV/rAtEnd@");
		props.put("4.4.L", "@Female@ and @ChildAtEnd@ and @RegimenZDV-ddl-LPV/rAtEnd@");
		props.put("4.4.M", "@Female@ and @ChildAtEnd@ and @RegimenTDF-ddl-LPV/rAtEnd@");
		
		props.put("4.4.R", "@Female@ and @ChildAtEnd@ and @RegimenAnyChildSecondLineARVAtEnd@");
		props.put("4.4.S", "(@Female@ and @AdultAtStart@ and @RegimenAnyAdultSecondLineARVAtEnd@) or (@Female@ and @ChildAtEnd@ and @RegimenAnyChildSecondLineARVAtEnd@)");
		props.put("4.4.T", "(@Female@ and @AdultAtStart@ and @RegimenAnyAdultSecondLineARVAtEnd@) or (@Female@ and @ChildAtEnd@ and @RegimenAnyChildSecondLineARVAtEnd@) or (@Female@ and @AdultAtStart@ and @RegimenAnyAdultFirstLineARVAtEnd@) or (@Female@ and @ChildAtEnd@ and @RegimenAnyChildFirstLineARVAtEnd@)");
		
		props.put("4.4.U", "@ChildAtEnd@ and @RegimenAnyChildSecondLineARVAtEnd@");
		props.put("4.4.V", "(@AdultAtStart@ and @RegimenAnyAdultSecondLineARVAtEnd@) or (@ChildAtEnd@ and @RegimenAnyChildSecondLineARVAtEnd@)");
		props.put("4.4.W", "(@AdultAtStart@ and @RegimenAnyAdultSecondLineARVAtEnd@) or (@ChildAtEnd@ and @RegimenAnyChildSecondLineARVAtEnd@) or (@AdultAtStart@ and @RegimenAnyAdultFirstLineARVAtEnd@) or (@ChildAtEnd@ and @RegimenAnyChildFirstLineARVAtEnd@)");
		
		// Box 5.1 Number of persons who did not pick up their ARV regimens
		props.put("5.1.A", "@Male@ and @RegimenMissedDuring@ and @RegimenReceived1MonthPrior@ and @RegimenReceived2MonthsPrior@");
		props.put("5.1.B", "@Male@ and @RegimenMissedDuring@ and @RegimenMissed1MonthPrior@ and @RegimenReceived2MonthsPrior@");
		props.put("5.1.C", "@Male@ and @RegimenMissedDuring@ and @RegimenMissed1MonthPrior@ and @RegimenMissed2MonthsPrior@");
		props.put("5.1.D", "@Male@ and @RegimenMissedDuring@");
		props.put("5.1.E", "@Female@ and @RegimenMissedDuring@ and @RegimenReceived1MonthPrior@ and @RegimenReceived2MonthsPrior@");
		props.put("5.1.F", "@Female@ and @RegimenMissedDuring@ and @RegimenMissed1MonthPrior@ and @RegimenReceived2MonthsPrior@");
		props.put("5.1.G", "@Female@ and @RegimenMissedDuring@ and @RegimenMissed1MonthPrior@ and @RegimenMissed2MonthsPrior@");
		props.put("5.1.H", "@Female@ and @RegimenMissedDuring@");
		props.put("5.1.I", "@RegimenMissedDuring@");
		
		// Box 5.2 Outcomes for those who did not pick up regimens in past month
		props.put("5.2.A", "@PatientLostAtEnd@");
		props.put("5.2.B", "@PatientDroppedAtEnd@");
		props.put("5.2.C", "@PatientDiedAtEnd@");
		props.put("5.2.D", "@TreatmentStoppedAtEnd@");
		props.put("5.2.E", "@TransferredOutAtEnd@");
	
		// Box 6 training (data not collected)
		
		// Box 7.1 PEP adults
		props.put("7.1.A", "@Male@ and @AdultAtStart@ and @RegimenAZT+3TCAtEnd@");
		props.put("7.1.B", "@Male@ and @AdultAtStart@ and @RegimenCombivir+IndinavirAtEnd@");
		props.put("7.1.C", "@Male@ and @AdultAtStart@ and @RegimenAnyAdultPEPAtEnd@");
		props.put("7.1.D", "@Female@ and @AdultAtStart@ and @RegimenAZT+3TCAtEnd@");
		props.put("7.1.E", "@Female@ and @AdultAtStart@ and @RegimenCombivir+IndinavirAtEnd@");
		props.put("7.1.F", "@Male@ and @AdultAtStart@ and @RegimenAnyAdultPEPAtEnd@");
		props.put("7.1.G", "@AdultAtStart@ and @RegimenAnyAdultPEPAtEnd@");

		// Box 7.2 PEP children
		props.put("7.2.A", "@Male@ and @ChildAtEnd@ and @Weight<15kgAtEnd@ and @RegimenZDV+3TCAtEnd@");
		props.put("7.2.B", "@Male@ and @ChildAtEnd@ and @Weight<15kgAtEnd@ and @RegimenZDV+3TC+Lop/RitAtEnd@");
		props.put("7.2.C", "@Male@ and @ChildAtEnd@ and @Weight<15kgAtEnd@ and @RegimenAnyChildPEPAtEnd@");
		props.put("7.2.D", "@Male@ and @ChildAtEnd@ and @Weight15-40kgAtEnd@ and @RegimenZDV+3TCAtEnd@");
		props.put("7.2.E", "@Male@ and @ChildAtEnd@ and @Weight15-40kgAtEnd@ and @RegimenZDV+3TC+Lop/RitAtEnd@");
		props.put("7.2.F", "@Male@ and @ChildAtEnd@ and @Weight15-40kgAtEnd@ and @RegimenAnyChildPEPAtEnd@");
		
		props.put("7.2.G", "@Female@ and @ChildAtEnd@ and @Weight<15kgAtEnd@ and @RegimenZDV+3TCAtEnd@");
		props.put("7.2.H", "@Female@ and @ChildAtEnd@ and @Weight<15kgAtEnd@ and @RegimenZDV+3TC+Lop/RitAtEnd@");
		props.put("7.2.I", "@Female@ and @ChildAtEnd@ and @Weight<15kgAtEnd@ and @RegimenAnyChildPEPAtEnd@");
		props.put("7.2.J", "@Female@ and @ChildAtEnd@ and @Weight15-40kgAtEnd@ and @RegimenZDV+3TCAtEnd@");
		props.put("7.2.K", "@Female@ and @ChildAtEnd@ and @Weight15-40kgAtEnd@ and @RegimenZDV+3TC+Lop/RitAtEnd@");
		props.put("7.2.L", "@Female@ and @ChildAtEnd@ and @Weight15-40kgAtEnd@ and @RegimenAnyChildPEPAtEnd@");
		
		props.put("7.2.M", "@ChildAtEnd@ and @RegimenAnyChildPEPAtEnd@");
		props.put("7.2.N", "(@AdultAtStart@ and @RegimenAnyAdultPEPAtEnd@) or (@ChildAtEnd@ and @RegimenAnyChildPEPAtEnd@)");
		
		// Box 8 PMTCT
		props.put("8.A", "@PregnantAtStart@ and @RegimenNVPAtEnd@");
		props.put("8.B", "@PregnantAtStart@ and @RegimenAZT+3TCAtEnd@");
		props.put("8.C", "@PregnantAtStart@ and @RegimenAZTAtEnd@");
//		props.put("8.D", "@PregnantAtStart@ and @RegimenOtherAtEnd@");
		props.put("8.E", "@RegimenAnyAdultPMTCTAtEnd@");
		
		props.put("8.F", "@InfantOnDate@ and @RegimenAZTsyrupAtEnd@");
		props.put("8.G", "@InfantOnDate@ and @RegimenNVPsyrupAtEnd@");
		props.put("8.H", "@InfantOnDate@ and @RegimenAZT+NVPAtEnd@");
		props.put("8.I", "@RegimenAnyInfantPMTCTAtEnd@");
		props.put("8.J", "@InfantOnDate@ and @RegimenAZT+NVPAtEnd@");
		props.put("8.K", "@RegimenAnyInfantPMTCTAtEnd@");
		
		
		// Box 9 Nutritional Supplement(data not collected)
		
		return props;
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @return
	 */
	public Properties getMacros() {
		Properties props = new Properties();
		
		// Gender
		props.put("Male", "[Male]");
		props.put("Female", "[Female]");
		
		// Adult / Child
		props.put("AdultAtStart", "[AdultOnDate|effectiveDate=${report.startDate}]");
		props.put("AdultAtEnd", "[AdultOnDate|effectiveDate=${report.endDate}]");
		props.put("ChildAtStart", "[ChildOnDate|effectiveDate=${report.startDate}]");
		props.put("ChildAtEnd", "[ChildOnDate|effectiveDate=${report.endDate}]");
		props.put("InfantOnDate", "[InfantOnDate|effectiveDate=${report.endDate}]");
		
		// Pregnancy Status
		props.put("PregnantAtStart", "[PregnantOnDate|untilDate=${report.startDate}]");
		props.put("PregnantAtEnd", "[PregnantOnDate|untilDate=${report.endDate}]");
		
		// HIV Care Enrollment
		props.put("EnrolledInHivCareBeforeStart", "[FirstEncounterOnOrBeforeDate|untilDate=${report.startDate-1d}]");
		props.put("EnrolledInHivCareByEnd", "[FirstEncounterOnOrBeforeDate|untilDate=${report.endDate}]");
		props.put("EnrolledInHivCareDuring", "@EnrolledInHivCareByEnd@ and (not @EnrolledInHivCareBeforeStart@)");
		
		// ART Start
		props.put("ARTstartBeforeStart", "[OnArvsOnOrBeforeDate|untilDate=${report.startDate-1d}]");
		props.put("ARTstartByEnd", "[OnArvsOnOrBeforeDate|untilDate=${report.endDate}]");
		props.put("ARTstartDuring", "@ARTstartByEnd@ and (not @ARTstartBeforeStart@)");
		
		props.put("ARTstart6MonthsBeforeStart", "[OnArvsOnOrBeforeDate|untilDate=${report.startDate-6m}]");
		props.put("ARTstart6MonthsBeforeEnd", "[OnArvsOnOrBeforeDate|untilDate=${report.endDate-6m}]");
		props.put("ARTstartExactly6MonthsPrior", "@ARTstart6MonthsBeforeEnd@ and (not @ARTstart6MonthsBeforeStart@)");
		
		props.put("ARTstart12MonthsBeforeStart", "[OnArvsOnOrBeforeDate|untilDate=${report.startDate-12m}]");
		props.put("ARTstart12MonthsBeforeEnd", "[OnArvsOnOrBeforeDate|untilDate=${report.endDate-12m}]");
		props.put("ARTstartExactly12MonthsPrior", "@ARTstart12MonthsBeforeEnd@ and (not @ARTstart12MonthsBeforeStart@)");
		
		// ART Status
		props.put("ActiveNotOnARTAtEnd", "[ActiveNotOnARTOnDate|untilDate=${report.endDate}]");
		props.put("PatientLostAtEnd", "[PatientDefaultedOnDate|untilDate=${report.endDate}]");
		props.put("PatientDroppedAtEnd", "[PatientDefaultedOnDate|untilDate=${report.endDate}]");
		props.put("PatientDiedAtEnd", "[PatientDiedOnDate|untilDate=${report.endDate}]");
		props.put("TreatmentStoppedAtEnd", "[TreatmentStoppedOnDate|untilDate=${report.endDate}]");
		props.put("TransferredOutAtEnd", "[TransferredOutOnDate|untilDate=${report.endDate}]");
		
		// ART Status preferred method (not working)
//		props.put("ActiveNotOnARTAtEnd", "[ARTStatusOnDate|untilDate=${report.endDate}|value=1578]");
//		props.put("PatientLostAtEnd", "[ARTStatusOnDate|untilDate=${report.endDate}|value=1743]");
//		props.put("PatientDroppedAtEnd", "[ARTStatusOnDate|untilDate=${report.endDate}|value=1743]");
//		props.put("PatientDiedAtEnd", "[ARTStatusOnDate|untilDate=${report.endDate}|value=1742]");
//		props.put("TreatmentStoppedAtEnd", "[ARTStatusOnDate|untilDate=${report.endDate}|value=1579]");
// transferred out (1744) as a status requested on 2/8/08		
//		props.put("TransferredOutAtEnd", "[TransferredOutOnDate|untilDate=${report.endDate}|value=1744]");
		
		// Transfer
		props.put("TransferredInByEnd", "[TransferredInByEnd|untilDate=${report.endDate}]");
		
		// Monthly Regimens Missed and Received 
		props.put("RegimenMissedDuring", "[RegimenMissedBetweenDates|sinceDate=${report.startDate}|untilDate=${report.endDate}]");
		props.put("RegimenMissed1MonthPrior", "[RegimenMissedBetweenDates|sinceDate=${report.startDate-1m}|untilDate=${report.endDate-1m}]");
		props.put("RegimenMissed2MonthsPrior", "[RegimenMissedBetweenDates|sinceDate=${report.startDate-2m}|untilDate=${report.endDate-2m}]");
		props.put("RegimenMissedByEnd", "[RegimenMissedOnOrBeforeDate|untilDate=${report.endDate}]");	
		
		props.put("RegimenReceived1MonthPrior", "[RegimenReceivedBetweenDates|sinceDate=${report.startDate-1m}|untilDate=${report.endDate-1m}]");
		props.put("RegimenReceived2MonthsPrior", "[RegimenReceivedBetweenDates|sinceDate=${report.startDate-2m}|untilDate=${report.endDate-2m}]");
		
		// CD4
		props.put("CD4During", "[CD4BetweenDates|sinceDate=${report.startDate}|untilDate=${report.endDate}]");
		props.put("CD4ByEnd", "[CD4OnOrBeforeDate|untilDate=${report.endDate}]");
		props.put("CD4Exactly6MonthsPrior", "[CD4BetweenDates|sinceDate=${report.startDate-6m}|untilDate=${report.endDate-6m}]");
		props.put("CD4Exactly12MonthsPrior", "[CD4BetweenDates|sinceDate=${report.startDate-12m}|untilDate=${report.endDate-12m}]");
		
		// Regimen 1st line ARV
		props.put("Regimend4T30-3TC-NVPAtEnd", "[RegimenOnOrBefore|untilDate=${report.endDate}|drugList=2]");
		props.put("Regimend4T40-3TC-NVPAtEnd", "[RegimenOnOrBefore|untilDate=${report.endDate}|drugList=3]");
		props.put("Regimend4T30-3TC-EFVAtEnd", "[RegimenOnOrBefore|untilDate=${report.endDate}|drugList=5] and [RegimenOnOrBefore|untilDate=${report.endDate}|drugList=42] and [RegimenOnOrBefore|untilDate=${report.endDate}|drugList=11]");
		props.put("Regimend4T40-3TC-EFVAtEnd", "[RegimenOnOrBefore|untilDate=${report.endDate}|drugList=6] and [RegimenOnOrBefore|untilDate=${report.endDate}|drugList=42] and [RegimenOnOrBefore|untilDate=${report.endDate}|drugList=11]");
		props.put("RegimenZDV-3TC-NVPAtEnd", "[RegimenOnOrBefore|untilDate=${report.endDate}|drugList=39] and [RegimenOnOrBefore|untilDate=${report.endDate}|drugList=22]");
		props.put("RegimenZDV-3TC-EFVAtEnd", "[RegimenOnOrBefore|untilDate=${report.endDate}|drugList=39] and [RegimenOnOrBefore|untilDate=${report.endDate}|drugList=11]");
		props.put("RegimenAnyAdultFirstLineARVAtEnd", "((@Regimend4T30-3TC-NVPAtEnd@) or (@Regimend4T40-3TC-NVPAtEnd@) or (@Regimend4T30-3TC-EFVAtEnd@) or (@Regimend4T40-3TC-EFVAtEnd@) or (@RegimenZDV-3TC-NVPAtEnd@) or (@RegimenZDV-3TC-EFVAtEnd@))");

		props.put("Regimend4T-3TC-EFVAtEnd", "[RegimenOnOrBefore|untilDate=${report.endDate}|drugList=32] and [RegimenOnOrBefore|untilDate=${report.endDate}|drugList=41] and [RegimenOnOrBefore|untilDate=${report.endDate}|drugList=29]");
		props.put("Regimend4T-3TC-NVPAtEnd", "[RegimenOnOrBefore|untilDate=${report.endDate}|drugList=32] and [RegimenOnOrBefore|untilDate=${report.endDate}|drugList=41] and [RegimenOnOrBefore|untilDate=${report.endDate}|drugList=21]");
		props.put("RegimenAnyChildFirstLineARVAtEnd", "@Regimend4T-3TC-NVPAtEnd@ or @Regimend4T-3TC-EFVAtEnd@  or @RegimenZDV-3TC-NVPAtEnd@ or @RegimenZDV-3TC-EFVAtEnd@");

		// Regimen 2nd line ARV (none of these drugs in openmrs)
		props.put("RegimenZDV-ddl-LPV/rAtEnd", "[RegimenOnOrBefore|untilDate=${report.endDate}|drugList=38] and [RegimenOnOrBefore|untilDate=${report.endDate}|drugList=10] and [RegimenOnOrBefore|untilDate=${report.endDate}|drugList=23]");
		props.put("RegimenABC-ddl-LPV/rAtEnd", "[RegimenOnOrBefore|untilDate=${report.endDate}|drugList=40] and [RegimenOnOrBefore|untilDate=${report.endDate}|drugList=10] and [RegimenOnOrBefore|untilDate=${report.endDate}|drugList=23]");
// added Tenofovir TDF 
		props.put("RegimenTDF-ddl-LPV/rAtEnd", "[RegimenOnOrBefore|untilDate=${report.endDate}|drugList=68] and [RegimenOnOrBefore|untilDate=${report.endDate}|drugList=10] and [RegimenOnOrBefore|untilDate=${report.endDate}|drugList=23]");	
		props.put("RegimenAnyAdultSecondLineARVAtEnd", "@RegimenZDV-ddl-LPV/rAtEnd@ or @RegimenABC-ddl-LPV/rAtEnd@ or @RegimenTDF-ddl-LPV/rAtEnd@");
		props.put("RegimenAnyChildSecondLineARVAtEnd", "@RegimenZDV-ddl-LPV/rAtEnd@ or @RegimenABC-ddl-LPV/rAtEnd@ or @RegimenTDF-ddl-LPV/rAtEnd@");
		
		// Regimen PEP
		props.put("RegimenAZT+3TCAtEnd", "[RegimenOnOrBefore|untilDate=${report.endDate}|drugList=39]");
// added Indinavir IDV
		props.put("RegimenCombivir+IndinavirAtEnd", "[RegimenOnOrBefore|untilDate=${report.endDate}|drugList=39] and [RegimenOnOrBefore|untilDate=${report.endDate}|drugList=67]");
		props.put("RegimenZDV+3TCAtEnd", "[RegimenOnOrBefore|untilDate=${report.endDate}|drugList=39]");	
		props.put("RegimenZDV+3TC+Lop/RitAtEnd", "[RegimenOnOrBefore|untilDate=${report.endDate}|drugList=39] and [RegimenOnOrBefore|untilDate=${report.endDate}|drugList=23]");
		props.put("RegimenAnyAdultPEPAtEnd", "@RegimenAZT+3TCAtEnd@ or @RegimenCombivir+IndinavirAtEnd@");
		props.put("RegimenAnyChildPEPAtEnd", "@RegimenZDV+3TCAtEnd@ or @RegimenZDV+3TC+Lop/RitAtEnd@");
		
		// Regimen PMTCT
		props.put("RegimenNVPAtEnd", "[RegimenOnOrBefore|untilDate=${report.endDate}|drugList=22]");
		props.put("RegimenAZTAtEnd", "[RegimenOnOrBefore|untilDate=${report.endDate}|drugList=38]");	
		props.put("RegimenAnyAdultPMTCTAtEnd", "@RegimenNVPAtEnd@ or @RegimenAZT+3TCAtEnd@ or @RegimenAZTAtEnd@");
		props.put("RegimenAZTsyrupAtEnd", "[RegimenOnOrBefore|untilDate=${report.endDate}|drugList=36]");
		props.put("RegimenNVPsyrupAtEnd", "[RegimenOnOrBefore|untilDate=${report.endDate}|drugList=21]");
		props.put("RegimenAZT+NVPAtEnd", "[RegimenOnOrBefore|untilDate=${report.endDate}|drugList=36] and [RegimenOnOrBefore|untilDate=${report.endDate}|drugList=21]");	
		props.put("RegimenAnyInfantPMTCTAtEnd", "@RegimenAZTsyrupAtEnd@ or @RegimenNVPsyrupAtEnd@ or @RegimenAZT+NVPAtEnd@");
		
		// Weight
		props.put("Weight<15kgAtEnd", "[WeightOnDate|untilDate=${report.endDate}|modifier=LESS_THAN|value=15]");
		props.put("Weight15-40kgAtEnd", "[WeightOnDate|untilDate=${report.endDate}|modifier=GREATER_EQUAL|value=15] and [WeightOnDate|untilDate=${report.endDate}|modifier=LESS_EQUAL|value=40]");
		
		return props;
	}
	
	/**
	 * Auto generated method comment
	 * 
	 */
	public void setupPatientSearches() {

		// Gender
		if (Context.getReportObjectService().getPatientSearch("Male") == null) {
			System.out.println("Creating Male...");
			PatientSearch ps = PatientSearch.createFilterSearch(PatientCharacteristicFilter.class);
			ps.addArgument("gender", "m", String.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("Male", ps));
		}
		if (Context.getReportObjectService().getPatientSearch("Female") == null) {
			System.out.println("Creating Female...");
			PatientSearch ps = PatientSearch.createFilterSearch(PatientCharacteristicFilter.class);
			ps.addArgument("gender", "f", String.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("Female", ps));
		}
		
		// Adult / Child / Infant
		if (Context.getReportObjectService().getPatientSearch("AdultOnDate") == null) {
			System.out.println("Creating AdultOnDate...");
			PatientSearch ps = PatientSearch.createFilterSearch(PatientCharacteristicFilter.class);
			ps.addArgument("minAge", "15", Integer.class);
			ps.addArgument("effectiveDate", "${date}", Date.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("AdultOnDate", ps));
		}
		if (Context.getReportObjectService().getPatientSearch("ChildOnDate") == null) {
			System.out.println("Creating ChildOnDate...");
			PatientSearch ps = PatientSearch.createFilterSearch(PatientCharacteristicFilter.class);
			ps.addArgument("minAge", "0", Integer.class);
			ps.addArgument("maxAge", "14", Integer.class);
			ps.addArgument("effectiveDate", "${date}", Date.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("ChildOnDate", ps));
		}
		if (Context.getReportObjectService().getPatientSearch("InfantOnDate") == null) {
			System.out.println("Creating InfantOnDate...");
			PatientSearch ps = PatientSearch.createFilterSearch(PatientCharacteristicFilter.class);
			ps.addArgument("minAge", "0", Integer.class);
			ps.addArgument("maxAge", "1", Integer.class);
			ps.addArgument("effectiveDate", "${date}", Date.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("InfantOnDate", ps));
		}
		
		// Pregnancy Status
		if (Context.getReportObjectService().getPatientSearch("PregnantOnDate") == null) {
			System.out.println("Creating PregnantOnDate...");
			Concept pregnancyConcept = Context.getConceptService().getConceptByName("PREGNANCY STATUS");
			assertNotNull("PREGNANCY STATUS concept must be defined to run this test", pregnancyConcept);
			Concept yesConcept = Context.getConceptService().getConceptByName("YES");
			assertNotNull("YES concept must be defined to run this test", pregnancyConcept);
			PatientSearch ps = PatientSearch.createFilterSearch(ObsPatientFilter.class);
			ps.addArgument("timeModifier", "LAST", PatientSetService.TimeModifier.class);
			ps.addArgument("question", pregnancyConcept.getConceptId().toString(), Concept.class);
			ps.addArgument("modifier", "EQUAL", PatientSetService.Modifier.class);
			ps.addArgument("value", yesConcept.getConceptId().toString(), Concept.class);
			ps.addArgument("untilDate", "${date}", Date.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("PregnantOnDate", ps));
		}
		
		// HIV Care Enrollment
		if (Context.getReportObjectService().getPatientSearch("EnrolledInHivProgramOnOrBeforeDate") == null) {
			System.out.println("Creating EnrolledInHivProgramOnOrBeforeDate...");
			Program hivProgram = Context.getProgramWorkflowService().getProgram("HIV PROGRAM");
			assertNotNull("HIV PROGRAM must be defined to run this test", hivProgram);
			PatientSearch ps = PatientSearch.createFilterSearch(ProgramStatePatientFilter.class);
			ps.addArgument("program", hivProgram.getProgramId().toString(), Integer.class);
			ps.addArgument("untilDate", "${date}", Date.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("EnrolledInHivProgramOnOrBeforeDate", ps));
		}
		if (Context.getReportObjectService().getPatientSearch("FirstEncounterOnOrBeforeDate") == null) {
			System.out.println("Creating FirstEncounterOnOrBeforeDate...");
			PatientSearch ps = PatientSearch.createFilterSearch(EncounterPatientFilter.class);
			ps.addArgument("atLeastCount", "1", Integer.class);
			ps.addArgument("untilDate", "${date}", Date.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("FirstEncounterOnOrBeforeDate", ps));
		}
		
		// ART Start
		if (Context.getReportObjectService().getPatientSearch("OnArvsOnOrBeforeDate") == null) {
			System.out.println("Creating OnArvsOnOrBeforeDate...");
			Concept arvDrugSet = Context.getConceptService().getConceptByName("ANTIRETROVIRAL DRUGS");
			assertNotNull("ANTIRETROVIRAL DRUGS concept must be defined to run this test", arvDrugSet);
			PatientSearch ps = PatientSearch.createFilterSearch(DrugOrderFilter.class);
			ps.addArgument("drugSets", arvDrugSet.getConceptId().toString(), Concept.class);
			ps.addArgument("anyOrAll", "ANY", PatientSetService.GroupMethod.class);
			ps.addArgument("untilDate", "${date}", Date.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("OnArvsOnOrBeforeDate", ps));
		}
		
		// ART Status (preferred method that's not working)
//			if (Context.getReportObjectService().getPatientSearch("ARTStatusOnDate") == null) {
//			System.out.println("Creating ARTStatusOnDate...");
//			Concept ARTstatusConcept = Context.getConceptService().getConceptByName("ANTIRETROVIRAL TREATMENT STATUS");
//			assertNotNull("ANTIRETROVIRAL TREATMENT STATUS concept must be defined to run this test", ARTstatusConcept);
//			PatientSearch ps = PatientSearch.createFilterSearch(ObsPatientFilter.class);
//			ps.addArgument("question", ARTstatusConcept.getConceptId().toString(), Concept.class);
//			ps.addArgument("timeModifier", "LAST", PatientSetService.TimeModifier.class);
//			ps.addArgument("modifier", "EQUAL", PatientSetService.Modifier.class);
//			ps.addArgument("value", "${value}", Concept.class);
//			ps.addArgument("untilDate", "${date}", Date.class);
//			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("ARTStatusOnDate", ps));
//		}

		// ART Status
		if (Context.getReportObjectService().getPatientSearch("ActiveNotOnARTOnDate") == null) {
			System.out.println("Creating ActiveNotOnARTOnDate...");
			Concept ARTstatusConcept = Context.getConceptService().getConceptByName("ANTIRETROVIRAL TREATMENT STATUS");
			assertNotNull("ANTIRETROVIRAL TREATMENT STATUS concept must be defined to run this test", ARTstatusConcept);
			Concept ActiveNotOnARVsConcept = Context.getConceptService().getConceptByName("ACTIVE NOT ON ARVS");
			assertNotNull("ACTIVE NOT ON ARVS concept must be defined to run this test", ARTstatusConcept);
			PatientSearch ps = PatientSearch.createFilterSearch(ObsPatientFilter.class);
			ps.addArgument("question", ARTstatusConcept.getConceptId().toString(), Concept.class);
			ps.addArgument("timeModifier", "LAST", PatientSetService.TimeModifier.class);
			ps.addArgument("modifier", "EQUAL", PatientSetService.Modifier.class);
			ps.addArgument("value", ActiveNotOnARVsConcept.getConceptId().toString(), Concept.class);
			ps.addArgument("untilDate", "${date}", Date.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("ActiveNotOnARTOnDate", ps));
		}
// lost or dropped = defaulted?
		if (Context.getReportObjectService().getPatientSearch("PatientDefaultedOnDate") == null) {
			System.out.println("Creating PatientDefaultedOnDate...");
			Concept ARTstatusConcept = Context.getConceptService().getConceptByName("ANTIRETROVIRAL TREATMENT STATUS");
			assertNotNull("ANTIRETROVIRAL TREATMENT STATUS concept must be defined to run this test", ARTstatusConcept);
			Concept ActiveNotOnARVsConcept = Context.getConceptService().getConceptByName("PATIENT DEFAULTED");
			assertNotNull("PATIENT DEFAULTED concept must be defined to run this test", ARTstatusConcept);
			PatientSearch ps = PatientSearch.createFilterSearch(ObsPatientFilter.class);
			ps.addArgument("question", ARTstatusConcept.getConceptId().toString(), Concept.class);
			ps.addArgument("timeModifier", "LAST", PatientSetService.TimeModifier.class);
			ps.addArgument("modifier", "EQUAL", PatientSetService.Modifier.class);
			ps.addArgument("value", ActiveNotOnARVsConcept.getConceptId().toString(), Concept.class);
			ps.addArgument("untilDate", "${date}", Date.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("PatientDefaultedOnDate", ps));
		}
		if (Context.getReportObjectService().getPatientSearch("PatientDiedOnDate") == null) {
			System.out.println("Creating PatientDiedOnDate...");
			Concept ARTstatusConcept = Context.getConceptService().getConceptByName("ANTIRETROVIRAL TREATMENT STATUS");
			assertNotNull("ANTIRETROVIRAL TREATMENT STATUS concept must be defined to run this test", ARTstatusConcept);
			Concept ActiveNotOnARVsConcept = Context.getConceptService().getConceptByName("PATIENT DIED");
			assertNotNull("PATIENT DIED concept must be defined to run this test", ARTstatusConcept);
			PatientSearch ps = PatientSearch.createFilterSearch(ObsPatientFilter.class);
			ps.addArgument("question", ARTstatusConcept.getConceptId().toString(), Concept.class);
			ps.addArgument("timeModifier", "LAST", PatientSetService.TimeModifier.class);
			ps.addArgument("modifier", "EQUAL", PatientSetService.Modifier.class);
			ps.addArgument("value", ActiveNotOnARVsConcept.getConceptId().toString(), Concept.class);
			ps.addArgument("untilDate", "${date}", Date.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("PatientDiedOnDate", ps));
		}
		if (Context.getReportObjectService().getPatientSearch("TreatmentStoppedOnDate") == null) {
			System.out.println("Creating TreatmentStoppedOnDate...");
			Concept ARTstatusConcept = Context.getConceptService().getConceptByName("ANTIRETROVIRAL TREATMENT STATUS");
			assertNotNull("ANTIRETROVIRAL TREATMENT STATUS concept must be defined to run this test", ARTstatusConcept);
			Concept ActiveNotOnARVsConcept = Context.getConceptService().getConceptByName("TREATMENT STOPPED");
			assertNotNull("TREATMENT STOPPED concept must be defined to run this test", ARTstatusConcept);
			PatientSearch ps = PatientSearch.createFilterSearch(ObsPatientFilter.class);
			ps.addArgument("question", ARTstatusConcept.getConceptId().toString(), Concept.class);
			ps.addArgument("timeModifier", "LAST", PatientSetService.TimeModifier.class);
			ps.addArgument("modifier", "EQUAL", PatientSetService.Modifier.class);
			ps.addArgument("value", ActiveNotOnARVsConcept.getConceptId().toString(), Concept.class);
			ps.addArgument("untilDate", "${date}", Date.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("TreatmentStoppedOnDate", ps));
		}
//	transferred out not yet an art status	
		if (Context.getReportObjectService().getPatientSearch("TransferredOutOnDate") == null) {
			System.out.println("Creating TransferredOutOnDate...");
			Concept ARTstatusConcept = Context.getConceptService().getConceptByName("ANTIRETROVIRAL TREATMENT STATUS");
			assertNotNull("ANTIRETROVIRAL TREATMENT STATUS concept must be defined to run this test", ARTstatusConcept);
			Concept ActiveNotOnARVsConcept = Context.getConceptService().getConceptByName("ACTIVE NOT ON ARVS");
			assertNotNull("ACTIVE NOT ON ARVS concept must be defined to run this test", ARTstatusConcept);
			PatientSearch ps = PatientSearch.createFilterSearch(ObsPatientFilter.class);
			ps.addArgument("question", ARTstatusConcept.getConceptId().toString(), Concept.class);
			ps.addArgument("timeModifier", "LAST", PatientSetService.TimeModifier.class);
			ps.addArgument("modifier", "EQUAL", PatientSetService.Modifier.class);
			ps.addArgument("value", ActiveNotOnARVsConcept.getConceptId().toString(), Concept.class);
			ps.addArgument("untilDate", "${date}", Date.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("TransferredOutOnDate", ps));
		}
		
		// Transfer In
		if (Context.getReportObjectService().getPatientSearch("TransferredInByEnd") == null) {
			System.out.println("Creating TransferredInByEnd...");
			Concept TransferredInConcept = Context.getConceptService().getConceptByName("TRANSFER IN FROM");
			assertNotNull("TRANSFER IN FROM concept must be defined to run this test", TransferredInConcept);
			PatientSearch ps = PatientSearch.createFilterSearch(ObsPatientFilter.class);
			ps.addArgument("timeModifier", "LAST", PatientSetService.TimeModifier.class);
			ps.addArgument("question", TransferredInConcept.getConceptId().toString(), Concept.class);
			ps.addArgument("untilDate", "${date}", Date.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("TransferredInByEnd", ps));
		}
		
		// Missed Monthly Regimen
		if (Context.getReportObjectService().getPatientSearch("RegimenMissedOnOrBeforeDate") == null) {
			System.out.println("Creating RegimenMissedOnOrBeforeDate...");
			Concept RegimenMissedConcept = Context.getConceptService().getConceptByName("DATE OF MISSED VISIT");
			assertNotNull("DATE OF MISSED VISIT concept must be defined to run this test", RegimenMissedConcept);
			PatientSearch ps = PatientSearch.createFilterSearch(ObsPatientFilter.class);
			ps.addArgument("timeModifier", "ANY", PatientSetService.TimeModifier.class);
			ps.addArgument("question", RegimenMissedConcept.getConceptId().toString(), Concept.class);
			ps.addArgument("untilDate", "${date}", Date.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("RegimenMissedOnOrBeforeDate", ps));
		}		
		if (Context.getReportObjectService().getPatientSearch("RegimenMissedBetweenDates") == null) {
			System.out.println("Creating RegimenMissedBetweenDates...");
			Concept RegimenMissedConcept = Context.getConceptService().getConceptByName("DATE OF MISSED VISIT");
			assertNotNull("DATE OF MISSED VISIT concept must be defined to run this test", RegimenMissedConcept);
			PatientSearch ps = PatientSearch.createFilterSearch(ObsPatientFilter.class);
			ps.addArgument("timeModifier", "LAST", PatientSetService.TimeModifier.class);
			ps.addArgument("question", RegimenMissedConcept.getConceptId().toString(), Concept.class);
			ps.addArgument("sinceDate", "${date}", Date.class);
			ps.addArgument("untilDate", "${date}", Date.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("RegimenMissedBetweenDates", ps));
		}
		
		// Monthly Regimen Received
		if (Context.getReportObjectService().getPatientSearch("RegimenReceivedBetweenDates") == null) {
			System.out.println("Creating RegimenReceivedBetweenDates...");
			Concept RegimenReceivedConcept = Context.getConceptService().getConceptByName("MEDICATIONS DISPENSED");
			assertNotNull("MEDICATIONS DISPENSED concept must be defined to run this test", RegimenReceivedConcept);
			PatientSearch ps = PatientSearch.createFilterSearch(ObsPatientFilter.class);
			ps.addArgument("timeModifier", "LAST", PatientSetService.TimeModifier.class);
			ps.addArgument("question", RegimenReceivedConcept.getConceptId().toString(), Concept.class);
			ps.addArgument("sinceDate", "${date}", Date.class);
			ps.addArgument("untilDate", "${date}", Date.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("RegimenReceivedBetweenDates", ps));
		}
		
		// CD4 count
		if (Context.getReportObjectService().getPatientSearch("CD4BetweenDates") == null) {
			System.out.println("Creating CD4BetweenDates...");
			Concept CD4Concept = Context.getConceptService().getConceptByName("CD4 COUNT");
			assertNotNull("CD4 COUNT concept must be defined to run this test", CD4Concept);
			PatientSearch ps = PatientSearch.createFilterSearch(ObsPatientFilter.class);
			ps.addArgument("question", CD4Concept.getConceptId().toString(), Concept.class);
			ps.addArgument("timeModifier", "ANY", PatientSetService.TimeModifier.class);
			ps.addArgument("sinceDate", "${date}", Date.class);
			ps.addArgument("untilDate", "${date}", Date.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("CD4BetweenDates", ps));
		}
		if (Context.getReportObjectService().getPatientSearch("CD4OnOrBeforeDate") == null) {
			System.out.println("Creating CD4OnOrBeforeDate...");
			Concept CD4Concept = Context.getConceptService().getConceptByName("CD4 COUNT");
			assertNotNull("CD4 COUNT concept must be defined to run this test", CD4Concept);
			PatientSearch ps = PatientSearch.createFilterSearch(ObsPatientFilter.class);
			ps.addArgument("question", CD4Concept.getConceptId().toString(), Concept.class);
			ps.addArgument("timeModifier", "ANY", PatientSetService.TimeModifier.class);
			ps.addArgument("untilDate", "${date}", Date.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("CD4OnOrBeforeDate", ps));
		}
		
		// Regimen
		if (Context.getReportObjectService().getPatientSearch("RegimenOnOrBefore") == null) {
			System.out.println("Creating RegimenOnOrBefore...");
			PatientSearch ps = PatientSearch.createFilterSearch(DrugOrderFilter.class);
			ps.addArgument("drugList", "${drug}", Drug.class);
			ps.addArgument("anyOrAll", "ANY", PatientSetService.GroupMethod.class);
			ps.addArgument("untilDate", "${date}", Date.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("RegimenOnOrBefore", ps));
		}
		
		// Weight
		if (Context.getReportObjectService().getPatientSearch("WeightOnDate") == null) {
			System.out.println("Creating WeightOnDate...");
			Concept WeightConcept = Context.getConceptService().getConceptByName("WEIGHT (KG)");
			assertNotNull("WEIGHT (KG) concept must be defined to run this test", WeightConcept);
			PatientSearch ps = PatientSearch.createFilterSearch(ObsPatientFilter.class);
			ps.addArgument("timeModifier", "LAST", PatientSetService.TimeModifier.class);
			ps.addArgument("question", WeightConcept.getConceptId().toString(), Concept.class);
			ps.addArgument("untilDate", "${date}", Date.class);
			ps.addArgument("modifier", "${modifier}", PatientSetService.Modifier.class);
			ps.addArgument("value", "${value}", Object.class);
			Context.getReportObjectService().createReportObject(new PatientSearchReportObject("WeightOnDate", ps));
		}
		setComplete();
	}

	/**
	 * Auto generated method comment
	 * 
	 * @param indicators
	 * @return
	 */
	public String generateReportSchemaXml(Properties indicators) {
		Set<Object> indicatorKeys = indicators.keySet();
		StringBuilder xml = new StringBuilder();
		xml.append("<reportSchema id=\"1\">\n");
		xml.append("    <name>Test pepfar report</name>\n");
		xml.append("	<description>\n");
		xml.append("		Sample monthly PEPFAR report, modelled after the lesotho one\n");
		xml.append("	</description>\n");
		xml.append("	<parameters class=\"java.util.ArrayList\">\n");
		xml.append("		<parameter clazz=\"java.util.Date\"><name>report.startDate</name><label>When does the report period start?</label></parameter>/>\n");
		xml.append("		<parameter clazz=\"java.util.Date\"><name>report.endDate</name><label>When does the report period end?</label></parameter>\n");
		xml.append("	</parameters>\n");
		xml.append("	<dataSets class=\"java.util.ArrayList\">\n");
		xml.append("		<dataSetDefinition class=\"org.openmrs.dataset.CohortDataSetDefinition\" name=\"Cohorts\">\n");
		xml.append("			<strategies class=\"java.util.LinkedHashMap\">\n");
		for (Object indicatorKey : indicatorKeys) {
			xml.append("				<entry>\n");
			xml.append("					<string>"+ indicatorKey.toString() +"</string>\n");
			xml.append("					<cohort class=\"org.openmrs.reporting.PatientSearch\">\n");
			xml.append("						<specification>"+indicators.getProperty(indicatorKey.toString())+"</specification>\n");
			xml.append("					</cohort>\n");
			xml.append("				</entry>\n");
		}
		xml.append("			</strategies>\n");
		xml.append("		</dataSetDefinition>\n");
		xml.append("	</dataSets>\n");
		xml.append("</reportSchema>\n");
		return xml.toString();
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param xml
	 * @param macros
	 * @return
	 */
	public String explodeReportSchemaMacros(String xml, Properties macros) {
		Set<Object> propertyNames = macros.keySet();
    	if (StringUtils.hasText(xml)) {
    		while (true) {
    			String replacementXml = xml;
	    		for (Object propName : propertyNames) {
	    			System.out.println("Trying to replace @" + propName.toString() + "@ with " + macros.getProperty(propName.toString()));
	    			replacementXml = replacementXml.replace("@" + propName.toString() + "@", macros.getProperty(propName.toString()));
	    		}
	    		if (replacementXml.equals(xml)) {
	    			break;
	    		}
	    		xml = replacementXml;
    		}
    	}
    	return xml;
	}

}
