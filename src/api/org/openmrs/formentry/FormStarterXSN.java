package org.openmrs.formentry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
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
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.util.Helper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Generates a 'starter' XSN. This starter is essentially a blank XSN template
 * to play with in Infopath. The schema and template are generated and inserted
 * into the XSN.
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public class FormStarterXSN {

	private Log log = LogFactory.getLog(FormStarterXSN.class);

	private String schema;

	private String template;

	private Form form;

	public FormStarterXSN(Context context, Form form, String url) {
		this.form = form;
		schema = new FormSchemaBuilder(context, form).getSchema();
		template = new FormXmlTemplateBuilder(context, form, url)
				.getXmlTemplate((Patient)null);
		template = template.replaceAll("@SESSION@", "");
	}

	public String getXSN() throws IOException {
		File xsnFile = getXSNFile();
		File dir = xsnFile.getParentFile();
		
		// read the file into the return string
		String xsn = Helper.getFileAsString(xsnFile);

		// clean up
		deleteDirectory(dir);

		return xsn;
	}
	
	public void copyXSNToStream(OutputStream outputStream) throws IOException {
		File xsnFile = getXSNFile();
		File dir = xsnFile.getParentFile();
		
		FileInputStream formInputStream = new FileInputStream(xsnFile);

		// copy xsn file to the response output
		Helper.copyFile(formInputStream, outputStream);
		
		// clean up
		deleteDirectory(dir);
	}
	
	private File getXSNFile() throws IOException {

		String xsnFolderPath = FormEntryConstants.FORMENTRY_STARTER_XSN_FOLDER_PATH;
		log.debug("Getting starter XSN contents: " + xsnFolderPath);

		File tempDir = copyFile(xsnFolderPath);
		if (tempDir == null)
			throw new IOException("Filename not found: '" + xsnFolderPath + "'");

		String outputDir = tempDir.getAbsolutePath();
		String outputFilename = "starter_template.xsn";

		String namespace = form.getSchemaNamespace();

		String serverUrl = FormEntryConstants.FORMENTRY_INFOPATH_SERVER_URL;
		String publishUrl = FormEntryConstants.FORMENTRY_INFOPATH_PUBLISH_URL + outputFilename;
		String submitUrl = FormEntryConstants.FORMENTRY_INFOPATH_SUBMIT_URL; // "http://localhost:8080/amrs/formUpload";
		String schemaFilename = FormEntryConstants.FORMENTRY_DEFAULT_SCHEMA_NAME; // "FormEntry.xsd";
		String templateFilename = FormEntryConstants.FORMENTRY_DEFAULT_TEMPLATE_NAME;
		String sampleDataFilename = FormEntryConstants.FORMENTRY_DEFAULT_SAMPLEDATA_NAME;

		// prepare manifest
		String solutionVersion = prepareManifest(tempDir, publishUrl, namespace);

		if (solutionVersion == null)
			log.warn("Solution Version is null");

		log.debug("\nsolution version: " + solutionVersion);

		// replace FormEntry.xsd with the generated schema
		File schemaFile = FormEntryUtil.findFile(tempDir, schemaFilename);
		if (schemaFile == null)
			throw new IOException("Schema: '" + schemaFilename
					+ "' cannot be null");
		FileWriter schemaOutput = new FileWriter(schemaFile, false);
		schemaOutput.write(schema);
		schemaOutput.close();

		// replace template.xml with the generated xml
		File templateFile = FormEntryUtil.findFile(tempDir, templateFilename);
		if (templateFile == null)
			throw new IOException("Template: '" + templateFilename
					+ "' cannot be null");
		FileWriter templateOutput = new FileWriter(templateFile, false);
		log.debug(template);
		templateOutput.write(template);
		templateOutput.close();

		// replace sampleData.xml with the generated xml
		File sampleDataFile = FormEntryUtil.findFile(tempDir, sampleDataFilename);
		if (sampleDataFile == null)
			throw new IOException("Template: '" + sampleDataFilename
					+ "' cannot be null");
		FileWriter sampleDataOutput = new FileWriter(sampleDataFile, false);
		sampleDataOutput.write(template);
		sampleDataOutput.close();

		// update server_url in openmrs-infopath.js
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("SERVER_URL", serverUrl);
		setVariables(tempDir, "openmrs-infopath.js", vars);

		// create ddf
		FormEntryUtil.createDdf(tempDir, outputDir, outputFilename);

		// make cab
		FormEntryUtil.makeCab(tempDir);

		return FormEntryUtil.findFile(tempDir, outputFilename);
	}

	private String prepareManifest(File tempDir, String url, String namespace) {
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

			writeXml(doc, manifest.getPath());
			// "c:\\documents and
			// settings\\bwolfe.rii\\desktop\\java_xsn\\manifest.xsf");
		} catch (ParserConfigurationException e) {
			log.error("Error parsing form data", e);
		} catch (SAXException e) {
			log.error("Error parsing form data", e);
		} catch (IOException e) {
			log.error("Error parsing form data", e);
		}

		return solutionVersion;
	}

	private File copyFile(String xsnFolderPath) throws IOException {
		File xsnFolder = new File(xsnFolderPath);
		if (!xsnFolder.exists())
			return null;

		// temp directory to hold the new xsn contents
		File tempDir = FormEntryUtil.createTempDirectory("XSN");
		if (tempDir == null)
			throw new IOException("Failed to create temporary directory");

		// iterate over and copy each file in the given folder
		for (File f : xsnFolder.listFiles()) {
			File newFile = new File(tempDir, f.getName());
			FileChannel in = null, out = null;
			try {
				in = new FileInputStream(f).getChannel();
				out = new FileOutputStream(newFile).getChannel();
				in.transferTo(0, in.size(), out);
			} finally {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			}
		}
		return tempDir;
	}

	private File findManifest(File dir) {
		return FormEntryUtil.findFile(dir, "manifest.xsf");
	}

	private void writeXml(Document doc, String filename) {
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

	private boolean deleteDirectory(File dir) throws IOException {
		if (!dir.exists() || !dir.isDirectory())
			throw new IOException("Could not delete direcotry '"
					+ dir.getAbsolutePath() + "' (not a directory)");
		File[] fileList = dir.listFiles();
		for (File f : fileList) {
			f.delete();
		}
		return dir.delete();
	}

	private void setVariables(File dir, String filename,
			Map<String, String> vars) throws IOException {
		File file = FormEntryUtil.findFile(dir, filename);
		if (file == null)
			throw new IOException("Could not find file '" + filename + "'");

		FileInputStream inputStream = new FileInputStream(file);
		byte[] b = new byte[inputStream.available()];
		inputStream.read(b);
		inputStream.close();
		String fileContent = new String(b);
		for (String variableName : vars.keySet()) {
			// \s = whitespace
			String regexp = "var\\s" + variableName + "\\s=[^\n]*;";
			String rplcmnt = "var " + variableName + " = \""
					+ vars.get(variableName) + "\";";
			log.debug("replacing regexp: " + regexp + " with " + rplcmnt);
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

	private Element getSingleElement(Document doc, String tagName) {
		Element elem = null;
		NodeList elemList = doc.getElementsByTagName(tagName);
		if (elemList != null && elemList.getLength() > 0)
			elem = (Element) elemList.item(0);
		return elem;
	}

}
