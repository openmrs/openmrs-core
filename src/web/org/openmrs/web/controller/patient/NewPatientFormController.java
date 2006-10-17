package org.openmrs.web.controller.patient;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientAddress;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientName;
import org.openmrs.Tribe;
import org.openmrs.api.context.Context;
import org.openmrs.formentry.FormEntryService;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.openmrs.web.propertyeditor.LocationEditor;
import org.openmrs.web.propertyeditor.TribeEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class NewPatientFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
    
    SimpleDateFormat dateFormat;
    
    // identifiers submitted with the form.  Stored here so that they can
    // be redisplayed for the user after an error
    Set<PatientIdentifier> newIdentifiers = new HashSet<PatientIdentifier>();
    String pref = "";
    
	/**
	 * 
	 * Allows for other Objects to be used as values in input tags.
	 *   Normally, only strings and lists are expected 
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest, org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		
		
		dateFormat = new SimpleDateFormat(OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS().get(Context.getLocale().toString().toLowerCase()), Context.getLocale());
		
        NumberFormat nf = NumberFormat.getInstance(Context.getLocale());
        binder.registerCustomEditor(java.lang.Integer.class,
                new CustomNumberEditor(java.lang.Integer.class, nf, true));
        binder.registerCustomEditor(java.util.Date.class, 
        		new CustomDateEditor(dateFormat, true, 10));
        binder.registerCustomEditor(Tribe.class, new TribeEditor());
        binder.registerCustomEditor(Location.class, new LocationEditor());
	}

	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
	
		ShortPatientModel pli = (ShortPatientModel)obj;
		
		if (Context.isAuthenticated()) {
			FormEntryService ps = Context.getFormEntryService();
			MessageSourceAccessor msa = getMessageSourceAccessor();
			
			if (request.getParameter("action") == null || request.getParameter("action").equals(msa.getMessage("general.save"))) {
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "familyName", "error.name");
				
				String[] identifiers = request.getParameterValues("identifier");
				String[] types = request.getParameterValues("identifierType");
				String[] locs = request.getParameterValues("location");
				pref = request.getParameter("preferred");
				if (pref == null)
					pref = "";
				
				log.debug("identifiers: " + identifiers);
				for (String s : identifiers)
					log.debug(s);
				log.debug("types: " + types);
				for (String s : types)
					log.debug(s);
				log.debug("locations: " + locs);
				for (String s : locs)
					log.debug(s);
				log.debug("preferred: " + pref);
				
				// loop over the identifiers to create the patient.identifiers set
				for (int i=0; i<identifiers.length;i++) {
					// arguments for the spring error messages
					String[] args = {identifiers[i]};
					
					// add the new identifier only if they put in some identifier string
					if (identifiers[i].length() > 0) {
						
						// set up the actual identifier java object
						PatientIdentifierType pit = null;
						if (types[i] == null || types[i].equals("")) {
							String msg = getMessageSourceAccessor().getMessage("PatientIdentifier.identifierType.null", args);
							errors.reject(msg);
						}
						else
							pit = ps.getPatientIdentifierType(Integer.valueOf(types[i]));
						
						Location loc = null;
						if (locs[i] == null || locs[i].equals("")) {
							String msg = getMessageSourceAccessor().getMessage("PatientIdentifier.location.null", args);
							errors.reject(msg);
						}
						else
							loc = ps.getLocation(Integer.valueOf(locs[i]));
						
						PatientIdentifier pi = new PatientIdentifier(identifiers[i], pit, loc);
						pi.setPreferred(pref.equals(identifiers[i]+types[i]));
						newIdentifiers.add(pi);
						
						log.debug("Creating patient identifier with identifier: " + identifiers[i]);
						log.debug("and type: " + types[i]);
						log.debug("and location: " + locs[i]);
					
						try {
							if (pit.hasCheckDigit() && !OpenmrsUtil.isValidCheckDigit(identifiers[i])) {
								log.error("hasCheckDigit and is not valid: " + pit.getName() + " " + identifiers[i]);
								String msg = getMessageSourceAccessor().getMessage("error.checkdigits.verbose", args);
								errors.rejectValue("identifier", msg);
							}
							else if (pit.hasCheckDigit() == false && identifiers[i].contains("-")) {
								log.error("hasn't CheckDigit and contains '-': " + pit.getName() + " " + identifiers[i]);
								String[] args2 = {"-", identifiers[i]}; 
								String msg = getMessageSourceAccessor().getMessage("error.character.invalid", args2);
								errors.rejectValue("identifier", msg);
							}
						} catch (Exception e) {
							log.error("exception thrown with: " + pit.getName() + " " + identifiers[i]);
							log.error("Error while adding patient identifiers to savedIdentifier list", e);
							String msg = getMessageSourceAccessor().getMessage("error.checkdigits", args);
							errors.rejectValue("identifier", msg);
						}
					}
				}
			}
			
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "gender", "error.null");
			
			// check patients birthdate against future dates and really old dates
			if (pli.getBirthdate() != null) {
				if (pli.getBirthdate().after(new Date()))
					errors.rejectValue("birthdate", "error.date.future");
				else {
					Calendar c = Calendar.getInstance();
					c.setTime(new Date());
					c.add(Calendar.YEAR, -120); // patient cannot be older than 120 years old 
					if (pli.getBirthdate().before(c.getTime())){
						errors.rejectValue("birthdate", "error.date.nonsensical");
					}
				}
			}
		}
			
		return super.processFormSubmission(request, response, pli, errors);
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
		
		
		if (Context.isAuthenticated()) {
			FormEntryService ps = Context.getFormEntryService();
			ShortPatientModel p = (ShortPatientModel)obj;
			String view = getSuccessView();
			
			MessageSourceAccessor msa = getMessageSourceAccessor();
			if (request.getParameter("action") != null && request.getParameter("action").equals(msa.getMessage("general.cancel"))) {
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "general.canceled");
				return new ModelAndView(new RedirectView("addPatient.htm"));
			}
			
			Patient patient = new Patient();
			if (p.getPatientId() != null)
				patient = ps.getPatient(p.getPatientId());
			boolean duplicate = false;
			for (PatientName pn : patient.getNames()) {
				if (((pn.getGivenName() == null && p.getGivenName() == null) || pn.getGivenName().equals(p.getGivenName())) &&
					((pn.getMiddleName() == null && p.getMiddleName() == null) || pn.getMiddleName().equals(p.getMiddleName())) &&
					((pn.getFamilyName() == null && p.getFamilyName() == null) || pn.getFamilyName().equals(p.getFamilyName())))
					duplicate = true;
			}
			
			if (!duplicate)
				patient.addName(new PatientName(p.getGivenName(), p.getMiddleName(), p.getFamilyName()));
			
			log.debug("address: " + p.getAddress());
			if (p.getAddress() != null && !p.getAddress().isBlank()) {
				duplicate = false;
				for (PatientAddress pa : patient.getAddresses()) {
					if (pa.equals(p.getAddress()))
						duplicate = true;
				}
				log.debug("duplicate:  " + duplicate);
				if (!duplicate) {
					patient.addAddress(p.getAddress());
				}
			}
			log.debug("patient addresses: " + patient.getAddresses());
			
			// set or unset the preferred bit for the old identifiers if needed
			if (patient.getIdentifiers() == null)
				patient.setIdentifiers(new HashSet<PatientIdentifier>());
			
			for (PatientIdentifier pi : patient.getIdentifiers()) {
				pi.setPreferred(pref.equals(pi.getIdentifier()+pi.getIdentifierType().getPatientIdentifierTypeId()));
			}
			
			// add the new identifiers
			patient.getIdentifiers().addAll(newIdentifiers);
			
			// set the other patient attributes
			patient.setBirthdate(p.getBirthdate());
			patient.setBirthdateEstimated(p.getBirthdateEstimated());
			patient.setGender(p.getGender());
			patient.setHealthCenter(p.getHealthCenter());
			patient.setMothersName(p.getMothersName());
			if (p.getTribe() == "" || p.getTribe() == null)
				patient.setTribe(null);
			else {
				Tribe t = ps.getTribe(Integer.valueOf(p.getTribe()));
				patient.setTribe(t);
			}
			
			patient.setDead(p.getDead());
			if (patient.isDead()) {
				patient.setDeathDate(p.getDeathDate());
				patient.setCauseOfDeath(p.getCauseOfDeath());
			}
			else {
				patient.setDeathDate(null);
				patient.setCauseOfDeath(null);
			}
			
			ps.updatePatient(patient);
						
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Patient.saved");
			return new ModelAndView(new RedirectView(view + "?patientId=" + patient.getPatientId()));
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
		
		Patient p = null;
		
		if (Context.isAuthenticated()) {
			FormEntryService ps = Context.getFormEntryService();
			String patientId = request.getParameter("pId");
	    	if (patientId != null && !patientId.equals("")) {
	    		p = ps.getPatient(Integer.valueOf(patientId));
	    	}
		}
		
		ShortPatientModel patient = new ShortPatientModel(p);
		
		String name = request.getParameter("name");
		if (p == null && name != null) {
			String firstName = name;
			String middleName = "";
			String lastName = "";
			
			if (name.contains(",")) {
				String[] names = name.split(", ");
				String[] firstNames = names[1].split(" ");
				if (firstNames.length == 2) {
					lastName = names[0];
					firstName = firstNames[0];
					middleName = firstNames[1];
				}
				else {
					firstName = names[1];
					lastName = names[2];
				}
			}
			else if (name.contains(" ")) {
				String[] names = name.split(" ");
				if (names.length == 3) {
					firstName = names[0];
					middleName = names[1];
					lastName = names[2];
				}
				else {
					firstName = names[0];
					lastName = names[1];
				}
			}
			patient.setGivenName(firstName);
			patient.setMiddleName(middleName);
			patient.setFamilyName(lastName);
			
			patient.setGender(request.getParameter("gndr"));
			Date birthdate = null;
			boolean birthdateEstimated = false;
			String date = request.getParameter("birthyear");
			String age = request.getParameter("age");
			if (date != null && !date.equals("")) {
				try {
					birthdate = DateFormat.getDateInstance(DateFormat.SHORT).parse("01/01/" + date);
					birthdateEstimated = true;
				} catch (ParseException e) { log.debug(e); }
			}
			else if (age != null && !age.equals("")) {
				Calendar c = Calendar.getInstance();
				c.setTime(new Date());
				Integer d = c.get(Calendar.YEAR);
				d = d - Integer.parseInt(age);
				try {
					birthdate = DateFormat.getDateInstance(DateFormat.SHORT).parse("01/01/" + d);
					birthdateEstimated = true;
				} catch (ParseException e) { log.debug(e); }
			}
			if (birthdate != null)
				patient.setBirthdate(birthdate);
			patient.setBirthdateEstimated(birthdateEstimated);
		}
		
		if (patient.getAddress() == null) {
			PatientAddress pa = new PatientAddress();
			pa.setPreferred(true);
			patient.setAddress(pa);
		}
		
        return patient;
    }
    
	/**
	 * 
	 * Called prior to form display.  Allows for data to be put 
	 * 	in the request to be used in the view
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
	 */
	protected Map referenceData(HttpServletRequest request) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();

		List<PatientIdentifier> identifiers = new Vector<PatientIdentifier>();
		
		Patient patient = null;
		
		if (Context.isAuthenticated()) {
			FormEntryService ps = Context.getFormEntryService();
			String patientId = request.getParameter("pId");
	    	if (patientId != null && !patientId.equals("")) {
	    		patient = ps.getPatient(Integer.valueOf(patientId));
	    		identifiers.addAll(patient.getIdentifiers());
	    	}
		}
		map.put("datePattern", dateFormat.toLocalizedPattern().toLowerCase());
		
		// give them both the just-entered identifiers and the patient's current identifiers
		identifiers.addAll(newIdentifiers);
		
		if (pref.length() > 0)
			for (PatientIdentifier pi : identifiers)
				pi.setPreferred(pref.equals(pi.getIdentifier()+pi.getIdentifierType().getPatientIdentifierTypeId()));
		
		map.put("identifiers", identifiers);
		
		return map;
	}   
	
}