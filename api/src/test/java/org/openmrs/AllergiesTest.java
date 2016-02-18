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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.api.APIException;

/**
 * Tests methods in {@link org.openmrs.Allergies}.
 */
public class AllergiesTest {
	
	Allergies allergies;
	
	@Before
	public void setup(){
		allergies = new Allergies();
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
		Assert.assertEquals(allergies.getAllergyStatus(), Allergies.UNKNOWN);
		Allergy allergy = new Allergy();
		
		Assert.assertTrue(allergies.add(allergy));
		Assert.assertTrue(allergies.contains(allergy));
		Assert.assertEquals(allergies.getAllergyStatus(), Allergies.SEE_LIST);
		
		allergy = new Allergy();
		allergies.add(0, allergy);
		Assert.assertEquals(allergies.indexOf(allergy), 0);
		Assert.assertEquals(allergies.get(0), allergy);
		Assert.assertNotEquals(allergies.get(1), allergy);
		Assert.assertEquals(allergies.getAllergyStatus(), Allergies.SEE_LIST);
		
		allergy = new Allergy();
		allergies.set(0, allergy);
		Assert.assertEquals(allergies.size(), 2);
		Assert.assertEquals(allergies.get(0), allergy);
		Assert.assertEquals(allergies.getAllergyStatus(), Allergies.SEE_LIST);
	}
	
	/**
	 * @see {@link Allergies#addAll(java.util.Collection)}
	 * @see {@link Allergies#addAll(int, java.util.Collection)}
	 */
	@Test
	public void shouldAddAllergyCollectionAndSetCorrectStatus(){
		Assert.assertEquals(allergies.getAllergyStatus(), Allergies.UNKNOWN);
		List<Allergy> allergyList = new ArrayList<Allergy>();
		for (int i = 0; i < 5; i++) {
			allergyList.add(new Allergy());
		}
		
		Assert.assertTrue(allergies.addAll(allergyList));
		Assert.assertEquals(allergies.getAllergyStatus(), Allergies.SEE_LIST);
		
		Allergy allergy = new Allergy();
		allergyList.clear();
		for (int i = 0; i < 5; i++) {
			allergyList.add(new Allergy());
		}
		allergyList.set(1, allergy);
		
		Assert.assertTrue(allergies.addAll(2, allergyList));
		Assert.assertEquals(allergies.get(3), allergy);
		Assert.assertEquals(allergies.getAllergyStatus(), Allergies.SEE_LIST);
	}
	/**
	 * @see {@link Allergies#remove(Allergy)}
	 * @see {@link Allergies#remove(int)}
	 */
	@Test
	public void shouldRemoveAllergyAndSetCorrectStatus(){
		Assert.assertEquals(allergies.getAllergyStatus(), Allergies.UNKNOWN);
		Allergy allergy1 = new Allergy();
		Allergy allergy2 = new Allergy();
		allergies.add(allergy1);
		allergies.add(0, allergy2);
		Assert.assertEquals(allergies.getAllergyStatus(), Allergies.SEE_LIST);
		
		Assert.assertFalse(allergies.remove(new Allergy()));
		Assert.assertEquals(allergies.remove(0), allergy2);
		Assert.assertEquals(allergies.size(), 1);
		Assert.assertEquals(allergies.getAllergyStatus(), Allergies.SEE_LIST);
		
		Assert.assertTrue(allergies.remove(allergy1));
		Assert.assertEquals(allergies.getAllergyStatus(), Allergies.UNKNOWN);
	}
	
	/**
	 * @see {@link Allergies#clear()}
	 */
	@Test 
	public void shouldClearAllergyAndSetCorrectStatus(){
		allergies.add(new Allergy());
		allergies.add(new Allergy());
		Assert.assertEquals(allergies.size(), 2);
		Assert.assertEquals(allergies.getAllergyStatus(), Allergies.SEE_LIST);
		
		allergies.clear();
		Assert.assertEquals(allergies.size(), 0);
		Assert.assertEquals(allergies.getAllergyStatus(), Allergies.UNKNOWN);
	}
	
	/**
	 * @see {@link Allergies#confirmNoKnownAllergies()}
	 */
	@Test
	public void shouldConfirmNoKnownAllergies(){
		allergies.confirmNoKnownAllergies();
		Assert.assertEquals(allergies.getAllergyStatus(), Allergies.NO_KNOWN_ALLERGIES);
	}
	
