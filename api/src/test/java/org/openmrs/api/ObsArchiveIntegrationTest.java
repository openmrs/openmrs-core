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

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.ObsArchivingTaskData;
import org.openmrs.scheduler.tasks.ObsArchivingTaskHandler;
import org.openmrs.test.jupiter.BaseContextSensitiveNonTransactionalTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the obs archiving and restoration flow.
 */
public class ObsArchiveIntegrationTest extends BaseContextSensitiveNonTransactionalTest {

	@Autowired
	private ObsService obsService;

	@Autowired
	private AdministrationService adminService;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private org.springframework.transaction.TransactionManager transactionManager;

	@Autowired
	private org.hibernate.SessionFactory sessionFactory;

	private JdbcTemplate jdbcTemplate;

	private Integer testObsId;

	@BeforeEach
	public void setup() {
		jdbcTemplate = new JdbcTemplate(dataSource);
		try {
			jdbcTemplate.execute("DELETE FROM obs_archive");
			jdbcTemplate.execute("DELETE FROM obs_archive_reference_range");
		} catch (Exception e) {
			// Tables may not exist yet on first run
		}
		try {
			// Hibernate omits this column due to @MapsId, but archiving query expects it
			jdbcTemplate.execute(
			    "ALTER TABLE obs_reference_range ADD COLUMN IF NOT EXISTS obs_reference_range_id INT AUTO_INCREMENT");
		} catch (Exception e) {
			// Ignore
		}
		adminService.saveGlobalProperty(new GlobalProperty("obs.archive.enabled", "true"));
		adminService.saveGlobalProperty(new GlobalProperty("obs.archive.retention_days", "-1"));
		adminService.saveGlobalProperty(new GlobalProperty("obs.archive.last_processed_obs_id", "-1"));
	}

	@AfterEach
	public void cleanup() {
		// Restore any archived obs back and clean up since there is no auto-rollback
		try {
			jdbcTemplate.execute(
			    "INSERT INTO obs (obs_id, person_id, concept_id, encounter_id, order_id, obs_datetime, location_id, "
			            + "obs_group_id, accession_number, value_group_id, value_coded, value_coded_name_id, value_drug, "
			            + "value_datetime, value_numeric, value_modifier, value_text, value_complex, comments, creator, "
			            + "date_created, voided, voided_by, date_voided, void_reason, uuid, previous_version, "
			            + "form_namespace_and_path, status, interpretation) "
			            + "SELECT obs_id, person_id, concept_id, encounter_id, order_id, obs_datetime, location_id, "
			            + "obs_group_id, accession_number, value_group_id, value_coded, value_coded_name_id, value_drug, "
			            + "value_datetime, value_numeric, value_modifier, value_text, value_complex, comments, creator, "
			            + "date_created, voided, voided_by, date_voided, void_reason, uuid, previous_version, "
			            + "form_namespace_and_path, status, interpretation FROM obs_archive");
			jdbcTemplate.execute("DELETE FROM obs_archive");
			jdbcTemplate.execute("DELETE FROM obs_archive_reference_range");
		} catch (Exception e) {
			// Best-effort cleanup
		}

		if (testObsId != null) {
			try {
				jdbcTemplate.update("DELETE FROM obs_reference_range WHERE obs_id = ?", testObsId);
				jdbcTemplate.update("DELETE FROM obs WHERE obs_id = ?", testObsId);
			} catch (Exception e) {
				// Best-effort cleanup
			}
			testObsId = null;
		}
	}

	@Test
	public void unvoidObs_shouldRestoreFromArchive() throws Exception {
		// Create a brand new observation via service API (auto-committed)
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(7));
		obs.setConcept(Context.getConceptService().getConcept(5089));
		obs.setValueNumeric(50.0);
		obs.setObsDatetime(new java.util.Date());
		obs.setLocation(Context.getLocationService().getLocation(1));
		obs = obsService.saveObs(obs, "initial save");
		testObsId = obs.getObsId();
		assertNotNull(testObsId, "Obs should be saved with an ID");

