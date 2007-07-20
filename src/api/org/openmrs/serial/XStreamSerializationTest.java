package org.openmrs.serial;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.User;
import org.openmrs.serial.converter.julie.JulieConverter;
import org.openmrs.serial.converter.xstream.PersonAddressConverter;
import org.openmrs.serial.converter.xstream.UserConverter;

import com.ibatis.common.logging.Log;
import com.thoughtworks.xstream.XStream;

public class XStreamSerializationTest {

    public static void main(String[] args) throws Exception {
        XStreamSerializationTest test = new XStreamSerializationTest();
        
        Object object = test.createObjectStructure();
        
        System.out.println("*** Standard XStream via reflection ***");
        System.out.println(test.serializeWithXStream(object));
        
        System.out.println("*** XStream via reflection + omitfield ***");
        System.out.println(test.serializeWithXStreamOmit(object));
        
        System.out.println("*** Xstream via converters ***");
        System.out.println(test.serializeWithXStreamManualConverters(object));
        
        System.out.println("*** Julie's solution ***");
        System.out.println(test.serializeUsingJuliesCode(object));
    }

    private Object createObjectStructure() {
        User user = new User();
        user.setUsername("Anders");
        user.setDead(false);
        // loop
        user.setChangedBy(user);
        // null value
        user.setCreator(null);
        
        Calendar calendar = new GregorianCalendar();
        calendar.clear();
        calendar.set(1982, Calendar.MARCH, 3);
        user.setBirthdate(calendar.getTime());
        
        PersonAddress address = new PersonAddress();
        address.setAddress1("Address1");
        address.setAddress2("Address2");
        address.setCityVillage("City");
        address.setCountry("Norway");
        calendar.clear();
        calendar.set(2007, Calendar.JULY, 19);
        address.setDateCreated(calendar.getTime());
        
        Set<PersonAddress> addressSet = new HashSet<PersonAddress>();
        addressSet.add(address);
        user.setAddresses(addressSet);
        // loop
        
        return user;
    }
    
    // Default
    private String serializeWithXStream(Object object) {
        XStream xstream = new XStream();
        
        return xstream.toXML(object);
    }
    
    // No Converters
    // +: less work, can reach all the voided fields
    // -: (can't change birthdateEstimated into attribute of birthdate since that's just another field)
    private String serializeWithXStreamOmit(Object object) {
        XStream xstream = new XStream();
        
        xstream.omitField(User.class, "log");
        
        xstream.useAttributeFor(User.class, "voided");
        xstream.useAttributeFor(Person.class, "dead");
        xstream.useAttributeFor(Person.class, "voided");
        xstream.aliasAttribute(Person.class, "voided", "personvoided");
        
        xstream.alias("user", User.class);
        xstream.alias("personaddress", PersonAddress.class);
        
        return xstream.toXML(object);    
    }
    
    // Converters
    private String serializeWithXStreamManualConverters(Object object) {
        XStream xstream = new XStream();
        
        xstream.alias("user", User.class);
        xstream.alias("personaddress", PersonAddress.class);
        
        xstream.registerConverter(new UserConverter());
        xstream.registerConverter(new PersonAddressConverter());
        
        return xstream.toXML(object);
    }
    
    // Julie's solution
    private String serializeUsingJuliesCode(Object object) throws Exception {
        if ( !(object instanceof JulieConverter) )
        {
            throw new Exception("Can't serialize objects that don't implement the JulieConverter interface");
        }
        
        Package pkg = new FilePackage();
        Record record = pkg.createRecordForWrite("user");
        Item top = record.getRootItem();
        
        ((JulieConverter)object).save(record, top);
        
        return record.toString();
    }
}
