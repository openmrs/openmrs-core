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
package org.openmrs.web.dwr;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;

public class DWRPatientSetService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public void clearMyPatientSet() {
		Context.getPatientSetService().clearMyPatientSet();
	}
	
	public void setMyPatientSet(String patientIds) {
		Context.getPatientSetService().setMyPatientSet(new Cohort(patientIds));
	}
	
	public String getMyPatientSet() {
		return Context.getPatientSetService().getMyPatientSet().getCommaSeparatedPatientIds();
	}
	
	public Integer getMyPatientSetSize() {
		return Context.getPatientSetService().getMyPatientSet().size();
	}
	
	public Vector<PatientListItem> getFromMyPatientSet(Integer fromIndex, Integer pageSize) {
		Vector<PatientListItem> ret = new Vector<PatientListItem>();
		List<Integer> temp = new ArrayList<Integer>(Context.getPatientSetService().getMyPatientSet().getMemberIds());
		if (fromIndex >= temp.size()) {
			return ret;
		}
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		int toIndex = Math.min(fromIndex + pageSize, temp.size());
		List<Patient> patients = Context.getPatientSetService().getPatients(temp.subList(fromIndex, toIndex));
		for (Patient patient : patients) {
			ret.add(new PatientListItem(patient));
		}
		return ret;
	}
	
	/* Not sure if you can make this call from DWR--argument is a collection
	public Vector<PatientListItem> getPatients(Collection<Integer> patientIds) {
		Context Context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (Context != null) {
			Vector<PatientListItem> ret = new Vector<PatientListItem>();
			List<Patient> patients = Context.getPatientSetService().getPatients(patientIds);
			for (Patient patient : patients) {
				ret.add(new PatientListItem(patient));
			}
			return ret;
		} else {
			return null;
		}
	}
	*/
	
	public Vector<PatientListItem> getPatients(String patientIds) {
		Vector<PatientListItem> ret = new Vector<PatientListItem>();
		List<Integer> ptIds = new ArrayList<Integer>();
		for (String s : patientIds.split(",")) {
			try {
				ptIds.add(Integer.valueOf(s));
			} catch (Exception ex) { }
		}
		List<Patient> patients = Context.getPatientSetService().getPatients(ptIds);
		for (Patient patient : patients) {
			ret.add(new PatientListItem(patient));
		}
		return ret;
	}
		
	public void addToMyPatientSet(Integer ptId) {
		Context.getPatientSetService().addToMyPatientSet(ptId);
	}

	public void removeFromMyPatientSet(Integer ptId) {
		Context.getPatientSetService().removeFromMyPatientSet(ptId);
	}

}
