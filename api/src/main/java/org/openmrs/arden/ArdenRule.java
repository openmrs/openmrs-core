/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.arden;

public interface ArdenRule {
	
	/*
	public abstract class ArdenRule {

	private Context context;
	private Patient patient;
	private ArdenDataSource dataSource;
	
	public ArdenRule() {
		
	}
	
	public ArdenRule(Context context, Patient patient, ArdenDataSource dataSource) {
		setContext(context);
		setPatient(patient);
		setDataSource(dataSource);
	}
	
	public void setContext(Context context) {
		this.context = context;
	}
	
	public Context getContext() {
		return context;
	}
	
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	public Patient getPatient() {
		return this.patient;
	}
	
	public void setDataSource(ArdenDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public ArdenDataSource getDataSource() {
		return dataSource;
	}
	
	public abstract ArdenValue evaluate();
	
	public abstract Set<ArdenRule> getChildren();
	*/
	public boolean evaluate();
	
	public String doAction();
	
	public ArdenRule getChildren();
	
	public ArdenRule getInstance();
	
	public void printDebug();
}
