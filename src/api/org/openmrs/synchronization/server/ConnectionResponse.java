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
package org.openmrs.synchronization.server;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.synchronization.SyncException;

/**
 * 
 */
public class ConnectionResponse {
	
	private static final Log log = LogFactory.getLog(ConnectionResponse.class);

	private ServerConnectionState state;
	private String errorMessage;
	private long checksum;
	private String responsePayload;
	private CheckedInputStream cis;
	private boolean useCompression;
	
	public ConnectionResponse() { 
		this.setErrorMessage("");
		this.setResponsePayload("");
		this.setState(ServerConnectionState.CONNECTION_FAILED);		
	}
	
	/**
	 * 
	 * @param inputStream
	 * @throws SyncException
	 */
	public ConnectionResponse(InputStream is, boolean useCompression) throws SyncException {
		try { 
			this.useCompression = useCompression;
			this.cis = new CheckedInputStream(is, new CRC32());

			if (this.useCompression) { 
		        GZIPInputStream zis = new GZIPInputStream(new BufferedInputStream(cis)); 	        
		        this.responsePayload = IOUtils.toString(zis);
		        IOUtils.closeQuietly(zis);
				IOUtils.closeQuietly(cis);
		        log.info("**********************  CHECKSUM: " + cis.getChecksum().getValue() );
		        this.checksum = cis.getChecksum().getValue();			
			} 
			else { 
				this.responsePayload = IOUtils.toString(cis);				
			}
			
			log.info("Response compressed: " + useCompression);
			//log.info("Response input: " + is.toString());
			//log.info("Response data: " + this.responsePayload);
			log.info("Response checksum: " + this.checksum);

	        this.setState(ServerConnectionState.OK);
	        
		} catch (IOException e) { 
			//throw new SyncException(e);
			log.error("An error occurred while unzipping response", e);
		}
		
	}
	
	public long getChecksum() { 
		return checksum;		
	}
	
	public String getErrorMessage() {
    	return errorMessage;
    }

	public void setErrorMessage(String errorMessage) {
    	this.errorMessage = errorMessage;
    }

	public String getResponsePayload() {
    	return responsePayload;
    }
	
	public void setResponsePayload(String responsePayload) {
    	this.responsePayload = responsePayload;
    }
	
	public ServerConnectionState getState() {
    	return state;
    }
	
	public void setState(ServerConnectionState state) {
    	this.state = state;
    }
}
