package org.openmrs.hl7;

import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOContext;
import org.openmrs.hl7.db.HL7DAO;

/**
 * OpenMRS HL7 API
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public class HL7Service {

	private Log log = LogFactory.getLog(this.getClass());

	private Context context;
	private DAOContext daoContext;

	public HL7Service(Context c, DAOContext d) {
		this.context = c;
		this.daoContext = d;
	}

	private HL7DAO dao() {
		return daoContext.getHL7DAO();
	}

	public void createHL7Source(HL7Source hl7Source) {
		if (!context.hasPrivilege(HL7Constants.PRIV_ADD_HL7_SOURCE))
			throw new APIAuthenticationException(
					"Insufficient privilege to create an HL7 source");
		dao().createHL7Source(hl7Source);
	}

	public HL7Source getHL7Source(Integer hl7SourceId) {
		if (!context.hasPrivilege(HL7Constants.PRIV_VIEW_HL7_SOURCE))
			throw new APIAuthenticationException(
					"Insufficient privilege to view an HL7 source");
		return dao().getHL7Source(hl7SourceId);
	}

	public Collection<HL7Source> getHL7Sources() {
		if (!context.hasPrivilege(HL7Constants.PRIV_VIEW_HL7_SOURCE))
			throw new APIAuthenticationException(
					"Insufficient privilege to view an HL7 source");
		return dao().getHL7Sources();
	}

	public void updateHL7Source(HL7Source hl7Source) {
		if (!context.hasPrivilege(HL7Constants.PRIV_UPDATE_HL7_SOURCE))
			throw new APIAuthenticationException(
					"Insufficient privilege to update an HL7 source");
		dao().updateHL7Source(hl7Source);
	}

	public void deleteHL7Source(HL7Source hl7Source) {
		if (!context.hasPrivilege(HL7Constants.PRIV_DELETE_HL7_SOURCE))
			throw new APIAuthenticationException(
					"Insufficient privilege to delete an HL7 source");
		dao().deleteHL7Source(hl7Source);
	}

	public void createHL7InQueue(HL7InQueue hl7InQueue) {
		if (!context.hasPrivilege(HL7Constants.PRIV_ADD_HL7_IN_QUEUE))
			throw new APIAuthenticationException(
					"Insufficient privilege to create an HL7 inbound queue entry");
		dao().createHL7InQueue(hl7InQueue);
	}

	public HL7InQueue getHL7InQueue(Integer hl7InQueueId) {
		if (!context.hasPrivilege(HL7Constants.PRIV_VIEW_HL7_IN_QUEUE))
			throw new APIAuthenticationException(
					"Insufficient privilege to view an HL7 inbound queue entry");
		return dao().getHL7InQueue(hl7InQueueId);
	}

	public Collection<HL7InQueue> getHL7InQueues() {
		if (!context.hasPrivilege(HL7Constants.PRIV_VIEW_HL7_IN_QUEUE))
			throw new APIAuthenticationException(
					"Insufficient privilege to view an HL7 inbound queue entry");
		return dao().getHL7InQueues();
	}

	public HL7InQueue getNextHL7InQueue() {
		if (!context.hasPrivilege(HL7Constants.PRIV_VIEW_HL7_IN_QUEUE))
			throw new APIAuthenticationException(
					"Insufficient privilege to view an HL7 inbound queue entry");
		return dao().getNextHL7InQueue();
	}

	public void deleteHL7InQueue(HL7InQueue hl7InQueue) {
		if (!context.hasPrivilege(HL7Constants.PRIV_DELETE_HL7_IN_QUEUE))
			throw new APIAuthenticationException(
					"Insufficient privilege to delete an HL7 inbound queue entry");
		dao().deleteHL7InQueue(hl7InQueue);
	}
	
	public void createHL7InArchive(HL7InArchive hl7InArchive) {
		if (!context.hasPrivilege(HL7Constants.PRIV_ADD_HL7_IN_ARCHIVE))
			throw new APIAuthenticationException(
					"Insufficient privilege to create an HL7 inbound archive entry");		
		dao().createHL7InArchive(hl7InArchive);
	}

	public HL7InArchive getHL7InArchive(Integer hl7InArchiveId) {
		if (!context.hasPrivilege(HL7Constants.PRIV_VIEW_HL7_IN_ARCHIVE))
			throw new APIAuthenticationException(
					"Insufficient privilege to view an HL7 inbound archive entry");		
		return dao().getHL7InArchive(hl7InArchiveId);
	}
	
	public Collection<HL7InArchive> getHL7InArchives() {
		if (!context.hasPrivilege(HL7Constants.PRIV_VIEW_HL7_IN_ARCHIVE))
			throw new APIAuthenticationException(
					"Insufficient privilege to view an HL7 inbound archive entry");		
		return dao().getHL7InArchives();
	}
	
	public void updateHL7InArchive(HL7InArchive hl7InArchive) {
		if (!context.hasPrivilege(HL7Constants.PRIV_UPDATE_HL7_IN_ARCHIVE))
			throw new APIAuthenticationException(
					"Insufficient privilege to update an HL7 inbound archive entry");		
		dao().updateHL7InArchive(hl7InArchive);
	}
	
	public void deleteHL7InArchive(HL7InArchive hl7InArchive) {
		if (!context.hasPrivilege(HL7Constants.PRIV_DELETE_HL7_IN_ARCHIVE))
			throw new APIAuthenticationException(
					"Insufficient privilege to delete an HL7 inbound archive entry");		
		dao().deleteHL7InArchive(hl7InArchive);
	}

	public void createHL7InError(HL7InError hl7InError) {
		if (!context.hasPrivilege(HL7Constants.PRIV_ADD_HL7_IN_ARCHIVE))
			throw new APIAuthenticationException(
					"Insufficient privilege to create an HL7 inbound archive entry");		
		dao().createHL7InError(hl7InError);
	}

	public HL7InError getHL7InError(Integer hl7InErrorId) {
		if (!context.hasPrivilege(HL7Constants.PRIV_VIEW_HL7_IN_ARCHIVE))
			throw new APIAuthenticationException(
					"Insufficient privilege to view an HL7 inbound archive entry");		
		return dao().getHL7InError(hl7InErrorId);
	}
	
	public Collection<HL7InError> getHL7InErrors() {
		if (!context.hasPrivilege(HL7Constants.PRIV_VIEW_HL7_IN_ARCHIVE))
			throw new APIAuthenticationException(
					"Insufficient privilege to view an HL7 inbound archive entry");		
		return dao().getHL7InErrors();
	}
	
	public void updateHL7InError(HL7InError hl7InError) {
		if (!context.hasPrivilege(HL7Constants.PRIV_UPDATE_HL7_IN_ARCHIVE))
			throw new APIAuthenticationException(
					"Insufficient privilege to update an HL7 inbound archive entry");		
		dao().updateHL7InError(hl7InError);
	}
	
	public void deleteHL7InError(HL7InError hl7InError) {
		if (!context.hasPrivilege(HL7Constants.PRIV_DELETE_HL7_IN_ARCHIVE))
			throw new APIAuthenticationException(
					"Insufficient privilege to delete an HL7 inbound archive entry");		
		dao().deleteHL7InError(hl7InError);
	}
	
	/**
	 * @param xcn HL7 component of data type XCN (extended composite ID number and name for persons) (see HL7 2.5 manual Ch.2A.86) 
	 * @return Internal ID # of the specified user, or null if that user can't be found or is ambiguous
	 */
	public Integer resolveUserId(String[] xcn) throws HL7Exception {
		// TODO: properly handle family and given names. For now I'm treating givenName+familyName as a username.
		String idNumber = xcn[0];
		String familyName = null;
		String givenName = null;
		String assigningAuthority = null;
		if (xcn.length >= 3) {
			familyName = xcn[1];
			givenName = xcn[2];
		}
		if (xcn.length >= 9) {
			assigningAuthority = xcn[8];
		}
		if (idNumber != null) {
			try {
				Integer userId = new Integer(idNumber);
				User u = context.getUserService().getUser(userId);
				return u.getUserId();
			} catch (Exception ex) {
				log.error("Error handling ID Number component '" + idNumber + "' of XCN.", ex);
				return null;
			}
		} else {
			try {
				StringBuilder username = new StringBuilder();
				if (familyName != null) {
					username.append(familyName);
				}
				if (givenName != null) {
					username.append(givenName);
				}
				User u = context.getUserService().getUserByUsername(username.toString());
				return u.getUserId();
			} catch (Exception ex) {
				log.error("Error handling family name '" + familyName + "' and given name '" + givenName + "' components of XCN.", ex);
				return null;
			}
		}
	}
	
	/**
	 * @param pl HL7 component of data type PL (person location) (see Ch 2.A.53)
	 * @return internal identifier of the specified location, or null if it is not found or ambiguous
	 */
	public Integer resolveLocationId(String[] pl) throws HL7Exception {
		// TODO: Get rid of hack that allows first component to be an integer location.location_id
		String pointOfCare = pl[0];
		String facility = null;
		if (pl.length >= 4) {
			facility = pl[3];
		}

		// HACK: try to treat the first component (which should be "Point of Care" as an internal openmrs location_id 
		try {
			Integer locationId = new Integer(pointOfCare);
			Location l = context.getEncounterService().getLocation(locationId);
			return l == null ? null : l.getLocationId(); 
		} catch (Exception ex) {
			if (facility == null) { // we have no tricks left up our sleeve, so throw an exception
				throw new HL7Exception("Error trying to treat PL.pointOfCare '" + pointOfCare + "' as a location.location_id", ex);
			}
		}
		
		// Treat the 4th component "Facility" as location.name
		try {
			Location l = context.getEncounterService().getLocationByName(facility);
			if (l == null) {
				log.debug("Couldn't find a location named '" + facility + "'");
			}
			return l == null ? null : l.getLocationId();
		} catch (Exception ex) {
			log.error("Error trying to treat PL.facility '" + facility + "' as a location.name", ex);
			return null;
		}
	}
	
	/**
	 * @param pid A PID segment of an hl7 message
	 * @return The internal id number of the Patient described by the PID segment, or null of the patient is not found, or if the PID segment is ambiguous 
	 * @throws HL7Exception
	 */
	public Integer resolvePatientId(HL7Segment pid) throws HL7Exception {
		/*
		 * TODO: Properly handle assigning authority. If specified it's currently treated as PatientIdentifierType.name
		 * TODO: Throw exceptions instead of returning null in some cases
		 * TODO: Don't hydrate Patient objects unnecessarily
		 * 
		 ***TODO: Determine how to handle assigning authority and openmrs patient_id numbers***
		 * 
		 */
		Integer patientId = null;
		String hl7PatientId = pid.getComponent(3, 1);
		String assigningAuthority = pid.getComponent(3, 4);
		if ("".equals(assigningAuthority)) {
			assigningAuthority = null;
		}
		
		try {
			patientId = Integer.parseInt(hl7PatientId);
		} catch (NumberFormatException e) {
			//throw new HL7Exception("Invalid patient ID '" + hl7PatientId + "'");
			log.warn("Invalid patient ID '" + hl7PatientId + "'");
		}
		
		if (assigningAuthority == null || patientId != null) {
			try {
				Integer ptId = new Integer(hl7PatientId);
				Patient patient = context.getPatientService().getPatient(ptId);
				return patient.getPatientId();
			} catch (Exception ex) {
				log.error("Exception while treating PID.patient_id '" + hl7PatientId + "' as an internal identifier", ex);
				return null;
			}
		} else {
			try {
				PatientIdentifierType pit = context.getPatientService().getPatientIdentifierType(assigningAuthority);
				if (pit == null) {
					throw new HL7Exception("Can't find PatientIdentifierType named " + assigningAuthority);
				}
				List<PatientIdentifier> ids = context.getPatientService().getPatientIdentifiers(hl7PatientId, pit);
				if (ids.size() == 1) {
					return ids.get(0).getPatient().getPatientId();
				} else {
					return null;
				}
			} catch (Exception ex) {
				log.error("Exception while handling PID.patient_id '" + hl7PatientId + "' for assigning authority '" + assigningAuthority + "'", ex);
				return null;
			}
		}
	}

	public void garbageCollect() {
		dao().garbageCollect();
	}
}
