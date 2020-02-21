/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Represents a bounded or unbounded numeric range. By default the range is closed (ake inclusive)
 * on the low end and open (aka exclusive) on the high end: mathematically "[low, high)". (I'm not
 * using the similarly-named class from Apache commons because it doesn't implement comparable, and
 * because it only allows inclusive bounds.)
 */
public class DoubleRange implements Comparable<DoubleRange> {
	
	private Double low;
	
	private Double high;
	
	private boolean closedLow = true; //TODO: add setters and getters for these
	
	private boolean closedHigh = false;
	
	/**
	 * <strong>Should</strong> return null low and high if accessors are not called
	 */
	public DoubleRange() {
	}
	
	/**
	 * <strong>Should</strong> return infinite low and high if called with null parameters
	 */
	public DoubleRange(Double low, Double high) {
		this.low = low == null ? new Double(Double.NEGATIVE_INFINITY) : low;
		this.high = high == null ? new Double(Double.POSITIVE_INFINITY) : high;
	}
	
	/**
	 * @return Returns the high.
	 * <strong>Should</strong> return correct value of high if it high was set previously
	 * <strong>Should</strong> return positive infinity if high was not set previously
	 */
	public Double getHigh() {
		return high;
	}
	
	/**
	 * @param high The high to set.
	 * <strong>Should</strong> set high to positive infinity on null parameter
	 * <strong>Should</strong> cause low to have the set value
	 */
	public void setHigh(Double high) {
		this.high = high == null ? new Double(Double.POSITIVE_INFINITY) : high;
	}
	
	/**
	 * @return Returns the low.
	 * <strong>Should</strong> return correct value of low if low was set previously
	 * <strong>Should</strong> return negative infinity if low was not set previously
	 */
	public Double getLow() {
		return low;
	}
	
	/**
	 * @param low The low to set.
	 * <strong>Should</strong> set low to negative infinity on null parameter
	 * <strong>Should</strong> cause low to have the set value
	 */
	public void setLow(Double low) {
		this.low = low == null ? new Double(Double.NEGATIVE_INFINITY) : low;
	}
	
	/**
	 * first sorts according to low-bound (ascending) then according to high-bound (descending)
	 * <strong>Should</strong> return plus 1 if this low is greater than other low
	 * <strong>Should</strong> return minus one if this low is lower than other low
	 * <strong>Should</strong> return plus one if both lows are equal but other high is greater than this high
	 * <strong>Should</strong> return minus one if both lows are equal but other high is less than this high
	 * <strong>Should</strong> return zero if both lows and both highs are equal
	 * <strong>Should</strong> return 1 if this range is wider than other range
	 */
	@Override
	public int compareTo(DoubleRange other) {
		int temp = low.compareTo(other.low);
		if (temp == 0) {
			temp = other.high.compareTo(high);
		}
		return temp;
	}
	
	/**
	 * BUG: this method should return false if both ends of the range are null.
	 * It currently returns true in this case.
	 *
	 * checks whether a double is in this range
	 * @param 	d the Double to check for in this range
	 * @return  true if d is in this range, false otherwise
	 * <strong>Should</strong> return true if parameter is in range
	 * <strong>Should</strong> return false if parameter is not in range
	 * <strong>Should</strong> return false if parameter is equal to high
	 * <strong>Should</strong> return true if parameter is equal to low
	 * <strong>Should</strong> return false if parameter is lower than low
	 * <strong>Should</strong> return false if both low and high are null
	 */
	public boolean contains(double d) {
		if (low != null) {
			if (closedLow) {
				if (d < low) {
					return false;
				}
			} else {
				//unreachable code as closedLow is never set to false anywhere
				if (d <= low) {
					return false;
				}
			}
		}
		if (high != null) {
			if (closedHigh) {
				//unreachable code as closedHigh is never set to true anywhere
				return d <= high;
			} else {
				return d < high;
			}
		}
		return true;
	}
	
	/**
	 *
	 * @return a String representation of the DoubleRange
	 * <strong>Should</strong> print the range if high and low are not null and not infinite
	 * <strong>Should</strong> print empty high if high is infinite
	 * <strong>Should</strong> print empty low if low is infinite
	 * <strong>Should</strong> print empty string if low and high are infinite
	 */
	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		if (low != null && low != Double.NEGATIVE_INFINITY) {
			ret.append(">");
			if (closedLow) {
				ret.append("=");
			}
			ret.append(" ").append(Format.format(low));
			if (high != null && high != Double.NEGATIVE_INFINITY) {
				//BUG: should not append this if high is also infinite
				ret.append(" and ");
			}
		}
		if (high != null && high != Double.POSITIVE_INFINITY) {
			ret.append("<");
			if (closedHigh) {
				ret.append("=");
			}
			ret.append(" ").append(Format.format(high));
		}
		return ret.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof DoubleRange) {
			DoubleRange other = (DoubleRange) o;
			return low.equals(other.low) && high.equals(other.high);
		}
		return false;
	}
	
	/**
	 * <strong>Should</strong> return the same hashCode for objects representing the same interval
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(low).append(high).build();
	}
	
}
