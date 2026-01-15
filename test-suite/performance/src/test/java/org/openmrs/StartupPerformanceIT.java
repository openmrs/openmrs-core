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
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmrs.test.Containers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.SelinuxContext;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;


/**
 * Tests the startup performance using a previous version of the application and comparing against the nightly image.
 */
@Testcontainers
public class StartupPerformanceIT {
	
	private static final Logger logger = LoggerFactory.getLogger(StartupPerformanceIT.class);
	private static final Logger containerLogger = LoggerFactory.getLogger("testContainersLogger");

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
			"openmrs/openmrs-core:" + TO_VERSION, 10); //degraded performance, 
		// need to look into why, if it affects platform and O3
	}

	@Test
	@Disabled("Platform modules do not run on openmrs-core 3.0.0 yet")
	public void shouldFailIfStartupTimeOfPlatformIncreases() throws SQLException, IOException {
		compareStartupPerformance("openmrs/openmrs-platform:" + FROM_VERSION,
				"openmrs/openmrs-platform:" + TO_VERSION, 0);
	}

	@Test
	@Disabled("O3 modules do not run on openmrs-core 3.0.0 yet")
	public void shouldFailIfStartupTimeOfO3Increases() throws SQLException, IOException {
		// Using O3 3.6.x as a reference, which is running on openmrs-core 2.8.x
		compareStartupPerformance("openmrs/openmrs-reference-application-3-backend:3.6.x-no-demo",
				"openmrs/openmrs-reference-application-3-backend:3.6.x-no-demo", 0);
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
	 * Consumes only lines starting with a level according to the OpenMRS Log4j2 configuration.
	 * It won't accept Tomcat or startup bash script logs, which do not match the pattern and are 
	 * wrongly interpreted as errors.
	 * <p>
	 * It records the time when the first log entry is received to measure the startup time of an application
	 * without container creation.
	 */
	public static class LogConsumer extends Slf4jLogConsumer {
		
		private final AtomicLong startTime = new AtomicLong(0);

		public LogConsumer(Logger logger) {
			super(logger);
			withSeparateOutputStreams();
		}

		public LogConsumer(Logger logger, boolean separateOutputStreams) {
			super(logger, separateOutputStreams);
		}

		public Long getStartTime() {
			return startTime.get();
		}
		
		public void resetStartTime() {
			startTime.set(0);
		}

		@Override
		public void accept(OutputFrame outputFrame) {
			startTime.compareAndSet(0, System.nanoTime());
			if (!outputFrame.getUtf8String().startsWith("ERROR") || !outputFrame.getUtf8String().startsWith("WARN")
				|| !outputFrame.getUtf8String().startsWith("INFO")  || !outputFrame.getUtf8String().startsWith("DEBUG") 
				|| !outputFrame.getUtf8String().startsWith("TRACE")) {
				return;
			}
			super.accept(outputFrame);
		}
	} 

	/**
	 * Compares startup performance.
	 * 
	 * @param fromImage docker distro image to compare
	 * @param toImage docker distro image to compare with a war file replaced with the one from the current build
	 * @param diffInPercent set to expected speed-up or slow-down between versions
	 * @throws SQLException if fails to access DB
	 */
	private void compareStartupPerformance(String fromImage, String toImage, int diffInPercent) throws IOException, SQLException {
		clearDB();
		LogConsumer logConsumer =  new LogConsumer(containerLogger);
		long fromContainerStartupTime;
		long toContainerStartupTime;
		File tempDirectory = Files.createTempDirectory("test").toFile();
		try (GenericContainer<?> fromContainer = newOpenMRSContainer(fromImage, tempDirectory, logConsumer)) {
			// Do not measure initial setup
			logConsumer.resetStartTime();
			fromContainer.start();
			long startupTime = Duration.ofNanos(System.nanoTime() - logConsumer.getStartTime()).toMillis();
			logger.info("{} installed in {}ms", fromContainer.getDockerImageName(), startupTime);
			fromContainer.stop();

			fromContainerStartupTime = measureMeanStartupTime(fromContainer, logConsumer);

			// Overwrite the war file from the image to the one that was just built instead of using an image created 
			// on the fly from code with ImageFromDockerfile.
			// ImageFromDockerfile runs into some issue when building an image and there is no easy way to debug.
			try (GenericContainer<?> toContainer = newOpenMRSContainer(toImage, tempDirectory, logConsumer)) {
				//toContainer is re-using DB and OpenMRS application data to do upgrade instead of fresh install
				assertThat("The test must run after webapp is packaged",
						Files.exists(Path.of("../../webapp/target/openmrs.war")), is(true));
				toContainer.withCopyFileToContainer(MountableFile.forHostPath("../../webapp/target/openmrs.war"),
						"/openmrs/distribution/openmrs_core/openmrs.war");
				// Do not measure initial setup
				logConsumer.resetStartTime();
				toContainer.start();
				startupTime = Duration.ofNanos(System.nanoTime() - logConsumer.getStartTime()).toMillis();
				logger.info("{} upgraded in {}ms", toContainer.getDockerImageName(), startupTime);
				toContainer.stop();

				toContainerStartupTime = measureMeanStartupTime(toContainer, logConsumer);
			}
		} finally {
			tempDirectory.delete();
		}

		long diff = toContainerStartupTime - fromContainerStartupTime;
		int actualDiffInPercent = diff == 0 ? 0 : Math.round(((float) diff) / fromContainerStartupTime * 100);
		String describeDifference = actualDiffInPercent == 0 ? "almost the same" : 
			String.format("%d%% (%dms) %s", Math.abs(actualDiffInPercent), Math.abs(diff), (diff < 0 ? "faster" : "slower"));
		logger.info("{} started up on average in {}ms, while {} started up in {}ms, which is {}", fromImage, 
			fromContainerStartupTime, toImage, toContainerStartupTime, describeDifference);

		// 15% is an accepted variation between runs
		long acceptedDiff = Math.round(((double) fromContainerStartupTime) * (diffInPercent + 15) / 100);
		
		assertThat("Fail if slower than " + diffInPercent + "% + 15% (accepted variation between runs)", diff, lessThan(acceptedDiff)); 
	}

	private long measureMeanStartupTime(GenericContainer<?> container, LogConsumer logConsumer) {
		List<Long> times = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			logConsumer.resetStartTime();
			container.start();
			long startupTime = Duration.ofNanos(System.nanoTime() - logConsumer.getStartTime()).toMillis();
			logger.info("{} started up in {}ms", container.getDockerImageName(), startupTime);
			times.add(startupTime);
			container.stop();
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

	private GenericContainer<?> newOpenMRSContainer(String image, File dataDir, Consumer<OutputFrame> logConsumer) {
		GenericContainer<?> container = new GenericContainer<>(image)
			.withImagePullPolicy(PullPolicy.alwaysPull())
			.withExposedPorts(8080)
			.withNetwork(dbContainer.getNetwork())
			.withEnv("OMRS_DB", "mariadb")
			.withEnv("OMRS_DB_HOSTNAME", "mariadb")
			.withEnv("OMRS_DB_NAME", dbContainer.getDatabaseName())
			.withEnv("OMRS_DB_USERNAME", dbContainer.getUsername())
			.withEnv("OMRS_DB_PASSWORD", dbContainer.getPassword())
			.withEnv("OMRS_DB_PORT", "3306")
			.withCreateContainerCmdModifier(cmd -> {
				cmd.getHostConfig() // Simulate lower specs
					.withMemory(DataSize.of(2, DataUnit.GIGABYTES).toBytes())
					.withCpuCount(2L);
			})
			.waitingFor(Wait.forHttp("/openmrs/health/started").withStartupTimeout(Duration.ofMinutes(30)))
			.withLogConsumer(logConsumer);
		container.addFileSystemBind(dataDir.getAbsolutePath(), "/openmrs/data/", BindMode.READ_WRITE,
			SelinuxContext.SHARED);
		
		return container;
	}
}
