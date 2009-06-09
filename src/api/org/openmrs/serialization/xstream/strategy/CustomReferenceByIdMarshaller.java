/*
 * Copyright (C) 2004, 2005, 2006 Joe Walnes.
 * Copyright (C) 2006, 2007, 2008 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 15. March 2004 by Joe Walnes
 */
package org.openmrs.serialization.xstream.strategy;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.reflection.SerializationMethodInvoker;
import com.thoughtworks.xstream.core.TreeMarshaller;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.path.Path;
import com.thoughtworks.xstream.io.path.PathTracker;
import com.thoughtworks.xstream.io.path.PathTrackingWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * This class refers to a part of logic in XStream's source and adds functionality to support
 * building references for CGLIB proxies.
 * 
 * @see CustomReferenceByIdMarshallingStrategy
 * @see CustomObjectIdDictionary
 * @see SequenceGenerator
 */
public class CustomReferenceByIdMarshaller extends TreeMarshaller {
	
	//used to store the id of every element
	private CustomObjectIdDictionary references = new CustomObjectIdDictionary();
	
	private CustomObjectIdDictionary implicitElements = new CustomObjectIdDictionary();
	
	//store all serialized elements in a stack
	private PathTracker pathTracker = new PathTracker();
	
	//the last element which is serialized with a "id" attribute
	private Path lastPath;
	
	private final IDGenerator idGenerator;
	
	//use marker to remove the cglib's signature
	private static final String marker = new String("EnhancerByCGLIB");
	
	public static interface IDGenerator {
		
		String next(Object item);
	}
	
	public CustomReferenceByIdMarshaller(HierarchicalStreamWriter writer, ConverterLookup converterLookup, Mapper mapper,
	    IDGenerator idGenerator) {
		super(writer, converterLookup, mapper);
		this.writer = new PathTrackingWriter(writer, pathTracker);
		this.idGenerator = idGenerator;
	}
	
	public CustomReferenceByIdMarshaller(HierarchicalStreamWriter writer, ConverterLookup converterLookup, Mapper mapper) {
		this(writer, converterLookup, mapper, new SequenceGenerator(1));
	}
	
	/**
	 * get the value of attribute "reference" which is valued as existingReferenceKey.toString()
	 * 
	 * @param currentPath - the path represents the current object which needs to be serialized
	 * @param existingReferenceKey - which's value will be put into the attribute "reference" of a
	 *            element
	 * @return the value of attribute "reference"
	 */
	protected String createReference(Path currentPath, Object existingReferenceKey) {
		return existingReferenceKey.toString();
	}
	
	/**
	 * get the id for the current object needs to be serialized
	 * 
	 * @param currentPath - the path represents the current object which needs to be serialized
	 * @param item - the current object which needs to be serialized
	 * @return a new id for current object
	 */
	protected Object createReferenceKey(Path currentPath, Object item) {
		return idGenerator.next(item);
	}
	
	/**
	 * add a "id" attribute valued as referenceKey into the serialized element
	 * 
	 * @param referenceKey - its string value will be put into serialized element as its attribute
	 *            "id"
	 */
	protected void fireValidReference(Object referenceKey) {
		String attributeName = getMapper().aliasForSystemAttribute("id");
		if (attributeName != null) {
			writer.addAttribute(attributeName, referenceKey.toString());
		}
	}
	
	public static class ReferencedImplicitElementException extends ConversionException {
		
		/**
		 * @deprecated since 1.2.1
		 */
		public ReferencedImplicitElementException(final String msg) {
			super(msg);
		}
		
		public ReferencedImplicitElementException(final Object item, final Path path) {
			super("Cannot reference implicit element");
			add("implicit-element", item.toString());
			add("referencing-element", path.toString());
		}
	}
	
	public void convert(Object item, Converter converter) {
		if (getMapper().isImmutableValueType(item.getClass())) {
			// strings, ints, dates, etc... don't bother using references.
			converter.marshal(item, writer, this);
		} else {
			Path currentPath = pathTracker.getPath();
			//look up whether the object which is referenced(pointed) by "item" has already been serialized in previous
			Object existingReferenceKey = references.lookupId(item);
			if (existingReferenceKey != null) {
				String attributeName = null;
				/*
				 * In cglib, it will return a proxy whose className is "superClassName$$EnhancerByCGLIB$$..." while we want to get one instance of sub class through lazy initialize
				 * For example, if the item's class is "org.openmrs.Person$$EnhancerByCGLIB$$..." and its actual class is "org.openmrs.User",
				 * we will need to add "resolves-to" to show the actual type of this proxy
				 */
				if (this.isCGlibEhanced(item)) {
					//through "callWriteReplace(Object)" of "SerializationMethodInvoker", we can get the actual type of a proxy
					SerializationMethodInvoker serializationMethodInvoker = new SerializationMethodInvoker();
					Object newObj = (Object) serializationMethodInvoker.callWriteReplace(item);
					if (!newObj.getClass().equals(item.getClass().getSuperclass())) {
						//here add "resolves-to" attribute into element, so that while deserializing, xstream can know the actual class through "resolves-to"
						attributeName = getMapper().aliasForSystemAttribute("resolves-to");
						if (attributeName != null) {
							String actualClassName = getMapper().serializedClass(newObj.getClass());
							writer.addAttribute(attributeName, actualClassName);
						}
					}
				}
				attributeName = getMapper().aliasForSystemAttribute("reference");
				if (attributeName != null) {
					writer.addAttribute(attributeName, createReference(currentPath, existingReferenceKey));
				}
			} else if (implicitElements.lookupId(item) != null) {
				throw new ReferencedImplicitElementException(item, currentPath);
			} else {
				Object newReferenceKey = createReferenceKey(currentPath, item);
				if (lastPath == null || !currentPath.isAncestor(lastPath)) {
					fireValidReference(newReferenceKey);
					lastPath = currentPath;
					/*
					 * put the id of current "item" into "references"
					 * so that we can determinate whether the left items need "reference" attribute while they are to be serialized
					 */
					references.associateId(item, newReferenceKey);
				} else {
					implicitElements.associateId(item, newReferenceKey);
				}
				converter.marshal(item, writer, this);
			}
		}
	}
	
	private boolean isCGlibEhanced(Object obj) {
		String className = obj.getClass().getName();
		if (className.indexOf(marker) != -1)
			return true;
		else
			return false;
	}
	
}
