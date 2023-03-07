/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.Hyperlink;
import org.openmrs.module.webservices.rest.web.RestInit;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Custom XStream converter to serialize XML. It is only used for XML, which has
 * autodetectAnnotations enabled in the xStreamMarshaller bean.
 * 
 * @see RestInit
 */
public class SimpleObjectConverter extends AbstractCollectionConverter {
	
	public SimpleObjectConverter(Mapper mapper) {
		super(mapper);
	}
	
	@Override
	public boolean canConvert(Class clazz) {
		return SimpleObject.class.isAssignableFrom(clazz);
	}
	
	@Override
	public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
		if (value instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) value;
			for (Object obj : map.entrySet()) {
				Entry<?, ?> entry = (Entry<?, ?>) obj;
				writer.startNode(entry.getKey().toString());
				//Marshal recursively to process lists or maps with other simple objects
				marshal(entry.getValue(), writer, context);
				writer.endNode();
			}
		} else if (value instanceof List) {
			List<?> list = (List<?>) value;
			for (Object obj : list) {
				if (obj instanceof SimpleObject) {
					//Use custom representation for any subresource
					Hyperlink self = getSelfLink((SimpleObject) obj);
					if (self == null || self.getResourceAlias() == null) {
						writer.startNode("object");
					} else {
						writer.startNode(self.getResourceAlias());
					}
					//Marshal recursively to process lists or maps with other simple objects
					marshal(obj, writer, context);
					writer.endNode();
				} else {
					//Use default representation for any other object
					writeItem(obj, context, writer);
				}
			}
		} else if (value != null) {
			context.convertAnother(value);
		}
	}
	
	/**
	 * Get the self link from a simple object
	 * 
	 * @param object
	 * @return
	 */
	private Hyperlink getSelfLink(SimpleObject object) {
		List<Hyperlink> links = (List<Hyperlink>) object.get("links");
		if (links == null || links.size() == 0) {
			return null;
		}
		for (Hyperlink link : links) {
			if (link.getRel().equals("self")) {
				return link;
			}
		}
		return null;
	}
	
	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		return null;
	}
	
}
