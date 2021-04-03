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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Obs;
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
		
		List<Obs> obsListActual;
		List<Obs> obsListExpected;
		
		//Order by id desc
		obsListExpected = session.createCriteria(Obs.class, "obs").addOrder(Order.desc("id")).list();
		
		obsListActual = dao.getObservations(null, null, null, null, null, null, Collections.singletonList("id"), null, null, null, null,
		    false, null);
		assertArrayEquals(obsListExpected.toArray(), obsListActual.toArray());
		
		obsListActual = dao.getObservations(null, null, null, null, null, null, Collections.singletonList("id desc"), null, null, null,
		    null, false, null);
		assertArrayEquals(obsListExpected.toArray(), obsListActual.toArray());
		
		//Order by id asc
		obsListExpected = session.createCriteria(Obs.class, "obs").addOrder(Order.asc("id")).list();
		obsListActual = dao.getObservations(null, null, null, null, null, null, Collections.singletonList("id asc"), null, null, null,
		    null, false, null);
		assertArrayEquals(obsListExpected.toArray(), obsListActual.toArray());
		
		//Order by person_id asc and id desc
		obsListExpected = session.createCriteria(Obs.class, "obs").addOrder(Order.asc("person.id")).addOrder(Order.desc("id")).list();
		
		obsListActual = dao.getObservations(null, null, null, null, null, null, Arrays.asList("person.id asc", "id"), null,
		    null, null, null, false, null);
		assertArrayEquals(obsListExpected.toArray(), obsListActual.toArray());
		
		//Order by person_id asc and id asc
		obsListExpected = session.createCriteria(Obs.class, "obs").addOrder(Order.asc("person.id"))
		        .addOrder(Order.asc("id")).list();
		
		obsListActual = dao.getObservations(null, null, null, null, null, null, Arrays.asList("person.id asc", "id asc"),
		    null, null, null, null, false, null);
		assertArrayEquals(obsListExpected.toArray(), obsListActual.toArray());
	}
}
