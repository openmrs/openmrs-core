/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web;

import org.openmrs.module.webservices.helper.ServerLogActionWrapper;
import org.openmrs.util.MemoryAppender;

import java.util.ArrayList;
import java.util.List;

/**
 * MockServerLogActionWrapper used to run the Unit tests for ServerLogResource
 */
public class MockServerLogActionWrapper<T extends ServerLogActionWrapper> extends ServerLogActionWrapper {

	public List<String> mockMemoryAppenderBuffer = new ArrayList<String>();

	private ServerLogActionWrapper serverLogActionWrapper;

	public MockServerLogActionWrapper(T serverLogActionWrapper) {
		this.serverLogActionWrapper = serverLogActionWrapper;
	}

	/**
	 * Override method from ServerLogActionWrapper to get the logs from mockMemoryAppender
	 * 
	 * @return List of log lines
	 */
	@Override
	public List<String[]> getServerLogs() {
		List<String> logLines = mockMemoryAppenderBuffer;
		List<String[]> finalOutput = new ArrayList<String[]>();

		for (String logLine : logLines) {
			String[] logElements = serverLogActionWrapper.logLinePatternMatcher(logLine);
			finalOutput.add(logElements);
		}
		return finalOutput;
	}

	@Override
	public MemoryAppender getMemoryAppender() {
		return serverLogActionWrapper.getMemoryAppender();
	}
}
