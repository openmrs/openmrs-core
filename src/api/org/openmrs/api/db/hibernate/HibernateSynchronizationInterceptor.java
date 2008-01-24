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
import java.lang.reflect.Method;
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
import org.hibernate.CallbackException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.Transaction;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.Type;
import org.hibernate.collection.PersistentSet;
import org.hibernate.proxy.HibernateProxy;
import org.openmrs.User;
import org.openmrs.api.SynchronizationService;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.DefaultNormalizer;
import org.openmrs.serialization.Item;
import org.openmrs.serialization.Normalizer;
import org.openmrs.serialization.Package;
import org.openmrs.serialization.Record;
import org.openmrs.serialization.TimestampNormalizer;
import org.openmrs.synchronization.SyncStatusState;
import org.openmrs.synchronization.SyncItemState;
import org.openmrs.synchronization.SyncRecordState;
import org.openmrs.synchronization.SyncUtil;
import org.openmrs.synchronization.Synchronizable;
import org.openmrs.synchronization.SynchronizableInstance;
import org.openmrs.synchronization.engine.SyncItem;
import org.openmrs.synchronization.engine.SyncItemKey;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.SyncException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Implements 'change interception' for data synchronization feature using Hibernate interceptor mechanism. 
 * Intercepted changes are recorded into the synchronization journal table in DB.
 * <p>
 * For detailed technical discussion see feature technical documentation on openmrs.org.
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

    /* 
     * App context. This is needed to retrieve an instance of current Spring SessionFactory. There should be 
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

       //explicitely bailout if sync is disabled
       if (SyncUtil.getSyncStatus() == SyncStatusState.DISABLED) return;

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
        
        try {
	        //explicitely bailout if sync is disabled
	        if (SyncUtil.getSyncStatus() == SyncStatusState.DISABLED) return;
	        
	        // If synchronization is NOT deactivated
	        if (deactivated.get() == null) {
	            SyncRecord record = syncRecordHolder.get();
	            syncRecordHolder.remove();
	
	            // Does this transaction contain any serialized changes?
	            if (record.hasItems()) {
	            	
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
	                	if(log.isInfoEnabled())
	                		log.info("OriginalGuid is null, so assigning a new GUID");
	                    record.setOriginalGuid(record.getGuid());
	                } else {
	                	if(log.isInfoEnabled())
	                		log.info("OriginalGuid is: " + record.getOriginalGuid());
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
	                    log.debug("No SyncItems in SyncRecord, save discarded (note: maybe a read-only transaction)!");
	            }
	        }
        } catch(Exception ex) {
            log.error("Journal error\n", ex);
            if (SyncUtil.getSyncStatus() == SyncStatusState.ENABLED_STRICT)
                throw(new SyncException("Error in interceptor, see log messages and callstack.", ex));        	
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

        //explicitely bailout if sync is disabled
        if (SyncUtil.getSyncStatus() == SyncStatusState.DISABLED) return;
        
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
        
        //explicitely bailout if sync is disabled
        if (SyncUtil.getSyncStatus() == SyncStatusState.DISABLED) return false;

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
            return packageObject((Synchronizable)entity, state, propertyNames, types, id, SyncItemState.NEW);
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

        //explicitely bailout if sync is disabled
        if (SyncUtil.getSyncStatus() == SyncStatusState.DISABLED) return false;
        
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
            return packageObject((Synchronizable)entity, currentState, propertyNames, types, id, SyncItemState.UPDATED);
        }
        else {
        	//TODO: replace entity in the record?
        	if (log.isDebugEnabled()) {
        		log.debug("replacing sync item in the sync record for entity: " + entity.getClass().getName());
        	}
        	
        }
        
        return false;
    }
    
    @Override
    public void postFlush(Iterator entities) {
        
        if(log.isDebugEnabled())
            log.debug("postFlush called.");

        //explicitely bailout if sync is disabled
        if (SyncUtil.getSyncStatus() == SyncStatusState.DISABLED) return;

        //clear the holder
        pendingFlushHolder.remove();
    }
    
    /**
     * Intercept prepared stmts for logging purposes only.
     * <p>
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

        //explicitely bailout if sync is disabled
        if (SyncUtil.getSyncStatus() == SyncStatusState.DISABLED) return sql;
        
        return sql;
    }
   
   
    /**
     *  Handles collection remove event. As can be seen in org.hibernate.engine.Collections, 
     *  hibernate only calls remove when it is about to recreate a collection.
     *   
     * @see org.hibernate.engine.Collections.prepareCollectionForUpdate
     * @see org.openmrs.api.impl.SynchronizationIngestServiceImpl
     */
    @Override
	public void onCollectionRemove(Object collection, Serializable key) throws CallbackException {
    	if (log.isDebugEnabled()) {
    		log.debug("COLLECTION remove with key: " + key);
    	}
    	
        //explicitely bailout if sync is disabled
        if (SyncUtil.getSyncStatus() == SyncStatusState.DISABLED) return;
    	
    	//this.processPersistentSet((PersistentSet)collection,key, "remove");
    }
    
    /**
     *  Handles collection recreate. Recreate is triggered by hibernate when collection object is replaced by new/different instance. 
     *  <p>remarks: See hibernate AbstractFlushingEventListener and org.hibernate.engine.Collections implementation to understand how
     *  collection updates are hooked up in hibernate, specifically see Collections.prepareCollectionForUpdate().
     * 
     * @see org.hibernate.engine.Collections
     * @see org.hibernate.event.def.AbstractFlushingEventListener
     */
    @Override
	public void onCollectionRecreate(Object collection, Serializable key) throws CallbackException {
    	if (log.isDebugEnabled()) {
    		log.debug("COLLECTION recreate with key: " + key);
    	}

        //explicitely bailout if sync is disabled
        if (SyncUtil.getSyncStatus() == SyncStatusState.DISABLED) return;

    	if (!(collection instanceof org.hibernate.collection.PersistentSet)) {
    		log.info("Cannot process collection that is not instance of PersistentSet, collection type was:" + 
    		          collection.getClass().getName());
    		return;
    	}
    	
    	this.processPersistentSet((PersistentSet)collection,key, "recreate");
    	
    }

    /**
     *  Handles updates of a collection (i.e. added/removed entries).
     *  <p>remarks: See hibernate AbstractFlushingEventListener implementation to understand how
     *  collection updates are hooked up in hibernate. 
     * 
     * @see org.hibernate.engine.Collections
     * @see org.hibernate.event.def.AbstractFlushingEventListener
     */
    @Override
	public void onCollectionUpdate(Object collection, Serializable key) throws CallbackException {
    	if (log.isDebugEnabled()) {
    		log.debug("COLLECTION update with key: " + key);
    	}

        //explicitely bailout if sync is disabled
        if (SyncUtil.getSyncStatus() == SyncStatusState.DISABLED) return;
    	
    	if (!(collection instanceof org.hibernate.collection.PersistentSet)) {
    		log.info("Cannot process collection that is not instance of PersistentSet, collection type was:" + 
    		          collection.getClass().getName());
    		return;
    	}
    	
    	this.processPersistentSet((PersistentSet)collection,key, "update");    	    	    		
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
     * @param id Value of the identifier for this entity 
     * @return True if data was altered, false otherwise.
     */
    protected boolean packageObject(Synchronizable entity, Object[] currentState, String[] propertyNames, Type[] types, Serializable id, SyncItemState state)
        throws SyncException {
        boolean dataChanged = false;
                
        String objectGuid = null;
        String originalRecordGuid = null;
        Set<String> transientProps = null;
        String infoMsg = null;
        SessionFactory factory = null;
        
        ClassMetadata data = null;
        String idPropertyName = null;
        org.hibernate.tuple.IdentifierProperty idPropertyObj = null;
        
        //The container of values to be serialized:
        //Holds tuples of <property-name> -> {<property-type-name>, <property-value as string>}        
        HashMap<String, PropertyClassValue> values = new HashMap<String, PropertyClassValue> ();

        try {
        	
            /* Get the GUID of this object if it has one, used as SyncItemKey.
             * if this is save event that is 'local' (i.e. getLastRecordGuid not yet assigned)
             * *and* guid is already assigned, clear it out and get a new one:
             * this can happen is someone does object copy and has incorrect constructor or if object
             * is disconnected from session and then saved anew; as in obs edit
             */  
        	if (state == SyncItemState.NEW && entity.getGuid() != null && entity.getLastRecordGuid() == null) {
        		entity.setGuid(null);
        	} else {
        		objectGuid = entity.getGuid();
        	}
        	
        	//pull-out sync-network wide change id, if one was already assigned (i.e. this change is coming from some other server)
        	originalRecordGuid = entity.getLastRecordGuid();
            
            //build up a starting msg for all logging
            StringBuilder sb = new StringBuilder();
            sb.append("In PackageObject, entity type:"); sb.append(entity.getClass().getName());
            sb.append(", entity guid:"); sb.append(objectGuid);
            sb.append(", originalGuid guid:"); sb.append(originalRecordGuid);
            infoMsg = sb.toString();
            
        	if (log.isInfoEnabled())
        		log.info(infoMsg);
        	
    		// Transient properties are not serialized.
            transientProps = new HashSet<String>();
            for ( Field f : entity.getClass().getDeclaredFields() ) {
            	if ( Modifier.isTransient(f.getModifiers()) ) {
            		transientProps.add(f.getName());
            		if (log.isInfoEnabled())
            			log.info("The field " + f.getName() + " is transient - so we won't serialize it");
            	}
            }

            /* Retrieve metadata for this type; we need to determine what is the PK field for this type.
             * We need to know this since PK values are *not* journaled; values of primary keys are assigned
             * where physical DB records are created. This is so to avoid issues with id collisions.
             * 
             * In case of <generator class="assigned" />, the Identifier property is already assigned value and needs to be 
             * journalled. Also, the prop wil *not* be part of currentState,thus we need to pull it out with reflection/metadata.
             */
            factory = (SessionFactory)this.context.getBean("sessionFactory");
            data = factory.getClassMetadata(entity.getClass());
            if (data.hasIdentifierProperty()) {
            	idPropertyName = data.getIdentifierPropertyName();
            	idPropertyObj = ((org.hibernate.persister.entity.AbstractEntityPersister)data).getEntityMetamodel().getIdentifierProperty();
            	if (idPropertyObj.getIdentifierGenerator() != null && idPropertyObj.getIdentifierGenerator() instanceof org.hibernate.id.Assigned) {
            		// serialize value as string
                    values.put(idPropertyName, new PropertyClassValue(id.getClass().getName(), id.toString()));
            	}
            }
                        
            /*
             * Loop through all the properties/values and put in a hash for duplicate removal
             */
            for (int i = 0; i < types.length; i++) {
                String typeName = types[i].getName();                
                if (log.isDebugEnabled())
                    log.debug("Processing, type: " + typeName + " Field: " + propertyNames[i]);

                if (propertyNames[i].equals(idPropertyName) && log.isInfoEnabled())
                    log.info(infoMsg + ", Id for this class: " + idPropertyName + " , value:" + currentState[i]);

                /*
                 * If:
                 * a) this field is a String GUID, and 
                 * b) objectGuid is null or "",
                 * we need to assign a new GUID before processing it. 
                 * Note: dataChanged is also set to true to let Hibernate know we are changing
                 * a record right underneath of it. 
                 */
                if (typeName.equals("string") && propertyNames[i].equals("guid") 
                		&& (objectGuid == null || objectGuid.equals(""))
                	) {
                    objectGuid = UUID.randomUUID().toString();
                    currentState[i] = objectGuid;
                    dataChanged = true;
                    infoMsg = infoMsg + ", newly assigned guid:" + sb.append(currentState[i]);
                    if (log.isInfoEnabled())
                        log.info("Issuing new GUID: " + currentState[i]);
                }

                if (currentState[i] != null) {
                    // is this the primary key or transient? if so, we don't want to serialize
                	if ( propertyNames[i].equals(idPropertyName) 
                			|| ("personId".equals(idPropertyName) && "patientId".equals(propertyNames[i])) 
                			|| ("personId".equals(idPropertyName) && "userId".equals(propertyNames[i]))
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
                                if(log.isWarnEnabled())
                                    log.warn("Attempted to package/serialize child object, but child object was not yet initialized (and thus was null)");
    	                        if ( types[i].getReturnedClass().equals(User.class) ) {
                                    //IS THIS RELIABLE??!?
    	                        	log.warn("SUBSTITUTED AUTHENTICATED USER FOR ACTUAL USER");
    	                        	childGuid = Context.getAuthenticatedUser().getGuid();
    	                        } else {
                                    //TODO: abort here also?
    	                        	log.error("COULD NOT SUBSTITUTE AUTHENTICATED USER FOR ACTUAL USER");
    	                        }
                            } catch (Exception e) {
                                log.error(infoMsg + ", Could not find child object - object is null, therefore guid is null");
                            	if (SyncUtil.getSyncStatus() == SyncStatusState.ENABLED_STRICT)
                                    throw(e);
                            }
                            
                            /* child object is Synchronizable but its guid is null, final attempt: load via PK if PK value available
                             * common scenario: this can happen when people are saving object graphs that are (at least partially) 
                             * manually constructed (i.e. setting concept on obs just by filling in conceptid
                             * without first fetching the full concept state from DB for perf. reasons */
                            if (childGuid == null) {
                                childGuid = fetchGuid(childObject);
                                if(log.isDebugEnabled()) {
                                    log.debug(infoMsg + "Field was null, attempted to fetch guid with the following results");
                                    log.debug("Field type:" + childObject.getClass().getName() + ",guid:" + childGuid);
                                }
                            }
                            
                            if (childGuid != null) {
                                values.put(propertyNames[i], new PropertyClassValue(typeName, childGuid));
                            } else {
                                String msg = infoMsg + ", Field value should be synchronized, but guid is null.  Field Type: " + typeName + " Field Name: " + propertyNames[i];
                                log.error(msg);
                                if (SyncUtil.getSyncStatus() == SyncStatusState.ENABLED_STRICT)
                                    throw(new SyncException(msg));
                            }
                        } else {
                            //state != null but it is not safetype or implements Synchronizable: do not package and log as info
                            if(log.isInfoEnabled())
                                log.info(infoMsg + ", Field Type: " + typeName + " Field Name: " + propertyNames[i] + " is not safe or Synchronizable, skipped!");
                        }
                	}
                } else {
                    //current state null -- skip
                	if(log.isDebugEnabled())
                    	log.debug("Field Type: " + typeName + " Field Name: " + propertyNames[i] + " is null, skipped");
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
                    appendRecord(xml, entityItem, property, pcv.getClazz(), pcv.getValue());
                } catch (Exception e) {
                    String msg = "Could not append attribute. Error while processing property: " + property;
                    log.error(msg,e);
                    if (SyncUtil.getSyncStatus() == SyncStatusState.ENABLED_STRICT)
                        throw(new SyncException(msg));
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
            
            if (log.isDebugEnabled())
                log.debug("Adding SyncItem to SyncRecord");
            
            syncRecordHolder.get().addItem(syncItem);
            syncRecordHolder.get().addContainedClass(entity.getClass().getSimpleName());
            
            //set the originating guid for the record: do this once per Tx; else we may end up with empty
            //string (i.e. depending on exact sequence of auto-flush, it may be that onFlushDirty is called last time
            //before a call to Synchornizable.setLastRecordGuid() is made
            if (syncRecordHolder.get().getOriginalGuid() == null || "".equals(syncRecordHolder.get().getOriginalGuid())) { 
            	syncRecordHolder.get().setOriginalGuid(originalRecordGuid);
            }
        } catch (SyncException ex) {
            log.error("Journal error\n", ex);
            if (SyncUtil.getSyncStatus() == SyncStatusState.ENABLED_STRICT)
                throw(ex);
        }
        catch (Exception e) {
            log.error("Journal error\n", e);
            if (SyncUtil.getSyncStatus() == SyncStatusState.ENABLED_STRICT)
                throw(new SyncException("Error in interceptor, see log messages and callstack.", e));
        }
        
        return dataChanged;
    }
    
    /**
     * Adds a property value to the existing serialization record as a string.
     * <p>
     * If data is null or empty string it will be skipped, no empty serialization items are written. In case of
     * xml serialization, the data will be serialized as: &lt;property type='classname'&gt;data&lt;/property&gt;
     * 
     * @param xml record node to append to
     * @param parent the pointer to the root parent node
     * @param property new item name (in case of xml serialization this will be child element name)
     * @param classname type of the property, will be recorded as attribute named 'type' on the child item
     * @param data String content, in case of xml serialized as text node (i.e. not CDATA)
     * @throws Exception
     */    
	protected void appendRecord(Record xml, Item parent, String property, String classname, String data) throws Exception {
        //if (data != null && data.length() > 0) {
    	// this will break if we don't allow data.length==0 - some string values are required NOT NULL, but can be blank
    	if (data != null) {
            Item item = xml.createItem(parent, property);
            item.setAttribute("type", classname);
            xml.createText(item, data);
        }
    }
    
    /**
     * Determines if entity is to be 'synchronized'. There are three ways this can happen:
     * <p>
     * 1. Sync status is globally set to disabled.
     * <p>
     * 2. Entity implements Synchronizable interface.
     * <p>
     * 3. Entity implements SynchronizableInstance and IsSynchronizable is set to true
     * <p>
     * 4. Finally, interceptor supports manual override to suspend synchronization by setting the deactivated bit 
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
        
        if (SyncUtil.getSyncStatus() == SyncStatusState.DISABLED) return false;
        
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
    
    /**
     * Retrieves guid of Synchronizable instance from the storage based on indentity value (i.e. PK).
     * 
     * <p>Remarks: While this method loads the instance into session, for safe measure it promptly evicts it. The only
     * reason to do so is because not doing it causes an error in hibernate when processing disconnected proxies. Specifically,
     * during obs edit, several properties are are disconnected as the form controller uses session.clear() 
     * and then session.merge(). By debugging, it turnes out if we fetch the instance that was disconnected into the current
     * session here, hibernate will fail the pending edit. For exact call stack run in debug mode, but the 
     * final stack frames are in ForeignKeys.isNullifiable: us fetching the instance will cause the following li.getImplementation(session)
     * to return not null which is then followed up by object = li.getImplementation(); -- this will throw an exception. To
     * avoid this we manually evict fetch instance in this method.
     * 
     * @param obj Instance of Synchronizable for which to retrieve guid for.
     * @return guid from storage if obj identity value is set, else null.
     * @see ForeignKeys 
     */
    protected String fetchGuid(Synchronizable obj) {
        String guid = null;
        String idPropertyName = null;
        Object idPropertyValue = null;
        Method m = null;
        
        // what are you doing to me?!
        if (obj == null) return null;
                
        try {
        	        	
            SessionFactory factory = (SessionFactory)this.context.getBean("sessionFactory");
            Class objTrueType = null;
            if (obj instanceof HibernateProxy) {
            	objTrueType = org.hibernate.proxy.HibernateProxyHelper.getClassWithoutInitializingProxy(obj);
            } else {
            	objTrueType = obj.getClass();
            };
            
            //ClassMetadata is only available for entities configured in hibernate
            ClassMetadata data = factory.getClassMetadata(objTrueType);
            if (data != null) {
            	idPropertyName = data.getIdentifierPropertyName();            
            	if (idPropertyName != null) {
                
            		m = SyncUtil.getGetterMethod(objTrueType,idPropertyName);
            		if (m != null) {
            			idPropertyValue = m.invoke(obj, (Object[])null);
            		}
            	}
            }
            
            //finally try to fetch the instance and get its guid
            if (idPropertyValue != null) {             
            	Synchronizable fetchedInstance = (Synchronizable)factory.getCurrentSession().get(objTrueType, (Serializable)idPropertyValue);
            	guid = fetchedInstance.getGuid();
            	factory.getCurrentSession().evict(fetchedInstance); //to be sure, evict it -- see method remarks for more detail
            	
            }
        }
        catch(Exception ex) {
            //something went wrong - no matter just return null
            guid = null;
            log.warn("Error in fetchGuid: returning null", ex);
        }
        
        return guid;
    }

    /**
     * Processes changes to persistent sets that contains instances of Synchronizable objects.
     * 
     * <p>Remarks:
     * <p>Xml 'schema' for the sync item content for the persisted set follows. Note that for persisted sets
     * syncItemKey is a composite of owner object guid and the property name that contains the collection. 
     * <br/>&lt;persistent-set&gt; element: wrapper element
     * <br/>&lt;owner guid='' propertyName='' type='' action='recreate|update' &gt; element: 
     * this captures the information about the object that holds reference to the collection being processed 
     * <br/>-guid: owner object guid
     * <br/>-properyName: names of the property on owner object that holds this collection
     * <br/>-type: owner class name
     * <br/>-action: recreate, update -- these are collection events defined by hibernate interceptor
     * <br/>&lt;entry action='update|delete' guid='' type='' &gt; element: this captures info about individual collection entries:
     * <br/>-action: what is being done to this item of the collection: 
     * delete (item was removed from the collection) or update (item was added to the collection)
     * <br/>-guid: entry's guid
     * <br/>-type: class name
     * 
     * @param set Instance of Hibernate PersistentSet to process.
     * @param key key of owner for the set.
     * @param action action being performed on the set: update, recreate
     */
    protected 	void processPersistentSet(PersistentSet set, Serializable key, String action){
    	Synchronizable owner = null;
    	String originalRecordGuid = null;
    	SessionFactory factory = null;
    	
    	//we only process recreate and update
    	if (!"update".equals(action) && !"recreate".equals(action)) {
        	log.error("Unexpected 'action' supplied, valid values: recreate, update. value provided: " + action);
            if (SyncUtil.getSyncStatus() == SyncStatusState.ENABLED_STRICT)
            	throw new CallbackException("Unexpected 'action' supplied while processing a persistent set.");    		
    	}
    	
    	//retrieve owner and original guid if there is one 
    	if (set.getOwner() instanceof Synchronizable) {
    		owner = (Synchronizable)set.getOwner();
    		originalRecordGuid = owner.getLastRecordGuid();
    	}else {
    		log.info("Cannot process PersistentSet where owner is not Synchronizable.");
    		return;
    	}

        factory = (SessionFactory)this.context.getBean("sessionFactory");

    	/* determine if this set needs to be processed. Process if:
    	 * 1. it is recreate or
    	 * 2. is dirty && current state does not equal stored snapshot
    	 */
    	boolean process = false;
    	if ("recreate".equals(action)) {
    		process = true;
    	} else {
    		if (!set.isDirty()) {
    		log.info("Set marked as not dirty.");
	    	} else {
		    	org.hibernate.persister.collection.CollectionPersister persister = ((org.hibernate.engine.SessionFactoryImplementor) factory).getCollectionPersister(set.getRole());
		    	if (!set.equalsSnapshot(persister)) {
		    		process = true;
		    	} else {
		    		log.info("set current state and snapshots are same, no update needed.");
		    	}
	    	}
    	}
    	if (!process) return;
    	
    	//pull out the property name on owner that corresponds to the collection
        ClassMetadata data = factory.getClassMetadata(owner.getClass());
        String[] propNames = data.getPropertyNames();
        String ownerPropertyName = null; //this is the name of the property on owner object that contains the set
        for(String propName : propNames) {
        	Object propertyVal = data.getPropertyValue(owner,propName, org.hibernate.EntityMode.POJO);
        	//note: test both with equals() and == because PersistentSet.equals()
        	//actually does not handle equality of two persistent sets well
        	if (set == propertyVal || set.equals(propertyVal)) {
        		ownerPropertyName = propName;
        		break;
        	}
        }
        if (ownerPropertyName == null) {
        	log.error("Could not find the property on owner object that corresponds to the set being processed.");
        	log.error("owner info: \ntype: " + owner.getClass().getName() + ", \nguid: " + owner.getGuid() + ",\n property name for collection: " + ownerPropertyName);
            if (SyncUtil.getSyncStatus() == SyncStatusState.ENABLED_STRICT)
            	throw new CallbackException("Could not find the property on owner object that corresponds to the set being processed.");
        }
        
                
        // Setup the serialization data structures to hold the state            
        Package pkg = new Package();
        try {
        	
	        Record xml = pkg.createRecordForWrite(set.getClass().getName());
	        Item entityItem = xml.getRootItem();
	        
	        //serialize owner info: we will need type, prop name where set goes, and owner guid
	        Item item = xml.createItem(entityItem, "owner");
            item.setAttribute("type", owner.getClass().getName());
            item.setAttribute("properyName", ownerPropertyName);
            item.setAttribute("action", action);
            item.setAttribute("guid", owner.getGuid());
            
            //now persist new/updated set entries
	    	for(Object entry : set) {	    		
	    		if (entry instanceof Synchronizable) {
	    			Synchronizable obj = (Synchronizable) entry;
	    			
	    			//attempt to retrieve entry guid
	    			String entryGuid = obj.getGuid();
	    			if (entryGuid == null) {
	    				entryGuid = fetchGuid(obj);
                        if(log.isDebugEnabled()) {
                            log.debug("Entry guid was null, attempted to fetch guid with the following results");
                            log.debug("Entry type:" + obj.getClass().getName() + ",guid:" + entryGuid);
                        }
	    			}
	    			//well, this is messed up: have an instance of Synchronizable but has no guid
	    			if (entryGuid == null) {
		    			log.error("Cannot handle set entries where guid is null.");
	                	throw new CallbackException("Cannot handle set entries where guid is null.");
	    			}	
	    			
	    	        Item itemUpdate = xml.createItem(entityItem, "entry");
	    	        itemUpdate.setAttribute("type", obj.getClass().getName());
	    	        itemUpdate.setAttribute("action", "update");
	    	        itemUpdate.setAttribute("guid", entryGuid);
	    		}
	    		else {
	    			//TODO: more debug info
	    			log.error("Cannot handle sets where entries are not Synchronizable!");
	                if (SyncUtil.getSyncStatus() == SyncStatusState.ENABLED_STRICT)
	                	throw new CallbackException("Cannot handle sets where entries are not Synchronizable!");
	    		}
	    	} 	 	
	    	
	    	//add on deletes
	    	if (!"recreate".equals(action) && set.getRole() != null) {
		    	org.hibernate.persister.collection.CollectionPersister persister = ((org.hibernate.engine.SessionFactoryImplementor) factory).getCollectionPersister(set.getRole());
		    	Iterator it = set.getDeletes(persister, false);
		    	if (it != null) {
			    	while (it.hasNext()) {
			    		Object entryDelete = it.next();
			    		if (entryDelete instanceof Synchronizable) {
			    			Synchronizable objDelete = (Synchronizable) entryDelete;
			    			//attempt to retrieve entry guid
			    			String entryDeleteGuid = objDelete.getGuid();
			    			if (entryDeleteGuid == null) {
			    				entryDeleteGuid = fetchGuid(objDelete);
		                        if(log.isDebugEnabled()) {
		                            log.debug("Entry guid was null, attempted to fetch guid with the following results");
		                            log.debug("Entry type:" + entryDeleteGuid.getClass().getName() + ",guid:" + entryDeleteGuid);
		                        }
			    			}
			    			//well, this is messed up: have an instance of Synchronizable but has no guid
			    			if (entryDeleteGuid == null) {
				    			log.error("Cannot handle set delete entries where guid is null.");
			                	throw new CallbackException("Cannot handle set delete entries where guid is null.");
			    			}	
			    			
			    	        Item itemDelete = xml.createItem(entityItem, "entry");
			    	        itemDelete.setAttribute("type", objDelete.getClass().getName());
			    	        itemDelete.setAttribute("action", "delete");
			    	        itemDelete.setAttribute("guid", entryDeleteGuid);
			    		}
			    		else {
			    			//TODO: more debug info
			    			log.error("Cannot handle sets where entries are not Synchronizable!");
			                if (SyncUtil.getSyncStatus() == SyncStatusState.ENABLED_STRICT)
			                	throw new CallbackException("Cannot handle sets where entries are not Synchronizable!");
			    		}
			    	}
		    	}
	    	}
	        
	        /*
	         * Create SyncItem and store change in SyncRecord kept in ThreadLocal.
	         * note: when making SyncItemKey, make it a composite string of guid + prop. name to avoid collisions
	         * with updates to parent object or updates to more than one collection on same owner
	         */
	        SyncItem syncItem = new SyncItem();
	        syncItem.setKey(new SyncItemKey<String>(owner.getGuid() + "|" + ownerPropertyName, String.class));
	        syncItem.setState(SyncItemState.UPDATED);
	        syncItem.setContainedType(set.getClass());
	        syncItem.setContent(xml.toStringAsDocumentFragement());   
	        
	        syncRecordHolder.get().addOrReplaceItem(syncItem);
	        syncRecordHolder.get().addContainedClass(owner.getClass().getSimpleName());
	        
	        //do the original guid dance, same as in packageObject
            if (syncRecordHolder.get().getOriginalGuid() == null || "".equals(syncRecordHolder.get().getOriginalGuid())) { 
            	syncRecordHolder.get().setOriginalGuid(originalRecordGuid);
            }
        }
        catch(Exception ex) {
        	log.error("Error processing Persistent set, see callstack and inner expection",ex);
            if (SyncUtil.getSyncStatus() == SyncStatusState.ENABLED_STRICT)
            	throw new CallbackException("Error processing Persistent set, see callstack and inner expection.",ex);
        }	
    }
}

