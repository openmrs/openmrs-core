/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.cache;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.hibernate.SessionFactory;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;
import org.junit.jupiter.api.Test;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.api.db.UserDAO;
import org.openmrs.api.db.hibernate.search.session.SearchSessionFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * Callers such as Initializer flush the session through {@code Context.flushSession()} outside a
 * transaction, which only works while {@code contextDAO} has its transaction proxy. Creating the
 * {@link RolePrivilegeCache} must not create {@code contextDAO}, since the cache can be created
 * while advisors are still being collected, before {@code contextDAO} could be proxied.
 */
public class RolePrivilegeCacheContextDAOProxyTest {

	/**
	 * Matches nothing. Autowiring it by type makes Spring create the service factory beans, and so
	 * the {@link RolePrivilegeCache}, while advisors are being collected.
	 */
	public static class TypeMatchingAdvisor extends StaticMethodMatcherPointcutAdvisor {

		@Autowired(required = false)
		public TypeMatchingAdvisor(TransactionInterceptor transactionInterceptor) {
		}

		@Override
		public boolean matches(Method method, Class<?> targetClass) {
			return false;
		}
	}

	public static class ServiceTarget {

		public void setRolePrivilegeCache(RolePrivilegeCache rolePrivilegeCache) {
		}
	}

	public static PlatformTransactionManager transactionManager() {
		return mock(PlatformTransactionManager.class);
	}

	public static UserDAO userDAO() {
		return mock(UserDAO.class);
	}

	public static SessionFactory sessionFactory() {
		return mock(SessionFactory.class);
	}

	public static SearchSessionFactory searchSessionFactory() {
		return mock(SearchSessionFactory.class);
	}

	public static SpringEmbeddedCacheManager cacheManager() {
		SpringEmbeddedCacheManager cacheManager = mock(SpringEmbeddedCacheManager.class);
		when(cacheManager.getNativeCacheManager()).thenReturn(mock(EmbeddedCacheManager.class));
		return cacheManager;
	}

	@Test
	public void contextDAO_shouldKeepItsTransactionProxyWhenAdvisorsCreateServicesEarly() {
		try (ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
		        "org/openmrs/api/cache/RolePrivilegeCacheContextDAOProxyTest-context.xml")) {
			ContextDAO contextDAO = context.getBean("contextDAO", ContextDAO.class);
			assertTrue(AopUtils.isAopProxy(contextDAO), contextDAO.getClass().getName());
		}
	}
}
