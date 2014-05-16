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
