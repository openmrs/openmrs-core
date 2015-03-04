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

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.openmrs.Obs;
import org.openmrs.Patient;

public class ArdenValue {
	
	private Vector<Obs> obs;
	
	//	private boolean concludeVal;
	//	private Vector<String> printStr;
	private Patient patient;
	
	private Locale locale;
	
	//default constructor
	public ArdenValue() {
		obs = new Vector<Obs>();
	}
	
	public ArdenValue(Patient p, Locale l) {
		obs = new Vector<Obs>();
		patient = p;
		locale = l;
	}
	
	public void addObs(List<Obs> o) {
		obs.addAll(o);
	}
	
	/**
	 * @return Returns the valueNumeric. Returning the last elements value for now // TODO
	 */
	public Double getValueNumeric() {
		if (!obs.isEmpty()) {
			return obs.lastElement().getValueNumeric();
		} else {
			return null;
		}
	}
	
	/**
	 * This converts the value_numeric to a value_boolean, essentially
	 *
	 * @return Boolean of the obs value
	 */
	public Boolean getValueAsBoolean() {
		return (getValueNumeric() == null ? false : getValueNumeric() != 0);
	}
	
	/**
	 * @return Returns the valueText. Returning the last elements value for now // TODO
	 */
	public String getValueText() {
		if (!obs.isEmpty()) {
			return obs.lastElement().getValueText();
		} else {
			return null;
		}
	}
	
	public Integer getValueCoded() {
		if (!obs.isEmpty()) {
			return obs.lastElement().getValueCoded().getConceptId();
		} else {
			return null;
		}
	}
	
	public Patient getPatient() {
		return patient;
	}
	
	public void PrintObsMap() {
		Iterator<Obs> iterator = obs.iterator();
		Obs obs;
		
		while (iterator.hasNext()) {
			obs = iterator.next();
			System.out.println(obs.getValueAsString(locale) + "---" + obs.getDateCreated());
			
		}
		
	}
}
