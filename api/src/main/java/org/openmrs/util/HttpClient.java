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
				if (entry.getKey() == null || entry.getValue() == null)
					continue;
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
			if (wr != null)
				try {
					wr.close();
				}
				catch (Exception e) { /* pass */
				}
			if (rd != null)
				try {
					rd.close();
				}
				catch (Exception e) { /* pass */
				}
		}
		
		return response;
	}
}
