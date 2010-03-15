package org.openmrs.validator;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.api.APIException;
import org.openmrs.api.DuplicateConceptNameException;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link ConceptValidator} class.
 */
public class ConceptValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @verifies {@link ConceptValidator#validate(Object,Errors)} test = should fail if there is a
	 *           duplicate unretired concept name in the locale
	 */
	@Test(expected = DuplicateConceptNameException.class)
	@Verifies(value = "should fail if there is a duplicate unretired concept name in the locale", method = "validate(Concept)")
	public void validate_shouldFailIfThereIsADuplicateUnretiredConceptNameInTheLocale() throws Exception {
		
		Concept concept = new Concept();
		concept.setPreferredName(Context.getLocale(), new ConceptName("CD4 COUNT", Context.getLocale()));
		Errors errors = new BindException(concept, "concept");
		new ConceptValidator().validate(concept, errors);
	}
	
	@Test(expected = APIException.class)
	@Verifies(value = "should fail if the preferred name is an empty string", method = "validate(Concept)")
	public void validate_shouldFailIfThePreferredNameIsAnEmptyString() throws Exception {
		
		Concept concept = new Concept();
		concept.setPreferredName(Context.getLocale(), new ConceptName("", Context.getLocale()));
		Errors errors = new BindException(concept, "concept");
		new ConceptValidator().validate(concept, errors);
		Assert.assertTrue(errors.hasErrors());
		
	}
	
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "should fail if the object parameter is null", method = "validate(Concept)")
	public void validate_shouldFailIfTheObjectParameterIsNull() throws Exception {
		
		Errors errors = new BindException(null, "concept");
		new ConceptValidator().validate(null, errors);
		Assert.assertTrue(errors.hasErrors());
	}
	
	/**
	 * @verifies {@link ConceptValidator#validate(Object,Errors)} test = should pass if the found
	 *           duplicate concept is actually retired
	 */
	@Test
	@Verifies(value = "should pass if the found duplicate concept is actually retired", method = "validate(Concept)")
	public void validate_shouldPassIfTheFoundDuplicateConceptIsActuallyRetired() throws Exception {
		Concept c = Context.getConceptService().getConcept(5497);
		//comment out this line or change the value to false and the test should fail
		c.setRetired(true);
		
		Concept concept = new Concept();
		concept.setPreferredName(Context.getLocale(), new ConceptName("cd4 count", Context.getLocale()));
		Errors errors = new BindException(concept, "concept");
		new ConceptValidator().validate(concept, errors);
		Assert.assertTrue(!errors.hasErrors());
	}
	
	@Test
	@Verifies(value = "should pass if the concept is being updated with no name change", method = "validate(Concept)")
	public void validate_shouldPassIfTheConceptIsBeingUpdatedWithNoNameChange() throws Exception {
		
		Concept conceptToUpdate = Context.getConceptService().getConcept(5497);
		Errors errors = new BindException(conceptToUpdate, "concept");
		conceptToUpdate.setCreator(Context.getAuthenticatedUser());
		new ConceptValidator().validate(conceptToUpdate, errors);
		Assert.assertFalse(errors.hasErrors());
		
	}
	
}
