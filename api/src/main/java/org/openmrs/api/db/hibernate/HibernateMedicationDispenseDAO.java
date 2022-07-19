/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import org.hibernate.SessionFactory;
import org.openmrs.MedicationDispense;
import org.openmrs.api.db.MedicationDispenseDAO;
import org.openmrs.parameter.MedicationDispenseCriteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * Hibernate implementation of the MedicationDispenseDAO
 * @since 2.6.0
 * @see MedicationDispenseDAO
 */
public class HibernateMedicationDispenseDAO implements MedicationDispenseDAO {
	
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public MedicationDispense getMedicationDispense(Integer medicationDispenseId) {
		return sessionFactory.getCurrentSession().get(MedicationDispense.class, medicationDispenseId);
	}

	@Override
	public MedicationDispense getMedicationDispenseByUuid(String uuid) {
		return sessionFactory.getCurrentSession()
			.createQuery("select md from MedicationDispense md where md.uuid = :uuid", MedicationDispense.class)
			.setParameter("uuid", uuid)
			.uniqueResult();
	}

	@Override
	public List<MedicationDispense> getMedicationDispenseByCriteria(MedicationDispenseCriteria criteria) {
		CriteriaBuilder criteriaBuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<MedicationDispense> criteriaQuery = criteriaBuilder.createQuery(MedicationDispense.class);
		Root<MedicationDispense> md = criteriaQuery.from(MedicationDispense.class);
		List<Predicate> predicates = new ArrayList<>();
		if (criteria.getPatient() != null) {
			predicates.add(criteriaBuilder.equal(md.get("patient"), criteria.getPatient()));
		}
		if (criteria.getEncounter() != null) {
			predicates.add(criteriaBuilder.equal(md.get("encounter"), criteria.getEncounter()));
		}
		if (criteria.getDrugOrder() != null) {
			predicates.add(criteriaBuilder.equal(md.get("drugOrder"), criteria.getDrugOrder()));
		}
		if (!criteria.isIncludeVoided()) {
			predicates.add(criteriaBuilder.equal(md.get("voided"), false));
		}
		if (predicates.size() > 0) {
			criteriaQuery.where(predicates.toArray(new Predicate[]{}));
		}
		criteriaQuery.orderBy(criteriaBuilder.asc(md.get("medicationDispenseId")));
		return sessionFactory.getCurrentSession().createQuery(criteriaQuery).list();
	}

	@Override
	public MedicationDispense saveMedicationDispense(MedicationDispense medicationDispense) {
		sessionFactory.getCurrentSession().saveOrUpdate(medicationDispense);
		return medicationDispense;
	}

	@Override
	public void deleteMedicationDispense(MedicationDispense medicationDispense) {
		sessionFactory.getCurrentSession().delete(medicationDispense);
	}
}
