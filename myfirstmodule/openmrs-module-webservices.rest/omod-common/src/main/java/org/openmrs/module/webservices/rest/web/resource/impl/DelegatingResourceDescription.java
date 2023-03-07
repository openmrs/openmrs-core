/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.resource.impl;

import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.Hyperlink;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.RepresentationDescription;
import org.openmrs.module.webservices.rest.web.response.ConversionException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Used by implementations of {@link DelegatingCrudResource} to indicate what delegate properties,
 * and what methods they want to include in a particular representation
 */
public class DelegatingResourceDescription implements RepresentationDescription {
	
	Map<String, Property> properties = new LinkedHashMap<String, Property>();
	
	List<Hyperlink> links = new ArrayList<Hyperlink>();
	
	public void addProperty(String propertyName) {
		addProperty(propertyName, propertyName, null, false);
	}
	
	public void addRequiredProperty(String propertyName) {
		addProperty(propertyName, propertyName, null, true);
	}
	
	public void addProperty(String propertyName, Representation rep) {
		addProperty(propertyName, propertyName, rep, false);
	}
	
	public void addRequiredProperty(String propertyName, Representation rep) {
		addProperty(propertyName, propertyName, rep, true);
	}
	
	public void addProperty(String propertyName, Representation rep, Class<?> convertAs) {
		addProperty(propertyName, propertyName, rep, false, convertAs);
	}
	
	public void addProperty(String propertyName, Method method) {
		addProperty(propertyName, method, null, false);
	}
	
	public void addProperty(String propertyName, String delegatePropertyName) {
		addProperty(propertyName, delegatePropertyName, null, false);
	}
	
	public void addRequiredProperty(String propertyName, String delegatePropertyName) {
		addProperty(propertyName, delegatePropertyName, null, true);
	}
	
	public void addProperty(String propertyName, String delegatePropertyName, Representation rep) {
		addProperty(propertyName, delegatePropertyName, rep, false);
	}
	
	public void addRequiredProperty(String propertyName, String delegatePropertyName, Representation rep) {
		addProperty(propertyName, delegatePropertyName, rep, true);
	}
	
	public void addProperty(String propertyName, Method method, Representation rep) {
		addProperty(propertyName, method, rep, false);
	}
	
	public void addProperty(String propertyName, String delegatePropertyName, Representation rep, boolean required) {
		addProperty(propertyName, delegatePropertyName, rep, required, null);
	}
	
	public void addProperty(String propertyName, String delegatePropertyName, Representation rep, boolean required,
	        Class<?> convertAs) {
		if (rep == null)
			rep = Representation.DEFAULT;
		Property property = new Property(delegatePropertyName, rep, required);
		property.setConvertAs(convertAs);
		properties.put(propertyName, property);
	}
	
	public void addProperty(String propertyName, Method method, Representation rep, boolean required) {
		if (rep == null)
			rep = Representation.DEFAULT;
		properties.put(propertyName, new Property(method, rep, required));
	}
	
	/**
	 * Removes the given property
	 * 
	 * @param propertyName
	 */
	public void removeProperty(String propertyName) {
		properties.remove(propertyName);
	}
	
	public DelegatingResourceDescription addSelfLink() {
		return addLink("self", ".");
	}
	
	public DelegatingResourceDescription addLink(String rel, String uri) {
		links.add(new Hyperlink(rel, uri));
		return this;
	}
	
	/**
	 * @return the properties
	 */
	public Map<String, Property> getProperties() {
		return properties;
	}
	
	/**
	 * @return the links
	 */
	public List<Hyperlink> getLinks() {
		return links;
	}
	
	/**
	 * A property that will be included in a representation
	 */
	public class Property {
		
		private String delegateProperty;
		
		private Method method;
		
		private Representation rep;
		
		private Class<?> convertAs;
		
		private boolean required;
		
		public Property(String delegateProperty, Representation rep) {
			this.delegateProperty = delegateProperty;
			this.rep = rep;
			this.required = false;
		}
		
		public Property(String delegateProperty, Representation rep, boolean required) {
			this.delegateProperty = delegateProperty;
			this.rep = rep;
			this.required = required;
		}
		
		public Property(Method method, Representation rep) {
			this.method = method;
			this.rep = rep;
			this.required = false;
		}
		
		public Property(Method method, Representation rep, boolean required) {
			this.method = method;
			this.rep = rep;
			this.required = required;
		}
		
		/**
		 * @return the delegateProperty
		 */
		public String getDelegateProperty() {
			return delegateProperty;
		}
		
		/**
		 * @param delegateProperty the delegateProperty to set
		 */
		public void setDelegateProperty(String delegateProperty) {
			this.delegateProperty = delegateProperty;
		}
		
		/**
		 * @return the method
		 */
		public Method getMethod() {
			return method;
		}
		
		/**
		 * @param method the method to set
		 */
		public void setMethod(Method method) {
			this.method = method;
		}
		
		/**
		 * @return the rep
		 */
		public Representation getRep() {
			return rep;
		}
		
		/**
		 * @param rep the rep to set
		 */
		public void setRep(Representation rep) {
			this.rep = rep;
		}
		
		/**
		 * @return the required
		 */
		public boolean isRequired() {
			return required;
		}
		
		/**
		 * @param required the required to set
		 */
		public void setRequired(boolean required) {
			this.required = required;
		}
		
		/**
		 * @return the specific class to convert this property as (which
		 */
		public Class<?> getConvertAs() {
			return convertAs;
		}
		
		/**
		 * In case you want to force a specific converter, e.g. in relationship resource, force
		 * personA to be converted by the person resource even if personA is actually a Patient
		 * 
		 * @param convertAs convert this property with the converter for this specific class, which
		 *            should be a superclass of any object you intend to convert
		 */
		public void setConvertAs(Class<?> convertAs) {
			this.convertAs = convertAs;
		}
		
		public <T> Object evaluate(BaseDelegatingConverter<T> converter, T delegate) throws ConversionException {
			if (delegateProperty != null) {
				Object propVal = converter.getProperty(delegate, delegateProperty);
				if (propVal instanceof Collection) {
					List<Object> ret = new ArrayList<Object>();
					for (Object element : (Collection<?>) propVal)
						ret.add(ConversionUtil.convertToRepresentation(element, rep, getConvertAs()));
					return ret;
				} else {
					return ConversionUtil.convertToRepresentation(propVal, rep, getConvertAs());
				}
			} else if (method != null) {
				try {
					return method.invoke(converter, delegate);
				}
				catch (Exception ex) {
					throw new ConversionException("method " + method, ex);
				}
			} else {
				throw new RuntimeException("Property with no delegateProperty or method specified");
			}
		}
		
	}
}
