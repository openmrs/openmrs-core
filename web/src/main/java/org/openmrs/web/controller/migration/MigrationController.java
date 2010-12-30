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
package org.openmrs.web.controller.migration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Location;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.HL7InQueue;
import org.openmrs.hl7.HL7Service;
import org.openmrs.hl7.HL7Source;
import org.openmrs.migration.MigrationHelper;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.RedirectView;

public class MigrationController implements Controller {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
	        IOException {
		
		Map<String, Object> myModel = new HashMap<String, Object>();
		if (Context.isAuthenticated()) {
			String message = request.getParameter("message");
			if (message == null || message.length() == 0) {
				message = "Paste some xml";
			}
			
			LocationService ls = Context.getLocationService();
			List<Location> locations = ls.getAllLocations();
			UserService us = Context.getUserService();
			List<User> users = us.getAllUsers();
			
			myModel.put("message", message);
			myModel.put("locations", locations);
			myModel.put("users", users);
		}
		
		return new ModelAndView("/migration/migration", "model", myModel);
	}
	
	public ModelAndView uploadUsers(HttpServletRequest request, HttpServletResponse response) throws ServletException,
	        IOException, ParserConfigurationException, ParseException {
		String xml = request.getParameter("user_xml");
		log.debug("xml to upload = " + xml);
		int numAdded = MigrationHelper.importUsers(MigrationHelper.parseXml(xml));
		return new ModelAndView(new RedirectView("migration.form?message="
		        + URLEncoder.encode("Added " + numAdded + " users", "UTF-8")));
	}
	
	public ModelAndView uploadLocations(HttpServletRequest request, HttpServletResponse response) throws ServletException,
	        IOException, ParserConfigurationException {
		String xml = request.getParameter("location_xml");
		log.debug("xml to upload = " + xml);
		int numAdded = MigrationHelper.importLocations(MigrationHelper.parseXml(xml));
		return new ModelAndView(new RedirectView("migration.form?message="
		        + URLEncoder.encode("Uploaded " + numAdded + " locations", "UTF-8")));
	}
	
	public ModelAndView runHl7(HttpServletRequest request, HttpServletResponse response) throws ServletException,
	        IOException {
		List<String> messages = new ArrayList<String>();
		
		String filename = request.getParameter("filename");
		if (filename != null && filename.length() > 0) {
			try {
				BufferedReader r = new BufferedReader(new FileReader(filename));
				StringBuilder thisMessage = new StringBuilder();
				while (true) {
					String line = r.readLine();
					if (line == null || line.startsWith("MSH")) {
						if (thisMessage.length() != 0) {
							messages.add(thisMessage.toString());
							log.debug("read message : " + thisMessage);
							if (messages.size() % 100 == 0) {
								log.debug("read " + messages.size() + " messages so far");
							}
							thisMessage = new StringBuilder();
						}
					}
					if (line == null) {
						break;
					}
					thisMessage.append(line).append('\r');
				}
			}
			catch (Exception ex) {
				log.error("Failed to read hl7 input file " + filename, ex);
				throw new RuntimeException(ex);
			}
		} else {
			String hl7 = request.getParameter("hl7");
			hl7 = hl7.replaceAll("\\n", "");
			for (int index = hl7.indexOf("MSH"); index >= 0; index = hl7.indexOf("MSH", index + 1)) {
				int endIndex = hl7.indexOf("MSH", index + 1);
				String oneMessage = endIndex <= 0 ? hl7.substring(index) : hl7.substring(index, endIndex);
				messages.add(oneMessage);
				// log.debug("has slashR: " + (oneMessage.indexOf("\r") > 0) + " , has slashN: " + (oneMessage.indexOf("\n") > 0));
			}
		}
		
		// log.debug("About to handle " + messages.size() + " messages");
		
		// split into messages
		for (String oneMessage : messages) {
			log.debug("oneMessage: " + oneMessage);
			// Confusing terminology: an HL7InQueue is just one entry in the queue
			HL7InQueue hl7InQueue = new HL7InQueue();
			hl7InQueue.setHL7Data(oneMessage);
			HL7Service hs = Context.getHL7Service();
			if (hs.getAllHL7Sources().isEmpty()) {
				HL7Source hl7Source = new HL7Source();
				hl7Source.setName("MigrationTestTool");
				hl7Source.setDescription("Testing migrating data, from MigrationController.");
				hs.saveHL7Source(hl7Source);
			}
			hl7InQueue.setHL7Source(Context.getHL7Service().getHL7Source(1));
			log.debug("hl7InQueue.hl7Data: " + hl7InQueue.getHL7Data());
			Context.getHL7Service().saveHL7InQueue(hl7InQueue);
		}
		
		return new ModelAndView(new RedirectView("migration.form"));
	}
	
