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

import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * This is the base class for reference ranges.
 * 
 * @since 2.7.0
 */
@MappedSuperclass
@Audited
public abstract class BaseReferenceRange extends BaseOpenmrsObject implements Serializable {
	
	@Column(name = "hi_absolute")
	private Double hiAbsolute;

	@Column(name = "hi_critical")
	private Double hiCritical;

	@Column(name = "hi_normal")
	private Double hiNormal;

	@Column(name = "low_absolute")
	private Double lowAbsolute;

	@Column(name = "low_critical")
	private Double lowCritical;

	@Column(name = "low_normal")
	private Double lowNormal;

	/**
	 * Gets high absolute value of the referenceRange
	 *
	 * @return hiAbsolute the high absolute value
	 */
	public Double getHiAbsolute() {
		return this.hiAbsolute;
	}

	/**
	 * Sets high absolute value of the referenceRange
	 *
	 * @param hiAbsolute high absolute value to set
	 */
	public void setHiAbsolute(Double hiAbsolute) {
		this.hiAbsolute = hiAbsolute;
	}

	/**
	 * Gets high critical value of the referenceRange
	 *
	 * @return the high critical value
	 */
	public Double getHiCritical() {
		return this.hiCritical;
	}

	/**
	 * Sets high critical value of the referenceRange
	 *
	 * @param hiCritical high critical value to set
	 */
	public void setHiCritical(Double hiCritical) {
		this.hiCritical = hiCritical;
	}

	/**
	 * Returns high normal value of the referenceRange
	 *
	 * @return the high normal value
	 */
	public Double getHiNormal() {
		return this.hiNormal;
	}

	/**
	 * Sets high normal value of the referenceRange
	 *
	 * @param hiNormal high normal value to set
	 */
	public void setHiNormal(Double hiNormal) {
		this.hiNormal = hiNormal;
	}

	/**
	 * Gets low absolute value of the referenceRange
	 *
	 * @return the low absolute value
	 */
	public Double getLowAbsolute() {
		return this.lowAbsolute;
	}

	/**
	 * Sets low absolute value of the referenceRange
	 *
	 * @param lowAbsolute low absolute value to set
	 */
	public void setLowAbsolute(Double lowAbsolute) {
		this.lowAbsolute = lowAbsolute;
	}

	/**
	 * Gets low critical value of the referenceRange
	 *
	 * @return the low critical value
	 */
	public Double getLowCritical() {
		return this.lowCritical;
	}

	/**
	 * Sets low critical value of the referenceRange
	 *
	 * @param lowCritical low critical value to set
	 */
	public void setLowCritical(Double lowCritical) {
		this.lowCritical = lowCritical;
	}

	/**
	 * Gets low normal value of the referenceRange
	 *
	 * @return the low normal value
	 */
	public Double getLowNormal() {
		return this.lowNormal;
	}

	/**
	 * Sets low normal value of the referenceRange
	 *
	 * @param lowNormal low normal value to set
	 */
	public void setLowNormal(Double lowNormal) {
		this.lowNormal = lowNormal;
	}
}
