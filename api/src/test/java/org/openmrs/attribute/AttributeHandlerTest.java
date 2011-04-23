package org.openmrs.attribute;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * Some generic tests for AttributeHandlers
 */
public class AttributeHandlerTest extends BaseContextSensitiveTest {
	
	@Test(expected = InvalidAttributeValueException.class)
	public void shouldTestAttributeHandler() throws Exception {
		VisitAttributeType paymentDateAttrType = new VisitAttributeType();
		paymentDateAttrType.setLogicalType("date");
		
		try {
			VisitAttribute legalDate = new VisitAttribute();
			legalDate.setAttributeType(paymentDateAttrType);
			//legalDate.setObjectValue(new SimpleDateFormat("yyyy-MM-dd").parse("2011-04-15"));
			legalDate.setObjectValue(new java.sql.Date(new SimpleDateFormat("yyyy-MM-dd").parse("2011-04-15").getTime()));
			Assert.assertEquals("2011-04-15", legalDate.getSerializedValue());
			Assert.assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse("2011-04-15"), legalDate.getObjectValue());
		}
		catch (InvalidAttributeValueException ex) {
			// should not fail here
			throw new RuntimeException(ex);
		}
		
		VisitAttribute illegalDate = new VisitAttribute();
		illegalDate.setAttributeType(paymentDateAttrType);
		illegalDate.setObjectValue(new Date(System.currentTimeMillis() + 100000));
	}
	
	class Visit implements AttributeHolder<VisitAttribute> {
		
		@Override
		public Set<VisitAttribute> getAttributes() {
			return null;
		}
		
		@Override
		public List<VisitAttribute> getActiveAttributes() {
			return null;
		}
	}
	
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
	
	class VisitAttribute extends BaseAttribute<Visit> {
		
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
