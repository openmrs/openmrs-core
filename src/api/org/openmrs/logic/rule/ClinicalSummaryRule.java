package org.openmrs.logic.rule;

import java.util.List;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.logic.Aggregation;
import org.openmrs.logic.DateConstraint;
import org.openmrs.logic.Duration;
import org.openmrs.logic.LogicDataSource;
import org.openmrs.logic.Result;
import org.openmrs.logic.Rule;

public class ClinicalSummaryRule extends Rule {

	@Override
	public Result eval(LogicDataSource dataSource, Patient patient,
			Object[] args) {
		StringBuffer xml = new StringBuffer();
		xml.append("<?xml version=\"1.0\"?>\n");
		xml.append("<clinicalSummary>\n");
		append(xml, "id", dataSource.eval(patient, "PATIENT IDENTIFIER").toString());
		Result altIds = dataSource.eval(patient,
				"ALTERNATE PATIENT IDENTIFIERS");
		if (altIds.size() > 0) {
			xml.append("  <alternateIds>\n");
			for (Result id : altIds.getResultList()) {
				xml.append("    <id>");
				xml.append(id.toString());
				xml.append("</id>\n");
			}
			xml.append("  </alternateIds>\n");
		}
		append(xml, "name", dataSource.eval(patient, "NAME").toString());
		append(xml, "birthdate", dataSource.eval(patient, "BIRTHDATE").toString());
		append(xml, "healthCenter", dataSource.eval(patient, "HEALTH CENTER").toString());
		Result civilStatus = dataSource.eval(patient, Aggregation.latest(),
				"CIVIL STATUS", null, null);
		if (!civilStatus.isNull())
			append(xml, "civilStatus", civilStatus.toString());
		Result pregnant = dataSource.eval(patient, Aggregation.latest(),
				"PREGNANCY STATUS", DateConstraint.withinPreceding(Duration
						.months(11)), args);
		if (pregnant.isNull())
			append (xml, "pregnant", "UNKNOWN");
		else if ("F".equals(patient.getGender()))
			append(xml, "pregnant", pregnant.toBoolean().toString());
		Result numberChildrenSired = dataSource.eval(patient, Aggregation
				.latest(), "TOTAL NUMBER OF CHILDREN SIRED", null, args);
		if (numberChildrenSired.isNull())
			append(xml, "numberChildrenSired", "UNKNOWN");
		else append(xml, "numberChildrenSired", numberChildrenSired.toString());
		Result youngChildren = dataSource.eval(patient, Aggregation.latest(),
				"TOTAL CHILDREN UNDER 5YO LIVING IN HOME", null, args);
		if (youngChildren.isNull())
			append(xml, "youngChildren", "UNKNOWN");
		else append(xml, "youngChildren", youngChildren.toString());
		Result whoStage = dataSource.eval(patient, Aggregation.latest(),
				"CURRENT WHO HIV STAGE", null, args);
		append(xml, "whoStage", whoStage.toString());
		Result hospitalized = dataSource.eval(patient, "HOSPITALIZED WITHIN PAST YEAR");
		append(xml, "hospitalized", hospitalized.toString());
		Result problemList = dataSource.eval(patient, "PROBLEM LIST");
		if (problemList.size() > 0) {
			xml.append("  <problemList>\n");
			for (Result p : problemList.getResultList()) {
				xml.append("    <problem>");
				xml.append(p.getConcept().getName(Context.getLocale()));
				xml.append("</problem>\n");
			}
			xml.append("  </problemList>\n");
		}
		Result perfectAdherence = dataSource.eval(patient, "PERFECT ADHERENCE");
		append(xml, "perfectAdherence", perfectAdherence.toString());
		xml.append("  <flowsheet>\n");
		List<Result> weightList = dataSource.eval(patient, Aggregation.latest(5), "WEIGHT (KG)", null, args).getResultList();
		appendToFlowsheet(xml, "WEIGHT (KG)", weightList);
		List<Result> hgbList = dataSource.eval(patient, Aggregation.latest(5), "HEMOGLOBIN", null, args).getResultList();
		appendToFlowsheet(xml, "HGB", hgbList);
		List<Result> satList = dataSource.eval(patient, Aggregation.latest(5), "BLOOD OXYGEN SATURATION", null, args).getResultList();
		appendToFlowsheet(xml, "SA02", satList);
		List<Result> cd4PercentList = dataSource.eval(patient, Aggregation.latest(5), "CD4, BY FACS", null, args).getResultList();
		appendToFlowsheet(xml, "CD4", cd4PercentList);
		List<Result> creatinineList = dataSource.eval(patient, Aggregation.latest(5), "SERUM CREATININE", null, args).getResultList();
		appendToFlowsheet(xml, "CREATININE", creatinineList);
		List<Result> sgptList = dataSource.eval(patient, Aggregation.latest(5), "SERUM GLUTAMIC-PYRUVIC TRANSAMINASE", null, args).getResultList();
		appendToFlowsheet(xml, "SGPT", sgptList);
		List<Result> cxrList = dataSource.eval(patient, Aggregation.latest(5), "X-RAY, CHEST", null, args).getResultList();
		appendToFlowsheet(xml, "CXR", cxrList);
		xml.append("  </flowsheet>\n");
		xml.append("</clinicalSummary>");
		return new Result(xml.toString());
	}

	private void append(StringBuffer xml, String tag, String value) {
		xml.append("  <");
		xml.append(tag);
		xml.append(">");
		xml.append(value);
		xml.append("</");
		xml.append(tag);
		xml.append(">\n");
	}
	
	private void appendToFlowsheet(StringBuffer xml, String name, List<Result> resultList) {
		if (resultList.size() > 0) {
			xml.append("    <results name=\"");
			xml.append(name);
			xml.append("\">\n");
			for (Result result : resultList) {
				xml.append("      <value date=\"");
				xml.append(result.getDate());
				xml.append("\">");
				xml.append(result.toString());
				xml.append("</value>\n"); 
			}
			xml.append("    </results>\n");
		}

	}

}
