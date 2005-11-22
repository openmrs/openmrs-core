package org.openmrs.api.db.ibatis;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openmrs.Patient;
import org.openmrs.PatientAddress;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientName;
import org.openmrs.Tribe;
import org.openmrs.User;
import org.openmrs.api.db.APIException;
import org.openmrs.api.db.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.util.Compare;

public class IbatisPatientService implements PatientService {
	
	Context context;

	public IbatisPatientService(Context context) {
		this.context = context;
	}
	
	public Patient createPatient(Patient patient) throws APIException {
		try {
			User authenticatedUser = context.getAuthenticatedUser();
			
			patient.setCreator(authenticatedUser);
			
			SqlMap.instance().insert("createPatient", patient);
			
			//add all items in all sets
			for(Iterator i = patient.getAddresses().iterator(); i.hasNext();) {
				PatientAddress pAddress = (PatientAddress)i.next();
				pAddress.setCreator(authenticatedUser);
				pAddress.setPatient(patient);
				SqlMap.instance().insert("createPatientAddress", pAddress);
				pAddress.setClean();
			}
			for(Iterator i = patient.getNames().iterator(); i.hasNext();) {
				PatientName pName = (PatientName)i.next();
				pName.setCreator(authenticatedUser);
				pName.setPatient(patient);
				SqlMap.instance().insert("createPatientName", pName);
				pName.setClean();
			}
			for(Iterator i = patient.getIdentifiers().iterator(); i.hasNext();) {
				PatientIdentifier pIdentifier = (PatientIdentifier)i.next();
				pIdentifier.setCreator(authenticatedUser);
				pIdentifier.setPatient(patient);
				SqlMap.instance().insert("createPatientIdentifier", pIdentifier);
				pIdentifier.setClean();
			}
			
			patient.setClean();
			
		} catch (SQLException e) {
			throw new APIException(e);
		}
		return patient;
	}
	
