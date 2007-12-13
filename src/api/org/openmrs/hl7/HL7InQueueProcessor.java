package org.openmrs.hl7;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.springframework.transaction.annotation.Transactional;

import ca.uhn.hl7v2.HL7Exception;

/**
 * Processes message in the HL7 inbound queue. Messages are moved into either
 * the archive or error table depending on success or failure of the processing.
 * 
 * You may, however, set a global property that causes the processor to ignore
 * messages regarding unknown patients from a non-local HL7 source. (i.e. those
 * messages neither go to the archive or the error table.) 
 * 
 * @version 1.0
 */
@Transactional
public class HL7InQueueProcessor /* implements Runnable */{

	private final Log log = LogFactory.getLog(this.getClass());

	private HL7Receiver receiver = new HL7Receiver();
	private static Boolean isRunning = false; // allow only one running

	// processor per JVM

	/**
	 * Empty constructor (requires context to be set using
	 * <code>setContext(Context)</code> method before any other calls are
	 * made)
	 */
	public HL7InQueueProcessor() {
	}

	/**
	 * Process a single queue entry from the inbound HL7 queue
	 * 
	 * @param hl7InQueue
	 *            queue entry to be processed
	 */
	public void processHL7InQueue(HL7InQueue hl7InQueue) {

		if (log.isDebugEnabled())
			log.debug("Processing HL7 inbound queue (id="
					+ hl7InQueue.getHL7InQueueId() + ",key="
					+ hl7InQueue.getHL7SourceKey() + ")");

		// Parse the HL7 into an HL7Message or abort with failure
		String hl7Message = hl7InQueue.getHL7Data();
		try {
			// Send the inbound HL7 message to our receiver routine for
			// processing
			if (log.isDebugEnabled())
				log.debug("Sending HL7 message to HL7 receiver");
			receiver.processMessage(hl7Message);

			// Move HL7 inbound queue entry into the archive before exiting
			if (log.isDebugEnabled())
				log.debug("Archiving HL7 inbound queue entry");
			HL7InArchive hl7InArchive = new HL7InArchive(hl7InQueue);
			Context.getHL7Service().createHL7InArchive(hl7InArchive);
			if (log.isDebugEnabled())
				log.debug("Removing HL7 message from inbound queue");
			Context.getHL7Service().deleteHL7InQueue(hl7InQueue);
		} catch (HL7Exception e) {
			boolean skipError = false;
			log.error(e.getCause().getMessage());
			log.error(e.getCause().getMessage().equals("Could not resolve patient"));
			log.error(hl7InQueue.getHL7Source().getName());
			log.error(!hl7InQueue.getHL7Source().getName().equals("local"));
			log.error(Context.getAdministrationService().getGlobalProperty("hl7_processor.ignore_missing_patient_non_local", "false"));
			log.error(Context.getAdministrationService().getGlobalProperty("hl7_processor.ignore_missing_patient_non_local", "false").equals("true"));
			if ( e.getCause().getMessage().equals("Could not resolve patient")
					&& !hl7InQueue.getHL7Source().getName().equals("local")
					&& Context.getAdministrationService().getGlobalProperty("hl7_processor.ignore_missing_patient_non_local", "false").equals("true") ) {
				skipError = true;
			}
			if (!skipError)
				setFatalError(hl7InQueue, "Trouble parsing HL7 message ("
						+ hl7InQueue.getHL7SourceKey() + ")", e);
			return;
		} catch (Exception e) {
			setFatalError(hl7InQueue,
					"Exception while attempting to process HL7 In Queue ("
							+ hl7InQueue.getHL7SourceKey() + ")", e);
		}

		// clean up memory after processing each queue entry (otherwise, the
		// memory-intensive process may crash or eat up all our memory)
		try {
			Context.getHL7Service().garbageCollect();
		} catch (Exception e) {
			log
					.error(
							"Exception while performing garbagecollect in hl7 inbound processor",
							e);
		}

	}

	/**
	 * Transform the next pending HL7 inbound queue entry. If there are no
	 * pending items in the queue, this method simply returns quietly.
	 * 
	 * @return true if a queue entry was processed, false if queue was empty
	 */
	public boolean processNextHL7InQueue() {
		boolean entryProcessed = false;
		HL7Service hl7Service = Context.getHL7Service();
		HL7InQueue hl7InQueue;
		if ((hl7InQueue = hl7Service.getNextHL7InQueue()) != null) {
			processHL7InQueue(hl7InQueue);
			entryProcessed = true;
		}
		return entryProcessed;
	}

