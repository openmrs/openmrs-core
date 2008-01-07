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
package org.openmrs.api.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.SynchronizationIngestService;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.Item;
import org.openmrs.serialization.Record;
import org.openmrs.synchronization.SyncConstants;
import org.openmrs.synchronization.SyncItemState;
import org.openmrs.synchronization.SyncRecordState;
import org.openmrs.synchronization.SyncUtil;
import org.openmrs.synchronization.Synchronizable;
import org.openmrs.synchronization.engine.SyncItem;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.ingest.SyncImportItem;
import org.openmrs.synchronization.ingest.SyncImportRecord;
import org.openmrs.synchronization.ingest.SyncItemIngestException;
import org.openmrs.synchronization.server.RemoteServer;
import org.openmrs.synchronization.server.RemoteServerType;
import org.openmrs.synchronization.server.SyncServerRecord;
import org.openmrs.util.OpenmrsUtil;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

public class SynchronizationIngestServiceImpl implements SynchronizationIngestService {

    private Log log = LogFactory.getLog(this.getClass());
     
    /**
     * 
     * @see org.openmrs.api.SynchronizationIngestService#processSyncImportRecord(SyncImportRecord importRecord)
     * 
     * @param importRecord
     * @throws APIException
     */
    public void processSyncImportRecord(SyncImportRecord importRecord, RemoteServer server) throws APIException {
        if ( importRecord != null ) {
            if ( importRecord.getGuid() != null && importRecord.getState() != null ) {
                SyncRecord record = Context.getSynchronizationService().getSyncRecordByOriginalGuid(importRecord.getGuid());
                if ( server.getServerType().equals(RemoteServerType.PARENT) ) {
                    // with parents, we set the actual state of the record
                    if ( importRecord.getState().equals(SyncRecordState.ALREADY_COMMITTED) ) record.setState(SyncRecordState.COMMITTED);
                    else if ( importRecord.getState().equals(SyncRecordState.NOT_SUPPOSED_TO_SYNC) ) record.setState(SyncRecordState.REJECTED);
                    else record.setState(importRecord.getState());
                } else {
                    // with non-parents we set state in the server-record
                    SyncServerRecord serverRecord = record.getServerRecord(server);
                    if ( importRecord.getState().equals(SyncRecordState.ALREADY_COMMITTED) ) serverRecord.setState(SyncRecordState.COMMITTED);
                    else if ( importRecord.getState().equals(SyncRecordState.NOT_SUPPOSED_TO_SYNC) ) serverRecord.setState(SyncRecordState.REJECTED);
                    else serverRecord.setState(importRecord.getState());
                }
                
                Context.getSynchronizationService().updateSyncRecord(record);
            }
        }        
    }
    
