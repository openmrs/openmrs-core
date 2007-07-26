package org.openmrs.serial.converter.xstream;

import org.openmrs.PersonAddress;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class PersonAddressConverter implements Converter {

    /**
     * @see com.thoughtworks.xstream.converters.Converter#marshal(java.lang.Object, com.thoughtworks.xstream.io.HierarchicalStreamWriter, com.thoughtworks.xstream.converters.MarshallingContext)
     */
    public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context) {
        PersonAddress address = (PersonAddress)object;
        
        writer.addAttribute("preferred", Boolean.toString(address.getPreferred()));
        writer.addAttribute("datecreated", address.getDateCreated().toString());
        writer.addAttribute("voided", Boolean.toString(address.getVoided()));
        
        writer.startNode("address1");
        writer.setValue(address.getAddress1());
        writer.endNode();
        
        writer.startNode("address2");
        context.convertAnother(address.getAddress2());
        writer.endNode();
        
        writer.startNode("cityvillage");
        context.convertAnother(address.getCityVillage());
        writer.endNode();
        
        writer.startNode("country");
        context.convertAnother(address.getCountry());
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
        return clazz.equals(PersonAddress.class);
//        clazz.asSubclass(clazz);
//        clazz.getDeclaringClass();
//        clazz.getEnclosingClass();
//        clazz.getGenericSuperclass();
//        clazz.getSuperclass();
//        clazz.isAssignableFrom(cls);
    }

}
