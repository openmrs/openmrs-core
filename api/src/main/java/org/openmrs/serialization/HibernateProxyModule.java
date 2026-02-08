/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.serialization;

import java.io.IOException;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

/**
 * A Jackson module that handles serialization of Hibernate proxy objects.
 * This replaces the jackson-datatype-hibernate6 {@code Hibernate6Module} which is
 * not compatible with Hibernate 7.x.
 * <p>
 * When a Hibernate proxy is encountered during serialization, this module initializes
 * the proxy and serializes the underlying entity. Uninitialized lazy-loaded proxies
 * are serialized as {@code null}.
 * </p>
 *
 * @since 3.0.0
 */
class HibernateProxyModule extends Module {

	@Override
	public String getModuleName() {
		return "HibernateProxyModule";
	}

	@Override
	public Version version() {
		return Version.unknownVersion();
	}

	@Override
	public void setupModule(SetupContext context) {
		context.addBeanSerializerModifier(new BeanSerializerModifier() {
			@Override
			public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
				if (HibernateProxy.class.isAssignableFrom(beanDesc.getBeanClass())) {
					return new HibernateProxySerializer(serializer);
				}
				return serializer;
			}
		});
	}

	private static class HibernateProxySerializer extends JsonSerializer<Object> {

		private final JsonSerializer<Object> delegate;

		@SuppressWarnings("unchecked")
		HibernateProxySerializer(JsonSerializer<?> delegate) {
			this.delegate = (JsonSerializer<Object>) delegate;
		}

		@Override
		public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			if (value instanceof HibernateProxy) {
				if (!Hibernate.isInitialized(value)) {
					gen.writeNull();
					return;
				}
				value = Hibernate.unproxy(value);
			}
			delegate.serialize(value, gen, serializers);
		}
	}
}