    /**
     * 
     * TODO
     * 
     * @param record
     * @return
     */
    public SyncImportRecord processSyncRecord(SyncRecord record, RemoteServer server) throws APIException {
        SyncImportRecord importRecord = new SyncImportRecord();
        importRecord.setState(SyncRecordState.FAILED);  // by default, until we know otherwise
        importRecord.setRetryCount(record.getRetryCount());
        importRecord.setTimestamp(record.getTimestamp());
        
        try {
            // first, let's see if this server even accepts this kind of syncRecord
            if ( !server.getClassesReceived().containsAll(record.getContainedClassSet())) {
                importRecord.setState(SyncRecordState.NOT_SUPPOSED_TO_SYNC);
                log.warn("\nNOT INGESTING RECORD with " + record.getContainedClasses() + " BECAUSE SERVER IS NOT READY TO ACCEPT ALL CONTAINED OBJECTS\n");
            } else {
                //log.warn("\nINGESTING ALL CLASSES: " + recordClasses + " BECAUSE SERVER IS READY TO ACCEPT ALL");
                // second, let's see if this SyncRecord has already been imported
                // use the original record id to locate import_record copy
                log.warn("AT THIS POINT, ORIGINALGUID FOR RECORD IS " + record.getOriginalGuid());
                importRecord = Context.getSynchronizationService().getSyncImportRecord(record.getOriginalGuid());
                boolean isUpdateNeeded = false;
                
                if ( importRecord == null ) {
                    isUpdateNeeded = true;
                    importRecord = new SyncImportRecord(record);
                    importRecord.setGuid(record.getOriginalGuid());
                    Context.getSynchronizationService().createSyncImportRecord(importRecord);
                } else {
                    log.info("ImportRecord already exists and has state: " + importRecord.getState());
                    SyncRecordState state = importRecord.getState();
                    if ( state.equals(SyncRecordState.COMMITTED) ) {
                        // apparently, the remote/child server exporting to this server doesn't realize it's
                        // committed, so let's remind by sending back this import record with already_committed
                        importRecord.setState(SyncRecordState.ALREADY_COMMITTED);
                    } else if (state.equals(SyncRecordState.FAILED)) {
                        // apparently, this record was already sent and full-failed - let's not attempt again
                        // TODO: eventually we should allow you to override this with a -force option
                    }else {
                        isUpdateNeeded = true;
                    }
                }
                
                if ( isUpdateNeeded ) {
                    
                    boolean isError = false;
                            
                    // for each sync item, process it and insert/update the database
                    for ( SyncItem item : record.getItems() ) {
                        // this could be done differently - just passing actual item to processSyncItem
                        String syncItem = item.getContent();
                        SyncImportItem importedItem = this.processSyncItem(syncItem, record.getOriginalGuid() + "|" + server.getGuid());
                        importedItem.setKey(item.getKey());
                        importRecord.addItem(importedItem);
                        if ( !importedItem.getState().equals(SyncItemState.SYNCHRONIZED)) isError = true;
                    }
                    if ( !isError ) {
                        importRecord.setState(SyncRecordState.COMMITTED);
                        
                        // now that we know there's no error, we have to prevent this change from being sent back to the originating server
                        /*
                         * This actually can't be done here, since hibernate may not yet commit the record - 
                         * instead we have to get hacky: in processxxx() methods we set originalGuid and then commit changes.
                         * Once that is done, the interceptor pulls the original guid out and calls SynchronizationServiceImpl.createSyncRecord()
                         * where sync records *are* not written for the originting server
                         * 
                        SyncRecord newRecord = Context.getSynchronizationService().getSyncRecord(record.getOriginalGuid());
                        if ( newRecord != null ) {
                            if ( server.getServerType().equals(RemoteServerType.PARENT)) {
                                newRecord.setState(SyncRecordState.COMMITTED);
                            } else {
                                SyncServerRecord serverRecord = newRecord.getServerRecord(server);
                                if ( serverRecord != null ) {
                                    serverRecord.setState(SyncRecordState.COMMITTED);
                                    
                                } else {
                                    log.warn("No server record was created for server " + server.getNickname() + " and record " + record.getOriginalGuid());
                                }
                            }
                            Context.getSynchronizationService().updateSyncRecord(newRecord);

                        } else {
                            log.warn("Can't find newly created record on system by originalGuid" + record.getOriginalGuid());
                        }
                        */
                    } else {
                        // rollback!!
                        // also, set to failure.  if we've come this far and record failed to commit, it will likely never commit
                        importRecord.setState(SyncRecordState.FAILED);
                    }
                    
                    Context.getSynchronizationService().updateSyncImportRecord(importRecord);
                }
            }
        } catch (Exception e ) {
            e.printStackTrace();
        }

        return importRecord;
    }
    
