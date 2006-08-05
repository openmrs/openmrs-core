package org.openmrs.web.dwr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.PatientSet;
import org.openmrs.web.WebConstants;

import uk.ltd.getahead.dwr.WebContextFactory;

public class DWRPatientSetService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public void clearMyPatientSet() {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			context.getPatientSetService().clearMyPatientSet();
		}		
	}
	
	public void setMyPatientSet(String patientIds) {
		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			context.getPatientSetService().setMyPatientSet(PatientSet.parseCommaSeparatedPatientIds(patientIds));
		}
	}
	
	public String getMyPatientSet() {
		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			return context.getPatientSetService().getMyPatientSet().toCommaSeparatedPatientIds();
		} else {
			return null;
		}
	}
	
	public Integer getMyPatientSetSize() {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			return context.getPatientSetService().getMyPatientSet().size();
		} else {
			return null;
		}		
	}
	
	public Vector<PatientListItem> getFromMyPatientSet(Integer fromIndex, Integer pageSize) {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			Vector<PatientListItem> ret = new Vector<PatientListItem>();
			List<Integer> temp = new ArrayList<Integer>(context.getPatientSetService().getMyPatientSet().getPatientIds());
			if (fromIndex >= temp.size()) {
				return ret;
			}
			if (fromIndex < 0) {
				fromIndex = 0;
			}
			int toIndex = Math.min(fromIndex + pageSize, temp.size());
			List<Patient> patients = context.getPatientSetService().getPatients(temp.subList(fromIndex, toIndex));
			for (Patient patient : patients) {
				ret.add(new PatientListItem(patient));
			}
			return ret;
		} else {
			return null;
		}		
	}
	
	/* Not sure if you can make this call from DWR--argument is a collection
	public Vector<PatientListItem> getPatients(Collection<Integer> patientIds) {
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			Vector<PatientListItem> ret = new Vector<PatientListItem>();
			List<Patient> patients = context.getPatientSetService().getPatients(patientIds);
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
		Context context = (Context) WebContextFactory.get().getSession()
			.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			Vector<PatientListItem> ret = new Vector<PatientListItem>();
			List<Integer> ptIds = new ArrayList<Integer>();
			for (String s : patientIds.split(",")) {
				try {
					ptIds.add(Integer.valueOf(s));
				} catch (Exception ex) { }
			}
			List<Patient> patients = context.getPatientSetService().getPatients(ptIds);
			for (Patient patient : patients) {
				ret.add(new PatientListItem(patient));
			}
			return ret;
		} else {
			return null;
		}
	}
		
	public void addToMyPatientSet(Integer ptId) {
		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			context.getPatientSetService().addToMyPatientSet(ptId);
		}
	}

	public void removeFromMyPatientSet(Integer ptId) {
		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context != null) {
			context.getPatientSetService().removeFromMyPatientSet(ptId);
		}
	}

}
