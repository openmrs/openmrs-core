/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.layout;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LayoutSupportTest {

	@Test
	public void getLayoutTemplateByCodeName_shouldReturnMatchingTemplate() {
		LayoutTemplate template = new TestLayoutTemplate();
		template.setCodeName("test-code");

		TestLayoutSupport support = new TestLayoutSupport();
		support.setLayoutTemplates(Collections.singletonList(template));

		assertEquals(template, support.getLayoutTemplateByCodeName("test-code"));
	}

	@Test
	public void getLayoutTemplateByCountry_shouldReturnMatchingTemplate() {
		LayoutTemplate template = new TestLayoutTemplate();
		template.setCountry("India");

		TestLayoutSupport support = new TestLayoutSupport();
		support.setLayoutTemplates(Collections.singletonList(template));

		assertEquals(template, support.getLayoutTemplateByCountry("India"));
	}

	@Test
	public void getLayoutTemplateByDisplayName_shouldReturnMatchingTemplate() {
		LayoutTemplate template = new TestLayoutTemplate();
		template.setDisplayName("Test Template");

		TestLayoutSupport support = new TestLayoutSupport();
		support.setLayoutTemplates(Collections.singletonList(template));

		assertEquals(template, support.getLayoutTemplateByDisplayName("Test Template"));
	}

	private static class TestLayoutTemplate extends LayoutTemplate {

		@Override
		public String getLayoutToken() {
			return "";
		}

		@Override
		public String getNonLayoutToken() {
			return "";
		}

		@Override
		public LayoutSupport<?> getLayoutSupportInstance() {
			return null;
		}
	}

	private static class TestLayoutSupport extends LayoutSupport<LayoutTemplate> {

		@Override
		public String getDefaultLayoutFormat() {
			return null;
		}
	}
}
