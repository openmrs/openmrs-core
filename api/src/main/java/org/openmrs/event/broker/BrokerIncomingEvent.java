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

import java.util.Map;
import java.util.Set;

import org.openmrs.event.EventPayload;
import org.openmrs.serialization.JacksonConfig;

/**
 * The event is published by a broker whenever there's a new message.
 * <p>
 * It needs to be used with {@link BrokerEventListener}.
 * <p>
 * You may put a condition on the listener to limit to specific source or broker e.g.
 * {@code @BrokerEventListener('my-queue', broker='my-broker")}
 * <p>
 * The payload may be either an {@code InputStream} or deserialized by a broker from JSON with
 * {@link JacksonConfig#objectMapper()}.
 * <p>
 * In case the payload requires custom deserialization, you may extend the class and implement
 * {@link EventPayload}.
 * <p>
 * If the payload is an {@code InputStream}, it is automatically buffered into memory on the first
 * read. If the stream size exceeds 256 kB, it spills over to a temporary file. This allows multiple
 * listeners to consume the stream's content, as each will receive a new, independent
 * {@code InputStream}. The temporary file is automatically scheduled for deletion, when the event
 * is garbage-collected.
 * <p>
 * Values from headers are deserialized to simple values or in case of complex objects from JSON
 * with {@link JacksonConfig#objectMapper()}.
 *
 * @since 2.9.0
 */
public class BrokerIncomingEvent<T> extends BrokerEvent<T> {

	private static final long serialVersionUID = 1L;

	private String source;

	public BrokerIncomingEvent() {
	}

	public BrokerIncomingEvent(T payload, String source) {
		super(payload);
		this.source = source;
	}

	public BrokerIncomingEvent(T payload, String source, String broker) {
		super(payload, broker);
		this.source = source;
	}

	public BrokerIncomingEvent(T payload, String source, String broker, Map<String, Object> headers) {
		super(payload, broker, headers);
		this.source = source;
	}

	public BrokerIncomingEvent(T payload, String source, String broker, Map<String, Object> headers, Set<String> tags) {
		super(payload, broker, headers, tags);
		this.source = source;
	}

	/**
	 * The source this event came from e.g. queue name.
	 *
	 * @return the source of the event
	 */
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

}
