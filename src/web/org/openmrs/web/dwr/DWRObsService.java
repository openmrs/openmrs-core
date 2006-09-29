package org.openmrs.web.dwr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;

import uk.ltd.getahead.dwr.WebContextFactory;

public class DWRObsService {

	protected final Log log = LogFactory.getLog(getClass());
	
	
	public Vector getObservations(Integer encounterId) { 
				
		log.info("Get observations for encounter " + encounterId);
		Vector<Object> obsList = new Vector<Object>();

		HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();

		try {
			Encounter encounter = Context.getEncounterService().getEncounter(encounterId);
			
			Set<Obs> observations = Context.getObsService().getObservations(encounter);
			for (Obs obs : observations) {
				obsList.add(new ObsListItem(obs,request.getLocale()));
			}		
			
			
			
		} catch (Exception e) {
			log.error(e);
			obsList.add("Error while attempting to find obs - " + e.getMessage());
		}

		return obsList;
	}
	
	
	public void createObs(Integer patientId, Integer encounterId, Integer conceptId, String valueText, String obsDateStr) { 
		
		log.info("Create new observation ");
	
		try {
			
			Date obsDate = null;
			if ( obsDateStr != null ) {
				// TODO Standardize date input 
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
				try {
					obsDate = sdf.parse(obsDateStr);
				} catch (ParseException e) {
					log.error("Error parsing date ... " + obsDate);
					obsDate = new Date();
				}
			}				
			
			Patient patient = Context.getPatientService().getPatient(patientId);
			Concept concept = Context.getConceptService().getConcept(conceptId);
			Encounter encounter = Context.getEncounterService().getEncounter(encounterId);
			
			Obs obs = new Obs();
			obs.setPatient(patient);
			obs.setConcept(concept);
			obs.setEncounter(encounter);
			obs.setObsDatetime(obsDate);
			obs.setLocation(encounter.getLocation());
			obs.setCreator(Context.getAuthenticatedUser());
			obs.setDateCreated(new Date());
			
			// TODO Currently only handles numeric and text values ... need to expand to support all others
			String hl7DataType = concept.getDatatype().getHl7Abbreviation();
			if ("NM".equals(hl7DataType)) { 
				obs.setValueNumeric(Double.valueOf(valueText));
			} 
			else { 
				obs.setValueText(valueText);
			}
			
			// Create the observation
			Context.getObsService().createObs(obs);
			
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	
	
	public Vector findObs(String phrase, boolean includeVoided) {
		
		// List to return
		// Object type gives ability to return error strings
		Vector<Object> objectList = new Vector<Object>();	

		try {
			EncounterService es = Context.getEncounterService();
			Set<Encounter> encs = new HashSet<Encounter>();
			
			/*
			if (phrase.matches("\\d+")) {
				// user searched on a number.  Insert obs with corresponding obsId
				Obs e = os.getObs(Integer.valueOf(phrase));
				if (e != null) {
					encs.add(e);
				}
			}
			*/
			
			if (phrase == null || phrase.equals("")) {
				//TODO get all concepts for testing purposes?
			}
			else {
				encs.addAll(es.getEncountersByPatientIdentifier(phrase, includeVoided));
			}

			if (encs.size() == 0) {
				objectList.add("No matches found for <b>" + phrase + "</b>");
			}
			else {
				objectList = new Vector<Object>(encs.size());
				for (Encounter e : encs) {
					objectList.add(new EncounterListItem(e));
				}
			}
		} catch (Exception e) {
			log.error(e);
			objectList.add("Error while attempting to find obs - " + e.getMessage());
		}

		return objectList;
	}

}
