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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.hibernate.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter that binds a Hibernate {@link Session} to the thread for the entire processing
 * of the request, enabling the "Open Session in View" pattern for lazy loading in web views.
 * <p>
 * This filter binds a {@link SessionHolder} (rather than an {@code EntityManagerHolder}) to ensure
 * compatibility with {@link org.springframework.orm.jpa.hibernate.HibernateTransactionManager},
 * which expects a {@link SessionHolder} in {@link TransactionSynchronizationManager}.
 *
 * @since 3.0.0
 */
public class OpenSessionInViewFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(OpenSessionInViewFilter.class);

	private volatile SessionFactory sessionFactory;

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

		SessionFactory sf = lookupSessionFactory();
		boolean participate = false;

		if (TransactionSynchronizationManager.hasResource(sf)) {
			participate = true;
		} else {
			log.debug("Opening Hibernate Session in OpenSessionInViewFilter");
			Session session = sf.openSession();
			session.setHibernateFlushMode(FlushMode.MANUAL);
			TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
		}

		try {
			filterChain.doFilter(request, response);
		} finally {
			if (!participate) {
				SessionHolder holder = (SessionHolder) TransactionSynchronizationManager.unbindResource(sf);
				log.debug("Closing Hibernate Session in OpenSessionInViewFilter");
				holder.getSession().close();
			}
		}
	}

	private SessionFactory lookupSessionFactory() {
		SessionFactory sf = this.sessionFactory;
		if (sf == null) {
			WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
			sf = wac.getBean(SessionFactory.class);
			this.sessionFactory = sf;
		}
		return sf;
	}
}
