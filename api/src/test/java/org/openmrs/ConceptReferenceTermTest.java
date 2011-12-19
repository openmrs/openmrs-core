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
package org.openmrs;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;

/**
 * Contains test methods for concept reference terms
 */
public class ConceptReferenceTermTest {
	
	/**
	 * @see {@link ConceptReferenceTerm#addConceptReferenceTermMap(ConceptReferenceTermMap)}
	 */
	@Test
	@Verifies(value = "should set termA as the term to which a mapping is being added", method = "addConceptReferenceTermMap(ConceptReferenceTermMap)")
	public void addConceptReferenceTermMap_shouldSetTermAAsTheTermToWhichAMappingIsBeingAdded() throws Exception {
		ConceptReferenceTerm term = new ConceptReferenceTerm(2);
		term.addConceptReferenceTermMap(new ConceptReferenceTermMap(new ConceptReferenceTerm(1), new ConceptMapType(1)));
		Assert.assertEquals(true, term.equals(term.getConceptReferenceTermMaps().iterator().next().getTermA()));
	}
	
	/**
	 * @see {@link ConceptReferenceTerm#addConceptReferenceTermMap(ConceptReferenceTermMap)}
	 */
	@Test
	@Verifies(value = "should not add a map where termB is itself", method = "addConceptReferenceTermMap(ConceptReferenceTermMap)")
	public void addConceptReferenceTermMap_shouldNotAddAMapWhereTermBIsItself() throws Exception {
		ConceptReferenceTerm term = new ConceptReferenceTerm(2);
		term.setUuid("test uuid");
		term.addConceptReferenceTermMap(new ConceptReferenceTermMap(new ConceptReferenceTerm(1), new ConceptMapType(1)));
		//add a mapping where termB is itself
		term.addConceptReferenceTermMap(new ConceptReferenceTermMap(term, new ConceptMapType(1)));
		Assert.assertEquals(1, term.getConceptReferenceTermMaps().size());
	}
	
	/**
	 * @see {@link ConceptReferenceTerm#addConceptReferenceTermMap(ConceptReferenceTermMap)}
	 */
	@Test
	@Verifies(value = "should not add duplicate concept reference term maps", method = "addConceptReferenceTermMap(ConceptReferenceTermMap)")
	public void addConceptReferenceTermMap_shouldNotAddDuplicateConceptReferenceTermMaps() throws Exception {
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
		
		Assert.assertEquals(2, term.getConceptReferenceTermMaps().size());
	}
}
