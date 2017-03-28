/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.validator;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.Drug;
import org.openmrs.DrugReferenceMap;
import org.openmrs.api.ConceptService;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Contains tests methods for the {@link DrugValidator}
 */
public class DrugValidatorTest extends BaseContextSensitiveTest {
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Autowired
	private ConceptService conceptService;
	
	protected static final String GET_DRUG_MAPPINGS = "org/openmrs/api/include/ConceptServiceTest-getDrugMappings.xml";
	
	@Before
	public void executeDrugMappingsDataSet() {
		executeDataSet(GET_DRUG_MAPPINGS);
	}
	
	/**
	 * @see DrugValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfTheDrugObjectIsNull() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("The parameter obj should not be null and must be of type" + Drug.class);
		new DrugValidator().validate(null, new BindException(new Drug(), "drug"));
	}
	
	/**
	 * @see DrugValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfDrugOnDrugReferenceMapIsNull() {
		Drug drug = new Drug();
		drug.setDrugReferenceMaps(Collections.singleton(new DrugReferenceMap()));
		Errors errors = new BindException(drug, "drug");
		new DrugValidator().validate(drug, errors);
		Assert.assertTrue(errors.hasFieldErrors("drugReferenceMaps[0].drug"));
	}
	
	/**
	 * @see DrugValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfConceptReferenceTermOnDrugReferenceMapIsNull() {
		Drug drug = new Drug();
		drug.addDrugReferenceMap(new DrugReferenceMap());
		Errors errors = new BindException(drug, "drug");
		new DrugValidator().validate(drug, errors);
		Assert.assertTrue(errors.hasFieldErrors("drugReferenceMaps[0].conceptReferenceTerm"));
	}
	
	/**
	 * @see DrugValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldInvokeConceptReferenceTermValidatorIfTermOnDrugReferenceMapIsNew() {
		Drug drug = new Drug();
		drug.addDrugReferenceMap(new DrugReferenceMap(new ConceptReferenceTerm(), null));
		Errors errors = new BindException(drug, "drug");
		new DrugValidator().validate(drug, errors);
		//reference term validator should have been called which should reject a null code
		Assert.assertTrue(errors.hasFieldErrors("drugReferenceMaps[0].conceptReferenceTerm.code"));
	}
	
	/**
	 * @see DrugValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldInvokeConceptMapTypeValidatorIfConceptMapTypeOnDrugReferenceMapIsNew() {
		Drug drug = new Drug();
		drug.addDrugReferenceMap(new DrugReferenceMap(conceptService.getConceptReferenceTerm(1), new ConceptMapType()));
		Errors errors = new BindException(drug, "drug");
		new DrugValidator().validate(drug, errors);
		//concept map type validator should have been called which should reject a null name
		Assert.assertTrue(errors.hasFieldErrors("drugReferenceMaps[0].conceptMapType.name"));
	}
	
	/**
	 * @see DrugValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldRejectDrugMultipleMappingsToTheSameTerm() {
		Drug drug = new Drug();
		DrugReferenceMap term1 = new DrugReferenceMap(conceptService.getConceptReferenceTerm(1), conceptService
		        .getConceptMapType(1));
		DrugReferenceMap term2 = new DrugReferenceMap(conceptService.getConceptReferenceTerm(1), conceptService
		        .getConceptMapType(2));
		drug.addDrugReferenceMap(term1);
		drug.addDrugReferenceMap(term2);
		Assert.assertEquals(2, drug.getDrugReferenceMaps().size());
		Errors errors = new BindException(drug, "drug");
		new DrugValidator().validate(drug, errors);
		Assert.assertTrue(errors.hasFieldErrors("drugReferenceMaps[1].conceptReferenceTerm"));
	}
	
	/**
	 * @see DrugValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassIfAllFieldsAreCorrect() {
		Drug drug = new Drug();
		drug.addDrugReferenceMap(new DrugReferenceMap(conceptService.getConceptReferenceTerm(1), conceptService
		        .getConceptMapType(1)));
		Errors errors = new BindException(drug, "drug");
		new DrugValidator().validate(drug, errors);
		Assert.assertFalse(errors.hasFieldErrors());
	}
	
	/**
	 * @see DrugValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		Drug drug = new Drug();
		drug.addDrugReferenceMap(new DrugReferenceMap(conceptService.getConceptReferenceTerm(1), conceptService
		        .getConceptMapType(1)));
		drug.setName("name");
		drug.setStrength("strength");
		drug.setRetireReason("retireReason");
		Errors errors = new BindException(drug, "drug");
		new DrugValidator().validate(drug, errors);
		Assert.assertFalse(errors.hasFieldErrors());
	}
	
	/**
	 * @see DrugValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		Drug drug = new Drug();
		drug.addDrugReferenceMap(new DrugReferenceMap(conceptService.getConceptReferenceTerm(1), conceptService
		        .getConceptMapType(1)));
		drug
		        .setName("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		drug
		        .setStrength("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		drug
		        .setRetireReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		Errors errors = new BindException(drug, "drug");
		new DrugValidator().validate(drug, errors);
		Assert.assertTrue(errors.hasFieldErrors("name"));
		Assert.assertTrue(errors.hasFieldErrors("strength"));
		Assert.assertTrue(errors.hasFieldErrors("retireReason"));
	}
}
