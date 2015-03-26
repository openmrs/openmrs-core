/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.hl7.handler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptName;
import org.openmrs.ConceptProposal;
import org.openmrs.Drug;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.Provider;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.HL7Constants;
import org.openmrs.hl7.HL7InQueueProcessor;
import org.openmrs.hl7.HL7Service;
import org.openmrs.obs.ComplexData;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.util.StringUtils;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.Varies;
import ca.uhn.hl7v2.model.v25.datatype.CE;
import ca.uhn.hl7v2.model.v25.datatype.CWE;
import ca.uhn.hl7v2.model.v25.datatype.CX;
import ca.uhn.hl7v2.model.v25.datatype.DLD;
import ca.uhn.hl7v2.model.v25.datatype.DT;
import ca.uhn.hl7v2.model.v25.datatype.DTM;
import ca.uhn.hl7v2.model.v25.datatype.ED;
import ca.uhn.hl7v2.model.v25.datatype.EI;
import ca.uhn.hl7v2.model.v25.datatype.FT;
import ca.uhn.hl7v2.model.v25.datatype.ID;
import ca.uhn.hl7v2.model.v25.datatype.IS;
import ca.uhn.hl7v2.model.v25.datatype.NM;
import ca.uhn.hl7v2.model.v25.datatype.PL;
import ca.uhn.hl7v2.model.v25.datatype.ST;
import ca.uhn.hl7v2.model.v25.datatype.TM;
import ca.uhn.hl7v2.model.v25.datatype.TS;
import ca.uhn.hl7v2.model.v25.datatype.XCN;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_OBSERVATION;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION;
import ca.uhn.hl7v2.model.v25.group.ORU_R01_PATIENT_RESULT;
import ca.uhn.hl7v2.model.v25.message.ORU_R01;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.model.v25.segment.NK1;
import ca.uhn.hl7v2.model.v25.segment.OBR;
import ca.uhn.hl7v2.model.v25.segment.OBX;
import ca.uhn.hl7v2.model.v25.segment.ORC;
import ca.uhn.hl7v2.model.v25.segment.PID;
import ca.uhn.hl7v2.model.v25.segment.PV1;
import ca.uhn.hl7v2.parser.EncodingCharacters;
import ca.uhn.hl7v2.parser.PipeParser;

/**
 * Parses ORUR01 messages into openmrs Encounter objects Usage: GenericParser parser = new
 * GenericParser(); MessageTypeRouter router = new MessageTypeRouter();
 * router.registerApplication("ORU", "R01", new ORUR01Handler()); Message hl7message =
 * parser.parse(somehl7string);
 *
 * @see HL7InQueueProcessor
 */
public class ORUR01Handler implements Application {
	
	private Log log = LogFactory.getLog(ORUR01Handler.class);
	
	private static EncounterRole unknownRole = null;
	
	/**
	 * Always returns true, assuming that the router calling this handler will only call this
	 * handler with ORU_R01 messages.
	 *
	 * @return true
	 */
	@Override
	public boolean canProcess(Message message) {
		return message != null && "ORU_R01".equals(message.getName());
	}
	
	/**
	 * Processes an ORU R01 event message
	 *
	 * @should create encounter and obs from hl7 message
	 * @should create basic concept proposal
	 * @should create concept proposal and with obs alongside
	 * @should not create problem list observation with concept proposals
	 * @should append to an existing encounter
	 * @should create obs group for OBRs
	 * @should create obs valueCodedName
	 * @should fail on empty concept proposals
	 * @should fail on empty concept answers
	 * @should set value_Coded matching a boolean concept for obs if the answer is 0 or 1 and
	 *         Question datatype is coded
	 * @should set value as boolean for obs if the answer is 0 or 1 and Question datatype is Boolean
	 * @should set value_Numeric for obs if Question datatype is Numeric and the answer is either 0
	 *         or 1
	 * @should set value_Numeric for obs if Question datatype is Numeric
	 * @should fail if question datatype is coded and a boolean is not a valid answer
	 * @should fail if question datatype is neither Boolean nor numeric nor coded
	 * @should create an encounter and find the provider by identifier
	 * @should create an encounter and find the provider by personId
	 * @should create an encounter and find the provider by uuid
	 * @should create an encounter and find the provider by providerId
	 * @should fail if the provider name type code is not specified and is not a personId
	 * @should understand form uuid if present
	 * @should prefer form uuid over id if both are present
	 * @should prefer form id if uuid is not found
	 * @should set complex data for obs with complex concepts
	 */
	@Override
	public Message processMessage(Message message) throws ApplicationException {
		
		if (!(message instanceof ORU_R01)) {
			throw new ApplicationException(Context.getMessageSourceService().getMessage("ORUR01.error.invalidMessage"));
		}
		
		log.debug("Processing ORU_R01 message");
		
		Message response;
		try {
			ORU_R01 oru = (ORU_R01) message;
			response = processORU_R01(oru);
		}
		catch (ClassCastException e) {
			log.warn("Error casting " + message.getClass().getName() + " to ORU_R01", e);
			throw new ApplicationException(Context.getMessageSourceService().getMessage("ORUR01.error.invalidMessageType ",
			    new Object[] { message.getClass().getName() }, null), e);
		}
		catch (HL7Exception e) {
			log.warn("Error while processing ORU_R01 message", e);
			throw new ApplicationException(Context.getMessageSourceService().getMessage("ORUR01.error.WhileProcessing"), e);
		}
		
		log.debug("Finished processing ORU_R01 message");
		
		return response;
	}
	
