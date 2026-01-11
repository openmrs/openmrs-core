/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util

import org.openmrs.OpenmrsMetadata
import java.io.Serializable
import java.util.Locale

/**
 * A comparator that sorts first based on non-retired, and second based on name. (Locale is
 * currently not used, but will be when we add the ability to localize metadata.)
 *
 * @since 1.7
 */
class MetadataComparator(locale: Locale?) : Comparator<OpenmrsMetadata>, Serializable {
	
	init {
		// locale is currently not used
	}
	
	/**
	 * @see Comparator.compare
	 */
	override fun compare(left: OpenmrsMetadata, right: OpenmrsMetadata): Int {
		var temp = OpenmrsUtil.compareWithNullAsLowest(left.retired, right.retired)
		if (temp == 0) {
			temp = OpenmrsUtil.compareWithNullAsLowest(left.name, right.name)
		}
		return temp
	}
	
	companion object {
		private const val serialVersionUID = 1L
	}
}
