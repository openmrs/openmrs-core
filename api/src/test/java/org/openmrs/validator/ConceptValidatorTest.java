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

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openmrs.test.matchers.HasFieldErrors.hasFieldErrors;
import static org.openmrs.test.matchers.HasGlobalErrors.hasGlobalErrors;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.DuplicateConceptNameException;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link ConceptValidator} class.
 */
public class ConceptValidatorTest extends BaseContextSensitiveTest {
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	private ConceptValidator validator;
	
	private Concept concept;
	
	private Errors errors;
	
	@Autowired
	private ConceptService conceptService;
	
	private Concept cd4Count;
	
	private Concept weight;
	
	@Before
	public void setUp() {
		validator = new ConceptValidator();
		concept = new Concept();
		errors = new BindException(concept, "concept");
		cd4Count = conceptService.getConcept(5497);
		weight = conceptService.getConcept(5089);
	}
	
	@Test
	public void validate_shouldFailIfTheObjectParameterIsNull() {
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("The parameter obj should not be null and must be of type" + Concept.class);
		validator.validate(null, errors);
	}
	
	@Test
	public void shouldFailIfNamesAreEmpty() {
		
		validator.validate(concept, errors);
		
		assertThat(errors, hasGlobalErrors("Concept.name.atLeastOneRequired"));
	}
	
	@Test
	public void validate_shouldFailIfTheConceptDatatypeIsNull() {
		
		concept.addName(new ConceptName("some name", Context.getLocale()));
		concept.addDescription(new ConceptDescription("some description", null));
		concept.setConceptClass(new ConceptClass(1));
		
		validator.validate(concept, errors);
		
		assertThat(errors, hasFieldErrors("datatype", "Concept.datatype.empty"));
	}
	
	@Test
	public void validate_shouldFailIfTheConceptClassIsNull() {
		
		concept.addName(new ConceptName("some name", Context.getLocale()));
		concept.addDescription(new ConceptDescription("some description", null));
		concept.setDatatype(new ConceptDatatype(1));
		
		validator.validate(concept, errors);
		
		assertThat(errors, hasFieldErrors("conceptClass", "Concept.conceptClass.empty"));
	}
	@Test
	public void validate_shouldFailIfAnyNameIsAnEmptyString() {
		
		concept.addDescription(new ConceptDescription("some description", null));
		concept.setConceptClass(new ConceptClass(1));
		concept.setDatatype(new ConceptDatatype(1));
		concept.addName(new ConceptName("name", Context.getLocale()));
		concept.addName(new ConceptName("", Context.getLocale()));
		
		validator.validate(concept, errors);
		
		assertThat(errors, hasGlobalErrors("Concept.name.empty"));
	}
	
	@Test
	public void validate_shouldFailIfAnyNameIsANullValue() {
		
		concept.addDescription(new ConceptDescription("some description", null));
		concept.setConceptClass(new ConceptClass(1));
		concept.setDatatype(new ConceptDatatype(1));
		concept.addName(new ConceptName("name", Context.getLocale()));
		concept.addName(new ConceptName(null, Context.getLocale()));
		
		validator.validate(concept, errors);
		
		assertThat(errors, hasGlobalErrors("Concept.name.empty"));
	}
	
	@Test
	public void validate_shouldFailIfThereIsADuplicateUnretiredConceptNameInTheLocale() {
		
		Context.setLocale(new Locale("en", "GB"));
		concept = cd4Count;
		String duplicateName = concept.getFullySpecifiedName(Context.getLocale()).getName();
		ConceptName newName = new ConceptName(duplicateName, Context.getLocale());
		newName.setDateCreated(Calendar.getInstance().getTime());
		newName.setCreator(Context.getAuthenticatedUser());
		concept.addName(newName);
		errors = new BindException(concept, "concept");
		
		expectedException.expect(DuplicateConceptNameException.class);
		expectedException
		        .expectMessage("'" + duplicateName + "' is a duplicate name in locale '" + Context.getLocale() + "'");
		validator.validate(concept, errors);
	}
	
	@Test
	public void validate_shouldFailIfAnyNamesInTheSameLocaleForThisConceptAreSimilar() {
		
		concept.addName(new ConceptName("same name", Context.getLocale()));
		concept.addName(new ConceptName("same name", Context.getLocale()));
		concept.addDescription(new ConceptDescription("some description",null));
		concept.setConceptClass(new ConceptClass());
		concept.setDatatype(new ConceptDatatype());
		
		expectedException.expect(DuplicateConceptNameException.class);
		expectedException
		        .expectMessage("'same name' is a duplicate name in locale '" + Context.getLocale() + "'");
		validator.validate(concept, errors);
	}
	
