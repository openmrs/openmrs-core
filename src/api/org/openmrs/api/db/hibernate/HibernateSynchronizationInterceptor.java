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
package org.openmrs.api.db.hibernate;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.EmptyInterceptor;
import org.hibernate.LazyInitializationException;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;
import org.openmrs.User;
import org.openmrs.api.SynchronizationService;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.DefaultNormalizer;
import org.openmrs.serialization.Item;
import org.openmrs.serialization.Normalizer;
import org.openmrs.serialization.Package;
import org.openmrs.serialization.Record;
import org.openmrs.serialization.TimestampNormalizer;
import org.openmrs.synchronization.SyncItemState;
import org.openmrs.synchronization.SyncRecordState;
import org.openmrs.synchronization.Synchronizable;
import org.openmrs.synchronization.SynchronizableInstance;
import org.openmrs.synchronization.engine.SyncItem;
import org.openmrs.synchronization.engine.SyncItemKey;
import org.openmrs.synchronization.engine.SyncRecord;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Implements 'change interception' for data synchronization feature using Hibernate interceptor mechanism. 
 * Intercepted changes are recorded into the synchronization journal table in DB.
 * TODO - add more content here.
 * 
 * @see org.hibernate.EmptyInterceptor
 */
public class HibernateSynchronizationInterceptor extends EmptyInterceptor implements ApplicationContextAware {

    /**
     * Helper container class to store type/value tuple for a given object property. Utilized during 
     * serializtion of intercepted entity changes.
     *
     * @see HibernateSynchronizationInterceptor#packageObject(Synchronizable, Object[], String[], Type[], SyncItemState)
     */
    protected class PropertyClassValue {
        String clazz, value;

        public String getClazz() {
            return clazz;
        }

        public String getValue() {
            return value;
        }

        public PropertyClassValue(String clazz, String value) {
            this.clazz = clazz;
            this.value = value;
        }
    }    

    /**
     * From Spring docs: There might be a single instance of Interceptor for a
     * SessionFactory, or a new instance might be specified for each Session.
     * Whichever approach is used, the interceptor must be serializable if the
     * Session is to be serializable. This means that SessionFactory-scoped
     * interceptors should implement readResolve().
     */
    private static final long serialVersionUID = -4905755656754047400L;

    protected final Log log = LogFactory.getLog(HibernateSynchronizationInterceptor.class);

    protected SynchronizationService synchronizationService = null;

    /* App context. This is needed to retrieve an instance of current Spring SessionFactory. There should be 
     * a better way to do this but we collectively couldn't find one.
    */
    private ApplicationContext context;
    
    static DefaultNormalizer defN = new DefaultNormalizer();
    static TimestampNormalizer tsN = new TimestampNormalizer();

    static final String sp = "_";
    
    //safetypes are *hibernate* types that we know how to serialize with help of Normalizers
    static final Map<String, Normalizer> safetypes;
    static {
        safetypes = new HashMap<String, Normalizer>();
        //safetypes.put("binary", defN);
        //blob
        safetypes.put("boolean", defN);
        //safetypes.put("big_integer", defN);
        //safetypes.put("big_decimal", defN);
        //safetypes.put("byte", defN);
        //celendar
        //calendar_date
        //character
        //clob
        //currency
        //date
        //dbtimestamp
        safetypes.put("double", defN);
        safetypes.put("float", defN);
        safetypes.put("integer", defN);
        //locale
        safetypes.put("long", defN);
        safetypes.put("short", defN);
        safetypes.put("string", defN);
        safetypes.put("text", defN);
        safetypes.put("timestamp", tsN);
        //time
        //timezone
    }
    
    private ThreadLocal<SyncRecord> syncRecordHolder = new ThreadLocal<SyncRecord>();
    private ThreadLocal<Boolean> deactivated = new ThreadLocal<Boolean>();
    private ThreadLocal<HashSet<Object>> pendingFlushHolder = new ThreadLocal<HashSet<Object>>();
    
    public HibernateSynchronizationInterceptor() {
    }

    /**
     * Deactivates synchronization. Will be reset on transaction completion or manually.
     */
    public void deactivateTransactionSerialization() {
        deactivated.set(true);
    }

    /**
     * Re-activates synchronization.
     */
    public void activateTransactionSerialization() {
        deactivated.remove();
    }

