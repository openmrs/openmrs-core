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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;
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
import org.testcontainers.utility.MountableFile;


/**
 * Tests the startup performance using a previous version of the application and comparing against the nightly image.
 */
@Testcontainers(disabledWithoutDocker = true)
public class StartupPerformanceIT {
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final List<String> CORE_MINOR_VERSIONS = Arrays.asList("2.5", "2.6", "2.7", "2.8", "2.9", "3.0");
	private static final String PROJECT_VERSION = System.getProperty("project.version");
	private static final String TO_VERSION = prepareToVersion(PROJECT_VERSION);
	private static final String FROM_VERSION = prepareFromVersion(PROJECT_VERSION);
	
	@Container
	private static final MariaDBContainer<?> dbContainer = Containers.newMariaDBContainer().withNetwork(Network.newNetwork())
			.withNetworkAliases("mariadb");

	@Test
	public void shouldFailIfStartupTimeOfCoreIncreases() throws Exception {
		compareStartupPerformance("openmrs/openmrs-core:" + FROM_VERSION, 
			"openmrs/openmrs-core:" + TO_VERSION, Duration.ofSeconds(10));
	}

	private static @NotNull String prepareToVersion(String projectVersion) {
		return projectVersion.substring(0, projectVersion.lastIndexOf(".")) + ".x";
	}

	private static @NotNull String prepareFromVersion(String projectVersion) {
		String projectMinorVersion =  projectVersion.substring(0, projectVersion.lastIndexOf("."));
		String prevVersion = null;
		boolean versionFound = false;
		for (String version : CORE_MINOR_VERSIONS) {
			if (version.equals(projectMinorVersion)) {
				versionFound = true;
				break;
			}
			prevVersion = version;
		}
		assertThat("Please make sure that " + projectMinorVersion + " is in the list of CORE_MINOR_VERSIONS", 
			true, is(versionFound));
		return prevVersion + ".x";
	}

	private void compareStartupPerformance(String fromImage, String toImage, Duration timeDiffAccepted) throws SQLException {
		clearDB();
		Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(logger);
		GenericContainer<?> fromContainer = newOpenMRSContainer(fromImage, logConsumer);
		// Do not measure initial setup
		fromContainer.start();
		fromContainer.stop();

		long fromContainerStartupTime = measureMeanStartupTime(fromContainer);

		// Overwrite the war file from the image to the one that was just built instead of using an image created 
		// on the fly from code with ImageFromDockerfile.
		// ImageFromDockerfile runs into some issue when building an image and there is no easy way to debug.
		GenericContainer<?> toContainer = newOpenMRSContainer(toImage, logConsumer);
		assertThat("The test must run after webapp is packaged", 
			Files.exists(Path.of("../../webapp/target/openmrs.war")), is(true));
		toContainer.withCopyFileToContainer(MountableFile.forHostPath("../../webapp/target/openmrs.war"), 
			"/openmrs/distribution/openmrs_core/openmrs.war");
		// Do not measure initial setup
		toContainer.start();
		toContainer.stop();

		long toContainerStartupTime = measureMeanStartupTime(toContainer);

		long diff = Duration.ofNanos(fromContainerStartupTime - toContainerStartupTime).getSeconds();
		logger.info("{} started up in {}s, while {} started up in {}s with the difference of {}s", fromImage, 
			Duration.ofNanos(fromContainerStartupTime).getSeconds(), toImage, 
			Duration.ofNanos(toContainerStartupTime).getSeconds(), diff);
		
		assertThat(diff, lessThan(timeDiffAccepted.getSeconds()));
	}

	@Test
	@Disabled("Platform modules do not run on openmrs-core 3.0.0 yet")
	public void shouldFailIfStartupTimeOfPlatformIncreases() throws SQLException{
		compareStartupPerformance("openmrs/openmrs-platform:" + FROM_VERSION, 
			"openmrs/openmrs-platform:" + TO_VERSION, Duration.ofSeconds(10));
	}

	@Test
	@Disabled("O3 do not run on openmrs-core 3.0.0 yet")
	public void shouldFailIfStartupTimeOfO3Increases() throws SQLException{
		//Using O3 3.6.x as a reference, which is running on openmrs-core 2.8.x
		compareStartupPerformance("openmrs/openmrs-reference-application-3-backend:3.6.x", 
				"openmrs/openmrs-reference-application-3-backend:nightly", Duration.ofSeconds(10));
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
