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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.beans.EventHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.XStreamException;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.collection.internal.PersistentList;
import org.hibernate.loader.PropertyPath;
import org.hibernate.mapping.Column;
import org.hibernate.type.OrderedMapType;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.AdministrationService;

public class SimpleXStreamSerializerTest {
	
	
	/**
	 * @throws SerializationException
	 * @see org.openmrs.serialization.SimpleXStreamSerializer#serialize(Object)
	 */
	@Test
	public void serialize_shouldSerializeObject() throws SerializationException {
		
		OpenmrsSerializer serializer = new SimpleXStreamSerializer();
		
		Foo foo = new Foo("test", 1);
		List<String> list = new ArrayList<>();
		list.add("foo");
		list.add("bar");
		Map<Integer, String> map = new HashMap<>();
		map.put(1, "foo");
		map.put(2, "fooBar");
		map.put(3, "bar");
		foo.setAttributeList(list);
		foo.setAttributeMap(map);
		
		String serializedFoo = serializer.serialize(foo);
		
		assertTrue(StringUtils.deleteWhitespace(serializedFoo).equals(
		    StringUtils.deleteWhitespace("<org.openmrs.serialization.Foo>\n" + "  <attributeString>test</attributeString>\n"
		            + "  <attributeInt>1</attributeInt>\n" + "  <attributeList>\n" + "    <string>foo</string>\n"
		            + "    <string>bar</string>\n" + "  </attributeList>\n" + "  <attributeMap>\n" + "    <entry>\n"
		            + "      <int>1</int>\n" + "      <string>foo</string>\n" + "    </entry>\n" + "    <entry>\n"
		            + "      <int>2</int>\n" + "      <string>fooBar</string>\n" + "    </entry>\n" + "    <entry>\n"
		            + "      <int>3</int>\n" + "      <string>bar</string>\n" + "    </entry>\n" + "  </attributeMap>\n"
		            + "  </org.openmrs.serialization.Foo>")));
		
	}
	
	/**
	 * @throws SerializationException
	 * @see org.openmrs.serialization.SimpleXStreamSerializer#serialize(Object)
	 */
	@Test
	public void deserialize_shouldDeserializeStringToClassInstance() throws SerializationException {
		String serializedFoo = "<org.openmrs.serialization.Foo>\n" + "  <attributeString>Testing</attributeString>\n"
		        + "  <attributeInt>4</attributeInt>\n" + "  <attributeList>\n" + "    <string>fooBar</string>\n"
		        + "    <string>bar</string>\n" + "  </attributeList>\n" + "  <attributeMap>\n" + "    <entry>\n"
		        + "      <int>10</int>\n" + "      <string>foo</string>\n" + "    </entry>\n" + "    <entry>\n"
		        + "      <int>20</int>\n" + "      <string>fooBar</string>\n" + "    </entry>\n" + "    <entry>\n"
		        + "      <int>30</int>\n" + "      <string>bar</string>\n" + "    </entry>\n" + "  </attributeMap>\n"
		        + "</org.openmrs.serialization.Foo>";
		
		SimpleXStreamSerializer serializer = new SimpleXStreamSerializer();
		
		serializer.getXstream().allowTypeHierarchy(Foo.class);
		
		Foo foo = serializer.deserialize(serializedFoo, Foo.class);
		
		assertTrue(foo.getAttributeString().equals("Testing"));
		assertEquals(4, foo.getAttributeInt());

		List<String> newList = foo.getAttributeList();
		assertThat(newList, hasSize(2));
		assertTrue(newList.get(0).equals("fooBar"));
		assertTrue(newList.get(1).equals("bar"));

		Map<Integer, String> newMap = foo.getAttributeMap();
		assertEquals(3, newMap.size());
		assertTrue(newMap.get(10).equals("foo"));
		assertTrue(newMap.get(20).equals("fooBar"));
		assertTrue(newMap.get(30).equals("bar"));
		
	}
	
	/**
	 * @throws SerializationException
	 * @see SimpleXStreamSerializer#deserialize(String,Class)
	 */
	@Test
	public void deserialize_shouldNotDeserializeProxies() throws SerializationException {
		String serialized = "<dynamic-proxy>" + "<interface>org.openmrs.OpenmrsObject</interface>"
		        + "<handler class=\"java.beans.EventHandler\">" + "<target class=\"java.lang.ProcessBuilder\">"
		        + "<command>" + "<string>someApp</string>" + "</command></target>" + "<action>start</action>" + "</handler>"
		        + "</dynamic-proxy>";
		
		assertThrows(SerializationException.class, () -> new SimpleXStreamSerializer().deserialize(serialized, OpenmrsObject.class));
	}
	
