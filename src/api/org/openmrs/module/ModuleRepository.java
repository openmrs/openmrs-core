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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;

/**
 * Methods to cache in modules from the online repository and allows searching from administration
 * page
 */
@SuppressWarnings("deprecation")
public class ModuleRepository {
	
	private static final Log log = LogFactory.getLog(ModuleRepository.class);

	private static Date lastUpdatedDate = new Date(100, 0, 01); // first date will be 01/01/2000
	
	private static Map<String, Module> repository = new WeakHashMap<String, Module>();

	private static final int MODULE_ID_INDEX = 0;
	
	private static final int MODULE_DOWNLOAD_URL_INDEX = 1;
	
	private static final int MODULE_NAME_INDEX = 2;
	
	private static final int MODULE_VERSION_INDEX = 3;
	
	private static final int MODULE_AUTHOR_INDEX = 4;
	
	private static final int MODULE_DESCRIPTION_INDEX = 5;
	
	public static final String MODULE_REPOSITORY_URL = "modulerepository.url.allModules";
	
	public static final String MODULE_REPOSITORY_THRESHOLD = "modulerepository.threshold";
	
	private static String moduleRepositoryUrl = null;
	
	private static int noOfModules = 0;
	
	private static int noOfModuleUpdates = 0;

	/**
	 * Initializes the ModuleRepository and calls for the first module repository cache
	 */
	public static void initialize() {
		AdministrationService as = Context.getAdministrationService();
		try {
			String url = as.getGlobalProperty(MODULE_REPOSITORY_URL);
			moduleRepositoryUrl = url;
			cacheModuleRepository();
		}
		catch (Throwable t) {
			log.error("Error while initializing Module Repository", t);
		}

	}
	
