package org.openmrs.api;

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
import org.openmrs.context.Context;
import org.openmrs.context.ContextFactory;

public class ObsServiceTest extends TestCase {
	
	protected EncounterService es;
	protected PatientService ps;
	protected UserService us;
	protected ObsService obsService;
	protected OrderService orderService;
	protected ConceptService conceptService;
	
	public void setUp() throws Exception{
		Context context = ContextFactory.getContext();
		
		context.authenticate("admin", "test");
		
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
		Patient patient1 = (Patient)ps.getPatientByIdentifier("%").get(1);
		System.out.println(ps.getPatientByIdentifier("%"));
		System.out.println("patient1: " + patient1.getPatientId());
		Encounter encounter1 = (Encounter)es.getEncounter(1);
		Date datetime1 = new Date();
		Location location1 = es.getLocation(3);
		Integer groupId1 = new Integer(1);
		Integer valueGroupId1 = new Integer(5);
		boolean valueBoolean1 = true;
		Date valueDatetime1 = new Date();
		Concept valueCoded1 = null;
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
		o.setValueBoolean(valueBoolean1);
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
		Patient patient2 = (Patient)ps.getPatientByIdentifier("%").get(4);
		System.out.println("patient2: " + patient2.getPatientId());
		Encounter encounter2 = (Encounter)es.getEncounter(2);
		Date datetime2 = new Date();
		Location location2 = es.getLocation(2);
		Integer groupId2 = new Integer(2);
		Integer valueGroupId2 = new Integer(3);
		boolean valueBoolean2 = false;
		Date valueDatetime2 = new Date();
		Concept valueCoded2 = null;
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
		o2.setValueBoolean(valueBoolean2);
		o2.setValueDatetime(valueDatetime2);
		o2.setValueCoded(valueCoded2);
		o2.setValueNumeric(valueNumeric2);
		o2.setValueModifier(valueModifier2);
		o2.setValueText(valueText2);
		o2.setComment(comment2);
		
		obsService.updateObs(o2);
		
		Obs o3 = obsService.getObs(o2.getObsId());
		
		//o2 should equal o3 and neither should equal o1
		
		assertTrue(o3.equals(o));
		if (o3.getOrder() != null && o.getOrder() != null)
			assertFalse(o3.getOrder().equals(o.getOrder()));
		assertFalse(o3.getComment().equals(o.getComment()));
		System.out.println("o3.getPatient: " + o3.getPatient().getPatientId() +
						   " o.getPatient: " + o.getPatient().getPatientId());
		assertFalse(o3.getPatient().equals(o.getPatient()));
		if (o3.getConcept() != null && o.getConcept() != null)
			assertFalse(o3.getConcept().equals(o.getConcept()));
		assertFalse(o3.getEncounter().equals(o.getEncounter()));
		assertFalse(o3.getObsDatetime().equals(o.getObsDatetime()));
		assertFalse(o3.getLocation().equals(o.getLocation()));
		assertFalse(o3.getObsGroupId().equals(o.getObsGroupId()));
		assertFalse(o3.getValueGroupId().equals(o.getValueGroupId()));
		assertFalse(o3.getValueBoolean().equals(o.getValueBoolean()));
		assertFalse(o3.getValueDatetime().equals(o.getValueDatetime()));
		if (o3.getValueCoded() != null && o.getValueCoded() != null)
			assertFalse(o3.getValueCoded().equals(o.getValueCoded()));
		assertFalse(o3.getValueNumeric().equals(o.getValueNumeric()));
		assertFalse(o3.getValueModifier().equals(o.getValueModifier()));
		assertFalse(o3.getValueText().equals(o.getValueText()));
		
		
		obsService.voidObs(o, "testing void function");
		
		Obs o4 = obsService.getObs(o.getObsId());
		
		assertFalse(o4.getVoidReason().equals(o3.getVoidReason()));
		assertFalse(o4.getVoidedBy().equals(o3.getVoidedBy()));
		assertTrue(o4.isVoided());
		
		obsService.deleteObs(o);
		obsService.deleteObs(o3); //gratuitous
		
		assertNull(obsService.getObs(o.getObsId()));
		
	}	
	
