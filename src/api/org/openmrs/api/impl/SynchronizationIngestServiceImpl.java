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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.SynchronizationIngestService;
import org.openmrs.api.context.Context;
import org.openmrs.synchronization.SyncConstants;
import org.openmrs.synchronization.SyncItemState;
import org.openmrs.synchronization.SyncRecordState;
import org.openmrs.synchronization.SyncPreCommitAction;
import org.openmrs.synchronization.SyncUtil;
import org.openmrs.synchronization.Synchronizable;
import org.openmrs.synchronization.engine.SyncItem;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.ingest.SyncImportItem;
import org.openmrs.synchronization.ingest.SyncImportRecord;
import org.openmrs.synchronization.ingest.SyncIngestException;
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
     * Applies  synchronization record against the local data store in single transaction.  
     * <p/> Remarks: Exceptions are always thrown if something goes wrong while processing the record in order to abort sync items as 
     * one transaction. To report back SyncImportRecord accuratenly in case of exception, notice that SyncIngestException contains
     * SyncImportRecord. In case of exception, callers should inspect this value as it will contain more information about the status of sync
     * item as it failed.
     * 
     * @param record SyncRecord to be processed
     * @param server Server where the record came from
     * @return
     */
    public SyncImportRecord processSyncRecord(SyncRecord record, RemoteServer server) throws SyncIngestException {
        
    	ArrayList<SyncItem> deletedItems = new ArrayList<SyncItem>();
    	SyncImportRecord importRecord = new SyncImportRecord();
        importRecord.setState(SyncRecordState.FAILED);  // by default, until we know otherwise
        importRecord.setRetryCount(record.getRetryCount());
        importRecord.setTimestamp(record.getTimestamp());
        List<SyncPreCommitAction> preCommitRecordActions = new ArrayList<SyncPreCommitAction> ();  
        
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
                	log.info("ImportRecord does not exist, so creating new one");
                    isUpdateNeeded = true;
                    importRecord = new SyncImportRecord(record);
                    importRecord.setState(SyncRecordState.FAILED);
                    importRecord.setGuid(record.getOriginalGuid());
                    Context.getSynchronizationService().createSyncImportRecord(importRecord);
                } else {
                	if(log.isWarnEnabled()) {
                		log.warn("ImportRecord already exists and has retry count: " + importRecord.getRetryCount() + ", state: " + importRecord.getState());
                	}
                    SyncRecordState state = importRecord.getState();
                    if ( state.equals(SyncRecordState.COMMITTED)  || state.equals(SyncRecordState.ALREADY_COMMITTED) ) {
                        // apparently, the remote/child server exporting to this server doesn't realize it's
                        // committed, so let's remind by sending back this import record with already_committed
                        importRecord.setState(SyncRecordState.ALREADY_COMMITTED);
                    } else if (state.equals(SyncRecordState.FAILED)) {
                		//mark as failed and retry next time
                    	importRecord.setState(SyncRecordState.FAILED);
                		importRecord.setRetryCount(importRecord.getRetryCount() + 1);
                		isUpdateNeeded = true;
                    }else {
                        isUpdateNeeded = true;
                    }
                }
                
                if ( isUpdateNeeded ) {
                    log.debug("Looks like update is needed");
                	
                    boolean isError = false;
                            
                    //as we start setting properties, suspend session flushing 
                    Context.getSynchronizationService().setFlushModeManual();

                    // for each sync item, process it and insert/update the database; 
                    //put deletes into deletedItems collection -- these will get processed last
                    for ( SyncItem item : record.getItems() ) {
                    	if (item.getState() == SyncItemState.DELETED) {
                    		deletedItems.add(item);
                    	} else {
	                        SyncImportItem importedItem = this.processSyncItem(item, record.getOriginalGuid() + "|" + server.getGuid(),preCommitRecordActions);
	                        importedItem.setKey(item.getKey());
	                        importRecord.addItem(importedItem);
	                        if ( !importedItem.getState().equals(SyncItemState.SYNCHRONIZED)) isError = true;
                    	}
                    }
                    
                    Context.getSynchronizationService().flushSession();
                    Context.getSynchronizationService().setFlushModeAutomatic();
                    
                    /* now run through deletes: deletes must be processed after inserts/updates
                     * because of hibernate flushing semantics inside transactions:
                     * if deleted entity is part of a collection on another object within the same session
                     * and this object gets flushed, error is thrown stating that deleted entities must first be removed
                     * from collection; this happens immediately when stmts are executed (and not at the Tx boundry) because
                     * default hibernate FlushMode is AUTO. To futher avoid this issue, explicitely susspend flushing for the 
                     * duration of deletes.
                     */
                	Context.getSynchronizationService().setFlushModeManual(); 
                    for ( SyncItem item : deletedItems ) {
                        SyncImportItem importedItem = this.processSyncItem(item, record.getOriginalGuid() + "|" + server.getGuid(),preCommitRecordActions);
                        importedItem.setKey(item.getKey());
                        importRecord.addItem(importedItem);
                        if ( !importedItem.getState().equals(SyncItemState.SYNCHRONIZED)) isError = true;
                    }
                    Context.getSynchronizationService().flushSession();
                    Context.getSynchronizationService().setFlushModeAutomatic();
                    
                    /* 
                     * finally execute the pending actions that resulted from processing all sync items 
                     */
                    Context.getSynchronizationService().setFlushModeManual();
                    SyncUtil.applyPreCommitRecordActions(preCommitRecordActions);
                    Context.getSynchronizationService().flushSession();
                    Context.getSynchronizationService().setFlushModeAutomatic();
                    
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
                    	//One of SyncItem commits failed, throw to rollback and set failure information.
                    	log.warn("Error while processing SyncRecord with original ID " + record.getOriginalGuid() + " (" + record.getContainedClasses() + ")");
                        importRecord.setState(SyncRecordState.FAILED);
                        SyncIngestException sie = new SyncIngestException(SyncConstants.ERROR_ITEM_NOT_COMMITTED,null,null,importRecord);
                        throw(sie);
                    }
                    
                    Context.getSynchronizationService().updateSyncImportRecord(importRecord);
                }
            }
        } catch (SyncIngestException e) {
	        e.printStackTrace();
        	//fill in sync import record and rethrow to abort tx
	        importRecord.setState(SyncRecordState.FAILED);
	        importRecord.setErrorMessage(e.getMessage());
        	e.setSyncImportRecord(importRecord);
        	throw (e);
        }
        catch (Exception e ) {
            e.printStackTrace();
            //fill in sync import record and rethrow to abort tx
            importRecord.setState(SyncRecordState.FAILED);
            importRecord.setErrorMessage(e.getMessage());
            SyncIngestException sie = new SyncIngestException(e,SyncConstants.ERROR_RECORD_UNEXPECTED,null,null,importRecord);
            throw(sie);
        } finally {
        	//reset the flush mode back to automatic, no matter what
        	Context.getSynchronizationService().setFlushModeAutomatic();
        }

        return importRecord;
    }
    
    /**
     * Note: preCommitRecordActions collection is provided as a way for the synchronizable instances to 'schedule' action that is necessary
     * for processing of the object yet it cannot be applied until the end of the processing of the parent sync record. For example, rebuild XSN
     * cannot happen until all form fields held in the sync items are applied first; thus the call to rebuild XSN need to happen after all
     * sync items were processed and before committing the sync record.
     * 
     * HashMap contained in the collection is to capture the action, and the necessary object to resolve that action. The action
     * is understood and applied by {@link org.openmrs.synchronization.SyncUtil#applyPreCommitRecordActions(ArrayList)}
     * 
     * @see org.openmrs.api.SynchronizationIngestService#processSyncItem(org.openmrs.synchronization.engine.SyncItem, java.lang.String, java.util.ArrayList)
     * @see org.openmrs.synchronization.SyncUtil#applyPreCommitRecordActions(ArrayList)
     * 
     */
    public SyncImportItem processSyncItem(SyncItem item, String originalGuid,List<SyncPreCommitAction> preCommitRecordActions)  throws APIException {
    	String itemContent = null;
        SyncImportItem ret = null; 

        try {
        	ret = new SyncImportItem();
            //ret.setContent(itemContent); - no need to copy content back: the server that send it knows it already
            ret.setState(SyncItemState.UNKNOWN);

            Object o = null;
			itemContent = item.getContent();
			
            if (log.isDebugEnabled()) {
                log.debug("STARTING TO PROCESS: " + itemContent);
                log.debug("SyncItem state is: " + item.getState());
            }
            
            o = SyncUtil.getRootObject(itemContent);
            if (o instanceof org.hibernate.collection.PersistentCollection) {
            	log.debug("Processing a persistent collection");
            	processHibernateCollection(o.getClass(),itemContent,originalGuid);
            } else {
            	processSynchronizable((Synchronizable)o,item,originalGuid,preCommitRecordActions);
            }
            ret.setState(SyncItemState.SYNCHRONIZED);                
        } catch (SyncIngestException e) {
        	//MUST RETHROW to abort transaction
        	e.setSyncItemContent(itemContent);
        	throw (e);
        }
        catch (Exception e) {
        	//MUST RETHROW to abort transaction
            throw new SyncIngestException(e,SyncConstants.ERROR_ITEM_UNEXPECTED, null, itemContent,null);
        }       
        
        return ret;        
    }
    
    /**
     * Processes the serializes state of a collection.
     * <p>Remarks: Handles two types of hibernate collections: PersistentSortedSet and PersistenSet.
     * Processing of collections is handled as follows based on the serialized info stored in incoming:
     * <p>1. Pull out owner info, and collection action (i.e. update, recreate). 
     * Attempt to create instance of the owner using openmrs API and retrieve the reference 
     * to the existing collection that is associated with the owner.
     * <br/>2. Iterate owner serialized entries and process actions (i.e entry update, delete)
     * <br/>3. Record the original guid using owner finally, trigger owner update using openmrs api
     * <br/>For algorhitmic details, see code comments as the implementation is extensively commented.
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
    	boolean needsRecreate = false;

    	//first find out what kid of set we are dealing with:
    	//Hibernate PersistentSortedSet == TreeSet, note this is derived from PersistentSet so we have to test for it first
    	//Hibernate PersistentSet == HashSet
    	if (!org.hibernate.collection.PersistentSet.class.isAssignableFrom(collectionType)) {    		
    		//don't know how to process this collection type
    		log.error("Do not know how to process this collection type: " + collectionType.getName());
    		throw new SyncIngestException(SyncConstants.ERROR_ITEM_BADXML_MISSING, null, incoming,null);
    	}
    	    	    	
    	//next, pull out the owner node and get owner instance: 
    	//we need reference to owner object before we start messing with collection entries
    	nodes = SyncUtil.getChildNodes(incoming);
    	if (nodes == null) {
    		throw new SyncIngestException(SyncConstants.ERROR_ITEM_BADXML_MISSING, null, incoming,null);
    	}
        for ( i = 0; i < nodes.getLength(); i++ ) {
    		if ("owner".equals(nodes.item(i).getNodeName())) {
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
    		throw new SyncIngestException(SyncConstants.ERROR_ITEM_BADXML_MISSING, null, incoming,null);
    	}
        owner = (Synchronizable)SyncUtil.getOpenmrsObj(ownerClassName, ownerGuid);    	
    	
        //we didn't get the owner record: throw an execption
        //TODO: in future, when we have conflict resolution, this maybe handled differently
        if (owner == null) {
        	log.error("Cannot retrieve the collection's owner object.");
    		log.error("Owner info: " +
      				"\nownerClassName:" + ownerClassName + 
      				"\nownerCollectionPropertyName:" + ownerCollectionPropertyName +
      				"\nownerCollectionAction:" + ownerCollectionAction +
      				"\nownerGuid:" + ownerGuid);	        	
        	throw new SyncIngestException(SyncConstants.ERROR_ITEM_BADXML_MISSING, null, incoming,null);
        }
        
    	//NOTE: we cannot just new up a collection and assign to parent:
        //if hibernate mapping has cascade deletes, it will orphan existing collection and hibernate will throw error
        //to that effect: "A collection with cascade="all-delete-orphan" was no longer referenced by the owning entity instance"
        //*only* if this is recreate; clear up the existing collection and start over
        Method m = null;
        m = SyncUtil.getGetterMethod(owner.getClass(),ownerCollectionPropertyName);
        if (m == null) {
        	log.error("Cannot retrieve getter method for ownerCollectionPropertyName:" + ownerCollectionPropertyName);
    		log.error("Owner info: " +
      				"\nownerClassName:" + ownerClassName + 
      				"\nownerCollectionPropertyName:" + ownerCollectionPropertyName +
      				"\nownerCollectionAction:" + ownerCollectionAction +
      				"\nownerGuid:" + ownerGuid);	        	
        	throw new SyncIngestException(SyncConstants.ERROR_ITEM_BADXML_MISSING, null, incoming,null);
        }
        entries = (Set)m.invoke(owner, (Object[])null);

        /*Two instances where even after this we may need to create a new collection:
         * a) when collection is lazy=false and it is newly created; then asking parent for it will
         * not return new & empty proxy, it will return null
         * b) Special recreate logic:
         * if fetched owner instance has nothing attached, then it is safe to just create brand new collection
         * and assign it to owner without worrying about getting orphaned deletes error
         * if owner has something attached, then we process recreate as delete/update; 
         * that is clear out the existing entries and then proceed to add ones received via sync. 
         * This code essentially mimics hibernate org.hibernate.engine.Collections.prepareCollectionForUpdate()
         * implementation. 
         * NOTE: The unfortunate bi-product of this approach is that this series of events will not produce 
         * 'recreate' event in the interceptor: thus parent's sync journal entries will look slightly diferently 
         * from what child was sending up: child sent up single 'recreate' collection action however
         * parent will instead have single 'update' with deletes & updates in it. Presumably, this is a distinction
         * without a difference.
         */
        	
    	if (entries == null) {
        	if (org.hibernate.collection.PersistentSortedSet.class.isAssignableFrom(collectionType)) {
        		needsRecreate = true;
        		entries = new TreeSet();
        	} else if (org.hibernate.collection.PersistentSet.class.isAssignableFrom(collectionType)) {
        		needsRecreate = true;
        		entries = new HashSet();
        	}
    	}
    	        
        if (entries == null) {
    		log.error("Was not able to retrieve reference to the collection using owner object.");
    		log.error("Owner info: " +
    				"\nownerClassName:" + ownerClassName + 
    				"\nownerCollectionPropertyName:" + ownerCollectionPropertyName +
    				"\nownerCollectionAction:" + ownerCollectionAction +
    				"\nownerGuid:" + ownerGuid);
    		throw new SyncIngestException(SyncConstants.ERROR_ITEM_BADXML_MISSING, null, incoming,null);
        }
        
    	//clear existing entries before adding new ones:
        if ("recreate".equals(ownerCollectionAction)) {
    		entries.clear();
    	}
        
        //now, finally process nodes, phew!!
        for ( i = 0; i < nodes.getLength(); i++ ) {
        	if("entry".equals(nodes.item(i).getNodeName())) {
				String entryClassName = ((Element)nodes.item(i)).getAttribute("type");
				String entryGuid = ((Element)nodes.item(i)).getAttribute("guid");
				String entryAction = ((Element)nodes.item(i)).getAttribute("action");
				Object entry = SyncUtil.getOpenmrsObj(entryClassName, entryGuid);
				
				if (entry == null) {
					//the object not found: most likely cause here is data collision
		    		log.error("Was not able to retrieve reference to the collection entry object.");
		    		log.error("Entry info: " +
		    				"\nentryClassName:" + entryClassName + 
		    				"\nentryGuid:" + entryGuid +
		    				"\nentryAction:" + entryAction);
					throw new SyncIngestException(SyncConstants.ERROR_ITEM_NOT_COMMITTED, ownerClassName, incoming,null);					
				} else if ("update".equals(entryAction)) {				
					if (!OpenmrsUtil.collectionContains(entries, entry)) {
						entries.add(entry);
					}
				} else if ("delete".equals(entryAction)) {
					OpenmrsUtil.collectionContains(entries, entry);
					entries.contains(entry);					
					if (!entries.remove(entry)) {
						//couldn't find entry in collection: hmm, bad implementation of equals?
						//fall back to trying to find the item in entries by guid
						Synchronizable toBeRemoved = null;
						for(Object o : entries) {
							if (o instanceof Synchronizable) {
								if( entryGuid.equals(((Synchronizable)o).getGuid())) {
									toBeRemoved = (Synchronizable)o;
									break;
								}
							}
						}
						if (toBeRemoved == null) {
							//the item to be removed was not located in the collection: log it for reference and continue
							log.warn("Was not able to process collection entry delete.");
				    		log.warn("Owner info: " +
				      				"\nownerClassName:" + ownerClassName + 
				      				"\nownerCollectionPropertyName:" + ownerCollectionPropertyName +
				      				"\nownerCollectionAction:" + ownerCollectionAction +
				      				"\nownerGuid:" + ownerGuid);
				    		log.warn("entry info: " +
					      				"\nentryClassName:" + entryClassName + 
					      				"\nentryGuid:" + entryGuid);							
						} else {
							//finally, remove it from the collection
							entries.remove(toBeRemoved);
						}
					}
					
				} else {
					log.error("Unknown collection entry action, action was: " + entryAction);
					throw new SyncIngestException(SyncConstants.ERROR_ITEM_NOT_COMMITTED, ownerClassName, incoming,null);
				}
    		}
    	}
              
        //set the original guid: this will prevent the change from being send back to originating server
        ((Synchronizable)owner).setLastRecordGuid(originalGuid);

        //assign collection back to the owner if it is recreate
        if (needsRecreate) {
        	SyncUtil.setProperty(owner,ownerCollectionPropertyName,entries);
        }
        
        //finally, trigger update
        try {
        	//no need to mess around with precommit actions for collections, at least
        	//at this point
            SyncUtil.updateOpenmrsObject2(owner, ownerClassName, ownerGuid,null);
        } catch ( Exception e ) {
        	e.printStackTrace();
            throw new SyncIngestException(SyncConstants.ERROR_ITEM_NOT_COMMITTED, ownerClassName, incoming,null);
        }
    }

    /**
     * Processes serialized SyncItem state by attempting to hydrate the object SyncItem represents and then using OpenMRS service layer to
     * update the hydrated instance of Synchronizable object.
     * <p/>Remarks: This implementation relies on internal knowledge of how SyncItems are serialized: it itterates over direct child nodes of the root xml
     * node in incoming assuming they are serialized public properties of the object that is being hydrated. Consequently, for each child node, 
     * property setter is determined and then called. After setting all properties, OpenMRS service layer API is used to actually save 
     * the object into persistent store. The details of how property setters are determined and how appropriate service layer methods
     * are determined are contained in SyncUtil class.
     * <p/>
     * SyncItem with status of DELETED is handled differently from insert/update: In case of a delete, all that is needed (and sent) 
     * is the object type and its GUID. Consequently, the process for handling deletes consists of first fetching 
     * existing object by guid and then deleting it by a call to sync service API. Note, if object is not found in DB by its guid, we
     * skip the delete and record warning message. 
     * <p/>
     * preCommitRecordActions collection is provided as a way for the synchronizable instances to 'schedule' action that is necessary
     * for processing of the object yet it cannot be applied until the end of the processing of the parent sync record. For example, rebuild XSN
     * cannot happen until all form fields held in the sync items are applied first; thus the call to rebuild XSN need to happen after all
     * sync items were processed and before committing the sync record.
     *  
     * @param o empty instance of class that this SyncItem represents 
     * @param incoming Serialized state of SyncItem.
     * @param originalGuid Unique id of the object that is stored in SyncItem recorded when this object was first created. NOTE:
     * this value is retained and forwarded unchanged throughout the network of sychronizing servers in order to avoid re-applying
     * same changes over and over.
     * @param preCommitRecordActions collection of actions that will be applied a the end of the processing sync record
     * 
     * @see SyncUtil#setProperty(Object, String, Object)
     * @see SyncUtil#getOpenmrsObj(String, String)
     * @see SyncUtil#updateOpenmrsObject(Object, String, String, boolean)
     */
    private void processSynchronizable(Synchronizable o, SyncItem item, String originalGuid, List<SyncPreCommitAction> preCommitRecordActions) throws Exception {

    	String itemContent = null;
        String className = null;
        boolean alreadyExists = false;
        boolean isDelete = false;
        ArrayList<Field> allFields = null;
        NodeList nodes = null;

        isDelete = (item.getState() == SyncItemState.DELETED) ? true : false; 
        itemContent = item.getContent();
    	className = o.getClass().getName();
        allFields = SyncUtil.getAllFields(o);  // get fields, both in class and superclass - we'll need to know what type each field is
        nodes = SyncUtil.getChildNodes(itemContent);  // get all child nodes (xml) of the root object

	    if ( o == null || className == null || allFields == null || nodes == null ) {
	    	log.warn("Item is missing a className or all fields or nodes");
	    	throw new SyncIngestException(SyncConstants.ERROR_ITEM_NOCLASS, className, itemContent,null);
	    }

	    String guid = SyncUtil.getAttribute(nodes, "guid", allFields);
        Synchronizable objOld = (Synchronizable)SyncUtil.getOpenmrsObj(className, guid);
        if ( objOld != null ) {
            o = objOld;
            alreadyExists = true;
        }
	       
        if (log.isDebugEnabled()) {
	        log.debug("isUpdate: " + alreadyExists);
	        log.debug("isDelete: " + isDelete);
        }
                
        //set the original guid: this will prevent the change from being send back to originating server
        //see SynchronizationServiceImpl.createRecord() which will eventually get called from interceptor when
        //this change in committed
        ((Synchronizable)o).setLastRecordGuid(originalGuid);
        
    	//execute delete if instance was found and operation is delete
        if (alreadyExists && isDelete) {
        	SyncUtil.deleteOpenmrsObject(o);
        }else if (!alreadyExists && isDelete) { 
        	log.warn("Object to be deleted was not found in the database. skipping delete operation:");
        	log.warn("-object type:" + o.getClass().toString());
        	log.warn("-object guid:" + guid);
        } else {
            //if we are doing insert/update:
            //1. set serialized props state
        	//2. force it down the hibernate's throat with help of openmrs api
	        for ( int i = 0; i < nodes.getLength(); i++ ) {
	            try {
	            	log.debug("trying to set property: " + nodes.item(i).getNodeName() + " in className " + className);
	                SyncUtil.setProperty(o, nodes.item(i), allFields);
	            } catch ( Exception e ) {
	            	log.error("Error when trying to set " + nodes.item(i).getNodeName() + ", which is a " + className);
	            	e.printStackTrace();
	                throw new SyncIngestException(SyncConstants.ERROR_ITEM_UNSET_PROPERTY, nodes.item(i).getNodeName() + "," + className, itemContent,null);
	            }
	        }
        	        
	        // now try to commit this fully inflated object
	        try {
	        	log.warn("About to update or create a " + className + " object, guid: " + guid);
	            SyncUtil.updateOpenmrsObject2(o, className, guid,preCommitRecordActions);
	            Context.getSynchronizationService().flushSession();
	        } catch ( Exception e ) {
	        	e.printStackTrace();
	            throw new SyncIngestException(SyncConstants.ERROR_ITEM_NOT_COMMITTED, className, itemContent,null);
	        }
        }
        	                
        return;
    }
}
