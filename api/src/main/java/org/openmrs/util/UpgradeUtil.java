package org.openmrs.util;

import org.apache.commons.io.IOUtils;
import org.openmrs.api.APIException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;

public class UpgradeUtil {
	/**
	 * Returns conceptId for the given units from DatabaseUtil#ORDER_ENTRY_UPGRADE_SETTINGS_FILENAME
	 * located in application data directory.
	 *
	 * @param units
	 * @return conceptId
	 * @should return concept_id for units
	 * @should fail if units is not specified
	 */
	public static Integer getConceptIdForUnits(String units) {
		String appDataDir = OpenmrsUtil.getApplicationDataDirectory();
		Properties props = new Properties();
		String conceptId = null;
		File propFile;
		BufferedReader br;
		String propString;
		try {
			propFile = new File(appDataDir + System.getProperty("file.separator") + DatabaseUtil.ORDER_ENTRY_UPGRADE_SETTINGS_FILENAME);
			br = new BufferedReader(new FileReader(propFile));
			// Escape the space to properly read properties.
			propString = IOUtils.toString(br).replaceAll("[^\\S\\n]", "\\\\ ");
			props.load(new StringReader(propString));
			for (Map.Entry prop : props.entrySet()) {
				if (prop.getKey().equals(units)) {
					conceptId = prop.getValue().toString();

					if (conceptId != null) {
						return Integer.valueOf(conceptId);
					} else {
						return null;
					}
				}
			}
		}
		catch (NumberFormatException e) {
			throw new APIException("Your order entry upgrade settings file" + "contains invalid mapping from " + units
			        + " to concept ID " + conceptId
			        + ". ID must be an integer or null. Please refer to upgrade instructions for more details.", e);
		}
		catch (IOException e) {
			if (e instanceof FileNotFoundException) {
				throw new APIException("Unable to find file containing order entry upgrade settings in your "
				        + "application data directory: " + appDataDir
				        + "\nPlease refer to upgrade instructions for more details.", e);
			} else {
				throw new APIException(e);
			}
		}

		throw new APIException("Your order entry upgrade settings file" + " does not have mapping for " + units
		        + ". Please refer to upgrade instructions for more details.");
	}
}
