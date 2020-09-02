/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.CharArrayReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptProposal;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.ObsServiceImpl;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.obs.handler.BinaryDataHandler;
import org.openmrs.obs.handler.ImageHandler;
import org.openmrs.obs.handler.TextHandler;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.DateUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsConstants.PERSON_TYPE;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO clean up and add tests for all methods in ObsService
 */
public class ObsServiceTest extends BaseContextSensitiveTest {
	
	protected static final String INITIAL_OBS_XML = "org/openmrs/api/include/ObsServiceTest-initial.xml";
	
	protected static final String ENCOUNTER_OBS_XML = "org/openmrs/api/include/ObsServiceTest-EncounterOverwrite.xml";
	
	protected static final String COMPLEX_OBS_XML = "org/openmrs/api/include/ObsServiceTest-complex.xml";

	protected static final String REVISION_OBS_XML = "org/openmrs/api/include/ObsServiceTest-RevisionObs.xml";
	
	@Autowired
	private ObsService obsService;

	
	/**
	 * This method gets the revision obs for voided obs
	 *
	 * @see ObsService#getRevisionObs(Obs)
	 */
	@Test
	public void shouldGetRevisedObs() {
		executeDataSet(INITIAL_OBS_XML);
		executeDataSet(REVISION_OBS_XML);

		ObsService os = Context.getObsService();
		Obs initialObs = os.getObsByUuid("uuid14");
		Obs revisedObs = os.getRevisionObs(initialObs);
		assertEquals(17, revisedObs.getId().intValue());
		assertEquals(2, revisedObs.getGroupMembers(true).size());
	}

	@Test
	public void shouldReturnAPIExceptionWhenObsIsNull(){
		ObsService os = Context.getObsService();
		APIException exception = assertThrows(APIException.class, () -> os.saveObs(null,"Null Obs"));
		assertThat(exception.getMessage(), is(Context.getMessageSourceService().getMessage("Obs.error.cannot.be.null")));
	}
	
	/**
	 * This test tests multi-level heirarchy obsGroup cascades for create, delete, update, void, and
	 * unvoid
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldSaveUpdateDeleteVoidObsGroupCascades() {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService os = Context.getObsService();
		ConceptService cs = Context.getConceptService();
		
		//create an obs
		Obs o = new Obs();
		o.setConcept(cs.getConcept(3));
		o.setDateCreated(new Date());
		o.setCreator(Context.getAuthenticatedUser());
		o.setLocation(new Location(1));
		o.setObsDatetime(new Date());
		o.setPerson(new Patient(2));
		o.setValueText("original obs value text");
		
		//create a second obs
		Obs o2 = new Obs();
		o2.setConcept(cs.getConcept(3));
		o2.setDateCreated(new Date());
		o2.setCreator(Context.getAuthenticatedUser());
		o2.setLocation(new Location(1));
		o2.setObsDatetime(new Date());
		o2.setValueText("second obs value text");
		o2.setPerson(new Patient(2));
		
		//create a parent obs
		Obs oParent = new Obs();
		oParent.setConcept(cs.getConcept(23)); //in the concept set table as a set
		oParent.setDateCreated(new Date());
		oParent.setCreator(Context.getAuthenticatedUser());
		oParent.setLocation(new Location(1));
		oParent.setObsDatetime(new Date());
		oParent.setPerson(new Patient(2));
		
		//add o and o2 to the parent obs
		oParent.addGroupMember(o2);
		oParent.addGroupMember(o);
		
		//create a grandparent obs
		Obs oGP = new Obs();
		oGP.setConcept(cs.getConcept(3));
		oGP.setDateCreated(new Date());
		oGP.setCreator(Context.getAuthenticatedUser());
		oGP.setLocation(new Location(1));
		oGP.setObsDatetime(new Date());
		oGP.setPerson(new Patient(2));
		//oGP.setValueText("grandparent obs value text");
		
		oGP.addGroupMember(oParent);
		
		//create a leaf observation
		Obs o3 = new Obs();
		o3.setConcept(cs.getConcept(3));
		o3.setDateCreated(new Date());
		o3.setCreator(Context.getAuthenticatedUser());
		o3.setLocation(new Location(1));
		o3.setObsDatetime(new Date());
		o3.setValueText("leaf obs value text");
		o3.setPerson(new Patient(2));
		
		//and add it to the grandparent
		oGP.addGroupMember(o3);
		
		//create a great-grandparent
		Obs oGGP = new Obs();
		oGGP.setConcept(cs.getConcept(3));
		oGGP.setDateCreated(new Date());
		oGGP.setCreator(Context.getAuthenticatedUser());
		oGGP.setLocation(new Location(1));
		oGGP.setObsDatetime(new Date());
		//oGGP.setValueText("great grandparent value text");
		oGGP.setPerson(new Patient(2));
		
		oGGP.addGroupMember(oGP);
		
		//create a great-great grandparent
		Obs oGGGP = new Obs();
		oGGGP.setConcept(cs.getConcept(3));
		oGGGP.setDateCreated(new Date());
		oGGGP.setCreator(Context.getAuthenticatedUser());
		oGGGP.setLocation(new Location(1));
		oGGGP.setObsDatetime(new Date());
		//oGGGP.setValueText("great great grandparent value text");
		oGGGP.setPerson(new Patient(2));
		
		oGGGP.addGroupMember(oGGP);
		
		//Create the great great grandparent
		os.saveObs(oGGGP, null);
		int oGGGPId = oGGGP.getObsId();
		
		//now navigate the tree and make sure that all tree members have obs_ids
		//indicating that they've been saved (unsaved_value in the hibernate mapping set to null so
		// the notNull assertion is sufficient):
		Obs testGGGP = os.getObs(oGGGPId);
		assertTrue(testGGGP.isObsGrouping());
		Set<Obs> GGGPmembers = testGGGP.getGroupMembers();
		assertEquals(GGGPmembers.size(), 1);
		for (Obs testGGP : GGGPmembers) {
			assertTrue(testGGP.isObsGrouping());
			assertEquals(testGGP.getGroupMembers().size(), 1);
			assertNotNull(testGGP.getObsId());
			for (Obs testGP : testGGP.getGroupMembers()) {
				assertTrue(testGP.isObsGrouping());
				assertEquals(testGP.getGroupMembers().size(), 2);
				assertNotNull(testGP.getObsId());
				for (Obs parent : testGP.getGroupMembers()) {
					if (parent.isObsGrouping()) {
						assertEquals(parent.getGroupMembers().size(), 2);
						assertNotNull(parent.getObsId());
						for (Obs child : parent.getGroupMembers()) {
							assertNotNull(child.getObsId());
							//make an edit to a value so that we can save the great great grandfather
							//and see if the changes have been reflected:
							child.setValueText("testingUpdate");
						}
					}
					
				}
				
			}
		}
		
		Obs oGGGPThatWasUpdated = os.saveObs(oGGGP, "Updating obs group parent");
		
		//now, re-walk the tree to verify that the bottom-level leaf obs have the new text value:
		
		int childOneId = 0;
		int childTwoId = 0;
		assertTrue(oGGGPThatWasUpdated.isObsGrouping());
		Set<Obs> GGGPmembers2 = oGGGPThatWasUpdated.getGroupMembers();
		assertEquals(GGGPmembers2.size(), 1);
		for (Obs testGGP : GGGPmembers2) {
			assertTrue(testGGP.isObsGrouping());
			assertEquals(testGGP.getGroupMembers().size(), 1);
			assertNotNull(testGGP.getObsId());
			for (Obs testGP : testGGP.getGroupMembers()) {
				assertTrue(testGP.isObsGrouping());
				assertEquals(testGP.getGroupMembers().size(), 2);
				assertNotNull(testGP.getObsId());
				for (Obs parent : testGP.getGroupMembers()) {
					if (parent.isObsGrouping()) {
						assertEquals(parent.getGroupMembers().size(), 2);
						assertNotNull(parent.getObsId());
						int i = 0;
						for (Obs child : parent.getGroupMembers()) {
							assertEquals("testingUpdate", child.getValueText());
							//set childIds, so that we can test voids/unvoids/delete
							if (i == 0)
								childOneId = child.getObsId();
							else
								childTwoId = child.getObsId();
							i++;
						}
					}
					
				}
				
			}
		}
		
		//check voiding:
		//first, just create an Obs, and void it, and verify:
		Obs oVoidTest = new Obs();
		oVoidTest.setConcept(cs.getConcept(1));
		oVoidTest.setValueNumeric(50d);
		oVoidTest.setDateCreated(new Date());
		oVoidTest.setCreator(Context.getAuthenticatedUser());
		oVoidTest.setLocation(new Location(1));
		oVoidTest.setObsDatetime(new Date());
		oVoidTest.setPerson(new Patient(2));
		oVoidTest.setValueText("value text of soon-to-be-voided obs");
		
		Obs obsThatWasVoided = os.saveObs(oVoidTest, null);
		os.voidObs(obsThatWasVoided, "testing void method");
		
		assertTrue(obsThatWasVoided.getVoided());
		
		//unvoid:
		obsThatWasVoided.setVoided(false);
		assertFalse(obsThatWasVoided.getVoided());
		
		//Now test voiding cascade:
		// i.e. by voiding the grandparent, we void the n-th generation leaf obs
		os.voidObs(oGGGPThatWasUpdated, "testing void cascade");
		assertTrue(oGGGPThatWasUpdated.getVoided());
		
		Obs childLeafObs = os.getObs(childOneId);
		assertTrue(childLeafObs.getVoided());
		
		//now test the un-void:
		os.unvoidObs(oGGGPThatWasUpdated);
		assertFalse(oGGGPThatWasUpdated.getVoided());
		assertFalse(childLeafObs.getVoided());
		
		//test this again using just the os.updateObs method on the great great grandparent:
		
		os.voidObs(oGGGPThatWasUpdated, "testing void cascade");
		childLeafObs = os.getObs(childOneId);
		assertTrue(childLeafObs.getVoided());
		
		os.unvoidObs(oGGGPThatWasUpdated);
		childLeafObs = os.getObs(childOneId);
		assertFalse(childLeafObs.getVoided());
		
		//now, test the feature that unvoid doesn't happen unless child obs has the same dateVoided as
		// the Obj argument that gets passed into unvoid:
		
		os.voidObs(oGGGPThatWasUpdated, "testing void cascade");
		childLeafObs = os.getObs(childOneId);
		assertTrue(childLeafObs.getVoided());
		
		childLeafObs.setDateVoided(new Date(childLeafObs.getDateVoided().getTime() - 5000));
		//os.saveObs(childLeafObs, "saving child leaf obs");
		os.unvoidObs(oGGGPThatWasUpdated);
		
		// commenting this out because junit4 doesn't seem to care
		//commitTransaction(false);
		
		childLeafObs = os.getObs(childOneId);
		Obs childLeafObsTwo = os.getObs(childTwoId);
		
		//childLeafObs had its date voided date changed, so it should not get unvoided by the unvoid cascade
		//childLeafObsTwo should be unvoided, as the dateVoided date is still the same as the great-great
		//grandparent Obs
		
		assertFalse(childLeafObsTwo.getVoided());
		assertTrue(childLeafObs.getVoided());
		
		//finally, check the delete cascade:
		
		os.purgeObs(oGGGPThatWasUpdated);
		
		assertNull(os.getObs(oGGGPThatWasUpdated.getObsId()));
		assertNull(os.getObs(childOneId));
		assertNull(os.getObs(childTwoId));
	}
	
	/**
	 * This method gets observations and only fetches obs that are for patients
	 * 
	 * @throws ParseException
	 * @see ObsService#getObservations(List, List, List, List, List, List, List, Integer, Integer,
	 *      Date, Date, boolean)
	 */
	@Test
	public void getObservations_shouldCompareDatesUsingLteAndGte() throws ParseException {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService os = Context.getObsService();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
		// Test 1, No bounderies
		Date sd = df.parse("2006-02-01");
		Date ed = df.parse("2006-02-20");
		List<Obs> obs = os.getObservations(null, null, null, null, null, null, null, null, null, sd, ed, false);
		assertEquals(9, obs.size());
		
		// Test 2, From boundary
		sd = df.parse("2006-02-13");
		ed = df.parse("2006-02-20");
		obs = os.getObservations(null, null, null, null, null, null, null, null, null, sd, ed, false);
		assertEquals(4, obs.size());
		
		// Test 3, To boundary
		sd = df.parse("2006-02-01");
		ed = df.parse("2006-02-15");
		obs = os.getObservations(null, null, null, null, null, null, null, null, null, sd, ed, false);
		assertEquals(8, obs.size());
		
		// Test 4, Both Boundaries
		sd = df.parse("2006-02-11");
		ed = new SimpleDateFormat("yyyy-MM-dd-hh-mm").parse("2006-02-11-11-59");
		obs = os.getObservations(null, null, null, null, null, null, null, null, null, sd, ed, false);
		assertEquals(1, obs.size());
		
		// Test 5, Outside before
		sd = df.parse("2006-02-01");
		ed = df.parse("2006-02-08");
		obs = os.getObservations(null, null, null, null, null, null, null, null, null, sd, ed, false);
		assertEquals(0, obs.size());
		
		// Test 6, Outside After
		sd = df.parse("2006-02-17");
		ed = df.parse("2006-02-20");
		obs = os.getObservations(null, null, null, null, null, null, null, null, null, sd, ed, false);
		assertEquals(0, obs.size());
	}
	
