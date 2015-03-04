/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class ConceptStatsFormController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#processFormSubmission(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object object,
	        BindException errors) throws Exception {
		
		Concept concept = (Concept) object;
		ConceptService cs = Context.getConceptService();
		
		// check to see if they clicked next/previous concept:
		String jumpAction = request.getParameter("jumpAction");
		if (jumpAction != null) {
			Concept newConcept = null;
			if ("previous".equals(jumpAction)) {
				newConcept = cs.getPrevConcept(concept);
			} else if ("next".equals(jumpAction)) {
				newConcept = cs.getNextConcept(concept);
			}
			
			if (newConcept != null) {
				return new ModelAndView(new RedirectView(getSuccessView() + "?conceptId=" + newConcept.getConceptId()));
			}
			
		}
		
		return new ModelAndView(new RedirectView(getSuccessView()));
	}
	
	/**
	 * This is called prior to displaying a form for the first time. It tells Spring the
	 * form/command object to load into the request
	 *
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		
		Concept concept = null;
		
		ConceptService cs = Context.getConceptService();
		String conceptId = request.getParameter("conceptId");
		if (conceptId != null) {
			concept = cs.getConcept(Integer.valueOf(conceptId));
		}
		
		if (concept == null) {
			concept = new Concept();
		}
		
		return concept;
	}
	
	/**
	 * Called prior to form display. Allows for data to be put in the request to be used in the view
	 *
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
	 */
	protected Map<String, Object> referenceData(HttpServletRequest request) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		if (!Context.hasPrivilege("View Observations")) {
			return map;
		}
		
		MessageSourceAccessor msa = getMessageSourceAccessor();
		Locale locale = Context.getLocale();
		ConceptService cs = Context.getConceptService();
		String conceptId = request.getParameter("conceptId");
		//List<Obs> obs = new Vector<Obs>();
		//List<Obs> obsAnswered = new Vector<Obs>();
		
		if (conceptId != null) {
			Concept concept = cs.getConcept(Integer.valueOf(conceptId));
			ObsService obsService = Context.getObsService();
			
			if (concept != null) {
				
				// previous/next ids for links
				map.put("previousConcept", cs.getPrevConcept(concept));
				map.put("nextConcept", cs.getNextConcept(concept));
				
				//obs = obsService.getObservations(concept, "valueNumeric, obsId");
				//obsAnswered = obsService.getObservationsAnsweredByConcept(concept);
				
				if (ConceptDatatype.NUMERIC.equals(concept.getDatatype().getHl7Abbreviation())) {
					map.put("displayType", "numeric");
					
					List<Obs> numericAnswers = obsService.getObservations(null, null, Collections.singletonList(concept),
					    null, Collections.singletonList(OpenmrsConstants.PERSON_TYPE.PERSON), null, Collections
					            .singletonList("valueNumeric"), null, null, null, null, false);
					
					if (numericAnswers.size() > 0) {
						Double min = numericAnswers.get(0).getValueNumeric();
						Double max = (Double) numericAnswers.get(numericAnswers.size() - 1).getValueNumeric();
						Double median = (Double) numericAnswers.get(numericAnswers.size() / 2).getValueNumeric();
						
						Map<Double, Integer> counts = new HashMap<Double, Integer>(); // counts for the histogram
						Double total = 0.0; // sum of values. used for mean
						
						// dataset setup for lineChart
						TimeSeries timeSeries = new TimeSeries(concept.getName().getName(), Day.class);
						TimeSeriesCollection timeDataset = new TimeSeriesCollection();
						Calendar calendar = Calendar.getInstance();
						
						// array for histogram
						double[] obsNumerics = new double[(numericAnswers.size())];
						
						Integer i = 0;
						for (Obs obs : numericAnswers) {
							Date date = (Date) obs.getObsDatetime();
							Double value = (Double) obs.getValueNumeric();
							
							// for mean calculation
							total += value;
							
							// for histogram
							obsNumerics[i++] = value;
							Integer count = counts.get(value);
							counts.put(value, count == null ? 1 : count + 1);
							
							// for line chart
							calendar.setTime(date);
							Day day = new Day(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, // January = 0 
							        calendar.get(Calendar.YEAR) < 1900 ? 1900 : calendar.get(Calendar.YEAR) // jfree chart doesn't like the 19th century
							);
							timeSeries.addOrUpdate(day, value);
						}
						
						Double size = new Double(numericAnswers.size());
						Double mean = total / size;
						
						map.put("size", numericAnswers.size());
						map.put("min", min);
						map.put("max", max);
						map.put("mean", mean);
						map.put("median", median);
						
						// create histogram chart
						HistogramDataset histDataset = new HistogramDataset(); // dataset for histogram
						histDataset.addSeries(concept.getName().getName(), obsNumerics, counts.size());
						
						JFreeChart histogram = ChartFactory.createHistogram(concept.getName().getName(), msa
						        .getMessage("Concept.stats.histogramDomainAxisTitle"), msa
						        .getMessage("Concept.stats.histogramRangeAxisTitle"), histDataset, PlotOrientation.VERTICAL,
						    false, true, false);
						map.put("histogram", histogram);
						
						if (size > 25) {
							// calculate 98th percentile of the data:
							Double x = 0.98;
							Integer xpercentile = (int) (x * size);
							Double upperQuartile = numericAnswers.get(xpercentile).getValueNumeric();
							Double lowerQuartile = numericAnswers.get((int) (size - xpercentile)).getValueNumeric();
							Double innerQuartile = upperQuartile - lowerQuartile;
							Double innerQuartileLimit = innerQuartile * 1.5; // outliers will be greater than this from the upper/lower quartile
							Double upperQuartileLimit = upperQuartile + innerQuartileLimit;
							Double lowerQuartileLimit = lowerQuartile - innerQuartileLimit;
							
							List<Obs> outliers = new Vector<Obs>();
							
							// move outliers to the outliers list
							// removing lower quartile outliers
							for (i = 0; i < size - xpercentile; i++) {
								Obs possibleOutlier = numericAnswers.get(i);
								if (possibleOutlier.getValueNumeric() >= lowerQuartileLimit) {
									break; // quit if this value is greater than the lower limit
								}
								outliers.add(possibleOutlier);
							}
							
							// removing upper quartile outliers
							for (i = size.intValue() - 1; i >= xpercentile; i--) {
								Obs possibleOutlier = numericAnswers.get(i);
								if (possibleOutlier.getValueNumeric() <= upperQuartileLimit) {
									break; // quit if this value is less than the upper limit
								}
								outliers.add(possibleOutlier);
							}
							numericAnswers.removeAll(outliers);
							
							double[] obsNumericsOutliers = new double[(numericAnswers.size())];
							i = 0;
							counts.clear();
							for (Obs values : numericAnswers) {
								Double value = values.getValueNumeric();
								obsNumericsOutliers[i++] = value;
								Integer count = counts.get(value);
								counts.put(value, count == null ? 1 : count + 1);
							}
							
							// create outlier histogram chart
							HistogramDataset outlierHistDataset = new HistogramDataset();
							outlierHistDataset.addSeries(concept.getName().getName(), obsNumericsOutliers, counts.size());
							
							JFreeChart histogramOutliers = ChartFactory.createHistogram(concept.getName().getName(), msa
							        .getMessage("Concept.stats.histogramDomainAxisTitle"), msa
							        .getMessage("Concept.stats.histogramRangeAxisTitle"), outlierHistDataset,
							    PlotOrientation.VERTICAL, false, true, false);
							map.put("histogramOutliers", histogramOutliers);
							map.put("outliers", outliers);
							
						}
						
						// create line graph chart
						timeDataset.addSeries(timeSeries);
						JFreeChart lineChart = ChartFactory.createTimeSeriesChart(concept.getName().getName(), msa
						        .getMessage("Concept.stats.lineChartDomainAxisLabel"), msa
						        .getMessage("Concept.stats.lineChartRangeAxisLabel"), timeDataset, false, true, false);
						map.put("timeSeries", lineChart);
						
					}
				} else if (ConceptDatatype.BOOLEAN.equals(concept.getDatatype().getHl7Abbreviation())) {
					// create bar chart for boolean answers
					map.put("displayType", "boolean");
					
					List<Obs> obs = obsService.getObservations(null, null, Collections.singletonList(concept), null,
					    Collections.singletonList(OpenmrsConstants.PERSON_TYPE.PERSON), null, null, null, null, null, null,
					    false);
					
					DefaultPieDataset pieDataset = new DefaultPieDataset();
					
					// count the number of unique answers
					Map<String, Integer> counts = new HashMap<String, Integer>();
					for (Obs o : obs) {
						Boolean answer = o.getValueAsBoolean();
						if (answer == null) {
							answer = false;
						}
						String name = answer.toString();
						Integer count = counts.get(name);
						counts.put(name, count == null ? 1 : count + 1);
					}
					
					// put the counts into the dataset
					for (Map.Entry<String, Integer> entry : counts.entrySet()) {
						pieDataset.setValue(entry.getKey(), entry.getValue());
					}
					
					JFreeChart pieChart = ChartFactory.createPieChart(concept.getName().getName(), pieDataset, true, true,
					    false);
					map.put("pieChart", pieChart);
					
				} else if (ConceptDatatype.CODED.equals(concept.getDatatype().getHl7Abbreviation())) {
					// create pie graph for coded answers
					map.put("displayType", "coded");
					
					List<Obs> obs = obsService.getObservations(null, null, Collections.singletonList(concept), null,
					    Collections.singletonList(OpenmrsConstants.PERSON_TYPE.PERSON), null, null, null, null, null, null,
					    false);
					
					DefaultPieDataset pieDataset = new DefaultPieDataset();
					
					// count the number of unique answers
					Map<String, Integer> counts = new HashMap<String, Integer>();
					for (Obs o : obs) {
						Concept value = o.getValueCoded();
						String name;
						if (value == null) {
							name = "[value_coded is null]";
						} else {
							name = value.getName().getName();
						}
						Integer count = counts.get(name);
						counts.put(name, count == null ? 1 : count + 1);
					}
					
					// put the counts into the dataset
					for (Map.Entry<String, Integer> entry : counts.entrySet()) {
						pieDataset.setValue(entry.getKey(), entry.getValue());
					}
					
					JFreeChart pieChart = ChartFactory.createPieChart(concept.getName().getName(), pieDataset, true, true,
					    false);
					map.put("pieChart", pieChart);
					
				}
			}
			
		}
		
		//map.put("obs", obs);
		//map.put("obsAnswered", obsAnswered);
		
		map.put("locale", locale.getLanguage().substring(0, 2));
		
		return map;
	}
	
}