	/**
	 * Convenience method to respond to fatal errors by moving the queue entry
	 * into an error bin prior to aborting
	 */
	private void setFatalError(HL7InQueue hl7InQueue, String error,
			Throwable cause) {
		HL7InError hl7InError = new HL7InError(hl7InQueue);
		hl7InError.setError(error);
		hl7InError.setErrorDetails(cause == null ? "" : cause.getMessage());
		Context.getHL7Service().createHL7InError(hl7InError);
		Context.getHL7Service().deleteHL7InQueue(hl7InQueue);
		log.error(error, cause);
	}

	/*
	 * Run method for processing all entries in the HL7 inbound queue
	 * 
	 * public void run() { try { while (processNextHL7InQueue()) { // loop until
	 * queue is empty } } catch (Exception e) { log.error("Error while
	 * processing HL7 inbound queue", e); } }
	 */

	/**
	 * Starts up a thread to process all existing HL7InQueue entries
	 */
	public void processHL7InQueue() throws HL7Exception {
		synchronized (isRunning) {
			if (isRunning) {
				log
						.warn("HL7 processor aborting (another processor already running)");
				return;
			}
			isRunning = true;
		}
		try {
			log.debug("Start processing hl7 in queue");
			while (processNextHL7InQueue()) {
				// loop until queue is empty
			}
			log.debug("Done processing hl7 in queue");
		}
		finally {
			isRunning = false;
		}
	}

	/*
	 * private static Hashtable<Context, Thread> threadCache = new Hashtable<Context,
	 * Thread>();
	 * 
	 * private static Thread getThreadForContext(Context context) { Thread
	 * thread; if (threadCache.containsKey(context)) thread =
	 * threadCache.get(context); else { thread = new Thread(new
	 * HL7InQueueProcessor(context)); threadCache.put(context, thread); } return
	 * thread; }
	 */

