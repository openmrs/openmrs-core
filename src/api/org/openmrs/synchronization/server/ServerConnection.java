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
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.synchronization.SyncConstants;
import org.openmrs.synchronization.SyncTransmissionState;
import org.openmrs.synchronization.SyncUtil;

/**
 * 
 */
public class ServerConnection {

	private static final Log log = LogFactory.getLog(ServerConnection.class);

	public static ConnectionResponse test(String address, String username,
			String password) {
		return sendExportedData(address,
		                        username,
		                        password,
		                        SyncConstants.TEST_MESSAGE);
	}

	public static ConnectionResponse sendExportedData(RemoteServer server,
			String message) {
		return sendExportedData(server.getAddress(),
		                        server.getUsername(),
		                        server.getPassword(),
		                        message,
		                        false);
	}

	public static ConnectionResponse sendExportedData(RemoteServer server,
			String message, boolean isResponse) {
		return sendExportedData(server.getAddress(),
		                        server.getUsername(),
		                        server.getPassword(),
		                        message,
		                        isResponse);
	}

	public static ConnectionResponse sendExportedData(String address,
			String username, String password, String message) {
		return sendExportedData(address, username, password, message, false);
	}




	public static ConnectionResponse sendExportedData(String url, String username, String password, String content, boolean isResponse) {

		// Default response - default constructor institantiates contains error codes 
		ConnectionResponse syncResponse = new ConnectionResponse();
		
		
		PostMethod method = new PostMethod(url);
		
		try {
			
			
			boolean useCompression = 
				Boolean.parseBoolean(Context.getAdministrationService().getGlobalProperty(SyncConstants.PROPERTY_ENABLE_COMPRESSION, "false"));
			
			log.info("use compression: " + useCompression);
			// Compress content
			ConnectionRequest request = new ConnectionRequest(content, useCompression);

			// Create up multipart request
			Part[] parts = {
					new FilePart("syncDataFile", new ByteArrayPartSource("syncDataFile", request.getBytes())),
					new StringPart("username", username),				
					new StringPart("password", password),				
					new StringPart("compressed", String.valueOf(useCompression)),
					new StringPart("isResponse", String.valueOf(isResponse)),
					new StringPart("checksum", String.valueOf(request.getChecksum()))
			};	
			url = url + SyncConstants.DATA_IMPORT_SERVLET;
			log.info("POST multipart request to " + url);
			
			method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));


