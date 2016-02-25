/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.module.BaseModuleActivatorTest;
import org.openmrs.web.test.TestContextLoader;
import org.springframework.mock.web.MockServletContext;
import org.springframework.mock.web.portlet.MockEvent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.support.XmlWebApplicationContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 * Tests methods on the {@link WebUtil} class.
 */
public class ListenerTest {

    /**
     * Create WEB-INFO file for testing purpose
     * @param servletContext
     */
    private void createWEB_INFOFile(ServletContext servletContext){
        Log log = LogFactory.getLog(ListenerTest.class);
        String realPath = servletContext.getRealPath("");
        String absPath = realPath + "/WEB-INF/dwr-modules.xml";

        File dwrFile = new File(absPath.replace("/", File.separator));
        dwrFile.getParentFile().mkdirs();
        FileWriter writer = null;
        try {
            writer = new FileWriter(dwrFile);
            writer
                    .write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE dwr PUBLIC \"-//GetAhead Limited//DTD Direct Web Remoting 2.0//EN\" \"http://directwebremoting.org/schema/dwr20.dtd\">\n<dwr></dwr>");
        }
        catch (IOException io) {
            log.error("Unable to clear out the " + dwrFile.getAbsolutePath()
                      + " file.  Please redeploy the openmrs war file", io);
        }
        finally {
            if (writer != null) {
                try {
                    writer.close();
                }
                catch (IOException io) {
                    log.warn("Couldn't close Writer: " + io);
                }
            }
        }
    }

    /**
     * Delete WEB-INFO file if exist
     * @param servletContext
     */
    private void deleteWEB_INFOFile(ServletContext servletContext){
        Log log = LogFactory.getLog(ListenerTest.class);
        String realPath = servletContext.getRealPath("");
        String absPath = realPath + "/WEB-INF/dwr-modules.xml";

        File dwrFile = new File(absPath.replace("/", File.separator));
        if (dwrFile.exists()){
            dwrFile.delete();
        }
    }

    /**
     * @see org.openmrs.web.Listener#performWebStartOfModules(ServletContext)
     * @verifies should not throw exception
     */
    @Test
    public void performWebStartOfModules_shouldNotThrowException() throws Throwable {
        MockServletContext sc = new MockServletContext();
        Listener.performWebStartOfModules(sc);
    }

    /**
     * @see org.openmrs.web.Listener#contextInitialized(ServletContextEvent)
     * @verifies should not throw exception
     */
    @Test
    public void contextInitialized_shouldNotThrowException() throws Throwable {
        MockServletContext servletContext = new MockServletContext();
        //Create WEB-INFO file
        createWEB_INFOFile(servletContext);
        ServletContextEvent contextEvent = new ServletContextEvent(servletContext);
        Listener listener = new Listener();
        listener.contextInitialized(contextEvent);
        //Delete WEB-INFO aftwer usage
        deleteWEB_INFOFile(servletContext);
    }

    /**
     * @see org.openmrs.web.Listener#contextInitialized(ServletContextEvent)
     * @verifies should not throw exception when call contextInitialized twice
     */
    @Test
    public void contextInitialized_shouldNotThrowExceptionWhenCallThisMethodTwice() throws Throwable {
        MockServletContext servletContext = new MockServletContext();
        //Create WEB-INFO file
        createWEB_INFOFile(servletContext);
        ServletContextEvent contextEvent = new ServletContextEvent(servletContext);
        Listener listener = new Listener();
        listener.contextInitialized(contextEvent);
        listener.contextInitialized(contextEvent);
        //Delete WEB-INFO aftwer usage
        deleteWEB_INFOFile(servletContext);
    }
}