    public SyncImportItem processSyncItem(String incoming, String originalGuid)  throws APIException {
        SyncImportItem ret = new SyncImportItem();
        ret.setContent(incoming);
        ret.setState(SyncItemState.UNKNOWN);

        try {
            Object o = null;
            
            try {
                if (log.isDebugEnabled())
                    log.debug("STARTING TO PROCESS: " + incoming);
                
                o = SyncUtil.getRootObject(incoming);
                if (o instanceof org.hibernate.collection.PersistentCollection) {
                	processHibernateCollection(o.getClass(),incoming,originalGuid);
                } else {
                	processSynchronizable((Synchronizable)o,incoming,originalGuid);
                }
                ret.setState(SyncItemState.SYNCHRONIZED);
                
            } catch (Exception e) {
            	e.printStackTrace();
                throw new SyncItemIngestException(SyncConstants.ERROR_ITEM_BADXML_ROOT, null, incoming);
            }
                
        } catch (SyncItemIngestException siie) {
            ret.setErrorMessage(siie.getItemError());
            ret.setErrorMessageArgs(siie.getItemErrorArgs());
            ret.setState(SyncItemState.CONFLICT);
        } catch (Exception e) {
            ret.setErrorMessage(SyncConstants.ERROR_ITEM_NOT_PROCESSED);
        }       
        
        return ret;        
    }
    
