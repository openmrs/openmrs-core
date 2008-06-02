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
import org.openmrs.Encounter;
import org.openmrs.logic.Duration;
import org.openmrs.logic.LogicCriteria;
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

	// Helper function, converts logic service's criteria into Hibernate's
	// criteria
	private Criteria logicToHibernate(LogicCriteria logicCriteria) {

		// base criteria is to pull all encounters for given patient list
		Criteria criteria = sessionFactory.getCurrentSession()
		                                  .createCriteria(Encounter.class);

		boolean notOperator = false;
		Date indexDate = Calendar.getInstance().getTime();
		Criterion restrictionCriteria = null; // limit encounters pulled by
		// criteria

		while (logicCriteria != null) {
			if (logicCriteria.getOperator() == Operator.BEFORE||
					logicCriteria.getOperator() == Operator.LT) {
				if (logicCriteria.getRightOperand() instanceof Date)
					restrictionCriteria = Restrictions.lt("encounterDatetime",
					                                      logicCriteria.getRightOperand());
				else
					log.error("Invalid operand value for "
					        + logicCriteria.getRightOperand().toString()
					        + " operation");

			} else if (logicCriteria.getOperator() == Operator.LTE) {
				if (logicCriteria.getRightOperand() instanceof Date)
					restrictionCriteria = Restrictions.le("encounterDatetime",
					                                      logicCriteria.getRightOperand());
				else
					log.error("Invalid operand value for "
					        + logicCriteria.getRightOperand().toString()
					        + " operation");

			}else if (logicCriteria.getOperator() == Operator.AFTER||
					logicCriteria.getOperator() == Operator.GT) {
				if (logicCriteria.getRightOperand() instanceof Date)
					restrictionCriteria = Restrictions.gt("encounterDatetime",
					                                      logicCriteria.getRightOperand());
				else
					log.error("Invalid operand value for "
					        + logicCriteria.getRightOperand().toString()
					        + " operation");

			} else if (logicCriteria.getOperator() == Operator.GTE) {
				if (logicCriteria.getRightOperand() instanceof Date)
					restrictionCriteria = Restrictions.ge("encounterDatetime",
					                                      logicCriteria.getRightOperand());
				else
					log.error("Invalid operand value for "
					        + logicCriteria.getRightOperand().toString()
					        + " operation");

			}else if (logicCriteria.getOperator() == Operator.AND) {
				// AND should be handled on the Rule level
			} else if (logicCriteria.getOperator() == Operator.OR) {
				// OR should be handled on the Rule level
			} else if (logicCriteria.getOperator() == Operator.CONTAINS) {
				// value type text not used by encounter
			} else if (logicCriteria.getOperator() == Operator.NOT) {
				notOperator = !notOperator;

			} else if (logicCriteria.getOperator() == Operator.EQUALS) {
				if (logicCriteria.getRightOperand() instanceof Date)
					restrictionCriteria = Restrictions.eq("encounterDatetime",
					                                      logicCriteria.getRightOperand());

				else
					log.error("Invalid operand value for "
					        + logicCriteria.getRightOperand().toString()
					        + " operation");

			} else if (logicCriteria.getOperator() == Operator.LAST) {
				criteria.addOrder(Order.desc("encounterDatetime"));

			} else if (logicCriteria.getOperator() == Operator.FIRST) {
				criteria.addOrder(Order.asc("encounterDatetime"));

			} else if (logicCriteria.getOperator() == Operator.EXISTS) {
				// EXISTS can be handled on the higher level (above
				// LogicService, even) by coercing the Result into a Boolean for
				// each patient
			} else if (logicCriteria.getOperator() == Operator.ASOF) {
				if (logicCriteria.getRightOperand() instanceof Date) {
					indexDate = (Date) logicCriteria.getRightOperand();
					criteria.add(Restrictions.le("encounterDatetime", indexDate));
				} else
					log.error("Invalid operand value for "
					        + logicCriteria.getRightOperand().toString()
					        + " operation");
			} else if (logicCriteria.getOperator() == Operator.WITHIN) {
				if (logicCriteria.getRightOperand() instanceof Duration) {
					Duration duration = (Duration) logicCriteria.getRightOperand();
					Calendar within = Calendar.getInstance();
					within.setTime(indexDate);

					if (duration.getUnits() == Duration.Units.YEARS) {
						within.roll(Calendar.YEAR, -duration.getDuration()
						                                    .intValue());
					} else if (duration.getUnits() == Duration.Units.MONTHS) {
						within.roll(Calendar.MONTH, -duration.getDuration()
						                                     .intValue());
					} else if (duration.getUnits() == Duration.Units.WEEKS) {
						within.roll(Calendar.WEEK_OF_YEAR,
						            -duration.getDuration().intValue());
					} else if (duration.getUnits() == Duration.Units.DAYS) {
						within.roll(Calendar.DAY_OF_YEAR,
						            -duration.getDuration().intValue());
					} else if (duration.getUnits() == Duration.Units.MINUTES) {
						within.roll(Calendar.MINUTE, -duration.getDuration()
						                                      .intValue());
					} else if (duration.getUnits() == Duration.Units.SECONDS) {
						within.roll(Calendar.SECOND, -duration.getDuration()
						                                      .intValue());
					}

					criteria.add(Restrictions.ge("encounterDatetime",
					                             within.getTime()));
				} else
					log.error("Invalid operand value for "
					        + logicCriteria.getRightOperand().toString()
					        + " operation");
			}
			if (notOperator)
			{
				restrictionCriteria = Restrictions.not(restrictionCriteria);	
			}

			if(restrictionCriteria != null)
			{
				criteria.add(restrictionCriteria);
			}
			logicCriteria = (LogicCriteria) logicCriteria.getLeftOperand();
		}

		return criteria;
	}

	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncounters(org.openmrs.Person,
	 *      org.openmrs.logic.LogicCriteria)
	 */
	@SuppressWarnings("unchecked")
	public List<Encounter> getEncounters(Cohort who,
	        LogicCriteria logicCriteria) {
		Criteria criteria = logicToHibernate(logicCriteria);
	
		List<Encounter> results = new ArrayList<Encounter>();

		criteria.add(Restrictions.eq("voided", false));
		criteria.add(Restrictions.in("patient.patientId", who.getMemberIds()));
		results.addAll(criteria.list());

		return results;
	}

}
