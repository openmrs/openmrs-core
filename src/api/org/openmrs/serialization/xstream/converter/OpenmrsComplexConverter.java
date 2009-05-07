package org.openmrs.serialization.xstream.converter;

import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.User;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class OpenmrsComplexConverter implements Converter {
	
	public boolean canConvert(Class c) {
		return ConceptDatatype.class.isAssignableFrom(c) || ConceptClass.class.isAssignableFrom(c);
		// || User.class.isAssignableFrom(c);
	}
	
	public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
		if (obj instanceof ConceptDatatype) {
			ConceptDatatype d = (ConceptDatatype) obj;
			writer.setValue(d.getName());
		} else if (obj instanceof ConceptClass) {
			ConceptClass c = (ConceptClass) obj;
			writer.setValue(c.getName());
		} else if (obj instanceof User) {
			User u = (User) obj;
			writer.setValue(u.getUsername());
		}
	}
	
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		return null;
	}
	
}