	/**
	 * @throws SerializationException
	 * @see SimpleXStreamSerializer#deserialize(String,Class)
	 */
	@Test
	public void deserialize_shouldIgnoreEntities() throws SerializationException {
		String xml = "<!DOCTYPE ZSL [<!ENTITY xxe1 \"some attribute value\" >]>" + "<org.openmrs.serialization.Foo>"
		        + "<attributeString>&xxe1;</attributeString>" + "</org.openmrs.serialization.Foo>";
		
		assertThrows(SerializationException.class, () -> new SimpleXStreamSerializer().deserialize(xml, Foo.class));
	}
	
	/**
	 * @throws SerializationException
	 * @see SimpleXStreamSerializer#serialize(Object)
	 */
	@Test
	public void serialize_shouldNotSerializeProxies() throws SerializationException {
		EventHandler h = new EventHandler(new ProcessBuilder("someApp"), "start", null, null);
		Object proxy = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { OpenmrsObject.class }, h);
		assertThrows(XStreamException.class, () -> new SimpleXStreamSerializer().serialize(proxy));
	}

	/**
	 * @throws SerializationException
	 * @see SimpleXStreamSerializer#deserialize(String,Class)
	 */
	@Test
	public void deserialize_shouldNotDeserializeClassesThatAreNotWhitelisted() throws SerializationException {
		//given
		AdministrationService adminService = mock(AdministrationService.class);
		when(adminService.getSerializerWhitelistTypes()).thenReturn(Arrays.asList("org.hibernate.*"));
		SimpleXStreamSerializer serializer = new SimpleXStreamSerializer(adminService);
		Column column = new Column();
		String columnXml = serializer.serialize(column);
		
		//when/then
		assertThrows(SerializationException.class, () -> serializer.deserialize(columnXml, Column.class));
		assertThrows(SerializationException.class, () -> new SimpleXStreamSerializer().deserialize(columnXml, Column.class));
	}

	/**
	 * @throws SerializationException
	 * @see SimpleXStreamSerializer#deserialize(String,Class)
	 */
	@Test
	public void deserialize_shouldDeserializeWhitelistedPackages() throws SerializationException {
		//given
		Column column = new Column();
		AdministrationService adminService = mock(AdministrationService.class);
		when(adminService.getSerializerWhitelistTypes()).thenReturn(Arrays.asList("org.hibernate.**"));
		SimpleXStreamSerializer serializer = new SimpleXStreamSerializer(adminService);
		
		String columnXml = serializer.serialize(column);
		String propertyPath = serializer.serialize(new PropertyPath());
		
		//when/then
		assertNotNull(serializer.deserialize(columnXml, Column.class));
		assertNotNull(serializer.deserialize(propertyPath, PropertyPath.class));
	}

	/**
	 * @throws SerializationException
	 * @see SimpleXStreamSerializer#deserialize(String,Class)
	 */
	@Test
	public void deserialize_shouldDeserializeWhitelistedClassesAndPackages() throws SerializationException {
		//given
		Column column = new Column();
		AdministrationService adminService = mock(AdministrationService.class);
		when(adminService.getSerializerWhitelistTypes()).thenReturn(Arrays.asList("org.hibernate.loader.PropertyPath", 
				"org.hibernate.mapping.*", "org.hibernate.collection.**"));
		SimpleXStreamSerializer serializer = new SimpleXStreamSerializer(adminService);
		String columnXml = serializer.serialize(column);
		String propertyPath = serializer.serialize(new PropertyPath());
		String persistentList = serializer.serialize(new PersistentList());
		String orderedMapType = serializer.serialize(new OrderedMapType("role", "ref"));
		
		//when/then
		assertNotNull(serializer.deserialize(columnXml, Column.class));
		assertNotNull(serializer.deserialize(propertyPath, PropertyPath.class));
		assertNotNull(serializer.deserialize(persistentList, PersistentList.class));
		assertThrows(SerializationException.class, () -> serializer.deserialize(orderedMapType, OrderedMapType.class));
	}

	@Test
	public void deserialize_shouldDeserializeWhitelistedHierarchies() throws SerializationException {
		//given
		AdministrationService adminService = mock(AdministrationService.class);
		when(adminService.getSerializerWhitelistTypes()).thenReturn(Arrays.asList(
			"hierarchyOf:org.hibernate.type.MapType"));
		SimpleXStreamSerializer serializer = new SimpleXStreamSerializer(adminService);
		String orderedMapType = serializer.serialize(new OrderedMapType("role", "ref"));

		//when/then
		assertNotNull(serializer.deserialize(orderedMapType, OrderedMapType.class));
	}
}
