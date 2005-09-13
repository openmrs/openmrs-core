package org.openmrs.api.ibatis;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openmrs.Patient;
import org.openmrs.PatientAddress;
import org.openmrs.PatientName;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.context.Context;
import org.openmrs.util.Compare;

public class IbatisPatientService implements PatientService {
	
	Context context;

	public IbatisPatientService(Context context) {
		this.context = context;
	}
	
	/* TODO insert/update patient_identifier? */
	public Patient createPatient(Patient patient) throws APIException {
		try {
			User authenticatedUser = context.getAuthenticatedUser();
			
			patient.setCreator(authenticatedUser);
			SqlMap.instance().insert("createPatient", patient);
			SqlMap.instance().update("createTribe", patient.getTribe());
			for(Iterator i = patient.getAddresses().iterator(); i.hasNext();) {
				PatientAddress pAddress = (PatientAddress)i.next();
				pAddress.setCreator(authenticatedUser);
				SqlMap.instance().insert("createPatientAddress", pAddress);
			}
			for(Iterator i = patient.getNames().iterator(); i.hasNext();) {
				PatientName pName = (PatientName)i.next();
				pName.setCreator(authenticatedUser);
				SqlMap.instance().insert("createPatientName", pName);
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
		return patient;
	}
	
	public void updatePatient(Patient patient) throws APIException {
		try {
			try {
				Map map;
				List toAdd;
				List toDel;
				SqlMap.instance().startTransaction();
				if (patient.getCreator() == null)
					this.createPatient(patient);
				else {
					SqlMap.instance().update("updatePatient", patient);
					SqlMap.instance().update("updateTribe", patient.getTribe());
					
					//udpate addresses
					List oldAddresses = SqlMap.instance().queryForList("getPatientAddressByPatientId", patient.getPatientId());
					map = Compare.compareLists(oldAddresses, patient.getAddresses());
					toAdd = (List)map.get("toAdd");
					toDel = (List)map.get("toDel");
					for (Iterator i = toAdd.iterator(); i.hasNext();)
						SqlMap.instance().insert("createPatientAddress", i.next());
					for (Iterator i = toDel.iterator(); i.hasNext();)
						SqlMap.instance().insert("deletePatientAddress", i.next());
					
					//update names
					List oldNames = SqlMap.instance().queryForList("getPatientNameByPatientId", patient.getPatientId());
					map = Compare.compareLists(oldNames, patient.getNames());
					toAdd = (List)map.get("toAdd");
					toDel = (List)map.get("toDel");
					for (Iterator i = toAdd.iterator(); i.hasNext();)
						SqlMap.instance().insert("createPatientName", i.next());
					for (Iterator i = toDel.iterator(); i.hasNext();)
						SqlMap.instance().insert("deletePatientName", i.next());
					
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

	
}
