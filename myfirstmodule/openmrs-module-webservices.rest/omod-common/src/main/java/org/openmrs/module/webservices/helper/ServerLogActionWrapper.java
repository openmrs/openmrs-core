/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.helper;

import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.util.MemoryAppender;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * ServerLogActionWrapper used to serve the Server logs
 */
public abstract class ServerLogActionWrapper {
	
	public List<String[]> serverLog;
	
	public void setServerLog(List<String[]> serverLog) {
		this.serverLog = serverLog;
	}
	
	public List<String[]> getServerLog() {
		return serverLog;
	}
	
	/**
	 * Get server logs
	 * 
	 * @return List of last hundred server logs
	 */
	public List<String[]> getServerLogs() {
		// Check the GET_SERVER_LOGS privilege to serve the server logs
		Context.requirePrivilege(RestConstants.PRIV_GET_SERVER_LOGS);
		// Use the Memory Appender to retrieve the logs
		MemoryAppender memoryAppender = getMemoryAppender();

		if (memoryAppender == null) {
			return Collections.emptyList();
		}

		List<String> logLines = memoryAppender.getLogLines();
		List<String[]> finalOutput = new ArrayList<String[]>();
		for (String logLine : logLines) {
			String[] logElements = logLinePatternMatcher(logLine);
			finalOutput.add(logElements);
		}
		return finalOutput;
	}
	
	/**
	 * Match and find the patterns for log line
	 * 
	 * @param logLine Log lines from the terminal
	 * @return Array of matched patterns
	 */
	public String[] logLinePatternMatcher(String logLine) {
		String[] logElements = new String[4];
		// Defined Pattern to analyze
		String regExPatternType = "(INFO|ERROR|WARN|DEBUG)\\s.*?[-].*?\\s((?:[A-z][A-z].+))\\s[|](.*?)[|]\\s((.*\\n*)+)";
		try {
			Pattern pattern = Pattern.compile(regExPatternType);
			Matcher matcher = pattern.matcher(logLine);
			if (matcher.find()) {
				// If pattern matches to the message
				logElements[0] = matcher.group(1);
				logElements[1] = matcher.group(2);
				logElements[2] = matcher.group(3);
				logElements[3] = matcher.group(4);
			}
			return logElements;
		}
		catch (PatternSyntaxException e) {
			// In case of Exception, It will return array with error information
			logElements[0] = "ERROR";
			logElements[1] = "";
			logElements[2] = "PatternSyntaxException";
			logElements[3] = e.getMessage();
			return logElements;
		}
	}

	public abstract MemoryAppender getMemoryAppender();
}
