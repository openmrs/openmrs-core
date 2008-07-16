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
package org.openmrs.logic.datasource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.result.Result;

/**
 * Provides access to patient program data
 */
public class ProgramDataSource implements LogicDataSource {

	private static Log log = LogFactory.getLog(ProgramDataSource.class);
	private static final Collection<String> keys = new ArrayList<String>();
    
	private static String PROGRAM					= "PROGRAM";
	private static String PROGRAM_ENROLLMENT_KEY 	= "PROGRAM ENROLLMENT";	
	private static String PROGRAM_COMPLETED_KEY 	= "PROGRAM COMPLETION";
	private static String CURRENT_STATE_KEY 		= "CURRENT STATE";

    static {
        String[] keyList = new String[] { 
        	PROGRAM_ENROLLMENT_KEY,
    		PROGRAM_COMPLETED_KEY,
    		CURRENT_STATE_KEY
        };
        
        for (String k : keyList)
            keys.add(k);
    }	
    
    
    
 
    /**
     * 
     * @see org.openmrs.logic.datasource.LogicDataSource#read(org.openmrs.logic.LogicContext, org.openmrs.Cohort, org.openmrs.logic.LogicCriteria)
     */
    public Map<Integer, Result> read(LogicContext context, Cohort patients,
            LogicCriteria criteria) {

    	
    	log.info("read patient programs for " + patients.size() + " patients, criteria " + criteria);
		Map<Integer, Result> resultSet = new HashMap<Integer, Result>();
				
		Collection<PatientProgram> patientPrograms = getPatientPrograms(patients, criteria);
		
		for (PatientProgram patientProgram : patientPrograms) {
			//log.info("PatientProgram: " + patientProgram.getDateEnrolled());
			String token = (String) criteria.getRightOperand();
			Integer personId = patientProgram.getPatient().getPersonId();

			Result result = null;
			
			if (PROGRAM_ENROLLMENT_KEY.equals(token)) { 
				result = new Result(patientProgram.getProgram().getConcept());
				result.setResultDate(patientProgram.getDateEnrolled());						
			} 
			else if (PROGRAM_COMPLETED_KEY.equals(token)) { 
				result = new Result(patientProgram.getProgram().getConcept());
				result.setResultDate(patientProgram.getDateCompleted());
			} 			
			else if (CURRENT_STATE_KEY.equals(token)) { 
				result = new Result(patientProgram.getCurrentState().getState().getConcept());
				result.setResultDate(patientProgram.getDateEnrolled());				
			}
			
			if (result != null) {
				log.info("Add result to result set: " + result);
				if (!resultSet.containsKey(personId)) { 
					resultSet.put(personId, result);
				}
				else { 
					resultSet.get(personId).add(result);
				}
			}					
		}
		
		return resultSet;
    }

    /**
     * @see org.openmrs.logic.datasource.LogicDataSource#getDefaultTTL()
     */
    public int getDefaultTTL() {
        return 60 * 30; // 30 minutes
    }

    /**
     * @see org.openmrs.logic.datasource.LogicDataSource#getKeys()
     */
    public Collection<String> getKeys() {
        return keys;
    }

    /**
     * @see org.openmrs.logic.datasource.LogicDataSource#hasKey(java.lang.String)
     */
    public boolean hasKey(String key) {
        return getKeys().contains(key);
    }

    
    
    /**
     * Get the patient programs for the 
     * 
     * @param patients
     * @param criteria
     * @return
     */
    public Collection<PatientProgram> getPatientPrograms(Cohort patients, LogicCriteria criteria) {        	
    	Collection<PatientProgram> patientPrograms = new ArrayList<PatientProgram>();    	
    	ProgramWorkflowService service = Context.getProgramWorkflowService();
    	for(Integer patientId : patients.getMemberIds()) { 
    		//log.info("Patient: " + patient);
    		patientPrograms.addAll(service.getPatientPrograms(new Patient(patientId)));
    		
    	}
    	//log.info("Patient programs: " + patientPrograms.size());
    	return patientPrograms;
    	
    }
}
