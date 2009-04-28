package org.openmrs.serialization.xstream.converter;

import org.openmrs.ConceptSet;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ConceptSetConverter implements Converter {
	
	public boolean canConvert(Class c) {
		return ConceptSet.class.isAssignableFrom(c);
	}
	
	public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
		ConceptSet cs = (ConceptSet) obj;
		writer.startNode("concept");
		writer.setValue(cs.getConcept().getConceptId().toString());
		writer.endNode();
		writer.startNode("conceptSet");
		writer.setValue(cs.getConceptSet().getConceptId().toString());
		writer.endNode();
		writer.startNode("sortWeight");
		writer.setValue(cs.getSortWeight().toString());
		writer.endNode();
	}
	
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		ConceptSet ret = new ConceptSet();
		ConceptService cs = Context.getConceptService();
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			if ("concept".equals(reader.getNodeName())) {
				ret.setConcept(cs.getConcept(Integer.valueOf(reader.getValue())));
			} else if ("conceptSet".equals(reader.getNodeName())) {
				ret.setConceptSet(cs.getConcept(Integer.valueOf(reader.getValue())));
			} else if ("sortWeight".equals(reader.getNodeName())) {
				ret.setSortWeight(Double.valueOf(reader.getValue()));
			}
			reader.moveUp();
		}
		return ret;
	}
	
}
