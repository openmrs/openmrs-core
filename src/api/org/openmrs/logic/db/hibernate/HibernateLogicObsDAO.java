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
import org.openmrs.Obs;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.Duration;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.LogicExpression;
import org.openmrs.logic.LogicExpressionBinary;
import org.openmrs.logic.LogicTransform;
import org.openmrs.logic.db.LogicObsDAO;
import org.openmrs.logic.op.Operator;

/**
 *
 */
public class HibernateLogicObsDAO implements LogicObsDAO {
	
	private static final String COMPONENT_ENCOUNTER_ID = "encounterId";
	
	private static final String COMPONENT_OBS_DATETIME = "obsDatetime";
	
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
	
	private Criterion getCriterion(LogicExpression logicExpression, Date indexDate) throws LogicException {
		Operator operator = logicExpression.getOperator();
		Object rightOperand = logicExpression.getRightOperand();
		Object leftOperand = null;
		if (logicExpression instanceof LogicExpressionBinary) {
			leftOperand = ((LogicExpressionBinary) logicExpression).getLeftOperand();
		}
		List<Criterion> criterion = new ArrayList<Criterion>();
		
		//the root token can be a concept name for the obs datasource
		String rootToken = logicExpression.getRootToken();
		
		Concept concept = Context.getConceptService().getConcept(rootToken);
		if (concept != null) {
			criterion.add(Restrictions.eq("concept", concept));
		} else {
			if (rootToken != null
			        && (rootToken.equalsIgnoreCase(COMPONENT_ENCOUNTER_ID) || rootToken
			                .equalsIgnoreCase(COMPONENT_OBS_DATETIME))) {
				//this is a component not a concept so it is fine
			} else {
				throw new LogicException("Concept: " + rootToken + " does not exist");
			}
		}
		
		if (operator == Operator.BEFORE) {
			criterion.add(Restrictions.lt("obsDatetime", rightOperand));
			
		} else if (operator == Operator.AFTER) {
			criterion.add(Restrictions.gt("obsDatetime", rightOperand));
			
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
			// used with PROBLEM ADDED concept, to retrieve the "ANSWERED
			// BY" concept, stashed inside the concept's valueCoded member
			// variable. for example:
			// new LogicCriteria("PROBLEM ADDED").contains("HIV INFECTED");
			
			if (rightOperand instanceof Float) {
				concept = Context.getConceptService().getConcept(((Float) rightOperand).intValue());
				criterion.add(Restrictions.eq("valueCoded", concept));
				
			}
			if (rightOperand instanceof Double) {
				concept = Context.getConceptService().getConcept(((Double) rightOperand).intValue());
				criterion.add(Restrictions.eq("valueCoded", concept));
			} else if (rightOperand instanceof Integer) {
				concept = Context.getConceptService().getConcept((Integer) rightOperand);
				criterion.add(Restrictions.eq("valueCoded", concept));
				
			} else if (rightOperand instanceof String) {
				concept = Context.getConceptService().getConceptByIdOrName((String) rightOperand);
				criterion.add(Restrictions.eq("valueCoded", concept));
				
			} else if (rightOperand instanceof Concept) {
				criterion.add(Restrictions.eq("valueCoded", rightOperand));
				
			} else
				log.error("Invalid operand value for CONTAINS operation");
		} else if (operator == Operator.EQUALS) {
			if (rootToken.equalsIgnoreCase(COMPONENT_ENCOUNTER_ID)) {
				EncounterService encounterService = Context.getEncounterService();
				Encounter encounter = encounterService.getEncounter((Integer) rightOperand);
				criterion.add(Restrictions.eq("encounter", encounter));
			} else if (rightOperand instanceof Float || rightOperand instanceof Integer || rightOperand instanceof Double)
				criterion.add(Restrictions.eq("valueNumeric", Double.parseDouble(rightOperand.toString())));
			else if (rightOperand instanceof String)
				criterion.add(Restrictions.eq("valueText", rightOperand));
			else if (rightOperand instanceof Date)
				if (leftOperand instanceof String && leftOperand.equals(COMPONENT_OBS_DATETIME)) {
					criterion.add(Restrictions.eq(COMPONENT_OBS_DATETIME, rightOperand));
				} else {
					criterion.add(Restrictions.eq("valueDatetime", rightOperand));
				}
			else if (rightOperand instanceof Concept)
				criterion.add(Restrictions.eq("valueCoded", rightOperand));
			else
				log.error("Invalid operand value for EQUALS operation");
			
		} else if (operator == Operator.LTE) {
			if (rightOperand instanceof Float || rightOperand instanceof Integer || rightOperand instanceof Double)
				criterion.add(Restrictions.le("valueNumeric", Double.parseDouble(rightOperand.toString())));
			else if (rightOperand instanceof Date)
				if (leftOperand instanceof String && leftOperand.equals(COMPONENT_OBS_DATETIME)) {
					criterion.add(Restrictions.le(COMPONENT_OBS_DATETIME, rightOperand));
				} else {
					criterion.add(Restrictions.le("valueDatetime", rightOperand));
				}
			else
				log.error("Invalid operand value for LESS THAN EQUAL operation");
			
		} else if (operator == Operator.GTE) {
			if (rightOperand instanceof Float || rightOperand instanceof Integer || rightOperand instanceof Double)
				criterion.add(Restrictions.ge("valueNumeric", Double.parseDouble(rightOperand.toString())));
			else if (rightOperand instanceof Date)
				if (leftOperand instanceof String && leftOperand.equals(COMPONENT_OBS_DATETIME)) {
					criterion.add(Restrictions.ge(COMPONENT_OBS_DATETIME, rightOperand));
				} else {
					criterion.add(Restrictions.ge("valueDatetime", rightOperand));
				}
			else
				log.error("Invalid operand value for GREATER THAN EQUAL operation");
			
		} else if (operator == Operator.LT) {
			if (rightOperand instanceof Float || rightOperand instanceof Integer || rightOperand instanceof Double)
				criterion.add(Restrictions.lt("valueNumeric", Double.parseDouble(rightOperand.toString())));
			else if (rightOperand instanceof Date)
				if (leftOperand instanceof String && leftOperand.equals(COMPONENT_OBS_DATETIME)) {
					criterion.add(Restrictions.lt(COMPONENT_OBS_DATETIME, rightOperand));
				} else {
					criterion.add(Restrictions.lt("valueDatetime", rightOperand));
				}
			else
				log.error("Invalid operand value for LESS THAN operation");
			
		} else if (operator == Operator.GT) {
			if (rightOperand instanceof Float || rightOperand instanceof Integer || rightOperand instanceof Double)
				criterion.add(Restrictions.gt("valueNumeric", Double.parseDouble(rightOperand.toString())));
			else if (rightOperand instanceof Date)
				if (leftOperand instanceof String && leftOperand.equals(COMPONENT_OBS_DATETIME)) {
					criterion.add(Restrictions.gt(COMPONENT_OBS_DATETIME, rightOperand));
				} else {
					criterion.add(Restrictions.gt("valueDatetime", rightOperand));
				}
			else
				log.error("Invalid operand value for GREATER THAN operation");
			
		} else if (operator == Operator.EXISTS) {
			// EXISTS can be handled on the higher level (above
			// LogicService, even) by coercing the Result into a Boolean for
			// each patient
		} else if (operator == Operator.ASOF && rightOperand instanceof Date) {
			indexDate = (Date) rightOperand;
			criterion.add(Restrictions.le("obsDatetime", indexDate));
			
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
				criterion.add(Restrictions.between("obsDatetime", within.getTime(), indexDate));
			} else {
				criterion.add(Restrictions.between("obsDatetime", indexDate, within.getTime()));
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
	private List<Obs> logicToHibernate(LogicExpression expression, Cohort who) throws LogicException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class);
		
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
			criteria.addOrder(Order.desc("obsDatetime")).addOrder(Order.desc("dateCreated")).addOrder(Order.desc("obsId"));
		} else if (transformOperator == Operator.FIRST) {
			criteria.addOrder(Order.asc("obsDatetime")).addOrder(Order.asc("dateCreated")).addOrder(Order.asc("obsId"));
		} else if (transformOperator == Operator.DISTINCT) {
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		}
		
