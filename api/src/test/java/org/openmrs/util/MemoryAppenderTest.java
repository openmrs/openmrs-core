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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

public class MemoryAppenderTest {

    private MemoryAppender memoryAppender;
    private Logger logger;

    @Before
    public void setup() {
        memoryAppender = MemoryAppender.newBuilder()
                .setName("MEMORY_APPENDER_TEST")
                .setLayout(PatternLayout.newBuilder().withPattern("%m").build())
                .build();
        memoryAppender.start();

        setupLogger();
    }

    @After
    public void tearDown() {
        logger.removeAppender(memoryAppender);
        memoryAppender.stop();
        ((Logger) LogManager.getRootLogger()).getContext().updateLoggers();

        memoryAppender = null;
        logger = null;
    }

    @Test
    public void memoryAppender_shouldAppendAMessage() {
        logger.warn("Logging message");

        List<String> logLines = memoryAppender.getLogLines();
        assertThat(logLines, notNullValue());
        assertThat(logLines.size(), equalTo(1));
        assertThat(logLines.get(0), equalTo("Logging message"));
    }

    @Test
    public void memoryAppender_shouldAppendMultipleMessages() {
        int nTimes = 12;
        for (int i = 0; i < nTimes; i++) {
            logger.warn("Logging message");
        }

        List<String> logLines = memoryAppender.getLogLines();
        assertThat(logLines, notNullValue());
        assertThat(logLines.size(), equalTo(nTimes));
        for (int i = 0; i < nTimes; i++) {
            assertThat(logLines.get(i), equalTo("Logging message"));
        }
    }

    @Test
    public void memoryAppender_shouldOnlyKeepBufferSizeItems() {
        // clear setup() results
        logger.removeAppender(memoryAppender);

        memoryAppender = MemoryAppender.newBuilder()
                .setName("MEMORY_APPENDER_TEST")
                .setLayout(PatternLayout.newBuilder().withPattern("%m").build())
                .setBufferSize(4)
                .build();
        memoryAppender.start();

        setupLogger();

        for (int i = 0; i < 12; i++) {
            logger.warn("Logging message");
        }

        List<String> logLines = memoryAppender.getLogLines();
        assertThat(logLines, notNullValue());
        assertThat(logLines.size(), equalTo(4));
    }

    private void setupLogger() {
        logger = (Logger) LogManager.getLogger("MemoryAppenderTest");
        // NB This needs to come before the setLevel() call
        logger.setAdditive(false);
        logger.setLevel(Level.ALL);
        logger.addAppender(memoryAppender);
    }

}