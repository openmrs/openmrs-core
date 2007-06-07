package org.openmrs.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.JFreeChart;

/**
 * Servlet for rendering a 3D piechart of categories and values
 * width:  Width of the generated image
 * height: Height of the generated image
 * mimeType: Accepts either image/png or image/jpeg
 * chartTitle: The title of the graph
 */
public class DisplayChartServlet extends AbstractGraphServlet {

	public static final long serialVersionUID = 1231232L;
	private Log log = LogFactory.getLog(DisplayChartServlet.class);
	
	public static final String SERVLET_NAME = "displayChartServlet";
	public static final String CHART_KEY = "chartKey";
	
	protected JFreeChart createChart(HttpServletRequest request, HttpServletResponse response) {
		
		String key = request.getParameter(CHART_KEY);
		
		HttpSession session = request.getSession();
		Object o = session.getAttribute(key);
		
		if (o == null)
			log.error("Unable to find chart in session with key: " + key);
		
		return (JFreeChart)o;
		
	}
}

	



