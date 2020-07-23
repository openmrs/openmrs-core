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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptReferenceTermMap;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Contains tests methods for the {@link ConceptReferenceTermValidator}
 */
public class ConceptReferenceTermValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see ConceptReferenceTermValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfTheCodeIsAWhiteSpaceCharacter() {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("name");
		term.setCode(" ");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		assertTrue(errors.hasFieldErrors("code"));
	}
	
	/**
	 * @see ConceptReferenceTermValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfTheCodeIsAnEmptyString() {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("name");
		term.setCode("");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		assertTrue(errors.hasFieldErrors("code"));
	}
	
	/**
	 * @see ConceptReferenceTermValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfTheCodeIsNull() {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("name");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		assertTrue(errors.hasFieldErrors("code"));
	}
	
	/**
	 * @see ConceptReferenceTermValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfTheConceptReferenceTermCodeIsADuplicateInItsConceptSource() {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("name");
		term.setCode("WGT234");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		assertTrue(errors.hasFieldErrors("code"));
	}
	
	/**
	 * @see ConceptReferenceTermValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfTheConceptReferenceTermObjectIsNull() {
		Errors errors = new BindException(new ConceptReferenceTerm(), "term");
		assertThrows(IllegalArgumentException.class, () -> new ConceptReferenceTermValidator().validate(null, errors));
	}
	
	/**
	 * @see ConceptReferenceTermValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfTheConceptSourceIsNull() {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("name");
		term.setCode("code");
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		assertTrue(errors.hasFieldErrors("conceptSource"));
	}
	
	/**
	 * @see ConceptReferenceTermValidator#validate(Object,Errors)
	 */
	@Test
	@Disabled
	//we might need these back when the constraint is put back
	public void validate_shouldFailIfTheNameIsAWhiteSpaceCharacter() {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName(" ");
		term.setCode("code");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see ConceptReferenceTermValidator#validate(Object,Errors)
	 */
	@Test
	@Disabled
	public void validate_shouldFailIfTheNameIsAnEmptyString() {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("");
		term.setCode("code");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see ConceptReferenceTermValidator#validate(Object,Errors)
	 */
	@Test
	@Disabled
	public void validate_shouldFailIfTheNameIsNull() {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setCode("code");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see ConceptReferenceTermValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassIfAllTheRequiredFieldsAreSetAndValid() {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("name");
		term.setCode("code");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ConceptReferenceTermValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassIfTheDuplicateCodeIsForATermFromAnotherConceptSource() {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("unique name");
		//set to a duplicate code for a term from another source
		term.setCode("2332523");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ConceptReferenceTermValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassIfTheDuplicateNameIsForATermFromAnotherConceptSource() {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		//set to a duplicate name for a term from another source
		term.setName("weight term2");
		term.setCode("unique code");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ConceptReferenceTermValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfAConceptReferenceTermMapHasNoConceptMapType() {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("name");
		term.setCode("code");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		term.addConceptReferenceTermMap(new ConceptReferenceTermMap(new ConceptReferenceTerm(1), null));
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		assertTrue(errors.hasFieldErrors("conceptReferenceTermMaps[0].conceptMapType"));
	}
	
	/**
	 * @see ConceptReferenceTermValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfTermBOfAConceptReferenceTermMapIsNotSet() {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("name");
		term.setCode("code");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		Set<ConceptReferenceTermMap> maps = new LinkedHashSet<>();
		maps.add(new ConceptReferenceTermMap(null, new ConceptMapType(1)));
		term.setConceptReferenceTermMaps(maps);
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		assertTrue(errors.hasFieldErrors("conceptReferenceTermMaps[0].termB"));
	}
	
	/**
	 * @see ConceptReferenceTermValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfATermIsMappedToItself() {
		ConceptReferenceTerm term = Context.getConceptService().getConceptReferenceTerm(1);
		Set<ConceptReferenceTermMap> maps = term.getConceptReferenceTermMaps();
		ConceptReferenceTermMap invalidMap = maps.iterator().next();
		invalidMap.setTermB(term);
		term.setConceptReferenceTermMaps(maps);
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		assertTrue(errors.hasFieldErrors("conceptReferenceTermMaps[0].termB"));
	}
	
	/**
	 * @see ConceptReferenceTermValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfATermIsMappedMultipleTimesToTheSameTerm() {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		ConceptService cs = Context.getConceptService();
		term.setCode("unique code");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		ConceptReferenceTermMap map1 = new ConceptReferenceTermMap(cs.getConceptReferenceTerm(1), cs.getConceptMapType(1));
		term.addConceptReferenceTermMap(map1);
		//test should fail if we change the term below
		ConceptReferenceTermMap map2 = new ConceptReferenceTermMap(cs.getConceptReferenceTerm(1), cs.getConceptMapType(1));
		term.addConceptReferenceTermMap(map2);
		
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		System.err.println(errors.getAllErrors());
		
		//the term for second mapping should be rejected
		assertTrue(errors.hasFieldErrors("conceptReferenceTermMaps[1].termB"));
	}
	
	/**
	 * @see ConceptReferenceTermValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("name");
		term.setCode("code");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		term.setVersion("version");
		term.setDescription("Description");
		term.setRetireReason("RetireReason");
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ConceptReferenceTermValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term
		        .setName("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		term
		        .setCode("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		term
		        .setVersion("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		term
		        .setDescription("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		term
		        .setRetireReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		assertTrue(errors.hasFieldErrors("name"));
		assertTrue(errors.hasFieldErrors("code"));
		assertTrue(errors.hasFieldErrors("version"));
		assertTrue(errors.hasFieldErrors("description"));
		assertTrue(errors.hasFieldErrors("retireReason"));
	}
}
