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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.synchronization.SyncConstants;
import org.openmrs.synchronization.SyncTransmissionState;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.engine.SyncTransmission;
import org.openmrs.synchronization.ingest.SyncDeserializer;
import org.openmrs.synchronization.ingest.SyncImportRecord;
import org.openmrs.synchronization.ingest.SyncRecordIngest;
import org.openmrs.synchronization.ingest.SyncTransmissionResponse;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
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
		
		log.debug("in onSubmit method");

		// There are 3 ways to come to this point, so we'll handle all of them:
		// 1) uploading a file (results in a file attachment as response)
		// 2) posting data to page (results in pure XML output)
		// 3) remote connection (with username + password, also posting data) (results in pure XML)
		// none of these result in user-friendly - so no comfy, user-friendly stuff needed here
		
		boolean isUpload = ServletRequestUtils.getBooleanParameter(request, "upload", false);
		String contents = ServletRequestUtils.getStringParameter(request, "syncData", "");

		if (isUpload && request instanceof MultipartHttpServletRequest) {
			log.debug("Seems we actually have a file object");
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
			MultipartFile multipartSyncFile = multipartRequest.getFile("syncDataFile");
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
					log.warn("Unable to read in sync data file", e);
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
		} else {
			log.debug("seems we DO NOT have a file object");
		}

		// prepare to process the input
		String ret = "";
		SyncTransmissionResponse str = new SyncTransmissionResponse();
		
    	str.setErrorMessage(SyncConstants.ERROR_TX_NOT_UNDERSTOOD);
    	str.setFileName(SyncConstants.FILENAME_TX_NOT_UNDERSTOOD);
    	str.setGuid(SyncConstants.GUID_UNKNOWN);
        str.setSyncSourceGuid(SyncConstants.GUID_UNKNOWN);
        str.setSyncParentGuid(SyncConstants.GUID_UNKNOWN);
    	str.setState(SyncTransmissionState.TRANSMISSION_NOT_UNDERSTOOD);
        
        str.setTimestamp(new Date()); //set the timestamp of the response

    	System.out.println("CONTENT IN IMPORT CONTROLLER: " + contents);
    	
		if ( contents.length() > 0 ) {
			
			// if this is option 3 (posting from remote server), we need to authenticate
			if ( !Context.isAuthenticated() ) {
				String username = ServletRequestUtils.getStringParameter(request, "username", "");
				String password = ServletRequestUtils.getStringParameter(request, "password", "");
				
				try {
					Context.authenticate(username, password);
				} catch ( Exception e ) {
					// nothing to do - we'll have to respond saying no authentication
				}
			}

			if ( Context.isAuthenticated() ) {
			    
                //fill-in the server guid for the response first
                str.setSyncParentGuid(SyncRecordIngest.getSyncParentGuid());
                
				if ( SyncConstants.TEST_MESSAGE.equals(contents) ) {
					str.setErrorMessage("");
					str.setState(SyncTransmissionState.OK);
					str.setGuid("");
			    	str.setFileName(SyncConstants.FILENAME_TEST);
				} else {
					SyncTransmission st = null;
					
					try {
						st = SyncDeserializer.xmlToSyncTransmission(contents);
					} catch ( Exception e ) {
						log.error("Unable to deserialize the following: " + contents);
						e.printStackTrace();
					}
					
					if ( st != null ) {
						// the constructor for SyncTransmissionResponse is null-safe
						str = new SyncTransmissionResponse(st);
						
						List<SyncImportRecord> importRecords = new ArrayList<SyncImportRecord>();
						
						if ( st.getSyncRecords() != null ) {
							for ( SyncRecord record : st.getSyncRecords() ) {
								//SyncImportRecord importRecord = SyncRecordIngest.processSyncRecord(record);
                                SyncImportRecord importRecord = Context.getSynchronizationIngestService().ProcessSyncRecord(record);
								importRecords.add(importRecord);
							}
						}
						if ( importRecords.size() > 0 ) str.setSyncImportRecords(importRecords);

					} else {
						// don't need to do anything because the default Error values capture it all
					}
				}

			} else {
		    	str.setErrorMessage(SyncConstants.ERROR_AUTH_FAILED);
		    	str.setFileName(SyncConstants.FILENAME_AUTH_FAILED);
		    	str.setGuid(SyncConstants.GUID_UNKNOWN);
                str.setSyncSourceGuid(SyncConstants.GUID_UNKNOWN);
		    	str.setState(SyncTransmissionState.AUTH_FAILED);
			}
		}

		try {
			str.CreateFile(true);
			ret = str.getFileOutput();
		} catch ( Exception e ) {
			log.debug("Could not get output while writing file.  In case problem writing file, trying again to just get output.");
		}
		
		if ( ret.length() == 0 ) {
			try {
				str.CreateFile(false);
				ret = str.getFileOutput();
			} catch ( Exception e ) {
				log.error("Could not get output while writing file.  In case problem writing file, trying again to just get output.");
			}
		}
		
		System.out.println("RESPONSE IS: " + ret);

		if ( isUpload ) {
            response.setHeader("Content-Disposition", "attachment; filename=" + str.getFileName() + ".xml");
		}
		InputStream in = new ByteArrayInputStream(ret.getBytes());
		response.setContentType("text/xml; charset=utf-8");
        OutputStream out = response.getOutputStream();
        IOUtils.copy(in, out);
        out.flush();
        out.close();

        // never a situation where we want to actually use the model/view - either file download or XML
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
}