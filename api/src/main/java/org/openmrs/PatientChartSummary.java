/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Aggregates a patient's clinical data into a single summary object suitable for AI consumption.
 * This provides a unified view of encounters, observations, conditions, diagnoses, allergies,
 * orders, and program enrollments without requiring multiple service calls.
 *
 * @since 3.0.0
 */
public class PatientChartSummary {
	
	private Patient patient;
	
	private Date generatedDate;
	
	private Date fromDate;
	
	private Date toDate;
	
	private List<Encounter> encounters = new ArrayList<>();
	
	private List<Obs> observations = new ArrayList<>();
	
	private List<Condition> conditions = new ArrayList<>();
	
	private List<Diagnosis> diagnoses = new ArrayList<>();
	
	private Allergies allergies = new Allergies();
	
	private List<Order> orders = new ArrayList<>();
	
	private List<Visit> visits = new ArrayList<>();
	
	private List<PatientProgram> programEnrollments = new ArrayList<>();
	
	public PatientChartSummary() {
	}
	
	public PatientChartSummary(Patient patient) {
		this.patient = patient;
		this.generatedDate = new Date();
	}
	
	/**
	 * @return the patient this summary belongs to
	 */
	public Patient getPatient() {
		return patient;
	}
	
	/**
	 * @param patient the patient this summary belongs to
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	/**
	 * @return the date this summary was generated
	 */
	public Date getGeneratedDate() {
		return generatedDate;
	}
	
	/**
	 * @param generatedDate the date this summary was generated
	 */
	public void setGeneratedDate(Date generatedDate) {
		this.generatedDate = generatedDate;
	}
	
	/**
	 * @return the start of the date range for this summary, or null if unbounded
	 */
	public Date getFromDate() {
		return fromDate;
	}
	
	/**
	 * @param fromDate the start of the date range for this summary
	 */
	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}
	
	/**
	 * @return the end of the date range for this summary, or null if unbounded
	 */
	public Date getToDate() {
		return toDate;
	}
	
	/**
	 * @param toDate the end of the date range for this summary
	 */
	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}
	
	/**
	 * @return the patient's encounters
	 */
	public List<Encounter> getEncounters() {
		return encounters;
	}
	
	/**
	 * @param encounters the patient's encounters
	 */
	public void setEncounters(List<Encounter> encounters) {
		this.encounters = encounters;
	}
	
	/**
	 * @return the patient's observations
	 */
	public List<Obs> getObservations() {
		return observations;
	}
	
	/**
	 * @param observations the patient's observations
	 */
	public void setObservations(List<Obs> observations) {
		this.observations = observations;
	}
	
	/**
	 * @return the patient's conditions
	 */
	public List<Condition> getConditions() {
		return conditions;
	}
	
	/**
	 * @param conditions the patient's conditions
	 */
	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}
	
	/**
	 * @return the patient's diagnoses
	 */
	public List<Diagnosis> getDiagnoses() {
		return diagnoses;
	}
	
	/**
	 * @param diagnoses the patient's diagnoses
	 */
	public void setDiagnoses(List<Diagnosis> diagnoses) {
		this.diagnoses = diagnoses;
	}
	
	/**
	 * @return the patient's allergies
	 */
	public Allergies getAllergies() {
		return allergies;
	}
	
	/**
	 * @param allergies the patient's allergies
	 */
	public void setAllergies(Allergies allergies) {
		this.allergies = allergies;
	}
	
	/**
	 * @return the patient's orders (drug, test, referral)
	 */
	public List<Order> getOrders() {
		return orders;
	}
	
	/**
	 * @param orders the patient's orders
	 */
	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}
	
	/**
	 * @return the patient's visits
	 */
	public List<Visit> getVisits() {
		return visits;
	}
	
	/**
	 * @param visits the patient's visits
	 */
	public void setVisits(List<Visit> visits) {
		this.visits = visits;
	}
	
	/**
	 * @return the patient's program enrollments
	 */
	public List<PatientProgram> getProgramEnrollments() {
		return programEnrollments;
	}
	
	/**
	 * @param programEnrollments the patient's program enrollments
	 */
	public void setProgramEnrollments(List<PatientProgram> programEnrollments) {
		this.programEnrollments = programEnrollments;
	}
}
