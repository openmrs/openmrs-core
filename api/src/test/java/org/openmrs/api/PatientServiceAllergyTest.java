/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Allergen;
import org.openmrs.AllergenType;
import org.openmrs.Allergies;
import org.openmrs.Allergy;
import org.openmrs.AllergyReaction;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;

/**
 * Tests allergy methods in {@link $ PatientService} .
 */
public class PatientServiceAllergyTest extends BaseContextSensitiveTest {
	
	private static final String ALLERGY_TEST_DATASET = "org/openmrs/api/include/allergyTestDataset.xml";
	private static final String ALLERGY_OTHER_NONCODED_TEST_DATASET = "org/openmrs/api/include/otherNonCodedConcept.xml";
	
	private PatientService allergyService;
	@Before
	public void runBeforeAllTests() {
		if (allergyService == null) {
			allergyService = Context.getPatientService();
		}

		executeDataSet(ALLERGY_OTHER_NONCODED_TEST_DATASET);
		executeDataSet(ALLERGY_TEST_DATASET);
		
		Allergen.setOtherNonCodedConceptUuid(Context.getAdministrationService().getGlobalProperty(
			    OpenmrsConstants.GP_ALLERGEN_OTHER_NON_CODED_UUID));
	}
	
	/**
	 * @see PatientService#getAllergies(Patient)
	 */
	@Test
	public void getAllergyByUuid_shouldGetAllergyByUuid() {
		Allergy allergy = allergyService.getAllergyByUuid("21543629-7d8c-11e1-909d-c80aa9edcf4e");		
		Assert.assertNotNull(allergy);
	}
	
	/**
	 * @see PatientService#getAllergies(Patient)
	 */
	@Test
	public void getAllergies_shouldGetTheAllergyListAndStatus() {
		//get a patient with some allergies
		Patient patient = allergyService.getPatient(2);
		Allergies allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.SEE_LIST, allergies.getAllergyStatus());
		Assert.assertEquals(4, allergies.size());
		
		//should properly load reactions
		Assert.assertEquals(2, allergies.get(0).getReactions().size());
		Assert.assertEquals(2, allergies.get(1).getReactions().size());
		Assert.assertEquals(0, allergies.get(2).getReactions().size());
		Assert.assertEquals(0, allergies.get(3).getReactions().size());
		
