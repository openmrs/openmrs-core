package org.openmrs.api.db;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextFactory;

public class EncounterServiceTest extends TestCase {
	
	protected EncounterService es;
	protected PatientService ps;
	protected UserService us;
	protected Encounter enc;
	
	public void setUp() throws Exception{
		Context context = ContextFactory.getContext();
		
		//TODO are we throwing errors for bad authentication?
		context.authenticate("USER-1", "test");
		
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
		
		Location loc1 = es.getLocations().get(0);
		assertNotNull(loc1);
		EncounterType encType1 = es.getEncounterTypes().get(0);
		assertNotNull(encType1);
		Date d1 = new Date();
		Patient pat1 = ps.getPatient(1);
		assertNotNull(pat1);
		User pro1 = ContextFactory.getContext().getAuthenticatedUser();
		
		enc.setLocation(loc1);
		enc.setEncounterType(encType1);
		enc.setEncounterDatetime(d1);
		enc.setPatient(pat1);
		enc.setProvider(pro1);

		es.createEncounter(enc);
		
		Encounter newEnc = es.getEncounter(enc.getEncounterId());
		assertNotNull(newEnc);
		assertTrue(enc.equals(newEnc));
		
		
		//testing updation
		
		Location loc2 = es.getLocations().get(1);
		assertNotNull(loc2);
		EncounterType encType2 = es.getEncounterTypes().get(1);
		assertNotNull(encType2);
		Date d2 = new Date();
		Patient pat2 = ps.getPatient(2);
		assertNotNull(pat2);
		
		enc.setLocation(loc2);
		enc.setEncounterType(encType2);
		enc.setEncounterDatetime(d2);
		enc.setPatient(pat2);

		//es.updateEncounter(newEnc);
		
		Encounter newestEnc = es.getEncounter(newEnc.getEncounterId());

		assertFalse(loc1.equals(loc2));
		assertTrue(newestEnc.getLocation().equals(loc2));
		assertFalse(encType1.equals(encType2));
		assertTrue(newestEnc.getEncounterType().equals(encType2));
		assertFalse(d1.equals(d2));
		assertTrue(newestEnc.getEncounterDatetime().equals(d2));
		assertFalse(pat1.equals(pat2));
		assertTrue(newestEnc.getPatient().equals(pat2));		
		
		//testing deletion
		
		es.deleteEncounter(newEnc);
		
		Encounter e = es.getEncounter(newEnc.getEncounterId());
		
		assertNull(e);
		
	}	
	
	public static Test suite() {
		return new TestSuite(EncounterServiceTest.class, "Basic EncounterService functionality");
	}

}
