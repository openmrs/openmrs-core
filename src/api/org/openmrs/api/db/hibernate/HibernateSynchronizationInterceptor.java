package org.openmrs.api.db.hibernate;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;
import org.hibernate.type.Type;
import org.openmrs.api.SynchronizationService;
import org.openmrs.api.context.Context;
import org.openmrs.serial.FilePackage;
import org.openmrs.serial.Item;
import org.openmrs.serial.Record;
import org.openmrs.synchronization.engine.SyncItem;
import org.openmrs.synchronization.engine.SyncItemKey;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.engine.SyncRecordState;
import org.openmrs.synchronization.engine.SyncItem.SyncItemState;
import org.openmrs.util.OpenmrsUtil;

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

public class HibernateSynchronizationInterceptor extends EmptyInterceptor 
{
    /**
     * From Spring docs: There might be a single instance of Interceptor for a
     * SessionFactory, or a new instance might be specified for each Session.
     * Whichever approach is used, the interceptor must be serializable if the
     * Session is to be serializable. This means that SessionFactory-scoped
     * interceptors should implement readResolve().
     */
    public static final long serialVersionUID = 0L;
    protected final Log log = LogFactory.getLog(HibernateSynchronizationInterceptor.class);

    protected SynchronizationService synchronizationService = null;
    
    static defaultNormalizer defN = new defaultNormalizer();
    static timestampNormalizer tsN = new timestampNormalizer();

    static final String sp = "_";
    static final Map<String,normalizer> safetypes;
    static {
        safetypes = new HashMap<String,normalizer>();
        safetypes.put("string", defN);
        safetypes.put("timestamp", tsN);
        safetypes.put("boolean", defN);
        safetypes.put("integer", defN);
    }

    public HibernateSynchronizationInterceptor(){}

    public void afterTransactionBegin(Transaction tx) 
    {
        log.debug("afterTransactionBegin: " + tx);
    }    

    public void afterTransactionCompletion(Transaction tx) 
    {
        log.debug("afterTransactionCompletion: " + tx);
    }

    public boolean onSave(Object entity,
                          Serializable id,
                          Object[] state,
                          String[] propertyNames,
                          Type[] types) 
    {
        log.debug("onSave: " + state.toString());
        
        packageObject(entity, state, propertyNames, types, SyncItemState.NEW);
        
        return false;
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

        packageObject(entity, currentState, propertyNames, types, SyncItemState.UPDATED);

        return false;
    }

    @SuppressWarnings("unchecked")
    public void postFlush(Iterator entities)
    {
        while (false && entities.hasNext())
        {
            Object entity = entities.next();
            log.debug("postFlush: " + entity.getClass().getName());
        }
    }

    private void packageObject(Object entity, Object[] currentState,
                               String[] propertyNames, Type[] types, SyncItemState state)
    {
        HashMap <String, propertyClassValue> values = 
            new HashMap <String, propertyClassValue> ();

        try {
            // use Package when you don't want files on disk
            //org.openmrs.serial.Package pkg = new org.openmrs.serial.Package();
            org.openmrs.serial.Package pkg = new FilePackage();
            Record xml = pkg.createRecordForWrite(entity.getClass().getName());
            Item entityItem = xml.getRootItem();
          
            // properties/values put in a hash for dupe removeal
            for (int i = 0; i < types.length; i++)
            {
                String typeName = types[i].getName();
                Object object = currentState[i];

                log.debug("Type: " + typeName + " Field: " + propertyNames[i]);

                if (object!=null)
                {
                    normalizer n;
                    if ((n=safetypes.get(typeName)) != null)
                    {
                        values.put(propertyNames[i], new propertyClassValue(typeName, n.toString(object)));
                    }
                    // maybe has guid
                    else if (typeName.indexOf("org.openmrs") > -1)
                    {
                        values.put(propertyNames[i], new propertyClassValue(typeName, getGuid(object)));
                    }
                    else
                    {
                        log.warn("Type: " + typeName  + " Field: " + propertyNames[i] + " is not safe and has no GUID!");
                    }
                }
                else
                {
                    log.warn("Type: " + typeName  + " Field: " + propertyNames[i] + " is null");
                }
            }

            // serialize from hashmap
            Iterator<Map.Entry<String, propertyClassValue>> its = values.entrySet().iterator();
            while(its.hasNext())
            {
                Map.Entry<String, propertyClassValue> me = its.next();
                String property = me.getKey();
                propertyClassValue pcv = me.getValue();

                appendAttribute(xml, entityItem, property, pcv.getClazz(), pcv.getValue());
            }

            // look up, see how this was created
            if (pkg instanceof FilePackage) {
                // testing name generator
                Calendar calendar = new GregorianCalendar();
                String filename = calendar.get(Calendar.YEAR) + sp
                        + calendar.get(Calendar.MONTH) + sp
                        + calendar.get(Calendar.DATE) + sp
                        + calendar.get(Calendar.HOUR) + sp
                        + calendar.get(Calendar.MINUTE) + sp
                        + calendar.get(Calendar.MILLISECOND);
                
                pkg.savePackage(OpenmrsUtil.getApplicationDataDirectory() + "/journal/" + filename);
            }

            //nice to gc
            values.clear();

            // Store change in sync table:
            SyncItem syncItem = new SyncItem();
            //FIXME: need to fetch GUID from object, just for testing.
            syncItem.setKey(new SyncItemKey<String>(UUID.randomUUID().toString())); 
            syncItem.setState(state);
            //syncItem.setContent(xml.toString());
            syncItem.setContent(xml.toStringAsDocumentFragement());
            
            List<SyncItem> items = new ArrayList<SyncItem>();
            items.add(syncItem);
            
            SyncRecord record = new SyncRecord();
            record.setGuid(UUID.randomUUID().toString());
            record.setState(SyncRecordState.NEW);
            record.setTimestamp(new Date());
            record.setRetryCount(0);
            record.setItems(items);
            
            // Save SyncRecord
            if (synchronizationService == null) {
                synchronizationService = Context.getSynchronizationService();
            }
            synchronizationService.createSyncRecord(record);
        }
        catch (Exception e) {
            log.error("Journal error\n", e);
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

    private String getGuid(Object object)
    {
        String methodName = "getGuid";

        Method method;
        try {
            method = object.getClass().getMethod(methodName, new Class[] {});
            
            String result = (String)method.invoke(object, new Object[0]);            
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