    /**
     * Intercepts the start of a transaction. A new SyncRecord is created for this transaction/
     * thread to keep track of changes done during the transaction. Kept ThreadLocal.
     * 
     * @see org.hibernate.EmptyInterceptor#afterTransactionBegin(org.hibernate.Transaction)
     */
    @Override
    public void afterTransactionBegin(Transaction tx) {
       if(log.isDebugEnabled())
            log.debug("afterTransactionBegin: " + tx + " deactivated: " + deactivated.get());
         
        if(syncRecordHolder.get() != null ) {
            log.warn("Replacing existing SyncRecord in SyncRecord holder");
        }
        
        syncRecordHolder.set(new SyncRecord());
    }

    /**
     * Intercepts right before a commit is done. Not called in case of a rollback pr. Hibernate
     * documentation. If synchronization is not disabled for this transaction/thread the
     * SyncRecord kept ThreadLocal will be saved to the database, if it contains changes (SyncItems).
     * 
     * @see org.hibernate.EmptyInterceptor#beforeTransactionCompletion(org.hibernate.Transaction)
     */
    @Override
    public void beforeTransactionCompletion(Transaction tx) {
        if(log.isDebugEnabled())
            log.debug("beforeTransactionCompletion: " + tx + " deactivated: " + deactivated.get());
        
        // If synchronization is NOT deactivated
        if (deactivated.get() == null) {
            SyncRecord record = syncRecordHolder.get();
            syncRecordHolder.remove();

            // Does this transaction contain any serialized changes?
            if (record.getItems() != null) {
            
                if(log.isDebugEnabled())
                    log.debug(record.getItems().size() + " SyncItems in SyncRecord, saving!");
                
                // Grab user if we have one, and use the GUID of the user as creator of this SyncRecord
                User user = Context.getAuthenticatedUser();
                if (user != null) {
                    record.setCreator(user.getGuid());
                }
                
                // Grab database version
                record.setDatabaseVersion(Context.getAdministrationService().getGlobalProperty("database_version"));
                
                // Complete the record
                record.setGuid(UUID.randomUUID().toString());
                if ( record.getOriginalGuid() == null ) {
                    log.warn("OriginalGuid is null, so assigning a new GUID");
                    record.setOriginalGuid(record.getGuid());
                } else {
                    log.warn("OriginalGuid is " + record.getOriginalGuid() + "!!!!");
                }
                record.setState(SyncRecordState.NEW);
                record.setTimestamp(new Date());
                record.setRetryCount(0);
    
                // Save SyncRecord
                if (synchronizationService == null) {
                    synchronizationService = Context.getSynchronizationService();
                }
    
                synchronizationService.createSyncRecord(record, record.getOriginalGuid());
            }
            else {
            	//note: this will happen all the time with read-only transactions
                if(log.isDebugEnabled())
                    log.debug("No SyncItems in SyncRecord, save discarded!");
            }
        }
    }
    
    /**
     * Intercepts after the transaction is completed, also called on rollback.
     * Clean up any remaining ThreadLocal objects/reset.
     * 
     * @see org.hibernate.EmptyInterceptor#afterTransactionCompletion(org.hibernate.Transaction)
     */
    @Override
    public void afterTransactionCompletion(Transaction tx) {
        if (log.isDebugEnabled())
            log.debug("afterTransactionCompletion: " + tx + " committed: " + tx.wasCommitted()+ " rolledback: " + tx.wasRolledBack() + " deactivated: " + deactivated.get());
        
        // clean out SyncRecord in case of rollback:
        syncRecordHolder.remove();
        
        // reactivate the interceptor
        deactivated.remove();
    }

    /**
     * Called before an object is saved. Triggers in our case for new objects
     * (inserts)
     * 
     * Packages up the changes and sets item state to NEW.
     * 
     * @return false if data is unmodified by this interceptor, true if
     *         modified. Adding GUIDs to new objects that lack them.
     * 
     * @see org.hibernate.EmptyInterceptor#onSave(java.lang.Object,
     *      java.io.Serializable, java.lang.Object[], java.lang.String[],
     *      org.hibernate.type.Type[])
     */
    @Override
    public boolean onSave(Object entity,
                          Serializable id,
                          Object[] state,
                          String[] propertyNames,
                          Type[] types) 
    {
	    if (log.isDebugEnabled())
    	    log.debug("onSave: " + state.toString());

        //first see if entity should be written to the journal at all
        if (!this.shouldSynchronize(entity)){
            if (log.isDebugEnabled())
                log.debug("Determined entity not to be journaled, exiting onSave.");        
            return false;
		}
        
        //create new flush holder if needed
        if (pendingFlushHolder.get() == null) 
            pendingFlushHolder.set(new HashSet<Object>());

        if (!pendingFlushHolder.get().contains(entity)) {
            pendingFlushHolder.get().add(entity);
            return packageObject((Synchronizable)entity, state, propertyNames, types, SyncItemState.NEW);
        }
        
        return false;
    }

