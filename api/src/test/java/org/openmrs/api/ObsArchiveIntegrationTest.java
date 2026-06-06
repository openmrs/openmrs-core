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
		ObsArchivingTaskHandler archivingTaskHandler = new ObsArchivingTaskHandler(dataSource);
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
}
