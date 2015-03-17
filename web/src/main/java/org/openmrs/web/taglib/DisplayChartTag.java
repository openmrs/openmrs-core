/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.taglib;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.JFreeChart;
import org.openmrs.web.servlet.AbstractGraphServlet;
import org.openmrs.web.servlet.DisplayChartServlet;

public class DisplayChartTag extends BodyTagSupport {
	
	/**
	 * Serialized ID
	 */
	private static final long serialVersionUID = 1027317526568196572L;
	
	/**
	 * Log
	 */
	private final Log log = LogFactory.getLog(DisplayChartTag.class);
	
	/**
	 * Tag properties
	 */
	private JFreeChart chart;
	
	private Integer height = 300; // Default value = 300.  Should be set in config properties.
	
	private Integer width = 500; // Default value = 500.  Should be set in config properties.
	
	/**
	 * Render graph.
	 *
	 * @return return result code
	 */
	public int doStartTag() throws JspException {
		if (chart != null) {
			try {
				HttpSession session = pageContext.getSession();
				HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
				
				Long time = System.currentTimeMillis();
				Double random = Math.random() * 1000.0;
				String key = "chart-" + time + "-" + session.getId() + "-" + random;
				session.setAttribute(key, chart);
				
				pageContext.getOut().write(
				    "<img src=\"" + request.getContextPath() + "/" + DisplayChartServlet.SERVLET_NAME + "?"
				            + DisplayChartServlet.CHART_KEY + "=" + key + "&mimeType=" + AbstractGraphServlet.PNG_MIME_TYPE
				            + "&width=" + width + "&height=" + height + "\" />");
				
			}
			catch (IOException e) {
				log.error("Unable to generate chart servlet url", e);
			}
		}
		
		return EVAL_BODY_BUFFERED;
	}
	
	/**
	 * @see javax.servlet.jsp.tagext.Tag#doEndTag()
	 */
	public int doEndTag() throws JspException {
		try {
			if (bodyContent != null) {
				bodyContent.writeOut(bodyContent.getEnclosingWriter());
			}
		}
		catch (java.io.IOException e) {
			throw new JspTagException("IO Error: " + e.getMessage());
		}
		return EVAL_PAGE;
	}
	
	/**
	 * @return the chart
	 */
	public JFreeChart getChart() {
		return chart;
	}
	
	/**
	 * @param chart the chart to set
	 */
	public void setChart(JFreeChart chart) {
		this.chart = chart;
	}
	
	/**
	 * @return the desired height of the image
	 */
	public Integer getHeight() {
		return this.height;
	}
	
	/**
	 * @param height the desired height of the image
	 */
	public void setHeight(Integer height) {
		this.height = height;
	}
	
	/**
	 * @return the desired height of the image
	 */
	public Integer getWidth() {
		return this.width;
	}
	
	/**
	 * @param width the desired height of the image
	 */
	public void setWidth(Integer width) {
		this.width = width;
	}
}
