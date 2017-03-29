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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.CharArrayReader;
import java.io.File;
import java.io.Reader;
import java.text.DateFormat;
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

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.DateUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsConstants.PERSON_TYPE;
import org.openmrs.util.OpenmrsUtil;

/**
 * TODO clean up and add tests for all methods in ObsService
 */
public class ObsServiceTest extends BaseContextSensitiveTest {
	
	protected static final String INITIAL_OBS_XML = "org/openmrs/api/include/ObsServiceTest-initial.xml";
	
	protected static final String ENCOUNTER_OBS_XML = "org/openmrs/api/include/ObsServiceTest-EncounterOverwrite.xml";
	
	protected static final String COMPLEX_OBS_XML = "org/openmrs/api/include/ObsServiceTest-complex.xml";

	protected static final String REVISION_OBS_XML = "org/openmrs/api/include/ObsServiceTest-RevisionObs.xml";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();


	/**
	 * This method gets the revision obs for voided obs
	 *
	 * @see ObsService#getRevisionObs(Obs)
	 */
	@Test
	public void shouldGetRevisedObs() throws Exception {
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
		expectedException.expect(APIException.class);
		expectedException.expectMessage(Context.getMessageSourceService().getMessage("Obs.error.cannot.be.null"));
		ObsService os = Context.getObsService();
		os.saveObs(null,"Null Obs");
	}
	