		Criterion c = this.getCriterion(expression, indexDate);
		if (c != null) {
			criteria.add(c);
		}
		
		List<Obs> results = new ArrayList<Obs>();
		
		criteria.add(Restrictions.eq("voided", false));
		criteria.add(Restrictions.in("person.personId", who.getMemberIds()));
		results.addAll(criteria.list());
		
		//return a single result per patient for these operators
		//I don't see an easy way to do this in hibernate so I am
		//doing some postprocessing
		if (transformOperator == Operator.FIRST || transformOperator == Operator.LAST) {
			HashMap<Integer, ArrayList<Obs>> nResultMap = new HashMap<Integer, ArrayList<Obs>>();
			
			for (Obs currResult : results) {
				Integer currPersonId = currResult.getPersonId();
				ArrayList<Obs> prevResults = nResultMap.get(currPersonId);
				if (prevResults == null) {
					prevResults = new ArrayList<Obs>();
					nResultMap.put(currPersonId, prevResults);
				}
				
				if (prevResults.size() < numResults) {
					prevResults.add(currResult);
				}
			}
			
			if (nResultMap.values().size() > 0) {
				results.clear();
				
				for (ArrayList<Obs> currPatientObs : nResultMap.values()) {
					results.addAll(currPatientObs);
				}
			}
		}
		return results;
	}
	
	/**
	 * @throws LogicException
	 * @see org.openmrs.api.db.ObsDAO#getObservations(org.openmrs.Person,
	 *      org.openmrs.logic.LogicCriteria)
	 */
	@SuppressWarnings("unchecked")
	public List<Obs> getObservations(Cohort who, LogicCriteria logicCriteria) throws LogicException {
		log.debug("*** Reading observations ***");
		return logicToHibernate(logicCriteria.getExpression(), who);
	}
	
}
