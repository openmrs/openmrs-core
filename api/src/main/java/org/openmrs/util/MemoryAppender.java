/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

/**
 * This class stores a few lines of the output to the log file. This class is set in the log4j
 * descriptor file: /metadata/api/log4j/log4j.xml
 */
public class MemoryAppender extends AppenderSkeleton {
	
	private CircularFifoBuffer buffer;
	
	private int bufferSize = 100;
	
	public MemoryAppender() {
	}
	
	protected void append(LoggingEvent loggingEvent) {
		if (buffer != null) {
			buffer.add(loggingEvent);
		}
	}
	
	public void close() {
		buffer.clear();
	}
	
	public boolean requiresLayout() {
		return true;
	}
	
	public void activateOptions() {
		this.buffer = new CircularFifoBuffer(bufferSize);
	}
	
	public List<String> getLogLines() {
		List<String> logLines = new ArrayList<String>(buffer.size());
		Layout layout = this.getLayout();
		for (Iterator<?> iterBuffer = buffer.iterator(); iterBuffer.hasNext();) {
			LoggingEvent loggingEvent = (LoggingEvent) iterBuffer.next();
			logLines.add(layout.format(loggingEvent));
		}
		return logLines;
	}
	
	public int getBufferSize() {
		return bufferSize;
	}
	
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
}
