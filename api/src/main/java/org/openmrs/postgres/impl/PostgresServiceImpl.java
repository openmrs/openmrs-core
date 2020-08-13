/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.postgres.impl;


import org.openmrs.postgres.PostgresService;
import org.openmrs.postgres.db.PostgresDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Database specific functions related to PostgreSQL
 */
@Transactional
public class PostgresServiceImpl implements PostgresService {
	
	private static final Logger log = LoggerFactory.getLogger(PostgresServiceImpl.class);
	
	private PostgresDAO postgresDao;
	
	/**
	 * Default constructor
	 */
	public PostgresServiceImpl() {
	}
	
	/**
	 * @see org.openmrs.postgres.PostgresService#setPostgresDAO(org.openmrs.postgres.db.PostgresDAO)
	 */
	@Override
	public void setPostgresDAO(PostgresDAO dao) {
		this.postgresDao = dao;
	}
	
	/**
	 * @see org.openmrs.postgres.PostgresService#updateAllSequence()
	 */
	@Override
	public void updateAllSequence() {
		postgresDao.updateSequence();
	}
	
}
