/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.logic.op;

public interface Operator {

	/**
	 * @deprecated Use {@link OperatorConstants#CONTAINS} instead.
	 */
	@Deprecated
	public static final Operator CONTAINS = OperatorConstants.CONTAINS;

	/**
	 * @deprecated Use {@link OperatorConstants#EQUALS} instead.
	 */
	@Deprecated
	public static final Operator EQUALS = OperatorConstants.EQUALS;

	/**
	 * @deprecated Use {@link OperatorConstants#WITHIN} instead.
	 */
	@Deprecated
	public static final Operator WITHIN = OperatorConstants.WITHIN;

	/**
	 * @deprecated Use {@link OperatorConstants#GT} instead.
	 */
	@Deprecated
	public static final Operator GT = OperatorConstants.GT;

	/**
	 * @deprecated Use {@link OperatorConstants#GTE} instead.
	 */
	@Deprecated
	public static final Operator GTE = OperatorConstants.GTE;

	/**
	 * @deprecated Use {@link OperatorConstants#LT} instead.
	 */
	@Deprecated
	public static final Operator LT = OperatorConstants.LT;

	/**
	 * @deprecated Use {@link OperatorConstants#LTE} instead.
	 */
	@Deprecated
	public static final Operator LTE = OperatorConstants.LTE;

	/**
	 * @deprecated Use {@link OperatorConstants#BEFORE} instead.
	 */
	@Deprecated
	public static final Operator BEFORE = OperatorConstants.BEFORE;

	/**
	 * @deprecated Use {@link OperatorConstants#AFTER} instead.
	 */
	@Deprecated
	public static final Operator AFTER = OperatorConstants.AFTER;

	/**
	 * @deprecated Use {@link OperatorConstants#IN} instead.
	 */
	@Deprecated
	public static final Operator IN = OperatorConstants.IN;

	/**
	 * @deprecated Use {@link OperatorConstants#ASOF} instead.
	 */
	@Deprecated
	public static final Operator ASOF = OperatorConstants.ASOF;

	/**
	 * @deprecated Use {@link OperatorConstants#AND} instead.
	 */
	@Deprecated
	public static final Operator AND = OperatorConstants.AND;

	/**
	 * @deprecated Use {@link OperatorConstants#OR} instead.
	 */
	@Deprecated
	public static final Operator OR = OperatorConstants.OR;

	/**
	 * @deprecated Use {@link OperatorConstants#NOT} instead.
	 */
	@Deprecated
	public static final Operator NOT = OperatorConstants.NOT;

	/**
	 * @deprecated Use {@link OperatorConstants#LAST} instead.
	 */
	@Deprecated
	public static final Operator LAST = OperatorConstants.LAST;

	/**
	 * @deprecated Use {@link OperatorConstants#FIRST} instead.
	 */
	@Deprecated
	public static final Operator FIRST = OperatorConstants.FIRST;

	/**
	 * @deprecated Use {@link OperatorConstants#DISTINCT} instead.
	 */
	@Deprecated
	public static final Operator DISTINCT = OperatorConstants.DISTINCT;

	/**
	 * @deprecated Use {@link OperatorConstants#EXISTS} instead.
	 */
	@Deprecated
	public static final Operator EXISTS = OperatorConstants.EXISTS;

	/**
	 * @deprecated Use {@link OperatorConstants#NOT_EXISTS} instead.
	 */
	@Deprecated
	public static final Operator NOT_EXISTS = OperatorConstants.NOT_EXISTS;

	/**
	 * @deprecated Use {@link OperatorConstants#COUNT} instead.
	 */
	@Deprecated
	public static final Operator COUNT = OperatorConstants.COUNT;

	/**
	 * @deprecated Use {@link OperatorConstants#AVERAGE} instead.
	 */
	@Deprecated
	public static final Operator AVERAGE = OperatorConstants.AVERAGE;

}
public interface Operator {

}
