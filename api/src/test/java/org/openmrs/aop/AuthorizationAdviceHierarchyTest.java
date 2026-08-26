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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Daemon;
import org.openmrs.logging.MemoryAppender;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.test.jupiter.BaseContextMockTest;
import org.openmrs.util.PrivilegeConstants;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests how {@link AuthorizationAdvice} resolves {@link Authorized} for the method it is advising.
 * <p>
 * Java does not inherit method annotations, so a subtype that redeclares an annotated method — most
 * often only to narrow the return type — carries no annotation of its own, and read naively
 * requires no privilege at all. These tests hold the advice to resolving the annotation through the
 * method hierarchy, to letting a redeclaration that does carry one override what it inherits, and
 * to reporting a method that really is guarded by nothing.
 * <p>
 * The advice is driven directly rather than through a proxy, so the {@link Method} each test passes
 * stands in for the one Spring AOP would hand it — including, deliberately, the synthetic bridge
 * method a caller holding a supertype reference actually invokes. Since the advice reads neither
 * the arguments nor the target, every call passes {@link #NO_ARGS} and a null target.
 */
public class AuthorizationAdviceHierarchyTest extends BaseContextMockTest {

	private static final Object[] NO_ARGS = new Object[0];

	/**
	 * Stands in for a module's base service interface: every {@link Authorized} in this hierarchy is
	 * declared here and nowhere else.
	 */
	public interface BaseTestService {

		@Authorized(PrivilegeConstants.GET_CONCEPTS)
		Object getGuardedThing();

		@Authorized({ PrivilegeConstants.GET_CONCEPTS, PrivilegeConstants.GET_OBS })
		Object getThingRequiringAnyPrivilege();

		@Authorized(value = { PrivilegeConstants.GET_CONCEPTS, PrivilegeConstants.GET_OBS }, requireAll = true)
		Object getThingRequiringAllPrivileges();

		/**
		 * An {@link Authorized} naming no privilege, which asks only that the caller be authenticated.
		 */
		@Authorized
		Object getThingRequiringAuthenticationOnly();

		/**
		 * Overloads share a {@link Method#hashCode()}, so this pair is what tells a cache keyed on the
		 * method apart from one keyed on its name.
		 */
		@Authorized(PrivilegeConstants.GET_CONCEPTS)
		Object getOverloadedThing();

		@Authorized(PrivilegeConstants.GET_ENCOUNTERS)
		Object getOverloadedThing(String argument);

		Object getUnguardedThing();
	}

	/**
	 * The sub-interface at the heart of the ticket: it redeclares the inherited methods without
	 * repeating their annotations. Narrowing the return type is the usual reason to do that, and it
	 * also makes the compiler emit a synthetic bridge method for each.
	 */
	public interface NarrowedTestService extends BaseTestService {

		@Override
		String getGuardedThing();

		@Override
		String getThingRequiringAnyPrivilege();

		@Override
		String getThingRequiringAllPrivileges();

		@Override
		String getThingRequiringAuthenticationOnly();

		@Override
		String getOverloadedThing();

		@Override
		String getOverloadedThing(String argument);

		@Override
		String getUnguardedThing();
	}

	/**
	 * A sub-interface that redeclares an inherited method with an {@link Authorized} of its own, which
	 * is how a subtype changes the privileges a method requires.
	 */
	public interface RedeclaringTestService extends BaseTestService {

		@Override
		@Authorized(PrivilegeConstants.GET_ENCOUNTERS)
		String getGuardedThing();
	}

	public static class NarrowedTestServiceImpl implements NarrowedTestService {

		@Override
		public String getGuardedThing() {
			return null;
		}

		@Override
		public String getThingRequiringAnyPrivilege() {
			return null;
		}

		@Override
		public String getThingRequiringAllPrivileges() {
			return null;
		}

		@Override
		public String getThingRequiringAuthenticationOnly() {
			return null;
		}

		@Override
		public String getOverloadedThing() {
			return null;
		}

		@Override
		public String getOverloadedThing(String argument) {
			return null;
		}

		@Override
		public String getUnguardedThing() {
			return null;
		}
	}

	public static class RedeclaringTestServiceImpl implements RedeclaringTestService {

		@Override
		public String getGuardedThing() {
			return null;
		}

		@Override
		public Object getThingRequiringAnyPrivilege() {
			return null;
		}

		@Override
		public Object getThingRequiringAllPrivileges() {
			return null;
		}

		@Override
		public Object getThingRequiringAuthenticationOnly() {
			return null;
		}

		@Override
		public Object getOverloadedThing() {
			return null;
		}

		@Override
		public Object getOverloadedThing(String argument) {
			return null;
		}

		@Override
		public Object getUnguardedThing() {
			return null;
		}
	}

	/**
	 * Only needed so that the advice can build the message of the exception it throws.
	 */
	@Mock
	private MessageSourceService messageSourceService;

	private MemoryAppender memoryAppender;

	private Logger logger;

	private Level originalLevel;

	private boolean originalAdditive;

	private AuthorizationAdvice advice;

	@BeforeEach
	public void setUp(TestInfo testInfo) {
		// MemoryAppender keys its backing buffer on the appender name in a static map, so each test needs
		// its own name or it sees the log lines every other test produced
		memoryAppender = MemoryAppender.newBuilder().setName("AuthorizationAdviceHierarchyTest-" + testInfo.getDisplayName())
		        .setLayout(PatternLayout.newBuilder().withPattern("%m").build()).build();
		memoryAppender.start();

		logger = (Logger) LogManager.getLogger(AuthorizationAdvice.class);
		originalLevel = logger.getLevel();
		originalAdditive = logger.isAdditive();
		// NB This needs to come before the setLevel() call
		logger.setAdditive(false);
		logger.setLevel(Level.WARN);
		logger.addAppender(memoryAppender);

		// echo the code and arguments back so that the tests can see which privileges a failure named
		lenient().when(messageSourceService.getMessage(anyString(), any(Object[].class), any(Locale.class)))
		        .thenAnswer(invocation -> invocation.getArgument(0, String.class)
		                + Arrays.toString(invocation.getArgument(1, Object[].class)));
		lenient().when(messageSourceService.getMessage(anyString()))
		        .thenAnswer(invocation -> invocation.getArgument(0, String.class));

		// a fresh advice per test so that one test's metadata cache cannot answer another test's lookups
		advice = new AuthorizationAdvice();
	}

	@AfterEach
	public void tearDown() {
		logger.removeAppender(memoryAppender);
		logger.setLevel(originalLevel);
		logger.setAdditive(originalAdditive);
		((Logger) LogManager.getRootLogger()).getContext().updateLoggers();
		memoryAppender.stop();

		memoryAppender = null;
		logger = null;
		advice = null;
	}

	/**
	 * Makes {@link org.openmrs.api.context.Context#hasPrivilege(String)} answer true for exactly the
	 * given privileges and false for everything else.
	 */
	private void userHolds(String... privileges) {
		Set<String> held = new HashSet<>(Arrays.asList(privileges));
		lenient().when(userContext.hasPrivilege(anyString()))
		        .thenAnswer(invocation -> held.contains(invocation.getArgument(0, String.class)));
	}

	/**
	 * @return the method the compiler generated from the source declaration, as opposed to the bridge
	 *         method a covariant return type also produces
	 */
	private static Method declaredMethod(Class<?> type, String name, Class<?>... parameterTypes) {
		return Arrays.stream(type.getDeclaredMethods()).filter(method -> method.getName().equals(name))
		        .filter(method -> !method.isBridge())
		        .filter(method -> Arrays.equals(method.getParameterTypes(), parameterTypes)).findFirst()
		        .orElseThrow(() -> new AssertionError("no declared method " + name + " on " + type.getName()));
	}

	/**
	 * @return the synthetic bridge method a covariant return type produces, which is what a caller
	 *         holding a reference to the supertype actually invokes
	 */
	private static Method bridgeMethod(Class<?> type, String name) {
		return Arrays.stream(type.getDeclaredMethods()).filter(method -> method.getName().equals(name))
		        .filter(method -> method.getParameterCount() == 0).filter(Method::isBridge).findFirst()
		        .orElseThrow(() -> new AssertionError("no bridge method " + name + " on " + type.getName()));
	}

	@Test
	public void before_shouldEnforceThePrivilegeInheritedThroughACovariantRedeclaration() {
		userHolds();

		assertThrows(APIAuthenticationException.class,
		    () -> advice.before(declaredMethod(NarrowedTestServiceImpl.class, "getGuardedThing"), NO_ARGS, null));
	}

	@Test
	public void before_shouldPassWhenThePrivilegeInheritedThroughACovariantRedeclarationIsHeld() {
		userHolds(PrivilegeConstants.GET_CONCEPTS);

		assertDoesNotThrow(
		    () -> advice.before(declaredMethod(NarrowedTestServiceImpl.class, "getGuardedThing"), NO_ARGS, null));
	}

	@Test
	public void before_shouldEnforceTheInheritedPrivilegeThroughTheGeneratedBridgeMethod() {
		userHolds();

		assertThrows(APIAuthenticationException.class,
		    () -> advice.before(bridgeMethod(NarrowedTestServiceImpl.class, "getGuardedThing"), NO_ARGS, null));
	}

	@Test
	public void before_shouldPassOnTheGeneratedBridgeMethodWhenTheInheritedPrivilegeIsHeld() {
		userHolds(PrivilegeConstants.GET_CONCEPTS);

		assertDoesNotThrow(
		    () -> advice.before(bridgeMethod(NarrowedTestServiceImpl.class, "getGuardedThing"), NO_ARGS, null));
	}

	@Test
	public void before_shouldPreferTheAnnotationOnTheRedeclarationOverTheInheritedOne() {
		// the privilege the parent declared no longer opens the method
		userHolds(PrivilegeConstants.GET_CONCEPTS);

		assertThrows(APIAuthenticationException.class,
		    () -> advice.before(declaredMethod(RedeclaringTestServiceImpl.class, "getGuardedThing"), NO_ARGS, null));
	}

	@Test
	public void before_shouldRequireThePrivilegeNamedByTheRedeclaration() {
		userHolds(PrivilegeConstants.GET_ENCOUNTERS);

		assertDoesNotThrow(
		    () -> advice.before(declaredMethod(RedeclaringTestServiceImpl.class, "getGuardedThing"), NO_ARGS, null));
	}

	@Test
	public void before_shouldInheritRequireAllThroughARedeclaration() {
		// holding one of the two is not enough, because the annotation that supplies the privileges also
		// supplies requireAll = true
		userHolds(PrivilegeConstants.GET_CONCEPTS);

		assertThrows(APIAuthenticationException.class, () -> advice
		        .before(declaredMethod(NarrowedTestServiceImpl.class, "getThingRequiringAllPrivileges"), NO_ARGS, null));
	}

	@Test
	public void before_shouldPassWhenEveryInheritedRequiredPrivilegeIsHeld() {
		userHolds(PrivilegeConstants.GET_CONCEPTS, PrivilegeConstants.GET_OBS);

		assertDoesNotThrow(() -> advice
		        .before(declaredMethod(NarrowedTestServiceImpl.class, "getThingRequiringAllPrivileges"), NO_ARGS, null));
	}

	@Test
	public void before_shouldInheritRequireAllBeingUnsetThroughARedeclaration() {
		// the same two privileges, but declared without requireAll, so either one opens the method
		userHolds(PrivilegeConstants.GET_OBS);

		assertDoesNotThrow(() -> advice
		        .before(declaredMethod(NarrowedTestServiceImpl.class, "getThingRequiringAnyPrivilege"), NO_ARGS, null));
	}

	@Test
	public void before_shouldRequireOnlyAuthenticationWhenTheInheritedAnnotationNamesNoPrivilege() {
		// the mocked UserContext reports an authenticated user, and no privilege is named, so there is
		// nothing left to check
		userHolds();

		assertDoesNotThrow(() -> advice.before(
		    declaredMethod(NarrowedTestServiceImpl.class, "getThingRequiringAuthenticationOnly"), NO_ARGS, null));
		verify(userContext, never()).hasPrivilege(anyString());
	}

	@Test
	public void before_shouldRejectAnUnauthenticatedCallerWhenTheInheritedAnnotationNamesNoPrivilege() {
		when(userContext.getAuthenticatedUser()).thenReturn(null);

		assertThrows(APIAuthenticationException.class,
		    () -> advice.before(declaredMethod(NarrowedTestServiceImpl.class, "getThingRequiringAuthenticationOnly"),
		        NO_ARGS, null));
	}

	@Test
	public void before_shouldAcceptProxyPrivilegesInPlaceOfAuthenticationWhenNoPrivilegeIsNamed() {
		// initialisation code runs unauthenticated but with proxy privileges, and has to get through
		when(userContext.getAuthenticatedUser()).thenReturn(null);
		when(userContext.hasProxyPrivileges()).thenReturn(true);

		assertDoesNotThrow(() -> advice.before(
		    declaredMethod(NarrowedTestServiceImpl.class, "getThingRequiringAuthenticationOnly"), NO_ARGS, null));
	}

	@Test
	public void before_shouldCheckNothingOnADaemonThread() {
		userHolds();

		try (MockedStatic<Daemon> daemon = mockStatic(Daemon.class)) {
			daemon.when(Daemon::isDaemonThread).thenReturn(true);

			assertDoesNotThrow(
			    () -> advice.before(declaredMethod(NarrowedTestServiceImpl.class, "getGuardedThing"), NO_ARGS, null));
		}

		verify(userContext, never()).hasPrivilege(anyString());
	}

	@Test
	public void before_shouldResolveEachOverloadSeparatelyThroughTheSameAdvice() {
		// the two overloads share a Method.hashCode() and a name, so a cache keyed on either would hand the
		// first one's privileges to the second
		userHolds(PrivilegeConstants.GET_CONCEPTS);
		Method noArgument = declaredMethod(NarrowedTestServiceImpl.class, "getOverloadedThing");
		Method oneArgument = declaredMethod(NarrowedTestServiceImpl.class, "getOverloadedThing", String.class);

		assertDoesNotThrow(() -> advice.before(noArgument, NO_ARGS, null));
		assertThrows(APIAuthenticationException.class, () -> advice.before(oneArgument, NO_ARGS, null));
	}

	@Test
	public void before_shouldResolveEachOverloadSeparatelyWhicheverIsSeenFirst() {
		userHolds(PrivilegeConstants.GET_ENCOUNTERS);
		Method noArgument = declaredMethod(NarrowedTestServiceImpl.class, "getOverloadedThing");
		Method oneArgument = declaredMethod(NarrowedTestServiceImpl.class, "getOverloadedThing", String.class);

		assertDoesNotThrow(() -> advice.before(oneArgument, NO_ARGS, null));
		assertThrows(APIAuthenticationException.class, () -> advice.before(noArgument, NO_ARGS, null));
	}

	@Test
	public void before_shouldKeepEachMethodsPrivilegesApartAcrossRepeatedCalls() {
		// the cached answer has to stay attached to the method it was resolved for, however many other
		// methods pass through the same advice in between
		userHolds(PrivilegeConstants.GET_CONCEPTS, PrivilegeConstants.GET_OBS);
		Method guarded = declaredMethod(NarrowedTestServiceImpl.class, "getGuardedThing");
		Method needsEncounters = declaredMethod(NarrowedTestServiceImpl.class, "getOverloadedThing", String.class);
		Method unguarded = declaredMethod(NarrowedTestServiceImpl.class, "getUnguardedThing");

		assertDoesNotThrow(() -> advice.before(guarded, NO_ARGS, null));
		assertThrows(APIAuthenticationException.class, () -> advice.before(needsEncounters, NO_ARGS, null));
		assertDoesNotThrow(() -> advice.before(unguarded, NO_ARGS, null));
		assertDoesNotThrow(() -> advice.before(guarded, NO_ARGS, null));
		assertThrows(APIAuthenticationException.class, () -> advice.before(needsEncounters, NO_ARGS, null));
	}

	@Test
	public void before_shouldNotCheckAnyPrivilegeForAMethodWithNoAnnotationInItsHierarchy() {
		userHolds();

		assertDoesNotThrow(
		    () -> advice.before(declaredMethod(NarrowedTestServiceImpl.class, "getUnguardedThing"), NO_ARGS, null));
		verify(userContext, never()).hasPrivilege(anyString());
	}

	@Test
	public void before_shouldWarnAboutAMethodWithNoAnnotationInItsHierarchy() throws Throwable {
		advice.before(declaredMethod(NarrowedTestServiceImpl.class, "getUnguardedThing"), NO_ARGS, null);

		assertThat(memoryAppender.getLogLines(), hasItem(containsString(
		    "No @Authorized annotation applies to " + NarrowedTestServiceImpl.class.getName() + ".getUnguardedThing()")));
	}

	@Test
	public void before_shouldWarnOnlyOnceAboutTheSameUnguardedMethod() throws Throwable {
		Method unguarded = declaredMethod(NarrowedTestServiceImpl.class, "getUnguardedThing");

		advice.before(unguarded, NO_ARGS, null);
		advice.before(unguarded, NO_ARGS, null);

		assertThat(memoryAppender.getLogLines(), contains(containsString("getUnguardedThing()")));
	}

	@Test
	public void before_shouldNotWarnAboutTheBridgeMethodOfAnUnguardedMethod() throws Throwable {
		// the bridge method stands in for a declaration that is reported in its own right, so warning
		// about it as well would just double up
		advice.before(bridgeMethod(NarrowedTestServiceImpl.class, "getUnguardedThing"), NO_ARGS, null);

		assertThat(memoryAppender.getLogLines(), empty());
	}

	@Test
	public void before_shouldNotWarnAboutAMethodGuardedThroughItsHierarchy() throws Throwable {
		userHolds(PrivilegeConstants.GET_CONCEPTS);

		advice.before(declaredMethod(NarrowedTestServiceImpl.class, "getGuardedThing"), NO_ARGS, null);

		assertThat(memoryAppender.getLogLines(), empty());
	}

	@Test
	public void before_shouldReportThePrivilegesOfTheResolvedAnnotationWhenNoneAreHeld() {
		userHolds();

		APIAuthenticationException thrown = assertThrows(APIAuthenticationException.class, () -> advice
		        .before(declaredMethod(NarrowedTestServiceImpl.class, "getThingRequiringAnyPrivilege"), NO_ARGS, null));

		// the privileges are those of the inherited annotation, in the order it declared them
		assertThat(thrown.getMessage(),
		    is("error.privilegesRequired[" + PrivilegeConstants.GET_CONCEPTS + "," + PrivilegeConstants.GET_OBS + "]"));
	}
}
