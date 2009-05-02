package org.openmrs.serialization.xstream.converter;

import org.openmrs.ConceptClass;
import org.openmrs.api.context.Context;

import com.thoughtworks.xstream.converters.SingleValueConverter;

public class ConceptClassConverter implements SingleValueConverter {
	
	public boolean canConvert(Class c) {
		return ConceptClass.class.isAssignableFrom(c);
	}
	
	public Object fromString(String s) {
		return Context.getConceptService().getConceptClassByName(s);
	}
	
	public String toString(Object o) {
		return ((ConceptClass) o).getName();
	}
	
}
