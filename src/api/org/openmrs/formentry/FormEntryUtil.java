package org.openmrs.formentry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FormEntryUtil {

	private static Log log = LogFactory.getLog(FormEntryUtil.class);

	public static void startup(Properties p) {

		// Override the FormEntry constants if specified by the user

		String val = p.getProperty("formentry.infopath.server_url", null);
		if (val != null)
			FormEntryConstants.FORMENTRY_INFOPATH_SERVER_URL = val;

		val = p.getProperty("formentry.infopath.publish_url", null);
		if (val != null)
			FormEntryConstants.FORMENTRY_INFOPATH_PUBLISH_URL = val;

		val = p.getProperty("formentry.infopath.taskpane_caption", null);
		if (val != null)
			FormEntryConstants.FORMENTRY_INFOPATH_TASKPANE_CAPTION = val;

		val = p.getProperty("formentry.infopath.initial_url", null);
		if (val != null)
			FormEntryConstants.FORMENTRY_INFOPATH_TASKPANE_INITIAL_URL = val;

		val = p.getProperty("formentry.infopath.submit_url", null);
		if (val != null)
			FormEntryConstants.FORMENTRY_INFOPATH_SUBMIT_URL = val;

		val = p.getProperty("formentry.infopath.output_dir", null);
		if (val != null)
			FormEntryConstants.FORMENTRY_INFOPATH_OUTPUT_DIR = val;

		val = p.getProperty("formentry.starter_xsn_folder_path", null);
		if (val != null)
			FormEntryConstants.FORMENTRY_STARTER_XSN_FOLDER_PATH = val;

		val = p.getProperty("formentry.infopath.archive_dir", null);
		if (val != null)
			FormEntryConstants.FORMENTRY_INFOPATH_ARCHIVE_DIR = val;

		val = p.getProperty("formentry.infopath_archive_date_format", null);
		if (val != null)
			FormEntryConstants.FORMENTRY_INFOPATH_ARCHIVE_DATE_FORMAT = val;

	}

	public static void startup() {

	}

	/**
	 * Expand the xsn at <code>xsnFilePath</code> into a temp dir
	 * 
	 * @param xsnFilePath
	 * @return Directory in temp dir containing xsn contents
	 * @throws IOException
	 */
	public static File expandXsn(String xsnFilePath) throws IOException {
		File xsnFile = new File(xsnFilePath);
		if (!xsnFile.exists())
			return null;

		File tempDir = createTempDirectory("XSN");
		if (tempDir == null)
			throw new IOException("Failed to create temporary directory");

		String cmd = "expand -F:* \"" + xsnFilePath + "\" \""
				+ tempDir.getAbsolutePath() + "\"";
		log.debug("executing command: " + cmd);
		String output = execCmd(cmd);
		log.debug("expandXsn output: " + output);

		return tempDir;
	}

	/**
	 * Make an xsn (aka CAB file) with the contents of <code>tempDir</code>
	 * 
	 * @param tempDir
	 */
	public static void makeCab(File tempDir) {
		// """calls MakeCAB to make a CAB file from DDF in tempdir directory"""

		String cmd = "makecab /F \"" + tempDir.getAbsolutePath()
				+ "\\publish.ddf\"";
		log.debug("executing command: " + cmd);
		String output = execCmd(cmd);
		log.debug("make cab output: " + output);
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

	/**
	 * Create a temporary directory with the given prefix and a random suffix
	 * 
	 * @param prefix
	 * @return New temp directory pointer
	 * @throws IOException
	 */
	public static File createTempDirectory(String prefix) throws IOException {
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

	/**
	 * Finds the given filename in the given dir
	 * 
	 * @param dir
	 * @param filename
	 * @return File or null if not found
	 */
	public static File findFile(File dir, String filename) {
		File file = null;
		for (File f : dir.listFiles()) {
			if (f.getName().equalsIgnoreCase(filename)) {
				file = f;
				break;
			}
		}
		return file;
	}

	/**
	 * Create ddf that the makeCab exe uses to compile the xsn
	 * 
	 * @param xsnDir
	 * @param outputDir
	 * @param outputFileName
	 */
	public static void createDdf(File xsnDir, String outputDir,
			String outputFileName) {
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
				+ ".Set DiskDirectory1=\""
				+ outputDir.replace("/", File.separator) // allow for either
															// direction of
															// slash
				+ "\"\n";

		log.debug("ddf = " + ddf);

		for (File f : xsnDir.listFiles())
			ddf += "\"" + f.getPath() + "\"\n";

		File ddfFile = new File(xsnDir, "publish.ddf");
		try {
			FileWriter out = new FileWriter(ddfFile);
			out.write(ddf);
			out.close();
		} catch (IOException e) {
			log.error("Could not create DDF file to generate XSN archive", e);
		}
	}
}
