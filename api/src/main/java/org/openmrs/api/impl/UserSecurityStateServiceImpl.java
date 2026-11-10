/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import org.openmrs.User;
import org.openmrs.UserSecurityState;
import org.openmrs.api.UserSecurityStateService;
import org.openmrs.api.db.UserSecurityStateDAO;
import org.openmrs.validator.ValidateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link UserSecurityStateService}.
 *
 * @since 3.0.0
 */
@Service("userSecurityStateService")
@Transactional(isolation = Isolation.READ_COMMITTED)
public class UserSecurityStateServiceImpl extends BaseOpenmrsService implements UserSecurityStateService {

	private final UserSecurityStateDAO dao;

	@Autowired
	public UserSecurityStateServiceImpl(UserSecurityStateDAO dao) {
		this.dao = dao;
	}

	/**
	 * @see UserSecurityStateService#getValue(User, String)
	 */
	@Override
	@Transactional(readOnly = true)
	public String getValue(User user, String property) {
		return dao.getValue(user.getUserId(), property);
	}

	/**
	 * @see UserSecurityStateService#setValue(User, String, String)
	 */
	@Override
	public void setValue(User user, String property, String value) {
		UserSecurityState entry = validated(user, property, value);
		dao.setValue(entry.getUserId(), entry.getProperty(), entry.getValue());
	}

	/**
	 * @see UserSecurityStateService#compareAndSet(User, String, String, String)
	 */
	@Override
	public boolean compareAndSet(User user, String property, String expectedValue, String newValue) {
		UserSecurityState entry = validated(user, property, newValue);
		return dao.compareAndSet(entry.getUserId(), entry.getProperty(), expectedValue, entry.getValue());
	}

	/**
	 * @see UserSecurityStateService#deleteIfValueMatches(User, String, String)
	 */
	@Override
	public boolean deleteIfValueMatches(User user, String property, String expectedValue) {
		UserSecurityState entry = validated(user, property, expectedValue);
		return dao.deleteIfValueMatches(entry.getUserId(), entry.getProperty(), entry.getValue());
	}

	/**
	 * @see UserSecurityStateService#delete(User, String)
	 */
	@Override
	public boolean delete(User user, String property) {
		return dao.delete(user.getUserId(), property);
	}

	/**
	 * @see UserSecurityStateService#increment(User, String)
	 */
	@Override
	public long increment(User user, String property) {
		UserSecurityState entry = validated(user, property, "1");
		return dao.increment(entry.getUserId(), entry.getProperty());
	}

	private static UserSecurityState validated(User user, String property, String value) {
		UserSecurityState entry = new UserSecurityState();
		entry.setUserId(user == null ? null : user.getUserId());
		entry.setProperty(property);
		entry.setValue(value);
		ValidateUtil.validate(entry);
		return entry;
	}
}
