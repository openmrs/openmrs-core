package org.openmrs.web.dwr;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Person;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

public class DWRPersonService {

	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * 
	 * @param name
	 * @param birthyear
	 * @param age
	 * @param gender
	 * @return
	 */
	public List<?> getSimilarPeople(String name, String birthdate, String age, String gender) {
		Vector<Object> personList = new Vector<Object>();

		Integer userId = Context.getAuthenticatedUser().getUserId();
		log.info(userId + "|" + name + "|" + birthdate + "|" + age + "|" + gender);
		
		PersonService ps = Context.getPersonService();
		List<Person> persons = new Vector<Person>();
		
		Integer d = null;
		birthdate = birthdate.trim();
		age = age.trim();
		if (birthdate.length() > 0) {
			// extract the year from the given birthdate string
			DateFormat format = OpenmrsUtil.getDateFormat();
			Date dateObject = null;
			try {
				dateObject = format.parse(birthdate);
			}
			catch (Exception e) {}
			
			if (dateObject != null) {
				Calendar c = Calendar.getInstance();
				c.setTime(dateObject);
				d = c.get(Calendar.YEAR);
			}
		}
		else if (age.length() > 0) {
			// calculate their birth year from the given age string
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			d = c.get(Calendar.YEAR);
			d = d - Integer.parseInt(age);
		}
		
		if (gender.length() < 1)
			gender = null;
		
		persons.addAll(ps.getSimilarPeople(name, d, gender));
		
		personList = new Vector<Object>(persons.size());
		for (Person p : persons) {
			personList.add(new PersonListItem(p));
		}
		
		return personList;

	}
	
	/**
	 * 
	 * @param searchPhrase
	 * @param includeVoided
	 * @return
	 */
	public List<?> findPeople(String searchPhrase, boolean includeVoided) {
		return findPeopleByRoles(searchPhrase, includeVoided, null);
	}
	
	public List<?> findPeopleByRoles(String searchPhrase, boolean includeVoided, String roles) {
		Vector<Object> personList = new Vector<Object>();
		PersonService ps = Context.getPersonService();
		
		for (Person p : ps.findPeople(searchPhrase, includeVoided, roles)) {
			personList.add(new PersonListItem(p));
		}
		
		return personList;
	}
	
	/**
	 * 
	 * @param patientId
	 * @return
	 */
	public PersonListItem getPerson(Integer personId) {
		Person p = Context.getPersonService().getPerson(personId);
		return new PersonListItem(p);
	}
}
