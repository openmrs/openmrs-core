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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.hibernate.SessionHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests how {@link HibernateContextDAO#closeSession()} behaves when a transaction runs on the
 * session {@link HibernateContextDAO#openSession()} bound. The session must be bound before any
 * transaction starts, so the tests cannot run in one.
 */
@SkipBaseSetup
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class HibernateContextDAOTransactionTest extends BaseContextSensitiveTest {

	@Autowired
	private ContextDAO contextDAO;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private PlatformTransactionManager transactionManager;

	private TransactionTemplate transaction;

	@BeforeEach
	public void releaseBaseSession() {
		// release any session the base class opened, so each session a test opens is closed by one call
		Context.closeSession();
		assertNull(TransactionSynchronizationManager.getResource(sessionFactory));
		transaction = new TransactionTemplate(transactionManager);
	}

	@Test
	public void closeSession_shouldIgnoreAnUnmatchedCloseInsideATransactionOnItsSession() {
		contextDAO.openSession();
		SessionHolder holder = (SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
		try {
			transaction.executeWithoutResult(status -> {
				assertSame(holder.getSession(), sessionFactory.getCurrentSession());
				contextDAO.closeSession();
			});

			assertSessionStillUsable(holder);
		} finally {
			contextDAO.closeSession();
		}

		assertNull(TransactionSynchronizationManager.getResource(sessionFactory));
	}

	@Test
	public void closeSession_shouldIgnoreAnUnmatchedCloseInATransactionsCompletionCallback() {
		contextDAO.openSession();
		SessionHolder holder = (SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
		try {
			transaction.executeWithoutResult(
			    status -> TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

				    @Override
				    public void afterCompletion(int completionStatus) {
					    contextDAO.closeSession();
				    }
			    }));

			assertSessionStillUsable(holder);
		} finally {
			contextDAO.closeSession();
		}

		assertNull(TransactionSynchronizationManager.getResource(sessionFactory));
	}

	@Test
	public void closeSession_shouldCloseItsSessionDespiteAHibernateTransactionLeftOpen() {
		contextDAO.openSession();
		SessionHolder holder = (SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
		holder.getSession().beginTransaction();

		contextDAO.closeSession();

		assertNull(TransactionSynchronizationManager.getResource(sessionFactory));
		assertFalse(holder.getSession().isOpen());
		transaction.executeWithoutResult(
		    status -> sessionFactory.getCurrentSession().createNativeQuery("select 1", Integer.class).getSingleResult());
	}

	private void assertSessionStillUsable(SessionHolder holder) {
		assertSame(holder, TransactionSynchronizationManager.getResource(sessionFactory));
		assertTrue(holder.getSession().isOpen());
		transaction.executeWithoutResult(
		    status -> sessionFactory.getCurrentSession().createNativeQuery("select 1", Integer.class).getSingleResult());
	}
}
