/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.api.db.hibernate;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Form;
import org.openmrs.FormResource;
import org.openmrs.api.DatatypeService;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ClobDatatypeStorage;
import org.openmrs.test.BaseContextSensitiveTest;

public class HibernateFormDAOTest extends BaseContextSensitiveTest {
	
	private static final String FORM_DATA_XML = "org/openmrs/api/include/HibernateFormDAOTest.xml";
	
	private FormService formService;
	
	private HibernateFormDAO formDao = null;
	
	private DatatypeService dataTypeService;
	
	/**
	 * Run this before each unit test in this class.
	 *
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {

        executeDataSet(FORM_DATA_XML);

		formService = Context.getFormService();
		dataTypeService = Context.getDatatypeService();
		
		if (formDao == null)
			formDao = (HibernateFormDAO) applicationContext.getBean("formDAO");
		

	}
	
	/**
	 * Verifies whether the corresponding row in clob_datatype_storage table is deleted when
	 * deleting a FormResource of 'LongFreeText' datatype
	 * @throws Exception
	 */
	@Test
	public void deleteFormResource_shouldDeleteClobDatatypeRowWhenDeletingLocalTextFreeTypeResource() throws Exception {
		
		Form form = formService.getForm(1);
		FormResource formResource = new FormResource();
		formResource.setForm(form);
		formResource.setFormResourceId(1);
		formResource.setName("Test Resource");
		formResource.setDatatypeClassname("org.openmrs.customdatatype.datatype.LongFreeTextDatatype");
		formResource.setValue("Test String");
		formService.saveFormResource(formResource);
		
		// to make sure formResource is created, verify whether a 'value reference'
		// is created for formResource after saving it
		Assert.assertNotNull(formResource.getValueReference());
		
		// make sure a row is added in the clob_datatype_storage table with value ref. of formResource as uuid
		ClobDatatypeStorage clobDatatypeEntry = dataTypeService.getClobDatatypeStorageByUuid(formResource
		        .getValueReference());
		int clobEntryId = clobDatatypeEntry.getId();
		Assert.assertNotNull(clobDatatypeEntry);
		
		// delete the FormResource
		formDao.deleteFormResource(formResource);
		
		// assert that the clob datatype table row is too deleted
		Assert.assertNull(dataTypeService.getClobDatatypeStorage(clobEntryId));
		
	}
}
