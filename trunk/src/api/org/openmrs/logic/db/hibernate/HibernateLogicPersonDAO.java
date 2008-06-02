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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Person;
import org.openmrs.logic.Duration;
import org.openmrs.logic.LogicCriteria;
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
	
	// Helper function, converts logic service's criteria into Hibernate's
	// criteria
	private Criteria logicToHibernate(LogicCriteria logicCriteria) {
		Criteria criteria = sessionFactory.getCurrentSession()
		                                  .createCriteria(Person.class);

		String attr = "";
		boolean notOperator = false;
		boolean asOf = false;
		Date indexDate = Calendar.getInstance().getTime();

		while (logicCriteria != null) {
			if (logicCriteria.getOperator() == null
			        && logicCriteria.getRightOperand() instanceof String) {
				// there's a string referencing a person's attribute inside the
				// right operand
				String token = (String) logicCriteria.getRightOperand();

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
					break;
				}
			} else if (logicCriteria.getOperator() == Operator.AFTER
			        || logicCriteria.getOperator() == Operator.GT) {
				Criterion c = Restrictions.gt(attr,
				                              logicCriteria.getRightOperand());
				if (notOperator)
					c = Restrictions.not(c);
				criteria.add(c);

			} else if (logicCriteria.getOperator() == Operator.GTE) {
				Criterion c = Restrictions.ge(attr,
				                              logicCriteria.getRightOperand());
				if (notOperator)
					c = Restrictions.not(c);
				criteria.add(c);

			}else if (logicCriteria.getOperator() == Operator.AND) {
				// TODO to be added once everything else is tested

			} else if (logicCriteria.getOperator() == Operator.BEFORE
			        || logicCriteria.getOperator() == Operator.LT) {
				Criterion c = Restrictions.lt(attr,
				                              logicCriteria.getRightOperand());
				if (notOperator)
					c = Restrictions.not(c);
				criteria.add(c);

			} else if (logicCriteria.getOperator() == Operator.LTE) {
				Criterion c = Restrictions.le(attr,
				                              logicCriteria.getRightOperand());
				if (notOperator)
					c = Restrictions.not(c);
				criteria.add(c);

			}else if (logicCriteria.getOperator() == Operator.CONTAINS
			        || logicCriteria.getOperator() == Operator.EQUALS) {
				Criterion c = Restrictions.eq(attr,
				                              logicCriteria.getRightOperand());
				if (notOperator)
					c = Restrictions.not(c);
				criteria.add(c);

			} else if (logicCriteria.getOperator() == Operator.EXISTS) {
				// this is handled on the highest level, by coercing the Result
				// into a boolean
			} else if (logicCriteria.getOperator() == Operator.FIRST) {
				criteria.addOrder(Order.asc(attr)).setMaxResults(1);

			} else if (logicCriteria.getOperator() == Operator.LAST) {
				criteria.addOrder(Order.desc(attr)).setMaxResults(1);

			} else if (logicCriteria.getOperator() == Operator.NOT) {
				notOperator = !notOperator;

			} else if (logicCriteria.getOperator() == Operator.OR) {
				// TODO to be added once everything else is tested

			} else if (logicCriteria.getOperator() == Operator.ASOF
			        && logicCriteria.getRightOperand() instanceof Date) {
				indexDate = (Date) logicCriteria.getRightOperand();
				asOf = true;

			} else if (logicCriteria.getOperator() == Operator.WITHIN
			        && logicCriteria.getRightOperand() instanceof Duration) {
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
					within.roll(Calendar.WEEK_OF_YEAR, -duration.getDuration()
					                                            .intValue());
				} else if (duration.getUnits() == Duration.Units.DAYS) {
					within.roll(Calendar.DAY_OF_YEAR, -duration.getDuration()
					                                           .intValue());
				} else if (duration.getUnits() == Duration.Units.MINUTES) {
					within.roll(Calendar.MINUTE, -duration.getDuration()
					                                      .intValue());
				} else if (duration.getUnits() == Duration.Units.SECONDS) {
					within.roll(Calendar.SECOND, -duration.getDuration()
					                                      .intValue());
				}

				if (attr.equals("deathDate"))
					// TODO add support for dateCreated, dateChanged and
					// dateVoided
					criteria.add(Restrictions.ge(attr, within.getTime()));
				else
					// defaults to "birthdate"
					criteria.add(Restrictions.ge("birthdate", within.getTime()));

			}

			if (logicCriteria.getLeftOperand() instanceof LogicCriteria)
				logicCriteria = (LogicCriteria) logicCriteria.getLeftOperand();
			else
				logicCriteria = null; // exit the loop

		}

		// we need this outside of the loop, because the actual parameter to
		// which the AS OF applies to (birthdate, deathdate, etc.) is known
		// after AS OF is encountered in the tree (AS OF is always at the top)
		// ( see LogicDataSource#eval() )
		if (asOf) {
			if (attr.equals("deathdate"))
				// TODO add support for dateCreated, dateChanged and
				// dateVoided
				criteria.add(Restrictions.le(attr, indexDate));
			else
				// defaults to "birthdate"
				criteria.add(Restrictions.le("birthdate", indexDate));
		}

		return criteria;
	}

	/**
	 * @see org.openmrs.api.db.PersonDAO#getPersons(java.util.List,
	 *      org.openmrs.logic.LogicCriteria)
	 */
	@SuppressWarnings("unchecked")
	public List<Person> getPersons(Collection<Integer> personIds,
	        LogicCriteria logicCriteria) {
		Criteria criteria = logicToHibernate(logicCriteria);
		List<Person> results = new ArrayList<Person>();

		criteria.add(Restrictions.in("personId", personIds));
		results.addAll(criteria.list());

		return results;

	}


}
