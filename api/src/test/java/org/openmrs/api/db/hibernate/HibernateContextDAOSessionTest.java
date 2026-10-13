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

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.db.UserDAO;
import org.openmrs.api.db.hibernate.search.session.SearchSessionFactory;
import org.springframework.orm.jpa.hibernate.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests which sessions {@link HibernateContextDAO#openSession()} and
 * {@link HibernateContextDAO#closeSession()} bind and release, without a Spring context.
 */
public class HibernateContextDAOSessionTest {

	private SessionFactory sessionFactory;

	private HibernateContextDAO dao;

	@BeforeEach
	public void setUp() {
		sessionFactory = mock(SessionFactory.class);
		dao = new HibernateContextDAO(sessionFactory, mock(SearchSessionFactory.class), mock(UserDAO.class));
	}

	@AfterEach
	public void tearDown() {
		TransactionSynchronizationManager.unbindResourceIfPossible(sessionFactory);
	}

	@Test
	public void closeSession_shouldCloseTheSessionOpenSessionBound() {
		Session session = mock(Session.class);
		when(sessionFactory.openSession()).thenReturn(session);

		dao.openSession();
		assertTrue(TransactionSynchronizationManager.hasResource(sessionFactory));
		dao.closeSession();

		assertFalse(TransactionSynchronizationManager.hasResource(sessionFactory));
		verify(session).close();
	}

	@Test
	public void closeSession_shouldCloseANestedSessionOnlyOnTheOutermostClose() {
		Session session = mock(Session.class);
		when(sessionFactory.openSession()).thenReturn(session);

		dao.openSession();
		dao.openSession();
		dao.closeSession();

		assertTrue(TransactionSynchronizationManager.hasResource(sessionFactory));
		verify(session, never()).close();

		dao.closeSession();

		assertFalse(TransactionSynchronizationManager.hasResource(sessionFactory));
		verify(session).close();
	}

	@Test
	public void closeSession_shouldCloseItsSessionDespiteAnUnclosedJoinOfAnotherSession() {
		// a join of a transaction's session that is never closed, as when the caller throws first
		SessionHolder transactionHolder = new SessionHolder(mock(Session.class));
		TransactionSynchronizationManager.bindResource(sessionFactory, transactionHolder);
		dao.openSession();
		TransactionSynchronizationManager.unbindResource(sessionFactory);

		Session session = mock(Session.class);
		when(sessionFactory.openSession()).thenReturn(session);
		dao.openSession();
		dao.closeSession();

		assertFalse(TransactionSynchronizationManager.hasResource(sessionFactory));
		verify(session).close();
	}

	@Test
	public void closeSession_shouldNotCloseASessionThisThreadDidNotOpen() {
		// as a Spring transaction or the web layer binds its session
		Session session = mock(Session.class);
		SessionHolder holder = new SessionHolder(session);
		TransactionSynchronizationManager.bindResource(sessionFactory, holder);

		dao.closeSession();

		assertSame(holder, TransactionSynchronizationManager.getResource(sessionFactory));
		verify(session, never()).close();
	}

	@Test
	public void closeSession_shouldLeaveASessionItJoinedToItsOwner() {
		Session session = mock(Session.class);
		SessionHolder holder = new SessionHolder(session);
		TransactionSynchronizationManager.bindResource(sessionFactory, holder);

		dao.openSession();
		dao.closeSession();

		assertSame(holder, TransactionSynchronizationManager.getResource(sessionFactory));
		verify(session, never()).close();
		verify(sessionFactory, never()).openSession();
	}
}
