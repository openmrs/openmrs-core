package org.openmrs.web.dwr;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientAddress;
import org.openmrs.Tribe;
import org.openmrs.api.context.Context;
import org.openmrs.formentry.FormEntryService;
import org.openmrs.web.WebConstants;

import uk.ltd.getahead.dwr.WebContextFactory;

public class DWRPatientService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public Vector findPatients(String searchValue, boolean includeVoided) {
		
		Vector<Object> patientList = new Vector<Object>();

		HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
		
		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		if (context == null) {
			patientList.add("Your session has expired.");
			patientList.add("Please <a href='" + request.getContextPath() + "/logout'>log in</a> again.");
		}
		else {
			try {
				
				Integer userId = -1;
				if (context.isAuthenticated())
					userId = context.getAuthenticatedUser().getUserId();
				log.info(userId + "|" + searchValue);
				
				FormEntryService ps = context.getFormEntryService();
				List<Patient> patients;
				
				patients = ps.findPatients(searchValue, includeVoided);
				patientList = new Vector<Object>(patients.size());
				for (Patient p : patients) {
					patientList.add(new PatientListItem(p));
				}
				
				// only 2 results found ( TODO change to "no results found")
				// decapitated search
				if (patientList.size() < 3) {
					String[] names = searchValue.split(" ");
					String newSearch = "";
					for (String name : names) {
						if (name.length() > 3)
							name = name.substring(0, 4);
						newSearch += " " + name;
					}
					
					newSearch = newSearch.trim();
					List<Patient> newPatients = ps.findPatients(newSearch, includeVoided);
					newPatients.removeAll(patients);
					if (newPatients.size() > 0) {
						patientList.add("Minimal patients returned. Results for <b>" + newSearch + "</b>");
						for (Patient p : newPatients) {
							PatientListItem pi = new PatientListItem(p);
							patientList.add(pi);
						}
					}
				}
				
			} catch (Exception e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				log.error(e + " - " + sw.toString());
				patientList.add("Error while attempting to find patient - " + e.getMessage());
			}
		}
		return patientList;
	}
	
	public PatientListItem getPatient(Integer patientId) {
		Context context = (Context) WebContextFactory.get().getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		FormEntryService ps = context.getFormEntryService();
		Patient p = ps.getPatient(patientId);
		PatientListItem pli = new PatientListItem(p);
		if (p.getAddresses() != null && p.getAddresses().size() > 0) {
			PatientAddress pa = (PatientAddress)p.getAddresses().toArray()[0];
			pli.setAddress1(pa.getAddress1());
			pli.setAddress2(pa.getAddress2());
		}
		return pli;
	}
	
	public Vector getSimilarPatients(String name, String birthyear, String age, String gender) {
		Vector<Object> patientList = new Vector<Object>();

		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
		
		if (context == null) {
			patientList.add("Your session has expired.");
			patientList.add("Please <a href='" + request.getContextPath() + "/logout'>log in</a> again.");
		}
		else {
			
			Integer userId = context.getAuthenticatedUser().getUserId();
			log.info(userId + "|" + name + "|" + birthyear + "|" + age + "|" + gender);
			
			FormEntryService ps = context.getFormEntryService();
			List<Patient> patients = new Vector<Patient>();
			
			Integer d = null;
			birthyear = birthyear.trim();
			age = age.trim();
			if (birthyear.length() > 3)
				d = Integer.valueOf(birthyear);
			else if (age.length() > 0) {
				Calendar c = Calendar.getInstance();
				c.setTime(new Date());
				d = c.get(Calendar.YEAR);
				d = d - Integer.parseInt(age);
			}
			
			if (gender.length() < 1)
				gender = null;
			
			patients.addAll(ps.getSimilarPatients(name, d, gender));
			
			patientList = new Vector<Object>(patients.size());
			for (Patient p : patients) {
				patientList.add(new PatientListItem(p));
			}
		}
		return patientList;
	}
	
	public Vector findTribes(String search) {
		HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
		
		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		Vector<Object> tribeList = new Vector<Object>();
		
		if (context == null) {
			tribeList.add("Your session has expired.");
			tribeList.add("Please <a href='" + request.getContextPath() + "/logout'>log in</a> again.");
		}
		else {
			try {
				tribeList.addAll(context.getFormEntryService().findTribes(search));
			} catch (Exception e) {
				log.error(e);
				tribeList.add("Error while attempting to find tribe - " + e.getMessage());
			}
		}
		
		return tribeList;
			
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Tribe> getTribes() {
		HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
		
		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		Vector tribeList = new Vector();
		
		if (context == null) {
			tribeList.add("Your session has expired.");
			tribeList.add("Please <a href='" + request.getContextPath() + "/logout'>log in</a> again.");
		}
		else {
			try {
				tribeList.addAll(context.getFormEntryService().getTribes());
			} catch (Exception e) {
				log.error(e);
				tribeList.add("Error while attempting to find tribe - " + e.getMessage());
			}
		}
		
		return tribeList;
	}
	
	/**
	 * find all patients with duplicate attributes (searchOn)
	 *   
	 * @param searchOn
	 * @return list of patientListItems
	 */
	public Vector findDuplicatePatients(String[] searchOn) {
		HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
		
		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

		Vector<Object> patientList = new Vector<Object>();
		
		if (context == null) {
			patientList.add("Your session has expired.");
			patientList.add("Please <a href='" + request.getContextPath() + "/logout'>log in</a> again.");
		}
		else {
			try {
				Set<String> options = new HashSet<String>(searchOn.length);
				for (String s : searchOn)
					options.add(s);
				
				List<Patient> patients = context.getPatientService().findDuplicatePatients(options);
				
				if (patients.size() > 200)
					patients.subList(0, 200);
				
				for (Patient p : patients)
					patientList.add(new PatientListItem(p));
			} catch (Exception e) {
				log.error(e);
				patientList.add("Error while attempting to find duplicate patients - " + e.getMessage());
			}
		}
		
		return patientList;
	}

}
