package org.openmrs.arden;

import org.openmrs.Concept;

/**
 * 
 * @author vanand
 *Arden Clauses are represented by ArdenClause classes. 
 *Clauses can be constructed from concepts, rules, or other clauses. 
 *They also expose aggregate and predicate methods such as earliest(), latest(), max(), and greaterThan(n). 
 *Every method returns another ArdenClause. 
 *The Arden Clauses do not actually fetch data or return values; rather, they represent a semantic hierarchy
 *needed to resolve a particular Arden Value. The criteria are abstracted 
 *so an Arden Clause can be passed down to the database layer and, ideally, be translated to SQL
 *or similar language. 
 */

public class ArdenClause {
	private ArdenRule rule;
	private Concept concept;
	private ArdenClause clause;
	public static enum Predicate {latest,earliest,max, min,avg,
									greaterThan,lessThan, greaterThanEqual, lessThanEqual
									};
	private Predicate predicate;
	private Double predicateVal;

/*
 * Default Constructor
 */	
	
public ArdenClause(){
	
}
public  ArdenClause concept(Concept c) {
	concept = c;
	return this;
}
public ArdenClause rule(ArdenRule r) {
	rule = r;
	return this;
}
public ArdenClause clause(ArdenClause cl){
	clause = cl;
	return this;
}

/*
 * getters
 */
public Concept getConcept(){
	return concept;
}

public Predicate getPredicate() {
	return predicate;
	
}

public ArdenClause latest(){
	predicate = Predicate.latest;
	return this;
}
public ArdenClause latest(double val){
	predicate = Predicate.latest;
	predicateVal = val;
	return this;
}

public ArdenClause earliest(){
	predicate = Predicate.earliest;
	return this;
}
public ArdenClause earliest(double val){
	predicate = Predicate.earliest;
	predicateVal = val;
	return this;
}
public ArdenClause max(){
	predicate = Predicate.max;
	return this;
}

public ArdenClause min(){
	predicate = Predicate.min;
	return this;
}

public ArdenClause avg(){
	predicate = Predicate.avg;
	return this;
}

public ArdenClause greaterThan(double val){
	predicate = Predicate.greaterThan;
	predicateVal = val;
	return this;
}

public ArdenClause lessThan(double val){
	predicate = Predicate.lessThan;
	predicateVal = val;
	return this;
}


public ArdenClause greaterThanEqual(double val){
	predicate = Predicate.greaterThanEqual;
	predicateVal = val;
	return this;
}

public ArdenClause lessThanEqual(double val){
	predicate = Predicate.lessThanEqual;
	predicateVal = val;
	return this;
}
}
