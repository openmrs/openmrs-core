/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler.jobrunr;

import org.jobrunr.jobs.context.JobDashboardProgressBar;
import org.openmrs.scheduler.TaskProgress;

/**
 * @since 2.9.x
 */
public class JobRunrTaskProgress implements TaskProgress {

	private final JobDashboardProgressBar progressBar;

	public JobRunrTaskProgress(JobDashboardProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	@Override
	public int getTotalProgress() {
		return (int) progressBar.getTotalAmount();
	}

	@Override
	public void increaseByOne() {
		progressBar.incrementSucceeded();
	}

	@Override
	public int getProgress() {
		return (int) progressBar.getSucceededAmount();
	}

	@Override
	public void setValue(int currentProgress) {
		progressBar.setProgress(currentProgress);
	}
}
