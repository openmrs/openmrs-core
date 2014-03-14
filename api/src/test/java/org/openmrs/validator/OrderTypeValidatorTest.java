package org.openmrs.validator;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.ConceptClass;
import org.openmrs.OrderType;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import java.util.Collection;
import java.util.HashSet;

/**
 * Contains tests methods for the {@link OrderTypeValidator}
 */
public class OrderTypeValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link OrderTypeValidator#validate(Object,Errors)}
	 */
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "should fail if the orderType object is null", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheOrderTypeObjectIsNull() throws Exception {
		Errors errors = new BindException(new OrderType(), "orderType");
		new OrderTypeValidator().validate(null, errors);
	}
	
	/**
	 * @see {@link OrderTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if name is null", method = "validate(Object,Errors)")
	public void validate_shouldFailIfNameIsNull() throws Exception {
		OrderType orderType = new OrderType();
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link OrderTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if name is empty", method = "validate(Object,Errors)")
	public void validate_shouldFailIfNameIsEmpty() throws Exception {
		OrderType orderType = new OrderType();
		orderType.setName("");
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link OrderTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if name is white space", method = "validate(Object,Errors)")
	public void validate_shouldFailIfNameIsWhiteSpace() throws Exception {
		OrderType orderType = new OrderType();
		orderType.setName(" ");
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link OrderTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if name is a duplicate", method = "validate(Object,Errors)")
	public void validate_shouldFailIfNameIsADuplicate() throws Exception {
		OrderType orderType = new OrderType();
		orderType.setName("Drug order");
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link OrderTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if javaClass is a duplicate", method = "validate(Object,Errors)")
	public void validate_shouldFailIfJavaClassIsADuplicate() throws Exception {
		OrderType orderType = new OrderType();
		orderType.setName("java class test");
		orderType.setJavaClassName("org.openmrs.DrugOrder");
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("javaClassName"));
	}
	
	/**
	 * @see {@link OrderTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if conceptClass is a duplicate", method = "validate(Object,Errors)")
	public void validate_shouldFailIfConceptClassIsADuplicate() throws Exception {
		OrderType orderType = new OrderType();
		orderType.setName("concept class test");
		Collection<ConceptClass> col = new HashSet<ConceptClass>();
		col.add(Context.getConceptService().getConceptClass(1));
		orderType.setConceptClasses(col);
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("conceptClasses"));
	}
	
	/**
	 * @see {@link OrderTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass if all fields are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassIfAllFieldsAreCorrect() throws Exception {
		OrderType orderType = new OrderType();
		orderType.setName("unique name");
		orderType.setJavaClassName("org.openmrs.TestDrugOrder");
		Collection<ConceptClass> col = new HashSet<ConceptClass>();
		col.add(Context.getConceptService().getConceptClass(2));
		orderType.setConceptClasses(col);
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
}
