package org.openmrs.serialization.xstream.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.hibernate.collection.PersistentList;
import org.hibernate.collection.PersistentMap;
import org.hibernate.collection.PersistentSet;
import org.hibernate.collection.PersistentSortedMap;
import org.hibernate.collection.PersistentSortedSet;

import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * Replaces Hibernate 3 specific collections with java.util implementations. This mapper takes care
 * only of the writing to the XML (deflating) not the other way around (inflating) because there is
 * no need.
 * 
 * @author Costin Leau
 */
public class HibernateCollectionMapper extends MapperWrapper {
	
	private final static String[] hbClassNames = { PersistentList.class.getName(), PersistentSet.class.getName(),
	        PersistentMap.class.getName(), PersistentSortedSet.class.getName(), PersistentSortedMap.class.getName() };
	
	private final static String[] jdkClassNames = { ArrayList.class.getName(), HashSet.class.getName(),
	        HashMap.class.getName(), TreeSet.class.getName(), TreeMap.class.getName() };
	
	private final static Class[] hbClasses = { PersistentList.class, PersistentSet.class, PersistentMap.class,
	        PersistentSortedSet.class, PersistentSortedMap.class };
	
	private final static Class[] jdkClasses = { ArrayList.class, HashSet.class, HashMap.class, TreeSet.class, TreeMap.class };
	
	public HibernateCollectionMapper(Mapper wrapped) {
		super(wrapped);
	}
	
	/**
	 * @see com.thoughtworks.xstream.mapper.Mapper#serializedClass(java.lang.Class)
	 */
	public String serializedClass(Class type) {
		return super.serializedClass(replaceClasses(type));
	}
	
	/**
	 * @see com.thoughtworks.xstream.mapper.Mapper#serializedMember(java.lang.Class,
	 *      java.lang.String)
	 */
	public String serializedMember(Class type, String fieldName) {
		return super.serializedMember(replaceClasses(type), fieldName);
	}
	
	/**
	 * Simple replacements between the HB 3 collections and their underlying collections from
	 * java.util.
	 * 
	 * @param name
	 * @return the equivalent JDK class name
	 */
	private String replaceClasses(String name) {
		for (int i = 0; i < hbClassNames.length; i++) {
			if (name.equals(hbClassNames[i]))
				return jdkClassNames[i];
		}
		return name;
	}
	
	/**
	 * Simple replacements between the HB 3 collections and their underlying collections from
	 * java.util.
	 * 
	 * @param clazz
	 * @return the equivalent JDK class
	 */
	private Class replaceClasses(Class clazz) {
		for (int i = 0; i < hbClasses.length; i++) {
			if (clazz.equals(hbClasses[i]))
				return jdkClasses[i];
		}
		return clazz;
	}
}
