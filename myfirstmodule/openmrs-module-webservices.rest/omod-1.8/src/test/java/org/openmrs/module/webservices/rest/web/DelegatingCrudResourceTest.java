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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.RepHandler;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.LocationResource1_8;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.UserResource1_8;
import org.openmrs.util.Reflect;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ReflectionUtils;

/**
 * Contains tests for Representation Descriptions of all resources
 */
public class DelegatingCrudResourceTest extends BaseModuleWebContextSensitiveTest {
	
	/**
	 * This test looks at all subclasses of DelegatingCrudResource, and test all {@link RepHandler}
	 * methods to make sure they are all capable of running without exceptions. It also checks that
	 */
	@SuppressWarnings("rawtypes")
	@Test
	@Ignore
	public void testAllReprsentationDescriptions() throws Exception {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(true);
		//only match subclasses of BaseDelegatingResource
		provider.addIncludeFilter(new AssignableTypeFilter(BaseDelegatingResource.class));
		
		// scan in org.openmrs.module.webservices.rest.web.resource package 
		Set<BeanDefinition> components = provider
		        .findCandidateComponents("org.openmrs.module.webservices.rest.web.resource");
		if (CollectionUtils.isEmpty(components))
			Assert.fail("Faile to load any resource classes");
		
		for (BeanDefinition component : components) {
			Class resourceClass = Class.forName(component.getBeanClassName());
			for (Method method : ReflectionUtils.getAllDeclaredMethods(resourceClass)) {
				ParameterizedType parameterizedType = (ParameterizedType) resourceClass.getGenericSuperclass();
				Class openmrsClass = (Class) parameterizedType.getActualTypeArguments()[0];
				//User Resource is special in that the Actual parameterized Type isn't a standard domain object, so we also
				//need to look up fields and methods from the org.openmrs.User class 
				boolean isUserResource = resourceClass.equals(UserResource1_8.class);
				List<Object> refDescriptions = new ArrayList<Object>();
				
				if (method.getName().equals("getRepresentationDescription")
				        && method.getDeclaringClass().equals(resourceClass)) {
					//get all the rep definitions for all representations
					refDescriptions.add(method.invoke(resourceClass.newInstance(), new Object[] { Representation.REF }));
					refDescriptions.add(method.invoke(resourceClass.newInstance(), new Object[] { Representation.DEFAULT }));
					refDescriptions.add(method.invoke(resourceClass.newInstance(), new Object[] { Representation.FULL }));
				}
				
				for (Object value : refDescriptions) {
					if (value != null) {
						DelegatingResourceDescription des = (DelegatingResourceDescription) value;
						for (String key : des.getProperties().keySet()) {
							if (!key.equals("uri") && !key.equals("display") && !key.equals("auditInfo")) {
								boolean hasFieldOrPropertySetter = (ReflectionUtils.findField(openmrsClass, key) != null);
								if (!hasFieldOrPropertySetter) {
									hasFieldOrPropertySetter = hasSetterMethod(key, resourceClass);
									if (!hasFieldOrPropertySetter && isUserResource)
										hasFieldOrPropertySetter = (ReflectionUtils.findField(User.class, key) != null);
								}
								if (!hasFieldOrPropertySetter)
									hasFieldOrPropertySetter = hasSetterMethod(key, resourceClass);
								
								//TODO replace this hacky way that we are using to check if there is a get method for a 
								//collection that has no actual getter e.g activeIdentifers and activeAttributes for Patient
								if (!hasFieldOrPropertySetter) {
									hasFieldOrPropertySetter = (ReflectionUtils.findMethod(openmrsClass,
									    "get" + StringUtils.capitalize(key)) != null);
									if (!hasFieldOrPropertySetter && isUserResource)
										hasFieldOrPropertySetter = (ReflectionUtils.findMethod(User.class, "get"
										        + StringUtils.capitalize(key)) != null);
								}
								
								if (!hasFieldOrPropertySetter)
									hasFieldOrPropertySetter = isallowedMissingProperty(resourceClass, key);
								
								Assert.assertTrue("No property found for '" + key + "' for " + openmrsClass
								        + " nor setter method on resource " + resourceClass, hasFieldOrPropertySetter);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Convenience method that checks of the specified resource class has a method for setting the
	 * given property
	 * 
	 * @param propName
	 * @param resource
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private static boolean hasSetterMethod(String propName, Class resourceClass) {
		for (Method candidate : resourceClass.getMethods()) {
			PropertySetter ann = candidate.getAnnotation(PropertySetter.class);
			if (ann != null && ann.value().equals(propName)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Convenience method that checks if the specified property is included among the allowed
	 * missing properties of the given resource class via reflection
	 * 
	 * @param clazz
	 * @param fieldName
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@SuppressWarnings("rawtypes")
	private static boolean isallowedMissingProperty(Class resourceClass, String propName) throws IllegalArgumentException,
	        IllegalAccessException, InstantiationException {
		List<Field> fields = Reflect.getAllFields(resourceClass);
		if (CollectionUtils.isNotEmpty(fields)) {
			for (Field field : fields) {
				if (field.getName().equals("allowedMissingProperties"))
					return ((Set) field.get(resourceClass.newInstance())).contains(propName);
			}
		}
		return false;
	}
	
	@Test
	public void convert_shouldConvertASimpleObjectThatIncludesAUuid() {
		final String uuid = "91f6c840-da25-11e8-ae91-0242ac110002";
		SimpleObject so = new SimpleObject();
		so.add("uuid", uuid);
		so.add("name", "Location name");
		so.add("description", "Location description");
		DelegatingCrudResource<Location> resource = new LocationResource1_8();
		Location location = resource.convert(so);
		Assert.assertEquals(uuid, location.getUuid());
	}
	
}
