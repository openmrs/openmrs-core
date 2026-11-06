/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AbstractSingletonProxyFactoryBean;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.EmptyTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.util.ClassUtils;

/**
 * Declares the object type of every {@link AbstractSingletonProxyFactoryBean} definition, such as a
 * module service defined with a
 * {@link org.springframework.transaction.interceptor.TransactionProxyFactoryBean}, so that Spring
 * can match it by type without creating it.
 * <p>
 * Without the declared type, the first lookup by type made while the core advisors are being
 * created fully creates such a factory bean together with its target, and with it every core
 * service the target references. Those core services are then proxied before the advisors exist and
 * run without authorization, required data handling, transactions or caching.
 *
 * @since 3.0.0
 */
public class ProxyFactoryBeanObjectTypePostProcessor implements BeanFactoryPostProcessor {

	private static final Logger log = LoggerFactory.getLogger(ProxyFactoryBeanObjectTypePostProcessor.class);

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		ClassLoader classLoader = beanFactory.getBeanClassLoader();
		for (String beanName : beanFactory.getBeanDefinitionNames()) {
			BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
			if (definition.getAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE) != null) {
				continue;
			}
			BeanDefinition merged = beanFactory.getMergedBeanDefinition(beanName);
			String className = merged.getBeanClassName();
			if (merged.isAbstract() || className == null || !ClassUtils.isPresent(className, classLoader)
			        || !AbstractSingletonProxyFactoryBean.class
			                .isAssignableFrom(ClassUtils.resolveClassName(className, classLoader))) {
				continue;
			}
			MutablePropertyValues properties = merged.getPropertyValues();
			if (properties.contains("proxyInterfaces")) {
				continue;
			}
			try {
				Class<?> targetClass = getTargetClass(beanFactory, properties.get("target"), classLoader);
				if (targetClass != null) {
					boolean proxyTargetClass = isTrue(properties.get("proxyTargetClass"))
					        || isTrue(properties.get("optimize"));
					definition.setAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE,
					    getProxyClass(targetClass, proxyTargetClass, classLoader));
				}
			} catch (RuntimeException | LinkageError e) {
				log.debug("Could not determine the object type of {}, Spring will determine it", beanName, e);
			}
		}
	}

	private Class<?> getTargetClass(ConfigurableListableBeanFactory beanFactory, Object target, ClassLoader classLoader) {
		if (target instanceof RuntimeBeanReference reference) {
			return beanFactory.isFactoryBean(reference.getBeanName()) ? null
			        : beanFactory.getType(reference.getBeanName(), false);
		}
		BeanDefinition inner = target instanceof BeanDefinitionHolder holder ? holder.getBeanDefinition()
		        : target instanceof BeanDefinition definition ? definition : null;
		if (inner != null && inner.getBeanClassName() != null && inner.getFactoryMethodName() == null
		        && ClassUtils.isPresent(inner.getBeanClassName(), classLoader)) {
			Class<?> innerClass = ClassUtils.resolveClassName(inner.getBeanClassName(), classLoader);
			return FactoryBean.class.isAssignableFrom(innerClass) ? null : innerClass;
		}
		return null;
	}

	private boolean isTrue(Object value) {
		Object raw = value instanceof TypedStringValue typed ? typed.getValue() : value;
		return raw != null && Boolean.parseBoolean(raw.toString());
	}

	/**
	 * Mirrors {@link AbstractSingletonProxyFactoryBean#afterPropertiesSet()}: the target's interfaces
	 * are proxied unless the target class is proxied or it has no interfaces. Like
	 * {@code proxyTargetClass}, {@code optimize} makes Spring proxy the target class, see
	 * {@link org.springframework.aop.framework.DefaultAopProxyFactory}.
	 */
	private Class<?> getProxyClass(Class<?> targetClass, boolean proxyTargetClass, ClassLoader classLoader) {
		Class<?>[] interfaces = ClassUtils.getAllInterfacesForClass(targetClass, classLoader);
		if (proxyTargetClass || interfaces.length == 0) {
			return targetClass;
		}
		ProxyFactory proxyFactory = new ProxyFactory(interfaces);
		proxyFactory.setTargetSource(EmptyTargetSource.forClass(targetClass));
		return proxyFactory.getProxyClass(classLoader);
	}
}
