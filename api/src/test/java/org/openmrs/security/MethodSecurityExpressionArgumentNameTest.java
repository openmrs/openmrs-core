/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.security;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.DefaultSecurityParameterNameDiscoverer;
import org.springframework.util.ClassUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Guards the one silent failure mode of {@code #argName} in method security expressions: Spring
 * Security resolves argument names against the <em>implementation</em> method
 * ({@link AopUtils#getMostSpecificMethod}), not the annotated interface method, so an interface and
 * implementation that disagree on a parameter name make {@code #argName} evaluate to {@code null}.
 * Since {@code hasPermission(null, privilege)} is a plain privilege check that consults no
 * {@link DomainObjectAuthorizationRule}, such a method loses its domain-object authorization
 * without any error.
 * <p>
 * Also asserts parameter names are discoverable at all, which depends on the build keeping
 * {@code <parameters>true</parameters>} - Spring no longer falls back to debug symbols.
 */
public class MethodSecurityExpressionArgumentNameTest {

	/**
	 * {@code #root}, {@code #this} and the positional {@code #a0}/{@code #p0} aliases are supplied by
	 * Spring, not by parameter names.
	 */
	private static final Pattern BUILT_IN_VARIABLE = Pattern.compile("root|this|[ap]\\d+");

	private static final Pattern VARIABLE_REFERENCE = Pattern.compile("#(\\w+)");

	private static final List<Class<? extends Annotation>> SECURITY_ANNOTATIONS = Arrays.asList(PreAuthorize.class,
	    PostAuthorize.class, PostFilter.class);

	/**
	 * The discoverer {@code DefaultMethodSecurityExpressionHandler} itself defaults to, so that
	 * {@code @P("name")} on an implementation parameter counts here exactly as it does at runtime.
	 */
	private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultSecurityParameterNameDiscoverer();

	@Test
	public void shouldResolveEveryArgumentReferenceOnTheImplementationMethod() throws Exception {
		List<Class<?>> classes = openmrsClasses();
		assertTrue(classes.size() > 100, "expected to scan the openmrs classes, found only " + classes.size());

		Map<Class<?>, List<Class<?>>> implementationsByInterface = implementationsByInterface(classes);
		List<String> problems = new ArrayList<>();
		int checked = 0;

		for (Class<?> type : classes) {
			if (!type.isInterface()) {
				continue;
			}
			for (Method method : declaredMethods(type)) {
				Set<String> references = argumentReferences(method);
				if (references.isEmpty()) {
					continue;
				}
				checked++;
				for (Class<?> implementation : implementationsByInterface.getOrDefault(type, List.of())) {
					verify(method, implementation, references, problems);
				}
			}
		}

		assertTrue(checked > 0, "found no method security expression referencing an argument");
		if (!problems.isEmpty()) {
			fail(problems.size() + " method security expression(s) reference an argument name that does not exist on "
			        + "the implementation method, so the expression silently evaluates to null:\n"
			        + String.join("\n", problems));
		}
	}

	private void verify(Method method, Class<?> implementation, Set<String> references, List<String> problems) {
		Method target = AopUtils.getMostSpecificMethod(method, implementation);
		String[] names = parameterNameDiscoverer.getParameterNames(target);
		if (names == null) {
			problems.add(describe(method, implementation) + " has no discoverable parameter names - is the build still "
			        + "compiling with <parameters>true</parameters>?");
			return;
		}
		Set<String> available = new HashSet<>(Arrays.asList(names));
		for (String reference : references) {
			if (!available.contains(reference)) {
				problems.add(describe(method, implementation) + " references #" + reference + " but the implementation "
				        + "declares " + Arrays.toString(names) + " - rename so both sides agree, or annotate the "
				        + "implementation parameter with @P(\"" + reference + "\")");
			}
		}
	}

	private String describe(Method method, Class<?> implementation) {
		return method.getDeclaringClass().getSimpleName() + "." + method.getName() + "() implemented by "
		        + implementation.getName();
	}

	private Set<String> argumentReferences(Method method) {
		Set<String> references = new LinkedHashSet<>();
		for (Class<? extends Annotation> annotationType : SECURITY_ANNOTATIONS) {
			Annotation annotation = method.getAnnotation(annotationType);
			if (annotation == null) {
				continue;
			}
			Matcher matcher = VARIABLE_REFERENCE.matcher(expressionOf(annotation));
			while (matcher.find()) {
				String name = matcher.group(1);
				if (!BUILT_IN_VARIABLE.matcher(name).matches()) {
					references.add(name);
				}
			}
		}
		return references;
	}

	private String expressionOf(Annotation annotation) {
		if (annotation instanceof PreAuthorize preAuthorize) {
			return preAuthorize.value();
		}
		if (annotation instanceof PostAuthorize postAuthorize) {
			return postAuthorize.value();
		}
		return ((PostFilter) annotation).value();
	}

	private Map<Class<?>, List<Class<?>>> implementationsByInterface(List<Class<?>> classes) {
		Map<Class<?>, List<Class<?>>> implementations = new HashMap<>();
		for (Class<?> type : classes) {
			if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
				continue;
			}
			for (Class<?> implemented : ClassUtils.getAllInterfacesForClass(type)) {
				implementations.computeIfAbsent(implemented, key -> new ArrayList<>()).add(type);
			}
		}
		return implementations;
	}

	private Method[] declaredMethods(Class<?> type) {
		try {
			return type.getDeclaredMethods();
		} catch (Throwable ignored) {
			// a signature referencing an optional dependency that is absent from the test classpath
			return new Method[0];
		}
	}

	private List<Class<?>> openmrsClasses() throws IOException {
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		MetadataReaderFactory metadata = new CachingMetadataReaderFactory(resolver);
		List<Class<?>> classes = new ArrayList<>();
		for (Resource resource : resolver.getResources("classpath*:org/openmrs/**/*.class")) {
			String name;
			try {
				name = metadata.getMetadataReader(resource).getClassMetadata().getClassName();
			} catch (Throwable ignored) {
				continue;
			}
			try {
				// never initialize - loading the whole api would run unrelated static initializers
				classes.add(Class.forName(name, false, getClass().getClassLoader()));
			} catch (Throwable ignored) {
				// absent optional dependency
			}
		}
		return classes;
	}
}
