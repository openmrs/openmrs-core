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

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.ConceptService;
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
		Context.setLocale(new Locale("en", "GB"));
		Concept concept = Context.getConceptService().getConcept(5497);
		String duplicateName = concept.getFullySpecifiedName(Context.getLocale()).getName();
		
		ConceptName newName = new ConceptName(duplicateName, Context.getLocale());
		newName.setDateCreated(Calendar.getInstance().getTime());
		newName.setCreator(Context.getAuthenticatedUser());
		concept.addName(newName);
		Errors errors = new BindException(concept, "concept");
		new ConceptValidator().validate(concept, errors);
	}
	
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "should fail if the object parameter is null", method = "validate(Concept)")
	public void validate_shouldFailIfTheObjectParameterIsNull() throws Exception {
		Errors errors = new BindException(null, "concept");
		new ConceptValidator().validate(null, errors);
		Assert.assertTrue(errors.hasErrors());
	}
	
	@Test
	@Verifies(value = "should pass if the concept is being updated with no name change", method = "validate(Concept)")
	public void validate_shouldPassIfTheConceptIsBeingUpdatedWithNoNameChange() throws Exception {
		
		Concept conceptToUpdate = Context.getConceptService().getConcept(5497);
		conceptToUpdate.setCreator(Context.getAuthenticatedUser());
		Errors errors = new BindException(conceptToUpdate, "concept");
		new ConceptValidator().validate(conceptToUpdate, errors);
		Assert.assertFalse(errors.hasErrors());
		
	}
	
	/**
	 * @see {@link ConceptValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if any name is an empty string", method = "validate(Object,Errors)")
	public void validate_shouldFailIfAnyNameIsAnEmptyString() throws Exception {
		Concept concept = new Concept();
		concept.addName(new ConceptName("name", Context.getLocale()));
		concept.addName(new ConceptName("", Context.getLocale()));
		Errors errors = new BindException(concept, "concept");
		new ConceptValidator().validate(concept, errors);
		Assert.assertEquals(true, errors.hasErrors());
	}
	
	/**
	 * @see {@link ConceptValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "fail if any name is a null value", method = "validate(Object,Errors)")
	public void validate_shouldFailIfAnyNameIsANullValue() throws Exception {
		Concept concept = new Concept();
		concept.addName(new ConceptName("name", Context.getLocale()));
		concept.addName(new ConceptName(null, Context.getLocale()));
		Errors errors = new BindException(concept, "concept");
		new ConceptValidator().validate(concept, errors);
		Assert.assertEquals(true, errors.hasErrors());
	}
	
	/**
	 * Tests for duplicate names for the same concept and not with names for other concepts
	 * 
	 * @see {@link ConceptValidator#validate(Object,Errors)}
	 */
	@Test(expected = DuplicateConceptNameException.class)
	@Verifies(value = "should fail if any names in the same locale for this concept are similar", method = "validate(Object,Errors)")
	public void validate_shouldFailIfAnyNamesInTheSameLocaleForThisConceptAreSimilar() throws Exception {
		Concept concept = new Concept();
		concept.addName(new ConceptName("same name", Context.getLocale()));
		concept.addName(new ConceptName("same name", Context.getLocale()));
		Errors errors = new BindException(concept, "concept");
		new ConceptValidator().validate(concept, errors);
	}
	
	/**
	 * Tests for uniqueness amongst all fully specified names in the same locale
	 * 
	 * @see {@link ConceptValidator#validate(Object,Errors)}
	 */
	@Test(expected = DuplicateConceptNameException.class)
	@Verifies(value = "should fail if there is a duplicate unretired fully specified name in the same locale", method = "validate(Object,Errors)")
	public void validate_shouldFailIfThereIsADuplicateUnretiredFullySpecifiedNameInTheSameLocale() throws Exception {
		Context.setLocale(new Locale("en", "GB"));
		Concept concept = Context.getConceptService().getConcept(5497);
		Assert.assertEquals(true, concept.getFullySpecifiedName(Context.getLocale()).isFullySpecifiedName());
		String duplicateName = concept.getFullySpecifiedName(Context.getLocale()).getName();
		
		Concept anotherConcept = Context.getConceptService().getConcept(5089);
		anotherConcept.getFullySpecifiedName(Context.getLocale()).setName(duplicateName);
		Errors errors = new BindException(anotherConcept, "concept");
		new ConceptValidator().validate(anotherConcept, errors);
	}
	
	/**
	 * Tests for uniqueness amongst all preferred names in the same locale
	 * 
	 * @see {@link ConceptValidator#validate(Object,Errors)}
	 */
	@Test(expected = DuplicateConceptNameException.class)
	@Verifies(value = "should fail if there is a duplicate unretired preferred name in the same locale", method = "validate(Object,Errors)")
	public void validate_shouldFailIfThereIsADuplicateUnretiredPreferredNameInTheSameLocale() throws Exception {
		Context.setLocale(new Locale("en", "GB"));
		Concept concept = Context.getConceptService().getConcept(5497);
		ConceptName preferredName = new ConceptName("preferred name", Context.getLocale());
		concept.setPreferredName(preferredName);
		Context.getConceptService().saveConcept(concept);
		Assert.assertEquals("preferred name", concept.getPreferredName(Context.getLocale()).getName());
		
		Concept anotherConcept = Context.getConceptService().getConcept(5089);
		anotherConcept.getFullySpecifiedName(Context.getLocale()).setName("preferred name");
		
		Errors errors = new BindException(anotherConcept, "concept");
		new ConceptValidator().validate(anotherConcept, errors);
	}
	
	/**
	 * @see {@link ConceptValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if there is no name explicitly marked as fully specified", method = "validate(Object,Errors)")
	public void validate_shouldFailIfThereIsNoNameExplicitlyMarkedAsFullySpecified() throws Exception {
		Concept concept = Context.getConceptService().getConcept(5497);
		for (ConceptName name : concept.getNames())
			name.setConceptNameType(null);
		
		Errors errors = new BindException(concept, "concept");
		new ConceptValidator().validate(concept, errors);
		Assert.assertEquals(true, errors.hasErrors());
	}
	
	/**
	 * @see {@link ConceptValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass if the concept has atleast one fully specified name added to it", method = "validate(Object,Errors)")
	public void validate_shouldPassIfTheConceptHasAtleastOneFullySpecifiedNameAddedToIt() throws Exception {
		Concept concept = new Concept();
		concept.addName(new ConceptName("one name", Context.getLocale()));
		Errors errors = new BindException(concept, "concept");
		new ConceptValidator().validate(concept, errors);
		Assert.assertEquals(false, errors.hasErrors());
	}
	
	/**
	 * @see {@link ConceptValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass if the duplicate ConceptName is neither preferred nor fully Specified", method = "validate(Object,Errors)")
	public void validate_shouldPassIfTheDuplicateConceptNameIsNeitherPreferredNorFullySpecified() throws Exception {
		Context.setLocale(new Locale("en", "GB"));
		Concept concept = Context.getConceptService().getConcept(5497);
		//use a synonym as the duplicate name
		ConceptName duplicateName = concept.getSynonyms(Context.getLocale()).iterator().next();
		Assert.assertEquals(true, duplicateName.isSynonym());
		
		Concept anotherConcept = Context.getConceptService().getConcept(5089);
		anotherConcept.getFullySpecifiedName(Context.getLocale()).setName(duplicateName.getName());
		Errors errors = new BindException(anotherConcept, "concept");
		new ConceptValidator().validate(anotherConcept, errors);
		Assert.assertEquals(false, errors.hasErrors());
	}
	
	/**
	 * @see {@link ConceptValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass if the concept with a duplicate name is retired", method = "validate(Object,Errors)")
	public void validate_shouldPassIfTheConceptWithADuplicateNameIsRetired() throws Exception {
		Context.setLocale(new Locale("en", "GB"));
		Concept concept = Context.getConceptService().getConcept(5497);
		concept.setRetired(true);
		Context.getConceptService().saveConcept(concept);
		String duplicateName = concept.getFullySpecifiedName(Context.getLocale()).getName();
		
		Concept anotherConcept = Context.getConceptService().getConcept(5089);
		anotherConcept.getFullySpecifiedName(Context.getLocale()).setName(duplicateName);
		Errors errors = new BindException(anotherConcept, "concept");
		new ConceptValidator().validate(anotherConcept, errors);
		Assert.assertEquals(false, errors.hasErrors());
	}
	
	/**
	 * @see ConceptValidator#validate(Object,Errors)
	 * @verifies pass if the concept being validated is retired and has a duplicate name
	 */
	@Test
	public void validate_shouldPassIfTheConceptBeingValidatedIsRetiredAndHasADuplicateName() throws Exception {
		Context.setLocale(new Locale("en", "GB"));
		Concept concept = Context.getConceptService().getConcept(5497);
		Context.getConceptService().saveConcept(concept);
		String duplicateName = concept.getFullySpecifiedName(Context.getLocale()).getName();
		
		Concept anotherConcept = Context.getConceptService().getConcept(5089);
		anotherConcept.setRetired(true);
		anotherConcept.getFullySpecifiedName(Context.getLocale()).setName(duplicateName);
		Errors errors = new BindException(anotherConcept, "concept");
		new ConceptValidator().validate(anotherConcept, errors);
		Assert.assertEquals(false, errors.hasErrors());
	}
	
	/**
	 * @see {@link ConceptValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass if the concept has a synonym that is also a short name", method = "validate(Object,Errors)")
	public void validate_shouldPassIfTheConceptHasASynonymThatIsAlsoAShortName() throws Exception {
		Concept concept = new Concept();
		concept.addName(new ConceptName("CD4", Context.getLocale()));
		// Add the short name. Because the short name is not counted as a Synonym. 
		// ConceptValidator will not record any errors.
		ConceptName name = new ConceptName("CD4", Context.getLocale());
		name.setConceptNameType(ConceptNameType.SHORT);
		concept.addName(name);
		Errors errors = new BindException(concept, "concept");
		new ConceptValidator().validate(concept, errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link ConceptValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if a term is mapped multiple times to the same concept", method = "validate(Object,Errors)")
	public void validate_shouldFailIfATermIsMappedMultipleTimesToTheSameConcept() throws Exception {
		Concept concept = new Concept();
		ConceptService cs = Context.getConceptService();
		concept.addName(new ConceptName("my name", Context.getLocale()));
		ConceptMap map1 = new ConceptMap(cs.getConceptReferenceTerm(1), cs.getConceptMapType(1));
		concept.addConceptMapping(map1);
		ConceptMap map2 = new ConceptMap(cs.getConceptReferenceTerm(1), cs.getConceptMapType(1));
		concept.addConceptMapping(map2);
		
		Errors errors = new BindException(concept, "concept");
		new ConceptValidator().validate(concept, errors);
		
		//the second mapping should be rejected
		Assert.assertEquals(true, errors.hasFieldErrors("conceptMappings[1]"));
	}
	
	/**
	 * @see {@link ConceptValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass if the duplicate name in the locale for the concept being validated is voided", method = "validate(Object,Errors)")
	public void validate_shouldPassIfTheDuplicateNameInTheLocaleForTheConceptBeingValidatedIsVoided() throws Exception {
		ConceptService cs = Context.getConceptService();
		ConceptName otherName = cs.getConceptName(1439);
		//sanity check since names should only be unique amongst preferred and fully specified names
		Assert.assertTrue(otherName.isFullySpecifiedName() || otherName.isPreferred());
		Assert.assertFalse(otherName.isVoided());
		Assert.assertFalse(otherName.getConcept().isRetired());
		
		//change to a duplicate name in the same locale
		ConceptName duplicateName = cs.getConceptName(2477);
		duplicateName.setName(otherName.getName());
		Concept concept = duplicateName.getConcept();
		concept.setPreferredName(duplicateName);
		//ensure that the name has been marked as preferred in its locale
		Assert.assertEquals(duplicateName, concept.getPreferredName(duplicateName.getLocale()));
		Assert.assertTrue(duplicateName.isPreferred());
		duplicateName.setVoided(true);
		
		Errors errors = new BindException(concept, "concept");
		new ConceptValidator().validate(concept, errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ConceptValidator#validate(Object,Errors)
	 * @verifies fail if there is a duplicate unretired concept name in the same locale different
	 *           than the system locale
	 */
	@Test(expected = DuplicateConceptNameException.class)
	public void validate_shouldFailIfThereIsADuplicateUnretiredConceptNameInTheSameLocaleDifferentThanTheSystemLocale()
	        throws Exception {
		Context.setLocale(new Locale("pl"));
		Locale en = new Locale("en", "GB");
		Concept concept = Context.getConceptService().getConcept(5497);
		Assert.assertEquals(true, concept.getFullySpecifiedName(en).isFullySpecifiedName());
		String duplicateName = concept.getFullySpecifiedName(en).getName();
		
		Concept anotherConcept = Context.getConceptService().getConcept(5089);
		anotherConcept.getFullySpecifiedName(en).setName(duplicateName);
		Errors errors = new BindException(anotherConcept, "concept");
		new ConceptValidator().validate(anotherConcept, errors);
	}
	
	/**
	 * @see {@link ConceptValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass for a new concept with a map created with deprecated concept map methods", method = "validate(Object,Errors)")
	public void validate_shouldPassForANewConceptWithAMapCreatedWithDeprecatedConceptMapMethods() throws Exception {
		ConceptService cs = Context.getConceptService();
		Concept concept = new Concept();
		concept.addName(new ConceptName("test name", Context.getLocale()));
		ConceptMap map = new ConceptMap();
		map.setSourceCode("unique code");
		map.setSource(cs.getConceptSource(1));
		concept.addConceptMapping(map);
		ValidateUtil.validate(concept);
	}
	
	/**
	 * @see {@link ConceptValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass for an edited concept with a map created with deprecated concept map methods", method = "validate(Object,Errors)")
	public void validate_shouldPassForAnEditedConceptWithAMapCreatedWithDeprecatedConceptMapMethods() throws Exception {
		ConceptService cs = Context.getConceptService();
		Concept concept = cs.getConcept(5497);
		ConceptMap map = new ConceptMap();
		map.setSourceCode("unique code");
		map.setSource(cs.getConceptSource(1));
		concept.addConceptMapping(map);
		ValidateUtil.validate(concept);
	}
	
	/**
	 * @see ConceptValidator#validate(Object,Errors)
	 * @verifies not fail if a term has two new mappings on it
	 */
	@Test
	public void validate_shouldNotFailIfATermHasTwoNewMappingsOnIt() throws Exception {
		Concept concept = new Concept();
		ConceptService cs = Context.getConceptService();
		concept.addName(new ConceptName("my name", Context.getLocale()));
		ConceptReferenceTerm newTerm = new ConceptReferenceTerm(cs.getConceptSource(1), "1234", "term one two three four");
		ConceptMap map1 = new ConceptMap(newTerm, cs.getConceptMapType(1));
		concept.addConceptMapping(map1);
		ConceptReferenceTerm newTermTwo = new ConceptReferenceTerm(cs.getConceptSource(1), "12345",
		        "term one two three four five");
		ConceptMap map2 = new ConceptMap(newTermTwo, cs.getConceptMapType(1));
		concept.addConceptMapping(map2);
		
		Errors errors = new BindException(concept, "concept");
		new ConceptValidator().validate(concept, errors);
		
		//the second mapping should be rejected
		Assert.assertEquals(false, errors.hasFieldErrors("conceptMappings[1]"));
	}
	
	/**
	 * @see {@link ConceptValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if field lengths are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
		Concept concept = new Concept();
		concept.addName(new ConceptName("CD4", Context.getLocale()));
		concept.setVersion("version");
		concept.setRetireReason("retireReason");
		
		Errors errors = new BindException(concept, "concept");
		new ConceptValidator().validate(concept, errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link ConceptValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if field lengths are not correct", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
		Concept concept = new Concept();
		concept.addName(new ConceptName("CD4", Context.getLocale()));
		concept.setVersion("too long text too long text too long text too long text");
		concept
		        .setRetireReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(concept, "concept");
		new ConceptValidator().validate(concept, errors);
		Assert.assertTrue(errors.hasFieldErrors("version"));
		Assert.assertTrue(errors.hasFieldErrors("retireReason"));
	}
	
	/**
	 * @see {@link ConceptValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass if fully specified name is the same as short name", method = "validate(Object,Errors)")
	public void validate_shouldPassIfFullySpecifiedNameIsTheSameAsShortName() throws Exception {
		Concept concept = new Concept();
		
		ConceptName conceptFullySpecifiedName = new ConceptName("YES", new Locale("pl"));
		conceptFullySpecifiedName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		
		ConceptName conceptShortName = new ConceptName("yes", new Locale("pl"));
		conceptShortName.setConceptNameType(ConceptNameType.SHORT);
		
		concept.addName(conceptFullySpecifiedName);
		concept.addName(conceptShortName);
		
		Errors errors = new BindException(concept, "concept");
		new ConceptValidator().validate(concept, errors);
		Assert.assertEquals(false, errors.hasErrors());
	}
	
	/**
	 * @see {@link ConceptValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass if different concepts have the same short name", method = "validate(Object,Errors)")
	public void validate_shouldPassIfDifferentConceptsHaveTheSameShortNames() throws Exception {
		Context.setLocale(new Locale("en", "GB"));
		
		List<Concept> concepts = Context.getConceptService().getConceptsByName("HSM");
		Assert.assertEquals(1, concepts.size());
		Assert.assertEquals(true, concepts.get(0).getShortNameInLocale(Context.getLocale()).getName()
		        .equalsIgnoreCase("HSM"));
		
		Concept concept = new Concept();
		ConceptName conceptFullySpecifiedName = new ConceptName("holosystolic murmur", Context.getLocale());
		conceptFullySpecifiedName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		
		ConceptName conceptShortName = new ConceptName("HSM", Context.getLocale());
		conceptShortName.setConceptNameType(ConceptNameType.SHORT);
		
		concept.addName(conceptFullySpecifiedName);
		concept.addName(conceptShortName);
		
		Errors errors = new BindException(concept, "concept");
		new ConceptValidator().validate(concept, errors);
		Assert.assertEquals(false, errors.hasErrors());
	}
}
