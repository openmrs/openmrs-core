package org.openmrs; 
/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmrs.test.Containers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


/**
 * Tests the startup performance using a released version of the application and comparing against the nightly image.
 * <p>
 * Ideally we would build an image from code instead of using nightly in order to not rely on building externally before
 * running the test.
 */
@Testcontainers(disabledWithoutDocker = true)
public class StartupPerformanceIT {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	@Container
	private static final MariaDBContainer<?> dbContainer = Containers.newMariaDBContainer().withNetwork(Network.newNetwork())
			.withNetworkAliases("mariadb");

	@Test
	public void shouldFailIfStartupTimeOfCoreIncreases() throws SQLException {
		compareStartupPerformance("openmrs/openmrs-core:2.8.0", "opemrs/openmrs-core:nightly");
	}

	private void compareStartupPerformance(String fromImage, String toImage) throws SQLException {
		clearDB();
		Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(logger);
		GenericContainer<?> releasedVersion = newOpenMRSContainer("openmrs/openmrs-core:2.8.0", logConsumer);
		// Do not measure initial setup
		releasedVersion.start();
		releasedVersion.stop();

		long releasedVersionStartupTime = measureMeanStartupTime(releasedVersion);

		//TODO: Use an image created on the fly from code with ImageFromDockerfile instead of a tagged version
		// It's not possible right now, because of some issue with building an image this way and no easy way to debug
		GenericContainer<?> nightlyVersion = newOpenMRSContainer("openmrs/openmrs-core:nightly", logConsumer);
		// Do not measure initial setup
		nightlyVersion.start();
		nightlyVersion.stop();

		long nightlyVersionStartupTime = measureMeanStartupTime(nightlyVersion);

		long diff = Duration.ofNanos(releasedVersionStartupTime - nightlyVersionStartupTime).getSeconds();

		logger.info("{} started up in {}s, while {} started up in {}s", fromImage, 
			Duration.ofNanos(releasedVersionStartupTime).getSeconds(), toImage, 
			Duration.ofNanos(nightlyVersionStartupTime).getSeconds());
		assertThat(diff, lessThan(10L));
	}

	@Test
	@Disabled("Modules do not run on openmrs-core 3.0.0 yet")
	public void shouldFailIfStartupTimeOfPlatformIncreases() throws SQLException{
		compareStartupPerformance("openmrs/openmrs-platform:2.8.0", "opemrs/openmrs-platform:nightly");
	}

	private long measureMeanStartupTime(GenericContainer<?> releasedVersion) {
		List<Long> times = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			long start = System.nanoTime();
			releasedVersion.start();
			times.add(System.nanoTime() - start);
			releasedVersion.stop();
		}
		return (long) times.stream().mapToLong(Long::longValue).average().orElse(0);
	}
	
	private void clearDB() throws SQLException {
		// Use an initial DB dump in the future to skip setup
		try (Connection conn = DriverManager.getConnection(
			dbContainer.getJdbcUrl(), dbContainer.getUsername(), dbContainer.getPassword());
			 Statement stmt = conn.createStatement()) {

			stmt.executeUpdate("DROP DATABASE IF EXISTS " + dbContainer.getDatabaseName());
			stmt.executeUpdate("CREATE DATABASE " + dbContainer.getDatabaseName());
		}
	}

	private GenericContainer<?> newOpenMRSContainer(String image, Slf4jLogConsumer logConsumer) {
		return new GenericContainer<>(image)
				.withExposedPorts(8080)
				.withNetwork(dbContainer.getNetwork())
				.withEnv("OMRS_DB", "mariadb")
				.withEnv("OMRS_DB_HOSTNAME", "mariadb")
				.withEnv("OMRS_DB_NAME", dbContainer.getDatabaseName())
				.withEnv("OMRS_DB_USERNAME", dbContainer.getUsername())
				.withEnv("OMRS_DB_PASSWORD", dbContainer.getPassword())
				.withEnv("OMRS_DB_PORT", "3306")
				.waitingFor(Wait.forHttp("/openmrs/health/started").withStartupTimeout(Duration.ofMinutes(5)))
				.withLogConsumer(logConsumer);
	}
}
