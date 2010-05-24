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

/**
 * This class is responsible for the de/serialization between LocalizedString object and a single
 * string
 */
public class LocalizedStringSerializer implements OpenmrsSerializer {
	
	/**
	 * This is the separator between the unlocalized value and the localized values(variants)
	 */
	private static final String SEPERATOR = "^v1^";
	
	/**
	 * This is the separator between locale and string value
	 */
	private static final String PARTITION = ":";
	
	/**
	 * This is the separator between each pair which includes one locale and string value
	 */
	private static final String SPLITTER = ";";
	
	/**
	 * A utility method to deserialize a String to a LocalizedString object. <br />
	 * 
	 * <pre>
	 * 		Deserialization mechanism:
	 * 		Database Text ---> Object Value:
	 * 		Favorite Color ---> {unlocalizedValue: "Favorite Color", variants: null}
	 * 		Favorite Color^v1^en_UK:Favourite Colour;fr:Couleur pr¨¦f¨¦r¨¦e ---> {unlocalizedValue: "Favorite Color", variants: [en_UK = "Favourite Colour", fr = "Couleur pr¨¦f¨¦r¨¦e"]}
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
			//escape the special character '^'
			String[] array1 = serializedObject.split(StringUtils.replaceEach(SEPERATOR, new String[] { "^" },
			    new String[] { "\\^" }));
			ls.setUnlocalizedValue(array1[0]);
			if (array1.length > 1) {/*has optional variants*/
				ls.setVariants(new HashMap<Locale, String>());
				String[] array2 = array1[1].split(SPLITTER);
				for (String str : array2) {
					String[] array3 = str.split(PARTITION);
					Locale loc = LocaleUtility.fromSpecification(array3[0]);
					//because string value is optional, so it may be empty within locale, we need to check such case
					if (array3.length == 1)
						ls.getVariants().put(loc, "");
					else
						ls.getVariants().put(loc, array3[1]);
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
	 * {unlocalizedValue: "Favorite Color", variants: [en_UK = "Favourite Colour", fr = "Couleur pr¨¦f¨¦r¨¦e"]} ---> Favorite Color^v1^en_UK:Favourite Colour;fr:Couleur pr¨¦f¨¦r¨¦e
	 * </pre>
	 * 
	 * @param o - the passed object which will be cast to LocalizedString type
	 * @return the string get by serializing the passed object
	 * @should return null if given object is null
	 * @should not fail if given object hasnt variants
	 * @should serialize correctly if given object has variants
	 * @should throw a SerializationException if given object doesnt belong to LocalizedString
	 * @see org.openmrs.serialization.OpenmrsSerializer#serialize(java.lang.Object)
	 */
	@Override
	public String serialize(Object o) throws SerializationException {
		if (o == null)
			return null;
		if (o instanceof LocalizedString) {
			LocalizedString localizedString = (LocalizedString) o;
			StringBuffer sb = new StringBuffer("");
			sb.append(localizedString.getUnlocalizedValue());
			if (localizedString.getVariants() != null && !localizedString.getVariants().isEmpty()) {
				sb.append(SEPERATOR);
				Iterator<Entry<Locale, String>> it = localizedString.getVariants().entrySet().iterator();
				while (it.hasNext()) {
					Entry<Locale, String> entry = it.next();
					sb.append(entry.getKey());
					sb.append(PARTITION);
					sb.append(entry.getValue());
					if (it.hasNext())
						sb.append(SPLITTER);
				}
			}
			return sb.toString();
		} else {
			throw new SerializationException("Can not serialize an object of type:" + o.getClass().getName());
		}
	}
	
}
