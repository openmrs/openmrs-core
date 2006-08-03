package org.openmrs.formentry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.openmrs.api.context.Context;
import org.openmrs.util.Helper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.SAXException;

/**
 * Performs the <em>publish</em> process for InfoPath forms. Publishing an
 * InfoPath form requires that multiple URL references and some specific XML
 * attributes are altered within the contents of the XSN file.
 * 
 * @author Ben Wolfe
 * @author Burke Mamlin
 * @version 1.0
 */
public class PublishInfoPath {

	private static Log log = LogFactory.getLog(PublishInfoPath.class);

	/**
	 * Public access method for publishing an InfoPath&reg; form (XSN file). The
	 * given file is expanded into its constituents and the various URL and
	 * schema references within those files are updated before the files are
	 * re-constituted into an XSN archive.
	 * 
	 * @param file
	 *            the XSN file to be published
	 * @param form
	 *            the OpenMRS form with which the given XSN is to be associated
	 */
	public static void publishXSN(File file, Context context)
			throws IOException {
		if (file.exists())
			publishXSN(file.getAbsolutePath(), context);
		else
			publishXSN(new FileInputStream(file), context);
	}

	/**
	 * Public access method for publishing an InfoPath&reg; form (XSN file). The
	 * given file is expanded into its constituents and the various URL and
	 * schema references within those files are updated before the files are
	 * re-constituted into an XSN archive.
	 * 
	 * @param inputStream
	 *            inputStream from which XSN may be read
	 * @param form
	 *            the OpenMRS form with which the given XSN is to be associated
	 */
	public static void publishXSN(InputStream inputStream, Context context)
			throws IOException {
		File tempDir = FormEntryUtil.createTempDirectory("UPLOADEDXSN");

		log.debug("Temp publish dir: " + tempDir.getAbsolutePath());

		// create file on file system to hold the uploaded file
		File filesystemXSN = File.createTempFile("upload", ".xsn", tempDir);

		// copy the uploaded file over to the temp file system file
		Helper.copyFile(inputStream, new FileOutputStream(filesystemXSN));

		publishXSN(filesystemXSN.getAbsolutePath(), context);

		deleteDirectory(tempDir);
	}

