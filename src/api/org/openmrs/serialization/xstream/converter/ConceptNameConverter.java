package org.openmrs.serialization.xstream.converter;

import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import org.openmrs.ConceptName;
import org.openmrs.ConceptNameTag;
import org.openmrs.api.context.Context;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ConceptNameConverter implements Converter {

	public boolean canConvert(Class c) {
		return ConceptName.class.isAssignableFrom(c);
	}
	
	/*
	 * TODO: figure out whether to preserve conceptNameId on unmarshall
	 * TODO: figure out whether to preserve creator and dateCreated
	 */
	
	public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
		ConceptName cn = (ConceptName) obj;

		writer.addAttribute("conceptNameId", cn.getConceptNameId().toString());
		
		writer.startNode("locale");
		context.convertAnother(cn.getLocale());
		writer.endNode();
		writer.startNode("name");
		writer.setValue(cn.getName());
		writer.endNode();
		writer.startNode("voided");
		writer.setValue(cn.getVoided().toString());
		writer.endNode();
		if (cn.getVoidedBy() != null) {
            writer.startNode("voidedBy");
            context.convertAnother(cn.getVoidedBy());
            writer.endNode();
		}
		if (cn.getDateVoided() != null) {
            writer.startNode("dateVoided");
            context.convertAnother(cn.getDateVoided());
            writer.endNode();
		}
		if (cn.getVoidReason() != null) {
            writer.startNode("voidReason");
            writer.setValue(cn.getVoidReason());
            writer.endNode();
		}
        for (Iterator<ConceptNameTag> i = cn.getTags().iterator(); i.hasNext(); ) {
            ConceptNameTag cnt = i.next();
            writer.startNode("tag");
            writer.setValue(cnt.getTag());
            writer.endNode();
        }
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		ConceptName ret = new ConceptName();
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			if ("locale".equals(reader.getNodeName())) {
				ret.setLocale((Locale) context.convertAnother(ret, Locale.class));
			} else if ("name".equals(reader.getNodeName())) {
				ret.setName(reader.getValue());
			} else if ("voided".equals(reader.getNodeName())) {
			    ret.setVoided(Boolean.valueOf(reader.getValue()));
			} else if ("voidedBy".equals(reader.getNodeName())) {
			    // ignore this and let the service handle it ???
			} else if ("dateVoided".equals(reader.getNodeName())) {
			    ret.setDateVoided((Date) context.convertAnother(reader, Date.class));
			} else if ("voidReason".equals(reader.getNodeName())) {
			    ret.setVoidReason(reader.getValue());
			} else if ("tag".equals(reader.getNodeName())) {
			    ret.addTag(Context.getConceptService().getConceptNameTagByName(reader.getValue()));
			}
			reader.moveUp();
		}
		return ret;
	}

}
