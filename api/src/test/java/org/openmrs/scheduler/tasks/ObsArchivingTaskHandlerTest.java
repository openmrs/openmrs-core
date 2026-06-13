/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler.tasks;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveNonTransactionalTest;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ObsArchivingTaskHandler}.
 * <p>
 * This test extends {@link BaseContextSensitiveNonTransactionalTest} (non-transactional) because
 * the handler uses its own {@link JdbcTemplate} which gets a separate JDBC connection from the
 * DataSource. In a {@code @Transactional} test, the handler's connection would not see the test's
 * uncommitted changes, causing all archiving queries to return zero rows.
 */
public class ObsArchivingTaskHandlerTest extends BaseContextSensitiveNonTransactionalTest {

	@Autowired
	private ObsArchivingTaskHandler handler;

	@Autowired
	private ObsService obsService;

	@Autowired
	private AdministrationService adminService;

	@Autowired
	private DataSource dataSource;

	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	public void setup() {
		jdbcTemplate = new JdbcTemplate(dataSource);
		try {
			jdbcTemplate.execute("DELETE FROM obs_archive");
			jdbcTemplate.execute("DELETE FROM obs_archive_reference_range");
		} catch (Exception e) {
			// Tables may not exist yet on first run
		}

		// Drop Hibernate-created foreign key on previous_version in H2
		try {
			java.util.List<String> constraints = jdbcTemplate
			        .queryForList(
			            "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE "
			                    + "WHERE UPPER(TABLE_NAME) = 'OBS' AND UPPER(COLUMN_NAME) = 'PREVIOUS_VERSION'",
			            String.class);
			for (String constraint : constraints) {
				try {
					jdbcTemplate.execute("ALTER TABLE obs DROP CONSTRAINT " + constraint);
				} catch (Exception e) {
					// Ignore
				}
			}
		} catch (Exception e) {
			try {
				jdbcTemplate.execute("ALTER TABLE obs DROP CONSTRAINT IF EXISTS FKRDYF6DEFJW3MNOX499SIXH4LK");
			} catch (Exception ex) {
				// Ignore
			}
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
			            + "form_namespace_and_path, status, interpretation FROM obs_archive a "
			            + "WHERE NOT EXISTS (SELECT 1 FROM obs o WHERE o.obs_id = a.obs_id)");
			jdbcTemplate.execute("DELETE FROM obs_archive_reference_range");
			jdbcTemplate.execute("DELETE FROM obs_archive");
			// Reset voided state on any obs we voided during setup
			jdbcTemplate.execute(
			    "UPDATE obs SET voided = false, date_voided = NULL, void_reason = NULL, voided_by = NULL WHERE obs_id IN (7, 9)");
		} catch (Exception e) {
			// Best-effort cleanup
		}
	}

	@Test
	public void execute_shouldArchiveVoidedObservations() throws Exception {
		adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_OBS_ARCHIVE_ENABLED, "true"));

		// Void obs_id=7 via the service API (committed, visible to all connections)
		Obs obs = obsService.getObs(7);
		obsService.voidObs(obs, "test voiding");
		Context.flushSession();
		Context.clearSession();

		// Run archiving
		handler.execute(new ObsArchivingTaskData(), null);

		// Verify it was archived
		Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = 7", Integer.class);
		assertEquals(1, count);

		// Verify it was deleted from obs
		count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs WHERE obs_id = 7", Integer.class);
		assertEquals(0, count);

		// Verify transparent retrieval still works
		Context.clearSession();
		Obs retrieved = obsService.getObs(7);
		assertNotNull(retrieved);
		assertTrue(retrieved.getVoided());
	}

	@Test
	public void execute_shouldBatchProcessAndResumeFromCheckpoint() throws Exception {
		adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_OBS_ARCHIVE_ENABLED, "true"));

		// Void multiple obs via the service API
		obsService.voidObs(obsService.getObs(7), "test voiding");
		obsService.voidObs(obsService.getObs(9), "test voiding");
		Context.flushSession();
		Context.clearSession();

		// Set batch size to 1 so we can track checkpoint behavior
		adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_OBS_ARCHIVE_BATCH_SIZE, "1"));

		// Run archiving - should archive obs_id=9 first (descending order), then stop after 1 batch
		handler.execute(new ObsArchivingTaskData(), null);

		Integer countArchive = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive", Integer.class);
		// Handler processes ALL matching in the while loop, not just one batch.
		// There are 2 voided obs (7, 9).
		assertEquals(2, countArchive);

		// Verify checkpoint was reset (sweep complete)
		String checkpoint = adminService.getGlobalProperty(OpenmrsConstants.GP_OBS_ARCHIVE_LAST_PROCESSED_OBS_ID);
		assertEquals("-1", checkpoint);
	}

	@Test
	public void execute_shouldNotExecuteIfDisabled() throws Exception {
		adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_OBS_ARCHIVE_ENABLED, "false"));

		Obs obs = obsService.getObs(7);
		obsService.voidObs(obs, "test voiding");
		Context.flushSession();
		Context.clearSession();

		handler.execute(new ObsArchivingTaskData(), null);

		Integer countArchive = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = 7",
		    Integer.class);
		assertEquals(0, countArchive);
	}

	@Test
	public void execute_shouldNotArchiveObservationsVoidedWithinRetentionPeriod() throws Exception {
		adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_OBS_ARCHIVE_ENABLED, "true"));
		adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_OBS_ARCHIVE_RETENTION_DAYS, "90"));

		// Void an obs now
		Obs obs = obsService.getObs(7);
		obsService.voidObs(obs, "test voiding");
		Context.flushSession();
		Context.clearSession();

		handler.execute(new ObsArchivingTaskData(), null);

		// Verify it was NOT archived because it was voided today (within 90 days)
		Integer countArchive = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = 7",
		    Integer.class);
		assertEquals(0, countArchive);

		// Now backdate the void date to 100 days ago
		java.util.Date pastDate = new java.util.Date(System.currentTimeMillis() - (100L * 24L * 3600L * 1000L));
		jdbcTemplate.update("UPDATE obs SET date_voided = ? WHERE obs_id = 7", pastDate);

		handler.execute(new ObsArchivingTaskData(), null);

		// Verify it IS archived now
		countArchive = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = 7", Integer.class);
		assertEquals(1, countArchive);
	}

	@Test
	public void execute_shouldResumeFromCheckpoint() throws Exception {
		adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_OBS_ARCHIVE_ENABLED, "true"));

		// Void obs 7 and 9
		obsService.voidObs(obsService.getObs(7), "test voiding");
		obsService.voidObs(obsService.getObs(9), "test voiding");
		Context.flushSession();
		Context.clearSession();

		// Manually set checkpoint to 8. The handler processes in DESC order (from highest to lowest).
		// So if checkpoint is 8, it will only process obs_id < 8.
		// Thus, obs 9 should be ignored.
		adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_OBS_ARCHIVE_LAST_PROCESSED_OBS_ID, "8"));

		handler.execute(new ObsArchivingTaskData(), null);

		// Only 7 should be archived
		assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = 7", Integer.class));
		assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = 9", Integer.class));
	}

	@Test
	public void execute_shouldFallbackToRowByRowOnBatchFailure() throws Exception {
		adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GP_OBS_ARCHIVE_ENABLED, "true"));

		// Void multiple obs
		obsService.voidObs(obsService.getObs(7), "test voiding");
		obsService.voidObs(obsService.getObs(9), "test voiding");
		Context.flushSession();
		Context.clearSession();

		// Deliberately cause a primary key violation for obs_id 9 by pre-inserting it into obs_archive
		jdbcTemplate.update(
		    "INSERT INTO obs_archive (obs_id, person_id, concept_id, obs_datetime, creator, date_created, voided, status, uuid) VALUES "
		            + "(9, 1, 5089, '2000-01-01', 1, '2000-01-01', true, 'FINAL', 'dummy-uuid-for-9')");

		// Run archiving. The batch containing 7 and 9 will fail.
		// It should catch the exception, fall back to row-by-row, skip 9 (because it fails again), and successfully archive 7.
		handler.execute(new ObsArchivingTaskData(), null);

		// Verify 7 was successfully archived
		assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive WHERE obs_id = 7", Integer.class));
		assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs WHERE obs_id = 7", Integer.class));

		// Verify 9 was left in the active table (and the dummy row remains in archive)
		assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs WHERE obs_id = 9", Integer.class));

		// Clean up the dummy row to avoid issues in cleanup
		jdbcTemplate.update("DELETE FROM obs_archive WHERE uuid = 'dummy-uuid-for-9'");
	}
}
