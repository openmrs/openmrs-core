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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
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
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.status.StatusLogger;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.ThreadSafeCircularFifoQueue;

/**
 * This class stores a configurable number lines of the output from the log file.
 * <p/>
 * Note that this class is implemented as a single-buffer-per-appender-name meaning that each
 * appender name can only support a single buffer size (the most recently applied)
 */
@Plugin(name = "Memory", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class MemoryAppender extends AbstractAppender {

	// we store the buffers by name, using SoftReferences to allow them to be garbage collected
	// this is a HashMap as it is only accessed from a synchronized method
	private static final Map<String, SoftReference<AtomicReference<ThreadSafeCircularFifoQueue<LogEvent>>>> BUFFERS = new HashMap<>(
	        1);

	private final AtomicReference<ThreadSafeCircularFifoQueue<LogEvent>> bufferRef;

	private final int bufferSize;

	protected MemoryAppender(String name, Filter filter, StringLayout layout, boolean ignoreExceptions,
	    Property[] properties, AtomicReference<ThreadSafeCircularFifoQueue<LogEvent>> bufferRef) {
		super(name, filter, layout, ignoreExceptions, properties);

		this.bufferRef = bufferRef;
		this.bufferSize = bufferRef.get().capacity();
	}

	protected MemoryAppender(String name, Filter filter,
		StringLayout layout, boolean ignoreExceptions,
		Property[] properties, int bufferSize) {
		super(name, filter, layout, ignoreExceptions, properties);

		this.bufferRef = getBufferRef(name, bufferSize);
		this.bufferSize = bufferRef.get().capacity();
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
		AtomicReference<ThreadSafeCircularFifoQueue<LogEvent>> buffer = getBufferRef(name, theBufferSize);

		MemoryAppender appender = new MemoryAppender(name, filter, layout, ignoreExceptions, null, buffer);
		if (!appender.isStarted()) {
			appender.start();
		}
		return appender;
	}

	@Override
	public void append(LogEvent logEvent) {
		bufferRef.get().add(logEvent.toImmutable());
	}
	
	public int getBufferSize() {
		return bufferSize;
	}
	
	public List<String> getLogLines() {
		LogEvent[] events = bufferRef.get().toArray(new LogEvent[0]);
		if (events.length == 0) {
			return Collections.emptyList();
		}

		Layout<? extends Serializable> layout = getLayout();
		if (!(layout instanceof StringLayout)) {
			StatusLogger.getLogger().warn(
			    "MemoryAppender {} is not configured with a StringLayout; defaulting to standard OpenMRS pattern", this);
			layout = PatternLayout.newBuilder().withPattern(OpenmrsConstants.DEFAULT_LOG_LAYOUT_PATTERN).build();
		}

		return Arrays.stream(events).filter(Objects::nonNull).map(((StringLayout) layout)::toSerializable)
		        .collect(Collectors.toList());
	}

	public static class MemoryAppenderBuilder extends AbstractAppender.Builder<MemoryAppenderBuilder> {

		private int bufferSize = 100;

		private StringLayout layout = PatternLayout.newBuilder().withPattern(OpenmrsConstants.DEFAULT_LOG_LAYOUT_PATTERN)
		        .build();

		public MemoryAppenderBuilder() {
			super();
			setName(OpenmrsConstants.MEMORY_APPENDER_NAME);
		}
		
		public MemoryAppenderBuilder setBufferSize(int bufferSize) {
			if (bufferSize < 0) {
				throw new IllegalArgumentException("bufferSize must be a positive number or 0");
			}

			this.bufferSize = bufferSize == 0 ? 100 : bufferSize;
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
			String name = getName();
			AtomicReference<ThreadSafeCircularFifoQueue<LogEvent>> buffer = getBufferRef(name, bufferSize);
			return new MemoryAppender(name, getFilter(), layout, isIgnoreExceptions(), getPropertyArray(), buffer);
		}
	}

	private static synchronized AtomicReference<ThreadSafeCircularFifoQueue<LogEvent>> getBufferRef(String name,
	        int bufferSize) {
		// clean-up
		BUFFERS.values().removeIf(ref -> ref.get() == null);

		AtomicReference<ThreadSafeCircularFifoQueue<LogEvent>> bufferRef = null;
		SoftReference<AtomicReference<ThreadSafeCircularFifoQueue<LogEvent>>> ref = BUFFERS.get(name);
		if (ref != null) {
			bufferRef = ref.get();
		}

		if (bufferRef == null) {
			ThreadSafeCircularFifoQueue<LogEvent> queue = new ThreadSafeCircularFifoQueue<>(bufferSize);
			bufferRef = new AtomicReference<>(queue);
			BUFFERS.put(name, new SoftReference<>(bufferRef));
		} else {
			ThreadSafeCircularFifoQueue<LogEvent> queue = bufferRef.get();
			if (queue.capacity() != bufferSize) {
				ThreadSafeCircularFifoQueue<LogEvent> resized = new ThreadSafeCircularFifoQueue<>(bufferSize);
				LogEvent[] snapshot = queue.toArray(new LogEvent[0]);
				// retain up to the bufferSize most recent messages when shrinking, since this is a FIFO queue
				int messagesToMove = Math.min(snapshot.length, bufferSize);
				int start = Math.max(0, snapshot.length - messagesToMove);
				for (int i = start; i < snapshot.length; i++) {
					resized.add(snapshot[i]);
				}
				bufferRef.set(resized);
			}
		}

		return bufferRef;
	}

}
