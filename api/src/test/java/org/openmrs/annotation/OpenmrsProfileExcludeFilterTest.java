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
import static org.junit.Assert.fail;

import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

public class OpenmrsProfileExcludeFilterTest extends BaseContextSensitiveTest {
	
	/**
	 * @see OpenmrsProfileExcludeFilter#match(org.springframework.core.type.classreading.MetadataReader, org.springframework.core.type.classreading.MetadataReaderFactory)
	 */
	@Test(expected = NoSuchBeanDefinitionException.class)
	public void match_shouldNotIncludeBeanForOpenmrsFrom1_6To1_7() {
		applicationContext.getBean(OpenmrsProfile1_6To1_7.class);
	}
	
	/**
	 * @see OpenmrsProfileExcludeFilter#match(org.springframework.core.type.classreading.MetadataReader, org.springframework.core.type.classreading.MetadataReaderFactory)
	 */
	@Test
	public void match_shouldIncludeBeanForOpenmrs1_10AndLater() {
		OpenmrsProfile1_10 bean = applicationContext.getBean(OpenmrsProfile1_10.class);
		
		assertThat(bean, is(notNullValue()));
	}
	
	/**
	 * @see OpenmrsProfileExcludeFilter#match(org.springframework.core.type.classreading.MetadataReader, org.springframework.core.type.classreading.MetadataReaderFactory)
	 */
	@Test
	public void match_shouldIncludeBeanForOpenmrs1_8AndLater() {
		OpenmrsProfile1_8 bean = applicationContext.getBean(OpenmrsProfile1_8.class);
		
		assertThat(bean, is(notNullValue()));
	}
	
	/**
	 * @see OpenmrsProfileExcludeFilter#match(org.springframework.core.type.classreading.MetadataReader, org.springframework.core.type.classreading.MetadataReaderFactory)
	 */
	@Test(expected = NoSuchBeanDefinitionException.class)
	public void match_shouldNotIncludeBeanForOpenmrs1_8AndLaterIfModuleMissing() {
		applicationContext.getBean(OpenmrsProfile1_8WithHtmlformentry.class);
	}
	
	@Test
	public void shouldBeIgnoredIfOpenmrsVersionDoesNotMatch() {
		assumeOpenmrsPlatformVersion("1.6.*");
		
		fail("It should have been ignored!");
	}
	
	@Test
	public void shouldBeIgnoredIfModuleDoesNotMatch() {
		assumeOpenmrsModules("metadatasharing:1.2");
		
		fail("It should have been ignored!");
	}
	
	@Test
	public void shouldNotBeIgnoredIfOpenmrsVersionDoesMatch() {
		assumeOpenmrsPlatformVersion("1.9");
	}
	
	/**
	 * @see OpenmrsProfileExcludeFilter#match(org.springframework.core.type.classreading.MetadataReader, org.springframework.core.type.classreading.MetadataReaderFactory)
	 */
	@Test
	public void match_shouldIncludeBeanIfModuleMissing() {
		OpenmrsProfileWithoutMissingModule bean = applicationContext.getBean(OpenmrsProfileWithoutMissingModule.class);
		
		assertThat(bean, is(notNullValue()));
	}
}
