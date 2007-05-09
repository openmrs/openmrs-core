package org.openmrs.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.DuplicateIdentifierException;
import org.openmrs.api.IdentifierNotUniqueException;
import org.openmrs.api.InsufficientIdentifiersException;
import org.openmrs.api.InvalidCheckDigitException;
import org.openmrs.api.InvalidIdentifierFormatException;
import org.openmrs.api.MissingRequiredIdentifierException;
import org.openmrs.api.PatientIdentifierException;
import org.openmrs.api.context.Context;

public class PatientIdentifiersPortletController extends PortletController {

	protected void populateModel(HttpServletRequest request, Map model) {
		Map<String, Location> locationNameToId = new HashMap<String, Location>();
		
		List<Location> locations = Context.getEncounterService().getLocations();
		for (Location l : locations) {
			locationNameToId.put(l.getName(), l);
		}
		
		model.put("locationsByName", locationNameToId);
		
		// check to see if there is any negative info about patient's identifiers
		Map<PatientIdentifier, String> identifierErrors = new HashMap<PatientIdentifier, String>();
		String identifierError = "";
		
		Patient patient = (Patient)model.get("patient");
		for ( PatientIdentifier identifier : patient.getActiveIdentifiers() ) {
			try {
				Context.getPatientService().checkPatientIdentifier(identifier);
			} catch ( InvalidCheckDigitException icde ) {
				log.error("Caugh checkDigit error");
				identifierErrors.put(identifier, "PatientIdentifier.error.checkDigit");
			} catch ( IdentifierNotUniqueException inue ) {
				log.error("Caugh identifier not unique error");
				identifierErrors.put(identifier, "PatientIdentifier.error.notUnique");
			} catch ( InvalidIdentifierFormatException iife ) {
				log.error("Caugh format error");
				identifierErrors.put(identifier, "PatientIdentifier.error.formatInvalid");
			} catch ( PatientIdentifierException pie ) {
				log.error("Caugh general error");
				identifierErrors.put(identifier, "PatientIdentifier.error.general");
			}
		}
		
		try {
			Context.getPatientService().checkPatientIdentifiers(patient);
		} catch ( DuplicateIdentifierException die ) {
			log.error("Caugh duplicateIdentifier error");
			identifierError = "PatientIdentifier.error.duplicate";
		} catch ( MissingRequiredIdentifierException mrie ) {
			log.error("Caugh missingRequired error");
			identifierError = "PatientIdentifier.error.missingRequired";
		} catch ( InsufficientIdentifiersException iie ) {
			log.error("Caugh insufficient identifiers error");
			identifierError = "PatientIdentifier.error.insufficientIdentifiers";
		} catch ( PatientIdentifierException pie ) {
			log.error("Caugh general error for patient");
			identifierError = "PatientIdentifier.error.general";
		}
		
		model.put("identifierErrors", identifierErrors);
		model.put("identifierError", identifierError);
	}
	
}
