/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.event.broker;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.FileBackedOutputStream;
import org.openmrs.event.BaseEvent;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Common code for {@link BrokerIncomingEvent} and {@link BrokerOutgoingEvent} to handle payload.
 * 
 * @param <T>
 * @since 2.9.0
 */
public abstract class BrokerEvent<T> extends BaseEvent implements ResolvableTypeProvider {
	private static final long serialVersionUID = 1L;
	
	private static final int MAX_MEMORY_PAYLOAD_SIZE = 256 * 1024; // 256 kB
	
	private final transient Object cacheLock = new Object();
	
	protected String broker;
	
	protected T payload;
	
	protected Map<String, Object> headers;
	
	private transient volatile ByteSource cachedPayloadSource;

	protected BrokerEvent() {
	}

	protected BrokerEvent(T payload) {
		this(payload, null);
	}

	protected BrokerEvent(T payload, String broker) {
		this(payload, broker, new HashMap<>());
	}


	protected BrokerEvent(T payload, String broker, Map<String, Object> headers) {
		this(payload, broker, headers, new HashSet<>());
	}

	protected BrokerEvent(T payload, String broker, Map<String, Object> headers, Set<String> tags) {
		super(tags);
		this.broker = broker;
		this.payload = payload;
		this.headers = headers;
	}

	/**
	 * It implements caching of {@code InputStream} in memory or temporary file. Call {@code super.getPayload()} if
	 * overriding.
	 *
	 * @return the payload
	 */
	public T getPayload() {
		// Check if the payload is a stream that needs caching.
		if (payload instanceof InputStream) {
			// Double-checked locking to ensure thread-safe, lazy initialization of the cache file.
			if (cachedPayloadSource == null) {
				synchronized (cacheLock) {
					if (cachedPayloadSource == null) {
						//noinspection UnstableApiUsage
						try (InputStream in = (InputStream) payload;
						     FileBackedOutputStream out = new FileBackedOutputStream(MAX_MEMORY_PAYLOAD_SIZE, true)) {

							ByteStreams.copy(in, out);
							//noinspection UnstableApiUsage
							this.cachedPayloadSource = out.asByteSource();

							// The original stream is consumed, null it out so we don't try to cache it again.
							this.payload = null;
						} catch (IOException e) {
							throw new UncheckedIOException("Failed to cache InputStream payload", e);
						}
					}
				}
			}
		}

		if (cachedPayloadSource != null) {
			try {
				//noinspection unchecked
				return (T) cachedPayloadSource.openStream();
			} catch (IOException e) {
				throw new UncheckedIOException("Failed to open cached payload stream", e);
			}
		}

		return payload;
	}

	public void setPayload(T payload) {
		this.payload = payload;
	}

	/**
	 * The broker identifier this event came from.
	 *
	 * @return the broker
	 */
	public String getBroker() {
		return broker;
	}

	public void setBroker(String broker) {
		this.broker = broker;
	}

	public Map<String, Object> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, Object> headers) {
		this.headers = headers;
	}

	@JsonIgnore
	@Override
	public @Nullable ResolvableType getResolvableType() {
		if (payload != null && getClass().getTypeParameters().length == 1) {
			return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forInstance(payload));
		} else {
			return ResolvableType.forClass(getClass());
		}
	}
}
