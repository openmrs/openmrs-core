/*
 * Copyright (C) 2004, 2005 Joe Walnes.
 * Copyright (C) 2006, 2007 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 16. March 2004 by Joe Walnes
 */
package org.openmrs.serialization.xstream.strategy;

import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.core.ReferenceByIdMarshallingStrategy;
import com.thoughtworks.xstream.core.TreeMarshaller;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Override the "createMarshallingContext" method of "ReferenceByIdMarshallingStrategy", so that it
 * can build reference for CGLIB proxies.
 * 
 * @see CustomReferenceByIdMarshaller
 */
public class CustomReferenceByIdMarshallingStrategy extends ReferenceByIdMarshallingStrategy {
	
	/**
	 * @see com.thoughtworks.xstream.core.ReferenceByIdMarshallingStrategy#createMarshallingContext(com.thoughtworks.xstream.io.HierarchicalStreamWriter,
	 *      com.thoughtworks.xstream.converters.ConverterLookup,
	 *      com.thoughtworks.xstream.mapper.Mapper)
	 */
	@Override
	protected TreeMarshaller createMarshallingContext(HierarchicalStreamWriter writer, ConverterLookup converterLookup,
	                                                  Mapper mapper) {
		return new CustomReferenceByIdMarshaller(writer, converterLookup, mapper);
	}
	
}
