package org.openmrs.serial;

import java.io.StringWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.openmrs.BaseTest;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.serial.converter.julie.JulieConverter;
import org.openmrs.serial.converter.xstream.PersonAddressConverter;
import org.openmrs.serial.converter.xstream.UserConverter;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.graph.CycleStrategy;
import org.simpleframework.xml.load.Persister;

import com.thoughtworks.xstream.XStream;

public class XStreamSerializationTest extends BaseTest {
    
    protected UserService userService = null;
    
    protected Object data = null;
    
    @Override
    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();
        authenticate();
    }

    @Override
    protected void onSetUpInTransaction() throws Exception {
        super.onSetUpInTransaction();
        
        // JUnit: use data from DB:
        UserService userService = Context.getUserService();
        User user = userService.createUser(createSerializationUser(), "");
        user.addAddress(createSerializationAddress());
        
        setData(user);
    }

    @Override
    protected void onTearDownInTransaction() throws Exception {
        super.onTearDownInTransaction();
        
        setDefaultRollback(true);

        // JUnit: remove data from DB:
//        UserService userService = Context.getUserService();
//        userService.deleteUser((User)data);
    }

    public static void main(String[] args) throws Exception {
        XStreamSerializationTest test = new XStreamSerializationTest();
        
        // Run from main: use data created in this class rather than from DB:
        User user = test.createSerializationUser();
        user.addAddress(test.createSerializationAddress());
        test.setData(user);
        
        test.testXStreamDefault();
        test.testXStreamDefaultAndOmit();
        test.testXStreamConverters();
        test.testJulie();
        test.testSimple();
    }
    
    protected void setData(Object data) {
        this.data = data;
    }
    
    protected User createSerializationUser() {
        User user = new User();
        user.setUsername("serializationUser");
        user.setDead(false);
        user.setGender("female");
        
        // circular reference
        user.setChangedBy(user);
        
        Calendar calendar = new GregorianCalendar();
        calendar.clear();
        calendar.set(1982, Calendar.MARCH, 3);
        user.setBirthdate(calendar.getTime());
        
        return user;
    }
    
    protected PersonAddress createSerializationAddress() {
        PersonAddress address = new PersonAddress();
        address.setAddress1("Address1");
        address.setAddress2("Address2");
        address.setCityVillage("City");
        address.setCountry("Country");
        Calendar calendar = new GregorianCalendar();
        calendar.clear();
        calendar.set(2007, Calendar.JULY, 19);
        address.setDateCreated(calendar.getTime());

        return address;
    }
    
    public void testXStreamDefault() {
        System.out.println("*** Standard XStream via reflection ***");
        try {
            System.out.println(serializeWithXStream(data));
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
            fail("Serialization failed with an exception: " + e.getMessage());
        }
    }
    
    public void testXStreamDefaultAndOmit() {
        System.out.println("*** XStream via reflection + omitfield ***");
        try {
            System.out.println(serializeWithXStreamOmit(data));
        }
        catch (Exception e) {
            e.printStackTrace(System.out);        
            fail("Serialization failed with an exception: " + e.getMessage());
        }
    }
    
    public void testXStreamConverters() {
        System.out.println("*** Xstream via converters ***");
        try {
            System.out.println(serializeWithXStreamManualConverters(data));
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
            fail("Serialization failed with an exception: " + e.getMessage());
        }
    }
    
    public void testJulie() {
        System.out.println("*** Julie's solution ***");
        try {
            System.out.println(serializeUsingJuliesCode(data));
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
            fail("Serialization failed with an exception: " + e.getMessage());
        }
    }
    
    public void testSimple() {
        System.out.println("*** Simple's solution ***");
        try {
            System.out.println(serializeWithSimple(data));
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
            fail("Serialization failed with an exception: " + e.getMessage());
        }
    }
    
    // Default
    private String serializeWithXStream(Object data) {
        XStream xstream = new XStream();
        
        return xstream.toXML(data);
    }
    
    // No Converters
    private String serializeWithXStreamOmit(Object data) {
        XStream xstream = new XStream();
        
        xstream.omitField(User.class, "log");
        
        xstream.useAttributeFor(User.class, "voided");
        xstream.useAttributeFor(Person.class, "dead");
        xstream.useAttributeFor(Person.class, "voided");
        xstream.aliasAttribute(Person.class, "voided", "personvoided");
        
        xstream.alias("user", User.class);
        xstream.alias("personaddress", PersonAddress.class);
        
        return xstream.toXML(data);    
    }
    
    // Converters
    private String serializeWithXStreamManualConverters(Object data) {
        XStream xstream = new XStream();
        
        xstream.alias("user", User.class);
        xstream.alias("personaddress", PersonAddress.class);
        
        xstream.registerConverter(new UserConverter());
        xstream.registerConverter(new PersonAddressConverter());
        
        return xstream.toXML(data);
    }
    
    // Julie's solution
    private String serializeUsingJuliesCode(Object data) throws Exception {
        if ( !(data instanceof JulieConverter) )
        {
            throw new Exception("Can't serialize objects that don't implement the JulieConverter interface");
        }
        
        Package pkg = new FilePackage();
        Record record = pkg.createRecordForWrite("user");
        Item top = record.getRootItem();
        
        ((JulieConverter)data).save(record, top);
        
        return record.toString();
    }
    
    // Simple.sf.net
    private String serializeWithSimple(Object data) throws Exception {
        // CycleStrategy needed to handle cyclic references. Inserts id/refs all 
        // over the place. Without it we'll run out of heapspace.
        Serializer serializer = new Persister(new CycleStrategy());
        
        StringWriter writer = new StringWriter();
        serializer.write(data, writer);
        
        return writer.toString();
    }
}
