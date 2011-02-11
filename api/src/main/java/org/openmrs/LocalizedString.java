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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.util.LocalizedStringUtil;

/**
 * This class will essentially encapsulate an unlocalized String value, and an optional Map of
 * Locale->String variants. <br />
 * This will be the data type for any fields that we wish to Localize.
 * 
 * @since 1.9
 */
public class LocalizedString implements Serializable {
	
	public static final long serialVersionUID = 533456781L;
	
	private String unlocalizedValue;
	
	private Map<Locale, String> variants;
	
	/**
	 * default constructor
	 */
	public LocalizedString() {
	}
	
	/**
	 * copy constructor
	 */
	public LocalizedString(LocalizedString ls) {
		if (ls != null) {
			if (ls.getUnlocalizedValue() != null)
				unlocalizedValue = new String(ls.getUnlocalizedValue());
			if (ls.getVariants() != null) {
				variants = new HashMap<Locale, String>();
				variants.putAll(ls.getVariants());
			}
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
			String right = LocalizedStringUtil.serialize(ls);
			String left = LocalizedStringUtil.serialize(this);
			return left.equals(right);
		}
		return this == obj;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return LocalizedStringUtil.serialize(this).hashCode();
	}
	
	/**
	 * Get a LocalizedString object by deserializing this given s
	 * 
	 * @param s - string to deserialize
	 * @return a LocalizedString object
	 * @see LocalizedStringUtil#deserialize(String)
	 */
	public static LocalizedString valueOf(String s) {
		return LocalizedStringUtil.deserialize(s);
	}
	
	/**
	 * @see java.lang.Object#toString()
	 * @see #getValue()
	 */
	@Override
	public String toString() {
		return getValue();
	}
}
