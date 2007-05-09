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
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

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
	
	
	public void createObs(Integer personId, Integer encounterId, Integer conceptId, String valueText, String obsDateStr) { 
		
		log.info("Create new observation ");
	
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
		
		Person person = Context.getPersonService().getPerson(personId);
		Concept concept = Context.getConceptService().getConcept(conceptId);
		Encounter encounter = encounterId == null ? null : Context.getEncounterService().getEncounter(encounterId);
		
		Obs obs = new Obs();
		obs.setPerson(person);
		obs.setConcept(concept);
		obs.setObsDatetime(obsDate);
		if (encounter != null) {
			obs.setEncounter(encounter);
			obs.setLocation(encounter.getLocation());
		} else {
			Location unknown = Context.getEncounterService().getLocationByName("Unknown Location");
			if (unknown == null)
				unknown = Context.getEncounterService().getLocationByName("Unknown");
			obs.setLocation(unknown);
		}
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

	public Vector<ObsListItem> getObsByPatientConceptEncounter(String personId, String conceptId, String encounterId) {
		log.debug("Started with: [" + personId + "] [" + conceptId + "] [" + encounterId + "]");
		
		Vector<ObsListItem> ret = new Vector<ObsListItem>();
		
		Integer pId = null;
		try {
			pId = new Integer(personId);
		} catch ( NumberFormatException nfe ) {
			pId = null;
		}
		
		Integer eId = null;
		try {
			eId = new Integer(encounterId);
		} catch ( NumberFormatException nfe ) {
			eId = null;
		}
		
		Person p = null;
		Concept c = null;
		Encounter e = null;
		
		if ( pId != null ) p = Context.getPersonService().getPerson(pId);
		if ( conceptId != null ) c = OpenmrsUtil.getConceptByIdOrName(conceptId);
		if ( eId != null ) e = Context.getEncounterService().getEncounter(eId);
		
		Set<Obs> obss = null;
		
		if ( p != null && c != null ) {
			log.debug("Getting obss with patient and concept");
			obss = Context.getObsService().getObservations(p, c);
		} else if ( e != null ) {
			log.debug("Getting obss with encounter");
			obss = Context.getObsService().getObservations(e);
		} else if ( p != null ) {
			log.debug("Getting obss with just patient");
			obss = Context.getObsService().getObservations(p);
		}

		if ( obss != null ) {
			for ( Obs obs : obss ) {
				ObsListItem newItem = new ObsListItem(obs, Context.getLocale());
				ret.add(newItem);
			}
			log.debug("obss was size " + obss.size());
		}
		
		return ret;
	}
	
	public ObsListItem getObs(Integer obsId) {
		Obs o = null;
		if ( obsId != null ) {
			o = Context.getObsService().getObs(obsId);
		}
		
		ObsListItem oItem = null;
		
		if ( o != null ) {
			oItem = new ObsListItem(o, Context.getLocale());
		}
		
		return oItem;
	}
}
