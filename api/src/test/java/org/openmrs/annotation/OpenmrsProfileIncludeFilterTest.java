/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.annotation;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

public class OpenmrsProfileIncludeFilterTest extends BaseContextSensitiveTest {
	
	/**
	 * @see OpenmrsProfileIncludeFilter#match(MetadataReader,MetadataReaderFactory)
	 */
	@Test
	public void match_shouldCreateBeanForOpenmrs1_8AndLater() {
		OpenmrsComponent1_8 bean = applicationContext.getBean(OpenmrsComponent1_8.class);
		
		assertThat(bean, is(notNullValue()));
	}
	
	/**
	 * @see OpenmrsProfileIncludeFilter#match(MetadataReader,MetadataReaderFactory)
	 */
	@Test(expected = NoSuchBeanDefinitionException.class)
	public void match_shouldNotCreateBeanForOpenmrs1_6To1_7() {
		applicationContext.getBean(OpenmrsComponent1_6To1_7.class);
	}
	
}
