/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import java.util.Date;

import static org.apache.commons.lang3.time.DateUtils.addSeconds;

/**
 * @since 2.3.4
 */
public abstract class BaseDosingInstructions implements DosingInstructions {

	/**
	 * @see DosingInstructions#getAutoExpireDate(DrugOrder)
	 */
	@Override
	public Date getAutoExpireDate(DrugOrder drugOrder) {
		if (drugOrder.getDuration() == null || drugOrder.getDurationUnits() == null) {
			return null;
		}
		String durationCode = Duration.getCode(drugOrder.getDurationUnits());
		if (durationCode == null) {
			return null;
		}
		Duration duration = new Duration(drugOrder.getDuration(), durationCode);
		return aMomentBefore(duration.addToDate(drugOrder.getEffectiveStartDate(), drugOrder.getFrequency()));
	}

	private Date aMomentBefore(Date date) {
		return addSeconds(date, -1);
	}
}
