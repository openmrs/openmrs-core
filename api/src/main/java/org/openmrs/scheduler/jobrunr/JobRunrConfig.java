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

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.jobrunr.configuration.JobRunr;
import org.jobrunr.configuration.JobRunrConfiguration;
import org.jobrunr.dashboard.JobRunrDashboardWebServerConfiguration;
import org.jobrunr.jobs.filters.RetryFilter;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.storage.StorageProvider;
import org.jobrunr.storage.sql.common.SqlStorageProviderFactory;
import org.jobrunr.utils.mapper.jackson.JacksonJsonMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @since 2.9.x
 */
@Configuration
public class JobRunrConfig {

	@Bean
	public StorageProvider storageProvider(DataSource dataSource) {
		return SqlStorageProviderFactory.using(dataSource);
	}

	@Bean
	public JobRunrConfiguration.JobRunrConfigurationResult jobRunrConfiguration(
	        @Qualifier("schedulerObjectMapper") ObjectMapper objectMapper, StorageProvider storageProvider,
	        ApplicationContext applicationContext, @Value("${jobrunr.dashboard.username:admin}") String user,
	        @Value("${jobrunr.dashboard.password:}") String password, @Value("${jobrunr.dashboard.port:9000}") int port,
	        @Value("${jobrunr.dashboard.enabled:false}") boolean dashboardEnabled) {
		if (dashboardEnabled && StringUtils.isBlank(password)) {
			throw new IllegalArgumentException("jobrunr.dashboard.password must not be blank");
		}

		JobRunrConfiguration config = JobRunr.configure().useJsonMapper(new JacksonJsonMapper(objectMapper))
		        .useStorageProvider(storageProvider).useJobActivator(applicationContext::getBean)
		        .withJobFilter(new RetryFilter(3)).useBackgroundJobServer();
		if (dashboardEnabled) {
			config.useDashboard(JobRunrDashboardWebServerConfiguration.usingStandardDashboardConfiguration().andPort(port)
			        .andBasicAuthentication(user, password));
		}
		return config.initialize();
	}

	@Bean
	public JobRequestScheduler jobRequestScheduler(JobRunrConfiguration.JobRunrConfigurationResult jobRunrConfiguration) {
		return jobRunrConfiguration.getJobRequestScheduler();
	}

	@Bean
	public JobScheduler jobScheduler(JobRunrConfiguration.JobRunrConfigurationResult jobRunrConfiguration) {
		return jobRunrConfiguration.getJobScheduler();
	}
}
