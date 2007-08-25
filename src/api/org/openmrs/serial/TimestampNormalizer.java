package org.openmrs.serial;

import java.io.Serializable;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class TimestampNormalizer extends Normalizer
{
    public static final String DATETIME_MASK = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    
    public String toString(Object o) {
        
        java.sql.Date d;
        java.sql.Time t;
        long time;
        String result = null;

        if (o instanceof java.sql.Timestamp){
            result = ((java.sql.Timestamp)o).toString();
        }
        else if (o instanceof java.sql.Date){
            d = (java.sql.Date)o;
            t = new java.sql.Time(d.getTime());
            result = d.toString() + ' ' + t.toString();
        }
        else if (o instanceof java.util.Date){
            SimpleDateFormat dfm = new SimpleDateFormat(TimestampNormalizer.DATETIME_MASK);
            result = dfm.format((Date)o);;
        }
        else if (o instanceof java.util.Calendar){
            time = ((java.util.Calendar)o).getTime().getTime();
            d = new java.sql.Date(time);
            t = new java.sql.Time(time);
            result = d.toString() + ' ' + t.toString();
        }
        else {
            log.warn("Unknown class in timestamp " + o.getClass().getName());
            result = o.toString();//ugh
        }

        return result;
    }
    
    @Override
    public Object fromString(Class clazz, String s) {
        //TODO - this needs work

        if (s == null || "".equals(s)) return null;
        
        if (clazz.getName() == "java.util.Date") {
            //result = d.toString() + ' ' + t.toString();
            Date result = null;                          
            SimpleDateFormat dfm = new SimpleDateFormat(TimestampNormalizer.DATETIME_MASK);
            try {
                result = dfm.parse(s.trim());
            }
            catch(Exception e) {
                log.error("Failed to parse timestamp. Mask is: " + dfm.toPattern() + " and value: " + s, e);
                //TODO throw e;
            }
            return result;
        }
        
        return null;
    }
}
