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
package org.openmrs.logic.db.hibernate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Person;
import org.openmrs.logic.Duration;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.LogicExpression;
import org.openmrs.logic.LogicExpressionBinary;
import org.openmrs.logic.LogicTransform;
import org.openmrs.logic.db.LogicPersonDAO;
import org.openmrs.logic.op.Operator;

/**
 * 
 */
public class HibernateLogicPersonDAO implements LogicPersonDAO {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	private Criterion getCriterion(LogicExpression logicExpression, Date indexDate) {
		Operator operator = logicExpression.getOperator();
		Object rightOperand = logicExpression.getRightOperand();
		Object leftOperand = null;
		
		if (logicExpression instanceof LogicExpressionBinary) {
			leftOperand = ((LogicExpressionBinary) logicExpression).getLeftOperand();
		}
		List<Criterion> criterion = new ArrayList<Criterion>();
		String attr = "";
		String token = logicExpression.getRootToken();
		
		if (token.equalsIgnoreCase("GENDER"))
			attr = "gender";
		else if (token.equalsIgnoreCase("BIRTHDATE"))
			attr = "birthdate";
		else if (token.equalsIgnoreCase("BIRTHDATE ESTIMATED"))
			attr = "birthdateEstimated";
		else if (token.equalsIgnoreCase("DEAD"))
			attr = "dead";
		else if (token.equalsIgnoreCase("DEATH DATE"))
			attr = "deathDate";
		else if (token.equalsIgnoreCase("CAUSE OF DEATH"))
			attr = "causeOfDeath";
		// TODO add support for all attributes from Person.java
		else {
			log.error("Illegal or unsupported token:" + token);
		}
		
		if (operator == Operator.BEFORE || operator == Operator.LT) {
			criterion.add(Restrictions.lt(attr, rightOperand));
			
		} else if (operator == Operator.AFTER || operator == Operator.GT) {
			criterion.add(Restrictions.gt(attr, rightOperand));
			
		} else if (operator == Operator.AND || operator == Operator.OR) {
			
			Criterion leftCriteria = null;
			Criterion rightCriteria = null;
			
			if (leftOperand instanceof LogicExpression) {
				leftCriteria = this.getCriterion((LogicExpression) leftOperand, indexDate);
			}
			if (rightOperand instanceof LogicExpression) {
				rightCriteria = this.getCriterion((LogicExpression) rightOperand, indexDate);
			}
			
			if (leftCriteria != null && rightCriteria != null) {
				if (operator == Operator.AND) {
					criterion.add(Restrictions.and(leftCriteria, rightCriteria));
				}
				if (operator == Operator.OR) {
					criterion.add(Restrictions.or(leftCriteria, rightCriteria));
				}
			}
		} else if (operator == Operator.NOT) {
			
			Criterion rightCriteria = null;
			
			if (rightOperand instanceof LogicExpression) {
				rightCriteria = this.getCriterion((LogicExpression) rightOperand, indexDate);
			}
			
			if (rightCriteria != null) {
				criterion.add(Restrictions.not(rightCriteria));
			}
			
		} else if (operator == Operator.CONTAINS || operator == Operator.EQUALS) {
			
			criterion.add(Restrictions.eq(attr, rightOperand));
			
		} else if (operator == Operator.LTE) {
			
			criterion.add(Restrictions.le(attr, rightOperand));
			
		} else if (operator == Operator.GTE) {
			
			criterion.add(Restrictions.ge(attr, rightOperand));
			
		} else if (operator == Operator.EXISTS) {
			// EXISTS can be handled on the higher level (above
			// LogicService, even) by coercing the Result into a Boolean for
			// each patient
		} else if (operator == Operator.ASOF && rightOperand instanceof Date) {
			indexDate = (Date) rightOperand;
			criterion.add(Restrictions.le(attr, indexDate));
			
		} else if (operator == Operator.WITHIN && rightOperand instanceof Duration) {
			
			Duration duration = (Duration) rightOperand;
			Calendar within = Calendar.getInstance();
			within.setTime(indexDate);
			
			if (duration.getUnits() == Duration.Units.YEARS) {
				within.add(Calendar.YEAR, duration.getDuration().intValue());
			} else if (duration.getUnits() == Duration.Units.MONTHS) {
				within.add(Calendar.MONTH, duration.getDuration().intValue());
			} else if (duration.getUnits() == Duration.Units.WEEKS) {
				within.add(Calendar.WEEK_OF_YEAR, duration.getDuration().intValue());
			} else if (duration.getUnits() == Duration.Units.DAYS) {
				within.add(Calendar.DAY_OF_YEAR, duration.getDuration().intValue());
			} else if (duration.getUnits() == Duration.Units.MINUTES) {
				within.add(Calendar.MINUTE, duration.getDuration().intValue());
			} else if (duration.getUnits() == Duration.Units.SECONDS) {
				within.add(Calendar.SECOND, duration.getDuration().intValue());
			}
			
			if (indexDate.compareTo(within.getTime()) > 0) {
				criterion.add(Restrictions.between(attr, within.getTime(), indexDate));
			} else {
				criterion.add(Restrictions.between(attr, indexDate, within.getTime()));
			}
		}
		
		Criterion c = null;
		
		for (Criterion crit : criterion) {
			if (c == null) {
				c = crit;
			} else {
				c = Restrictions.and(c, crit);
			}
		}
		return c;
	}
	
	// Helper function, converts logic service's criteria into Hibernate's
	// criteria
	private List<Person> logicToHibernate(LogicExpression expression, Collection<Integer> personIds) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Person.class);
		
		Date indexDate = Calendar.getInstance().getTime();
		Operator transformOperator = null;
		LogicTransform transform = expression.getTransform();
		Integer numResults = null;
		
		if (transform != null) {
			transformOperator = transform.getTransformOperator();
			numResults = transform.getNumResults();
		}
		
		if (numResults == null) {
			numResults = 1;
		}
		// set the transform and evaluate the right criteria
		// if there is any
		if (transformOperator == Operator.DISTINCT) {
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		}
		Criterion c = this.getCriterion(expression, indexDate);
		if (c != null) {
			criteria.add(c);
		}
		List<Person> results = new ArrayList<Person>();
		
		criteria.add(Restrictions.in("personId", personIds));
		results.addAll(criteria.list());
		
		//return a single result per patient for these operators
		//I don't see an easy way to do this in hibernate so I am
		//doing some postprocessing
		if (transformOperator == Operator.FIRST || transformOperator == Operator.LAST) {
			HashMap<Integer, ArrayList<Person>> nResultMap = new HashMap<Integer, ArrayList<Person>>();
			
			for (Person currResult : results) {
				Integer currPersonId = currResult.getPersonId();
				ArrayList<Person> prevResults = nResultMap.get(currPersonId);
				if (prevResults == null) {
					prevResults = new ArrayList<Person>();
					nResultMap.put(currPersonId, prevResults);
				}
				
				if (prevResults.size() < numResults) {
					prevResults.add(currResult);
				}
			}
			
			if (nResultMap.values().size() > 0) {
				results.clear();
				
				for (ArrayList<Person> currPatientPerson : nResultMap.values()) {
					results.addAll(currPatientPerson);
				}
			}
		}
		return results;
	}
	
	/**
	 * @see org.openmrs.api.db.PersonDAO#getPersons(java.util.List, org.openmrs.logic.LogicCriteria)
	 */
	@SuppressWarnings("unchecked")
	public List<Person> getPersons(Collection<Integer> personIds, LogicCriteria logicCriteria) {
		return logicToHibernate(logicCriteria.getExpression(), personIds);
	}
	
}
