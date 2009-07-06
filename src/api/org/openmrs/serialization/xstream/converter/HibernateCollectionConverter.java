package org.openmrs.serialization.xstream.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.collection.PersistentSet;
import org.hibernate.collection.PersistentSortedSet;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * XStream converter that strips HB collection specific information and retrieves the underlying
 * collection which is then parsed by the delegated converter. This converter only takes care of the
 * values inside the collections while the mapper takes care of the collection naming.
 * 
 * @author Costin Leau
 */
public class HibernateCollectionConverter implements Converter {
	
	private static Log log = LogFactory.getLog(HibernateCollectionConverter.class);
	
	private Converter listSetConverter;
	
	private Converter mapConverter;
	
	private Converter treeMapConverter;
	
	private Converter treeSetConverter;
	
	private Converter hashSetConverter;
	
	private Converter defaultConverter;
	
	public HibernateCollectionConverter(ConverterLookup converterLookup) {
		listSetConverter = converterLookup.lookupConverterForType(ArrayList.class);
		mapConverter = converterLookup.lookupConverterForType(HashMap.class);
		treeMapConverter = converterLookup.lookupConverterForType(TreeMap.class);
		treeSetConverter = converterLookup.lookupConverterForType(TreeSet.class);
		hashSetConverter = converterLookup.lookupConverterForType(HashSet.class);
		defaultConverter = converterLookup.lookupConverterForType(Object.class);
	}
	
	/**
	 * @see com.thoughtworks.xstream.converters.Converter#canConvert(java.lang.Class)
	 */
	public boolean canConvert(Class type) {
		return PersistentCollection.class.isAssignableFrom(type);
	}
	
	/**
	 * @see com.thoughtworks.xstream.converters.Converter#marshal(java.lang.Object,
	 *      com.thoughtworks.xstream.io.HierarchicalStreamWriter,
	 *      com.thoughtworks.xstream.converters.MarshallingContext)
	 */
	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		Object collection = source;
		if (source instanceof PersistentCollection) {
			PersistentCollection col = (PersistentCollection) source;
			if (!Hibernate.isInitialized(source)) {
				col.forceInitialization();
			}
			collection = col.getStoredSnapshot();
			
		}
		
		// the set is returned as a map by Hibernate (unclear why exactly)
		if (PersistentSortedSet.class.equals(source.getClass())) {
			collection = new TreeSet(((HashMap) collection).keySet());
		} else if (PersistentSet.class.equals(source.getClass())) {
			collection = new HashSet(((HashMap) collection).keySet());
		}
		
		// delegate the collection to the approapriate converter
		if (listSetConverter.canConvert(collection.getClass())) {
			listSetConverter.marshal(collection, writer, context);
			return;
		}
		if (mapConverter.canConvert(collection.getClass())) {
			mapConverter.marshal(collection, writer, context);
			return;
		}
		if (treeMapConverter.canConvert(collection.getClass())) {
			treeMapConverter.marshal(collection, writer, context);
			return;
		}
		if (treeSetConverter.canConvert(collection.getClass())) {
			treeSetConverter.marshal(collection, writer, context);
			return;
		}
		if (hashSetConverter.canConvert(collection.getClass())) {
			hashSetConverter.marshal(collection, writer, context);
			return;
		}
		
		defaultConverter.marshal(collection, writer, context);
	}
	
	/**
	 * @see com.thoughtworks.xstream.converters.Converter#unmarshal(com.thoughtworks.xstream.io.HierarchicalStreamReader,
	 *      com.thoughtworks.xstream.converters.UnmarshallingContext)
	 */
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		log.debug("**** UNMARSHAL **** " + context.getRequiredType());
		return null;
	}
}