	@Test
	public void validate_shouldFailIfThereIsADuplicateUnretiredFullySpecifiedNameInTheSameLocale() {
		
		Context.setLocale(new Locale("en", "GB"));
		Assert.assertEquals(true, cd4Count.getFullySpecifiedName(Context.getLocale()).isFullySpecifiedName());
		String duplicateName = cd4Count.getFullySpecifiedName(Context.getLocale()).getName();
		Concept anotherConcept = weight;
		anotherConcept.getFullySpecifiedName(Context.getLocale()).setName(duplicateName);
		Errors errors = new BindException(anotherConcept, "concept");
		
		expectedException.expect(DuplicateConceptNameException.class);
		expectedException
		        .expectMessage("'" + duplicateName + "' is a duplicate name in locale '" + Context.getLocale() + "'");
		validator.validate(anotherConcept, errors);
	}
	
	@Test
	public void validate_shouldFailIfThereIsADuplicateUnretiredPreferredNameInTheSameLocale() {
		
		Context.setLocale(new Locale("en", "GB"));
		Concept concept = cd4Count;
		ConceptName preferredName = new ConceptName("preferred name", Context.getLocale());
		concept.setPreferredName(preferredName);
		conceptService.saveConcept(concept);
		Assert.assertEquals("preferred name", concept.getPreferredName(Context.getLocale()).getName());
		Concept anotherConcept = weight;
		anotherConcept.getFullySpecifiedName(Context.getLocale()).setName("preferred name");
		Errors errors = new BindException(anotherConcept, "concept");
		
		expectedException.expect(DuplicateConceptNameException.class);
		expectedException
		        .expectMessage("'" + preferredName + "' is a duplicate name in locale '" + Context.getLocale() + "'");
		validator.validate(anotherConcept, errors);
	}
	
	@Test
	public void validate_shouldFailIfThereIsNoNameExplicitlyMarkedAsFullySpecified() {
		
		Concept concept = cd4Count;
		for (ConceptName name : concept.getNames()) {
			name.setConceptNameType(null);
		}
		Errors errors = new BindException(concept, "concept");
		
		validator.validate(concept, errors);
		
		assertThat(errors, hasGlobalErrors("Concept.error.no.FullySpecifiedName"));
	}
	
