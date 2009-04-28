package org.openmrs.serialization.xstream.converter;

import org.openmrs.ConceptDatatype;
import org.openmrs.api.context.Context;

import com.thoughtworks.xstream.converters.SingleValueConverter;

public class ConceptDatatypeConverter implements SingleValueConverter {
	
	public boolean canConvert(Class c) {
		return ConceptDatatype.class.isAssignableFrom(c);
	}
	
	public Object fromString(String s) {
		return Context.getConceptService().getConceptDatatypeByName(s);
	}
	
	public String toString(Object o) {
		return ((ConceptDatatype) o).getName();
	}
	
}
