/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import jakarta.servlet.ServletContext;

import org.openmrs.util.OpenmrsJacksonLocaleModule;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerExceptionResolverComposite;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.resource.DefaultServletHttpRequestHandler;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.springframework.web.servlet.view.xml.MarshallingView;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "org.openmrs.web.controller")
public class WebConfig implements WebMvcConfigurer {

	/**
	 * Spring Security's rethrowing resolver, matched by name because the class is package-private - see
	 * {@link #removeRethrowingAccessDeniedExceptionResolver(ObjectProvider)}. A rename upstream would
	 * silently restore the bare 403 page, which is what
	 * {@code WebConfigTest#removeRethrowingAccessDeniedExceptionResolver_*} pins by producing the
	 * resolver through Spring Security's own configurer rather than standing in for it.
	 */
	private static final String RETHROWING_ACCESS_DENIED_RESOLVER = "org.springframework.security.config.annotation."
	        + "method.configuration.AuthorizationProxyWebConfiguration$AccessDeniedExceptionResolver";

	/**
	 * Registers OpenMRS custom property editors globally on the handler adapter. This restores the
	 * Platform 2.x behavior where openmrs-servlet.xml configured the RequestMappingHandlerAdapter with
	 * {@link OpenmrsBindingInitializer}.
	 */
	@Bean
	public SmartInitializingSingleton configureBindingInitializer(
	        ObjectProvider<RequestMappingHandlerAdapter> adapterProvider) {
		return () -> {
			RequestMappingHandlerAdapter adapter = adapterProvider.getObject();
			WebBindingInitializer existingInitializer = adapter.getWebBindingInitializer();
			OpenmrsBindingInitializer openmrsInitializer = new OpenmrsBindingInitializer();
			adapter.setWebBindingInitializer(binder -> {
				if (existingInitializer != null) {
					existingInitializer.initBinder(binder);
				}
				openmrsInitializer.initBinder(binder);
			});
		};
	}

	@Bean
	public StandardServletMultipartResolver multipartResolver() {
		return new StandardServletMultipartResolver();
	}

	@Bean
	public ViewResolver jspViewResolver() {
		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver() {

			@Override
			protected AbstractUrlBasedView buildView(String viewName) throws Exception {
				// Strip leading slash to prevent double-slash paths (e.g. /WEB-INF/view//portlets/login.jsp)
				// which Jetty 12 rejects. Module controllers like PortletController return view names
				// starting with "/" (e.g. "/portlets/login").
				if (viewName.startsWith("/")) {
					viewName = viewName.substring(1);
				}
				return super.buildView(viewName);
			}
		};
		viewResolver.setViewClass(JstlView.class);
		viewResolver.setPrefix("/WEB-INF/view/");
		viewResolver.setSuffix(".jsp");
		return viewResolver;
	}

	@Bean
	public ContentNegotiatingViewResolver contentNegotiatingViewResolver() {
		ContentNegotiatingViewResolver viewResolver = new ContentNegotiatingViewResolver();
		viewResolver.setDefaultViews(Arrays.asList(mappingJackson2JsonView(), marshallingView()));
		return viewResolver;
	}

	@Bean
	public MappingJackson2JsonView mappingJackson2JsonView() {
		MappingJackson2JsonView view = new MappingJackson2JsonView();
		view.setExtractValueFromSingleKeyModel(true);
		return view;
	}

	@Bean
	public MarshallingView marshallingView() {
		MarshallingView view = new MarshallingView();
		view.setMarshaller(xStreamMarshaller());
		return view;
	}

	/**
	 * Forwards {@code /index.htm} to the servlet container's default servlet so the static welcome page
	 * is served when no UI module is installed. The {@code *.htm} servlet mapping otherwise routes
	 * {@code /index.htm} to the openmrs DispatcherServlet, which has no core handler for it and would
	 * return 404 on {@code /openmrs/}. The mapping is registered with the lowest precedence so
	 * legacyui's {@code /**\/*.htm} handler (order 100) still wins when legacyui is installed, and
	 * other unmatched URLs (e.g. REST API paths) still flow through Spring's NoHandlerFoundException
	 * handlers rather than being silently forwarded.
	 */
	@Bean
	public SimpleUrlHandlerMapping indexHtmFallbackMapping(ServletContext servletContext) {
		DefaultServletHttpRequestHandler handler = new DefaultServletHttpRequestHandler();
		handler.setServletContext(servletContext);
		return new SimpleUrlHandlerMapping(Map.of("/index.htm", handler), Integer.MAX_VALUE - 1);
	}

	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		Map<String, MediaType> mediaTypes = new HashMap<>();
		mediaTypes.put("json", MediaType.APPLICATION_JSON);
		mediaTypes.put("xml", MediaType.APPLICATION_XML);

