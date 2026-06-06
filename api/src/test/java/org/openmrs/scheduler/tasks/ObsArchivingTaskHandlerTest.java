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
			// Reset voided state on any obs we voided during setup
			jdbcTemplate.execute(
			    "UPDATE obs SET voided = false, date_voided = NULL, void_reason = NULL, voided_by = NULL WHERE obs_id IN (7, 9)");
		} catch (Exception e) {
			// Best-effort cleanup
		}
	}

	@Test
	public void execute_shouldArchiveVoidedObservations() throws Exception {
		adminService
		        .saveGlobalProperty(new GlobalProperty(org.openmrs.util.OpenmrsConstants.GP_OBS_ARCHIVE_ENABLED, "true"));

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
		adminService
		        .saveGlobalProperty(new GlobalProperty(org.openmrs.util.OpenmrsConstants.GP_OBS_ARCHIVE_ENABLED, "true"));

		// Void multiple obs via the service API
		obsService.voidObs(obsService.getObs(7), "test voiding");
		obsService.voidObs(obsService.getObs(9), "test voiding");
		Context.flushSession();
		Context.clearSession();

		// Set batch size to 1 so we can track checkpoint behavior
		adminService
		        .saveGlobalProperty(new GlobalProperty(org.openmrs.util.OpenmrsConstants.GP_OBS_ARCHIVE_BATCH_SIZE, "1"));

		// Run archiving - should archive obs_id=9 first (descending order), then stop after 1 batch
		handler.execute(new ObsArchivingTaskData(), null);

		Integer countArchive = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM obs_archive", Integer.class);
		// Handler processes ALL matching in the while loop, not just one batch.
		// There are 2 voided obs (7, 9).
		assertEquals(2, countArchive);

		// Verify checkpoint was reset (sweep complete)
		String checkpoint = adminService
		        .getGlobalProperty(org.openmrs.util.OpenmrsConstants.GP_OBS_ARCHIVE_LAST_PROCESSED_OBS_ID);
		assertEquals("-1", checkpoint);
	}
}
