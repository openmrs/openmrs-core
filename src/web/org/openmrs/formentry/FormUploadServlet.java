package org.openmrs.formentry;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Handles uploads of form data and loads them into a queue for processing
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public class FormUploadServlet extends HttpServlet {

	private static final long serialVersionUID = -3545085468235057302L;

	private Log log = LogFactory.getLog(this.getClass());
	
	private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String formName = "";
		String xml = "no xml!";
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			xml = IOUtils.toString(request.getInputStream());
			Document doc = db.parse(IOUtils.toInputStream(xml));
			NodeList formElemList = doc.getElementsByTagName("form");
			if (formElemList != null && formElemList.getLength() > 0) {
				Element formElem = (Element)formElemList.item(0);
				formName = formElem.getAttribute("name");
			}
		} catch (ParserConfigurationException e) {
			log.error("Error parsing form data", e);
		} catch (SAXException e) {
			log.error("Error parsing form data", e);
		} catch (IOException e) {
			log.error("Error parsing form data", e);
		}

		FormEntryQueue formEntryQueue = new FormEntryQueue();
		formEntryQueue.setFormData(xml);
		Context context = (Context)request.getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		FormEntryService formEntryService = context.getFormEntryService();
		formEntryService.createFormEntryQueue(formEntryQueue);
		
		ServletOutputStream out = response.getOutputStream();
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/html;charset=utf-8");
		out.println("<html><head><title>FormEntry</title></head>"
				+ "<body><h1>Details (with xml)</h1>"
				+ "<p>form name = \"" + formName + "\"</p>"
				+ "<p><textarea cols=40 rows=8>" + xml + "</textarea></p>"
				+ "</body></html>");			
	}

	// for testing
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ServletOutputStream out = response.getOutputStream();
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		out.print("<html><head>Invalid Request</head><body>Invalid Request</body></html>");
	}
	
}
