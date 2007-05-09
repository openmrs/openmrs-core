package org.openmrs.web.controller.patient;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
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
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
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
import org.openmrs.web.controller.person.PersonFormController;
import org.openmrs.web.propertyeditor.ConceptEditor;
import org.openmrs.web.propertyeditor.LocationEditor;
import org.openmrs.web.propertyeditor.PatientIdentifierTypeEditor;
import org.openmrs.web.propertyeditor.TribeEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

public class PatientFormController extends PersonFormController {
	
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
		
		dateFormat = Context.getDateFormat();
		
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
								PersonAddress pa = new PersonAddress();
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
								//}
						}
					}
						
				// Patient Names
	
					objs = patient.getNames().toArray();
					for (int i = 0; i < objs.length; i++ ) {
						if (request.getParameter("names[" + i + "].givenName") == null)
							patient.removeName((PersonName)objs[i]);
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
								PersonName pn = new PersonName();
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
					
					/*
					 * 					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.formatInvalid");
					isError = true;
				} catch ( InvalidCheckDigitException icde ) {
					log.error(icde);
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.checkDigit");
					isError = true;
				} catch ( IdentifierNotUniqueException inue ) {
					log.error(inue);
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.notUnique");
					isError = true;
				} catch ( DuplicateIdentifierException die ) {
					log.error(die);
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.duplicate");
					isError = true;
				} catch ( PatientIdentifierException pie ) {
					log.error(pie);
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.general");

					 */
					
				// check patient identifier checkdigits
					for (PatientIdentifier pi : patient.getIdentifiers()) {
						// skip voided identifiers
						if (pi.isVoided()) continue;
						PatientIdentifierType pit = pi.getIdentifierType();
						String identifier = pi.getIdentifier();
						String format = pit.getFormat();
						String formatDescription = pit.getFormatDescription();
						String formatStr = format;
						if ( format == null ) formatStr = "";
						if ( formatDescription != null ) if ( formatDescription.length() > 0 ) formatStr = formatDescription;
						String[] args = {identifier, formatStr};
						try {
							if (pit.hasCheckDigit() && !OpenmrsUtil.isValidCheckDigit(identifier)) {
								log.error("hasCheckDigit and is not valid: " + pit.getName() + " " + identifier);
								String msg = getMessageSourceAccessor().getMessage("error.checkdigits.verbose", args);
								errors.rejectValue("identifiers", msg);
							} else if ( format != null ) if ( format.length() > 0 && !identifier.matches(format) ) {
								log.error("Identifier format is not valid: (" + format + ") " + identifier);
								String msg = getMessageSourceAccessor().getMessage("error.identifier.formatInvalid", args);
								errors.rejectValue("identifiers", msg);
							}
							// Modified on 17 Jan 2007 by CA - don't think we need this if we can input a regexp for each ID type
							/*
							else if (pit.getFormat() != null) if ( pit.getFormat().length() > 0 && !identifier.matches(pit.getFormat()) ){
								log.error("Identifer has wrong format: " + identifier + " does not match " + pit.getFormat());
								String[] args2 = {identifier, pit.getFormat()}; 
								String msg = getMessageSourceAccessor().getMessage("error.character.invalid", args2);
								errors.rejectValue("identifiers", msg);
							}
							*/
						} catch (Exception e) {
							log.error("exception thrown with: " + pit.getName() + " " + identifier);
							log.error("Error while adding patient identifiers to savedIdentifier list", e);
							String msg = getMessageSourceAccessor().getMessage("error.identifier.formatInvalid", args);
							errors.rejectValue("identifiers", msg);
						}
						
						if (errors.hasErrors())
							return showForm(request, response, errors);
					}
					
					
			} // end "if we're not deleting the patient"
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
				boolean isError = false;
				
				try {
					Context.getPatientService().updatePatient(patient);	
				} catch ( InvalidIdentifierFormatException iife ) {
					log.error(iife);
					patient.removeIdentifier(iife.getPatientIdentifier());
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.formatInvalid");
					isError = true;
				} catch ( InvalidCheckDigitException icde ) {
					log.error(icde);
					patient.removeIdentifier(icde.getPatientIdentifier());
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.checkDigit");
					isError = true;
				} catch ( IdentifierNotUniqueException inue ) {
					log.error(inue);
					patient.removeIdentifier(inue.getPatientIdentifier());
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.notUnique");
					isError = true;
				} catch ( DuplicateIdentifierException die ) {
					log.error(die);
					patient.removeIdentifier(die.getPatientIdentifier());
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.duplicate");
					isError = true;
				} catch ( InsufficientIdentifiersException iie ) {
					log.error(iie);
					patient.removeIdentifier(iie.getPatientIdentifier());
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.insufficientIdentifiers");
					isError = true;
				} catch ( PatientIdentifierException pie ) {
					log.error(pie);
					patient.removeIdentifier(pie.getPatientIdentifier());
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "PatientIdentifier.error.general");
					isError = true;
				}
				
				if ( patient.getDead() && !isError ) {
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
									obsDeath.setPerson(patient);
									obsDeath.setConcept(causeOfDeath);
									Location loc = Context.getEncounterService().getLocationByName("Unknown Location");
									if ( loc == null ) loc = Context.getEncounterService().getLocation(new Integer(1));
									// TODO person healthcenter //if ( loc == null ) loc = patient.getHealthCenter();
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
								
				if ( !isError ) {
					String view = getSuccessView();
					
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Patient.saved");
					
					view = view + "?patientId=" + patient.getPatientId();
					return new ModelAndView(new RedirectView(view));
				} else {
					return showForm(request, response, errors);
				}
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
			Integer id = null;
	    	if (patientId != null) {
	    		try {
	    			id = Integer.valueOf(patientId);
	    			patient = ps.getPatient(id);
	    			if (patient == null)
	    				throw new ObjectRetrievalFailureException(Patient.class, id); 
	    		}
	    		catch (NumberFormatException numberError) {
	    			log.warn("Invalid userId supplied: '" + patientId + "'", numberError);
	    		}
	    		catch (ObjectRetrievalFailureException noPatientEx) {
	    			try {
		    			Person person = Context.getPersonService().getPerson(id);
		    			patient = new Patient(person);
		    		}
		    		catch (ObjectRetrievalFailureException noPersonEx) {
		    			log.warn("There is no patient or person with id: '" + patientId + "'", noPersonEx);
		    			throw new ServletException("There is no patient or person with id: '" + patientId + "'");
		    		}
	    		}
	    	}
		}

		if (patient == null) {
			patient = new Patient();
			
			String name = request.getParameter("name");
			if (name != null) {
				String gender = request.getParameter("gndr");
				String date = request.getParameter("birthyear");
				String age = request.getParameter("age");
				
				getMiniPerson(patient, name, gender, date, age);
			}
		}
		
		if (patient.getIdentifiers().size() < 1)
			patient.addIdentifier(new PatientIdentifier());
		
		super.setupFormBackingObject(patient);
		
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
		
		if (Context.isAuthenticated()) {
			boolean onlyPublishedForms = true;
			if (Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_UNPUBLISHED_FORMS))
				onlyPublishedForms = false;
			forms.addAll(Context.getFormService().getForms(onlyPublishedForms));
			
			Set<Encounter> encs = Context.getEncounterService().getEncounters(patient);
			if (encs != null && encs.size() > 0)
				encounters.addAll(encs);
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
		map.put("emptyName", new PersonName());
		map.put("emptyAddress", new PersonAddress());
		map.put("encounters", encounters);

		super.setupReferenceData(map, patient);
		
		return map;
	}    
	
}