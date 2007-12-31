package org.openmrs.hl7.handler;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptProposal;
import org.openmrs.Drug;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.HL7InError;
import org.openmrs.hl7.HL7Service;
import org.openmrs.util.OpenmrsConstants;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.Varies;
import ca.uhn.hl7v2.model.v25.datatype.CE;
import ca.uhn.hl7v2.model.v25.datatype.CWE;
import ca.uhn.hl7v2.model.v25.datatype.DLD;
import ca.uhn.hl7v2.model.v25.datatype.DT;
import ca.uhn.hl7v2.model.v25.datatype.DTM;
import ca.uhn.hl7v2.model.v25.datatype.IS;
import ca.uhn.hl7v2.model.v25.datatype.NM;
import ca.uhn.hl7v2.model.v25.datatype.PL;
import ca.uhn.hl7v2.model.v25.datatype.ST;
import ca.uhn.hl7v2.model.v25.datatype.TM;
import ca.uhn.hl7v2.model.v25.datatype.TS;
import ca.uhn.hl7v2.model.v25.datatype.XCN;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_PATIENT_RESULT;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import ca.uhn.hl7v2.model.v25.segment.ORC;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.model.v25.segment.PV1;
import ca.uhn.hl7v2.parser.EncodingCharacters;
import ca.uhn.hl7v2.parser.PipeParser;

public class ORUR01Handler implements Application {

	private Log log = LogFactory.getLog(ORUR01Handler.class);

	/**
	 * Always returns true, assuming that the router calling this handler will
	 * only call this handler with ORU_R01 messages.
	 * 
	 * @returns true
	 */
	public boolean canProcess(Message message) {
		return message != null && "ORU_R01".equals(message.getName());
	}

	/**
	 * Processes an ORU R01 event message
	 */
	public Message processMessage(Message message) throws ApplicationException {

		if (!(message instanceof ORU_R01))
			throw new ApplicationException(
					"Invalid message sent to ORU_R01 handler");

		if (log.isDebugEnabled())
			log.debug("Processing ORU_R01 message");

		Message response;
		try {
			ORU_R01 oru = (ORU_R01) message;
			response = processORU_R01(oru);
		} catch (ClassCastException e) {
			log.error("Error casting " + message.getClass().getName()
					+ " to ORU_R01", e);
			throw new ApplicationException("Invalid message type for handler");
		} catch (HL7Exception e) {
			log.error("Error while processing ORU_R01 message", e);
			throw new ApplicationException(e);
		}

		if (log.isDebugEnabled())
			log.debug("Finished processing ORU_R01 message");

		return response;
	}

