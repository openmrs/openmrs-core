/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.arden;

import java.util.LinkedList;
import java.util.ListIterator;

public class MLMEvaluateElement extends LinkedList {
	
	public void printThisList() {
		System.out.println("\n This list evaluate order is  - ");
		ListIterator<String> iter = this.listIterator(0);
		while (iter.hasNext()) {
			System.out.println(iter.next());
		}
		System.out.println("----------------------");
	}
}
