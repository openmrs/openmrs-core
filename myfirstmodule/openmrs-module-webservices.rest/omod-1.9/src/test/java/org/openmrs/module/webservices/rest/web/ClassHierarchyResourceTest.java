/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Order;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.OrderResource1_8;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Integration tests for the framework that lets a resource handle an entire class hierarchy
 */
public class ClassHierarchyResourceTest extends BaseModuleWebContextSensitiveTest {
	
	private static final String DATASET_FILENAME = "customTestDataset.xml";
	
	private static final String PATIENT_UUID = "5946f880-b197-400b-9caa-a3c661d23041";
	
	private static final String SUPERCLASS_UUID = "ff97d3a0-8dbf-11e1-bc86-da3a922f3783";
	
	private static final String SUBCLASS_UUID = "921de0a3-05c4-444a-be03-e01b4c4b9142";
	
	private static final String ASPIRIN_CONCEPT_UUID = "15f83cd6-64e9-4e06-a5f9-364d3b14a43d";
	
	private static final String ASPIRIN_DRUG_UUID = "05ec820a-d297-44e3-be6e-698531d9dd3f";
	
	private static final String LUNCH_ORDER_TYPE_UUID = "e23733ab-787e-4096-8ba2-577a902d2c2b";
	
	RequestContext context;
	
	OrderResource1_8 resource;
	
