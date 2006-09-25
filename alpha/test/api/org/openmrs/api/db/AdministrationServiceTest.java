package org.openmrs.api.db;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmrs.EncounterType;
import org.openmrs.FieldType;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.OrderType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextFactory;

public class AdministrationServiceTest extends TestCase {
	
	protected PatientService ps;
	protected UserService us;
	protected AdministrationService as;
	protected ObsService obsService;
	protected OrderService orderService;
	protected FormService formService;
	protected EncounterService encounterService;
	protected PatientService patientService;
	
	public void setUp() throws Exception{
		Context context = ContextFactory.getContext();
		
		context.authenticate("USER-1", "test");
		
		encounterService = context.getEncounterService();
		assertNotNull(encounterService);
		ps = context.getPatientService();
		assertNotNull(ps);
		us = context.getUserService();
		assertNotNull(us);
		as = context.getAdministrationService();
		assertNotNull(as);
		obsService = context.getObsService();
		assertNotNull(obsService);
		orderService = context.getOrderService();
		assertNotNull(orderService);
		formService = context.getFormService();
		assertNotNull(formService);
		patientService = context.getPatientService();
		assertNotNull(patientService);
		
	}

	public void testMimeType() throws Exception {
		
		//testing equals()/hashset() for mimetype /////////// 
		
		HashSet set = new HashSet();
		
		MimeType m1 = new MimeType();
		MimeType m2 = new MimeType();
		m1.setMimeType("test1");
		m2.setMimeType("test2");
		set.add(m1);
		set.add(m2);
		
		as.createMimeType(m1);
		
		assertTrue(set.size() == 2);
		assertNotNull(set.contains(m1));
		////////////////////////////////////////
		
		
		//testing creation
		
		MimeType mimeType = new MimeType();
		
		mimeType.setMimeType("testing");
		mimeType.setDescription("desc");
		
		as.createMimeType(mimeType);
		
		MimeType newMimeType = obsService.getMimeType(mimeType.getMimeTypeId());
		assertNotNull(newMimeType);
		
		List<MimeType> mimeTypes = obsService.getMimeTypes();
		
		//make sure we get a list
		assertNotNull(mimeTypes);
		
		boolean found = false;
		for(Iterator i = mimeTypes.iterator(); i.hasNext();) {
			MimeType mimeType2 = (MimeType)i.next();
			assertNotNull(mimeType);
			//check .equals function
			assertTrue(mimeType.equals(mimeType2) == (mimeType.getMimeTypeId().equals(mimeType2.getMimeTypeId())));
			//mark found flag
			if (mimeType.equals(mimeType2))
				found = true;
		}
		
		//assert that the new mimeType was returned in the list
		assertTrue(found);
		
		
		//check updation
		newMimeType.setMimeType("another test");
		as.updateMimeType(newMimeType);
		
		MimeType newerMimeType = obsService.getMimeType(newMimeType.getMimeTypeId());
		assertTrue(newerMimeType.getMimeType().equals(newMimeType.getMimeType()));
		
		
		//check deletion
		as.deleteMimeType(newerMimeType);
		
		assertNull(obsService.getMimeType(newMimeType.getMimeTypeId()));

	}

	public void testOrderType() throws Exception {
		
		//testing creation
		
		OrderType orderType = new OrderType();
		
		orderType.setName("testing");
		orderType.setDescription("desc");
		
		as.createOrderType(orderType);
		
		OrderType newOrderType = orderService.getOrderType(orderType.getOrderTypeId());
		assertNotNull(newOrderType);
		
		List<OrderType> orderTypes = orderService.getOrderTypes();
		
		//make sure we get a list
		assertNotNull(orderTypes);
		
		boolean found = false;
		for(Iterator i = orderTypes.iterator(); i.hasNext();) {
			OrderType orderType2 = (OrderType)i.next();
			assertNotNull(orderType);
			//check .equals function
			assertTrue(orderType.equals(orderType2) == (orderType.getOrderTypeId().equals(orderType2.getOrderTypeId())));
			//mark found flag
			if (orderType.equals(orderType2))
				found = true;
		}
		
		//assert that the new orderType was returned in the list
		assertTrue(found);
		
		
		//check updation
		newOrderType.setName("another test");
		as.updateOrderType(newOrderType);
		
		OrderType newerOrderType = orderService.getOrderType(newOrderType.getOrderTypeId());
		assertTrue(newerOrderType.getName().equals(newOrderType.getName()));
		
		
		//check deletion
		as.deleteOrderType(newOrderType);
		assertNull(orderService.getOrderType(newOrderType.getOrderTypeId()));

	}
	
	public void testFieldType() throws Exception {
		
		//testing creation
		
		FieldType fieldType = new FieldType();
		
		fieldType.setName("testing");
		fieldType.setDescription("desc");
		fieldType.setIsSet(true);
		
		as.createFieldType(fieldType);
		
		FieldType newFieldType = formService.getFieldType(fieldType.getFieldTypeId());
		assertNotNull(newFieldType);
		
		List<FieldType> fieldTypes = formService.getFieldTypes();
		
		//make sure we get a list
		assertNotNull(fieldTypes);
		
		boolean found = false;
		for(Iterator i = fieldTypes.iterator(); i.hasNext();) {
			FieldType fieldType2 = (FieldType)i.next();
			assertNotNull(fieldType);
			//check .equals function
			assertTrue(fieldType.equals(fieldType2) == (fieldType.getFieldTypeId().equals(fieldType2.getFieldTypeId())));
			//mark found flag
			if (fieldType.equals(fieldType2))
				found = true;
		}
		
		//assert that the new fieldType was returned in the list
		assertTrue(found);
		
		
		//check updation
		newFieldType.setName("another test");
		as.updateFieldType(newFieldType);
		
		FieldType newerFieldType = formService.getFieldType(newFieldType.getFieldTypeId());
		assertTrue(newerFieldType.getName().equals(newFieldType.getName()));
		
		
		//check deletion
		as.deleteFieldType(newFieldType);
		assertNull(formService.getFieldType(newFieldType.getFieldTypeId()));

	}
	
