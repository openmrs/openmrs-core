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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
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
 * Tests the startup performance of the current build (2.9.x) against the previous release (2.8.x).
 * <p>
 * Rather than external wall-clock time - which folds in container creation and health-poll latency and is
 * dominated by shared-CI-runner variance - this compares each container's own reported startup time
 * (Tomcat's "Server startup in [N] milliseconds") and, as a deterministic gate, verifies that the current
 * build actually skips the version-gated setup work on an unchanged-version restart (TRUNK-6418). Both
 * containers run back-to-back on the same host, so the internal metric is far steadier than wall-clock.
 */
@Testcontainers
public class StartupPerformanceIT {

	private static final Logger logger = LoggerFactory.getLogger(StartupPerformanceIT.class);
	private static final Logger containerLogger = LoggerFactory.getLogger("testContainersLogger");

	private static final List<String> CORE_MINOR_VERSIONS = Arrays.asList("2.5", "2.6", "2.7", "2.8", "2.9", "3.0");
	private static final String PROJECT_VERSION = System.getProperty("project.version");
	private static final String TO_VERSION = prepareToVersion(PROJECT_VERSION);
	private static final String FROM_VERSION = prepareFromVersion(PROJECT_VERSION);

	private static final int RESTART_COUNT = 3;

	// We expect the current build to start at least as fast as the previous release; allow it to be at
	// most 5% slower to absorb the small residual variance in the internal metric between runs.
	private static final double MAX_STARTUP_RATIO = 1.05;

	private static final Pattern TOMCAT_STARTUP_PATTERN = Pattern.compile("Server startup in \\[(\\d+)\\] millisecond");

	@Container
	private static final MariaDBContainer<?> dbContainer = Containers.newMariaDBContainer().withNetwork(Network.newNetwork())
			.withNetworkAliases("mariadb");

	@Test
	public void shouldFailIfStartupTimeOfPlatformIncreases() throws SQLException, IOException {
		compareStartupPerformance("openmrs/openmrs-platform:" + FROM_VERSION,
				"openmrs/openmrs-platform:" + TO_VERSION);
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
	 * Installs {@code fromImage}, then upgrades in place to {@code toImage} (the current build's war), and
	 * compares their restart performance. Fails if the current build either stops skipping version-gated
	 * setup on restart (TRUNK-6418) or starts more than {@link #MAX_STARTUP_RATIO} slower than the previous
	 * release.
	 *
	 * @param fromImage docker distro image of the previous release
	 * @param toImage docker distro image whose war is replaced with the one from the current build
	 * @throws SQLException if fails to access DB
	 */
	private void compareStartupPerformance(String fromImage, String toImage) throws IOException, SQLException {
		clearDB();
		LogConsumer logConsumer =  new LogConsumer(containerLogger);
		long fromStartupMillis;
		StartupMeasurement toMeasurement;
		File tempDirectory = Files.createTempDirectory("test").toFile();
		try (GenericContainer<?> fromContainer = newOpenMRSContainer(fromImage, tempDirectory, logConsumer)) {
			// Fresh install of the previous release; the initial setup is not measured.
			fromContainer.start();
			logger.info("{} installed", fromContainer.getDockerImageName());
			fromContainer.stop();

			fromStartupMillis = measureMeanTomcatStartup(fromContainer).meanMillis;

			// Overwrite the war file from the image with the one that was just built instead of using an image
			// created on the fly from code with ImageFromDockerfile.
			// ImageFromDockerfile runs into some issue when building an image and there is no easy way to debug.
			try (GenericContainer<?> toContainer = newOpenMRSContainer(toImage, tempDirectory, logConsumer)) {
				//toContainer is re-using DB and OpenMRS application data to do upgrade instead of fresh install
				assertThat("The test must run after webapp is packaged",
						Files.exists(Paths.get("../../webapp/target/openmrs.war")), is(true));
				toContainer.withCopyFileToContainer(MountableFile.forHostPath("../../webapp/target/openmrs.war"),
						"/openmrs/distribution/openmrs_core/openmrs.war");
				// Upgrade start (writes the current version to the DB and data dir); not measured.
				toContainer.start();
				logger.info("{} upgraded", toContainer.getDockerImageName());
				toContainer.stop();

				toMeasurement = measureMeanTomcatStartup(toContainer);
			}
		} finally {
			tempDirectory.delete();
		}

		// Behavioral gate: on an unchanged-version restart the current build must skip the version-gated
		// setup work (TRUNK-6418) rather than re-running Liquibase. This is deterministic - unlike timing -
		// and is the actual optimization we care about not regressing.
		String restartLogs = toMeasurement.lastRestartLogs;
		assertThat("Restart must skip module setup when the version is unchanged (TRUNK-6418)",
				restartLogs, containsString("did not change, skipping setup"));
		assertThat("Restart must not run Liquibase when the version is unchanged (TRUNK-6418)",
				restartLogs, not(containsString("liquibasechangelog")));

		// Timing gate + trend: compare the containers' own reported startup time. We expect the current
		// build to be faster; fail only if it is more than MAX_STARTUP_RATIO slower than the previous release.
		long toStartupMillis = toMeasurement.meanMillis;
		long diff = toStartupMillis - fromStartupMillis;
		int diffPercent = fromStartupMillis == 0 ? 0 : Math.round(((float) diff) / fromStartupMillis * 100);
		logger.info("{} started up on average in {}ms, while {} started up in {}ms, which is {}% {}", fromImage,
				fromStartupMillis, toImage, toStartupMillis, Math.abs(diffPercent), diff <= 0 ? "faster" : "slower");

		long maxAcceptedMillis = Math.round(fromStartupMillis * MAX_STARTUP_RATIO);
		assertThat(toImage + " (" + toStartupMillis + "ms) must start within "
				+ Math.round((MAX_STARTUP_RATIO - 1) * 100) + "% of " + fromImage + " (" + fromStartupMillis + "ms)",
				toStartupMillis, lessThanOrEqualTo(maxAcceptedMillis));
	}

	/**
	 * Restarts the (already installed) container {@link #RESTART_COUNT} times and returns the mean of the
	 * startup times it reports itself, along with the logs of the final restart for behavioral assertions.
	 */
	private StartupMeasurement measureMeanTomcatStartup(GenericContainer<?> container) {
		List<Long> times = new ArrayList<>();
		String lastRestartLogs = "";
		for (int i = 0; i < RESTART_COUNT; i++) {
			container.start();
			long startupMillis = awaitTomcatStartupMillis(container);
			// Re-read once the startup line is present so the captured logs are complete for later assertions.
			lastRestartLogs = container.getLogs();
			logger.info("{} restart {}/{} reported startup in {}ms", container.getDockerImageName(), i + 1,
					RESTART_COUNT, startupMillis);
			times.add(startupMillis);
			container.stop();
		}
		long mean = (long) times.stream().mapToLong(Long::longValue).average().orElse(0);
		return new StartupMeasurement(mean, lastRestartLogs);
	}

	/**
	 * Reads the startup time the container reports via Tomcat's "Server startup in [N] milliseconds" line.
	 * That line is logged moments after the health endpoint goes green, so we briefly poll the logs for it
	 * rather than assuming it has already been flushed.
	 */
	private long awaitTomcatStartupMillis(GenericContainer<?> container) {
		for (int attempt = 0; attempt < 20; attempt++) {
			long millis = -1;
			Matcher matcher = TOMCAT_STARTUP_PATTERN.matcher(container.getLogs());
			// Use the last match, which corresponds to the current (most recent) container start.
			while (matcher.find()) {
				millis = Long.parseLong(matcher.group(1));
			}
			if (millis >= 0) {
				return millis;
			}
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
		throw new AssertionError("Could not find Tomcat's 'Server startup in [N] milliseconds' in logs for "
				+ container.getDockerImageName());
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

	/** Mean startup time of a container's restarts plus the logs of the final restart. */
	private static final class StartupMeasurement {

		private final long meanMillis;

		private final String lastRestartLogs;

		private StartupMeasurement(long meanMillis, String lastRestartLogs) {
			this.meanMillis = meanMillis;
			this.lastRestartLogs = lastRestartLogs;
		}
	}
}
