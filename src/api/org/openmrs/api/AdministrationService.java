package org.openmrs.api;

import org.openmrs.EncounterType;
import org.openmrs.FieldType;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.OrderType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Tribe;

/**
 * Admin-related services
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public interface AdministrationService {
	
	/**
	 * Create a new EncounterType
	 * @param EncounterType to create
	 * @return newly created EncounterType
	 * @throws APIException
	 */
	public EncounterType createEncounterType(EncounterType encounterType) throws APIException;

	/**
	 * Update an encounter type
	 * @param EncounterType to update
	 * @throws APIException
	 */
	public void updateEncounterType(EncounterType encounterType) throws APIException;

	/**
	 * Delete an encounter type
	 * @param EncounterType to delete
	 * @throws APIException
	 */
	public void deleteEncounterType(EncounterType encounterType) throws APIException;

	/**
	 * Create a new PatientIdentifierType
	 * @param PatientIdentifierType to create
	 * @return newly created PatientIdentifierType
	 * @throws APIException
	 */
	public PatientIdentifierType createPatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException;

	/**
	 * Update PatientIdentifierType
	 * @param PatientIdentifierType to update
	 * @throws APIException
	 */
	public void updatePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException;
	
	/**
	 * Delete PatientIdentifierType
	 * @param PatientIdentifierType to delete
	 * @throws APIException
	 */
	public void deletePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException;

	/**
	 * Create a new Tribe
	 * @param Tribe to create
	 * @return newly created Tribe
	 * @throws APIException
	 */
	public Tribe createTribe(Tribe tribe) throws APIException;

	/**
	 * Update Tribe
	 * @param Tribe to update
	 * @throws APIException
	 */
	public void updateTribe(Tribe tribe) throws APIException;

	/**
	 * Delete Tribe
	 * @param Tribe to delete
	 * @throws APIException
	 */
	public void deleteTribe(Tribe tribe) throws APIException;	
	
	/**
	 * Create a new OrderType
	 * @param OrderType to create
	 * @return newly created OrderType
	 * @throws APIException
	 */
	public OrderType createOrderType(OrderType orderType) throws APIException;

	/**
	 * Update OrderType
	 * @param OrderType to update
	 * @throws APIException
	 */
	public void updateOrderType(OrderType orderType) throws APIException;

	/**
	 * Delete OrderType
	 * @param OrderType to delete
	 * @throws APIException
	 */
	public void deleteOrderType(OrderType orderType) throws APIException;
	
	/**
	 * Create a new FieldType
	 * @param FieldType to create
	 * @return newly created FieldType
	 * @throws APIException
	 */
	public FieldType createFieldType(FieldType fieldType) throws APIException;

	/**
	 * Update FieldType
	 * @param FieldType to update
	 * @throws APIException
	 */
	public void updateFieldType(FieldType fieldType) throws APIException;

	/**
	 * Delete FieldType
	 * @param FieldType to delete
	 * @throws APIException
	 */
	public void deleteFieldType(FieldType fieldType) throws APIException;
	
	/**
	 * Create a new MimeType
	 * @param MimeType to create
	 * @return newly created MimeType
	 * @throws APIException
	 */
	public MimeType createMimeType(MimeType mimeType) throws APIException;

	/**
	 * Update MimeType
	 * @param MimeType to update
	 * @throws APIException
	 */
	public void updateMimeType(MimeType mimeType) throws APIException;

	/**
	 * Delete MimeType
	 * @param MimeType to delete
	 * @throws APIException
	 */
	public void deleteMimeType(MimeType mimeType) throws APIException;	

	/**
	 * Create a new Location
	 * @param Location to create
	 * @return newly created Location
	 * @throws APIException
	 */
	public Location createLocation(Location location) throws APIException;

	/**
	 * Update Location
	 * @param Location to update
	 * @throws APIException
	 */
	public void updateLocation(Location location) throws APIException;

	/**
	 * Delete Location
	 * @param Location to delete
	 * @throws APIException
	 */
	public void deleteLocation(Location location) throws APIException;	


}