	/**
	 * Caches the Module Repository
	 */
	public static void cacheModuleRepository() throws IOException {
		synchronized (repository) {
			URL url = null;
			InputStream jsonInputStream = null;
			try {
				url = getURL();
				jsonInputStream = ModuleUtil.getURLStream(url);
				ObjectMapper mapper = new ObjectMapper();
				//Reading in the JSON from the input stream
				HashMap<String, Object> map = mapper.readValue(jsonInputStream, HashMap.class);
				ArrayList<ArrayList<String>> metadata = (ArrayList<ArrayList<String>>) map.get("Values");
				for (ArrayList<String> moduleMetaData : metadata) {
					String moduleId = moduleMetaData.get(MODULE_ID_INDEX).trim(); // Module Id
					Module mod = new Module(moduleMetaData.get(MODULE_NAME_INDEX).trim()); // Module Name
					mod.setModuleId(moduleId);
					mod.setDownloadURL(moduleMetaData.get(MODULE_DOWNLOAD_URL_INDEX).trim()); // Download URL
					mod.setVersion(moduleMetaData.get(MODULE_VERSION_INDEX).trim()); // Version
					mod.setAuthor(moduleMetaData.get(MODULE_AUTHOR_INDEX).trim()); // Author
					mod.setDescription(escape(moduleMetaData.get(MODULE_DESCRIPTION_INDEX).trim())); // Description
					//If older version available remove it
					if (repository.containsKey(moduleId)) {
						repository.remove(moduleId);
					}
					repository.put(moduleId, mod);
				}
				// Set the last updated date to current date
				lastUpdatedDate = new Date();
				noOfModules = getAllModules().size();
				checkForModuleUpdates();
			}
			catch (MalformedURLException e) {
				log.error("Module Repository URL is malformed", e);
				return;
			}
			catch (IOException e) {
				if (e instanceof SocketException || e instanceof UnknownHostException) {
					log.error("No internet is available to cache modules", e);
				} else {
					log.error(e.getMessage(), e);
				}
				throw e;
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
	
	/**
	 * This method returns all the cached modules
	 * 
	 * @return cached modules
	 */
	public static Set<Module> getAllModules() {
		Map<String, Module> modules = new HashMap<String, Module>(repository);

		Map<String, Module> loadedModules = ModuleFactory.getLoadedModulesMap();

		// Marking already loaded modules "Installed"
		for (Module mod : loadedModules.values()) {
			Module module = modules.get(mod.getModuleId());
			if (module != null) {
				Module newMod = new Module(module.getName());
				newMod.setModuleId(module.getModuleId());
				newMod.setAuthor(module.getAuthor());
				newMod.setDescription(escape(module.getDescription()));
				newMod.setVersion(module.getVersion());
				newMod.setDownloadURL("Installed");
				modules.remove(module.getModuleId());
				modules.put(module.getModuleId(), newMod);
			}
		}
		
		return new HashSet<Module>(modules.values());
	}
	
	/**
	 * This method returns modules which matches a search key
	 * 
	 * @param search - Module Search Key
	 * @return search key matching modules
	 * @should return an array list of modules for matching search
	 * @should return an empty array list of modules if search is empty
	 */
	public static List<Module> searchModules(String search) {
		List<Module> modules = new ArrayList<Module>();

		Set<Module> repository = getAllModules();
		
		noOfModules = repository.size();
		
		if (search == null || "".equals(search)) {
			return modules;
		}

		for (Module mod : repository) {
			if ((mod.getModuleId().contains(search) || mod.getName().contains(search)
			        || mod.getDescription().contains(search) || mod.getAuthor().contains(search))
			        && !modules.contains(mod)) {
				modules.add(mod);
			}
		}
		
		return modules;
	}
	
	/**
	 * This method returns the no of modules in the repository
	 * 
	 * @return no of modules
	 */
	public static int getNoOfModules() {
		return noOfModules;
	}
	
	/**
	 * This method checks whether any latest versions of the loaded modules available in the cached
	 * repository
	 */
	public static void checkForModuleUpdates() {
		synchronized (repository) {
			noOfModuleUpdates = 0;
			Map<String, Module> loadedModules = ModuleFactory.getLoadedModulesMap();
		
			for (String key : loadedModules.keySet()) {
				Module loadedModule = loadedModules.get(key);
				Module repositoryModule = repository.get(key);
			
				if (repositoryModule != null) {
					if (ModuleUtil.compareVersion(loadedModule.getVersion(), repositoryModule.getVersion()) < 0
					        && !ModuleFactory.hasPendingModuleActionForModuleId(loadedModule.getModuleId())) {
						loadedModule.setDownloadURL(repositoryModule.getDownloadURL());
						loadedModule.setUpdateVersion(repositoryModule.getVersion());
						noOfModuleUpdates++;
					}
				}
			}
		}
	}
	
	/**
	 * This method returns the no of module updates available
	 * 
	 * @return no of module updates
	 */
	public static int getNoOfModuleUpdates() {
		return noOfModuleUpdates;
	}

	/**
	 * @param date
	 * @return formatted date
	 */
	private static String formatDate(Date d) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(d);
	}
	
	/**
	 * @return URL for caching
	 * @throws MalformedURLException
	 */
	private static URL getURL() throws MalformedURLException {
		String url = moduleRepositoryUrl.replace("<VERSION>", String.valueOf(OpenmrsConstants.OPENMRS_VERSION_SHORT));
		url = url.replace("<DATE>", formatDate(lastUpdatedDate));
		return new URL(url);
	}
	
	/**
	 * This method can be used to find out whether the module repository cache is older than the
	 * threshold.
	 * 
	 * @return true if cache is older than the threshold else false
	 */
	public static boolean isCacheExpired() {
		AdministrationService as = Context.getAdministrationService();
		String threshold = as.getGlobalProperty(MODULE_REPOSITORY_THRESHOLD);
		int iThreshold = 7;
		try {
			if (threshold != null) {
				iThreshold = Integer.parseInt(threshold);
			}
		}
		catch (NumberFormatException e) {
			log.error("Error while parsing " + MODULE_REPOSITORY_THRESHOLD + ", 7 will be used", e);
		}
		long today = new Date().getTime();
		long updatedDate = lastUpdatedDate.getTime();
		
		long dayDiff = (today - updatedDate) / (86400000);
		return dayDiff > iThreshold;
	}
	
	/**
	 * copied from http://json-simple.googlecode.com/svn/trunk/src/org/json/simple/JSONValue.java
	 * Revision 184 Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters (U+0000
	 * through U+001F).
	 * 
	 * @param s
	 * @return
	 */
	private static String escape(String s) {
		if (s == null)
			return null;
		StringBuffer sb = new StringBuffer();
		escape(s, sb);
		return sb.toString();
	}
	
	/**
	 * @param s - Must not be null.
	 * @param sb
	 */
	private static void escape(String s, StringBuffer sb) {
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			switch (ch) {
				case '"':
					sb.append("\\\"");
					break;
				case '\\':
					sb.append("\\\\");
					break;
				case '\b':
					sb.append("\\b");
					break;
				case '\f':
					sb.append("\\f");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\r':
					sb.append("\\r");
					break;
				case '\t':
					sb.append("\\t");
					break;
				case '/':
					sb.append("\\/");
					break;
				default:
					// Reference: http://www.unicode.org/versions/Unicode5.1.0/
					if ((ch >= '\u0000' && ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F')
					        || (ch >= '\u2000' && ch <= '\u20FF')) {
						String ss = Integer.toHexString(ch);
						sb.append("\\u");
						for (int k = 0; k < 4 - ss.length(); k++) {
							sb.append('0');
						}
						sb.append(ss.toUpperCase());
					} else {
						sb.append(ch);
					}
			}
		}// for
	}
}