	/**
	 * This test tests multi-level heirarchy obsGroup cascades for create, delete, update, void, and
	 * unvoid
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldSaveUpdateDeleteVoidObsGroupCascades() throws Exception {
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
	 * @see ObsService#getObservations(List, List, List, List, List, List, List, Integer, Integer,
	 *      Date, Date, boolean)
	 */
	@Test
	public void getObservations_shouldCompareDatesUsingLteAndGte() throws Exception {
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
	 * @throws Exception
	 */
	@Test
	public void shouldGetObservationsOnDay() throws Exception {
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
	 * @see ObsService#getComplexObs(Integer,String)
	 */
	@Test
	public void getComplexObs_shouldFillInComplexDataObjectForComplexObs() throws Exception {
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
		
		Obs complexObs = os.getComplexObs(44, ComplexObsHandler.RAW_VIEW);
		
		Assert.assertNotNull(complexObs);
		Assert.assertTrue(complexObs.isComplex());
		Assert.assertNotNull(complexObs.getValueComplex());
		Assert.assertNotNull(complexObs.getComplexData());
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
	public void getComplexObs_shouldNotFailWithNullView() throws Exception {
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
	public void getComplexObs_shouldReturnNormalObsForNonComplexObs() throws Exception {
		executeDataSet(COMPLEX_OBS_XML);
		
		ObsService os = Context.getObsService();
		
		Obs normalObs = os.getComplexObs(7, ComplexObsHandler.RAW_VIEW);
		
		Assert.assertFalse(normalObs.isComplex());
	}
	
	/**
	 * @see ObsService#getHandler(String)
	 */
	@Test
	public void getHandler_shouldHaveDefaultImageAndTextHandlersRegisteredBySpring() throws Exception {
		ObsService os = Context.getObsService();
		ComplexObsHandler imgHandler = os.getHandler("ImageHandler");
		Assert.assertNotNull(imgHandler);
		
		ComplexObsHandler textHandler = os.getHandler("TextHandler");
		Assert.assertNotNull(textHandler);
	}
	
	/**
	 * @see ObsService#getHandler(String)
	 */
	@Test
	public void getHandler_shouldGetHandlerWithMatchingKey() throws Exception {
		ObsService os = Context.getObsService();
		ComplexObsHandler handler = os.getHandler("ImageHandler");
		Assert.assertNotNull(handler);
		Assert.assertTrue(handler instanceof ImageHandler);
	}
	
	/**
	 * @see ObsService#getHandlers()
	 */
	@Test
	public void getHandlers_shouldNeverReturnNull() throws Exception {
		Assert.assertNotNull(Context.getObsService().getHandlers());
		
		// test our current implementation without it being initialized by spring
		Assert.assertNotNull(new ObsServiceImpl().getHandlers());
	}
	
	/**
	 * @see ObsService#registerHandler(String,ComplexObsHandler)
	 */
	@Test
	public void registerHandler_shouldRegisterHandlerWithTheGivenKey() throws Exception {
		ObsService os = Context.getObsService();
		
		os.registerHandler("DummyHandler", new ImageHandler());
		
		ComplexObsHandler dummyHandler = os.getHandler("DummyHandler");
		Assert.assertNotNull(dummyHandler);
	}
	
	/**
	 * @see ObsService#registerHandler(String,String)
	 */
	@Test
	public void registerHandler_shouldLoadHandlerAndRegisterKey() throws Exception {
		ObsService os = Context.getObsService();
		
		// name it something other than what we used in the previous test
		os.registerHandler("DummyHandler2", "org.openmrs.obs.handler.ImageHandler");
		
		ComplexObsHandler dummyHandler = os.getHandler("DummyHandler2");
		Assert.assertNotNull(dummyHandler);
	}
	
	/**
	 * @see ObsService#removeHandler(String)
	 */
	@Test
	public void removeHandler_shouldNotFailWithInvalidKey() throws Exception {
		Context.getObsService().removeHandler("SomeRandomHandler");
	}
	
	/**
	 * @see ObsService#removeHandler(String)
	 */
	@Test
	public void removeHandler_shouldRemoveHandlerWithMatchingKey() throws Exception {
		ObsService os = Context.getObsService();
		
		// add the handler and make sure its there
		os.registerHandler("DummyHandler3", "org.openmrs.obs.handler.ImageHandler");
		ComplexObsHandler dummyHandler = os.getHandler("DummyHandler3");
		Assert.assertNotNull(dummyHandler);
		
		// now remove the handler and make sure its gone
		os.removeHandler("DummyHandler3");
		ComplexObsHandler dummyHandlerAgain = os.getHandler("DummyHandler3");
		Assert.assertNull(dummyHandlerAgain);
	}
	
	/**
	 * @see ObsService#saveObs(Obs,String)
	 */
	@Test
	public void saveObs_shouldCreateNewFileFromComplexDataForNewObs() throws Exception {
		executeDataSet(COMPLEX_OBS_XML);
		ObsService os = Context.getObsService();
		ConceptService cs = Context.getConceptService();
		AdministrationService as = Context.getAdministrationService();
		
		// make sure the file isn't there to begin with
		File complexObsDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(as
		        .getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR));
		File createdFile = new File(complexObsDir, "nameOfFile.txt");
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
		
		try {
			os.saveObs(obsToSave, null);
			
			// make sure the file appears now after the save
			Assert.assertTrue(createdFile.exists());
		}
		finally {
			// we always have to delete this inside the same unit test because it is outside the
			// database and hence can't be "rolled back" like everything else
			createdFile.delete();
		}
	}
	
	/**
	 * @see ObsService#saveObs(Obs,String)
	 */
	@Test
	public void saveObs_shouldNotOverwriteFileWhenUpdatingAComplexObs() throws Exception {
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
			Assert.assertEquals(oldFileSize, previouslyCreatedFile.length());
			
			String valueComplex = obsToSave.getValueComplex();
			String filename = valueComplex.substring(valueComplex.indexOf("|") + 1).trim();
			newComplexFile = new File(complexObsDir, filename);
			// make sure the file appears now after the save
			Assert.assertTrue(newComplexFile.length() > oldFileSize);
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
	public void setHandlers_shouldAddNewHandlersWithNewKeys() throws Exception {
		ObsService os = Context.getObsService();
		
		Map<String, ComplexObsHandler> handlers = new HashMap<String, ComplexObsHandler>();
		handlers.put("DummyHandler4", new ImageHandler());
		handlers.put("DummyHandler5", new BinaryDataHandler());
		handlers.put("DummyHandler6", new TextHandler());
		
		// set the handlers and make sure they're there
		os.setHandlers(handlers);
		
		ComplexObsHandler dummyHandler4 = os.getHandler("DummyHandler4");
		Assert.assertNotNull(dummyHandler4);
		
		ComplexObsHandler dummyHandler5 = os.getHandler("DummyHandler5");
		Assert.assertNotNull(dummyHandler5);
		
		ComplexObsHandler dummyHandler6 = os.getHandler("DummyHandler6");
		Assert.assertNotNull(dummyHandler6);
	}
	
	/**
	 * @see ObsService#setHandlers(Map<QString;QComplexObsHandler;>)}
	 */
	@Test
	public void setHandlers_shouldOverrideHandlersWithSameKey() throws Exception {
		ObsService os = Context.getObsService();
		
		Map<String, ComplexObsHandler> handlers = new HashMap<String, ComplexObsHandler>();
		handlers.put("DummyHandlerToOverride", new ImageHandler());
		
		// set the handlers and make sure they're there
		os.setHandlers(handlers);
		
		ComplexObsHandler dummyHandlerToOverride = os.getHandler("DummyHandlerToOverride");
		Assert.assertTrue(dummyHandlerToOverride instanceof ImageHandler);
		
		// now override that key and make sure the new class is stored
		
		Map<String, ComplexObsHandler> handlersAgain = new HashMap<String, ComplexObsHandler>();
		handlersAgain.put("DummyHandlerToOverride", new BinaryDataHandler());
		
		os.setHandlers(handlersAgain);
		
		ComplexObsHandler dummyHandlerToOverrideAgain = os.getHandler("DummyHandlerToOverride");
		Assert.assertTrue(dummyHandlerToOverrideAgain instanceof BinaryDataHandler);
		
	}
	
	/**
	 * @see ObsService#saveObs(Obs,String)
	 */
	@Test
	public void saveObs_shouldVoidTheGivenObsInTheDatabase() throws Exception {
		Obs obs = Context.getObsService().getObs(7);
		obs.setValueNumeric(1.0);
		Context.getObsService().saveObs(obs, "just testing");
		
		// fetch the obs from the database again
		obs = Context.getObsService().getObs(7);
		Assert.assertTrue(obs.getVoided());
	}
	
	/**
	 * @see ObsService#getObs(Integer)
	 */
	@Test
	public void getObs_shouldGetObsMatchingGivenObsId() throws Exception {
		ObsService obsService = Context.getObsService();
		
		Obs obs = obsService.getObs(7);
		
		Assert.assertEquals(5089, obs.getConcept().getId().intValue());
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean)
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldGetAllObsAssignedToGivenEncounters() throws Exception {
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations(null, Collections.singletonList(new Encounter(4)), null, null, null,
		    null, null, null, null, null, null, false, null);
		
		Assert.assertEquals(6, obss.size());
	}
	
	/**
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean)
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservationCount_shouldGetCountOfObsAssignedToGivenEncounters() throws Exception {
		ObsService obsService = Context.getObsService();
		
		Integer count = obsService.getObservationCount(null, Collections.singletonList(new Encounter(4)), null, null, null,
		    null, null, null, null, false, null);
		
		Assert.assertEquals(6, count.intValue());
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean)
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldGetAllObsWithAnswerConceptInGivenAnswersParameter() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations(null, null, null, Collections.singletonList(new Concept(7)), null, null,
		    null, null, null, null, null, false, null);
		
		// obs 11 in INITIAL_OBS_XML and obs 13 in standardTestDataset
		Assert.assertEquals(3, obss.size());
		Set<Integer> ids = new HashSet<Integer>();
		for (Obs o : obss) {
			ids.add(o.getObsId());
		}
		Assert.assertTrue(ids.contains(11));
		Assert.assertTrue(ids.contains(13));
	}
	
	/**
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean)
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservationCount_shouldGetCountOfObsWithAnswerConceptInGivenAnswersParameter() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		Integer count = obsService.getObservationCount(null, null, null, Collections.singletonList(new Concept(7)), null,
		    null, null, null, null, false, null);
		
		Assert.assertEquals(3, count.intValue());
		
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean)
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldGetAllObsWithQuestionConceptInGivenQuestionsParameter() throws Exception {
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations(null, null, Collections.singletonList(new Concept(5497)), null, null,
		    null, null, null, null, null, null, false, null);
		
		Assert.assertEquals(2, obss.size());
	}
	
	/**
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean)
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservationCount_shouldGetCountOfObsWithQuestionConceptInGivenQuestionsParameter() throws Exception {
		ObsService obsService = Context.getObsService();
		
		Integer count = obsService.getObservationCount(null, null, Collections.singletonList(new Concept(5497)), null, null,
		    null, null, null, null, false, null);
		
		Assert.assertEquals(2, count.intValue());
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean)
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldIncludeVoidedObsIfIncludeVoidedObsIsTrue() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations(Collections.singletonList(new Person(9)), null, null, null, null, null,
		    null, null, null, null, null, true, null);
		
		Assert.assertEquals(2, obss.size());
		
		Assert.assertEquals(10, obss.get(0).getObsId().intValue());
		Assert.assertEquals(9, obss.get(1).getObsId().intValue());
	}
	
	/**
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean)
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservationCount_shouldIncludeVoidedObsInTheCountIfIncludeVoidedObsIsTrue() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		Integer obss = obsService.getObservationCount(Collections.singletonList(new Person(9)), null, null, null, null,
		    null, null, null, null, true, null);
		
		Assert.assertEquals(2, obss.intValue());
		
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean)
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldLimitNumberOfObsReturnedToMostReturnNParameter() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		List<Obs> count = obsService.getObservations(Collections.singletonList(new Person(8)), null, null, null, null, null,
		    null, 1, null, null, null, false, null);
		
		Assert.assertEquals(1, count.size());
		
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean)
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldNotIncludeVoidedObs() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations(Collections.singletonList(new Person(9)), null, null, null, null, null,
		    null, null, null, null, null, false, null);
		
		Assert.assertEquals(1, obss.size());
		
		Assert.assertEquals(9, obss.get(0).getObsId().intValue());
	}
	
	/**
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean)
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservationCount_shouldNotIncludeVoidedObsInCount() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		Integer obss = obsService.getObservationCount(Collections.singletonList(new Person(9)), null, null, null, null,
		    null, null, null, null, false, null);
		
		Assert.assertEquals(1, obss.intValue());
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean)
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldReturnObsWhoseGroupIdIsGivenObsGroupId() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations(null, null, null, null, null, null, null, null, 2 /*obsGroupId*/, null,
		    null, false, null);
		
		Assert.assertEquals(2, obss.size());
	}
	
	/**
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean)
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservationCount_shouldReturnCountOfObsWhoseGroupIdIsGivenObsGroupId() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		Integer count = obsService.getObservationCount(null, null, null, null, null, null, 2 /*obsGroupId*/, null, null,
		    false, null);
		
		Assert.assertEquals(2, count.intValue());
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean)
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldReturnObsWhosePersonIsAPatientOnly() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations(null, null, null, null, Collections.singletonList(PERSON_TYPE.PATIENT),
		    null, null, null, null, null, null, false, null);
		