		// Void the observation via the service API
		obsService.voidObs(obs, "test voiding");
		Context.flushSession();
		Context.clearSession();

		// Run archiving
		ObsArchivingTaskHandler archivingTaskHandler = new ObsArchivingTaskHandler(sessionFactory, transactionManager);
		archivingTaskHandler.execute(new ObsArchivingTaskData(), null);

		Integer archiveCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?",
		    Integer.class, testObsId);
		Integer obsCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs WHERE obs_id = ?", Integer.class,
		    testObsId);

		assertEquals(1, archiveCount, "Obs should be in obs_archive after archiving");
		assertEquals(0, obsCount, "Obs should be removed from obs after archiving");

		// Verify transparent retrieval from archive still works
		Context.clearSession();
		Obs archivedObs = obsService.getObs(testObsId);
		assertNotNull(archivedObs, "getObs should transparently retrieve from archive");
		assertTrue(archivedObs.getVoided(), "Archived obs should still be voided");

		Obs archivedObsByUuid = obsService.getObsByUuid(archivedObs.getUuid());
		assertNotNull(archivedObsByUuid, "getObsByUuid should transparently retrieve from archive");
		assertEquals(archivedObs.getObsId(), archivedObsByUuid.getObsId());

		// Unvoid the observation — this should restore it from archive
		obsService.unvoidObs(archivedObs);
		Context.flushSession();
		Context.clearSession();

		// Verify it was restored back to the obs table
		archiveCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class,
		    testObsId);
		assertEquals(0, archiveCount, "Obs should be removed from obs_archive after unvoiding");

		Integer countActive = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs WHERE obs_id = ?", Integer.class,
		    testObsId);
		assertEquals(1, countActive, "Obs should be restored to obs after unvoiding");

		Obs activeObs = obsService.getObs(testObsId);
		assertNotNull(activeObs);
		assertFalse(activeObs.getVoided(), "Unvoided obs should not be voided");
	}

	@Test
	public void saveObs_shouldRestoreFromArchive() throws Exception {
		// Create a brand new observation via service API (auto-committed)
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(7));
		obs.setConcept(Context.getConceptService().getConcept(5089));
		obs.setValueNumeric(100.0);
		obs.setObsDatetime(new java.util.Date());
		obs.setLocation(Context.getLocationService().getLocation(1));
		obs = obsService.saveObs(obs, "initial save");
		testObsId = obs.getObsId();
		assertNotNull(testObsId, "Obs should be saved with an ID");

		// Void the observation via the service API
		obsService.voidObs(obs, "test voiding");
		Context.flushSession();
		Context.clearSession();

		// Run archiving
		ObsArchivingTaskHandler archivingTaskHandler = new ObsArchivingTaskHandler(sessionFactory, transactionManager);
		archivingTaskHandler.execute(new ObsArchivingTaskData(), null);

		Integer archiveCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?",
		    Integer.class, testObsId);
		assertEquals(1, archiveCount, "Obs should be in obs_archive after archiving");

		// Get the archived observation
		Obs archivedObs = obsService.getObs(testObsId);
		assertNotNull(archivedObs);
		assertTrue(archivedObs.getVoided());

		// Modify voided to false and call saveObs directly (simulating the REST controller update method)
		archivedObs.setVoided(false);
		archivedObs.setVoidedBy(null);
		archivedObs.setDateVoided(null);
		archivedObs.setVoidReason(null);
		obsService.saveObs(archivedObs, "REST update unvoid");
		Context.flushSession();
		Context.clearSession();

		// Verify it was restored back to the obs table
		archiveCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class,
		    testObsId);
		assertEquals(0, archiveCount, "Obs should be removed from obs_archive after saving with voided=false");

		Integer countActive = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs WHERE obs_id = ?", Integer.class,
		    testObsId);
		assertEquals(1, countActive, "Obs should be restored to obs after saving with voided=false");

		Obs activeObs = obsService.getObs(testObsId);
		assertNotNull(activeObs);
		assertFalse(activeObs.getVoided());
	}

	@Test
	public void unvoidObs_shouldRestoreWithoutPullingBackPreviousVersionsButKeepPreviousVersionIntact() throws Exception {
		Integer id1 = null;
		Integer id2 = null;
		Integer id3 = null;
		try {
			// 1. Create a chain of edits: obs1 -> obs2 -> obs3
			Obs obs1 = new Obs();
			obs1.setPerson(Context.getPersonService().getPerson(7));
			obs1.setConcept(Context.getConceptService().getConcept(5089));
			obs1.setValueNumeric(10.0);
			obs1.setObsDatetime(new java.util.Date());
			obs1.setLocation(Context.getLocationService().getLocation(1));
			obs1 = obsService.saveObs(obs1, "obs1");
			id1 = obs1.getObsId();

			Obs obs2 = new Obs();
			obs2.setPerson(Context.getPersonService().getPerson(7));
			obs2.setConcept(Context.getConceptService().getConcept(5089));
			obs2.setValueNumeric(20.0);
			obs2.setObsDatetime(new java.util.Date());
			obs2.setLocation(Context.getLocationService().getLocation(1));
			obs2.setPreviousVersion(obs1);
			obs2 = obsService.saveObs(obs2, "obs2");
			id2 = obs2.getObsId();

			Obs obs3 = new Obs();
			obs3.setPerson(Context.getPersonService().getPerson(7));
			obs3.setConcept(Context.getConceptService().getConcept(5089));
			obs3.setValueNumeric(30.0);
			obs3.setObsDatetime(new java.util.Date());
			obs3.setLocation(Context.getLocationService().getLocation(1));
			obs3.setPreviousVersion(obs2);
			obs3 = obsService.saveObs(obs3, "obs3");
			id3 = obs3.getObsId();

			// Explicitly void obs1 and obs2 so they are eligible for archiving
			obsService.voidObs(obs1, "replaced");
			obsService.voidObs(obs2, "replaced");

			Context.flushSession();
			Context.clearSession();

			// obs1 and obs2 are voided (replaced by newer versions), obs3 is active.
			assertTrue(obsService.getObs(id1).getVoided());
			assertTrue(obsService.getObs(id2).getVoided());
			assertFalse(obsService.getObs(id3).getVoided());

			// 2. Run archiving - should move voided (obs1, obs2) to archive
			ObsArchivingTaskHandler archivingTaskHandler = new ObsArchivingTaskHandler(sessionFactory, transactionManager);
			archivingTaskHandler.execute(new ObsArchivingTaskData(), null);

			// Verify archiving succeeded
			assertEquals(1,
			    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class, id1));
			assertEquals(1,
			    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class, id2));
			assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs WHERE obs_id = ?", Integer.class, id1));
			assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs WHERE obs_id = ?", Integer.class, id2));
			assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs WHERE obs_id = ?", Integer.class, id3));

			// 3. Verify active obs3's previousVersion (obs2) is still transparently accessible and not null
			Context.clearSession();
			Obs activeObs3 = obsService.getObs(id3);
			assertNotNull(activeObs3);
			Obs prevOfObs3 = activeObs3.getPreviousVersion();
			assertNotNull(prevOfObs3, "Previous version should be transparently fetched from archive");
			assertEquals(id2, prevOfObs3.getObsId());
			assertTrue(prevOfObs3.getVoided());

			// 4. Unvoid obs2 (the previous version of obs3)
			Context.clearSession();
			Obs archivedObs2 = obsService.getObs(id2);
			assertNotNull(archivedObs2);
			obsService.unvoidObs(archivedObs2);
			Context.flushSession();
			Context.clearSession();

			// 5. Verify obs2 was restored to active obs table
			assertEquals(0,
			    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class, id2));
			assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs WHERE obs_id = ?", Integer.class, id2));

			// 6. Verify obs1 was NOT pulled back to active obs table (it remains archived)
			assertEquals(1,
			    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class, id1));
			assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs WHERE obs_id = ?", Integer.class, id1));

			// 7. Verify obs2's previousVersion (obs1) is still transparently accessible and not null
			Obs restoredObs2 = obsService.getObs(id2);
			assertNotNull(restoredObs2);
			assertFalse(restoredObs2.getVoided());
			Obs prevOfObs2 = restoredObs2.getPreviousVersion();
			assertNotNull(prevOfObs2, "Previous version of restored obs should be transparently fetched from archive");
			assertEquals(id1, prevOfObs2.getObsId());
			assertTrue(prevOfObs2.getVoided());
		} finally {
			// Clean up database rows created by this test (guaranteed to execute even on assertion failure)
			if (id1 != null) {
				jdbcTemplate.update("DELETE FROM obs_reference_range WHERE obs_id = ?", id1);
				jdbcTemplate.update("DELETE FROM obs_archive_reference_range WHERE obs_id = ?", id1);
				jdbcTemplate.update("DELETE FROM obs WHERE obs_id = ?", id1);
				jdbcTemplate.update("DELETE FROM obs_archive WHERE obs_id = ?", id1);
			}
			if (id2 != null) {
				jdbcTemplate.update("DELETE FROM obs_reference_range WHERE obs_id = ?", id2);
				jdbcTemplate.update("DELETE FROM obs_archive_reference_range WHERE obs_id = ?", id2);
				jdbcTemplate.update("DELETE FROM obs WHERE obs_id = ?", id2);
				jdbcTemplate.update("DELETE FROM obs_archive WHERE obs_id = ?", id2);
			}
			if (id3 != null) {
				jdbcTemplate.update("DELETE FROM obs_reference_range WHERE obs_id = ?", id3);
				jdbcTemplate.update("DELETE FROM obs_archive_reference_range WHERE obs_id = ?", id3);
				jdbcTemplate.update("DELETE FROM obs WHERE obs_id = ?", id3);
				jdbcTemplate.update("DELETE FROM obs_archive WHERE obs_id = ?", id3);
			}
		}
	}

	@Test
	public void unvoidObs_shouldRestoreParentAndItsGroupMembersFromArchive() throws Exception {
		Integer parentId = null;
		Integer childId = null;
		try {
			// 1. Create a parent obs and a child obs
			Obs parent = new Obs();
			parent.setPerson(Context.getPersonService().getPerson(7));
			parent.setConcept(Context.getConceptService().getConcept(5089)); // Just any concept
			parent.setObsDatetime(new java.util.Date());
			parent.setLocation(Context.getLocationService().getLocation(1));

			Obs child = new Obs();
			child.setPerson(Context.getPersonService().getPerson(7));
			child.setConcept(Context.getConceptService().getConcept(5089));
			child.setValueNumeric(42.0);
			child.setObsDatetime(new java.util.Date());
			child.setLocation(Context.getLocationService().getLocation(1));

			parent.addGroupMember(child);

			parent = obsService.saveObs(parent, "save parent with child");
			parentId = parent.getObsId();

			// Find the saved child ID
			childId = parent.getGroupMembers(true).iterator().next().getObsId();

			// 2. Void parent (this automatically voids children too)
			obsService.voidObs(parent, "void parent");
			Context.flushSession();
			Context.clearSession();

			assertTrue(obsService.getObs(parentId).getVoided());
			assertTrue(obsService.getObs(childId).getVoided());

			// 3. Run archiving - moves both parent and child to obs_archive
			ObsArchivingTaskHandler archivingTaskHandler = new ObsArchivingTaskHandler(sessionFactory, transactionManager);
			archivingTaskHandler.execute(new ObsArchivingTaskData(), null);

			// Verify both are in archive
			assertEquals(1,
			    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class, parentId));
			assertEquals(1,
			    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class, childId));
			assertEquals(0,
			    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs WHERE obs_id = ?", Integer.class, parentId));
			assertEquals(0,
			    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs WHERE obs_id = ?", Integer.class, childId));

			// 4. Unvoid the parent observation from archive
			Context.clearSession();
			Obs archivedParent = obsService.getObs(parentId);
			assertNotNull(archivedParent);

			obsService.unvoidObs(archivedParent);
			Context.flushSession();
			Context.clearSession();

			// 5. Verify both are restored and unvoided
			assertEquals(0,
			    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class, parentId));
			assertEquals(0,
			    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class, childId));

			Obs restoredParent = obsService.getObs(parentId);
			assertNotNull(restoredParent);
			assertFalse(restoredParent.getVoided());

			Obs restoredChild = obsService.getObs(childId);
			assertNotNull(restoredChild);
			assertFalse(restoredChild.getVoided());

		} finally {
			if (childId != null) {
				jdbcTemplate.update("DELETE FROM obs_reference_range WHERE obs_id = ?", childId);
				jdbcTemplate.update("DELETE FROM obs WHERE obs_id = ?", childId);
				jdbcTemplate.update("DELETE FROM obs_archive WHERE obs_id = ?", childId);
			}
			if (parentId != null) {
				jdbcTemplate.update("DELETE FROM obs_reference_range WHERE obs_id = ?", parentId);
				jdbcTemplate.update("DELETE FROM obs WHERE obs_id = ?", parentId);
				jdbcTemplate.update("DELETE FROM obs_archive WHERE obs_id = ?", parentId);
			}
		}
	}

	@Test
	public void saveObs_shouldRestoreParentAndItsGroupMembersFromArchive() throws Exception {
		Integer parentId = null;
		Integer childId = null;
		try {
			// 1. Create a parent group observation and a child observation
			Obs parent = new Obs();
			parent.setPerson(Context.getPersonService().getPerson(7));
			parent.setConcept(Context.getConceptService().getConcept(5089));
			parent.setObsDatetime(new java.util.Date());
			parent.setLocation(Context.getLocationService().getLocation(1));

			Obs child = new Obs();
			child.setPerson(Context.getPersonService().getPerson(7));
			child.setConcept(Context.getConceptService().getConcept(5089));
			child.setValueNumeric(42.0);
			child.setObsDatetime(new java.util.Date());
			child.setLocation(Context.getLocationService().getLocation(1));

			parent.addGroupMember(child);

			parent = obsService.saveObs(parent, "save parent with child");
			parentId = parent.getObsId();

			// Find the saved child ID
			childId = parent.getGroupMembers(true).iterator().next().getObsId();

			// 2. Void parent (this automatically voids children too)
			obsService.voidObs(parent, "void parent");
			Context.flushSession();
			Context.clearSession();

			// 3. Run archiving - moves both parent and child to obs_archive
			ObsArchivingTaskHandler archivingTaskHandler = new ObsArchivingTaskHandler(sessionFactory, transactionManager);
			archivingTaskHandler.execute(new ObsArchivingTaskData(), null);

			// Verify both are in archive
			assertEquals(1,
			    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class, parentId));
			assertEquals(1,
			    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class, childId));

			// 4. Load parent and set voided=false, then call saveObs
			Context.clearSession();
			Obs archivedParent = obsService.getObs(parentId);
			assertNotNull(archivedParent);

			archivedParent.setVoided(false);
			archivedParent.setVoidedBy(null);
			archivedParent.setDateVoided(null);
			archivedParent.setVoidReason(null);

			obsService.saveObs(archivedParent, "REST update unvoid parent group");
			Context.flushSession();
			Context.clearSession();

			// 5. Verify both are restored and unvoided
			assertEquals(0,
			    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class, parentId));
			assertEquals(0,
			    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class, childId));

			Obs restoredParent = obsService.getObs(parentId);
			assertNotNull(restoredParent);
			assertFalse(restoredParent.getVoided());

			Obs restoredChild = obsService.getObs(childId);
			assertNotNull(restoredChild);
			assertFalse(restoredChild.getVoided());

		} finally {
			if (childId != null) {
				jdbcTemplate.update("DELETE FROM obs_reference_range WHERE obs_id = ?", childId);
				jdbcTemplate.update("DELETE FROM obs WHERE obs_id = ?", childId);
				jdbcTemplate.update("DELETE FROM obs_archive WHERE obs_id = ?", childId);
			}
			if (parentId != null) {
				jdbcTemplate.update("DELETE FROM obs_reference_range WHERE obs_id = ?", parentId);
				jdbcTemplate.update("DELETE FROM obs WHERE obs_id = ?", parentId);
				jdbcTemplate.update("DELETE FROM obs_archive WHERE obs_id = ?", parentId);
			}
		}
	}

	@Test
	public void unvoidObs_shouldNotRestoreGroupMembersVoidedAtDifferentTime() throws Exception {
		Integer parentId = null;
		Integer child1Id = null;
		Integer child2Id = null;
		try {
			// 1. Create parent and two children
			Obs parent = new Obs();
			parent.setPerson(Context.getPersonService().getPerson(7));
			parent.setConcept(Context.getConceptService().getConcept(5089));
			parent.setObsDatetime(new java.util.Date());
			parent.setLocation(Context.getLocationService().getLocation(1));

			Obs child1 = new Obs();
			child1.setPerson(Context.getPersonService().getPerson(7));
			child1.setConcept(Context.getConceptService().getConcept(5089));
			child1.setValueNumeric(11.0);
			child1.setObsDatetime(new java.util.Date());
			child1.setLocation(Context.getLocationService().getLocation(1));

			Obs child2 = new Obs();
			child2.setPerson(Context.getPersonService().getPerson(7));
			child2.setConcept(Context.getConceptService().getConcept(5089));
			child2.setValueNumeric(22.0);
			child2.setObsDatetime(new java.util.Date());
			child2.setLocation(Context.getLocationService().getLocation(1));

			parent.addGroupMember(child1);
			parent.addGroupMember(child2);

			parent = obsService.saveObs(parent, "save parent with two children");
			parentId = parent.getObsId();

			// Retrieve saved child IDs
			for (Obs child : parent.getGroupMembers(true)) {
				if (child.getValueNumeric().equals(11.0)) {
					child1Id = child.getObsId();
				} else if (child.getValueNumeric().equals(22.0)) {
					child2Id = child.getObsId();
				}
			}

			assertNotNull(child1Id);
			assertNotNull(child2Id);
			// 2. Void child1 individually first (different time)
			obsService.voidObs(child1, "void child1 individually");
			Context.flushSession();
			Context.clearSession();

			java.util.Date child1Voided = jdbcTemplate.queryForObject("SELECT date_voided FROM obs WHERE obs_id = ?",
			    java.util.Date.class, child1Id);
			java.util.Date differentDate = new java.util.Date(child1Voided.getTime() - 3600000);
			jdbcTemplate.update("UPDATE obs SET date_voided = ? WHERE obs_id = ?", differentDate, child1Id);

			// 3. Void parent (this cascades voiding to child2)
			parent = obsService.getObs(parentId);
			obsService.voidObs(parent, "void parent");
			Context.flushSession();
			Context.clearSession();

			// 4. Run archiving - moves parent, child1, and child2 to obs_archive
			ObsArchivingTaskHandler archivingTaskHandler = new ObsArchivingTaskHandler(sessionFactory, transactionManager);
			archivingTaskHandler.execute(new ObsArchivingTaskData(), null);

			// Verify all 3 are in archive
			assertEquals(1,
			    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class, parentId));
			assertEquals(1,
			    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class, child1Id));
			assertEquals(1,
			    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class, child2Id));

			// 5. Unvoid parent (using saveObs with voided=false to trigger our REST-equivalent flow)
			Context.clearSession();
			Obs archivedParent = obsService.getObs(parentId);
			assertNotNull(archivedParent);

			archivedParent.setVoided(false);
			archivedParent.setVoidedBy(null);
			archivedParent.setDateVoided(null);
			archivedParent.setVoidReason(null);

			obsService.saveObs(archivedParent, "REST unvoid parent");
			Context.flushSession();
			Context.clearSession();

			// 6. Verify parent and child2 (same void date) are restored, but child1 (different void date) remains in archive
			assertEquals(0,
			    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class, parentId));
			assertEquals(0,
			    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class, child2Id));
			assertEquals(1,
			    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class, child1Id)); // Still archived!

			// Verify table states
			assertNotNull(obsService.getObs(parentId));
			assertFalse(obsService.getObs(parentId).getVoided());
			assertNotNull(obsService.getObs(child2Id));
			assertFalse(obsService.getObs(child2Id).getVoided());

			java.util.List<Integer> activeChildIds = jdbcTemplate.query("SELECT obs_id FROM obs WHERE obs_id = ?",
			    (rs, rowNum) -> rs.getInt("obs_id"), child1Id);
			assertTrue(activeChildIds.isEmpty(), "Child1 should not be restored to the active table!");

		} finally {
			if (child1Id != null) {
				jdbcTemplate.update("DELETE FROM obs_reference_range WHERE obs_id = ?", child1Id);
				jdbcTemplate.update("DELETE FROM obs WHERE obs_id = ?", child1Id);
				jdbcTemplate.update("DELETE FROM obs_archive WHERE obs_id = ?", child1Id);
			}
			if (child2Id != null) {
				jdbcTemplate.update("DELETE FROM obs_reference_range WHERE obs_id = ?", child2Id);
				jdbcTemplate.update("DELETE FROM obs WHERE obs_id = ?", child2Id);
				jdbcTemplate.update("DELETE FROM obs_archive WHERE obs_id = ?", child2Id);
			}
			if (parentId != null) {
				jdbcTemplate.update("DELETE FROM obs_reference_range WHERE obs_id = ?", parentId);
				jdbcTemplate.update("DELETE FROM obs WHERE obs_id = ?", parentId);
				jdbcTemplate.update("DELETE FROM obs_archive WHERE obs_id = ?", parentId);
			}
		}
	}

	@Test
	public void unvoidObs_shouldRestoreTreeButOnlyUnvoidChildWhenSingleArchivedChildIsUnvoided() throws Exception {
		Integer parentId = null;
		Integer child1Id = null;
		Integer child2Id = null;
		try {
			// 1. Create parent and two children
			Obs parent = new Obs();
			parent.setPerson(Context.getPersonService().getPerson(7));
			parent.setConcept(Context.getConceptService().getConcept(5089));
			parent.setObsDatetime(new java.util.Date());
			parent.setLocation(Context.getLocationService().getLocation(1));

			Obs child1 = new Obs();
			child1.setPerson(Context.getPersonService().getPerson(7));
			child1.setConcept(Context.getConceptService().getConcept(5089));
			child1.setValueNumeric(11.0);
			child1.setObsDatetime(new java.util.Date());
			child1.setLocation(Context.getLocationService().getLocation(1));

			Obs child2 = new Obs();
			child2.setPerson(Context.getPersonService().getPerson(7));
			child2.setConcept(Context.getConceptService().getConcept(5089));
			child2.setValueNumeric(22.0);
			child2.setObsDatetime(new java.util.Date());
			child2.setLocation(Context.getLocationService().getLocation(1));

			parent.addGroupMember(child1);
			parent.addGroupMember(child2);

			parent = obsService.saveObs(parent, "save parent with two children");
			parentId = parent.getObsId();

			for (Obs child : parent.getGroupMembers(true)) {
				if (child.getValueNumeric().equals(11.0)) {
					child1Id = child.getObsId();
				} else if (child.getValueNumeric().equals(22.0)) {
					child2Id = child.getObsId();
				}
			}

			// 2. Void parent (this automatically voids children too)
			obsService.voidObs(parent, "void parent");
			Context.flushSession();
			Context.clearSession();

			// 3. Run archiving - moves all to obs_archive
			ObsArchivingTaskHandler archivingTaskHandler = new ObsArchivingTaskHandler(sessionFactory, transactionManager);
			archivingTaskHandler.execute(new ObsArchivingTaskData(), null);

			// Verify all are in archive
			assertEquals(1,
			    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class, parentId));
			assertEquals(1,
			    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class, child1Id));
			assertEquals(1,
			    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class, child2Id));

			// 4. Load ONLY child1 and unvoid it
			Context.clearSession();
			Obs archivedChild = obsService.getObs(child1Id);
			assertNotNull(archivedChild);

			Obs unvoidedChild = obsService.unvoidObs(archivedChild);
			Context.flushSession();
			Context.clearSession();

			Integer newChildId = unvoidedChild.getObsId();
			assertEquals(child1Id, newChildId,
			    "Unvoiding an archived child should update the same row, not create a new one");

			// 5. Verify ALL old rows are restored from archive
			assertEquals(0,
			    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class, parentId));
			assertEquals(0,
			    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class, child1Id));
			assertEquals(1,
			    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class, child2Id));

			// 6. Verify the new child is unvoided. The old child1, parent and child2 should still be voided=true.
			Obs restoredNewChild = obsService.getObs(newChildId);
			assertNotNull(restoredNewChild);
			assertFalse(restoredNewChild.getVoided());

			Obs restoredParent = obsService.getObs(parentId);
			assertNotNull(restoredParent);
			assertTrue(restoredParent.getVoided());

			Obs restoredChild2 = obsService.getObs(child2Id);
			assertNotNull(restoredChild2);
			assertTrue(restoredChild2.getVoided());

		} finally {
			// Delete any new rows created by unvoiding
			jdbcTemplate.update(
			    "DELETE FROM obs_reference_range WHERE obs_id IN (SELECT obs_id FROM obs WHERE previous_version IN (?, ?, ?))",
			    parentId, child1Id, child2Id);
			jdbcTemplate.update("DELETE FROM obs WHERE previous_version IN (?, ?, ?)", parentId, child1Id, child2Id);

			// Nullify foreign keys to avoid constraint violations during cleanup,
			// especially since unvoiding creates a new row with previous_version
			jdbcTemplate.update(
			    "UPDATE obs SET obs_group_id = NULL, previous_version = NULL WHERE obs_group_id IN (?, ?, ?) OR previous_version IN (?, ?, ?)",
			    parentId, child1Id, child2Id, parentId, child1Id, child2Id);

			if (child1Id != null) {
				jdbcTemplate.update("DELETE FROM obs_reference_range WHERE obs_id = ?", child1Id);
				jdbcTemplate.update("DELETE FROM obs WHERE obs_id = ?", child1Id);
				jdbcTemplate.update("DELETE FROM obs_archive WHERE obs_id = ?", child1Id);
			}
			if (child2Id != null) {
				jdbcTemplate.update("DELETE FROM obs_reference_range WHERE obs_id = ?", child2Id);
				jdbcTemplate.update("DELETE FROM obs WHERE obs_id = ?", child2Id);
				jdbcTemplate.update("DELETE FROM obs_archive WHERE obs_id = ?", child2Id);
			}
			if (parentId != null) {
				jdbcTemplate.update("DELETE FROM obs_reference_range WHERE obs_id = ?", parentId);
				jdbcTemplate.update("DELETE FROM obs WHERE obs_id = ?", parentId);
				jdbcTemplate.update("DELETE FROM obs_archive WHERE obs_id = ?", parentId);
			}
		}
	}
}
