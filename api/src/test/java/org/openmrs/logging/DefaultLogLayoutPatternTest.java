/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.Test;
import org.openmrs.util.OpenmrsConstants;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

class DefaultLogLayoutPatternTest {

	@Test
	void layoutPattern_shouldReplaceControlCharactersWithSpaces() {
		String message = "before\nafter";
		PatternLayout layout = PatternLayout.newBuilder().withPattern(OpenmrsConstants.DEFAULT_LOG_LAYOUT_PATTERN).build();
		LogEvent event = Log4jLogEvent.newBuilder().setLoggerName("test").setLevel(Level.INFO)
		        .setMessage(new SimpleMessage(message)).build();
		String result = layout.toSerializable(event);
		assertThat(result, containsString("before after"));
		assertThat(result, not(containsString("before\n")));
	}

	@Test
	void layoutPattern_shouldReplaceCarriageReturnWithSpaces() {
		String message = "before\rafter";
		PatternLayout layout = PatternLayout.newBuilder().withPattern(OpenmrsConstants.DEFAULT_LOG_LAYOUT_PATTERN).build();
		LogEvent event = Log4jLogEvent.newBuilder().setLoggerName("test").setLevel(Level.INFO)
		        .setMessage(new SimpleMessage(message)).build();
		String result = layout.toSerializable(event);
		assertThat(result, containsString("before after"));
		assertThat(result, not(containsString("before\r")));
	}

	@Test
	void layoutPattern_shouldNotReplaceTabs() {
		String message = "before\tafter";
		PatternLayout layout = PatternLayout.newBuilder().withPattern(OpenmrsConstants.DEFAULT_LOG_LAYOUT_PATTERN).build();
		LogEvent event = Log4jLogEvent.newBuilder().setLoggerName("test").setLevel(Level.INFO)
		        .setMessage(new SimpleMessage(message)).build();
		String result = layout.toSerializable(event);
		assertThat(result, containsString("before\tafter"));
	}

	@Test
	void layoutPattern_shouldReplaceLineAndParagraphSeparatorsWithSpaces() {
		PatternLayout layout = PatternLayout.newBuilder().withPattern(OpenmrsConstants.DEFAULT_LOG_LAYOUT_PATTERN).build();

		String nelMessage = "before\u0085after";
		LogEvent nelEvent = Log4jLogEvent.newBuilder().setLoggerName("test").setLevel(Level.INFO)
		        .setMessage(new SimpleMessage(nelMessage)).build();
		assertThat(layout.toSerializable(nelEvent), containsString("before after"));

		String lsMessage = "before\u2028after";
		LogEvent lsEvent = Log4jLogEvent.newBuilder().setLoggerName("test").setLevel(Level.INFO)
		        .setMessage(new SimpleMessage(lsMessage)).build();
		assertThat(layout.toSerializable(lsEvent), containsString("before after"));

		String psMessage = "before\u2029after";
		LogEvent psEvent = Log4jLogEvent.newBuilder().setLoggerName("test").setLevel(Level.INFO)
		        .setMessage(new SimpleMessage(psMessage)).build();
		assertThat(layout.toSerializable(psEvent), containsString("before after"));
	}
}
