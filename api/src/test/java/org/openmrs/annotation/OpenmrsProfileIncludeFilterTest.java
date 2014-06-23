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
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

public class OpenmrsProfileIncludeFilterTest extends BaseContextSensitiveTest {
	
	/**
	 * @see OpenmrsProfileIncludeFilter#match(MetadataReader,MetadataReaderFactory)
	 * @verifies create bean for openmrs 1_8 and later
	 */
	@Test
	public void match_shouldCreateBeanForOpenmrs1_8AndLater() throws Exception {
		OpenmrsComponent1_8 bean = applicationContext.getBean(OpenmrsComponent1_8.class);
		
		assertThat(bean, is(notNullValue()));
	}
	
	/**
	 * @see OpenmrsProfileIncludeFilter#match(MetadataReader,MetadataReaderFactory)
	 * @verifies not create bean for openmrs 1_6 to 1_7
	 */
	@Test(expected = NoSuchBeanDefinitionException.class)
	public void match_shouldNotCreateBeanForOpenmrs1_6To1_7() throws Exception {
		applicationContext.getBean(OpenmrsComponent1_6To1_7.class);
	}
	
}
