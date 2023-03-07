/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.openmrs.api.context.Context;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;

/**
 * For configuration of beans whose classes are determined at runtime depending on the OpenMRS core
 * platform version.
 */
@Configuration("webservices.rest.dynamicBeanConfiguration")
public class DynamicBeanConfiguration {
	
	/**
	 * In spring 3.1.2 MappingJacksonHttpMessageConverter was replaced with
	 * MappingJackson2HttpMessageConverter and eventually removed in version 4.1.0. This bean
	 * configuration method allows the module to run on both pre and post 4.1.0 versions of Spring
	 * by loading the new class if it's available in the spring version that OpenMRS is running on
	 * otherwise it falls back to the old one.
	 */
	@Bean(name = "jsonHttpMessageConverter")
	public HttpMessageConverter getMappingJacksonHttpMessageConverter() throws Exception {
		
		Class<?> clazz;
		try {
			clazz = Context.loadClass("org.springframework.http.converter.json.MappingJacksonHttpMessageConverter");
		}
		catch (ClassNotFoundException e) {
			clazz = Context.loadClass("org.springframework.http.converter.json.MappingJackson2HttpMessageConverter");
		}
		
		return (HttpMessageConverter) clazz.newInstance();
	}
	
	/**
	 * The AnnotationMethodHandlerExceptionResolver class was deprecated and eventually removed in
	 * Spring 5 The recommended replacement class ExceptionHandlerExceptionResolver was introduced
	 * in Spring 3.1.0 which is not available on OpenMRS platform versions 1.9.x and 1.10.x which
	 * run Spring 3.0.5 That's why we can't just statically replace this class in the
	 * webModuleApplicationContext.xml file.
	 */
	@Bean
	public AbstractHandlerExceptionResolver getHandlerExceptionResolver() throws Exception {
		
		AbstractHandlerExceptionResolver bean = null;
		
		HttpMessageConverter<?> stringHttpMessageConverter = Context.getRegisteredComponent(
			    "stringHttpMessageConverter", HttpMessageConverter.class);
		
		HttpMessageConverter<?> jsonHttpMessageConverter = Context.getRegisteredComponent("jsonHttpMessageConverter",
			    HttpMessageConverter.class);
		
		HttpMessageConverter<?> xmlMarshallingHttpMessageConverter = Context.getRegisteredComponent(
			    "xmlMarshallingHttpMessageConverter", HttpMessageConverter.class);
		
		try {
			Class<?> clazz = Context
			        .loadClass("org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerExceptionResolver");
			
			bean = (AbstractHandlerExceptionResolver) clazz.newInstance();
			
			Method method = bean.getClass().getMethod("setMessageConverters", new Class[] { HttpMessageConverter[].class });
			method.invoke(bean, new Object[] { new HttpMessageConverter[] { stringHttpMessageConverter,
			        jsonHttpMessageConverter, xmlMarshallingHttpMessageConverter } });
		}
		catch (ClassNotFoundException e) {
			Class<?> clazz = Context
			        .loadClass("org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver");
			
			bean = (AbstractHandlerExceptionResolver) clazz.newInstance();
			
			Method method = bean.getClass().getMethod("setMessageConverters", new Class[] { List.class });
			method.invoke(bean, new Object[] { Arrays.asList( stringHttpMessageConverter,
			        jsonHttpMessageConverter, xmlMarshallingHttpMessageConverter ) });
		}
		
		if (bean != null) {
			bean.setOrder(1);
			
		}
		
		return bean;
	}
	
	/**
	 * The DefaultAnnotationHandlerMapping class was deprecated and eventually removed in Spring 5
	 * The recommended replacement class RequestMappingHandlerMapping was introduced in Spring 3.1.0
	 * which is not available on OpenMRS platform versions 1.9.x and 1.10.x which run Spring 3.0.5
	 * That's why we can't just statically replace this class in the webModuleApplicationContext.xml
	 * file.
	 */
	@Bean
	public AbstractHandlerMapping getHandlerMapping() throws Exception {
		
		Class<?> clazz;
		try {
			clazz = Context.loadClass("org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping");
		}
		catch (ClassNotFoundException e) {
			clazz = Context.loadClass("org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping");
		}
		
		return (AbstractHandlerMapping) clazz.newInstance();
	}
}
