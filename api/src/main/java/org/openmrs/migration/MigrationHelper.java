/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.migration;

import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The MigrationHelper will be removed from openmrs-core. If you need the code migrate it to your code base.
 * 
 * @deprecated since 2.2.0
 */
@Deprecated
public class MigrationHelper {

	private MigrationHelper() {
	}
	
	private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

	private static final Logger log = LoggerFactory.getLogger(MigrationHelper.class);
	
	static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	/**
	 * @deprecated since 2.2.0 migrate the method to your code base if needed
	 */
	@Deprecated
	public static Date parseDate(String s) throws ParseException {
		if (s == null || s.length() == 0) {
			return null;
		} else {
			if (s.length() == 10) {
				s += " 00:00:00";
			}
			DateFormat df = new SimpleDateFormat(DATE_TIME_PATTERN);
			return df.parse(s);
		}
	}

	/**
	 * @deprecated since 2.2.0 migrate the method to your code base if needed
	 */
	@Deprecated
	public static Document parseXml(String xml) throws ParserConfigurationException {
		DocumentBuilder builder = factory.newDocumentBuilder();
		try {
			// Disable resolution of external entities. See TRUNK-3942 
			builder.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
			
			return builder.parse(new InputSource(new StringReader(xml)));
		}
		catch (IOException | SAXException ex) {
			return null;
		}
	}
	
	private static void findNodesNamed(Node node, String lookForName, Collection<Node> ret) {
		if (node.getNodeName().equals(lookForName)) {
			ret.add(node);
		} else {
			NodeList list = node.getChildNodes();
			for (int i = 0; i < list.getLength(); ++i) {
				findNodesNamed(list.item(i), lookForName, ret);
			}
		}
	}
	
	/**
	 * Takes XML like: &lt;something&gt; &lt;user date_changed="2001-03-06 08:46:53.0"
	 * date_created="2001-03-06 08:46:53.0" username="hamish@mit.edu" first_name="Hamish"
	 * last_name="Fraser" user_id="2001"/&gt; &lt;/something&gt; Returns the number of users added
	 * 
	 * @deprecated since 2.2.0 migrate the method to your code base if needed
	 */
	@Deprecated
	public static int importUsers(Document document) throws ParseException {
		int ret = 0;
		Random rand = new Random();
		UserService us = Context.getUserService();
		
		List<Node> toAdd = new ArrayList<>();
		findNodesNamed(document, "user", toAdd);
		for (Node node : toAdd) {
			Element e = (Element) node;
			String username = e.getAttribute("username");
			if (username == null || username.length() == 0) {
				throw new IllegalArgumentException("each <user /> element must define a user_name attribute");
			}
			if (us.getUserByUsername(username) != null) {
				continue;
			}
			User user = new User();
			user.setPerson(new Person());
			PersonName pn = new PersonName(e.getAttribute("first_name"), "", e.getAttribute("last_name"));
			user.addName(pn);
			user.setUsername(username);
			user.setDateCreated(parseDate(e.getAttribute("date_created")));
			user.setDateChanged(parseDate(e.getAttribute("date_changed")));
			
			// Generate a temporary password: 8-12 random characters
			String pass;
			{
				int length = rand.nextInt(4) + 8;
				char[] password = new char[length];
				for (int x = 0; x < length; x++) {
					int randDecimalAsciiVal = rand.nextInt(93) + 33;
					password[x] = (char) randDecimalAsciiVal;
				}
				pass = new String(password);
			}
			us.createUser(user, pass);
			++ret;
		}
		return ret;
	}
	
	/**
	 * Takes XML like: &lt;something&gt; &lt;location name="Cerca-la-Source"/&gt; &lt;/something&gt; returns the
	 * number of locations added
	 * 
	 * @deprecated since 2.2.0 migrate the method to your code base if needed
	 */
	@Deprecated
	public static int importLocations(Document document) {
		int ret = 0;
		LocationService ls = Context.getLocationService();
		List<Node> toAdd = new ArrayList<>();
		findNodesNamed(document, "location", toAdd);
		for (Node node : toAdd) {
			Element e = (Element) node;
			String name = e.getAttribute("name");
			if (name == null || name.length() == 0) {
				throw new IllegalArgumentException("each <location /> element must define a name attribute");
			}
			if (ls.getLocation(name) != null) {
				continue;
			}
			Location location = new Location();
			location.setName(name);
			
			ls.saveLocation(location);
			++ret;
		}
		return ret;
	}
	
