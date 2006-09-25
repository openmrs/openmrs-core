package org.openmrs.api.db;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmrs.ComplexObs;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextFactory;

public class ObsServiceTest extends TestCase {
	
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
		conceptService = context.getConceptService();
		assertNotNull(conceptService);
		//orderService = context.getOrderService();
		//assertNotNull(orderService);
		//conceptService = context.getConceptService();
		//assertNotNull(conceptService);
		
	}

	public void testObsCreateUpdateDelete() throws Exception {
		
		Obs o = new Obs();
		
		//testing creation
		
		Order order1 = null;
		Concept concept1 = conceptService.getConcept(1);
		Patient patient1 = (Patient)ps.getPatientsByIdentifier("%", true).toArray()[1];
		System.out.println(ps.getPatientsByIdentifier("%", true));
		System.out.println("patient1: " + patient1.getPatientId());
		Encounter encounter1 = (Encounter)es.getEncounter(1);
		Date datetime1 = new Date();
		Location location1 = es.getLocation(3);
		Integer groupId1 = new Integer(1);
		Integer valueGroupId1 = new Integer(5);
		boolean valueBoolean1 = true;
		Date valueDatetime1 = new Date();
		Concept valueCoded1 = conceptService.getConcept(11);
		Double valueNumeric1 = 1.0;
		String valueModifier1 = "a1";
		String valueText1 = "value text1";
		String comment1 = "commenting1";
		
		o.setOrder(order1);
		o.setConcept(concept1);
		o.setPatient(patient1);
		o.setEncounter(encounter1);
		o.setObsDatetime(datetime1);
		o.setLocation(location1);
		o.setObsGroupId(groupId1);
		o.setValueGroupId(valueGroupId1);
		//o.setValueBoolean(valueBoolean1);
		o.setValueDatetime(valueDatetime1);
		o.setValueCoded(valueCoded1);
		o.setValueNumeric(valueNumeric1);
		o.setValueModifier(valueModifier1);
		o.setValueText(valueText1);
		o.setComment(comment1);
		
		obsService.createObs(o);
		
		Obs o2 = obsService.getObs(o.getObsId());
		assertNotNull(o2);
		
		Order order2 = null;
		Concept concept2 = conceptService.getConcept(2);
		Patient patient2 = (Patient)ps.getPatientsByIdentifier("%", true).toArray()[4];
		System.out.println("patient2: " + patient2.getPatientId());
		Encounter encounter2 = (Encounter)es.getEncounter(2);
		Date datetime2 = new Date();
		Location location2 = es.getLocation(2);
		Integer groupId2 = new Integer(2);
		Integer valueGroupId2 = new Integer(3);
		boolean valueBoolean2 = false;
		Date valueDatetime2 = new Date();
		Concept valueCoded2 = conceptService.getConcept(22);
		Double valueNumeric2 = 2.0;
		String valueModifier2 = "cc";
		String valueText2 = "value text2";
		String comment2 = "commenting2";
		
		o2.setOrder(order2);
		o2.setConcept(concept2);
		o2.setPatient(patient2);
		o2.setEncounter(encounter2);
		o2.setObsDatetime(datetime2);
		o2.setLocation(location2);
		o2.setObsGroupId(groupId2);
		o2.setValueGroupId(valueGroupId2);
		//o2.setValueBoolean(valueBoolean2);
		o2.setValueDatetime(valueDatetime2);
		o2.setValueCoded(valueCoded2);
		o2.setValueNumeric(valueNumeric2);
		o2.setValueModifier(valueModifier2);
		o2.setValueText(valueText2);
		o2.setComment(comment2);
		
		obsService.updateObs(o2);
		
		Obs o3 = obsService.getObs(o2.getObsId());
		System.out.println("o3.isComplex(): " + o3.isComplexObs());
		
		//o2 should equal o3 and neither should equal o1
		
		assertTrue(o3.equals(o));
		if (order2 != null)
			assertTrue(o3.getOrder().equals(order2));
		assertTrue(o3.getPatient().equals(patient2));
		assertTrue(o3.getComment().equals(comment2));
		assertTrue(o3.getConcept().equals(concept2));
		assertTrue(o3.getEncounter().equals(encounter2));
		assertTrue(o3.getObsDatetime().equals(datetime2));
		assertTrue(o3.getLocation().equals(location2));
		assertTrue(o3.getObsGroupId().equals(groupId2));
		assertTrue(o3.getValueGroupId().equals(valueGroupId2));
		//assertTrue(o3.getValueBoolean().equals(valueBoolean2));
		assertTrue(o3.getValueDatetime().equals(valueDatetime2));
		assertTrue(o3.getValueCoded().equals(valueCoded2));
		assertTrue(o3.getValueNumeric().equals(valueNumeric2));
		assertTrue(o3.getValueModifier().equals(valueModifier2));
		assertTrue(o3.getValueText().equals(valueText2));
		
		System.out.println(o3.getPatient().getCreator().getUsername());
		obsService.voidObs(o, "testing void function");
		
		Obs o4 = obsService.getObs(o.getObsId());
		
		assertTrue(o4.getVoidReason().equals("testing void function"));
		assertTrue(o4.getVoidedBy().equals(o3.getVoidedBy()));
		assertTrue(o4.isVoided());
		
		obsService.deleteObs(o);
		//TODO what to do on multiple delete?
		//obsService.deleteObs(o3); //gratuitous
		
		assertNull(obsService.getObs(o.getObsId()));
		
	}	
	
	public void testComplexObsCreateUpdateDelete() throws Exception {
		
		ComplexObs o = new ComplexObs();
		
		//testing creation
		
		Order order1 = null;
		Concept concept1 = conceptService.getConcept(1);
		Patient patient1 = (Patient)ps.getPatientsByIdentifier("%", true).toArray()[1];
		Encounter encounter1 = (Encounter)es.getEncounter(1);
		Date datetime1 = new Date();
		Location location1 = es.getLocation(3);
		Integer groupId1 = new Integer(1);
		Integer valueGroupId1 = new Integer(5);
		boolean valueBoolean1 = true;
		Date valueDatetime1 = new Date();
		Concept valueCoded1 = conceptService.getConcept(13);
		Double valueNumeric1 = 1.0;
		String valueModifier1 = "a1";
		String valueText1 = "value text1";
		String comment1 = "commenting1";
		MimeType mimetype1 = obsService.getMimeTypes().get(0);
		String urn1 = "urn1";
		String complexValue1 = "complex value1";
		
		o.setOrder(order1);
		o.setConcept(concept1);
		o.setPatient(patient1);
		o.setEncounter(encounter1);
		o.setObsDatetime(datetime1);
		o.setLocation(location1);
		o.setObsGroupId(groupId1);
		o.setValueGroupId(valueGroupId1);
		//o.setValueBoolean(valueBoolean1);
		o.setValueDatetime(valueDatetime1);
		o.setValueCoded(valueCoded1);
		o.setValueNumeric(valueNumeric1);
		o.setValueModifier(valueModifier1);
		o.setValueText(valueText1);
		o.setComment(comment1);
		o.setMimeType(mimetype1);
		o.setUrn(urn1);
		o.setComplexValue(complexValue1);
		
		obsService.createObs(o);
		
		ComplexObs o2 = (ComplexObs)obsService.getObs(o.getObsId());
		assertNotNull(o2);
		
		Order order2 = null;
		Concept concept2 = conceptService.getConcept(2);
		Patient patient2 = (Patient)ps.getPatientsByIdentifier("%", true).toArray()[4];
		Encounter encounter2 = (Encounter)es.getEncounter(2);
		Date datetime2 = new Date();
		Location location2 = es.getLocation(2);
		Integer groupId2 = new Integer(2);
		Integer valueGroupId2 = new Integer(3);
		boolean valueBoolean2 = false;
		Date valueDatetime2 = new Date();
		Concept valueCoded2 = conceptService.getConcept(25);
		Double valueNumeric2 = 2.0;
		String valueModifier2 = "cc";
		String valueText2 = "value text2";
		String comment2 = "commenting2";
		MimeType mimetype2 = obsService.getMimeTypes().get(1);
		String urn2 = "urn2";
		String complexValue2 = "complex value2";
		
		o2.setOrder(order2);
		o2.setConcept(concept2);
		o2.setPatient(patient2);
		o2.setEncounter(encounter2);
		o2.setObsDatetime(datetime2);
		o2.setLocation(location2);
		o2.setObsGroupId(groupId2);
		o2.setValueGroupId(valueGroupId2);
		//o2.setValueBoolean(valueBoolean2);
		o2.setValueDatetime(valueDatetime2);
		o2.setValueCoded(valueCoded2);
		o2.setValueNumeric(valueNumeric2);
		o2.setValueModifier(valueModifier2);
		o2.setValueText(valueText2);
		o2.setComment(comment2);
		o2.setMimeType(mimetype2);
		o2.setUrn(urn2);
		o2.setComplexValue(complexValue2);
		
		obsService.updateObs(o2);
		
		ComplexObs o3 = (ComplexObs)obsService.getObs(o2.getObsId());
		Obs o7 = obsService.getObs(o2.getObsId());
		System.out.println("o7.isComplex(): " + o7.isComplexObs());
		//o2=03=o but 
		//(values of o2 = values of o3) != values of o
		
		assertTrue(o3.equals(o));
		if (order2 != null)
			assertTrue(o3.getOrder().equals(order2));
		assertTrue(o3.getPatient().equals(patient2));
		assertTrue(o3.getComment().equals(comment2));
		assertTrue(o3.getConcept().equals(concept2));
		assertTrue(o3.getEncounter().equals(encounter2));
		assertTrue(o3.getObsDatetime().equals(datetime2));
		assertTrue(o3.getLocation().equals(location2));
		assertTrue(o3.getObsGroupId().equals(groupId2));
		assertTrue(o3.getValueGroupId().equals(valueGroupId2));
		//assertTrue(o3.getValueBoolean().equals(valueBoolean2));
		assertTrue(o3.getValueDatetime().equals(valueDatetime2));
		assertTrue(o3.getValueCoded().equals(valueCoded2));
		assertTrue(o3.getValueNumeric().equals(valueNumeric2));
		assertTrue(o3.getValueModifier().equals(valueModifier2));
		assertTrue(o3.getValueText().equals(valueText2));
		assertTrue(o3.getMimeType().equals(mimetype2));
		assertTrue(o3.getUrn().equals(urn2));
		assertTrue(o3.getComplexValue().equals(complexValue2));
		
		System.out.println(o3.getPatient().getCreator().getUsername());
		obsService.voidObs(o, "testing void function");
		
		Obs o4 = obsService.getObs(o.getObsId());
		
		assertTrue(o4.getVoidReason().equals("testing void function"));
		assertTrue(o4.getVoidedBy().equals(o3.getVoidedBy()));
		assertTrue(o4.isVoided());
		
		obsService.deleteObs(o);
		//TODO what to do on multiple delete?
		//obsService.deleteObs(o3); //gratuitous
		
		assertNull(obsService.getObs(o.getObsId()));
	}	
	
	public static Test suite() {
		return new TestSuite(ObsServiceTest.class, "Basic ObsService functionality");
	}

}
