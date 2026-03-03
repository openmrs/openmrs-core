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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

/**
 * Tests for {@link org.openmrs.api.db.hibernate.HibernateObsDAO}
 */
public class HibernateObsDAOTest extends BaseContextSensitiveTest {
	
	private HibernateObsDAO dao = null;
	
	private SessionFactory sessionFactory = null;
	
	@BeforeEach
	public void setUp() {
		if (dao == null)
			// fetch the dao from the spring application context
			dao = (HibernateObsDAO) applicationContext.getBean("obsDAO");
		
		sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory");
	}
	
	/**
	 * @see org.openmrs.api.db.hibernate.HibernateObsDAO#getObservations(java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, Integer, Integer, java.util.Date, java.util.Date, boolean)
	 */
	@Test
	public void getObservations_shouldBeOrderedCorrectly() {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		List<Obs> obsListActual;
		List<Obs> obsListExpected;

		//Order by id desc
		CriteriaQuery<Obs> cqDesc = cb.createQuery(Obs.class);
		Root<Obs> rootDesc = cqDesc.from(Obs.class);
		cqDesc.orderBy(cb.desc(rootDesc.get("obsId")));
		obsListExpected = session.createQuery(cqDesc).getResultList();

		obsListActual = dao.getObservations(null, null, null, null, null, null, Collections.singletonList("obsId desc"), null, null, null,
			null, false, null);
		assertArrayEquals(obsListExpected.toArray(), obsListActual.toArray());

		//Order by obsId asc
		CriteriaQuery<Obs> cqAsc = cb.createQuery(Obs.class);
		Root<Obs> rootAsc = cqAsc.from(Obs.class);
		cqAsc.orderBy(cb.asc(rootAsc.get("obsId")));
		obsListExpected = session.createQuery(cqAsc).getResultList();

		obsListActual = dao.getObservations(null, null, null, null, null, null, Collections.singletonList("obsId asc"), null, null, null,
			null, false, null);
		assertArrayEquals(obsListExpected.toArray(), obsListActual.toArray());

		// Order by person_id asc and id desc
		CriteriaQuery<Obs> cqAscDesc = cb.createQuery(Obs.class);
		Root<Obs> rootAscDesc = cqAscDesc.from(Obs.class);
		Join<Obs, Person> personJoinAscDesc = rootAscDesc.join("person");
		cqAscDesc.orderBy(cb.asc(personJoinAscDesc.get("personId")), cb.desc(rootAscDesc.get("obsId")));
		obsListExpected = session.createQuery(cqAscDesc).getResultList();

		obsListActual = dao.getObservations(null, null, null, null, null, null, Arrays.asList("personId asc", "obsId desc"), null,
			null, null, null, false, null);
		assertArrayEquals(obsListExpected.toArray(), obsListActual.toArray());

		//Order by person_id asc and id asc
		CriteriaQuery<Obs> cqAscAsc = cb.createQuery(Obs.class);
		Root<Obs> rootAscAsc = cqAscAsc.from(Obs.class);
		Join<Obs, Person> personJoinAscAsc = rootAscAsc.join("person");
		cqAscAsc.orderBy(cb.asc(personJoinAscAsc.get("personId")), cb.asc(rootAscAsc.get("obsId")));
		obsListExpected = session.createQuery(cqAscAsc).getResultList();

		obsListActual = dao.getObservations(null, null, null, null, null, null, Arrays.asList("personId asc", "obsId asc"),
			null, null, null, null, false, null);
		assertArrayEquals(obsListExpected.toArray(), obsListActual.toArray());
	}
}
