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
package org.openmrs.serialization.xstream;

import java.util.HashSet;

import org.hibernate.collection.PersistentSet;
import org.openmrs.Concept;
import org.openmrs.ConceptDescription;
import org.openmrs.api.APIException;
import org.openmrs.serialization.OpenmrsSerializer;
import org.openmrs.serialization.xstream.converter.ConceptAnswerConverter;
import org.openmrs.serialization.xstream.converter.ConceptClassConverter;
import org.openmrs.serialization.xstream.converter.ConceptDatatypeConverter;
import org.openmrs.serialization.xstream.converter.ConceptDescriptionConverter;
import org.openmrs.serialization.xstream.converter.ConceptNameConverter;
import org.openmrs.serialization.xstream.converter.ConceptNameTagConverter;
import org.openmrs.serialization.xstream.converter.ConceptSetConverter;
import org.openmrs.serialization.xstream.converter.CustomSQLTimestampConverter;
import org.openmrs.serialization.xstream.converter.DrugConverter;
import org.openmrs.serialization.xstream.converter.HibernateCollectionConverter;
import org.openmrs.serialization.xstream.converter.LocationConverter;
import org.openmrs.serialization.xstream.converter.UserConverter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.DateConverter;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Implmentations of this interface provide serialization using XStream
 */
public class XStreamSerializer implements OpenmrsSerializer {
	
	private XStream xstream = null;
	
	/**
	 * Default Constructor
	 */
	public XStreamSerializer() {
		xstream = new XStream(new DomDriver());
		
		xstream.registerConverter(new UserConverter());
		xstream.registerConverter(new LocationConverter());
		xstream.registerConverter(new ConceptDatatypeConverter());
		xstream.registerConverter(new ConceptClassConverter());
		xstream.registerConverter(new ConceptNameConverter());
		xstream.registerConverter(new ConceptDescriptionConverter());
		xstream.registerConverter(new ConceptNameTagConverter());
		xstream.registerConverter(new ConceptSetConverter());
		xstream.registerConverter(new ConceptAnswerConverter());
		xstream.registerConverter(new DrugConverter());
		xstream.registerConverter(new HibernateCollectionConverter(xstream.getConverterLookup()));
		xstream.registerConverter(new CustomSQLTimestampConverter());
		xstream.registerConverter(new DateConverter());
		
		xstream.omitField(Concept.class, "log");
		xstream.omitField(Concept.class, "creator");
		xstream.omitField(Concept.class, "changedBy");
		
		xstream.useAttributeFor(Concept.class, "conceptId");
		xstream.useAttributeFor(Concept.class, "set");
		xstream.useAttributeFor(Concept.class, "retired");
		xstream.useAttributeFor(Concept.class, "datatype");
		xstream.useAttributeFor(Concept.class, "conceptClass");
		xstream.useAttributeFor(Concept.class, "version");
		xstream.useAttributeFor(Concept.class, "dateCreated");
		xstream.useAttributeFor(Concept.class, "dateChanged");
		
		xstream.useAttributeFor(ConceptDescription.class, "dateCreated");
		
		xstream.addDefaultImplementation(HashSet.class, PersistentSet.class);
	}
	
	/**
	 * @see org.openmrs.serialization.OpenmrsSerializer#serialize(java.lang.Object)
	 */
	public String serialize(Object o) throws APIException {
		return xstream.toXML(o);
	}
	
	/**
	 * @see org.openmrs.serialization.OpenmrsSerializer#deserialize(java.lang.String,
	 *      java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <T extends Object> T deserialize(String serializedObject, Class<? extends T> clazz) throws APIException {
		return (T) xstream.fromXML(serializedObject);
	}
}