	public void testPatientIdentifierType() throws Exception {
		
		//testing creation
		
		PatientIdentifierType patientIdentifierType = new PatientIdentifierType();
		
		patientIdentifierType.setName("testing");
		patientIdentifierType.setDescription("desc");
		
		as.createPatientIdentifierType(patientIdentifierType);
		
		PatientIdentifierType newPatientIdentifierType = patientService.getPatientIdentifierType(patientIdentifierType.getPatientIdentifierTypeId());
		assertNotNull(newPatientIdentifierType);
		
		List<PatientIdentifierType> patientIdentifierTypes = patientService.getPatientIdentifierTypes();
		
		//make sure we get a list
		assertNotNull(patientIdentifierTypes);
		
		boolean found = false;
		for(Iterator i = patientIdentifierTypes.iterator(); i.hasNext();) {
			PatientIdentifierType patientIdentifierType2 = (PatientIdentifierType)i.next();
			assertNotNull(patientIdentifierType);
			//check .equals function
			assertTrue(patientIdentifierType.equals(patientIdentifierType2) == (patientIdentifierType.getPatientIdentifierTypeId().equals(patientIdentifierType2.getPatientIdentifierTypeId())));
			//mark found flag
			if (patientIdentifierType.equals(patientIdentifierType2))
				found = true;
		}
		
		//assert that the new patientIdentifierType was returned in the list
		assertTrue(found);
		
		
		//check updation
		newPatientIdentifierType.setName("another test");
		as.updatePatientIdentifierType(newPatientIdentifierType);
		
		PatientIdentifierType newerPatientIdentifierType = patientService.getPatientIdentifierType(newPatientIdentifierType.getPatientIdentifierTypeId());
		assertTrue(newerPatientIdentifierType.getPatientIdentifierTypeId().equals(newPatientIdentifierType.getPatientIdentifierTypeId()));
		
		
		//check deletion
		as.deletePatientIdentifierType(newPatientIdentifierType);
		assertNull(patientService.getPatientIdentifierType(newPatientIdentifierType.getPatientIdentifierTypeId()));

	}
	
	public void testEncounterType() throws Exception {
		
		//testing creation
		
		EncounterType encounterType = new EncounterType();
		
		encounterType.setName("testing");
		encounterType.setDescription("desc");
		
		as.createEncounterType(encounterType);
		
		//assertNotNull(newE);
		
		EncounterType newEncounterType = encounterService.getEncounterType(encounterType.getEncounterTypeId());
		assertNotNull(newEncounterType);
		
		List<EncounterType> encounterTypes = encounterService.getEncounterTypes();
		
		//make sure we get a list
		assertNotNull(encounterTypes);
		
		boolean found = false;
		for(Iterator i = encounterTypes.iterator(); i.hasNext();) {
			EncounterType encounterType2 = (EncounterType)i.next();
			assertNotNull(encounterType);
			//check .equals function
			assertTrue(encounterType.equals(encounterType2) == (encounterType.getEncounterTypeId().equals(encounterType2.getEncounterTypeId())));
			//mark found flag
			if (encounterType.equals(encounterType2))
				found = true;
		}
		
		//assert that the new encounterType was returned in the list
		assertTrue(found);
		
		
		//check updation
		newEncounterType.setName("another test");
		as.updateEncounterType(newEncounterType);
		
		EncounterType newerEncounterType = encounterService.getEncounterType(newEncounterType.getEncounterTypeId());
		assertTrue(newerEncounterType.getName().equals(newEncounterType.getName()));
		
		
		//check deletion
		as.deleteEncounterType(newEncounterType);
		assertNull(encounterService.getEncounterType(newEncounterType.getEncounterTypeId()));

	}
	
	public void testLocation() throws Exception {
		
		//testing creation
		
		Location location = new Location();
		
		location.setName("testing");
		location.setDescription("desc");
		location.setAddress1("123");
		location.setAddress1("456");
		location.setCityVillage("city");
		location.setStateProvince("state");
		location.setCountry("country");
		location.setPostalCode("post");
		location.setLatitude("lat");
		location.setLongitude("lon");
		
		as.createLocation(location);
		
		Location newLocation = encounterService.getLocation(location.getLocationId());
		assertNotNull(newLocation);
		
		List<Location> locations = encounterService.getLocations();
		
		//make sure we get a list
		assertNotNull(locations);
		
		boolean found = false;
		for(Iterator i = locations.iterator(); i.hasNext();) {
			Location location2 = (Location)i.next();
			assertNotNull(location);
			//check .equals function
			assertTrue(location.equals(location2) == (location.getLocationId().equals(location2.getLocationId())));
			//mark found flag
			if (location.equals(location2))
				found = true;
		}
		
		//assert that the new location was returned in the list
		assertTrue(found);
		
		
		//check updation
		newLocation.setName("another test");
		as.updateLocation(newLocation);
		
		Location newerLocation = encounterService.getLocation(newLocation.getLocationId());
		assertTrue(newerLocation.getName().equals(newLocation.getName()));
		
		
		//check deletion
		as.deleteLocation(newLocation);
		assertNull(encounterService.getLocation(newLocation.getLocationId()));

	}
	
	public static Test suite() {
		return new TestSuite(AdministrationServiceTest.class, "Basic IbatisAdministrationService functionality");
	}

}