	/**
	 * Takes a list of Strings of the format RELATIONSHIP:&lt;user last name&gt;,&lt;user first
	 * name&gt;,&lt;relationship type name&gt;,&lt;patient identifier type name&gt;,&lt;identifier&gt; so if user hfraser
	 * if the cardiologist of the patient with patient_id 8039 in PIH's old emr, then:
	 * RELATIONSHIP:hfraser,Cardiologist,HIV-EMRV1,8039 (the "RELATIONSHIP:" is not actually
	 * necessary. Anything before and including the first : will be dropped If autoCreateUsers is
	 * true, and no user exists with the given username, one will be created. If autoAddRole is
	 * true, then whenever a user is auto-created, if a role exists with the same name as
	 * relationshipType.name, then the user will be added to that role
	 * 
	 * @deprecated since 2.2.0 migrate the method to your code base if needed
	 */
	@Deprecated
	public static int importRelationships(Collection<String> relationships, boolean autoCreateUsers, boolean autoAddRole) {
		PatientService ps = Context.getPatientService();
		UserService us = Context.getUserService();
		PersonService personService = Context.getPersonService();
		List<Relationship> relsToAdd = new ArrayList<>();
		Random rand = new Random();
		for (String s : relationships) {
			if (s.contains(":")) {
				s = s.substring(s.indexOf(":") + 1);
			}
			String[] ss = s.split(",");
			if (ss.length < 5) {
				throw new IllegalArgumentException("The line '" + s + "' is in the wrong format");
			}
			String userLastName = ss[0];
			String userFirstName = ss[1];
			String username = (userFirstName + userLastName).replaceAll(" ", "");
			String relationshipType = ss[2];
			String identifierType = ss[3];
			String identifier = ss[4];
			User user = null;
			{ // first try looking for non-voided users
				List<User> users = us.getUsersByName(userFirstName, userLastName, false);
				if (users.size() == 1) {
					user = users.get(0);
				} else if (users.size() > 1) {
					throw new IllegalArgumentException("Found " + users.size() + " users named '" + userLastName + ", "
					        + userFirstName + "'");
				}
			}
			if (user == null) {
				// next try looking for voided users
				List<User> users = us.getUsersByName(userFirstName, userLastName, false);
				if (users.size() == 1) {
					user = users.get(0);
				} else if (users.size() > 1) {
					throw new IllegalArgumentException("Found " + users.size() + " voided users named '" + userLastName
					        + ", " + userFirstName + "'");
				}
			}
			if (user == null && autoCreateUsers) {
				user = new User();
				user.setPerson(new Person());
				PersonName pn = new PersonName(userFirstName, "", userLastName);
				user.addName(pn);
				user.setUsername(username);
				// Generate a temporary password: 8-12 random characters
				String pass;
				{
					int length = rand.nextInt(4) + 8;
					char[] password = new char[length];
					for (int x = 0; x < length; x++) {
						int randDecimalAsciiVal = rand.nextInt(93) + 33;
						password[x] = (char) randDecimalAsciiVal;
					}
					pass = new String(password);
				}
				if (autoAddRole) {
					Role role = us.getRole(relationshipType);
					if (role != null) {
						user.addRole(role);
					}
				}
				us.createUser(user, pass);
			}
			if (user == null) {
				throw new IllegalArgumentException("Can't find user '" + userLastName + ", " + userFirstName + "'");
			}
			Person person = personService.getPerson(user.getUserId());
			
			RelationshipType relationship = personService.getRelationshipTypeByName(relationshipType);
			PatientIdentifierType pit = ps.getPatientIdentifierTypeByName(identifierType);
			List<PatientIdentifier> found = ps.getPatientIdentifiers(identifier, Collections.singletonList(pit), null, null,
			    null);
			if (found.size() != 1) {
				throw new IllegalArgumentException("Found " + found.size() + " patients with identifier '" + identifier
				        + "' of type " + identifierType);
			}
			Person relative = personService.getPerson(found.get(0).getPatient().getPatientId());
			Relationship rel = new Relationship();
			rel.setPersonA(person);
			rel.setRelationshipType(relationship);
			rel.setPersonB(relative);
			relsToAdd.add(rel);
		}
		int addedSoFar = 0;
		for (Relationship rel : relsToAdd) {
			personService.saveRelationship(rel);
			++addedSoFar;
		}
		return addedSoFar;
	}

