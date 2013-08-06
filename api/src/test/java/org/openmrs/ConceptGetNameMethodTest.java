package org.openmrs;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class ConceptGetNameMethodTest extends BaseContextSensitiveTest {
	
	@Test
	public void getName_shouldReturnNameInFrenchIfExistFrenchNameAndDefaultLocaleIsEnglish() {
		
		Concept concept = new Concept();
		ConceptName frenchConceptName = new ConceptName("frenchName", Locale.FRENCH);
		
		ConceptName englishConceptName = new ConceptName("enqlishName", Locale.ENGLISH);
		
		concept.addName(englishConceptName);
		concept.addName(frenchConceptName);
		
		//add French name
		
		ConceptName givenName = concept.getName(Locale.FRENCH);
		assertEquals(frenchConceptName, givenName);
	}
	
}
