package org.openmrs.web.taglib.functions;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Obs;

public class Filter {
	
	public static final long serialVersionUID = 1L;
	private final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Returns a subset of the passed set of encounters that match the passed encounter type id
	 * @param encs: Superset of encounters
	 * @param type: EncounterTypeId to match
	 * @return: Subset of passed encounters that match EncounterTypeId
	 */		
	public static Set<Encounter> filterEncountersByType(Set<Encounter> encs, Integer type) {
		Set<Encounter> ret = new HashSet<Encounter>();
		for (Iterator<Encounter> i=encs.iterator(); i.hasNext();) {
			Encounter e = i.next();
			if (e.getEncounterType().getEncounterTypeId().intValue() == type.intValue()) {
				ret.add(e);
			}
		}
		return ret;
	}
	
	/**
	 * Returns a subset of the passed set of observations that match the passed concept type id
	 * @param obs: Superset of obs
	 * @param concept: ConceptId to match
	 * @return: Subset of passed obs that match ConceptId
	 */		
	public static Set<Obs> filterObsByConcept(Set<Obs> obs, Integer concept) {
		Set<Obs> ret = new HashSet<Obs>();
		for (Iterator<Obs> i=obs.iterator(); i.hasNext();) {
			Obs o = i.next();
			if (o.getConcept().getConceptId().intValue() == concept.intValue()) {
				ret.add(o);
			}
		}
		return ret;
	}
}
