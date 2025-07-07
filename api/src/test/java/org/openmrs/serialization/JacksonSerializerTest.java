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
import org.hibernate.type.OrderedMapType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Encounter;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
                "org.openmrs.serialization.Foo"));
    }

    @Test
    public void serialize_shouldSerializeNonOpenmrsObject() {
        // setup
        Foo foo = new Foo("test", 1);
        List <String> list = new ArrayList <> ();
        list.add("foo");
        list.add("bar");
        Map <Integer, String> map = new HashMap <> ();
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

        List <String> newList = foo.getAttributeList();
        assertThat(newList, hasSize(2));
        assertTrue(newList.get(0).equals("fooBar"));
        assertTrue(newList.get(1).equals("bar"));

        Map <Integer, String> newMap = foo.getAttributeMap();
        assertEquals(3, newMap.size());
        assertTrue(newMap.get(10).equals("foo"));
        assertTrue(newMap.get(20).equals("fooBar"));
        assertTrue(newMap.get(30).equals("bar"));

    }

    @Test
    public void serialize_shouldSerializeOpenmrsObject() throws Exception{
        // setup
        ObjectMapper mapper = new ObjectMapper();

        // replay
        String serializedEnc = serializer.serialize(Context.getEncounterService().getEncounter(3));

        // verify

        JsonNode expectedEnc = mapper.readTree("{\"uuid\":\"6519d653-393b-4118-9c83-a3715b82d4ac\",\"creator\":\"1010d442-e134-11de-babe-001e378eb67e\",\"dateCreated\":\"2008-08-18T14:09:05\",\"voided\":false,\"encounterId\":3,\"encounterDatetime\":\"2008-08-01T00:00:00\",\"patient\":\"5946f880-b197-400b-9caa-a3c661d23041\",\"location\":\"8d6c993e-c2cc-11de-8d13-0010c6dffd0f\",\"form\":\"d9218f76-6c39-45f4-8efa-4c5c6c199f50\",\"encounterType\":\"07000be2-26b6-4cce-8b40-866d8435b613\",\"obs\":[{\"uuid\":\"39fb7f47-e80a-4056-9285-bd798be13c63\",\"creator\":\"1010d442-e134-11de-babe-001e378eb67e\",\"dateCreated\":\"2008-08-18T14:09:35\",\"voided\":false,\"obsId\":7,\"concept\":\"c607c80f-1ea9-4da3-bb88-6276ce8868dd\",\"obsDatetime\":\"2008-07-01T00:00:00\",\"valueNumeric\":50.0,\"person\":\"5946f880-b197-400b-9caa-a3c661d23041\",\"location\":\"8d6c993e-c2cc-11de-8d13-0010c6dffd0f\",\"encounter\":\"6519d653-393b-4118-9c83-a3715b82d4ac\",\"dirty\":false,\"status\":\"FINAL\",\"id\":7,\"personId\":7,\"complex\":false,\"obsGrouping\":false,\"conceptDescription\":\"5f4d710b-d333-40b7-b449-6e0e739d15d0\"},{\"uuid\":\"be48cdcb-6a76-47e3-9f2e-2635032f3a9a\",\"creator\":\"1010d442-e134-11de-babe-001e378eb67e\",\"dateCreated\":\"2008-08-18T14:11:13\",\"voided\":false,\"obsId\":9,\"concept\":\"a09ab2c5-878e-4905-b25d-5784167d0216\",\"obsDatetime\":\"2008-08-01T00:00:00\",\"valueNumeric\":150.0,\"person\":\"5946f880-b197-400b-9caa-a3c661d23041\",\"location\":\"8d6c993e-c2cc-11de-8d13-0010c6dffd0f\",\"encounter\":\"6519d653-393b-4118-9c83-a3715b82d4ac\",\"dirty\":false,\"status\":\"PRELIMINARY\",\"id\":9,\"personId\":7,\"complex\":false,\"obsGrouping\":false,\"conceptDescription\":\"2677cc71-0580-4d91-b18f-0a63a1840aa0\"}],\"visit\":\"a2428fea-6b78-11e0-93c3-18a905e044dc\",\"id\":3,\"allObs\":[{\"uuid\":\"be48cdcb-6a76-47e3-9f2e-2635032f3a9a\",\"creator\":\"1010d442-e134-11de-babe-001e378eb67e\",\"dateCreated\":\"2008-08-18T14:11:13\",\"voided\":false,\"obsId\":9,\"concept\":\"a09ab2c5-878e-4905-b25d-5784167d0216\",\"obsDatetime\":\"2008-08-01T00:00:00\",\"valueNumeric\":150.0,\"person\":\"5946f880-b197-400b-9caa-a3c661d23041\",\"location\":\"8d6c993e-c2cc-11de-8d13-0010c6dffd0f\",\"encounter\":\"6519d653-393b-4118-9c83-a3715b82d4ac\",\"dirty\":false,\"status\":\"PRELIMINARY\",\"id\":9,\"personId\":7,\"complex\":false,\"obsGrouping\":false,\"conceptDescription\":\"2677cc71-0580-4d91-b18f-0a63a1840aa0\"},{\"uuid\":\"39fb7f47-e80a-4056-9285-bd798be13c63\",\"creator\":\"1010d442-e134-11de-babe-001e378eb67e\",\"dateCreated\":\"2008-08-18T14:09:35\",\"voided\":false,\"obsId\":7,\"concept\":\"c607c80f-1ea9-4da3-bb88-6276ce8868dd\",\"obsDatetime\":\"2008-07-01T00:00:00\",\"valueNumeric\":50.0,\"person\":\"5946f880-b197-400b-9caa-a3c661d23041\",\"location\":\"8d6c993e-c2cc-11de-8d13-0010c6dffd0f\",\"encounter\":\"6519d653-393b-4118-9c83-a3715b82d4ac\",\"dirty\":false,\"status\":\"FINAL\",\"id\":7,\"personId\":7,\"complex\":false,\"obsGrouping\":false,\"conceptDescription\":\"5f4d710b-d333-40b7-b449-6e0e739d15d0\"}],\"activeEncounterProviders\":[{\"uuid\":\"19e0aae8-20ee-46b7-ade6-9e68f897b7a9\",\"creator\":\"1010d442-e134-11de-babe-001e378eb67e\",\"dateCreated\":\"2006-03-11T15:57:35\",\"voided\":false,\"encounterProviderId\":1,\"encounter\":\"6519d653-393b-4118-9c83-a3715b82d4ac\",\"provider\":\"c2299800-cca9-11e0-9572-0800200c9a66\",\"encounterRole\":\"a0b03050-c99b-11e0-9572-0800200c9a66\",\"id\":1}],\"providersByRoles\":{\"EncounterRole: [1 ]\":[{\"uuid\":\"c2299800-cca9-11e0-9572-0800200c9a66\",\"name\":\"Super User\",\"creator\":\"1010d442-e134-11de-babe-001e378eb67e\",\"dateCreated\":\"2005-01-01T00:00:00\",\"retired\":false,\"providerId\":1,\"person\":\"ba1b19c2-3ed6-4f63-b8c0-f762dc8d7562\",\"identifier\":\"Test\",\"id\":1}]},\"ordersWithoutOrderGroups\":[{\"uuid\":\"921de0a3-05c4-444a-be03-e01b4c4b9142\",\"creator\":\"1010d442-e134-11de-babe-001e378eb67e\",\"dateCreated\":\"2008-08-08T00:00:00\",\"voided\":false,\"orderId\":1,\"patient\":\"5946f880-b197-400b-9caa-a3c661d23041\",\"orderType\":\"131168f4-15f5-102d-96e4-000c29c2a5d7\",\"concept\":\"15f83cd6-64e9-4e06-a5f9-364d3b14a43d\",\"instructions\":\"2x daily\",\"dateActivated\":\"2008-08-08T00:00:00\",\"encounter\":\"6519d653-393b-4118-9c83-a3715b82d4ac\",\"orderer\":\"c2299800-cca9-11e0-9572-0800200c9a66\",\"dateStopped\":\"2008-08-15T00:00:00\",\"urgency\":\"ROUTINE\",\"orderNumber\":\"1\",\"careSetting\":\"6f0c9a92-6f24-11e3-af88-005056821db0\",\"action\":\"NEW\",\"dose\":325.0,\"doseUnits\":\"557b9699-68a3-11e3-bd76-0800271c1b75\",\"frequency\":\"28090760-7c38-11e3-baa7-0800200c9a66\",\"asNeeded\":false,\"quantity\":1.0,\"quantityUnits\":\"5a2aa3db-68a3-11e3-bd76-0800271c1b75\",\"drug\":\"05ec820a-d297-44e3-be6e-698531d9dd3f\",\"dosingType\":\"org.openmrs.SimpleDosingInstructions\",\"numRefills\":10,\"route\":\"e10ffe54-5184-4efe-8960-cd565ec1cdf8\",\"dispenseAsWritten\":false,\"drugOrder\":true,\"dosingInstructionsInstance\":{\"dose\":325.0,\"doseUnits\":\"557b9699-68a3-11e3-bd76-0800271c1b75\",\"route\":\"e10ffe54-5184-4efe-8960-cd565ec1cdf8\",\"frequency\":\"28090760-7c38-11e3-baa7-0800200c9a66\",\"asNeeded\":false},\"nonCodedDrug\":false,\"id\":1,\"started\":true,\"active\":false,\"activated\":true,\"expired\":false,\"effectiveStartDate\":\"2008-08-08T00:00:00\",\"discontinuedRightNow\":true,\"effectiveStopDate\":\"2008-08-15T00:00:00\"},{\"uuid\":\"e1f95924-697a-11e3-bd76-0800271c1b75\",\"creator\":\"1010d442-e134-11de-babe-001e378eb67e\",\"dateCreated\":\"2008-08-15T00:00:00\",\"voided\":false,\"orderId\":111,\"patient\":\"5946f880-b197-400b-9caa-a3c661d23041\",\"orderType\":\"131168f4-15f5-102d-96e4-000c29c2a5d7\",\"concept\":\"15f83cd6-64e9-4e06-a5f9-364d3b14a43d\",\"dateActivated\":\"2008-08-15T00:00:00\",\"encounter\":\"6519d653-393b-4118-9c83-a3715b82d4ac\",\"orderer\":\"c2299800-cca9-11e0-9572-0800200c9a66\",\"accessionNumber\":\"ACC-123\",\"urgency\":\"ROUTINE\",\"orderNumber\":\"111\",\"careSetting\":\"6f0c9a92-6f24-11e3-af88-005056821db0\",\"previousOrder\":\"921de0a3-05c4-444a-be03-e01b4c4b9142\",\"action\":\"REVISE\",\"dose\":325.0,\"doseUnits\":\"557b9699-68a3-11e3-bd76-0800271c1b75\",\"frequency\":\"28090760-7c38-11e3-baa7-0800200c9a66\",\"asNeeded\":false,\"quantity\":1.0,\"quantityUnits\":\"5a2aa3db-68a3-11e3-bd76-0800271c1b75\",\"drug\":\"05ec820a-d297-44e3-be6e-698531d9dd3f\",\"dosingType\":\"org.openmrs.SimpleDosingInstructions\",\"numRefills\":10,\"route\":\"e10ffe54-5184-4efe-8960-cd565ec1cdf8\",\"dispenseAsWritten\":false,\"drugOrder\":true,\"dosingInstructionsInstance\":{\"dose\":325.0,\"doseUnits\":\"557b9699-68a3-11e3-bd76-0800271c1b75\",\"route\":\"e10ffe54-5184-4efe-8960-cd565ec1cdf8\",\"frequency\":\"28090760-7c38-11e3-baa7-0800200c9a66\",\"asNeeded\":false},\"nonCodedDrug\":false,\"id\":111,\"started\":true,\"active\":true,\"activated\":true,\"expired\":false,\"effectiveStartDate\":\"2008-08-15T00:00:00\",\"discontinuedRightNow\":false}]}");
        JsonNode actaulEnc = mapper.readTree(serializedEnc);

        assertTrue(expectedEnc.equals(actaulEnc));
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
    public void deserialize_shouldSucceedIfClassMatchesExactWhitelistEntry() throws Exception {
        // setup
        adminService.saveGlobalProperty(
            new GlobalProperty("jackson.serializer.whitelist.types",
                "org.openmrs.serialization.Foo"));
        String deserializedFoo = "{\"attributeString\":\"exact\",\"attributeInt\":200,\"attributeList\":null,\"attributeMap\":null}";

        // verify
        assertDoesNotThrow(() -> serializer.deserialize(deserializedFoo, Foo.class));
    }

    @Test
    public void deserialize_shouldSucceedIfClassMatchesWildcardPackagePattern() throws Exception {
        // setup
        adminService.saveGlobalProperty(
            new GlobalProperty("jackson.serializer.whitelist.types",
                "org.openmrs.serialization.*"));
        String deserializedFoo = "{\"attributeString\":\"wild\",\"attributeInt\":123,\"attributeList\":null,\"attributeMap\":null}";

        // verify
        assertDoesNotThrow(() -> serializer.deserialize(deserializedFoo, Foo.class));
    }

    @Test
    public void deserialize_shouldSucceedIfClassMatchesMultiPackagesWildcardPattern() throws Exception {
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

    @Test
	public void deserialize_shouldDeserializeWhitelistedHierarchies() throws SerializationException {
		// setup
        adminService.saveGlobalProperty(
            new GlobalProperty("jackson.serializer.whitelist.types",
                "hierarchyOf:org.hibernate.type.MapType"));
		String orderedMapType = serializer.serialize(new OrderedMapTypeWithNoArg("role", "ref"));

		// verify
		assertDoesNotThrow(() -> serializer.deserialize(orderedMapType, OrderedMapTypeWithNoArg.class));
	}

    public static class OrderedMapTypeWithNoArg extends OrderedMapType {
		public OrderedMapTypeWithNoArg() {
			super(null, null);
		}
        public OrderedMapTypeWithNoArg(String arg1, String arg2) {
			super(arg1, arg2);
		}
	}
}