	/**
	 * @deprecated since 2.2.0 migrate the method to your code base if needed
	 */
	@Deprecated
	public static int importProgramsAndStatuses(List<String> programWorkflow) throws ParseException {
		ProgramWorkflowService pws = Context.getProgramWorkflowService();
		PatientService ps = Context.getPatientService();
		List<PatientProgram> patientPrograms = new ArrayList<>();
		Map<String, PatientProgram> knownPatientPrograms = new HashMap<>();
		Map<String, Program> programsByName = new HashMap<>();
		for (Program program : pws.getAllPrograms()) {
			programsByName.put(program.getConcept().getName(Context.getLocale(), false).getName(), program);
		}
		for (String s : programWorkflow) {
			// ENROLLMENT:HIVEMR-V1,9266,IMB HIV PROGRAM,2005-08-25,
			log.debug(s);
			if (s.startsWith("ENROLLMENT:")) {
				s = s.substring(s.indexOf(":") + 1);
				String[] temp = s.split(",");
				PatientIdentifierType pit = ps.getPatientIdentifierTypeByName(temp[0]);
				String identifier = temp[1];
				List<PatientIdentifier> pis = ps.getPatientIdentifiers(identifier, Collections.singletonList(pit), null,
				    null, null);
				if (pis.size() != 1) {
					throw new IllegalArgumentException("Found " + pis.size() + " instances of identifier " + identifier
					        + " of type " + pit);
				}
				Patient p = pis.get(0).getPatient();
				Program program = programsByName.get(temp[2]);
				if (program == null) {
					throw new RuntimeException("Couldn't find program \"" + temp[2] + "\" in " + programsByName);
				}
				Date enrollmentDate = temp.length < 4 ? null : parseDate(temp[3]);
				Date completionDate = temp.length < 5 ? null : parseDate(temp[4]);
				PatientProgram pp = new PatientProgram();
				pp.setPatient(p);
				pp.setProgram(program);
				pp.setDateEnrolled(enrollmentDate);
				pp.setDateCompleted(completionDate);
				patientPrograms.add(pp);
				knownPatientPrograms.put(temp[0] + "," + temp[1] + "," + temp[2], pp); // "HIVEMR-V1,9266,IMB HIV PROGRAM"
			} else if (s.startsWith("STATUS:")) {
				// STATUS:HIVEMR-V1,9266,IMB HIV PROGRAM,TREATMENT STATUS,ACTIVE,2005-08-25,,
				s = s.substring(s.indexOf(":") + 1);
				String[] temp = s.split(",");
				Program program = programsByName.get(temp[2]);
				if (program == null) {
					throw new RuntimeException("Couldn't find program \"" + temp[2] + "\" in " + programsByName);
				}
				ProgramWorkflow wf = program.getWorkflowByName(temp[3]);
				if (wf == null) {
					throw new RuntimeException("Couldn't find workflow \"" + temp[3] + "\" for program " + program + " (in "
					        + program.getAllWorkflows() + ")");
				}
				ProgramWorkflowState st = wf.getStateByName(temp[4]);
				if (st == null) {
					throw new RuntimeException("Couldn't find state \"" + temp[4] + "\" for workflow " + wf + " (in "
					        + wf.getStates() + ")");
				}
				Date startDate = temp.length < 6 ? null : parseDate(temp[5]);
				Date endDate = temp.length < 7 ? null : parseDate(temp[6]);
				PatientState state = new PatientState();
				PatientProgram pp = knownPatientPrograms.get(temp[0] + "," + temp[1] + "," + temp[2]);
				state.setPatientProgram(pp);
				state.setState(st);
				state.setStartDate(startDate);
				state.setEndDate(endDate);
				pp.getStates().add(state);
			}
		}
		int numAdded = 0;
		
		for (PatientProgram pp : knownPatientPrograms.values()) {
			pws.savePatientProgram(pp);
			++numAdded;
		}
		return numAdded;
	}
	
}
