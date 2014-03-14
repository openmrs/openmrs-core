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
package org.openmrs.validator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.Drug;
import org.openmrs.DrugReferenceMap;
import org.openmrs.api.ConceptService;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Contains tests methods for the {@link DrugValidator}
 */
public class DrugValidatorTest extends BaseContextSensitiveTest {
	
	@Autowired
	private ConceptService conceptService;
	
	protected static final String GET_DRUG_MAPPINGS = "org/openmrs/api/include/ConceptServiceTest-getDrugMappings.xml";
	
	@Before
	public void executeDrugMappingsDataSet() throws Exception {
		executeDataSet(GET_DRUG_MAPPINGS);
	}
	
	/**
	 * @see {@link DrugValidator#validate(Object, Errors)}
	 */
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "should fail if the drug object is null", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheDrugObjectIsNull() {
		Errors errors = new BindException(new Drug(), "drug");
		new DrugValidator().validate(null, errors);
	}
	
	/**
	 * @see {@link DrugValidator#validate(Object, Errors)}
	 */
	@Test
	@Verifies(value = "should fail if the drug on any of drugReferenceMap is null", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheDrugObjectOnDrugReferenceMapIsNull() {
		Drug drug = conceptService.getDrug(11);
		Errors errors = new BindException(drug, "drug");
		
		for (DrugReferenceMap drugReferenceMap : drug.getDrugReferenceMaps()) {
			if (drugReferenceMap.getId().equals(3)) {
				drugReferenceMap.setDrug(null);
			}
		}
		
		new DrugValidator().validate(drug, errors);
		
		Assert.assertEquals(true, errors.hasFieldErrors("drugReferenceMaps[0].drug"));
	}
	
	/**
	 * @see {@link DrugValidator#validate(Object, Errors)}
	 */
	@Test
	@Verifies(value = "should fail if the conceptReferenceTerm on any of drugReferenceMap is null", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheConceptReferenceTermOnDrugReferenceMapIsNull() {
		Drug drug = conceptService.getDrug(11);
		Errors errors = new BindException(drug, "drug");
		
		for (DrugReferenceMap drugReferenceMap : drug.getDrugReferenceMaps()) {
			if (drugReferenceMap.getId().equals(3)) {
				drugReferenceMap.setConceptReferenceTerm(null);
			}
		}
		
		new DrugValidator().validate(drug, errors);
		
		Assert.assertEquals(true, errors.hasFieldErrors("drugReferenceMaps[0].conceptReferenceTerm"));
	}
	
	/**
	 * @see {@link DrugValidator#validate(Object, Errors)}
	 */
	@Test
	@Verifies(value = "should fail if the conceptMapType on any of drugReferenceMap is null", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheConceptConceptMapTypeOnDrugReferenceMapIsNull() {
		Drug drug = conceptService.getDrug(11);
		Errors errors = new BindException(drug, "drug");
		for (DrugReferenceMap drugReferenceMap : drug.getDrugReferenceMaps()) {
			if (drugReferenceMap.getId().equals(3)) {
				drugReferenceMap.setConceptMapType(null);
			}
		}
		
		new DrugValidator().validate(drug, errors);
		
		Assert.assertEquals(true, errors.hasFieldErrors("drugReferenceMaps[0].conceptMapType"));
	}
	
	/**
	 * @see {@link DrugValidator#validate(Object, Errors)}
	 */
	@Test
	@Verifies(value = "should invoke ConceptReferenceTermValidator and fail if conceptReferenceTerm is new (no id set) and "
	        + "conceptReferenceTerm.code is null on any of drugReferenceMap", method = "validate(Object,Errors)")
	public void validate_shouldInvokeConceptReferenceTermValidatorIfTheReferenceTermInDrugReferenceMapIsNew() {
		Drug drug = conceptService.getDrug(11);
		Errors errors = new BindException(drug, "drug");
		ConceptReferenceTerm newConceptReferenceTerm = new ConceptReferenceTerm();
		
		newConceptReferenceTerm.setConceptSource(conceptService.getConceptSource(1));
		
		for (DrugReferenceMap drugReferenceMap : drug.getDrugReferenceMaps()) {
			if (drugReferenceMap.getId().equals(3)) {
				drugReferenceMap.setConceptReferenceTerm(newConceptReferenceTerm);
			}
		}
		
		new DrugValidator().validate(drug, errors);
		
		Assert.assertEquals(true, errors.hasFieldErrors("drugReferenceMaps[0].conceptReferenceTerm.code"));
	}
	
	/**
	 * @see {@link DrugValidator#validate(Object, Errors)}
	 */
	@Test
	@Verifies(value = "should invoke ConceptMapTypeValidator and fail if conceptMapType is new (no id set) and "
	        + "conceptMapType.name is null on any of drugReferenceMap", method = "validate(Object,Errors)")
	public void validate_shouldInvokeConceptMapTypeValidatorIfTheConceptMapTypeInDrugReferenceMapIsNew() {
		Drug drug = conceptService.getDrug(11);
		
		ConceptMapType newConceptMapType = new ConceptMapType();
		
		for (DrugReferenceMap drugReferenceMap : drug.getDrugReferenceMaps()) {
			if (drugReferenceMap.getId().equals(3)) {
				drugReferenceMap.setConceptMapType(newConceptMapType);
			}
		}
		Errors errors = new BindException(drug, "drug");
		new DrugValidator().validate(drug, errors);
		
		Assert.assertEquals(true, errors.hasFieldErrors("drugReferenceMaps[0].conceptMapType.name"));
	}
}
