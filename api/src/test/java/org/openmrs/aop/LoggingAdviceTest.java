/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.aop;

import java.lang.reflect.Method;
import java.util.List;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.openmrs.annotation.Logging;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.logging.MemoryAppender;
import org.openmrs.util.OpenmrsConstants;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link LoggingAdvice}. The advice logs to the fixed logger name
 * {@link OpenmrsConstants#LOG_CLASS_DEFAULT}, so these tests attach a {@link MemoryAppender} to
 * that logger and drive its level to exercise each branch.
 */
class LoggingAdviceTest {

	/**
	 * Methods the advice is exercised against. The advice classifies by method name prefix, so the
	 * names here matter: "get" is a getter (TRACE) and "save" is a setter (DEBUG).
	 */
	public interface Sample {

		String getThing(Integer id);

		String saveThing(String thing, String password);

		@Logging(ignore = true)
		String saveIgnoredThing(String thing);

		@Logging(ignoredArgumentIndexes = { 1 })
		String saveThingWithSecret(String thing, String password);

		@Logging(ignoredArgumentIndexes = { 1, 7 })
		String saveThingWithOutOfRangeIgnoredIndex(String thing, String password);

		@Logging(ignoreAllArgumentValues = true)
		String saveSecretThing(String thing);
	}

	private MemoryAppender memoryAppender;

	private Logger logger;

	private Level originalLevel;

	private boolean originalAdditive;

	private LoggingAdvice advice;

	@BeforeEach
	void setup(TestInfo testInfo) {
		// MemoryAppender keys its backing buffer on the appender name in a static map, so each test needs
		// its own name or it sees the log lines every other test produced
		memoryAppender = MemoryAppender.newBuilder().setName("LoggingAdviceTest-" + testInfo.getDisplayName())
		        .setLayout(PatternLayout.newBuilder().withPattern("%m").build()).build();
		memoryAppender.start();

		logger = (Logger) LogManager.getLogger(OpenmrsConstants.LOG_CLASS_DEFAULT);
		originalLevel = logger.getLevel();
		originalAdditive = logger.isAdditive();
		// NB This needs to come before the setLevel() call
		logger.setAdditive(false);
		logger.setLevel(Level.WARN);
		logger.addAppender(memoryAppender);

		advice = new LoggingAdvice();
	}

	@AfterEach
	void tearDown() {
		logger.removeAppender(memoryAppender);
		logger.setLevel(originalLevel);
		logger.setAdditive(originalAdditive);
		((Logger) LogManager.getRootLogger()).getContext().updateLoggers();
		memoryAppender.stop();

		Context.clearUserContext();

		memoryAppender = null;
		logger = null;
		advice = null;
	}

	private MethodInvocation invocationFor(String methodName, Class<?>[] parameterTypes, Object... arguments)
	        throws Throwable {
		Method method = Sample.class.getMethod(methodName, parameterTypes);
		MethodInvocation invocation = mock(MethodInvocation.class);
		when(invocation.getMethod()).thenReturn(method);
		when(invocation.getArguments()).thenReturn(arguments);
		when(invocation.proceed()).thenReturn("result");
		return invocation;
	}

	@Test
	void shouldNotLogAnythingWhenLoggerIsDisabled() throws Throwable {
		MethodInvocation invocation = invocationFor("saveThing", new Class<?>[] { String.class, String.class }, "thing",
		    "secret");

		assertThat(advice.invoke(invocation), org.hamcrest.Matchers.equalTo("result"));

		verify(invocation, times(1)).proceed();
		assertThat(memoryAppender.getLogLines(), empty());
	}

	@Test
	void shouldStillProceedWhenLoggerIsDisabled() throws Throwable {
		MethodInvocation invocation = invocationFor("getThing", new Class<?>[] { Integer.class }, 1);

		advice.invoke(invocation);

		verify(invocation, times(1)).proceed();
	}

	@Test
	void shouldPropagateExceptionsWhenLoggerIsDisabled() throws Throwable {
		MethodInvocation invocation = invocationFor("getThing", new Class<?>[] { Integer.class }, 1);
		IllegalStateException thrown = new IllegalStateException("boom");
		when(invocation.proceed()).thenThrow(thrown);

		assertThat(assertThrows(IllegalStateException.class, () -> advice.invoke(invocation)),
		    org.hamcrest.Matchers.sameInstance(thrown));
		assertThat(memoryAppender.getLogLines(), empty());
	}

	@Test
	void shouldLogSettersWhenDebugIsEnabled() throws Throwable {
		logger.setLevel(Level.DEBUG);
		MethodInvocation invocation = invocationFor("saveThing", new Class<?>[] { String.class, String.class }, "thing",
		    "secret");

		advice.invoke(invocation);

		List<String> logLines = memoryAppender.getLogLines();
		assertThat(logLines, hasItem(containsString("In method Sample.saveThing. Arguments: String=thing, String=secret,")));
		assertThat(logLines, hasItem(containsString("Exiting method saveThing. execution time:")));
	}

	@Test
	void shouldNotLogGettersWhenOnlyDebugIsEnabled() throws Throwable {
		logger.setLevel(Level.DEBUG);
		MethodInvocation invocation = invocationFor("getThing", new Class<?>[] { Integer.class }, 1);

		advice.invoke(invocation);

		verify(invocation, times(1)).proceed();
		assertThat(memoryAppender.getLogLines(), empty());
	}

	@Test
	void shouldLogGettersWhenTraceIsEnabled() throws Throwable {
		logger.setLevel(Level.TRACE);
		MethodInvocation invocation = invocationFor("getThing", new Class<?>[] { Integer.class }, 1);

		advice.invoke(invocation);

		List<String> logLines = memoryAppender.getLogLines();
		assertThat(logLines, hasItem(containsString("In method Sample.getThing. Arguments: Integer=1,")));
		// TRACE implies DEBUG here, so the execution time is included
		assertThat(logLines, hasItem(containsString("Exiting method getThing. execution time:")));
	}

	@Test
	void shouldNotLogMethodsAnnotatedAsIgnored() throws Throwable {
		logger.setLevel(Level.TRACE);
		MethodInvocation invocation = invocationFor("saveIgnoredThing", new Class<?>[] { String.class }, "thing");

		advice.invoke(invocation);

		verify(invocation, times(1)).proceed();
		assertThat(memoryAppender.getLogLines(), empty());
	}

	@Test
	void shouldMaskArgumentsAtTheAnnotatedIndexes() throws Throwable {
		logger.setLevel(Level.DEBUG);
		MethodInvocation invocation = invocationFor("saveThingWithSecret", new Class<?>[] { String.class, String.class },
		    "thing", "secret");

		advice.invoke(invocation);

		List<String> logLines = memoryAppender.getLogLines();
		assertThat(logLines, hasItem(
		    containsString("In method Sample.saveThingWithSecret. Arguments: String=thing, String=<Arg value ignored>,")));
		assertThat(logLines, not(hasItem(containsString("secret"))));
	}

	@Test
	void shouldIgnoreAnnotatedIndexesOutsideTheParameterList() throws Throwable {
		logger.setLevel(Level.DEBUG);
		MethodInvocation invocation = invocationFor("saveThingWithOutOfRangeIgnoredIndex",
		    new Class<?>[] { String.class, String.class }, "thing", "secret");

		advice.invoke(invocation);

		assertThat(memoryAppender.getLogLines(), hasItem(containsString(
		    "In method Sample.saveThingWithOutOfRangeIgnoredIndex. Arguments: String=thing, String=<Arg value ignored>,")));
	}

	@Test
	void shouldOmitAllArgumentsWhenAnnotatedToDoSo() throws Throwable {
		logger.setLevel(Level.DEBUG);
		MethodInvocation invocation = invocationFor("saveSecretThing", new Class<?>[] { String.class }, "thing");

		advice.invoke(invocation);

		List<String> logLines = memoryAppender.getLogLines();
		assertThat(logLines, hasItem(containsString("In method Sample.saveSecretThing")));
		assertThat(logLines, not(hasItem(containsString("Arguments:"))));
	}

	@Test
	void shouldLogTheErrorAndTheExitWhenTheWrappedMethodThrows() throws Throwable {
		UserContext userContext = mock(UserContext.class);
		when(userContext.getAuthenticatedUser()).thenReturn(null);
		Context.setUserContext(userContext);

		logger.setLevel(Level.DEBUG);
		MethodInvocation invocation = invocationFor("saveThing", new Class<?>[] { String.class, String.class }, "thing",
		    "secret");
		IllegalStateException thrown = new IllegalStateException("boom");
		when(invocation.proceed()).thenThrow(thrown);

		assertThat(assertThrows(IllegalStateException.class, () -> advice.invoke(invocation)),
		    org.hamcrest.Matchers.sameInstance(thrown));

		List<String> logLines = memoryAppender.getLogLines();
		assertThat(logLines, hasItem(containsString("An error occurred while executing this method.")));
		assertThat(logLines, hasItem(containsString("Guest (Not logged in)")));
		assertThat(logLines, hasItem(containsString("Exiting method saveThing")));
	}

	@Test
	void shouldLogEntryBeforeAndExitAfterTheWrappedMethod() throws Throwable {
		logger.setLevel(Level.DEBUG);
		MethodInvocation invocation = invocationFor("saveSecretThing", new Class<?>[] { String.class }, "thing");

		advice.invoke(invocation);

		assertThat(memoryAppender.getLogLines(),
		    contains(containsString("In method Sample.saveSecretThing"), containsString("Exiting method saveSecretThing")));
	}
}
