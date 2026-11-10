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

import org.apache.commons.lang3.StringUtils;
import org.openmrs.User;
import org.openmrs.api.UserSecurityStateService;
import org.openmrs.api.db.UserSecurityStateDAO;
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

	private static final int MAX_PROPERTY_LENGTH = 255;

	private static final int MAX_VALUE_LENGTH = 4000;

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
		return dao.getValue(userId(user), property(property));
	}

	/**
	 * @see UserSecurityStateService#setValue(User, String, String)
	 */
	@Override
	public void setValue(User user, String property, String value) {
		dao.setValue(userId(user), property(property), value(value, "value"));
	}

	/**
	 * @see UserSecurityStateService#compareAndSet(User, String, String, String)
	 */
	@Override
	public boolean compareAndSet(User user, String property, String expectedValue, String newValue) {
		return dao.compareAndSet(userId(user), property(property), expectedValue, value(newValue, "newValue"));
	}

	/**
	 * @see UserSecurityStateService#deleteIfValueMatches(User, String, String)
	 */
	@Override
	public boolean deleteIfValueMatches(User user, String property, String expectedValue) {
		return dao.deleteIfValueMatches(userId(user), property(property), value(expectedValue, "expectedValue"));
	}

	/**
	 * @see UserSecurityStateService#delete(User, String)
	 */
	@Override
	public boolean delete(User user, String property) {
		return dao.delete(userId(user), property(property));
	}

	/**
	 * @see UserSecurityStateService#increment(User, String)
	 */
	@Override
	public long increment(User user, String property) {
		return dao.increment(userId(user), property(property));
	}

	private static Integer userId(User user) {
		if (user == null || user.getUserId() == null) {
			throw new IllegalArgumentException("user must be a saved user");
		}
		return user.getUserId();
	}

	private static String property(String property) {
		if (StringUtils.isBlank(property)) {
			throw new IllegalArgumentException("property must not be blank");
		}
		if (property.length() > MAX_PROPERTY_LENGTH) {
			throw new IllegalArgumentException("property must not be longer than " + MAX_PROPERTY_LENGTH + " characters");
		}
		return property;
	}

	private static String value(String value, String name) {
		if (StringUtils.isBlank(value)) {
			throw new IllegalArgumentException(name + " must not be blank");
		}
		if (value.length() > MAX_VALUE_LENGTH) {
			throw new IllegalArgumentException(name + " must not be longer than " + MAX_VALUE_LENGTH + " characters");
		}
		return value;
	}
}