	// Hardcoded for PIH v1-v2 migration. Sorry about that.
	public ModelAndView uploadRegimens(HttpServletRequest request, HttpServletResponse response) throws ServletException,
	        IOException, ParseException {
		String csv = request.getParameter("regimen_csv");
		int numAdded = importRegimens(csv);
		
		return new ModelAndView(new RedirectView("migration.form?message="
		        + URLEncoder.encode("Uploaded " + numAdded + " regimens", "UTF-8")));
	}
	
	/**
	 * TODO: DOCUMENT THIS
	 */
	public ModelAndView uploadMigrationFile(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException, ParseException {
		String filename = request.getParameter("filename");
		if (filename == null || filename.length() == 0)
			throw new IllegalArgumentException("Must specify a 'filename' parameter");
		
		boolean autoCreateUsers = false;
		boolean autoAddRole = false;
		try {
			autoCreateUsers = request.getParameter("auto_create_users").toLowerCase().startsWith("t");
		}
		catch (Exception ex) {}
		try {
			autoAddRole = request.getParameter("add_role_when_creating_users").toLowerCase().startsWith("t");
		}
		catch (Exception ex) {}
		
		List<String> relationships = new ArrayList<String>();
		List<String> programWorkflow = new ArrayList<String>();
		BufferedReader r = new BufferedReader(new FileReader(filename));
		while (true) {
			String s = r.readLine();
			if (s == null)
				break;
			s = s.trim();
			if (s.length() == 0)
				continue;
			if (s.startsWith("RELATIONSHIP:")) {
				relationships.add(s);
			} else if (s.startsWith("ENROLLMENT:") || s.startsWith("STATUS:")) {
				programWorkflow.add(s);
			} else {
				throw new IllegalArgumentException("Don't know how to handle '" + s + "'");
			}
		}
		int numRels = MigrationHelper.importRelationships(relationships, autoCreateUsers, autoAddRole);
		String message = "";
		message += "Uploaded " + numRels + " relationships<br/>";
		int numProgram = MigrationHelper.importProgramsAndStatuses(programWorkflow);
		message += "Uploaded " + numProgram + " programs and statuses<br/>";
		return new ModelAndView(new RedirectView("migration.form?message=" + URLEncoder.encode(message, "UTF-8")));
	}
	
	// takes something like "^glokawera@pih.org" and returns a user with that username (after the ^)
	private User userHelper(String username) {
		if (username == null)
			return null;
		int ind = username.indexOf('^');
		if (ind >= 0)
			username = username.substring(ind + 1);
		return Context.getUserService().getUserByUsername(username);
	}
	
	/**
	 * Takes CSV like: patientId,drugName,formulationName,startDate,autoExpireDate,discontinuedDate,
	 * discontinuedReason,doseStrength,doseUnit,dosesPerDay,daysPerWeek,prn
	 * 
	 * @return The number of regimens added
	 */
	public int importRegimens(String csv) throws IOException, ParseException {
		PatientIdentifierType pihIdentifierType = Context.getPatientService().getPatientIdentifierTypeByName("HIVEMR-V1");
		OrderType orderType = Context.getOrderService().getOrderType(1);
		if (!orderType.getName().equals("Drug Order")) {
			throw new RuntimeException("ERROR! ASSUMED THAT ORDER TYPE 1 IS DRUG ORDER, BUT IT'S NOT");
		}
		Map<Integer, List<Order>> patientRegimens = new HashMap<Integer, List<Order>>();
		int numAdded = 0;
		BufferedReader r = new BufferedReader(new StringReader(csv));
		for (String s = r.readLine(); s != null; s = r.readLine()) {
			String[] st = s.split(",");
			Integer patientId = Integer.valueOf(st[0]);
			//String drugName = st[1]; // ignored for now
			String formulationName = st[2];
			Date startDate = parseDate(st[3]);
			Date autoExpireDate = parseDate(st[4]);
			Date discontinuedDate = parseDate(st[5]);
			String discontinuedReason = st[6];
			if (discontinuedReason.trim().length() == 0)
				discontinuedReason = null;
			Double doseStrength = Double.parseDouble(st[7]);
			String doseUnit = st[8];
			if (doseUnit.trim().length() == 0)
				doseUnit = null;
			Integer dosesPerDay = Integer.valueOf(st[9]);
			Integer daysPerWeek = Integer.valueOf(st[10]);
			Boolean prn = Boolean.valueOf(st[11]);
			String creator = st[12];
			Date dateCreated = parseDate(st[13]);
			String discontinuedBy = st.length > 14 ? st[14] : null;
			
			if (dosesPerDay == null || dosesPerDay == 0) {
				throw new IllegalArgumentException("Doses per day must be a positive integer");
			}
			Drug drug = Context.getConceptService().getDrug(formulationName);
			if (drug == null)
				throw new IllegalArgumentException("Can't find drug '" + formulationName + "'");
			
			DrugOrder reg = new DrugOrder();
			reg.setDrug(drug);
			reg.setConcept(drug.getConcept());
			reg.setStartDate(startDate);
			reg.setAutoExpireDate(autoExpireDate);
			reg.setDiscontinued(discontinuedDate != null);
			reg.setDiscontinuedDate(discontinuedDate);
			//reg.setDiscontinuedReason(discontinuedReason);
			reg.setDose(doseStrength);
			reg.setEquivalentDailyDose(doseStrength);
			reg.setUnits(doseUnit);
			reg.setFrequency(dosesPerDay + "/day x " + daysPerWeek + " days/week");
			reg.setPrn(prn);
			reg.setComplex(false);
			reg.setOrderType(orderType);
			reg.setCreator(userHelper(creator));
			reg.setDateCreated(dateCreated);
			reg.setDiscontinuedBy(userHelper(discontinuedBy));
			List<Order> pat = patientRegimens.get(patientId);
			if (pat == null) {
				pat = new ArrayList<Order>();
				patientRegimens.put(patientId, pat);
			}
			pat.add(reg);
		}
		for (Map.Entry<Integer, List<Order>> e : patientRegimens.entrySet()) {
			List<PatientIdentifier> pil = Context.getPatientService().getPatientIdentifiers(e.getKey().toString(),
			    Collections.singletonList(pihIdentifierType), null, null, null);
			if (pil.size() != 1) {
				throw new RuntimeException("Found " + pil.size() + " PatientIdentifiers for " + pihIdentifierType + " of "
				        + e.getKey());
			}
			Patient p = pil.get(0).getPatient();
			List<Order> list = e.getValue();
			Context.getOrderService().createOrdersAndEncounter(p, list);
			numAdded += list.size();
		}
		return numAdded;
	}
	
	static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static Date parseDate(String s) throws ParseException {
		if (s == null || s.length() == 0) {
			return null;
		} else {
			if (s.length() == 10) {
				s += " 00:00:00";
			}
			return df.parse(s);
		}
	}
	
}