			// Open a connection to the server and post the data
			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(ServerConnection.getTimeout().intValue());
			int status = client.executeMethod(method);	
			
			
			// As long as the response is OK (200)
			if (status == HttpStatus.SC_OK) {
				// Decompress the response from the server
				log.info("Response from server:" + method.getResponseBodyAsString());
	
				// Check to see if the child/parent sent back a compressed response
				Header compressionHeader = method.getResponseHeader("Enable-Compression");
				useCompression = (compressionHeader!=null)?new Boolean(compressionHeader.getValue()):false;
				log.info("Response header Enable-Compression: " + useCompression);

				// Decompress the data received (if compression is enabled)
				syncResponse = new ConnectionResponse(method.getResponseBodyAsStream(), useCompression);
				
				// Now we want to validate the checksum
				Header checksumHeader = method.getResponseHeader("Content-Checksum");
				long checksumReceived = (checksumHeader!=null)?new Long(checksumHeader.getValue()):0;
				log.info("Response header Content-Checksum: " + checksumReceived);
				
				
				log.info("checksum value received in response header: " + checksumReceived );
	        	log.info("checksum of payload: " +  syncResponse.getChecksum());
	
	        	// TODO Need to figure out what to do with this response
				if (checksumReceived > 0 && (checksumReceived !=  syncResponse.getChecksum())) {
		        	log.error("ERROR: FAILED CHECKSUM!");
		        	syncResponse.setState(ServerConnectionState.CONNECTION_FAILED);	// contains error message           
	            }
			} 
			// if there's an error response code we should set the tran
			else { 
				// HTTP error response code
				syncResponse.setResponsePayload("HTTP " + status + " Error Code: " + method.getResponseBodyAsString());
				syncResponse.setState(ServerConnectionState.CONNECTION_FAILED);	// contains error message 
			}			
				
		} catch (MalformedURLException mue) {
			log.error("Malformed URL " + url, mue);
			syncResponse.setState(ServerConnectionState.MALFORMED_URL);
		} catch (Exception e) { // all other exceptions really just mean that the connection was bad
			log.error("Error occurred while sending/receiving data ", e);
			syncResponse.setState(ServerConnectionState.CONNECTION_FAILED);
		} finally { 			
			method.releaseConnection();
		}
		return syncResponse;
	}

	public static Double getTimeout() {
		// let's figure out a suitable timeout
		Double timeout = (1000.0 * 60 * 10); // let's just default at 10 min
												// for now
		try {
			Integer maxRecords = new Integer(Context.getAdministrationService()
			                                        .getGlobalProperty(SyncConstants.PROPERTY_NAME_MAX_RECORDS,
			                                                           SyncConstants.PROPERTY_NAME_MAX_RECORDS_DEFAULT));
			timeout = (4 + (maxRecords * 0.2)) * 60 * 1000; // formula we cooked
															// up after running
															// several tests:
															// latency + 0.25N
		} catch (NumberFormatException nfe) {
			// it's ok if this fails (not sure how it could) = we'll just do 10
			// min timeout
		}
		return timeout;
	}

	/*
	public static ConnectionResponse sendExportedData(String address,
	        String username, String password, String message, boolean isResponse) {

		ConnectionResponse connResponse = new ConnectionResponse();
		connResponse.setErrorMessage("");
		connResponse.setResponsePayload("");
		connResponse.setState(ServerConnectionState.CONNECTION_FAILED);

        String dataParamName = "syncData";
		if (isResponse) dataParamName = "syncDataResponse";

			// first calc checksum for the data to be send
			CRC32 crc = new CRC32();
		byte[] messageAsBytes = null;
		try {
			messageAsBytes = message.getBytes(SyncConstants.UTF8); 
        } catch (UnsupportedEncodingException e1) {
	        log.error("Couldn't convert sync message to UTF-8 bytes", e1);
        }
        
        if ( messageAsBytes != null ) {
        	
	        crc.update(messageAsBytes);
	
			log.warn("Checksum for the post of data the server: "
			        + crc.getValue());
	
			// let's figure out a suitable timeout
			Double timeout = (1000.0 * 60 * 10);  // let's just default at 10 min for now
			try {
				Integer maxRecords = new Integer(Context.getAdministrationService().getGlobalProperty(SyncConstants.PROPERTY_NAME_MAX_RECORDS, SyncConstants.PROPERTY_NAME_MAX_RECORDS_DEFAULT));
				timeout = (4 + (maxRecords * 0.2)) * 60 * 1000;  // formula we cooked up after running several tests: latency + 0.25N
			} catch ( NumberFormatException nfe ) {
				// it's ok if this fails (not sure how it could) = we'll just do 10 min timeout
			}
			
			PartSource data = new ByteArrayPartSource(dataParamName, messageAsBytes);
			PostMethod filePost = new PostMethod(address + SyncConstants.DATA_IMPORT_SERVLET);
			Part[] parts = {		
				new StringPart("username", username),
				new StringPart("password", password),
				new StringPart("syncMultipart", "true"),
				new StringPart("dataType", dataParamName),
				new StringPart("checksum", Long.toString(crc.getValue())),
				new FilePart(dataParamName, data)
			};
			filePost.setRequestEntity(
			                          new MultipartRequestEntity(parts, filePost.getParams())
			);
			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(timeout.intValue());
			try {
	            int status = client.executeMethod(filePost);
	            
	            if ( status == 200 ) {
	            	connResponse.setResponsePayload(filePost.getResponseBodyAsString());
	            	connResponse.setState(ServerConnectionState.OK);
	            } else {
	            	connResponse.setResponsePayload("ERROR: received HTTP status " + status + " while trying to send sync data");
	            }
	            
	        } catch (HttpException e) {
				connResponse.setState(ServerConnectionState.MALFORMED_URL);
	            log.error("Error generated", e);
	        } catch (IOException e) {
				connResponse.setState(ServerConnectionState.CONNECTION_FAILED);
	            log.error("Error generated", e);
	        } catch (Exception e) {
				connResponse.setState(ServerConnectionState.CONNECTION_FAILED);
	            log.error("Error generated", e);
	        } finally {
	        	filePost.releaseConnection();
	        }
        }
        
		return connResponse;
			
			
			// now build the post string
			StringBuilder sb = new StringBuilder();
			sb.append("username=");
			sb.append(URLEncoder.encode(username, SyncConstants.UTF8));
			sb.append("&password=");
			sb.append(URLEncoder.encode(password, SyncConstants.UTF8));
			sb.append("&checksum=");
			sb.append(URLEncoder.encode(Long.toString(crc.getValue()),
			                            SyncConstants.UTF8));
			sb.append("&");
			sb.append(dataParamName);
			sb.append("=");
			sb.append(URLEncoder.encode(message, SyncConstants.UTF8));

			// send
            cr = sendExportedData(address + SyncConstants.DATA_IMPORT_SERVLET,
			                      sb.toString());

			// cr = sendExportedData(address +
			// SyncConstants.DATA_IMPORT_SERVLET,
			// "username=" + URLEncoder.encode(username, SyncConstants.UTF8)
			// + "&password=" + URLEncoder.encode(password, SyncConstants.UTF8)
			// + "&" + dataParamName + "=" + URLEncoder.encode(message,
			// SyncConstants.UTF8));
        } catch (UnsupportedEncodingException e) {
			log.error("Unable to encode synchronization data as UTF-8 before sending to parent server",
			          e);
            e.printStackTrace();
        }
	}
*/




	/*
	public static ConnectionResponse sendExportedData(String postUrl, String formData) {

		ConnectionResponse connResponse = new ConnectionResponse();
		connResponse.setErrorMessage("");
		connResponse.setResponsePayload("");
		connResponse.setState(ServerConnectionState.CONNECTION_FAILED);

		// Make sure URL is verified through SSL...
		HostnameVerifier hv = new HostnameVerifier() {
		    public boolean verify(String urlHostName, SSLSession session) {
				System.out.println("Warning: URL Host: " + urlHostName
				        + " vs. " + session.getPeerHost());
		        return true;
		    }
		};

		HttpsURLConnection.setDefaultHostnameVerifier(hv);

		StringBuffer buffer = new StringBuffer("");

		System.out.println("SENDING POSTDATA: " + formData);

        HttpURLConnection httpConnection = null;
        PrintWriter writer = null;
        InputStream in = null;

		try {
			URL url = new URL(postUrl);

			if (url.getProtocol() == "https") {
				httpConnection = (HttpsURLConnection)url.openConnection();
			} else {
				httpConnection = (HttpURLConnection)url.openConnection();
			}

			// let's figure out a suitable timeout
			Double timeout = (1000.0 * 60 * 10);  // let's just default at 10 min for now
			try {
				Integer maxRecords = new Integer(Context.getAdministrationService().getGlobalProperty(SyncConstants.PROPERTY_NAME_MAX_RECORDS, SyncConstants.PROPERTY_NAME_MAX_RECORDS_DEFAULT));
				timeout = (4 + (maxRecords * 0.2)) * 60 * 1000;  // formula we cooked up after running several tests: latency + 0.25N
			} catch ( NumberFormatException nfe ) {
				// it's ok if this fails (not sure how it could) = we'll just do 10 min timeout
			}

			httpConnection.setConnectTimeout(timeout.intValue());
			//urlcon.set
			//urlcon.setConnectTimeout(SyncConstants.CONNECTION_TIMEOUT_MS);
			urlcon.setAllowUserInteraction(false);
			urlcon.setUseCaches(false);
			urlcon.setRequestMethod(SyncConstants.POST_METHOD);
			urlcon.setRequestProperty("Content-length", "" + formData.length());
			urlcon.setRequestProperty("Content-type",
			                          "application/x-www-form-urlencoded; charset="
			                                  + SyncConstants.UTF8.toLowerCase());
			urlcon.setDoOutput(true);
			urlcon.setDoInput(true);
			//Buff
			pout = new PrintWriter(new BufferedWriter(new OutputStreamWriter(urlcon.getOutputStream(),
			                                                                 SyncConstants.UTF8)),
			                       true);
			writer.print(formData);
			writer.flush();

			in = httpConnection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line = "";
			while ((line = br.readLine()) != null) {
				buffer.append(line);
			}
	        connResponse.setResponsePayload(buffer.toString());
			connResponse.setState(ServerConnectionState.OK);
        } catch (MalformedURLException mue) {
        	log.error("URL", mue);
        	mue.printStackTrace();
			connResponse.setState(ServerConnectionState.MALFORMED_URL);
        } catch (Exception e) {
        	// all other exceptions really just mean that the connection was bad
			log.error("Error while trying to connect to remote server at "
			        + postUrl, e);
	        e.printStackTrace();
        } finally {
            try {				
            	if (in != null)	in.close();
				if (writer != null) writer.close();
				if (httpConnection != null)	httpConnection.disconnect();
				in = null;
				writer = null;
				httpConnection = null;
                log.warn("Disconnecting from server...");
			} catch (Exception e) {
                log.error("Error while trying to disconnect from server");
            }
        }

		return connResponse;
	}
	 */




}
