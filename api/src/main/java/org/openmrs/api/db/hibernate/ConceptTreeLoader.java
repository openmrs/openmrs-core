package org.openmrs.api.db.hibernate;

import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class ConceptTreeLoader {
	
	private HibernateLazyLoader hibernateLazyLoader;
	
	public ConceptTreeLoader(HibernateLazyLoader hibernateLazyLoader) {
		this.hibernateLazyLoader = hibernateLazyLoader;
	}
	
	public void loadTree(List<ConceptSearchResult> conceptSearchResults, List concepts) {
		for (Object obj : concepts) {
			List list = (List) obj;
			Concept concept = (Concept) list.get(0);
			HashSet<Concept> conceptsAlreadyRetrieved = new HashSet<Concept>();
			Concept implementationFromProxy = getImplementationForTheWholeTree(concept, -1, conceptsAlreadyRetrieved);
			conceptSearchResults.add(new ConceptSearchResult((String) list.get(1), implementationFromProxy,
			        (ConceptName) list.get(3), (Double) list.get(2)));
		}
	}
	
	private Concept getImplementationForTheWholeTree(Concept proxyConcept, int parentConceptId,
	        HashSet<Concept> conceptsAlreadyRetrieved) {
		Concept concept = getImplementationFromProxy(proxyConcept);
		addConcept(concept, parentConceptId, conceptsAlreadyRetrieved);
		Collection<ConceptSet> conceptSets = concept.getConceptSets();
		for (ConceptSet conceptSet : conceptSets) {
			Concept underlyingConcept = getImplementationForTheWholeTree(conceptSet.getConcept(), concept.getConceptId(),
			    conceptsAlreadyRetrieved);
			conceptSet.setConcept(underlyingConcept);
			conceptsAlreadyRetrieved.remove(underlyingConcept);
		}
		return concept;
	}
	
	private void addConcept(Concept implementationFromProxy, int parentConceptId, HashSet<Concept> conceptsAlreadyRetrieved) {
		if (conceptsAlreadyRetrieved.contains(implementationFromProxy)) {
			throw new IllegalStateException("The concept with id=" + implementationFromProxy.getConceptId()
			        + " is a circular reference within concept with id=" + parentConceptId
			        + ". Check your data and then try again");
		} else {
			conceptsAlreadyRetrieved.add(implementationFromProxy);
		}
	}
	
	private <T> T getImplementationFromProxy(T entity) {
		if (entity == null) {
			throw new NullPointerException("Entity passed for initialization is null");
		}
		
		return (T) hibernateLazyLoader.load(entity);
	}
}
