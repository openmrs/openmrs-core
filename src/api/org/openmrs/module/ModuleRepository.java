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
package org.openmrs.module;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.openmrs.util.OpenmrsConstants;

/**
 * Methods to cache in modules from the online repository and allows searching from administration
 * page
 */
public class ModuleRepository {
	
	private static final Log log = LogFactory.getLog(ModuleRepository.class);

	private static Date lastUpdatedDate = new Date(100, 0, 01);
	
	private static Set<Module> repository = new HashSet<Module>();

	public static void cacheModuleRepository() {
		Thread t = new Thread() {
			@Override
            public void run() {
				synchronized (repository) {
					URL url = null;
					InputStream jsonInputStream = null;
					try{
						url = getURL();
						jsonInputStream = ModuleUtil.getURLStream(url);
						JsonFactory factory = new JsonFactory();
						JsonParser parser = factory.createJsonParser(jsonInputStream);
						parser.nextToken();
						while (parser.nextToken() != JsonToken.END_OBJECT) {
							String nameField = parser.getCurrentName();
							if ("Columns".equals(nameField)) {
								String value = parser.getText();
							} else if ("Values".equals(nameField)) {
								while (parser.nextToken() != JsonToken.END_OBJECT) {
									String value = parser.getText();
								}
							}
						}
					}catch(MalformedURLException e){
						log.error("Module Repository URL is malformed",e);
						return;
					}catch (IOException e) {
						if (e instanceof SocketException || e instanceof UnknownHostException) {
							log.error("No internet is available to cache modules",e);
						}else{
							log.error(e.getMessage(), e);
						}
						return;
					}
					finally {
						if (jsonInputStream != null) {
							try {
								jsonInputStream.close();
							}
							catch (IOException e) {
								log.error("Can not close input stream", e);
							}
						}
					}
				}
			}
		};
		t.start();
	}
	
	private static String formatDate(Date d) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(d);
	}
	
	private static URL getURL() throws MalformedURLException {
		return new URL("http://localhost:8080/modules/getAllModules?openmrsVersion="
		        + OpenmrsConstants.OPENMRS_VERSION_SHORT + "&lastUpdatedDate=" + formatDate(lastUpdatedDate));
		//return new URL(WebConstants.MODULE_REPOSITORY_URL + "/getAllModules?openmrsVersion="
		        //+ OpenmrsConstants.OPENMRS_VERSION_SHORT + "&lastUpdatedDate=" + formatDate(lastUpdatedDate));
	}
}
