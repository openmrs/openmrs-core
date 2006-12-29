package org.openmrs.module;

import java.io.StringReader;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * This class will parse an xml update.rdf file
 * 
 * @author bwolfe
 * @version 1.0
 */
public class UpdateFileParser {
	
	private static Log log = LogFactory.getLog(UpdateFileParser.class);
	
	private String content;
	
	private String moduleId = null;
	private String currentVersion = null;
	private String downloadURL = null;
	
	/**
	 * Default constructor
	 * 
	 * @param String to parse (Contents of update.rdf file)
	 */
	public UpdateFileParser(String s) {
		this.content = s;
	}
	
	/**
	 * Parse the contents of the update.rdf file.
	 * 
	 * @throws ModuleException
	 */
	public void parse() throws ModuleException {
		StringReader stringReader = null;
		try {
			Document updateDoc = null;
			try {
				stringReader = new StringReader(content);
				InputSource inputSource = new InputSource(stringReader);
				inputSource.setSystemId("./");
				
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				updateDoc = db.parse(inputSource);
			}
			catch (Exception e) {
				log.warn("Unable to parse content");
				throw new ModuleException("Error parsing update.rdf file: " + content, e);
			}
			
			Element rootNode = updateDoc.getDocumentElement();
			
			String configVersion = rootNode.getAttribute("configVersion");
			
			if (!validConfigVersions().contains(configVersion))
				throw new ModuleException("Invalid configVersion: '" + configVersion + "' found In content: " + content);
			
			this.moduleId = getElement(rootNode, configVersion, "moduleId");
			this.currentVersion = getElement(rootNode, configVersion, "currentVersion");
			this.downloadURL = getElement(rootNode, configVersion, "downloadURL");
		}
		catch (ModuleException e) {
			// rethrow the moduleException
			throw e;
		}
		finally {
			if (stringReader != null)
				stringReader.close();
		}
			
	}
	
	/**
	 * Generic method to get a module tag
	 * @param element
	 * @param version
	 * @param tag
	 * @return
	 */
	private static String getElement(Element element, String version, String tag) {
		if (element.getElementsByTagName(tag).getLength() > 0)
			return element.getElementsByTagName(tag).item(0).getTextContent();
		return "";
	}
	
	/**
	 * List of the valid sqldiff versions
	 * @return
	 */
	private static List<String> validConfigVersions() {
		List<String> versions = new Vector<String>();
		versions.add("1.0");
		return versions;
	}
	
	/**
	 * @return the downloadURL
	 */
	public String getDownloadURL() {
		return downloadURL;
	}

	/**
	 * @return the moduleId
	 */
	public String getModuleId() {
		return moduleId;
	}

	/**
	 * @return the version
	 */
	public String getCurrentVersion() {
		return currentVersion;
	}
	
}
