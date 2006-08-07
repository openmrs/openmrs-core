package org.openmrs.web.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
import org.jfree.data.category.DefaultCategoryDataset;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;

public class ShowGraphServlet  extends HttpServlet {

	public static final long serialVersionUID = 1231231L;
	private Log log = LogFactory.getLog(this.getClass());
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		try { 
			// TODO (jmiranda) Need better error handling
			Integer patientId = Integer.parseInt(request.getParameter("patientId"));
			Integer conceptId = Integer.parseInt(request.getParameter("conceptId"));
			Integer width = Integer.parseInt(request.getParameter("width")!=null?request.getParameter("width"):"500");
			Integer height = Integer.parseInt(request.getParameter("width")!=null?request.getParameter("height"):"300");
			
			Context context = getContext(request);
			Patient patient = context.getPatientService().getPatient(patientId);
			Concept concept = context.getConceptService().getConcept(conceptId);
			
			if (concept != null && concept.isNumeric()) { 
				List<Obs> observations = new ArrayList<Obs>(context.getObsService().getObservations(patient, concept));
				Collections.sort(observations, new Comparator<Obs>() {
						public int compare(Obs left, Obs right) {
							Date d1 = left.getObsDatetime();
							Date d2 = right.getObsDatetime();
							if (d1 == null) {
								return -1;
							} else if (d2 == null) {
								return 1;
							} else {
								return d1.compareTo(d2);
							}
						}
					});
				DefaultCategoryDataset dataset = new DefaultCategoryDataset();
				Locale locale = request.getLocale();
				ConceptName conceptName = concept.getName(locale);
				for( Obs obs : observations ) { 
					dataset.addValue(obs.getValueNumeric(), conceptName.getName(), obs.getObsDatetime().toString().substring(0, 7));
				}
				JFreeChart chart = ChartFactory.createLineChart(
					conceptName.getName(),
					"Date",
					"Value",
					dataset,
					PlotOrientation.VERTICAL,
					true, 
					true, 
					false
				);
				
				try { 
					response.setContentType("image/png");
					ChartUtilities.writeChartAsPNG(response.getOutputStream(), chart, width, height);					
				} catch (IOException e) { 
					log.error(e);
				}
			}
		} 
		// Add error handling above and remove this try/catch 
		catch (Exception e) { 
			log.error(e);
		}
	}	
	
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
