package org.openmrs.web.controller.person;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.web.propertyeditor.ConceptEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class PersonFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected static final Log log = LogFactory.getLog(PersonFormController.class);
    
    /**
	 * 
	 * Allows for other Objects to be used as values in input tags.
	 *   Normally, only strings and lists are expected 
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest, org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		
		binder.registerCustomEditor(java.lang.Integer.class, 
				new CustomNumberEditor(java.lang.Integer.class, true));
        binder.registerCustomEditor(java.util.Date.class, 
        		new CustomDateEditor(Context.getDateFormat(), true));
        binder.registerCustomEditor(org.openmrs.Concept.class, 
        		new ConceptEditor());
	}

	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#processFormSubmission(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		Person person = (Person)obj;
		
		if (!Context.isAuthenticated()) {
			errors.reject("auth.invalid");
		}
		else {
			// Make sure they assign a name
				if (person.getPersonName().getGivenName() == "")
					errors.rejectValue("names[0].givenName", "Person.name.required");
				if (person.getPersonName().getFamilyName() == "")
					errors.rejectValue("names[0].familyName", "Person.name.required");
				
			// Make sure they choose a gender
				if (person.getGender() == null || person.getGender().equals(""))
					errors.rejectValue("gender", "Person.gender.required");
				
			// look for person attributes in the request and save to person
				for (PersonAttributeType type : Context.getPersonService().getPersonAttributeTypes("", "all")) {
					String value = request.getParameter(type.getPersonAttributeTypeId().toString());
					
					person.addAttribute(new PersonAttribute(type, value));
				}
				
			// check patients birthdate against future dates and really old dates
				if (person.getBirthdate() != null) {
					if (person.getBirthdate().after(new Date()))
						errors.rejectValue("birthdate", "error.date.future");
					else {
						Calendar c = Calendar.getInstance();
						c.setTime(new Date());
						c.add(Calendar.YEAR, -120); // patient cannot be older than 120 years old 
						if (person.getBirthdate().before(c.getTime())){
							errors.rejectValue("birthdate", "error.date.nonsensical");
						}
					}
				}
				
			//	 Patient Info 
				//ValidationUtils.rejectIfEmptyOrWhitespace(errors, "birthdate", "error.null");
				if (person.isVoided())
					ValidationUtils.rejectIfEmptyOrWhitespace(errors, "voidReason", "error.null");
				if (person.isDead() && (person.getCauseOfDeath() == null))
					errors.rejectValue("causeOfDeath", "Patient.dead.causeOfDeathNull");
			
		
		}
		
		if (log.isDebugEnabled())
			log.debug("Person Attributes: \n" + person.printAttributes());
		
		return super.processFormSubmission(request, response, person, errors);
	}
	
	/**
	 * Setup the person object.  Should be called by the PersonFormController/UserFormController.formBackingObject(request)
	 * 
	 * @param person
	 * @return
	 */
	protected Person setupFormBackingObject(Person person) {
		
		// set a default name and address for the person.  This allows us to use person.names[0] binding in the jsp
		if (person.getNames().size() < 1)
			person.addName(new PersonName());
		
		if (person.getAddresses().size() < 1)
			person.addAddress(new PersonAddress());
		
		// initialize the user/person sets
		// hibernate seems to have an issue with empty lists/sets if they aren't initialized
		
		person.getAttributes().size();
		
		return person;
	}
	
	/**
	 * Setup the reference map object.  Should be called by the PersonFormController/UserFormController.referenceData(...)
	 * 
	 * @param person
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Map setupReferenceData(Map map, Person person) throws Exception {
		
		String causeOfDeathOther = "";
		
		if (Context.isAuthenticated()) {
			String propCause = Context.getAdministrationService().getGlobalProperty("concept.causeOfDeath");
			Concept conceptCause = Context.getConceptService().getConceptByIdOrName(propCause);
			
			/*
			if ( conceptCause != null ) {
				// TODO add back in for persons
				Set<Obs> obssDeath = Context.getObsService().getObservations(person, conceptCause);
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
				*/log.warn("No concept death cause found");/*
			}
			*/
		}
		
		map.put("causeOfDeathOther", causeOfDeathOther);
		map.put("datePattern", Context.getDateFormat().toLocalizedPattern().toLowerCase());
		
		return map;
    }
    
    /**
     * Add the given name, gender, and birthdate/age to the given Person
     *  
     * @param <P> Should be a Patient or User object
     * @param person
     * @param name
     * @param gender
     * @param date birthdate
     * @param age
     */
    public static <P extends Person> void getMiniPerson(P person, String name, String gender, String date, String age) {
    	
    	person.addName(Context.getPersonService().splitPersonName(name));
    	
    	person.setGender(gender);
		Date birthdate = null;
		boolean birthdateEstimated = false;
		if (date != null && !date.equals("")) {
			try {
				birthdate = DateFormat.getDateInstance(DateFormat.SHORT).parse("01/01/" + date);
				birthdateEstimated = true;
			} catch (ParseException e) { log.debug("Error getting date from birthdate", e); }
		}
		else if (age != null && !age.equals("")) {
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			Integer d = c.get(Calendar.YEAR);
			d = d - Integer.parseInt(age);
			try {
				birthdate = DateFormat.getDateInstance(DateFormat.SHORT).parse("01/01/" + d);
				birthdateEstimated = true;
			} catch (ParseException e) { log.debug("Error getting date from age", e); }
		}
		if (birthdate != null)
			person.setBirthdate(birthdate);
		person.setBirthdateEstimated(birthdateEstimated);
    	
    }

}
