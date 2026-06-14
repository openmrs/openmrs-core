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

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.openmrs.Obs;
import org.openmrs.ObsArchive;
import org.openmrs.ObsArchiveReferenceRange;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.TaskContext;
import org.openmrs.scheduler.TaskHandler;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.PrivilegeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Task handler for observation archiving using Hibernate Session and HQL.
 */
@Component
public class ObsArchivingTaskHandler implements TaskHandler<ObsArchivingTaskData> {

	private static final Logger log = LoggerFactory.getLogger(ObsArchivingTaskHandler.class);

	private final SessionFactory sessionFactory;

	private final PlatformTransactionManager transactionManager;

	public ObsArchivingTaskHandler(SessionFactory sessionFactory, TransactionManager transactionManager) {
		this.sessionFactory = sessionFactory;
		this.transactionManager = (PlatformTransactionManager) transactionManager;
	}

	@Override
	public void execute(ObsArchivingTaskData taskData, TaskContext taskContext) throws Exception {
		boolean enabled = Boolean.parseBoolean(
		    Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GP_OBS_ARCHIVE_ENABLED, "false"));
		if (!enabled) {
			log.debug("Observation Archiving Job is paused.");
			return;
		}

		int batchSize = Integer.parseInt(
		    Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GP_OBS_ARCHIVE_BATCH_SIZE, "1000"));
		int retentionDays = Integer.parseInt(
		    Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GP_OBS_ARCHIVE_RETENTION_DAYS, "90"));
		long lastProcessedId = Long.parseLong(Context.getAdministrationService()
		        .getGlobalProperty(OpenmrsConstants.GP_OBS_ARCHIVE_LAST_PROCESSED_OBS_ID, "-1"));

		Date cutoffDate = new Date(System.currentTimeMillis() - (retentionDays * 24L * 3600 * 1000));

		while (true) {
			String enabledProperty = Context.getAdministrationService()
			        .getGlobalProperty(OpenmrsConstants.GP_OBS_ARCHIVE_ENABLED, "false");
			if (!Boolean.parseBoolean(enabledProperty)) {
				log.debug("Observation Archiving Job paused during execution.");
				break;
			}

			List<Integer> batchIds = fetchNextBatch(lastProcessedId, cutoffDate, batchSize);
			if (batchIds.isEmpty()) {
				saveLastProcessedId(-1);
				log.debug("Observation Archiving Job completed a full sweep.");
				break;
			}

			try {
				archiveAndDeleteBatch(batchIds);
				lastProcessedId = batchIds.get(batchIds.size() - 1);
				saveLastProcessedId(lastProcessedId);
			} catch (DataAccessException | TransactionException e) {
				log.warn("Batch failed, falling back to row-by-row archiving for batch starting at {}", lastProcessedId, e);
				handleBatchFailure(batchIds);
				lastProcessedId = batchIds.get(batchIds.size() - 1);
				saveLastProcessedId(lastProcessedId);
			}
		}
	}

	private List<Integer> fetchNextBatch(long lastProcessedId, Date cutoffDate, int batchSize) {
		Session session = sessionFactory.getCurrentSession();
		Query<Integer> query;
		if (lastProcessedId == -1) {
			query = session.createQuery(
			    "SELECT o.obsId FROM Obs o WHERE o.voided = :voided AND o.dateVoided < :cutoffDate ORDER BY o.obsId DESC",
			    Integer.class);
		} else {
			query = session.createQuery(
			    "SELECT o.obsId FROM Obs o WHERE o.voided = :voided AND o.dateVoided < :cutoffDate AND o.obsId < :lastProcessedId ORDER BY o.obsId DESC",
			    Integer.class).setParameter("lastProcessedId", (int) lastProcessedId);
		}
		return query.setParameter("voided", true).setParameter("cutoffDate", cutoffDate).setMaxResults(batchSize).list();
	}

	private void archiveAndDeleteBatch(List<Integer> batchIds) {
		if (batchIds.isEmpty()) {
			return;
		}

		new TransactionTemplate(transactionManager).execute(status -> {
			Session session = sessionFactory.getCurrentSession();

			List<Obs> obsList = session
			        .createQuery("FROM Obs o LEFT JOIN FETCH o.referenceRange WHERE o.obsId IN (:batchIds)", Obs.class)
			        .setParameter("batchIds", batchIds).list();

			List<Integer> idsToProcess = new java.util.ArrayList<>();
			for (Obs obs : obsList) {
				if (obs.hasGroupMembers(true)) {
					Number activeChildrenCount = (Number) session.createQuery(
					    "SELECT count(c) FROM Obs c WHERE c.obsGroup.obsId = :parentId AND c.obsId NOT IN (:batchIds)")
					        .setParameter("parentId", obs.getObsId()).setParameter("batchIds", batchIds).uniqueResult();
					if (activeChildrenCount != null && activeChildrenCount.intValue() > 0) {
						log.debug("Skipping parent obs {} because it still has active children in the obs table",
						    obs.getObsId());
						continue;
					}
				}
				idsToProcess.add(obs.getObsId());
			}

			if (idsToProcess.isEmpty()) {
				return null;
			}

			idsToProcess.sort(java.util.Comparator.reverseOrder());

			for (Integer obsId : idsToProcess) {
				Obs obs = session.get(Obs.class, obsId);
				if (obs == null) {
					continue;
				}

				ObsArchive archive = new ObsArchive();
				archive.setObsId(obs.getObsId());
				archive.setPerson(obs.getPerson());
				archive.setConcept(obs.getConcept());
				archive.setEncounter(obs.getEncounter());
				archive.setOrder(obs.getOrder());
				archive.setObsDatetime(obs.getObsDatetime());
				archive.setLocation(obs.getLocation());
				if (obs.getObsGroup() != null) {
					archive.setObsGroupId(obs.getObsGroup().getObsId());
				}
				archive.setAccessionNumber(obs.getAccessionNumber());
				archive.setValueGroupId(obs.getValueGroupId());
				archive.setValueCoded(obs.getValueCoded());
				archive.setValueCodedName(obs.getValueCodedName());
				archive.setValueDrug(obs.getValueDrug());
				archive.setValueDatetime(obs.getValueDatetime());
				archive.setValueNumeric(obs.getValueNumeric());
				archive.setValueModifier(obs.getValueModifier());
				archive.setValueText(obs.getValueText());
				archive.setValueComplex(obs.getValueComplex());
				archive.setComments(obs.getComment());
				archive.setFormNamespaceAndPath(obs.getFormNamespaceAndPath());

				archive.setCreator(obs.getCreator());
				archive.setDateCreated(obs.getDateCreated());
				archive.setChangedBy(obs.getChangedBy());
				archive.setDateChanged(obs.getDateChanged());
				archive.setVoided(obs.getVoided());
				archive.setVoidedBy(obs.getVoidedBy());
				archive.setDateVoided(obs.getDateVoided());
				archive.setVoidReason(obs.getVoidReason());
				archive.setUuid(obs.getUuid());

				if (obs.getPreviousVersion() != null) {
					archive.setPreviousVersionId(obs.getPreviousVersion().getObsId());
				} else if (obs.getPreviousVersionId() != null) {
					archive.setPreviousVersionId(obs.getPreviousVersionId());
				}
				archive.setStatus(obs.getStatus());
				archive.setInterpretation(obs.getInterpretation());

				if (obs.getReferenceRange() != null) {
					ObsArchiveReferenceRange range = new ObsArchiveReferenceRange();
					range.setObsReferenceRangeId(obs.getReferenceRange().getObsReferenceRangeId());
					range.setHiAbsolute(obs.getReferenceRange().getHiAbsolute());
					range.setHiCritical(obs.getReferenceRange().getHiCritical());
					range.setHiNormal(obs.getReferenceRange().getHiNormal());
					range.setLowAbsolute(obs.getReferenceRange().getLowAbsolute());
					range.setLowCritical(obs.getReferenceRange().getLowCritical());
					range.setLowNormal(obs.getReferenceRange().getLowNormal());
					range.setUuid(obs.getReferenceRange().getUuid());
					range.setObsArchive(archive);
					archive.setReferenceRange(range);
				}

				archive.setArchivedBy(Context.getAuthenticatedUser());
				archive.setDateArchived(new Date());

				session.persist(archive);

				if (obs.getReferenceRange() != null) {
					session.remove(obs.getReferenceRange());
				}
				session.remove(obs);
			}

			return null;
		});
	}

	private void handleBatchFailure(List<Integer> batchIds) {
		for (Integer obsId : batchIds) {
			try {
				archiveAndDeleteBatch(List.of(obsId));
			} catch (DataAccessException | TransactionException e) {
				log.warn("Skipping observation {} due to constraint violation during archiving.", obsId, e);
			}
		}
	}

	private void saveLastProcessedId(long lastProcessedId) {
		try {
			Context.addProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
			Context.getAdministrationService().setGlobalProperty(OpenmrsConstants.GP_OBS_ARCHIVE_LAST_PROCESSED_OBS_ID,
			    String.valueOf(lastProcessedId));
		} finally {
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
		}
	}
}
