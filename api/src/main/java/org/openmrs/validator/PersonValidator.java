package org.openmrs.validator;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * This class validates a Person object.
 */
public class PersonValidator implements Validator {
	
	private Logger log = Logger.getLogger(PersonValidator.class);
	
	@Autowired
	private PersonNameValidator personNameValidator;
	
	@Autowired
	private PersonAddressValidator personAddressValidator;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return Person.class.isAssignableFrom(clazz);
	}
	
	@Override
	public void validate(Object target, Errors errors) {
		if (log.isDebugEnabled())
			log.debug(this.getClass().getName() + ".validate...");
		
		if (target == null) {
			return;
		}
		
		Person person = (Person) target;
		
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "gender", "Person.gender.required");
		
		for (PersonName personName : person.getNames()) {
			personNameValidator.validate(personName, errors);
		}
		
		//validate the personAddress
		int index = 0;
		for (PersonAddress address : person.getAddresses()) {
			try {
				errors.pushNestedPath("addresses[" + index + "]");
				ValidationUtils.invokeValidator(personAddressValidator, address, errors);
			}
			finally {
				errors.popNestedPath();
				index++;
			}
		}
		
		// check birth date against future dates and really old dates
		if (person.getBirthdate() != null) {
			if (person.getBirthdate().after(new Date()))
				errors.rejectValue("birthdate", "error.date.future");
			else {
				Calendar c = Calendar.getInstance();
				c.setTime(new Date());
				c.add(Calendar.YEAR, -120); // person cannot be older than 120 years old 
				if (person.getBirthdate().before(c.getTime())) {
					errors.rejectValue("birthdate", "error.date.nonsensical");
				}
			}
		}
		
		if (person.isVoided())
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "voidReason", "error.null");
		if (person.isDead())
			ValidationUtils.rejectIfEmpty(errors, "causeOfDeath", "Person.dead.causeOfDeathNull");
		
		ValidateUtil.validateFieldLengths(errors, Person.class, "gender");
	}
	
}
