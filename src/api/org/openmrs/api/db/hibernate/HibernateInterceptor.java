package org.openmrs.api.db.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.type.Type;

import org.openmrs.serial.*;

// these private classes will be repackaged; this packing is temporary!
abstract class normalizer
{
    protected final Log log = LogFactory.getLog(normalizer.class);

    public abstract String toString(Object o);
    public abstract void fromString(Object o, String s);
}
class defaultNormalizer extends normalizer
{
    public String toString(Object o) {return o.toString();}
    public void fromString(Object o, String s) {}
}
class timestampNormalizer extends normalizer
{
    public String toString(Object o)
    {
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

class propertyClassValue 
{
    String clazz, value;

    public String getClazz(){return clazz;}
    public String getValue(){return value;}

    public propertyClassValue(String clazz, String value)
    {
        this.clazz=clazz;
        this.value=value;
    }
}

public class HibernateInterceptor extends EmptyInterceptor 
{
    public static final long serialVersionUID = 0L;
    protected final Log log = LogFactory.getLog(HibernateInterceptor.class);

    static defaultNormalizer defN = new defaultNormalizer();
    static timestampNormalizer tsN = new timestampNormalizer();

    static final String sp = "_";
    static final java.util.HashMap <String,normalizer> safetypes;
    static {
        safetypes = new java.util.HashMap<String,normalizer>();
        safetypes.put("string", defN);
        safetypes.put("timestamp", tsN);
        safetypes.put("boolean", defN);
        safetypes.put("integer", defN);
    }

	private SessionFactory sessionFactory;

    public HibernateInterceptor(){}

    protected void setSessionFactory(SessionFactory sessionFactory)
    {
		this.sessionFactory = sessionFactory;
    }
    
    public void afterTransactionBegin(Transaction tx) 
    {
        log.debug("afterTransactionBegin: " + tx.toString());
    }    

    public void afterTransactionCompletion(Transaction tx) 
    {
        log.debug("afterTransactionCompletion: " + tx.toString());
    }

    public boolean onSave(Object entity,
                          Serializable id,
                          Object[] state,
                          String[] propertyNames,
                          Type[] types) 
    {
        log.debug("onSave: " + state.toString());
        return true;
    }

    public void onDelete(Object entity,
            Serializable id,
            Object[] state,
            String[] propertyNames,
            Type[] types) 
    {
    } 

    public boolean onFlushDirty(Object entity,
                                Serializable id,
                                Object[] currentState,
                                Object[] previousState,
                                String[] propertyNames,
                                Type[] types) 
    {
        
        log.debug("onFlushDirty: " + entity.getClass().getName());

        packageObject(entity, currentState, propertyNames, types);

        return true;
    }

    public void postFlush(java.util.Iterator entities)
    {
        while (false && entities.hasNext())
        {
            Object entity = entities.next();
            log.debug("postFlush: " + entity.getClass().getName());
        }
    }

    private String packageObject(Object entity, Object[] currentState,
                               String[] propertyNames, Type[] types)
    {
        HashMap <String,propertyClassValue> values = 
            new HashMap <String,propertyClassValue> ();

        try {
            // testing name generator
            java.util.GregorianCalendar gc = new java.util.GregorianCalendar();
            String fname = gc.get(gc.YEAR) + sp + gc.get(gc.MONTH) + sp + gc.get(gc.DATE)
                + sp + gc.get(gc.HOUR) + sp + gc.get(gc.MINUTE) + sp 
                + gc.get(gc.MILLISECOND);
            
            // use Package when you don't want files on disk
            //Package pkg = new Package();
            FilePackage pkg = new FilePackage();
            Record xml = pkg.createRecordForWrite(entity.getClass().getName());
            Item entityItem = xml.getRootItem();
          
            // properties/values put in a hash for dupe removeal
            for (int i = 0; i < types.length; i++)
            {
                String tName = types[i].getName();
                Object tObj = currentState[i];

                log.debug("Field " + propertyNames[i] + " type " + tName);

                if (tObj!=null)
                {
                    normalizer n;
                    if ((n=safetypes.get(tName)) !=null)
                    {
                        values.put(propertyNames[i], 
                                   new propertyClassValue(tName, n.toString(tObj)));
                    }
                    // maybe has guid
                    else if (tName.indexOf("org.openmrs") > -1)
                    {
                        values.put(propertyNames[i], new propertyClassValue(tName, getGuid(tObj)));
                    }
                    else
                    {
                        log.warn(tName + " is not safe and has no guid!!!!\n");
                    }
                }
                else
                {
                    log.warn(tName + " is null\n");
                }
            }

            // serialize from hashmap
            Iterator its = values.entrySet().iterator();
            while(its.hasNext())
            {
                Map.Entry <String,propertyClassValue> me = 
                    (Map.Entry <String,propertyClassValue>) its.next();
                String property = me.getKey();
                propertyClassValue pcv = me.getValue();

                appendAttribute(xml, entityItem, property, pcv.getClazz(), pcv.getValue());
            }

            // look up, see how this was created
            if (pkg instanceof FilePackage) {
                pkg.savePackage(org.openmrs.util.OpenmrsUtil.getApplicationDataDirectory() 
                               + "/journal/" + fname);
            }


            //nice to gc
            values.clear();

            // why?
            return pkg.toString();
        }
        catch (Exception e) {
            log.error("Journal error\n");
            e.printStackTrace();
            return null;
        }
    }
        
    private void appendAttribute(Record xml, Item parent, String attribute, String classname,
                                 String data) throws Exception
    {
        if (data!=null && data.length()>0)
        {
            Item item = xml.createItem(parent, attribute);
            item.setAttribute("type", classname);
            xml.createText(item, data);
        }
    }
                
    private String getGuid(Object obj)
    {
        String mname = "getGuid";
        Class[] types = new Class[] {};

        Method method;
        try {
            method = obj.getClass().getMethod(mname, types);
            String result = (String)method.invoke(obj, new Object[0]);            
            if (result!=null && result.length() < 1)
            { 
                result = null;
            }
            return result;
        }
        catch (Exception e) {
            //log.warn("No method/error on '" + mname + "' in " + obj.getClass().getName());
            return null;
        }
    }

}

