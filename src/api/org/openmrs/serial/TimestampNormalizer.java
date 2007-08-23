package org.openmrs.serial;

import java.io.Serializable;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class TimestampNormalizer extends Normalizer
{
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
            DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:S");
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

        java.sql.Date d;
        java.sql.Time t;
        long time;

        if (clazz.getName() == "java.util.Date") {
            //result = d.toString() + ' ' + t.toString();
            Date result = null;
            DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:S");
            try {
                result = dfm.parse(s);
            }
            catch(Exception e) {
                //TODO
            }
            return result;
        }
        
        return null;
    }
}
