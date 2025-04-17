/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter.util;

import org.openmrs.web.filter.initialization.InitializationWizardModel;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;

/**
 * Utility class for saving and loading the state of an {@link InitializationWizardModel}
 * to and from an {@link HttpSession}.
 * <p>
 * This helps to avoid repetitive boilerplate code and simplifies session persistence
 * of wizard step values.
 */
public class SessionModelUtils {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(SessionModelUtils.class);
	private static final String PREFIX = "setup.";

	/**
	 * Saves all non-null fields of the {@link InitializationWizardModel} to the HTTP session.
	 * The field name is used as the session key, prefixed with "setup.".
	 *
	 * @param session the current {@link HttpSession}, must not be null
	 * @param model the {@link InitializationWizardModel} to save
	 */
	public static void saveToSession(HttpSession session, InitializationWizardModel model) {
		if (session == null || model == null) return;

		log.warn("Saving wizard model to session");

		for (Field field : model.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			String fieldName = field.getName().toLowerCase();
			// NOTE: Passwords should not be stored in session as this poses a risk of unintended exposure.
			if (fieldName.contains("password")) {
				log.warn("Skipping password related field: {}", field.getName());
				continue;
			}
			try {
				Object value = field.get(model);
				if (value != null) {
					String key = PREFIX + field.getName();
					session.setAttribute(key, value);
					log.warn("Saved field '{}' with value: {}", key, value);
				}
			} catch (IllegalAccessException e) {
				log.error("Could not access field during save: {}", field.getName(), e);
			}
		}
	}

	/**
	 * Loads all existing session attributes (with "setup." prefix) into the corresponding fields
	 * of the given {@link InitializationWizardModel}, if present.
	 *
	 * @param session the current {@link HttpSession}, must not be null
	 * @param model the {@link InitializationWizardModel} to populate with values
	 */
	public static void loadFromSession(HttpSession session, InitializationWizardModel model) {
		if (session == null || model == null) return;

		log.warn("Loading wizard model from session");

		for (Field field : model.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			String fieldName = field.getName().toLowerCase();
			// NOTE: Passwords should not be restored from session to avoid persisting sensitive credentials in memory.
			if (fieldName.contains("password")) {
				log.warn("Skipping password-related field during load: {}", field.getName());
				continue;
			}
			try {
				String key = PREFIX + field.getName();
				Object value = session.getAttribute(key);
				if (value != null) {
					field.set(model, value);
					log.warn("Loaded field '{}' with value: {}", key, value);
				}
			} catch (IllegalAccessException e) {
				log.error("Could not set field during load: {}", field.getName(), e);
			}
		}
	}
}
