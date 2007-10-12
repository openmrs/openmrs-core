/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.serialization;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimestampNormalizer extends Normalizer
{
    public static final String DATETIME_MASK = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final String DATETIME_MASK_BACKUP = "yyyy-MM-dd HH:mm:ss.S";  // because we are converting to strings in more than one way, so need a way to convert back
	public static final String DATETIME_DISPLAY_FORMAT = "dd-MMM-yyyy HH:mm:ss";
    
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
