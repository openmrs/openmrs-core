package org.openmrs.api.db.hibernate;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSet;
import org.springframework.test.annotation.ExpectedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

// ConceptSet = cs
// Concept = c
public class ConceptTreeLoaderTest {
	
	// cs1 has (cs2, cs3). cs2 has (cs4, cs5). cs3 has (cs4, cs6)
	@Test
	public void shouldLoadTreeWhenThereAreNoCyclicReferences() {
		ArrayList<ConceptSearchResult> conceptSearchResults = new ArrayList<ConceptSearchResult>();
		ArrayList concepts = new ArrayList();
		Concept concept1 = createConcept("1", 1);
		Concept concept2 = createConcept("2", 2);
		Concept concept3 = createConcept("3", 3);
		Concept concept4 = createConcept("4", 4);
		Concept concept5 = createConcept("5", 5);
		Concept concept6 = createConcept("6", 6);
		
		//        ConceptSet conceptSet1 = createConceptSet("1", concept1);
		ConceptSet conceptSet2 = createConceptSet("2", concept2);
		ConceptSet conceptSet3 = createConceptSet("3", concept3);
		ConceptSet conceptSet4 = createConceptSet("4", concept4);
		ConceptSet conceptSet5 = createConceptSet("5", concept5);
		ConceptSet conceptSet6 = createConceptSet("6", concept6);
		
		concept1.setConceptSets(conceptSetList(conceptSet2, conceptSet3));
		concept2.setConceptSets(conceptSetList(conceptSet4, conceptSet5));
		concept3.setConceptSets(conceptSetList(conceptSet4, conceptSet6));
		
		concepts.add(concept1);
		concepts.add("");
		concepts.add(new Double(0));
		concepts.add(null);
		ArrayList arrayList = new ArrayList();
		arrayList.add(concepts);
		ConceptTreeLoader conceptTreeLoader = new ConceptTreeLoader(new HibernateLazyLoaderStub());
		conceptTreeLoader.loadTree(conceptSearchResults, arrayList);
		Assert.assertEquals(1, conceptSearchResults.size());
		Assert.assertEquals(2, conceptSearchResults.get(0).getConcept().getConceptSets().size());
	}
	
	// cs1 has (cs2, cs3). cs2 has (cs1)
	@Test(expected = IllegalStateException.class)
	public void shouldLoadTreeWhenThereAreCyclicReferences() {
		ArrayList<ConceptSearchResult> conceptSearchResults = new ArrayList<ConceptSearchResult>();
		ArrayList concepts = new ArrayList();
		Concept concept1 = createConcept("1", 1);
		Concept concept2 = createConcept("2", 2);
		Concept concept3 = createConcept("3", 3);
		
		ConceptSet conceptSet1 = createConceptSet("1", concept1);
		ConceptSet conceptSet2 = createConceptSet("2", concept2);
		ConceptSet conceptSet3 = createConceptSet("3", concept3);
		
		concept1.setConceptSets(conceptSetList(conceptSet2, conceptSet3));
		concept2.setConceptSets(conceptSetList(conceptSet1));
		
		concepts.add(concept1);
		concepts.add("");
		concepts.add(new Double(0));
		concepts.add(null);
		ArrayList arrayList = new ArrayList();
		arrayList.add(concepts);
		ConceptTreeLoader conceptTreeLoader = new ConceptTreeLoader(new HibernateLazyLoaderStub());
		conceptTreeLoader.loadTree(conceptSearchResults, arrayList);
		Assert.assertEquals(1, conceptSearchResults.size());
		Assert.assertEquals(2, conceptSearchResults.get(0).getConcept().getConceptSets().size());
	}
	
	private Collection<ConceptSet> conceptSetList(ConceptSet... conceptSets) {
		ArrayList<ConceptSet> conceptSetsList = new ArrayList<ConceptSet>();
		Collections.addAll(conceptSetsList, conceptSets);
		return conceptSetsList;
	}
	
	private Concept createConcept(String conceptUUID, int conceptId) {
		Concept concept = new Concept();
		concept.setUuid(conceptUUID);
		concept.setId(conceptId);
		return concept;
	}
	
	private ConceptSet createConceptSet(String conceptUUID, Concept concept) {
		ConceptSet conceptSet = new ConceptSet();
		conceptSet.setUuid(conceptUUID);
		conceptSet.setConcept(concept);
		return conceptSet;
	}
	
	class HibernateLazyLoaderStub extends HibernateLazyLoader {
		
		@Override
		public Object load(Object entity) {
			return entity;
		}
	}
}
