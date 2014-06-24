/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api.db.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Order;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

import java.util.Arrays;
import java.util.List;

/**
 * Tests for {@link org.openmrs.api.db.hibernate.HibernateObsDAO}
 */
public class HibernateObsDAOTest extends BaseContextSensitiveTest {
	
	private HibernateObsDAO dao = null;
	
	private SessionFactory sessionFactory = null;
	
	@Before
	public void setUp() throws Exception {
		if (dao == null)
			// fetch the dao from the spring application context
			dao = (HibernateObsDAO) applicationContext.getBean("obsDAO");
		
		sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory");
	}
	
	/**
	 * @verifies the order of the fetched Obs is proper according to the specified sort options
	 * @see org.openmrs.api.db.hibernate.HibernateObsDAO#getObservations(java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, Integer, Integer, java.util.Date, java.util.Date, boolean)
	 */
	@Test
	@Verifies(value = "the order of the fetched Obs is proper according to the specified sort options", method = "getObservations(java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, Integer, Integer, java.util.Date, java.util.Date, boolean)")
	public void getObservations_shouldBeOrderedCorrectly() throws Exception {
		Session session = sessionFactory.getCurrentSession();
		
		List<Obs> obsListActual;
		List<Obs> obsListExpected;
		
		//Order by id desc
		obsListExpected = session.createCriteria(Obs.class, "obs").addOrder(Order.desc("id")).list();
		
		obsListActual = dao.getObservations(null, null, null, null, null, null, Arrays.asList("id"), null, null, null, null,
		    false, null);
		Assert.assertArrayEquals(obsListExpected.toArray(), obsListActual.toArray());
		
		obsListActual = dao.getObservations(null, null, null, null, null, null, Arrays.asList("id desc"), null, null, null,
		    null, false, null);
		Assert.assertArrayEquals(obsListExpected.toArray(), obsListActual.toArray());
		
		//Order by id asc
		obsListExpected = session.createCriteria(Obs.class, "obs").addOrder(Order.asc("id")).list();
		obsListActual = dao.getObservations(null, null, null, null, null, null, Arrays.asList("id asc"), null, null, null,
		    null, false, null);
		Assert.assertArrayEquals(obsListExpected.toArray(), obsListActual.toArray());
		
		//Order by person_id asc and id desc
		obsListExpected = session.createCriteria(Obs.class, "obs").addOrder(Order.asc("person.id")).addOrder(
		    Order.desc("id")).list();
		
		obsListActual = dao.getObservations(null, null, null, null, null, null, Arrays.asList("person.id asc", "id"), null,
		    null, null, null, false, null);
		Assert.assertArrayEquals(obsListExpected.toArray(), obsListActual.toArray());
		
		//Order by person_id asc and id asc
		obsListExpected = session.createCriteria(Obs.class, "obs").addOrder(Order.asc("person.id"))
		        .addOrder(Order.asc("id")).list();
		
		obsListActual = dao.getObservations(null, null, null, null, null, null, Arrays.asList("person.id asc", "id asc"),
		    null, null, null, null, false, null);
		Assert.assertArrayEquals(obsListExpected.toArray(), obsListActual.toArray());
	}
}
