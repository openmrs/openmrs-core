/*
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

package org.openmrs.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * This class supports doing an HTTP post to a URL. (It replaces the OpenmrsUtil.postToUrl method, allowing us to
 * mock http calls in unit tests.)
 */
public class HttpClient {
	
	private static Log log = LogFactory.getLog(HttpClient.class);
	
	private HttpUrl url;
	
	public HttpClient(String url) throws MalformedURLException {
		this(new HttpUrl(url));
	}
	
	public HttpClient(HttpUrl url) {
		this.url = url;
	}
	
	public String post(Map<String, String> parameters) {
		OutputStreamWriter wr = null;
		BufferedReader rd = null;
		String response = "";
		StringBuffer data = new StringBuffer();
		
		try {
			// Construct data
			for (Map.Entry<String, String> entry : parameters.entrySet()) {
				
				// skip over invalid post variables
				if (entry.getKey() == null || entry.getValue() == null) {
					continue;
				}
				data.append("&"); // only append this if its _not_ the first
				// datum
				
				// finally, setup the actual post string
				data.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
				data.append("=");
				data.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
			}
			
			// Send the data
			HttpURLConnection connection = url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Length", String.valueOf(data.length()));
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			
			wr = new OutputStreamWriter(connection.getOutputStream());
			wr.write(data.toString());
			wr.flush();
			wr.close();
			
			// Get the response
			rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				response = String.format("%s%s\n", response, line);
			}
			
		}
		catch (Exception e) {
			log.warn("Exception while posting to : " + this.url, e);
			log.warn("Reponse from server was: " + response);
		}
		finally {
			if (wr != null) {
				try {
					wr.close();
				}
				catch (Exception e) { /* pass */
				}
			}
			if (rd != null) {
				try {
					rd.close();
				}
				catch (Exception e) { /* pass */
				}
			}
		}
		
		return response;
	}
}
