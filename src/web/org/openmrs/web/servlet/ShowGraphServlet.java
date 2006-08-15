package org.openmrs.web.servlet;


import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

import org.jfree.chart.axis.DateAxis;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;

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
			Integer width = Integer.parseInt(request.getParameter("width")!=null?request.getParameter("width"):"500");
			Integer height = Integer.parseInt(request.getParameter("width")!=null?request.getParameter("height"):"300");
			String mimeType = request.getParameter("mimeType")!=null?request.getParameter("mimeType"):PNG_MIME_TYPE;
			
			Context context = getContext(request);
			Patient patient = context.getPatientService().getPatient(patientId);
			Concept concept = context.getConceptService().getConcept(conceptId);
			
			Set<Obs> observations = new HashSet<Obs>();
			String chartTitle, yAxisTitle = "";
			if (concept != null ) { 
				// Get observations
				observations = context.getObsService().getObservations(patient, concept);				
				chartTitle = concept.getName(request.getLocale()).getName();
				yAxisTitle = chartTitle;
			}
			else { 
				chartTitle = "Concept " + conceptId + " not found";
				yAxisTitle = "Value";
			}
			
			// Create data set
			TimeSeries series = new TimeSeries(yAxisTitle, Day.class);
			TimeSeriesCollection dataset = new TimeSeriesCollection();
			Calendar calendar = Calendar.getInstance();
			for( Obs obs : observations ) { 
				calendar.setTime(obs.getObsDatetime());
				log.info("Adding value: " + obs.getValueNumeric() + " for " + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) );
				Day day = new Day(
						calendar.get(Calendar.DAY_OF_MONTH),
						calendar.get(Calendar.MONTH)+1,			// January = 0 
						calendar.get(Calendar.YEAR));
				series.addOrUpdate(day, obs.getValueNumeric());
			}
			dataset.addSeries(series);
				
			// Create graph
			JFreeChart chart = ChartFactory.createTimeSeriesChart(
				chartTitle,
				"Date",
				yAxisTitle,
				dataset,
				true, 
				true, 
				false
			);
			

			// Modify x (time) axis
	        XYPlot plot = (XYPlot) chart.getPlot();		        
	        DateAxis axis = (DateAxis) plot.getDomainAxis();
	        axis.setDateFormatOverride(new SimpleDateFormat("MMM-yy"));

	        // Add filled data points
			XYItemRenderer r = plot.getRenderer();
			if (r instanceof XYLineAndShapeRenderer) {
				XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
				renderer.setBaseShapesFilled(true);
				renderer.setBaseShapesVisible(true);
			}		
	        
			// Modify response to disable caching
			response.setHeader("Pragma", "No-cache"); 
			response.setDateHeader("Expires", 0); 
			response.setHeader("Cache-Control", "no-cache");
			
			// Write chart out to response as image 
			try { 
				if ( JPG_MIME_TYPE.equalsIgnoreCase(mimeType) ) { 
					response.setContentType(JPG_MIME_TYPE);
					ChartUtilities.writeChartAsJPEG(response.getOutputStream(), chart, width, height);
				} 
				else if ( PNG_MIME_TYPE.equalsIgnoreCase(mimeType)) { 
					response.setContentType(PNG_MIME_TYPE);
					ChartUtilities.writeChartAsPNG(response.getOutputStream(), chart, width, height);	
				} else { 

					
				}
			} catch (IOException e) { 
				log.error(e);
			}
		
		} 
		// Add error handling above and remove this try/catch 
		catch (Exception e) { 
			log.error(e);
		}
	}	
	
	/**
	 * 
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {	
		doGet(request,response);		
	}
	
	
	/**
	 * Convenience method to get context from session.  
	 * 
	 * TODO Should probably be added to some helper class since it is used all of the time. 
	 */
	public Context getContext(HttpServletRequest request) throws ServletException { 
		HttpSession session = request.getSession();
		Context context = (Context)session.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null) {
			throw new ServletException("Requires a valid context");
		}	
		return context;
	}

}
