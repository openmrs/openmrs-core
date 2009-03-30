package org.openmrs.serialization.xstream.converter;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import com.thoughtworks.xstream.converters.SingleValueConverter;

public class CustomSQLTimestampConverter implements SingleValueConverter {
    
    public boolean canConvert(Class c) {
        return Timestamp.class.isAssignableFrom(c);
    }
    
    public Object fromString(String s) {
       long i = Long.parseLong(s);
       Calendar cal = Calendar.getInstance();
       cal.setTimeInMillis(i);
       return new Timestamp(cal.getTime().getTime());
    }

    public String toString(Object o) {
        Timestamp ts = (Timestamp) o;
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(ts.getTime()));
        return String.valueOf(cal.getTimeInMillis());
    }
    
    
}




