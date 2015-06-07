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
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import java.util.HashSet;
import java.util.Set;

public class ProviderValidatorTest extends BaseContextSensitiveTest {
	
	private Provider provider;
	
	private Errors errors;
	
	private ProviderValidator providerValidator;
	
	private ProviderService providerService;
	
	private static final String PROVIDER_ATTRIBUTE_TYPES_XML = "org/openmrs/api/include/ProviderServiceTest-providerAttributes.xml";
	
	private static final String OTHERS_PROVIDERS_XML = "org/openmrs/api/include/ProviderServiceTest-otherProviders.xml";
	
	@Before
	public void setup() throws Exception {
		provider = new Provider();
		errors = new BindException(provider, "provider");
		providerValidator = new ProviderValidator();
		providerService = Context.getProviderService();
	}
	
	/**
	 * @see ProviderValidator#validate(Object, Errors)
	 * @verifies be valid if identifier is not set
	 */
	@Test
	public void validate_shouldBeValidIfIdentifierIsNotSet() throws Exception {
		//given
		provider.setIdentifier(null);
		
		Person person = new Person();
		Set<PersonName> personNames = new HashSet<PersonName>(1);
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
	 * @verifies be valid if identifier is set
	 */
	@Test
	public void validate_shouldBeValidIfIdentifierIsSet() throws Exception {
		//given
		provider.setIdentifier("id");
		
		//when
		providerValidator.validate(provider, errors);
		
		//then
		Assert.assertFalse(errors.hasFieldErrors("identifier"));
	}
	
	/**
	 * @see ProviderValidator#validate(Object, Errors)
	 * @verifies be invalid if provider is retired and the retired reason is not mentioned
	 */
	@Test
	public void validate_shouldBeInvalidIfProviderIsRetiredAndTheRetiredReasonIsNotMentioned() throws Exception {
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
	 * @verifies be invalid if person is not set
	 */
	@Test
	public void validate_shouldBeInvalidIfPersonIsNotSet() throws Exception {
		//given
		provider.setIdentifier("id");
		provider.setPerson(null);
		
		//when
		providerValidator.validate(provider, errors);
		
		//then
		Assert.assertTrue(errors.hasErrors());
		Assert.assertTrue(errors.hasFieldErrors("name"));
		Assert.assertEquals("Provider.error.personOrName.required", errors.getFieldError("name").getCode());
	}
	
	/**
	 * @see ProviderValidator#validate(Object, Errors)
	 * @verifies be valid if only person is set
	 */
	@Test
	public void validate_shouldBeValidIfOnlyPersonIsSet() throws Exception {
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
	 * @verifies reject a provider if it has fewer than min occurs of an attribute
	 */
	@Test
	public void validate_shouldRejectAProviderIfItHasFewerThanMinOccursOfAnAttribute() throws Exception {
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
	 * @verifies reject a Provider if it has more than max occurs of an attribute
	 */
	@Test
	public void validate_shouldRejectAProviderIfItHasMoreThanMaxOccursOfAnAttribute() throws Exception {
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
	 * @see {@link ProviderValidator#validate(Object, Errors)}
	 */
	@Test
	@Verifies(value = "should accept duplicate identifier if the existing provider is not retired", method = "validate(Object,Errors)")
	public void validate_shouldAcceptDuplicateIdentifierIfTheExistingProviderIsNotRetired() throws Exception {
		executeDataSet(OTHERS_PROVIDERS_XML);
		Provider duplicateProvider = providerService.getProvider(200);
		
		Provider existingProviderToEdit = providerService.getProvider(1);
		existingProviderToEdit.setIdentifier(duplicateProvider.getIdentifier());
		
		providerValidator.validate(existingProviderToEdit, errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link ProviderValidator#validate(Object, Errors)}
	 */
	@Test
	@Verifies(value = "should accept duplicate identifier if the existing provider is retired", method = "validate(Object,Errors)")
	public void validate_shouldAcceptDuplicateIdentifierIfTheExistingProviderIsRetired() throws Exception {
		executeDataSet(OTHERS_PROVIDERS_XML);
		Provider duplicateRetiredProvider = providerService.getProvider(201);
		Assert.assertTrue(duplicateRetiredProvider.isRetired());
		
		Provider provider = providerService.getProvider(1);
		provider.setIdentifier(duplicateRetiredProvider.getIdentifier());
		
		providerValidator.validate(provider, errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link ProviderValidator#validate(Object, Errors)}
	 */
	@Test
	@Verifies(value = "should accept a duplicate identifier for a new provider which is not retired", method = "validate(Object,Errors)")
	public void validate_shouldAcceptADuplicateIdentifierForANewProviderWhichIsNotRetired() throws Exception {
		Provider duplicateProvider = providerService.getProvider(1);
		Assert.assertFalse(duplicateProvider.isRetired());
		
		Provider provider = new Provider();
		provider.setIdentifier(duplicateProvider.getIdentifier());
		
		providerValidator.validate(provider, errors);
		Assert.assertFalse(errors.hasFieldErrors("identifier"));
	}
	
	/**
	 * @see {@link ProviderValidator#validate(Object, Errors)}
	 */
	@Test
	@Verifies(value = "should accept a duplicate identifier for a new provider which is retired", method = "validate(Object,Errors)")
	public void validate_shouldAcceptADuplicateIdentifierForANewProviderWhichIsRetired() throws Exception {
		executeDataSet(OTHERS_PROVIDERS_XML);
		Provider duplicateProvider = providerService.getProvider(1);
		Assert.assertFalse(duplicateProvider.isRetired());
		
		Provider providerToValidate = providerService.getProvider(201);
		Assert.assertTrue(providerToValidate.isRetired());
		providerToValidate.setIdentifier(duplicateProvider.getIdentifier());
		
		providerValidator.validate(providerToValidate, errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link ProviderValidator#validate(Object, Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if field lengths are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
		Provider provider = new Provider();
		provider.setIdentifier("identifier");
		provider.setRetireReason("retireReason");
		
		Person person = new Person();
		Set<PersonName> personNames = new HashSet<PersonName>(1);
		PersonName personName = new PersonName();
		personName.setFamilyName("name");
		personNames.add(personName);
		person.setNames(personNames);
		provider.setPerson(person);
		
		providerValidator.validate(provider, errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link ProviderValidator#validate(Object, Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if field lengths are not correct", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
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
