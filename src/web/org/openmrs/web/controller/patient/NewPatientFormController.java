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
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientAddress;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientName;
import org.openmrs.Person;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.Tribe;
import org.openmrs.api.APIException;
import org.openmrs.api.DuplicateIdentifierException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.IdentifierNotUniqueException;
import org.openmrs.api.InsufficientIdentifiersException;
import org.openmrs.api.InvalidCheckDigitException;
import org.openmrs.api.InvalidIdentifierFormatException;
import org.openmrs.api.PatientIdentifierException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.openmrs.web.propertyeditor.ConceptEditor;
import org.openmrs.web.propertyeditor.LocationEditor;
import org.openmrs.web.propertyeditor.TribeEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
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
        binder.registerCustomEditor(Concept.class, "causeOfDeath", new ConceptEditor());
	}

	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
	
		ShortPatientModel pli = (ShortPatientModel)obj;
		
		if (Context.isAuthenticated()) {
			PatientService ps = Context.getPatientService();
			EncounterService es = Context.getEncounterService();
			MessageSourceAccessor msa = getMessageSourceAccessor();
			
			if (request.getParameter("action") == null || request.getParameter("action").equals(msa.getMessage("general.save"))) {
				ValidationUtils.rejectIfEmptyOrWhitespace(errors, "familyName", "error.name");
				
				String[] identifiers = request.getParameterValues("identifier");
				String[] types = request.getParameterValues("identifierType");
				String[] locs = request.getParameterValues("location");
				pref = request.getParameter("preferred");
				if (pref == null)
					pref = "";
				
				if (log.isDebugEnabled()) {
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
				}
				
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
							loc = es.getLocation(Integer.valueOf(locs[i]));
						
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
			PatientService ps = Context.getPatientService();
			ShortPatientModel p = (ShortPatientModel)obj;
			String view = getSuccessView();
			boolean isError = false;
			
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
			
			try {
				ps.updatePatient(patient);
			} catch ( InvalidIdentifierFormatException iife ) {
				log.error(iife);
				patient.setIdentifiers(null);
				p.setIdentifier(null);
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.formatInvalid");
				//errors = new BindException(new InvalidIdentifierFormatException(msa.getMessage("PatientIdentifier.error.formatInvalid")), "givenName");
				isError = true;
			} catch ( InvalidCheckDigitException icde ) {
				log.error(icde);
				patient.setIdentifiers(null);
				p.setIdentifier(null);
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.checkDigit");
				//errors = new BindException(new InvalidCheckDigitException(msa.getMessage("PatientIdentifier.error.checkDigit")), "givenName");
				isError = true;
			} catch ( IdentifierNotUniqueException inue ) {
				log.error(inue);
				patient.setIdentifiers(null);
				p.setIdentifier(null);
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.notUnique");
				//errors = new BindException(new IdentifierNotUniqueException(msa.getMessage("PatientIdentifier.error.notUnique")), "givenName");
				isError = true;
			} catch ( DuplicateIdentifierException die ) {
				log.error(die);
				patient.setIdentifiers(null);
				p.setIdentifier(null);
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.duplicate");
				//errors = new BindException(new DuplicateIdentifierException(msa.getMessage("PatientIdentifier.error.duplicate")), "givenName");
				isError = true;
			} catch ( InsufficientIdentifiersException iie ) {
				log.error(iie);
				patient.setIdentifiers(null);
				p.setIdentifier(null);
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.insufficientIdentifiers");
				//errors = new BindException(new InsufficientIdentifiersException(msa.getMessage("PatientIdentifier.error.insufficientIdentifiers")), "givenName");
				isError = true;
			} catch ( PatientIdentifierException pie ) {
				log.error(pie);
				patient.setIdentifiers(null);
				p.setIdentifier(null);
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.general");
				//errors = new BindException(new PatientIdentifierException(msa.getMessage("PatientIdentifier.error.general")), "givenName");
				isError = true;
			}
						
			// update patient's relationships
			if ( !isError ) {
				String[] relatives = request.getParameterValues("relative");
				String[] types = request.getParameterValues("relationshipType");
				Person person = Context.getAdministrationService().getPerson(patient);
				List<Relationship> relationships;
				List<Person> newRelatives = new Vector<Person>(); //list of all persons specifically selected in the form
				
				if (person != null) 
					relationships = Context.getPatientService().getRelationships(person);
				else
					relationships = new Vector<Relationship>();
				
				if ( relatives != null ) {
					for (int x = 0 ; x < relatives.length; x++ ) {
						String relativeString = relatives[x];
						String typeString = types[x];
						
						if (relativeString != null && relativeString.length() > 0 && typeString != null && typeString.length() > 0) {
							Patient relativePatient = ps.getPatient(Integer.valueOf(relativeString));
							RelationshipType type = ps.getRelationshipType(Integer.valueOf(typeString));
							
							Person relative = Context.getAdministrationService().getPerson(relativePatient);
							newRelatives.add(relative);
							
							boolean found = false;
							// TODO this assumes that a relative can only be related in one way
							for (Relationship rel : relationships) {
								//skip the relationships where this patient is the object
								if (rel.getPerson().equals(person))
									found = true;
								
								// just update the type of relationships that have the same relative
								if (rel.getPerson().equals(relative)) {
									rel.setRelationship(type);
									found = true;
								}
							}
							if (!found) {
								Relationship r = new Relationship(relative, person, type);
								relationships.add(r);
							}
						}
					}
					
				}
	
				for (Relationship rel : relationships) {
					if (newRelatives.contains(rel.getPerson()) || 
							person.equals(rel.getPerson()))
						Context.getAdministrationService().updateRelationship(rel);
					else
						Context.getAdministrationService().deleteRelationship(rel);
				}
				
				
				if ( patient.getDead() ) {
					log.debug("Patient is dead, so let's make sure there's an Obs for it");
					// need to make sure there is an Obs that represents the patient's cause of death, if applicable
	
					String codProp = Context.getAdministrationService().getGlobalProperty("concept.causeOfDeath");
					Concept causeOfDeath = Context.getConceptService().getConceptByIdOrName(codProp);
	
					if ( causeOfDeath != null ) {
						Set<Obs> obssDeath = Context.getObsService().getObservations(patient, causeOfDeath);
						if ( obssDeath != null ) {
							if ( obssDeath.size() > 1 ) {
								log.error("Multiple causes of death (" + obssDeath.size() + ")?  Shouldn't be...");
							} else {
								Obs obsDeath = null;
								if ( obssDeath.size() == 1 ) {
									// already has a cause of death - let's edit it.
									log.debug("Already has a cause of death, so changing it");
									
									obsDeath = obssDeath.iterator().next();
									
								} else {
									// no cause of death obs yet, so let's make one
									log.debug("No cause of death yet, let's create one.");
									
									obsDeath = new Obs();
									obsDeath.setPatient(patient);
									obsDeath.setConcept(causeOfDeath);
									Location loc = Context.getEncounterService().getLocationByName("Unknown Location");
									if ( loc == null ) loc = Context.getEncounterService().getLocation(new Integer(1));
									if ( loc == null ) loc = patient.getHealthCenter();
									if ( loc != null ) obsDeath.setLocation(loc);
									else log.error("Could not find a suitable location for which to create this new Obs");
								}
								
								// put the right concept and (maybe) text in this obs
								Concept currCause = patient.getCauseOfDeath();
								if ( currCause == null ) {
									// set to NONE
									log.debug("Current cause is null, attempting to set to NONE");
									String noneConcept = Context.getAdministrationService().getGlobalProperty("concept.none");
									currCause = Context.getConceptService().getConceptByIdOrName(noneConcept);
								}
								
								if ( currCause != null ) {
									log.debug("Current cause is not null, setting to value_coded");
									obsDeath.setValueCoded(currCause);
									
									Date dateDeath = patient.getDeathDate();
									if ( dateDeath == null ) dateDeath = new Date();
									obsDeath.setObsDatetime(dateDeath);
	
									// check if this is an "other" concept - if so, then we need to add value_text
									String otherConcept = Context.getAdministrationService().getGlobalProperty("concept.otherNonCoded");
									Concept conceptOther = Context.getConceptService().getConceptByIdOrName(otherConcept);
									if ( conceptOther != null ) {
										if ( conceptOther.equals(currCause) ) {
											// seems like this is an other concept - let's try to get the "other" field info
											String otherInfo = ServletRequestUtils.getStringParameter(request, "causeOfDeath_other", "");
											log.debug("Setting value_text as " + otherInfo);
											obsDeath.setValueText(otherInfo);
										} else {
											log.debug("New concept is NOT the OTHER concept, so setting to blank");
											obsDeath.setValueText("");
										}
									} else {
										log.debug("Don't seem to know about an OTHER concept, so deleting value_text");
										obsDeath.setValueText("");
									}
									
									Context.getObsService().updateObs(obsDeath);
								} else {
									log.debug("Current cause is still null - aborting mission");
								}
							}
						}
					} else {
						log.debug("Cause of death is null - should not have gotten here without throwing an error on the form.");
					}
					
				}
			}
			
			if ( isError ) {
				log.error("REDIRECTING TO " + this.getFormView());
				
				return this.showForm(request, response, errors);
				//return new ModelAndView(new RedirectView(getFormView()));
			} else {
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Patient.saved");
				return new ModelAndView(new RedirectView(view + "?patientId=" + patient.getPatientId()));
			}
		} else {
			return new ModelAndView(new RedirectView(getFormView()));
		}
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
			PatientService ps = Context.getPatientService();
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
		
		Person person = Context.getAdministrationService().getPerson(p);
		patient.setRelationships(Context.getPatientService().getRelationships(person));
		
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
			PatientService ps = Context.getPatientService();
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