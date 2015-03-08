/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.arden;

import org.openmrs.Concept;

/**
 * Arden Clauses are represented by ArdenClause classes. Clauses can be constructed from concepts,
 * rules, or other clauses. They also expose aggregate and predicate methods such as earliest(),
 * latest(), max(), and greaterThan(n). Every method returns another ArdenClause. The Arden Clauses
 * do not actually fetch data or return values; rather, they represent a semantic hierarchy needed
 * to resolve a particular Arden Value. The criteria are abstracted so an Arden Clause can be passed
 * down to the database layer and, ideally, be translated to SQL or similar language.
 */

public class ArdenClause {
	
	private ArdenRule rule;
	
	private Concept concept;
	
	private ArdenClause clause;
	
	public static enum Predicate {
		last,
		first,
		max,
		min,
		avg,
		greaterThan,
		lessThan,
		greaterThanEqual,
		lessThanEqual
	};
	
	private Predicate predicate;
	
	private Double predicateVal;
	
	/*
	 * Default Constructor
	 */

	public ArdenClause() {
		
	}
	
	public ArdenClause concept(Concept c) {
		concept = c;
		return this;
	}
	
	public ArdenClause rule(ArdenRule r) {
		rule = r;
		return this;
	}
	
	public ArdenClause clause(ArdenClause cl) {
		clause = cl;
		return this;
	}
	
	/*
	* getters
	*/
	public Concept getConcept() {
		return concept;
	}
	
	public Predicate getPredicate() {
		return predicate;
		
	}
	
	public ArdenClause last() {
		predicate = Predicate.last;
		return this;
	}
	
	public ArdenClause last(double val) {
		predicate = Predicate.last;
		predicateVal = val;
		return this;
	}
	
	public ArdenClause first() {
		predicate = Predicate.first;
		return this;
	}
	
	public ArdenClause first(double val) {
		predicate = Predicate.first;
		predicateVal = val;
		return this;
	}
	
	public ArdenClause max() {
		predicate = Predicate.max;
		return this;
	}
	
	public ArdenClause min() {
		predicate = Predicate.min;
		return this;
	}
	
	public ArdenClause avg() {
		predicate = Predicate.avg;
		return this;
		
	}
	
	public ArdenClause greaterThan(double val) {
		predicate = Predicate.greaterThan;
		predicateVal = val;
		return this;
	}
	
	public ArdenClause lessThan(double val) {
		predicate = Predicate.lessThan;
		predicateVal = val;
		return this;
	}
	
	public ArdenClause greaterThanEqual(double val) {
		predicate = Predicate.greaterThanEqual;
		predicateVal = val;
		return this;
	}
	
	public ArdenClause lessThanEqual(double val) {
		predicate = Predicate.lessThanEqual;
		predicateVal = val;
		return this;
	}
	
	/*
	* Temporal operations
	*/
	public ArdenClause within() {
		return this;
	}
	
	public ArdenClause within(String str1, String str2) {
		return this;
	}
	
	public ArdenClause past() {
		return this;
	}
	
	public ArdenClause after(String str) {
		return this;
	}
	
	public ArdenClause before(String str) {
		return this;
	}
	
	public ArdenClause at(String str) {
		return this;
	}
	
	public ArdenClause equal(String str) {
		return this;
	}
	
	public ArdenClause Days(int val) {
		return this;
	}
	
	public ArdenClause Years(int val) {
		return this;
	}
	
}