	private Message processORU_R01(ORU_R01 oru) throws HL7Exception {

		// TODO: ideally, we would branch or alter our behavior based on the
		// sending application.
		// String sendingApplication = getSendingApplication(oru);

		// validate message
		validate(oru);

		// extract segments for convenient use below
		MSH msh = getMSH(oru);
		PID pid = getPID(oru);
		PV1 pv1 = getPV1(oru);
		ORC orc = getORC(oru); // we're using the ORC assoc with first OBR to
		// hold data enterer and date entered for now

		// Obtain message control id (unique ID for message from sending
		// application)
		String messageControlId = msh.getMessageControlID().getValue();
		if (log.isDebugEnabled())
			log.debug("Found HL7 message in inbound queue with control id = "
					+ messageControlId);

		HL7Service hl7Service = Context.getHL7Service();

		// create the encounter
		Patient patient = getPatient(pid);
		if (log.isDebugEnabled())
			log.debug("Processing HL7 message for patient "
					+ patient.getPatientId());
		Encounter encounter = createEncounter(msh, patient, pv1, orc);

		try {
			updateHealthCenter(patient, pv1);
		} catch (Exception e) {
			log.error("Error while processing Discharge To Location ("
					+ messageControlId + ")", e);
		}

		// create observations
		if (log.isDebugEnabled())
			log.debug("Creating observations for message " + messageControlId
					+ "...");
		ORU_R01_PATIENT_RESULT patientResult = oru.getPATIENT_RESULT();
		int numObr = patientResult.getORDER_OBSERVATIONReps();
		for (int i = 0; i < numObr; i++) {
			if (log.isDebugEnabled())
				log.debug("Processing OBR (" + i + " of " + numObr + ")");
			ORU_R01_ORDER_OBSERVATION orderObs = patientResult
					.getORDER_OBSERVATION(i);
			// OBR obr = orderObs.getOBR();
			Hashtable<String, Vector<Obs>> obsGroups = null;
			int numObs = orderObs.getOBSERVATIONReps();
			for (int j = 0; j < numObs; j++) {
				if (log.isDebugEnabled())
					log.debug("Processing OBS (" + j + " of " + numObs + ")");
				OBX obx = orderObs.getOBSERVATION(j).getOBX();
				try {
					if (log.isDebugEnabled())
						log.debug("Parsing observation");
					Obs obs = parseObs(encounter, obx);
					if (obs != null) {
						if (log.isDebugEnabled())
							log.debug("Obs is not null");
						String subId = obx.getObservationSubID().getValue();
						if (log.isDebugEnabled())
							log.debug("Obs sub id = " + subId);
						if (subId != null && subId.length() > 0) {
							if (obsGroups == null)
								obsGroups = new Hashtable<String, Vector<Obs>>();
							if (log.isDebugEnabled())
								log.debug("Adding obs to obs group");
							addToObsGroup(obsGroups, subId, obs);
						} else {
							if (log.isDebugEnabled())
								log.debug("Creating obs via API call");
							Context.getObsService().createObs(obs);
						}
						if (log.isDebugEnabled())
							log.debug("Done with this obs");
					}
				} catch (HL7Exception e) {
					// Handle obs-level exceptions
					log.warn("HL7Exception", e);
					HL7InError hl7InError = new HL7InError();
					hl7InError.setError(e.getMessage());
					hl7InError.setErrorDetails(PipeParser.encode(obx,
							new EncodingCharacters('|', "^~\\&")));
					hl7InError.setHL7SourceKey(messageControlId);
					hl7Service.createHL7InError(hl7InError);
				}
			}
			if (obsGroups != null && obsGroups.size() > 0) {
				if (log.isDebugEnabled())
					log.debug("Processing " + obsGroups.size()
							+ " obs group(s)");
				for (Vector<Obs> group : obsGroups.values()) {
					Obs[] groupArray = new Obs[group.size()];
					group.toArray(groupArray);
					if (groupArray.length == 1) {
						if (log.isDebugEnabled())
							log
									.debug("Creating obs (single entry within obs group)");
						Context.getObsService().createObs(groupArray[0]);
					} else if (groupArray.length > 1) {
						if (log.isDebugEnabled())
							log.debug("Creating obs group");
						Context.getObsService().createObsGroup(groupArray);
					}
				}
				if (log.isDebugEnabled())
					log.debug("Finished creating obs group(s)");
			}
		}
		if (log.isDebugEnabled())
			log.debug("Finished creating observations");
		
		// Notify HL7 service that we have created a new encounter, allowing
		// features/modules to trigger on HL7-generated encounters.
		// TODO: this can be removed once we have a obs_group table and all
		// obs can be created in memory as part of the encounter *before* we
		// call EncounterService.createEncounter().  For now, making obs groups
		// requires that one obs be created (in the database) before others can
		// be linked to it, forcing us to save the encounter prematurely.
		log.debug("Current thread: " + Thread.currentThread());
		hl7Service.encounterCreated(encounter);
		
		return oru;

	}

	// private String getSendingApplication(ORU_R01 oru) {
	// return oru.getMSH().getSendingApplication().getUniversalID().getValue();
	// }

	private void addToObsGroup(Hashtable<String, Vector<Obs>> obsGroups,
			String subId, Obs obs) {
		int periodPos = subId.indexOf('.');
		String subIdKey = (periodPos == -1 ? subId : subId.substring(0,
				periodPos));
		if (obsGroups.containsKey(subIdKey)) {
			obsGroups.get(subIdKey).add(obs);
		} else {
			Vector<Obs> entry = new Vector<Obs>();
			entry.add(obs);
			obsGroups.put(subIdKey, entry);
		}

	}

	private void validate(Message message) throws HL7Exception {
		// TODO: check version, etc.
	}

	private MSH getMSH(ORU_R01 oru) {
		return oru.getMSH();
	}

	private PID getPID(ORU_R01 oru) {
		return oru.getPATIENT_RESULT().getPATIENT().getPID();
	}

	private PV1 getPV1(ORU_R01 oru) {
		return oru.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1();
	}

	private ORC getORC(ORU_R01 oru) {
		return oru.getPATIENT_RESULT().getORDER_OBSERVATION().getORC();
	}

