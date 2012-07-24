package org.openmrs.validator;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Drug;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link ConceptDrugValidator} class.
 */
public class ConceptDrugValidatorTest {
	
	/**
	 * @see ConceptDrugValidator#validate(Object,Errors)
	 * @verifies fail if a concept is not specified
	 */
	@Test
	public void validate_shouldFailIfAConceptIsNotSpecified() throws Exception {
		Drug drug = new Drug();
		Errors errors = new BindException(drug, "drug");
		new ConceptDrugValidator().validate(drug, errors);
		Assert.assertTrue(errors.hasErrors());
	}
	
	/**
	 * @see ConceptDrugValidator#supports(Class)
	 * @verifies reject classes not extending Drug
	 */
	@Test
	public void supports_shouldRejectClassesNotExtendingDrug() throws Exception {
		Assert.assertFalse(new ConceptDrugValidator().supports(String.class));
	}
	
	/**
	 * @see ConceptDrugValidator#supports(Class)
	 * @verifies support Drug class
	 */
	@Test
	public void supports_shouldSupportDrug() throws Exception {
		Assert.assertTrue(new ConceptDrugValidator().supports(Drug.class));
	}
	
}
