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

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.ProviderAttributeType;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

public class ProviderValidatorTest extends BaseContextSensitiveTest {
	
	private Provider provider;
	
	private Errors errors;
	
	private ProviderValidator providerValidator;
	
	private ProviderService providerService;
	
	private static final String PROVIDER_ATTRIBUTE_TYPES_XML = "org/openmrs/api/include/ProviderServiceTest-providerAttributes.xml";
	
	private static final String OTHERS_PROVIDERS_XML = "org/openmrs/api/include/ProviderServiceTest-otherProviders.xml";
	
	@Before
	public void setup() {
		provider = new Provider();
		errors = new BindException(provider, "provider");
		providerValidator = new ProviderValidator();
		providerService = Context.getProviderService();
	}
	
	/**
	 * @see ProviderValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldBeValidIfIdentifierIsNotSet() {
		//given
		provider.setIdentifier(null);
		
		Person person = new Person();
		Set<PersonName> personNames = new HashSet<>(1);
		PersonName personName = new PersonName();
		personName.setFamilyName("name");
		personNames.add(personName);
		person.setNames(personNames);
		provider.setPerson(person);
		
		//when
		providerValidator.validate(provider, errors);
		
		//then
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ProviderValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldBeValidIfIdentifierIsSet() {
		//given
		provider.setIdentifier("id");
		
		//when
		providerValidator.validate(provider, errors);
		
		//then
		Assert.assertFalse(errors.hasFieldErrors("identifier"));
	}
	
	/**
	 * @see ProviderValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldBeInvalidIfProviderIsRetiredAndTheRetiredReasonIsNotMentioned() {
		provider.setIdentifier("id");
		provider.setRetired(true);
		provider.setPerson(new Person());
		
		providerValidator.validate(provider, errors);
		
		Assert.assertTrue(errors.hasErrors());
		Assert.assertTrue(errors.hasFieldErrors("retireReason"));
		Assert.assertEquals("Provider.error.retireReason.required", errors.getFieldError("retireReason").getCode());
		
		errors = new BindException(provider, "provider");
		provider.setRetireReason("getting old..");
		
		providerValidator.validate(provider, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ProviderValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldBeInvalidIfPersonIsNotSet() {
		//given
		provider.setIdentifier("id");
		provider.setPerson(null);
		
		//when
		providerValidator.validate(provider, errors);
		
		//then
		Assert.assertTrue(errors.hasErrors());
		Assert.assertTrue(errors.hasFieldErrors("name"));
		Assert.assertTrue(errors.hasFieldErrors("person"));
		Assert.assertEquals("Provider.error.personOrName.required", errors.getFieldError("name").getCode());
		Assert.assertEquals("Provider.error.personOrName.required", errors.getFieldError("person").getCode());
	}
	
	/**
	 * @see ProviderValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldBeValidIfOnlyPersonIsSet() {
		//given
		provider.setIdentifier("id");
		provider.setPerson(new Person(1));
		
		//when
		providerValidator.validate(provider, errors);
		
		//then
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ProviderValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldRejectAProviderIfItHasFewerThanMinOccursOfAnAttribute() {
		provider.setId(null);
		provider.setPerson(null);
		executeDataSet(PROVIDER_ATTRIBUTE_TYPES_XML);
		ProviderAttributeType attributeType = providerService.getProviderAttributeType(1);
		attributeType.setMinOccurs(2);
		attributeType.setMaxOccurs(3);
		providerService.saveProviderAttributeType(attributeType);
		
		provider.addAttribute(makeAttribute("one"));
		Errors errors = new BindException(provider, "provider");
		new ProviderValidator().validate(provider, errors);
		Assert.assertTrue(errors.hasFieldErrors("activeAttributes"));
	}
	
	/**
	 * @see ProviderValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldRejectAProviderIfItHasMoreThanMaxOccursOfAnAttribute() {
		provider.setId(null);
		provider.setPerson(null);
		executeDataSet(PROVIDER_ATTRIBUTE_TYPES_XML);
		provider.addAttribute(makeAttribute("one"));
		provider.addAttribute(makeAttribute("two"));
		provider.addAttribute(makeAttribute("three"));
		provider.addAttribute(makeAttribute("four"));
		new ProviderValidator().validate(provider, errors);
		Assert.assertTrue(errors.hasFieldErrors("activeAttributes"));
	}
	
	private ProviderAttribute makeAttribute(String serializedValue) {
		ProviderAttribute attr = new ProviderAttribute();
		attr.setAttributeType(providerService.getProviderAttributeType(1));
		attr.setValueReferenceInternal(serializedValue);
		return attr;
	}
	
	/**
	 * @see ProviderValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldAcceptDuplicateIdentifierIfTheExistingProviderIsNotRetired() {
		executeDataSet(OTHERS_PROVIDERS_XML);
		Provider duplicateProvider = providerService.getProvider(200);
		
		Provider existingProviderToEdit = providerService.getProvider(1);
		existingProviderToEdit.setIdentifier(duplicateProvider.getIdentifier());
		
		providerValidator.validate(existingProviderToEdit, errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ProviderValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldAcceptDuplicateIdentifierIfTheExistingProviderIsRetired() {
		executeDataSet(OTHERS_PROVIDERS_XML);
		Provider duplicateRetiredProvider = providerService.getProvider(201);
		Assert.assertTrue(duplicateRetiredProvider.getRetired());
		
		Provider provider = providerService.getProvider(1);
		provider.setIdentifier(duplicateRetiredProvider.getIdentifier());
		
		providerValidator.validate(provider, errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ProviderValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldAcceptADuplicateIdentifierForANewProviderWhichIsNotRetired() {
		Provider duplicateProvider = providerService.getProvider(1);
		Assert.assertFalse(duplicateProvider.getRetired());
		
		Provider provider = new Provider();
		provider.setIdentifier(duplicateProvider.getIdentifier());
		
		providerValidator.validate(provider, errors);
		Assert.assertFalse(errors.hasFieldErrors("identifier"));
	}
	
	/**
	 * @see ProviderValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldAcceptADuplicateIdentifierForANewProviderWhichIsRetired() {
		executeDataSet(OTHERS_PROVIDERS_XML);
		Provider duplicateProvider = providerService.getProvider(1);
		Assert.assertFalse(duplicateProvider.getRetired());
		
		Provider providerToValidate = providerService.getProvider(201);
		Assert.assertTrue(providerToValidate.getRetired());
		providerToValidate.setIdentifier(duplicateProvider.getIdentifier());
		
		providerValidator.validate(providerToValidate, errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ProviderValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		Provider provider = new Provider();
		provider.setIdentifier("identifier");
		provider.setRetireReason("retireReason");
		
		Person person = new Person();
		Set<PersonName> personNames = new HashSet<>(1);
		PersonName personName = new PersonName();
		personName.setFamilyName("name");
		personNames.add(personName);
		person.setNames(personNames);
		provider.setPerson(person);
		
		providerValidator.validate(provider, errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ProviderValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		Provider provider = new Provider();
		provider
		        .setIdentifier("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		provider
		        .setRetireReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(provider, "type");
		providerValidator.validate(provider, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("identifier"));
		Assert.assertTrue(errors.hasFieldErrors("retireReason"));
	}
}
