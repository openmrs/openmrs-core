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
package org.openmrs.logic;

public class Aggregation {

	public static final int UNASSIGNED = 1;
	public static final int COUNT = 2;
	public static final int EXIST = 3;
	public static final int AVERAGE = 4;
	public static final int MEDIAN = 5;
	public static final int SUM = 6;
	public static final int STDDEV = 7;
	public static final int VARIANCE = 8;
	public static final int MINIMUM = 9;
	public static final int MAXIMUM = 10;
	public static final int LAST = 11;
	public static final int FIRST = 12;
	public static final int ANY = 13;
	public static final int ALL = 14;
	public static final int NO = 15;
	public static final int LATEST = 16;
	public static final int EARLIEST = 17;
	public static final int N_MINIMUM = 18;
	public static final int N_MAXIMUM = 19;
	public static final int N_FIRST = 20;
	public static final int N_LAST = 21;
	public static final int N_EARLIEST = 22;
	public static final int N_LATEST = 23;

	private static Aggregation count, exist, average, median, sum, stddev, variance,
			minimum, maximum, last, first, any, all, no, latest, earliest;

	int type = UNASSIGNED;
	int n = 0;

	private Aggregation(int type) {
		this.type = type;
	}

	private Aggregation(int type, int n) {
		this.type = type;
		this.n = n;
	}
	
	public int getType() {
		return type;
	}
	
	public int getN() {
		return n;
	}

	public static Aggregation count() {
		if (count == null)
			count = new Aggregation(COUNT);
		return count;
	}

	public static Aggregation exist() {
		if (exist == null)
			exist = new Aggregation(EXIST);
		return exist;
	}

	public static Aggregation average() {
		if (average == null)
			average = new Aggregation(AVERAGE);
		return average;
	}

	public static Aggregation median() {
		if (median == null)
			median = new Aggregation(MEDIAN);
		return median;
	}

	public static Aggregation sum() {
		if (sum == null)
			sum = new Aggregation(SUM);
		return sum;
	}

	public static Aggregation stddev() {
		if (stddev == null)
			stddev = new Aggregation(STDDEV);
		return stddev;
	}

	public static Aggregation variance() {
		if (variance == null)
			variance = new Aggregation(VARIANCE);
		return variance;
	}

	public static Aggregation minimum() {
		if (minimum == null)
			minimum = new Aggregation(MINIMUM);
		return minimum;
	}

	public static Aggregation maximum() {
		if (maximum == null)
			maximum = new Aggregation(MAXIMUM);
		return maximum;
	}

	public static Aggregation last() {
		if (last == null)
			last = new Aggregation(LAST);
		return last;
	}

	public static Aggregation first() {
		if (first == null)
			first = new Aggregation(FIRST);
		return first;
	}

	public static Aggregation any() {
		if (any == null)
			any = new Aggregation(ANY);
		return any;
	}

	public static Aggregation all() {
		if (all == null)
			all = new Aggregation(ALL);
		return all;
	}

	public static Aggregation no() {
		if (no == null)
			no = new Aggregation(NO);
		return no;
	}

	public static Aggregation latest() {
		if (latest == null)
			latest = new Aggregation(LATEST);
		return latest;
	}

	public static Aggregation earliest() {
		if (earliest == null)
			earliest = new Aggregation(EARLIEST);
		return earliest;
	}

	public static Aggregation minimum(int n) {
		return new Aggregation(N_MINIMUM, n);
	}

	public static Aggregation maximum(int n) {
		return new Aggregation(N_MAXIMUM, n);
	}

	public static Aggregation first(int n) {
		return new Aggregation(N_FIRST, n);
	}

	public static Aggregation last(int n) {
		return new Aggregation(N_LAST, n);
	}

	public static Aggregation earliest(int n) {
		return new Aggregation(N_EARLIEST, n);
	}

	public static Aggregation latest(int n) {
		return new Aggregation(N_LATEST, n);
	}

}
