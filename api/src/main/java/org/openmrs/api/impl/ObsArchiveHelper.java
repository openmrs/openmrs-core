/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.proxy.HibernateProxy;
import org.openmrs.Obs;
import org.openmrs.ObsArchive;
import org.openmrs.ObsReferenceRange;
import org.openmrs.OpenmrsObject;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for observation archiving operations using Hibernate Session and HQL.
 */
public class ObsArchiveHelper {

	private static final Logger log = LoggerFactory.getLogger(ObsArchiveHelper.class);

	private static final String PARAM_OBS_ID = "obsId";

	private final SessionFactory sessionFactory;

	public ObsArchiveHelper(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public boolean isArchived(Integer obsId) {
		if (obsId == null) {
			return false;
		}
		try {
			// session.get() pre-warms the Hibernate L1 cache for subsequent
			// calls in the same transaction.
			return withManualFlush(() -> sessionFactory.getCurrentSession().get(ObsArchive.class, obsId) != null);
		} catch (HibernateException e) {
			log.warn("Failed to check if obs {} is archived. Assuming it is not.", obsId, e);
			return false;
		}
	}

	public boolean hasArchivedChildren(Integer obsId) {
		if (obsId == null) {
			return false;
		}
		try {
			return withManualFlush(() -> {
				Session session = sessionFactory.getCurrentSession();
				Object result = session.createQuery("SELECT 1 FROM ObsArchive a WHERE a.obsGroupId = :obsId")
				        .setParameter(PARAM_OBS_ID, obsId).setMaxResults(1).uniqueResult();
				return result != null;
			});
		} catch (HibernateException e) {
			log.warn("Failed to check if obs {} has archived children. Assuming it does not.", obsId, e);
			return false;
		}
	}

	public List<Obs> getArchivedObsByEncounterId(Integer encounterId) {
		if (encounterId == null) {
			return Collections.emptyList();
		}
		try {
			return withManualFlush(() -> {
				Session session = sessionFactory.getCurrentSession();
				List<ObsArchive> archives = session
				        .createQuery("FROM ObsArchive a WHERE a.encounter.encounterId = :encId", ObsArchive.class)
				        .setParameter("encId", encounterId).list();
				return archives.stream().map(this::convertToObs).collect(Collectors.toList());
			});
		} catch (HibernateException e) {
			log.debug("Failed to get archived obs for encounter {}.", encounterId, e);
			return Collections.emptyList();
		}
	}

	public Obs getObsFromArchive(Integer obsId) {
		if (obsId == null) {
			return null;
		}
		try {
			return withManualFlush(() -> {
				Session session = sessionFactory.getCurrentSession();
				ObsArchive archive = session.get(ObsArchive.class, obsId);
				return convertToObs(archive);
			});
		} catch (HibernateException e) {
			log.debug("Failed to get obs {} from archive.", obsId, e);
			return null;
		}
	}

	public Obs getObsFromArchiveByUuid(String uuid) {
		if (uuid == null) {
			return null;
		}
		try {
			return withManualFlush(() -> {
				Session session = sessionFactory.getCurrentSession();
				ObsArchive archive = (ObsArchive) session.createQuery("FROM ObsArchive WHERE uuid = :uuid")
				        .setParameter("uuid", uuid).uniqueResult();
				return convertToObs(archive);
			});
		} catch (HibernateException e) {
			log.debug("Failed to get obs by uuid {} from archive.", uuid, e);
			return null;
		}
	}

	public boolean restoreFromArchive(Integer obsId) {
		return restoreFromArchive(obsId, true);
	}

	public boolean restoreFromArchive(Integer obsId, boolean restoreChildren) {
		Session session = sessionFactory.getCurrentSession();
		// Load the entity to seed the L1 cache for subsequent operations
		ObsArchive archive = session.get(ObsArchive.class, obsId);
		if (archive == null) {
			return false;
		}

		// Restore the parent group first if it is also archived.
		// restoreFromArchive returns early if the parent is not found.
		Integer obsGroupId = archive.getObsGroupId();
		if (obsGroupId != null) {
			restoreFromArchive(obsGroupId, false);
		}

		moveRecordFromArchiveToActiveTable(obsId);

		if (restoreChildren) {
			// Read dateVoided from the already-loaded entity
			Date dateVoided = archive.getDateVoided();
			List<Integer> childIds;
			if (dateVoided != null) {
				childIds = session.createQuery(
				    "SELECT a.obsId FROM ObsArchive a WHERE a.obsGroupId = :obsId AND a.dateVoided = :dateVoided",
				    Integer.class).setParameter(PARAM_OBS_ID, obsId).setParameter("dateVoided", dateVoided).list();
			} else {
				childIds = session
				        .createQuery("SELECT a.obsId FROM ObsArchive a WHERE a.obsGroupId = :obsId AND a.dateVoided IS NULL",
				            Integer.class)
				        .setParameter(PARAM_OBS_ID, obsId).list();
			}
			for (Integer childId : childIds) {
				restoreFromArchive(childId, true);
			}
		}

		return true;
	}

	private void moveRecordFromArchiveToActiveTable(Integer obsId) {
		Session session = sessionFactory.getCurrentSession();
		// Retrieve the entity from the L1 cache, loaded earlier by restoreFromArchive()
		ObsArchive archive = session.get(ObsArchive.class, obsId);
		if (archive == null) {
			return;
		}

		// WARNING: This method uses native SQL. If the schema of the `obs` or `obs_archive` table
		// changes (e.g., a column is added or removed), this SQL MUST be updated to match.
		// 1. Move obs using native query
		session.createNativeQuery(
		    "INSERT INTO obs (obs_id, person_id, concept_id, encounter_id, order_id, obs_datetime, location_id, "
		            + "obs_group_id, accession_number, value_group_id, value_coded, value_coded_name_id, value_drug, "
		            + "value_datetime, value_numeric, value_modifier, value_text, value_complex, comments, creator, "
		            + "date_created, voided, voided_by, date_voided, void_reason, uuid, previous_version, "
		            + "form_namespace_and_path, status, interpretation) "
		            + "SELECT obs_id, person_id, concept_id, encounter_id, order_id, obs_datetime, location_id, "
		            + "obs_group_id, accession_number, value_group_id, value_coded, value_coded_name_id, value_drug, "
		            + "value_datetime, value_numeric, value_modifier, value_text, value_complex, comments, creator, "
		            + "date_created, voided, voided_by, date_voided, void_reason, uuid, previous_version, "
		            + "form_namespace_and_path, status, interpretation FROM obs_archive WHERE obs_id = :obsId")
		        .setParameter(PARAM_OBS_ID, obsId).executeUpdate();

		// 2. Move reference range using native query
		session.createNativeQuery("INSERT INTO obs_reference_range (obs_id, hi_absolute, hi_critical, hi_normal, "
		        + "low_absolute, low_critical, low_normal, uuid) "
		        + "SELECT obs_id, hi_absolute, hi_critical, hi_normal, low_absolute, "
		        + "low_critical, low_normal, uuid FROM obs_archive_reference_range WHERE obs_id = :obsId")
		        .setParameter(PARAM_OBS_ID, obsId).executeUpdate();

		// 3. Delete from archive reference range
		session.createNativeQuery("DELETE FROM obs_archive_reference_range WHERE obs_id = :obsId")
		        .setParameter(PARAM_OBS_ID, obsId).executeUpdate();

		// 4. Delete from archive obs
		session.createNativeQuery("DELETE FROM obs_archive WHERE obs_id = :obsId").setParameter(PARAM_OBS_ID, obsId)
		        .executeUpdate();

		// Evict the stale entity since native SQL bypasses Hibernate's entity lifecycle
		session.evict(archive);
	}

	private Obs convertToObs(ObsArchive archive) {
		if (archive == null) {
			return null;
		}
		Obs obs = new Obs();
		obs.setObsId(archive.getObsId());
		obs.setPerson(archive.getPerson());
		obs.setConcept(archive.getConcept());
		obs.setEncounter(archive.getEncounter());
		obs.setOrder(archive.getOrder());
		obs.setObsDatetime(archive.getObsDatetime());
		obs.setLocation(archive.getLocation());
		if (archive.getObsGroupId() != null) {
			Obs parent = new Obs();
			parent.setObsId(archive.getObsGroupId());
			obs.setObsGroup(parent);
		}
		obs.setAccessionNumber(archive.getAccessionNumber());
		obs.setValueGroupId(archive.getValueGroupId());
		obs.setValueCoded(archive.getValueCoded());
		obs.setValueCodedName(archive.getValueCodedName());
		obs.setValueDrug(archive.getValueDrug());
		obs.setValueDatetime(archive.getValueDatetime());
		obs.setValueNumeric(archive.getValueNumeric());
		obs.setValueModifier(archive.getValueModifier());
		obs.setValueText(archive.getValueText());
		obs.setValueComplex(archive.getValueComplex());
		obs.setComment(archive.getComments());
		obs.setFormNamespaceAndPath(archive.getFormNamespaceAndPath());

		obs.setCreator(archive.getCreator());
		obs.setDateCreated(archive.getDateCreated());
		obs.setChangedBy(archive.getChangedBy());
		obs.setDateChanged(archive.getDateChanged());
		obs.setVoided(archive.getVoided());
		obs.setVoidedBy(archive.getVoidedBy());
		obs.setDateVoided(archive.getDateVoided());
		obs.setVoidReason(archive.getVoidReason());
		obs.setUuid(archive.getUuid());

		if (archive.getPreviousVersionId() != null) {
			Obs prev = new Obs();
			prev.setObsId(archive.getPreviousVersionId());
			obs.setPreviousVersion(prev);
		}
		obs.setStatus(archive.getStatus());
		obs.setInterpretation(archive.getInterpretation());

		if (archive.getReferenceRange() != null) {
			ObsReferenceRange range = new ObsReferenceRange();
			range.setObsReferenceRangeId(archive.getReferenceRange().getObsReferenceRangeId());
			range.setHiAbsolute(archive.getReferenceRange().getHiAbsolute());
			range.setHiCritical(archive.getReferenceRange().getHiCritical());
			range.setHiNormal(archive.getReferenceRange().getHiNormal());
			range.setLowAbsolute(archive.getReferenceRange().getLowAbsolute());
			range.setLowCritical(archive.getReferenceRange().getLowCritical());
			range.setLowNormal(archive.getReferenceRange().getLowNormal());
			range.setUuid(archive.getReferenceRange().getUuid());
			range.setObs(obs);
			obs.setReferenceRange(range);
		}

		return obs;
	}

	public <T> T withManualFlush(Supplier<T> action) {
		Session session = sessionFactory.getCurrentSession();
		FlushMode originalFlushMode = session.getHibernateFlushMode();
		session.setHibernateFlushMode(FlushMode.MANUAL);
		try {
			return action.get();
		} finally {
			session.setHibernateFlushMode(originalFlushMode);
		}
	}

	public static <T, U extends T> boolean hibernateAwareEquals(T d1, U d2) {
		if (d1 == null) {
			return d2 == null;
		} else if (d2 == null) {
			return false;
		}
		if (d1 == d2) {
			return true;
		}
		if (d1 instanceof OpenmrsObject && d2 instanceof OpenmrsObject) {
			Class<?> class1 = (d1 instanceof HibernateProxy)
			        ? ((HibernateProxy) d1).getHibernateLazyInitializer().getPersistentClass()
			        : d1.getClass();
			Class<?> class2 = (d2 instanceof HibernateProxy)
			        ? ((HibernateProxy) d2).getHibernateLazyInitializer().getPersistentClass()
			        : d2.getClass();

			if (!(class1.isAssignableFrom(class2) || class2.isAssignableFrom(class1))) {
				return false;
			}

			if (d1 instanceof HibernateProxy || d2 instanceof HibernateProxy) {
				Object id1 = (d1 instanceof HibernateProxy)
				        ? ((HibernateProxy) d1).getHibernateLazyInitializer().getIdentifier()
				        : ((OpenmrsObject) d1).getId();
				Object id2 = (d2 instanceof HibernateProxy)
				        ? ((HibernateProxy) d2).getHibernateLazyInitializer().getIdentifier()
				        : ((OpenmrsObject) d2).getId();

				if (id1 != null && id2 != null) {
					return id1.equals(id2);
				}
				if (id1 == null || id2 == null) {
					return false;
				}
			}
		}
		return (d1 instanceof Date d1Date && d2 instanceof Date d2Date) ? OpenmrsUtil.compare(d1Date, d2Date) == 0
		        : d1.equals(d2);
	}
}
