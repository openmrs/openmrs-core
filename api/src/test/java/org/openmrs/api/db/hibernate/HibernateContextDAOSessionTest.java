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

import java.sql.Connection;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.jdbc.Work;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests which sessions {@link HibernateContextDAO#openSession()},
 * {@link HibernateContextDAO#closeSession()} and {@link HibernateContextDAO#releaseThreadSession()}
 * bind and release, without a Spring context.
 */
public class HibernateContextDAOSessionTest {

	private SessionFactory sessionFactory;

	private HibernateContextDAO dao;

	@BeforeEach
	public void setUp() {
		sessionFactory = mock(SessionFactory.class);
		dao = new HibernateContextDAO();
		dao.setSessionFactory(sessionFactory);
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

	@Test
	public void releaseThreadSession_shouldReturnFalseWhenNoSessionIsBound() {
		assertFalse(dao.releaseThreadSession());
		assertFalse(TransactionSynchronizationManager.hasResource(sessionFactory));
	}

	@Test
	public void releaseThreadSession_shouldReleaseASessionLeftOpenByUnmatchedOpens() throws Exception {
		// a task opens its own session and throws before closing it, inside the session its runner opened
		Connection connection = mock(Connection.class);
		Session leaked = openSessionMock(connection, false);
		when(sessionFactory.openSession()).thenReturn(leaked);
		dao.openSession();
		dao.openSession();
		dao.closeSession();
		assertTrue(TransactionSynchronizationManager.hasResource(sessionFactory));

		assertTrue(dao.releaseThreadSession());

		assertFalse(TransactionSynchronizationManager.hasResource(sessionFactory));
		verify(connection).rollback();
		verify(leaked).close();
	}

	@Test
	public void releaseThreadSession_shouldLetTheNextUnitOfWorkOpenAFreshSession() throws Exception {
		Session leaked = openSessionMock(mock(Connection.class), false);
		Session fresh = openSessionMock(mock(Connection.class), false);
		when(sessionFactory.openSession()).thenReturn(leaked, fresh);
		dao.openSession();
		dao.openSession();
		dao.closeSession();

		dao.releaseThreadSession();
		dao.openSession();

		SessionHolder holder = (SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
		assertSame(fresh, holder.getSession());
		assertNotSame(leaked, holder.getSession());
		dao.closeSession();
		assertFalse(TransactionSynchronizationManager.hasResource(sessionFactory));
		verify(fresh).close();
	}

	@Test
	public void releaseThreadSession_shouldRollBackAnActiveHibernateTransaction() throws Exception {
		Session session = openSessionMock(mock(Connection.class), true);
		Transaction transaction = session.getTransaction();
		when(sessionFactory.openSession()).thenReturn(session);
		dao.openSession();

		assertTrue(dao.releaseThreadSession());

		verify(transaction).rollback();
		verify(session).close();
	}

	@Test
	public void releaseThreadSession_shouldNotRollBackAConnectionInAutoCommitMode() throws Exception {
		Connection connection = mock(Connection.class);
		when(connection.getAutoCommit()).thenReturn(true);
		Session session = openSessionMock(connection, false);
		when(sessionFactory.openSession()).thenReturn(session);
		dao.openSession();

		assertTrue(dao.releaseThreadSession());

		verify(connection, never()).rollback();
		verify(session).close();
	}

	@Test
	public void releaseThreadSession_shouldCloseTheSessionWhenTheRollbackFails() throws Exception {
		Session session = openSessionMock(mock(Connection.class), true);
		Transaction transaction = session.getTransaction();
		doThrow(new IllegalStateException("connection broken")).when(transaction).rollback();
		when(sessionFactory.openSession()).thenReturn(session);
		dao.openSession();

		assertTrue(dao.releaseThreadSession());

		assertFalse(TransactionSynchronizationManager.hasResource(sessionFactory));
		verify(session).close();
	}

	@Test
	public void releaseThreadSession_shouldUnbindTheSessionWhenTheCloseFails() throws Exception {
		Session session = openSessionMock(mock(Connection.class), false);
		doThrow(new IllegalStateException("close failed")).when(session).close();
		when(sessionFactory.openSession()).thenReturn(session);
		dao.openSession();

		assertTrue(dao.releaseThreadSession());

		assertFalse(TransactionSynchronizationManager.hasResource(sessionFactory));
	}

	@Test
	public void releaseThreadSession_shouldNotReleaseASessionBoundByAnotherOwner() {
		// as a Spring transaction or OpenSessionInViewFilter binds its session
		Session session = mock(Session.class);
		SessionHolder holder = new SessionHolder(session);
		TransactionSynchronizationManager.bindResource(sessionFactory, holder);

		assertFalse(dao.releaseThreadSession());

		assertSame(holder, TransactionSynchronizationManager.getResource(sessionFactory));
		verify(session, never()).close();
	}

	/**
	 * A session as {@link SessionFactory#openSession()} hands it out, whose {@link Session#doWork(Work)}
	 * runs against the given connection.
	 */
	private Session openSessionMock(Connection connection, boolean transactionActive) throws Exception {
		Session session = mock(Session.class);
		Transaction transaction = mock(Transaction.class);
		when(session.isOpen()).thenReturn(true);
		when(session.getTransaction()).thenReturn(transaction);
		when(transaction.isActive()).thenReturn(transactionActive);
		doAnswer(invocation -> {
			((Work) invocation.getArgument(0)).execute(connection);
			return null;
		}).when(session).doWork(any(Work.class));
		return session;
	}
}