	/**
	 * Uses the OpenmrsUtil.getLastMomentOfDay(Date) method to get all observations for a given day
	 * 
	 * @throws ParseException
	 * @throws Exception
	 */
	@Test
	public void shouldGetObservationsOnDay() throws ParseException {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService os = Context.getObsService();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
		Date sd = df.parse("2006-02-13");
		Date ed = df.parse("2006-02-13");
		List<Obs> obs = os.getObservations(null, null, null, null, null, null, null, null, null, sd,
		    OpenmrsUtil.getLastMomentOfDay(ed), false);
		assertEquals(1, obs.size());
	}
	
	/**
	 * @throws IOException
	 * @see ObsService#getComplexObs(Integer,String)
	 */
	@Test
	public void getComplexObs_shouldFillInComplexDataObjectForComplexObs() throws IOException {
		executeDataSet(COMPLEX_OBS_XML);
		// create gif file
		// make sure the file isn't there to begin with
		AdministrationService as = Context.getAdministrationService();
		File complexObsDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(as
		        .getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR));
		File createdFile = new File(complexObsDir, "openmrs_logo_small.gif");
		if (createdFile.exists())
			createdFile.delete();
		int width = 10;
		int height = 10;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster raster = image.getRaster();
		int[] colorArray = new int[3];
		int h = 255;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (i == 0 || j == 0 || i == width - 1 || j == height - 1 || (i > width / 3 && i < 2 * width / 3)
				        && (j > height / 3 && j < 2 * height / 3)) {
					colorArray[0] = h;
					colorArray[1] = h;
					colorArray[2] = 0;
				} else {
					colorArray[0] = 0;
					colorArray[1] = 0;
					colorArray[2] = h;
				}
				raster.setPixel(i, j, colorArray);
			}
		}
		ImageIO.write(image, "gif", createdFile);
		// end create gif file
		ObsService os = Context.getObsService();

		Obs complexObs = os.getObs(44);
		
		assertNotNull(complexObs);
		assertTrue(complexObs.isComplex());
		assertNotNull(complexObs.getValueComplex());
		assertNotNull(complexObs.getComplexData());
		assertEquals(complexObs, os.getObsByUuid(complexObs.getUuid()));
		// delete gif file
		// we always have to delete this inside the same unit test because it is
		// outside the
		// database and hence can't be "rolled back" like everything else
		createdFile.delete();
	}
	
	/**
	 * @throws IOException
	 * @see ObsService#getComplexObs(Integer,String)
	 */
	@Test
	public void getComplexObs_shouldNotFailWithNullView() throws IOException {
		executeDataSet(COMPLEX_OBS_XML);
		// create gif file
		// make sure the file isn't there to begin with
		AdministrationService as = Context.getAdministrationService();
		File complexObsDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(as
		        .getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR));
		File createdFile = new File(complexObsDir, "openmrs_logo_small.gif");
		if (createdFile.exists())
			createdFile.delete();
		int width = 10;
		int height = 10;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster raster = image.getRaster();
		int[] colorArray = new int[3];
		int h = 255;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (i == 0 || j == 0 || i == width - 1 || j == height - 1 || (i > width / 3 && i < 2 * width / 3)
				        && (j > height / 3 && j < 2 * height / 3)) {
					colorArray[0] = h;
					colorArray[1] = h;
					colorArray[2] = 0;
				} else {
					colorArray[0] = 0;
					colorArray[1] = 0;
					colorArray[2] = h;
				}
				raster.setPixel(i, j, colorArray);
			}
		}
		ImageIO.write(image, "gif", createdFile);
		// end create gif file
		ObsService os = Context.getObsService();
		
		os.getComplexObs(44, null);
		// delete gif file
		// we always have to delete this inside the same unit test because it is
		// outside the
		// database and hence can't be "rolled back" like everything else
		createdFile.delete();
	}
	
	/**
	 * @see ObsService#getComplexObs(Integer,String)
	 */
	@Test
	public void getComplexObs_shouldReturnNormalObsForNonComplexObs() {
		executeDataSet(COMPLEX_OBS_XML);
		
		ObsService os = Context.getObsService();
		
		Obs normalObs = os.getComplexObs(7, ComplexObsHandler.RAW_VIEW);
		
		assertFalse(normalObs.isComplex());
	}
	
	/**
	 * @see ObsService#getHandler(String)
	 */
	@Test
	public void getHandler_shouldHaveDefaultImageAndTextHandlersRegisteredBySpring() {
		ObsService os = Context.getObsService();
		ComplexObsHandler imgHandler = os.getHandler("ImageHandler");
		assertNotNull(imgHandler);
		
		ComplexObsHandler textHandler = os.getHandler("TextHandler");
		assertNotNull(textHandler);
	}
	
	/**
	 * @see ObsService#getHandler(String)
	 */
	@Test
	public void getHandler_shouldGetHandlerWithMatchingKey() {
		ObsService os = Context.getObsService();
		ComplexObsHandler handler = os.getHandler("ImageHandler");
		assertNotNull(handler);
		assertTrue(handler instanceof ImageHandler);
	}
	
	/**
	 * @see ObsService#getHandlers()
	 */
	@Test
	public void getHandlers_shouldNeverReturnNull() {
		assertNotNull(Context.getObsService().getHandlers());
		
		// test our current implementation without it being initialized by spring
		assertNotNull(new ObsServiceImpl().getHandlers());
	}
	
	/**
	 * @see ObsService#registerHandler(String,ComplexObsHandler)
	 */
	@Test
	public void registerHandler_shouldRegisterHandlerWithTheGivenKey() {
		ObsService os = Context.getObsService();
		
		os.registerHandler("DummyHandler", new ImageHandler());
		
		ComplexObsHandler dummyHandler = os.getHandler("DummyHandler");
		assertNotNull(dummyHandler);
	}
	
	/**
	 * @see ObsService#registerHandler(String,String)
	 */
	@Test
	public void registerHandler_shouldLoadHandlerAndRegisterKey() {
		ObsService os = Context.getObsService();
		
		// name it something other than what we used in the previous test
		os.registerHandler("DummyHandler2", "org.openmrs.obs.handler.ImageHandler");
		
		ComplexObsHandler dummyHandler = os.getHandler("DummyHandler2");
		assertNotNull(dummyHandler);
	}
	
	/**
	 * @see ObsService#removeHandler(String)
	 */
	@Test
	public void removeHandler_shouldNotFailWithInvalidKey() {
		Context.getObsService().removeHandler("SomeRandomHandler");
	}
	
	/**
	 * @see ObsService#removeHandler(String)
	 */
	@Test
	public void removeHandler_shouldRemoveHandlerWithMatchingKey() {
		ObsService os = Context.getObsService();
		
		// add the handler and make sure its there
		os.registerHandler("DummyHandler3", "org.openmrs.obs.handler.ImageHandler");
		ComplexObsHandler dummyHandler = os.getHandler("DummyHandler3");
		assertNotNull(dummyHandler);
		
		// now remove the handler and make sure its gone
		os.removeHandler("DummyHandler3");
		ComplexObsHandler dummyHandlerAgain = os.getHandler("DummyHandler3");
		assertNull(dummyHandlerAgain);
	}
	
	/**
	 * @see ObsService#saveObs(Obs,String)
	 */
	@Test
	public void saveObs_shouldCreateNewFileFromComplexDataForNewObs() {
		executeDataSet(COMPLEX_OBS_XML);
		ObsService os = Context.getObsService();
		ConceptService cs = Context.getConceptService();
		AdministrationService as = Context.getAdministrationService();
				
		// the complex data to put onto an obs that will be saved
		Reader input = new CharArrayReader("This is a string to save to a file".toCharArray());
		ComplexData complexData = new ComplexData("nameOfFile.txt", input);
		
		// must fetch the concept instead of just new Concept(8473) because the attributes on concept are checked
		// this is a concept mapped to the text handler
		Concept questionConcept = cs.getConcept(8474);
		
		Obs obsToSave = new Obs(new Person(1), questionConcept, new Date(), new Location(1));
		obsToSave.setComplexData(complexData);
		
		// make sure the file isn't there to begin with
		String filename = "nameOfFile_" + obsToSave.getUuid() + ".txt";
		File complexObsDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(as
	        .getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR));
		File createdFile = new File(complexObsDir, filename);
		if (createdFile.exists()) {
			createdFile.delete();
		}
		
		try {
			os.saveObs(obsToSave, null);
			
			// make sure the file appears now after the save
			assertTrue(createdFile.exists());
		}
		finally {
			// we always have to delete this inside the same unit test because it is outside the
			// database and hence can't be "rolled back" like everything else
			createdFile.delete();
		}
	}
	
	/**
	 * @throws IOException
	 * @see ObsService#saveObs(Obs,String)
	 */
	@Test
	public void saveObs_shouldNotOverwriteFileWhenUpdatingAComplexObs() throws IOException {
		executeDataSet(COMPLEX_OBS_XML);
		ObsService os = Context.getObsService();
		ConceptService cs = Context.getConceptService();
		AdministrationService as = Context.getAdministrationService();
		
		// Create the file that was supposedly put there by another obs
		File complexObsDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(as
		        .getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR));
		File previouslyCreatedFile = new File(complexObsDir, "nameOfFile.txt");
		
		FileUtils.writeByteArrayToFile(previouslyCreatedFile, "a string to save to a file".getBytes());
		
		// the file we'll be creating...defining it here so we can delete it in a finally block
		File newComplexFile = null;
		try {
			
			long oldFileSize = previouslyCreatedFile.length();
			
			// now add a new file to this obs and update it
			// ...then make sure the original file is still there
			
			// the complex data to put onto an obs that will be saved
			Reader input2 = new CharArrayReader("diff string to save to a file with the same name".toCharArray());
			ComplexData complexData = new ComplexData("nameOfFile.txt", input2);
			
			// must fetch the concept instead of just new Concept(8473) because the attributes on concept are checked
			// this is a concept mapped to the text handler
			Concept questionConcept = cs.getConcept(8474);
			
			Obs obsToSave = new Obs(new Person(1), questionConcept, new Date(), new Location(1));
			
			obsToSave.setComplexData(complexData);
			
			os.saveObs(obsToSave, null);
			
			// make sure the old file still appears now after the save
			assertEquals(oldFileSize, previouslyCreatedFile.length());
			
			String valueComplex = obsToSave.getValueComplex();
			String filename = valueComplex.substring(valueComplex.indexOf("|") + 1).trim();
			newComplexFile = new File(complexObsDir, filename);
			// make sure the file appears now after the save
			assertTrue(newComplexFile.length() > oldFileSize);
		}
		finally {
			// clean up the files we created
			newComplexFile.delete();
			try {
				previouslyCreatedFile.delete();
			}
			catch (Exception e) {
				// pass
			}
		}
		
	}
	
	/**
	 * @see ObsService#setHandlers(Map<QString;QComplexObsHandler;>)}
	 */
	@Test
	public void setHandlers_shouldAddNewHandlersWithNewKeys() {
		ObsService os = Context.getObsService();
		
		Map<String, ComplexObsHandler> handlers = new HashMap<>();
		handlers.put("DummyHandler4", new ImageHandler());
		handlers.put("DummyHandler5", new BinaryDataHandler());
		handlers.put("DummyHandler6", new TextHandler());
		
		// set the handlers and make sure they're there
		os.setHandlers(handlers);
		
		ComplexObsHandler dummyHandler4 = os.getHandler("DummyHandler4");
		assertNotNull(dummyHandler4);
		
		ComplexObsHandler dummyHandler5 = os.getHandler("DummyHandler5");
		assertNotNull(dummyHandler5);
		
		ComplexObsHandler dummyHandler6 = os.getHandler("DummyHandler6");
		assertNotNull(dummyHandler6);
	}
	
	/**
	 * @see ObsService#setHandlers(Map<QString;QComplexObsHandler;>)}
	 */
	@Test
	public void setHandlers_shouldOverrideHandlersWithSameKey() {
		ObsService os = Context.getObsService();
		
		Map<String, ComplexObsHandler> handlers = new HashMap<>();
		handlers.put("DummyHandlerToOverride", new ImageHandler());
		
		// set the handlers and make sure they're there
		os.setHandlers(handlers);
		
		ComplexObsHandler dummyHandlerToOverride = os.getHandler("DummyHandlerToOverride");
		assertTrue(dummyHandlerToOverride instanceof ImageHandler);
		
		// now override that key and make sure the new class is stored
		
		Map<String, ComplexObsHandler> handlersAgain = new HashMap<>();
		handlersAgain.put("DummyHandlerToOverride", new BinaryDataHandler());
		
		os.setHandlers(handlersAgain);
		
		ComplexObsHandler dummyHandlerToOverrideAgain = os.getHandler("DummyHandlerToOverride");
		assertTrue(dummyHandlerToOverrideAgain instanceof BinaryDataHandler);
		
	}
	
	/**
	 * @see ObsService#saveObs(Obs,String)
	 */
	@Test
	public void saveObs_shouldVoidTheGivenObsInTheDatabase() {
		Obs obs = Context.getObsService().getObs(7);
		obs.setValueNumeric(1.0);
		Context.getObsService().saveObs(obs, "just testing");
		
		// fetch the obs from the database again
		obs = Context.getObsService().getObs(7);
		assertTrue(obs.getVoided());
	}
	
	/**
	 * @see ObsService#getObs(Integer)
	 */
	@Test
	public void getObs_shouldGetObsMatchingGivenObsId() {
		ObsService obsService = Context.getObsService();
		
		Obs obs = obsService.getObs(7);
		
		assertEquals(5089, obs.getConcept().getId().intValue());
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean)
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldGetAllObsAssignedToGivenEncounters() {
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations(null, Collections.singletonList(new Encounter(4)), null, null, null,
		    null, null, null, null, null, null, false, null);
		
		assertEquals(6, obss.size());
	}
	
	/**
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean)
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservationCount_shouldGetCountOfObsAssignedToGivenEncounters() {
		ObsService obsService = Context.getObsService();
		
		Integer count = obsService.getObservationCount(null, Collections.singletonList(new Encounter(4)), null, null, null,
		    null, null, null, null, false, null);
		
		assertEquals(6, count.intValue());
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean)
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldGetAllObsWithAnswerConceptInGivenAnswersParameter() {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations(null, null, null, Collections.singletonList(new Concept(7)), null, null,
		    null, null, null, null, null, false, null);
		
		// obs 11 in INITIAL_OBS_XML and obs 13 in standardTestDataset
		assertEquals(3, obss.size());
		Set<Integer> ids = new HashSet<>();
		for (Obs o : obss) {
			ids.add(o.getObsId());
		}
		assertTrue(ids.contains(11));
		assertTrue(ids.contains(13));
	}
	
	/**
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean)
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservationCount_shouldGetCountOfObsWithAnswerConceptInGivenAnswersParameter() {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		Integer count = obsService.getObservationCount(null, null, null, Collections.singletonList(new Concept(7)), null,
		    null, null, null, null, false, null);
		
		assertEquals(3, count.intValue());
		
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean)
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldGetAllObsWithQuestionConceptInGivenQuestionsParameter() {
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations(null, null, Collections.singletonList(new Concept(5497)), null, null,
		    null, null, null, null, null, null, false, null);
		
		assertEquals(2, obss.size());
	}
	
	/**
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean)
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservationCount_shouldGetCountOfObsWithQuestionConceptInGivenQuestionsParameter() {
		ObsService obsService = Context.getObsService();
		
		Integer count = obsService.getObservationCount(null, null, Collections.singletonList(new Concept(5497)), null, null,
		    null, null, null, null, false, null);
		
		assertEquals(2, count.intValue());
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean)
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldIncludeVoidedObsIfIncludeVoidedObsIsTrue() {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations(Collections.singletonList(new Person(9)), null, null, null, null, null,
		    null, null, null, null, null, true, null);
		
		assertEquals(2, obss.size());
		
		assertEquals(10, obss.get(0).getObsId().intValue());
		assertEquals(9, obss.get(1).getObsId().intValue());
	}
	
	/**
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean)
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservationCount_shouldIncludeVoidedObsInTheCountIfIncludeVoidedObsIsTrue() {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		Integer obss = obsService.getObservationCount(Collections.singletonList(new Person(9)), null, null, null, null,
		    null, null, null, null, true, null);
		
		assertEquals(2, obss.intValue());
		
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean)
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldLimitNumberOfObsReturnedToMostReturnNParameter() {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		List<Obs> count = obsService.getObservations(Collections.singletonList(new Person(8)), null, null, null, null, null,
		    null, 1, null, null, null, false, null);
		
		assertEquals(1, count.size());
		
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean)
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldNotIncludeVoidedObs() {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations(Collections.singletonList(new Person(9)), null, null, null, null, null,
		    null, null, null, null, null, false, null);
		
		assertEquals(1, obss.size());
		
		assertEquals(9, obss.get(0).getObsId().intValue());
	}
	
	/**
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean)
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservationCount_shouldNotIncludeVoidedObsInCount() {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		Integer obss = obsService.getObservationCount(Collections.singletonList(new Person(9)), null, null, null, null,
		    null, null, null, null, false, null);
		
		assertEquals(1, obss.intValue());
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean)
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldReturnObsWhoseGroupIdIsGivenObsGroupId() {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations(null, null, null, null, null, null, null, null, 2 /*obsGroupId*/, null,
		    null, false, null);
		
		assertEquals(2, obss.size());
	}
	
	/**
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean)
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservationCount_shouldReturnCountOfObsWhoseGroupIdIsGivenObsGroupId() {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		Integer count = obsService.getObservationCount(null, null, null, null, null, null, 2 /*obsGroupId*/, null, null,
		    false, null);
		
		assertEquals(2, count.intValue());
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean)
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldReturnObsWhosePersonIsAPatientOnly() {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations(null, null, null, null, Collections.singletonList(PERSON_TYPE.PATIENT),
		    null, null, null, null, null, null, false, null);
		
		assertEquals(15, obss.size());
	}
	
	/**
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean)
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservationCount_shouldReturnCountOfObsWhosePersonIsAPatientOnly() {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		Integer count = obsService.getObservationCount(null, null, null, null,
		    Collections.singletonList(PERSON_TYPE.PATIENT), null, null, null, null, false, null);
		
		assertEquals(15, count.intValue());
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean)
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldReturnAllObsWhosePersonIsAPersonOnly() {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations(null, null, null, null, Collections.singletonList(PERSON_TYPE.PERSON),
		    null, null, null, null, null, null, false, null);
		
		assertEquals(17, obss.size());
	}
	
	/**
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean)
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservationCount_shouldReturnCountOfAllObsWhosePersonIsAPersonOnly() {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		Integer count = obsService.getObservationCount(null, null, null, null,
		    Collections.singletonList(PERSON_TYPE.PERSON), null, null, null, null, false, null);
		
		assertEquals(17, count.intValue());
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean)
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldReturnObsWhosePersonIsAUserOnly() {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations(null, null, null, null, Collections.singletonList(PERSON_TYPE.USER),
		    null, null, null, null, null, null, false, null);
		
		assertEquals(1, obss.size());
	}
	
	/**
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean)
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservationCount_shouldReturnCountOfObsWhosePersonIsAUserOnly() {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		Integer count = obsService.getObservationCount(null, null, null, null, Collections.singletonList(PERSON_TYPE.USER),
		    null, null, null, null, false, null);
		
		assertEquals(1, count.intValue());
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean)
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldReturnObsWithLocationInGivenLocationsParameter() {
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations(null, null, null, null, null,
		    Collections.singletonList(new Location(1)), null, null, null, null, null, false, null);
		
		assertEquals(8, obss.size());
	}
	
	/**
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean)
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservationCount_shouldReturnCountOfObsWithLocationInGivenLocationsParameter() {
		ObsService obsService = Context.getObsService();
		
		Integer count = obsService.getObservationCount(null, null, null, null, null,
		    Collections.singletonList(new Location(1)), null, null, null, false, null);
		
		assertEquals(8, count.intValue());
	}
	
	/**
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservationCount_shouldReturnCountOfObsWithMatchingAccessionNumber() {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		Integer count = obsService.getObservationCount(null, null, null, null, null, null, null, null, null, false, "AN1");
		
		assertEquals(2, count.intValue());
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean)
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldSortReturnedObsByConceptIdIfSortIsConcept() {
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations(Collections.singletonList(new Person(7)), null, null, null, null, null,
		    Arrays.asList("concept", "obsDatetime"), null, null, null, null, false, null);
		
		// check the order of a few of the obs returned
		assertEquals(11, obss.get(0).getObsId().intValue());
		assertEquals(9, obss.get(1).getObsId().intValue());
		assertEquals(16, obss.get(2).getObsId().intValue());
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean)
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldSortReturnedObsByObsDatetimeIfSortIsEmpty() {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations(Collections.singletonList(new Person(8)), null, null, null, null, null,
				new ArrayList<>(), null, null, null, null, false, null);
		
		assertEquals(8, obss.get(0).getObsId().intValue());
		assertEquals(7, obss.get(1).getObsId().intValue());
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldOnlyReturnedObsWithMatchingAccessionNumber() {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss1 = obsService.getObservations(null, null, null, null, null, null, null, null, null, null, null,
		    false, "AN1");
		
		List<Obs> obss2 = obsService.getObservations(Collections.singletonList(new Person(6)), null, null, null, null, null,
		    null, null, null, null, null, false, "AN2");
		
		List<Obs> obss3 = obsService.getObservations(Collections.singletonList(new Person(8)), null, null, null, null, null,
		    null, null, null, null, null, false, "AN2");
		
		assertEquals(2, obss1.size());
		assertEquals(1, obss2.size());
		assertEquals(0, obss3.size());
	}
	
	/**
	 * @see ObsService#getObservations(String)
	 */
	@Test
	public void getObservations_shouldGetObsMatchingPatientIdentifierInSearchString() {
		executeDataSet(INITIAL_OBS_XML);

		updateSearchIndex();
		
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations("12345K");
		
		assertEquals(2, obss.size());
		assertEquals(4, obss.get(0).getObsId().intValue());
		assertEquals(3, obss.get(1).getObsId().intValue());
	}
	
	/**
	 * @see ObsService#getObservations(String)
	 */
	@Test
	public void getObservations_shouldGetObsMatchingEncounterIdInSearchString() {
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations("5");
		
		assertEquals(1, obss.size());
		assertEquals(16, obss.get(0).getObsId().intValue());
	}
	
	/**
	 * @see ObsService#getObservations(String)
	 */
	@Test
	public void getObservations_shouldGetObsMatchingObsIdInSearchString() {
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations("15");
		
		assertEquals(1, obss.size());
		assertEquals(15, obss.get(0).getObsId().intValue());
	}
	
	/**
	 * @see ObsService#getObservationsByPerson(Person)
	 */
	@Test
	public void getObservationsByPerson_shouldGetAllObservationsAssignedToGivenPerson() {
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservationsByPerson(new Person(7));
		
		assertEquals(9, obss.size());
		assertEquals(16, obss.get(0).getObsId().intValue());
		assertEquals(7, obss.get(8).getObsId().intValue());
	}
	
	/**
	 * @see ObsService#getObservationsByPersonAndConcept(Person,Concept)
	 */
	@Test
	public void getObservationsByPersonAndConcept_shouldGetObservationsMatchingPersonAndQuestion() {
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservationsByPersonAndConcept(new Person(7), new Concept(5089));
		
		assertEquals(3, obss.size());
		assertEquals(16, obss.get(0).getObsId().intValue());
		assertEquals(10, obss.get(1).getObsId().intValue());
		assertEquals(7, obss.get(2).getObsId().intValue());
	}
	
	/**
	 * @see ObsService#getObservationsByPersonAndConcept(Person,Concept)
	 */
	@Test
	public void getObservationsByPersonAndConcept_shouldNotFailWithNullPersonParameter() {
		ObsService obsService = Context.getObsService();
		
		obsService.getObservationsByPersonAndConcept(null, new Concept(7));
	}
	
	/**
	 * @see ObsService#purgeObs(Obs)
	 */
	@Test
	public void purgeObs_shouldDeleteTheGivenObsFromTheDatabase() {
		ObsService obsService = Context.getObsService();
		Obs obs = obsService.getObs(7);
		
		obsService.purgeObs(obs);
		
		assertNull(obsService.getObs(7));
		
		
		executeDataSet(COMPLEX_OBS_XML);
		Obs complexObs = obsService.getComplexObs(44, ComplexObsHandler.RAW_VIEW);
		// obs #44 is coded by the concept complex #8473 pointing to ImageHandler
		// ImageHandler inherits AbstractHandler which handles complex data files on disk
		assertNotNull(complexObs.getComplexData());
		AdministrationService as = Context.getAdministrationService();
		File complexObsDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(as
		        .getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR));
		for (File file : complexObsDir.listFiles()) {
			file.delete();
		}

		obsService.purgeObs(complexObs);
		
		assertNull(obsService.getObs(obs.getObsId()));
	}
	
	/**
	 * @see ObsService#purgeObs(Obs,boolean)
	 */
	@Test
	public void purgeObs_shouldThrowAPIExceptionIfGivenTrueCascade() {
		assertThrows(APIException.class, () -> Context.getObsService().purgeObs(new Obs(1), true));
	}
	
	/**
	 * @see ObsService#saveObs(Obs,String)
	 */
	@Test
	public void saveObs_shouldAllowSettingPropertiesOnObs() {
		ObsService obsService = Context.getObsService();
		
		Order order = null;
		Concept concept = Context.getConceptService().getConcept(3);
		Patient patient = new Patient(2);
		Encounter encounter = new Encounter(3);
		Date datetime = new Date();
		Location location = new Location(1);
		Integer valueGroupId = 7;
		Date valueDatetime = new Date();
		Concept valueCoded = new Concept(3);
		Double valueNumeric = 2.0;
		String valueModifier = "cc";
		String valueText = "value text2";
		String comment = "commenting2";
		
		Obs obs = new Obs();
		obs.setOrder(order);
		obs.setConcept(concept);
		obs.setPerson(patient);
		obs.setEncounter(encounter);
		obs.setObsDatetime(datetime);
		obs.setLocation(location);
		obs.setValueGroupId(valueGroupId);
		obs.setValueDatetime(valueDatetime);
		obs.setValueCoded(valueCoded);
		obs.setValueNumeric(valueNumeric);
		obs.setValueModifier(valueModifier);
		obs.setValueText(valueText);
		obs.setComment(comment);
		
		Obs saved = obsService.saveObs(obs, null);
		
		assertEquals(order, saved.getOrder());
		assertEquals(patient, saved.getPerson());
		assertEquals(comment, saved.getComment());
		assertEquals(concept, saved.getConcept());
		assertEquals(encounter, saved.getEncounter());
		assertEquals(DateUtil.truncateToSeconds(datetime), saved.getObsDatetime());
		assertEquals(location, saved.getLocation());
		assertEquals(valueGroupId, saved.getValueGroupId());
		assertEquals(DateUtil.truncateToSeconds(valueDatetime), saved.getValueDatetime());
		assertEquals(valueCoded, saved.getValueCoded());
		assertEquals(valueNumeric, saved.getValueNumeric());
		assertEquals(valueModifier, saved.getValueModifier());
		assertEquals(valueText, saved.getValueText());
	}
	
	/**
	 * @see ObsService#saveObs(Obs,String)
	 */
	@Test
	public void saveObs_shouldCreateVeryBasicObsAndAddNewObsId() {
		Obs o = new Obs();
		o.setConcept(Context.getConceptService().getConcept(3));
		o.setPerson(new Patient(2));
		o.setEncounter(new Encounter(3));
		o.setObsDatetime(new Date());
		o.setLocation(new Location(1));
		o.setValueNumeric(50d);
		
		Obs oSaved = Context.getObsService().saveObs(o, null);
		
		// make sure the returned Obs and the passed in obs
		// now both have primary key obsIds
		assertTrue(oSaved.getObsId().equals(o.getObsId()));
	}
	
	/**
	 * @see ObsService#saveObs(Obs,String)
	 */
	@Test
	public void saveObs_shouldReturnADifferentObjectWhenUpdatingAnObs() {
		ObsService obsService = Context.getObsService();
		
		Obs obs = obsService.getObs(7);
		
		// change something on the obs and save it again
		obs.setComment("A new comment");
		Obs obsSaved = obsService.saveObs(obs, "Testing that a new obs is returned");
		
		assertFalse(obsSaved.getObsId().equals(obs.getObsId()));
	}
	
	/**
	 * @see ObsService#unvoidObs(Obs)
	 */
	@Test
	public void unvoidObs_shouldCascadeUnvoidToChildGroupedObs() {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		// a obs with child groups
		Obs parentObs = obsService.getObs(2);
		
		obsService.voidObs(parentObs, "testing void cascade to child obs groups");
		
		assertTrue(obsService.getObs(9).getVoided());
		assertTrue(obsService.getObs(10).getVoided());
	}
	
	/**
	 * @see ObsService#unvoidObs(Obs)
	 */
	@Test
	public void unvoidObs_shouldUnsetVoidedBitOnGivenObs() {
		ObsService obsService = Context.getObsService();
		
		Obs obs = obsService.getObs(7);
		
		obsService.unvoidObs(obs);
		
		assertFalse(obs.getVoided());
	}
	
	/**
	 * @see ObsService#voidObs(Obs,String)
	 */
	@Test
	public void voidObs_shouldFailIfReasonParameterIsEmpty() {
		ObsService obsService = Context.getObsService();
		
		Obs obs = obsService.getObs(7);
		
		assertThrows(IllegalArgumentException.class, () -> obsService.voidObs(obs, ""));
	}
	
	/**
	 * @see ObsService#voidObs(Obs,String)
	 */
	@Test
	public void voidObs_shouldSetVoidedBitOnGivenObs() {
		ObsService obsService = Context.getObsService();
		
		Obs obs = obsService.getObs(7);
		
		obsService.voidObs(obs, "testing void function");
		
		assertTrue(obs.getVoided());
	}
	
	/**
	 * @see ObsService#saveObs(Obs,String)
	 */
	@Test
	public void saveObs_shouldSetCreatorAndDateCreatedOnNewObs() {
		Obs o = new Obs();
		o.setConcept(Context.getConceptService().getConcept(3));
		o.setPerson(new Patient(2));
		o.setEncounter(new Encounter(3));
		o.setObsDatetime(new Date());
		o.setLocation(new Location(1));
		o.setValueNumeric(50d);
		
		Context.getObsService().saveObs(o, null);
		assertNotNull(o.getDateCreated());
		assertNotNull(o.getCreator());
	}
	
	/**
	 * @see ObsService#saveObs(Obs,String)
	 */
	@Test
	public void saveObs_shouldCascadeSaveToChildObsGroups() {
		ObsService obsService = Context.getObsService();
		
		Obs parentObs = new Obs();
		parentObs.setConcept(Context.getConceptService().getConcept(3));
		parentObs.setObsDatetime(new Date());
		parentObs.setPerson(new Patient(2));
		parentObs.setLocation(new Location(1));
		
		Obs groupMember = new Obs();
		groupMember.setConcept(Context.getConceptService().getConcept(3));
		groupMember.setValueNumeric(1.0);
		groupMember.setObsDatetime(new Date());
		groupMember.setPerson(new Patient(2));
		groupMember.setLocation(new Location(1));
		parentObs.addGroupMember(groupMember);
		
		obsService.saveObs(parentObs, null);
		
		// make sure the child obs was saved
		assertNotNull(groupMember.getObsId());
	}
	
	/**
	 * @see ObsService#saveObs(Obs,String)
	 */
	@Test
	public void saveObs_shouldCascadeUpdateToNewChildObsGroups() {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		// a obs with child groups
		Obs origParentObs = obsService.getObs(2);
		Set<Obs> originalMembers = new HashSet<>(origParentObs.getGroupMembers(true));
		assertEquals(3, originalMembers.size());
		assertTrue(originalMembers.contains(obsService.getObs(9)));
		assertTrue(originalMembers.contains(obsService.getObs(10)));

		Obs groupMember = new Obs();
		groupMember.setConcept(Context.getConceptService().getConcept(3));
		groupMember.setObsDatetime(new Date());
		groupMember.setPerson(new Patient(2));
		groupMember.setLocation(new Location(2));
		groupMember.setValueNumeric(50d);
		origParentObs.addGroupMember(groupMember);
		assertNotNull(groupMember.getObsGroup());
		
		Obs newParentObs = obsService.saveObs(origParentObs, "Updating obs group");
		assertEquals(origParentObs, newParentObs);
		assertEquals(4, newParentObs.getGroupMembers(true).size());
		// make sure the api filled in all of the necessary ids again
		assertNotNull(groupMember.getObsId());
		assertTrue(newParentObs.getGroupMembers(true).contains(obsService.getObs(9)));
		assertTrue(newParentObs.getGroupMembers(true).contains(obsService.getObs(10)));
		assertTrue(newParentObs.getGroupMembers(true).contains(obsService.getObs(groupMember.getObsId())));
	}
	
	/**
	 * @see ObsService#getObservationCount(List, boolean)
	 */
	@Test
	public void getObservationCount_shouldIncludeVoidedObservationsUsingTheSpecifiedConceptNamesAsAnswers() {
		ObsService os = Context.getObsService();
		Obs o = new Obs();
		o.setConcept(Context.getConceptService().getConcept(3));
		o.setPerson(new Patient(2));
		o.setEncounter(new Encounter(3));
		o.setObsDatetime(new Date());
		o.setLocation(new Location(1));
		ConceptName cn1 = new ConceptName(1847);
		o.setValueCodedName(cn1);
		os.saveObs(o, null);
		
		Obs o2 = new Obs();
		o2.setConcept(Context.getConceptService().getConcept(3));
		o2.setPerson(new Patient(2));
		o2.setEncounter(new Encounter(3));
		o2.setObsDatetime(new Date());
		o2.setLocation(new Location(1));
		ConceptName cn2 = new ConceptName(2453);
		o2.setValueCodedName(cn2);
		o2.setVoided(true);
		os.saveObs(o2, null);
		
		List<ConceptName> names = new LinkedList<>();
		names.add(cn1);
		names.add(cn2);
		assertEquals(2, os.getObservationCount(names, true).intValue());
	}
	
	/**
	 * @see ObsService#getObservationCount(List, boolean)
	 */
	@Test
	public void getObservationCount_shouldReturnTheCountOfAllObservationsUsingTheSpecifiedConceptNamesAsAnswers()
	{
		ObsService os = Context.getObsService();
		Obs o = new Obs();
		o.setConcept(Context.getConceptService().getConcept(3));
		o.setPerson(new Patient(2));
		o.setEncounter(new Encounter(3));
		o.setObsDatetime(new Date());
		o.setLocation(new Location(1));
		ConceptName cn1 = new ConceptName(1847);
		o.setValueCodedName(cn1);
		os.saveObs(o, null);
		
		Obs o2 = new Obs();
		o2.setConcept(Context.getConceptService().getConcept(3));
		o2.setPerson(new Patient(2));
		o2.setEncounter(new Encounter(3));
		o2.setObsDatetime(new Date());
		o2.setLocation(new Location(1));
		ConceptName cn2 = new ConceptName(2453);
		o2.setValueCodedName(cn2);
		os.saveObs(o2, null);
		
		List<ConceptName> names = new LinkedList<>();
		names.add(cn1);
		names.add(cn2);
		assertEquals(2, os.getObservationCount(names, true).intValue());
		
	}
	
	/**
	 * @see ObsService#getObservationCount(List, boolean)
	 */
	@Test
	public void getObservationCount_shouldReturnZeroIfNoObservationIsUsingAnyOfTheConcepNamesInTheList() {
		List<ConceptName> names = new LinkedList<>();
		names.add(new ConceptName(1847));
		names.add(new ConceptName(2453));
		assertEquals(0, Context.getObsService().getObservationCount(names, true).intValue());
	}
	
	/**
	 * @see ObsService#saveObs(Obs,String)
	 */
	@Test
	public void saveObs_shouldLinkOriginalAndUpdatedObs() {
		// build
		int obsId = 7;
		ObsService obsService = Context.getObsService();
		Obs obs = obsService.getObs(obsId);
		
		// operate
		// change something on the obs and save it again
		obs.setComment("A new comment");
		Obs obsSaved = obsService.saveObs(obs, "Testing linkage");
		obs = obsService.getObs(obsId);
		
		// check
		assertNotNull(obsSaved);
		assertNotNull(obs);
		assertEquals(obs, obsSaved.getPreviousVersion());
	}
	
	/**
	 * @see ObsService#saveObs(Obs,String)
	 */
	@Test
	public void saveObs_shouldSetVoidReasonMessageToChangeMessage() {
		
		// Set changeMessage arg to saveObs() method - should equal void reason on new Obs
		String changeMessage = "Testing TRUNK-3701";
		
		int obsId = 7;
		ObsService obsService = Context.getObsService();
		Obs obs = obsService.getObs(obsId);
		
		// change something on the obs and save it again
		obs.setComment("Comment to make sure obs changes");
		
		obsService.saveObs(obs, changeMessage);
		obs = obsService.getObs(obsId); //refetch original (now voided) obs
		
		// check
		assertEquals(changeMessage, obs.getVoidReason());
	}
	
	@Test
	public void saveObs_shouldOverwriteObsPersonValueWithEncounterPatient() {
		String changeMessage = "Testing TRUNK-3283";
		
		executeDataSet(ENCOUNTER_OBS_XML);
		ObsService obsService = Context.getObsService();
		Obs obs = obsService.getObs(13);
		//overwrite ObsPerson with EncounterPatient
		Obs obsSaved = obsService.saveObs(obs, changeMessage);
		
		assertEquals(obs.getPerson(), obsSaved.getEncounter().getPatient());
	}
	
	/**
	 * @see ObsService#purgeObs(Obs,boolean)
	 */
	@Test
	public void purgeObs_shouldDeleteAnyObsGroupMembersBeforeDeletingTheObs() {
		
		executeDataSet(INITIAL_OBS_XML);
		ObsService obsService = Context.getObsService();
		
		final int parentObsId = 1;
		Obs obs = obsService.getObs(parentObsId);
		
		final int childObsId = 2;
		final int unrelatedObsId = 3;
		final int orderReferencingObsId = 4;
		obs.addGroupMember(obsService.getObs(childObsId));
		obs.addGroupMember(obsService.getObs(orderReferencingObsId));
		
		final int conceptProposalObsId = 5;
		ConceptProposal conceptProposal = new ConceptProposal();
		conceptProposal.setObs(obsService.getObs(conceptProposalObsId));
		obs.addGroupMember(conceptProposal.getObs());
		
		//before calling purgeObs method the Obs exists
		assertNotNull(obsService.getObs(parentObsId));
		assertNotNull(obsService.getObs(childObsId));
		assertNotNull(obsService.getObs(unrelatedObsId));
		assertNotNull(obsService.getObs(orderReferencingObsId));
		assertNotNull(obsService.getObs(conceptProposalObsId));
		
		Context.getObsService().purgeObs(obs, false);
		
		//	After calling purgeObs method Obs are deleted
		assertNull(obsService.getObs(parentObsId));
		assertNull(obsService.getObs(childObsId));
		assertNotNull(obsService.getObs(unrelatedObsId));
		assertNull(obsService.getObs(orderReferencingObsId));
		assertNull(obsService.getObs(conceptProposalObsId));
	}
	
	/**
	 * @see ObsService#purgeObs(Obs,boolean)
	 */
	@Test
	public void purgeObs_shouldNotDeleteReferencedOrdersWhenPurgingObs() {
		
		executeDataSet(INITIAL_OBS_XML);
		ObsService obsService = Context.getObsService();
		final OrderService orderService = Context.getOrderService();
		
		final int orderReferencingObsId = 4;
		final Obs obs = obsService.getObs(orderReferencingObsId);
		
		final Order order = obs.getOrder();
		final Integer referencedOrderId = order.getOrderId();
		
		Context.getObsService().purgeObs(obs, false);
		
		assertNull(obsService.getObs(orderReferencingObsId));
		assertNotNull(orderService.getOrder(referencedOrderId));
	}
	
	/**
	 * @see ObsService#saveObs(Obs,String)
	 */
	@Test
	public void saveObs_shouldDeleteThePreviousFileWhenAComplexObservationIsUpdatedWithANewComplexValue() {
		
		String changeMessage = "Testing TRUNK-4538";
		
		executeDataSet(COMPLEX_OBS_XML);
		
		ObsService os = Context.getObsService();
		ConceptService cs = Context.getConceptService();
		AdministrationService as = Context.getAdministrationService();
		
		// make sure the file isn't there to begin with
		File complexObsDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(as
		        .getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR));
		final File createdFile = new File(complexObsDir, "nameOfFile.txt");
		if (createdFile.exists())
			createdFile.delete();
		
		// the complex data to put onto an obs that will be saved
		Reader input = new CharArrayReader("This is a string to save to a file".toCharArray());
		ComplexData complexData = new ComplexData("nameOfFile.txt", input);
		
		// must fetch the concept instead of just new Concept(8473) because the attributes on concept are checked
		// this is a concept mapped to the text handler
		Concept questionConcept = cs.getConcept(8474);
		
		Obs obsToSave = new Obs(new Person(1), questionConcept, new Date(), new Location(1));
		obsToSave.setComplexData(complexData);
		os.saveObs(obsToSave, null);
		
		File updatedFile = new File(complexObsDir, "nameOfUpdatedFile.txt");
		if (updatedFile.exists())
			updatedFile.delete();
		
		// the complex data to put onto an obs that will be updated
		Reader updatedInput = new CharArrayReader(
		        "This is a string to save to a file which uploaded to update an obs".toCharArray());
		ComplexData updatedComplexData = new ComplexData("nameOfUpdatedFile.txt", updatedInput);
		
		obsToSave.setComplexData(updatedComplexData);
		try {
			os.saveObs(obsToSave, changeMessage);
			
			assertFalse(createdFile.exists());
		}
		finally {
			// we always have to delete this inside the same unit test because it is outside the
			// database and hence can't be "rolled back" like everything else
			updatedFile.delete();
		}
		
	}
	
	/**
	 * @see ObsService#saveObs(Obs,String)
	 */
	@Test
	public void saveObs_shouldNotVoidAnObsWithNoChanges() {
		executeDataSet(ENCOUNTER_OBS_XML);
		ObsService os = Context.getObsService();
		Obs obs = os.getObs(14);
		assertFalse(obs.getGroupMembers(true).isEmpty());
		assertFalse(obs.getGroupMembers(false).isEmpty());
		assertFalse(obs.isDirty());
		Set<Obs> originalMembers = new HashSet<>(obs.getGroupMembers());
		for (Obs o : originalMembers) {
			assertFalse(o.isDirty());
		}
		Obs saveObs = os.saveObs(obs, "no change");
		assertEquals(obs, saveObs);
		assertFalse(saveObs.getVoided());

		Set<Obs> savedMembers = new HashSet<>(saveObs.getGroupMembers());
		assertFalse(saveObs.isDirty());
		for (Obs o : savedMembers) {
			assertFalse(o.isDirty(), "obs"+o.getId());
		}

	}

	/**
	 * @see ObsService#saveObs(Obs,String)
	 */
	@Test
	public void saveObs_shouldCopyTheFormNamespaceAndPathFieldInEditedObs() {
		executeDataSet(INITIAL_OBS_XML);
		Obs obs = Context.getObsService().getObs(7);
		obs.setValueNumeric(5.0);
		Obs o2 = Context.getObsService().saveObs(obs, "just testing");
		assertNotNull(obs.getFormFieldNamespace());

		// fetch the obs from the database again
		obs = Context.getObsService().getObs(o2.getObsId());
		assertNotNull(obs.getFormFieldNamespace());
		assertNotNull(obs.getFormFieldPath());
	}

	/**
	 * @see ObsService#saveObs(Obs,String)
	 */
	@Test
	public void saveObs_shouldVoidOnlyOldObsWhenAllObsEditedAndNewObsAdded() {
		executeDataSet(INITIAL_OBS_XML);
		ConceptService cs = Context.getConceptService();
		Date newDate = new Date();
		//Update the entire Obs Tree obsDateTime
		Obs obs = Context.getObsService().getObs(2);
		obs.setObsDatetime(new Date());
		Obs child = null;
		for(Obs member : obs.getGroupMembers()) {
			member.setObsDatetime(newDate);
			if(member.getId() == 17) {
				child = member;
			}
		}

		Obs child1 = child.getGroupMembers().iterator().next();
		child1.setObsDatetime(newDate);

		//add a new obs at depth>1
		Obs o1 = new Obs();
		o1.setConcept(cs.getConcept(3));
		o1.setDateCreated(newDate);
		o1.setCreator(Context.getAuthenticatedUser());
		o1.setLocation(new Location(1));
		o1.setObsDatetime(newDate);
		o1.setValueText("NewObs Value");
		o1.setPerson(new Patient(2));
		child.addGroupMember(o1);

		int count = 0;

		Obs newObs = Context.getObsService().saveObs(obs, "just testing");

		assertEquals(newObs.getObsDatetime().toString(), newDate.toString());

		for(Obs member : newObs.getGroupMembers()) {
			assertEquals(member.getObsDatetime().toString(), newDate.toString());
			if(member.getGroupMembers()!= null) {

				for (Obs memberChild : member.getGroupMembers()) {
					assertEquals(memberChild.getObsDatetime().toString(), newDate.toString());
					if (memberChild.getValueText()!= null && memberChild.getValueText().equals("NewObs Value")) {
						count++;
					}
				}
				if (count == 0) {
					fail("New Obs not created");
				}
			}
		}
	}
	
	@Test
	public void saveObs_shouldSetStatusToAmendedWhenModifyingAnObsWithFinalStatus() throws Exception {
		Obs existing = obsService.getObs(7);
		existing.setValueNumeric(60.0);
		Obs amended = obsService.saveObs(existing, "testing");
		assertThat(amended.getValueNumeric(), is(60.0));
		assertThat(amended.getStatus(), is(Obs.Status.AMENDED));
		assertThat(existing.getStatus(), is(Obs.Status.FINAL));
	}
	
	@Test
	public void saveObs_shouldNotChangeStatusOfPreliminaryWhenModifyingAnObs() throws Exception {
		Obs existing = obsService.getObs(9);
		existing.setValueNumeric(175.0);
		Obs newObs = obsService.saveObs(existing, "testing");
		assertThat(newObs.getValueNumeric(), is(175.0));
		assertThat(newObs.getStatus(), is(Obs.Status.PRELIMINARY));
	}
	
	@Test
	public void saveObs_shouldLetYouChangeStatusFromPreliminaryToFinalWhenModifyingAnObs() throws Exception {
		Obs existing = obsService.getObs(9);
		existing.setValueNumeric(175.0);
		existing.setStatus(Obs.Status.FINAL);
		Obs newObs = obsService.saveObs(existing, "testing");
		assertThat(newObs.getValueNumeric(), is(175.0));
		assertThat(newObs.getStatus(), is(Obs.Status.FINAL));
	}
	
	/**
	 * Tests that we support a manual workaround in case you need to modify a FINAL obs and leave its status as FINAL
	 */
	@Test
	public void shouldNotAutomaticallySetStatusWhenManuallyCopyingAnObs() throws Exception {
		Obs existing = obsService.getObs(7);
		Obs newObs = Obs.newInstance(existing);
		newObs.setValueNumeric(60.0);
		newObs.setPreviousVersion(existing);
		newObs = obsService.saveObs(newObs, null);
		obsService.voidObs(existing, "testing");
		
		assertThat(existing.getStatus(), is(Obs.Status.FINAL));
		assertThat(existing.getVoided(), is(true));
		assertThat(newObs.getStatus(), is(Obs.Status.FINAL));
	}
}