	// TODO void patient names instead of deleting them
	// TODO update all addresses and names passed in 
	public void updatePatient(Patient patient) throws APIException {
		try {
			try {
				Map map;
				List toAdd;
				List toDel;
				SqlMap.instance().startTransaction();
				//TODO do we want to check on getPatientId instead of creator?
				if (patient.getCreator() == null)
					this.createPatient(patient);
				else {
					
					User authenticatedUser = context.getAuthenticatedUser(); 
					
					if (patient.isDirty()) {
						//======== update addresses ================
						List oldAddresses = SqlMap.instance().queryForList("getPatientAddressByPatientId", patient.getPatientId());
						map = Compare.compareLists(oldAddresses, patient.getAddresses());
						toAdd = (List)map.get("toAdd");
						toDel = (List)map.get("toDel");
						//add addresses to db if patient now has them
						if (toAdd != null) {
							for (Iterator i = toAdd.iterator(); i.hasNext();)
							{
								PatientAddress pAddress = (PatientAddress)i.next();
								SqlMap.instance().insert("createPatientAddress", pAddress);
								pAddress.setClean();
							}
						}
						//delete db addresses no longer on patient
						if (toDel != null) {
							for (Iterator i = toDel.iterator(); i.hasNext();)
								SqlMap.instance().update("voidPatientAddress", i.next());
						}
						// update dirty addresses
						for (Iterator i = patient.getAddresses().iterator(); i.hasNext();) {
							PatientAddress pAddress = (PatientAddress)i.next();
							if (pAddress.isDirty()) {
								SqlMap.instance().update("updatePatientAddress", pAddress);
								pAddress.setClean();
							}
						}
						
						//======== update names ====================
						List oldNames = SqlMap.instance().queryForList("getPatientNameByPatientId", patient.getPatientId());
						map = Compare.compareLists(oldNames, patient.getNames());
						toAdd = (List)map.get("toAdd");
						toDel = (List)map.get("toDel");
						if (toAdd != null) {
							for (Iterator i = toAdd.iterator(); i.hasNext();)
							{
								PatientName pName = (PatientName)i.next();
								pName.setCreator(authenticatedUser);
								SqlMap.instance().insert("createPatientName", pName);
								pName.setClean();
							}
						}
						if (toDel != null) {
							for (Iterator i = toDel.iterator(); i.hasNext();)
								SqlMap.instance().delete("deletePatientName", i.next());
						}
						// update dirty addresses
						for (Iterator i = patient.getNames().iterator(); i.hasNext();) {
							PatientName pName = (PatientName)i.next();
							if (pName.isDirty()) {
								SqlMap.instance().update("updatePatientName", pName);
								pName.setClean();
							}
						}

						//======== update identifiers ====================
						List oldIdentifiers = SqlMap.instance().queryForList("getPatientIdentifierByPatientId", patient.getPatientId());
						map = Compare.compareLists(oldIdentifiers, patient.getIdentifiers());
						toAdd = (List)map.get("toAdd");
						toDel = (List)map.get("toDel");
						// add ids to db that patient obj now has
						if (toAdd != null) {
							for (Iterator i = toAdd.iterator(); i.hasNext();)
							{
								PatientIdentifier pIdentifier = (PatientIdentifier)i.next();
								pIdentifier.setCreator(authenticatedUser);
								SqlMap.instance().insert("createPatientIdentifier", pIdentifier);
								pIdentifier.setClean();
							}
						}
						// remove ids from db that patient obj does not have
						if (toDel != null) {
							for (Iterator i = toDel.iterator(); i.hasNext();)
								SqlMap.instance().delete("deletePatientIdentifier", i.next());
						}
						// update dirty identifiers
						for (Iterator i = patient.getIdentifiers().iterator(); i.hasNext();) {
							PatientIdentifier pIdentifier = (PatientIdentifier)i.next();
							if (pIdentifier.isDirty()) {
								SqlMap.instance().update("updatePatientIdentifier", pIdentifier);
								pIdentifier.setClean();
							}
						}
						
						//======== update patient object =============
						patient.setChangedBy(authenticatedUser);
						SqlMap.instance().update("updatePatient", patient);
						patient.setClean();
					}
					
				}
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
		
	}

	public void deletePatient(Patient patient) throws APIException {
		try {
			try {
				SqlMap.instance().startTransaction();
				SqlMap.instance().delete("deletePatient", patient);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
		
	}

	public Patient getPatient(Integer patientId) throws APIException {
		Patient patient;
		try {
			patient = (Patient)SqlMap.instance().queryForObject("getPatient", patientId);
		} catch (SQLException e) {
			throw new APIException(e);
		}
		return patient;
	}

	public List getPatientByIdentifier(String identifier) throws APIException {
		List patients;
		try {
			patients = SqlMap.instance().queryForList("getPatientByIdentifier", identifier);
		} catch (SQLException e) {
			throw new APIException(e);
		}
		return patients;
	}

	public void voidPatient(Patient patient, String reason) throws APIException {
		patient.setVoided(true);
		patient.setVoidedBy(context.getAuthenticatedUser());
		patient.setVoidReason(reason);
		try {
			SqlMap.instance().update("voidPatient", patient);
		} catch (SQLException e) {
			throw new APIException(e);
		}
		
	}
	
	public List<PatientIdentifierType> getPatientIdentifierTypes() throws APIException {
		
		List<PatientIdentifierType> patientIdentifierTypes;
		
		try {
			try {
				SqlMap.instance().startTransaction();
				patientIdentifierTypes = SqlMap.instance().queryForList("getAllPatientIdentifierTypes", null);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
		
		return patientIdentifierTypes;
	}

	public PatientIdentifierType getPatientIdentifierType(Integer patientIdentifierTypeId) throws APIException {

		PatientIdentifierType patientIdentifierType;
		
		try {
			try {
				SqlMap.instance().startTransaction();
				patientIdentifierType = (PatientIdentifierType)SqlMap.instance().queryForObject("getPatientIdentifierType", patientIdentifierTypeId);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
		
		return patientIdentifierType;
	}

	/**
	 * @see org.openmrs.api.db.PatientService#getPatientTribes()
	 */
	public List<Tribe> getPatientTribes() throws APIException {
		List<Tribe> tribes;
		
		try {
			try {
				SqlMap.instance().startTransaction();
				tribes = SqlMap.instance().queryForList("getAllTribes", null);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
		
		return tribes;
	}

	
}
