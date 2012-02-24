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

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;

/**
 * Sets the flush mode to COMMIT for all transactions in the AUTO or ALWAYS modes.
 */
public class HibernateTransactionManagerFlushOnCommit extends HibernateTransactionManager {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * @see org.springframework.orm.hibernate3.HibernateTransactionManager#doBegin(java.lang.Object,
	 *      org.springframework.transaction.TransactionDefinition)
	 */
	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition) {
		super.doBegin(transaction, definition);
		Session session = getSessionFactory().getCurrentSession();
		if (!session.getFlushMode().lessThan(FlushMode.COMMIT)) {
			session.setFlushMode(FlushMode.COMMIT);
		}
	}
	
}