		//get a patient without allergies
		patient = allergyService.getPatient(6);
		allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.UNKNOWN, allergies.getAllergyStatus());
		Assert.assertEquals(0, allergies.size());
	}
	
	/**
	 * @see PatientService#setAllergies(Patient,Allergies)
	 */
	@Test
	public void setAllergies_shouldSaveTheAllergyListAndStatus() {
		//get a patient without any allergies
		Patient patient = allergyService.getPatient(7);
		Allergies allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.UNKNOWN, allergies.getAllergyStatus());
		Assert.assertEquals(0, allergies.size());
		
		//save some allergies for this patient
		Allergen allergen = new Allergen(AllergenType.DRUG, new Concept(3), null);
		Concept severity = new Concept(4);
		Allergy allergy = new Allergy(patient, allergen, severity, "some comment", new ArrayList<>());
		AllergyReaction reaction = new AllergyReaction(allergy, new Concept(21), null);
		allergy.addReaction(reaction);
		allergies = new Allergies();
		allergies.add(allergy);
		allergyService.setAllergies(patient, allergies);
		
		//now the patient should have allergies and the correct allergy status
		allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.SEE_LIST, allergies.getAllergyStatus());
		Assert.assertEquals(1, allergies.size());
		Assert.assertEquals(1, allergies.get(0).getReactions().size());
	}
	
	/**
	 * @see PatientService#setAllergies(Patient,Allergies)
	 */
	@Test
	public void setAllergies_shouldVoidRemovedAllergiesAndMaintainStatusAsSeeListIfSomeAllergiesAreRemoved()
	{
		//get a patient with some allergies
		Patient patient = allergyService.getPatient(2);
		Allergies allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.SEE_LIST, allergies.getAllergyStatus());
		Assert.assertEquals(4, allergies.size());
		
		//remove one allergy
		allergies.remove(0);
		
		//remove one reaction out of the two
		allergies.get(0).getReactions().remove(0);
		
		//add a reaction to the third allergy
		AllergyReaction reaction = new AllergyReaction(null, new Concept(22), null);
		allergies.get(2).addReaction(reaction);
		
		allergyService.setAllergies(patient, allergies);
		
		//should remain with three un voided allergies and status maintained as see list
		allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.SEE_LIST, allergies.getAllergyStatus());
		Assert.assertEquals(3, allergies.size());
		Assert.assertEquals(1, allergies.get(0).getReactions().size());
		Assert.assertEquals(0, allergies.get(1).getReactions().size());
		Assert.assertEquals(1, allergies.get(2).getReactions().size());
	}
	
	/**
	 * @see PatientService#setAllergies(Patient,Allergies)
	 */
	@Test
	public void setAllergies_shouldVoidAllAllergiesAndSetStatusToUnknownIfAllAllergiesAreRemoved() {
		//get a patient with some allergies
		Patient patient = allergyService.getPatient(2);
		Allergies allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.SEE_LIST, allergies.getAllergyStatus());
		Assert.assertEquals(4, allergies.size());
		
		//remove all allergies
		while (allergies.size() > 0) {
			allergies.remove(0);
		}
		
		allergyService.setAllergies(patient, allergies);
		
		//all allergies should be voided and status set to unknown
		allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.UNKNOWN, allergies.getAllergyStatus());
		Assert.assertEquals(0, allergies.size());
	}
	
	/**
	 * @see PatientService#setAllergies(Patient,Allergies)
	 */
	@Test
	public void setAllergies_shouldVoidAllAllergiesAndSetStatusToNoKnownAllergiesIfAllAllergiesAreRemovedAndStatusSetAsSuch()
	{
		//get a patient with some allergies
		Patient patient = allergyService.getPatient(2);
		Allergies allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.SEE_LIST, allergies.getAllergyStatus());
		Assert.assertEquals(4, allergies.size());
		
		//remove all allergies
		while (allergies.size() > 0) {
			allergies.remove(0);
		}
		
		//set the status to no known allergies
		allergies.confirmNoKnownAllergies();
		allergyService.setAllergies(patient, allergies);
		
		//all allergies should be voided and status set to no known allergies
		allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.NO_KNOWN_ALLERGIES, allergies.getAllergyStatus());
		Assert.assertEquals(0, allergies.size());
	}
	
	/**
	 * @see PatientService#setAllergies(Patient,Allergies)
	 */
	@Test
	public void setAllergies_shouldSetStatusToNoKnownAllergiesForPatientWithoutAllergies()
	{
		//get a patient without any allergies
		Patient patient = allergyService.getPatient(7);
		Allergies allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.UNKNOWN, allergies.getAllergyStatus());
		Assert.assertEquals(0, allergies.size());
		
		//confirm that patient has no known allergies
		allergies = new Allergies();
		allergies.confirmNoKnownAllergies();
		allergyService.setAllergies(patient, allergies);
		
		//now the patient should have the no known allergies status
		allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.NO_KNOWN_ALLERGIES, allergies.getAllergyStatus());
		Assert.assertEquals(0, allergies.size());
	}
	
	/**
	 * @see PatientService#setAllergies(Patient,Allergies)
	 */
	@Test
	public void setAllergies_shouldVoidAllergiesWithEditedComment()
	{
		
		//get a patient with some allergies
		Patient patient = allergyService.getPatient(2);
		Allergies allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.SEE_LIST, allergies.getAllergyStatus());
		Assert.assertEquals(4, allergies.size());
				
		Allergy editedAllergy = allergies.get(0);
		//clear any cache for this object such that the next calls fetch it from the database
		Context.evictFromSession(editedAllergy);
		//edit comment
		editedAllergy.setComment("edited comment");
		
		Assert.assertTrue(allergies.contains(editedAllergy));

		allergyService.setAllergies(patient, allergies);
		
		//should remain with four unvoided allergies and status maintained as see list
		allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.SEE_LIST, allergies.getAllergyStatus());
		Assert.assertEquals(4, allergies.size());
		
		//the edited allergy should have been voided
		Assert.assertFalse(allergies.contains(editedAllergy));
	}
	
	/**
	 * @see PatientService#setAllergies(Patient,Allergies)
	 */
	@Test
	public void setAllergies_shouldVoidAllergiesWithEditedSeverity()
	{
		
		//get a patient with some allergies
		Patient patient = allergyService.getPatient(2);
		Allergies allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.SEE_LIST, allergies.getAllergyStatus());
		Assert.assertEquals(4, allergies.size());
				
		Allergy editedAllergy = allergies.get(0);
		//clear any cache for this object such that the next calls fetch it from the database
		Context.evictFromSession(editedAllergy);
		//edit severity
		editedAllergy.setSeverity(new Concept(24));
		
		Assert.assertTrue(allergies.contains(editedAllergy));

		allergyService.setAllergies(patient, allergies);
		
		//should remain with four unvoided allergies and status maintained as see list
		allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.SEE_LIST, allergies.getAllergyStatus());
		Assert.assertEquals(4, allergies.size());
		
		//the edited allergy should have been voided
		Assert.assertFalse(allergies.contains(editedAllergy));
	}
	
	/**
	 * @see PatientService#setAllergies(Patient,Allergies)
	 */
	@Test
	public void setAllergies_shouldVoidAllergiesWithEditedCodedAllergen()
	{
		
		//get a patient with some allergies
		Patient patient = allergyService.getPatient(2);
		Allergies allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.SEE_LIST, allergies.getAllergyStatus());
		Assert.assertEquals(4, allergies.size());
				
		Allergy editedAllergy = allergies.get(0);
		//clear any cache for this object such that the next calls fetch it from the database
		Context.evictFromSession(editedAllergy);
		//edit coded allergen
		editedAllergy.getAllergen().setCodedAllergen(new Concept(24));
		
		Assert.assertTrue(allergies.contains(editedAllergy));

		allergyService.setAllergies(patient, allergies);
		
		//should remain with four unvoided allergies and status maintained as see list
		allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.SEE_LIST, allergies.getAllergyStatus());
		Assert.assertEquals(4, allergies.size());
		
		//the edited allergy should have been voided
		Assert.assertFalse(allergies.contains(editedAllergy));
	}
	
	/**
	 * @see PatientService#setAllergies(Patient,Allergies)
	 */
	@Test
	public void setAllergies_shouldVoidAllergiesWithEditedNonCodedAllergen()
	{
		
		//get a patient with some allergies
		Patient patient = allergyService.getPatient(2);
		Allergies allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.SEE_LIST, allergies.getAllergyStatus());
		Assert.assertEquals(4, allergies.size());
				
		Allergy editedAllergy = allergies.get(0);
		//clear any cache for this object such that the next calls fetch it from the database
		Context.evictFromSession(editedAllergy);
		//edit non coded allergen
		editedAllergy.getAllergen().setNonCodedAllergen("some non coded allergen");
		
		Assert.assertTrue(allergies.contains(editedAllergy));

		allergyService.setAllergies(patient, allergies);
		
		//should remain with four unvoided allergies and status maintained as see list
		allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.SEE_LIST, allergies.getAllergyStatus());
		Assert.assertEquals(4, allergies.size());
		
		//the edited allergy should have been voided
		Assert.assertFalse(allergies.contains(editedAllergy));
	}
	
	/**
	 * @see PatientService#setAllergies(Patient,Allergies)
	 */
	@Test
	public void setAllergies_shouldVoidAllergiesWithRemovedReactions()
	{
		
		//get a patient with some allergies
		Patient patient = allergyService.getPatient(2);
		Allergies allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.SEE_LIST, allergies.getAllergyStatus());
		Assert.assertEquals(4, allergies.size());
				
		Allergy editedAllergy = allergies.get(0);
		//clear any cache for this object such that the next calls fetch it from the database
		Context.evictFromSession(editedAllergy);
		//remove a reaction
		editedAllergy.getReactions().remove(0);
		
		Assert.assertTrue(allergies.contains(editedAllergy));

		allergyService.setAllergies(patient, allergies);
		
		//should remain with four unvoided allergies and status maintained as see list
		allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.SEE_LIST, allergies.getAllergyStatus());
		Assert.assertEquals(4, allergies.size());
		
		//the edited allergy should have been voided
		Assert.assertFalse(allergies.contains(editedAllergy));
	}
	
	/**
	 * @see PatientService#setAllergies(Patient,Allergies)
	 */
	@Test
	public void setAllergies_shouldVoidAllergiesWithAddedReactions()
	{
		
		//get a patient with some allergies
		Patient patient = allergyService.getPatient(2);
		Allergies allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.SEE_LIST, allergies.getAllergyStatus());
		Assert.assertEquals(4, allergies.size());
				
		Allergy editedAllergy = allergies.get(0);
		//clear any cache for this object such that the next calls fetch it from the database
		Context.evictFromSession(editedAllergy);
		//add a reaction
		AllergyReaction reaction = new AllergyReaction(null, new Concept(22), null);
		editedAllergy.addReaction(reaction);
		
		Assert.assertTrue(allergies.contains(editedAllergy));

		allergyService.setAllergies(patient, allergies);
		
		//should remain with four unvoided allergies and status maintained as see list
		allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.SEE_LIST, allergies.getAllergyStatus());
		Assert.assertEquals(4, allergies.size());
		
		//the edited allergy should have been voided
		Assert.assertFalse(allergies.contains(editedAllergy));
	}
	
	/**
	 * @see PatientService#setAllergies(Patient,Allergies)
	 */
	@Test
	public void setAllergies_shouldVoidAllergiesWithEditedReactionCoded()
	{
		
		//get a patient with some allergies
		Patient patient = allergyService.getPatient(2);
		Allergies allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.SEE_LIST, allergies.getAllergyStatus());
		Assert.assertEquals(4, allergies.size());
				
		Allergy editedAllergy = allergies.get(0);
		//clear any cache for this object such that the next calls fetch it from the database
		Context.evictFromSession(editedAllergy);
		//edit a reaction
		AllergyReaction reaction = editedAllergy.getReactions().get(0);
		reaction.setReaction(new Concept(11));
		
		Assert.assertTrue(allergies.contains(editedAllergy));

		allergyService.setAllergies(patient, allergies);
		
		//should remain with four unvoided allergies and status maintained as see list
		allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.SEE_LIST, allergies.getAllergyStatus());
		Assert.assertEquals(4, allergies.size());
		
		//the edited allergy should have been voided
		Assert.assertFalse(allergies.contains(editedAllergy));
	}
	
	/**
	 * @see PatientService#setAllergies(Patient,Allergies)
	 */
	@Test
	public void setAllergies_shouldVoidAllergiesWithEditedReactionNonCoded()
	{
		
		//get a patient with some allergies
		Patient patient = allergyService.getPatient(2);
		Allergies allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.SEE_LIST, allergies.getAllergyStatus());
		Assert.assertEquals(4, allergies.size());
				
		Allergy editedAllergy = allergies.get(0);
		//clear any cache for this object such that the next calls fetch it from the database
		Context.evictFromSession(editedAllergy);
		//edit a reaction
		AllergyReaction reaction = editedAllergy.getReactions().get(0);
		reaction.setReactionNonCoded("some non coded text");
		
		Assert.assertTrue(allergies.contains(editedAllergy));

		allergyService.setAllergies(patient, allergies);
		
		//should remain with four unvoided allergies and status maintained as see list
		allergies = allergyService.getAllergies(patient);
		Assert.assertEquals(Allergies.SEE_LIST, allergies.getAllergyStatus());
		Assert.assertEquals(4, allergies.size());
		
		//the edited allergy should have been voided
		Assert.assertFalse(allergies.contains(editedAllergy));
	}

	@Test
	public void setAllergies_shouldSetTheNonCodedConceptForNonCodedAllergenIfNotSpecified() {
        Patient patient = allergyService.getPatient(2);
        Allergen allergen = new Allergen(AllergenType.DRUG, null, "Some allergy name");
        Allergy allergy = new Allergy(patient, allergen, null, null, null);
        Allergies allergies = allergyService.getAllergies(patient);
        allergies.add(allergy);
        allergyService.setAllergies(patient, allergies);
        Assert.assertFalse(allergy.getAllergen().isCoded());
    }
}
