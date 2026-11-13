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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
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

	@Test
	public void getObsVersionHistory_shouldReturnTheSameChainWithSelfJoinAsWithRecursiveCte() {
		// chain lengths around the self-join depth: one query covers VERSION_CHAIN_JOIN_DEPTH + 1 obs
		int perQuery = HibernateObsDAO.VERSION_CHAIN_JOIN_DEPTH + 1;
		for (int length : new int[] { 1, perQuery, perQuery + 1, 2 * perQuery + 1 }) {
			Obs latest = buildVersionChain(length);
			List<Integer> viaCte = obsIds(dao.getObsVersionHistory(latest, true));
			List<Integer> viaSelfJoin = obsIds(dao.getObsVersionHistory(latest, false));
			assertEquals(length, viaSelfJoin.size(), "chain length " + length);
			assertEquals(latest.getObsId(), viaSelfJoin.get(0));
			assertEquals(viaCte, viaSelfJoin, "chain length " + length);
		}
	}
	
	@Test
	public void getObsVersionHistory_shouldIssueOneSelfJoinQueryPerJoinDepthOfVersions() {
		int perQuery = HibernateObsDAO.VERSION_CHAIN_JOIN_DEPTH + 1;
		Obs latest = buildVersionChain(2 * perQuery + 1);
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
		Statistics stats = sessionFactory.getStatistics();
		stats.clear();
		
		dao.getObsVersionHistory(latest, false);
		
		// three self-join queries plus the IN-clause load
		assertEquals(4, stats.getPrepareStatementCount());
	}
	
	@Test
	public void getObsVersionHistory_shouldReturnEmptyListWithSelfJoinForObsIdWithNoMatchingRow() {
		assertTrue(dao.getObsVersionHistory(new Obs(999999), false).isEmpty());
	}
	
	@Test
	public void supportsRecursiveCte_shouldDependOnDatabaseAndVersion() throws SQLException {
		assertFalse(HibernateObsDAO.supportsRecursiveCte(metaData("MySQL", "5.7.44", 5)));
		assertTrue(HibernateObsDAO.supportsRecursiveCte(metaData("MySQL", "8.0.46", 8)));
		assertFalse(HibernateObsDAO.supportsRecursiveCte(metaData("MySQL", "5.5.5-10.1.48-MariaDB-1~bionic", 5)));
		assertFalse(HibernateObsDAO.supportsRecursiveCte(metaData("MySQL", "5.5.5-10.2.1-MariaDB", 5)));
		assertTrue(HibernateObsDAO.supportsRecursiveCte(metaData("MySQL", "5.5.5-10.2.2-MariaDB", 5)));
		assertTrue(HibernateObsDAO.supportsRecursiveCte(metaData("MariaDB", "10.6.12-MariaDB-1:10.6.12+maria~ubu2004", 10)));
		assertTrue(HibernateObsDAO.supportsRecursiveCte(metaData("MariaDB", "11.4.2-MariaDB", 11)));
		assertTrue(HibernateObsDAO.supportsRecursiveCte(metaData("PostgreSQL", "12.22", 12)));
		assertTrue(HibernateObsDAO.supportsRecursiveCte(metaData("H2", "2.2.224 (2023-09-17)", 2)));
	}
	
	private static DatabaseMetaData metaData(String productName, String productVersion, int majorVersion)
	        throws SQLException {
		DatabaseMetaData metaData = mock(DatabaseMetaData.class, withSettings().lenient());
		when(metaData.getDatabaseProductName()).thenReturn(productName);
		when(metaData.getDatabaseProductVersion()).thenReturn(productVersion);
		when(metaData.getDatabaseMajorVersion()).thenReturn(majorVersion);
		return metaData;
	}
	
	/** Saves a copy of obs 7 from the standard test dataset and revises it until the chain has the given number of obs. */
	private Obs buildVersionChain(int length) {
		ObsService obsService = Context.getObsService();
		Obs latest = obsService.saveObs(Obs.newInstance(obsService.getObs(7)), null);
		for (int i = 1; i < length; i++) {
			latest.setValueNumeric(50.0 + i);
			latest = obsService.saveObs(latest, "revision " + i);
		}
		return latest;
	}
	
	private static List<Integer> obsIds(List<Obs> obs) {
		return obs.stream().map(Obs::getObsId).collect(Collectors.toList());
	}
}
