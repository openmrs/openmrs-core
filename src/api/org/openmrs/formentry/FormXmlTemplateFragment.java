package org.openmrs.formentry;

import java.util.Date;

import org.openmrs.User;

/**
 * Convenience class for generating various fragments of an XML template for
 * OpenMRS forms.
 * 
 * @author Burke Mamlin
 * @version 1.0
 * @see org.openmrs.formentry.FormXmlTemplateBuilder
 */
public class FormXmlTemplateFragment {

	public static String header(String urn, String infoPathSolutionVersion,
			String url) {
		return "<?xml version=\"1.0\"?>\n"
				+ "<?mso-infoPathSolution name=\""
				+ urn
				+ "\" href=\""
				+ url
				+ "\" solutionVersion=\""
				+ infoPathSolutionVersion
				+ "\" productVersion=\"11.0.6357\" PIVersion=\"1.0.0.0\" ?>\n"
				+ "<?mso-application progid=\"InfoPath.Document\"?>\n";
	}

	public static String openForm(Integer formId, String formName,
			String formVersion, String namespace, boolean includeDefaultScripts) {
		return "<form id=\"" + formId + "\" name=\"" + formName + "\" "
				+ "version=\"" + formVersion + "\" "
				+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+ "xmlns:openmrs=\"" + namespace + "\" "
				+ "xmlns:xd=\"http://schemas.microsoft.com/office/infopath/2003\">\n"
				+ "  <header>\n"
				+ "    <enterer>"
				+ (includeDefaultScripts ? "$!{enterer}" : "")
				+ "</enterer>\n"
				+ "    <date_entered>"
				+ (includeDefaultScripts ? "$!{dateEntered}" : "")
				+ "</date_entered>\n"
				+ "    <session>"
				+ (includeDefaultScripts ? "$!{sessionId}" : "")
				+ "</session>\n" + "  </header>\n";
	}

	public static String closeForm() {
		return "  <other></other>" + "</form>\n";
	}
}
