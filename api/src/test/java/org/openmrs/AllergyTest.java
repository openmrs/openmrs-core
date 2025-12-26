package org.openmrs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import java.util.Arrays;

public class AllergyTest extends BaseContextSensitiveTest {
	
	private static final String ALLERGY_TEST_DATASET = "org/openmrs/api/include/allergyTestDataset.xml";
	private Allergy allergy1;
	private Allergy allergy2;
	
	@BeforeEach
	public void setup() {
		executeDataSet(ALLERGY_TEST_DATASET);
		
		Patient patient = Context.getPatientService().getPatient(2);
		Concept concept1 = Context.getConceptService().getConcept(3);
		Concept concept2 = Context.getConceptService().getConcept(4);
		
		Allergen allergen1 = new Allergen(AllergenType.DRUG, concept1, null);
		Allergen allergen2 = new Allergen(AllergenType.DRUG, concept1, null);
		
		Allergy baseAllergy = new Allergy();
		baseAllergy.setPatient(patient);
		baseAllergy.setAllergen(allergen1);
		
		AllergyReaction reaction1 = new AllergyReaction(baseAllergy, concept1, null); // Providing baseAllergy as the Allergy object
		AllergyReaction reaction2 = new AllergyReaction(baseAllergy, concept1, null); // Providing baseAllergy as the Allergy object
		
		allergy1 = new Allergy(patient, allergen1, concept1, "Comment 1", Arrays.asList(reaction1));
		allergy2 = new Allergy(patient, allergen2, concept1, "Comment 1", Arrays.asList(reaction2));
	}
	
	@Test
	public void hasSameValues_shouldReturnTrueForIdenticalAllergies() {
		assertTrue(allergy1.hasSameValues(allergy2));
	}
	
	@Test
	public void hasSameValues_shouldReturnFalseForDifferentAllergen() {
		allergy2.getAllergen().setCodedAllergen(Context.getConceptService().getConcept(5));
		assertFalse(allergy1.hasSameValues(allergy2));
	}
	
	@Test
	public void hasSameValues_shouldReturnFalseForDifferentSeverity() {
		allergy2.setSeverity(Context.getConceptService().getConcept(5));
		assertFalse(allergy1.hasSameValues(allergy2));
	}
	
	@Test
	public void hasSameValues_shouldReturnFalseForDifferentComments() {
		allergy2.setComments("Different comment");
		assertFalse(allergy1.hasSameValues(allergy2));
	}
	
	@Test
	public void hasSameValues_shouldReturnFalseForDifferentReactions() {
		AllergyReaction differentReaction = new AllergyReaction(allergy1, Context.getConceptService().getConcept(5), null); // Providing baseAllergy and different Concept
		allergy2.setReactions(Arrays.asList(differentReaction));
		assertFalse(allergy1.hasSameValues(allergy2));
	}
}
