/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for the account lockout mechanism. The first lockout lasts the duration
 * configured in {@link OpenmrsConstants#GP_UNLOCK_ACCOUNT_WAITING_TIME}, and every consecutive
 * lockout doubles that duration up to
 * {@link OpenmrsConstants#GP_MAX_ACCOUNT_LOCKOUT_DURATION}.
 */
public final class AccountLockoutUtil {
	
	private static final Logger log = LoggerFactory.getLogger(AccountLockoutUtil.class);
	
	private static final long DEFAULT_INITIAL_LOCKOUT_DURATION_MS = TimeUnit.MINUTES.toMillis(10);
	
	private static final long DEFAULT_MAX_LOCKOUT_DURATION_MS = TimeUnit.HOURS.toMillis(24);
	
	private AccountLockoutUtil() {
	}
	
	/**
	 * Calculates the duration of a lockout. The first lockout lasts
	 * {@code initialDurationMs}, and every consecutive lockout doubles that duration until
	 * {@code maximumDurationMs} is reached. The loop stops once the maximum is reached so that a
	 * corrupt or unusually large lockout count cannot overflow the calculation.
	 *
	 * @param lockoutCount the number of consecutive lockouts, where the first lockout is 1
	 * @param initialDurationMs the duration of the first lockout in milliseconds
	 * @param maximumDurationMs the maximum lockout duration in milliseconds
	 * @return the lockout duration in milliseconds
	 */
	static long calculateLockoutDurationMs(long lockoutCount, long initialDurationMs, long maximumDurationMs) {
		if (initialDurationMs <= 0) {
			return 0L;
		}
		
		long maximum = maximumDurationMs > 0 ? maximumDurationMs : initialDurationMs;
		long durationMs = Math.min(initialDurationMs, maximum);
		
		for (long lockout = 1; lockout < lockoutCount && durationMs < maximum; lockout++) {
			durationMs = durationMs > maximum / 2 ? maximum : durationMs * 2;
		}
		return Math.min(durationMs, maximum);
	}
	
	/**
	 * Gets the configured lockout duration for the given number of consecutive lockouts.
	 *
	 * @param lockoutCount the number of consecutive lockouts, where the first lockout is 1
	 * @return the lockout duration in milliseconds
	 */
	public static long getLockoutDurationMs(long lockoutCount) {
		long initialDurationMs = DEFAULT_INITIAL_LOCKOUT_DURATION_MS;
		long maximumDurationMs = DEFAULT_MAX_LOCKOUT_DURATION_MS;
		
		try {
			Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
			initialDurationMs = getMinutesGlobalProperty(OpenmrsConstants.GP_UNLOCK_ACCOUNT_WAITING_TIME, initialDurationMs);
			maximumDurationMs = getMinutesGlobalProperty(OpenmrsConstants.GP_MAX_ACCOUNT_LOCKOUT_DURATION, maximumDurationMs);
		}
		catch (Exception e) {
			log.error("Unable to read the account lockout global properties. Using the default initial duration "
			        + "of 10 minutes and maximum duration of 24 hours.", e);
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		}
		
		return calculateLockoutDurationMs(lockoutCount, initialDurationMs, maximumDurationMs);
	}
	
	private static long getMinutesGlobalProperty(String property, long defaultDurationMs) {
		String configuredValue = Context.getAdministrationService().getGlobalProperty(property);
		if (StringUtils.isNotBlank(configuredValue)) {
			try {
				long minutes = Long.parseLong(configuredValue.trim());
				if (minutes >= 0) {
					return TimeUnit.MINUTES.toMillis(minutes);
				}
			}
			catch (NumberFormatException e) {
				log.error("Unable to convert the global property {} to a valid number of minutes. Using the default.",
				    property, e);
			}
		}
		return defaultDurationMs;
	}
	
	/**
	 * Gets the time at which the given user's recorded lockout ends, or null if no lockout is
	 * recorded. The returned time is in the past once the lockout has expired, so callers decide
	 * whether the lockout is still active by comparing it with the current time.
	 *
	 * @param user the user to check
	 * @return the time the recorded lockout ends in milliseconds since the epoch, even if that time
	 *         has already passed, or null if the user does not have a recorded lockout
	 */
	public static Long getLockoutEndTimeMs(User user) {
		String lockoutTimestamp = user.getUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP, null);
		if (StringUtils.isBlank(lockoutTimestamp) || "0".equals(lockoutTimestamp)) {
			return null;
		}
		
		long lockoutStart;
		try {
			lockoutStart = Long.parseLong(lockoutTimestamp);
		}
		catch (NumberFormatException e) {
			log.warn("bad value stored in {} user property: {}", OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP,
			    lockoutTimestamp);
			return null;
		}
		if (lockoutStart <= 0) {
			return null;
		}
		
		long lockoutDuration = getLockoutDurationMs(getLockoutCount(user));
		return lockoutDuration > Long.MAX_VALUE - lockoutStart ? Long.MAX_VALUE : lockoutStart + lockoutDuration;
	}
	
	/**
	 * Gets the number of consecutive lockouts stored for the given user.
	 *
	 * @param user the user to check
	 * @return the number of consecutive lockouts, defaulting to zero if none is defined
	 */
	public static int getLockoutCount(User user) {
		String lockoutCountString = user.getUserProperty(OpenmrsConstants.USER_PROPERTY_LOCKOUT_COUNT, "0");
		int lockoutCount = 0;
		try {
			lockoutCount = Integer.parseInt(lockoutCountString);
		}
		catch (NumberFormatException e) {
			log.warn("bad value stored in {} user property: {}", OpenmrsConstants.USER_PROPERTY_LOCKOUT_COUNT,
			    lockoutCountString);
		}
		return Math.max(lockoutCount, 0);
	}
}
