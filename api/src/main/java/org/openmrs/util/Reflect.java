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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.azeckoski.reflectutils.ClassData;

/**
 * This class has convenience methods to find the fields on a class and superclass as well as
 * methods to check the class type of members in a collection
 */
public class Reflect {
	
	@SuppressWarnings("unchecked")
	private Class parametrizedClass;
	
	/**
	 * @param parametrizedClass Class
	 * @should throw exception when null is passed
	 */
	@SuppressWarnings("unchecked")
	public Reflect(Class parametrizedClass) {
		
		if (parametrizedClass == null) {
			throw new NullPointerException("Parametrized class cannot be null");
		}
		this.parametrizedClass = parametrizedClass;
	}
	
	/**
	 * @param fieldClass
	 * @return true if, given fieldClass is Collection otherwise returns false
	 * @should return true if given fieldClass is Collection class
	 * @should return false if given fieldClass is not a Collection class
	 */
	public static boolean isCollection(Class<?> fieldClass) {
		return Collection.class.isAssignableFrom(fieldClass);
	}
	
	/**
	 * @param object Object
	 * @return true if, given object is Collection otherwise returns false
	 * @should return true if given object is Collection class
	 * @should return false if given object is not a Collection
	 */
	public static boolean isCollection(Object object) {
		return isCollection(object.getClass());
	}
	
	/**
	 * This method return all the fields (including private) from the given class and its super
	 * classes.
	 * 
	 * @param fieldClass Class
	 * @return List<Field>
	 * @should return all fields include private and super classes
	 */
	@SuppressWarnings("unchecked")
	public static List<Field> getAllFields(Class<?> fieldClass) {
		return new ClassData(fieldClass).getFields();
	}
	
	/**
	 * @param subClass Class
	 * @return true if, given subClass is accessible from the parameterized class
	 * @should return true if given subClass is accessible from given parameterized class
	 * @should return false if given subClass is not accessible from given parameterized class
	 */
	@SuppressWarnings("unchecked")
	public boolean isSuperClass(Class subClass) {
		return parametrizedClass.isAssignableFrom(subClass);
	}
	
	/**
	 * @param t
	 * @return true if given type is a subclass, or a generic type bounded by a subclass of the
	 *         parameterized class
	 * @should return true for a generic whose bound is a subclass
	 * @should return false for a generic whose bound is not a subclass
	 */
	public boolean isSuperClass(Type t) {
		if (t instanceof TypeVariable<?>) {
			TypeVariable<?> typeVar = (TypeVariable<?>) t;
			if (typeVar.getBounds() == null || typeVar.getBounds().length == 0) {
				return parametrizedClass.equals(Object.class);
			}
			for (Type typeBound : typeVar.getBounds()) {
				if (isSuperClass(typeBound)) {
					return true;
				}
			}
			return false;
		} else if (t instanceof Class<?>) {
			return isSuperClass((Class<?>) t);
		} else {
			throw new IllegalArgumentException("Don't know how to handle: " + t.getClass());
		}
	}
	
	/**
	 * @param object Object
	 * @return true if, given object is accessible from the parameterized class
	 * @should return true if given object is accessible from given parameterized class
	 * @should return false if given object is not accessible from given parameterized class
	 */
	public boolean isSuperClass(Object object) {
		return isSuperClass(object.getClass());
	}
	
	/**
	 * This method validate the given field is Collection and the elements should be of
	 * parameterized type
	 * 
	 * @param field Field
	 * @return boolean
	 * @should return true if given field is Collection and its element type is given parameterized
	 *         class type
	 * @should return false if given field is not a Collection
	 * @should return false if given field is Collection and element type is other than given
	 *         parameterized class type
	 */
	@SuppressWarnings("unchecked")
	public boolean isCollectionField(Field field) {
		if (isCollection(field.getType())) {
			try {
				ParameterizedType type = (ParameterizedType) field.getGenericType();
				if (type.getActualTypeArguments()[0] instanceof Class) {
					return (parametrizedClass.isAssignableFrom((Class) type.getActualTypeArguments()[0]));
				} else if (type.getActualTypeArguments()[0] instanceof TypeVariable) {
					return isSuperClass((TypeVariable<?>) type.getActualTypeArguments()[0]);
				} else {}
			}
			catch (ClassCastException e) {
				// Do nothing.  If this exception is thrown, then field is not a Collection of OpenmrsObjects
			}
		}
		return false;
	}
	
	/**
	 * This method return all the fields (including private) until the given parameterized class
	 * 
	 * @param subClass Class
	 * @return List<Field>
	 * @should return only the sub class fields of given parameterized class
	 */
	@SuppressWarnings("unchecked")
	public List<Field> getInheritedFields(Class subClass) {
		
		List<Field> allFields = getAllFields(subClass);
		for (Iterator iterator = allFields.iterator(); iterator.hasNext();) {
			Field field = (Field) iterator.next();
			if (!hasField(field)) {
				iterator.remove();
			}
		}
		
		return allFields;
	}
	
	/**
	 * @param field
	 * @return true if, given field is declared in parameterized class or its sub classes
	 */
	public boolean hasField(Field field) {
		return isSuperClass(field.getDeclaringClass());
	}
	
}
