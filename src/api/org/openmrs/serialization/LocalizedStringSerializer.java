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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.openmrs.LocalizedString;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.LocalizedStringUtil;

/**
 * This class is responsible for the de/serialization between LocalizedString object and a single
 * string
 */
public class LocalizedStringSerializer implements OpenmrsSerializer {
	
	/**
	 * The header marks whether there are variant values in LocalizedString object
	 */
	public static final String HEADER = "i18n:v1;";
	
	/**
	 * The separator between locale and string value
	 */
	public static final String PARTITION = ":";
	
	/**
	 * The separator between each pair which includes one locale and string value
	 */
	public static final String SPLITTER = ";";
	
	/**
	 * A utility method to deserialize a String to a LocalizedString object. <br />
	 * 
	 * <pre>
	 * 		Deserialization mechanism:
	 * 		Database Text ---> Object Value:
	 * 		Favorite Color ---> {unlocalizedValue: "Favorite Color", variants: null}
	 * 		i18n:v1;unlocalized:Favorite Color;en_UK:Favourite Colour;fr:Couleur pr¨¦f¨¦r¨¦e; ---> {unlocalizedValue: "Favorite Color", variants: [en_UK = "Favourite Colour", fr = "Couleur pr¨¦f¨¦r¨¦e"]}
	 * </pre>
	 * 
	 * @param serializedObject - String to deserialize
	 * @param clazz - The class to deserialize the Object into; for this method, it should be
	 *            LocalizedString.class
	 * @return a LocalizedString object get by deserializing the passed serializedObject
	 * @should throw a SerializationException if given clazz doesnt equal with LocalizedString class
	 * @should return null if given serializedObject is null
	 * @should return null if given serializedObject is empty
	 * @should not fail if given serializedObject doesnt contains variants
	 * @should deserialize correctly if given serializedObject contains variants
	 * @should deescape correctly if given serializedObject contains escaped delimiter
	 * @see org.openmrs.serialization.OpenmrsSerializer#deserialize(java.lang.String,
	 *      java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T deserialize(String serializedObject, Class<? extends T> clazz) throws SerializationException {
		if (LocalizedString.class.equals(clazz)) {
			if (StringUtils.isBlank(serializedObject))
				return null;
			LocalizedString ls = new LocalizedString();
			if (!serializedObject.contains(HEADER)) {
				ls.setUnlocalizedValue(LocalizedStringUtil.deescapeDelimiter(serializedObject));
				return (T) ls;
			} else {
				String[] array1 = serializedObject.split("(?<!\\\\);");
				//ignore array1[0], because it is "i18n:v1;"
				//parse unlocalized value
				String[] array2 = array1[1].split("(?<!\\\\):");
				if (array2.length == 1)
					ls.setUnlocalizedValue("");
				else
					ls.setUnlocalizedValue(LocalizedStringUtil.deescapeDelimiter(array2[1]));
				
				//parse variant values
				ls.setVariants(new HashMap<Locale, String>());
				for (int x = 2; x < array1.length; x++) {
					array2 = array1[x].split("(?<!\\\\):");
					Locale loc = LocaleUtility.fromSpecification(array2[0]);
					if (array2.length == 1)
						ls.getVariants().put(loc, "");
					else
						ls.getVariants().put(loc, LocalizedStringUtil.deescapeDelimiter(array2[1]));
				}
			}
			return (T) ls;
		} else {
			throw new SerializationException("'" + serializedObject
			        + "' can only be deserialize to a 'LocalizedString' object, not '" + clazz.getName() + "'");
		}
	}
	
	/**
	 * A utility method to serialize a LocalizedString object to a String. <br />
	 * 
	 * <pre>
	 * Serialization mechanism:
	 * Object Value ---> Database Text:
	 * {unlocalizedValue: "Favorite Color", variants: null} ---> Favorite Color
	 * {unlocalizedValue: "Favorite Color", variants: [en_UK = "Favourite Colour", fr = "Couleur pr¨¦f¨¦r¨¦e"]} ---> i18n:v1;unlocalized:Favorite Color;en_UK:Favourite Colour;fr:Couleur pr¨¦f¨¦r¨¦e;
	 * </pre>
	 * 
	 * @param o - the passed object which will be cast to LocalizedString type
	 * @return the string get by serializing the passed object
	 * @should return null if given object is null
	 * @should not fail if given object hasnt variants
	 * @should serialize correctly if given object has variants
	 * @should throw a SerializationException if given object doesnt belong to LocalizedString
	 * @should escape correctly if given object has a name including delimiter
	 * @see org.openmrs.serialization.OpenmrsSerializer#serialize(java.lang.Object)
	 */
	@Override
	public String serialize(Object o) throws SerializationException {
		if (o == null)
			return null;
		if (o instanceof LocalizedString) {
			LocalizedString localizedString = (LocalizedString) o;
			StringBuffer sb = new StringBuffer("");
			sb.append(LocalizedStringUtil.escapeDelimiter(localizedString.getUnlocalizedValue()));
			if (localizedString.getVariants() != null && !localizedString.getVariants().isEmpty()) {
				sb.insert(0, HEADER);
				sb.insert(HEADER.length(), "unlocalized:");
				sb.append(";");
				Iterator<Entry<Locale, String>> it = localizedString.getVariants().entrySet().iterator();
				while (it.hasNext()) {
					Entry<Locale, String> entry = it.next();
					sb.append(entry.getKey());
					sb.append(PARTITION);
					sb.append(LocalizedStringUtil.escapeDelimiter(entry.getValue()));
					sb.append(SPLITTER);
				}
			}
			return sb.toString();
		} else {
			throw new SerializationException("Can not serialize an object of type:" + o.getClass().getName());
		}
	}
	
}
