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

import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.ObsArchiveHelper;
import org.openmrs.scheduler.tasks.ObsArchivingTaskData;
import org.openmrs.scheduler.tasks.ObsArchivingTaskHandler;
import org.openmrs.test.jupiter.BaseContextSensitiveNonTransactionalTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
	private TransactionManager transactionManager;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private ObsArchiveHelper obsArchiveHelper;

	private List<Integer> createdObsIds = new ArrayList<>();

	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	public void setup() {
		jdbcTemplate = new JdbcTemplate(dataSource);
		try {
			jdbcTemplate.execute("DELETE FROM obs_archive");
			jdbcTemplate.execute("DELETE FROM obs_archive_reference_range");
		} catch (DataAccessException e) {
			// Tables may not exist yet on first run
		}

		adminService.saveGlobalProperty(new GlobalProperty("obs.archive.enabled", "true"));
		adminService.saveGlobalProperty(new GlobalProperty("obs.archive.retention_days", "-1"));
		adminService.saveGlobalProperty(new GlobalProperty("obs.archive.last_processed_obs_id", "-1"));
	}

	private Obs createSingleObs(Double value) {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(7));
		obs.setConcept(Context.getConceptService().getConcept(5089));
		if (value != null)
			obs.setValueNumeric(value);
		obs.setObsDatetime(new java.util.Date());
		obs.setLocation(Context.getLocationService().getLocation(1));
		return obs;
	}

	private Obs createAndSaveSingleObs(Double value) {
		Obs obs = createSingleObs(value);
		obs = obsService.saveObs(obs, "initial save");
		createdObsIds.add(obs.getObsId());
		return obs;
	}

	private Obs createAndSaveObsTree(Double... childValues) {
		Obs parent = new Obs();
		parent.setPerson(Context.getPersonService().getPerson(7));
		parent.setConcept(Context.getConceptService().getConcept(5089));
		parent.setObsDatetime(new java.util.Date());
		parent.setLocation(Context.getLocationService().getLocation(1));

		if (childValues != null) {
			for (Double val : childValues) {
				Obs child = new Obs();
				child.setPerson(Context.getPersonService().getPerson(7));
				child.setConcept(Context.getConceptService().getConcept(5089));
				if (val != null)
					child.setValueNumeric(val);
				child.setObsDatetime(new java.util.Date());
				child.setLocation(Context.getLocationService().getLocation(1));
				parent.addGroupMember(child);
			}
		}
		parent = obsService.saveObs(parent, "save parent with children");
		createdObsIds.add(parent.getObsId());
		for (Obs c : parent.getGroupMembers(true)) {
			createdObsIds.add(c.getObsId());
		}
		return parent;
	}

	private void assertArchived(int obsId) {
		assertTrue(obsArchiveHelper.isArchived(obsId), "obs " + obsId + " should be in archive");
		assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs WHERE obs_id = ?", Integer.class, obsId),
		    "obs " + obsId + " should NOT be in active table");
	}

	private void assertActive(int obsId) {
		assertFalse(obsArchiveHelper.isArchived(obsId), "obs " + obsId + " should NOT be in archive");
		assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs WHERE obs_id = ?", Integer.class, obsId),
		    "obs " + obsId + " should be in active table");
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
			            + "form_namespace_and_path, status, interpretation FROM obs_archive a "
			            + "WHERE NOT EXISTS (SELECT 1 FROM obs o WHERE o.obs_id = a.obs_id)");
			jdbcTemplate.execute("DELETE FROM obs_archive_reference_range");
			jdbcTemplate.execute("DELETE FROM obs_archive");
		} catch (DataAccessException e) {
			// Best-effort cleanup
		}

		for (Integer id : createdObsIds) {
			try {
				jdbcTemplate.update("UPDATE obs SET obs_group_id = NULL WHERE obs_group_id = ?", id);
				jdbcTemplate.update("UPDATE obs SET previous_version = NULL WHERE previous_version = ?", id);
			} catch (DataAccessException e) {
				// Best-effort cleanup
			}
		}

		for (Integer id : createdObsIds) {
			try {
				jdbcTemplate.update("DELETE FROM obs_archive_reference_range WHERE obs_id = ?", id);
				jdbcTemplate.update("DELETE FROM obs_archive WHERE obs_id = ?", id);
				jdbcTemplate.update("DELETE FROM obs_reference_range WHERE obs_id = ?", id);
				jdbcTemplate.update("DELETE FROM obs WHERE obs_id = ?", id);
			} catch (DataAccessException e) {
				// Best-effort cleanup
			}
		}
		createdObsIds.clear();
	}

	@Test
	public void unvoidObs_shouldRestoreFromArchive() throws Exception {
		Obs obs = createAndSaveSingleObs(50.0);
		int testObsId = obs.getObsId();

		// Void the observation via the service API
		obsService.voidObs(obs, "test voiding");
		Context.flushSession();
		Context.clearSession();

		// Run archiving
		ObsArchivingTaskHandler archivingTaskHandler = new ObsArchivingTaskHandler(sessionFactory, transactionManager);
		archivingTaskHandler.execute(new ObsArchivingTaskData(), null);

		assertArchived(testObsId);

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
		assertActive(testObsId);

		Obs activeObs = obsService.getObs(testObsId);
		assertNotNull(activeObs);
		assertFalse(activeObs.getVoided(), "Unvoided obs should not be voided");
	}

	@Test
	public void saveObs_shouldRestoreFromArchive() throws Exception {
		Obs obs = createAndSaveSingleObs(100.0);
		int testObsId = obs.getObsId();

		// Void the observation via the service API
		obsService.voidObs(obs, "test voiding");
		Context.flushSession();
		Context.clearSession();

		// Run archiving
		ObsArchivingTaskHandler archivingTaskHandler = new ObsArchivingTaskHandler(sessionFactory, transactionManager);
		archivingTaskHandler.execute(new ObsArchivingTaskData(), null);

		assertArchived(testObsId);

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
		assertActive(testObsId);

		Obs activeObs = obsService.getObs(testObsId);
		assertNotNull(activeObs);
		assertFalse(activeObs.getVoided());
	}

	@Test
	public void unvoidObs_shouldRestoreWithoutPullingBackPreviousVersionsButKeepPreviousVersionIntact() throws Exception {
		// 1. Create a chain of edits: obs1 -> obs2 -> obs3
		Obs obs1 = createSingleObs(10.0);
		obs1 = obsService.saveObs(obs1, "obs1");
		Integer id1 = obs1.getObsId();
		createdObsIds.add(id1);

		Obs obs2 = createSingleObs(20.0);
		obs2.setPreviousVersion(obs1);
		obs2 = obsService.saveObs(obs2, "obs2");
		Integer id2 = obs2.getObsId();
		createdObsIds.add(id2);

		Obs obs3 = createSingleObs(30.0);
		obs3.setPreviousVersion(obs2);
		obs3 = obsService.saveObs(obs3, "obs3");
		Integer id3 = obs3.getObsId();
		createdObsIds.add(id3);

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
		assertArchived(id1);
		assertArchived(id2);
		assertActive(id3);

		// 3. Verify active obs3's previousVersion (obs2) proxy resolves to null since it's archived
		Context.clearSession();
		Obs activeObs3 = obsService.getObs(id3);
		assertNotNull(activeObs3);
		Obs prevOfObs3 = activeObs3.getPreviousVersion();
		assertNull(prevOfObs3, "Previous version should not be transparently fetched from archive anymore");

		Obs fetchedPrev = obsService.getObs(activeObs3.getPreviousVersionId());
		assertNotNull(fetchedPrev);
		assertEquals(id2, fetchedPrev.getObsId());
		assertTrue(fetchedPrev.getVoided());

		// 4. Unvoid obs2 (the previous version of obs3)
		Context.clearSession();
		Obs archivedObs2 = obsService.getObs(id2);
		assertNotNull(archivedObs2);
		obsService.unvoidObs(archivedObs2);
		Context.flushSession();
		Context.clearSession();

		// 5. Verify obs2 was restored to active obs table
		assertActive(id2);

		// 6. Verify obs1 was NOT pulled back to active obs table (it remains archived)
		assertArchived(id1);

		// 7. Verify obs2's previousVersion (obs1) proxy resolves to null since it's archived
		Obs restoredObs2 = obsService.getObs(id2);
		assertNotNull(restoredObs2);
		assertFalse(restoredObs2.getVoided());
		Obs prevOfObs2 = restoredObs2.getPreviousVersion();
		assertNull(prevOfObs2, "Previous version of restored obs should not be transparently fetched from archive");

		Obs fetchedPrevOfObs2 = obsService.getObs(restoredObs2.getPreviousVersionId());
		assertNotNull(fetchedPrevOfObs2);
		assertEquals(id1, fetchedPrevOfObs2.getObsId());
		assertTrue(fetchedPrevOfObs2.getVoided());

	}

	@Test
	public void unvoidObs_shouldRestoreParentAndItsGroupMembersFromArchive() throws Exception {
		// 1. Create a parent obs and a child obs
		Obs parent = createAndSaveObsTree(42.0);
		Integer parentId = parent.getObsId();
		Integer childId = parent.getGroupMembers(true).iterator().next().getObsId();

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
		assertArchived(parentId);
		assertArchived(childId);

		// 4. Unvoid the parent observation from archive
		Context.clearSession();
		Obs archivedParent = obsService.getObs(parentId);
		assertNotNull(archivedParent);

		obsService.unvoidObs(archivedParent);
		Context.flushSession();
		Context.clearSession();

		// 5. Verify both are restored and unvoided
		assertActive(parentId);
		assertActive(childId);

		Obs restoredParent = obsService.getObs(parentId);
		assertNotNull(restoredParent);
		assertFalse(restoredParent.getVoided());

		Obs restoredChild = obsService.getObs(childId);
		assertNotNull(restoredChild);
		assertFalse(restoredChild.getVoided());

	}

	@Test
	public void saveObs_shouldRestoreParentAndItsGroupMembersFromArchive() throws Exception {
		// 1. Create a parent group observation and a child observation
		Obs parent = createAndSaveObsTree(42.0);
		Integer parentId = parent.getObsId();
		Integer childId = parent.getGroupMembers(true).iterator().next().getObsId();

		// 2. Void parent (this automatically voids children too)
		obsService.voidObs(parent, "void parent");
		Context.flushSession();
		Context.clearSession();

		// 3. Run archiving - moves both parent and child to obs_archive
		ObsArchivingTaskHandler archivingTaskHandler = new ObsArchivingTaskHandler(sessionFactory, transactionManager);
		archivingTaskHandler.execute(new ObsArchivingTaskData(), null);

		// Verify both are in archive
		assertArchived(parentId);
		assertArchived(childId);

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
		assertActive(parentId);
		assertActive(childId);

		Obs restoredParent = obsService.getObs(parentId);
		assertNotNull(restoredParent);
		assertFalse(restoredParent.getVoided());

		Obs restoredChild = obsService.getObs(childId);
		assertNotNull(restoredChild);
		assertFalse(restoredChild.getVoided());

	}

	@Test
	public void unvoidObs_shouldNotRestoreGroupMembersVoidedAtDifferentTime() throws Exception {
		// 1. Create parent and two children
		Obs parent = createAndSaveObsTree(11.0, 22.0);
		Integer parentId = parent.getObsId();
		Integer child1Id = null;
		Integer child2Id = null;

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
		obsService.voidObs(obsService.getObs(child1Id), "void child1 individually");
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
		assertArchived(parentId);
		assertArchived(child1Id);
		assertArchived(child2Id);

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
		assertActive(parentId);
		assertActive(child2Id);
		assertArchived(child1Id);

		// Verify table states
		assertNotNull(obsService.getObs(parentId));
		assertFalse(obsService.getObs(parentId).getVoided());
		assertNotNull(obsService.getObs(child2Id));
		assertFalse(obsService.getObs(child2Id).getVoided());

	}

	@Test
	public void unvoidObs_shouldRestoreTreeButOnlyUnvoidChildWhenSingleArchivedChildIsUnvoided() throws Exception {
		// 1. Create parent and two children
		Obs parent = createAndSaveObsTree(11.0, 22.0);
		Integer parentId = parent.getObsId();
		Integer child1Id = null;
		Integer child2Id = null;

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
		assertArchived(parentId);
		assertArchived(child1Id);
		assertArchived(child2Id);

		// 4. Load ONLY child1 and unvoid it
		Context.clearSession();
		Obs archivedChild = obsService.getObs(child1Id);
		assertNotNull(archivedChild);

		Obs unvoidedChild = obsService.unvoidObs(archivedChild);
		Context.flushSession();
		Context.clearSession();

		Integer newChildId = unvoidedChild.getObsId();
		assertEquals(child1Id, newChildId, "Unvoiding an archived child should update the same row, not create a new one");

		// 5. Verify ALL old rows are restored from archive
		assertActive(parentId);
		assertActive(child1Id);
		assertArchived(child2Id);

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

		// Add any new row IDs to createdObsIds for cleanup
		createdObsIds.addAll(jdbcTemplate.query("SELECT obs_id FROM obs WHERE previous_version IN (?, ?, ?)",
		    (rs, rowNum) -> rs.getInt("obs_id"), parentId, child1Id, child2Id));
		// Nullify foreign keys to avoid constraint violations during cleanup
		jdbcTemplate.update(
		    "UPDATE obs SET obs_group_id = NULL, previous_version = NULL WHERE obs_group_id IN (?, ?, ?) OR previous_version IN (?, ?, ?)",
		    parentId, child1Id, child2Id, parentId, child1Id, child2Id);
	}

	@Test
	public void archiveAndRestore_shouldExecuteWithoutErrors() throws Exception {
		Obs obs = createAndSaveSingleObs(50.0);
		int testObsId = obs.getObsId();

		// Void the observation
		obsService.voidObs(obs, "test voiding");
		Context.flushSession();
		Context.clearSession();

		// Run archiving
		ObsArchivingTaskHandler archivingTaskHandler = new ObsArchivingTaskHandler(sessionFactory, transactionManager);
		archivingTaskHandler.execute(new ObsArchivingTaskData(), null);

		// Verify columns are archived in DB table
		assertArchived(testObsId);

		// Verify transparent retrieval via service
		Context.clearSession();
		Obs archivedObs = obsService.getObs(testObsId);
		assertNotNull(archivedObs);

		// Unvoid the observation to trigger restore
		obsService.unvoidObs(archivedObs);
		Context.flushSession();
		Context.clearSession();

		// Verify restored columns in active table and entity
		assertActive(testObsId);
	}
}