	/**
	 * Bulk of the processing done here. Called by the main processMessage method
	 *
	 * @param oru the message to process
	 * @return the processed message
	 * @throws HL7Exception
	 * @should process multiple NK1 segments
	 */
	@SuppressWarnings("deprecation")
	private Message processORU_R01(ORU_R01 oru) throws HL7Exception {
		
		// TODO: ideally, we would branch or alter our behavior based on the
		// sending application.
		// String sendingApplication = getSendingApplication(oru);
		
		// validate message
		validate(oru);
		
		// extract segments for convenient use below
		MSH msh = getMSH(oru);
		PID pid = getPID(oru);
		List<NK1> nk1List = getNK1List(oru);
		PV1 pv1 = getPV1(oru);
		ORC orc = getORC(oru); // we're using the ORC assoc with first OBR to
		// hold data enterer and date entered for now
		
		// Obtain message control id (unique ID for message from sending
		// application)
		String messageControlId = msh.getMessageControlID().getValue();
		if (log.isDebugEnabled()) {
			log.debug("Found HL7 message in inbound queue with control id = " + messageControlId);
		}
		
		HL7Service hl7Service = Context.getHL7Service();
		
		// create the encounter
		Patient patient = getPatient(pid);
		if (log.isDebugEnabled()) {
			log.debug("Processing HL7 message for patient " + patient.getPatientId());
		}
		Encounter encounter = createEncounter(msh, patient, pv1, orc);
		
		// do the discharge to location logic
		try {
			updateHealthCenter(patient, pv1);
		}
		catch (Exception e) {
			log.error("Error while processing Discharge To Location (" + messageControlId + ")", e);
		}
		
		// process NK1 (relationship) segments
		for (NK1 nk1 : nk1List) {
			processNK1(patient, nk1);
		}
		
		// list of concepts proposed in the obs of this encounter.
		// these proposals need to be created after the encounter
		// has been created
		List<ConceptProposal> conceptProposals = new ArrayList<ConceptProposal>();
		
		// create observations
		if (log.isDebugEnabled()) {
			log.debug("Creating observations for message " + messageControlId + "...");
		}
		// we ignore all MEDICAL_RECORD_OBSERVATIONS that are OBRs.  We do not
		// create obs_groups for them
		List<Integer> ignoredConceptIds = new ArrayList<Integer>();
		
		String obrConceptId = Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_MEDICAL_RECORD_OBSERVATIONS, "1238");
		if (StringUtils.hasLength(obrConceptId)) {
			ignoredConceptIds.add(Integer.valueOf(obrConceptId));
		}
		
