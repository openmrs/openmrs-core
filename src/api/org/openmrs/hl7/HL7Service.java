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

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.datatype.CX;
import ca.uhn.hl7v2.model.v25.datatype.PL;
import ca.uhn.hl7v2.model.v25.datatype.TS;
import ca.uhn.hl7v2.model.v25.datatype.XCN;
import ca.uhn.hl7v2.model.v25.datatype.XPN;
import ca.uhn.hl7v2.model.v25.segment.PID;

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
	 * @param xcn
	 *            HL7 component of data type XCN (extended composite ID number
	 *            and name for persons) (see HL7 2.5 manual Ch.2A.86)
	 * @return Internal ID # of the specified user, or null if that user can't
	 *         be found or is ambiguous
	 */
	public Integer resolveUserId(XCN xcn) throws HL7Exception {
		// TODO: properly handle family and given names. For now I'm treating
		// givenName+familyName as a username.
		String idNumber = xcn.getIDNumber().getValue();
		String familyName = xcn.getFamilyName().getOwnSurname().getValue();
		String givenName = xcn.getGivenName().getValue();
		String assigningAuthority = xcn.getAssigningAuthority()
				.getUniversalID().getValue();
		if (idNumber != null && idNumber.length() > 0) {
			// log.debug("searching for user by id " + idNumber);
			try {
				Integer userId = new Integer(idNumber);
				User user = context.getUserService().getUser(userId);
				return user.getUserId();
			} catch (Exception e) {
				log.error("Invalid user ID '" + idNumber + "'", e);
				return null;
			}
		} else {
			// log.debug("searching for user by name");
			try {
				StringBuilder username = new StringBuilder();
				if (familyName != null) {
					username.append(familyName);
				}
				if (givenName != null) {
					if (username.length() > 0)
						username.append(" "); // separate names with a space
					username.append(givenName);
				}
				User user = context.getUserService().getUserByUsername(
						username.toString());
				return user.getUserId();
			} catch (Exception e) {
				log.error("Error resolving user with family name '"
						+ familyName + "' and given name '" + givenName + "'",
						e);
				return null;
			}
		}
	}

	/**
	 * @param pl
	 *            HL7 component of data type PL (person location) (see Ch
	 *            2.A.53)
	 * @return internal identifier of the specified location, or null if it is
	 *         not found or ambiguous
	 */
	public Integer resolveLocationId(PL pl) throws HL7Exception {
		// TODO: Get rid of hack that allows first component to be an integer
		// location.location_id
		String pointOfCare = pl.getPointOfCare().getValue();
		String facility = pl.getFacility().getUniversalID().getValue();

		// HACK: try to treat the first component (which should be "Point of
		// Care" as an internal openmrs location_id
		try {
			Integer locationId = new Integer(pointOfCare);
			Location l = context.getEncounterService().getLocation(locationId);
			return l == null ? null : l.getLocationId();
		} catch (Exception ex) {
			if (facility == null) { // we have no tricks left up our sleeve, so
				// throw an exception
				throw new HL7Exception("Error trying to treat PL.pointOfCare '"
						+ pointOfCare + "' as a location.location_id", ex);
			}
		}

		// Treat the 4th component "Facility" as location.name
		try {
			Location l = context.getEncounterService().getLocationByName(
					facility);
			if (l == null) {
				log.debug("Couldn't find a location named '" + facility + "'");
			}
			return l == null ? null : l.getLocationId();
		} catch (Exception ex) {
			log.error("Error trying to treat PL.facility '" + facility
					+ "' as a location.name", ex);
			return null;
		}
	}

	/**
	 * @param pid
	 *            A PID segment of an hl7 message
	 * @return The internal id number of the Patient described by the PID
	 *         segment, or null of the patient is not found, or if the PID
	 *         segment is ambiguous
	 * @throws HL7Exception
	 */
	public Integer resolvePatientId(PID pid) throws HL7Exception {
		// TODO: Properly handle assigning authority. If specified it's
		// currently treated as PatientIdentifierType.name
		// TODO: Throw exceptions instead of returning null in some cases
		// TODO: Don't hydrate Patient objects unnecessarily
		// TODO: Determine how to handle assigning authority and openmrs
		// patient_id numbers

		Integer patientId = null;

		CX[] patientIdentifierList = pid.getPatientIdentifierList();
		if (patientIdentifierList.length < 1)
			throw new HL7Exception("Missing patient identifier in PID segment");

		// TODO other potential identifying characteristics in PID we could use
		// to identify the patient
		// XPN[] patientName = pid.getPatientName();
		// String gender = pid.getAdministrativeSex().getValue();
		// TS dateOfBirth = pid.getDateTimeOfBirth();

		// Take the first uniquely matching identifier
		for (CX identifier : patientIdentifierList) {
			String hl7PatientId = identifier.getIDNumber().getValue();
			// TODO if 1st component is blank, check 2nd and 3rd of assigning
			// authority
			String assigningAuthority = identifier.getAssigningAuthority()
					.getNamespaceID().getValue();

			if (assigningAuthority != null && assigningAuthority.length() > 0) {
				// Assigning authority defined
				try {
					PatientIdentifierType pit = context.getPatientService()
							.getPatientIdentifierType(assigningAuthority);
					if (pit == null) {
						log.debug("Can't find PatientIdentifierType named '"
								+ assigningAuthority + "'");
						continue; // skip identifiers with unknown type
					}
					List<PatientIdentifier> matchingIds = context
							.getPatientService().getPatientIdentifiers(
									hl7PatientId, pit);
					if (matchingIds == null || matchingIds.size() < 1) {
						// no matches
						continue; // try next identifier
					} else if (matchingIds.size() == 1) {
						// unique match -- we're done
						return matchingIds.get(0).getPatient().getPatientId();
					} else {
						// ambiguous identifier
						log.debug("Ambiguous identifier in PID. "
								+ matchingIds.size()
								+ " matches for identifier '" + hl7PatientId
								+ "' of type '" + pit + "'");
						continue; // try next identifier
					}
				} catch (Exception e) {
					log.error("Error resolving patient identifier '"
							+ hl7PatientId + "' for assigning authority '"
							+ assigningAuthority + "'", e);
					continue;
				}
			} else {
				try {
					log
							.debug("PID contains patient ID '"
									+ hl7PatientId
									+ "' without assigning authority -- assuming patient.patient_id");
					patientId = Integer.parseInt(hl7PatientId);
					return patientId;
				} catch (NumberFormatException e) {
					// throw new HL7Exception("Invalid patient ID '" +
					// hl7PatientId + "'");
					log.warn("Invalid patient ID '" + hl7PatientId + "'");
				}
			}
		}

		return null;
	}

	public void garbageCollect() {
		dao().garbageCollect();
	}
}
