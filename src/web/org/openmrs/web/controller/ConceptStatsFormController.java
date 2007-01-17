package org.openmrs.web.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class ConceptStatsFormController extends SimpleFormController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * This is called prior to displaying a form for the first time. It tells
	 * Spring the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request)
			throws ServletException {

		Concept concept = null;

		ConceptService cs = Context.getConceptService();
		String conceptId = request.getParameter("conceptId");
		if (conceptId != null) {
			concept = cs.getConcept(Integer.valueOf(conceptId));
		}

		if (concept == null)
			concept = new Concept();

		return concept;
	}

	/**
	 * Called prior to form display. Allows for data to be put in the request to
	 * be used in the view
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
	 */
	protected Map referenceData(HttpServletRequest request) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		if (!Context.hasPrivilege("View Observations"))
			return map;
		
		MessageSourceAccessor msa = getMessageSourceAccessor();
		Locale locale = Context.getLocale();
		ConceptService cs = Context.getConceptService();
		String conceptId = request.getParameter("conceptId");
		//List<Obs> obs = new Vector<Obs>();
		//List<Obs> obsAnswered = new Vector<Obs>();
		
		if (conceptId != null) {
			Concept concept = cs.getConcept(Integer.valueOf(conceptId));

			if (concept != null) {
				
				// previous/next ids for links
				map.put("previousConcept", cs.getPrevConcept(concept));
				map.put("nextConcept", cs.getNextConcept(concept));

				ObsService obsService = Context.getObsService();
				
				//obs = obsService.getObservations(concept, "valueNumeric, obsId");
				//obsAnswered = obsService.getObservationsAnsweredByConcept(concept);
				
				// Object[obsDatetime, valueNumeric] 
				List<Object[]> numericAnswers = obsService.getNumericAnswersForConcept(concept, true);
				
				if (numericAnswers.size() > 0) {
					Double min = (Double)numericAnswers.get(0)[1];
					Double max = (Double)numericAnswers.get(numericAnswers.size()-1)[1];
					Double median = (Double)numericAnswers.get(numericAnswers.size() / 2)[1];
					
					Map<Double, Integer> counts = new HashMap<Double, Integer>(); // counts for the histogram
					Double total = 0.0; // sum of values. used for mean
					
					// dataset setup for lineChart
					TimeSeries timeSeries = new TimeSeries(concept.getName().getName(), Day.class);
					TimeSeriesCollection timeDataset = new TimeSeriesCollection();
					Calendar calendar = Calendar.getInstance();
					
					// array for histogram
					double[] obsNumerics = new double[(numericAnswers.size())];
					
					Integer x = 0;
					for (Object[] values : numericAnswers) {
						Date date = (Date)values[0];
						Double value = (Double)values[1];
						
						// for mean calculation
						total += value;
						
						// for histogram
						obsNumerics[x++] = value;
						Integer count = counts.get(value);
						counts.put(value, count == null ? 1 : count + 1);
						
						// for line chart
						calendar.setTime(date);
						Day day = new Day(
							calendar.get(Calendar.DAY_OF_MONTH),
							calendar.get(Calendar.MONTH)+1,			// January = 0 
							calendar.get(Calendar.YEAR)
						);
						timeSeries.addOrUpdate(day, value);
					}
					
					Double mean = total / new Double(numericAnswers.size());
					
					map.put("obsNumerics", numericAnswers);
					map.put("min", min);
					map.put("max", max);
					map.put("mean", mean);
					map.put("median", median);
					
					// create histogram chart
					HistogramDataset histDataset = new HistogramDataset(); // dataset for histogram
					histDataset.addSeries(concept.getName().getName(), obsNumerics, counts.size());
					
					JFreeChart histogram = ChartFactory.createHistogram(
							concept.getName().getName(),
							msa.getMessage("Concept.stats.histogramDomainAxisTitle"),
							msa.getMessage("Concept.stats.histogramRangeAxisTitle"),
							histDataset,
							PlotOrientation.VERTICAL,
							false, 
							true, 
							false
						);
					map.put("histogram", histogram);
					
					// create line graph chart
					timeDataset.addSeries(timeSeries);
					JFreeChart lineChart = ChartFactory.createTimeSeriesChart(
							concept.getName().getName(),
							msa.getMessage("Concept.stats.lineChartDomainAxisLabel"),
							msa.getMessage("Concept.stats.histogramRangeAxisLabel"),
							timeDataset,
							false, 
							true, 
							false
						);
					map.put("lineChart", lineChart);
					
				}
				
			}
			
		}
		
		//map.put("obs", obs);
		//map.put("obsAnswered", obsAnswered);
		
		map.put("locale", locale.getLanguage().substring(0, 2));

		return map;
	}

}
