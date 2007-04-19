package org.openmrs.web.dwr;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Person;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;

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
	public List<?> getSimilarPeople(String name, String birthyear, String age, String gender) {
		Vector<Object> personList = new Vector<Object>();

		Integer userId = Context.getAuthenticatedUser().getUserId();
		log.info(userId + "|" + name + "|" + birthyear + "|" + age + "|" + gender);
		
		PersonService ps = Context.getPersonService();
		List<Person> persons = new Vector<Person>();
		
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
		Vector<Object> personList = new Vector<Object>();
		PersonService ps = Context.getPersonService();
		
		for (Person p : ps.findPeople(searchPhrase, includeVoided)) {
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