		// we also ignore all PROBLEM_LIST that are OBRs
		String obrProblemListConceptId = Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PROBLEM_LIST, "1284");
		if (StringUtils.hasLength(obrProblemListConceptId)) {
			ignoredConceptIds.add(Integer.valueOf(obrProblemListConceptId));
		}
		
		ORU_R01_PATIENT_RESULT patientResult = oru.getPATIENT_RESULT();
		int numObr = patientResult.getORDER_OBSERVATIONReps();
		for (int i = 0; i < numObr; i++) {
			if (log.isDebugEnabled()) {
				log.debug("Processing OBR (" + i + " of " + numObr + ")");
			}
			ORU_R01_ORDER_OBSERVATION orderObs = patientResult.getORDER_OBSERVATION(i);
			
			// the parent obr
			OBR obr = orderObs.getOBR();
			
			if (!StringUtils.hasText(obr.getUniversalServiceIdentifier().getIdentifier().getValue())) {
				throw new HL7Exception(Context.getMessageSourceService().getMessage("ORUR01.errorInvalidOBR ",
				    new Object[] { messageControlId }, null));
			}
			
			// if we're not ignoring this obs group, create an
			// Obs grouper object that the underlying obs objects will use
			Obs obsGrouper = null;
			Concept obrConcept = getConcept(obr.getUniversalServiceIdentifier(), messageControlId);
			if (obrConcept != null && !ignoredConceptIds.contains(obrConcept.getId())) {
				// maybe check for a parent obs group from OBR-29 Parent ?
				
				// create an obs for this obs group too
				obsGrouper = new Obs();
				obsGrouper.setConcept(obrConcept);
				obsGrouper.setPerson(encounter.getPatient());
				obsGrouper.setEncounter(encounter);
				Date datetime = getDatetime(obr);
				if (datetime == null) {
					datetime = encounter.getEncounterDatetime();
				}
				obsGrouper.setObsDatetime(datetime);
				obsGrouper.setLocation(encounter.getLocation());
				obsGrouper.setCreator(encounter.getCreator());
				
				// set comments if there are any
				StringBuilder comments = new StringBuilder();
				ORU_R01_ORDER_OBSERVATION parent = (ORU_R01_ORDER_OBSERVATION) obr.getParent();
				int totalNTEs = parent.getNTEReps();
				for (int iNTE = 0; iNTE < totalNTEs; iNTE++) {
					for (FT obxComment : parent.getNTE(iNTE).getComment()) {
						if (comments.length() > 0) {
							comments.append(" ");
						}
						comments.append(obxComment.getValue());
					}
				}
				// only set comments if there are any
				if (StringUtils.hasText(comments.toString())) {
					obsGrouper.setComment(comments.toString());
				}
				
				// add this obs as another row in the obs table
				encounter.addObs(obsGrouper);
			}
			
			// loop over the obs and create each object, adding it to the encounter
			int numObs = orderObs.getOBSERVATIONReps();
			HL7Exception errorInHL7Queue = null;
			for (int j = 0; j < numObs; j++) {
				if (log.isDebugEnabled()) {
					log.debug("Processing OBS (" + j + " of " + numObs + ")");
				}
				
				OBX obx = orderObs.getOBSERVATION(j).getOBX();
				try {
					log.debug("Parsing observation");
					Obs obs = parseObs(encounter, obx, obr, messageControlId);
					if (obs != null) {
						
						// if we're backfilling an encounter, don't use
						// the creator/dateCreated from the encounter
						if (encounter.getEncounterId() != null) {
							obs.setCreator(getEnterer(orc));
							obs.setDateCreated(new Date());
						}
						
						// set the obsGroup on this obs
						if (obsGrouper != null) {
							// set the obs to the group.  This assumes the group is already
							// on the encounter and that when the encounter is saved it will
							// propagate to the children obs
							obsGrouper.addGroupMember(obs);
						} else {
							// set this obs on the encounter object that we
							// will be saving later
							log.debug("Obs is not null. Adding to encounter object");
							encounter.addObs(obs);
							log.debug("Done with this obs");
						}
					}
				}
				catch (ProposingConceptException proposingException) {
					Concept questionConcept = proposingException.getConcept();
					String value = proposingException.getValueName();
					//if the sender never specified any text for the proposed concept
					if (!StringUtils.isEmpty(value)) {
						conceptProposals.add(createConceptProposal(encounter, questionConcept, value));
					} else {
						errorInHL7Queue = new HL7Exception(Context.getMessageSourceService().getMessage(
						    "Hl7.proposed.concept.name.empty"), proposingException);
						break;//stop any further processing of current message
					}
					
				}
				catch (HL7Exception e) {
					errorInHL7Queue = e;
				}
				finally {
					// Handle obs-level exceptions
					if (errorInHL7Queue != null) {
						throw new HL7Exception(Context.getMessageSourceService().getMessage(
						    "ORUR01.error.improperlyFormattedOBX",
						    new Object[] { PipeParser.encode(obx, new EncodingCharacters('|', "^~\\&")) }, null),
						        HL7Exception.DATA_TYPE_ERROR, errorInHL7Queue);
					}
				}
			}
			
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Finished creating observations");
			log.debug("Current thread: " + Thread.currentThread());
			log.debug("Creating the encounter object");
		}
		Context.getEncounterService().saveEncounter(encounter);
		
		// Notify HL7 service that we have created a new encounter, allowing
		// features/modules to trigger on HL7-generated encounters.
		// -This can be removed once we have a obs_group table and all
		// obs can be created in memory as part of the encounter *before* we
		// call EncounterService.createEncounter().  For now, making obs groups
		// requires that one obs be created (in the database) before others can
		// be linked to it, forcing us to save the encounter prematurely."
		//
		// NOTE: The above referenced fix is now done.  This method is
		// deprecated and will be removed in the next release.  All modules
		// should modify their AOP methods to hook around
		// EncounterService.createEncounter(Encounter).
		hl7Service.encounterCreated(encounter);
		
		// loop over the proposed concepts and save each to the database
		// now that the encounter is saved
		for (ConceptProposal proposal : conceptProposals) {
			Context.getConceptService().saveConceptProposal(proposal);
		}
		
		return oru;
		
	}
	
	/**
	 * process an NK1 segment and add relationships if needed
	 *
	 * @param patient
	 * @param nk1
	 * @throws HL7Exception
	 * @should create a relationship from a NK1 segment
	 * @should not create a relationship if one exists
	 * @should create a person if the relative is not found
	 * @should fail if the coding system is not 99REL
	 * @should fail if the relationship identifier is formatted improperly
	 * @should fail if the relationship type is not found
	 */
	protected void processNK1(Patient patient, NK1 nk1) throws HL7Exception {
		// guarantee we are working with our custom coding system
		String relCodingSystem = nk1.getRelationship().getNameOfCodingSystem().getValue();
		if (!relCodingSystem.equals(HL7Constants.HL7_LOCAL_RELATIONSHIP)) {
			throw new HL7Exception(Context.getMessageSourceService().getMessage("ORUR01.error.relationshipCoding",
			    new Object[] { relCodingSystem }, null));
		}
		
		// get the relationship type identifier
		String relIdentifier = nk1.getRelationship().getIdentifier().getValue();
		
		// validate the format of the relationship identifier
		if (!Pattern.matches("[0-9]+[AB]", relIdentifier)) {
			throw new HL7Exception(Context.getMessageSourceService().getMessage("ORUR01.error.relationshipType",
			    new Object[] { relIdentifier }, null));
		}
		
		// get the type ID
		Integer relTypeId = 0;
		try {
			relTypeId = Integer.parseInt(relIdentifier.substring(0, relIdentifier.length() - 1));
		}
		catch (NumberFormatException e) {
			throw new HL7Exception(Context.getMessageSourceService().getMessage("ORUR01.error.relationshipType",
			    new Object[] { relIdentifier }, null));
		}
		
		// find the relationship type
		RelationshipType relType = Context.getPersonService().getRelationshipType(relTypeId);
		if (relType == null) {
			throw new HL7Exception(Context.getMessageSourceService().getMessage("ORUR01.error.relationshipTypeNotFound",
			    new Object[] { relTypeId }, null));
		}
		
		// find the relative
		Person relative = getRelative(nk1);
		
		// determine if the patient is person A or B; the relIdentifier indicates
		// the relative's side of the relationship, so the patient is the inverse
		boolean patientIsPersonA = relIdentifier.endsWith("B");
		boolean patientCanBeEitherPerson = relType.getbIsToA().equals(relType.getaIsToB());
		
		// look at existing relationships to determine if a new one is needed
		Set<Relationship> rels = new HashSet<Relationship>();
		if (relative != null) {
			if (patientCanBeEitherPerson || patientIsPersonA) {
				rels.addAll(Context.getPersonService().getRelationships(patient, relative, relType));
			}
			if (patientCanBeEitherPerson || !patientIsPersonA) {
				rels.addAll(Context.getPersonService().getRelationships(relative, patient, relType));
			}
		}
		
		// create a relationship if none is found
		if (rels.isEmpty()) {
			
			// check the relative's existence
			if (relative == null) {
				// create one based on NK1 information
				relative = Context.getHL7Service().createPersonFromNK1(nk1);
				if (relative == null) {
					throw new HL7Exception(Context.getMessageSourceService().getMessage("ORUR01.error.relativeNotCreated"));
				}
			}
			
			// create the relationship
			Relationship relation = new Relationship();
			if (patientCanBeEitherPerson || patientIsPersonA) {
				relation.setPersonA(patient);
				relation.setPersonB(relative);
			} else {
				relation.setPersonA(relative);
				relation.setPersonB(patient);
			}
			relation.setRelationshipType(relType);
			Context.getPersonService().saveRelationship(relation);
		}
	}
	
	/**
	 * Not used
	 *
	 * @param message
	 * @throws HL7Exception
	 */
	private void validate(Message message) throws HL7Exception {
		// TODO: check version, etc.
	}
	
	private MSH getMSH(ORU_R01 oru) {
		return oru.getMSH();
	}
	
	private PID getPID(ORU_R01 oru) {
		return oru.getPATIENT_RESULT().getPATIENT().getPID();
	}
	
	/**
	 * finds NK1 segments in an ORU_R01 message. all HAPI-rendered Messages have at least one NK1
	 * segment but if the original message truly does not contain an NK1, the setID will be null on
	 * the generated NK1
	 *
	 * @param oru ORU_R01 message to be parsed for NK1 segments
	 * @return list of not-null NK1 segments
	 * @throws HL7Exception
	 */
	public List<NK1> getNK1List(ORU_R01 oru) throws HL7Exception {
		List<NK1> res = new ArrayList<NK1>();
		// there will always be at least one NK1, even if the original message does not contain one
		for (int i = 0; i < oru.getPATIENT_RESULT().getPATIENT().getNK1Reps(); i++) {
			// if the setIDNK1 value is null, this NK1 is blank
			if (oru.getPATIENT_RESULT().getPATIENT().getNK1(i).getSetIDNK1().getValue() != null) {
				res.add(oru.getPATIENT_RESULT().getPATIENT().getNK1(i));
			}
		}
		return res;
	}
	
	private PV1 getPV1(ORU_R01 oru) {
		return oru.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1();
	}
	
	private ORC getORC(ORU_R01 oru) {
		return oru.getPATIENT_RESULT().getORDER_OBSERVATION().getORC();
	}
	
	/**
	 * This method does not call the database to create the encounter row. The encounter is only
	 * created after all obs have been attached to it Creates an encounter pojo to be attached
	 * later. This method does not create an encounterId
	 *
	 * @param msh
	 * @param patient
	 * @param pv1
	 * @param orc
	 * @return
	 * @throws HL7Exception
	 */
	private Encounter createEncounter(MSH msh, Patient patient, PV1 pv1, ORC orc) throws HL7Exception {
		
		// the encounter we will return
		Encounter encounter = null;
		
		// look for the encounter id in PV1-19
		CX visitNumber = pv1.getVisitNumber();
		Integer encounterId = null;
		try {
			encounterId = Integer.valueOf(visitNumber.getIDNumber().getValue());
		}
		catch (NumberFormatException e) {
			// pass
		}
		
		// if an encounterId was passed in, assume that these obs are
		// going to be appended to it.  Fetch the old encounter from
		// the database
		if (encounterId != null) {
			encounter = Context.getEncounterService().getEncounter(encounterId);
		} else {
			// if no encounter_id was passed in, this is a new
			// encounter, create the object
			encounter = new Encounter();
			
			Date encounterDate = getEncounterDate(pv1);
			Provider provider = getProvider(pv1);
			Location location = getLocation(pv1);
			Form form = getForm(msh);
			EncounterType encounterType = getEncounterType(msh, form);
			User enterer = getEnterer(orc);
			//			Date dateEntered = getDateEntered(orc); // ignore this since we have no place in the data model to store it
			
			encounter.setEncounterDatetime(encounterDate);
			if (unknownRole == null) {
				unknownRole = Context.getEncounterService()
				        .getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID);
			}
			encounter.setProvider(unknownRole, provider);
			encounter.setPatient(patient);
			encounter.setLocation(location);
			encounter.setForm(form);
			encounter.setEncounterType(encounterType);
			encounter.setCreator(enterer);
			encounter.setDateCreated(new Date());
		}
		
		return encounter;
	}
	
	/**
	 * Creates the Obs pojo from the OBX message
	 *
	 * @param encounter The Encounter object this Obs is a member of
	 * @param obx The hl7 obx message
	 * @param obr The parent hl7 or message
	 * @param uid unique string for this message for any error reporting purposes
	 * @return Obs pojo with all values filled in
	 * @throws HL7Exception if there is a parsing exception
	 * @throws ProposingConceptException if the answer to this obs is a proposed concept
	 * @should add comments to an observation from NTE segments
	 * @should add multiple comments for an observation as one comment
	 * @should add comments to an observation group
	 */
	private Obs parseObs(Encounter encounter, OBX obx, OBR obr, String uid) throws HL7Exception, ProposingConceptException {
		if (log.isDebugEnabled()) {
			log.debug("parsing observation: " + obx);
		}
		Varies[] values = obx.getObservationValue();
		
		// bail out if no values were found
		if (values == null || values.length < 1) {
			return null;
		}
		
		String hl7Datatype = values[0].getName();
		if (log.isDebugEnabled()) {
			log.debug("  datatype = " + hl7Datatype);
		}
		Concept concept = getConcept(obx.getObservationIdentifier(), uid);
		if (log.isDebugEnabled()) {
			log.debug("  concept = " + concept.getConceptId());
		}
		ConceptName conceptName = getConceptName(obx.getObservationIdentifier());
		if (log.isDebugEnabled()) {
			log.debug("  concept-name = " + conceptName);
		}
		
		Date datetime = getDatetime(obx);
		if (log.isDebugEnabled()) {
			log.debug("  timestamp = " + datetime);
		}
		if (datetime == null) {
			datetime = encounter.getEncounterDatetime();
		}
		
		Obs obs = new Obs();
		obs.setPerson(encounter.getPatient());
		obs.setConcept(concept);
		obs.setEncounter(encounter);
		obs.setObsDatetime(datetime);
		obs.setLocation(encounter.getLocation());
		obs.setCreator(encounter.getCreator());
		obs.setDateCreated(encounter.getDateCreated());
		
		// set comments if there are any
		StringBuilder comments = new StringBuilder();
		ORU_R01_OBSERVATION parent = (ORU_R01_OBSERVATION) obx.getParent();
		// iterate over all OBX NTEs
		for (int i = 0; i < parent.getNTEReps(); i++) {
			for (FT obxComment : parent.getNTE(i).getComment()) {
				if (comments.length() > 0) {
					comments.append(" ");
				}
				comments = comments.append(obxComment.getValue());
			}
		}
		// only set comments if there are any
		if (StringUtils.hasText(comments.toString())) {
			obs.setComment(comments.toString());
		}
		
		Type obx5 = values[0].getData();
		if ("NM".equals(hl7Datatype)) {
			String value = ((NM) obx5).getValue();
			if (value == null || value.length() == 0) {
				log.warn("Not creating null valued obs for concept " + concept);
				return null;
			} else if ("0".equals(value) || "1".equals(value)) {
				concept = concept.hydrate(concept.getConceptId().toString());
				obs.setConcept(concept);
				if (concept.getDatatype().isBoolean()) {
					obs.setValueBoolean(value.equals("1"));
				} else if (concept.getDatatype().isNumeric()) {
					try {
						obs.setValueNumeric(Double.valueOf(value));
					}
					catch (NumberFormatException e) {
						throw new HL7Exception(Context.getMessageSourceService().getMessage(
						    "ORUR01.error.notnumericConcept",
						    new Object[] { value, concept.getConceptId(), conceptName.getName(), uid }, null), e);
					}
				} else if (concept.getDatatype().isCoded()) {
					Concept answer = "1".equals(value) ? Context.getConceptService().getTrueConcept() : Context
					        .getConceptService().getFalseConcept();
					boolean isValidAnswer = false;
					Collection<ConceptAnswer> conceptAnswers = concept.getAnswers();
					if (conceptAnswers != null && conceptAnswers.size() > 0) {
						for (ConceptAnswer conceptAnswer : conceptAnswers) {
							if (conceptAnswer.getAnswerConcept().getId().equals(answer.getId())) {
								obs.setValueCoded(answer);
								isValidAnswer = true;
								break;
							}
						}
					}
					//answer the boolean answer concept was't found
					if (!isValidAnswer) {
						throw new HL7Exception(Context.getMessageSourceService().getMessage("ORUR01.error.invalidAnswer",
						    new Object[] { answer.toString(), uid }, null));
					}
				} else {
					//throw this exception to make sure that the handler doesn't silently ignore bad hl7 message
					throw new HL7Exception(Context.getMessageSourceService().getMessage("ORUR01.error.CannotSetBoolean",
					    new Object[] { obs.getConcept().getConceptId() }, null));
				}
			} else {
				try {
					obs.setValueNumeric(Double.valueOf(value));
				}
				catch (NumberFormatException e) {
					throw new HL7Exception(Context.getMessageSourceService().getMessage("ORUR01.error.notnumericConcept",
					    new Object[] { value, concept.getConceptId(), conceptName.getName(), uid }, null), e);
				}
			}
		} else if ("CWE".equals(hl7Datatype)) {
			log.debug("  CWE observation");
			CWE value = (CWE) obx5;
			String valueIdentifier = value.getIdentifier().getValue();
			log.debug("    value id = " + valueIdentifier);
			String valueName = value.getText().getValue();
			log.debug("    value name = " + valueName);
			if (isConceptProposal(valueIdentifier)) {
				if (log.isDebugEnabled()) {
					log.debug("Proposing concept");
				}
				throw new ProposingConceptException(concept, valueName);
			} else {
				log.debug("    not proposal");
				try {
					Concept valueConcept = getConcept(value, uid);
					obs.setValueCoded(valueConcept);
					if (HL7Constants.HL7_LOCAL_DRUG.equals(value.getNameOfAlternateCodingSystem().getValue())) {
						Drug valueDrug = new Drug();
						valueDrug.setDrugId(Integer.valueOf(value.getAlternateIdentifier().getValue()));
						obs.setValueDrug(valueDrug);
					} else {
						ConceptName valueConceptName = getConceptName(value);
						if (valueConceptName != null) {
							if (log.isDebugEnabled()) {
								log.debug("    value concept-name-id = " + valueConceptName.getConceptNameId());
								log.debug("    value concept-name = " + valueConceptName.getName());
							}
							obs.setValueCodedName(valueConceptName);
						}
					}
				}
				catch (NumberFormatException e) {
					throw new HL7Exception(Context.getMessageSourceService().getMessage("ORUR01.error.InvalidConceptId",
					    new Object[] { valueIdentifier, valueName }, null));
				}
			}
			if (log.isDebugEnabled()) {
				log.debug("  Done with CWE");
			}
		} else if ("CE".equals(hl7Datatype)) {
			CE value = (CE) obx5;
			String valueIdentifier = value.getIdentifier().getValue();
			String valueName = value.getText().getValue();
			if (isConceptProposal(valueIdentifier)) {
				throw new ProposingConceptException(concept, valueName);
			} else {
				try {
					obs.setValueCoded(getConcept(value, uid));
					obs.setValueCodedName(getConceptName(value));
				}
				catch (NumberFormatException e) {
					throw new HL7Exception(Context.getMessageSourceService().getMessage("ORUR01.error.InvalidConceptId",
					    new Object[] { valueIdentifier, valueName }, null));
				}
			}
		} else if ("DT".equals(hl7Datatype)) {
			DT value = (DT) obx5;
			if (value != null) {
				Date valueDate = getDate(value.getYear(), value.getMonth(), value.getDay(), 0, 0, 0);
				obs.setValueDatetime(valueDate);
			} else {
				log.warn("Not creating null valued obs for concept " + concept);
				return null;
			}
		} else if ("TS".equals(hl7Datatype)) {
			DTM value = ((TS) obx5).getTime();
			if (value != null) {
				Date valueDate = getDate(value.getYear(), value.getMonth(), value.getDay(), value.getHour(), value
				        .getMinute(), value.getSecond());
				
				obs.setValueDatetime(valueDate);
			} else {
				log.warn("Not creating null valued obs for concept " + concept);
				return null;
			}
		} else if ("TM".equals(hl7Datatype)) {
			TM value = (TM) obx5;
			if (value != null) {
				Date valueTime = getDate(0, 0, 0, value.getHour(), value.getMinute(), value.getSecond());
				obs.setValueDatetime(valueTime);
			} else {
				log.warn("Not creating null valued obs for concept " + concept);
				return null;
			}
		} else if ("ST".equals(hl7Datatype)) {
			ST value = (ST) obx5;
			if (value == null || value.getValue() == null || value.getValue().trim().length() == 0) {
				log.warn("Not creating null valued obs for concept " + concept);
				return null;
			}
			obs.setValueText(value.getValue());
		} else if ("ED".equals(hl7Datatype)) {
			ED value = (ED) obx5;
			if (value == null || value.getData() == null || !StringUtils.hasText(value.getData().getValue())) {
				log.warn("Not creating null valued obs for concept " + concept);
				return null;
			}
			//we need to hydrate the concept so that the EncounterSaveHandler
			//doesn't fail since it needs to check if it is a concept numeric
			Concept c = Context.getConceptService().getConcept(obs.getConcept().getConceptId());
			obs.setConcept(c);
			String title = null;
			if (obs.getValueCodedName() != null) {
				title = obs.getValueCodedName().getName();
			}
			if (!StringUtils.hasText(title)) {
				title = c.getName().getName();
			}
			obs.setComplexData(new ComplexData(title, value.getData().getValue()));
		} else {
			// unsupported data type
			// TODO: support RP (report), SN (structured numeric)
			// do we need to support BIT just in case it slips thru?
			throw new HL7Exception(Context.getMessageSourceService().getMessage("ORUR01.error.UpsupportedObsType",
			    new Object[] { hl7Datatype }, null));
		}
		
		return obs;
	}
	
	/**
	 * Derive a concept name from the CWE component of an hl7 message.
	 *
	 * @param cwe
	 * @return
	 * @throws HL7Exception
	 */
	private ConceptName getConceptName(CWE cwe) throws HL7Exception {
		ST altIdentifier = cwe.getAlternateIdentifier();
		ID altCodingSystem = cwe.getNameOfAlternateCodingSystem();
		return getConceptName(altIdentifier, altCodingSystem);
	}
	
	/**
	 * Derive a concept name from the CE component of an hl7 message.
	 *
	 * @param ce
	 * @return
	 * @throws HL7Exception
	 */
	private ConceptName getConceptName(CE ce) throws HL7Exception {
		ST altIdentifier = ce.getAlternateIdentifier();
		ID altCodingSystem = ce.getNameOfAlternateCodingSystem();
		return getConceptName(altIdentifier, altCodingSystem);
	}
	
	/**
	 * Derive a concept name from the CWE component of an hl7 message.
	 *
	 * @param altIdentifier
	 * @param altCodingSystem
	 * @return
	 */
	private ConceptName getConceptName(ST altIdentifier, ID altCodingSystem) throws HL7Exception {
		if (altIdentifier != null && HL7Constants.HL7_LOCAL_CONCEPT_NAME.equals(altCodingSystem.getValue())) {
			String hl7ConceptNameId = altIdentifier.getValue();
			return getConceptName(hl7ConceptNameId);
		}
		
		return null;
	}
	
	/**
	 * Utility method to retrieve the openmrs ConceptName specified in an hl7 message observation
	 * segment. This method assumes that the check for 99NAM has been done already and is being
	 * given an openmrs conceptNameId
	 *
	 * @param hl7ConceptNameId internal ConceptNameId to look up
	 * @return ConceptName from the database
	 * @throws HL7Exception
	 */
	private ConceptName getConceptName(String hl7ConceptNameId) throws HL7Exception {
		ConceptName specifiedConceptName = null;
		if (hl7ConceptNameId != null) {
			// get the exact concept name specified by the id
			try {
				Integer conceptNameId = Integer.valueOf(hl7ConceptNameId);
				specifiedConceptName = new ConceptName();
				specifiedConceptName.setConceptNameId(conceptNameId);
			}
			catch (NumberFormatException e) {
				// if it is not a valid number, more than likely it is a bad hl7 message
				log.debug("Invalid concept name ID '" + hl7ConceptNameId + "'", e);
			}
		}
		return specifiedConceptName;
		
	}
	
	private boolean isConceptProposal(String identifier) {
		return OpenmrsUtil.nullSafeEquals(identifier, OpenmrsConstants.PROPOSED_CONCEPT_IDENTIFIER);
	}
	
	private Date getDate(int year, int month, int day, int hour, int minute, int second) {
		Calendar cal = Calendar.getInstance();
		// Calendar.set(MONTH, int) is zero-based, Hl7 is not
		cal.set(year, month - 1, day, hour, minute, second);
		return cal.getTime();
	}
	
	/**
	 * Get an openmrs Concept object out of the given hl7 coded element
	 *
	 * @param codedElement ce to pull from
	 * @param uid unique string for this message for any error reporting purposes
	 * @return new Concept object
	 * @throws HL7Exception if parsing errors occur
	 */
	private Concept getConcept(CE codedElement, String uid) throws HL7Exception {
		String hl7ConceptId = codedElement.getIdentifier().getValue();
		
		String codingSystem = codedElement.getNameOfCodingSystem().getValue();
		return getConcept(hl7ConceptId, codingSystem, uid);
	}
	
	/**
	 * Get an openmrs Concept object out of the given hl7 coded with exceptions element
	 *
	 * @param codedElement cwe to pull from
	 * @param uid unique string for this message for any error reporting purposes
	 * @return new Concept object
	 * @throws HL7Exception if parsing errors occur
	 */
	private Concept getConcept(CWE codedElement, String uid) throws HL7Exception {
		String hl7ConceptId = codedElement.getIdentifier().getValue();
		
		String codingSystem = codedElement.getNameOfCodingSystem().getValue();
		return getConcept(hl7ConceptId, codingSystem, uid);
	}
	
	/**
	 * Get a concept object representing this conceptId and coding system.<br/>
	 * If codingSystem is 99DCT, then a new Concept with the given conceptId is returned.<br/>
	 * Otherwise, the coding system is looked up in the ConceptMap for an openmrs concept mapped to
	 * that code.
	 *
	 * @param hl7ConceptId the given hl7 conceptId
	 * @param codingSystem the coding system for this conceptid (e.g. 99DCT)
	 * @param uid unique string for this message for any error reporting purposes
	 * @return a Concept object or null if no conceptId with given coding system found
	 * @should return null if codingSystem not found
	 * @should return a Concept if given local coding system
	 * @should return a mapped Concept if given a valid mapping
	 */
	protected Concept getConcept(String hl7ConceptId, String codingSystem, String uid) throws HL7Exception {
		if (codingSystem == null || HL7Constants.HL7_LOCAL_CONCEPT.equals(codingSystem)) {
			// the concept is local
			try {
				Integer conceptId = Integer.valueOf(hl7ConceptId);
				return Context.getConceptService().getConcept(conceptId);
			}
			catch (NumberFormatException e) {
				throw new HL7Exception(Context.getMessageSourceService().getMessage("ORUR01.error.hl7ConceptId",
				    new Object[] { hl7ConceptId, uid }, null));
			}
		} else {
			// the concept is not local, look it up in our mapping
			return Context.getConceptService().getConceptByMapping(hl7ConceptId, codingSystem);
		}
	}
	
	/**
	 * Pull the timestamp for this obx out. if an invalid date is found, null is returned
	 *
	 * @param obx the obs to parse and get the timestamp from
	 * @return an obx timestamp or null
	 * @throws HL7Exception
	 * @see {@link #getDatetime(TS)}
	 */
	private Date getDatetime(OBX obx) throws HL7Exception {
		TS ts = obx.getDateTimeOfTheObservation();
		return getDatetime(ts);
	}
	
	/**
	 * Pull the timestamp for this obr out. if an invalid date is found, null is returned
	 *
	 * @param obr
	 * @return
	 * @throws HL7Exception
	 */
	private Date getDatetime(OBR obr) throws HL7Exception {
		TS ts = obr.getObservationDateTime();
		return getDatetime(ts);
		
	}
	
	/**
	 * Return a java date object for the given TS
	 *
	 * @param ts TS to parse
	 * @return date object or null
	 * @throws HL7Exception
	 */
	private Date getDatetime(TS ts) throws HL7Exception {
		Date datetime = null;
		DTM value = ts.getTime();
		
		if (value.getYear() == 0 || value.getValue() == null) {
			return null;
		}
		
		try {
			datetime = getDate(value.getYear(), value.getMonth(), value.getDay(), value.getHour(), value.getMinute(), value
			        .getSecond());
		}
		catch (DataTypeException e) {

		}
		return datetime;
		
	}
	
	private Date getEncounterDate(PV1 pv1) throws HL7Exception {
		return tsToDate(pv1.getAdmitDateTime());
	}
	
	private Provider getProvider(PV1 pv1) throws HL7Exception {
		XCN hl7Provider = pv1.getAttendingDoctor(0);
		Provider provider = null;
		String id = hl7Provider.getIDNumber().getValue();
		String assignAuth = hl7Provider.getAssigningAuthority().getUniversalID().getValue();
		String type = hl7Provider.getAssigningAuthority().getUniversalIDType().getValue();
		String errorMessage = "";
		if (StringUtils.hasText(id)) {
			String specificErrorMsg = "";
			if (OpenmrsUtil.nullSafeEquals("L", type)) {
				if (HL7Constants.PROVIDER_ASSIGNING_AUTH_PROV_ID.equalsIgnoreCase(assignAuth)) {
					try {
						provider = Context.getProviderService().getProvider(Integer.valueOf(id));
					}
					catch (NumberFormatException e) {
						// ignore
					}
					specificErrorMsg = "with provider Id";
				} else if (HL7Constants.PROVIDER_ASSIGNING_AUTH_IDENTIFIER.equalsIgnoreCase(assignAuth)) {
					provider = Context.getProviderService().getProviderByIdentifier(id);
					specificErrorMsg = "with provider identifier";
				} else if (HL7Constants.PROVIDER_ASSIGNING_AUTH_PROV_UUID.equalsIgnoreCase(assignAuth)) {
					provider = Context.getProviderService().getProviderByUuid(id);
					specificErrorMsg = "with provider uuid";
				}
			} else {
				try {
					Person person = Context.getPersonService().getPerson(Integer.valueOf(id));
					Collection<Provider> providers = Context.getProviderService().getProvidersByPerson(person);
					if (!providers.isEmpty()) {
						provider = providers.iterator().next();
					}
				}
				catch (NumberFormatException e) {
					// ignore
				}
				specificErrorMsg = "associated to a person with person id";
			}
			
			errorMessage = "Could not resolve provider " + specificErrorMsg + ":" + id;
		} else {
			errorMessage = "No unique identifier was found for the provider";
		}
		
		if (provider == null) {
			throw new HL7Exception(errorMessage);
		}
		
		return provider;
	}
	
	private Patient getPatient(PID pid) throws HL7Exception {
		Integer patientId = Context.getHL7Service().resolvePatientId(pid);
		if (patientId == null) {
			throw new HL7Exception(Context.getMessageSourceService().getMessage("ORUR01.error.UnresolvedPatient"));
		}
		
		return Context.getPatientService().getPatient(patientId);
	}
	
	/**
	 * gets a relative based on an NK1 segment
	 *
	 * @param nk1 an NK1 segment from the HL7 request
	 * @return a matching Person or null if not found
	 * @throws HL7Exception
	 */
	private Person getRelative(NK1 nk1) throws HL7Exception {
		// if there are no associated party identifiers, the person will not exist
		if (nk1.getNextOfKinAssociatedPartySIdentifiers().length < 1) {
			return null;
		}
		// find the related person via given IDs
		return Context.getHL7Service().resolvePersonFromIdentifiers(nk1.getNextOfKinAssociatedPartySIdentifiers());
	}
	
	private Location getLocation(PV1 pv1) throws HL7Exception {
		PL hl7Location = pv1.getAssignedPatientLocation();
		Integer locationId = Context.getHL7Service().resolveLocationId(hl7Location);
		if (locationId == null) {
			throw new HL7Exception(Context.getMessageSourceService().getMessage("ORUR01.error.UnresolvedLocation"));
		}
		
		return Context.getLocationService().getLocation(locationId);
	}
	
	/**
	 * needs to find a Form based on information in MSH-21. example: 16^AMRS.ELD.FORMID
	 *
	 * @param msh
	 * @return
	 * @throws HL7Exception
	 */
	private Form getForm(MSH msh) throws HL7Exception {
		String uuid = null;
		String id = null;
		
		for (EI identifier : msh.getMessageProfileIdentifier()) {
			if (identifier != null && identifier.getNamespaceID() != null) {
				String identifierType = identifier.getNamespaceID().getValue();
				if (OpenmrsUtil.nullSafeEquals(identifierType, HL7Constants.HL7_FORM_UUID)) {
					uuid = identifier.getEntityIdentifier().getValue();
				} else if (OpenmrsUtil.nullSafeEquals(identifierType, HL7Constants.HL7_FORM_ID)) {
					id = identifier.getEntityIdentifier().getValue();
				} else {
					log.warn("Form identifier type of " + identifierType + " unknown to ORU R01 processor.");
				}
			}
		}
		
		Form form = null;
		
		// prefer uuid over id
		if (uuid != null) {
			form = Context.getFormService().getFormByUuid(uuid);
		}
		
		// if uuid did not work ...
		if (form == null) {
			try {
				Integer formId = Integer.parseInt(id);
				form = Context.getFormService().getForm(formId);
			}
			catch (NumberFormatException e) {
				throw new HL7Exception(Context.getMessageSourceService().getMessage("ORUR01.error.parseFormId"), e);
			}
		}
		
		return form;
	}
	
	private EncounterType getEncounterType(MSH msh, Form form) {
		if (form != null) {
			return form.getEncounterType();
		}
		// TODO: resolve encounter type from MSH data - do we need PV1 too?
		return null;
	}
	
	private User getEnterer(ORC orc) throws HL7Exception {
		XCN hl7Enterer = orc.getEnteredBy(0);
		Integer entererId = Context.getHL7Service().resolveUserId(hl7Enterer);
		if (entererId == null) {
			throw new HL7Exception(Context.getMessageSourceService().getMessage("ORUR01.error.UnresolvedEnterer"));
		}
		User enterer = new User();
		enterer.setUserId(entererId);
		return enterer;
	}
	
	//TODO: Debug (and use) methods in HL7Util instead
	private Date tsToDate(TS ts) throws HL7Exception {
		// need to handle timezone
		String dtm = ts.getTime().getValue();
		int year = Integer.parseInt(dtm.substring(0, 4));
		int month = (dtm.length() >= 6 ? Integer.parseInt(dtm.substring(4, 6)) - 1 : 0);
		int day = (dtm.length() >= 8 ? Integer.parseInt(dtm.substring(6, 8)) : 1);
		int hour = (dtm.length() >= 10 ? Integer.parseInt(dtm.substring(8, 10)) : 0);
		int min = (dtm.length() >= 12 ? Integer.parseInt(dtm.substring(10, 12)) : 0);
		int sec = (dtm.length() >= 14 ? Integer.parseInt(dtm.substring(12, 14)) : 0);
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
	 * Creates a ConceptProposal object that will need to be saved to the database at a later point.
	 *
	 * @param encounter
	 * @param concept
	 * @param originalText
	 * @return
	 */
	private ConceptProposal createConceptProposal(Encounter encounter, Concept concept, String originalText) {
		// value is a proposed concept, create a ConceptProposal
		// instead of an Obs for this observation
		// TODO: at this point if componentSeparator (^) is in text,
		// we'll only use the text before that delimiter!
		ConceptProposal conceptProposal = new ConceptProposal();
		conceptProposal.setOriginalText(originalText);
		conceptProposal.setState(OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED);
		conceptProposal.setEncounter(encounter);
		conceptProposal.setObsConcept(concept);
		return conceptProposal;
	}
	
	private void updateHealthCenter(Patient patient, PV1 pv1) {
		// Update patient's location if it has changed
		if (log.isDebugEnabled()) {
			log.debug("Checking for discharge to location");
		}
		DLD dld = pv1.getDischargedToLocation();
		log.debug("DLD = " + dld);
		if (dld == null) {
			return;
		}
		IS hl7DischargeToLocation = dld.getDischargeLocation();
		log.debug("is = " + hl7DischargeToLocation);
		if (hl7DischargeToLocation == null) {
			return;
		}
		String dischargeToLocation = hl7DischargeToLocation.getValue();
		log.debug("dischargeToLocation = " + dischargeToLocation);
		if (dischargeToLocation != null && dischargeToLocation.length() > 0) {
			if (log.isDebugEnabled()) {
				log.debug("Patient discharged to " + dischargeToLocation);
			}
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
			patient = Context.getPatientService().getPatient(patient.getPatientId());
			
			PersonAttributeType healthCenterAttrType = Context.getPersonService().getPersonAttributeTypeByName(
			    "Health Center");
			
			if (healthCenterAttrType == null) {
				log.error("A person attribute type with name 'Health Center' is not defined but patient "
				        + patient.getPatientId() + " is trying to change their health center to " + newLocationId);
				return;
			}
			
			PersonAttribute currentHealthCenter = patient.getAttribute("Health Center");
			
			if (currentHealthCenter == null || !newLocationId.toString().equals(currentHealthCenter.getValue())) {
				PersonAttribute newHealthCenter = new PersonAttribute(healthCenterAttrType, newLocationId.toString());
				
				log.debug("Updating patient's location from " + currentHealthCenter + " to " + newLocationId);
				
				// add attribute (and void old if there is one)
				patient.addAttribute(newHealthCenter);
				
				// save the patient and their new attribute
				Context.getPatientService().savePatient(patient);
			}
			
		}
		log.debug("finished discharge to location method");
	}
}