	/**
	 * Creates an encounter
	 */
	private Encounter createEncounter(MSH msh, Patient patient, PV1 pv1, ORC orc)
			throws HL7Exception {
		Encounter encounter = new Encounter();

		Date encounterDate = getEncounterDate(pv1);
		User provider = getProvider(pv1);
		Location location = getLocation(pv1);
		Form form = getForm(msh);
		EncounterType encounterType = getEncounterType(msh, form);
		User enterer = getEnterer(orc);
		Date dateEntered = getDateEntered(orc);

		encounter.setEncounterDatetime(encounterDate);
		encounter.setProvider(provider);
		encounter.setPatient(patient);
		encounter.setLocation(location);
		encounter.setForm(form);
		encounter.setEncounterType(encounterType);
		encounter.setCreator(enterer);
		encounter.setDateCreated(dateEntered);
		if (log.isDebugEnabled())
			log.debug("Creating encounter");
		Context.getEncounterService().createEncounter(encounter);

		if (encounter == null || encounter.getEncounterId() == null
				|| encounter.getEncounterId() == 0) {
			throw new HL7Exception("Invalid encounter");
		}
		return encounter;
	}

	private Obs parseObs(Encounter encounter, OBX obx) throws HL7Exception {
		if (log.isDebugEnabled())
			log.debug("parsing observation: " + obx);
		Varies[] values = obx.getObservationValue();
		if (values == null || values.length < 1)
			return null;
		String hl7Datatype = values[0].getName();
		if (log.isDebugEnabled())
			log.debug("  datatype = " + hl7Datatype);
		Concept concept = getConcept(obx);
		if (log.isDebugEnabled())
			log.debug("  concept = " + concept.getConceptId());
		Date datetime = getDatetime(obx);
		if (log.isDebugEnabled())
			log.debug("  timestamp = " + datetime);
		if (datetime == null)
			datetime = encounter.getEncounterDatetime();

		Obs obs = new Obs();
		obs.setPerson(encounter.getPatient());
		obs.setConcept(concept);
		obs.setEncounter(encounter);
		obs.setObsDatetime(datetime);
		obs.setLocation(encounter.getLocation());
		obs.setCreator(encounter.getCreator());
		obs.setDateCreated(encounter.getDateCreated());
		Type obx5 = values[0].getData();
		if ("NM".equals(hl7Datatype)) {
			String value = ((NM) obx5).getValue();
			obs.setValueNumeric(Double.valueOf(value));
		} else if ("CWE".equals(hl7Datatype)) {
			log.debug("  CWE observation");
			CWE value = (CWE) obx5;
			String valueIdentifier = value.getIdentifier().getValue();
			log.debug("    value id = " + valueIdentifier);
			String valueName = value.getText().getValue();
			log.debug("    value name = " + valueName);
			if (isConceptProposal(valueIdentifier)) {
				if (log.isDebugEnabled())
					log.debug("Proposing concept");
				proposeConcept(encounter, concept, valueName);
			} else {
				log.debug("    not proposal");
				try {
					Concept valueConcept = new Concept();
					valueConcept.setConceptId(new Integer(valueIdentifier));
					obs.setValueCoded(valueConcept);
					if ("99RX".equals(value.getNameOfAlternateCodingSystem()
							.getValue())) {
						Drug valueDrug = new Drug();
						valueDrug.setDrugId(new Integer(value
								.getAlternateIdentifier().getValue()));
						obs.setValueDrug(valueDrug);
					}
				} catch (NumberFormatException e) {
					throw new HL7Exception("Invalid concept ID '"
							+ valueIdentifier + "' for OBX-5 value '"
							+ valueName + "'");
				}
			}
			if (log.isDebugEnabled())
				log.debug("  Done with CWE");
		} else if ("CE".equals(hl7Datatype)) {
			CE value = (CE) obx5;
			String valueIdentifier = value.getIdentifier().getValue();
			String valueName = value.getText().getValue();
			if (isConceptProposal(valueIdentifier))
				proposeConcept(encounter, concept, valueName);
			else {
				try {
					Concept valueCoded = new Concept();
					valueCoded.setConceptId(new Integer(valueIdentifier));
					obs.setValueCoded(valueCoded);
				} catch (NumberFormatException e) {
					throw new HL7Exception("Invalid concept ID '"
							+ valueIdentifier + "' for OBX-5 value '"
							+ valueName + "'");
				}
			}
		} else if ("DT".equals(hl7Datatype)) {
			DT value = (DT) obx5;
			Date valueDate = getDate(value.getYear(), value.getMonth(), value
					.getDay(), 0, 0, 0);
			obs.setValueDatetime(valueDate);
		} else if ("TS".equals(hl7Datatype)) {
			DTM value = ((TS) obx5).getTime();
			Date valueDate = getDate(value.getYear(), value.getMonth(), value
					.getDay(), value.getHour(), value.getMinute(), value
					.getSecond());
			obs.setValueDatetime(valueDate);
		} else if ("TM".equals(hl7Datatype)) {
			TM value = (TM) obx5;
			Date valueTime = getDate(0, 0, 0, value.getHour(), value
					.getMinute(), value.getSecond());
			obs.setValueDatetime(valueTime);
		} else if ("ST".equals(hl7Datatype)) {
			ST value = (ST) obx5;
			obs.setValueText(value.getValue());
		} else {
			// unsupported data type
			// TODO: support RP (report), SN (structured numeric)
			// do we need to support BIT just in case it slips thru?
			throw new HL7Exception("Unsupported observation datatype '"
					+ hl7Datatype + "'");
		}
		return obs;
	}