    /**
     * Processes the serializes state of a collection.
     * <p>Remarks: Handles two types of hibernate collections: PersistentSortedSet and PersistenSet.
     * Processing of collections is handled as follows based on the serialized info stored in incoming:
     * <p>Pull out owner info, and collection action (i.e. update, recreate, remove). Attempt to create instance of the owner using openmrs API and
     * retrieve the reference to the existing collection that is associated with the owner.
     * <p>Iterate owner serialized entries and process actions (i.e entry update, delete)
     * <p>record the original guid using owner
     * <p>finally, trigger owner update using openmrs api
     * <p>For algorhitmic details, see code comments as the implementation is extensively commented.
     * 
     * @param type collection type.
     * @param incoming serialized state, interceptor implementation for serialization details
     * @param originalGuid unique guid assigned to this update that will be propagated throughout the synchronization to avoid
     * duplicating this change
     */
    private void processHibernateCollection(Class collectionType, String incoming, String originalGuid) throws Exception {

    	Synchronizable owner = null;
    	String ownerClassName = null;
    	String ownerCollectionPropertyName = null;
    	String ownerGuid = null;
    	String ownerCollectionAction = null; //is this coll update or recreate?
    	NodeList nodes = null;
    	Set entries = null;
    	int i = 0;

    	//first find out what kid of set we are dealing with:
    	//Hibernate PersistentSortedSet == TreeSet, note this is derived from PersistentSet so we have to test for it first
    	//Hibernate PersistentSet == HashSet
    	if (!org.hibernate.collection.PersistentSet.class.isAssignableFrom(collectionType)) {    		
    		//don't know how to process this collection type
    		log.error("Do not know how to process this collection type: " + collectionType.getName());
    		throw new SyncItemIngestException(SyncConstants.ERROR_ITEM_BADXML_MISSING, null, incoming);
    	}
    	    	
    	//next, pull out the owner node and get owner instance: 
    	//we need reference to owner object before we start messing with collection entries
    	nodes = SyncUtil.getChildNodes(incoming);
    	if (nodes == null) {
    		throw new SyncItemIngestException(SyncConstants.ERROR_ITEM_BADXML_MISSING, null, incoming);
    	}
        for ( i = 0; i < nodes.getLength(); i++ ) {
    		if (nodes.item(i).getNodeName() == "owner") {
    	    	//pull out collection owner info: class name of owner, its guid, and name of poperty on owner that holds this collection
    			ownerClassName = ((Element)nodes.item(i)).getAttribute("type");
    			ownerCollectionPropertyName = ((Element)nodes.item(i)).getAttribute("properyName");
    			ownerCollectionAction = ((Element)nodes.item(i)).getAttribute("action");
    			ownerGuid = ((Element)nodes.item(i)).getAttribute("guid");
    			break;
    		}
    	}
    	if (ownerGuid == null) {
    		log.error("Owner guid is null while processing collection.");
    		throw new SyncItemIngestException(SyncConstants.ERROR_ITEM_BADXML_MISSING, null, incoming);
    	}
        owner = (Synchronizable)SyncUtil.getOpenmrsObj(ownerClassName, ownerGuid);    	
    	
    	//NOTE: we cannot just new up a collection and assign to parent:
        //if hibernate mapping has cascade deletes, it will orphan existing collection and hibernate will throw error
        //to that effect: "A collection with cascade="all-delete-orphan" was no longer referenced by the owning entity instance"
        //*only* if this is recreate; clear up the existing collection and start over
        if ("recreate".equals(ownerCollectionAction)) {
        	if (org.hibernate.collection.PersistentSortedSet.class.isAssignableFrom(collectionType)) {    		
        		entries = new TreeSet();
        	} else if (org.hibernate.collection.PersistentSet.class.isAssignableFrom(collectionType)) {
        		entries = new HashSet();
        	}
        } else {
	        Method m = null;
	        m = SyncUtil.getGetterMethod(owner.getClass(),ownerCollectionPropertyName);
	        if (m == null) {
	        	log.error("Cannot retrieve getter method for ownerCollectionPropertyName:" + ownerCollectionPropertyName);
	    		log.error("Owner info: " +
	      				"\nownerClassName:" + ownerClassName + 
	      				"\nownerCollectionPropertyName:" + ownerCollectionPropertyName +
	      				"\nownerCollectionAction:" + ownerCollectionAction +
	      				"\nownerGuid:" + ownerGuid);	        	
	        	throw new SyncItemIngestException(SyncConstants.ERROR_ITEM_BADXML_MISSING, null, incoming);
	        }
	        entries = (Set)m.invoke(owner, (Object[])null);
        }
        
        if (entries == null) {
    		log.error("Was not able to retrieve reference to the collection using owner object.");
    		log.error("Owner info: " +
    				"\nownerClassName:" + ownerClassName + 
    				"\nownerCollectionPropertyName:" + ownerCollectionPropertyName +
    				"\nownerCollectionAction:" + ownerCollectionAction +
    				"\nownerGuid:" + ownerGuid);
    		throw new SyncItemIngestException(SyncConstants.ERROR_ITEM_BADXML_MISSING, null, incoming);
        }
        
        //now, finally process nodes, phew!!
        for ( i = 0; i < nodes.getLength(); i++ ) {
        	if(nodes.item(i).getNodeName() == "entry") {
				String entryClassName = ((Element)nodes.item(i)).getAttribute("type");
				String entryGuid = ((Element)nodes.item(i)).getAttribute("guid");
				String entryAction = ((Element)nodes.item(i)).getAttribute("action");
				Object entry = SyncUtil.getOpenmrsObj(entryClassName, entryGuid);
				if ("update".equals(entryAction)) {				
					if (!OpenmrsUtil.collectionContains(entries, entry)) {
						entries.add(entry);
					}
				} else if ("delete".equals(entryAction)) {
					if (!entries.remove(entry)) {
						//couldn't find entry in collection: hmm, bad implementation of equals?
						//fall back to trying to find the item in entries by guid
						Synchronizable toBeRemoved = null;
						for(Object o : entries) {
							if (o instanceof Synchronizable) {
								if( entryGuid == ((Synchronizable)o).getGuid() ) {
									toBeRemoved = (Synchronizable)o;
									break;
								}
							}
						}
						if (toBeRemoved == null) {
							log.error("Was not able to process collection entry delete.");
				    		log.error("Owner info: " +
				      				"\nownerClassName:" + ownerClassName + 
				      				"\nownerCollectionPropertyName:" + ownerCollectionPropertyName +
				      				"\nownerCollectionAction:" + ownerCollectionAction +
				      				"\nownerGuid:" + ownerGuid);
				    		log.error("entry info: " +
					      				"\nentryClassName:" + entryClassName + 
					      				"\nentryGuid:" + entryGuid);							
							throw new SyncItemIngestException(SyncConstants.ERROR_ITEM_NOT_COMMITTED, ownerClassName, incoming);
						} else {
							//finally, remove it from the collection
							entries.remove(toBeRemoved);
						}
					}
					
				} else {
					log.error("Unknown collection entry action, action was: " + entryAction);
					throw new SyncItemIngestException(SyncConstants.ERROR_ITEM_NOT_COMMITTED, ownerClassName, incoming);
				}
    		}
    	}
              
        //set the original guid: this will prevent the change from being send back to originating server
        ((Synchronizable)owner).setLastRecordGuid(originalGuid);

        //assign collection back to the owner if it is recreate
        if ("recreate".equals(ownerCollectionAction)) {
        	SyncUtil.setProperty(owner,ownerCollectionPropertyName,entries);
        }
        
        //finally, trigger update
        try {
            SyncUtil.updateOpenmrsObject(owner, ownerClassName, ownerGuid, true);
        } catch ( Exception e ) {
        	e.printStackTrace();
            throw new SyncItemIngestException(SyncConstants.ERROR_ITEM_NOT_COMMITTED, ownerClassName, incoming);
        }
    }

