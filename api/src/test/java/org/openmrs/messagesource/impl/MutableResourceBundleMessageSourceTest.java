/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.messagesource.impl;

import java.util.Collection;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.openmrs.messagesource.PresentationMessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MutableResourceBundleMessageSourceTest {

	@Test
	void getPresentation_shouldReturnPresentationForKeyAndLocale() {
		MutableResourceBundleMessageSource messageSource = new MutableResourceBundleMessageSource();

		PresentationMessage presentation = messageSource.getPresentation("general.finish", Locale.UK);

		assertNotNull(presentation);
		assertEquals("general.finish", presentation.getCode());
		assertEquals("Finish", presentation.getMessage());
		assertEquals(Locale.UK, presentation.getLocale());
	}

	@Test
	void getPresentationsInLocale_shouldReturnPresentationsForLocale() {
		MutableResourceBundleMessageSource messageSource = new MutableResourceBundleMessageSource();

		Collection<PresentationMessage> presentations = messageSource.getPresentationsInLocale(Locale.UK);

		assertTrue(presentations.stream().anyMatch(presentation -> "general.finish".equals(presentation.getCode())));
		assertTrue(presentations.stream().allMatch(presentation -> Locale.UK.equals(presentation.getLocale())));
	}
}
