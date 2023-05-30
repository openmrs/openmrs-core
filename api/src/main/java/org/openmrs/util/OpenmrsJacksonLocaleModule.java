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

import java.io.IOException;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * This is a Jackson-Databind module that simply changes how we serialize locales by pre-adopting the Jackson 3.0 convention
 * of using toLanguageTag() instead of toString(). When Jackson 3.0 is available, we should be able to drop this class.
 * <p/>
 * This module is available to be used by any use-case that creates an ObjectMapper. However, it is only registered by default
 * for the Spring MappingJackson2HttpMessageConverter class.
 */
public class OpenmrsJacksonLocaleModule extends Module {
	
	private static final String MODULE_NAME = "openmrs-locale";
	
	private static final Version VERSION = new Version(1, 0, 0, null, "org.openmrs.web", "openmrs-locale");
	
	@Override
	public String getModuleName() {
		return MODULE_NAME;
	}
	
	@Override
	public Version version() {
		return VERSION;
	}
	
	@Override
	public void setupModule(SetupContext setupContext) {
		setupContext.addSerializers(new Serializers.Base() {
			
			@Override
			@SuppressWarnings("unchecked")
			public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
				
				final Class<?> raw = type.getRawClass();
				if (Locale.class.isAssignableFrom(raw)) {
					return new OpenmrsLocaleSerializer((Class<Locale>) raw);
				}
				
				return super.findSerializer(config, type, beanDesc);
			}
		});
	}
	
	private static class OpenmrsLocaleSerializer extends StdSerializer<Locale> {
		
		protected OpenmrsLocaleSerializer(Class<Locale> t) {
			super(t, false);
		}
		
		@Override
		public void serialize(Locale locale, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
			throws IOException {
			if (locale == Locale.ROOT) {
				jsonGenerator.writeString("");
			} else {
				jsonGenerator.writeString(locale.toLanguageTag());
			}
		}
	}
}
