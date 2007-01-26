package org.openmrs.web.controller.patient;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientAddress;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientName;
import org.openmrs.Tribe;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
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
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
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
		
		
		dateFormat = new SimpleDateFormat(OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS().get(Context.getLocale().toString().toLowerCase()), Context.getLocale());
		
        NumberFormat nf = NumberFormat.getInstance(Context.getLocale());
        binder.registerCustomEditor(java.lang.Integer.class,
                new CustomNumberEditor(java.lang.Integer.class, nf, true));
        binder.registerCustomEditor(java.util.Date.class, 
        		new CustomDateEditor(dateFormat, true, 10));
        binder.registerCustomEditor(Tribe.class, new TribeEditor());
        binder.registerCustomEditor(PatientIdentifierType.class, new PatientIdentifierTypeEditor());
        binder.registerCustomEditor(Location.class, new LocationEditor());
        binder.registerCustomEditor(Concept.class, "civilStatus", new ConceptEditor());
        binder.registerCustomEditor(Concept.class, "causeOfDeath", new ConceptEditor());
	}

	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#processFormSubmission(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object, BindException errors) throws Exception {
	
		Patient patient = (Patient)object;
		
		if (Context.isAuthenticated()) {
			
			PatientService ps = Context.getPatientService();
			EncounterService es = Context.getEncounterService();
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
								pi.setLocation(es.getLocation(Integer.valueOf(locs[i])));
								patient.addIdentifier(pi);
							}
						}
					}
					
				// Patient Address
				
					String [] add1s = ServletRequestUtils.getStringParameters(request, "address1");
					String [] add2s = ServletRequestUtils.getStringParameters(request, "address2");
					String [] cities = ServletRequestUtils.getStringParameters(request, "cityVillage");
					String [] states = ServletRequestUtils.getStringParameters(request, "stateProvince");
					String [] countries = ServletRequestUtils.getStringParameters(request, "country");
					String [] lats = ServletRequestUtils.getStringParameters(request, "latitude");
					String [] longs = ServletRequestUtils.getStringParameters(request, "longitude");
					String [] pCodes = ServletRequestUtils.getStringParameters(request, "postalCode");
					String [] counties = ServletRequestUtils.getStringParameters(request, "countyDistrict");
					String [] cells = ServletRequestUtils.getStringParameters(request, "neighborhoodCell");
					
					if (add1s != null || add2s != null || cities != null || states != null || countries != null
							|| lats != null || longs != null || pCodes != null || counties != null || cells != null ) {
						int maxAddrs = 0;

						if ( add1s != null ) if ( add1s.length > maxAddrs ) maxAddrs = add1s.length;
						if ( add2s != null ) if ( add2s.length > maxAddrs ) maxAddrs = add2s.length;
						if ( cities != null ) if ( cities.length > maxAddrs ) maxAddrs = cities.length;
						if ( states != null ) if ( states.length > maxAddrs ) maxAddrs = states.length;
						if ( countries != null ) if ( countries.length > maxAddrs ) maxAddrs = countries.length;
						if ( lats != null ) if ( lats.length > maxAddrs ) maxAddrs = lats.length;
						if ( longs != null ) if ( longs.length > maxAddrs ) maxAddrs = longs.length;
						if ( pCodes != null ) if ( pCodes.length > maxAddrs ) maxAddrs = pCodes.length;
						if ( counties != null ) if ( counties.length > maxAddrs ) maxAddrs = counties.length;
						if ( cells != null ) if ( cells.length > maxAddrs ) maxAddrs = cells.length;
						
						log.debug("There appears to be " + maxAddrs + " addresses that need to be saved");
						
						for (int i = 0; i < maxAddrs; i++) {
/*
							if ( add1s[i] != null || add2s[i] != null || cities[i] != null || states[i] != null 
									|| countries[i] != null || lats[i] != null || longs[i] != null
									|| pCodes[i] != null || counties[i] != null || cells[i] != null ) {
*/							
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
								if (pCodes.length >= i+1)
									pa.setPostalCode(pCodes[i]);
								if (counties.length >= i+1)
									pa.setCountyDistrict(counties[i]);
								if (cells.length >= i+1)
									pa.setNeighborhoodCell(cells[i]);
								patient.addAddress(pa);
//							}
						}
					}
						
				// Patient Names
	
					objs = patient.getNames().toArray();
					for (int i = 0; i < objs.length; i++ ) {
						if (request.getParameter("names[" + i + "].givenName") == null)
							patient.removeName((PatientName)objs[i]);
					}
	
					//String[] prefs = request.getParameterValues("preferred");  (unreliable form info)
					String[] gNames = ServletRequestUtils.getStringParameters(request, "givenName");
					String[] mNames = ServletRequestUtils.getStringParameters(request, "middleName");
					String[] fNamePrefixes = ServletRequestUtils.getStringParameters(request, "familyNamePrefix");
					String[] fNames = ServletRequestUtils.getStringParameters(request, "familyName");
					String[] fName2s = ServletRequestUtils.getStringParameters(request, "familyName2");
					String[] fNameSuffixes = ServletRequestUtils.getStringParameters(request, "familyNameSuffix");
					String[] degrees = ServletRequestUtils.getStringParameters(request, "degree");
					
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
						errors.rejectValue("names", "Patient.names.length");
					
				// Patient Info 
					//ValidationUtils.rejectIfEmptyOrWhitespace(errors, "birthdate", "error.null");
					if (patient.isVoided())
						ValidationUtils.rejectIfEmptyOrWhitespace(errors, "voidReason", "error.null");
					if (patient.isDead() && (patient.getCauseOfDeath() == null))
						errors.rejectValue("causeOfDeath", "Patient.dead.causeOfDeathNull");
					
				// check patients birthdate against future dates and really old dates
					if (patient.getBirthdate() != null) {
						if (patient.getBirthdate().after(new Date()))
							errors.rejectValue("birthdate", "error.date.future");
						else {
							Calendar c = Calendar.getInstance();
							c.setTime(new Date());
							c.add(Calendar.YEAR, -120); // patient cannot be older than 120 years old 
							if (patient.getBirthdate().before(c.getTime())){
								errors.rejectValue("birthdate", "error.date.nonsensical");
							}
						}
					}
				
				// check patient identifier checkdigits
					for (PatientIdentifier pi : patient.getIdentifiers()) {
						// skip voided identifiers
						if (pi.isVoided()) continue;
						PatientIdentifierType pit = pi.getIdentifierType();
						String identifier = pi.getIdentifier();
						String[] args = {identifier};
						try {
							if (pit.hasCheckDigit() && !OpenmrsUtil.isValidCheckDigit(identifier)) {
								log.error("hasCheckDigit and is not valid: " + pit.getName() + " " + identifier);
								String msg = getMessageSourceAccessor().getMessage("error.checkdigits.verbose", args);
								errors.rejectValue("identifiers", msg);
							}
							else if (pit.hasCheckDigit() == false && identifier.contains("-")) {
								log.error("hasn't CheckDigit and contains '-': " + pit.getName() + " " + identifier);
								String[] args2 = {"-", identifier}; 
								String msg = getMessageSourceAccessor().getMessage("error.character.invalid", args2);
								errors.rejectValue("identifiers", msg);
							}
						} catch (Exception e) {
							log.error("exception thrown with: " + pit.getName() + " " + identifier);
							log.error("Error while adding patient identifiers to savedIdentifier list", e);
							String msg = getMessageSourceAccessor().getMessage("error.checkdigits", args);
							errors.rejectValue("identifiers", msg);
						}
					}
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
		
		Patient patient = (Patient)obj;
				
		if (Context.isAuthenticated()) {

			MessageSourceAccessor msa = getMessageSourceAccessor();
			String action = request.getParameter("action");
			PatientService ps = Context.getPatientService();
						
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
				//boolean isNew = (patient.getPatientId() == null);
				
				Context.getPatientService().updatePatient(patient);

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
								
				Context.getPatientService().updatePatient(patient);

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

		Patient patient = null;
		
		if (Context.isAuthenticated()) {
			PatientService ps = Context.getPatientService();
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
		
		Patient patient = (Patient)obj;
		List<Form> forms = new Vector<Form>();
		Map<String, Object> map = new HashMap<String, Object>();
		List<Encounter> encounters = new Vector<Encounter>();
		String causeOfDeathOther = "";

		if (Context.isAuthenticated()) {
			boolean onlyPublishedForms = true;
			if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_UNPUBLISHED_FORMS))
				onlyPublishedForms = false;
			forms.addAll(Context.getFormService().getForms(onlyPublishedForms));
			
			Set<Encounter> encs = Context.getEncounterService().getEncounters(patient);
			if (encs != null && encs.size() > 0)
				encounters.addAll(encs);
			
			String propCause = Context.getAdministrationService().getGlobalProperty("concept.causeOfDeath");
			Concept conceptCause = Context.getConceptService().getConceptByIdOrName(propCause);
			
			if ( conceptCause != null ) {
				Set<Obs> obssDeath = Context.getObsService().getObservations(patient, conceptCause);
				if ( obssDeath.size() == 1 ) {
					Obs obsDeath = obssDeath.iterator().next();
					causeOfDeathOther = obsDeath.getValueText();
					if ( causeOfDeathOther == null ) {
						log.debug("cod is null, so setting to empty string");
						causeOfDeathOther = "";
					} else {
						log.debug("cod is valid: " + causeOfDeathOther);
					}
				} else {
					log.debug("obssDeath is wrong size: " + obssDeath.size());
				}
			} else {
				log.debug("No concept causee found");
			}
		}

		String patientVariation = "";
		
		Concept reasonForExitConcept = Context.getConceptService().getConceptByIdOrName(Context.getAdministrationService().getGlobalProperty("concept.reasonExitedCare"));
		if ( reasonForExitConcept != null ) {
			Set<Obs> patientExitObs = Context.getObsService().getObservations(patient, reasonForExitConcept);
			if ( patientExitObs != null ) {
				log.debug("Exit obs is size " + patientExitObs.size() );
				if ( patientExitObs.size() == 1 ) {
					Obs exitObs = patientExitObs.iterator().next();
					Concept exitReason = exitObs.getValueCoded();
					Date exitDate = exitObs.getObsDatetime();
					if ( exitReason != null && exitDate != null ) {
						patientVariation = "Exited";
					}
				} else {
					log.error("Too many reasons for exit - not putting data into model");
				}
			}
		}
		
		map.put("patientVariation", patientVariation);
		
		map.put("forms", forms);
				
		// empty objects used to create blank template in the view
		map.put("emptyIdentifier", new PatientIdentifier());
		map.put("emptyName", new PatientName());
		map.put("emptyAddress", new PatientAddress());
		map.put("encounters", encounters);
		map.put("datePattern", dateFormat.toLocalizedPattern().toLowerCase());
		map.put("causeOfDeathOther", causeOfDeathOther);
		
		return map;
	}    
	
}