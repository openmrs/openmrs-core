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
package org.openmrs.logic.datasource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Cohort;
import org.openmrs.Person;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.db.LogicPersonDAO;
import org.openmrs.logic.result.Result;
import org.openmrs.logic.util.Util;

/**
 * Provides access to person demographic data.
 * 
 * Valid keys are:
 * <ul>
 * <li><strong>gender</strong> &mdash; text result of "M" or "F"</li>
 * <li><strong>birthdate</strong> &mdash; date result with patient's birthdate</li>
 * <li><strong>birthdate_estimated</strong> &mdash; boolean result (true if patient's 
 * birthdate is estimated)</li>
 * <li><strong>death</strong> &mdash; a coded result containing the cause of death with
 * the result date equal to the death date.  If the patient is not dead, then the result
 * is null.</li>
 * </ul>
 */
public class PersonDataSource implements LogicDataSource {

    private static final Collection<String> keys = new ArrayList<String>();
    
    private LogicPersonDAO logicPersonDAO;
 
	/**
     * @return the logicPersonDAO
     */
    public LogicPersonDAO getLogicPersonDAO() {
    	return logicPersonDAO;
    }

	/**
     * @param logicPersonDAO the logicPersonDAO to set
     */
    public void setLogicPersonDAO(LogicPersonDAO logicPersonDAO) {
    	this.logicPersonDAO = logicPersonDAO;
    }
    
    
    static {
        String[] keyList = new String[] { "gender", "birthdate",
                "birthdate estimated", "dead", "death date", "cause of death" };
        for (String k : keyList)
            keys.add(k);
    }

    /**
     * @see org.openmrs.logic.datasource.LogicDataSource#doEval(org.openmrs.Cohort,
     *      org.openmrs.logic.LogicCriteria)
     */
    public Map<Integer, Result> read(LogicContext context, Cohort who,
            LogicCriteria criteria) {

        Map<Integer, Result> resultMap = new HashMap<Integer, Result>();

        // calculate
        List<Person> personList = getLogicPersonDAO().getPersons(
                who.getMemberIds(), criteria);

        // put in the result map
        for (Person person : personList) {
            String token = criteria.getRootToken();
            if (token.equalsIgnoreCase("GENDER"))
                resultMap.put(person.getPersonId(), new Result(person
                        .getGender()));
            else if (token.equalsIgnoreCase("BIRTHDATE"))
                resultMap.put(person.getPersonId(), new Result(person
                        .getBirthdate()));
            else if (token.equalsIgnoreCase("BIRTHDATE ESTIMATED"))
                resultMap.put(person.getPersonId(), new Result(person
                        .getBirthdateEstimated()));
            else if (token.equalsIgnoreCase("death date"))
            	resultMap.put(person.getPersonId(), new Result(person.getDeathDate()));
            else if (token.equalsIgnoreCase("cause of death")) {
            	Result deathResult;
            	if (person.isDead())
            		deathResult = new Result(person.getDeathDate(), person.getCauseOfDeath(),person);
            	else
            		deathResult = Result.emptyResult();
                resultMap.put(person.getPersonId(), deathResult);
            }else if(token.equalsIgnoreCase("dead")){
            	resultMap.put(person.getPersonId(), new Result(person.isDead()));
            }
            // TODO more keys to be added
        }

        Util.applyAggregators(resultMap, criteria,who);
        return resultMap;
    }

    /**
     * @see org.openmrs.logic.datasource.LogicDataSource#getTTL()
     */
    public int getDefaultTTL() {
        return 60 * 60 * 4; // 4 hours
    }

    /**
     * @see org.openmrs.logic.datasource.LogicDataSource#getKeys()
     */
    public Collection<String> getKeys() {
        return keys;
    }

    /**
     * @see org.openmrs.logic.datasource.LogicDataSource#hasKey(java.lang.String)
     */
    public boolean hasKey(String key) {
        return getKeys().contains(key);
    }

}
