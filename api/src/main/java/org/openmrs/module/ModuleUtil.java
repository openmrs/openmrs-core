/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.scheduler.SchedulerUtil;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractRefreshableApplicationContext;

/**
 * Utility methods for working and manipulating modules
 */
public class ModuleUtil {

	private ModuleUtil() {
	}
	
	private static final Logger log = LoggerFactory.getLogger(ModuleUtil.class);
	
	/**
	 * Start up the module system with the given properties.
	 *
	 * @param props Properties (OpenMRS runtime properties)
	 */
	public static void startup(Properties props) throws ModuleMustStartException {
		
		String moduleListString = props.getProperty(ModuleConstants.RUNTIMEPROPERTY_MODULE_LIST_TO_LOAD);
		
		if (moduleListString == null || moduleListString.isEmpty()) {
			// Attempt to get all of the modules from the modules folder
			// and store them in the modules list
			log.debug("Starting all modules");
			ModuleFactory.loadModules();
		} else {
			// use the list of modules and load only those
			log.debug("Starting all modules in this list: " + moduleListString);
			
			String[] moduleArray = moduleListString.split(" ");
			List<File> modulesToLoad = new ArrayList<>();
			
			for (String modulePath : moduleArray) {
				if (modulePath != null && modulePath.length() > 0) {
					File file = new File(modulePath);
					if (file.exists()) {
						modulesToLoad.add(file);
					} else {
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
						} else {
							log
							        .error("Unable to load module at path: "
							                + modulePath
							                + " because no file exists there and it is not found on the classpath. (absolute path tried: "
							                + file.getAbsolutePath() + ")");
						}
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
			if (modules == null || modules.isEmpty()) {
				log.debug("No modules loaded");
			} else {
				log.debug("Found and loaded {} module(s)", modules.size());
			}
		}
		
		// make sure all openmrs required moduls are loaded and started
		checkOpenmrsCoreModulesStarted();
		
		// make sure all mandatory modules are loaded and started
		checkMandatoryModulesStarted();
	}
	
	/**
	 * Stops the module system by calling stopModule for all modules that are currently started
	 */
	public static void shutdown() {

		List<Module> modules = new ArrayList<>(ModuleFactory.getStartedModules());
		
		for (Module mod : modules) {
			log.debug("stopping module: {}", mod.getModuleId());
			
			if (mod.isStarted()) {
				ModuleFactory.stopModule(mod, true, true);
			}
		}
		
		log.debug("done shutting down modules");
		
		// clean up the static variables just in case they weren't done before
		ModuleFactory.extensionMap.clear();
		ModuleFactory.loadedModules.invalidateAll();
		ModuleFactory.moduleClassLoaders.invalidateAll();
		ModuleFactory.startedModules.invalidateAll();
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
		if (OpenmrsUtil.folderContains(folder, filename)) {
			throw new ModuleException(filename + " is already associated with a loaded module.");
		}
		
		File file = new File(folder.getAbsolutePath(), filename);
		
		try (FileOutputStream outputStream = new FileOutputStream(file)) {
			OpenmrsUtil.copyFile(inputStream, outputStream);
		}
		catch (IOException e) {
			throw new ModuleException("Can't create module file for " + filename, e);
		}
		finally {
			try {
				inputStream.close();
			}
			catch (Exception e) { /* pass */}
		}
		
		return file;
	}

	/**
	 * Checks if the current OpenMRS version is in an array of versions.
	 * <p>
	 * This method calls {@link ModuleUtil#matchRequiredVersions(String, String)} internally.
	 * </p>
	 *
	 * @param versions the openmrs versions to be checked against the current openmrs version
	 * @return true if the current openmrs version is in versions otherwise false
	 * <strong>Should</strong> return false when versions is null
	 * <strong>Should</strong> return false when versions is empty
	 * <strong>Should</strong> return true if current openmrs version matches one element in versions
	 * <strong>Should</strong> return false if current openmrs version does not match any element in versions
	 */
	public static boolean isOpenmrsVersionInVersions(String ...versions) {
		if (versions == null || versions.length == 0) {
			return false;
		}

		boolean result = false;
		for (String version : versions) {
			if (matchRequiredVersions(OpenmrsConstants.OPENMRS_VERSION_SHORT, version)) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	/**
	 * This method is an enhancement of {@link #compareVersion(String, String)} and adds support for
	 * wildcard characters and upperbounds. <br>
	 * <br>
	 * This method calls {@link ModuleUtil#checkRequiredVersion(String, String)} internally. <br>
	 * <br>
	 * The require version number in the config file can be in the following format:
	 * <ul>
	 * <li>1.2.3</li>
	 * <li>1.2.*</li>
	 * <li>1.2.2 - 1.2.3</li>
	 * <li>1.2.* - 1.3.*</li>
	 * </ul>
	 * <p>
	 * Again the possible require version number formats with their interpretation:
	 * <ul>
	 * <li>1.2.3 means 1.2.3 and above</li>
	 * <li>1.2.* means any version of the 1.2.x branch. That is 1.2.0, 1.2.1, 1.2.2,... but not 1.3.0, 1.4.0</li>
	 * <li>1.2.2 - 1.2.3 means 1.2.2 and 1.2.3 (inclusive)</li>
	 * <li>1.2.* - 1.3.* means any version of the 1.2.x and 1.3.x branch</li>
	 * </ul>
	 * </p>
	 *
	 * @param version openmrs version number to be compared
	 * @param versionRange value in the config file for required openmrs version
	 * @return true if the <code>version</code> is within the <code>value</code>
	 * <strong>Should</strong> allow ranged required version
	 * <strong>Should</strong> allow ranged required version with wild card
	 * <strong>Should</strong> allow ranged required version with wild card on one end
	 * <strong>Should</strong> allow single entry for required version
	 * <strong>Should</strong> allow required version with wild card
	 * <strong>Should</strong> allow non numeric character required version
	 * <strong>Should</strong> allow ranged non numeric character required version
	 * <strong>Should</strong> allow ranged non numeric character with wild card
	 * <strong>Should</strong> allow ranged non numeric character with wild card on one end
	 * <strong>Should</strong> return false when openmrs version beyond wild card range
	 * <strong>Should</strong> return false when required version beyond openmrs version
	 * <strong>Should</strong> return false when required version with wild card beyond openmrs version
	 * <strong>Should</strong> return false when required version with wild card on one end beyond openmrs version
	 * <strong>Should</strong> return false when single entry required version beyond openmrs version
	 * <strong>Should</strong> allow release type in the version
	 * <strong>Should</strong> match when revision number is below maximum revision number
	 * <strong>Should</strong> not match when revision number is above maximum revision number
	 * <strong>Should</strong> correctly set upper and lower limit for versionRange with qualifiers and wild card
	 * <strong>Should</strong> match when version has wild card plus qualifier and is within boundary
	 * <strong>Should</strong> not match when version has wild card plus qualifier and is outside boundary
	 * <strong>Should</strong> match when version has wild card and is within boundary
	 * <strong>Should</strong> not match when version has wild card and is outside boundary
	 * <strong>Should</strong> return true when required version is empty
	 */
	public static boolean matchRequiredVersions(String version, String versionRange) {
		// There is a null check so no risk in keeping the literal on the right side
		if (StringUtils.isNotEmpty(versionRange)) {
			String[] ranges = versionRange.split(",");
			for (String range : ranges) {
				// need to externalize this string
				String separator = "-";
				if (range.indexOf("*") > 0 || range.indexOf(separator) > 0 && (!isVersionWithQualifier(range))) {
					// if it contains "*" or "-" then we must separate those two
					// assume it's always going to be two part
					// assign the upper and lower bound
					// if there's no "-" to split lower and upper bound
					// then assign the same value for the lower and upper
					String lowerBound = range;
					String upperBound = range;
					
					int indexOfSeparator = range.indexOf(separator);
					while (indexOfSeparator > 0) {
						lowerBound = range.substring(0, indexOfSeparator);
						upperBound = range.substring(indexOfSeparator + 1);
						if (upperBound.matches("^\\s?\\d+.*")) {
							break;
						}
						indexOfSeparator = range.indexOf(separator, indexOfSeparator + 1);
					}
					
					// only preserve part of the string that match the following format:
					// - xx.yy.*
					// - xx.yy.zz*
					lowerBound = StringUtils.remove(lowerBound, lowerBound.replaceAll("^\\s?\\d+[\\.\\d+\\*?|\\.\\*]+", ""));
					upperBound = StringUtils.remove(upperBound, upperBound.replaceAll("^\\s?\\d+[\\.\\d+\\*?|\\.\\*]+", ""));
					
					// if the lower contains "*" then change it to zero
					if (lowerBound.indexOf("*") > 0) {
						lowerBound = lowerBound.replaceAll("\\*", "0");
					}
					
					// if the upper contains "*" then change it to maxRevisionNumber
					if (upperBound.indexOf("*") > 0) {
						upperBound = upperBound.replaceAll("\\*", Integer.toString(Integer.MAX_VALUE));
					}
					
					int lowerReturn = compareVersionIgnoringQualifier(version, lowerBound);
					
					int upperReturn = compareVersionIgnoringQualifier(version, upperBound);
					
					if (lowerReturn < 0 || upperReturn > 0) {
						log.debug("Version " + version + " is not between " + lowerBound + " and " + upperBound);
					} else {
						return true;
					}
				} else {
					if (compareVersionIgnoringQualifier(version, range) < 0) {
						log.debug("Version " + version + " is below " + range);
					} else {
						return true;
					}
				}
			}
		}
		else {
			//no version checking if required version is not specified
			return true;
		}
		
		return false;
	}
	
	/**
	 * This method is an enhancement of {@link #compareVersion(String, String)} and adds support for
	 * wildcard characters and upperbounds. <br>
	 * <br>
	 * <br>
	 * The require version number in the config file can be in the following format:
	 * <ul>
	 * <li>1.2.3</li>
	 * <li>1.2.*</li>
	 * <li>1.2.2 - 1.2.3</li>
	 * <li>1.2.* - 1.3.*</li>
	 * </ul>
	 * <p>
	 * Again the possible require version number formats with their interpretation:
	 * <ul>
	 * <li>1.2.3 means 1.2.3 and above</li>
	 * <li>1.2.* means any version of the 1.2.x branch. That is 1.2.0, 1.2.1, 1.2.2,... but not 1.3.0, 1.4.0</li>
	 * <li>1.2.2 - 1.2.3 means 1.2.2 and 1.2.3 (inclusive)</li>
	 * <li>1.2.* - 1.3.* means any version of the 1.2.x and 1.3.x branch</li>
	 * </ul>
	 * </p>
	 *
	 * @param version openmrs version number to be compared
	 * @param versionRange value in the config file for required openmrs version
	 * @throws ModuleException if the <code>version</code> is not within the <code>value</code>
	 * <strong>Should</strong> throw ModuleException if openmrs version beyond wild card range
	 * <strong>Should</strong> throw ModuleException if required version beyond openmrs version
	 * <strong>Should</strong> throw ModuleException if required version with wild card beyond openmrs version
	 * <strong>Should</strong> throw ModuleException if required version with wild card on one end beyond openmrs
	 *         version
	 * <strong>Should</strong> throw ModuleException if single entry required version beyond openmrs version
	 * <strong>Should</strong> throw ModuleException if SNAPSHOT not handled correctly
	 * <strong>Should</strong> handle SNAPSHOT versions
	 * <strong>Should</strong> handle ALPHA versions
	 */
	public static void checkRequiredVersion(String version, String versionRange) throws ModuleException {
		if (!matchRequiredVersions(version, versionRange)) {
			String ms = Context.getMessageSourceService().getMessage("Module.requireVersion.outOfBounds",
			    new String[] { versionRange, version }, Context.getLocale());
			throw new ModuleException(ms);
		}
	}
	
	/**
	 * Compare two version strings.
	 *
	 * @param versionA String like 1.9.2.0, may include a qualifier like "-SNAPSHOT", may be null
	 * @param versionB String like 1.9.2.0, may include a qualifier like "-SNAPSHOT", may be null
	 * @return the value <code>0</code> if versions are equal; a value less than <code>0</code> if first version is
	 * 		   before the second one; a value greater than <code>0</code> if first version is after the second one.
	 * 		   If version numbers are equal and only one of them has a qualifier, the version without the qualifier is
	 * 		   considered greater.
	 */
	public static int compareVersion(String versionA, String versionB) {
		return compareVersion(versionA, versionB, false);
	}

	/**
	 * Compare two version strings. Any version qualifiers are ignored in the comparison.
	 *
	 * @param versionA String like 1.9.2.0, may include a qualifier like "-SNAPSHOT", may be null
	 * @param versionB String like 1.9.2.0, may include a qualifier like "-SNAPSHOT", may be null
	 * @return the value <code>0</code> if versions are equal; a value less than <code>0</code> if first version is
	 * 		   before the second one; a value greater than <code>0</code> if first version is after the second one.
	 */
	public static int compareVersionIgnoringQualifier(String versionA, String versionB) {
		return compareVersion(versionA, versionB, true);
	}

	private static int compareVersion(String versionA, String versionB, boolean ignoreQualifier) {
		try {
			if (versionA == null || versionB == null) {
				return 0;
			}

			List<String> versionANumbers = new ArrayList<>();
			List<String> versionBNumbers = new ArrayList<>();
			String qualifierSeparator = "-";

			// strip off any qualifier e.g. "-SNAPSHOT"
			int qualifierIndexA = versionA.indexOf(qualifierSeparator);
			if (qualifierIndexA != -1) {
				versionA = versionA.substring(0, qualifierIndexA);
			}

			// strip off any qualifier e.g. "-SNAPSHOT"
			int qualifierIndexB = versionB.indexOf(qualifierSeparator);
			if (qualifierIndexB != -1) {
				versionB = versionB.substring(0, qualifierIndexB);
			}

			Collections.addAll(versionANumbers, versionA.split("\\."));
			Collections.addAll(versionBNumbers, versionB.split("\\."));

			// match the sizes of the lists
			while (versionANumbers.size() < versionBNumbers.size()) {
				versionANumbers.add("0");
			}
			while (versionBNumbers.size() < versionANumbers.size()) {
				versionBNumbers.add("0");
			}

			for (int x = 0; x < versionANumbers.size(); x++) {
				String verAPartString = versionANumbers.get(x).trim();
				String verBPartString = versionBNumbers.get(x).trim();
				Long verAPart = NumberUtils.toLong(verAPartString, 0);
				Long verBPart = NumberUtils.toLong(verBPartString, 0);

				int ret = verAPart.compareTo(verBPart);
				if (ret != 0) {
					return ret;
				}
			}
			
			// At this point the version numbers are equal.
			if (!ignoreQualifier) {
				if (qualifierIndexA >= 0 && qualifierIndexB < 0) {
					return -1;
				} else if (qualifierIndexA < 0 && qualifierIndexB >= 0) {
					return 1;
				}
			}
		}
		catch (NumberFormatException e) {
			log.error("Error while converting a version/value to an integer: " + versionA + "/" + versionB, e);
		}
		
		// default return value if an error occurs or elements are equal
		return 0;
	}
	
	/**
	 * Checks for qualifier version (i.e "-SNAPSHOT", "-ALPHA" etc. after maven version conventions)
	 *
	 * @param version String like 1.9.2-SNAPSHOT
	 * @return true if version contains qualifier
	 */
	public static boolean isVersionWithQualifier(String version) {
		Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)(\\.(\\d+))?(\\-([A-Za-z]+))").matcher(version);
		return matcher.matches();
	}
	
	/**
	 * Gets the folder where modules are stored. ModuleExceptions are thrown on errors
	 *
	 * @return folder containing modules
	 * <strong>Should</strong> use the runtime property as the first choice if specified
	 * <strong>Should</strong> return the correct file if the runtime property is an absolute path
	 */
	public static File getModuleRepository() {
		
		String folderName = Context.getRuntimeProperties().getProperty(ModuleConstants.REPOSITORY_FOLDER_RUNTIME_PROPERTY);
		if (StringUtils.isBlank(folderName)) {
			AdministrationService as = Context.getAdministrationService();
			folderName = as.getGlobalProperty(ModuleConstants.REPOSITORY_FOLDER_PROPERTY,
			    ModuleConstants.REPOSITORY_FOLDER_PROPERTY_DEFAULT);
		}
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
		
		if (!folder.isDirectory()) {
			throw new ModuleException("Module repository is not a directory at: " + folder.getAbsolutePath());
		}
		
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
		if (file == null) {
			return null;
		}
		try {
			return file.getCanonicalFile().toURI().toURL();
		}
		catch (IOException | NoSuchMethodError ioe) {
			throw new MalformedURLException("Cannot convert: " + file.getName() + " to url");
		}
	}
	
	/**
	 * Expand the given <code>fileToExpand</code> jar to the <code>tmpModuleFile</code> directory
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
	 * <strong>Should</strong> expand entire jar if name is null
	 * <strong>Should</strong> expand entire jar if name is empty string
	 * <strong>Should</strong> expand directory with parent tree if name is directory and keepFullPath is true
	 * <strong>Should</strong> expand directory without parent tree if name is directory and keepFullPath is false
	 * <strong>Should</strong> expand file with parent tree if name is file and keepFullPath is true
	 */
	public static void expandJar(File fileToExpand, File tmpModuleDir, String name, boolean keepFullPath) throws IOException {
		String docBase = tmpModuleDir.getAbsolutePath();
		try (JarFile jarFile = new JarFile(fileToExpand)) {
			Enumeration<JarEntry> jarEntries = jarFile.entries();
			boolean foundName = (name == null);
			
			// loop over all of the elements looking for the match to 'name'
			while (jarEntries.hasMoreElements()) {
				JarEntry jarEntry = jarEntries.nextElement();
				if (name == null || jarEntry.getName().startsWith(name)) {
					String entryName = jarEntry.getName();
					// trim out the name path from the name of the new file
					if (!keepFullPath && name != null) {
						entryName = entryName.replaceFirst(name, "");
					}
					
					// if it has a slash, it's in a directory
					int last = entryName.lastIndexOf('/');
					if (last >= 0) {
						File parent = new File(docBase, entryName.substring(0, last));
						parent.mkdirs();
						log.debug("Creating parent dirs: " + parent.getAbsolutePath());
					}
					// we don't want to "expand" directories or empty names
					if (entryName.endsWith("/") || "".equals(entryName)) {
						continue;
					}
					try(InputStream input = jarFile.getInputStream(jarEntry)) {
						expand(input, docBase, entryName);
					}
					foundName = true;
				}
			}
			if (!foundName) {
				log.debug("Unable to find: " + name + " in file " + fileToExpand.getAbsolutePath());
			}
			
		}
		catch (IOException e) {
			log.warn("Unable to delete tmpModuleFile on error", e);
			throw e;
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
	private static void expand(InputStream input, String fileDir, String name) throws IOException {
		log.debug("expanding: {}", name);

		File file = new File(fileDir, name);

		if (!file.toPath().normalize().startsWith(fileDir)) {
			throw new UnsupportedOperationException("Attempted to write file '" + name + "' rejected as it attempts to write outside the chosen directory. This may be the result of a zip-slip style attack.");
		}
		
		try (FileOutputStream outStream = new FileOutputStream(file)) {
			OpenmrsUtil.copyFile(input, outStream);
		}
	}
	
	/**
	 * Downloads the contents of a URL and copies them to a string (Borrowed from oreilly)
	 *
	 * @param url
	 * @return InputStream of contents
	 * <strong>Should</strong> return a valid input stream for old module urls
	 */
	public static InputStream getURLStream(URL url) {
		InputStream in = null;
		try {
			URLConnection uc = url.openConnection();
			uc.setDefaultUseCaches(false);
			uc.setUseCaches(false);
			uc.setRequestProperty("Cache-Control", "max-age=0,no-cache");
			uc.setRequestProperty("Pragma", "no-cache");
			
			log.debug("Logging an attempt to connect to: " + url);
			
			in = openConnectionCheckRedirects(uc);
		}
		catch (IOException io) {
			log.warn("io while reading: " + url, io);
		}
		
		return in;
	}
	
	/**
	 * Convenience method to follow http to https redirects. Will follow a total of 5 redirects,
	 * then fail out due to foolishness on the url's part.
	 *
	 * @param c the {@link URLConnection} to open
	 * @return an {@link InputStream} that is not necessarily at the same url, possibly at a 403
	 *         redirect.
	 * @throws IOException
	 * @see #getURLStream(URL)
	 */
	protected static InputStream openConnectionCheckRedirects(URLConnection c) throws IOException {
		boolean redir;
		int redirects = 0;
		InputStream in;
		do {
			if (c instanceof HttpURLConnection) {
				((HttpURLConnection) c).setInstanceFollowRedirects(false);
			}
			// We want to open the input stream before getting headers
			// because getHeaderField() et al swallow IOExceptions.
			in = c.getInputStream();
			redir = false;
			if (c instanceof HttpURLConnection) {
				HttpURLConnection http = (HttpURLConnection) c;
				int stat = http.getResponseCode();
				if (stat == 300 || stat == 301 || stat == 302 || stat == 303 || stat == 305 || stat == 307) {
					URL base = http.getURL();
					String loc = http.getHeaderField("Location");
					URL target = null;
					if (loc != null) {
						target = new URL(base, loc);
					}
					http.disconnect();
					// Redirection should be allowed only for HTTP and HTTPS
					// and should be limited to 5 redirects at most.
					if (target == null || !("http".equals(target.getProtocol()) || "https".equals(target.getProtocol()))
					        || redirects >= 5) {
						throw new SecurityException("illegal URL redirect");
					}
					redir = true;
					c = target.openConnection();
					redirects++;
				}
			}
		} while (redir);
		return in;
	}
	
	/**
	 * Downloads the contents of a URL and copies them to a string (Borrowed from oreilly)
	 *
	 * @param url
	 * @return String contents of the URL
	 * <strong>Should</strong> return an update rdf page for old https dev urls
	 * <strong>Should</strong> return an update rdf page for old https module urls
	 * <strong>Should</strong> return an update rdf page for module urls
	 */
	public static String getURL(URL url) {
		InputStream in = null;
		ByteArrayOutputStream out = null;
		String output = "";
		try {
			in = getURLStream(url);
			if (in == null) {
				// skip this module if updateURL is not defined
				return "";
			}
			
			out = new ByteArrayOutputStream();
			OpenmrsUtil.copyFile(in, out);
			output = out.toString(StandardCharsets.UTF_8.name());
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
			if (StringUtils.isNotEmpty(updateURL)) {
				try {
					// get the contents pointed to by the url
					URL url = new URL(updateURL);
					if (!url.toString().endsWith(ModuleConstants.UPDATE_FILE_NAME)) {
						log.warn("Illegal url: " + url);
						continue;
					}
					String content = getURL(url);
					
					// skip empty or invalid updates
					if ("".equals(content)) {
						continue;
					}
					
					// process and parse the contents
					UpdateFileParser parser = new UpdateFileParser(content);
					parser.parse();
					
					log.debug("Update for mod: " + mod.getModuleId() + " compareVersion result: "
					        + compareVersion(mod.getVersion(), parser.getCurrentVersion()));
					
					// check the update.rdf version against the installed version
					if (compareVersion(mod.getVersion(), parser.getCurrentVersion()) < 0) {
						if (mod.getModuleId().equals(parser.getModuleId())) {
							mod.setDownloadURL(parser.getDownloadURL());
							mod.setUpdateVersion(parser.getCurrentVersion());
							updateFound = true;
						} else {
							log.warn("Module id does not match in update.rdf:" + parser.getModuleId());
						}
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
		if (prop == null) {
			prop = properties.getProperty(ModuleConstants.RUNTIMEPROPERTY_ALLOW_ADMIN, "false");
		}
		
		return "true".equals(prop);
	}
	
	/**
	 * @see ModuleUtil#refreshApplicationContext(AbstractRefreshableApplicationContext, boolean, Module)
	 */
	public static AbstractRefreshableApplicationContext refreshApplicationContext(AbstractRefreshableApplicationContext ctx) {
		return refreshApplicationContext(ctx, false, null);
	}
	
	/**
	 * Refreshes the given application context "properly" in OpenMRS. Will first shut down the
	 * Context and destroy the classloader, then will refresh and set everything back up again.
	 *
	 * @param ctx Spring application context that needs refreshing.
	 * @param isOpenmrsStartup if this refresh is being done at application startup.
	 * @param startedModule the module that was just started and waiting on the context refresh.
	 * @return AbstractRefreshableApplicationContext The newly refreshed application context.
	 */
	public static AbstractRefreshableApplicationContext refreshApplicationContext(AbstractRefreshableApplicationContext ctx,
	        boolean isOpenmrsStartup, Module startedModule) {
		//notify all started modules that we are about to refresh the context
		Set<Module> startedModules = new LinkedHashSet<>(ModuleFactory.getStartedModulesInOrder());
		for (Module module : startedModules) {
			try {
				if (module.getModuleActivator() != null) {
					Thread.currentThread().setContextClassLoader(ModuleFactory.getModuleClassLoader(module));
					module.getModuleActivator().willRefreshContext();
				}
			}
			catch (Exception e) {
				log.warn("Unable to call willRefreshContext() method in the module's activator", e);
			}
		}
		
		OpenmrsClassLoader.saveState();
		SchedulerUtil.shutdown();
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
		SchedulerUtil.startup(Context.getRuntimeProperties());
		
		OpenmrsClassLoader.setThreadsToNewClassLoader();
		
		// reload the advice points that were lost when refreshing Spring
		log.debug("Reloading advice for all started modules: {}", startedModules.size());
		
		try {
			//The call backs in this block may need lazy loading of objects
			//which will fail because we use an OpenSessionInViewFilter whose opened session
			//was closed when the application context was refreshed as above.
			//So we need to open another session now. TRUNK-3739
			Context.openSessionWithCurrentUser();
			for (Module module : startedModules) {
				if (!module.isStarted()) {
					continue;
				}
				
				ModuleFactory.loadAdvice(module);
				try {
					ModuleFactory.passDaemonToken(module);
					
					if (module.getModuleActivator() != null) {
						module.getModuleActivator().contextRefreshed();
						try {
							//if it is system start up, call the started method for all started modules
							if (isOpenmrsStartup) {
								module.getModuleActivator().started();
							}
							//if refreshing the context after a user started or uploaded a new module
							else if (!isOpenmrsStartup && module.equals(startedModule)) {
								module.getModuleActivator().started();
							}
						}
						catch (Exception e) {
							log.warn("Unable to invoke started() method on the module's activator", e);
							ModuleFactory.stopModule(module, true, true);
						}
					}
					
				}
				catch (Exception e) {
					log.warn("Unable to invoke method on the module's activator ", e);
				}
			}
		}
		finally {
			Context.closeSessionWithCurrentUser();
		}
		
		return ctx;
	}
	
	/**
	 * Looks at the &lt;moduleid&gt;.mandatory properties and at the currently started modules to make
	 * sure that all mandatory modules have been started successfully.
	 *
	 * @throws ModuleException if a mandatory module isn't started
	 * <strong>Should</strong> throw ModuleException if a mandatory module is not started
	 */
	protected static void checkMandatoryModulesStarted() throws ModuleException {
		
		List<String> mandatoryModuleIds = getMandatoryModules();
		Set<String> startedModuleIds = ModuleFactory.getStartedModulesMap().keySet();
		
		mandatoryModuleIds.removeAll(startedModuleIds);
		
		// any module ids left in the list are not started
		if (!mandatoryModuleIds.isEmpty()) {
			throw new MandatoryModuleException(mandatoryModuleIds);
		}
	}
	
	/**
	 * Looks at the list of modules in {@link ModuleConstants#CORE_MODULES} to make sure that all
	 * modules that are core to OpenMRS are started and have at least a minimum version that OpenMRS
	 * needs.
	 *
	 * @throws ModuleException if a module that is core to OpenMRS is not started
	 * <strong>Should</strong> throw ModuleException if a core module is not started
	 */
	protected static void checkOpenmrsCoreModulesStarted() throws OpenmrsCoreModuleException {
		
		// if there is a property telling us to ignore required modules, drop out early
		if (ignoreCoreModules()) {
			return;
		}
		
		// make a copy of the constant so we can modify the list
		Map<String, String> coreModules = new HashMap<>(ModuleConstants.CORE_MODULES);
		
		Collection<Module> startedModules = ModuleFactory.getStartedModulesMap().values();
		
		// loop through the current modules and test them
		for (Module mod : startedModules) {
			String moduleId = mod.getModuleId();
			if (coreModules.containsKey(moduleId)) {
				String coreReqVersion = coreModules.get(moduleId);
				if (compareVersion(mod.getVersion(), coreReqVersion) >= 0) {
					coreModules.remove(moduleId);
				} else {
					log.debug("Module: " + moduleId + " is a core module and is started, but its version: "
					        + mod.getVersion() + " is not within the required version: " + coreReqVersion);
				}
			}
		}
		
		// any module ids left in the list are not started
		if (coreModules.size() > 0) {
			throw new OpenmrsCoreModuleException(coreModules);
		}
	}
	
	/**
	 * Uses the runtime properties to determine if the core modules should be enforced or not.
	 *
	 * @return true if the core modules list can be ignored.
	 */
	public static boolean ignoreCoreModules() {
		String ignoreCoreModules = Context.getRuntimeProperties().getProperty(ModuleConstants.IGNORE_CORE_MODULES_PROPERTY,
		    "false");
		return Boolean.parseBoolean(ignoreCoreModules);
	}
	
	/**
	 * Returns all modules that are marked as mandatory. Currently this means there is a
	 * &lt;moduleid&gt;.mandatory=true global property.
	 *
	 * @return list of modules ids for mandatory modules
	 * <strong>Should</strong> return mandatory module ids
	 */
	public static List<String> getMandatoryModules() {
		
		List<String> mandatoryModuleIds = new ArrayList<>();
		
		try {
			List<GlobalProperty> props = Context.getAdministrationService().getGlobalPropertiesBySuffix(".mandatory");
			
			for (GlobalProperty prop : props) {
				if ("true".equalsIgnoreCase(prop.getPropertyValue())) {
					mandatoryModuleIds.add(prop.getProperty().replace(".mandatory", ""));
				}
			}
		}
		catch (Exception e) {
			log.warn("Unable to get the mandatory module list", e);
		}
		
		return mandatoryModuleIds;
	}
	
	/**
	 * <pre>
	 * Gets the module that should handle a path. The path you pass in should be a module id (in
	 * path format, i.e. /ui/springmvc, not ui.springmvc) followed by a resource. Something like
	 * the following:
	 *   /ui/springmvc/css/ui.css
	 *
	 * The first running module out of the following would be returned:
	 *   ui.springmvc.css
	 *   ui.springmvc
	 *   ui
	 * </pre>
	 *
	 * @param path
	 * @return the running module that matches the most of the given path
	 * <strong>Should</strong> handle ui springmvc css ui dot css when ui dot springmvc module is running
	 * <strong>Should</strong> handle ui springmvc css ui dot css when ui module is running
	 * <strong>Should</strong> return null for ui springmvc css ui dot css when no relevant module is running
	 */
	public static Module getModuleForPath(String path) {
		int ind = path.lastIndexOf('/');
		if (ind <= 0) {
			throw new IllegalArgumentException(
			        "Input must be /moduleId/resource. Input needs a / after the first character: " + path);
		}
		String moduleId = path.startsWith("/") ? path.substring(1, ind) : path.substring(0, ind);
		moduleId = moduleId.replace('/', '.');
		// iterate over progressively shorter module ids
		while (true) {
			Module mod = ModuleFactory.getStartedModuleById(moduleId);
			if (mod != null) {
				return mod;
			}
			// try the next shorter module id
			ind = moduleId.lastIndexOf('.');
			if (ind < 0) {
				break;
			}
			moduleId = moduleId.substring(0, ind);
		}
		return null;
	}
	
	/**
	 * Takes a global path and returns the local path within the specified module. For example
	 * calling this method with the path "/ui/springmvc/css/ui.css" and the ui.springmvc module, you
	 * would get "/css/ui.css".
	 *
	 * @param module
	 * @param path
	 * @return local path
	 * <strong>Should</strong> handle ui springmvc css ui dot css example
	 */
	public static String getPathForResource(Module module, String path) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		return path.substring(module.getModuleIdAsPath().length());
	}
	
	/**
	 * This loops over all FILES in this jar to get the package names. If there is an empty
	 * directory in this jar it is not returned as a providedPackage.
	 *
	 * @param file jar file to look into
	 * @return list of strings of package names in this jar
	 */
	public static Collection<String> getPackagesFromFile(File file) {
		
		// End early if we're given a non jar file
		if (!file.getName().endsWith(".jar")) {
			return Collections.emptySet();
		}
		
		Set<String> packagesProvided = new HashSet<>();
		
		JarFile jar = null;
		try {
			jar = new JarFile(file);
			
			Enumeration<JarEntry> jarEntries = jar.entries();
			while (jarEntries.hasMoreElements()) {
				JarEntry jarEntry = jarEntries.nextElement();
				if (jarEntry.isDirectory()) {
					// skip over directory entries, we only care about files
					continue;
				}
				String name = jarEntry.getName();
				
				// Skip over some folders in the jar/omod
				if (name.startsWith("lib") || name.startsWith("META-INF") || name.startsWith("web/module")) {
					continue;
				}
				
				Integer indexOfLastSlash = name.lastIndexOf("/");
				if (indexOfLastSlash <= 0) {
					continue;
				}
				String packageName = name.substring(0, indexOfLastSlash);
				
				packageName = packageName.replaceAll("/", ".");
				
				if (packagesProvided.add(packageName) && log.isTraceEnabled()) {
					log.trace("Adding module's jarentry with package: " + packageName);
				}
			}
			
			jar.close();
		}
		catch (IOException e) {
			log.error("Error while reading file: " + file.getAbsolutePath(), e);
		}
		finally {
			if (jar != null) {
				try {
					jar.close();
				}
				catch (IOException e) {
					// Ignore quietly
				}
			}
		}
		
		return packagesProvided;
	}
	
	/**
	 * Get a resource as from the module's api jar. Api jar should be in the omod's lib folder.
	 * 
	 * @param jarFile omod file loaded as jar
	 * @param moduleId id of the module
	 * @param version version of the module
	 * @param resource name of a resource from the api jar
	 * @return resource as an input stream or <code>null</code> if resource cannot be loaded
	 * <strong>Should</strong> load file from api as input stream
	 * <strong>Should</strong> return null if api is not found
	 * <strong>Should</strong> return null if file is not found in api
	 */
	public static InputStream getResourceFromApi(JarFile jarFile, String moduleId, String version, String resource) {
		String apiLocation = "lib/" + moduleId + "-api-" + version + ".jar";
		return getResourceFromInnerJar(jarFile, apiLocation, resource);
	}
	
	/**
	 * Load resource from a jar inside a jar.
	 * 
	 * @param outerJarFile jar file that contains a jar file
	 * @param innerJarFileLocation inner jar file location relative to the outer jar
	 * @param resource path to a resource relative to the inner jar
	 * @return resource from the inner jar as an input stream or <code>null</code> if resource cannot be loaded
	 */
	private static InputStream getResourceFromInnerJar(JarFile outerJarFile, String innerJarFileLocation, String resource) {
		File tempFile = null;
		FileOutputStream tempOut = null;
		JarFile innerJarFile = null;
		InputStream innerInputStream = null;
		try {
			tempFile = File.createTempFile("tempFile", "jar");
			tempOut = new FileOutputStream(tempFile);
			ZipEntry innerJarFileEntry = outerJarFile.getEntry(innerJarFileLocation);
			if (innerJarFileEntry != null) {
				IOUtils.copy(outerJarFile.getInputStream(innerJarFileEntry), tempOut);
				innerJarFile = new JarFile(tempFile);
				ZipEntry targetEntry = innerJarFile.getEntry(resource);
				if (targetEntry != null) {
					// clone InputStream to make it work after the innerJarFile is closed
					innerInputStream = innerJarFile.getInputStream(targetEntry);
					byte[] byteArray = IOUtils.toByteArray(innerInputStream);
					return new ByteArrayInputStream(byteArray);
				}
			}
		}
		catch (IOException e) {
			log.error("Unable to get '" + resource + "' from '" + innerJarFileLocation + "' of '" + outerJarFile.getName()
			        + "'", e);
		}
		finally {
			IOUtils.closeQuietly(tempOut);
			IOUtils.closeQuietly(innerInputStream);

			// close inner jar file before attempting to delete temporary file
			try {
				if (innerJarFile != null) {
					innerJarFile.close();
				}
			}
			catch (IOException e) {
				log.warn("Unable to close inner jarfile: " + innerJarFile, e);
			}

			// delete temporary file
			if (tempFile != null && !tempFile.delete()) {
				log.warn("Could not delete temporary jarfile: " + tempFile);
			}
		}
		return null;
	}
	
	/**
	 * Gets the root folder of a module's sources during development
	 * 
	 * @param moduleId the module id
	 * @return the module's development folder is specified, else null
	 */
	public static File getDevelopmentDirectory(String moduleId) {
		String directory = System.getProperty(moduleId + ".development.directory");
		if (StringUtils.isNotBlank(directory)) {
			return new File(directory);
		}
		
		return null;
	}
}
