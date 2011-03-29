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
package org.openmrs.serialization;

import org.openmrs.ImplementationId;
import org.openmrs.Patient;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * This serializer uses the xstream library to serialize and deserialize objects.
 * <br/>
 * All classes are automatically aliased.  So a serialization of the {@link Patient} class
 * will not be:
 * <code>
 * &lt;org.openmrs.Patient ...>
 *   &lt;element
 *   ...
 * &lt;/org.openmrs.Patient>
 * </code>
 * but instead will be:
 * <code>
 * &lt;patient ...>
 *   &lt;element
 *   ...
 * &lt;/patient>
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
	 * @param xstream
	 * @throws SerializationException
	 */
	public SimpleXStreamSerializer(XStream customXstream) throws SerializationException {
		if (customXstream == null) {
			
			xstream = new XStream(new DomDriver());
			
		} else {
			this.xstream = customXstream;
		}
		
		//this is added to read the prior simpleframework-serialized values.
		// TODO find a better way to do this.
		this.xstream.useAttributeFor(ImplementationId.class, "implementationId");
		
	}
	
	/**
	 * alias className for class "c", we will use such a form, alias "user" for
	 * "org.openmrs.User.class". The alias principle is to low the first letter of the simple name
	 * of class "c" Note: if in module have a few new classes need to be serialize, call this method
	 * for each new class will automatically alias className for them
	 *
	 * @param c - the class need to alias its className
	 */
	private void aliasClassName(Class<?> c) {
		//through Class.getSimpleName(), we get the short name of a class, such as get "User" for class "org.openmrs.User"
		String simpleName = c.getSimpleName();
		String firstLetter = simpleName.substring(0, 1);
		String leftName = simpleName.substring(1);
		String aliasName = firstLetter.toLowerCase() + leftName;
		xstream.alias(aliasName, c);
	}
	
	/**
	 * Expose the xstream object, so that module can config with xstream as need
	 *
	 * @return xstream can be configed by module
	 */
	public XStream getXstream() {
		return xstream;
	}
	
	/**
	 * @see OpenmrsSerializer#serialize(java.lang.Object)
	 */
	public String serialize(Object o) throws SerializationException {
		aliasClassName(o.getClass());
		return xstream.toXML(o);
	}
	
	/**
	 * @see OpenmrsSerializer#deserialize(String, Class)
	 */
	@SuppressWarnings("unchecked")
	public <T extends Object> T deserialize(String serializedObject, Class<? extends T> clazz) throws SerializationException {
		if (clazz != null) {
			aliasClassName(clazz);
		}
		return (T) xstream.fromXML(serializedObject);
	}
}
