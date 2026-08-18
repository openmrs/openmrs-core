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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.ConceptProposal;
import org.openmrs.Encounter;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.ObsReferenceRange;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.ObsArchiveHelper;
import org.openmrs.scheduler.tasks.ObsArchivingTaskData;
import org.openmrs.scheduler.tasks.ObsArchivingTaskHandler;
import org.openmrs.test.jupiter.BaseContextSensitiveNonTransactionalTest;
import org.openmrs.util.OpenmrsConstants;
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
			jdbcTemplate.execute("DELETE FROM obs_reference_range_archive");
			jdbcTemplate.execute("DELETE FROM outbox_event");
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
		if (value != null) {
			obs.setValueNumeric(value);
		}
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

	private Obs createAndSaveSingleObsWithReferenceRange(Double value) {
		Obs obs = createSingleObs(value);
		ObsReferenceRange range = new ObsReferenceRange();
		range.setHiAbsolute(100.0);
		range.setLowAbsolute(0.0);
		range.setObs(obs);
		obs.setReferenceRange(range);
		obs = obsService.saveObs(obs, "initial save with range");
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
				if (val != null) {
					child.setValueNumeric(val);
				}
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
			            + "WHERE NOT EXISTS (SELECT 1 FROM obs o WHERE o.obs_id = a.obs_id) ORDER BY obs_id ASC");
		} catch (DataAccessException e) {
			// Best-effort cleanup
		}
		try {
			jdbcTemplate.execute("DELETE FROM obs_reference_range_archive");
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
				jdbcTemplate.update("DELETE FROM obs_reference_range_archive WHERE obs_id = ?", id);
				jdbcTemplate.update("DELETE FROM obs_archive WHERE obs_id = ?", id);
				jdbcTemplate.update("DELETE FROM obs_reference_range WHERE obs_id = ?", id);
				jdbcTemplate.update("DELETE FROM obs WHERE obs_id = ?", id);
			} catch (DataAccessException e) {
				// Best-effort cleanup
			}
		}
		createdObsIds.clear();

		GlobalProperty p = adminService.getGlobalPropertyObject("obs.archive.enabled");
		if (p != null)
			adminService.purgeGlobalProperty(p);
		p = adminService.getGlobalPropertyObject("obs.archive.retention_days");
		if (p != null)
			adminService.purgeGlobalProperty(p);
		p = adminService.getGlobalPropertyObject("obs.archive.last_processed_obs_id");
		if (p != null)
			adminService.purgeGlobalProperty(p);

		Context.getUserService().removeUserProperty(Context.getAuthenticatedUser(),
		    OpenmrsConstants.USER_PROPERTY_LAST_LOGIN_TIMESTAMP);
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

		Context.clearSession();

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
		Context.clearSession();
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
	public void unvoidObs_shouldRestoreThreeLevelTreeFromArchive() throws Exception {
		// 1. Create a parent obs, a child obs, and a grandchild obs
		Obs parent = createSingleObs(null);
		Obs child = createSingleObs(null);
		Obs grandchild = createSingleObs(30.0);

		child.addGroupMember(grandchild);
		parent.addGroupMember(child);

		parent = obsService.saveObs(parent, "save 3-level tree");

		// Add to cleanup list
		Integer parentId = parent.getObsId();
		Integer childId = parent.getGroupMembers(true).iterator().next().getObsId();
		Integer grandchildId = parent.getGroupMembers(true).iterator().next().getGroupMembers(true).iterator().next()
		        .getObsId();
		createdObsIds.add(parentId);
		createdObsIds.add(childId);
		createdObsIds.add(grandchildId);

		// 2. Void parent (this automatically voids children and grandchildren too)
		obsService.voidObs(parent, "void parent");
		Context.flushSession();
		Context.clearSession();

		assertTrue(obsService.getObs(parentId).getVoided());
		assertTrue(obsService.getObs(childId).getVoided());
		assertTrue(obsService.getObs(grandchildId).getVoided());

		// 3. Run archiving - moves all to obs_archive
		ObsArchivingTaskHandler archivingTaskHandler = new ObsArchivingTaskHandler(sessionFactory, transactionManager);
		archivingTaskHandler.execute(new ObsArchivingTaskData(), null);

		// Verify all are in archive
		assertArchived(parentId);
		assertArchived(childId);
		assertArchived(grandchildId);

		// 4. Unvoid the parent observation from archive
		Context.clearSession();
		Obs archivedParent = obsService.getObs(parentId);
		assertNotNull(archivedParent);

		obsService.unvoidObs(archivedParent);
		Context.flushSession();
		Context.clearSession();

		// 5. Verify all are restored and unvoided
		assertActive(parentId);
		assertActive(childId);
		assertActive(grandchildId);

		Obs restoredParent = obsService.getObs(parentId);
		assertNotNull(restoredParent);
		assertFalse(restoredParent.getVoided());

		Obs restoredChild = obsService.getObs(childId);
		assertNotNull(restoredChild);
		assertFalse(restoredChild.getVoided());

		Obs restoredGrandchild = obsService.getObs(grandchildId);
		assertNotNull(restoredGrandchild);
		assertFalse(restoredGrandchild.getVoided());
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

	@Test
	public void archiveAndRestore_shouldPreserveFormNamespaceAndPath() throws Exception {
		Obs obs = createSingleObs(50.0);
		String formPath = "htmlformentry^path/to/form";
		obs.setFormNamespaceAndPath(formPath);
		obs = obsService.saveObs(obs, "initial save");
		createdObsIds.add(obs.getObsId());
		int testObsId = obs.getObsId();

		// Void the observation
		obsService.voidObs(obs, "test voiding");
		Context.flushSession();
		Context.clearSession();

		// Run archiving
		ObsArchivingTaskHandler archivingTaskHandler = new ObsArchivingTaskHandler(sessionFactory, transactionManager);
		archivingTaskHandler.execute(new ObsArchivingTaskData(), null);

		// Verify archived
		assertArchived(testObsId);

		// Verify transparent retrieval via service retains the path
		Context.clearSession();
		Obs archivedObs = obsService.getObs(testObsId);
		assertNotNull(archivedObs);
		assertEquals(formPath, archivedObs.getFormNamespaceAndPath(), "Archived obs should retain form namespace and path");

		// Unvoid the observation to trigger restore
		obsService.unvoidObs(archivedObs);
		Context.flushSession();
		Context.clearSession();

		// Verify restored in active table and retains the field
		assertActive(testObsId);
		Obs restoredObs = obsService.getObs(testObsId);
		assertNotNull(restoredObs);
		assertEquals(formPath, restoredObs.getFormNamespaceAndPath(), "Restored obs should retain form namespace and path");
	}

	@Test
	public void archiveAndRestore_shouldPreserveReferenceRangeIdAndFields() throws Exception {
		Obs obs = createAndSaveSingleObsWithReferenceRange(50.0);
		int testObsId = obs.getObsId();
		assertNotNull(obs.getReferenceRange());
		ObsReferenceRange originalRange = obs.getReferenceRange();
		int originalRangeId = originalRange.getObsReferenceRangeId();
		Double originalHiAbsolute = originalRange.getHiAbsolute();
		Double originalLowAbsolute = originalRange.getLowAbsolute();
		String originalUuid = originalRange.getUuid();

		// Void the observation
		obsService.voidObs(obs, "test voiding");
		Context.flushSession();
		Context.clearSession();

		// Run archiving
		ObsArchivingTaskHandler archivingTaskHandler = new ObsArchivingTaskHandler(sessionFactory, transactionManager);
		archivingTaskHandler.execute(new ObsArchivingTaskData(), null);

		// Verify archived
		assertArchived(testObsId);

		// Verify transparent retrieval via service retains the reference range and its exact data
		Context.clearSession();
		Obs archivedObs = obsService.getObs(testObsId);
		assertNotNull(archivedObs);
		assertNotNull(archivedObs.getReferenceRange());
		assertEquals(originalRangeId, archivedObs.getReferenceRange().getObsReferenceRangeId(),
		    "Archived obs should retain the exact obs_reference_range_id");
		assertEquals(originalHiAbsolute, archivedObs.getReferenceRange().getHiAbsolute(),
		    "Archived obs should retain the exact hi_absolute");
		assertEquals(originalLowAbsolute, archivedObs.getReferenceRange().getLowAbsolute(),
		    "Archived obs should retain the exact low_absolute");
		assertEquals(originalUuid, archivedObs.getReferenceRange().getUuid(), "Archived obs should retain the exact uuid");

		// Unvoid the observation to trigger restore
		obsService.unvoidObs(archivedObs);
		Context.flushSession();
		Context.clearSession();

		// Verify restored in active table and retains the exact ID and fields
		assertActive(testObsId);
		Obs restoredObs = obsService.getObs(testObsId);
		assertNotNull(restoredObs);
		assertNotNull(restoredObs.getReferenceRange());
		assertEquals(originalRangeId, restoredObs.getReferenceRange().getObsReferenceRangeId(),
		    "Restored obs should retain the exact obs_reference_range_id");
		assertEquals(originalHiAbsolute, restoredObs.getReferenceRange().getHiAbsolute(),
		    "Restored obs should retain the exact hi_absolute");
		assertEquals(originalLowAbsolute, restoredObs.getReferenceRange().getLowAbsolute(),
		    "Restored obs should retain the exact low_absolute");
		assertEquals(originalUuid, restoredObs.getReferenceRange().getUuid(), "Restored obs should retain the exact uuid");

		Integer directDbId = jdbcTemplate.queryForObject(
		    "SELECT obs_reference_range_id FROM obs_reference_range WHERE obs_id = ?", Integer.class, testObsId);
		assertEquals(originalRangeId, directDbId,
		    "Restored obs_reference_range_id should be properly persisted in the DB column");
	}

	@Test
	public void getArchivedObsByEncounterId_shouldReturnArchivedObsAndResolveGroups() throws Exception {
		// 1. Create a parent obs linked to an encounter, and a child obs WITHOUT an explicit encounter.
		// The child must be found via the OR clause in the HQL query (obsGroupId-based union),
		// not via a direct encounter match, to properly exercise the full query.
		Encounter encounter = Context.getEncounterService().getEncounter(3);

		Obs parent = createSingleObs(null);
		parent.setEncounter(encounter);

		Obs child = createSingleObs(42.0);
		// Deliberately NOT setting child.setEncounter(encounter) so the child has no direct
		// encounter link. The query must find it through:
		//   OR a.obsGroupId IN (SELECT oa.obsId FROM ObsArchive oa WHERE oa.encounter.encounterId = :encId)

		parent.addGroupMember(child);

		parent = obsService.saveObs(parent, "save parent with child for encounter");
		Integer parentId = parent.getObsId();
		Integer childId = parent.getGroupMembers(true).iterator().next().getObsId();
		createdObsIds.add(parentId);
		createdObsIds.add(childId);

		// 2. Void parent (this automatically voids children too)
		obsService.voidObs(parent, "void parent");
		Context.flushSession();
		Context.clearSession();

		// 3. Run archiving
		ObsArchivingTaskHandler archivingTaskHandler = new ObsArchivingTaskHandler(sessionFactory, transactionManager);
		archivingTaskHandler.execute(new ObsArchivingTaskData(), null);

		assertArchived(parentId);
		assertArchived(childId);

		// 4. Call getArchivedObsByEncounterId
		List<Obs> archivedObs = obsArchiveHelper.getArchivedObsByEncounterId(encounter.getEncounterId());

		assertNotNull(archivedObs);
		assertTrue(archivedObs.size() >= 2, "Should return at least the parent and child obs");

		Obs returnedParent = null;
		Obs returnedChild = null;
		for (Obs o : archivedObs) {
			if (o.getObsId().equals(parentId)) {
				returnedParent = o;
			} else if (o.getObsId().equals(childId)) {
				returnedChild = o;
			}
		}

		assertNotNull(returnedParent, "Parent should be in the returned list");
		assertNotNull(returnedChild, "Child should be in the returned list (found via obsGroupId union)");

		assertNull(returnedParent.getObsGroup(), "Parent should not have a group");
		assertNotNull(returnedChild.getObsGroup(), "Child should have a group");
		assertEquals(parentId, returnedChild.getObsGroup().getObsId(), "Child's group should be the parent");

		// 5. Verify Encounter.getAllObsIncludingArchived() correctly uses the archive
		Encounter reloadedEncounter = Context.getEncounterService().getEncounter(encounter.getEncounterId());
		java.util.Set<Obs> allObs = reloadedEncounter.getAllObsIncludingArchived();
		assertTrue(allObs.contains(returnedParent), "Parent should be included in Encounter.getAllObsIncludingArchived");
		assertTrue(allObs.contains(returnedChild), "Child should be included in Encounter.getAllObsIncludingArchived");
	}

	@Test
	public void purgeObs_shouldDeleteFromArchiveWhenArchived() throws Exception {
		Obs obs = createAndSaveSingleObs(50.0);
		int testObsId = obs.getObsId();

		// Void the observation
		obsService.voidObs(obs, "test voiding");
		Context.flushSession();
		Context.clearSession();

		// Run archiving
		ObsArchivingTaskHandler archivingTaskHandler = new ObsArchivingTaskHandler(sessionFactory, transactionManager);
		archivingTaskHandler.execute(new ObsArchivingTaskData(), null);

		assertArchived(testObsId);

		Context.clearSession();
		Obs archivedObs = obsService.getObs(testObsId);
		assertNotNull(archivedObs);

		// Purge the archived obs
		obsService.purgeObs(archivedObs);
		Context.flushSession();
		Context.clearSession();

		// Verify it's no longer in archive
		assertFalse(obsArchiveHelper.isArchived(testObsId), "Purged obs should be removed from archive");
		assertEquals(0,
		    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class, testObsId),
		    "obs " + testObsId + " should NOT be in archive table");

		assertNull(obsService.getObs(testObsId), "Purged obs should no longer exist");
	}

	@Test
	public void getArchivedChildObs_shouldReturnArchivedChildren() {
		Obs parent = new Obs();
		parent.setPerson(Context.getPersonService().getPerson(7));
		parent.setConcept(Context.getConceptService().getConcept(30));
		parent.setObsDatetime(new java.util.Date());
		parent.setLocation(Context.getLocationService().getLocation(1));

		Obs child = createSingleObs(10.0);
		parent.addGroupMember(child);
		obsService.saveObs(parent, "saving parent with child");
		Integer parentId = parent.getObsId();
		Integer childId = child.getObsId();
		createdObsIds.add(parentId);
		createdObsIds.add(childId);

		obsService.voidObs(parent, "voiding parent");
		Context.flushSession();
		Context.clearSession();
		ObsArchivingTaskHandler archivingTaskHandler = new ObsArchivingTaskHandler(sessionFactory, transactionManager);
		archivingTaskHandler.execute(new ObsArchivingTaskData(), null);

		assertArchived(parentId);
		assertArchived(childId);

		List<Obs> archivedChildren = obsArchiveHelper.getArchivedChildObs(parentId);
		assertEquals(1, archivedChildren.size());
		assertEquals(childId, archivedChildren.get(0).getObsId());
	}

	@Test
	public void getArchivedObsByPersonId_shouldReturnAllArchivedObsForPerson() {
		Obs obs1 = createAndSaveSingleObs(5.0);
		Obs obs2 = createAndSaveSingleObs(15.0);
		Integer personId = obs1.getPersonId();

		obsService.voidObs(obs1, "voiding");
		obsService.voidObs(obs2, "voiding");
		Context.flushSession();
		Context.clearSession();
		Context.flushSession();
		Context.clearSession();
		ObsArchivingTaskHandler archivingTaskHandler = new ObsArchivingTaskHandler(sessionFactory, transactionManager);
		archivingTaskHandler.execute(new ObsArchivingTaskData(), null);

		List<Obs> personObs = obsArchiveHelper.getArchivedObsByPersonId(personId);
		assertTrue(personObs.stream().anyMatch(o -> o.getObsId().equals(obs1.getObsId())));
		assertTrue(personObs.stream().anyMatch(o -> o.getObsId().equals(obs2.getObsId())));
	}

	@Test
	public void getArchivedObsByPersonIdAndConceptId_shouldReturnMatchingArchivedObs() {
		Obs obs1 = createAndSaveSingleObs(5.0);

		Obs obs2 = createSingleObs(15.0);
		obs2.setConcept(Context.getConceptService().getConcept(5497));
		obs2 = obsService.saveObs(obs2, "different concept");
		createdObsIds.add(obs2.getObsId());

		Integer personId = obs1.getPersonId();
		Integer conceptId1 = obs1.getConcept().getConceptId();
		Integer conceptId2 = obs2.getConcept().getConceptId();

		obsService.voidObs(obs1, "voiding");
		obsService.voidObs(obs2, "voiding");
		Context.flushSession();
		Context.clearSession();
		ObsArchivingTaskHandler archivingTaskHandler = new ObsArchivingTaskHandler(sessionFactory, transactionManager);
		archivingTaskHandler.execute(new ObsArchivingTaskData(), null);

		List<Obs> concept1Obs = obsArchiveHelper.getArchivedObsByPersonIdAndConceptId(personId, conceptId1);
		List<Obs> concept2Obs = obsArchiveHelper.getArchivedObsByPersonIdAndConceptId(personId, conceptId2);

		Integer obs1Id = obs1.getObsId();
		Integer obs2Id = obs2.getObsId();

		assertTrue(concept1Obs.stream().anyMatch(o -> o.getObsId().equals(obs1Id)));
		assertTrue(concept2Obs.stream().anyMatch(o -> o.getObsId().equals(obs2Id)));
	}

	@Test
	public void purgeObs_shouldDeleteArchivedObsWithReferenceRange() throws Exception {
		// Create obs WITH a reference range, so both obs_archive and obs_reference_range_archive rows exist
		Obs obs = createAndSaveSingleObsWithReferenceRange(72.5);
		int testObsId = obs.getObsId();

		// Void and archive it
		obsService.voidObs(obs, "test voiding");
		Context.flushSession();
		Context.clearSession();

		ObsArchivingTaskHandler archivingTaskHandler = new ObsArchivingTaskHandler(sessionFactory, transactionManager);
		archivingTaskHandler.execute(new ObsArchivingTaskData(), null);

		assertArchived(testObsId);

		// Confirm reference range was also archived
		assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_reference_range_archive WHERE obs_id = ?",
		    Integer.class, testObsId), "reference range should be in archive");

		Context.clearSession();
		Obs archivedObs = obsService.getObs(testObsId);
		assertNotNull(archivedObs);

		// Purge — this must delete obs_reference_range_archive BEFORE obs_archive (FK order)
		obsService.purgeObs(archivedObs);
		Context.flushSession();
		Context.clearSession();

		// Verify both archive tables are clean
		assertFalse(obsArchiveHelper.isArchived(testObsId), "Purged obs should be removed from archive");
		assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_reference_range_archive WHERE obs_id = ?",
		    Integer.class, testObsId), "reference range should also be purged from archive");
		assertEquals(0,
		    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = ?", Integer.class, testObsId),
		    "obs should be purged from archive");
		assertNull(obsService.getObs(testObsId), "Purged obs should no longer be retrievable");
	}

	@Test
	public void purgeObs_shouldDeleteArchivedObsGroupParent() throws Exception {
		// Create a parent obs with two children
		Obs parent = createAndSaveObsTree(10.0, 20.0);
		int parentId = parent.getObsId();
		List<Integer> childIds = new ArrayList<>();
		for (Obs child : parent.getGroupMembers(true)) {
			childIds.add(child.getObsId());
		}

		// Void parent (cascades to children) and archive all
		obsService.voidObs(parent, "test voiding group");
		Context.flushSession();
		Context.clearSession();

		ObsArchivingTaskHandler archivingTaskHandler = new ObsArchivingTaskHandler(sessionFactory, transactionManager);
		archivingTaskHandler.execute(new ObsArchivingTaskData(), null);

		assertArchived(parentId);
		for (Integer childId : childIds) {
			assertArchived(childId);
		}

		Context.clearSession();

		// Purge the parent directly. The archive helper should cascade and delete the children.
		Obs archivedParent = obsService.getObs(parentId);
		assertNotNull(archivedParent, "Archived parent should be retrievable");
		obsService.purgeObs(archivedParent);
		Context.flushSession();
		Context.clearSession();

		// Verify parent and children are gone
		assertFalse(obsArchiveHelper.isArchived(parentId), "Parent should be purged from archive");
		for (Integer childId : childIds) {
			assertFalse(obsArchiveHelper.isArchived(childId), "Child " + childId + " should be cascaded and purged");
			assertNull(obsService.getObs(childId), "Child " + childId + " should no longer be retrievable");
		}
	}

	@Test
	public void fetchNextBatch_shouldExcludeObsLinkedToConceptProposal() throws Exception {
		// 1. Create and save a new obs
		Obs obs = createAndSaveSingleObs(42.0);
		int obsId = obs.getObsId();

		// 2. Void the obs so it becomes eligible for archiving
		obsService.voidObs(obs, "test voiding");
		Context.flushSession();
		Context.clearSession();

		// 3. Create a ConceptProposal linked to this obs
		ConceptProposal proposal = new ConceptProposal();
		proposal.setOriginalText("Test Proposal");
		proposal.setEncounter(Context.getEncounterService().getEncounter(3));
		// Need to get the obs again in the new session to set it on proposal
		proposal.setObs(obsService.getObs(obsId));
		proposal.setState(OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED);
		Context.getConceptService().saveConceptProposal(proposal);
		Context.flushSession();
		Context.clearSession();

		// 4. Run archiving task
		ObsArchivingTaskHandler archivingTaskHandler = new ObsArchivingTaskHandler(sessionFactory, transactionManager);
		archivingTaskHandler.execute(new ObsArchivingTaskData(), null);

		// 5. Verify the obs is NOT archived, because the ConceptProposal link excluded it from the batch
		assertFalse(obsArchiveHelper.isArchived(obsId), "Obs linked to ConceptProposal should not be archived");
		assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs WHERE obs_id = ?", Integer.class, obsId),
		    "Obs linked to ConceptProposal should remain in the active table");

		// Clean up the concept proposal manually so it doesn't affect other tests
		Context.getConceptService()
		        .purgeConceptProposal(Context.getConceptService().getConceptProposal(proposal.getConceptProposalId()));
	}

	@Test
	public void volatileCache_shouldShortCircuitAndRecoverViaNotificationOrTTL() throws Exception {
		// First, get a real archived obs into the database
		Obs obs = createAndSaveSingleObs(42.0);
		int archivedObsId = obs.getObsId();
		obsService.voidObs(obs, "test voiding");
		Context.flushSession();
		Context.clearSession();
		ObsArchivingTaskHandler archivingTaskHandler = new ObsArchivingTaskHandler(sessionFactory, transactionManager);
		archivingTaskHandler.execute(new ObsArchivingTaskData(), null);

		assertTrue(obsArchiveHelper.isArchived(archivedObsId), "Sanity check: obs should be archived");

		// Extract fields for reflection to manipulate the internal cache
		Field hasDataField = ObsArchiveHelper.class.getDeclaredField("archiveHasData");
		hasDataField.setAccessible(true);
		Field timestampField = ObsArchiveHelper.class.getDeclaredField("archiveHasDataTimestamp");
		timestampField.setAccessible(true);

		// 1. Simulate a stale cache (archiveHasData = false) as if this node hasn't seen the data yet
		hasDataField.set(obsArchiveHelper, false);
		timestampField.set(obsArchiveHelper, System.currentTimeMillis());

		assertFalse(obsArchiveHelper.isArchived(archivedObsId),
		    "Should short-circuit and return false due to stale cache, despite data existing in DB");

		// 2. Simulate TTL expiry by backdating the timestamp (TTL is 60s, we backdate by 65s)
		timestampField.set(obsArchiveHelper, System.currentTimeMillis() - 65_000);

		assertTrue(obsArchiveHelper.isArchived(archivedObsId),
		    "Should hit the DB and return true because the stale cache TTL expired");

		// Verify the cache automatically updated itself after the query
		assertTrue((Boolean) hasDataField.get(obsArchiveHelper), "Cache should have updated itself to true after query");

		// 3. Reset cache to false again to test the explicit notification mechanism
		hasDataField.set(obsArchiveHelper, false);
		timestampField.set(obsArchiveHelper, System.currentTimeMillis());

		assertFalse(obsArchiveHelper.isArchived(archivedObsId), "Sanity check: short-circuiting again");

		// 4. Call markArchiveHasData (simulating the task handler finishing a sweep on this node)
		obsArchiveHelper.markArchiveHasData();

		assertTrue(obsArchiveHelper.isArchived(archivedObsId),
		    "Should return true immediately, bypassing the TTL, because of explicit notification");
	}
}
