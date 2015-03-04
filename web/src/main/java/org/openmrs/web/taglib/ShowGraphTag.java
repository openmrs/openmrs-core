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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;

public class ShowGraphTag extends BodyTagSupport {
	
	/**
	 * Serialized ID
	 */
	private static final long serialVersionUID = 1027317526568196571L;
	
	/**
	 * Log
	 */
	private final Log log = LogFactory.getLog(ShowGraphTag.class);
	
	/**
	 * Tag properties
	 */
	private Integer patientId;
	
	private String conceptName;
	
	private Integer height = 300; // Default value = 300.  Should be set in config properties.
	
	private Integer width = 500; // Default value = 500.  Should be set in config properties.
	
	/**
	 * Render graph.
	 *
	 * @return return result code
	 */
	public int doStartTag() throws JspException {
		
		Patient patient = Context.getPatientService().getPatient(patientId);
		Concept concept = Context.getConceptService().getConceptByName(conceptName);
		
		if (concept != null && concept.isNumeric()) {
			List<Obs> observations = Context.getObsService().getObservationsByPersonAndConcept(patient, concept);
			DefaultCategoryDataset dataset = new DefaultCategoryDataset();
			for (Obs obs : observations) {
				dataset.addValue(obs.getValueNumeric(), conceptName, obs.getObsDatetime());
			}
			JFreeChart chart = ChartFactory.createLineChart(conceptName, "Value", "Date", dataset, PlotOrientation.VERTICAL,
			    true, true, false);
			
			try {
				ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
				ChartUtilities.writeChartAsPNG(byteArray, chart, width, height);
				pageContext.getResponse().setContentType("image/png");
				pageContext.getResponse().getWriter().write(byteArray.toString());
				
			}
			catch (IOException e) {
				log.error(e);
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
	 * @return the patient primary key
	 */
	public Integer getPatientId() {
		return this.patientId;
	}
	
	/**
	 * @param patientId the primary key of the patient
	 */
	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}
	
	/**
	 * @return the concept name
	 */
	public String getConceptName() {
		return conceptName;
	}
	
	/**
	 * @param conceptName the desired concept
	 */
	public void setConceptName(String conceptName) {
		this.conceptName = conceptName;
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
