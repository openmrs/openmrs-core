/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.test;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openmrs.module.ModuleUtil;
import org.openmrs.util.OpenmrsConstants;

/**
 * Allows to execute tests only on the specific version of OpenMRS.
 */
public class OpenmrsProfileRule implements TestRule {
	
	private final String[] openmrsVersions;
	
	/**
	 * Allows to specify versions of OpenMRS on which tests should be executed.
	 * 
	 * @param openmrsVersion
	 * @param openmrsVersions
	 */
	public OpenmrsProfileRule(String openmrsVersion, String... openmrsVersions) {
		int length = openmrsVersions.length;
		this.openmrsVersions = Arrays.copyOf(openmrsVersions, length + 1);
		this.openmrsVersions[length] = openmrsVersion;
	}
	
	/**
	 * Allows to specify a version of OpenMRS on which tests should be executed.
	 * 
	 * @param openmrsVersion
	 * @param openmrsVersions
	 */
	public OpenmrsProfileRule(String openmrsVersion) {
		this.openmrsVersions = new String[] { openmrsVersion };
	}
	
	/**
	 * @see org.junit.rules.TestRule#apply(org.junit.runners.model.Statement,
	 *      org.junit.runner.Description)
	 */
	@Override
	public Statement apply(Statement base, Description description) {
		return new OpenmrsProfileStatement(base, description);
	}
	
	private class OpenmrsProfileStatement extends Statement {
		
		private final Statement base;
		
		private final Description description;
		
		public OpenmrsProfileStatement(Statement base, Description description) {
			this.base = base;
			this.description = description;
		}
		
		/**
		 * @see org.junit.runners.model.Statement#evaluate()
		 */
		@Override
		public void evaluate() throws Throwable {
			for (String openmrsVersion : openmrsVersions) {
				if (ModuleUtil.matchRequiredVersions(OpenmrsConstants.OPENMRS_VERSION_SHORT, openmrsVersion)) {
					base.evaluate();
					return;
				}
			}
			System.out.println("Ignored " + description.getMethodName() + " (run only on OpenMRS "
			        + StringUtils.join(openmrsVersions, ",") + ")");
		}
		
	}
	
}
