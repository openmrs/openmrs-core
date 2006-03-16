package org.openmrs.formentry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PublishInfoPath { //extends TestCase {

	private static Log log = LogFactory.getLog(PublishInfoPath.class);
	/*
	public void testClass() throws Exception {
		
		String namespace = "http://schema.iukenya.org/amrs/2006/FormEntry/1";
		String fileDir   = "c:\\documents and settings\\bwolfe.rii\\desktop\\java_xsn";
		String filename  = "arvf-4.1-1.xsn";
		String xsnFile   = fileDir + "\\" + filename;

		publishXSN(xsnFile, filename, namespace);
	}
	*/
	
	public static void publishXSN(File file, Form form) throws IOException {
		if (file.exists())
			publishXSN(file.getAbsolutePath(), form);
		else
			publishXSN(new FileInputStream(file), form);
	}
	
	public static void publishXSN(InputStream inputStream, Form form) throws IOException {
		File tempDir = createTempDirectory("UPLOADEDXSN");
		
		log.debug("Temp publish dir: " + tempDir.getAbsolutePath());
		
		// create file on file system to hold the uploaded file
		File filesystemXSN = File.createTempFile("upload", ".xsn", tempDir);
		
		// copy the uploaded file over to the temp file system file
		FileOutputStream out = new FileOutputStream(filesystemXSN);
        byte[] c = new byte[1];
        while (inputStream.read(c) != -1)
           out.write(c);
        out.close();
        
        publishXSN(filesystemXSN.getAbsolutePath(), form);
        
        //deleteDirectory(tempDir);
	}
	
	public static void publishXSN(String xsnFilepath, Form form) throws IOException {
		
		log.debug("publishing xsn at: " + xsnFilepath);
		
		File tempDir = expandXsn(xsnFilepath);
		if (tempDir == null)
			throw new IOException("Filename not found: '" + xsnFilepath + "'");
		
		String outputFilename	= form.getUri();
		String namespace		= form.getSchemaNamespace();
		
		String serverUrl		= "@INFOPATH-SERVER-URL@";
		String publishUrl		= "@INFOPATH-PUBLISH-URL@" + outputFilename;
		String taskPaneCaption	= "@INFOPATH-TASKPANE-CAPTION@"; //"Welcome!";
		String taskPaneInitialUrl = "@INFOPATH-TASKPANE-INITIAL-URL@"; //"http://localhost:8080/amrs/taskPane.htm";
		String submitUrl		= "@INFOPATH-SUBMIT-URL@"; //"http://localhost:8080/amrs/formUpload";
		String schemaFilename	= "@INFOPATH-SCHEMA-FILENAME@"; //"FormEntry.xsd";
		String outputDir		= "@INFOPATH-OUTPUT-DIR@"; //System.getProperty("user.home");
		
		// prepare manifest
		String solutionVersion = prepareManifest(tempDir, publishUrl, namespace,
				taskPaneCaption, taskPaneInitialUrl, submitUrl);

		log.debug("\nsolution version: " + solutionVersion);
		
		// set namespace
		File schema = findFile(tempDir, schemaFilename);
		if (schema == null)
			throw new IOException("Schema: '" + schemaFilename + "' cannot be null");
		String tag  = "xs:schema";
		setNamespace(schema, tag, namespace);
		
		// update server_url in openmrs-infopath.js
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("SERVER_URL", serverUrl);
		vars.put("SUBMIT_URL", submitUrl);
		setVariables(tempDir, "openmrs-infopath.js", vars);
		
		// create ddf
		createDdf(tempDir, outputDir, outputFilename);
		
		// make cab
		makeCab(tempDir);
		
		// clean up
		//deleteDirectory(tempDir);
	}

	private static String prepareManifest(File tempDir, String url, String namespace,
			String taskPaneCaption, String taskPaneInitialUrl, String submitUrl) {
		File manifest = findManifest(tempDir);
		if (manifest == null) {
			log.warn("Missing manifest!");
			return null;
		}

		String solutionVersion = null;

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(manifest);

			Element elem = getSingleElement(doc, "xsf:xDocumentClass");
			if (elem == null) {
				log
						.warn("Could not locate xsf:xDocumentClass element in manifest!");
				return null;
			}
			solutionVersion = elem.getAttribute("solutionVersion");
			if (elem.getAttribute("name") != null)
				elem.removeAttribute("name");
			elem.setAttribute("trustSetting", "manual");
			elem.setAttribute("trustLevel", "domain");
			elem.setAttribute("publishUrl", url);
			elem.setAttribute("xmlns:openmrs", namespace);

			// Find xsf:taskpane element
			elem = getSingleElement(doc, "xsf:taskpane");
			if (elem == null) {
				log.warn("Could not locate xsf:taskpane element within manifest");
				return null;
			}
			elem.setAttribute("caption", taskPaneCaption);
			elem.setAttribute("href", taskPaneInitialUrl);

			elem = getSingleElement(doc, "xsf:useHttpHandler");
			if (elem != null) {
				elem.setAttribute("href", submitUrl);
			}

			writeXml(doc, manifest.getPath());
					//"c:\\documents and settings\\bwolfe.rii\\desktop\\java_xsn\\manifest.xsf");
		} catch (ParserConfigurationException e) {
			log.error("Error parsing form data", e);
		} catch (SAXException e) {
			log.error("Error parsing form data", e);
		} catch (IOException e) {
			log.error("Error parsing form data", e);
		}

		return solutionVersion;
	}

	private static void setNamespace(File file, String tag, String namespace) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);

			Element elem = getSingleElement(doc, tag);
			if (elem == null) {
				log.warn("Could not locate " + tag + " element in "
						+ file.getName());
				return;
			}
			elem.setAttribute("xmlns:openmrs", namespace);
			writeXml(doc, file.getAbsolutePath());
		} catch (ParserConfigurationException e) {
			log.error("Error parsing form data", e);
		} catch (SAXException e) {
			log.error("Error parsing form data", e);
		} catch (IOException e) {
			log.error("Error parsing form data", e);
		}
	}

	private static void createDdf(File tempDir, String outputDir, String outputFileName) {
		String ddf = ";*** MakeCAB Directive file for "
				+ outputFileName
				+ "\n"
				+ ".OPTION EXPLICIT			; generate errors\n"
				+ ".Set CabinetNameTemplate="
				+ outputFileName
				+ "\n"
				+ ".set DiskDirectoryTemplate=CDROM	; all cabinets go in a single directory\n"
				+ ".Set CompressionType=MSZIP		; all files are compressed in cabinet files\n"
				+ ".Set UniqueFiles=\"OFF\"\n" + ".Set Cabinet=on\n"
				+ ".Set DiskDirectory1=\"" + outputDir + "\"\n";
		
		for (File f : tempDir.listFiles())
			ddf += "\"" + f.getPath() + "\"\n";

		File ddfFile = new File(tempDir, "publish.ddf");
		try {
			FileWriter out = new FileWriter(ddfFile);
			out.write(ddf);
			out.close();
		} catch (IOException e) {
			log.error("Could not create DDF file to generate XSN archive", e);
		}
	}

	private static void makeCab(File tempDir) {
		//"""calls MakeCAB to make a CAB file from DDF in tempdir directory"""
		
		String cmd = "makecab /F \"" + tempDir.getAbsolutePath() + "\\publish.ddf\"";
		log.debug("executing command: " + cmd);
		String output = execCmd(cmd);
		log.debug("make cab output: " + output);
	}

	private static File createTempDirectory(String prefix) throws IOException {
		String dirname = System.getProperty("java.io.tmpdir");
		if (dirname == null)
			throw new IOException("Cannot determine system temporary directory");

		File directory = new File(dirname);
		if (!directory.exists())
			throw new IOException("System temporary directory "
					+ directory.getName() + " does not exist.");
		if (!directory.isDirectory())
			throw new IOException("System temporary directory "
					+ directory.getName() + " is not really a directory.");

		File tempDir;
		do {
			String filename = prefix + System.currentTimeMillis();
			tempDir = new File(directory, filename);
		} while (tempDir.exists());

		if (!tempDir.mkdir())
			throw new IOException("Could not create temporary directory '"
					+ tempDir.getAbsolutePath() + "'");
		return tempDir;
	}
	
	private static File expandXsn(String xsn) throws IOException {
		File xsnFile = new File(xsn);
		if (!xsnFile.exists())
			return null;
	
		File tempDir = createTempDirectory("XSN");
		if (tempDir == null)
			throw new IOException("Failed to create temporary directory");
	
		String cmd = "expand -F:* \"" + xsn + "\" \""
				+ tempDir.getAbsolutePath() + "\"";
		log.debug("executing command: " + cmd);
		String output = execCmd(cmd);
		log.debug("expandXsn output: " + output);
		
		return tempDir;
	}

	private static String execCmd(String cmd) {
		String out = "";
		try {
			String line;
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader input = new BufferedReader(new InputStreamReader(p
					.getInputStream()));
			while ((line = input.readLine()) != null) {
				out += line;
			}
			input.close();
		} catch (Exception e) {
			log.error("Error while executing command: '" + cmd + "'", e);
		}
		return out;
	}

	private static File findManifest(File dir) {
		return findFile(dir, "manifest.xsf");
	}
	
	private static File findFile(File dir, String filename) {
		File file = null;
		for (File f : dir.listFiles()) {
			if (f.getName().equalsIgnoreCase(filename)) {
				file = f;
				break;
			}
		}
		return file;
	}

	private static void writeXml(Document doc, String filename) {
		try {
			// Create a transformer
			Transformer xformer = TransformerFactory.newInstance()
					.newTransformer();

			// Set the public and system id
			xformer.setOutputProperty(OutputKeys.METHOD, "xml");

			// Write the DOM document to a file
			Source source = new DOMSource(doc);
			Result result = new StreamResult(new FileOutputStream(filename));
			xformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
		} catch (TransformerException e) {
		} catch (FileNotFoundException e) {
		}
	}

	private static boolean deleteDirectory(File dir) throws IOException {
		if (!dir.exists() || !dir.isDirectory())
			throw new IOException("Could not delete direcotry '"
					+ dir.getAbsolutePath() + "' (not a directory)");
		File[] fileList = dir.listFiles();
		for (File f : fileList) {
			f.delete();
		}
		return dir.delete();
	}
	
	private static void setVariables(File dir, String filename, Map<String, String> vars) throws IOException {
		File file = findFile(dir, filename);
		FileInputStream inputStream = new FileInputStream(file);
		byte[] b = new byte[inputStream.available()];
        inputStream.read(b);
        inputStream.close ();
        String fileContent = new String (b); 
		for (String variableName : vars.keySet()) {
			String regexp = "var\\s" + variableName + "\\s=[^\n]*;";
			String rplcmnt = "var " + variableName + " = \"" + vars.get(variableName) + "\";"; 
			fileContent = fileContent.replaceAll(regexp, rplcmnt);
		}
		try {
			FileWriter out = new FileWriter(file);
			out.write(fileContent);
			out.close();
		} catch (IOException e) {
			log.error("Could not write '" + filename + "'", e);
		}
	}

	private static Element getSingleElement(Document doc, String elemName) {
		Element elem = null;
		NodeList elemList = doc.getElementsByTagName(elemName);
		if (elemList != null && elemList.getLength() > 0)
			elem = (Element) elemList.item(0);
		return elem;
	}
	
}
