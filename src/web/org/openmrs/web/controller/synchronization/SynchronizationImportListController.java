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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.synchronization.SyncConstants;
import org.openmrs.synchronization.SyncTransmissionState;
import org.openmrs.synchronization.SyncUtil;
import org.openmrs.synchronization.SyncUtilTransmission;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.engine.SyncTransmission;
import org.openmrs.synchronization.ingest.SyncDeserializer;
import org.openmrs.synchronization.ingest.SyncImportRecord;
import org.openmrs.synchronization.ingest.SyncTransmissionResponse;
import org.openmrs.synchronization.server.ConnectionRequest;
import org.openmrs.synchronization.server.ConnectionResponse;
import org.openmrs.synchronization.server.RemoteServer;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class SynchronizationImportListController extends SimpleFormController {

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

	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		
		log.info("***********************************************************\n");
		log.info("Inside SynchronizationImportListController");

		// There are 3 ways to come to this point, so we'll handle all of them:
		// 1) uploading a file (results in a file attachment as response)
		// 2) posting data to page (results in pure XML output)
		// 3) remote connection (with username + password, also posting data) (results in pure XML)
		// none of these result in user-friendly - so no comfy, user-friendly stuff needed here
		
		//outputing statistics: debug only!
		log.info("HttpServletRequest INFO:");
		log.info("ContentType: " + request.getContentType());
		log.info("CharacterEncoding: " + request.getCharacterEncoding());
		log.info("ContentLength: " + request.getContentLength());
		log.info("checksum: " + request.getParameter("checksum"));
		log.info("syncData: " + request.getParameter("syncData"));
		log.info("syncDataResponse: " + request.getParameter("syncDataResponse"));
		
    	long checksum = 0;
    	Integer serverId = 0;
		boolean isResponse = false;
    	boolean isUpload = false;
		boolean useCompression = false;

    	String contents = "";
    	String username = "";
        String password = "";
        

        //file-based upload, and multi-part form submission
		if (request instanceof MultipartHttpServletRequest) {
        	log.info("Processing contents of syncDataFile multipart request parameter");
        	MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;        	
    		serverId = ServletRequestUtils.getIntParameter(multipartRequest, "serverId", 0);
    		isResponse = ServletRequestUtils.getBooleanParameter(multipartRequest, "isResponse", false);
    		useCompression = ServletRequestUtils.getBooleanParameter(multipartRequest, "compressed", false);
    		isUpload = ServletRequestUtils.getBooleanParameter(multipartRequest, "upload", false);
			username =  ServletRequestUtils.getStringParameter(multipartRequest, "username", "");
			password =  ServletRequestUtils.getStringParameter(multipartRequest, "password", "");
            
            log.info("Request class: " + request.getClass());
            log.info("serverId: " + serverId);
            log.info("upload = " + isUpload);
            log.info("compressed = " + useCompression);
            log.info("response = " + isResponse);
            log.info("username = " + username);            
            
        	log.info("Request content length: " + request.getContentLength());
			MultipartFile multipartFile = multipartRequest.getFile("syncDataFile");
			if (multipartFile != null && !multipartFile.isEmpty()) {
				InputStream inputStream = null;
				try {
					// Decompress content in file
					ConnectionResponse syncResponse = 
						new ConnectionResponse(new ByteArrayInputStream(multipartFile.getBytes()), useCompression);
					
					log.info("Content to decompress: " + multipartFile.getBytes());
					log.info("Content received: " + syncResponse.getResponsePayload());
					log.info("Decompression Checksum: "+ syncResponse.getChecksum());
					
					
					contents = syncResponse.getResponsePayload();
					checksum = syncResponse.getChecksum();

					log.info("Final content: " + contents);
					
					
					
				} catch (Exception e) {
					log.warn("Unable to read in sync data file", e);
				} finally {
					IOUtils.closeQuietly(inputStream);
				}
			}
		} else {
			log.debug("seems we DO NOT have a file object");
		}

		// prepare to process the input: contents now contains decompressed request ready to be processed
		SyncTransmissionResponse str = new SyncTransmissionResponse();		
    	str.setErrorMessage(SyncConstants.ERROR_TX_NOT_UNDERSTOOD);
    	str.setFileName(SyncConstants.FILENAME_TX_NOT_UNDERSTOOD);
    	str.setGuid(SyncConstants.GUID_UNKNOWN);
        str.setSyncSourceGuid(SyncConstants.GUID_UNKNOWN);
        str.setSyncTargetGuid(SyncConstants.GUID_UNKNOWN);
    	str.setState(SyncTransmissionState.TRANSMISSION_NOT_UNDERSTOOD);        
        str.setTimestamp(new Date()); //set the timestamp of the response

    	if (log.isInfoEnabled()) {
    		log.info("CONTENT IN IMPORT CONTROLLER: " + contents);
    	}
    	

    	//if no content, nothing to process just send back response
		if ( contents == null || contents.length() < 0 ) {
			log.info("returning from ingest: nothing to process.");
			this.sendResponse(str, isUpload, response);
			return null;			
		}
			
		// if this is option 3 (posting from remote server), we need to authenticate
		if ( !Context.isAuthenticated() ) {
			try {
				Context.authenticate(username, password);
			} catch ( Exception e ) {}
		}
		// Could not authenticate user: send back error
		if ( !Context.isAuthenticated() ) {
	    	str.setErrorMessage(SyncConstants.ERROR_AUTH_FAILED);
	    	str.setFileName(SyncConstants.FILENAME_AUTH_FAILED);
	    	str.setState(SyncTransmissionState.AUTH_FAILED);
			
			this.sendResponse(str, isUpload, response);
			return null;
		}
		
        //Fill-in the server guid for the response: since request was authenticated we can start letting callers
		//know about us
        str.setSyncTargetGuid(Context.getSynchronizationService().getServerGuid());

        //Checksum check before doing anything at all: on unreliable networks we can get seemingly
        //valid HTTP POST but content is messed up, defend against it with custom checksums
        long checksumReceived = ServletRequestUtils.getLongParameter(request, "checksum", -1);
    	log.info("checksum value received in POST: " + checksumReceived );
    	log.info("checksum value of payload: " + checksum);
    	log.info("SIZE of payload: " + contents.length());
        if (checksumReceived > 0 && (checksumReceived != checksum)) {
        	log.error("ERROR: FAILED CHECKSUM!");
        	str.setState(SyncTransmissionState.TRANSMISSION_NOT_UNDERSTOOD);

        	this.sendResponse(str, isUpload, response);
        	return null;	            
        }
                     
        //Test message. Test message was sent (i.e. using 'test connection' button on server screen)
    	//just send empty ackowledgment
        if ( SyncConstants.TEST_MESSAGE.equals(contents) ) {
			str.setErrorMessage("");
			str.setState(SyncTransmissionState.OK);
			str.setGuid("");
	    	str.setFileName(SyncConstants.FILENAME_TEST);

	    	this.sendResponse(str, isUpload, response);
        	return null;	            
		}

        /*************************************************************************************************************************
         * This is a real transmission: 
         * - user was properly authenticated
         * - checksums match
         * - it is not a test transmission
         * 
         * Start processing!
         * 1. Deserialize what was sent; it can be either SyncTransmssion, or SyncTransmissionResponse
         * 2. If it is a respose,  
         *************************************************************************************************************************/
        SyncTransmission st = null;

        if ( !isResponse ) {
        	//this is not 'response' to something we sent out; thus the contents should contain plan SyncTransmission 
        	try {
            st = SyncDeserializer.xmlToSyncTransmission(contents);
        	} catch ( Exception e ) {
        		log.error("Unable to deserialize the following: " + contents, e);
    			str.setErrorMessage("Unable to deserialize transmission contents into SyncTansmission.");
    			str.setState(SyncTransmissionState.TRANSMISSION_NOT_UNDERSTOOD);
    	    	this.sendResponse(str, isUpload, response);
    	    	return null;
        	}
        } else {
            log.info("Processing a response, not a transmission");
            SyncTransmissionResponse priorResponse = null;
                
            try {
                priorResponse = SyncDeserializer.xmlToSyncTransmissionResponse(contents);
                log.info("This is a response from a previous transmission.  Guid is: " + priorResponse.getGuid());
            } catch ( Exception e ) {
                log.error("Unable to deserialize the following: " + contents, e);
                str.setErrorMessage("Unable to deserialize transmission contents into SyncTransmissionResponse.");
                str.setState(SyncTransmissionState.TRANSMISSION_NOT_UNDERSTOOD);
                this.sendResponse(str, isUpload, response);
    	    	return null;
            }
                
            //figure out where this came from:
            //for responses, the target ID contains the server that generated the response
            String sourceGuid = priorResponse.getSyncTargetGuid();
            log.info("SyncTransmissionResponse has a sourceGuid of " + sourceGuid);
            RemoteServer origin = Context.getSynchronizationService().getRemoteServer(sourceGuid);
            if ( origin == null ) {
            	log.error("Source server not registered locally. Unable to find source server by guid: " + sourceGuid);
                str.setErrorMessage("Source server not registered locally. Unable to find source server by guid " + sourceGuid);
                str.setState(SyncTransmissionState.INVALID_SERVER);
                this.sendResponse(str, isUpload, response);            	
            	return null;
            } else {
            	log.info("Found source server by guid: " + sourceGuid + " = " + origin.getNickname());
            	log.info("Source server is " + origin.getNickname());
            }
                                        
            if (priorResponse == null) {
            }
            
            // process response that was sent to us; the sync response normally contains:
            //a) results of the records that we sent out
            //b) new records from 'source' to be applied against this server
            if ( priorResponse.getSyncImportRecords() == null ) {
                log.debug("No records to process in response");
            } else {
                // now process each incoming syncImportRecord, this is just status update
                for ( SyncImportRecord importRecord : priorResponse.getSyncImportRecords() ) {
                    Context.getSynchronizationIngestService().processSyncImportRecord(importRecord, origin);
                }
            }
            
            // now set pull out the data that originated on the 'source' server and try to process it
            st = priorResponse.getSyncTransmission();

        }
                        
        // now process the syncTransmission if one was received                    
        if ( st != null ) {
            str = SyncUtilTransmission.processSyncTransmission(st);
        }
        
        //send response
		this.sendResponse(str, isUpload, response);
		
        // never a situation where we want to actually use the model/view - either file download or http request
        return null;
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
    	String ret = "";
    	
        return ret;
    }
    
    private void sendResponse(SyncTransmissionResponse str, boolean isUpload, HttpServletResponse response) throws Exception {
    	String content = null;
		try {
			str.createFile(false);
			content = str.getFileOutput();
		} catch ( Exception e ) {
			log.error("Could not get output while writing file.  In case problem writing file, trying again to just get output.");
		}
		
		if ( content.length() == 0 ) {
			try {
				str.createFile(false);
				content = str.getFileOutput();
			} catch ( Exception e ) {
				log.error("Could not get output while writing file.  In case problem writing file, trying again to just get output.");
			}
		}
	
        // If the file was uploaded manually, we'll send back an XML response
		if (isUpload) {			
			response.setHeader("Content-Disposition", "attachment; filename=" + str.getFileName() + ".xml");
	        InputStream in = new ByteArrayInputStream(content.getBytes());
	        IOUtils.copy(in, response.getOutputStream());
	        return;
		}

		// We're sending back a new sync transmission (an update).
		// We need to check the local server about whether we should apply compression.
		boolean useCompression = 
			Boolean.parseBoolean(Context.getAdministrationService().getGlobalProperty(SyncConstants.PROPERTY_ENABLE_COMPRESSION, "true"));
		log.debug("Global property sychronization.enable_compression = " + useCompression);

		// Otherwise, all other requests are compressed and sent back to the client 
		ConnectionRequest syncRequest = new ConnectionRequest(content, useCompression);				
        log.info("Compressed content length: " + syncRequest.getContentLength());
        log.info("Compression Checksum: "+ syncRequest.getChecksum());    
        log.info("Full Content to send: " + content);
        
        response.setContentLength((int)syncRequest.getContentLength());
        response.addHeader("Enable-Compression", String.valueOf(useCompression));
        response.addHeader("Content-Checksum", String.valueOf(syncRequest.getChecksum()));
        response.addHeader("Content-Encoding", "gzip");
        
        // Write compressed sync data to response
        InputStream in = new ByteArrayInputStream(syncRequest.getBytes());
        IOUtils.copy(in, response.getOutputStream());

        return;	        	
    	
    }
}
