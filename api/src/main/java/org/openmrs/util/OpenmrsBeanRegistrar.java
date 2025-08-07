/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import org.openmrs.annotation.Handler;
import org.openmrs.annotation.OpenmrsProfileExcludeFilter;
import org.openmrs.annotation.OpenmrsProfileIncludeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * Spring Java-based configuration class for manually registering OpenMRS components into the application context.
 * <p>
 * This registrar:
 * </p>
 * <ul>
 *   <li>Scans the {@code org.openmrs} package for candidate classes</li>
 *   <li>Applies inclusion filters to detect:
 *     <ul>
 *       <li>Beans annotated with {@code Handler}</li>
 *       <li>Beans that match the {@code OpenmrsProfileIncludeFilter}</li>
 *     </ul>
 *   </li>
 *   <li>Applies exclusion filters to ignore:
 *     <ul>
 *       <li>Beans matching {@code TestTypeFilter}</li>
 *       <li>Beans matching {@code OpenmrsProfileExcludeFilter}</li>
 *     </ul>
 *   </li>
 *   <li>Dynamically registers filtered components as Spring beans</li>
 * </ul>
 *
 * @since 3.0.0
 */
public class OpenmrsBeanRegistrar implements BeanDefinitionRegistryPostProcessor {

	private static final Logger log = LoggerFactory.getLogger(OpenmrsBeanRegistrar.class);

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		ClassPathScanningCandidateComponentProvider scanner =
			new ClassPathScanningCandidateComponentProvider(false);

		// Apply same filters as used in @ComponentScan
		scanner.addIncludeFilter(new AnnotationTypeFilter(Handler.class));
		scanner.addIncludeFilter(new OpenmrsProfileIncludeFilter());

		scanner.addExcludeFilter(new TestTypeFilter());
		scanner.addExcludeFilter(new OpenmrsProfileExcludeFilter());

		for (BeanDefinition bd : scanner.findCandidateComponents("org.openmrs")) {
			try {
				Class<?> clazz = Class.forName(bd.getBeanClassName());
				registry.registerBeanDefinition(toCamelCase(clazz.getSimpleName()), 
					BeanDefinitionBuilder.genericBeanDefinition(clazz).getBeanDefinition());
			} catch (ClassNotFoundException e) {
				throw new BeansException("Failed to load class: " + bd.getBeanClassName(), e) {};
			}
		}
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {}

	private static String toCamelCase(String input) {
		if (input == null || input.isEmpty()) {
			return input;
		}
		
		return input.substring(0, 1).toLowerCase() + input.substring(1);
	}
}