	/**
	 * Public access method for publishing an InfoPath&reg; form (XSN file). The
	 * given file is expanded into its constituents and the various URL and
	 * schema references within those files are updated before the files are
	 * re-constituted into an XSN archive.
	 * 
	 * @param xsnFilePath
	 *            full path to the XSN file
	 * @param form
	 *            the OpenMRS form with which the given XSN is to be associated
	 */
	public static void publishXSN(String xsnFilePath, Context context)
			throws IOException {

		log.debug("publishing xsn at: " + xsnFilePath);

		File tempDir = FormEntryUtil.expandXsn(xsnFilePath);
		if (tempDir == null)
			throw new IOException("Filename not found: '" + xsnFilePath + "'");

		Form form = determineForm(tempDir, context);
		String originalFormUri = FormEntryUtil.getFormUri(form);
		form.setBuild(form.getBuild() + 1);

		String outputFilename = FormEntryUtil.getFormUri(form);
		String namespace = FormEntryUtil.getFormSchemaNamespace(form);
		String solutionVersion = FormEntryUtil.getSolutionVersion(form);
		log.debug("solution version: " + solutionVersion);

		String serverUrl = FormEntryConstants.FORMENTRY_INFOPATH_SERVER_URL; // "@FORMENTRY-INFOPATH-SERVER_URL@";
		String publishUrl = FormEntryConstants.FORMENTRY_INFOPATH_PUBLISH_URL
				+ outputFilename;
		String taskPaneCaption = FormEntryConstants.FORMENTRY_INFOPATH_TASKPANE_CAPTION; // "Welcome!";
		String taskPaneInitialUrl = FormEntryConstants.FORMENTRY_INFOPATH_TASKPANE_INITIAL_URL; // "http://localhost:8080/amrs/taskPane.htm";
		String submitUrl = FormEntryConstants.FORMENTRY_INFOPATH_SUBMIT_URL; // "http://localhost:8080/amrs/formUpload";
		String schemaFilename = FormEntryConstants.FORMENTRY_DEFAULT_SCHEMA_NAME; // "FormEntry.xsd";
		String outputDirName = FormEntryConstants.FORMENTRY_INFOPATH_OUTPUT_DIR; // System.getProperty("user.home");
		
		// ensure that output directory exists
		File outputDir = new File(outputDirName);
		if (!outputDir.exists())
			outputDir.mkdirs();
		if (!outputDir.exists() || !outputDir.isDirectory())
			throw new IOException("Could not create or find output directory for forms (" + outputDirName + ")");
		
		// prepare manifest
		prepareManifest(tempDir, publishUrl, namespace, solutionVersion,
				taskPaneCaption, taskPaneInitialUrl, submitUrl);

		// set schema
		File schema = FormEntryUtil.findFile(tempDir, schemaFilename);
		if (schema == null)
			throw new IOException("Schema: '" + schemaFilename
					+ "' cannot be null");
		String tag = "xs:schema";
		setNamespace(schema, tag, namespace);

		// Ensure that we have a template with default scripts
		String templateWithDefaults;
		File templateWithDefaultsFile = FormEntryUtil.findFile(tempDir,
				FormEntryConstants.FORMENTRY_DEFAULT_DEFAULTS_NAME);
		if (templateWithDefaultsFile == null) {
			// if template containing defaults is missing, create one on the fly
			templateWithDefaults = new FormXmlTemplateBuilder(context, form,
					publishUrl).getXmlTemplate(true);
			try {
				FileWriter out = new FileWriter(templateWithDefaultsFile);
				out.write(templateWithDefaults);
				out.close();
			} catch (IOException e) {
				log.error("Could not write '"
						+ FormEntryConstants.FORMENTRY_DEFAULT_DEFAULTS_NAME
						+ "'", e);
			}
		} else {
			prepareTemplate(tempDir,
					FormEntryConstants.FORMENTRY_DEFAULT_DEFAULTS_NAME,
					solutionVersion, publishUrl);
			templateWithDefaults = readFile(templateWithDefaultsFile);
		}

		// update InfoPath solutionVersion within all XML template documents
		prepareTemplate(tempDir,
				FormEntryConstants.FORMENTRY_DEFAULT_TEMPLATE_NAME,
				solutionVersion, publishUrl);
		prepareTemplate(tempDir,
				FormEntryConstants.FORMENTRY_DEFAULT_SAMPLEDATA_NAME,
				solutionVersion, publishUrl);

		// update server_url in openmrs-infopath.js
		Map<String, String> vars = new HashMap<String, String>();
		vars.put(FormEntryConstants.FORMENTRY_SERVER_URL_VARIABLE_NAME,
				serverUrl);
		setVariables(tempDir,
				FormEntryConstants.FORMENTRY_DEFAULT_JSCRIPT_NAME, vars);

		// create ddf
		FormEntryUtil.createDdf(tempDir, outputDirName, outputFilename);

		// Copy XSN file to archive
		String archiveDir = FormEntryConstants.FORMENTRY_INFOPATH_ARCHIVE_DIR;
		File originalFile = new File(outputDirName, originalFormUri);
		if (archiveDir != null && originalFile.exists()) {
			String xsnArchiveFilePath = originalFormUri
					+ "-"
					+ form.getVersion()
					+ "-"
					+ form.getBuild()
					+ "-"
					+ new SimpleDateFormat(
							FormEntryConstants.FORMENTRY_INFOPATH_ARCHIVE_DATE_FORMAT,
							context.getLocale()).format(new Date()) + ".xsn";
			File xsnArchiveFile = new File(archiveDir, xsnArchiveFilePath);
			boolean success = moveFile(originalFile, xsnArchiveFile);
			if (!success) {
				log.warn("Unable to archive XSN " + xsnFilePath + " to "
						+ xsnArchiveFilePath);
			}
		}

		// make cab
		FormEntryUtil.makeCab(tempDir);

		// clean up
		deleteDirectory(tempDir);

		// update template, solution version, and build number on server
		form.setTemplate(templateWithDefaults);
		context.getFormService().updateForm(form);
	}

	// Prepare template file (update solutionVersion and href)
	private static void prepareTemplate(File tempDir, String fileName,
			String solutionVersion, String publishUrl) {
		File file = new File(tempDir, fileName);
		if (file == null) {
			log.warn("Missing file: '" + fileName + "'");
			return;
		}
		Document doc = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(file);
			Node root = doc.getDocumentElement().getParentNode();
			NodeList children = root.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node node = children.item(i);
				if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE
						&& node.getNodeName().equals("mso-infoPathSolution")) {
					ProcessingInstruction pi = (ProcessingInstruction) node;
					String data = pi.getData();
					data = data.replaceAll(
							"(\\ssolutionVersion\\s*=\\s*\")[^\"]+\"", "$1"
									+ solutionVersion + "\"");
					data = data.replaceAll("(\\shref\\s*=\\s*\")[^\"]+\"", "$1"
							+ publishUrl + "\"");
					pi.setData(data);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		writeXml(doc, file.getAbsolutePath());
	}

