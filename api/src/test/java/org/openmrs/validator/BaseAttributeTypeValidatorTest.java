package org.openmrs.validator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.VisitAttributeType;
import org.openmrs.attribute.AttributeType;
import org.openmrs.customdatatype.datatype.RegexValidatedTextDatatype;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

public class BaseAttributeTypeValidatorTest extends BaseContextSensitiveTest {
	
	VisitAttributeTypeValidator validator;
	
	@SuppressWarnings("rawtypes")
	VisitAttributeType attributeType;
	
	BindException errors;
	
	@Before
	public void before() {
		validator = new VisitAttributeTypeValidator();
		attributeType = new VisitAttributeType();
		errors = new BindException(attributeType, "attributeType");
	}
	
	/**
	 * @see BaseAttributeTypeValidator#validate(Object,Errors)
	 * @verifies not allow maxOccurs less than 1
	 */
	@Test
	public void validate_shouldNotAllowMaxOccursLessThan1() throws Exception {
		attributeType.setMaxOccurs(0);
		validator.validate(attributeType, errors);
		Assert.assertTrue(errors.getFieldErrors("maxOccurs").size() > 0);
	}
	
	/**
	 * @see BaseAttributeTypeValidator#validate(Object,Errors)
	 * @verifies not allow maxOccurs less than minOccurs
	 */
	@Test
	public void validate_shouldNotAllowMaxOccursLessThanMinOccurs() throws Exception {
		attributeType.setMinOccurs(3);
		attributeType.setMaxOccurs(2);
		validator.validate(attributeType, errors);
		Assert.assertTrue(errors.getFieldErrors("maxOccurs").size() > 0);
	}
	
	/**
	 * @see BaseAttributeTypeValidator#validate(Object,Errors)
	 * @verifies require datatypeClassname
	 */
	@Test
	public void validate_shouldRequireDatatypeClassname() throws Exception {
		validator.validate(attributeType, errors);
		Assert.assertTrue(errors.getFieldErrors("datatypeClassname").size() > 0);
	}
	
	/**
	 * @see BaseAttributeTypeValidator#validate(Object,Errors)
	 * @verifies require minOccurs
	 */
	@Test
	public void validate_shouldRequireMinOccurs() throws Exception {
		attributeType.setMinOccurs(null);
		validator.validate(attributeType, errors);
		Assert.assertTrue(errors.getFieldErrors("minOccurs").size() > 0);
	}
	
	/**
	 * @see BaseAttributeTypeValidator#validate(Object,Errors)
	 * @verifies require name
	 */
	@Test
	public void validate_shouldRequireName() throws Exception {
		validator.validate(attributeType, errors);
		Assert.assertTrue(errors.getFieldErrors("name").size() > 0);
	}
	
	/**
	 * @see BaseAttributeTypeValidator#validate(Object,Errors)
	 * @verifies require DatatypeConfiguration if Datatype equals Regex-Validated Text
	 */
	@Test
	public void validate_shouldRequireDatatypeConfigurationIfDatatypeEqualsRegexValidatedText() throws Exception {
		attributeType.setDatatypeClassname(RegexValidatedTextDatatype.class.getName());
		validator.validate(attributeType, errors);
		Assert.assertTrue(errors.getFieldErrors("datatypeConfig").size() > 0);
	}
	
	/**
	 * @see BaseAttributeTypeValidator#validate(Object,Errors)
	 * @verifies pass validation if all required values are set
	 */
	@Test
	public void validate_shouldPassValidationIfAllRequiredValuesAreSet() throws Exception {
		attributeType.setName("name");
		attributeType.setMinOccurs(1);
		attributeType.setDatatypeClassname(RegexValidatedTextDatatype.class.getName());
		attributeType.setDatatypeConfig("[a-z]+");
		validator.validate(attributeType, errors);
		Assert.assertFalse(errors.hasErrors());
	}
}
