/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.serialization;

import org.openmrs.ImplementationId;
import org.openmrs.Patient;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.extended.DynamicProxyConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * This serializer uses the xstream library to serialize and deserialize objects.
 * <br>
 * All classes are automatically aliased.  So a serialization of the {@link Patient} class
 * will not be:
 * <code>
 * &lt;org.openmrs.Patient ...&gt;
 *   &lt;element
 *   ...
 * &lt;/org.openmrs.Patient&gt;
 * </code>
 * but instead will be:
 * <code>
 * &lt;patient ...&gt;
 *   &lt;element
 *   ...
 * &lt;/patient&gt;
 * </code>
 *
 */
public class SimpleXStreamSerializer implements OpenmrsSerializer {
	
	// cached xstream object
	public XStream xstream = null;
	
	/**
	 * Default Constructor
	 *
	 * @throws SerializationException
	 */
	public SimpleXStreamSerializer() throws SerializationException {
		this(null);
	}
	
	/**
	 * Constructor that takes a custom XStream object
	 * @param customXstream
	 * @throws SerializationException
	 */
	public SimpleXStreamSerializer(XStream customXstream) throws SerializationException {
		if (customXstream == null) {
			
			xstream = new XStream();
			
		} else {
			this.xstream = customXstream;
		}
		xstream.registerConverter(new OpenmrsDynamicProxyConverter(), XStream.PRIORITY_VERY_HIGH);
		
		//this is added to read the prior simpleframework-serialized values.
		// TODO find a better way to do this.
		this.xstream.useAttributeFor(ImplementationId.class, "implementationId");
		
	}
	
	/**
	 * Expose the xstream object, so that module can config with xstream as need
	 *
	 * @return xstream can be configured by module
	 */
	public XStream getXstream() {
		return xstream;
	}
	
	/**
	 * @see OpenmrsSerializer#serialize(java.lang.Object)
	 * <strong>Should</strong> not serialize proxies
	 */
	@Override
	public String serialize(Object o) throws SerializationException {
		
		return xstream.toXML(o);
	}
	
	/**
	 * @see OpenmrsSerializer#deserialize(String, Class)
	 * <strong>Should</strong> not deserialize proxies
	 * <strong>Should</strong> ignore entities
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T deserialize(String serializedObject, Class<? extends T> clazz) throws SerializationException {
		
		try {
			return (T) xstream.fromXML(serializedObject);
		}
		catch (XStreamException e) {
			throw new SerializationException("Unable to deserialize class: " + clazz.getName(), e);
		}
	}
	
	/**
	 * An instance of this converter needs to be registered with a higher priority than the rest so
	 * that it's called early in the converter chain. This way, we can make sure we never get to
	 * xstream's DynamicProxyConverter that can deserialize proxies.
	 *
	 * @see <a href="http://tinyurl.com/ord2rry">this blog</a>
	 */
	private class OpenmrsDynamicProxyConverter extends DynamicProxyConverter {
		
		OpenmrsDynamicProxyConverter() {
			super(null);
		}
		
		@Override
		public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
			throw new XStreamException("Can't serialize proxies");
		}
		
		@Override
		public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
			throw new XStreamException("Can't deserialize proxies");
		}
		
	}
}