	private boolean isConceptProposal(String identifier) {
		return identifier.equals(OpenmrsConstants.PROPOSED_CONCEPT_IDENTIFIER);
	}

	private Date getDate(int year, int month, int day, int hour, int minute,
			int second) {
		Calendar cal = Calendar.getInstance();
		// Calendar.set(MONTH, int) is zero-based, Hl7 is not
		cal.set(year, month - 1, day, hour, minute, second);
		return cal.getTime();
	}

	private Concept getConcept(OBX obx) throws HL7Exception {
		// TODO: don't assume that all concepts are local
		String hl7ConceptId = obx.getObservationIdentifier().getIdentifier()
				.getValue();
		try {
			Integer conceptId = new Integer(hl7ConceptId);
			Concept concept = new Concept();
			concept.setConceptId(conceptId);
			return concept;
		} catch (NumberFormatException e) {
			throw new HL7Exception("Invalid concept ID '" + hl7ConceptId + "'");
		}
	}

	private Date getDatetime(OBX obx) throws HL7Exception {
		Date datetime = null;
		TS ts = obx.getDateTimeOfTheObservation();
		DTM value = ts.getTime();

		if (value.getYear() == 0 || value.getValue() == null)
			return null;

		try {
			datetime = getDate(value.getYear(), value.getMonth(), value
					.getDay(), value.getHour(), value.getMinute(), value
					.getSecond());
		} catch (DataTypeException e) {

		}
		return datetime;
	}

	private Date getEncounterDate(PV1 pv1) throws HL7Exception {
		return tsToDate(pv1.getAdmitDateTime());
	}

	private User getProvider(PV1 pv1) throws HL7Exception {
		XCN hl7Provider = pv1.getAttendingDoctor(0);
		Integer providerId = Context.getHL7Service().resolveUserId(hl7Provider);
		if (providerId == null)
			throw new HL7Exception("Could not resolve provider");
		User provider = new User();
		provider.setUserId(providerId);
		return provider;
	}

	private Patient getPatient(PID pid) throws HL7Exception {
		Integer patientId = Context.getHL7Service().resolvePatientId(pid);
		if (patientId == null)
			throw new HL7Exception("Could not resolve patient");
		Patient patient = new Patient();
		patient.setPatientId(patientId);
		return patient;
	}

	private Location getLocation(PV1 pv1) throws HL7Exception {
		PL hl7Location = pv1.getAssignedPatientLocation();
		Integer locationId = Context.getHL7Service().resolveLocationId(
				hl7Location);
		if (locationId == null)
			throw new HL7Exception("Could not resolve location");
		Location location = new Location();
		location.setLocationId(locationId);
		return location;
	}

	private Form getForm(MSH msh) throws HL7Exception {
		Integer formId = null;
		try {
			formId = Integer.parseInt(msh.getMessageProfileIdentifier(0)
					.getEntityIdentifier().getValue());
		} catch (Exception e) {
			throw new HL7Exception("Error parsing form id from message", e);
		}

		// must get entire form object in order to get its metadata
		// (encounterType) later
		Form form = null;
		if (formId != null)
			form = Context.getFormService().getForm(formId);

		return form;
	}

	private EncounterType getEncounterType(MSH msh, Form form) {
		if (form != null)
			return form.getEncounterType();
		// TODO: resolve encounter type from MSH data - do we need PV1 too?
		return null;
	}

	private User getEnterer(ORC orc) throws HL7Exception {
		XCN hl7Enterer = orc.getEnteredBy(0);
		Integer entererId = Context.getHL7Service().resolveUserId(hl7Enterer);
		if (entererId == null)
			throw new HL7Exception("Could not resolve enterer");
		User enterer = new User();
		enterer.setUserId(entererId);
		return enterer;
	}