		Assert.assertEquals(15, obss.size());
	}
	
	/**
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean)
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservationCount_shouldReturnCountOfObsWhosePersonIsAPatientOnly() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		Integer count = obsService.getObservationCount(null, null, null, null,
		    Collections.singletonList(PERSON_TYPE.PATIENT), null, null, null, null, false, null);
		
		Assert.assertEquals(15, count.intValue());
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean)
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldReturnAllObsWhosePersonIsAPersonOnly() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations(null, null, null, null, Collections.singletonList(PERSON_TYPE.PERSON),
		    null, null, null, null, null, null, false, null);
		
		Assert.assertEquals(17, obss.size());
	}
	
	/**
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean)
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservationCount_shouldReturnCountOfAllObsWhosePersonIsAPersonOnly() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		Integer count = obsService.getObservationCount(null, null, null, null,
		    Collections.singletonList(PERSON_TYPE.PERSON), null, null, null, null, false, null);
		
		Assert.assertEquals(17, count.intValue());
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean)
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldReturnObsWhosePersonIsAUserOnly() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations(null, null, null, null, Collections.singletonList(PERSON_TYPE.USER),
		    null, null, null, null, null, null, false, null);
		
		Assert.assertEquals(1, obss.size());
	}
	
	/**
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean)
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservationCount_shouldReturnCountOfObsWhosePersonIsAUserOnly() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		Integer count = obsService.getObservationCount(null, null, null, null, Collections.singletonList(PERSON_TYPE.USER),
		    null, null, null, null, false, null);
		
		Assert.assertEquals(1, count.intValue());
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean)
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldReturnObsWithLocationInGivenLocationsParameter() throws Exception {
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations(null, null, null, null, null,
		    Collections.singletonList(new Location(1)), null, null, null, null, null, false, null);
		
		Assert.assertEquals(8, obss.size());
	}
	
	/**
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean)
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservationCount_shouldReturnCountOfObsWithLocationInGivenLocationsParameter() throws Exception {
		ObsService obsService = Context.getObsService();
		
		Integer count = obsService.getObservationCount(null, null, null, null, null,
		    Collections.singletonList(new Location(1)), null, null, null, false, null);
		
		Assert.assertEquals(8, count.intValue());
	}
	
	/**
	 * @see ObsService#getObservationCount(List,List,List,List,List,List,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservationCount_shouldReturnCountOfObsWithMatchingAccessionNumber() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		Integer count = obsService.getObservationCount(null, null, null, null, null, null, null, null, null, false, "AN1");
		
		Assert.assertEquals(2, count.intValue());
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean)
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldSortReturnedObsByConceptIdIfSortIsConcept() throws Exception {
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations(Collections.singletonList(new Person(7)), null, null, null, null, null,
		    Arrays.asList(new String[] { "concept", "obsDatetime" }), null, null, null, null, false, null);
		
		// check the order of a few of the obs returned
		Assert.assertEquals(11, obss.get(0).getObsId().intValue());
		Assert.assertEquals(9, obss.get(1).getObsId().intValue());
		Assert.assertEquals(16, obss.get(2).getObsId().intValue());
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean)
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldSortReturnedObsByObsDatetimeIfSortIsEmpty() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations(Collections.singletonList(new Person(8)), null, null, null, null, null,
		    new ArrayList<String>(), null, null, null, null, false, null);
		
		Assert.assertEquals(8, obss.get(0).getObsId().intValue());
		Assert.assertEquals(7, obss.get(1).getObsId().intValue());
	}
	
	/**
	 * @see ObsService#getObservations(List,List,List,List,List,List,List,Integer,Integer,Date,Date,boolean,String)
	 */
	@Test
	public void getObservations_shouldOnlyReturnedObsWithMatchingAccessionNumber() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss1 = obsService.getObservations(null, null, null, null, null, null, null, null, null, null, null,
		    false, "AN1");
		
		List<Obs> obss2 = obsService.getObservations(Collections.singletonList(new Person(6)), null, null, null, null, null,
		    null, null, null, null, null, false, "AN2");
		
		List<Obs> obss3 = obsService.getObservations(Collections.singletonList(new Person(8)), null, null, null, null, null,
		    null, null, null, null, null, false, "AN2");
		
		Assert.assertEquals(2, obss1.size());
		Assert.assertEquals(1, obss2.size());
		Assert.assertEquals(0, obss3.size());
	}
	
	/**
	 * @see ObsService#getObservations(String)
	 */
	@Test
	public void getObservations_shouldGetObsMatchingPatientIdentifierInSearchString() throws Exception {
		executeDataSet(INITIAL_OBS_XML);

		updateSearchIndex();
		
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations("12345K");
		
		Assert.assertEquals(2, obss.size());
		Assert.assertEquals(4, obss.get(0).getObsId().intValue());
		Assert.assertEquals(3, obss.get(1).getObsId().intValue());
	}
	
	/**
	 * @see ObsService#getObservations(String)
	 */
	@Test
	public void getObservations_shouldGetObsMatchingEncounterIdInSearchString() throws Exception {
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations("5");
		
		Assert.assertEquals(1, obss.size());
		Assert.assertEquals(16, obss.get(0).getObsId().intValue());
	}
	
	/**
	 * @see ObsService#getObservations(String)
	 */
	@Test
	public void getObservations_shouldGetObsMatchingObsIdInSearchString() throws Exception {
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservations("15");
		
		Assert.assertEquals(1, obss.size());
		Assert.assertEquals(15, obss.get(0).getObsId().intValue());
	}
	
	/**
	 * @see ObsService#getObservationsByPerson(Person)
	 */
	@Test
	public void getObservationsByPerson_shouldGetAllObservationsAssignedToGivenPerson() throws Exception {
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservationsByPerson(new Person(7));
		
		Assert.assertEquals(9, obss.size());
		Assert.assertEquals(16, obss.get(0).getObsId().intValue());
		Assert.assertEquals(7, obss.get(8).getObsId().intValue());
	}
	
	/**
	 * @see ObsService#getObservationsByPersonAndConcept(Person,Concept)
	 */
	@Test
	public void getObservationsByPersonAndConcept_shouldGetObservationsMatchingPersonAndQuestion() throws Exception {
		ObsService obsService = Context.getObsService();
		
		List<Obs> obss = obsService.getObservationsByPersonAndConcept(new Person(7), new Concept(5089));
		
		Assert.assertEquals(3, obss.size());
		Assert.assertEquals(16, obss.get(0).getObsId().intValue());
		Assert.assertEquals(10, obss.get(1).getObsId().intValue());
		Assert.assertEquals(7, obss.get(2).getObsId().intValue());
	}
	
	/**
	 * @see ObsService#getObservationsByPersonAndConcept(Person,Concept)
	 */
	@Test
	public void getObservationsByPersonAndConcept_shouldNotFailWithNullPersonParameter() throws Exception {
		ObsService obsService = Context.getObsService();
		
		obsService.getObservationsByPersonAndConcept(null, new Concept(7));
	}
	
	/**
	 * @see ObsService#purgeObs(Obs)
	 */
	@Test
	public void purgeObs_shouldDeleteTheGivenObsFromTheDatabase() throws Exception {
		ObsService obsService = Context.getObsService();
		Obs obs = obsService.getObs(7);
		
		obsService.purgeObs(obs);
		
		Assert.assertNull(obsService.getObs(7));
		
		
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
	@Test(expected = APIException.class)
	public void purgeObs_shouldThrowAPIExceptionIfGivenTrueCascade() throws Exception {
		Context.getObsService().purgeObs(new Obs(1), true);
	}
	
	/**
	 * @see ObsService#saveObs(Obs,String)
	 */
	@Test
	public void saveObs_shouldAllowChangingOfEveryPropertyOnObs() throws Exception {
		ObsService obsService = Context.getObsService();
		
		Order order = null;
		Concept concept = Context.getConceptService().getConcept(3);
		Patient patient = new Patient(2);
		Encounter encounter = new Encounter(3);
		Date datetime = new Date();
		Location location = new Location(1);
		Integer valueGroupId = Integer.valueOf(7);
		Date valueDatetime = new Date();
		Concept valueCoded = new Concept(3);
		Double valueNumeric = 2.0;
		String valueModifier = "cc";
		String valueText = "value text2";
		String comment = "commenting2";
		
		Obs oToUpdate = new Obs();
		oToUpdate.setOrder(order);
		oToUpdate.setConcept(concept);
		oToUpdate.setPerson(patient);
		oToUpdate.setEncounter(encounter);
		oToUpdate.setObsDatetime(datetime);
		oToUpdate.setLocation(location);
		oToUpdate.setValueGroupId(valueGroupId);
		oToUpdate.setValueDatetime(valueDatetime);
		oToUpdate.setValueCoded(valueCoded);
		oToUpdate.setValueNumeric(valueNumeric);
		oToUpdate.setValueModifier(valueModifier);
		oToUpdate.setValueText(valueText);
		oToUpdate.setComment(comment);
		
		// do an update in the database for the same Obs
		Obs o1ToUpdateSaved = obsService.saveObs(oToUpdate, "Updating o1 with all new values");
		
		Obs obsSaved = obsService.getObs(o1ToUpdateSaved.getObsId());
		
		assertEquals(order, obsSaved.getOrder());
		assertEquals(patient, obsSaved.getPerson());
		assertEquals(comment, obsSaved.getComment());
		assertEquals(concept, obsSaved.getConcept());
		assertEquals(encounter, obsSaved.getEncounter());
		assertEquals(DateUtil.truncateToSeconds(datetime), obsSaved.getObsDatetime());
		assertEquals(location, obsSaved.getLocation());
		assertEquals(valueGroupId, obsSaved.getValueGroupId());
		assertEquals(DateUtil.truncateToSeconds(valueDatetime), obsSaved.getValueDatetime());
		assertEquals(valueCoded, obsSaved.getValueCoded());
		assertEquals(valueNumeric, obsSaved.getValueNumeric());
		assertEquals(valueModifier, obsSaved.getValueModifier());
		assertEquals(valueText, obsSaved.getValueText());
	}
	
	/**
	 * @see ObsService#saveObs(Obs,String)
	 */
	@Test
	public void saveObs_shouldCreateVeryBasicObsAndAddNewObsId() throws Exception {
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
	public void saveObs_shouldReturnADifferentObjectWhenUpdatingAnObs() throws Exception {
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
	public void unvoidObs_shouldCascadeUnvoidToChildGroupedObs() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		
		ObsService obsService = Context.getObsService();
		
		// a obs with child groups
		Obs parentObs = obsService.getObs(2);
		
		obsService.voidObs(parentObs, "testing void cascade to child obs groups");
		
		Assert.assertTrue(obsService.getObs(9).getVoided());
		Assert.assertTrue(obsService.getObs(10).getVoided());
	}
	
	/**
	 * @see ObsService#unvoidObs(Obs)
	 */
	@Test
	public void unvoidObs_shouldUnsetVoidedBitOnGivenObs() throws Exception {
		ObsService obsService = Context.getObsService();
		
		Obs obs = obsService.getObs(7);
		
		obsService.unvoidObs(obs);
		
		assertFalse(obs.getVoided());
	}
	
	/**
	 * @see ObsService#voidObs(Obs,String)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void voidObs_shouldFailIfReasonParameterIsEmpty() throws Exception {
		ObsService obsService = Context.getObsService();
		
		Obs obs = obsService.getObs(7);
		
		obsService.voidObs(obs, "");
	}
	
	/**
	 * @see ObsService#voidObs(Obs,String)
	 */
	@Test
	public void voidObs_shouldSetVoidedBitOnGivenObs() throws Exception {
		ObsService obsService = Context.getObsService();
		
		Obs obs = obsService.getObs(7);
		
		obsService.voidObs(obs, "testing void function");
		
		assertTrue(obs.getVoided());
	}
	
	/**
	 * @see ObsService#saveObs(Obs,String)
	 */
	@Test
	public void saveObs_shouldSetCreatorAndDateCreatedOnNewObs() throws Exception {
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
	public void saveObs_shouldCascadeSaveToChildObsGroups() throws Exception {
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
		Assert.assertNotNull(groupMember.getObsId());
	}
	
	/**
	 * @see ObsService#saveObs(Obs,String)
	 */
	@Test
	public void saveObs_shouldCascadeUpdateToNewChildObsGroups() throws Exception {
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
	public void getObservationCount_shouldIncludeVoidedObservationsUsingTheSpecifiedConceptNamesAsAnswers() throws Exception {
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
		
		List<ConceptName> names = new LinkedList<ConceptName>();
		names.add(cn1);
		names.add(cn2);
		Assert.assertEquals(2, os.getObservationCount(names, true).intValue());
	}
	
	/**
	 * @see ObsService#getObservationCount(List, boolean)
	 */
	@Test
	public void getObservationCount_shouldReturnTheCountOfAllObservationsUsingTheSpecifiedConceptNamesAsAnswers()
	    throws Exception {
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
		
		List<ConceptName> names = new LinkedList<ConceptName>();
		names.add(cn1);
		names.add(cn2);
		Assert.assertEquals(2, os.getObservationCount(names, true).intValue());
		
	}
	
	/**
	 * @see ObsService#getObservationCount(List, boolean)
	 */
	@Test
	public void getObservationCount_shouldReturnZeroIfNoObservationIsUsingAnyOfTheConcepNamesInTheList() throws Exception {
		List<ConceptName> names = new LinkedList<ConceptName>();
		names.add(new ConceptName(1847));
		names.add(new ConceptName(2453));
		Assert.assertEquals(0, Context.getObsService().getObservationCount(names, true).intValue());
	}
	
	/**
	 * @see ObsService#saveObs(Obs,String)
	 */
	@Test
	public void saveObs_shouldLinkOriginalAndUpdatedObs() throws Exception {
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
	public void saveObs_shouldSetVoidReasonMessageToChangeMessage() throws Exception {
		
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
	public void saveObs_shouldOverwriteObsPersonValueWithEncounterPatient() throws Exception {
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
	public void purgeObs_shouldDeleteAnyObsGroupMembersBeforeDeletingTheObs() throws Exception {
		
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
		Assert.assertNotNull(obsService.getObs(parentObsId));
		Assert.assertNotNull(obsService.getObs(childObsId));
		Assert.assertNotNull(obsService.getObs(unrelatedObsId));
		Assert.assertNotNull(obsService.getObs(orderReferencingObsId));
		Assert.assertNotNull(obsService.getObs(conceptProposalObsId));
		
		Context.getObsService().purgeObs(obs, false);
		
		//	After calling purgeObs method Obs are deleted
		Assert.assertNull(obsService.getObs(parentObsId));
		Assert.assertNull(obsService.getObs(childObsId));
		Assert.assertNotNull(obsService.getObs(unrelatedObsId));
		Assert.assertNull(obsService.getObs(orderReferencingObsId));
		Assert.assertNull(obsService.getObs(conceptProposalObsId));
	}
	
	/**
	 * @see ObsService#purgeObs(Obs,boolean)
	 */
	@Test
	public void purgeObs_shouldNotDeleteReferencedOrdersWhenPurgingObs() throws Exception {
		
		executeDataSet(INITIAL_OBS_XML);
		ObsService obsService = Context.getObsService();
		final OrderService orderService = Context.getOrderService();
		
		final int orderReferencingObsId = 4;
		final Obs obs = obsService.getObs(orderReferencingObsId);
		
		final Order order = obs.getOrder();
		final Integer referencedOrderId = order.getOrderId();
		
		Context.getObsService().purgeObs(obs, false);
		
		Assert.assertNull(obsService.getObs(orderReferencingObsId));
		Assert.assertNotNull(orderService.getOrder(referencedOrderId));
	}
	
	/**
	 * @see ObsService#saveObs(Obs,String)
	 */
	@Test
	public void saveObs_shouldDeleteThePreviousFileWhenAComplexObservationIsUpdatedWithANewComplexValue() throws Exception {
		
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
			
			Assert.assertFalse(createdFile.exists());
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
	public void saveObs_shouldNotVoidAnObsWithNoChanges() throws Exception {
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
			assertFalse("obs"+o.getId(), o.isDirty());
		}

	}

	/**
	 * @see ObsService#saveObs(Obs,String)
	 */
	@Test
	public void saveObs_shouldCopyTheFormNamespaceAndPathFieldInEditedObs() throws Exception {
		executeDataSet(INITIAL_OBS_XML);
		Obs obs = Context.getObsService().getObs(7);
		obs.setValueNumeric(5.0);
		Obs o2 = Context.getObsService().saveObs(obs, "just testing");
		Assert.assertNotNull(obs.getFormFieldNamespace());

		// fetch the obs from the database again
		obs = Context.getObsService().getObs(o2.getObsId());
		Assert.assertNotNull(obs.getFormFieldNamespace());
		Assert.assertNotNull(obs.getFormFieldPath());
	}

	/**
	 * @see ObsService#saveObs(Obs,String)
	 */
	@Test
	public void saveObs_shouldVoidOnlyOldObsWhenAllObsEditedAndNewObsAdded() throws Exception {
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

		Assert.assertEquals(newObs.getObsDatetime().toString(), newDate.toString());

		for(Obs member : newObs.getGroupMembers()) {
			Assert.assertEquals(member.getObsDatetime().toString(), newDate.toString());
			if(member.getGroupMembers()!= null) {

				for (Obs memberChild : member.getGroupMembers()) {
					Assert.assertEquals(memberChild.getObsDatetime().toString(), newDate.toString());
					if (memberChild.getValueText()!= null && memberChild.getValueText().equals("NewObs Value")) {
						count++;
					}
				}
				if (count == 0) {
					Assert.fail("New Obs not created");
				}
			}
		}
	}
}
