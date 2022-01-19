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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

/**
 * This class stores a configurable number lines of the output from the log file.
 */
@Plugin(name = "Memory", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class MemoryAppender extends AbstractAppender {

	private CircularFifoBuffer buffer;

	private int bufferSize;

	public static class MemoryAppenderBuilder extends AbstractAppender.Builder<MemoryAppenderBuilder> {
		private int bufferSize = 100;

		public MemoryAppenderBuilder setBufferSize(int bufferSize) {
			if (bufferSize < 0) {
				throw new IllegalArgumentException("bufferSize must be a positive number or 0");
			}

			this.bufferSize = bufferSize;
			return asBuilder();
		}

		public MemoryAppender build() {
			return new MemoryAppender(getName(), getFilter(), getLayout(), isIgnoreExceptions(), getPropertyArray(), bufferSize);
		}
	}

	public static MemoryAppenderBuilder newBuilder() {
		return new MemoryAppenderBuilder();
	}

	@PluginFactory
	@SuppressWarnings("unused")
	protected static MemoryAppender createAppender(
			@PluginAttribute("name") final String name,
			@PluginAttribute("bufferSize") final int bufferSize,
			@PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) final boolean ignoreExceptions,
			@PluginElement("Filter") final Filter filter,
			@PluginElement("Layout") final Layout<? extends Serializable> layout
	) {
		return new MemoryAppender(name, filter, layout, ignoreExceptions, null,
				bufferSize <= 0 ? 100 : bufferSize);
	}

	private MemoryAppender(String name, Filter filter,
						   Layout<? extends Serializable> layout, boolean ignoreExceptions,
						   Property[] properties, int bufferSize) {
		super(name, filter, layout, ignoreExceptions, properties);
		this.bufferSize = bufferSize;
	}

	@Override
	public void append(LogEvent logEvent) {
		if (buffer != null) {
			// log4j2 reuses events when possible, so we need an immutable copy to store
			logEvent = logEvent.toImmutable();
			buffer.add(logEvent);
		}
	}

	@Override
	public void start() {
		this.buffer = new CircularFifoBuffer(bufferSize);
		super.start();
	}

	@Override
	protected boolean stop(long timeout, TimeUnit timeUnit, boolean changeLifeCycleState) {
		buffer.clear();
		return super.stop(timeout, timeUnit, changeLifeCycleState);
	}

	public List<String> getLogLines() {
		if (buffer == null) {
			return new ArrayList<>();
		}

		List<String> logLines = new ArrayList<>(buffer.size());
		Layout<? extends Serializable> layout = this.getLayout();
		for (Object aBuffer : buffer) {
			LogEvent logEvent = (LogEvent) aBuffer;
			logLines.add((String) layout.toSerializable(logEvent));
		}
		return logLines;
	}

	@SuppressWarnings("unused")
	public int getBufferSize() {
		return bufferSize;
	}

}