    /**
     * Called before an object is updated in the database.
     * 
     * Packages up the changes and sets sync state to NEW for any objects we care about synchronizing.
     * 
     * @return false if data is unmodified by this interceptor, true if
     *         modified. Adding GUIDs to new objects that lack them.
     * 
     * @see org.hibernate.EmptyInterceptor#onFlushDirty(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
     */
    @Override
    public boolean onFlushDirty(Object entity,
                                Serializable id,
                                Object[] currentState,
                                Object[] previousState,
                                String[] propertyNames,
                                Type[] types) 
    {
	    if (log.isDebugEnabled())
    	    log.debug("onFlushDirty: " + entity.getClass().getName());

        //first see if entity should be written to the journal at all
        if (!this.shouldSynchronize(entity)){
            if (log.isDebugEnabled())
                log.debug("Determined entity not to be journaled, exiting onSave.");        
            return false;
		}

        /* NOTE: Accomodate Hibernate auto-flush semantics (as best as we understand them):
         * In case of sync ingest: When processing SyncRecord with >1 sync item via ProcessSyncRecord() on parent, 
         * calls to get object/update object by guid may cause auto-flush of pending updates; 
         * this would result in redundant sync items within a sync record. 
         * Use threadLocal HashSet to only keep one instance of dirty object for single
         * hibernate flush. Note that this is (i.e. incurring autoflush()) is not normally observed in rest of openmrs service
         * layer since most of the data change calls are encapsulated in single transactions.  
         */
        
        //create new holder if needed
        if (pendingFlushHolder.get() == null) 
            pendingFlushHolder.set(new HashSet<Object>());

        if (!pendingFlushHolder.get().contains(entity)) {
            pendingFlushHolder.get().add(entity);
            return packageObject((Synchronizable)entity, currentState, propertyNames, types, SyncItemState.UPDATED);
        }
        
        return false;
    }
    
    @Override
    public void postFlush(Iterator entities) {
        
        if(log.isDebugEnabled())
            log.debug("postFlush called.");
        
        //clear the holder
        pendingFlushHolder.remove();
    }
    
    /**
     * Intercept prepared stmts for logging purposes only.
     * NOTE: At this point, we are ignoring any prepared statements. This method gets called on
     * any prepared stmt; meaning selects also which makes handling this reliably difficult. Fundamentally,
     * short of replaying sql as is on parent, it is diffucult to imagine safe and complete implementation.
     * <p>
     * Preferred approach is to weed out all dynamic SQL from openMRS DB layer and if absolutely necessary, 
     * create a hook for DB layer code to explicitely specify what SQL should be passed to the parent during
     * synchronization. 
     * 
     * @see org.hibernate.EmptyInterceptor#onPrepareStatement(java.lang.String)
     */    
    @Override
    public String onPrepareStatement(String sql) {
        if(log.isDebugEnabled())
            log.debug("onPrepareStatement. sql: " + sql);

        return sql;
    }

