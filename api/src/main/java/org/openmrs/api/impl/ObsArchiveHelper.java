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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.jdbc.Work;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;

public class ObsArchiveHelper {

	private final SessionFactory sessionFactory;

	public ObsArchiveHelper(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public boolean isArchived(Integer obsId) {
		if (obsId == null)
			return false;

		Session session = sessionFactory.getCurrentSession();
		Number count = (Number) session.createNativeQuery("SELECT COUNT(1) FROM obs_archive WHERE obs_id = :obsId")
		        .setParameter("obsId", obsId).uniqueResult();

		return count != null && count.intValue() > 0;
	}

	public Obs getObsFromArchive(Integer obsId) {
		List<Obs> list = getObsList("SELECT * FROM obs_archive WHERE obs_id = ?", obsId);
		return list.isEmpty() ? null : list.get(0);
	}

	public Obs getObsFromArchiveByUuid(String uuid) {
		List<Obs> list = getObsList("SELECT * FROM obs_archive WHERE uuid = ?", uuid);
		return list.isEmpty() ? null : list.get(0);
	}

	public Map<String, Object> getArchivedMetadata(Integer obsId) {
		Session session = sessionFactory.getCurrentSession();
		Object[] result = (Object[]) session
		        .createNativeQuery("SELECT obs_group_id, previous_version FROM obs_archive WHERE obs_id = :obsId")
		        .setParameter("obsId", obsId).uniqueResult();
		if (result == null)
			return null;
		Map<String, Object> map = new HashMap<>();
		map.put("obs_group_id", result[0]);
		map.put("previous_version", result[1]);
		return map;
	}

	public void restoreFromArchive(Integer obsId) {
		Map<String, Object> metadata = getArchivedMetadata(obsId);
		if (metadata == null) {
			return;
		}

		Integer obsGroupId = (Integer) metadata.get("obs_group_id");
		if (obsGroupId != null && isArchived(obsGroupId)) {
			restoreFromArchive(obsGroupId);
		}

		Integer previousVersionId = (Integer) metadata.get("previous_version");
		if (previousVersionId != null && isArchived(previousVersionId)) {
			restoreFromArchive(previousVersionId);
		}

		moveRecordFromArchiveToActiveTable(obsId);
	}

	private void moveRecordFromArchiveToActiveTable(Integer obsId) {
		Session session = sessionFactory.getCurrentSession();
		// 1. Move obs
		session.createNativeQuery(
		    "INSERT INTO obs (obs_id, person_id, concept_id, encounter_id, order_id, obs_datetime, location_id, obs_group_id, accession_number, value_group_id, value_coded, value_coded_name_id, value_drug, value_datetime, value_numeric, value_modifier, value_text, value_complex, comments, creator, date_created, voided, voided_by, date_voided, void_reason, uuid, previous_version, form_namespace_and_path, status, interpretation) "
		            + "SELECT obs_id, person_id, concept_id, encounter_id, order_id, obs_datetime, location_id, obs_group_id, accession_number, value_group_id, value_coded, value_coded_name_id, value_drug, value_datetime, value_numeric, value_modifier, value_text, value_complex, comments, creator, date_created, voided, voided_by, date_voided, void_reason, uuid, previous_version, form_namespace_and_path, status, interpretation "
		            + "FROM obs_archive WHERE obs_id = :obsId")
		        .setParameter("obsId", obsId).executeUpdate();

		session.createNativeQuery("DELETE FROM obs_archive WHERE obs_id = :obsId").setParameter("obsId", obsId)
		        .executeUpdate();

		// 2. Move reference range
		session.createNativeQuery(
		    "INSERT INTO obs_reference_range (obs_reference_range_id, obs_id, hi_absolute, hi_critical, hi_normal, low_absolute, low_critical, low_normal, uuid) "
		            + "SELECT obs_reference_range_id, obs_id, hi_absolute, hi_critical, hi_normal, low_absolute, low_critical, low_normal, uuid "
		            + "FROM obs_archive_reference_range WHERE obs_id = :obsId")
		        .setParameter("obsId", obsId).executeUpdate();

		session.createNativeQuery("DELETE FROM obs_archive_reference_range WHERE obs_id = :obsId")
		        .setParameter("obsId", obsId).executeUpdate();
	}

	private List<Obs> getObsList(final String sql, final Object param) {
		final List<Obs> results = new ArrayList<>();
		sessionFactory.getCurrentSession().doWork(new Work() {

			@Override
			public void execute(Connection connection) throws SQLException {
				try (PreparedStatement ps = connection.prepareStatement(sql)) {
					if (param instanceof Integer) {
						ps.setInt(1, (Integer) param);
					} else {
						ps.setString(1, param.toString());
					}
					try (ResultSet rs = ps.executeQuery()) {
						while (rs.next()) {
							Obs obs = new Obs();
							obs.setObsId(rs.getInt("obs_id"));

							int personId = rs.getInt("person_id");
							if (!rs.wasNull())
								obs.setPerson(Context.getPersonService().getPerson(personId));

							int conceptId = rs.getInt("concept_id");
							if (!rs.wasNull())
								obs.setConcept(Context.getConceptService().getConcept(conceptId));

							int encounterId = rs.getInt("encounter_id");
							if (!rs.wasNull())
								obs.setEncounter(Context.getEncounterService().getEncounter(encounterId));

							int orderId = rs.getInt("order_id");
							if (!rs.wasNull())
								obs.setOrder(Context.getOrderService().getOrder(orderId));

							obs.setObsDatetime(rs.getTimestamp("obs_datetime"));

							int locationId = rs.getInt("location_id");
							if (!rs.wasNull())
								obs.setLocation(Context.getLocationService().getLocation(locationId));

							int obsGroupId = rs.getInt("obs_group_id");
							if (!rs.wasNull()) {
								Obs group = new Obs();
								group.setObsId(obsGroupId);
								obs.setObsGroup(group);
							}

							obs.setAccessionNumber(rs.getString("accession_number"));

							int valueGroupId = rs.getInt("value_group_id");
							if (!rs.wasNull())
								obs.setValueGroupId(valueGroupId);

							int valueCodedId = rs.getInt("value_coded");
							if (!rs.wasNull())
								obs.setValueCoded(Context.getConceptService().getConcept(valueCodedId));

							int valueCodedNameId = rs.getInt("value_coded_name_id");
							if (!rs.wasNull())
								obs.setValueCodedName(Context.getConceptService().getConceptName(valueCodedNameId));

							int valueDrugId = rs.getInt("value_drug");
							if (!rs.wasNull())
								obs.setValueDrug(Context.getConceptService().getDrug(valueDrugId));

							obs.setValueDatetime(rs.getTimestamp("value_datetime"));

							double valueNumeric = rs.getDouble("value_numeric");
							if (!rs.wasNull())
								obs.setValueNumeric(valueNumeric);

							obs.setValueModifier(rs.getString("value_modifier"));
							obs.setValueText(rs.getString("value_text"));
							obs.setValueComplex(rs.getString("value_complex"));
							obs.setComment(rs.getString("comments"));

							int creatorId = rs.getInt("creator");
							if (!rs.wasNull())
								obs.setCreator(Context.getUserService().getUser(creatorId));

							obs.setDateCreated(rs.getTimestamp("date_created"));
							obs.setVoided(rs.getBoolean("voided"));

							int voidedById = rs.getInt("voided_by");
							if (!rs.wasNull())
								obs.setVoidedBy(Context.getUserService().getUser(voidedById));

							obs.setDateVoided(rs.getTimestamp("date_voided"));
							obs.setVoidReason(rs.getString("void_reason"));
							obs.setUuid(rs.getString("uuid"));

							int previousVersionId = rs.getInt("previous_version");
							if (!rs.wasNull()) {
								Obs prev = new Obs();
								prev.setObsId(previousVersionId);
								obs.setPreviousVersion(prev);
							}

							String status = rs.getString("status");
							if (status != null) {
								obs.setStatus(Obs.Status.valueOf(status));
							}

							String interpretation = rs.getString("interpretation");
							if (interpretation != null) {
								obs.setInterpretation(Obs.Interpretation.valueOf(interpretation));
							}

							results.add(obs);
						}
					}
				}
			}
		});
		return results;
	}
}
