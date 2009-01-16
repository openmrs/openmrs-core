/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.servlet;

import java.awt.Font;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;

public class ShowGraphServlet extends HttpServlet {
	
	public static final long serialVersionUID = 1231231L;
	
	private Log log = LogFactory.getLog(ShowGraphServlet.class);
	
	//private static final DateFormat Formatter = new SimpleDateFormat("MM/dd/yyyy");
	
	// Supported mime types
	private static final String PNG_MIME_TYPE = "image/png";
	
	private static final String JPG_MIME_TYPE = "image/jpeg";
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try {
			// TODO (jmiranda) Need better error handling
			Integer patientId = Integer.parseInt(request.getParameter("patientId"));
			Integer conceptId = Integer.parseInt(request.getParameter("conceptId"));
			Integer width = request.getParameter("width") != null ? Integer.parseInt(request.getParameter("width"))
			        : new Integer(500);
			Integer height = request.getParameter("width") != null ? Integer.parseInt(request.getParameter("height"))
			        : new Integer(300);
			String mimeType = request.getParameter("mimeType") != null ? request.getParameter("mimeType") : PNG_MIME_TYPE;
			
			boolean userSpecifiedMaxRange = request.getParameter("maxRange") != null;
			boolean userSpecifiedMinRange = request.getParameter("minRange") != null;
			double maxRange = request.getParameter("maxRange") != null ? Double
			        .parseDouble(request.getParameter("maxRange")) : 0.0;
			double minRange = request.getParameter("minRange") != null ? Double
			        .parseDouble(request.getParameter("minRange")) : 0.0;
			
			Patient patient = Context.getPatientService().getPatient(patientId);
			Concept concept = Context.getConceptService().getConcept(conceptId);
			
			Set<Obs> observations = new HashSet<Obs>();
			String chartTitle, rangeAxisTitle, domainAxisTitle, titleFontSize = "";
			if (concept != null) {
				// Get observations
				observations = Context.getObsService().getObservations(patient, concept, false);
				chartTitle = concept.getName(request.getLocale()).getName();
				rangeAxisTitle = chartTitle;
			} else {
				chartTitle = "Concept " + conceptId + " not found";
				rangeAxisTitle = "Value";
				
			}
			domainAxisTitle = "Date";
			
			// Create data set
			TimeSeries series = new TimeSeries(rangeAxisTitle, Day.class);
			TimeSeriesCollection dataset = new TimeSeriesCollection();
			Calendar calendar = Calendar.getInstance();
			for (Obs obs : observations) {
				if (obs.getValueNumeric() != null) { // Shouldn't be needed but just in case
					calendar.setTime(obs.getObsDatetime());
					log.debug("Adding value: " + obs.getValueNumeric() + " for " + calendar.get(Calendar.MONTH) + "/"
					        + calendar.get(Calendar.YEAR));
					
					// Set range
					//if (obs.getValueNumeric().doubleValue() < minRange) 
					//	minRange = obs.getValueNumeric().doubleValue();
					
					//if (obs.getValueNumeric().doubleValue() > maxRange) 
					//	maxRange = obs.getValueNumeric().doubleValue();
					
					// Add data point to series
					Day day = new Day(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, // January = 0 
					        calendar.get(Calendar.YEAR));
					series.addOrUpdate(day, obs.getValueNumeric());
				}
			}
			// Add series to dataset
			dataset.addSeries(series);
			
			JFreeChart chart = ChartFactory.createTimeSeriesChart(chartTitle, null, null, dataset, false, false, false);
			
			// Customize title font
			Font font = new Font("Arial", Font.BOLD, 12);
			TextTitle title = chart.getTitle();
			title.setFont(font);
			chart.setTitle(title);
			
			// Customize the plot (range and domain axes)
			XYPlot plot = (XYPlot) chart.getPlot();
			plot.setNoDataMessage("No Data Available");
			// Add filled data points
			XYItemRenderer r = plot.getRenderer();
			if (r instanceof XYLineAndShapeRenderer) {
				XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
				
				renderer.setBaseShapesFilled(true);
				renderer.setBaseShapesVisible(true);
				
				// Only works with image maps (requires some work to support) 
				/*
				StandardXYToolTipGenerator g = new StandardXYToolTipGenerator(
				    StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
				    new SimpleDateFormat("MMM-yy"), 
				    new DecimalFormat("0.0")
				);
				renderer.setToolTipGenerator(g);
				*/
			}
			
			// Modify x-axis (datetime)
			DateAxis axis = (DateAxis) plot.getDomainAxis();
			axis.setDateFormatOverride(new SimpleDateFormat("MMM-yy"));
			
			// Set y-axis range (values)
			NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			
			rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			
			if (userSpecifiedMinRange) {
				minRange = (rangeAxis.getLowerBound() < minRange) ? rangeAxis.getLowerBound() : minRange;
			}
			
			if (userSpecifiedMaxRange) { // otherwise we just use default range
				maxRange = (rangeAxis.getUpperBound() > maxRange) ? rangeAxis.getUpperBound() : maxRange;
				//maxRange = maxRange + ((maxRange - minRange) * 0.1);	// add a buffer to the max
			}
			rangeAxis.setRange(minRange, maxRange);
			
			// Modify response to disable caching
			response.setHeader("Pragma", "No-cache");
			response.setDateHeader("Expires", 0);
			response.setHeader("Cache-Control", "no-cache");
			
			// Write chart out to response as image 
			try {
				if (JPG_MIME_TYPE.equalsIgnoreCase(mimeType)) {
					response.setContentType(JPG_MIME_TYPE);
					ChartUtilities.writeChartAsJPEG(response.getOutputStream(), chart, width, height);
				} else if (PNG_MIME_TYPE.equalsIgnoreCase(mimeType)) {
					response.setContentType(PNG_MIME_TYPE);
					ChartUtilities.writeChartAsPNG(response.getOutputStream(), chart, width, height);
				} else {
					// Throw exception: unsupported mime type
				}
			}
			catch (IOException e) {
				// if its tomcat and the user simply navigated away from the page, don't throw an error
				if (e.getClass().getName().equals("ClientAbortException")) {
					// do nothing
				}
				else {
					log.error("Unable to write chart", e);
				}
			}
			
		}
		// Add error handling above and remove this try/catch 
		catch (Exception e) {
			log.error("An unknown expected exception was thrown while rendering a graph", e);
		}
	}
	
	/**
	 * 
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
}
