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
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;

/**
 * Methods to cache in modules from the online repository and allows searching from administration
 * page
 */
@SuppressWarnings("deprecation")
public class ModuleRepository {
	
	private static final Log log = LogFactory.getLog(ModuleRepository.class);

	private static Date lastUpdatedDate = new Date(100, 0, 01); // first date will be 01/01/2000
	
	private static Map<String, Module> repository = new WeakHashMap<String, Module>();
	
	public static final String MODULE_REPOSITORY_CACHE_UPDATE_TASK_NAME = "Module Repository Cache Update";
	
	public static final String MODULE_REPOSITORY_CACHE_UPDATE_TASK_CLASS = "org.openmrs.scheduler.tasks.ModuleRepositoryCacheUpdateTask";

	private static final int MODULE_ID_INDEX = 0;
	
	private static final int MODULE_DOWNLOAD_URL_INDEX = 1;
	
	private static final int MODULE_NAME_INDEX = 2;
	
	private static final int MODULE_VERSION_INDEX = 3;
	
	private static final int MODULE_AUTHOR_INDEX = 4;
	
	private static final int MODULE_DESCRIPTION_INDEX = 5;
	
	private static final String MODULE_REPOSITORY_URL = "modulerepository.url.allModules";
	
	private static String moduleRepositoryUrl = null;
	
	private static int noOfModules = 0;

	/**
	 * Initializes the ModuleRepository and calls for the first module repository cache
	 */
	public static void initialize() {
		AdministrationService as = Context.getAdministrationService();
		try {
			/* Temporarily Used the authentication should be removed
			 * once ticket http://dev.openmrs.org/ticket/1947 is completed
			 */
			String username = as.getGlobalProperty("scheduler.username");
			String password = as.getGlobalProperty("scheduler.password");
			Context.authenticate(username, password);
			SchedulerService ss = Context.getSchedulerService();
			TaskDefinition task = ss.getTaskByName(MODULE_REPOSITORY_CACHE_UPDATE_TASK_NAME);
			if (task == null) {
				task = new TaskDefinition();
				task.setName(MODULE_REPOSITORY_CACHE_UPDATE_TASK_NAME);
				task.setDescription("This Task updates Module  Repository Cache");
				task.setTaskClass(MODULE_REPOSITORY_CACHE_UPDATE_TASK_CLASS);
				task.setStartTime(new Date());
				task.setStartOnStartup(true);
				task.setRepeatInterval(86400L); // Daily
				task.setStarted(true);
				ss.saveTask(task);
			}
		}
		catch (Throwable t) {
			log.error("Error while starting " + MODULE_REPOSITORY_CACHE_UPDATE_TASK_NAME, t);
		}
		
		try {
			String url = as.getGlobalProperty(MODULE_REPOSITORY_URL);
			if (url == null) {
				url = WebConstants.MODULE_REPOSITORY_URL + "/getAllModules?openmrsVersion=<VERSION>&lastUpdatedDate=<DATE>";
				GlobalProperty gp = new GlobalProperty();
				gp.setProperty(MODULE_REPOSITORY_URL);
				gp.setPropertyValue(url);
				gp.setDescription("Get All Module Repository URL");
				as.saveGlobalProperty(gp);
			}
			moduleRepositoryUrl = url;
			cacheModuleRepository();
		}
		catch (Throwable t) {
			log.error("Error while initializing Module Repository", t);
		}
		Context.logout();
	}
	
	/**
	 * Creates a new Thread to Start Caching the Module Repository
	 */
	public static void cacheModuleRepository() {
		Thread cacheThread = new Thread() {
			@Override
            public void run() {
				synchronized (repository) {
					URL url = null;
					InputStream jsonInputStream = null;
					try{
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
							mod.setDescription(moduleMetaData.get(MODULE_DESCRIPTION_INDEX).trim()); // Description
							//If older version available remove it
							if (repository.containsKey(moduleId)) {
								repository.remove(moduleId);
							}
							repository.put(moduleId, mod);
						}
						// Set the last updated date to current date
						lastUpdatedDate = new Date();
						noOfModules = getAllModules().size();
					}catch(MalformedURLException e){
						log.error("Module Repository URL is malformed", e);
						return;
					}catch (IOException e) {
						if (e instanceof SocketException || e instanceof UnknownHostException) {
							log.error("No internet is available to cache modules", e);
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
		cacheThread.start();
	}
	
	/**
	 * This method returns all the cached modules excluding the loaded ones
	 * 
	 * @return cached modules
	 */
	public static Set<Module> getAllModules() {
		Map<String, Module> modules = new HashMap<String, Module>(repository);

		Map<String, Module> loadedModules = ModuleFactory.getLoadedModulesMap();
		
		// Remove the already loaded modules
		for(Module mod:loadedModules.values()){
			modules.remove(mod.getModuleId());
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
		
		if ("".equals(search)) {
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
	 * This method returns the no of modules in the repository excluding the loaded modules
	 * 
	 * @return no of modules
	 */
	public static int getNoOfModules() {
		return noOfModules;
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
}
