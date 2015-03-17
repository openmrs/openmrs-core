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

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptReferenceTermMap;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Contains tests methods for the {@link ConceptReferenceTermValidator}
 */
public class ConceptReferenceTermValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link ConceptReferenceTermValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if the code is a white space character", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheCodeIsAWhiteSpaceCharacter() throws Exception {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("name");
		term.setCode(" ");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("code"));
	}
	
	/**
	 * @see {@link ConceptReferenceTermValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if the code is an empty string", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheCodeIsAnEmptyString() throws Exception {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("name");
		term.setCode("");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("code"));
	}
	
	/**
	 * @see {@link ConceptReferenceTermValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if the code is null", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheCodeIsNull() throws Exception {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("name");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("code"));
	}
	
	/**
	 * @see {@link ConceptReferenceTermValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if the concept reference term code is a duplicate in its concept source", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheConceptReferenceTermCodeIsADuplicateInItsConceptSource() throws Exception {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("name");
		term.setCode("WGT234");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("code"));
	}
	
	/**
	 * @see {@link ConceptReferenceTermValidator#validate(Object,Errors)}
	 */
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "should fail if the concept reference term object is null", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheConceptReferenceTermObjectIsNull() throws Exception {
		Errors errors = new BindException(new ConceptReferenceTerm(), "term");
		new ConceptReferenceTermValidator().validate(null, errors);
	}
	
	/**
	 * @see {@link ConceptReferenceTermValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if the concept source is null", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheConceptSourceIsNull() throws Exception {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("name");
		term.setCode("code");
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("conceptSource"));
	}
	
	/**
	 * @see {@link ConceptReferenceTermValidator#validate(Object,Errors)}
	 */
	@Test
	@Ignore
	//we might need these back when the constraint is put back
	@Verifies(value = "should fail if the name is a white space character", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheNameIsAWhiteSpaceCharacter() throws Exception {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName(" ");
		term.setCode("code");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link ConceptReferenceTermValidator#validate(Object,Errors)}
	 */
	@Test
	@Ignore
	@Verifies(value = "should fail if the name is an empty string", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheNameIsAnEmptyString() throws Exception {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("");
		term.setCode("code");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link ConceptReferenceTermValidator#validate(Object,Errors)}
	 */
	@Test
	@Ignore
	@Verifies(value = "should fail if the name is null", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheNameIsNull() throws Exception {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setCode("code");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link ConceptReferenceTermValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass if all the required fields are set and valid", method = "validate(Object,Errors)")
	public void validate_shouldPassIfAllTheRequiredFieldsAreSetAndValid() throws Exception {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("name");
		term.setCode("code");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		Assert.assertEquals(false, errors.hasErrors());
	}
	
	/**
	 * @see {@link ConceptReferenceTermValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass if the duplicate code is for a term from another concept source", method = "validate(Object,Errors)")
	public void validate_shouldPassIfTheDuplicateCodeIsForATermFromAnotherConceptSource() throws Exception {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("unique name");
		//set to a duplicate code for a term from another source
		term.setCode("2332523");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		Assert.assertEquals(false, errors.hasErrors());
	}
	
	/**
	 * @see {@link ConceptReferenceTermValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass if the duplicate name is for a term from another concept source", method = "validate(Object,Errors)")
	public void validate_shouldPassIfTheDuplicateNameIsForATermFromAnotherConceptSource() throws Exception {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		//set to a duplicate name for a term from another source
		term.setName("weight term2");
		term.setCode("unique code");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		Assert.assertEquals(false, errors.hasErrors());
	}
	
	/**
	 * @see {@link ConceptReferenceTermValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if a concept reference term map has no concept map type", method = "validate(Object,Errors)")
	public void validate_shouldFailIfAConceptReferenceTermMapHasNoConceptMapType() throws Exception {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("name");
		term.setCode("code");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		term.addConceptReferenceTermMap(new ConceptReferenceTermMap(new ConceptReferenceTerm(1), null));
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("conceptReferenceTermMaps[0].conceptMapType"));
	}
	
	/**
	 * @see {@link ConceptReferenceTermValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if termB of a concept reference term map is not set", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTermBOfAConceptReferenceTermMapIsNotSet() throws Exception {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("name");
		term.setCode("code");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		Set<ConceptReferenceTermMap> maps = new LinkedHashSet<ConceptReferenceTermMap>();
		maps.add(new ConceptReferenceTermMap(null, new ConceptMapType(1)));
		term.setConceptReferenceTermMaps(maps);
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("conceptReferenceTermMaps[0].termB"));
	}
	
	/**
	 * @see {@link ConceptReferenceTermValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if a term is mapped to itself", method = "validate(Object,Errors)")
	public void validate_shouldFailIfATermIsMappedToItself() throws Exception {
		ConceptReferenceTerm term = Context.getConceptService().getConceptReferenceTerm(1);
		Set<ConceptReferenceTermMap> maps = term.getConceptReferenceTermMaps();
		ConceptReferenceTermMap invalidMap = maps.iterator().next();
		invalidMap.setTermB(term);
		term.setConceptReferenceTermMaps(maps);
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("conceptReferenceTermMaps[0].termB"));
	}
	
	/**
	 * @see {@link ConceptReferenceTermValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if a term is mapped multiple times to the same term", method = "validate(Object,Errors)")
	public void validate_shouldFailIfATermIsMappedMultipleTimesToTheSameTerm() throws Exception {
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
		Assert.assertEquals(true, errors.hasFieldErrors("conceptReferenceTermMaps[1].termB"));
	}
	
	/**
	 * @see {@link ConceptReferenceTermValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if field lengths are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setName("name");
		term.setCode("code");
		term.setConceptSource(Context.getConceptService().getConceptSource(1));
		term.setVersion("version");
		term.setDescription("Description");
		term.setRetireReason("RetireReason");
		Errors errors = new BindException(term, "term");
		new ConceptReferenceTermValidator().validate(term, errors);
		Assert.assertEquals(false, errors.hasErrors());
	}
	
	/**
	 * @see {@link ConceptReferenceTermValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if field lengths are not correct", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
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
		Assert.assertEquals(true, errors.hasFieldErrors("name"));
		Assert.assertEquals(true, errors.hasFieldErrors("code"));
		Assert.assertEquals(true, errors.hasFieldErrors("version"));
		Assert.assertEquals(true, errors.hasFieldErrors("description"));
		Assert.assertEquals(true, errors.hasFieldErrors("retireReason"));
	}
}
