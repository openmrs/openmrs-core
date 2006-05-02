package org.openmrs.arden.parser;

/**
 * Defines the behavior of an error handler for the HQL parsers.
 * User: josh
 * Date: Dec 6, 2003
 * Time: 12:20:43 PM
 */
public interface ParseErrorHandler extends ErrorReporter {

	int getErrorCount();

	int getWarningCount();

	String[] getErrorMessages();

	String getErrorString();
}
