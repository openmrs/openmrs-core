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
import java.util.List;

import org.apache.commons.collections.buffer.BoundedFifoBuffer;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 * This class stores a few lines of the output to the log file. This class is set in the log4j
 * descriptor file: /metadata/api/log4j/log4j.xml
 */
public class MemoryAppender extends AppenderSkeleton {
	
	private CircularFifoBuffer buffer;
	// Used to store the errors from the startup
	private BoundedFifoBuffer errorBuffer;
	
	private int bufferSize = 100;
	private int errorBufferSize = 100;
	
	public MemoryAppender() {
	}
	
	@Override
	protected void append(LoggingEvent loggingEvent) {
		if (buffer != null) {
			if (loggingEvent.getLevel().equals(Level.ERROR)) {
				// Store only error logs up to errorBufferSize
				errorBuffer.add(loggingEvent);
			}
			// replace the oldest logs with newest logs
			buffer.add(loggingEvent);
		}
	}
	
	@Override
	public void close() {
		buffer.clear();
		errorBuffer.clear();
	}
	
	// used to clear the errorBuffer manually
	public void clearErrorBuffer() {
		errorBuffer.clear();
	}
	
	@Override
	public boolean requiresLayout() {
		return true;
	}
	
	@Override
	public void activateOptions() {
		this.buffer = new CircularFifoBuffer(bufferSize);
		this.errorBuffer = new BoundedFifoBuffer(errorBufferSize);
	}
	
	public List<String> getLogLines() {
		List<String> logLines = new ArrayList<>(buffer.size());
		Layout layout = this.getLayout();
		for (Object aBuffer : buffer) {
			LoggingEvent loggingEvent = (LoggingEvent) aBuffer;
			logLines.add(layout.format(loggingEvent));
		}
		return logLines;
	}

	public List<String> getErrorLogLines() {
		List<String> logLines = new ArrayList<>(errorBuffer.size());
		Layout layout = this.getLayout();
		for (Object bBuffer : errorBuffer) {
			LoggingEvent loggingEvent = (LoggingEvent) bBuffer;
			logLines.add(layout.format(loggingEvent));
		}
		return logLines;
	}
	
	public int getBufferSize() {
		return bufferSize;
	}
	
	public int getErrorBufferSize() {
		return errorBufferSize;
	}
	
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public void setErrorBufferSize(int errorBufferSize) {
		this.errorBufferSize = errorBufferSize;
	}
	
}
