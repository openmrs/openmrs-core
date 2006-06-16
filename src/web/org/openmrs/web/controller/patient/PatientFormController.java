package org.openmrs.web.controller.patient;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientAddress;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientName;
import org.openmrs.Tribe;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.formentry.FormEntryService;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.openmrs.web.propertyeditor.ConceptEditor;
import org.openmrs.web.propertyeditor.LocationEditor;
import org.openmrs.web.propertyeditor.PatientIdentifierTypeEditor;
import org.openmrs.web.propertyeditor.TribeEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.RequestUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class PatientFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    SimpleDateFormat dateFormat;
    
	/**
	 * 
	 * Allows for other Objects to be used as values in input tags.
	 *   Normally, only strings and lists are expected 
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest, org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		Context context = (Context) request.getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		dateFormat = new SimpleDateFormat(OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS().get(context.getLocale().toString().toLowerCase()), context.getLocale());
		
        NumberFormat nf = NumberFormat.getInstance(context.getLocale());
        binder.registerCustomEditor(java.lang.Integer.class,
                new CustomNumberEditor(java.lang.Integer.class, nf, true));
        binder.registerCustomEditor(java.util.Date.class, 
        		new CustomDateEditor(dateFormat, true, 10));
        binder.registerCustomEditor(Tribe.class, new TribeEditor(context));
        binder.registerCustomEditor(PatientIdentifierType.class, new PatientIdentifierTypeEditor(context));
        binder.registerCustomEditor(Location.class, new LocationEditor(context));
        binder.registerCustomEditor(Concept.class, "civilStatus", new ConceptEditor(context));
	}

	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#processFormSubmission(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object, BindException errors) throws Exception {
	
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		Patient patient = (Patient)object;
		
		if (context != null && context.isAuthenticated()) {
			
			FormEntryService ps = context.getFormEntryService();
			Object[] objs = null;
			
			MessageSourceAccessor msa = getMessageSourceAccessor();
			String action = request.getParameter("action");
			
			if (!action.equals(msa.getMessage("Patient.delete"))) {
			
				// Patient Identifiers 
					objs = patient.getIdentifiers().toArray();
					for (int i = 0; i < objs.length; i++ ) {
						if (request.getParameter("identifiers[" + i + "].identifier") == null)
							patient.removeIdentifier((PatientIdentifier)objs[i]);
					}
					
					String[] ids = request.getParameterValues("identifier");
					String[] idTypes = request.getParameterValues("identifierType");
					String[] locs = request.getParameterValues("location");
					
					if (ids != null) {
						for (int i = 0; i < ids.length; i++) {
							String id = ids[i].trim();
							if (!id.equals("") && !idTypes.equals("")) { //skips invalid and blank identifiers/identifierTypes
								PatientIdentifier pi = new PatientIdentifier();
								pi.setIdentifier(id);
								pi.setIdentifierType(ps.getPatientIdentifierType(Integer.valueOf(idTypes[i])));
								pi.setLocation(ps.getLocation(Integer.valueOf(locs[i])));
								patient.addIdentifier(pi);
							}
						}
					}
					
				// Patient Address
				
					String [] add1s = RequestUtils.getStringParameters(request, "address1");
					String [] add2s = RequestUtils.getStringParameters(request, "address2");
					String [] cities = RequestUtils.getStringParameters(request, "cityVillage");
					String [] states = RequestUtils.getStringParameters(request, "stateProvince");
					String [] countries = RequestUtils.getStringParameters(request, "country");
					String [] lats = RequestUtils.getStringParameters(request, "latitude");
					String [] longs = RequestUtils.getStringParameters(request, "longitude");
					
					if (add1s != null) {
						for (int i = 0; i < add1s.length; i++) {
							if (add1s[i] != "") { //skips invalid and blank address data box
								PatientAddress pa = new PatientAddress();
								if (add1s.length >= i+1)
									pa.setAddress1(add1s[i]);
								if (add2s.length >= i+1)
									pa.setAddress2(add2s[i]);
								if (cities.length >= i+1)
									pa.setCityVillage(cities[i]);
								if (states.length >= i+1)
									pa.setStateProvince(states[i]);
								if (countries.length >= i+1)
									pa.setCountry(countries[i]);
								if (lats.length >= i+1)
									pa.setLatitude(lats[i]);
								if (longs.length >= i+1)
									pa.setLongitude(longs[i]);
								patient.addAddress(pa);
							}
						}
					}
						
				// Patient Names
	
					objs = patient.getNames().toArray();
					for (int i = 0; i < objs.length; i++ ) {
						if (request.getParameter("names[" + i + "].givenName") == null)
							patient.removeName((PatientName)objs[i]);
					}
	
					//String[] prefs = request.getParameterValues("preferred");  (unreliable form info)
					String[] gNames = RequestUtils.getStringParameters(request, "givenName");
					String[] mNames = RequestUtils.getStringParameters(request, "middleName");
					String[] fNamePrefixes = RequestUtils.getStringParameters(request, "familyNamePrefix");
					String[] fNames = RequestUtils.getStringParameters(request, "familyName");
					String[] fName2s = RequestUtils.getStringParameters(request, "familyName2");
					String[] fNameSuffixes = RequestUtils.getStringParameters(request, "familyNameSuffix");
					String[] degrees = RequestUtils.getStringParameters(request, "degree");
					
					if (gNames != null) {
						for (int i = 0; i < gNames.length; i++) {
							if (gNames[i] != "") { //skips invalid and blank address data box
								PatientName pn = new PatientName();
								pn.setPreferred(false);
								if (gNames.length >= i+1)
									pn.setGivenName(gNames[i]);
								if (mNames.length >= i+1)
									pn.setMiddleName(mNames[i]);
								if (fNamePrefixes.length >= i+1)
									pn.setFamilyNamePrefix(fNamePrefixes[i]);
								if (fNames.length >= i+1)
									pn.setFamilyName(fNames[i]);
								if (fName2s.length >= i+1)
									pn.setFamilyName2(fName2s[i]);
								if (fNameSuffixes.length >= i+1)
									pn.setFamilyNameSuffix(fNameSuffixes[i]);
								if (degrees.length >= i+1)
									pn.setDegree(degrees[i]);
								patient.addName(pn);
							}
						}
					}
					
					if (patient.getNames().size() < 1)
						errors.rejectValue("patient.names", "Patient.names.length");
					
				// Patient Info 
					//ValidationUtils.rejectIfEmptyOrWhitespace(errors, "birthdate", "error.null");
					if (patient.isVoided())
						ValidationUtils.rejectIfEmptyOrWhitespace(errors, "voidReason", "error.null");
					if (patient.isDead() && (patient.getCauseOfDeath() == null || patient.getCauseOfDeath().equals("")))
						errors.rejectValue("causeOfDeath", "Patient.dead.causeOfDeathNull");
			}
		}		
		
		return super.processFormSubmission(request, response, patient, errors); 
	}

	/**
	 * 
	 * The onSubmit function receives the form/command object that was modified
	 *   by the input form and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		Patient patient = (Patient)obj;
				
		if (context != null && context.isAuthenticated()) {

			MessageSourceAccessor msa = getMessageSourceAccessor();
			String action = request.getParameter("action");
			PatientService ps = context.getPatientService();
						
			if (action.equals(msa.getMessage("Patient.delete"))) {
				try {
					ps.deletePatient(patient);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Patient.deleted");
					return new ModelAndView(new RedirectView("index.htm"));
				}
				catch (APIException e) {
					log.error(e);
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Patient.cannot.delete");
					return new ModelAndView(new RedirectView(getSuccessView() + "?patientId=" + patient.getPatientId().toString()));
				}
			}
			else {
				boolean isNew = (patient.getPatientId() == null);
				
				context.getFormEntryService().updatePatient(patient);
				
				String view = getSuccessView();
							
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Patient.saved");
				
				view = view + "?patientId=" + patient.getPatientId();
				return new ModelAndView(new RedirectView(view));
			}
		}
		return new ModelAndView(new RedirectView(getFormView()));
	}

	/**
	 * 
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {

		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		Patient patient = null;
		
		if (context != null && context.isAuthenticated()) {
			FormEntryService ps = context.getFormEntryService();
			String patientId = request.getParameter("patientId");
	    	if (patientId != null) {
	    		patient = ps.getPatient(Integer.valueOf(patientId));
	    	}
		}

		if (patient == null)
			patient = new Patient();
		
        return patient;
    }

	/**
	 * 
	 * Called prior to form display.  Allows for data to be put 
	 * 	in the request to be used in the view
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
	 */
	protected Map referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		Patient patient = (Patient)obj;
		List<Form> forms = new Vector<Form>();
		Map<String, Object> map = new HashMap<String, Object>();
		List<Encounter> encounters = new Vector<Encounter>();

		if (context != null && context.isAuthenticated()) {
			forms.addAll(context.getFormEntryService().getForms());
			Set<Encounter> encs = context.getEncounterService().getEncounters(patient);
			if (encs != null && encs.size() > 0)
				encounters.addAll(encs);
		}
			
		map.put("forms", forms);
				
		// empty objects used to create blank template in the view
		map.put("emptyIdentifier", new PatientIdentifier());
		map.put("emptyName", new PatientName());
		map.put("emptyAddress", new PatientAddress());
		map.put("encounters", encounters);
		map.put("datePattern", dateFormat.toLocalizedPattern().toLowerCase());
		
		return map;
	}    
	
}