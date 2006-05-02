package org.openmrs.api.db;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextFactory;

public class OrderServiceTest extends TestCase {
	
	protected EncounterService es;
	protected PatientService ps;
	protected UserService us;
	protected ObsService obsService;
	protected OrderService orderService;
	protected ConceptService conceptService;
	
	public void setUp() throws Exception{
		Context context = ContextFactory.getContext();
		
		context.authenticate("USER-1", "test");
		
		es = context.getEncounterService();
		assertNotNull(es);
		ps = context.getPatientService();
		assertNotNull(ps);
		us = context.getUserService();
		assertNotNull(us);
		obsService = context.getObsService();
		assertNotNull(obsService);
		orderService = context.getOrderService();
		assertNotNull(orderService);
		conceptService = context.getConceptService();
		assertNotNull(conceptService);
		
	}

	public void testOrderCreateUpdateDelete() throws Exception {
		
		Order order1 = new Order();
		
		//testing creation
		
		OrderType ordertypes1 = orderService.getOrderTypes().get(1);
		Concept concept1 = conceptService.getConcept(1);
		User orderer1 = us.getUser(1);
		Encounter encounter1 = (Encounter)es.getEncounter(1);
		String instructions1 = "instruct1";
		Date start1 = new Date();
		Date autoexpire1 = new Date();
		
		order1.setOrderType(ordertypes1);
		order1.setConcept(concept1);
		order1.setOrderer(orderer1);
		order1.setEncounter(encounter1);
		order1.setInstructions(instructions1);
		order1.setStartDate(start1);
		order1.setAutoExpireDate(autoexpire1);
		
		orderService.createOrder(order1);

		
		//testing updation
		
		
		Order order2 = orderService.getOrder(order1.getOrderId());
		assertNotNull(order2);
		
		OrderType ordertypes2 = orderService.getOrderTypes().get(2);
		Concept concept2 = conceptService.getConcept(2);
		User orderer2 = us.getUser(2);
		Encounter encounter2 = (Encounter)es.getEncounter(2);
		String instructions2 = "instruct2";
		Date start2 = new Date();
		Date autoexpire2 = new Date();
		
		order2.setOrderType(ordertypes2);
		order2.setConcept(concept2);
		order2.setOrderer(orderer2);
		order2.setEncounter(encounter2);
		order2.setInstructions(instructions2);
		order2.setStartDate(start2);
		order2.setAutoExpireDate(autoexpire2);
		
		orderService.updateOrder(order2);
		
		Order order3 = orderService.getOrder(order2.getOrderId());
		
		//order2 should equal order3 and neither should equal order1
		
		assertTrue(order3.equals(order1));
		assertTrue(order3.getConcept().equals(concept2));
		assertTrue(order3.getOrderer().equals(orderer2));
		assertTrue(order3.getOrderType().equals(ordertypes2));
		assertTrue(order3.getEncounter().equals(encounter2));
		assertTrue(order3.getInstructions().equals(instructions2));
		assertTrue(order3.getStartDate().equals(start2));
		assertTrue(order3.getAutoExpireDate().equals(autoexpire2));
		
		orderService.voidOrder(order1, "testing void function");
		
		Order order4 = orderService.getOrder(order1.getOrderId());
		
		assertTrue(order4.getVoidReason().equals("testing void function"));
		assertTrue(order4.getVoidedBy().equals(order3.getVoidedBy()));
		assertTrue(order4.isVoided());
		
		orderService.discontinueOrder(order4, "discontinue instruct");
		
		Order order5 = orderService.getOrder(order4.getOrderId());
		assertTrue(order5.getDiscontinuedReason().equals("discontinue instruct"));
		//System.out.println("order5.getDiscontinuedBy: " + order5.getDiscontinuedBy().getUsername());
		assertTrue(order5.getDiscontinuedBy().equals(ContextFactory.getContext().getAuthenticatedUser()));
		assertTrue(order5.isDiscontinued());
		
		orderService.undiscontinueOrder(order5);
		orderService.unvoidOrder(order5);
		
		//testing deletion
		
		orderService.deleteOrder(order1);
		//orderService.deleteOrder(order3); //gratuitous
		
		assertNull(orderService.getOrder(order1.getOrderId()));
		
	}	
	
