/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.serialization;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.Verifies;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map;

public class SimpleXStreamSerializerTest {
	
	/**
	 * @throws SerializationException
	 * @see {@link org.openmrs.serialization.SimpleXStreamSerializer#serialize(Object)}
	 */
	@Test
	@Verifies(value = "should get serialized", method = "serialize(Object)")
	public void serialize_shouldSerializeObject() throws SerializationException {
		
		OpenmrsSerializer serializer = new SimpleXStreamSerializer();
		
		Foo foo = new Foo("test", 1);
		List<String> list = new ArrayList<String>();
		list.add("foo");
		list.add("bar");
		Map<Integer, String> map = new HashMap<Integer, String>();
		map.put(1, "foo");
		map.put(2, "fooBar");
		map.put(3, "bar");
		foo.setAttributeList(list);
		foo.setAttributeMap(map);
		
		String serializedFoo = serializer.serialize(foo);
		
		Assert.assertTrue(StringUtils.deleteWhitespace(serializedFoo).equals(
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
	 * @see {@link org.openmrs.serialization.SimpleXStreamSerializer#serialize(Object)}
	 */
	@Test
	@Verifies(value = "should get deserialized", method = "deserialize(String, Class)")
	public void deserialize_shouldDeserializeStringToClassInstance() throws SerializationException {
		String serializedFoo = "<org.openmrs.serialization.Foo>\n" + "  <attributeString>Testing</attributeString>\n"
		        + "  <attributeInt>4</attributeInt>\n" + "  <attributeList>\n" + "    <string>fooBar</string>\n"
		        + "    <string>bar</string>\n" + "  </attributeList>\n" + "  <attributeMap>\n" + "    <entry>\n"
		        + "      <int>10</int>\n" + "      <string>foo</string>\n" + "    </entry>\n" + "    <entry>\n"
		        + "      <int>20</int>\n" + "      <string>fooBar</string>\n" + "    </entry>\n" + "    <entry>\n"
		        + "      <int>30</int>\n" + "      <string>bar</string>\n" + "    </entry>\n" + "  </attributeMap>\n"
		        + "</org.openmrs.serialization.Foo>";
		
		OpenmrsSerializer serializer = new SimpleXStreamSerializer();
		
		Foo foo = serializer.deserialize(serializedFoo, Foo.class);
		
		Assert.assertTrue(foo.getAttributeString().equals("Testing"));
		Assert.assertTrue(foo.getAttributeInt() == 4);
		
		List newList = foo.getAttributeList();
		Assert.assertTrue(newList.size() == 2);
		Assert.assertTrue(newList.get(0).equals("fooBar"));
		Assert.assertTrue(newList.get(1).equals("bar"));
		
		Map newMap = foo.getAttributeMap();
		Assert.assertTrue(newMap.size() == 3);
		Assert.assertTrue(newMap.get(10).equals("foo"));
		Assert.assertTrue(newMap.get(20).equals("fooBar"));
		Assert.assertTrue(newMap.get(30).equals("bar"));
		
	}
}
