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

public class OpenmrsProfileExcludeFilterTest extends BaseContextSensitiveTest {
	
	/**
	 * @verifies not include bean for openmrs from 1_6 to 1_7
	 * @see OpenmrsProfileExcludeFilter#match(org.springframework.core.type.classreading.MetadataReader, org.springframework.core.type.classreading.MetadataReaderFactory)
	 */
	@Test(expected = NoSuchBeanDefinitionException.class)
	public void match_shouldNotIncludeBeanForOpenmrsFrom1_6To1_8() throws Exception {
		applicationContext.getBean(OpenmrsProfile1_6To1_8.class);
	}
	
	/**
	 * @verifies not include bean for openmrs 1_10 and later
	 * @see OpenmrsProfileExclusionFilter#match(org.springframework.core.type.classreading.MetadataReader, org.springframework.core.type.classreading.MetadataReaderFactory)
	 */
	@Test(expected = NoSuchBeanDefinitionException.class)
	public void match_shouldNotIncludeBeanForOpenmrs1_10AndLater() throws Exception {
		applicationContext.getBean(OpenmrsProfile1_10.class);
	}
	
	/**
	 * @verifies include bean for openmrs 1_9 and later
	 * @see OpenmrsProfileExclusionFilter#match(org.springframework.core.type.classreading.MetadataReader, org.springframework.core.type.classreading.MetadataReaderFactory)
	 */
	@Test
	public void match_shouldIncludeBeanForOpenmrs1_9AndLater() throws Exception {
		OpenmrsProfile1_9 bean = applicationContext.getBean(OpenmrsProfile1_9.class);
		
		assertThat(bean, is(notNullValue()));
	}
	
	/**
	 * @verifies not include bean for openmrs 1_9 and later if module missing
	 * @see OpenmrsProfileExclusionFilter#match(org.springframework.core.type.classreading.MetadataReader, org.springframework.core.type.classreading.MetadataReaderFactory)
	 */
	@Test(expected = NoSuchBeanDefinitionException.class)
	public void match_shouldNotIncludeBeanForOpenmrs1_9AndLaterIfModuleMissing() throws Exception {
		applicationContext.getBean(OpenmrsProfile1_9WithHtmlformentry.class);
	}
}
