/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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
 * This class stores a few lines of the output to the log file.  This class
 * is set in the log4j descriptor file: /metadata/api/log4j/log4j.xml
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
		for (Iterator<?> iterBuffer = buffer.iterator(); iterBuffer.hasNext(); ) {
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
