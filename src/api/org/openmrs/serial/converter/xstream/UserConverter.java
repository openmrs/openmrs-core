package org.openmrs.serial.converter.xstream;

import org.openmrs.User;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class UserConverter implements Converter {

    /**
     * @see com.thoughtworks.xstream.converters.Converter#marshal(java.lang.Object, com.thoughtworks.xstream.io.HierarchicalStreamWriter, com.thoughtworks.xstream.converters.MarshallingContext)
     */
    public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context) {
        User user = (User)object;
        
        // NOTE: User has one voided, it's "parent class" Person has another with 
        // the same name that can't be reached without reflection. They might differ 
        // since a Person can be voided and/or a User (same goes for Patient)
        writer.addAttribute("voided", Boolean.toString(user.getVoided()));
        writer.addAttribute("dead", Boolean.toString(user.getDead()));
        
        writer.startNode("username");
        writer.setValue(user.getUsername());
        writer.endNode();
        
        writer.startNode("changedby");
        context.convertAnother(user.getChangedBy());
        writer.endNode();
        
        writer.startNode("addresses");
        context.convertAnother(user.getAddresses());
        writer.endNode();
        
        writer.startNode("birthdate");
        writer.addAttribute("estimated", Boolean.toString(user.getBirthdateEstimated()));
        writer.setValue(user.getBirthdate().toString());
        writer.endNode();
    }

    /**
     * @see com.thoughtworks.xstream.converters.Converter#unmarshal(com.thoughtworks.xstream.io.HierarchicalStreamReader, com.thoughtworks.xstream.converters.UnmarshallingContext)
     */
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        return null;
    }

    /**
     * @see com.thoughtworks.xstream.converters.ConverterMatcher#canConvert(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public boolean canConvert(Class clazz) {
        return clazz.equals(User.class);
    }

}
