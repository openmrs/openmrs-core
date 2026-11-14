/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.testmodule;

/**
 * Harmless stand-in used by the serializer type configuration tests. Its package is the package of
 * the {@code testmodule} fixture module, so a legitimate entry for that module names this class.
 */
public class ProbeTarget {

	private String name = "unset";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
