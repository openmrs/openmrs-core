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
package org.openmrs.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientProgram;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.Tribe;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.BlankIdentifierException;
import org.openmrs.api.DuplicateIdentifierException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.IdentifierNotUniqueException;
import org.openmrs.api.InsufficientIdentifiersException;
import org.openmrs.api.InvalidCheckDigitException;
import org.openmrs.api.InvalidIdentifierFormatException;
import org.openmrs.api.MissingRequiredIdentifierException;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientIdentifierException;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.PatientDAO;
import org.openmrs.order.OrderUtil;
import org.openmrs.patient.IdentifierValidator;
import org.openmrs.patient.UnallowedIdentifierException;
import org.openmrs.patient.impl.LuhnIdentifierValidator;
import org.openmrs.util.OpenmrsConstants;

/**
 * Default implementation of the patient service.  This class should
 * not be used on its own.  The current OpenMRS implementation
 * should be fetched from the Context via <code>Context.getPatientService()</code>
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.api.PatientService
 * @see org.openmrs.api.PersonService
 */
public class PatientServiceImpl extends BaseOpenmrsService implements PatientService {

	private Log log = LogFactory.getLog(this.getClass());
	
	private PatientDAO dao;
	
	/**
	 * PatientIdentifierValidators registered through spring's applicationContext-service.xml
	 */
	private static Map<Class<? extends IdentifierValidator>, IdentifierValidator> identifierValidators = null;
	