    /**
     * Processes serialized SyncItem state by attempting to hydrate the object SyncItem represents and then using OpenMRS service layer to
     * update the hydrated instance of Synchronizable object.
     * <p>Remarks: This implementation relies on internal knowledge of how SyncItems are serialized: it itterates over direct child nodes of the root xml
     * node in incoming assuming they are serialized public properties of the object that is being hydrated. Consequently, for each child node, 
     * property setter is determined and then called. After setting all properties, OpenMRS service layer API is used to actually save 
     * the object into persistent store. The details of how property setters are determined and how appropriate service layer methods
     * are determined are contained in SyncUtil class.
     *  
     * @param o empty instance of class that this SyncItem represents 
     * @param incoming Serialized state of SyncItem.
     * @param originalGuid Unique id of the object that is stored in SyncItem recorded when this object was first created. NOTE:
     * this value is retained and forwarded unchanged throughout the network of sychronizing servers in order to avoid re-applying
     * same changes over and over.
     * 
     * @see SyncUtil#setProperty(Object, String, Object)
     * @see SyncUtil#getOpenmrsObj(String, String)
     * @see SyncUtil#updateOpenmrsObject(Object, String, String, boolean)
     */
    private void processSynchronizable(Synchronizable o, String incoming, String originalGuid) throws Exception {

        String className = null;
        boolean isUpdateNotCreate = false;
        ArrayList<Field> allFields = null;
        NodeList nodes = null;

    	className = o.getClass().getName();
        allFields = SyncUtil.getAllFields(o);  // get fields, both in class and superclass - we'll need to know what type each field is
        nodes = SyncUtil.getChildNodes(incoming);  // get all child nodes (xml) of the root object

	    if ( o == null || className == null || allFields == null || nodes == null ) {
	    	throw new SyncItemIngestException(SyncConstants.ERROR_ITEM_NOCLASS, className, incoming);
	    }

	    String guid = SyncUtil.getAttribute(nodes, "guid", allFields);
        Synchronizable objOld = (Synchronizable)SyncUtil.getOpenmrsObj(className, guid);
        if ( objOld != null ) {
            o = objOld;
            isUpdateNotCreate = true;
        }
	        
        //process contained properties
        for ( int i = 0; i < nodes.getLength(); i++ ) {
            try {
                SyncUtil.setProperty(o, nodes.item(i), allFields);
            } catch ( Exception e ) {
            	log.error("Error when trying to set " + nodes.item(i).getNodeName() + ", which is a " + className);
            	e.printStackTrace();
                throw new SyncItemIngestException(SyncConstants.ERROR_ITEM_UNSET_PROPERTY, nodes.item(i).getNodeName() + "," + className, incoming);
            }
        }
        
        //set the original guid: this will prevent the change from being send back to originating server
        //see SynchronizationServiceImpl.createRecord() which will eventually get called from interceptor when
        //this change in committed
        ((Synchronizable)o).setLastRecordGuid(originalGuid);
	        
        // now try to commit this fully inflated object
        try {
            SyncUtil.updateOpenmrsObject(o, className, guid, isUpdateNotCreate);
        } catch ( Exception e ) {
        	e.printStackTrace();
            throw new SyncItemIngestException(SyncConstants.ERROR_ITEM_NOT_COMMITTED, className, incoming);
        }
	        
        if (log.isDebugEnabled()) {
            log.debug("We now have an object " + o.getClass().getName() + " to INSERT with possible GUID of " + ((Synchronizable)o).getGuid());
        }
        
        return;
    }
}
