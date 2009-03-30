package org.openmrs.serialization.xstream.converter;

import java.sql.Timestamp;
import java.util.Date;

import org.openmrs.ConceptNameTag;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ConceptNameTagConverter implements Converter {

    public boolean canConvert(Class c) {
        return ConceptNameTag.class.isAssignableFrom(c);
    }

    
    public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
        ConceptNameTag tag = (ConceptNameTag) obj;
        writer.startNode("conceptNameTagId");
        writer.setValue(tag.getConceptNameTagId().toString());
        writer.endNode();
        writer.startNode("tag");
        writer.setValue(tag.getTag());
        writer.endNode();
        writer.startNode("description");
        writer.setValue(tag.getDescription());
        writer.endNode();
        writer.startNode("voided");
        context.convertAnother(tag.getVoided());
        writer.endNode();
        if (tag.getDateVoided() != null) {
            writer.startNode("dateVoided");
            context.convertAnother(tag.getDateVoided());
            writer.endNode();
        }
        if (tag.getVoidReason() != null) {
            writer.startNode("voidReason");
            writer.setValue(tag.getVoidReason());
            writer.endNode();
        }
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        ConceptNameTag ret = new ConceptNameTag();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            if ("conceptNameTagId".equals(reader.getNodeName())) {
                ret.setConceptNameTagId(Integer.valueOf(reader.getValue()));
            } else if ("tag".equals(reader.getNodeName())) {
                ret.setTag(reader.getValue());
            } else if ("description".equals(reader.getNodeName())) {
                ret.setDescription(reader.getValue());
            } else if ("voided".equals(reader.getNodeName())) {
                ret.setVoided(Boolean.valueOf(reader.getValue()));
            } else if ("voidedBy".equals(reader.getNodeName())) {
                // ignore this and let the service handle it ???
            } else if ("dateVoided".equals(reader.getNodeName())) {
                if ("sql-timestamp".equals(reader.getAttribute("class")))
                    ret.setDateVoided((Date) context.convertAnother(reader, Timestamp.class));
                else
                    ret.setDateVoided((Date) context.convertAnother(reader, Date.class));
            } else if ("voidReason".equals(reader.getNodeName())) {
                ret.setVoidReason(reader.getValue());
            }
            reader.moveUp();
        }
        return ret;
    }

}
