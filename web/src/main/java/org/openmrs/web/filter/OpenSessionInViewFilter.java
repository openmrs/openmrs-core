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
 * Servlet filter that binds a Hibernate Session to the thread for the entire processing of the
 * request. Intended for the "Open Session in View" pattern, i.e. to allow for lazy loading in web
 * views despite the original transactions already being completed.
 * <p>
 * This filter binds a {@link SessionHolder} to the {@link TransactionSynchronizationManager},
 * which is compatible with {@link org.springframework.orm.jpa.hibernate.HibernateTransactionManager}.
 * <p>
 * This replaces the use of {@link org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter}
 * which binds an {@link org.springframework.orm.jpa.EntityManagerHolder} that is incompatible with
 * {@code HibernateTransactionManager}, causing a {@code ClassCastException}.
 *
 * @since 3.0.0
 */
public class OpenSessionInViewFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(OpenSessionInViewFilter.class);

	public static final String DEFAULT_SESSION_FACTORY_BEAN_NAME = "sessionFactory";

	private String sessionFactoryBeanName = DEFAULT_SESSION_FACTORY_BEAN_NAME;

	private volatile SessionFactory sessionFactory;

	/**
	 * Set the bean name of the SessionFactory to fetch from Spring's root application context.
	 * Default is "sessionFactory".
	 *
	 * @param sessionFactoryBeanName the bean name of the SessionFactory
	 */
	public void setSessionFactoryBeanName(String sessionFactoryBeanName) {
		this.sessionFactoryBeanName = sessionFactoryBeanName;
	}

	/**
	 * Return the bean name of the SessionFactory to fetch from Spring's root application context.
	 */
	protected String getSessionFactoryBeanName() {
		return this.sessionFactoryBeanName;
	}

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

		SessionFactory sf = lookupSessionFactory(request);
		boolean participate = false;

		if (TransactionSynchronizationManager.hasResource(sf)) {
			// Do not modify the Session: just set the participate flag.
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
				SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.unbindResource(sf);
				log.debug("Closing Hibernate Session in OpenSessionInViewFilter");
				sessionHolder.getSession().close();
			}
		}
	}

	/**
	 * Look up the SessionFactory that this filter should use.
	 */
	protected SessionFactory lookupSessionFactory(HttpServletRequest request) {
		SessionFactory sf = this.sessionFactory;
		if (sf == null) {
			sf = lookupSessionFactory();
			this.sessionFactory = sf;
		}
		return sf;
	}

	/**
	 * Look up the SessionFactory from the Spring root application context.
	 */
	protected SessionFactory lookupSessionFactory() {
		WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
		return wac.getBean(getSessionFactoryBeanName(), SessionFactory.class);
	}
}
