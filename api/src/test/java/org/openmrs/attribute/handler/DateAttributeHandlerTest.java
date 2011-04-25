package org.openmrs.attribute.handler;


import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DateAttributeHandlerTest {
	
	private DateAttributeHandler handler;
	
	@Before
	public void before() {
		handler = new DateAttributeHandler();
	}
	
	/**
	 * @see DateAttributeHandler#serialize(Date)
	 * @verifies convert a date into a ymd string representation
	 */
	@Test
	public void serialize_shouldConvertADateIntoAYmdStringRepresentation() throws Exception {
		Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2011-04-25 01:02:03");
		Assert.assertEquals("2011-04-25", handler.serialize(date));
	}

	/**
	 * @see DateAttributeHandler#deserialize(String)
	 * @verifies reconstruct a date serialized by this handler
	 */
	@Test
	public void deserialize_shouldReconstructADateSerializedByThisHandler() throws Exception {
		Date date = new SimpleDateFormat("yyyy-MM-dd").parse("2011-04-25");
		Assert.assertEquals(date, handler.deserialize(handler.serialize(date)));
	}

}