/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.dwr;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;

/**
 * DWR methods for ajaxy effects on {@link Person} objects.
 */
public class DWRPersonService {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	
	/**
	 * Searches for Person records that have a name similar to the given name, a birthdate that is
	 * null or within a few years of the given birthdate, and a gender that matches.
	 * 
	 * Note: this method contains a non-backwards-compatible change between 1.5 and 1.6, since DWR has
	 * trouble with method overloading. The String personType parameter was removed, since User no longer
	 * extends Person
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
		
		Integer d = null;
		birthdate = birthdate.trim();
		age = age.trim();
		if (birthdate.length() > 0) {
			// extract the year from the given birthdate string
			DateFormat format = Context.getDateFormat();
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
		} else if (age.length() > 0) {
			// calculate their birth year from the given age string
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			d = c.get(Calendar.YEAR);
			d = d - Integer.parseInt(age);
		}
		
		if (gender.length() < 1)
			gender = null;
			
		Set<Person> persons = ps.getSimilarPeople(name, d, gender);
		
		personList = new Vector<Object>(persons.size());
		for (Person p : persons) {
			personList.add(PersonListItem.createBestMatch(p));
		}
		
		return personList;
		
	}
	
	/**
	 * @param searchPhrase
	 * @param includeVoided
	 * @return
	 */
	public List<?> findPeople(String searchPhrase, boolean includeVoided) {
		return findPeopleByRoles(searchPhrase, includeVoided, null);
	}
	
	/**
	 * Find Person objects based on the given searchPhrase
	 * 
	 * @param searchPhrase partial name or partial identifier
	 * @param includeVoided true/false whether to include the voided objects
	 * @param roles if not null, restricts search to only users and only users with these roles
	 * @return list of PersonListItem s that match the given searchPhrase. The PersonListItems
	 *         contain as much information as possible about the matching persons, e.g. considering
	 *         whether they are patients or users, which for example is useful for displaying
	 *         patient identifiers in PersonSearch-widgets.
	 * @should match on patient identifiers
	 * @should allow null roles parameter
	 */
	public List<PersonListItem> findPeopleByRoles(String searchPhrase, boolean includeVoided, String roles) {
		Vector<PersonListItem> personList = new Vector<PersonListItem>();
		
		if (roles != null)
			roles = roles.trim();
		
		// if roles were given, search for users with those roles
		if (roles != null && roles.length() > 0) {
			UserService us = Context.getUserService();
			
			List<Role> roleList = new Vector<Role>();
			
			if (roles != null)
				if (roles.length() > 0) {
					String[] splitRoles = roles.split(",");
					for (String role : splitRoles) {
						roleList.add(new Role(role));
					}
				}
			
			for (User u : us.getUsers(searchPhrase, roleList, includeVoided)) {
				personList.add(new UserListItem(u));
			}
			
		} else {
			
			// if no roles were given, search for normal people
			PersonService ps = Context.getPersonService();
			for (Person p : ps.getPeople(searchPhrase, null)) {
				personList.add(PersonListItem.createBestMatch(p));
			}
			
			// also search on patient identifier if the query contains a number
			if (searchPhrase.matches(".*\\d+.*")) {
				PatientService patientService = Context.getPatientService();
				for (Patient p : patientService.getPatients(null, searchPhrase, null, false)) {
					personList.add(PersonListItem.createBestMatch(p));
				}
			}
			
		}
		
		return personList;
	}
	
	/**
	 * Creates a new person stub.
	 * 
	 * @param given
	 * @param middle
	 * @param family
	 * @param birthdate
	 * @param dateformat
	 * @param age
	 * @param gender
	 * @return PersonListItem person stub created
	 */
	public Object createPerson(String given, String middle, String family, String birthdate, String dateformat, String age,
	                           String gender) {
		log.error(given + " " + middle + " " + family + " " + birthdate + " " + dateformat + " " + age + " " + gender);
		User user = Context.getAuthenticatedUser();
		Person p = new Person();
		p.setPersonCreator(user);
		p.setPersonDateCreated(new Date());
		p.setPersonChangedBy(user);
		p.setPersonDateChanged(new Date());
		if ("".equals(gender)) {
			log.error("Gender cannot be null.");
			return new String("Gender cannot be null.");
		} else if (gender.toUpperCase().contains("M"))
			p.setGender("M");
		else if (gender.toUpperCase().contains("F"))
			p.setGender("F");
		else {
			log.error("Gender must be 'M' or 'F'.");
			return new String("Gender must be 'M' or 'F'.");
		}
		if ("".equals(given) || "".equals(family)) {
			log.error("Given name and family name cannot be null.");
			return new String("Given name and family name cannot be null.");
		}
		PersonName name = new PersonName(given, middle, family);
		name.setCreator(user);
		name.setDateCreated(new Date());
		name.setChangedBy(user);
		name.setDateChanged(new Date());
		p.addName(name);
		try {
			Date d = updateAge(birthdate, dateformat, age);
			p.setBirthdate(d);
		}
		catch (java.text.ParseException pe) {
			log.error(pe);
			return new String("Birthdate cannot be parsed.");
		}
		p.setGender(gender);
		Person person = Context.getPersonService().savePerson(p);
		return PersonListItem.createBestMatch(person);
	}
	
	/**
	 * @param patientId
	 * @return
	 */
	public PersonListItem getPerson(Integer personId) {
		Person p = Context.getPersonService().getPerson(personId);
		return PersonListItem.createBestMatch(p);
	}
	
	/**
	 * Private method to handle birth date and age input.
	 * 
	 * @param birthdate
	 * @param dateformat
	 * @param age
	 * @return
	 * @throws java.text.ParseException
	 */
	private Date updateAge(String birthdate, String dateformat, String age) throws java.text.ParseException {
		SimpleDateFormat df = new SimpleDateFormat();
		if (!"".equals(dateformat)) {
			dateformat = dateformat.toLowerCase().replaceAll("m", "M");
		} else
			dateformat = new String("MM/dd/yyyy");
		df.applyPattern(dateformat);
		Calendar cal = Calendar.getInstance();
		cal.clear(Calendar.HOUR);
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MILLISECOND);
		if ("".equals(birthdate)) {
			if ("".equals(age))
				return cal.getTime();
			try {
				cal.add(Calendar.YEAR, -(Integer.parseInt(age)));
			}
			catch (NumberFormatException nfe) {}
			return cal.getTime();
		} else
			cal.setTime(df.parse(birthdate));
		return cal.getTime();
	}
	
}
