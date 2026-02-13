/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler;

import java.util.Properties;
import javax.sql.DataSource;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.openmrs.api.context.Context;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Enables support for {@link Scheduled} and {@link SchedulerLock} annotations. 
 * <p>
 * Please use {@link ScheduledWithLock} instead of {@link Scheduled} and {@link SchedulerLock}
 * in case we ever need to change the implementation.
 * <p>
 * Please use {@link SchedulerService} for persistent background tasks instead.
 * <p>
 * See {@link ScheduledWithLock} for more details.
 * 
 * @since 2.9.x
 */
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
public class SchedulerConfig {

	@Bean
	public LockProvider lockProvider(DataSource dataSource) {
		return new JdbcTemplateLockProvider(
				JdbcTemplateLockProvider.Configuration.builder()
						.withJdbcTemplate(new JdbcTemplate(dataSource))
						.build());
	}

	@Bean
	public DataSource dataSource() {
		Properties properties = Context.getRuntimeProperties();
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(properties.getProperty("connection.driver_class",
				properties.getProperty("hibernate.connection.driver_class")));
		dataSource.setUrl(properties.getProperty("connection.url",
				properties.getProperty("hibernate.connection.url")));
		dataSource.setUsername(properties.getProperty("connection.username",
				properties.getProperty("hibernate.connection.username")));
		dataSource.setPassword(properties.getProperty("connection.password",
				properties.getProperty("hibernate.connection.password")));
		return dataSource;
	}
}
