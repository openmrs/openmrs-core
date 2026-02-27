/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.openmrs.Allergy;
import org.openmrs.Condition;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PatientChartSummary;
import org.openmrs.PatientProgram;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientChartSummaryService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.PatientChartSummaryDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of the {@link PatientChartSummaryService}.
 *
 * @since 3.0.0
 */
@Service("patientChartSummaryService")
@Transactional(readOnly = true)
public class PatientChartSummaryServiceImpl extends BaseOpenmrsService implements PatientChartSummaryService {
	
	@Autowired
	private PatientChartSummaryDAO patientChartSummaryDAO;
	
	public void setPatientChartSummaryDAO(PatientChartSummaryDAO patientChartSummaryDAO) {
		this.patientChartSummaryDAO = patientChartSummaryDAO;
	}
	
	/**
	 * @see PatientChartSummaryService#getChartSummary(Patient)
	 */
	@Override
	public PatientChartSummary getChartSummary(Patient patient) throws APIException {
		return getChartSummary(patient, null, null);
	}
	
	/**
	 * @see PatientChartSummaryService#getChartSummary(Patient, Date, Date)
	 */
	@Override
	public PatientChartSummary getChartSummary(Patient patient, Date fromDate, Date toDate) throws APIException {
		PatientChartSummary summary = new PatientChartSummary(patient);
		summary.setFromDate(fromDate);
		summary.setToDate(toDate);
		
		summary.setEncounters(patientChartSummaryDAO.getEncounters(patient, fromDate, toDate));
		summary.setObservations(patientChartSummaryDAO.getObservations(patient, fromDate, toDate));
		summary.setOrders(patientChartSummaryDAO.getOrders(patient, fromDate, toDate));
		summary.setVisits(patientChartSummaryDAO.getVisits(patient, fromDate, toDate));
		
		summary.setConditions(Context.getConditionService().getAllConditions(patient));
		summary.setDiagnoses(Context.getDiagnosisService().getDiagnoses(patient, fromDate));
		summary.setAllergies(Context.getPatientService().getAllergies(patient));
		summary.setProgramEnrollments(
		        Context.getProgramWorkflowService().getPatientPrograms(patient, null, null, null, null, null, false));
		
		return summary;
	}
	
	/**
	 * @see PatientChartSummaryService#getChartSummaryAsText(Patient)
	 */
	@Override
	public String getChartSummaryAsText(Patient patient) throws APIException {
		PatientChartSummary summary = getChartSummary(patient);
		return buildTextSummary(summary);
	}
	
	private String buildTextSummary(PatientChartSummary summary) {
		StringBuilder sb = new StringBuilder();
		Patient patient = summary.getPatient();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		// Demographics
		sb.append("PATIENT CHART SUMMARY\n");
		sb.append("=====================\n");
		sb.append("Name: ").append(patient.getPersonName()).append("\n");
		sb.append("Gender: ").append(patient.getGender()).append("\n");
		if (patient.getBirthdate() != null) {
			sb.append("Date of Birth: ").append(dateFormat.format(patient.getBirthdate())).append("\n");
		}
		sb.append("\n");
		
		// Allergies
		sb.append("ALLERGIES\n");
		sb.append("---------\n");
		if (summary.getAllergies().isEmpty()) {
			sb.append(summary.getAllergies().getAllergyStatus()).append("\n");
		} else {
			for (Allergy allergy : summary.getAllergies()) {
				sb.append("- ").append(allergy.getAllergen()).append("\n");
			}
		}
		sb.append("\n");
		
		// Active Conditions
		sb.append("CONDITIONS\n");
		sb.append("----------\n");
		if (summary.getConditions().isEmpty()) {
			sb.append("No recorded conditions\n");
		} else {
			for (Condition condition : summary.getConditions()) {
				sb.append("- ").append(condition.getCondition())
				        .append(" (").append(condition.getClinicalStatus()).append(")\n");
			}
		}
		sb.append("\n");
		
		// Diagnoses
		sb.append("DIAGNOSES\n");
		sb.append("---------\n");
		if (summary.getDiagnoses().isEmpty()) {
			sb.append("No recorded diagnoses\n");
		} else {
			for (Diagnosis diagnosis : summary.getDiagnoses()) {
				sb.append("- ").append(diagnosis.getDiagnosis())
				        .append(" (").append(diagnosis.getRank()).append(")\n");
			}
		}
		sb.append("\n");
		
		// Visits
		sb.append("VISITS (").append(summary.getVisits().size()).append(")\n");
		sb.append("------\n");
		for (Visit visit : summary.getVisits()) {
			sb.append("- ").append(dateFormat.format(visit.getStartDatetime()))
			        .append(": ").append(visit.getVisitType().getName()).append("\n");
		}
		sb.append("\n");
		
		// Encounters
		sb.append("ENCOUNTERS (").append(summary.getEncounters().size()).append(")\n");
		sb.append("----------\n");
		for (Encounter encounter : summary.getEncounters()) {
			sb.append("- ").append(dateFormat.format(encounter.getEncounterDatetime()))
			        .append(": ").append(encounter.getEncounterType().getName()).append("\n");
		}
		sb.append("\n");
		
		// Recent Observations
		sb.append("OBSERVATIONS (").append(summary.getObservations().size()).append(")\n");
		sb.append("------------\n");
		for (Obs obs : summary.getObservations()) {
			sb.append("- ").append(dateFormat.format(obs.getObsDatetime()))
			        .append(": ").append(obs.getConcept().getName())
			        .append(" = ").append(obs.getValueAsString(Context.getLocale())).append("\n");
		}
		sb.append("\n");
		
		// Orders
		sb.append("ORDERS (").append(summary.getOrders().size()).append(")\n");
		sb.append("------\n");
		for (Order order : summary.getOrders()) {
			sb.append("- ").append(dateFormat.format(order.getDateActivated()))
			        .append(": ").append(order.getConcept().getName())
			        .append(" (").append(order.getOrderType().getName()).append(")\n");
		}
		sb.append("\n");
		
		// Program Enrollments
		sb.append("PROGRAM ENROLLMENTS (").append(summary.getProgramEnrollments().size()).append(")\n");
		sb.append("-------------------\n");
		for (PatientProgram pp : summary.getProgramEnrollments()) {
			sb.append("- ").append(pp.getProgram().getName());
			if (pp.getDateEnrolled() != null) {
				sb.append(" (enrolled: ").append(dateFormat.format(pp.getDateEnrolled())).append(")");
			}
			sb.append("\n");
		}
		
		return sb.toString();
	}
}