	@Test
	public void validate_shouldPassIfTheConceptIsBeingUpdatedWithNoNameChange() {
		
		Concept conceptToUpdate = cd4Count;
		conceptToUpdate.setCreator(Context.getAuthenticatedUser());
		Errors errors = new BindException(conceptToUpdate, "concept");
		
		validator.validate(conceptToUpdate, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldPassIfTheConceptHasAtleastOneFullySpecifiedNameAddedToIt() {
		
		concept.addName(new ConceptName("one name", Context.getLocale()));
		concept.addDescription(new ConceptDescription("some description",null));
		concept.setConceptClass(new ConceptClass());
		concept.setDatatype(new ConceptDatatype());
		
		validator.validate(concept, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldPassIfTheDuplicateConceptNameIsNeitherPreferredNorFullySpecified() {
		
		Context.setLocale(new Locale("en", "GB"));
		Concept concept = cd4Count;
		//use a synonym as the duplicate name
		ConceptName duplicateName = concept.getSynonyms(Context.getLocale()).iterator().next();
		Assert.assertEquals(true, duplicateName.isSynonym());
		Concept anotherConcept = weight;
		anotherConcept.getFullySpecifiedName(Context.getLocale()).setName(duplicateName.getName());
		Errors errors = new BindException(anotherConcept, "concept");
		
		validator.validate(anotherConcept, errors);
		
		Assert.assertEquals(false, errors.hasErrors());
	}
	
	@Test
	public void validate_shouldPassIfTheConceptWithADuplicateNameIsRetired() {
		
		Context.setLocale(new Locale("en", "GB"));
		Concept concept = cd4Count;
		concept.setRetired(true);
		conceptService.saveConcept(concept);
		String duplicateName = concept.getFullySpecifiedName(Context.getLocale()).getName();
		Concept anotherConcept = weight;
		anotherConcept.getFullySpecifiedName(Context.getLocale()).setName(duplicateName);
		Errors errors = new BindException(anotherConcept, "concept");
		
		validator.validate(anotherConcept, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldPassIfTheConceptBeingValidatedIsRetiredAndHasADuplicateName() {
		
		Context.setLocale(new Locale("en", "GB"));
		Concept concept = cd4Count;
		conceptService.saveConcept(concept);
		String duplicateName = concept.getFullySpecifiedName(Context.getLocale()).getName();
		Concept anotherConcept = weight;
		anotherConcept.setRetired(true);
		anotherConcept.getFullySpecifiedName(Context.getLocale()).setName(duplicateName);
		Errors errors = new BindException(anotherConcept, "concept");
		
		validator.validate(anotherConcept, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldPassIfTheConceptHasASynonymThatIsAlsoAShortName() {
		
		concept.addName(new ConceptName("CD4", Context.getLocale()));
		concept.addDescription(new ConceptDescription("some description",null));
		concept.setConceptClass(new ConceptClass());
		concept.setDatatype(new ConceptDatatype());
		// Add the short name. Because the short name is not counted as a Synonym. 
		// ConceptValidator will not record any errors.
		ConceptName name = new ConceptName("CD4", Context.getLocale());
		name.setConceptNameType(ConceptNameType.SHORT);
		concept.addName(name);
		
		validator.validate(concept, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldFailIfATermIsMappedMultipleTimesToTheSameConcept() {
		
		concept.setConceptClass(new ConceptClass());
		concept.setDatatype(new ConceptDatatype());
		concept.addName(new ConceptName("my name", Context.getLocale()));
		concept.addDescription(new ConceptDescription("some description",null));
		ConceptMap map1 = new ConceptMap(conceptService.getConceptReferenceTerm(1), conceptService.getConceptMapType(1));
		concept.addConceptMapping(map1);
		ConceptMap map2 = new ConceptMap(conceptService.getConceptReferenceTerm(1), conceptService.getConceptMapType(1));
		concept.addConceptMapping(map2);
		
		validator.validate(concept, errors);
		
		assertThat(errors, hasFieldErrors("conceptMappings[1]"));
	}
	
	@Test
	public void validate_shouldPassIfTheDuplicateNameInTheLocaleForTheConceptBeingValidatedIsVoided() {
		
		ConceptName otherName = conceptService.getConceptName(1439);
		//sanity check since names should only be unique amongst preferred and fully specified names
		Assert.assertTrue(otherName.isFullySpecifiedName() || otherName.isPreferred());
		Assert.assertFalse(otherName.getVoided());
		Assert.assertFalse(otherName.getConcept().getRetired());
		
		//change to a duplicate name in the same locale
		ConceptName duplicateName = conceptService.getConceptName(2477);
		duplicateName.setName(otherName.getName());
		Concept concept = duplicateName.getConcept();
		concept.setPreferredName(duplicateName);
		//ensure that the name has been marked as preferred in its locale
		Assert.assertEquals(duplicateName, concept.getPreferredName(duplicateName.getLocale()));
		Assert.assertTrue(duplicateName.isPreferred());
		duplicateName.setVoided(true);
		
		Errors errors = new BindException(concept, "concept");
		
		validator.validate(concept, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldFailIfThereIsADuplicateUnretiredConceptNameInTheSameLocaleDifferentThanTheSystemLocale()
	{
		Context.setLocale(new Locale("pl"));
		Locale en = new Locale("en", "GB");
		Concept concept = cd4Count;
		Assert.assertEquals(true, concept.getFullySpecifiedName(en).isFullySpecifiedName());
		String duplicateName = concept.getFullySpecifiedName(en).getName();
		Concept anotherConcept = weight;
		anotherConcept.getFullySpecifiedName(en).setName(duplicateName);
		Errors errors = new BindException(anotherConcept, "concept");
		
		expectedException.expect(DuplicateConceptNameException.class);
		expectedException
		        .expectMessage("'" + duplicateName + "' is a duplicate name in locale '" + en + "'");
		validator.validate(concept, errors);
		validator.validate(anotherConcept, errors);
	}
	
	@Test
	public void validate_shouldPassForANewConceptWithAMapCreatedWithDeprecatedConceptMapMethods() {
		
		concept.addName(new ConceptName("test name", Context.getLocale()));
		concept.addDescription(new ConceptDescription("some description",null));
		concept.setConceptClass(new ConceptClass());
		concept.setDatatype(new ConceptDatatype());
		ConceptMap map = new ConceptMap();
		map.getConceptReferenceTerm().setCode("unique code");
		map.getConceptReferenceTerm().setConceptSource(conceptService.getConceptSource(1));
		concept.addConceptMapping(map);
		
		ValidateUtil.validate(concept);
	}
	
	@Test
	public void validate_shouldPassForAnEditedConceptWithAMapCreatedWithDeprecatedConceptMapMethods() {
		
		Concept concept = cd4Count;
		ConceptMap map = new ConceptMap();
		map.getConceptReferenceTerm().setCode("unique code");
		map.getConceptReferenceTerm().setConceptSource(conceptService.getConceptSource(1));
		concept.addConceptMapping(map);
		
		ValidateUtil.validate(concept);
	}
	
	@Test
	public void validate_shouldNotFailIfATermHasTwoNewMappingsOnIt() {
		
		concept.addName(new ConceptName("my name", Context.getLocale()));
		ConceptReferenceTerm newTerm = new ConceptReferenceTerm(conceptService.getConceptSource(1), "1234",
		        "term one two three four");
		ConceptMap map1 = new ConceptMap(newTerm, conceptService.getConceptMapType(1));
		concept.addConceptMapping(map1);
		ConceptReferenceTerm newTermTwo = new ConceptReferenceTerm(conceptService.getConceptSource(1), "12345",
		        "term one two three four five");
		ConceptMap map2 = new ConceptMap(newTermTwo, conceptService.getConceptMapType(1));
		concept.addConceptMapping(map2);
		
		validator.validate(concept, errors);
		
		assertThat(errors, not(hasFieldErrors("conceptMappings[1]")));
	}
	
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		
		concept.addName(new ConceptName("CD4", Context.getLocale()));
		concept.addDescription(new ConceptDescription("some description",null));
		concept.setVersion("version");
		concept.setRetireReason("retireReason");
		concept.setConceptClass(new ConceptClass());
		concept.setDatatype(new ConceptDatatype());

		validator.validate(concept, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		
		concept.addName(new ConceptName("CD4", Context.getLocale()));
		concept.addDescription(new ConceptDescription("some description",null));
		concept.setConceptClass(new ConceptClass());
		concept.setDatatype(new ConceptDatatype());
		concept.setVersion("too long text too long text too long text too long text");
		concept
		        .setRetireReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		validator.validate(concept, errors);
		
		assertThat(errors, hasFieldErrors("version", "error.exceededMaxLengthOfField"));
		assertThat(errors, hasFieldErrors("retireReason", "error.exceededMaxLengthOfField"));
	}
	
	@Test
	public void validate_shouldPassIfFullySpecifiedNameIsTheSameAsShortName() {
		
		ConceptName conceptFullySpecifiedName = new ConceptName("YES", new Locale("pl"));
		conceptFullySpecifiedName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		ConceptName conceptShortName = new ConceptName("yes", new Locale("pl"));
		conceptShortName.setConceptNameType(ConceptNameType.SHORT);
		concept.addName(conceptFullySpecifiedName);
		concept.addName(conceptShortName);
		concept.addDescription(new ConceptDescription("some description",null));
		concept.setConceptClass(new ConceptClass());
		concept.setDatatype(new ConceptDatatype());

		validator.validate(concept, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldPassIfDifferentConceptsHaveTheSameShortNames() {
		
		Context.setLocale(new Locale("en", "GB"));
		
		List<Concept> concepts = conceptService.getConceptsByName("HSM");
		Assert.assertEquals(1, concepts.size());
		Assert.assertEquals(true, concepts.get(0).getShortNameInLocale(Context.getLocale()).getName()
		        .equalsIgnoreCase("HSM"));
		
		concept.setConceptClass(new ConceptClass());
		concept.setDatatype(new ConceptDatatype());
		ConceptName conceptFullySpecifiedName = new ConceptName("holosystolic murmur", Context.getLocale());
		conceptFullySpecifiedName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		
		ConceptName conceptShortName = new ConceptName("HSM", Context.getLocale());
		conceptShortName.setConceptNameType(ConceptNameType.SHORT);
		
		concept.addName(conceptFullySpecifiedName);
		concept.addName(conceptShortName);
		concept.addDescription(new ConceptDescription("some description",null));
		
		validator.validate(concept, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}

	@Test
	public void validate_shouldFailIfCodedConceptContainsItselfAsAnAnswer() {
		
		Concept concept = conceptService.getConcept(30);
		ConceptAnswer conceptAnswer = new ConceptAnswer(concept);
		concept.addAnswer(conceptAnswer);

		Errors errors = new BindException(concept, "concept");
		
		validator.validate(concept, errors);
		
		assertThat(errors, hasGlobalErrors("Concept.contains.itself.as.answer"));
	}

	@Test
	public void validate_shouldNotFailIfAnyDescriptionIsNotEnteredWhileCreatingANewConcept() {
		
		concept.addName(new ConceptName("some name", Context.getLocale()));
		
		validator.validate(concept, errors);
		
		assertThat(errors, not(hasFieldErrors("description")));
	}

	@Test
	public void validate_shouldPassIfNoneofTheConceptDescriptionsIsNull() {
		
		concept.addName(new ConceptName("some name",Context.getLocale()));
		concept.addDescription(new ConceptDescription("some description",null));
		concept.setConceptClass(new ConceptClass());
		concept.setDatatype(new ConceptDatatype());
		
		validator.validate(concept, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}

	@Test
	public void validate_shouldNotFailIfBlankConceptDescriptionIsPassed() {
		
		concept.addName(new ConceptName("some name",Context.getLocale()));
		concept.addDescription(new ConceptDescription("   ",null));
		
		validator.validate(concept, errors);
		
		assertThat(errors, not(hasFieldErrors("description")));
	}
}
