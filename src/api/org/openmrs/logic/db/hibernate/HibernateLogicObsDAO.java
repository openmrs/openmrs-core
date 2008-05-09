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
import org.openmrs.logic.db.LogicObsDAO;
import org.openmrs.logic.op.Operator;

/**
 *
 */
public class HibernateLogicObsDAO implements LogicObsDAO {

	private static final String COMPONENT_ENCOUNTER_ID = "encounterId";

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
	
	private Criterion getCriterion(LogicCriteria logicCriteria, Date indexDate) {
		boolean notOperator = false;
		Criterion c = null;
		Operator operator = logicCriteria.getOperator();
		Object rightOperand = logicCriteria.getRightOperand();
		Object leftOperand = logicCriteria.getLeftOperand();

		if (operator == null) {
			// there's a concept string inside the right operand
			String conceptName = logicCriteria.getRootToken();
			if (conceptName == null) {
				conceptName = (String) rightOperand;
			}
			Concept concept = Context.getConceptService()
			                         .getConceptByIdOrName(conceptName);
			c = Restrictions.eq("concept", concept);

			if (notOperator)
				c = Restrictions.not(c);

		} else if (operator == Operator.BEFORE) {
			c = Restrictions.lt("obsDatetime", rightOperand);

			if (notOperator)
				c = Restrictions.not(c);

		} else if (operator == Operator.AFTER) {
			c = Restrictions.gt("obsDatetime", rightOperand);

			if (notOperator)
				c = Restrictions.not(c);

		} else if (operator == Operator.AND) {

			Criterion leftCriteria = null;
			Criterion rightCriteria = null;

			if (leftOperand instanceof LogicCriteria) {
				leftCriteria = this.getCriterion((LogicCriteria) leftOperand,
				                                 indexDate);
			}
			if (rightOperand instanceof LogicCriteria) {
				rightCriteria = this.getCriterion((LogicCriteria) rightOperand,
				                                  indexDate);
			}

			if (leftCriteria != null && rightCriteria != null) {
				c = Restrictions.and(leftCriteria, rightCriteria);
			}
		} else if (operator == Operator.OR) {
			Criterion leftCriteria = null;
			Criterion rightCriteria = null;

			if (leftOperand instanceof LogicCriteria) {
				leftCriteria = this.getCriterion((LogicCriteria) leftOperand,
				                                 indexDate);
			}
			if (rightOperand instanceof LogicCriteria) {
				rightCriteria = this.getCriterion((LogicCriteria) rightOperand,
				                                  indexDate);
			}

			if (leftCriteria != null && rightCriteria != null) {
				c = Restrictions.or(leftCriteria, rightCriteria);
			}
		} else if (operator == Operator.NOT) {
			notOperator = !notOperator;

		} else if (operator == Operator.CONTAINS) {
			// used with PROBLEM ADDED concept, to retrieve the "ANSWERED
			// BY" concept, stashed inside the concept's valueCoded member
			// variable. for example:
			// new LogicCriteria("PROBLEM ADDED").contains("HIV INFECTED");

			if (rightOperand instanceof Float) {
				Concept concept = Context.getConceptService()
				                         .getConcept(((Float) rightOperand).intValue());
				c = Restrictions.eq("valueCoded", concept);

			} else if (rightOperand instanceof Integer) {
				Concept concept = Context.getConceptService()
				                         .getConcept((Integer) rightOperand);
				c = Restrictions.eq("valueCoded", concept);

			} else if (rightOperand instanceof String) {
				Concept concept = Context.getConceptService()
				                         .getConceptByIdOrName((String) rightOperand);
				c = Restrictions.eq("valueCoded", concept);

			} else if (rightOperand instanceof Concept) {
				c = Restrictions.eq("valueCoded", rightOperand);

			} else
				log.error("Invalid operand value for CONTAINS operation");

			if (notOperator)
				c = Restrictions.not(c);

		} else if (operator == Operator.EQUALS) {
			if (leftOperand instanceof String
			        && ((String) leftOperand).equalsIgnoreCase(COMPONENT_ENCOUNTER_ID)) {
				EncounterService encounterService = Context.getEncounterService();
				Encounter encounter = encounterService.getEncounter((Integer) rightOperand);
				c = Restrictions.eq("encounter", encounter);
			} else if (rightOperand instanceof Float
			        || rightOperand instanceof Integer)
				c = Restrictions.eq("valueNumeric",
				                    Double.parseDouble(rightOperand
				                                                    .toString()));
			else if (rightOperand instanceof String)
				c = Restrictions.eq("valueText",
				                    rightOperand);
			else if (rightOperand instanceof Date)
				c = Restrictions.eq("valueDatetime",
				                    rightOperand);
			else if (rightOperand instanceof Concept)
				c = Restrictions.eq("valueCoded", rightOperand);
			else
				log.error("Invalid operand value for EQUALS operation");

			if (notOperator)
				c = Restrictions.not(c);

		} else if (operator == Operator.LTE) {
			if (rightOperand instanceof Float
			        || rightOperand instanceof Integer)
				c = Restrictions.le("valueNumeric",
				                    Double.parseDouble(rightOperand
				                                                    .toString()));
			else if (rightOperand instanceof String)
				c = Restrictions.le("valueText",
				                    rightOperand);
			else if (rightOperand instanceof Date)
				c = Restrictions.le("valueDatetime",
				                    rightOperand);
			else
				log.error("Invalid operand value for LESS THAN EQUAL operation");

			if (notOperator)
				c = Restrictions.not(c);

		} else if (operator == Operator.GTE) {
			if (rightOperand instanceof Float
			        || rightOperand instanceof Integer)
				c = Restrictions.ge("valueNumeric",
				                    Double.parseDouble(rightOperand
				                                                    .toString()));
			else if (rightOperand instanceof String)
				c = Restrictions.ge("valueText",
				                    rightOperand);
			else if (rightOperand instanceof Date)
				c = Restrictions.ge("valueDatetime",
				                    rightOperand);
			else
				log.error("Invalid operand value for GREATER THAN EQUAL operation");

			if (notOperator)
				c = Restrictions.not(c);

		} else if (operator == Operator.LT) {
			if (rightOperand instanceof Float
			        || rightOperand instanceof Integer)
				c = Restrictions.lt("valueNumeric",
				                    Double.parseDouble(rightOperand
				                                                    .toString()));
			else if (rightOperand instanceof String)
				c = Restrictions.lt("valueText",
				                    rightOperand);
			else if (rightOperand instanceof Date)
				c = Restrictions.lt("valueDatetime",
				                    rightOperand);
			else
				log.error("Invalid operand value for LESS THAN operation");

			if (notOperator)
				c = Restrictions.not(c);

		} else if (operator == Operator.GT) {
			if (rightOperand instanceof Float
			        || rightOperand instanceof Integer)
				c = Restrictions.gt("valueNumeric",
				                    Double.parseDouble(rightOperand
				                                                    .toString()));
			else if (rightOperand instanceof String)
				c = Restrictions.gt("valueText",
				                    rightOperand);
			else if (rightOperand instanceof Date)
				c = Restrictions.gt("valueDatetime",
				                    rightOperand);
			else
				log.error("Invalid operand value for GREATER THAN operation");

			if (notOperator)
				c = Restrictions.not(c);

		} else if (operator == Operator.EXISTS) {
			// EXISTS can be handled on the higher level (above
			// LogicService, even) by coercing the Result into a Boolean for
			// each patient
		} else if (operator == Operator.ASOF
		        && rightOperand instanceof Date) {
			indexDate = (Date) rightOperand;
			c = Restrictions.le("obsDatetime", indexDate);

		} else if (operator == Operator.WITHIN
		        && rightOperand instanceof Duration) {
			Duration duration = (Duration) rightOperand;
			Calendar within = Calendar.getInstance();
			within.setTime(indexDate);

			if (duration.getUnits() == Duration.Units.YEARS) {
				within.roll(Calendar.YEAR, -duration.getDuration().intValue());
			} else if (duration.getUnits() == Duration.Units.MONTHS) {
				within.roll(Calendar.MONTH, -duration.getDuration().intValue());
			} else if (duration.getUnits() == Duration.Units.WEEKS) {
				within.roll(Calendar.WEEK_OF_YEAR, -duration.getDuration()
				                                            .intValue());
			} else if (duration.getUnits() == Duration.Units.DAYS) {
				within.roll(Calendar.DAY_OF_YEAR, -duration.getDuration()
				                                           .intValue());
			} else if (duration.getUnits() == Duration.Units.MINUTES) {
				within.roll(Calendar.MINUTE, -duration.getDuration().intValue());
			} else if (duration.getUnits() == Duration.Units.SECONDS) {
				within.roll(Calendar.SECOND, -duration.getDuration().intValue());
			}

			c = Restrictions.ge("obsDatetime", within.getTime());

		}
		return c;
	}
	
