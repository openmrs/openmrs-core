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
package org.openmrs.web.controller.synchronization;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.Item;
import org.openmrs.serialization.Record;
import org.openmrs.serialization.TimestampNormalizer;
import org.openmrs.synchronization.SyncConstants;
import org.openmrs.synchronization.SyncRecordState;
import org.openmrs.synchronization.SyncStatusState;
import org.openmrs.synchronization.SyncTransmissionState;
import org.openmrs.synchronization.SyncUtil;
import org.openmrs.synchronization.SyncUtilTransmission;
import org.openmrs.synchronization.engine.SyncItem;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.engine.SyncSource;
import org.openmrs.synchronization.engine.SyncSourceJournal;
import org.openmrs.synchronization.engine.SyncTransmission;
import org.openmrs.synchronization.ingest.SyncDeserializer;
import org.openmrs.synchronization.ingest.SyncImportRecord;
import org.openmrs.synchronization.ingest.SyncTransmissionResponse;
import org.openmrs.synchronization.server.RemoteServer;
import org.openmrs.web.WebConstants;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SynchronizationStatusListController extends SimpleFormController {

    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    /**
     * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest,
     *      org.springframework.web.bind.ServletRequestDataBinder)
     */
    protected void initBinder(HttpServletRequest request,
            ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
    }

    /**
     * 
     * The onSubmit function receives the form/command object that was modified
     * by the input form and saves it to the db
     * 
     * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse, java.lang.Object,
     *      org.springframework.validation.BindException)
     */
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {

    	ModelAndView result = new ModelAndView(new RedirectView(getSuccessView()));
    	
        // TODO - replace with privilege check
        if (!Context.isAuthenticated()) throw new APIAuthenticationException("Not authenticated!");
        
        HttpSession httpSession = request.getSession();
        String success = "";
        String error = "";
        MessageSourceAccessor msa = getMessageSourceAccessor();
        
        String action = ServletRequestUtils.getStringParameter(request, "action", "");
        SyncStatusState syncStatus = SyncUtil.getSyncStatus();
        
        // handle transmission generation
        if ( syncStatus.equals(SyncStatusState.ENABLED_CONTINUE_ON_ERROR) || syncStatus.equals(SyncStatusState.ENABLED_STRICT) ) {
        	
	        if ("createTx".equals(action)) {            	
	            try {
	            	// we are creating a sync-transmission, so start by generating a SyncTransmission object
	            	SyncTransmission tx = SyncUtilTransmission.createSyncTransmission();
	                String toTransmit = tx.getFileOutput();
	
	                // Record last attempt
	                RemoteServer parent = Context.getSynchronizationService().getParentServer();
	                parent.setLastSync(new Date());
	                Context.getSynchronizationService().updateRemoteServer(parent);
	                
	                // Write sync transmission to response
	                InputStream in = new ByteArrayInputStream(toTransmit.getBytes());
	                response.setContentType("text/xml; charset=utf-8");
	                response.setHeader("Content-Disposition", "attachment; filename=" + tx.getFileName() + ".xml");
	                OutputStream out = response.getOutputStream();
	                IOUtils.copy(in, out);
	                out.flush();
	                out.close();
	
	                // don't return a model/view - we'll need to return a file instead.
	                result = null;
	            } catch(Exception e) {
	                e.printStackTrace();
	                error = msa.getMessage("SynchronizationStatus.createTx.error");  
	            }
	        } else if ( "uploadResponse".equals(action) && request instanceof MultipartHttpServletRequest) {
	
	        	try {
	            	String contents = "";
	                RemoteServer parent = Context.getSynchronizationService().getParentServer();
	
	            	// first, get contents of file that is being uploaded.  it is clear we are uploading a response from parent at this point
	            	MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
	    			MultipartFile multipartSyncFile = multipartRequest.getFile("syncResponseFile");
	    			if (multipartSyncFile != null && !multipartSyncFile.isEmpty()) {
	    				InputStream inputStream = null;
	
	    				try {
	    					inputStream = multipartSyncFile.getInputStream();
	    					BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
	    					String line = "";
	    					while ((line = in.readLine()) != null) {
	    						contents += line;
	    					}
	    				} catch (Exception e) {
	                        e.printStackTrace();
	    					log.warn("Unable to read in sync data file", e);
	    					error = e.getMessage();
	    				} finally {
	    					try {
	    						if (inputStream != null)
	    							inputStream.close();
	    					}
	    					catch (IOException io) {
	    						log.warn("Unable to close temporary input stream", io);
	    					}
	    				}
	    			}
	
	                if ( contents.length() > 0 ) {
	        			SyncTransmissionResponse str = SyncDeserializer.xmlToSyncTransmissionResponse(contents);
	        			
	        			int numCommitted = 0;
	        			int numAlreadyCommitted = 0;
	        			int numFailed = 0;
	        			int numOther = 0;
	        			
	        			if ( str.getSyncImportRecords() == null ) log.debug("No records to process in response");
	        			else {
	        				// process each incoming syncImportRecord
	        				for ( SyncImportRecord importRecord : str.getSyncImportRecords() ) {
	        					Context.getSynchronizationIngestService().processSyncImportRecord(importRecord, parent);
	                            // get some numbers to show user the results
	        					if ( importRecord.getState().equals(SyncRecordState.COMMITTED )) numCommitted++;
	        					else if ( importRecord.getState().equals(SyncRecordState.ALREADY_COMMITTED )) numAlreadyCommitted++;
	        					else if ( importRecord.getState().equals(SyncRecordState.FAILED )) numFailed++;
	        					else numOther++;
	        				}
	        			}
	        			
	        			try {
	        				// store this file on filesystem too
	        				str.createFile(true, SyncConstants.DIR_JOURNAL);
	        			} catch ( Exception e ) {
	        				log.error("Unable to create file to store SyncTransmissionResponse: " + str.getFileName());
	        				e.printStackTrace();
	        			}
	        			
	        			Object[] args = {numCommitted,numFailed,numAlreadyCommitted,numOther};
	        				
	        			success = msa.getMessage("SynchronizationStatus.uploadResponse.success", args);
	        		} else {
	        			error = msa.getMessage("SynchronizationStatus.uploadResponse.fileEmpty");
	        		}
	            } catch(Exception e) {
	                e.printStackTrace();
	                error = msa.getMessage("SynchronizationStatus.uploadResponse.error");  
	            }
	        }
        } else {
        	// this means sync isn't enabled - show appropriate message
            error = msa.getMessage("SynchronizationStatus.sync.disabled");  
        }
        
        
        if (!success.equals(""))
            httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success);
        
        if (!error.equals(""))
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error);
		
		return result;
	}

    /**
     * 
     * This is called prior to displaying a form for the first time. It tells
     * Spring the form/command object to load into the request
     * 
     * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
     */
    protected Object formBackingObject(HttpServletRequest request)
            throws ServletException {
        // default empty Object
        List<SyncRecord> recordList = new ArrayList<SyncRecord>();

        // only fill the Object if the user has authenticated properly
        if (Context.isAuthenticated()) {
            RemoteServer parent = Context.getSynchronizationService().getParentServer();
            if ( parent != null ) {
                SyncSource source = new SyncSourceJournal();
                recordList = source.getChanged(parent);
            }
        	
        	//SynchronizationService ss = Context.getSynchronizationService();
            //recordList.addAll(ss.getSyncRecords());
        }

        return recordList;
    }

	@Override
    protected Map referenceData(HttpServletRequest request, Object obj, Errors errors) throws Exception {
		Map<String,Object> ret = new HashMap<String,Object>();
		
		Map<String,String> recordTypes = new HashMap<String,String>();
		Map<Object,String> itemTypes = new HashMap<Object,String>();
		Map<Object,String> itemGuids = new HashMap<Object,String>();
		//Map<String,String> itemInfo = new HashMap<String,String>();
		//Map<String,String> itemInfoKeys = new HashMap<String,String>();
		Map<String,String> recordText = new HashMap<String,String>();
        Map<String,String> recordChangeType = new HashMap<String,String>();
        List<SyncRecord> recordList = (ArrayList<SyncRecord>)obj;
        SyncStatusState syncState = SyncUtil.getSyncStatus();

        //itemInfoKeys.put("Patient", "gender,birthdate");
        //itemInfoKeys.put("PersonName", "name");
        //itemInfoKeys.put("User", "username");
        
        for ( SyncRecord record : recordList ) {
            
            String mainClassName = null;
            String mainGuid = null;
            String mainState = null;
            
			for ( SyncItem item : record.getItems() ) {
				String syncItem = item.getContent();
                mainState = item.getState().toString();
				Record xml = Record.create(syncItem);
				Item root = xml.getRootItem();
				String className = root.getNode().getNodeName().substring("org.openmrs.".length());
				itemTypes.put(item.getKey().getKeyValue(), className);
				if ( mainClassName == null ) mainClassName = className;
                
				//String itemInfoKey = itemInfoKeys.get(className);
				
				// now we have to go through the item child nodes to find the real GUID that we want
				NodeList nodes = root.getNode().getChildNodes();
				for ( int i = 0; i < nodes.getLength(); i++ ) {
					Node n = nodes.item(i);
					String propName = n.getNodeName();
					if ( propName.equalsIgnoreCase("guid") ) {
                        String guid = n.getTextContent();
						itemGuids.put(item.getKey().getKeyValue(), guid);
                        if ( mainGuid == null ) mainGuid = guid;
                    }
				}
			}

			// persistent sets should show something other than their mainClassName (persistedSet)
			if ( mainClassName.indexOf("Persistent") >= 0 || mainClassName.indexOf("Tree") >= 0 ) mainClassName = record.getContainedClasses();
			
            recordTypes.put(record.getGuid(), mainClassName);
            recordChangeType.put(record.getGuid(), mainState);

            // refactored - CA 21 Jan 2008
            String displayName = "";
            try {
                displayName = SyncUtil.displayName(mainClassName, mainGuid);
            } catch ( Exception e ) {
            	// some methods like Concept.getName() throw Exception s all the time...
            	displayName = "";
            }
            if ( displayName != null ) if ( displayName.length() > 0 ) recordText.put(record.getGuid(), displayName);

        }
        
        // syncViaWeb error messages
        MessageSourceAccessor msa = getMessageSourceAccessor();
        Map<String,String> state = new HashMap<String,String>();
        state.put(SyncTransmissionState.AUTH_FAILED.toString(), msa.getMessage("SynchronizationStatus.transmission.noAuthError"));
        state.put(SyncTransmissionState.CERTIFICATE_FAILED.toString(), msa.getMessage("SynchronizationStatus.transmission.noCertError"));
        state.put(SyncTransmissionState.CONNECTION_FAILED.toString(), msa.getMessage("SynchronizationStatus.transmission.noConnectionError"));
        state.put(SyncTransmissionState.MALFORMED_URL.toString(), msa.getMessage("SynchronizationStatus.transmission.badUrl"));
        state.put(SyncTransmissionState.NO_PARENT_DEFINED.toString(), msa.getMessage("SynchronizationStatus.transmission.noParentError"));
        state.put(SyncTransmissionState.RESPONSE_NOT_UNDERSTOOD.toString(), msa.getMessage("SynchronizationStatus.transmission.corruptResponseError"));
        state.put(SyncTransmissionState.SEND_FAILED.toString(), msa.getMessage("SynchronizationStatus.transmission.sendError"));
        state.put(SyncTransmissionState.TRANSMISSION_CREATION_FAILED.toString(), msa.getMessage("SynchronizationStatus.transmission.createError"));
        state.put(SyncTransmissionState.TRANSMISSION_NOT_UNDERSTOOD.toString(), msa.getMessage("SynchronizationStatus.transmission.corruptTxError"));
        state.put(SyncTransmissionState.OK_NOTHING_TO_DO.toString(), msa.getMessage("SynchronizationStatus.transmission.okNoSyncNeeded"));
        state.put(SyncTransmissionState.MAX_RETRY_REACHED.toString(), msa.getMessage("SynchronizationStatus.transmission.maxRetryReached"));
        state.put(SyncRecordState.ALREADY_COMMITTED.toString(), msa.getMessage("Synchronization.record.state_ALREADY_COMMITTED"));
        state.put(SyncRecordState.COMMITTED.toString(), msa.getMessage("Synchronization.record.state_COMMITTED"));
        state.put(SyncRecordState.FAILED.toString(), msa.getMessage("Synchronization.record.state_FAILED"));
        state.put(SyncRecordState.FAILED_AND_STOPPED.toString(), msa.getMessage("Synchronization.record.state_FAILED_AND_STOPPED"));
        state.put(SyncRecordState.NEW.toString(), msa.getMessage("Synchronization.record.state_SENT"));
        state.put(SyncRecordState.PENDING_SEND.toString(), msa.getMessage("Synchronization.record.state_SENT"));
        state.put(SyncRecordState.SEND_FAILED.toString(), msa.getMessage("Synchronization.record.state_FAILED"));
        state.put(SyncRecordState.SENT.toString(), msa.getMessage("Synchronization.record.state_SENT"));
        state.put(SyncRecordState.SENT_AGAIN.toString(), msa.getMessage("Synchronization.record.state_SENT"));
        
        ret.put("mode", ServletRequestUtils.getStringParameter(request, "mode", "SEND_FILE"));
        ret.put("transmissionState", state.entrySet());

        
        ret.put("recordTypes", recordTypes);
        ret.put("itemTypes", itemTypes);
        ret.put("itemGuids", itemGuids);
        //ret.put("itemInfo", itemInfo);
        ret.put("recordText", recordText);
        ret.put("recordChangeType", recordChangeType);
        ret.put("parent", Context.getSynchronizationService().getParentServer());
        ret.put("syncDateDisplayFormat", TimestampNormalizer.DATETIME_DISPLAY_FORMAT);
        ret.put("syncState", syncState);
        
	    return ret;
    }

}