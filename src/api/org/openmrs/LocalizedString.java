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
package org.openmrs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.util.LocaleUtility;

/**
 * This class will essentially encapsulate an unlocalized String value, and an optional Map of
 * Locale->String variants. <br />
 * This will be the data type for any fields that we wish to Localize.
 */
public class LocalizedString {
	
	public static final long serialVersionUID = 533456781L;
	
	private String unlocalizedValue;
	
	private Map<Locale, String> variants;
	
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
	 * default constructor
	 */
	public LocalizedString() {
	}
	
	/**
	 * copy constructor
	 */
	public LocalizedString(LocalizedString ls) {
		unlocalizedValue = new String(ls.getUnlocalizedValue());
		if (variants == null && ls.getVariants() != null) {
			variants = new HashMap<Locale, String>();
			variants.putAll(ls.getVariants());
		}
	}
	
	/**
	 * Return a value for current user locale by doing a fuzzy match
	 * 
	 * @return a value for current user locale
	 * @see {@link #getValue(Locale)}
	 */
	public String getValue() {
		return getValue(Context.getLocale());
	}
	
	/**
	 * Return a value for the passed locale by doing a fuzzy match <br />
	 * The fuzzy match strategy:
	 * 
	 * <pre>
	 * (1) Lookup any variant matching the passed locale
	 * (2) Lookup any variant matching the passed locale with language-only
	 * (3) Return the unlocalizedValue
	 * </pre>
	 * 
	 * @param locale - the passed locale
	 * @return a value for the passed locale
	 * @should return unlocalized value if locale is null
	 * @should return a string when variant match locale
	 * @should return a string when variant match locale with language only
	 * @should return unlocalized value when no variant matching locale or locale with language only
	 */
	public String getValue(Locale locale) {
		if (locale != null) {
			if (variants != null && !variants.isEmpty()) {
				if (variants.containsKey(locale))
					return variants.get(locale);
				
				if (!StringUtils.isBlank(locale.getCountry())) {/*lookup within language-only locale*/
					locale = new Locale(locale.getLanguage());
					if (variants.containsKey(locale))
						return variants.get(locale);
				}
			}
		}
		return unlocalizedValue;
	}
	
	/**
	 * @return the unlocalizedValue
	 */
	public String getUnlocalizedValue() {
		return unlocalizedValue;
	}
	
	/**
	 * @param unlocalizedValue the unlocalizedValue to set
	 */
	public void setUnlocalizedValue(String unlocalizedValue) {
		this.unlocalizedValue = unlocalizedValue;
	}
	
	/**
	 * @return the variants
	 */
	public Map<Locale, String> getVariants() {
		return variants;
	}
	
	/**
	 * @param variants the variants to set
	 */
	public void setVariants(Map<Locale, String> variants) {
		this.variants = variants;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 * @should not fail if given obj is null
	 * @should not fail if given obj has null unlocalized value
	 * @should not fail if unlocalized value is null
	 * @should not fail if given obj has null variants
	 * @should not fail if variants is null
	 * @should confirm two new LocalizedString objects are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj instanceof LocalizedString) {
			LocalizedString ls = (LocalizedString) obj;
			if ((unlocalizedValue == null && ls.getUnlocalizedValue() == null)
			        || (unlocalizedValue != null && unlocalizedValue.equals(ls.getUnlocalizedValue()))) {
				if (variants != null)
					return variants.equals(ls.getVariants());
				else
					return ls.getVariants() == null;
			}
		}
		return this == obj;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (unlocalizedValue == null)
			return super.hashCode();
		return unlocalizedValue.hashCode();
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
	 * @param ls - the passed LocalizedString object
	 * @return the String get by serializing the passed LocalizedString object
	 * @should return null if given localizedString is null
	 * @should not fail if given localizedString hasnt variants
	 * @should serialize correctly if given localizedString has variants
	 */
	public static String serialize(LocalizedString localizedString) {
		if (localizedString != null) {
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
		}
		return null;
	}
	
	/**
	 * A utility method to deserialize a String to a LocalizedString object. <br />
	 * 
	 * <pre>
	 * Deserialization mechanism:
	 * Database Text ---> Object Value:
	 * Favorite Color ---> {unlocalizedValue: "Favorite Color", variants: null}
	 * Favorite Color^v1^en_UK:Favourite Colour;fr:Couleur pr¨¦f¨¦r¨¦e ---> {unlocalizedValue: "Favorite Color", variants: [en_UK = "Favourite Colour", fr = "Couleur pr¨¦f¨¦r¨¦e"]}
	 * </pre>
	 * 
	 * @param s - the passed String
	 * @return the LocalizedString object get by deserializing the passed String
	 * @should return null if given s is null
	 * @should return null if given s is empty
	 * @should not fail if given s doesnt contains variants
	 * @should deserialize correctly if given s contains variants
	 */
	public static LocalizedString deserialize(String s) {
		if (!StringUtils.isBlank(s)) {
			LocalizedString ls = new LocalizedString();
			//escape the special character '^'
			String[] array1 = s.split(StringUtils.replaceEach(SEPERATOR, new String[] { "^" }, new String[] { "\\^" }));
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
			return ls;
		}
		return null;
	}
}
