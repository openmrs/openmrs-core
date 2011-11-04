package org.openmrs.attribute;

import java.text.SimpleDateFormat;

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.BaseCustomizableData;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * Integration tests for using {@link BaseAttribute}, {@link BaseAttributeType}, and {@link AttributeHandler}
 * in concert.
 */
public class AttributeIntegrationTest extends BaseContextSensitiveTest {
	
	@Test
	public void shouldTestAttributeHandler() throws Exception {
		Visit visit = new Visit();
		VisitAttributeType paymentDateAttrType = new VisitAttributeType();
		paymentDateAttrType.setDatatypeClassname(org.openmrs.customdatatype.datatype.DateDatatype.class.getName());
		
		VisitAttribute legalDate = new VisitAttribute();
		legalDate.setAttributeType(paymentDateAttrType);
		// try using a subclass of java.util.Date, to make sure the handler can take subclasses.
		legalDate.setValue(new java.sql.Date(new SimpleDateFormat("yyyy-MM-dd").parse("2011-04-15").getTime()));
		Assert.assertEquals("2011-04-15", legalDate.getValueReference());
		Assert.assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse("2011-04-15"), legalDate.getValue());
		visit.addAttribute(legalDate);
		
		Assert.assertEquals(1, visit.getAttributes().size());
	}
	
	/**
	 * A parent class
	 */
	class Visit extends BaseCustomizableData<VisitAttribute> {
		
		@Override
		public Integer getId() {
			// not needed for testing
			return null;
		}
		
		@Override
		public void setId(Integer id) {
			// not needed for testing
		}
		
	}
	
	/**
	 * Attribute type for the parent class
	 */
	class VisitAttributeType extends BaseAttributeType<Visit> {
		
		@Override
		public Integer getId() {
			// not needed for testing
			return null;
		}
		
		@Override
		public void setId(Integer id) {
			// not needed for testing
		}
	}
	
	/**
	 * Attribute value for the parent class
	 */
	class VisitAttribute extends BaseAttribute<VisitAttributeType, Visit> {
		
		@Override
		public Integer getId() {
			// not needed for testing
			return null;
		}
		
		@Override
		public void setId(Integer id) {
			// not needed for testing
		}
	}
}
