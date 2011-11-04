package org.openmrs.customdatatype.datatype;

import java.text.SimpleDateFormat;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DateTest {
	
	DateDatatype datatype;
	
	@Before
	public void before() {
		datatype = new DateDatatype();
	}
	
	/**
	 * @see DateDatatype#fromReferenceString(String)
	 * @verifies reconstruct a date serialized by this handler
	 */
	@Test
	public void fromPersistentString_shouldReconstructADateSerializedByThisHandler() throws Exception {
		java.util.Date date = new SimpleDateFormat("yyyy-MM-dd").parse("2011-04-25");
		Assert.assertEquals(date, datatype.fromReferenceString(datatype.toReferenceString(date)));
	}
	
	/**
	 * @see DateDatatype#toReferenceString(DateDatatype)
	 * @verifies convert a date into a ymd string representation
	 */
	@Test
	public void toPersistentString_shouldConvertADateIntoAYmdStringRepresentation() throws Exception {
		java.util.Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2011-04-25 01:02:03");
		Assert.assertEquals("2011-04-25", datatype.toReferenceString(date));
	}
}
