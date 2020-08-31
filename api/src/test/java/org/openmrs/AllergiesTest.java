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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;

/**
 * Tests methods in {@link org.openmrs.Allergies}.
 */
public class AllergiesTest extends BaseContextSensitiveTest {
	
	private static final String ALLERGY_OTHER_NONCODED_TEST_DATASET = "org/openmrs/api/include/otherNonCodedConcept.xml";
	
	Allergies allergies;
	
	@BeforeEach
	public void setup() {
		allergies = new Allergies();

		executeDataSet(ALLERGY_OTHER_NONCODED_TEST_DATASET);
		Allergen.setOtherNonCodedConceptUuid(Context.getAdministrationService().getGlobalProperty(
			    OpenmrsConstants.GP_ALLERGEN_OTHER_NON_CODED_UUID));
	}
	
	/**
	 * @see {@link Allergies#add(Allergy)}
	 * @see {@link Allergies#add(int, Allergy)}
	 * @see {@link Allergies#get(int)}
	 * @see {@link Allergies#set(int, Allergy))}
	 * @see {@link Allergies#indexOf(Object)}
	 */
	@Test
	public void shouldAddAllergyAndSetCorrectStatus(){
		assertEquals(allergies.getAllergyStatus(), Allergies.UNKNOWN);
		Allergy allergy = new Allergy();
		
		assertTrue(allergies.add(allergy));
		assertTrue(allergies.contains(allergy));
		assertEquals(allergies.getAllergyStatus(), Allergies.SEE_LIST);
		
		allergy = new Allergy();
		allergies.add(0, allergy);
		assertEquals(allergies.indexOf(allergy), 0);
		assertEquals(allergies.get(0), allergy);
		assertNotEquals(allergies.get(1), allergy);
		assertEquals(allergies.getAllergyStatus(), Allergies.SEE_LIST);
		
		allergy = new Allergy();
		allergies.set(0, allergy);
		assertEquals(allergies.size(), 2);
		assertEquals(allergies.get(0), allergy);
		assertEquals(allergies.getAllergyStatus(), Allergies.SEE_LIST);
	}
	
	/**
	 * @see {@link Allergies#addAll(java.util.Collection)}
	 * @see {@link Allergies#addAll(int, java.util.Collection)}
	 */
	@Test
	public void shouldAddAllergyCollectionAndSetCorrectStatus(){
		assertEquals(allergies.getAllergyStatus(), Allergies.UNKNOWN);
		List<Allergy> allergyList = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			allergyList.add(new Allergy());
		}
		
		assertTrue(allergies.addAll(allergyList));
		assertEquals(allergies.getAllergyStatus(), Allergies.SEE_LIST);
		
		Allergy allergy = new Allergy();
		allergyList.clear();
		for (int i = 0; i < 5; i++) {
			allergyList.add(new Allergy());
		}
		allergyList.set(1, allergy);
		