    /**
     * Serializes and packages an intercepted change in object state.
     * <p>
     * IMPORTANT serialization notes:
     * <p>
     * Transient Properties. Transients are not serialized/journaled. Marking an object property as transient is the
     * supported way of designating it as something not to be recorded into the journal.
     * <p>
     * Hibernate Identity property. A property designated in Hibernate as identity (i.e. primary key) *is* not serialized.
     * This is because sync does not enforce global uniqueness of database primary keys. Instead, custom guid property is used.
     * This allows us to continue to use native types for 'traditional' entity relationships.   
     * <p>
     * Guid property.
     * 
     * @param entity The object changed.
     * @param currentState Array containing data for each field in the object as they will be saved.
     * @param propertyNames Array containing name for each field in the object, corresponding to currentState.
     * @param types Array containing Type of the field in the object, corresponding to currentState.
     * @param state SyncItemState, e.g. NEW, UPDATED, DELETED 
     * @return True if data was altered, false otherwise.
     */
    protected boolean packageObject(Synchronizable entity, Object[] currentState, String[] propertyNames, Type[] types, SyncItemState state) {
        boolean dataChanged = false;
                
        String objectGuid = null;
        String originalRecordGuid = null;
        Set<String> transientProps = null;
        
        //The container of values to be serialized:
        //Holds tuples of <property-name> -> {<property-type-name>, <property-value as string>}        
        HashMap<String, PropertyClassValue> values = new HashMap<String, PropertyClassValue> ();

        try {
            // Get the GUID of this object if it has one, used as SyncItemKey.
            objectGuid = entity.getGuid();
            originalRecordGuid = entity.getLastRecordGuid();
            if(log.isDebugEnabled())
                log.debug("In PackageObject, originalGuid is " + originalRecordGuid);

    		// Transient properties are not serialized.
            transientProps = new HashSet<String>();
            for ( Field f : entity.getClass().getDeclaredFields() ) {
            	if ( Modifier.isTransient(f.getModifiers()) ) {
            		transientProps.add(f.getName());
            		log.info("The field " + f.getName() + " is transient - so we won't serialize it");
            	}
            }

            /* Retrieve metadata for this type; we need to determine what is the PK field for this type.
             * We need to know this since PK values are *not* journaled; values of primary keys are assigned
             * where physical DB records are created. This is so to avoid issues with id collisions.
             */
            SessionFactory factory = (SessionFactory)this.context.getBean("sessionFactory");
            ClassMetadata data = factory.getClassMetadata(entity.getClass());
            String idProperty = data.getIdentifierPropertyName();
           if (log.isInfoEnabled())
                log.info("Id for this class: " + idProperty);
            
            /*
             * Loop through all the properties/values and put in a hash for duplicate removal
             */
            for (int i = 0; i < types.length; i++) {
                String typeName = types[i].getName();                
                if (log.isDebugEnabled())
                    log.debug("Processing, type: " + typeName + " Field: " + propertyNames[i]);
                /*
                 * If this field is a String GUID, and it's null or "", we need to assign a new GUID before 
                 * processing it. Note: dataChanged is also set to true to let Hibernate know we are changing
                 * a record right underneath of it. 
                 */
                if (typeName.equals("string") 
                		&& propertyNames[i].equals("guid") 
                		&& (currentState[i] == null || currentState[i].equals(""))) {
                    objectGuid = UUID.randomUUID().toString();
                    currentState[i] = objectGuid;
                    dataChanged = true;
                    if (log.isInfoEnabled())
                        log.info("Issued randomly generated GUID " + currentState[i] + " to Type: " + typeName + " Field: " + propertyNames[i]);
                }

                if (currentState[i] != null) {
                    // is this the primary key or transient? if so, we don't want to serialize
                	if ( propertyNames[i].equals(idProperty) 
                			|| ("personId".equals(idProperty) && "patientId".equals(propertyNames[i])) 
                			|| ("personId".equals(idProperty) && "userId".equals(propertyNames[i]))
                			|| transientProps.contains(propertyNames[i])
                			) {
                        if (log.isInfoEnabled())
                            log.info("Skipping property (" + propertyNames[i] + ") because it's either the primary key or it's transient.");

                	} else {

                        Normalizer n;
                        if ((n = safetypes.get(typeName)) != null) {
	                        // Handle safe types like boolean/String/integer/timestamp via Normalizers
                            values.put(propertyNames[i], new PropertyClassValue(typeName, n.toString(currentState[i])));
                        }
                        
                        /*
                         * Not a safe type, check if the object implements the Synchronizable interface
                         */
                        else if (currentState[i] instanceof Synchronizable) {
                            Synchronizable childObject = (Synchronizable)currentState[i];
                            // child objects are not always loaded if not needed, so let's surround this with try/catch, package only if need to
                            String childGuid = null;
                            try {
    	                        childGuid = childObject.getGuid();
                            } catch (LazyInitializationException e) {
    	                        log.warn("Attempted to package/serialize child object, but child object was not yet initialized (and thus was null)");
    	                        if ( types[i].getReturnedClass().equals(User.class) ) {
    	                        	log.warn("SUBSTITUTED AUTHENTICATED USER FOR ACTUAL USER");
    	                        	childGuid = Context.getAuthenticatedUser().getGuid();
    	                        } else {
    	                        	log.warn("COULD NOT SUBSTITUTE AUTHENTICATED USER FOR ACTUAL USER");
    	                        }
                            } catch (Exception e) {
                            	//TODO - throw exception here??!
                            	log.error("Could not find child object - object is null, therefore guid is null");
                            }
                            
                            if (childGuid != null) {
                                values.put(propertyNames[i], new PropertyClassValue(typeName, childGuid));
                            } else {
                                log.warn("Type: " + typeName + " Field: " + propertyNames[i] + " is null");
                            }
                        } else {
                        	//TODO - throw exception here??!
                            log.error("Type: " + typeName + " Field: " + propertyNames[i] + " is not safe and has no GUID, skipped!");
                        }
                	}
                } else {
                	if(log.isDebugEnabled())
                    	log.debug("Type: " + typeName + " Field: " + propertyNames[i] + " is null, skipped");
                }
            }


            /*
             * Now serialize the data identified and put in the value-map
             */
            // Setup the serialization data structures to hold the state            
            Package pkg = new Package();
            String className = entity.getClass().getName();
            Record xml = pkg.createRecordForWrite(className);
            Item entityItem = xml.getRootItem();

			//loop throgh the map of the properties that need to be serialized
			for ( Map.Entry<String, PropertyClassValue> me : values.entrySet() ) {
                String property = me.getKey();
                
                if (log.isDebugEnabled())
	                log.debug("About to grab value for: " + property);
                
                try {
                    PropertyClassValue pcv = me.getValue();
                    appendAttribute(xml, entityItem, property, pcv.getClazz(), pcv.getValue());
                } catch (Exception e) {
                	//TODO - throw?
                	log.error("Error while appending attribute", e);
                }
            }
            
            values.clear(); // Be nice to GC

            /*
             * Create SyncItem and store change in SyncRecord kept in ThreadLocal.
             */
            SyncItem syncItem = new SyncItem();
            syncItem.setKey(new SyncItemKey<String>(objectGuid, String.class)); 
            syncItem.setState(state);
            syncItem.setContent(xml.toStringAsDocumentFragement());
            
            if (log.isWarnEnabled()) {
                log.warn("Adding SyncItem to SyncRecord");
            }
            syncRecordHolder.get().addItem(syncItem);
            syncRecordHolder.get().addContainedClass(entity.getClass().getSimpleName());
            syncRecordHolder.get().setOriginalGuid(originalRecordGuid);
        } catch (Exception e) {
        	//TODO: throw here for sure!
            log.error("Journal error\n", e);
        }
        
        return dataChanged;
    }
    