	/**
	 * @see {@link Allergies#confirmNoKnownAllergies()}
	 */
	@Test(expected = APIException.class)
	public void shouldThrowAnErrorWhenTryingConfirmNoKnowAllergiesWhileAllergiesIsNotEmpty(){
		allergies.add(new Allergy());
		allergies.confirmNoKnownAllergies();
	}
	
	/**
	 * @see {@link Allergies#add(Allergy)}
	 */
	@Test(expected = APIException.class)
	public void add_shouldNotAllowDuplicateCodedAllergen(){
		Allergy allergy1 = new Allergy();
        Concept concept = new Concept();
		allergy1.setAllergen(new Allergen(null, concept, null));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, concept, null));
		
		allergies.add(allergy1);
		allergies.add(allergy2);
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
	@Test(expected = APIException.class)
	public void add_shouldNotAllowDuplicateNonCodedAllergen(){
		
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, null, "OTHER VALUE"));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, null, "OTHER VALUE"));
		
		allergies.add(allergy1);
		allergies.add(allergy2);
	}
	
	/**
	 * @see {@link Allergies#add(Allergy)}
	 */
	@Test
	public void add_shouldAllowNonDuplicateNonCodedAllergen(){
		
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, null, "OTHER VALUE1"));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, null, "OTHER VALUE2"));
		
		allergies.add(allergy1);
		allergies.add(allergy2);
	}
	
	/**
	 * @see {@link Allergies#add(int, Allergy)}
	 */
	@Test(expected = APIException.class)
	public void add2_shouldNotAllowDuplicateCodedAllergen(){
		Allergy allergy1 = new Allergy();
        Concept concept = new Concept();
		allergy1.setAllergen(new Allergen(null, concept, null));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, concept, null));
		
		allergies.add(0, allergy1);
		allergies.add(0, allergy2);
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
	@Test(expected = APIException.class)
	public void add2_shouldNotAllowDuplicateNonCodedAllergen(){
		
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, null, "OTHER VALUE"));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, null, "OTHER VALUE"));
		
		allergies.add(0, allergy1);
		allergies.add(0, allergy2);
	}
	
	/**
	 * @see {@link Allergies#add(int, Allergy)}
	 */
	@Test
	public void add2_shouldAllowNonDuplicateNonCodedAllergen(){
		
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, null, "OTHER VALUE1"));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, null, "OTHER VALUE2"));
		
		allergies.add(0, allergy1);
		allergies.add(0, allergy2);
	}
	
	/**
	 * @see {@link Allergies#addAll(java.util.Collection)}
	 */
	@Test(expected = APIException.class)
	public void addAll_shouldNotAllowDuplicateCodedAllergen(){
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, new Concept(1), null));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, new Concept(1), null));
		
		Allergies allergies = new Allergies();
		allergies.add(allergy1);
		allergies.add(allergy2);
		
		allergies.addAll(allergies);
	}
	
	/**
	 * @see {@link Allergies#addAll(java.util.Collection)}
	 */
	@Test(expected = APIException.class)
	public void addAll_shouldAllowNonDuplicateCodedAllergen(){
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, new Concept(1), null));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, new Concept(2), null));
		
		Allergies allergies = new Allergies();
		allergies.add(allergy1);
		allergies.add(allergy2);
		
		allergies.addAll(allergies);
	}
	
	/**
	 * @see {@link Allergies#addAll(java.util.Collection)}
	 */
	@Test(expected = APIException.class)
	public void addAll_shouldNotAllowDuplicateNonCodedAllergen(){
		
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, null, "OTHER VALUE"));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, null, "OTHER VALUE"));
		
		Allergies allergies = new Allergies();
		allergies.add(allergy1);
		allergies.add(allergy2);
		
		allergies.addAll(allergies);
	}
	
	/**
	 * @see {@link Allergies#addAll(java.util.Collection)}
	 */
	@Test(expected = APIException.class)
	public void addAll_shouldAllowNonDuplicateNonCodedAllergen(){
		
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, null, "OTHER VALUE1"));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, null, "OTHER VALUE2"));
		
		Allergies allergies = new Allergies();
		allergies.add(allergy1);
		allergies.add(allergy2);
		
		allergies.addAll(allergies);
	}
	
	/**
	 * @see {@link Allergies#addAll(int, java.util.Collection)}
	 */
	@Test(expected = APIException.class)
	public void addAll2_shouldNotAllowDuplicateCodedAllergen(){
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, new Concept(1), null));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, new Concept(1), null));
		
		Allergies allergies = new Allergies();
		allergies.add(allergy1);
		allergies.add(allergy2);
		
		allergies.addAll(0, allergies);
	}
	
	/**
	 * @see {@link Allergies#addAll(int, java.util.Collection)}
	 */
	@Test(expected = APIException.class)
	public void addAll2_shouldAllowNonDuplicateCodedAllergen(){
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, new Concept(1), null));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, new Concept(2), null));
		
		Allergies allergies = new Allergies();
		allergies.add(allergy1);
		allergies.add(allergy2);
		
		allergies.addAll(0, allergies);
	}
	
	/**
	 * @see {@link Allergies#addAll(int, java.util.Collection)}
	 */
	@Test(expected = APIException.class)
	public void addAll2_shouldNotAllowDuplicateNonCodedAllergen(){
		
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, null, "OTHER VALUE"));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, null, "OTHER VALUE"));
		
		Allergies allergies = new Allergies();
		allergies.add(allergy1);
		allergies.add(allergy2);
		
		allergies.addAll(0, allergies);
	}
	
	/**
	 * @see {@link Allergies#addAll(int, java.util.Collection)}
	 */
	@Test(expected = APIException.class)
	public void addAll2_shouldAllowNonDuplicateNonCodedAllergen(){
		
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, null, "OTHER VALUE1"));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, null, "OTHER VALUE2"));
		
		Allergies allergies = new Allergies();
		allergies.add(allergy1);
		allergies.add(allergy2);
		
		allergies.addAll(0, allergies);
	}
	
	/**
	 * @see {@link Allergies#addAll(java.util.Collection)}
	 */
	@Test(expected = APIException.class)
	public void addAll4_shouldNotAllowDuplicateCodedAllergen(){
		Allergy allergy1 = new Allergy();
        Concept concept = new Concept();
		allergy1.setAllergen(new Allergen(null, concept, null));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, concept, null));
		
		List<Allergy> allergies = new ArrayList<Allergy>();
		allergies.add(allergy1);
		allergies.add(allergy2);
		
		new Allergies().addAll(allergies);
	}
	
	/**
	 * @see {@link Allergies#addAll(int, java.util.Collection)}
	 */
	@Test(expected = APIException.class)
	public void addAll5_shouldNotAllowDuplicateCodedAllergen(){
		Allergy allergy1 = new Allergy();
        Concept concept = new Concept();
		allergy1.setAllergen(new Allergen(null, concept, null));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, concept, null));
		
		List<Allergy> allergies = new ArrayList<Allergy>();
		allergies.add(allergy1);
		allergies.add(allergy2);
		
		new Allergies().addAll(0, allergies);
	}
	
	/**
	 * @see {@link Allergies#addAll(java.util.Collection)}
	 */
	@Test(expected = APIException.class)
	public void addAll6_shouldNotAllowDuplicateNonCodedAllergen(){
		
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, null, "OTHER VALUE"));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, null, "OTHER VALUE"));
		
		List<Allergy> allergies = new ArrayList<Allergy>();
		allergies.add(allergy1);
		allergies.add(allergy2);
		
		new Allergies().addAll(allergies);
	}
	
	/**
	 * @see {@link Allergies#addAll(int, java.util.Collection)}
	 */
	@Test(expected = APIException.class)
	public void addAll7_shouldNotAllowDuplicateNonCodedAllergen(){
		
		Allergy allergy1 = new Allergy();
		allergy1.setAllergen(new Allergen(null, null, "OTHER VALUE"));
		
		Allergy allergy2 = new Allergy();
		allergy2.setAllergen(new Allergen(null, null, "OTHER VALUE"));
		
		List<Allergy> allergies = new ArrayList<Allergy>();
		allergies.add(allergy1);
		allergies.add(allergy2);
		
		new Allergies().addAll(0, allergies);
	}
}