		assertTrue(allergies.addAll(2, allergyList));
		assertEquals(allergies.get(3), allergy);
		assertEquals(allergies.getAllergyStatus(), Allergies.SEE_LIST);
	}
	/**
	 * @see {@link Allergies#remove(Allergy)}
	 * @see {@link Allergies#remove(int)}
	 */
	@Test
	public void shouldRemoveAllergyAndSetCorrectStatus(){
		assertEquals(allergies.getAllergyStatus(), Allergies.UNKNOWN);
		Allergy allergy1 = new Allergy();
		Allergy allergy2 = new Allergy();
		allergies.add(allergy1);
		allergies.add(0, allergy2);
		assertEquals(allergies.getAllergyStatus(), Allergies.SEE_LIST);
		
		assertFalse(allergies.remove(new Allergy()));
		assertEquals(allergies.remove(0), allergy2);
		assertEquals(allergies.size(), 1);
		assertEquals(allergies.getAllergyStatus(), Allergies.SEE_LIST);
		
		assertTrue(allergies.remove(allergy1));
		assertEquals(allergies.getAllergyStatus(), Allergies.UNKNOWN);
	}
	
	/**
	 * @see {@link Allergies#clear()}
	 */
	@Test 
	public void shouldClearAllergyAndSetCorrectStatus(){
		allergies.add(new Allergy());
		allergies.add(new Allergy());
		assertEquals(allergies.size(), 2);
		assertEquals(allergies.getAllergyStatus(), Allergies.SEE_LIST);
		
		allergies.clear();
		assertEquals(allergies.size(), 0);
		assertEquals(allergies.getAllergyStatus(), Allergies.UNKNOWN);
	}
	
	/**
	 * @see {@link Allergies#confirmNoKnownAllergies()}
	 */
	@Test
	public void shouldConfirmNoKnownAllergies(){
		allergies.confirmNoKnownAllergies();
		assertEquals(allergies.getAllergyStatus(), Allergies.NO_KNOWN_ALLERGIES);
	}
	
	/**
	 * @see {@link Allergies#confirmNoKnownAllergies()}
	 */
	@Test
	public void shouldThrowAnErrorWhenTryingConfirmNoKnowAllergiesWhileAllergiesIsNotEmpty(){
		allergies.add(new Allergy());
		assertThrows(APIException.class, () -> allergies.confirmNoKnownAllergies());
	}
	
	/**
	 * @see {@link Allergies#add(Allergy)}
	 */
	@Test
	public void add_shouldNotAllowDuplicateCodedAllergen(){
		Allergy allergy1 = new Allergy();
        Concept concept = new Concept();
		allergy1.setAllergen(new Allergen(null, concept, null));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, concept, null));
		
		allergies.add(allergy1);
		assertThrows(APIException.class, () -> allergies.add(allergy2));
	}
	
	/**
	 * @see {@link Allergies#add(Allergy)}
	 */
	@Test
	public void add_shouldAllowNonDuplicateCodedAllergen(){
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, new Concept(1), null));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, new Concept(2), null));
		
		allergies.add(allergy1);
		allergies.add(allergy2);
	}
	
	/**
	 * @see {@link Allergies#add(Allergy)}
	 */
	@Test
	public void add_shouldNotAllowDuplicateNonCodedAllergen(){
		Concept concept = Context.getConceptService().getConceptByUuid(Allergen.getOtherNonCodedConceptUuid());
		
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, concept, "OTHER VALUE"));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, concept, "OTHER VALUE"));
		
		allergies.add(allergy1);
		assertThrows(APIException.class, () -> allergies.add(allergy2));
	}
	
	/**
	 * @see {@link Allergies#add(Allergy)}
	 */
	@Test
	public void add_shouldAllowNonDuplicateNonCodedAllergen(){
		Concept concept = Context.getConceptService().getConceptByUuid(Allergen.getOtherNonCodedConceptUuid());
		
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, concept, "OTHER VALUE1"));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, concept, "OTHER VALUE2"));
		
		allergies.add(allergy1);
		allergies.add(allergy2);
	}
	
	/**
	 * @see {@link Allergies#add(int, Allergy)}
	 */
	@Test
	public void add2_shouldNotAllowDuplicateCodedAllergen(){
		Allergy allergy1 = new Allergy();
        Concept concept = new Concept();
		allergy1.setAllergen(new Allergen(null, concept, null));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, concept, null));
		
		allergies.add(0, allergy1);
		assertThrows(APIException.class, () -> allergies.add(0, allergy2));
	}
	
	/**
	 * @see {@link Allergies#add(int, Allergy)}
	 */
	@Test
	public void add2_shouldAllowNonDuplicateCodedAllergen(){
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, new Concept(1), null));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, new Concept(2), null));
		
		allergies.add(0, allergy1);
		allergies.add(0, allergy2);
	}
	
	/**
	 * @see {@link Allergies#add(int, Allergy)}
	 */
	@Test
	public void add2_shouldNotAllowDuplicateNonCodedAllergen(){
		Concept concept = Context.getConceptService().getConceptByUuid(Allergen.getOtherNonCodedConceptUuid());
		
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, concept, "OTHER VALUE"));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, concept, "OTHER VALUE"));
		
		allergies.add(0, allergy1);
		assertThrows(APIException.class, () -> allergies.add(0, allergy2));
	}
	
	/**
	 * @see {@link Allergies#add(int, Allergy)}
	 */
	@Test
	public void add2_shouldAllowNonDuplicateNonCodedAllergen(){
		Concept concept = Context.getConceptService().getConceptByUuid(Allergen.getOtherNonCodedConceptUuid());
		
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, concept, "OTHER VALUE1"));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, concept, "OTHER VALUE2"));
		
		allergies.add(0, allergy1);
		allergies.add(0, allergy2);
	}
	
	/**
	 * @see {@link Allergies#addAll(java.util.Collection)}
	 */
	@Test
	public void addAll_shouldNotAllowDuplicateCodedAllergen(){
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, new Concept(1), null));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, new Concept(1), null));
		
		Allergies allergies = new Allergies();
		allergies.add(allergy1);
		allergies.add(allergy2);
		
		assertThrows(APIException.class, () -> allergies.addAll(allergies));
	}
	
	/**
	 * @see {@link Allergies#addAll(java.util.Collection)}
	 */
	@Test
	public void addAll_shouldAllowNonDuplicateCodedAllergen(){
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, new Concept(1), null));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, new Concept(2), null));
		
		Allergies allergies = new Allergies();
		allergies.add(allergy1);
		allergies.add(allergy2);

		assertThrows(APIException.class, () -> allergies.addAll(allergies));
	}
	
	/**
	 * @see {@link Allergies#addAll(java.util.Collection)}
	 */
	@Test
	public void addAll_shouldNotAllowDuplicateNonCodedAllergen(){
		Concept concept = Context.getConceptService().getConceptByUuid(Allergen.getOtherNonCodedConceptUuid());
		
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, concept, "OTHER VALUE"));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, concept, "OTHER VALUE"));
		
		Allergies allergies = new Allergies();
		allergies.add(allergy1);
		assertThrows(APIException.class, () -> allergies.add(allergy2));
		assertThrows(APIException.class, () -> allergies.addAll(allergies));
	}
	
	/**
	 * @see {@link Allergies#addAll(java.util.Collection)}
	 */
	@Test
	public void addAll_shouldAllowNonDuplicateNonCodedAllergen(){
		Concept concept = Context.getConceptService().getConceptByUuid(Allergen.getOtherNonCodedConceptUuid());
		
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, concept, "OTHER VALUE1"));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, concept, "OTHER VALUE2"));
		
		Allergies allergies = new Allergies();
		allergies.add(allergy1);
		allergies.add(allergy2);

		assertThrows(APIException.class, () -> allergies.addAll(allergies));
	}
	
	/**
	 * @see {@link Allergies#addAll(int, java.util.Collection)}
	 */
	@Test
	public void addAll2_shouldNotAllowDuplicateCodedAllergen(){
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, new Concept(1), null));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, new Concept(1), null));
		
		Allergies allergies = new Allergies();
		allergies.add(allergy1);
		allergies.add(allergy2);

		assertThrows(APIException.class, () -> allergies.addAll(0, allergies));
	}
	
	/**
	 * @see {@link Allergies#addAll(int, java.util.Collection)}
	 */
	@Test
	public void addAll2_shouldAllowNonDuplicateCodedAllergen(){
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, new Concept(1), null));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, new Concept(2), null));
		
		Allergies allergies = new Allergies();
		allergies.add(allergy1);
		allergies.add(allergy2);

		assertThrows(APIException.class, () -> allergies.addAll(0, allergies));
	}
	
	/**
	 * @see {@link Allergies#addAll(int, java.util.Collection)}
	 */
	@Test
	public void addAll2_shouldNotAllowDuplicateNonCodedAllergen(){
		Concept concept = Context.getConceptService().getConceptByUuid(Allergen.getOtherNonCodedConceptUuid());
		
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, concept, "OTHER VALUE"));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, concept, "OTHER VALUE"));
		
		Allergies allergies = new Allergies();
		allergies.add(allergy1);
		assertThrows(APIException.class, () -> allergies.add(allergy2));
	}
	
	/**
	 * @see {@link Allergies#addAll(int, java.util.Collection)}
	 */
	@Test
	public void addAll2_shouldAllowNonDuplicateNonCodedAllergen(){
		Concept concept = Context.getConceptService().getConceptByUuid(Allergen.getOtherNonCodedConceptUuid());
		
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, concept, "OTHER VALUE1"));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, concept, "OTHER VALUE2"));
		
		Allergies allergies = new Allergies();
		allergies.add(allergy1);
		allergies.add(allergy2);

		assertThrows(APIException.class, () -> allergies.addAll(0, allergies));
	}
	
	/**
	 * @see {@link Allergies#addAll(java.util.Collection)}
	 */
	@Test
	public void addAll4_shouldNotAllowDuplicateCodedAllergen(){
		Allergy allergy1 = new Allergy();
        Concept concept = new Concept();
		allergy1.setAllergen(new Allergen(null, concept, null));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, concept, null));
		
		List<Allergy> allergies = new ArrayList<>();
		allergies.add(allergy1);
		allergies.add(allergy2);

		assertThrows(APIException.class, () -> new Allergies().addAll(allergies));
	}
	
	/**
	 * @see {@link Allergies#addAll(int, java.util.Collection)}
	 */
	@Test
	public void addAll5_shouldNotAllowDuplicateCodedAllergen(){
		Allergy allergy1 = new Allergy();
        Concept concept = new Concept();
		allergy1.setAllergen(new Allergen(null, concept, null));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, concept, null));
		
		List<Allergy> allergies = new ArrayList<>();
		allergies.add(allergy1);
		allergies.add(allergy2);

		assertThrows(APIException.class, () -> new Allergies().addAll(0, allergies));
	}
	
	/**
	 * @see {@link Allergies#addAll(java.util.Collection)}
	 */
	@Test
	public void addAll6_shouldNotAllowDuplicateNonCodedAllergen(){
		Concept concept = Context.getConceptService().getConceptByUuid(Allergen.getOtherNonCodedConceptUuid());
		
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, concept, "OTHER VALUE"));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, concept, "OTHER VALUE"));
		
		List<Allergy> allergies = new ArrayList<>();
		allergies.add(allergy1);
		allergies.add(allergy2);

		assertThrows(APIException.class, () -> new Allergies().addAll(allergies));
	}
	
	/**
	 * @see {@link Allergies#addAll(int, java.util.Collection)}
	 */
	@Test
	public void addAll7_shouldNotAllowDuplicateNonCodedAllergen(){
		Concept concept = Context.getConceptService().getConceptByUuid(Allergen.getOtherNonCodedConceptUuid());
		
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, concept, "OTHER VALUE"));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, concept, "OTHER VALUE"));
		
		List<Allergy> allergies = new ArrayList<>();
		allergies.add(allergy1);
		allergies.add(allergy2);

		assertThrows(APIException.class, () -> new Allergies().addAll(0, allergies));
	}
}
