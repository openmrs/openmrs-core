package org.openmrs.api;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.attribute.handler.AttributeHandler;
import org.openmrs.attribute.handler.DateAttributeHandler;
import org.openmrs.attribute.handler.RegexValidatedStringAttributeHandler;
import org.openmrs.attribute.handler.StringAttributeHandler;
import org.openmrs.test.BaseContextSensitiveTest;

public class AttributeServiceTest extends BaseContextSensitiveTest {
	
	AttributeService service;
	
	@Before
	public void before() {
		service = Context.getAttributeService();
	}
	
	/**
	 * @see AttributeService#getHandler(String,String)
	 * @verifies get a handler for the date datatype
	 */
	@Test
	public void getHandler_shouldGetAHandlerForTheDateDatatype() throws Exception {
		// ideally we'd have independent web-layer and api-layer tests
		AttributeHandler<?> handler = service.getHandler("date", null);
		Assert.assertTrue(DateAttributeHandler.class.isAssignableFrom(handler.getClass()));
	}
	
	/**
	 * @see AttributeService#getHandler(String,String)
	 * @verifies get a handler for the string with regex datatype
	 */
	@Test
	public void getHandler_shouldGetAHandlerForTheStringWithRegexDatatype() throws Exception {
		// ideally we'd have independent web-layer and api-layer tests
		AttributeHandler<?> handler = service.getHandler("regex-validated-string", null);
		Assert.assertTrue(RegexValidatedStringAttributeHandler.class.isAssignableFrom(handler.getClass()));
	}
	
	/**
	 * @see AttributeService#getHandler(String,String)
	 * @verifies get the default handler for an unknown datatype
	 */
	@Test
	public void getHandler_shouldGetTheDefaultHandlerForAnUnknownDatatype() throws Exception {
		// maybe this test should be on AttributeServiceImpl because the service interface doesn't define a default handler class  
		AttributeHandler<?> handler = service.getHandler("no-handler-for-this-unknown-type", null);
		Assert.assertEquals(StringAttributeHandler.class, handler.getClass());
	}
}
