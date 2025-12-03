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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.io.IOException;
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
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.SelinuxContext;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;


/**
 * Tests the startup performance using a previous version of the application and comparing against the nightly image.
 */
@Testcontainers
public class StartupPerformanceIT {
	
	private static final Logger logger = LoggerFactory.getLogger(StartupPerformanceIT.class);
	private static final Logger containerLogger = LoggerFactory.getLogger("containerLogger");

	private static final List<String> CORE_MINOR_VERSIONS = Arrays.asList("2.5", "2.6", "2.7", "2.8", "2.9", "3.0");
	private static final String PROJECT_VERSION = System.getProperty("project.version");
	private static final String TO_VERSION = prepareToVersion(PROJECT_VERSION);
	private static final String FROM_VERSION = prepareFromVersion(PROJECT_VERSION);
	
	@Container
	private static final MariaDBContainer<?> dbContainer = Containers.newMariaDBContainer().withNetwork(Network.newNetwork())
			.withNetworkAliases("mariadb");

	@Test
	public void shouldFailIfStartupTimeOfCoreIncreases() throws SQLException, IOException  {
		compareStartupPerformance("openmrs/openmrs-core:" + FROM_VERSION, 
			"openmrs/openmrs-core:" + TO_VERSION, Duration.ofSeconds(0));
	}

	@Test
	public void shouldFailIfStartupTimeOfPlatformIncreases() throws SQLException, IOException {
		compareStartupPerformance("openmrs/openmrs-platform:" + FROM_VERSION,
				"openmrs/openmrs-platform:" + TO_VERSION, Duration.ofSeconds(0));
	}

	@Test
	public void shouldFailIfStartupTimeOfO3Increases() throws SQLException, IOException {
		//Using O3 3.6.x as a reference, which is running on openmrs-core 2.8.x
		compareStartupPerformance("openmrs/openmrs-reference-application-3-backend:3.6.x",
				"openmrs/openmrs-reference-application-3-backend:nightly", Duration.ofSeconds(0));
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
		
		final String errorReason = "You must add version " + projectMinorVersion + " " +
			"to CORE_MINOR_VERSIONS";
		if (!versionFound) {
			logger.warn("Version {} not found in CORE_MINOR_VERSIONS. " +
				"Trying to find the previous version.", projectMinorVersion);
			String[] versionParts = projectMinorVersion.split("\\.");
			int minorVersion = Integer.parseInt(versionParts[1]) - 1;
			// Fail if the minor version part is lower than 0.
			assertThat(errorReason, minorVersion, is(greaterThanOrEqualTo(0)));
			String decrementedVersion = versionParts[0] + "." + minorVersion;
			return decrementedVersion + ".x";
		}
		assertThat(errorReason, prevVersion, notNullValue());
		return prevVersion + ".x"; // Append ".x" for the Docker tag convention
	}

	/**
	 * Compares startup performance.
	 * 
	 * @param fromImage docker distro image to compare
	 * @param toImage docker distro image to compare with a war file replaced with the one from the current build
	 * @param timeDiffAccepted set to expected speed-up or slow-down between versions
	 * @throws SQLException if fails to access DB
	 */
	private void compareStartupPerformance(String fromImage, String toImage, Duration timeDiffAccepted) throws IOException, SQLException {
		clearDB();
		Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(containerLogger).withSeparateOutputStreams();
		long fromContainerStartupTime;
		long toContainerStartupTime;
		File tempDirectory = Files.createTempDirectory("test").toFile();
		try (GenericContainer<?> fromContainer = newOpenMRSContainer(fromImage, logConsumer)) {
			fromContainer.addFileSystemBind(tempDirectory.getAbsolutePath(), "/openmrs/data/", BindMode.READ_WRITE, 
					SelinuxContext.SHARED);
			
			// Do not measure initial setup
			fromContainer.start();
			fromContainer.stop();

			fromContainerStartupTime = measureMeanStartupTime(fromContainer);

			// Overwrite the war file from the image to the one that was just built instead of using an image created 
			// on the fly from code with ImageFromDockerfile.
			// ImageFromDockerfile runs into some issue when building an image and there is no easy way to debug.
			try (GenericContainer<?> toContainer = newOpenMRSContainer(toImage, logConsumer)) {
				//toContainer is re-using DB and OpenMRS application data to do upgrade instead of fresh install
				toContainer.addFileSystemBind(tempDirectory.getAbsolutePath(), "/openmrs/data/", BindMode.READ_WRITE,
						SelinuxContext.SHARED);
				assertThat("The test must run after webapp is packaged",
						Files.exists(Path.of("../../webapp/target/openmrs.war")), is(true));
				toContainer.withCopyFileToContainer(MountableFile.forHostPath("../../webapp/target/openmrs.war"),
						"/openmrs/distribution/openmrs_core/openmrs.war");
				// Do not measure initial setup
				toContainer.start();
				toContainer.stop();

				toContainerStartupTime = measureMeanStartupTime(toContainer);
			}
		} finally {
			tempDirectory.delete();
		}

		long diff = Duration.ofNanos(toContainerStartupTime - fromContainerStartupTime).getSeconds();
		logger.info("{} started up in {}s, while {} started up in {}s with the latter starting {} by {}s", fromImage, 
			Duration.ofNanos(fromContainerStartupTime).getSeconds(), toImage, 
			Duration.ofNanos(toContainerStartupTime).getSeconds(), diff < 0 ? "faster" : "slower", Math.abs(diff));
		
		assertThat(diff, lessThan(timeDiffAccepted.getSeconds() + 10)); //10s is an accepted variation between runs
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
				.waitingFor(Wait.forHttp("/openmrs/health/started").withStartupTimeout(Duration.ofMinutes(30)))
				.withLogConsumer(logConsumer);
	}
}