	public void testComplexObsCreateUpdateDelete() throws Exception {
		
		ComplexObs o = new ComplexObs();
		
		//testing creation
		
		Order order1 = null;
		Concept concept1 = conceptService.getConcept(1);
		Patient patient1 = (Patient)ps.getPatientByIdentifier("%").get(1);
		Encounter encounter1 = (Encounter)es.getEncounter(1);
		Date datetime1 = new Date();
		Location location1 = es.getLocation(3);
		Integer groupId1 = new Integer(1);
		Integer valueGroupId1 = new Integer(5);
		boolean valueBoolean1 = true;
		Date valueDatetime1 = new Date();
		Concept valueCoded1 = null;
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
		o.setValueBoolean(valueBoolean1);
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
		Patient patient2 = (Patient)ps.getPatientByIdentifier("%").get(4);
		Encounter encounter2 = (Encounter)es.getEncounter(2);
		Date datetime2 = new Date();
		Location location2 = es.getLocation(2);
		Integer groupId2 = new Integer(2);
		Integer valueGroupId2 = new Integer(3);
		boolean valueBoolean2 = false;
		Date valueDatetime2 = new Date();
		Concept valueCoded2 = null;
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
		o2.setValueBoolean(valueBoolean2);
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
		
		//o2=03=o but 
		//(values of o2 = values of o3) != values of o
		
		assertTrue(o3.equals(o));
		if (o3.getOrder() != null && o.getOrder() != null)
			assertFalse(o3.getOrder().equals(o.getOrder()));
		if (o3.getConcept() != null && o.getConcept() != null)
			assertFalse(o3.getConcept().equals(o.getConcept()));
		assertFalse(o3.getPatient().equals(o.getPatient()));
		assertFalse(o3.getEncounter().equals(o.getEncounter()));
		assertFalse(o3.getObsDatetime().equals(o.getObsDatetime()));
		assertFalse(o3.getLocation().equals(o.getLocation()));
		assertFalse(o3.getObsGroupId().equals(o.getObsGroupId()));
		assertFalse(o3.getValueGroupId().equals(o.getValueGroupId()));
		assertFalse(o3.getValueBoolean().equals(o.getValueBoolean()));
		assertFalse(o3.getValueDatetime().equals(o.getValueDatetime()));
		if (o3.getValueCoded() != null && o.getValueCoded() != null)
			assertFalse(o3.getValueCoded().equals(o.getValueCoded()));
		assertFalse(o3.getValueNumeric().equals(o.getValueNumeric()));
		assertFalse(o3.getValueModifier().equals(o.getValueModifier()));
		assertFalse(o3.getValueText().equals(o.getValueText()));
		assertFalse(o3.getComment().equals(o.getComment()));
		assertFalse(o3.getMimeType().equals(o.getMimeType()));
		assertFalse(o3.getUrn().equals(o.getUrn()));
		assertFalse(o3.getComplexValue().equals(o.getComplexValue()));
		
		obsService.voidObs(o, "testing void function");
		
		ComplexObs o4 = (ComplexObs)obsService.getObs(o.getObsId());
		
		assertFalse(o4.getVoidReason().equals(o3.getVoidReason()));
		assertFalse(o4.getVoidedBy().equals(o3.getVoidedBy()));
		assertTrue(o4.isVoided());
		
		obsService.deleteObs(o);
		obsService.deleteObs(o3); //gratuitous
		
		assertNull(obsService.getObs(o.getObsId()));
		
	}	
	
	public static Test suite() {
		return new TestSuite(ObsServiceTest.class, "Basic ObsService functionality");
	}

}