	// /**
	// * Process a single queue entry from the inbound HL7 queue
	// *
	// * @param hl7InQueue
	// * queue entry to be processed
	// */
	// public void processHL7InQueue(HL7InQueue hl7InQueue) {
	//
	// HL7Message hl7Message;
	//
	// // Parse the HL7 into an HL7Message or abort with failure
	// String hl7Data = hl7InQueue.getHL7Data();
	// try {
	// hl7Message = new HL7Message(hl7Data);
	// } catch (HL7Exception e) {
	// setFatalError(hl7InQueue, "Error parsing HL7 message", e);
	// return;
	// }
	//
	// // Check the HL7 version
	// if (!hl7Message.getVersion().equals("2.5")) {
	// setFatalError(hl7InQueue, "Unsupported version (2.5 only)", null);
	// return;
	// }
	//
	// // Branch based on the type of message
	// String messageType = hl7Message.getMessageType();
	// if (messageType.equals("ORU"))
	// processORU(hl7InQueue, hl7Message);
	// else {
	// setFatalError(hl7InQueue, "Message type not supported: \""
	// + messageType + "\"", null);
	// return;
	// }
	//
	// // clean up memory after processing each queue entry
	// Context.getHL7Service().garbageCollect();
	// }
	//
	// /**
	// * Transform the next pending HL7 inbound queue entry. If there are no
	// * pending items in the queue, this method simply returns quietly.
	// *
	// * @return true if a queue entry was processed, false if queue was empty
	// */
	// public boolean processNextHL7InQueue() {
	// boolean entryProcessed = false;
	// HL7Service hl7Service = Context.getHL7Service();
	// HL7InQueue hl7InQueue;
	// if ((hl7InQueue = hl7Service.getNextHL7InQueue()) != null) {
	// processHL7InQueue(hl7InQueue);
	// entryProcessed = true;
	// }
	// return entryProcessed;
	// }
	//
	// /**
	// * Process ORU messages (this will eventually be moved into a separate
	// class
	// * as we expand to processing more message types ... or be replaced by
	// part
	// * of an HL7 library)
	// *
	// * @param hl7InQueue
	// * inbound queue entry to be processed
	// * @param hl7Message
	// * message generated from the queue entry
	// */
	// public void processORU(HL7InQueue hl7InQueue, HL7Message hl7Message) {
	//
	// // Extract the patient segment
	// HL7Segment pid = hl7Message.getNextSegment("PID");
	// if (pid == null) {
	// setFatalError(hl7InQueue, "Expected PID segment", null);
	// return;
	// }
	//
	// // Extract the visit segment
	// HL7Segment pv1 = hl7Message.getNextSegment("PV1");
	// if (pv1 == null) {
	// setFatalError(hl7InQueue, "Expected PV1 segment", null);
	// return;
	// }
	//
	// // Extract the common order segment (contains entry time and enterer)
	// HL7Segment orc = hl7Message.getNextSegment("ORC");
	// if (orc == null) {
	// setFatalError(hl7InQueue, "Expected ORC segment", null);
	// return;
	// }
	//
	// // Determine the enterer for obs data
	// User enterer = null;
	// try {
	// enterer = getEnterer(orc);
	// } catch (HL7Exception e) {
	// setFatalError(hl7InQueue, "Unable to determine the data enterer", e);
	// return;
	// }
	//
	// // Determine the patient from the PID (patient) segment
	// Patient patient = null;
	// try {
	// patient = getPatient(pid);
	// } catch (HL7Exception e) {
	// setFatalError(hl7InQueue, "Unable to determine the patient", e);
	// return;
	// }
	//
	// // Determine the provider from the PV1 (visit) segment
	// User provider = null;
	// try {
	// provider = getProvider(pv1);
	// } catch (HL7Exception e) {
	// setFatalError(hl7InQueue, "Unable to determine the provider", e);
	// return;
	// }
	//
	// // Determine the form used (OpenMRS-specific -- we record the form
	// // as a specific "profile" in MSH-21)
	// Form form = null;
	// try {
	// form = getForm(hl7Message);
	// } catch (Exception e) {
	// setFatalError(hl7InQueue, "Unable to determine OpenMRS form", e);
	// return;
	// }
	//
	// // Create an encounter for the observations
	// Encounter encounter = null;
	// try {
	// encounter = createEncounter(enterer, form, orc, pid, pv1);
	// } catch (HL7Exception e) {
	// setFatalError(hl7InQueue, "Unable to create encounter", e);
	// return;
	// }
	//
	// // Loop through the observations (grouped in OBRs, there should be
	// // an OBR segment followed by 0..n OBX with an observation per OBX)
	// Vector<Error> errors = new Vector<Error>();
	// HL7Segment obr;
	// while ((obr = hl7Message.getNextSegment("OBR")) != null) {
	// HL7Segment obx;
	// while (hl7Message.hasNextSegment("OBX")) {
	// obx = hl7Message.getNextSegment();
	// try {
	// createObservation(enterer, patient, encounter, provider,
	// obx);
	// } catch (HL7Exception e) {
	// addError(errors, obx.toString(), "OBX" + obx.getField(1)
	// + " parse error", e.getMessage());
	// }
	// }
	// }
	// if (errors != null && errors.size() > 0)
	// recordErrors(hl7InQueue, errors);
	//
	// // Move HL7 inbound queue entry into the archive before exiting
	// HL7InArchive hl7InArchive = new HL7InArchive(hl7InQueue);
	// Context.getHL7Service().createHL7InArchive(hl7InArchive);
	// Context.getHL7Service().deleteHL7InQueue(hl7InQueue);
	// }
	//
	// /**
	// * Creates an encounter
	// */
	// private Encounter createEncounter(User enterer, Form form, HL7Segment
	// orc,
	// HL7Segment pid, HL7Segment pv1) throws HL7Exception {
	// Encounter encounter = new Encounter();
	//
	// Date encounterDate = HL7Util.parseHL7Date(pv1.getField(44));
	// Date dateEntered = HL7Util.parseHL7Timestamp(orc.getField(9));
	// EncounterType encounterType = form.getEncounterType();
	// Location location = getLocation(pv1);
	// Patient patient = getPatient(pid);
	// User provider = getProvider(pv1);
	//
	// encounter.setEncounterDatetime(encounterDate);
	// encounter.setEncounterType(encounterType);
	// encounter.setForm(form);
	// encounter.setLocation(location);
	// encounter.setPatient(patient);
	// encounter.setProvider(provider);
	// encounter.setCreator(enterer);
	// encounter.setDateCreated(dateEntered);
	// Context.getEncounterService().createEncounter(encounter);
	//
	// if (encounter == null || encounter.getEncounterId() == null
	// || encounter.getEncounterId() == 0) {
	// throw new HL7Exception("Invalid encounter");
	// }
	// return encounter;
	// }
	//
	// /**
	// * Extracts the data enterer from the ORC segment
	// */
	// private User getEnterer(HL7Segment orc) throws HL7Exception {
	// String[] entererComponents = orc.getComponents(10);
	// Integer entererId = null;
	// try {
	// entererId = Context.getHL7Service().resolveUserId(entererComponents);
	// } catch (HL7Exception ex) {
	// throw new HL7Exception("Error retrieving User from ORC.orderer");
	// }
	// if (entererId == null) {
	// throw new HL7Exception("Could not find enterer specified in ORC
	// segment");
	// } else {
	// return Context.getUserService().getUser(entererId);
	// }
	// }
	//
	// /**
	// * Extract the patient from the PID segment
	// */
	// private Patient getPatient(HL7Segment pid) throws HL7Exception {
	// Integer ptId;
	// try {
	// ptId = Context.getHL7Service().resolvePatientId(pid);
	// } catch (HL7Exception ex) {
	// throw new HL7Exception("Error retrieving patient from PID segment", ex);
	// }
	// if (ptId == null) {
	// throw new HL7Exception("Could not find patient specified in PID
	// segment");
	// } else {
	// return Context.getPatientService().getPatient(ptId);
	// }
	// }
	//
	// /**
	// * Extracts the form used to record the observations from the MSH segment
	// * (we store the formId as a MSH profile ID in MSH-21)
	// */
	// private Form getForm(HL7Message hl7Message) throws HL7Exception {
	// String hl7FormId = hl7Message.getProfileId().split(
	// "\\" + hl7Message.getComponentSeparator())[0];
	// Integer formId = null;
	// Form form = null;
	// try {
	// formId = Integer.parseInt(hl7FormId);
	// } catch (NumberFormatException e) {
	// throw new HL7Exception("Invalid form ID '" + hl7FormId + "'");
	// }
	// if (formId == null || formId == 0) {
	// throw new HL7Exception("Unable to parse OpenMRS form ID '"
	// + hl7FormId + "'");
	// }
	// try {
	// form = Context.getFormService().getForm(formId);
	// } catch (Exception e) {
	// throw new HL7Exception("Error retrieving OpenMRS form " + formId, e);
	// }
	// if (form == null || form.getFormId() == null)
	// throw new HL7Exception("Could not find OpenMRS form " + formId);
	// return form;
	// }
	//
	// /**
	// * Extracts the location of the visit from the PV1 segment
	// */
	// private Location getLocation(HL7Segment pv1) throws HL7Exception {
	// String[] locationComponents = pv1.getComponents(3);
	// Integer locationId = null;
	// try {
	// locationId =
	// Context.getHL7Service().resolveLocationId(locationComponents);
	// } catch (HL7Exception ex) {
	// throw new HL7Exception("Error retrieving Location from PV1.'Assigned
	// Patient Location'", ex);
	// }
	// if (locationId == null) {
	// throw new HL7Exception("Could not find Assigned Patient Location
	// specified in PV1 segment");
	// } else {
	// return Context.getEncounterService().getLocation(locationId);
	// }
	// }
	//
	// /**
	// * Extracts the provider from the PV1 (visit) segment
	// */
	// private User getProvider(HL7Segment pv1) throws HL7Exception {
	// String[] providerComponents = pv1.getComponents(7);
	// Integer providerId = null;
	// try {
	// providerId = Context.getHL7Service().resolveUserId(providerComponents);
	// } catch (HL7Exception ex) {
	// throw new HL7Exception("Error retrieving User from PV1.provider");
	// }
	// if (providerId == null) {
	// throw new HL7Exception("Could not find provider specified in PV1
	// segment");
	// } else {
	// return Context.getUserService().getUser(providerId);
	// }
	// }
	//
	// /**
	// * Creates an observation. If the observed conceptId matches the keyword
	// for
	// * proposed concepts, then a ConceptProposal is generated instead.
	// */
	// private void createObservation(User enterer, Patient patient,
	// Encounter encounter, User provider, HL7Segment obx)
	// throws HL7Exception {
	// try {
	// String hl7Datatype = obx.getField(2);
	// Integer conceptId = Integer.parseInt(obx.getComponent(3, 1));
	// Concept concept = Context.getConceptService().getConcept(conceptId);
	// String subId = obx.getField(4);
	// String value = obx.getField(5);
	// String[] valueComponents = obx.getComponents(5);
	// String dateTimeRaw = obx.getField(14);
	// Date dateTime;
	// if (dateTimeRaw.length() < 1)
	// if (encounter != null)
	// dateTime = encounter.getEncounterDatetime();
	// else
	// dateTime = new Date();
	// else
	// dateTime = HL7Util.parseHL7Timestamp(obx.getField(14));
	//
	// Obs obs = new Obs();
	// obs.setPatient(patient);
	// obs.setConcept(concept);
	// obs.setEncounter(encounter);
	// obs.setObsDatetime(dateTime);
	// obs.setLocation(encounter.getLocation());
	// obs.setCreator(enterer);
	// if (!"".equals(subId)) {
	// obs.setObsGroupId(Integer.valueOf(subId));
	// }
	// if (encounter != null)
	// obs.setDateCreated(encounter.getDateCreated());
	// if (hl7Datatype.equals("NM"))
	// obs.setValueNumeric(Double.valueOf(value));
	// else if (hl7Datatype.equals("CWE") || hl7Datatype.equals("CE")) {
	// if (valueComponents[0]
	// .equals(OpenmrsConstants.PROPOSED_CONCEPT_IDENTIFIER)) {
	// proposeConcept(encounter, concept, valueComponents[1],
	// enterer);
	// return; // avoid trying to create an obs
	// } else {
	// Integer valueConceptId = Integer
	// .parseInt(valueComponents[0]);
	// Concept valueConcept = Context.getConceptService()
	// .getConcept(valueConceptId);
	// obs.setValueCoded(valueConcept);
	// }
	// } else if (hl7Datatype.equals("DT")) {
	// Date valueDate = HL7Util.parseHL7Date(value);
	// obs.setValueDatetime(valueDate);
	// } else if (hl7Datatype.equals("TS")) {
	// Date valueTimestamp = HL7Util.parseHL7Timestamp(value);
	// obs.setValueDatetime(valueTimestamp);
	// } else if (hl7Datatype.equals("TM")) {
	// Date valueTime = HL7Util.parseHL7Time(value);
	// obs.setValueDatetime(valueTime);
	// } else if (hl7Datatype.equals("ST"))
	// obs.setValueText(value);
	// else {
	// // unsupported data type
	// // TODO: support RP (report), SN (structured numeric)
	// // do we need to support BIT just in case it slips thru?
	// }
	// Context.getObsService().createObs(obs);
	//
	// } catch (Exception e) {
	// throw new HL7Exception(e);
	// }
	//
	// }
	//
	// /**
	// * Generates a ConceptProposal record
	// */
	// private void proposeConcept(Encounter encounter, Concept concept,
	// String originalText, User enterer) {
	// // value is a proposed concept, create a ConceptProposal
	// // instead of an Obs for this observation
	// // TODO: at this point if componentSeparator (^) is in text,
	// // we'll only use the text before that delimiter!
	// ConceptProposal conceptProposal = new ConceptProposal();
	// conceptProposal.setOriginalText(originalText);
	// conceptProposal.setState(OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED);
	// conceptProposal.setEncounter(encounter);
	// conceptProposal.setObsConcept(concept);
	// Context.getConceptService().proposeConcept(conceptProposal);
	// }
	//
	// /**
	// * Convenience method to respond to fatal errors by moving the queue entry
	// * into an error bin prior to aborting
	// */
	// private void setFatalError(HL7InQueue hl7InQueue, String error,
	// Throwable cause) {
	// HL7InError hl7InError = new HL7InError(hl7InQueue);
	// hl7InError.setError(error);
	// hl7InError.setErrorDetails(cause == null ? "" : cause.getMessage());
	// Context.getHL7Service().createHL7InError(hl7InError);
	// Context.getHL7Service().deleteHL7InQueue(hl7InQueue);
	// log.error(error, cause);
	// }
	//
	// /**
	// * Convenience method for tracking errors while processing observations
	// * (non-fatal errors)
	// */
	// private void addError(Vector<Error> errors, String hl7Data, String error,
	// String errorDetails) {
	// errors.add(new Error(hl7Data, error, errorDetails));
	// }
	//
	// /**
	// * Record (non-fatal) errors that occurred while processing observtions
	// */
	// private void recordErrors(HL7InQueue hl7InQueue, Vector<Error> errors) {
	// HL7InError hl7InError = new HL7InError();
	// String hl7Data = "";
	// String error = "";
	// String errorDetails = "";
	// for (Error err : errors) {
	// hl7Data += err.hl7Data + "\r";
	// error += err.error + "\n";
	// errorDetails += err.errorDetails + "\n";
	// }
	// hl7InError.setHL7Source(hl7InQueue.getHL7Source());
	// hl7InError.setHL7SourceKey(hl7InQueue.getHL7SourceKey());
	// hl7InError.setHL7Data(hl7Data);
	// hl7InError.setError(error);
	// hl7InError.setErrorDetails(errorDetails);
	// Context.getHL7Service().createHL7InError(hl7InError);
	// }
	//
	// /**
	// * Private inner class used to track errors while processing observations
	// */
	// private class Error {
	// String hl7Data;
	// String error;
	// String errorDetails;
	//
	// Error(String hl7Data, String error, String errorDetails) {
	// this.hl7Data = hl7Data;
	// this.error = error;
	// this.errorDetails = errorDetails;
	// }
	// }
}
