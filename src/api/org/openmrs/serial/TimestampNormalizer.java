package org.openmrs.serial;

import java.io.Serializable;

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
            time = ((java.util.Date)o).getTime();
            d = new java.sql.Date(time);
            t = new java.sql.Time(time);
            result = d.toString() + ' ' + t.toString();
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
    public void fromString(Object o, String s) {}
}
