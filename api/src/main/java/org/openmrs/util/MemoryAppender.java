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

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.core.ErrorHandler;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.StringLayout;
import org.apache.logging.log4j.core.config.Property;

/**
 * This class stores a configurable number lines of the output from the log file.
 *
 * Note that this class is implemented as a single-buffer-per-appender-name meaning that each appender name can only support
 * a single configuration (the most recent applied)
 * 
 * @deprecated As of 2.4.4, 2.5.1, and 2.6.0 this class is moved to the org.openmrs.logging package
 */
@Deprecated
public class MemoryAppender extends org.openmrs.logging.MemoryAppender {
	
	private final org.openmrs.logging.MemoryAppender implementation;
	
	MemoryAppender(org.openmrs.logging.MemoryAppender implementation) {
		super(implementation.getName(), implementation.getFilter(),
			(StringLayout) implementation.getLayout(), implementation.ignoreExceptions(),
			implementation.getPropertyArray(), 1);
		
		this.implementation = implementation;
	}
	
	protected MemoryAppender(String name, Filter filter,
		StringLayout layout, boolean ignoreExceptions,
		Property[] properties, int bufferSize) {
		super(name, filter, layout, ignoreExceptions, properties, bufferSize);
		
		implementation = null;
	}
	
	@Override
	public void append(LogEvent logEvent) {
		if (implementation != null) {
			implementation.append(logEvent);
		} else {
			super.append(logEvent);
		}
	}
	
	@Override
	public int getBufferSize() {
		if (implementation != null) {
			return implementation.getBufferSize();
		} else {
			return super.getBufferSize();
		}
	}
	
	@Override
	public List<String> getLogLines() {
		if (implementation != null) {
			return implementation.getLogLines();
		} else {
			return super.getLogLines();
		}
	}
	
	@Override
	public boolean requiresLocation() {
		if (implementation != null) {
			return implementation.requiresLocation();
		} else {
			return super.requiresLocation();
		}
	}
	
	@Override
	public void error(String msg) {
		if (implementation != null) {
			implementation.error(msg);
		} else {
			super.error(msg);
		}
	}
	
	@Override
	public void error(String msg, LogEvent event, Throwable t) {
		if (implementation != null) {
			implementation.error(msg, event, t);
		} else {
			super.error(msg, event, t);
		}
	}
	
	@Override
	public void error(String msg, Throwable t) {
		if (implementation != null) {
			implementation.error(msg, t);
		} else {
			super.error(msg, t);
		}
	}
	
	@Override
	public ErrorHandler getHandler() {
		if (implementation != null) {
			return implementation.getHandler();
		} else {
			return super.getHandler();
		}
	}
	
	@Override
	public Layout<? extends Serializable> getLayout() {
		if (implementation != null) {
			return implementation.getLayout();
		} else {
			return super.getLayout();
		}
	}
	
	@Override
	public String getName() {
		if (implementation != null) {
			return implementation.getName();
		} else {
			return super.getName();
		}
	}
	
	@Override
	public boolean ignoreExceptions() {
		if (implementation != null) {
			return implementation.ignoreExceptions();
		} else {
			return super.ignoreExceptions();
		}
	}
	
	@Override
	public void setHandler(ErrorHandler handler) {
		if (implementation != null) {
			implementation.setHandler(handler);
		} else {
			super.setHandler(handler);
		}
	}
	
	@Override
	public String toString() {
		if (implementation != null) {
			return implementation.toString();
		} else {
			return super.toString();
		}
	}
	
	@Override
	public synchronized void addFilter(Filter filter) {
		if (implementation != null) {
			implementation.addFilter(filter);
		} else {
			super.addFilter(filter);
		}
	}
	
	@Override
	public Filter getFilter() {
		if (implementation != null) {
			return implementation.getFilter();
		} else {
			return super.getFilter();
		}
	}
	
	@Override
	public boolean hasFilter() {
		if (implementation != null) {
			return implementation.hasFilter();
		} else {
			return super.hasFilter();
		}
	}
	
	@Override
	public boolean isFiltered(LogEvent event) {
		if (implementation != null) {
			return implementation.isFiltered(event);
		} else {
			return super.isFiltered(event);
		}
	}
	
	@Override
	public synchronized void removeFilter(Filter filter) {
		if (implementation != null) {
			implementation.removeFilter(filter);
		} else {
			super.removeFilter(filter);
		}
	}
	
	@Override
	public void start() {
		if (implementation != null) {
			implementation.start();
		} else {
			super.start();
		}
	}
	
	@Override
	public boolean stop(long timeout, TimeUnit timeUnit) {
		if (implementation != null) {
			return implementation.stop(timeout, timeUnit);
		} else {
			return super.stop(timeout, timeUnit);
		}
	}
	
	@Override
	public Property[] getPropertyArray() {
		if (implementation != null) {
			return implementation.getPropertyArray();
		} else {
			return super.getPropertyArray();
		}
	}
	
	@Override
	public State getState() {
		if (implementation != null) {
			return implementation.getState();
		} else {
			return super.getState();
		}
	}
	
	@Override
	public boolean isInitialized() {
		if (implementation != null) {
			return implementation.isInitialized();
		} else {
			return super.isInitialized();
		}
	}
	
	@Override
	public boolean isStarted() {
		if (implementation != null) {
			return implementation.isStarted();
		} else {
			return super.isStarted();
		}
	}
	
	@Override
	public boolean isStarting() {
		if (implementation != null) {
			return implementation.isStarting();
		} else {
			return super.isStarting();
		}
	}
	
	@Override
	public boolean isStopped() {
		if (implementation != null) {
			return implementation.isStopped();
		} else {
			return super.isStopped();
		}
	}
	
	@Override
	public boolean isStopping() {
		if (implementation != null) {
			return implementation.isStopping();
		} else {
			return super.isStopping();
		}
	}
	
	@Override
	public void initialize() {
		if (implementation != null) {
			implementation.initialize();
		} else {
			super.initialize();
		}
	}
	
	@Override
	public void stop() {
		if (implementation != null) {
			implementation.stop();
		} else {
			super.stop();
		}
	}
}
