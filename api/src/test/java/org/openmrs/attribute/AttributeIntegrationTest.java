package org.openmrs.attribute;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.attribute.handler.AttributeHandler;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * Integration tests for using {@link BaseAttribute}, {@link BaseAttributeType}, and {@link AttributeHandler}
 * in concert.
 */
public class AttributeIntegrationTest extends BaseContextSensitiveTest {
	
	@Test(expected = InvalidAttributeValueException.class)
	public void shouldTestAttributeHandler() throws Exception {
		Visit visit = new Visit();
		VisitAttributeType paymentDateAttrType = new VisitAttributeType();
		paymentDateAttrType.setLogicalType("date");
		
		try {
			VisitAttribute legalDate = new VisitAttribute();
			legalDate.setAttributeType(paymentDateAttrType);
			// try using a subclass of java.util.Date, to make sure the handler can take subclasses.
			legalDate.setObjectValue(new java.sql.Date(new SimpleDateFormat("yyyy-MM-dd").parse("2011-04-15").getTime()));
			Assert.assertEquals("2011-04-15", legalDate.getSerializedValue());
			Assert.assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse("2011-04-15"), legalDate.getObjectValue());
			visit.addAttribute(legalDate);
		}
		catch (InvalidAttributeValueException ex) {
			Assert.fail("should not fail on a legal date");
		}
		Assert.assertEquals(1, visit.getAttributes().size());
		
		VisitAttribute illegalDate = new VisitAttribute();
		illegalDate.setAttributeType(paymentDateAttrType);
		illegalDate.setObjectValue(new Date(System.currentTimeMillis() + 100000));
	}
	
	/**
	 * A parent class
	 */
	class Visit implements AttributeHolder<VisitAttribute> {
		
		private Set<VisitAttribute> attributes = new HashSet<AttributeIntegrationTest.VisitAttribute>();
		
		public void addAttribute(VisitAttribute attr) {
			attributes.add(attr);
		}
		
		@Override
		public Set<VisitAttribute> getAttributes() {
			return attributes;
		}
		
		@Override
		public List<VisitAttribute> getActiveAttributes() {
			List<VisitAttribute> ret = new ArrayList<AttributeIntegrationTest.VisitAttribute>();
			for (VisitAttribute attr : getAttributes())
				if (!attr.isVoided())
					ret.add(attr);
			return ret;
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
