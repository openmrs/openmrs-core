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
		System.out.println("HttpServletRequest INFO:");
		System.out.println("ContentType: " + request.getContentType());
		System.out.println("CharacterEncoding: " + request.getCharacterEncoding());
		System.out.println("ContentLength: " + request.getContentLength());
		System.out.println("checksum: " + request.getParameter("checksum"));
		System.out.println("syncData: " + request.getParameter("syncData"));
		System.out.println("syncDataResponse: " + request.getParameter("syncDataResponse"));

		// All requests should be multipart requests
		
    	long checksum = 0;
    	Integer serverId = 0;
		boolean isResponse = false;
    	boolean isUpload = false;
		boolean useCompression = false;

    	String contents = "";
    	String username = "";
        String password = "";
        

        //file-based upload and form submission
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
            log.info("password = " + password);
            
            
            
            
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

		// prepare to process the input
		SyncTransmissionResponse str = new SyncTransmissionResponse();		
    	str.setErrorMessage(SyncConstants.ERROR_TX_NOT_UNDERSTOOD);
    	str.setFileName(SyncConstants.FILENAME_TX_NOT_UNDERSTOOD);
    	str.setGuid(SyncConstants.GUID_UNKNOWN);
        str.setSyncSourceGuid(SyncConstants.GUID_UNKNOWN);
        str.setSyncTargetGuid(SyncConstants.GUID_UNKNOWN);
    	str.setState(SyncTransmissionState.TRANSMISSION_NOT_UNDERSTOOD);        
        str.setTimestamp(new Date()); //set the timestamp of the response

    	System.out.println("CONTENT IN IMPORT CONTROLLER: " + contents);
    	
		if ( contents.length() > 0 ) {
			
			// if this is option 3 (posting from remote server), we need to authenticate
			if ( !Context.isAuthenticated() ) {
				try {
					Context.authenticate(username, password);
				} catch ( Exception e ) {
					// nothing to do - we'll have to respond saying no authentication
					//TODO - clean this up
				}
			}

			if ( Context.isAuthenticated() ) {

                //fill-in the server guid for the response
                str.setSyncTargetGuid(Context.getSynchronizationService().getServerGuid());

	        	// TODO Will deal with checksum earlier when we first get the sync transmission
                //checksum check before doing anything at all
                long checksumReceived = ServletRequestUtils.getLongParameter(request, "checksum", -1);
	        	log.info("checksum value received in POST: " + checksumReceived );
	        	log.info("checksum value of payload: " + checksum);

	        	System.out.println("SIZE of payload: " + contents.length());
                if (checksumReceived > 0 && (checksumReceived != checksum)) {
    	        	log.error("ERROR: FAILED CHECKSUM!");
    	        	str.setState(SyncTransmissionState.TRANSMISSION_NOT_UNDERSTOOD);
    	        	this.sendResponse(str, isUpload, response);
    	        	return null;	            
                }
                
                                
                if ( SyncConstants.TEST_MESSAGE.equals(contents) ) {
					str.setErrorMessage("");
					str.setState(SyncTransmissionState.OK);
					str.setGuid("");
			    	str.setFileName(SyncConstants.FILENAME_TEST);

				} else {
                    SyncTransmission st = null;

                    if ( isResponse ) {
                        log.warn("UNDERSTOOD THAT THIS IS A RESPONSE");
                        SyncTransmissionResponse priorResponse = null;
                        
                        try {
                            priorResponse = SyncDeserializer.xmlToSyncTransmissionResponse(contents);
                            log.warn("WE SEEM TO HAVE GOTTEN THE PRIOR RESPONSE: " + priorResponse.getGuid());
                        } catch ( Exception e ) {
                            log.error("Unable to deserialize the following: " + contents);
                            e.printStackTrace();
                        }
                        
                        //figure out where this came from
                        //for responses, the target ID contains the server that generated the response
                        String sourceGuid = priorResponse.getSyncTargetGuid();
                        log.warn("Getting sourceGuid of " + sourceGuid);
                        RemoteServer origin = Context.getSynchronizationService().getRemoteServer(sourceGuid);
                        if ( origin == null ) log.warn("NOT ABLE TO GET ORIGIN SERVER BY SOURCEGUID");
                        else log.warn("EASILY ABLE TO GET ORIGIN SERVER BY SOURCEGUID: " + sourceGuid + " = " + origin.getNickname());
                        
                        // if that didn't do it, we should be able to get by serverId, if this is a file-based upload
                        if ( origin == null && serverId > 0 ) {
                            // make a last-ditch effort to try to figure out what server this is coming from, so we can behave appropriately.
                            log.warn("CANNOT GET ORIGIN SERVER FOR THIS REQUEST, get by serverId " +  serverId);
                            origin = Context.getSynchronizationService().getRemoteServer(serverId);
                            if ( origin != null && sourceGuid != null && sourceGuid.length() > 0 ) {
                                // take this opportunity to save the guid, now we've identified which server this is
                                origin.setGuid(sourceGuid);
                                Context.getSynchronizationService().updateRemoteServer(origin);
                            } else {
                                log.warn("STILL UNABLE TO GET ORIGIN WITH username " + username + " and sourceguid " + sourceGuid);
                            }
                        } else {
                            if ( origin == null ) log.warn("ORIGIN SERVER IS STILL NULL AFTER 2 ATTEMPTS");
                            else log.warn("ORIGIN SERVER IS " + origin.getNickname());
                        }

                        if ( origin == null ) {
                            // make a last-ditch effort to try to figure out what server this is coming from, so we can behave appropriately.
                            User authenticatedUser = Context.getAuthenticatedUser();
                            if ( authenticatedUser != null ) {
                                username = authenticatedUser.getUsername();
                                log.warn("CANNOT GET ORIGIN SERVER FOR THIS REQUEST, get by username " + username + " instead");
                                origin = Context.getSynchronizationService().getRemoteServerByUsername(username);
                                if ( origin != null && sourceGuid != null && sourceGuid.length() > 0 ) {
                                    // take this opportunity to save the guid, now we've identified which server this is
                                    origin.setGuid(sourceGuid);
                                    Context.getSynchronizationService().updateRemoteServer(origin);
                                } else {
                                    log.warn("STILL UNABLE TO GET ORIGIN WITH username " + username + " and sourceguid " + sourceGuid);
                                }
                            }
                        } else {
                            log.warn("ORIGIN SERVER IS " + origin.getNickname());
                        }
                        
                        if ( priorResponse != null ) {
                            // process response
                            if ( priorResponse.getSyncImportRecords() == null ) {
                                log.debug("No records to process in response");
                            } else {
                                // process each incoming syncImportRecord
                                for ( SyncImportRecord importRecord : priorResponse.getSyncImportRecords() ) {
                                    Context.getSynchronizationIngestService().processSyncImportRecord(importRecord, origin);
                                }
                            }
                            
                            // now set the syncTransmission
                            st = priorResponse.getSyncTransmission();
                        } else {
                            // don't need to do anything because the default Error values capture it all
                        }

                    } else {
                        try {
                            st = SyncDeserializer.xmlToSyncTransmission(contents);
                        } catch ( Exception e ) {
                            log.error("Unable to deserialize the following: " + contents);
                            e.printStackTrace();
                        }
                    }

                    // now process the syncTransmission                    
                    if ( st != null ) {
                        str = SyncUtilTransmission.processSyncTransmission(st);
                    } else {
                        // don't need to do anything because the default Error values capture it all
                    }
				}

			} 
			// Could not authenticate user
			else {
		    	str.setErrorMessage(SyncConstants.ERROR_AUTH_FAILED);
		    	str.setFileName(SyncConstants.FILENAME_AUTH_FAILED);
		    	str.setGuid(SyncConstants.GUID_UNKNOWN);
                str.setSyncSourceGuid(SyncConstants.GUID_UNKNOWN);
		    	str.setState(SyncTransmissionState.AUTH_FAILED);
			}
		}
		
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
			str.createFile(true);
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
		
		System.out.println("RESPONSE IS: " + content);

	
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
			Boolean.parseBoolean(Context.getAdministrationService().getGlobalProperty(SyncConstants.PROPERTY_ENABLE_COMPRESSION, "false"));
		log.info("Global property sychronization.enable_compression = " + useCompression);

		// Otherwise, all other requests are compressed and sent back to the client 
		ConnectionRequest syncRequest = new ConnectionRequest(content, useCompression);				
        log.info("Content to send: " + content);
        log.info("Compressed content: " + syncRequest.getBytes());
        log.info("Compression Checksum: "+ syncRequest.getChecksum());    

        
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
