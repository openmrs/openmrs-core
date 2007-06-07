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
