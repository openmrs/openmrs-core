/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.logging;

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.StringLayout;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.ThreadSafeCircularFifoQueue;

/**
 * This class stores a configurable number lines of the output from the log file.
 * <p/>
 * Note that this class is implemented as a single-buffer-per-appender-name meaning that each appender name can only support
 * a single configuration (the most recent applied)
 */
@Plugin(name = "Memory", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class MemoryAppender extends AbstractAppender {

	// we store the MemoryAppenders by name, using SoftReferences to allow them to be garbage collected
	// as an implementation detail, we expect this class to only have a single instance, so our map
	// is only allocated an initial capacity of 1
	private static final Map<String, SoftReference<MemoryAppender>> APPENDERS = new HashMap<>(1);

	private ThreadSafeCircularFifoQueue<LogEvent> buffer;

	private int bufferSize;

	protected MemoryAppender(String name, Filter filter,
		StringLayout layout, boolean ignoreExceptions,
		Property[] properties, int bufferSize) {
		super(name, filter, layout, ignoreExceptions, properties);

		this.buffer = new ThreadSafeCircularFifoQueue<>(bufferSize);
		this.bufferSize = bufferSize;
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
		@PluginElement("Layout") final StringLayout layout
	) {
		final int theBufferSize = bufferSize <= 0 ? 100 : bufferSize;
		MemoryAppender appender = null;
		if (APPENDERS.containsKey(name)) {
			appender = APPENDERS.get(name).get();

			if (appender != null && appender.bufferSize != theBufferSize) {
				LogEvent[] oldBuffer = appender.buffer.toArray(new LogEvent[0]);
				appender.buffer = new ThreadSafeCircularFifoQueue<>(theBufferSize);
				appender.bufferSize = theBufferSize;
				appender.buffer.addAll(Arrays.asList(oldBuffer));
			}
		}

		if (appender == null) {
			appender = new MemoryAppender(name, filter, layout, ignoreExceptions, null, theBufferSize);
			APPENDERS.put(name, new SoftReference<>(appender));
		}

		return appender;
	}

	@Override
	public void append(LogEvent logEvent) {
		buffer.add(logEvent.toImmutable());
	}
	
	public int getBufferSize() {
		return bufferSize;
	}
	
	public List<String> getLogLines() {
		if (buffer == null) {
			return new ArrayList<>(0);
		}

		LogEvent[] events = buffer.toArray(new LogEvent[0]);
		if (events.length == 0) {
			return Collections.emptyList();
		}
		
		return Arrays.stream(events).filter(Objects::nonNull).map(((StringLayout) getLayout())::toSerializable)
			.collect(Collectors.toList());
	}

	public static class MemoryAppenderBuilder extends AbstractAppender.Builder<MemoryAppenderBuilder> {

		private int bufferSize = 100;

		private StringLayout layout;
		
		public MemoryAppenderBuilder() {
			super();
			setName(OpenmrsConstants.MEMORY_APPENDER_NAME);
		}
		
		public MemoryAppenderBuilder setBufferSize(int bufferSize) {
			if (bufferSize < 0) {
				throw new IllegalArgumentException("bufferSize must be a positive number or 0");
			}

			this.bufferSize = bufferSize;
			return asBuilder();
		}

		@Override
		public Layout<? extends Serializable> getLayout() {
			return layout;
		}

		@Override
		public MemoryAppenderBuilder setLayout(Layout<? extends Serializable> layout) {
			if (layout instanceof StringLayout) {
				return setLayout((StringLayout) layout);
			}

			throw new IllegalArgumentException("MemoryAppender layouts must output string values");
		}

		public MemoryAppenderBuilder setLayout(StringLayout layout) {
			this.layout = layout;
			return asBuilder();
		}

		public MemoryAppender build() {
			return new MemoryAppender(getName(), getFilter(), layout, isIgnoreExceptions(), getPropertyArray(),
				bufferSize);
		}
	}

}
