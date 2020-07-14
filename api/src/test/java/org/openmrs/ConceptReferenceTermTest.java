/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Contains test methods for concept reference terms
 */
public class ConceptReferenceTermTest {
	
	/**
	 * @see ConceptReferenceTerm#addConceptReferenceTermMap(ConceptReferenceTermMap)
	 */
	@Test
	public void addConceptReferenceTermMap_shouldSetTermAAsTheTermToWhichAMappingIsBeingAdded() {
		ConceptReferenceTerm term = new ConceptReferenceTerm(2);
		term.addConceptReferenceTermMap(new ConceptReferenceTermMap(new ConceptReferenceTerm(1), new ConceptMapType(1)));
		assertTrue(term.equals(term.getConceptReferenceTermMaps().iterator().next().getTermA()));
	}
	
	/**
	 * @see ConceptReferenceTerm#addConceptReferenceTermMap(ConceptReferenceTermMap)
	 */
	@Test
	public void addConceptReferenceTermMap_shouldNotAddAMapWhereTermBIsItself() {
		ConceptReferenceTerm term = new ConceptReferenceTerm(2);
		term.setUuid("test uuid");
		term.addConceptReferenceTermMap(new ConceptReferenceTermMap(new ConceptReferenceTerm(1), new ConceptMapType(1)));
		//add a mapping where termB is itself
		term.addConceptReferenceTermMap(new ConceptReferenceTermMap(term, new ConceptMapType(1)));
		assertEquals(1, term.getConceptReferenceTermMaps().size());
	}
	
	/**
	 * @see ConceptReferenceTerm#addConceptReferenceTermMap(ConceptReferenceTermMap)
	 */
	@Test
	public void addConceptReferenceTermMap_shouldNotAddDuplicateConceptReferenceTermMaps() {
		ConceptReferenceTerm term = new ConceptReferenceTerm(5);
		ConceptReferenceTermMap map1 = new ConceptReferenceTermMap(1);
		map1.setTermB(new ConceptReferenceTerm(1));
		ConceptReferenceTermMap map2 = new ConceptReferenceTermMap(2);
		map2.setTermB(new ConceptReferenceTerm(2));
		//add a mapping with the same id as the one before
		ConceptReferenceTermMap map3 = new ConceptReferenceTermMap(2);
		map3.setTermB(new ConceptReferenceTerm(3));
		
		term.addConceptReferenceTermMap(map1);
		term.addConceptReferenceTermMap(map2);
		term.addConceptReferenceTermMap(map3);
		
		assertEquals(2, term.getConceptReferenceTermMaps().size());
	}
}
