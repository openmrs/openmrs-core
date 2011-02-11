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
package org.openmrs.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.openmrs.LocalizedString;

/**
 * Utility class that provides LocalizedString related methods
 * 
 * @since 1.9
 */
public class LocalizedStringUtil {
	
	/**
	 * The separator between locale and string value
	 */
	public static final String PARTITION = ":";
	
	/**
	 * The separator between each pair which includes one locale and string value
	 */
	public static final String SPLITTER = ";";
	
	/**
	 * The header marks whether there are variant values in LocalizedString object
	 */
	public static final String HEADER = "i18n" + PARTITION + "v1" + SPLITTER;
	
	/**
	 * A static utility method to deserialize a String into a LocalizedString object. <br />
	 * 
	 * <pre>
	 * 		Deserialization mechanism:
	 * 		Database Text ---> Object Value:
	 * 		Favorite Color ---> {unlocalizedValue: "Favorite Color", variants: null}
	 * 		i18n:v1;unlocalized:Favorite Color;en_UK:Favourite Colour;fr:Couleur pr__e; ---> {unlocalizedValue: "Favorite Color", variants: [en_UK = "Favourite Colour", fr = "Couleur pr__e"]}
	 * </pre>
	 * 
	 * @param s - String to deserialize
	 * @return a LocalizedString object get by deserializing the passed s
	 * @should return null if given s is null
	 * @should return null if given s is empty
	 * @should not fail if given s doesnt contains variants
	 * @should deserialize correctly if given s contains variants
	 * @should unescape correctly if given s contains escaped delimiter
	 */
	public static LocalizedString deserialize(String s) {
		if (StringUtils.isBlank(s))
			return null;
		LocalizedString localizedString = new LocalizedString();
		if (!s.startsWith(HEADER)) {
			localizedString.setUnlocalizedValue(unescapeDelimiter(s));
		} else {
			// e.g., if passed s is like "i18n:v1;unlocalized:He\;lo;es:Hola;"
			// here "He\;lo" is just an escaped string for the original value "He;lo"
			// so here use regex "(?<!\\\\);" to exclude "\;" when split serializedObject by delimiter ";"
			String[] array1 = s.split("(?<!\\\\)" + SPLITTER);
			
			// ignore array1[0], because it is "i18n:v1;"
			// parse unlocalized value
			// e.g., if passed serializedObject is like "i18n:v1;unlocalized:He\:lo;es:Hola;"
			// here "He\:lo" is just an escaped string for the original value "He:lo"
			// so here use regex "(?<!\\\\):" to exclude "\:" when split serializedObject by delimiter ":"
			String[] array2 = array1[1].split("(?<!\\\\)" + PARTITION);
			if (array2.length == 1)
				localizedString.setUnlocalizedValue("");
			else
				localizedString.setUnlocalizedValue(unescapeDelimiter(array2[1]));
			
			if (array1.length > 2) {
				// parse variant values
				localizedString.setVariants(new HashMap<Locale, String>());
				for (int x = 2; x < array1.length; x++) {
					array2 = array1[x].split("(?<!\\\\)" + PARTITION);
					Locale loc = LocaleUtility.fromSpecification(array2[0]);
					if (array2.length == 1)
						localizedString.getVariants().put(loc, "");
					else
						localizedString.getVariants().put(loc, unescapeDelimiter(array2[1]));
				}
			}
		}
		return localizedString;
	}
	
	/**
	 * A static utility method to serialize a LocalizedString object to a String. <br />
	 * 
	 * <pre>
	 * Serialization mechanism:
	 * Object Value ---> Database Text:
	 * {unlocalizedValue: "Favorite Color", variants: null} ---> Favorite Color
	 * {unlocalizedValue: "Favorite Color", variants: [en_UK = "Favourite Colour", fr = "Couleur pr__e"]} ---> i18n:v1;unlocalized:Favorite Color;en_UK:Favourite Colour;fr:Couleur pr__e;
	 * </pre>
	 * 
	 * @param localizedString - the {@link LocalizedString} object to serialize
	 * @return the string get by serializing the passed localizedString
	 * @should return null if given localizedString is null
	 * @should not fail if given localizedString hasnt variants
	 * @should serialize correctly if given localizedString has variants
	 * @should escape correctly if given localizedString has a name including delimiter
	 */
	public static String serialize(LocalizedString localizedString) {
		if (localizedString == null)
			return null;
		StringBuffer sb = new StringBuffer("");
		if (localizedString.getUnlocalizedValue() != null)
			sb.append(escapeDelimiter(localizedString.getUnlocalizedValue()));
		if (localizedString.getVariants() != null && !localizedString.getVariants().isEmpty()) {
			sb.insert(0, HEADER);
			sb.insert(HEADER.length(), "unlocalized" + PARTITION);
			sb.append(SPLITTER);
			Iterator<Entry<Locale, String>> it = localizedString.getVariants().entrySet().iterator();
			while (it.hasNext()) {
				Entry<Locale, String> entry = it.next();
				sb.append(entry.getKey());
				sb.append(PARTITION);
				sb.append(escapeDelimiter(entry.getValue()));
				sb.append(SPLITTER);
			}
		}
		return sb.toString();
	}
	
	/**
	 * Escape the passed text, such as convert ":" to "\:" or convert ";" to "\;"
	 * 
	 * @param text - string to escape
	 * @return a escaped string
	 */
	public static String escapeDelimiter(String text) {
		return StringUtils.replaceEach(text, new String[] { PARTITION, SPLITTER }, new String[] { "\\" + PARTITION,
		        "\\" + SPLITTER });
	}
	
	/**
	 * Unescape the passed text, such as convert "\:" to ":" or convert "\;" to ";"
	 * 
	 * @param text - string to unescape
	 * @return a unescaped string
	 */
	public static String unescapeDelimiter(String text) {
		return StringUtils.replaceEach(text, new String[] { "\\" + PARTITION, "\\" + SPLITTER }, new String[] { PARTITION,
		        SPLITTER });
	}
}
