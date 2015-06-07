/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.xml;

import java.beans.Introspector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.collection.spi.PersistentCollection;
import org.simpleframework.xml.graph.LabelLogic;
import org.simpleframework.xml.stream.NodeMap;

/**
 * This label logic will convert hibernate proxy class names to their equivalent pojo class names
 *
 * @deprecated - Use OpenmrsSerializer from Context.getSerializationService.getDefaultSerializer()
 */
@Deprecated
public class OpenmrsLabelLogic implements LabelLogic {
	
	private static Log log = LogFactory.getLog(OpenmrsLabelLogic.class);
	
	/**
	 * @see org.simpleframework.xml.graph.LabelLogic#getLabel(java.lang.Class, java.lang.Object,
	 *      java.lang.Class, org.simpleframework.xml.stream.NodeMap)
	 */
	@SuppressWarnings("unchecked")
	public String getLabel(Class field, Object value, Class real, NodeMap node) {
		String simpleName = real.getSimpleName();
		simpleName = Introspector.decapitalize(simpleName);
		Class type = value.getClass();
		
		if (type != field || !node.getName().equals(simpleName)) {
			
			// default value for "label"'s value is the name of the class
			String realClassName = real.getName();
			
			// if its a hibernate set, get the real object's type
			if (value instanceof PersistentCollection) {
				realClassName = getHibernateInstanceClass(type.getName());
			}
			
			// if we're cglib enhanced, ignore putting this class on the node
			if (!realClassName.contains("CGLIB")) {
				
				// don't have to return the classes for basic things
				if (type != field) {
					return realClassName;
				}
			} else {
				// check for each of the overriding pojo types
				for (String objectName : new String[] { "User", "Patient", "ComplexObs", "ConceptNumeric" }) {
					String className = "org.openmrs." + objectName;
					if (realClassName.startsWith(className) && !field.getName().equals(className)) {
						return className;
					}
				}
				
				return realClassName.substring(0, realClassName.indexOf("$$EnhancerByCGLIB"));
			}
		}
		
		// don't put a label in
		return null;
	}
	
	/**
	 * Convenience method to alter the name of hibernate collections to their normal non-proxied
	 * equivalents
	 *
	 * @param typename classname of the object
	 * @return deproxied classname
	 */
	private String getHibernateInstanceClass(String typename) {
		if (typename.equals("org.hibernate.collection.PersistentSet")) {
			return "java.util.HashSet";
		}
		if (typename.equals("org.hibernate.collection.PersistentSortedSet")) {
			return "java.util.TreeSet";
		} else if (typename.equals("org.hibernate.collection.PersistentList")) {
			return "java.util.ArrayList";
		} else if (typename.equals("org.hibernate.collection.PersistentMap")) {
			return "java.util.Map";
		} else if (typename.contains("hibernate")) {
			log.warn("Unknown possible invalid serialized object type: " + typename);
		}
		
		return typename;
	}
}
