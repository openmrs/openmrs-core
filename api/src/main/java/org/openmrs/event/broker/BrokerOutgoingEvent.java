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

import org.openmrs.event.EventPayload;
import org.openmrs.serialization.JacksonConfig;

import java.util.Map;
import java.util.Set;

/**
 * Publish the event to send it to brokers. Multiple brokers may be registered to process events for 
 * a single broker identifier.
 * <p>
 * If payload is an {@code InputStream}, it will be passed to a broker without any serialization, otherwise
 * the payload should be serializable to JSON with the default ObjectMapper {@link JacksonConfig#objectMapper()}.
 * <p>
 * If the payload is an {@code InputStream}, it is automatically buffered into memory on the first read.
 * If the stream size exceeds 256 kB, it spills over to a temporary file.
 * This allows multiple brokers to consume the stream's content, as each will receive a new,
 * independent {@code InputStream}. The temporary file is automatically scheduled for deletion, when the event is 
 * garbage-collected.
 * <p>
 * If you need a custom serialization, you may extend the class and implement {@link EventPayload}.
 * <p>
 * Values from headers are always serialized to simple values or in case of complex objects to JSON with 
 * {@link JacksonConfig#objectMapper()}.    
 * <p>
 * Default broker identifier is configured via runtime property: {@code event.broker.default}.
 * 
 * @since 2.9.0
 */
public class BrokerOutgoingEvent<T> extends BrokerEvent<T> {
	private static final long serialVersionUID = 1L;

	private String target;
	
	public BrokerOutgoingEvent() {
	}

	public BrokerOutgoingEvent(T payload, String target) {
		super(payload);
		this.target = target;
	}

	public BrokerOutgoingEvent(T payload, String target, String broker) {
		super(payload, broker);
		this.target = target;
	}
	
	public BrokerOutgoingEvent(T payload, String target, String broker, Map<String, Object> headers) {
		super(payload, broker, headers);
		this.target = target;
	}

	public BrokerOutgoingEvent(T payload, String target, String broker, Map<String, Object> headers, Set<String> tags) {
		super(payload, broker, headers, tags);
		this.target = target;
	}
	
	public String getTarget() {
		return target;
	}

	/**
	 * The target this event should be sent to e.g. queue name.
	 *
	 * @param target the target of the event
	 */
	public void setTarget(String target) {
		this.target = target;
	}
}