	private Date getDateEntered(ORC orc) throws HL7Exception {
		return tsToDate(orc.getDateTimeOfTransaction());
	}

	//TODO: Debug (and use) methods in HL7Util instead
	private Date tsToDate(TS ts) throws HL7Exception {
		// need to handle timezone
		String dtm = ts.getTime().getValue();
		int year = Integer.parseInt(dtm.substring(0, 4));
		int month = (dtm.length() >= 6 ? Integer.parseInt(dtm.substring(4, 6)) - 1
				: 0);
		int day = (dtm.length() >= 8 ? Integer.parseInt(dtm.substring(6, 8))
				: 1);
		int hour = (dtm.length() >= 10 ? Integer.parseInt(dtm.substring(8, 10))
				: 0);
		int min = (dtm.length() >= 12 ? Integer.parseInt(dtm.substring(10, 12))
				: 0);
		int sec = (dtm.length() >= 14 ? Integer.parseInt(dtm.substring(12, 14))
				: 0);
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day, hour, min, sec);
		// if (cal.getTimeZone().getRawOffset() != timeZoneOffsetMillis) {
		// TimeZone tz = (TimeZone)TimeZone.getDefault().clone();
		// tz.setRawOffset(timeZoneOffsetMillis);
		// cal.setTimeZone(tz);
		// }
		return cal.getTime();
	}

	/**
	 * Generates a ConceptProposal record
	 */
	private void proposeConcept(Encounter encounter, Concept concept,
			String originalText) {
		// value is a proposed concept, create a ConceptProposal
		// instead of an Obs for this observation
		// TODO: at this point if componentSeparator (^) is in text,
		// we'll only use the text before that delimiter!
		ConceptProposal conceptProposal = new ConceptProposal();
		conceptProposal.setOriginalText(originalText);
		conceptProposal.setState(OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED);
		conceptProposal.setEncounter(encounter);
		conceptProposal.setObsConcept(concept);
		Context.getConceptService().proposeConcept(conceptProposal);
	}

	private void updateHealthCenter(Patient patient, PV1 pv1) {
		// Update patient's location if it has changed
		if (log.isDebugEnabled())
			log.debug("Checking for discharge to location");
		DLD dld = pv1.getDischargedToLocation();
		log.debug("DLD = " + dld);
		if (dld == null)
			return;
		IS hl7DischargeToLocation = dld.getDischargeLocation();
		log.debug("is = " + hl7DischargeToLocation);
		if (hl7DischargeToLocation == null)
			return;
		String dischargeToLocation = hl7DischargeToLocation.getValue();
		log.debug("dischargeToLocation = " + dischargeToLocation);
		if (dischargeToLocation != null && dischargeToLocation.length() > 0) {
			if (log.isDebugEnabled())
				log.debug("Patient discharged to " + dischargeToLocation);
			// Ignore anything past the first subcomponent (or component)
			// delimiter
			for (int i = 0; i < dischargeToLocation.length(); i++) {
				char ch = dischargeToLocation.charAt(i);
				if (ch == '&' || ch == '^') {
					dischargeToLocation = dischargeToLocation.substring(0, i);
					break;
				}
			}
			Integer newLocationId = Integer.parseInt(dischargeToLocation);
			// Hydrate a full patient object from patient object containing only
			// identifier
			patient = Context.getPatientService().getPatient(
					patient.getPatientId());
			
			PersonAttributeType healthCenterAttrType = Context.getPersonService().getPersonAttributeType("Health Center");
			
			if (healthCenterAttrType == null) {
				log.error("A person attribute type with name 'Health Center' is not defined but patient " + 
						patient.getPatientId() + 
						" is trying to change their health center to " + 
						newLocationId);
				return;
			}
			
			PersonAttribute currentHealthCenter = patient.getAttribute("Health Center");
			
			if (currentHealthCenter == null || !currentHealthCenter.equals(newLocationId.toString())) {
				PersonAttribute newHealthCenter = new PersonAttribute(healthCenterAttrType, newLocationId.toString());
				
				log.debug("Updating patient's location from "
						+ currentHealthCenter + " to "
						+ newLocationId);
				
				// add attribute (and void old if there is one)
				patient.addAttribute(newHealthCenter);
				
				// save the patient and their new attribute
				Context.getPatientService().updatePatient(patient);
			}
			
		}
		log.debug("finished discharge to location method");
	}

}
