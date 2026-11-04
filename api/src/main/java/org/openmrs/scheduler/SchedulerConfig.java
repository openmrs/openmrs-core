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

import java.beans.PropertyVetoException;
import java.util.Properties;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
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
 * @since 2.9.0
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

	/**
	 * Connection pool shared by JobRunr's storage provider and ShedLock. It is separate from Hibernate's pool so
	 * that scheduler polling and heartbeats can never be starved by, or starve, application work.
	 * <p>
	 * Previously an unpooled {@code DriverManagerDataSource}: every JobRunr poll, heartbeat and state change
	 * opened and tore down a physical MySQL connection, per replica, every few seconds.
	 * <p>
	 * Tunable with runtime properties {@code scheduler.datasource.max_size} (default 10) and
	 * {@code scheduler.datasource.min_size} (default 2).
	 */
	@Bean(destroyMethod = "close")
	public DataSource dataSource() throws PropertyVetoException {
		Properties properties = Context.getRuntimeProperties();
		// Driver_class property may be blank
		String driverClass = properties.getProperty("connection.driver_class");
		if (StringUtils.isBlank(driverClass)) {
			driverClass = properties.getProperty("hibernate.connection.driver_class");
			if (StringUtils.isBlank(driverClass)) {
				driverClass = "com.mysql.jdbc.Driver";
			}
		}
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		dataSource.setDataSourceName("openmrs-scheduler");
		dataSource.setDriverClass(driverClass);
		dataSource.setJdbcUrl(properties.getProperty("connection.url",
				properties.getProperty("hibernate.connection.url")));
		dataSource.setUser(properties.getProperty("connection.username",
				properties.getProperty("hibernate.connection.username")));
		dataSource.setPassword(properties.getProperty("connection.password",
				properties.getProperty("hibernate.connection.password")));
		dataSource.setMinPoolSize(Integer.parseInt(properties.getProperty("scheduler.datasource.min_size", "2")));
		dataSource.setMaxPoolSize(Integer.parseInt(properties.getProperty("scheduler.datasource.max_size", "10")));
		dataSource.setAcquireRetryAttempts(5);
		dataSource.setMaxIdleTime(300);
		dataSource.setIdleConnectionTestPeriod(120);
		dataSource.setTestConnectionOnCheckin(true);
		// JobRunr and ShedLock manage their own commits; never let pending work survive a check-in
		dataSource.setAutoCommitOnClose(false);
		return dataSource;
	}
}
