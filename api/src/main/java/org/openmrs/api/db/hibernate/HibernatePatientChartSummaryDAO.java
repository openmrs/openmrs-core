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

import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;
import org.hibernate.SessionFactory;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.db.PatientChartSummaryDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Hibernate implementation of the {@link PatientChartSummaryDAO}.
 *
 * @since 3.0.0
 */
@Repository("patientChartSummaryDAO")
public class HibernatePatientChartSummaryDAO implements PatientChartSummaryDAO {
	
	private final SessionFactory sessionFactory;
	
	@Autowired
	public HibernatePatientChartSummaryDAO(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see PatientChartSummaryDAO#getEncounters(Patient, Date, Date)
	 */
	@Override
	public List<Encounter> getEncounters(Patient patient, Date fromDate, Date toDate) {
		StringBuilder hql = new StringBuilder("from Encounter e where e.patient.patientId = :patientId and e.voided = false");
		appendDateFilter(hql, "e.encounterDatetime");
		hql.append(" order by e.encounterDatetime desc");
		
		TypedQuery<Encounter> query = sessionFactory.getCurrentSession().createQuery(hql.toString(), Encounter.class);
		query.setParameter("patientId", patient.getId());
		setDateParameters(query, fromDate, toDate);
		return query.getResultList();
	}
	
	/**
	 * @see PatientChartSummaryDAO#getObservations(Patient, Date, Date)
	 */
	@Override
	public List<Obs> getObservations(Patient patient, Date fromDate, Date toDate) {
		StringBuilder hql = new StringBuilder("from Obs o where o.person.personId = :patientId and o.voided = false");
		appendDateFilter(hql, "o.obsDatetime");
		hql.append(" order by o.obsDatetime desc");
		
		TypedQuery<Obs> query = sessionFactory.getCurrentSession().createQuery(hql.toString(), Obs.class);
		query.setParameter("patientId", patient.getId());
		setDateParameters(query, fromDate, toDate);
		return query.getResultList();
	}
	
	/**
	 * @see PatientChartSummaryDAO#getOrders(Patient, Date, Date)
	 */
	@Override
	public List<Order> getOrders(Patient patient, Date fromDate, Date toDate) {
		StringBuilder hql = new StringBuilder("from Order o where o.patient.patientId = :patientId and o.voided = false");
		appendDateFilter(hql, "o.dateActivated");
		hql.append(" order by o.dateActivated desc");
		
		TypedQuery<Order> query = sessionFactory.getCurrentSession().createQuery(hql.toString(), Order.class);
		query.setParameter("patientId", patient.getId());
		setDateParameters(query, fromDate, toDate);
		return query.getResultList();
	}
	
	/**
	 * @see PatientChartSummaryDAO#getVisits(Patient, Date, Date)
	 */
	@Override
	public List<Visit> getVisits(Patient patient, Date fromDate, Date toDate) {
		StringBuilder hql = new StringBuilder("from Visit v where v.patient.patientId = :patientId and v.voided = false");
		appendDateFilter(hql, "v.startDatetime");
		hql.append(" order by v.startDatetime desc");
		
		TypedQuery<Visit> query = sessionFactory.getCurrentSession().createQuery(hql.toString(), Visit.class);
		query.setParameter("patientId", patient.getId());
		setDateParameters(query, fromDate, toDate);
		return query.getResultList();
	}
	
	private void appendDateFilter(StringBuilder hql, String dateField) {
		hql.append(" and (:fromDate is null or ").append(dateField).append(" >= :fromDate)");
		hql.append(" and (:toDate is null or ").append(dateField).append(" <= :toDate)");
	}
	
	private void setDateParameters(TypedQuery<?> query, Date fromDate, Date toDate) {
		query.setParameter("fromDate", fromDate);
		query.setParameter("toDate", toDate);
	}
}
