package org.openmrs.api;

import java.util.Date;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;
import org.openmrs.context.Context;
import org.openmrs.context.ContextFactory;

public class EncounterServiceTest extends TestCase {
	
	protected EncounterService es;
	protected PatientService ps;
	protected UserService us;
	protected Encounter enc;
	
	public void setUp() throws Exception{
		Context context = ContextFactory.getContext();
		
		context.authenticate("3-4", "test");
		
		es = context.getEncounterService();
		assertNotNull(es);
		ps = context.getPatientService();
		assertNotNull(ps);
		us = context.getUserService();
		assertNotNull(us);
		
		enc = new Encounter();
	}

	public void testEncounterCreateUpdateDelete() throws Exception {
		
		//testing creation
		
		Location loc = new Location();
		
		loc.setAddress1("123 here");
		loc.setAddress2("123 there");
		loc.setCityVillage("city");
		loc.setCountry("country");
		loc.setDescription("desc");
		loc.setLatitude("lat.03");
		loc.setLongitude("lon.03");
		loc.setName("first loc");
		loc.setPostalCode("postal");
	
		enc.setLocation(loc);
		
		List<EncounterType> encTypes = es.getEncounterTypes();
		
		enc.setEncounterType(encTypes.get(0));
		Date d = new Date();
		enc.setEncounterDatetime(d);
		
		enc.setPatient(ps.getPatient(1));
		
		enc.setProvider(us.getUserByUsername("bwolfe"));
				
		Encounter newEnc = es.createEncounter(enc);
		
		assertNotNull(newEnc);
		assertTrue(enc.equals(newEnc));
		
		
		//testing updation
		
		newEnc.setEncounterType(encTypes.get(1));
		es.updateEncounter(newEnc);
		
		newEnc = es.getEncounter(newEnc.getEncounterId());
		
		//assertTrue(newEnc.getEncounterType() != enc.getEncounterType());		
		
		//testing deletion
		
		es.deleteEncounter(newEnc);
		
		Encounter e = es.getEncounter(newEnc.getEncounterId());
		
		assertNull(e);
		
	}	
	
	public static Test suite() {
		return new TestSuite(EncounterServiceTest.class, "Basic EncounterService functionality");
	}

}
