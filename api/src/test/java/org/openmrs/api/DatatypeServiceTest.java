/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.attribute.handler.DateDatatypeHandler;
import org.openmrs.attribute.handler.LocationDatatypeHandler;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.customdatatype.datatype.DateDatatype;
import org.openmrs.customdatatype.datatype.LocationDatatype;
import org.openmrs.test.BaseContextSensitiveTest;

public class DatatypeServiceTest extends BaseContextSensitiveTest {
	
	/**
	 * @see DatatypeService#getHandler(CustomDatatype,String)
	 */
	@Test
	public void getHandler_shouldReturnAHandlerForTheSpecifiedDatatype() {
		DatatypeService service = Context.getDatatypeService();
		CustomDatatype dateDatatype = CustomDatatypeUtil.getDatatype(DateDatatype.class.getName(), null);
		Assert.assertEquals(DateDatatypeHandler.class, service.getHandler(dateDatatype, null).getClass());
	}
	
	/**
	 * @see DatatypeService#getHandler(CustomDatatype,String)
	 */
	@Test
	public void getHandler_shouldReturnAHandlerForADatatypeThatExtendsAGenericSuperclass() {
		DatatypeService service = Context.getDatatypeService();
		CustomDatatype locationDatatype = CustomDatatypeUtil.getDatatype(LocationDatatype.class.getName(), null);
		Assert.assertEquals(LocationDatatypeHandler.class, service.getHandler(locationDatatype, null).getClass());
	}
}
