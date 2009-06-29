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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.context.support.AbstractRefreshableApplicationContext;

/**
 * Utility methods for working and manipulating modules
 */
public class ModuleUtil {
	
	private static Log log = LogFactory.getLog(ModuleUtil.class);
	
	/**
	 * Start up the module system with the given properties.
	 * 
	 * @param props Properties (OpenMRS runtime properties)
	 */
	public static void startup(Properties props) throws MandatoryModuleException {
		
		String moduleListString = props.getProperty(ModuleConstants.RUNTIMEPROPERTY_MODULE_LIST_TO_LOAD);
		
		if (moduleListString == null || moduleListString.length() == 0) {
			// Attempt to get all of the modules from the modules folder
			// and store them in the modules list
			log.debug("Starting all modules");
			ModuleFactory.loadModules();
		} else {
			// use the list of modules and load only those
			log.debug("Starting all modules in this list: " + moduleListString);
			
			String[] moduleArray = moduleListString.split(" ");
			List<File> modulesToLoad = new Vector<File>();
			
			for (String modulePath : moduleArray) {
				if (modulePath != null && modulePath.length() > 0) {
					File file = new File(modulePath);
					if (file.exists())
						modulesToLoad.add(file);
					else {
						// try to load the file from the classpath
						InputStream stream = ModuleUtil.class.getClassLoader().getResourceAsStream(modulePath);
						
						// expand the classpath-found file to a temporary location
						if (stream != null) {
							try {
								// get and make a temp directory if necessary
								String tmpDir = System.getProperty("java.io.tmpdir");
								File expandedFile = File.createTempFile(file.getName() + "-", ".omod", new File(tmpDir));
								
								// pull the name from the absolute path load attempt
								FileOutputStream outStream = new FileOutputStream(expandedFile, false);
								
								// do the actual file copying
								OpenmrsUtil.copyFile(stream, outStream);
								
								// add the freshly expanded file to the list of modules we're going to start up
								modulesToLoad.add(expandedFile);
								expandedFile.deleteOnExit();
							}
							catch (IOException io) {
								log.error("Unable to expand classpath found module: " + modulePath, io);
							}
						} else
							log
							        .error("Unable to load module at path: "
							                + modulePath
							                + " because no file exists there and it is not found on the classpath. (absolute path tried: "
							                + file.getAbsolutePath() + ")");
					}
				}
			}
			
			ModuleFactory.loadModules(modulesToLoad);
		}
		
		// start all of the modules we just loaded
		ModuleFactory.startModules();
		
		// some debugging info
		if (log.isDebugEnabled()) {
			Collection<Module> modules = ModuleFactory.getStartedModules();
			if (modules == null || modules.size() == 0)
				log.debug("No modules loaded");
			else
				log.debug("Found and loaded " + modules.size() + " module(s)");
		}
		
		// make sure all mandatory modules are loaded and started
		checkMandatoryModulesStarted();
	}
	
	/**
	 * Stops the module system by calling stopModule for all modules that are currently started
	 */
	public static void shutdown() {
		
		List<Module> modules = new Vector<Module>();
		modules.addAll(ModuleFactory.getStartedModules());
		
		for (Module mod : modules) {
			if (log.isDebugEnabled())
				log.debug("stopping module: " + mod.getModuleId());
			
			if (mod.isStarted())
				ModuleFactory.stopModule(mod, true, true);
		}
		
		log.debug("done shutting down modules");
		
		// clean up the static variables just in case they weren't done before
		ModuleFactory.extensionMap = null;
		ModuleFactory.loadedModules = null;
		ModuleFactory.moduleClassLoaders = null;
		ModuleFactory.startedModules = null;
	}
	
	/**
	 * Add the <code>inputStream</code> as a file in the modules repository
	 * 
	 * @param inputStream <code>InputStream</code> to load
	 * @return filename String of the file's name of the stream
	 */
	public static File insertModuleFile(InputStream inputStream, String filename) {
		File folder = getModuleRepository();
		
		// check if module filename is already loaded
		if (OpenmrsUtil.folderContains(folder, filename))
			throw new ModuleException(filename + " is already associated with a loaded module.");
		
		File file = new File(folder.getAbsolutePath() + File.separator + filename);
		
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(file);
			OpenmrsUtil.copyFile(inputStream, outputStream);
		}
		catch (FileNotFoundException e) {
			throw new ModuleException("Can't create module file for " + filename, e);
		}
		catch (IOException e) {
			throw new ModuleException("Can't create module file for " + filename, e);
		}
		finally {
			try {
				inputStream.close();
			}
			catch (Exception e) { /* pass */}
			try {
				outputStream.close();
			}
			catch (Exception e) { /* pass */}
		}
		
