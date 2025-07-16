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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openmrs.OpenmrsObject;
import org.openmrs.api.DomainService;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

/**
 * A custom Jackson module that enables UUID-based serialization and deserialization 
 * of {@link OpenmrsObject} references.
 * <p>
 * During serialization, fields referencing {@code OpenmrsObject}s are serialized 
 * as their UUIDs rather than as full nested objects. 
 * During deserialization, UUID strings are resolved back to full {@code OpenmrsObject} 
 * instances using the {@link DomainService}.
 * </p>
 *
 * This approach helps to:
 * <ul>
 *   <li>Reduce object size by avoiding full object nesting</li>
 *   <li>Enable reference-based object graph resolution</li>
 *   <li>Support lazy loading of related entities based on UUIDs</li>
 * </ul>
 *
 */
public class UuidReferenceModule extends SimpleModule {

    protected DomainService domainService;

    /**
     * Constructs a new {@code UuidReferenceModule} with the given domain service.
     *
     * @param domainService the domain service used to resolve UUIDs into {@code OpenmrsObject}s
     */
    public UuidReferenceModule(DomainService domainService) {
        super("UuidReferenceModule");
        this.domainService = domainService;
    }

    /**
     * Sets up custom serializer and deserializer modifiers to intercept and override 
     * Jackson's default behavior for properties referencing {@link OpenmrsObject}s.
     *
     * @param context the Jackson setup context
     */
    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);

        // Serialization: override field serialization logic
        context.addBeanSerializerModifier(new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(
                    SerializationConfig config,
                    BeanDescription beanDesc,
                    List<BeanPropertyWriter> beanProperties) {

                Map<String, BeanPropertyDefinition> propertyDefMap = beanDesc.findProperties().stream().collect(Collectors.toMap(BeanPropertyDefinition::getName, def -> def));
                Iterator<BeanPropertyWriter> propIt = beanProperties.iterator();

                while (propIt.hasNext()) {
                    BeanPropertyWriter writer = propIt.next();
                    BeanPropertyDefinition def = propertyDefMap.get(writer.getName());

                    if (def == null || !def.hasField()) {
                        // Exclude from serialization if there's no backing field
                        propIt.remove();
                        continue;
                    }
                }
                for (int i = 0; i < beanProperties.size(); i++) {
                    BeanPropertyWriter original = beanProperties.get(i);

                    beanProperties.set(i, new BeanPropertyWriter(original) {
                        @Override
                        public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {
                            Object value = get(bean);
                            if (value instanceof OpenmrsObject) {
                                gen.writeStringField(getName(), ((OpenmrsObject) value).getUuid());
                            } else {
                                super.serializeAsField(bean, gen, prov);
                            }
                        }
                    });
                }
                return beanProperties;
            }
        });

        // Deserialization: override field deserialization logic
        context.addBeanDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public BeanDeserializerBuilder updateBuilder(
                    DeserializationConfig config,
                    BeanDescription beanDesc,
                    BeanDeserializerBuilder builder) {

                Iterator<SettableBeanProperty> properties = builder.getProperties();
                while (properties.hasNext()) {
                    SettableBeanProperty prop = properties.next();
                    Class<?> rawType = prop.getType().getRawClass();

                    if (OpenmrsObject.class.isAssignableFrom(rawType)) {
                        JsonDeserializer<?> uuidDeserializer = new JsonDeserializer<Object>() {
                            @Override
                            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                                String uuid = p.getValueAsString();
                                if (uuid == null || uuid.isEmpty()) {
                                    return null;
                                };
                                return domainService.fetchByUuid((Class<?>) rawType, uuid);
                            }
                        };

                        SettableBeanProperty newProp = prop.withValueDeserializer(uuidDeserializer);
                        builder.addOrReplaceProperty(newProp, true);
                    }
                }
                return builder;
            }
            @Override
            public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config, 
                    BeanDescription beanDesc, List<BeanPropertyDefinition> propDefs) {
                return propDefs.stream()
                    .filter(def -> def.hasField())
                    .collect(Collectors.toList());
            }
        });
    }
}