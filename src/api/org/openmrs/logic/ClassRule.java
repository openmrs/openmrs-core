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
package org.openmrs.logic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;

public class ClassRule extends Rule {

	protected final Log log = LogFactory.getLog(getClass());

	private Class clazz;
	private Method evalMethod = null;
	
	public ClassRule(Class clazz) {
		this.clazz = clazz;
	}

	private Method getEvalMethod() {
		if (evalMethod == null) {
			try {
				evalMethod = clazz.getMethod("eval", new Class[] {
						LogicDataSource.class, Patient.class, Object[].class });
			} catch (NoSuchMethodException e) {
				log.error("Could not find eval method on rule", e);
			}
		}
		return evalMethod;
	}

	@Override
	public Result eval(LogicDataSource dataSource, Patient patient,
			Object[] args) {
		Result result = Result.NULL_RESULT;
		try {
			result = (Result) getEvalMethod().invoke(clazz.newInstance(),
					new Object[] { dataSource, patient, args });
		} catch (InstantiationException e) {
			log.error("Unable to evaluate rule " + clazz.getName(), e);
		} catch (IllegalArgumentException e) {
			log.error("Unable to evaluate rule " + clazz.getName(), e);
		} catch (IllegalAccessException e) {
			log.error("Unable to evaluate rule " + clazz.getName(), e);
		} catch (InvocationTargetException e) {
			log.error("Unable to evaluate rule " + clazz.getName(), e);
		}
		return result;
	}

	@Override
	public Result eval(LogicDataSource dataSource, Patient patient) {
		return eval(dataSource, patient, null);
	}

	@Override
	public Class[] getArgumentProfile() {
		return super.getArgumentProfile();
	}

	@Override
	public Rule[] getDependencies() {
		return super.getDependencies();
	}

}