		configurer.defaultContentType(MediaType.APPLICATION_JSON).mediaTypes(mediaTypes);
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(new ByteArrayHttpMessageConverter());
		converters.add(new StringHttpMessageConverter());
		converters.add(new FormHttpMessageConverter());
		converters.add(new SourceHttpMessageConverter<>());
		converters.add(jacksonConverter());
		converters.add(xmlMarshallingHttpMessageConverter());
	}

	@Bean
	public Jackson2ObjectMapperFactoryBean openmrsObjectMapperFactoryBean() {
		Jackson2ObjectMapperFactoryBean factory = new Jackson2ObjectMapperFactoryBean();
		factory.setModulesToInstall(OpenmrsJacksonLocaleModule.class);
		return factory;
	}

	@Bean
	public MappingJackson2HttpMessageConverter jacksonConverter() {
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setObjectMapper(openmrsObjectMapper());
		return converter;
	}

	@Bean
	public ObjectMapper openmrsObjectMapper() {
		return Jackson2ObjectMapperBuilder.json().modulesToInstall(OpenmrsJacksonLocaleModule.class).build();
	}

	@Bean
	public XStreamMarshaller xStreamMarshaller() {
		return new XStreamMarshaller();
	}

	@Bean
	public HttpMessageConverter<Object> xmlMarshallingHttpMessageConverter() {
		MarshallingHttpMessageConverter converter = new MarshallingHttpMessageConverter();
		converter.setMarshaller(xStreamMarshaller());
		converter.setUnmarshaller(xStreamMarshaller());
		return converter;
	}

	/**
	 * {@code @EnableMethodSecurity} (on {@code OpenmrsSecurityConfig}) imports Spring Security's
	 * {@code AuthorizationProxyWebConfiguration}, a {@code WebMvcConfigurer} that inserts a resolver
	 * ahead of {@code DefaultHandlerExceptionResolver} whose whole body is to rethrow the first
	 * {@code AccessDeniedException} in the cause chain. That is meant to let a lazy denial from an
	 * {@code @AuthorizeReturnObject} proxy reach the filter chain instead of being swallowed by a
	 * catch-all MVC resolver, but in OpenMRS it also stops {@link #simpleMappingExceptionResolver()}
	 * (order 100, a separate bean, so consulted only after the whole {@code @EnableWebMvc} composite)
	 * from ever seeing a denial. An under-privileged user then gets the container's bare 403 page with
	 * no privilege named, where {@code APIAuthenticationException} used to reach
	 * {@code uncaughtException} and a UI module's login redirect.
	 * <p>
	 * Removing it restores that route for every MVC consumer at once, rather than each module adding an
	 * {@code @ExceptionHandler}. {@code @ExceptionHandler} methods still win, since
	 * {@code ExceptionHandlerExceptionResolver} sits ahead of this one in the composite - which is why
	 * webservices.rest keeps answering 401/403 itself. OpenMRS gives up Spring's propagate-to-the-
	 * filter-chain behaviour for denials in exchange, having its own {@code OpenmrsAccessDeniedHandler}
	 * there already.
	 * <p>
	 * Done as a {@link SmartInitializingSingleton} rather than by overriding
	 * {@code extendHandlerExceptionResolvers}: neither configurer carries an order, so both would sit
	 * at {@code LOWEST_PRECEDENCE} and a removal could run before Spring Security's insert. This runs
	 * once every singleton exists, so the composite is whole whatever order they contributed in.
	 */
	@Bean
	public SmartInitializingSingleton removeRethrowingAccessDeniedExceptionResolver(
	        ObjectProvider<HandlerExceptionResolverComposite> compositeProvider) {
		return () -> {
			HandlerExceptionResolverComposite composite = compositeProvider.getIfAvailable();
			if (composite == null || composite.getExceptionResolvers() == null) {
				return;
			}

			composite.setExceptionResolvers(composite.getExceptionResolvers().stream()
			        .filter(resolver -> !RETHROWING_ACCESS_DENIED_RESOLVER.equals(resolver.getClass().getName())).toList());
		};
	}

	@Bean
	public SimpleMappingExceptionResolver simpleMappingExceptionResolver() {
		SimpleMappingExceptionResolver exceptionResolver = new SimpleMappingExceptionResolver();
		Properties mappings = new Properties();
		mappings.put("java.lang.Exception", "uncaughtException");
		exceptionResolver.setExceptionMappings(mappings);
		exceptionResolver.setOrder(100);
		return exceptionResolver;
	}

	@Bean(name = "conversion-service")
	public FormattingConversionServiceFactoryBean conversionService() {
		return new FormattingConversionServiceFactoryBean();
	}

	@Bean
	public SimpleUrlHandlerMapping urlMapping() {
		SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
		handlerMapping.setOrder(99);
		return handlerMapping;
	}

	@Bean
	public SimpleControllerHandlerAdapter simpleControllerHandlerAdapter() {
		return new SimpleControllerHandlerAdapter();
	}

}
