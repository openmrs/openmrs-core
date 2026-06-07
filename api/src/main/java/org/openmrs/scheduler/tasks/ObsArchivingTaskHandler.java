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

import java.sql.Timestamp;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.TaskContext;
import org.openmrs.scheduler.TaskHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class ObsArchivingTaskHandler implements TaskHandler<ObsArchivingTaskData> {

	private static final Logger log = LoggerFactory.getLogger(ObsArchivingTaskHandler.class);

	private final SessionFactory sessionFactory;

	private final PlatformTransactionManager transactionManager;

	public ObsArchivingTaskHandler(SessionFactory sessionFactory,
	    org.springframework.transaction.TransactionManager transactionManager) {
		this.sessionFactory = sessionFactory;
		this.transactionManager = (PlatformTransactionManager) transactionManager;
	}

	@Override
	public void execute(ObsArchivingTaskData taskData, TaskContext taskContext) throws Exception {
		boolean enabled = Boolean.parseBoolean(Context.getAdministrationService()
		        .getGlobalProperty(org.openmrs.util.OpenmrsConstants.GP_OBS_ARCHIVE_ENABLED, "false"));
		if (!enabled) {
			log.debug("Observation Archiving Job is paused.");
			return;
		}

		int batchSize = Integer.parseInt(Context.getAdministrationService()
		        .getGlobalProperty(org.openmrs.util.OpenmrsConstants.GP_OBS_ARCHIVE_BATCH_SIZE, "1000"));
		int retentionDays = Integer.parseInt(Context.getAdministrationService()
		        .getGlobalProperty(org.openmrs.util.OpenmrsConstants.GP_OBS_ARCHIVE_RETENTION_DAYS, "90"));
		long lastProcessedId = Long.parseLong(Context.getAdministrationService()
		        .getGlobalProperty(org.openmrs.util.OpenmrsConstants.GP_OBS_ARCHIVE_LAST_PROCESSED_OBS_ID, "-1"));

		Timestamp cutoffDate = new Timestamp(System.currentTimeMillis() - (retentionDays * 24L * 3600 * 1000));

		while (true) {
			String enabledProperty = Context.getAdministrationService()
			        .getGlobalProperty(org.openmrs.util.OpenmrsConstants.GP_OBS_ARCHIVE_ENABLED, "false");
			if (!Boolean.parseBoolean(enabledProperty)) {
				log.debug("Observation Archiving Job paused during execution.");
				break;
			}

			List<Long> batchIds = fetchNextBatch(lastProcessedId, cutoffDate, batchSize);
			if (batchIds.isEmpty()) {
				saveLastProcessedId(-1);
				log.debug("Observation Archiving Job completed a full sweep.");
				break;
			}

			try {
				archiveAndDeleteBatch(batchIds);
				lastProcessedId = batchIds.get(batchIds.size() - 1);
				saveLastProcessedId(lastProcessedId);
			} catch (Exception e) {
				log.warn("Batch failed, falling back to row-by-row archiving for batch starting at {}", lastProcessedId, e);
				handleBatchFailure(batchIds);
				lastProcessedId = batchIds.get(batchIds.size() - 1);
				saveLastProcessedId(lastProcessedId);
			}
		}
	}

	private List<Long> fetchNextBatch(long lastProcessedId, Timestamp cutoffDate, int batchSize) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.query.NativeQuery<Long> query;
		if (lastProcessedId == -1) {
			query = session.createNativeQuery(
			    "SELECT obs_id FROM obs WHERE voided = :voided AND date_voided < :cutoffDate ORDER BY obs_id DESC",
			    Long.class);
		} else {
			query = session.createNativeQuery(
			    "SELECT obs_id FROM obs WHERE voided = :voided AND date_voided < :cutoffDate AND obs_id < :lastProcessedId ORDER BY obs_id DESC",
			    Long.class).setParameter("lastProcessedId", lastProcessedId);
		}
		return query.setParameter("voided", true).setParameter("cutoffDate", cutoffDate).setMaxResults(batchSize).list();
	}

	private void archiveAndDeleteBatch(List<Long> batchIds) {
		if (batchIds.isEmpty()) {
			return;
		}

		new TransactionTemplate(transactionManager).execute(status -> {
			Session session = sessionFactory.getCurrentSession();

			// 1. Archive reference ranges
			session.createNativeQuery(
			    "INSERT INTO obs_archive_reference_range (obs_reference_range_id, obs_id, hi_absolute, hi_critical, hi_normal, low_absolute, low_critical, low_normal, uuid) "
			            + "SELECT obs_reference_range_id, obs_id, hi_absolute, hi_critical, hi_normal, low_absolute, low_critical, low_normal, uuid "
			            + "FROM obs_reference_range WHERE obs_id IN (:batchIds)")
			        .setParameter("batchIds", batchIds).executeUpdate();

			// 2. Delete reference ranges
			session.createNativeQuery("DELETE FROM obs_reference_range WHERE obs_id IN (:batchIds)")
			        .setParameter("batchIds", batchIds).executeUpdate();

			// 3. Archive obs
			session.createNativeQuery(
			    "INSERT INTO obs_archive (obs_id, person_id, concept_id, encounter_id, order_id, obs_datetime, location_id, obs_group_id, accession_number, value_group_id, value_coded, value_coded_name_id, value_drug, value_datetime, value_numeric, value_modifier, value_text, value_complex, comments, creator, date_created, voided, voided_by, date_voided, void_reason, uuid, previous_version, form_namespace_and_path, status, interpretation) "
			            + "SELECT obs_id, person_id, concept_id, encounter_id, order_id, obs_datetime, location_id, obs_group_id, accession_number, value_group_id, value_coded, value_coded_name_id, value_drug, value_datetime, value_numeric, value_modifier, value_text, value_complex, comments, creator, date_created, voided, voided_by, date_voided, void_reason, uuid, previous_version, form_namespace_and_path, status, interpretation "
			            + "FROM obs WHERE obs_id IN (:batchIds)")
			        .setParameter("batchIds", batchIds).executeUpdate();

			// 4. Delete obs
			session.createNativeQuery("DELETE FROM obs WHERE obs_id IN (:batchIds)").setParameter("batchIds", batchIds)
			        .executeUpdate();
			return null;
		});
	}

	private void handleBatchFailure(List<Long> batchIds) {
		for (Long obsId : batchIds) {
			try {
				archiveAndDeleteBatch(List.of(obsId));
			} catch (Exception e) {
				log.warn("Skipping observation {} due to constraint violation during archiving.", obsId, e);
			}
		}
	}

	private void saveLastProcessedId(long lastProcessedId) {
		try {
			Context.addProxyPrivilege(org.openmrs.util.PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
			Context.getAdministrationService().setGlobalProperty("obs.archive.last_processed_obs_id",
			    String.valueOf(lastProcessedId));
		} finally {
			Context.removeProxyPrivilege(org.openmrs.util.PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
		}
	}
}
