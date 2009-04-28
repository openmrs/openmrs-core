package org.openmrs.serialization.xstream.converter;

import org.openmrs.ConceptAnswer;
import org.openmrs.Drug;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ConceptAnswerConverter implements Converter {
	
	public boolean canConvert(Class c) {
		return ConceptAnswer.class.isAssignableFrom(c);
	}
	
	public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
		ConceptAnswer ca = (ConceptAnswer) obj;
		writer.addAttribute("conceptAnswerId", ca.getConceptAnswerId().toString());
		writer.startNode("concept");
		writer.setValue(ca.getConcept().getConceptId().toString());
		writer.endNode();
		writer.startNode("answerConcept");
		try {
			writer.setValue(ca.getAnswerConcept().getConceptId().toString());
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		writer.endNode();
		if (ca.getAnswerDrug() != null) {
			writer.startNode("answerDrug");
			context.convertAnother(ca.getAnswerDrug());
			writer.endNode();
		}
	}
	
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		ConceptAnswer ret = new ConceptAnswer();
		ConceptService cs = Context.getConceptService();
		ret.setConceptAnswerId(Integer.valueOf(reader.getAttribute("conceptAnswerId")));
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			if ("concept".equals(reader.getNodeName())) {
				ret.setConcept(cs.getConcept(Integer.valueOf(reader.getValue())));
			} else if ("answerConcept".equals(reader.getNodeName())) {
				ret.setAnswerConcept(cs.getConcept(Integer.valueOf(reader.getValue())));
			} else if ("answerDrug".equals(reader.getNodeName())) {
				ret.setAnswerDrug((Drug) context.convertAnother(ret, Drug.class));
			}
			reader.moveUp();
		}
		if (ret.getAnswerConcept() == null && ret.getAnswerDrug() == null)
			throw new IllegalArgumentException("Couldn't find answer concept and/or drug for ConceptAnswer "
			        + ret.getConceptAnswerId());
		return ret;
	}
	
}