    // Helper function, converts logic service's criteria into Hibernate's
	// criteria
	private Criteria logicToHibernate(LogicCriteria logicCriteria) {
		Criteria criteria = sessionFactory.getCurrentSession()
		                                  .createCriteria(Obs.class);

		Date indexDate = Calendar.getInstance().getTime();
		Operator operator = logicCriteria.getOperator();

		// set the transform and evaluate the right criteria
		// if there is any
		if (operator == Operator.LAST) {
			criteria.addOrder(Order.desc("obsDatetime")).setMaxResults(1);
			if(logicCriteria.getRightOperand() instanceof LogicCriteria)
			{
				logicCriteria = (LogicCriteria) logicCriteria.getRightOperand();
			}

		} else if (operator == Operator.FIRST) {
			criteria.addOrder(Order.asc("obsDatetime")).setMaxResults(1);
			if(logicCriteria.getRightOperand() instanceof LogicCriteria)
			{
				logicCriteria = (LogicCriteria) logicCriteria.getRightOperand();
			}
		} 
		
		Criterion c = this.getCriterion(logicCriteria, indexDate);
		if (c != null) {
			criteria.add(c);
		}

		return criteria;
	}

    /**
	 * @see org.openmrs.api.db.ObsDAO#getObservations(org.openmrs.Person,
	 *      org.openmrs.logic.LogicCriteria)
	 */
    @SuppressWarnings("unchecked")
    public List<Obs> getObservations(Cohort who, LogicCriteria logicCriteria) {
    	log.debug("*** Reading observations ***");
        Criteria criteria = logicToHibernate(logicCriteria);
        List<Obs> results = new ArrayList<Obs>();

        criteria.add(Restrictions.eq("voided", false));
        criteria.add(Restrictions.in("person.personId", who.getMemberIds()));
        results.addAll(criteria.list());

        return results;
    }
    
    

}
