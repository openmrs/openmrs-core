/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.jpa.hibernate.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Open Session in View filter that binds a Hibernate {@link SessionHolder} to the thread for the
 * entire request to allow lazy loading in web views.
 * <p>
 * In Spring 7, the built-in {@code OpenSessionInViewFilter} was removed. The standard
 * {@code OpenEntityManagerInViewFilter} binds an {@code EntityManagerHolder} which is incompatible
 * with {@code HibernateTransactionManager} (causes {@code ClassCastException}), and switching to
 * {@code JpaTransactionManager} causes deadlocks with {@code REQUIRES_NEW} propagation. This
 * minimal filter resolves both issues.
 *
 * @since 3.0.0
 */
public class OpenSessionInViewFilter extends OncePerRequestFilter {

	@Override
	protected boolean shouldNotFilterAsyncDispatch() {
		return false;
	}

	@Override
	protected boolean shouldNotFilterErrorDispatch() {
		return false;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		SessionFactory sf = WebApplicationContextUtils
			.getRequiredWebApplicationContext(getServletContext())
			.getBean("sessionFactory", SessionFactory.class);

		if (TransactionSynchronizationManager.hasResource(sf)) {
			filterChain.doFilter(request, response);
			return;
		}

		Session session = sf.openSession();
		session.setHibernateFlushMode(FlushMode.MANUAL);
		SessionHolder holder = new SessionHolder(session);
		TransactionSynchronizationManager.bindResource(sf, holder);
		try {
			filterChain.doFilter(request, response);
		} finally {
			TransactionSynchronizationManager.unbindResource(sf);
			session.close();
		}
	}
}
