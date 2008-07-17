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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.engine.SessionFactoryImplementor;

/**
 * This class holds common methods and utilities that are used across
 * the hibernate related classes
 */
public class HibernateUtil {

	private static Log log = LogFactory.getLog(HibernateUtil.class);
	
	private static Dialect dialect = null;
	private static Boolean isHSQLDialect = null;
	
	/**
	 * Check and cache whether the currect dialect is HSQL or not.
	 * This is needed because some queries are different if in 
	 * the hsql world as opposed to the mysql/postgres world
	 * 
	 * @param sessionFactory
	 * @return true/false whether we're in hsql right now or not
	 */
	public static boolean isHSQLDialect(SessionFactory sessionFactory) {
		
		if (isHSQLDialect == null)
			// check and cache the dialect
			isHSQLDialect = HSQLDialect.class.getName().equals(
		                       getDialect(sessionFactory).getClass().getName());
		
		return isHSQLDialect;
	}
	
	/**
	 * Fetch the current Dialect of the given SessionFactory
	 * 
	 * @param sessionFactory SessionFactory to pull the dialect from
	 * @return Dialect of sql that this connection/session is using
	 */
	public static Dialect getDialect(SessionFactory sessionFactory) {
		
		// return cached dialect
		if (dialect != null)
			return dialect;
		
		SessionFactoryImplementor implementor = (SessionFactoryImplementor)sessionFactory;
		dialect = implementor.getDialect();
		
		if (log.isDebugEnabled())
			log.debug("Getting dialect for session: " + dialect);
		
		return dialect;
	}
}