	@Before
	public void beforeEachTests() throws Exception {
		executeDataSet(DATASET_FILENAME);
		context = new RequestContext();
		resource = (OrderResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(Order.class);
	}
	
	private SimpleObject buildSuperclass() {
		return new SimpleObject().add(RestConstants.PROPERTY_FOR_TYPE, "order").add("startDate", "2011-02-03")
		        .add("patient", PATIENT_UUID).add("concept", ASPIRIN_CONCEPT_UUID).add("orderType", LUNCH_ORDER_TYPE_UUID);
	}
	
	private SimpleObject buildSubclass() {
		return buildSuperclass().removeProperty("orderType").add(RestConstants.PROPERTY_FOR_TYPE, "drugorder")
		        .add("dose", "100").add("units", "mg").add("prn", "true").add("complex", "false")
		        .add("drug", ASPIRIN_DRUG_UUID);
	}
	
	@Test
	public void shouldCreateASuperclass() throws Exception {
		SimpleObject created = (SimpleObject) resource.create(buildSuperclass(), context);
		Util.log("Created superclass", created);
		Assert.assertEquals("order", created.get("type"));
	}
	
	@Test
	public void shouldCreateASubclass() throws Exception {
		SimpleObject created = (SimpleObject) resource.create(buildSubclass(), context);
		Util.log("Created subclass", created);
		Assert.assertEquals("drugorder", created.get("type"));
	}
	
	@Test
	public void shouldRetrieveASuperclass() throws Exception {
		SimpleObject retrieved = (SimpleObject) resource.retrieve(SUPERCLASS_UUID, context);
		Util.log("Retrieved superclass", retrieved);
		Assert.assertEquals("order", retrieved.get("type"));
		Assert.assertEquals("a09ab2c5-878e-4905-b25d-5784167d0216", Util.getByPath(retrieved, "concept/uuid"));
	}
	
	@Test
	public void shouldRetrieveASubclass() throws Exception {
		SimpleObject retrieved = (SimpleObject) resource.retrieve(SUBCLASS_UUID, context);
		Util.log("Retrieved subclass", retrieved);
		Assert.assertEquals("drugorder", retrieved.get("type"));
		Assert.assertEquals(325d, retrieved.get("dose"));
	}
	
	@Test
	public void shouldUpdateASuperclass() throws Exception {
		String newValue = "Do a CD4 Test STAT!";
		Object updated = resource.update(SUPERCLASS_UUID, new SimpleObject().add("instructions", newValue), context);
		Util.log("Updated subclass", updated);
		Assert.assertEquals(newValue, PropertyUtils.getProperty(updated, "instructions"));
	}
	
	@Test
	public void shouldUpdateASubclass() throws Exception {
		Object updated = resource.update(SUBCLASS_UUID, new SimpleObject().add("dose", "500"), context);
		Util.log("Updated subclass", updated);
		Assert.assertEquals(500d, PropertyUtils.getProperty(updated, "dose"));
	}
	
	@Test
	public void shouldDeleteASuperclass() throws Exception {
		resource.delete(SUPERCLASS_UUID, "because", context);
		Order deleted = Context.getOrderService().getOrderByUuid(SUPERCLASS_UUID);
		Assert.assertTrue(deleted.isVoided());
	}
	
	@Test
	public void shouldDeleteASubclass() throws Exception {
		resource.delete(SUBCLASS_UUID, "because", context);
		Order deleted = Context.getOrderService().getOrderByUuid(SUBCLASS_UUID);
		Assert.assertTrue(deleted.isVoided());
	}
	
	@Test
	public void shouldPurgeASuperclass() throws Exception {
		resource.purge(SUPERCLASS_UUID, context);
		Order purged = Context.getOrderService().getOrderByUuid(SUPERCLASS_UUID);
		Assert.assertNull(purged);
	}
	
	@Test
	public void shouldPurgeASubclass() throws Exception {
		resource.purge(SUBCLASS_UUID, context);
		Order purged = Context.getOrderService().getOrderByUuid(SUBCLASS_UUID);
		Assert.assertNull(purged);
	}
	
	@Test
	public void shouldGetAll() throws Exception {
		SimpleObject all = resource.getAll(context);
		Util.log("Get all", all);
		Assert.assertEquals(6, Util.getResultsSize(all));
		// ensure the type property gets added when we return multiple
		Object typeForFirst = Util.getByPath(all, "results[0]/type");
		Assert.assertTrue("drugorder".equals(typeForFirst) || "order".equals(typeForFirst));
	}
	
	@Test
	public void shouldGetAllOfSubclass() throws Exception {
		context.setType("drugorder");
		SimpleObject all = resource.getAll(context);
		Util.log("Get all of subclass", all);
		Assert.assertEquals(5, Util.getResultsSize(all));
		Assert.assertEquals("drugorder", Util.getByPath(all, "results[0]/type"));
	}
	
	@Test
	public void shouldUsePropertySetterAndGetterFromSubclassHandler() throws Exception {
		HivDrugOrderSubclassHandler handler = new HivDrugOrderSubclassHandler();
		resource.registerSubclassHandler(handler);
		HivDrugOrder o = handler.newDelegate();
		// this will only work if the @PropertySetter method on the subclass handler is used 
		resource.setProperty(o, "standardRegimenCode", "Peds-1a");
		// this will only work if the @PropertyGetter method on the subclass handler is used
		Object valueSet = resource.getProperty(o, "standardRegimenCode");
		Assert.assertEquals("Peds-1a", valueSet);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldNotAllowSpecifyingDefaultTypeOnGetAll() throws Exception {
		context.setType("order");
		resource.getAll(context);
	}
	
	@Test
	public void shouldGetAllOrdersForAPatient() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("patient", PATIENT_UUID);
		context.setRequest(request);
		SimpleObject simple = resource.search(context);
		Util.log("all orders for patient", simple);
		Assert.assertEquals(2, Util.getResultsSize(simple));
		Object typeForFirst = Util.getByPath(simple, "results[0]/type");
		Assert.assertTrue("drugorder".equals(typeForFirst) || "order".equals(typeForFirst));
	}
	
	@Test
	public void shouldGetAllDrugOrdersForAPatient() throws Exception {
		context.setType("drugorder");
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("patient", PATIENT_UUID);
		context.setRequest(request);
		SimpleObject simple = resource.search(context);
		Util.log("drug orders for patient", simple);
		Assert.assertEquals(1, Util.getResultsSize(simple));
		Assert.assertEquals("drugorder", Util.getByPath(simple, "results[0]/type"));
	}
	
}
