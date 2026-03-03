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

import liquibase.changelog.ChangeSet;
import org.openmrs.util.DatabaseUpdater;

/**
 * Interface used for callbacks when updating the database. Implement this interface and pass it to
 * {@link DatabaseUpdater#executeChangelog(String, ChangeSetExecutorCallback)}
 */
public interface ChangeSetExecutorCallback {
	
	/**
	 * This method is called after each changeset is executed.
	 *
	 * @param changeSet          the liquibase changeset that was just run
	 * @param numChangeSetsToRun the total number of changesets in the current file
	 */
	void executing(ChangeSet changeSet, int numChangeSetsToRun);
}
