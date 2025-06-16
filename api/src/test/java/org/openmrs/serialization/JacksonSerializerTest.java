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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Encounter;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class JacksonSerializerTest extends BaseContextSensitiveTest {

    @Autowired
    private AdministrationService adminService;

    @Autowired
    @Qualifier("jacksonSerializer")
    private JacksonSerializer serializer;

    @BeforeEach
    public void setup() {
        adminService.saveGlobalProperty(
            new GlobalProperty("jackson.serializer.whitelist.types",
                "org.openmrs.serialization.Foo, org.openmrs.Encounter"));
    }

    @Test
    public void serialize_shouldSerializeNonOpenmrsObject() {
        // setup
        Foo foo = new Foo("test", 1);
        List < String > list = new ArrayList < > ();
        list.add("foo");
        list.add("bar");
        Map < Integer, String > map = new HashMap < > ();
        map.put(1, "foo");
        map.put(2, "fooBar");
        map.put(3, "bar");
        foo.setAttributeList(list);
        foo.setAttributeMap(map);

        // replay
        String serializedFoo = serializer.serialize(foo);

        // verify
        assertTrue(StringUtils.deleteWhitespace(serializedFoo).equals(
            StringUtils.deleteWhitespace("{\"attributeString\":\"test\",\"attributeInt\":1,\"attributeList\":[\"foo\",\"bar\"],\"attributeMap\":{\"1\":\"foo\",\"2\":\"fooBar\",\"3\":\"bar\"}}")));

    }

    @Test
    public void deserialize_shouldDeserializeStringToNonOpenmrsObjectClassInstance() throws SerializationException {
        // setup
        String serializedFoo = "{\"attributeString\":\"Testing\",\"attributeInt\":4,\"attributeList\":[\"fooBar\",\"bar\"],\"attributeMap\":{\"20\":\"fooBar\",\"10\":\"foo\",\"30\":\"bar\"}}";

        // replay
        Foo foo = serializer.deserialize(serializedFoo, Foo.class);

        // verify
        assertTrue(foo.getAttributeString().equals("Testing"));
        assertEquals(4, foo.getAttributeInt());

        List < String > newList = foo.getAttributeList();
        assertThat(newList, hasSize(2));
        assertTrue(newList.get(0).equals("fooBar"));
        assertTrue(newList.get(1).equals("bar"));

        Map < Integer, String > newMap = foo.getAttributeMap();
        assertEquals(3, newMap.size());
        assertTrue(newMap.get(10).equals("foo"));
        assertTrue(newMap.get(20).equals("fooBar"));
        assertTrue(newMap.get(30).equals("bar"));

    }

    @Test
    public void serialize_shouldSerializeOpenmrsObject() {

        // replay
        String serializedEnc = serializer.serialize(Context.getEncounterService().getEncounter(3));

        // verify
        assertEquals(StringUtils.deleteWhitespace(serializedEnc),
            StringUtils.deleteWhitespace("{\"uuid\":\"6519d653-393b-4118-9c83-a3715b82d4ac\",\"creator\":\"1010d442-e134-11de-babe-001e378eb67e\",\"dateCreated\":\"2008-08-18T14:09:05\",\"changedBy\":null,\"dateChanged\":null,\"voided\":false,\"dateVoided\":null,\"voidedBy\":null,\"voidReason\":null,\"encounterId\":3,\"encounterDatetime\":\"2008-08-01T00:00:00\",\"patient\":\"5946f880-b197-400b-9caa-a3c661d23041\",\"location\":\"8d6c993e-c2cc-11de-8d13-0010c6dffd0f\",\"form\":\"d9218f76-6c39-45f4-8efa-4c5c6c199f50\",\"encounterType\":\"07000be2-26b6-4cce-8b40-866d8435b613\",\"orders\":null,\"diagnoses\":null,\"conditions\":null,\"obs\":null,\"visit\":\"a2428fea-6b78-11e0-93c3-18a905e044dc\",\"encounterProviders\":null,\"allergies\":null}"));

    }

    @Test
    public void deserialize_shouldDeserializeJsonStringToOpenmrsObjectClassInstance() throws Exception {
        // setup
        String serializedenc = "{\"uuid\":\"6519d653-393b-4118-9c83-a3715b82d4ac\",\"creator\":\"1010d442-e134-11de-babe-001e378eb67e\",\"dateCreated\":\"2008-08-18T14:09:05\",\"changedBy\":null,\"dateChanged\":null,\"voided\":false,\"dateVoided\":null,\"voidedBy\":null,\"voidReason\":null,\"encounterId\":3,\"encounterDatetime\":\"2008-08-01T00:00:00\",\"patient\":\"5946f880-b197-400b-9caa-a3c661d23041\",\"location\":\"8d6c993e-c2cc-11de-8d13-0010c6dffd0f\",\"form\":\"d9218f76-6c39-45f4-8efa-4c5c6c199f50\",\"encounterType\":\"07000be2-26b6-4cce-8b40-866d8435b613\",\"orders\":null,\"diagnoses\":null,\"conditions\":[],\"obs\":[{\"uuid\":\"be48cdcb-6a76-47e3-9f2e-2635032f3a9a\",\"creator\":\"1010d442-e134-11de-babe-001e378eb67e\",\"dateCreated\":\"2008-08-18T14:11:13\",\"changedBy\":null,\"dateChanged\":null,\"voided\":false,\"dateVoided\":null,\"voidedBy\":null,\"voidReason\":null,\"formNamespaceAndPath\":null,\"obsId\":9,\"concept\":\"a09ab2c5-878e-4905-b25d-5784167d0216\",\"obsDatetime\":\"2008-08-01T00:00:00\",\"accessionNumber\":null,\"obsGroup\":null,\"groupMembers\":[],\"valueCoded\":null,\"valueCodedName\":null,\"valueDrug\":null,\"valueGroupId\":null,\"valueDatetime\":null,\"valueNumeric\":150.0,\"valueModifier\":null,\"valueText\":null,\"valueComplex\":null,\"comment\":\"\",\"person\":\"5946f880-b197-400b-9caa-a3c661d23041\",\"order\":null,\"location\":\"8d6c993e-c2cc-11de-8d13-0010c6dffd0f\",\"encounter\":\"6519d653-393b-4118-9c83-a3715b82d4ac\",\"previousVersion\":null,\"dirty\":true,\"interpretation\":null,\"status\":\"PRELIMINARY\",\"referenceRange\":null,\"complex\":false,\"obsGrouping\":false},{\"uuid\":\"39fb7f47-e80a-4056-9285-bd798be13c63\",\"creator\":\"1010d442-e134-11de-babe-001e378eb67e\",\"dateCreated\":\"2008-08-18T14:09:35\",\"changedBy\":null,\"dateChanged\":null,\"voided\":false,\"dateVoided\":null,\"voidedBy\":null,\"voidReason\":null,\"formNamespaceAndPath\":null,\"obsId\":7,\"concept\":\"c607c80f-1ea9-4da3-bb88-6276ce8868dd\",\"obsDatetime\":\"2008-07-01T00:00:00\",\"accessionNumber\":null,\"obsGroup\":null,\"groupMembers\":[],\"valueCoded\":null,\"valueCodedName\":null,\"valueDrug\":null,\"valueGroupId\":null,\"valueDatetime\":null,\"valueNumeric\":50.0,\"valueModifier\":null,\"valueText\":null,\"valueComplex\":null,\"comment\":\"\",\"person\":\"5946f880-b197-400b-9caa-a3c661d23041\",\"order\":null,\"location\":\"8d6c993e-c2cc-11de-8d13-0010c6dffd0f\",\"encounter\":\"6519d653-393b-4118-9c83-a3715b82d4ac\",\"previousVersion\":null,\"dirty\":false,\"interpretation\":null,\"status\":\"FINAL\",\"referenceRange\":null,\"complex\":false,\"obsGrouping\":false}],\"visit\":\"a2428fea-6b78-11e0-93c3-18a905e044dc\",\"encounterProviders\":null,\"allergies\":[]}";

        // replay
        Encounter deserializedEnc = (Encounter) serializer.deserialize(serializedenc, Encounter.class);

        // verify
        assertTrue("6519d653-393b-4118-9c83-a3715b82d4ac".equals(deserializedEnc.getUuid()));
        assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse("2008-08-18T14:09:05"), deserializedEnc.getDateCreated());
    }

    @Test
    public void deserialize_shouldAllowIfClassMatchesExactWhitelistEntry() throws Exception {
        // setup
        adminService.saveGlobalProperty(
            new GlobalProperty("jackson.serializer.whitelist.types",
                "org.openmrs.serialization.Foo"));
        String deserializedFoo = "{\"attributeString\":\"exact\",\"attributeInt\":200,\"attributeList\":null,\"attributeMap\":null}";

        // verify
        assertDoesNotThrow(() -> serializer.deserialize(deserializedFoo, Foo.class));
    }

    @Test
    public void deserialize_shouldAllowIfClassMatchesWildcardPackagePattern() throws Exception {
        // setup
        adminService.saveGlobalProperty(
            new GlobalProperty("jackson.serializer.whitelist.types",
                "org.openmrs.serialization.*"));
        String deserializedFoo = "{\"attributeString\":\"wild\",\"attributeInt\":123,\"attributeList\":null,\"attributeMap\":null}";

        // verify
        assertDoesNotThrow(() -> serializer.deserialize(deserializedFoo, Foo.class));
    }

    @Test
    public void deserialize_shouldAllowIfClassMatchesMultiPackagesWildcardPattern() throws Exception {
        // setup
        adminService.saveGlobalProperty(
            new GlobalProperty("jackson.serializer.whitelist.types",
                "org.openmrs.**"));
        String deserializedFoo = "{\"attributeString\":\"deep\",\"attributeInt\":999,\"attributeList\":null,\"attributeMap\":null}";

        // verify
        assertDoesNotThrow(() -> serializer.deserialize(deserializedFoo, Foo.class));
    }

    @Test
    public void deserialize_shouldThrowSecurityExceptionIfClassNotWhitelisted() throws Exception {
        // setup
        adminService.saveGlobalProperty(
            new GlobalProperty("jackson.serializer.whitelist.types",
                "org.openmrs.model.*"));
        String deserializedFoo = "{\"attributeString\":\"bad\",\"attributeInt\":404,\"attributeList\":null,\"attributeMap\":null}";

        // verify
        assertThrows(SecurityException.class, () -> serializer.deserialize(deserializedFoo, Foo.class));
    }
}