/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.servlet;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;

/**
 * This servlet returns an image graphing the numeric values for given concept(s). <br/>
 * <br/>
 * This servlet is currently mapped to a /showGraphServlet url in web.xml<br/>
 * <br/>
 * For an example of usage, see WEB-INF/view/portlets/patientGraphs.jsp <br/>
 * <br/>
 * The only url parameters that are required are "patientId" and "conceptId".
 */
public class ShowGraphServlet extends HttpServlet {
	
	public static final long serialVersionUID = 1231231L;
	
	private Log log = LogFactory.getLog(ShowGraphServlet.class);
	
	// private static final DateFormat Formatter = new SimpleDateFormat("MM/dd/yyyy");
	
	// Supported mime types
	private static final String PNG_MIME_TYPE = "image/png";
	
	private static final String JPG_MIME_TYPE = "image/jpeg";
	
	private static final Color COLOR_ABNORMAL = new Color(255, 255, 0, 64);
	
	private static final Color COLOR_CRITICAL = new Color(255, 128, 128, 64);
	
	private static final Color COLOR_ERROR = new Color(255, 28, 28, 64);
	
	/**
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			JFreeChart chart = getChart(request);
			
			// get the height and width of the graph
			String widthString = request.getParameter("width");
			String heightString = request.getParameter("height");
			
			Integer width;
			Integer height;
			if (widthString != null && widthString.length() > 0) {
				width = Integer.parseInt(widthString);
			} else {
				width = 500;
			}
			if (heightString != null && heightString.length() > 0) {
				height = Integer.parseInt(heightString);
			} else {
				height = 300;
			}
			
			// get the requested mime type of the graph
			String mimeType = request.getParameter("mimeType");
			if (mimeType == null) {
				mimeType = PNG_MIME_TYPE;
			}
			
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
					throw new APIException("unsupported.mime.type", (Object[]) null);
				}
			}
			catch (IOException e) {
				// if its tomcat and the user simply navigated away from the page, don't throw an error
				if (e.getClass().getName().equals("org.apache.catalina.connector.ClientAbortException")) {
					// do nothing
				} else {
					log.error("Error class name: " + e.getClass().getName());
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
	 * The main method for this class. It will create a JFreeChart object to be written to the
	 * response.
	 *
	 * @param request the current request will all the parameters needed
	 * @return JFreeChart object to be rendered
	 * @should set value axis label to given units
	 * @should set value axis label to concept numeric units if given units is null
	 */
	protected JFreeChart getChart(HttpServletRequest request) {
		// All available GET parameters
		String patientId = request.getParameter("patientId"); // required
		String conceptId1 = request.getParameter("conceptId"); // required
		String conceptId2 = request.getParameter("conceptId2");
		String chartTitle = request.getParameter("chartTitle");
		String units = request.getParameter("units");
		
		String minRangeString = request.getParameter("minRange");
		String maxRangeString = request.getParameter("maxRange");
		
		String hideDate = request.getParameter("hideDate");
		
		Patient patient = Context.getPatientService().getPatient(Integer.parseInt(patientId));
		
		// Set date range to passed values, otherwise set a default date range to the last 12 months
		Calendar cal = Calendar.getInstance();
		Date fromDate = getFromDate(request.getParameter("fromDate"));
		Date toDate = getToDate(request.getParameter("toDate"));
		
		// Swap if fromDate is after toDate
		if (fromDate.getTime() > toDate.getTime()) {
			Long temp = fromDate.getTime();
			fromDate.setTime(toDate.getTime());
			toDate.setTime(temp);
		}
		
		// Graph parameters
		Double minRange = null;
		Double maxRange = null;
		Double normalLow = null;
		Double normalHigh = null;
		Double criticalLow = null;
		Double criticalHigh = null;
		String timeAxisTitle = null;
		String rangeAxisTitle = null;
		boolean userSpecifiedMaxRange = false;
		boolean userSpecifiedMinRange = false;
		
		// Fetching obs
		List<Obs> observations1 = new ArrayList<Obs>();
		List<Obs> observations2 = new ArrayList<Obs>();
		Concept concept1 = null, concept2 = null;
		if (conceptId1 != null) {
			concept1 = Context.getConceptService().getConcept(Integer.parseInt(conceptId1));
		}
		if (conceptId2 != null) {
			concept2 = Context.getConceptService().getConcept(Integer.parseInt(conceptId2));
		}
		if (concept1 != null) {
			observations1 = Context.getObsService().getObservationsByPersonAndConcept(patient, concept1);
			chartTitle = concept1.getName().getName();
			rangeAxisTitle = ((ConceptNumeric) concept1).getUnits();
			minRange = ((ConceptNumeric) concept1).getLowAbsolute();
			maxRange = ((ConceptNumeric) concept1).getHiAbsolute();
			normalLow = ((ConceptNumeric) concept1).getLowNormal();
			normalHigh = ((ConceptNumeric) concept1).getHiNormal();
			criticalLow = ((ConceptNumeric) concept1).getLowCritical();
			criticalHigh = ((ConceptNumeric) concept1).getHiCritical();
			
			// Only get observations2 if both concepts share the same units; update chart title and ranges
			if (concept2 != null) {
				String concept2Units = ((ConceptNumeric) concept2).getUnits();
				if (concept2Units != null && concept2Units.equals(rangeAxisTitle)) {
					observations2 = Context.getObsService().getObservationsByPersonAndConcept(patient, concept2);
					chartTitle += " + " + concept2.getName().getName();
					if (((ConceptNumeric) concept2).getHiAbsolute() != null
					        && ((ConceptNumeric) concept2).getHiAbsolute() > maxRange) {
						maxRange = ((ConceptNumeric) concept2).getHiAbsolute();
					}
					if (((ConceptNumeric) concept2).getLowAbsolute() != null
					        && ((ConceptNumeric) concept2).getLowAbsolute() < minRange) {
						minRange = ((ConceptNumeric) concept2).getLowAbsolute();
					}
				} else {
					log.warn("Units for concept id: " + conceptId2 + " don't match units for concept id: " + conceptId1
					        + ". Only displaying " + conceptId1);
					concept2 = null; // nullify concept2 so that the legend isn't shown later
				}
			}
		} else {
			chartTitle = "Concept " + conceptId1 + " not found";
			rangeAxisTitle = "Value";
		}
		
		// Overwrite with user-specified values, otherwise use default values
		if (units != null && units.length() > 0) {
			rangeAxisTitle = units;
		}
		if (minRangeString != null) {
			minRange = Double.parseDouble(minRangeString);
			userSpecifiedMinRange = true;
		}
		if (maxRangeString != null) {
			maxRange = Double.parseDouble(maxRangeString);
			userSpecifiedMaxRange = true;
		}
		if (chartTitle == null) {
			chartTitle = "";
		}
		if (rangeAxisTitle == null) {
			rangeAxisTitle = "";
		}
		if (minRange == null) {
			minRange = 0.0;
		}
		if (maxRange == null) {
			maxRange = 200.0;
		}
		
		// Create data set
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		TimeSeries series1, series2;
		
		// Interval-dependent units
		Class<? extends RegularTimePeriod> timeScale = null;
		if (toDate.getTime() - fromDate.getTime() <= 86400000) {
			// Interval <= 1 day: minutely
			timeScale = Minute.class;
			timeAxisTitle = "Time";
		} else if (toDate.getTime() - fromDate.getTime() <= 259200000) {
			// Interval <= 3 days: hourly
			timeScale = Hour.class;
			timeAxisTitle = "Time";
		} else {
			timeScale = Day.class;
			timeAxisTitle = "Date";
		}
		if (concept1 == null) {
			series1 = new TimeSeries("NULL", Hour.class);
		} else {
			series1 = new TimeSeries(concept1.getName().getName(), timeScale);
		}
		if (concept2 == null) {
			series2 = new TimeSeries("NULL", Hour.class);
		} else {
			series2 = new TimeSeries(concept2.getName().getName(), timeScale);
		}
		
		// Add data points for concept1
		for (Obs obs : observations1) {
			if (obs.getValueNumeric() != null && obs.getObsDatetime().getTime() >= fromDate.getTime()
			        && obs.getObsDatetime().getTime() < toDate.getTime()) {
				cal.setTime(obs.getObsDatetime());
				if (timeScale == Minute.class) {
					Minute min = new Minute(cal.get(Calendar.MINUTE), cal.get(Calendar.HOUR_OF_DAY), cal
					        .get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
					series1.addOrUpdate(min, obs.getValueNumeric());
				} else if (timeScale == Hour.class) {
					Hour hour = new Hour(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.DAY_OF_MONTH), cal
					        .get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
					series1.addOrUpdate(hour, obs.getValueNumeric());
				} else {
					Day day = new Day(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
					series1.addOrUpdate(day, obs.getValueNumeric());
				}
			}
		}
		
		// Add data points for concept2
		for (Obs obs : observations2) {
			if (obs.getValueNumeric() != null && obs.getObsDatetime().getTime() >= fromDate.getTime()
			        && obs.getObsDatetime().getTime() < toDate.getTime()) {
				cal.setTime(obs.getObsDatetime());
				if (timeScale == Minute.class) {
					Minute min = new Minute(cal.get(Calendar.MINUTE), cal.get(Calendar.HOUR_OF_DAY), cal
					        .get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
					series2.addOrUpdate(min, obs.getValueNumeric());
				} else if (timeScale == Hour.class) {
					Hour hour = new Hour(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.DAY_OF_MONTH), cal
					        .get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
					series2.addOrUpdate(hour, obs.getValueNumeric());
				} else {
					Day day = new Day(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
					series2.addOrUpdate(day, obs.getValueNumeric());
				}
			}
		}
		
		// Add series to dataset
		dataset.addSeries(series1);
		if (!series2.isEmpty()) {
			dataset.addSeries(series2);
		}
		
		// As of JFreeChart 1.0.11 the default background color is dark grey instead of white.
		// This line restores the original white background.
		ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
		
		JFreeChart chart = null;
		
		// Show legend only if more than one series
		if (concept2 == null) {
			chart = ChartFactory.createTimeSeriesChart(chartTitle, timeAxisTitle, rangeAxisTitle, dataset, false, false,
			    false);
		} else {
			chart = ChartFactory.createTimeSeriesChart(chartTitle, timeAxisTitle, rangeAxisTitle, dataset, true, false,
			    false);
		}
		
		// Customize title font
		Font font = new Font("Arial", Font.BOLD, 12);
		TextTitle title = chart.getTitle();
		title.setFont(font);
		chart.setTitle(title);
		
		// Add subtitle, unless 'hideDate' has been passed
		if (hideDate == null) {
			TextTitle subtitle = new TextTitle(fromDate.toString() + " - " + toDate.toString());
			subtitle.setFont(font);
			chart.addSubtitle(subtitle);
		}
		
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setNoDataMessage("No Data Available");
		
		// Add abnormal/critical range background color (only for single-concept graphs)
		if (concept2 == null) {
			IntervalMarker abnormalLow, abnormalHigh, critical;
			if (normalHigh != null) {
				abnormalHigh = new IntervalMarker(normalHigh, maxRange, COLOR_ABNORMAL);
				plot.addRangeMarker(abnormalHigh);
			}
			if (normalLow != null) {
				abnormalLow = new IntervalMarker(minRange, normalLow, COLOR_ABNORMAL);
				plot.addRangeMarker(abnormalLow);
			}
			if (criticalHigh != null) {
				critical = new IntervalMarker(criticalHigh, maxRange, COLOR_CRITICAL);
				plot.addRangeMarker(critical);
			}
			if (criticalLow != null) {
				critical = new IntervalMarker(minRange, criticalLow, COLOR_CRITICAL);
				plot.addRangeMarker(critical);
			}
			
			// there is data outside of the absolute lower limits for this concept (or of what the user specified as minrange)
			if (plot.getRangeAxis().getLowerBound() < minRange) {
				IntervalMarker error = new IntervalMarker(plot.getRangeAxis().getLowerBound(), minRange, COLOR_ERROR);
				plot.addRangeMarker(error);
			}
			
			if (plot.getRangeAxis().getUpperBound() > maxRange) {
				IntervalMarker error = new IntervalMarker(maxRange, plot.getRangeAxis().getUpperBound(), COLOR_ERROR);
				plot.addRangeMarker(error);
			}
			
		}
		
		// Visuals
		XYItemRenderer r = plot.getRenderer();
		if (r instanceof XYLineAndShapeRenderer) {
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
			renderer.setBaseShapesFilled(true);
			renderer.setBaseShapesVisible(true);
		}
		
		// Customize the plot (range and domain axes)
		
		// Modify x-axis (datetime)
		DateAxis timeAxis = (DateAxis) plot.getDomainAxis();
		if (timeScale == Day.class) {
			timeAxis.setDateFormatOverride(new SimpleDateFormat("dd-MMM-yyyy"));
		}
		
		timeAxis.setRange(fromDate, toDate);
		
		// Set y-axis range (values)
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		
		if (userSpecifiedMinRange) {
			minRange = (rangeAxis.getLowerBound() < minRange) ? rangeAxis.getLowerBound() : minRange;
		}
		
		if (userSpecifiedMaxRange) {
			// otherwise we just use default range
			maxRange = (rangeAxis.getUpperBound() > maxRange) ? rangeAxis.getUpperBound() : maxRange;
		}
		
		rangeAxis.setRange(minRange, maxRange);
		
		return chart;
	}
	
	/**
	 * Get the FromDate object from the given string that is the time in milliseconds. If
	 * dateFromRequest is null, return 1 year ago from today.
	 *
	 * @param dateFromRequest String that was passed into this servlet
	 * @return Date parsed from dateFromRequest string
	 * @should return one year previous to today if parameter is null
	 * @should return same date as given string parameter
	 */
	protected Date getFromDate(String dateFromRequest) {
		Date returnedDate = new Date(); // default to right now
		
		if (dateFromRequest != null && dateFromRequest.length() > 0) {
			returnedDate.setTime(Long.parseLong(dateFromRequest));
		} else {
			Calendar cal = Calendar.getInstance();
			cal.setTime(returnedDate);
			cal.set(cal.get(Calendar.YEAR) - 1, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
			returnedDate = cal.getTime();
		}
		
		return returnedDate;
	}
	
	/**
	 * Get the toDate object from the given string that is the time in milliseconds. If
	 * dateFromRequest is null, return tomorrow's date.
	 *
	 * @param dateFromRequest String that was passed into this servlet
	 * @return Date parsed from dateFromRequest string
	 * @should return next months date if parameter is null
	 * @should return date one day after given string date
	 * @should set hour minute and second to zero
	 */
	protected Date getToDate(String dateFromRequest) {
		Calendar cal = Calendar.getInstance();
		
		Date toDate = new Date();
		
		if (dateFromRequest != null && dateFromRequest.length() > 0) {
			cal.setTimeInMillis(Long.parseLong(dateFromRequest));
		} else {
			cal.setTime(toDate);
		}
		// set +1 day so the selected toDate is fully included in the interval
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) + 1, 0, 0, 0);
		toDate = cal.getTime();
		
		return toDate;
	}
	
	/**
	 * There are no post actions. Ignore this method.
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
