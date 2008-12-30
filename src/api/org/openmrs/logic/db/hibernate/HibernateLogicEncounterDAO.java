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
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.api.context.Context;
import org.openmrs.logic.Duration;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.LogicExpression;
import org.openmrs.logic.LogicExpressionBinary;
import org.openmrs.logic.LogicTransform;
import org.openmrs.logic.db.LogicEncounterDAO;
import org.openmrs.logic.op.Operator;

/**
 * 
 */
public class HibernateLogicEncounterDAO implements LogicEncounterDAO {
	
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
		
		//if the leftOperand is a String and does not match any components,
		//see if it is a concept name and restrict accordingly
		//a null operator implies a concept restriction
		if (leftOperand instanceof LogicExpression) {
			String conceptName = logicExpression.getRootToken();
			
			Concept concept = Context.getConceptService().getConceptByIdOrName(conceptName);
			criterion.add(Restrictions.eq("concept", concept));
		}
		
		if (operator == null) {
			String conceptName = logicExpression.getRootToken();
			
			Concept concept = Context.getConceptService().getConceptByIdOrName(conceptName);
			criterion.add(Restrictions.eq("concept", concept));
		} else if (operator == Operator.BEFORE) {
			criterion.add(Restrictions.lt("encounterDatetime", rightOperand));
			
		} else if (operator == Operator.AFTER) {
			criterion.add(Restrictions.gt("encounterDatetime", rightOperand));
			
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
			
		} else if (operator == Operator.CONTAINS) {

			//Not supported
		} else if (operator == Operator.EQUALS) {
			if (rightOperand instanceof Date)
				criterion.add(Restrictions.eq("encounterDatetime", rightOperand));
			else
				log.error("Invalid operand value for EQUALS operation");
			
		} else if (operator == Operator.LTE) {
			if (rightOperand instanceof Date)
				criterion.add(Restrictions.le("encounterDatetime", rightOperand));
			else
				log.error("Invalid operand value for LESS THAN EQUAL operation");
			
		} else if (operator == Operator.GTE) {
			if (rightOperand instanceof Date)
				criterion.add(Restrictions.ge("encounterDatetime", rightOperand));
			else
				log.error("Invalid operand value for GREATER THAN EQUAL operation");
			
		} else if (operator == Operator.LT) {
			if (rightOperand instanceof Date)
				criterion.add(Restrictions.lt("encounterDatetime", rightOperand));
			else
				log.error("Invalid operand value for LESS THAN operation");
			
		} else if (operator == Operator.GT) {
			if (rightOperand instanceof Date)
				criterion.add(Restrictions.gt("encounterDatetime", rightOperand));
			else
				log.error("Invalid operand value for GREATER THAN operation");
			
		} else if (operator == Operator.EXISTS) {
			// EXISTS can be handled on the higher level (above
			// LogicService, even) by coercing the Result into a Boolean for
			// each patient
		} else if (operator == Operator.ASOF && rightOperand instanceof Date) {
			indexDate = (Date) rightOperand;
			criterion.add(Restrictions.le("encounterDatetime", indexDate));
			
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
				criterion.add(Restrictions.between("encounterDatetime", within.getTime(), indexDate));
			} else {
				criterion.add(Restrictions.between("encounterDatetime", indexDate, within.getTime()));
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
	private List<Encounter> logicToHibernate(LogicExpression expression, Cohort who) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Encounter.class);
		
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
		if (transformOperator == Operator.LAST) {
			criteria.addOrder(Order.desc("encounterDatetime")).addOrder(Order.desc("dateCreated")).addOrder(
			    Order.desc("encounterId"));
		} else if (transformOperator == Operator.FIRST) {
			criteria.addOrder(Order.asc("encounterDatetime")).addOrder(Order.asc("encounterId"));
		} else if (transformOperator == Operator.DISTINCT) {
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		}
		
		Criterion c = this.getCriterion(expression, indexDate);
		if (c != null) {
			criteria.add(c);
		}
		
		List<Encounter> results = new ArrayList<Encounter>();
		
		criteria.add(Restrictions.eq("voided", false));
		criteria.add(Restrictions.in("person.personId", who.getMemberIds()));
		results.addAll(criteria.list());
		
		//return a single result per patient for these operators
		//I don't see an easy way to do this in hibernate so I am
		//doing some postprocessing
		if (transformOperator == Operator.FIRST || transformOperator == Operator.LAST) {
			HashMap<Integer, ArrayList<Encounter>> nResultMap = new HashMap<Integer, ArrayList<Encounter>>();
			
			for (Encounter currResult : results) {
				Integer currPersonId = currResult.getPatient().getPersonId();
				ArrayList<Encounter> prevResults = nResultMap.get(currPersonId);
				if (prevResults == null) {
					prevResults = new ArrayList<Encounter>();
					nResultMap.put(currPersonId, prevResults);
				}
				
				if (prevResults.size() < numResults) {
					prevResults.add(currResult);
				}
			}
			
			if (nResultMap.values().size() > 0) {
				results.clear();
				
				for (ArrayList<Encounter> currPatientEncounter : nResultMap.values()) {
					results.addAll(currPatientEncounter);
				}
			}
		}
		return results;
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncounters(org.openmrs.Person,
	 *      org.openmrs.logic.LogicCriteria)
	 */
	@SuppressWarnings("unchecked")
	public List<Encounter> getEncounters(Cohort who, LogicCriteria logicCriteria) {
		return logicToHibernate(logicCriteria.getExpression(), who);
	}
	
}
