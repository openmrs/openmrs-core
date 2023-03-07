/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.ObsResource1_8;
import org.openmrs.util.OpenmrsConstants;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ObsResource1_9Test extends BaseDelegatingResourceTest<ObsResource1_9, Obs> {
	
	public static final String BOOLEAN_CONCEPT_UUID = "0dde1358-7fcf-4341-a330-f119241a46e8";
	
	private Concept trueConcept;
	
	private Concept falseConcept;
	
	private enum ObsType {
		CODED, COMPLEX, DATETIME, DRUG, NUMERIC, TEXT
	}
	
	@Before
	public void setup() throws Exception {
		GlobalProperty trueConceptGlobalProperty = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_TRUE_CONCEPT, "7",
		        "Concept id of the concept defining the TRUE boolean concept");
		GlobalProperty falseConceptGlobalProperty = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_FALSE_CONCEPT, "8",
		        "Concept id of the concept defining the FALSE boolean concept");
		Context.getAdministrationService().saveGlobalProperty(trueConceptGlobalProperty);
		Context.getAdministrationService().saveGlobalProperty(falseConceptGlobalProperty);
		trueConcept = Context.getConceptService().getConcept(
		    Integer.parseInt(Context.getAdministrationService().getGlobalProperty(
		        OpenmrsConstants.GLOBAL_PROPERTY_TRUE_CONCEPT)));
		falseConcept = Context.getConceptService().getConcept(
		    Integer.parseInt(Context.getAdministrationService().getGlobalProperty(
		        OpenmrsConstants.GLOBAL_PROPERTY_FALSE_CONCEPT)));
		
	}
	
	@Override
	public Obs newObject() {
		return Context.getObsService().getObsByUuid(getUuidProperty());
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropPresent("person");
		assertPropPresent("concept");
		assertPropPresent("value");
		assertPropEquals("obsDatetime", getObject().getObsDatetime());
		assertPropEquals("accessionNumber", getObject().getAccessionNumber());
		assertPropEquals("obsGroup", getObject().getObsGroup());
		assertPropPresent("groupMembers");
		assertPropEquals("comment", getObject().getComment());
		assertPropPresent("location");
		assertPropPresent("order");
		assertPropPresent("encounter");
		assertPropEquals("voided", getObject().getVoided());
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropPresent("person");
		assertPropPresent("concept");
		assertPropPresent("value");
		assertPropEquals("obsDatetime", getObject().getObsDatetime());
		assertPropEquals("accessionNumber", getObject().getAccessionNumber());
		assertPropEquals("obsGroup", getObject().getObsGroup());
		assertPropPresent("groupMembers");
		assertPropEquals("comment", getObject().getComment());
		assertPropPresent("location");
		assertPropPresent("order");
		assertPropPresent("encounter");
		assertPropEquals("voided", getObject().getVoided());
		assertPropPresent("auditInfo");
	}
	
	@Override
	public String getDisplayProperty() {
		return "WEIGHT (KG): 50.0";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_8.OBS_UUID;
	}
	
	@Test
	public void asRepresentation_shouldReturnProperlyEncodedValues() throws Exception {
		Obs obs = getObject();
		obs.setComment(null); // to test that we don't get a NPE when no comment (specifically with the Location example)
		
		// coded
		Concept concept = Context.getConceptService().getConceptByUuid("a09ab2c5-878e-4905-b25d-5784167d0216");
		clearAndSetValue(obs, ObsType.CODED, concept);
		SimpleObject rep = getResource().asRepresentation(getObject(), Representation.DEFAULT);
		Assert.assertTrue(rep.keySet().contains("value"));
		rep = (SimpleObject) rep.get("value");
		Assert.assertEquals("coded", concept.getUuid(), rep.get("uuid"));
		
		// datetime
		Date datetime = new Date();
		clearAndSetValue(obs, ObsType.DATETIME, datetime);
		rep = getResource().asRepresentation(getObject(), Representation.DEFAULT);
		Assert.assertEquals("datetime", datetime, ConversionUtil.convert(rep.get("value"), Date.class));
		
		// drug
		Drug drug = Context.getConceptService().getDrugByUuid("3cfcf118-931c-46f7-8ff6-7b876f0d4202");
		clearAndSetValue(obs, ObsType.DRUG, drug);
		rep = getResource().asRepresentation(getObject(), Representation.DEFAULT);
		Assert.assertTrue(rep.keySet().contains("value"));
		rep = (SimpleObject) rep.get("value");
		Assert.assertEquals("drug", drug.getUuid(), rep.get("uuid"));
		
		// string-based (complex, text)
		String test = "whoa";
		clearAndSetValue(obs, ObsType.COMPLEX, test);
		rep = getResource().asRepresentation(getObject(), Representation.DEFAULT);
		Assert.assertEquals("complex", test, rep.get("value"));
		
		clearAndSetValue(obs, ObsType.TEXT, test);
		rep = getResource().asRepresentation(getObject(), Representation.DEFAULT);
		Assert.assertEquals("text", test, rep.get("value"));
		
		// numeric
		Double number = Double.MAX_VALUE;
		clearAndSetValue(obs, ObsType.NUMERIC, number);
		rep = getResource().asRepresentation(getObject(), Representation.DEFAULT);
		Assert.assertEquals("numeric", number, rep.get("value"));
		
		// location
		Location location = Context.getLocationService().getLocation(2);
		clearAndSetValue(obs, ObsType.TEXT, location.getId().toString());
		obs.setComment("org.openmrs.Location");
		rep = getResource().asRepresentation(getObject(), Representation.DEFAULT);
		Assert.assertTrue(rep.keySet().contains("value"));
		rep = (SimpleObject) rep.get("value");
		Assert.assertEquals("location", location.getUuid(), rep.get("uuid"));
		;
		
		// location referenced by uuid
		location = Context.getLocationService().getLocation(2);
		clearAndSetValue(obs, ObsType.TEXT, location.getUuid());
		obs.setComment("org.openmrs.Location");
		rep = getResource().asRepresentation(getObject(), Representation.DEFAULT);
		Assert.assertTrue(rep.keySet().contains("value"));
		rep = (SimpleObject) rep.get("value");
		Assert.assertEquals("location", location.getUuid(), rep.get("uuid"));
		
		// location that doesn't exist shouldn't cause error, just return null
		clearAndSetValue(obs, ObsType.TEXT, "20000");
		obs.setComment("org.openmrs.Location");
		rep = getResource().asRepresentation(getObject(), Representation.DEFAULT);
		Assert.assertNull(rep.get("value"));
		rep = (SimpleObject) rep.get("value");
	}
	
	@Test
	public void setGroupMembers_shouldSetGroupMembers() throws Exception {
		executeDataSet("obsWithGroupMembers.xml");
		ObsResource1_8 resource = getResource();
		Obs groupMemberParent = resource.getByUniqueId("47f18998-96cc-11e0-8d6b-9b9415a91423");
		
		Set<Obs> groupMembersBefore = groupMemberParent.getGroupMembers();
		Set<Obs> groupMembersAfter = (Set<Obs>) ObsResource1_8.getGroupMembers(resource
		        .getByUniqueId("5117f5d4-96cc-11e0-8d6b-9b9415a91433"));
		
		ObsResource1_8.setGroupMembers(groupMemberParent, groupMembersAfter);
		assertNotEquals(groupMembersBefore, groupMemberParent.getGroupMembers());
		assertEquals(groupMembersAfter, groupMemberParent.getGroupMembers());
	}
	
	@Test
	public void getGroupMembers_shouldReturnAllGroupMembers() throws Exception {
		executeDataSet("obsWithGroupMembers.xml");
		
		ObsResource1_8 resource = getResource();
		
		Set<Obs> groupMembers1 = (Set<Obs>) ObsResource1_8.getGroupMembers(resource
		        .getByUniqueId("47f18998-96cc-11e0-8d6b-9b9415a91423"));
		assertEquals(1, groupMembers1.size());
		
		Set<Obs> groupMembers2 = (Set<Obs>) ObsResource1_8.getGroupMembers(resource
		        .getByUniqueId("5117f5d4-96cc-11e0-8d6b-9b9415a91433"));
		assertEquals(2, groupMembers2.size());
		
	}
	
	@Test
	public void setValue_shouldReturnUuidForConceptTrue() throws Exception {
		Obs obs = new Obs();
		obs.setConcept(Context.getConceptService().getConceptByUuid(BOOLEAN_CONCEPT_UUID));
		ObsResource1_8.setValue(obs, trueConcept);
		assertEquals(trueConcept, ObsResource1_8.getValue(obs));
	}
	
	@Test
	public void setValue_shouldReturnDrug() throws Exception {
		Obs obs = new Obs();
		Concept concept = Context.getConceptService().getConceptByUuid("89ca642a-dab6-4f20-b712-e12ca4fc6d36");
		obs.setConcept(concept);
		// drug
		String drugUuid = "3cfcf118-931c-46f7-8ff6-7b876f0d4202";
		Drug drug = Context.getConceptService().getDrugByUuid(drugUuid);
		ObsResource1_8.setValue(obs, drugUuid);
		assertEquals(drug, ObsResource1_8.getValue(obs));
	}
	
	@Test
	public void setValue_shouldReturnUuidForConceptFalse() throws Exception {
		Obs obs = new Obs();
		obs.setConcept(Context.getConceptService().getConceptByUuid(BOOLEAN_CONCEPT_UUID));
		ObsResource1_8.setValue(obs, falseConcept);
		assertEquals(falseConcept, ObsResource1_8.getValue(obs));
	}
	
	@Test(expected = ConversionException.class)
	public void setValue_shouldThrowExceptionOnUnexpectedValue() throws Exception {
		Obs obs = new Obs();
		obs.setConcept(Context.getConceptService().getConceptByUuid(BOOLEAN_CONCEPT_UUID));
		ObsResource1_8.setValue(obs, "unexpected");
	}
	
	@Test
	public void setValue_shouldReturnUuidForPrimitiveTrue() throws Exception {
		Obs obs = new Obs();
		obs.setConcept(Context.getConceptService().getConceptByUuid(BOOLEAN_CONCEPT_UUID));
		ObsResource1_8.setValue(obs, true);
		assertEquals(trueConcept, ObsResource1_8.getValue(obs));
	}
	
	@Test
	public void setValue_shouldReturnUuidForPrimitiveFalse() throws Exception {
		Obs obs = new Obs();
		obs.setConcept(Context.getConceptService().getConceptByUuid(BOOLEAN_CONCEPT_UUID));
		ObsResource1_8.setValue(obs, false);
		assertEquals(falseConcept, ObsResource1_8.getValue(obs));
	}
	
	private void clearAndSetValue(Obs obs, ObsType type, Object value) {
		obs.setValueCoded(type.equals(ObsType.CODED) ? (Concept) value : null);
		obs.setValueComplex(type.equals(ObsType.COMPLEX) ? (String) value : null);
		obs.setValueDatetime(type.equals(ObsType.DATETIME) ? (Date) value : null);
		obs.setValueDrug(type.equals(ObsType.DRUG) ? (Drug) value : null);
		obs.setValueNumeric(type.equals(ObsType.NUMERIC) ? (Double) value : null);
		obs.setValueText(type.equals(ObsType.TEXT) ? (String) value : null);
	}
	
	@Test
	public void setConvertedProperties_shouldAllowAnyPropertyOrder() throws Exception {
		ObsResource1_8 resource = getResource();
		Obs obs = getObject();
		
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("value", 10.0);
		propertyMap.put("person", RestTestConstants1_8.PERSON_UUID);
		propertyMap.put("concept", "c607c80f-1ea9-4da3-bb88-6276ce8868dd");
		propertyMap.put("obsDatetime", "2013-12-09T00:00:00.000+0100");
		
		resource.setConvertedProperties(obs, propertyMap, resource.getUpdatableProperties(), false);
		org.springframework.util.Assert.isTrue(((Double) ObsResource1_8.getValue(obs)) == 10.0);
	}
}