	public void testDrugOrderCreateUpdateDelete() throws Exception {
		
		DrugOrder drugorder1 = new DrugOrder();
		
		//testing creation
		
		OrderType ordertypes1 = orderService.getOrderTypes().get(1);
		Concept concept1 = conceptService.getConcept(1);
		User orderer1 = us.getUser(1);
		Encounter encounter1 = (Encounter)es.getEncounter(1);
		String instructions1 = "instruct1";
		Date start1 = new Date();
		Date autoexpire1 = new Date();
		Drug drugInventoryId1 = conceptService.getDrugs().get(1);
		Integer dose1 = new Integer(1);
		String units1 = "units1";
		String freq1 = "freq1";
		Boolean prn1 = false;
		Boolean complex1 = false;
		Integer quantity1 = new Integer(1);
		
		drugorder1.setOrderType(ordertypes1);
		drugorder1.setConcept(concept1);
		drugorder1.setOrderer(orderer1);
		drugorder1.setEncounter(encounter1);
		drugorder1.setInstructions(instructions1);
		drugorder1.setStartDate(start1);
		drugorder1.setAutoExpireDate(autoexpire1);
		drugorder1.setDrug(drugInventoryId1);
		drugorder1.setDose(dose1);
		drugorder1.setUnits(units1);
		drugorder1.setFrequency(freq1);
		drugorder1.setPrn(prn1);
		drugorder1.setComplex(complex1);
		drugorder1.setQuantity(quantity1);
		
		orderService.createOrder(drugorder1);

		DrugOrder drugorder2 = (DrugOrder)orderService.getOrder(drugorder1.getOrderId());
		assertNotNull(drugorder2);
		
		OrderType ordertypes2 = orderService.getOrderTypes().get(2);
		Concept concept2 = conceptService.getConcept(2);
		User orderer2 = us.getUser(2);
		Encounter encounter2 = (Encounter)es.getEncounter(2);
		String instructions2 = "instruct2";
		Date start2 = new Date();
		Date autoexpire2 = new Date();
		Drug drugInventoryId2 = conceptService.getDrugs().get(2);
		Integer dose2 = new Integer(2);
		String units2 = "units2";
		String freq2 = "freq2";
		Boolean prn2 = true;
		Boolean complex2 = true;
		Integer quantity2 = new Integer(2);
		
		drugorder2.setOrderType(ordertypes2);
		drugorder2.setConcept(concept2);
		drugorder2.setOrderer(orderer2);
		drugorder2.setEncounter(encounter2);
		drugorder2.setInstructions(instructions2);
		drugorder2.setStartDate(start2);
		drugorder2.setAutoExpireDate(autoexpire2);
		drugorder2.setDrug(drugInventoryId2);
		drugorder2.setDose(dose2);
		drugorder2.setUnits(units2);
		drugorder2.setFrequency(freq2);
		drugorder2.setPrn(prn2);
		drugorder2.setComplex(complex2);
		drugorder2.setQuantity(quantity2);
		
		orderService.updateOrder(drugorder2);
		
		DrugOrder drugorder3 = (DrugOrder)orderService.getOrder(drugorder2.getOrderId());
		
		//drugorder2 should equal drugorder3 and neither should equal drugorder1
		
		assertTrue(drugorder3.equals(drugorder1));
		assertTrue(drugorder3.getConcept().equals(concept2));
		assertTrue(drugorder3.getOrderType().equals(ordertypes2));
		assertTrue(drugorder3.getOrderer().equals(orderer2));
		assertTrue(drugorder3.getEncounter().equals(encounter2));
		assertTrue(drugorder3.getInstructions().equals(instructions2));
		assertTrue(drugorder3.getStartDate().equals(start2));
		assertTrue(drugorder3.getAutoExpireDate().equals(autoexpire2));
		assertTrue(drugorder3.getDrug().equals(drugInventoryId2));
		assertTrue(drugorder3.getDose().equals(dose2));
		assertTrue(drugorder3.getUnits().equals(units2));
		assertTrue(drugorder3.getFrequency().equals(freq2));
		assertTrue(drugorder3.isPrn().equals(prn2));
		assertTrue(drugorder3.isComplex());
		assertTrue(drugorder3.getQuantity().equals(quantity2));
		
		
		orderService.voidOrder(drugorder1, "testing void function");
		
		DrugOrder drugorder4 = (DrugOrder)orderService.getOrder(drugorder1.getOrderId());
		
		assertTrue(drugorder4.getVoidReason().equals("testing void function"));
		assertTrue(drugorder4.getVoidedBy().equals(drugorder3.getVoidedBy()));
		assertTrue(drugorder4.isVoided());
		
		orderService.discontinueOrder(drugorder4, "discontinue instruct");
		
		DrugOrder drugorder5 = (DrugOrder)orderService.getOrder(drugorder4.getOrderId());
		assertTrue(drugorder5.getDiscontinuedReason().equals("discontinue instruct"));
		//System.out.println("drugorder5.getDiscontinuedBy: " + drugorder5.getDiscontinuedBy().getUsername());
		assertTrue(drugorder5.getDiscontinuedBy().equals(drugorder1.getDiscontinuedBy()));
		assertTrue(drugorder5.isDiscontinued());
		
		orderService.undiscontinueOrder(drugorder5);
		orderService.unvoidOrder(drugorder5);
		
		orderService.deleteOrder(drugorder1);
		//orderService.deleteOrder(drugorder3); //gratuitous
		
		//orderService.voidOrder(drugorder3, "reason");
		 
		assertNull(orderService.getOrder(drugorder1.getOrderId()));
		
		
	}	
	
	public static Test suite() {
		return new TestSuite(OrderServiceTest.class, "Basic Order Service functionality");
	}

}