    /**
     * Auto generated method comment
     * 
     * @param xml
     * @param parent
     * @param attribute
     * @param classname
     * @param data
     * @throws Exception
     */    
	protected void appendAttribute(Record xml, Item parent, String attribute, String classname, String data) throws Exception {
        //if (data != null && data.length() > 0) {
    	// this will break if we don't allow data.length==0 - some string values are required NOT NULL, but can be blank
    	if (data != null) {
            Item item = xml.createItem(parent, attribute);
            item.setAttribute("type", classname);
            xml.createText(item, data);
        }
    }
    
    /**
     * Determines if entity is to be 'synchronized'. There are three ways this can happen:
     * <p>
     * 1. Entity implements Synchronizable interface.
     * <p>
     * 2. Entity implements SynchronizableInstance and IsSynchronizable is set to true
     * <p>
     * 3. Finally, interceptor supports manual override to suspend synchronization by setting the deactivated bit 
     * (see {@link #deactivateTransactionSerialization()}).
     * This option is provided only for rare occasions when previous methods are not sufficient (i.e 
     * suspending interception in case of inline sql).
     *  
     * @param entity Object to examine.
     * @return true if entity should be synchronized, else false.
     * @see org.openmrs.synchronization.Synchronizable
     * @see org.openmrs.synchronization.SynchronizableInstance
     */
    protected boolean shouldSynchronize(Object entity) {
        
        //Synchronizable *only*.
        if (!(entity instanceof Synchronizable)) {
            if (log.isDebugEnabled())
                log.debug("Do nothing. Flush with type that does not implement Synchronizable, type is:" + entity.getClass().getName());            
            return false;
        } 
        
        //if it implements SynchronizableInstance, make sure it is set to synchronize
        if (entity instanceof SynchronizableInstance) {
            if (!((SynchronizableInstance)entity).getIsSynchronizable()) {
                if (log.isDebugEnabled())
                    log.debug("Do nothing. Flush with SynchronizableInstance set to false, type is:" + entity.getClass().getName());            
                return false;
            }
        }
     
        //finally, if 'deactivated' bit was set manually, return accordingly
        if (deactivated.get() == null)
            return true;
        else
            return false;
    }

	/**
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext context) throws BeansException {
    	this.context = context;
    }
}