	/**
	 * @see org.openmrs.api.PatientService#setPatientDAO(org.openmrs.api.db.PatientDAO)
	 */
	public void setPatientDAO(PatientDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @see {@link #savePatient(Patient)}
	 * @deprecated replaced by #savePatient(Patient)
	 * @see org.openmrs.api.PatientService#createPatient(org.openmrs.Patient)
	 */
	public Patient createPatient(Patient patient) throws APIException {
		return savePatient(patient);
	}
			
	/**
	 * @see org.openmrs.api.PatientService#savePatient(org.openmrs.Patient)
	 */
	public Patient savePatient(Patient patient) throws APIException {
		if (patient.getPatientId() == null)
			Context.requirePrivilege(OpenmrsConstants.PRIV_ADD_PATIENTS);
		else
			Context.requirePrivilege(OpenmrsConstants.PRIV_EDIT_PATIENTS);
		
		setCollectionProperties(patient);
		
		checkPatientIdentifiers(patient);
		
		Date now = new Date();
		if (patient.getDateCreated() == null)
			patient.setDateCreated(now);
		if (patient.getCreator() == null)
			patient.setCreator(Context.getAuthenticatedUser());
		if (patient.getPatientId() != null) {
			patient.setChangedBy(Context.getAuthenticatedUser());
			patient.setDateChanged(now);
		}
		
		return dao.savePatient(patient);
	}

	/**
	 * @see org.openmrs.api.PatientService#getPatient(java.lang.Integer)
	 */
	public Patient getPatient(Integer patientId) throws APIException {
		return dao.getPatient(patientId);
	}

	/**
	 * @see #savePatient(Patient)
	 * @deprecated replaced by #savePatient(Patient)
	 * @see org.openmrs.api.PatientService#updatePatient(org.openmrs.Patient)
	 */
	public Patient updatePatient(Patient patient) throws APIException {
		return savePatient(patient);
	}
	
	/**
     * @see org.openmrs.api.PatientService#getAllPatients()
     */
    public List<Patient> getAllPatients() throws APIException {
	    return getAllPatients(false);
    }

	/**
     * @see org.openmrs.api.PatientService#getAllPatients(boolean)
     */
    public List<Patient> getAllPatients(boolean includeVoided)
            throws APIException {
	    return dao.getAllPatients(includeVoided);
    }
		
	/**
     * @see org.openmrs.api.PatientService#getPatients(java.lang.String, java.lang.String, java.util.List)
     */
    public List<Patient> getPatients(String name, String identifier,
            List<PatientIdentifierType> identifierTypes)
            throws APIException {
		
    	if (identifierTypes == null)
    		identifierTypes = Collections.emptyList();
		
    	return dao.getPatients(name, identifier, identifierTypes);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#checkPatientIdentifiers(org.openmrs.Patient)
	 */
	public void checkPatientIdentifiers(Patient patient)
	        throws PatientIdentifierException {
		// check patient has at least one identifier
		if (patient.getIdentifiers().size() < 1 )
			throw new InsufficientIdentifiersException("At least one Patient Identifier is required");

		List<PatientIdentifier> identifiers = new Vector<PatientIdentifier>();
		identifiers.addAll(patient.getIdentifiers());
		List<String> identifiersUsed = new Vector<String>();
		List<PatientIdentifierType> requiredTypes = getPatientIdentifierTypes(null, null, true, null);
		if (requiredTypes == null)
			requiredTypes = new ArrayList<PatientIdentifierType>();
		List<PatientIdentifierType> foundRequiredTypes = new ArrayList<PatientIdentifierType>();

		for (PatientIdentifier pi : identifiers) {
			if (pi.isVoided())
				continue;

			try {
				checkPatientIdentifier(pi);
			} catch ( BlankIdentifierException bie ) {
				patient.removeIdentifier(pi);
				throw bie;
			}
			
			// check if this is a required identifier
			for ( PatientIdentifierType requiredType : requiredTypes ) {
				if ( pi.getIdentifierType().equals(requiredType) ) {
					foundRequiredTypes.add(requiredType);
					requiredTypes.remove(requiredType);
					break;
				}
			}

			// TODO: check patient has at least one "sufficient" identifier

			// check this patient for duplicate identifiers
			if (identifiersUsed.contains(pi.getIdentifier())) {
				patient.removeIdentifier(pi);
				throw new DuplicateIdentifierException("This patient has two identical identifiers of type "
				                                               + pi.getIdentifierType()
				                                                   .getName()
				                                               + ": "
				                                               + pi.getIdentifier()
				                                               + ", deleting one of them",
				                                       pi);
			}
			
			else
				identifiersUsed.add(pi.getIdentifier());
		}

		if ( requiredTypes.size() > 0 ) {
			String missingNames = "";
			for ( PatientIdentifierType pit : requiredTypes ) {
				missingNames += (missingNames.length() > 0) ? ", "
				        + pit.getName() : pit.getName();
			}
			throw new MissingRequiredIdentifierException("Patient is missing the following required identifier(s): "
			        + missingNames);
		}
	}

	/**
	 * @see org.openmrs.api.PatientService#checkPatientIdentifier(org.openmrs.PatientIdentifier)
	 */
	public void checkPatientIdentifier(PatientIdentifier pi)
	        throws PatientIdentifierException {
		
		// skip and remove invalid/empty identifiers
		if (pi == null)
			throw new BlankIdentifierException("Identifier cannot be null or blank",
			                                   pi);
		else if (pi.getIdentifier() == null)
			throw new BlankIdentifierException("Identifier cannot be null or blank",
			                                   pi);
		else if (pi.getIdentifier().length() == 0)
			throw new BlankIdentifierException("Identifier cannot be null or blank",
			                                   pi);
		
		// do not do any more checks if this identifier is voided
		if (pi.isVoided() == false) {
			
			// check is already in use by another patient
			Patient p = identifierInUse(pi);
			if (p != null) {
				throw new IdentifierNotUniqueException("Identifier "
				        + pi.getIdentifier() + " already in use by patient #"
				        + p.getPatientId(), pi);
			}
	
			// also validate regular expression - if it exists
			PatientIdentifierType pit = pi.getIdentifierType();
			String identifier = pi.getIdentifier();
	
			String regExp = pit.getFormat();
			if ( regExp != null ) {
				if ( regExp.length() > 0 ) {
					// if this ID has a valid corresponding regular expression,
					// check against it
					log.debug("Trying to match " + identifier + " and "
					        + regExp);
					if ( !identifier.matches(regExp) ) {
						log.debug("The two DO NOT match");
						if ( pit.getFormatDescription() != null ) {
							if (pit.getFormatDescription().length() > 0)
								throw new InvalidIdentifierFormatException("Identifier ["
								                                                   + identifier
								                                                   + "] does not match required format: "
								                                                   + pit.getFormatDescription(),
								                                           pi);
							else
								throw new InvalidIdentifierFormatException("Identifier ["
								                                                   + identifier
								                                                   + "] does not match required format: "
								                                                   + pit.getFormat(),
								                                           pi);
						} else {
							throw new InvalidIdentifierFormatException("Identifier ["
							                                                   + identifier
							                                                   + "] does not match required format: "
							                                                   + pit.getFormat(),
							                                           pi);
						}
					} else {
						log.debug("The two match!!");
					}
				}
			}
	
			// validate identifier
			if(pit.hasValidator()){
				IdentifierValidator piv = getIdentifierValidator(pit.getValidator());
				try{
					if(!piv.isValid(identifier))
						throw new InvalidCheckDigitException("Invalid check digit for identifier: " + identifier, pi);
				}catch(UnallowedIdentifierException e){
					throw new InvalidCheckDigitException("Identifier " + identifier +" is not appropriate for validation scheme " + piv.getName() + ".", pi);
				}
			}
		}
	}
	
	/**
	 * checks if the given PatientIdentifer is in use by any other patient
	 * 
	 * @param pi PatientIdentifier
	 * @return true/false whether or not this PatientIdentifier is in use
	 * @deprecated use getPatientByIdentifier(String) instead
	 */
	private Patient identifierInUse(PatientIdentifier pi) {
		return identifierInUse(pi.getIdentifier(),
		                       pi.getIdentifierType(),
		                       pi.getPatient());
	}
	
	/**
	 * @see org.openmrs.api.PatientService#identifierInUse(java.lang.String,
	 *      org.openmrs.PatientIdentifierType, org.openmrs.Patient)
	 * @deprecated use getPatientByIdentifier(String) instead
	 */
	public Patient identifierInUse(String identifier,
	        PatientIdentifierType type, Patient ignorePatient) {
		
		// get all patients with this identifier
		List<PatientIdentifierType> types = new Vector<PatientIdentifierType>();
		types.add(type);
		List<Patient> patients = getPatients(null, identifier, types);
		
		// ignore this patient
		patients.remove(ignorePatient);
		
		if (patients.size() > 0)
			return patients.get(0);
		else
		return null; 
	}

	/**
	 * @deprecated replaced by @deprecated replaced by {@link #getPatients(String, String, List, String)}
	 * @see org.openmrs.api.PatientService#getPatientsByIdentifier(java.lang.String, boolean)
	 */
	public List<Patient> getPatientsByIdentifier(String identifier,
	        boolean includeVoided) throws APIException {
		
		if (includeVoided == true)
			throw new APIException("Searching on voided patients is no longer allowed");
		
		return getPatients(null, identifier, null);
	}
	
	/**
	 * @deprecated replaced by @deprecated replaced by {@link #getPatients(String, String, List, String)}
	 * @see org.openmrs.api.PatientService#getPatientsByIdentifierPattern(java.lang.String, boolean)
	 */
	public List<Patient> getPatientsByIdentifierPattern(String identifier,
	        boolean includeVoided) throws APIException {
		
		if (includeVoided == true)
			throw new APIException("Searching on voided patients is no longer allowed");
		
		return getPatients(null, identifier, null);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#getPatientsByName(java.lang.String)
	 * @deprecated replaced by {@link #getPatients(String, String, List, String)}
	 */
	public List<Patient> getPatientsByName(String name) throws APIException {
		return getPatients(name, null, null);
	}
	
	/**
	 * @deprecated replaced by {@link #getPatients(String, String, List, String)}
	 */
	public List<Patient> getPatientsByName(String name, boolean includeVoided)
	        throws APIException {
		
		if (includeVoided == true)
			throw new APIException("Searching on voided patients is no longer allowed");
		
		return getPatients(name, null, null);
	}
	
	/**
	 * @see org.openmrs.api.PatientService#voidPatient(org.openmrs.Patient, java.lang.String)
	 */
	public Patient voidPatient(Patient patient, String reason) throws APIException {
		
		// TODO should we move voided attributes from the patient up to the person?
		// voiding a patient portion of a person and keeping the user portion doesn't
		// seem like a necessary goal
		
		if (patient == null)
			return null;

		User voidedBy = Context.getAuthenticatedUser();
		Date voidDate = new Date();
		
		if (patient.getIdentifiers() != null) 
			for (PatientIdentifier pi : patient.getIdentifiers()) {
				if (!pi.isVoided()) {
					pi.setVoided(true);
					pi.setVoidReason(reason);
					pi.setDateVoided(voidDate);
					pi.setVoidedBy(voidedBy);
				}
			}
		
		patient.setVoided(true);
		patient.setVoidedBy(voidedBy);
		patient.setDateVoided(voidDate);
		patient.setVoidReason(reason);
		
		return savePatient(patient);
	}

	/**
	 * @see org.openmrs.api.PatientService#unvoidPatient(org.openmrs.Patient)
	 */
	public Patient unvoidPatient(Patient patient) throws APIException {
		if (patient == null)
			return null;
		
		String voidReason = patient.getVoidReason();
		if (voidReason == null)
			voidReason = "";
		
		for (PatientIdentifier pi : patient.getIdentifiers()) {
			if (voidReason.equals(pi.getDateVoided())) {
				pi.setVoided(false);
				pi.setVoidReason(null);
				pi.setDateVoided(null);
				pi.setVoidedBy(null);
			}
		}
		patient.setVoided(false);
		patient.setVoidedBy(null);
		patient.setDateVoided(null);
		patient.setVoidReason(null);
		
		return savePatient(patient);
	}
	
	/**
	 * @see #voidPatient(org.openmrs.Patient,java.lang.String)
	 * @deprecated replaced by {@link #purgePatient(Patient)}
	 */
	public void deletePatient(Patient patient) throws APIException {
		purgePatient(patient);
	}

	/**
	 * @see org.openmrs.api.PatientService#purgePatient(org.openmrs.Patient)
	 */
	public void purgePatient(Patient patient) throws APIException {
		dao.deletePatient(patient);
	}
	
	
	// patient identifier section
	
	/**
     * @see org.openmrs.api.PatientService#getPatientIdentifiers(java.lang.String, java.util.List, java.util.List, java.util.List, java.lang.Boolean)
	 */
    public List<PatientIdentifier> getPatientIdentifiers(String identifier,
            List<PatientIdentifierType> patientIdentifierTypes,
            List<Location> locations, List<Patient> patients,
            Boolean isPreferred)
            throws APIException {
	    
    	if (patientIdentifierTypes == null)
    		patientIdentifierTypes = new Vector<PatientIdentifierType>();
    	
    	if (locations == null)
    		locations = new Vector<Location>();
    	
    	if (patients == null)
    		patients = new Vector<Patient>();
		
	    return dao.getPatientIdentifiers(identifier, patientIdentifierTypes, locations, patients, isPreferred);
	}
	
	/**
	 * @deprecated replaced by {@link #getPatientIdentifiers(String, List, List, List, Boolean)}
	 * @see org.openmrs.api.PatientService#getPatientIdentifiers(org.openmrs.PatientIdentifierType)
	 */
	public List<PatientIdentifier> getPatientIdentifiers(
	        PatientIdentifierType pit) throws APIException {
		List<PatientIdentifierType> types = new Vector<PatientIdentifierType>();
		types.add(pit);
		return getPatientIdentifiers(null, types, null, null, null);
	}
		
	/**
	 * @deprecated replaced by {@link #getPatientIdentifiers(String, List, List, List, Boolean, boolean)}
	 * @see org.openmrs.api.PatientService#getPatientIdentifiers(java.lang.String, org.openmrs.PatientIdentifierType)
	 */
	public List<PatientIdentifier> getPatientIdentifiers(String identifier,
	        PatientIdentifierType pit) throws APIException {
		List<PatientIdentifierType> types = new Vector<PatientIdentifierType>();
		types.add(pit);
		return getPatientIdentifiers(identifier, types, null, null, null);
	}
	
	/**
	 * @deprecated replaced by {@link #getPatientIdentifiers(String, List, List, List, Boolean, boolean)}
	 * @see org.openmrs.api.PatientService#getPatientIdentifiers(java.lang.String,
	 *      org.openmrs.PatientIdentifierType, boolean)
	 */
	public List<PatientIdentifier> getPatientIdentifiers(String identifier,
	        PatientIdentifierType patientIdentifierType, boolean includeVoided)
	        throws APIException {
		
		if (includeVoided == true)
			throw new APIException("Searching on voided identifiers is no longer allowed");
		
		List<PatientIdentifierType> types = new Vector<PatientIdentifierType>();
		types.add(patientIdentifierType);
		return getPatientIdentifiers(identifier, types, null, null, null);
	}

	/**
	 * @deprecated patient identifiers should not be updated directly; rather,
	 *             after changing patient identifiers, use
	 *             {@link #savePatient(Patient)} to save changes to the database
	 */
	public void updatePatientIdentifier(PatientIdentifier pi)
	        throws APIException {
		// this method allows you change only the Identifier type, so let's do
		// that and then do a full ID check
		Patient p = pi.getPatient();
		Set<PatientIdentifier> identifiers = p.getIdentifiers();
		for ( PatientIdentifier identifier : identifiers ) {
			if ( identifier.getIdentifier().equals(pi.getIdentifier())
			        && identifier.getLocation().equals(pi.getLocation())) {
				identifier.setIdentifierType(pi.getIdentifierType());
				break;
			}
		}
		
		savePatient(p);
	}
	
	// end patient identifier section
	
	
	// patient identifier _type_ section

	/**
	 * TODO: Add changedBy and DateChanged columns to table
	 * patient_identifier_type
	 * 
	 * @see org.openmrs.api.PatientService#savePatientIdentifierType(org.openmrs.PatientIdentifierType)
	 */
	public PatientIdentifierType savePatientIdentifierType(
	        PatientIdentifierType patientIdentifierType) throws APIException {
		Date now = new Date();
		if (patientIdentifierType.getDateCreated() == null)
			patientIdentifierType.setDateCreated(now);
		if (patientIdentifierType.getCreator() == null)
			patientIdentifierType.setCreator(Context.getAuthenticatedUser());
		// if (patientIdentifierType.getPatientIdentifierTypeId() != null) {
		// patientIdentifierType.setChangedBy(Context.getAuthenticatedUser());
		// patientIdentifierType.setDateChanged(now);
		// }
		return dao.savePatientIdentifierType(patientIdentifierType);
	}
		
	/**
	 * @deprecated replaced by {@link #getAllPatientIdentifierTypes()}
	 * @see org.openmrs.api.PatientService#getPatientIdentifierTypes()
	 * @see org.openmrs.api.PatientService#getAllPatientIdentifierTypes()
	 */
	public List<PatientIdentifierType> getPatientIdentifierTypes()
	        throws APIException {
		return getAllPatientIdentifierTypes();
	}

	/**
	 * @see org.openmrs.api.PatientService#getAllPatientIdentifierTypes()
	 */
	public List<PatientIdentifierType> getAllPatientIdentifierTypes()
	        throws APIException {
		return getAllPatientIdentifierTypes(false);
	}

	/**
	 * @see org.openmrs.api.PatientService#getAllPatientIdentifierTypes(boolean)
	 */
	public List<PatientIdentifierType> getAllPatientIdentifierTypes(boolean includeRetired)
	        throws APIException {
		return dao.getAllPatientIdentifierTypes(includeRetired);
	}
		
	/**
     * @see org.openmrs.api.PatientService#getPatientIdentifierTypes(java.lang.String, java.lang.String, java.lang.Boolean, java.lang.Boolean)
     */
    public List<PatientIdentifierType> getPatientIdentifierTypes(String name,
            String format, Boolean required, Boolean hasCheckDigit)
            throws APIException {
	    	return dao.getPatientIdentifierTypes(name, format, required, hasCheckDigit);
    }

	/**
	 * @see org.openmrs.api.PatientService#getPatientIdentifierType(java.lang.Integer)
	 */
	public PatientIdentifierType getPatientIdentifierType(
	        Integer patientIdentifierTypeId) throws APIException {
		return dao.getPatientIdentifierType(patientIdentifierTypeId);
				}

	/**
	 * @see org.openmrs.api.PatientService#getPatientIdentifierType(java.lang.String)
	 * @deprecated
	 */
	public PatientIdentifierType getPatientIdentifierType(String name)
	        throws APIException {
		return getPatientIdentifierTypeByName(name);
			}

	/**
	 * @see org.openmrs.api.PatientService#getPatientIdentifierTypeByName(java.lang.String)
	 */
	public PatientIdentifierType getPatientIdentifierTypeByName(String name)
	        throws APIException {
		List<PatientIdentifierType> types = getPatientIdentifierTypes(name, null, null, null);
		
		if (types.size() > 0)
			return types.get(0);
		
		return null;
	}

	/**
	 * @see org.openmrs.api.PatientService#retirePatientIdentifierType(org.openmrs.PatientIdentifierType, String)
	 */
	public PatientIdentifierType retirePatientIdentifierType(
	        PatientIdentifierType patientIdentifierType, String reason) throws APIException {
		if (reason == null || reason.length() < 1)
			throw new APIException("A reason is required when retiring an identifier type");
		
		patientIdentifierType.setRetired(true);
		patientIdentifierType.setRetiredBy(Context.getAuthenticatedUser());
		patientIdentifierType.setDateRetired(new Date());
		patientIdentifierType.setRetireReason(reason);
		return savePatientIdentifierType(patientIdentifierType);
	}

	/**
	 * @see org.openmrs.api.PatientService#unretirePatientIdentifierType(org.openmrs.PatientIdentifierType)
	 */
	public PatientIdentifierType unretirePatientIdentifierType(
	        PatientIdentifierType patientIdentifierType) throws APIException {
		patientIdentifierType.setRetired(false);
		patientIdentifierType.setRetiredBy(null);
		patientIdentifierType.setDateRetired(null);
		patientIdentifierType.setRetireReason(null);
		return savePatientIdentifierType(patientIdentifierType);
	}
		
	/**
	 * @see org.openmrs.api.PatientService#purgePatientIdentifierType(org.openmrs.PatientIdentifierType)
	 */
	public void purgePatientIdentifierType(
	        PatientIdentifierType patientIdentifierType) throws APIException {
		dao.deletePatientIdentifierType(patientIdentifierType);
	}
	
	// end patient identifier _type_ section
	
	
	// tribe section

	/**
	 * @deprecated tribe will be moved to patient attribute
	 * @see org.openmrs.api.PatientService#getTribe(java.lang.Integer)
	 */
	public Tribe getTribe(Integer tribeId) throws APIException {
		return dao.getTribe(tribeId);
	}
	
	/**
	 * @deprecated tribe will be moved to patient attributes
	 * @see org.openmrs.api.PatientService#getTribes()
	 */
	public List<Tribe> getTribes() throws APIException {
		return dao.getTribes();
	}
		
	/**
	 * @deprecated tribe will be moved to patient attributes
	 * @see org.openmrs.api.PatientService#findTribes(java.lang.String)
	 */
	public List<Tribe> findTribes(String search) throws APIException {
		return dao.findTribes(search);
	}
	
	// end tribe section
	

	/**
	 * @see org.openmrs.api.PatientService#findPatients(java.lang.String, boolean)
	 */
	public List<Patient> findPatients(String query, boolean includeVoided) throws APIException {
		if (includeVoided == true)
			throw new APIException("Searching on voided patients is no longer allowed");
		
		return getPatients(query);
	}
		
	/**
	 * @see org.openmrs.api.PatientService#findPatients(java.lang.String)
	 */
	public List<Patient> getPatients(String query) throws APIException {
		List<Patient> patients = new Vector<Patient>();
		
		// TODO create a global property that users can change for this
		//query must be more than 2 characters
		if (query.length() < 3)
			return patients;
		
		// if there is a number in the query string
		if (query.matches(".*\\d+.*")) {
			log.debug("[Identifier search] Query: " + query);
			return getPatients(null, query, null);
		} else {
			// there is no number in the string, search on name
			return getPatients(query, null, null);
		}
		}
	
	/**
	 * @see org.openmrs.api.PatientService#findPatient(org.openmrs.Patient)
	 * @see #getPatientByExample(Patient)
	 * @deprecated
	 */
	public Patient findPatient(Patient patientToMatch) throws APIException {
		return getPatientByExample(patientToMatch);
	}
	
	/**
	 * This default implementation simply looks at the OpenMRS internal id
	 * (patient_id). If the id is null, assume this patient isn't found. If the
	 * patient_id is not null, try and find that id in the database
	 * 
	 * @see org.openmrs.api.PatientService#getPatientByExample(org.openmrs.Patient)
	 */
	public Patient getPatientByExample(Patient patientToMatch) throws APIException {
		if (patientToMatch == null || patientToMatch.getPatientId() == null)
			return null;
		
		return getPatient(patientToMatch.getPatientId());
	}

	/**
	 * @deprecated use {@link #getDuplicatePatientsByAttributes(List)}
	 */
	public List<Patient> findDuplicatePatients(Set<String> attributes) throws APIException {
		List<String> attributesAsList = new Vector<String>();
		attributesAsList.addAll(attributes);
		
		return getDuplicatePatientsByAttributes(attributesAsList);
	}
	
	/**
     * @see org.openmrs.api.PatientService#getDuplicatePatientsByAttributes(java.util.List)
	 */
    public List<Patient> getDuplicatePatientsByAttributes(
            List<String> attributes) throws APIException {
    	
    	if (attributes == null || attributes.size() < 1) {
    		throw new APIException("There must be at least one attribute supplied to search on");
    	}
		
	    return dao.getDuplicatePatientsByAttributes(attributes);
	}
	
	/**
	 * 1) Moves object (encounters/obs) pointing to <code>nonPreferred</code>
	 * to <code>preferred</code> 2) Copies data
	 * (gender/birthdate/names/ids/etc) from <code>nonPreferred</code> to
	 * <code>preferred</code> iff the data is missing or null in
	 * <code>preferred</code> 3) <code>notPreferred</code> is marked as
	 * voided
	 * 
	 * @param preferred
	 * @param notPreferred
	 * @throws APIException
	 */
	public void mergePatients(Patient preferred, Patient notPreferred)
	        throws APIException {
		log.debug("Merging patients: (preferred)" + preferred.getPatientId()
		        + ", (notPreferred) " + notPreferred.getPatientId());
		
		// change all encounters. This will cascade to obs and orders contained in those encounters
		// TODO: this should be a copy, not a move
		EncounterService es = Context.getEncounterService();
		for (Encounter e : es.getEncounters(notPreferred)){
			e.setPatient(preferred);
			log.debug("Merging encounter " + e.getEncounterId() + " to "
			        + preferred.getPatientId());
			es.saveEncounter(e);
		}
		
		// move all identifiers
		for (PatientIdentifier pi : notPreferred.getIdentifiers()) {
			PatientIdentifier tmpIdentifier = new PatientIdentifier();
			tmpIdentifier.setIdentifier(pi.getIdentifier());
			tmpIdentifier.setIdentifierType(null); // don't compare identifier
			// types.
			tmpIdentifier.setLocation(pi.getLocation());
			tmpIdentifier.setPatient(preferred);
			boolean found = false;
			for (PatientIdentifier preferredIdentifier : preferred.getIdentifiers()) {
				if (preferredIdentifier.getIdentifier() != null
				        && preferredIdentifier.getIdentifier()
				                              .equals(tmpIdentifier.getIdentifier())
				        && preferredIdentifier.getIdentifierType() != null
				        && preferredIdentifier.getIdentifierType()
				                              .equals(tmpIdentifier.getIdentifierType()))
						found = true;
			}
			if (!found) {
				tmpIdentifier.setIdentifierType(pi.getIdentifierType());
				tmpIdentifier.setCreator(Context.getAuthenticatedUser());
				tmpIdentifier.setDateCreated(new Date());
				tmpIdentifier.setVoided(false);
				tmpIdentifier.setVoidedBy(null);
				tmpIdentifier.setVoidReason(null);
				// we don't want to change the preferred identifier of the preferred patient
				tmpIdentifier.setPreferred(false);
				preferred.addIdentifier(tmpIdentifier);
				log.debug("Merging identifier " + tmpIdentifier.getIdentifier()
				        + " to " + preferred.getPatientId());
			}
		}
		
		// move all names
		for (PersonName newName : notPreferred.getNames()) {
			boolean containsName = false;
			for (PersonName currentName : preferred.getNames()) {
				String given = newName.getGivenName();
				String middle = newName.getMiddleName();
				String family = newName.getFamilyName();
				
				if ((given != null && given.equals(currentName.getGivenName()))
				        && (middle != null && middle.equals(currentName.getMiddleName()))
				        && (family != null && family.equals(currentName.getFamilyName()))) {
					containsName = true;
				}
			}
			if (!containsName) {
				PersonName tmpName = PersonName.newInstance(newName);
				tmpName.setPersonNameId(null);
				tmpName.setVoided(false);
				tmpName.setVoidedBy(null);
				tmpName.setVoidReason(null);
				// we don't want to change the preferred name of the preferred patient
				tmpName.setPreferred(false);
				preferred.addName(tmpName);
				log.debug("Merging name " + newName.getGivenName() + " to "
				        + preferred.getPatientId());
			}
		}
		
		// move all addresses
		for (PersonAddress newAddress : notPreferred.getAddresses()) {
			boolean containsAddress = false;
			for (PersonAddress currentAddress : preferred.getAddresses()) {
				String address1 = currentAddress.getAddress1();
				String address2 = currentAddress.getAddress2();
				String cityVillage = currentAddress.getCityVillage();
				
				if ((address1 != null && address1.equals(newAddress.getAddress1()))
				        || (address2 != null && address2.equals(newAddress.getAddress2()))
				        || (cityVillage != null && cityVillage.equals(newAddress.getCityVillage()))) {
					containsAddress = true;
				}
			}
			if (!containsAddress) {
				PersonAddress tmpAddress = (PersonAddress)newAddress.clone();
				tmpAddress.setPersonAddressId(null);
				tmpAddress.setVoided(false);
				tmpAddress.setVoidedBy(null);
				tmpAddress.setVoidReason(null);
				preferred.addAddress(tmpAddress);
				log.debug("Merging address " + newAddress.getPersonAddressId()
				        + " to " + preferred.getPatientId());
			}
		}
	
		// copy all program enrollments
		ProgramWorkflowService programService = Context.getProgramWorkflowService();
		for (PatientProgram pp : programService.getPatientPrograms(notPreferred)) {
			if (!pp.getVoided()) {
				PatientProgram enroll = pp.copy();
				enroll.setPatient(preferred);
				log.debug("Copying patientProgram " + pp.getPatientProgramId() + " to " + preferred.getPatientId());
				programService.createPatientProgram(enroll);
			}
		}
		
		// copy all relationships
		PersonService personService = Context.getPersonService();
		for (Relationship rel : personService.getRelationships(notPreferred)) {
			if (!rel.isVoided()) {
				Relationship tmpRel = rel.copy();
				if (tmpRel.getPersonA().equals(notPreferred))
					tmpRel.setPersonA(preferred);
				if (tmpRel.getPersonB().equals(notPreferred))
					tmpRel.setPersonB(preferred);
				log.debug("Copying relationship " + rel.getRelationshipId() + " to " + preferred.getPatientId());
				personService.createRelationship(tmpRel);
			}
		}
		
		// move all obs that weren't contained in encounters
		// TODO: this should be a copy, not a move
		ObsService obsService = Context.getObsService();
		for (Obs obs : obsService.getObservations(notPreferred, false)) {
			if (obs.getEncounter() == null && !obs.isVoided()) {
				obs.setPerson(preferred);
				obsService.updateObs(obs);
			}
		}
		
		// copy all orders that weren't contained in encounters
		OrderService os = Context.getOrderService();
		for (Order o : os.getOrdersByPatient(notPreferred)) {
			if (o.getEncounter() == null && !o.getVoided()) {
				Order tmpOrder = o.copy();
				tmpOrder.setPatient(preferred);
				os.createOrder(tmpOrder);
			}
		}
		
		// copy person attributes
		for (PersonAttribute attr : notPreferred.getAttributes()) {
			if (!attr.isVoided()) {
				PersonAttribute tmpAttr = attr.copy();
				tmpAttr.setPerson(null);
				preferred.addAttribute(tmpAttr);
			}
		}
		
		// move all other patient info
		
		if (!"M".equals(preferred.getGender())
		        && !"F".equals(preferred.getGender()))
			preferred.setGender(notPreferred.getGender());
		
		/* 
		 * if (preferred.getRace() == null || preferred.getRace().equals(""))
		 * preferred.setRace(notPreferred.getRace());
		*/
		
		if (preferred.getBirthdate() == null
		        || preferred.getBirthdate().equals("")
		        || (preferred.getBirthdateEstimated() && !notPreferred.getBirthdateEstimated())) {
			preferred.setBirthdate(notPreferred.getBirthdate());
			preferred.setBirthdateEstimated(notPreferred.getBirthdateEstimated());
		}
		
		if (preferred.getTribe() == null)
			preferred.setTribe(notPreferred.getTribe());
		
		/*
		 * if (preferred.getBirthplace() == null ||
		 * preferred.getBirthplace().equals(""))
		 * preferred.setBirthplace(notPreferred.getBirthplace());
		 * 
		 * if (preferred.getCitizenship() == null ||
		 * preferred.getCitizenship().equals(""))
		 * preferred.setCitizenship(notPreferred.getCitizenship());
		 * 
		 * if (preferred.getMothersName() == null ||
		 * preferred.getMothersName().equals(""))
		 * preferred.setMothersName(notPreferred.getMothersName());
		 * 
		 * if (preferred.getCivilStatus() == null)
		 * preferred.setCivilStatus(notPreferred.getCivilStatus());
		*/
		
		if (preferred.getDeathDate() == null
		        || preferred.getDeathDate().equals(""))
			preferred.setDeathDate(notPreferred.getDeathDate());
		
		if (preferred.getCauseOfDeath() == null
		        || preferred.getCauseOfDeath().equals(""))
			preferred.setCauseOfDeath(notPreferred.getCauseOfDeath());
		
		/*
		 * if (preferred.getHealthDistrict() == null ||
		 * preferred.getHealthDistrict().equals(""))
		 * preferred.setHealthDistrict(notPreferred.getHealthDistrict());
		 * 
		 * if (preferred.getHealthCenter() == null)
		 * preferred.setHealthCenter(notPreferred.getHealthCenter());
		*/
		
		// void the non preferred patient
		voidPatient(notPreferred, "Merged with patient #"
		        + preferred.getPatientId());
		
		// Save the newly update preferred patient
		// This must be called _after_ voiding the nonPreferred patient so that
		//  a "Duplicate Identifier" error doesn't pop up.
		savePatient(preferred);
		
	}
	
	/**
	 * Iterates over Names/Addresses/Identifiers to set dateCreated and creator
	 * properties if needed
	 * 
	 * @param patient
	 */
	private void setCollectionProperties(Patient patient) {
		// set the properties on the collections for the generic person object
		Context.getPersonService().setCollectionProperties(patient);
		
		User currentUser = Context.getAuthenticatedUser();
		Date currentTime = new Date();
		
		// patient creator
		if (patient.getCreator() == null)
			patient.setCreator(currentUser);
		if (patient.getDateCreated() == null)
			patient.setDateCreated(currentTime);
		
		// patient changer
		if (patient.getPatientId() != null) {
			patient.setChangedBy(currentUser);
			patient.setDateChanged(currentTime);
		}
		
		// identifier collection
		if (patient.getIdentifiers() != null) {
			for (PatientIdentifier pIdentifier : patient.getIdentifiers()) {
				// set the creator and date created if not set yet
				if (pIdentifier.getDateCreated() == null)
					pIdentifier.setDateCreated(currentTime);
				if (pIdentifier.getCreator() == null)
					pIdentifier.setCreator(currentUser);
				
				// make sure the identifier is associated with the current
				// patient
				if (pIdentifier.getPatient() == null)
					pIdentifier.setPatient(patient);
				
				// if the user has marked this identifier as voided, make sure
				// the other void attrs are filled in
				if (pIdentifier.isVoided()) {
					if (pIdentifier.getVoidedBy() == null)
						pIdentifier.setVoidedBy(currentUser);
					if (pIdentifier.getDateVoided() == null)
						pIdentifier.setDateVoided(currentTime);
				}
			}
		}
	}

	/**
	 * This is the way to establish that a patient has left the care center.
	 * This API call is responsible for: 1) Closing workflow statuses 2)
	 * Terminating programs 3) Discontinuing orders 4) Flagging patient table
	 * (if applicable) 5) Creating any relevant observations about the patient
	 * 
	 * @param patient - the patient who has exited care
	 * @param dateExited - the declared date/time of the patient's exit
	 * @param reasonForExit - the concept that corresponds with why the patient
	 *        has been declared as exited
	 * @throws APIException
	 */
	public void exitFromCare(Patient patient, Date dateExited,
	        Concept reasonForExit) throws APIException {
			
			if ( patient == null )
					throw new APIException("Attempting to exit from care an invalid patient. Cannot proceed");
			if ( dateExited == null ) 
					throw new APIException("Must supply a valid dateExited when indicating that a patient has left care");
			if ( reasonForExit == null ) 
					throw new APIException("Must supply a valid reasonForExit (even if 'Unknown') when indicating that a patient has left care");
		
		
		// need to create an observation to represent this (otherwise how
		// will we know?)
		saveReasonForExitObs(patient, dateExited, reasonForExit);

		// need to terminate any applicable programs
		Context.getProgramWorkflowService()
		       .triggerStateConversion(patient, reasonForExit, dateExited);

		// need to discontinue any open orders for this patient
		OrderUtil.discontinueAllOrders(patient,
                                       reasonForExit,
                                       dateExited);
		}

	/**
	 * Auto generated method comment
	 * 
	 * @param patient
	 * @param exitDate
	 * @param cause
	 */
	// TODO: Patients should actually be allowed to exit multiple times 
	private void saveReasonForExitObs(Patient patient, Date exitDate,
	        Concept cause) throws APIException {
		
		if (patient == null)
			throw new APIException("Patient supplied to method is null");
		if (exitDate == null)
			throw new APIException("Exit date supplied to method is null");
		if (cause == null)
			throw new APIException("Cause supplied to method is null");
			
		// need to make sure there is an Obs that represents the patient's
		// exit
			log.debug("Patient is exiting, so let's make sure there's an Obs for it");

		String codProp = Context.getAdministrationService()
		                        .getGlobalProperty("concept.reasonExitedCare");
		Concept reasonForExit = Context.getConceptService()
		                               .getConceptByIdOrName(codProp);

			if ( reasonForExit != null ) {
			Set<Obs> obssExit = Context.getObsService()
			                           .getObservations(patient,
			                                            reasonForExit,
			                                            false);
				if ( obssExit != null ) {
					if ( obssExit.size() > 1 ) {
					log.error("Multiple reasons for exit ("
					        + obssExit.size() + ")?  Shouldn't be...");
					} else {
						Obs obsExit = null;
						if ( obssExit.size() == 1 ) {
							// already has a reason for exit - let's edit it.
							log.debug("Already has a reason for exit, so changing it");
							
							obsExit = obssExit.iterator().next();
							
						} else {
							// no reason for exit obs yet, so let's make one
							log.debug("No reason for exit yet, let's create one.");
							
							obsExit = new Obs();
							obsExit.setPerson(patient);
							obsExit.setConcept(reasonForExit);
							Location loc = null; //patient.getHealthCenter();
						if (loc == null)
							loc = Context.getEncounterService()
							             .getLocationByName("Unknown Location");
						if (loc == null)
							loc = Context.getLocationService()
							             .getLocation(new Integer(1));
						if (loc != null)
							obsExit.setLocation(loc);
						else
							log.error("Could not find a suitable location for which to create this new Obs");
						}
						
						if ( obsExit != null ) {
						// put the right concept and (maybe) text in this
						// obs
							obsExit.setValueCoded(cause);
							obsExit.setObsDatetime(exitDate);
						Context.getObsService().saveObs(obsExit, null);
						}
					}
				}
			} else {
				log.debug("Reason for exit is null - should not have gotten here without throwing an error on the form.");
			}
		}
		
	/**
	 * This is the way to establish that a patient has died. In addition to
	 * exiting the patient from care (see above), this method will also set the
	 * appropriate patient characteristics to indicate that they have died, when
	 * they died, etc.
	 * 
	 * @param patient - the patient who has died
	 * @param dateDied - the declared date/time of the patient's death
	 * @param causeOfDeath - the concept that corresponds with the reason the
	 *        patient died
	 * @param otherReason - in case the causeOfDeath is 'other', a place to
	 *        store more info
	 * @throws APIException
	 */
	public void processDeath(Patient patient, Date dateDied,
	        Concept causeOfDeath, String otherReason) throws APIException {
		//SQLStateConverter s = null;
		
		if ( patient != null && dateDied != null && causeOfDeath != null ) {
			// set appropriate patient characteristics
			patient.setDead(true);
			patient.setDeathDate(dateDied);
			patient.setCauseOfDeath(causeOfDeath);
			this.updatePatient(patient);
			saveCauseOfDeathObs(patient, dateDied, causeOfDeath, otherReason);
			
			// exit from program
			// first, need to get Concept for "Patient Died"
			String strPatientDied = Context.getAdministrationService()
			                               .getGlobalProperty("concept.patientDied");
			Concept conceptPatientDied = Context.getConceptService()
			                                    .getConceptByIdOrName(strPatientDied);
			
			if (conceptPatientDied == null)
				log.debug("ConceptPatientDied is null");
			exitFromCare(patient, dateDied, conceptPatientDied);

		} else {
			if ( patient == null )
					throw new APIException("Attempting to set an invalid patient's status to 'dead'");
			if ( dateDied == null ) 
					throw new APIException("Must supply a valid dateDied when indicating that a patient has died");
			if ( causeOfDeath == null ) 
					throw new APIException("Must supply a valid causeOfDeath (even if 'Unknown') when indicating that a patient has died");
		}
	}

	/**
	 * @see org.openmrs.api.PatientService#saveCauseOfDeathObs(org.openmrs.Patient, java.util.Date, org.openmrs.Concept, java.lang.String)
	 */
	public void saveCauseOfDeathObs(Patient patient, Date deathDate,
	        Concept cause, String otherReason) throws APIException {
		
		if (patient == null)
			throw new APIException("Patient supplied to method is null");
		if (deathDate == null)
			throw new APIException("Death date supplied to method is null");
		if (cause == null)
			throw new APIException("Cause supplied to method is null");
		
		if (!patient.getDead())
			patient.setDead(true);
			patient.setDeathDate(deathDate);
			patient.setCauseOfDeath(cause);
			
			log.debug("Patient is dead, so let's make sure there's an Obs for it");
		// need to make sure there is an Obs that represents the patient's
		// cause of death, if applicable

		String codProp = Context.getAdministrationService()
		                        .getGlobalProperty("concept.causeOfDeath");
		Concept causeOfDeath = Context.getConceptService()
		                              .getConceptByIdOrName(codProp);

			if ( causeOfDeath != null ) {
			Set<Obs> obssDeath = Context.getObsService()
			                            .getObservations(patient,
			                                             causeOfDeath,
			                                             false);
				if ( obssDeath != null ) {
					if ( obssDeath.size() > 1 ) {
					log.error("Multiple causes of death ("
					        + obssDeath.size() + ")?  Shouldn't be...");
					} else {
						Obs obsDeath = null;
						if ( obssDeath.size() == 1 ) {
							// already has a cause of death - let's edit it.
							log.debug("Already has a cause of death, so changing it");
							
							obsDeath = obssDeath.iterator().next();
							
						} else {
							// no cause of death obs yet, so let's make one
							log.debug("No cause of death yet, let's create one.");
							
							obsDeath = new Obs();
							obsDeath.setPerson(patient);
							obsDeath.setConcept(causeOfDeath);
							Location loc = null; //patient.getHealthCenter();
						if (loc == null)
							loc = Context.getEncounterService()
							             .getLocationByName("Unknown Location");
						if (loc == null)
							loc = Context.getLocationService()
							             .getLocation(new Integer(1));
						if (loc != null)
							obsDeath.setLocation(loc);
						else
							log.error("Could not find a suitable location for which to create this new Obs");
						}
						
						// put the right concept and (maybe) text in this obs
						Concept currCause = patient.getCauseOfDeath();
						if ( currCause == null ) {
							// set to NONE
							log.debug("Current cause is null, attempting to set to NONE");
						String noneConcept = Context.getAdministrationService()
						                            .getGlobalProperty("concept.none");
						currCause = Context.getConceptService()
						                   .getConceptByIdOrName(noneConcept);
						}
						
						if ( currCause != null ) {
							log.debug("Current cause is not null, setting to value_coded");
							obsDeath.setValueCoded(currCause);
							
							Date dateDeath = patient.getDeathDate();
						if (dateDeath == null)
							dateDeath = new Date();
							obsDeath.setObsDatetime(dateDeath);

						// check if this is an "other" concept - if so, then
						// we need to add value_text
						String otherConcept = Context.getAdministrationService()
						                             .getGlobalProperty("concept.otherNonCoded");
						Concept conceptOther = Context.getConceptService()
						                              .getConceptByIdOrName(otherConcept);
							if ( conceptOther != null ) {
								if ( conceptOther.equals(currCause) ) {
								// seems like this is an other concept -
								// let's try to get the "other" field info
								log.debug("Setting value_text as "
								        + otherReason);
									obsDeath.setValueText(otherReason);
								} else {
									log.debug("New concept is NOT the OTHER concept, so setting to blank");
									obsDeath.setValueText("");
								}
							} else {
								log.debug("Don't seem to know about an OTHER concept, so deleting value_text");
								obsDeath.setValueText("");
							}
							
						Context.getObsService().saveObs(obsDeath, null);
						} else {
							log.debug("Current cause is still null - aborting mission");
						}
					}
				}
			} else {
				log.debug("Cause of death is null - should not have gotten here without throwing an error on the form.");
			}
		}
	
	/**
     * @see org.openmrs.api.PatientService#getDefaultIdentifierValidator()
     */
    public IdentifierValidator getDefaultIdentifierValidator() {
	    String defaultPIV = Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_PATIENT_IDENTIFIER_VALIDATOR, "");
	    
	    try {
	        return identifierValidators.get(Class.forName(defaultPIV));
        } catch (ClassNotFoundException e) {
	        log.error("Global Property " + OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_PATIENT_IDENTIFIER_VALIDATOR +" not set to an actual class.", e);
	        return identifierValidators.get(LuhnIdentifierValidator.class);
        }
    }

	/**
     * @see org.openmrs.api.PatientService#getIdentifierValidator(java.lang.String)
     */
    public IdentifierValidator getIdentifierValidator(Class<IdentifierValidator> identifierValidator) {
	    return identifierValidators.get(identifierValidator);
    }

	public Map<Class<? extends IdentifierValidator>, IdentifierValidator> getIdentifierValidators() {
    	if(identifierValidators == null)
    		identifierValidators = new LinkedHashMap<Class<? extends IdentifierValidator>, IdentifierValidator>();
		return identifierValidators;
    }

	/**
	 * ADDs identifierValidators, doesn't replace them
	 * 
	 * @param identifierValidators
	 */
	public void setIdentifierValidators(
            Map<Class<? extends IdentifierValidator>, IdentifierValidator> identifierValidators) {
    	for(Map.Entry<Class<? extends IdentifierValidator>, IdentifierValidator> entry : identifierValidators.entrySet()){
    		getIdentifierValidators().put(entry.getKey(), entry.getValue());
    	}
    }

	/**
     * @see org.openmrs.api.PatientService#getAllIdentifierValidators()
     */
    public Collection<IdentifierValidator> getAllIdentifierValidators() {
	    return identifierValidators.values();
    }

	/**
     * @see org.openmrs.api.PatientService#getIdentifierValidator(java.lang.String)
     */
    public IdentifierValidator getIdentifierValidator(String pivClassName) {
	    try {
	        return getIdentifierValidator(((Class<IdentifierValidator>) Class.forName(pivClassName)));
        } catch (ClassNotFoundException e) {
	        log.error("Could not find patient identifier validator " + pivClassName, e);
	        return getDefaultIdentifierValidator();
        }
    }
}
