package org.openmrs.logic;

import java.util.Date;


public class DateConstraint implements Constraint {

	public static final int NULL_COMPARISON = 1;
	public static final int EQUAL = 2;
	public static final int NOT_EQUAL = 3;
	public static final int WITHIN = 4;
	public static final int NOT_WITHIN = 5;
	public static final int WITHIN_PRECEDING = 6;
	public static final int NOT_WITHIN_PRECEDING = 7;
	public static final int WITHIN_FOLLOWING = 8;
	public static final int NOT_WITHIN_FOLLOWING = 9;
	public static final int WITHIN_SURROUNDING = 10;
	public static final int NOT_WITHIN_SURROUNDING = 11;
	public static final int WITHIN_PAST = 12;
	public static final int NOT_WITHIN_PAST = 13;
	public static final int WITHIN_SAME_DAY_AS = 14;
	public static final int NOT_WITHIN_SAME_DAY_AS = 15;
	public static final int BEFORE = 16;
	public static final int NOT_BEFORE = 17;
	public static final int AFTER = 18;
	public static final int NOT_AFTER = 19;

	private int comparison = NULL_COMPARISON;
	private Date date;
	private Date secondDate;
	private Duration duration;

	private DateConstraint(int comparison, Date date, Date secondDate, Duration duration) {
		this.comparison = comparison;
		this.date = date;
		this.secondDate = secondDate;
		this.duration = duration;
	}

	public int getComparison() {
		return comparison;
	}

	public Date getDate() {
		return date;
	}

	public Date getSecondDate() {
		return secondDate;
	}

	public Duration getDuration() {
		return duration;
	}

	public static DateConstraint equal(Date date) {
		return new DateConstraint(EQUAL, date, null, null);
	}

	public static DateConstraint notEqual(Date date) {
		return new DateConstraint(NOT_EQUAL, date, null, null);
	}

	public static DateConstraint within(Date date, Date secondDate) {
		return new DateConstraint(WITHIN, date, secondDate, null);
	}

	public static DateConstraint notWithin(Date date, Date secondDate) {
		return new DateConstraint(NOT_WITHIN, date, secondDate, null);
	}

	public static DateConstraint withinPreceding(Duration duration) {
		return withinPreceding(duration, new Date());
	}

	public static DateConstraint withinPreceding(Duration duration, Date date) {
		return new DateConstraint(WITHIN_PRECEDING, date, null, duration);
	}

	public static DateConstraint notWithinPreceding(Duration duration, Date date) {
		return new DateConstraint(NOT_WITHIN_PRECEDING, date, null, duration);
	}

	public static DateConstraint withinFollowing(Duration duration, Date date) {
		return new DateConstraint(WITHIN_FOLLOWING, date, null, duration);
	}

	public static DateConstraint notWithinFollowing(Duration duration, Date date) {
		return new DateConstraint(NOT_WITHIN_FOLLOWING, date, null, duration);
	}

	public static DateConstraint withinSurrounding(Duration duration, Date date) {
		return new DateConstraint(WITHIN_SURROUNDING, date, null, duration);
	}

	public static DateConstraint notWithinSurrounding(Duration duration,
			Date date) {
		return new DateConstraint(NOT_WITHIN_SURROUNDING, date, null, duration);
	}

	public static DateConstraint withinPast(Duration duration, Date date) {
		return new DateConstraint(WITHIN_PAST, date, null, duration);
	}

	public static DateConstraint notWithinPast(Duration duration, Date date) {
		return new DateConstraint(NOT_WITHIN_PAST, date, null, duration);
	}

	public static DateConstraint withinSameDayAs(Date date) {
		return new DateConstraint(WITHIN_SAME_DAY_AS, date, null, null);
	}

	public static DateConstraint notWithinSameDayAs(Date date) {
		return new DateConstraint(NOT_WITHIN_SAME_DAY_AS, date, null, null);
	}

	public static DateConstraint before(Date date) {
		return new DateConstraint(BEFORE, date, null, null);
	}

	public static DateConstraint notBefore(Date date) {
		return new DateConstraint(NOT_BEFORE, date, null, null);
	}

	public static DateConstraint after(Date date) {
		return new DateConstraint(AFTER, date, null, null);
	}

	public static DateConstraint notAfter(Date date) {
		return new DateConstraint(NOT_AFTER, date, null, null);
	}

}
