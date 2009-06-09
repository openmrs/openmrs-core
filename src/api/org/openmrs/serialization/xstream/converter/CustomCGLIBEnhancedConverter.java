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
package org.openmrs.serialization.xstream.converter;

import net.sf.cglib.proxy.Enhancer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.SerializationMethodInvoker;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.CGLIBMapper;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Converter which deals with CGLIB proxy's serialization/deserialization. While serializing, it
 * will convert the proxy into its actual object and put its properties into the xml string. By
 * default, xstream won't serialize the cglib proxy's properties in the serialized xml string, but
 * just puts a few information in serialized xml string which describe the interfaces, packages and
 * other information of current class, so we need this own-defined "CustomCGLIBEnhancedConverter"
 * for cglib proxies.
 */
public class CustomCGLIBEnhancedConverter implements Converter {
	
	private static Log log = LogFactory.getLog(CustomCGLIBEnhancedConverter.class);
	
	private static String DEFAULT_NAMING_MARKER = "$$EnhancerByCGLIB$$";
	
	private Converter defaultConverter;
	
	private Mapper mapper;
	
	/**
	 * @param mapper
	 * @param converterLookup
	 */
	public CustomCGLIBEnhancedConverter(Mapper mapper, ConverterLookup converterLookup) {
		this.mapper = mapper;
		defaultConverter = converterLookup.lookupConverterForType(Object.class);
	}
	
	/**
	 * @see com.thoughtworks.xstream.converters.Converter#marshal(java.lang.Object,
	 *      com.thoughtworks.xstream.io.HierarchicalStreamWriter,
	 *      com.thoughtworks.xstream.converters.MarshallingContext)
	 */
	public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
		/*
		 * through "SerializationMethodInvoker"'s callWriteReplace method, we
		 * can get the actual type of any cglib proxy
		 */
		SerializationMethodInvoker serializationMethodInvoker = new SerializationMethodInvoker();
		Object newObj = (Object) serializationMethodInvoker.callWriteReplace(obj);
		
		/*
		 * if the proxy represents a sub class, it will give as
		 * follows: We can suppose this proxy represents one instance of
		 * User.class and User.class is a sub class of Person.class, then call
		 * this proxy's getClass().getSuperclass() will return "Person.class" so
		 * we need add a attribute "resolves-to" into the serialized xml string,
		 * then while deserializing, xstream can know the exact type of the
		 * deserialized class.
		 */
		if (!newObj.getClass().equals(obj.getClass().getSuperclass())) {
			/*
			 * add "resolves-to" attribute into element, so that while
			 * deserializing, xstream can know the actual class through
			 * "resolves-to"
			 */
			String attributeName = mapper.aliasForSystemAttribute("resolves-to");
			if (attributeName != null) {
				String actualClassName = this.mapper.serializedClass(newObj.getClass());
				writer.addAttribute(attributeName, actualClassName);
			}
		}
		defaultConverter.marshal(newObj, writer, context);
	}
	
	/**
	 * @see com.thoughtworks.xstream.converters.Converter#unmarshal(com.thoughtworks.xstream.io.HierarchicalStreamReader,
	 *      com.thoughtworks.xstream.converters.UnmarshallingContext)
	 */
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		log.debug("**** UNMARSHAL **** " + context.getRequiredType());
		return null;
	}
	
	/**
	 * @see com.thoughtworks.xstream.converters.ConverterMatcher#canConvert(java.lang.Class)
	 */
	public boolean canConvert(Class type) {
		return (Enhancer.isEnhanced(type) && type.getName().indexOf(DEFAULT_NAMING_MARKER) > 0)
		        || type == CGLIBMapper.Marker.class;
	}
	
}
