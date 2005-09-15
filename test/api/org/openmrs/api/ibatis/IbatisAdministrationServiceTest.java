package org.openmrs.api.ibatis;

import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmrs.EncounterType;
import org.openmrs.MimeType;
import org.openmrs.OrderType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;
import org.openmrs.context.Context;
import org.openmrs.context.ContextFactory;

public class IbatisAdministrationServiceTest extends TestCase {
	
	protected EncounterService es;
	protected PatientService ps;
	protected UserService us;
	protected AdministrationService as;
	protected ObsService obsService;
	protected OrderService orderService;
	
	public void setUp() throws Exception{
		Context context = ContextFactory.getContext();
		
		context.authenticate("admin", "test");
		
		es = context.getEncounterService();
		assertNotNull(es);
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
		
	}

	public void xtestEncounterType() throws Exception {
		
		//testing creation
		
		EncounterType et = new EncounterType();
		
		et.setName("testing");
		et.setDescription("desc");
		
		as.createEncounterType(et);
		
		EncounterType newEt = es.getEncounterType(et.getEncounterTypeId());
		
		assertNotNull(newEt);
		
		as.deleteEncounterType(newEt);

	}

	public void testMimeType() throws Exception {
		
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
			assertTrue(mimeType.equals(mimeType2) == (mimeType.getMimeType().equals(mimeType2.getMimeType())));
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
		as.deleteMimeType(newMimeType);

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
			assertTrue(orderType.equals(orderType2) == (orderType.getName().equals(orderType2.getName())));
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

	}
	
	
	public static Test suite() {
		return new TestSuite(IbatisAdministrationServiceTest.class, "Basic IbatisAdministrationService functionality");
	}

}
