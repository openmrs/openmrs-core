package org.openmrs.api.ibatis;

import java.sql.SQLException;
import java.util.List;

import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.context.Context;

public class IbatisPatientService implements PatientService {
	
	Context context;

	public IbatisPatientService(Context context) {
		this.context = context;
	}
	
	public Patient createPatient(Patient patient) throws APIException {
		// TODO Auto-generated method stub
		return null;
	}

	public void deletePatient(Integer patientId) throws APIException {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}

	
}