	// Convenience method for copying a file from one location to another
	// @returns true if copy was successful
	private static boolean moveFile(File from, File to) {
		boolean success = false;
		try {
			// Create channel on the source
			FileChannel srcChannel = new FileInputStream(from).getChannel();

			// Create channel on the destination
			FileChannel dstChannel = new FileOutputStream(to).getChannel();

			// Copy file contents from source to destination
			dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

			// Close the channels
			srcChannel.close();
			dstChannel.close();

			from.delete();

			// report successful copy
			success = true;
		} catch (IOException e) {
		}
		return success;
	}

	private static Form determineForm(File tempDir, Context context) {
		File xsd = FormEntryUtil.findFile(tempDir, "FormEntry.xsd");
		Form form = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(xsd);
			Element parent = getSingleElement(doc
					.getElementsByTagName("xs:element"), "form");
			if (parent == null) {
				log.warn("Could not locate xs:element element in xsd!");
				return null;
			}
			Element elem = getSingleElement(parent
					.getElementsByTagName("xs:attribute"), "id");
			if (elem == null) {
				log.warn("Could not locate xs:attribute element in xsd!");
				return null;
			}

			Integer formId = Integer.valueOf(elem.getAttribute("fixed"));
			form = context.getFormEntryService().getForm(formId);

		} catch (ParserConfigurationException e) {
			log.error("Error parsing form data", e);
		} catch (SAXException e) {
			log.error("Error parsing form data", e);
		} catch (IOException e) {
			log.error("Error parsing form data", e);
		}

		return form;
	}

	private static void prepareManifest(File tempDir, String url,
			String namespace, String solutionVersion, String taskPaneCaption,
			String taskPaneInitialUrl, String submitUrl) {
		File manifest = findManifest(tempDir);
		if (manifest == null) {
			log.warn("Missing manifest!");
			return;
		}

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(manifest);

			Element elem = getSingleElement(doc, "xsf:xDocumentClass");
			if (elem == null) {
				log
						.warn("Could not locate xsf:xDocumentClass element in manifest!");
				return;
			}
			elem.setAttribute("solutionVersion", solutionVersion);
			if (elem.getAttribute("name") != null)
				elem.removeAttribute("name");
			elem.setAttribute("trustSetting", "manual");
			elem.setAttribute("trustLevel", "domain");
			elem.setAttribute("publishUrl", url);
			elem.setAttribute("xmlns:openmrs", namespace);

			// Find xsf:taskpane element
			elem = getSingleElement(doc, "xsf:taskpane");
			if (elem != null) {
				elem.setAttribute("caption", taskPaneCaption);
				elem.setAttribute("href", taskPaneInitialUrl);
			} else {
				log
						.warn("Could not locate xsf:taskpane element within manifest");
			}

			elem = getSingleElement(doc, "xsf:useHttpHandler");
			if (elem != null) {
				elem.setAttribute("href", submitUrl);
			}

			writeXml(doc, manifest.getPath());

		} catch (ParserConfigurationException e) {
			log.error("Error parsing form data", e);
		} catch (SAXException e) {
			log.error("Error parsing form data", e);
		} catch (IOException e) {
			log.error("Error parsing form data", e);
		}

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

	private static File findManifest(File dir) {
		return FormEntryUtil.findFile(dir, "manifest.xsf");
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

	private static void setVariables(File dir, String filename,
			Map<String, String> vars) throws IOException {
		File file = FormEntryUtil.findFile(dir, filename);
		String fileContent = readFile(file);
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

	private static String readFile(File file) throws IOException {
		FileInputStream inputStream = new FileInputStream(file);
		byte[] b = new byte[inputStream.available()];
		inputStream.read(b);
		inputStream.close();
		return new String(b);
	}

	private static Element getSingleElement(Document doc, String tagName) {
		Element elem = null;
		NodeList elemList = doc.getElementsByTagName(tagName);
		if (elemList != null && elemList.getLength() > 0)
			elem = (Element) elemList.item(0);
		return elem;
	}

	private static Element getSingleElement(NodeList elemList,
			String nameAttrValue) {
		Element elem = null;
		if (elemList != null) {
			if (elemList.getLength() > 0) {
				for (Integer i = 0; i < elemList.getLength(); i++) {
					elem = (Element) elemList.item(0);
					if (elem.getAttribute("name").equals(nameAttrValue))
						break;
				}
			} else {
				elem = (Element) elemList.item(0);
			}
		}
		return elem;
	}
}
