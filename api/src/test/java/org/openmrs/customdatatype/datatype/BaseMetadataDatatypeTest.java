/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.customdatatype.datatype;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.customdatatype.CustomDatatype;

public class BaseMetadataDatatypeTest {
	
	/**
	 * @verifies use the name in summary instance
	 * @see BaseMetadataDatatype#doGetTextSummary(org.openmrs.OpenmrsMetadata)
	 */
	@Test
	public void doGetTextSummary_shouldUseTheNameInSummaryInstance() throws Exception {
		OpenmrsMetadata location = new Location();
		String expectedSummary = "some summary";
		location.setName(expectedSummary);
		BaseMetadataDatatype datatype = new MockLocationDatatype();
		CustomDatatype.Summary summary = datatype.doGetTextSummary(location);
		Assert.assertEquals(expectedSummary, summary.getSummary());
		Assert.assertEquals(true, summary.isComplete());
	}
}
