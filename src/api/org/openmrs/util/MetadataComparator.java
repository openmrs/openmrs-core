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

import java.util.Comparator;
import java.util.Locale;

import org.openmrs.LocalizedMetadata;
import org.openmrs.OpenmrsMetadata;

/**
 * A comparator that sorts first based on non-retired, and second based on name. <br />
 * Compare name within the specified locale(specified by constructor's passed argument 'locale').
 * 
 * @since 1.7
 */
public class MetadataComparator implements Comparator<OpenmrsMetadata> {
	
	private Locale locale;
	
	/**
	 * @param locale
	 */
	public MetadataComparator(Locale locale) {
		this.locale = locale;
	}
	
	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(OpenmrsMetadata left, OpenmrsMetadata right) {
		int temp = OpenmrsUtil.compareWithNullAsLowest(left.isRetired(), right.isRetired());
		if (temp == 0) {
			if (left instanceof LocalizedMetadata && right instanceof LocalizedMetadata) {
				Locale locale = getLocale();
				temp = OpenmrsUtil.compareWithNullAsLowest(((LocalizedMetadata) left).getLocalizedName().getValue(locale),
					((LocalizedMetadata) right).getLocalizedName().getValue(locale));
			} else {
				temp = OpenmrsUtil.compareWithNullAsLowest(left.getName(), right.getName());
			}
		}
		return temp;
	}
	
	/**
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}
	
	/**
	 * @param locale the locale to set
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
}
