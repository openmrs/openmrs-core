package org.openmrs.serialization.xstream.converter;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Locale;

import org.openmrs.ConceptDescription;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.basic.DateConverter;
import com.thoughtworks.xstream.converters.extended.SqlTimestampConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class ConceptDescriptionConverter implements Converter {

    // these are needed because for some reason convertAnother(Date) isn't working right
    private SingleValueConverter dateConverter = new DateConverter();
    private SingleValueConverter timestampConverter = new SqlTimestampConverter();
    
    public boolean canConvert(Class c) {
        return ConceptDescription.class.isAssignableFrom(c);
    }
    
    /**
     * @see com.thoughtworks.xstream.converters.Converter#marshal(java.lang.Object, com.thoughtworks.xstream.io.HierarchicalStreamWriter, com.thoughtworks.xstream.converters.MarshallingContext)
     */
    public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
        ConceptDescription cd = (ConceptDescription) obj;
        
        writer.addAttribute("conceptDescriptionId", cd.getConceptDescriptionId().toString());
        writer.startNode("locale");
        context.convertAnother(cd.getLocale());
        writer.endNode();
        writer.startNode("description");
        writer.setValue(cd.getDescription());
        writer.endNode();
        writer.startNode("dateCreated");
        // for some reason "context.convertAnother(cd.getDateCreated())" is outputting a Long instead of a properly formatted date string
        if (cd.getDateCreated() instanceof Timestamp)
            writer.setValue(timestampConverter.toString(cd.getDateCreated()));
        else
            writer.setValue(dateConverter.toString(cd.getDateCreated()));
        writer.endNode();
        if (cd.getDateChanged() != null) {
            writer.startNode("dateChanged");
            if (cd.getDateChanged() instanceof Timestamp)
                writer.setValue(timestampConverter.toString(cd.getDateChanged()));
            else
                writer.setValue(dateConverter.toString(cd.getDateChanged()));
            context.convertAnother(cd.getDateChanged());
            writer.endNode();
        }
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        ConceptDescription ret = new ConceptDescription();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            if ("locale".equals(reader.getNodeName())) {
                ret.setLocale((Locale) context.convertAnother(ret, Locale.class));
            } else if ("description".equals(reader.getNodeName())) {
                ret.setDescription(reader.getValue());
            } else if ("dateCreated".equals(reader.getNodeName())) {
                ret.setDateCreated(dateHelper(reader.getValue()));
            } else if ("dateChanged".equals(reader.getNodeName())) {
                ret.setDateChanged(dateHelper(reader.getValue()));
            }
            reader.moveUp();
        }
        return ret;
    }

    private Date dateHelper(String value) {
        try {
            return (Date) timestampConverter.fromString(value);
        } catch (Exception ex) {
            return (Date) dateConverter.fromString(value);
        }
    }
    
}
