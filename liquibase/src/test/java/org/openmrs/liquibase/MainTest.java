/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.liquibase;

import java.io.IOException;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

public class MainTest {
	
	@Test
	public void shouldCreateUpdatedSnapshotFiles() throws DocumentException, IOException {
		CoreDataTuner coreDataTuner = Mockito.mock(CoreDataTuner.class);
		SchemaOnlyTuner schemaOnlyTuner = Mockito.mock(SchemaOnlyTuner.class);
		
		Main.setCoreDataTuner(coreDataTuner);
		Main.setSchemaOnlyTuner(schemaOnlyTuner);
		
		Main.main(new String[0]);
		
		Mockito.verify(coreDataTuner, times(1)).addLicenseHeaderToFileIfNeeded(any());
		Mockito.verify(coreDataTuner, times(1)).createUpdatedChangeLogFile(any(), any());
		
		Mockito.verify(schemaOnlyTuner, times(1)).addLicenseHeaderToFileIfNeeded(any());
		Mockito.verify(schemaOnlyTuner, times(1)).createUpdatedChangeLogFile(any(), any());
	}
}