		return file;
	}
	
	/**
	 * Compares <code>version</code> to <code>value</code> version and value are strings like
	 * w.x.y.z Returns <code>0</code> if either <code>version</code> or <code>value</code> is null.
	 * 
	 * @param version String like w.x.y.z
	 * @param value String like w.x.y.z
	 * @return the value <code>0</code> if <code>version</code> is equal to the argument
	 *         <code>value</code>; a value less than <code>0</code> if <code>version</code> is
	 *         numerically less than the argument <code>value</code>; and a value greater than
	 *         <code>0</code> if <code>version</code> is numerically greater than the argument
	 *         <code>value</code>
	 */
	public static int compareVersion(String version, String value) {
		try {
			
			if (version == null || value == null)
				return 0;
			
			List<String> versions = new Vector<String>();
			List<String> values = new Vector<String>();
			
			Collections.addAll(versions, version.split("\\."));
			Collections.addAll(values, value.split("\\."));
			
			// match the sizes of the lists
			while (versions.size() < values.size()) {
				versions.add("0");
			}
			while (values.size() < versions.size()) {
				values.add("0");
			}
			
			for (int x = 0; x < versions.size(); x++) {
				String verNum = versions.get(x).trim();
				String valNum = values.get(x).trim();
				Integer ver = new Integer(verNum == "" ? "0" : verNum);
				Integer val = new Integer(valNum == "" ? "0" : valNum);
				
				int ret = ver.compareTo(val);
				if (ret != 0)
					return ret;
			}
		}
		catch (NumberFormatException e) {
			log.error("Error while converting a version/value to an integer: " + version + "/" + value, e);
		}
		
		// default return value if an error occurs or elements are equal
		return 0;
	}
	
	/**
	 * Gets the folder where modules are stored. ModuleExceptions are thrown on errors
	 * 
	 * @return folder containing modules
	 */
	public static File getModuleRepository() {
		
		AdministrationService as = Context.getAdministrationService();
		String folderName = as.getGlobalProperty(ModuleConstants.REPOSITORY_FOLDER_PROPERTY,
		    ModuleConstants.REPOSITORY_FOLDER_PROPERTY_DEFAULT);
		
		// try to load the repository folder straight away.
		File folder = new File(folderName);
		
		// if the property wasn't a full path already, assume it was intended to be a folder in the 
		// application directory
		if (!folder.exists()) {
			folder = new File(OpenmrsUtil.getApplicationDataDirectory(), folderName);
		}
		
		// now create the modules folder if it doesn't exist
		if (!folder.exists()) {
			log.warn("Module repository " + folder.getAbsolutePath() + " doesn't exist.  Creating directories now.");
			folder.mkdirs();
		}
		
		if (!folder.isDirectory())
			throw new ModuleException("Module repository is not a directory at: " + folder.getAbsolutePath());
		
		return folder;
	}
	
	/**
	 * Utility method to convert a {@link File} object to a local URL.
	 * 
	 * @param file a file object
	 * @return absolute URL that points to the given file
	 * @throws MalformedURLException if file can't be represented as URL for some reason
	 */
	public static URL file2url(final File file) throws MalformedURLException {
		if (file == null)
			return null;
		try {
			return file.getCanonicalFile().toURI().toURL();
		}
		catch (MalformedURLException mue) {
			throw mue;
		}
		catch (IOException ioe) {
			throw new MalformedURLException("Cannot convert: " + file.getName() + " to url");
		}
		catch (NoSuchMethodError nsme) {
			throw new MalformedURLException("Cannot convert: " + file.getName() + " to url");
		}
	}
	
	/**
	 * Expand the given <code>fileToExpand</code> jar to the <code>tmpModuleFile<code> directory 
	 * 
	 * If <code>name</code> is null, the entire jar is expanded. If<code>name</code> is not null,
	 * then only that path/file is expanded.
	 * 
	 * @param fileToExpand file pointing at a .jar
	 * @param tmpModuleDir directory in which to place the files
	 * @param name filename inside of the jar to look for and expand
	 * @param keepFullPath if true, will recreate entire directory structure in tmpModuleDir
	 *            relating to <code>name</code>. if false will start directory structure at
	 *            <code>name</code>
	 */
	@SuppressWarnings("unchecked")
	public static void expandJar(File fileToExpand, File tmpModuleDir, String name, boolean keepFullPath) throws IOException {
		JarFile jarFile = null;
		InputStream input = null;
		String docBase = tmpModuleDir.getAbsolutePath();
		try {
			jarFile = new JarFile(fileToExpand);
			Enumeration jarEntries = jarFile.entries();
			boolean foundName = (name == null);
			
			// loop over all of the elements looking for the match to 'name'
			while (jarEntries.hasMoreElements()) {
				JarEntry jarEntry = (JarEntry) jarEntries.nextElement();
				if (name == null || jarEntry.getName().startsWith(name)) {
					String entryName = jarEntry.getName();
					// trim out the name path from the name of the new file
					if (keepFullPath == false && name != null)
						entryName = entryName.replaceFirst(name, "");
					
					// if it has a slash, it's in a directory
					int last = entryName.lastIndexOf('/');
					if (last >= 0) {
						File parent = new File(docBase, entryName.substring(0, last));
						parent.mkdirs();
						log.debug("Creating parent dirs: " + parent.getAbsolutePath());
					}
					// we don't want to "expand" directories or empty names
					if (entryName.endsWith("/") || entryName.equals("")) {
						continue;
					}
					input = jarFile.getInputStream(jarEntry);
					expand(input, docBase, entryName);
					input.close();
					input = null;
					foundName = true;
				}
			}
			if (!foundName)
				log.debug("Unable to find: " + name + " in file " + fileToExpand.getAbsolutePath());
			
		}
		catch (IOException e) {
			log.warn("Unable to delete tmpModuleFile on error", e);
			throw e;
		}
		finally {
			try {
				input.close();
			}
			catch (Exception e) { /* pass */}
			try {
				jarFile.close();
			}
			catch (Exception e) { /* pass */}
		}
	}
	
	/**
	 * Expand the given file in the given stream to a location (fileDir/name) The <code>input</code>
	 * InputStream is not closed in this method
	 * 
	 * @param input stream to read from
	 * @param fileDir directory to copy to
	 * @param name file/directory within the <code>fileDir</code> to which we expand
	 *            <code>input</code>
	 * @return File the file created by the expansion.
	 * @throws IOException if an error occurred while copying
	 */
	private static File expand(InputStream input, String fileDir, String name) throws IOException {
		if (log.isDebugEnabled())
			log.debug("expanding: " + name);
		
		File file = new File(fileDir, name);
		FileOutputStream outStream = null;
		try {
			outStream = new FileOutputStream(file);
			OpenmrsUtil.copyFile(input, outStream);
		}
		finally {
			try {
				outStream.close();
			}
			catch (Exception e) { /* pass */}
		}
		
		return file;
	}
	
	/**
	 * Downloads the contents of a URL and copies them to a string (Borrowed from oreilly)
	 * 
	 * @param url
	 * @return InputStream of contents
	 * @should return a valid input stream for old module urls
	 */
	public static InputStream getURLStream(URL url) {
		InputStream in = null;
		try {
			URLConnection uc = url.openConnection();
			uc.setDefaultUseCaches(false);
			uc.setUseCaches(false);
			uc.setRequestProperty("Cache-Control", "max-age=0,no-cache");
			uc.setRequestProperty("Pragma", "no-cache");
			
			log.error("Logging an attempt to connect to: " + url);
			
			in = uc.getInputStream();
		}
		catch (IOException io) {
			log.warn("io while reading: " + url, io);
		}
		
		return in;
	}
	
	/**
	 * Downloads the contents of a URL and copies them to a string (Borrowed from oreilly)
	 * 
	 * @param url
	 * @return String contents of the URL
	 * @should return an update rdf page for old https dev urls
	 * @should return an update rdf page for old https module urls
	 * @should return an update rdf page for module urls
	 */
	public static String getURL(URL url) {
		InputStream in = null;
		OutputStream out = null;
		String output = "";
		try {
			in = getURLStream(url);
			if (in == null) // skip this module if updateURL is not defined
				return "";
			
			out = new ByteArrayOutputStream();
			OpenmrsUtil.copyFile(in, out);
			output = out.toString();
		}
		catch (IOException io) {
			log.warn("io while reading: " + url, io);
		}
		finally {
			try {
				in.close();
			}
			catch (Exception e) { /* pass */}
			try {
				out.close();
			}
			catch (Exception e) { /* pass */}
		}
		
		return output;
	}
	
	/**
	 * Iterates over the modules and checks each update.rdf file for an update
	 * 
	 * @return True if an update was found for one of the modules, false if none were found
	 * @throws ModuleException
	 */
	public static Boolean checkForModuleUpdates() throws ModuleException {
		
		Boolean updateFound = false;
		
		for (Module mod : ModuleFactory.getLoadedModules()) {
			String updateURL = mod.getUpdateURL();
			if (updateURL != null && !updateURL.equals("")) {
				try {
					// get the contents pointed to by the url
					URL url = new URL(updateURL);
					if (!url.toString().endsWith(ModuleConstants.UPDATE_FILE_NAME)) {
						log.warn("Illegal url: " + url);
						continue;
					}
					String content = getURL(url);
					
					// skip empty or invalid updates
					if (content.equals(""))
						continue;
					
					// process and parse the contents 
					UpdateFileParser parser = new UpdateFileParser(content);
					parser.parse();
					
					log.debug("Update for mod: " + mod.getModuleId() + " compareVersion result: "
					        + compareVersion(mod.getVersion(), parser.getCurrentVersion()));
					
					// check the udpate.rdf version against the installed version
					if (compareVersion(mod.getVersion(), parser.getCurrentVersion()) < 0) {
						if (mod.getModuleId().equals(parser.getModuleId())) {
							mod.setDownloadURL(parser.getDownloadURL());
							mod.setUpdateVersion(parser.getCurrentVersion());
							updateFound = true;
						} else
							log.warn("Module id does not match in update.rdf:" + parser.getModuleId());
					} else {
						mod.setDownloadURL(null);
						mod.setUpdateVersion(null);
					}
				}
				catch (ModuleException e) {
					log.warn("Unable to get updates from update.xml", e);
				}
				catch (MalformedURLException e) {
					log.warn("Unable to form a URL object out of: " + updateURL, e);
				}
			}
		}
		
		return updateFound;
	}
	
	/**
	 * @return true/false whether the 'allow upload' or 'allow web admin' property has been turned
	 *         on
	 */
	public static Boolean allowAdmin() {
		
		Properties properties = Context.getRuntimeProperties();
		String prop = properties.getProperty(ModuleConstants.RUNTIMEPROPERTY_ALLOW_UPLOAD, null);
		if (prop == null)
			prop = properties.getProperty(ModuleConstants.RUNTIMEPROPERTY_ALLOW_ADMIN, "false");
		
		return "true".equals(prop);
	}
	
	/**
	 * Refreshes the given application context "properly" in OpenMRS. Will first shut down the
	 * Context and destroy the classloader, then will refresh and set everything back up again
	 * 
	 * @param ctx Spring application context that needs refreshing
	 * @return AbstractRefreshableApplicationContext the newly refreshed application context
	 */
	public static AbstractRefreshableApplicationContext refreshApplicationContext(AbstractRefreshableApplicationContext ctx) {
		OpenmrsClassLoader.saveState();
		ServiceContext.destroyInstance();
		
		try {
			ctx.stop();
			ctx.close();
		}
		catch (Exception e) {
			log.warn("Exception while stopping and closing context: ", e);
			// Spring seems to be trying to refresh the context instead of /just/ stopping
			// pass
		}
		OpenmrsClassLoader.destroyInstance();
		ctx.setClassLoader(OpenmrsClassLoader.getInstance());
		Thread.currentThread().setContextClassLoader(OpenmrsClassLoader.getInstance());
		
		ServiceContext.getInstance().startRefreshingContext();
		try {
			ctx.refresh();
		}
		finally {
			ServiceContext.getInstance().doneRefreshingContext();
		}
		
		ctx.setClassLoader(OpenmrsClassLoader.getInstance());
		Thread.currentThread().setContextClassLoader(OpenmrsClassLoader.getInstance());
		
		OpenmrsClassLoader.restoreState();
		
		// reload the advice points that were lost when refreshing Spring
		if (log.isDebugEnabled())
			log.debug("Reloading advice for all started modules: " + ModuleFactory.getStartedModules().size());
		
		for (Module module : ModuleFactory.getStartedModules()) {
			ModuleFactory.loadAdvice(module);
		}
		
		return ctx;
	}
	
	/**
	 * Looks at the <moduleid>.mandatory properties and at the currently started modules to make
	 * sure that all mandatory modules have been started successfully.
	 * 
	 * @throws ModuleException if a mandatory module isn't started
	 * @should throw ModuleException if a mandatory module is not started
	 */
	protected static void checkMandatoryModulesStarted() throws ModuleException {
		
		List<String> mandatoryModuleIds = getMandatoryModules();
		Set<String> startedModuleIds = ModuleFactory.getStartedModulesMap().keySet();
		
		mandatoryModuleIds.removeAll(startedModuleIds);
		
		// any module ids left in the list are not started
		if (mandatoryModuleIds.size() > 0) {
			throw new MandatoryModuleException(mandatoryModuleIds);
		}
	}
	
	/**
	 * Returns all modules that are marked as mandatory. Currently this means there is a
	 * <moduleid>.mandatory=true global property.
	 * 
	 * @return list of modules ids for mandatory modules
	 * @should return mandatory module ids
	 */
	public static List<String> getMandatoryModules() {
		
		List<String> mandatoryModuleIds = new ArrayList<String>();
		
		try {
			List<GlobalProperty> props = Context.getAdministrationService().getGlobalPropertiesBySuffix(".mandatory");
			
			for (GlobalProperty prop : props) {
				if ("true".equalsIgnoreCase(prop.getPropertyValue())) {
					mandatoryModuleIds.add(prop.getProperty().replace(".mandatory", ""));
				}
			}
		}
		catch (Throwable t) {
			log.warn("Unable to get the mandatory module list", t);
		}
		
		return mandatoryModuleIds;
	}
	
}
