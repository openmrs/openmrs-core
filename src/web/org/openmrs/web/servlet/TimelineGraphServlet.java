package org.openmrs.web.servlet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

import org.jfree.chart.axis.NumberAxis;

import org.jfree.chart.axis.DateAxis;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.openmrs.reporting.DataTable;
import org.openmrs.reporting.TableRow;

/**
 * Servlet for rendering a graph of values over time. Accepts the following request parameters:
 * width:  Width of the generated image
 * height: Height of the generated image
 * mimeType: Accepts either image/png or image/jpeg
 * chartTitle: The title of the graph
 * rangeAxisTitle: The y-axis title
 * domainAxisTitle: The x-axis title
 * minRange: The minimum value for y-axis values
 * maxRange: The maximum value for y-axis values
 */
public class TimelineGraphServlet extends AbstractGraphServlet {

	public static final long serialVersionUID = 1231231L;
	private Log log = LogFactory.getLog(TimelineGraphServlet.class);

	protected JFreeChart createChart(HttpServletRequest request, HttpServletResponse response) {

		DataTable hivEnrollmentTable = (DataTable)request.getSession().getAttribute("hivEnrollmentDataTable");
		
		String chartTitle = request.getParameter("chartTitle") == null ? "" : request.getParameter("chartTitle");
		String rangeAxisTitle = request.getParameter("rangeAxisTitle") == null ? "" : request.getParameter("rangeAxisTitle");
		String domainAxisTitle = request.getParameter("domainAxisTitle") == null ? "" : request.getParameter("domainAxisTitle");
		
		// Create data set
		TimeSeries series = new TimeSeries(rangeAxisTitle, Day.class);
		TimeSeriesCollection dataset = new TimeSeriesCollection();

		ArrayList<TableRow> hivEnrollmentRows = hivEnrollmentTable.getRows();
		
		for (TableRow row : hivEnrollmentRows) {
			try {
				// Add data point to series
				Day day = Day.parseDay("" + row.get("hiv_enrollment_date") + "-01");
				Double dateCount = Double.valueOf("" + row.get("count"));
				log.debug("Adding value: " + dateCount + " for " + day );
				if (day != null) {
					series.addOrUpdate(day, dateCount);
				}
			} catch (Exception e) {
				log.error(e);
			}
		}
		
		// Add series to dataset
		dataset.addSeries(series);
		// Create graph
		JFreeChart chart = ChartFactory.createTimeSeriesChart(chartTitle, domainAxisTitle, rangeAxisTitle, dataset, true, true, false);
		
		// Customize the plot (range and domain axes)
        XYPlot plot = (XYPlot) chart.getPlot();		        
	        // Add filled data points
		XYItemRenderer r = plot.getRenderer();
		if (r instanceof XYLineAndShapeRenderer) {
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
			renderer.setBaseShapesFilled(true);
			renderer.setBaseShapesVisible(true);
		}		
        
        // Modify x-axis (datetime)
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MMM-yy"));
	        // Set y-axis range (values)
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
		double minRange = rangeAxis.getLowerBound();
		double maxRange = rangeAxis.getUpperBound();
		
		try {
			double userMinRange = Double.parseDouble(request.getParameter("minRange"));
			minRange = ( userMinRange < minRange ) ? userMinRange : minRange;
		} catch (Exception e) {}
		
		try {
			double userMaxRange = Double.parseDouble(request.getParameter("maxRange"));
			maxRange = ( userMaxRange < maxRange ) ? userMaxRange : maxRange;
		} catch (Exception e) {}
		
		rangeAxis.setRange(minRange, maxRange);
		
		return chart;
	}
}

	



