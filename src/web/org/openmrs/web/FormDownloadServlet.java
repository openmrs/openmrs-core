package org.openmrs.web;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientName;
import org.openmrs.context.Context;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class FormDownloadServlet extends HttpServlet {

	public static final long serialVersionUID = 123423L;

	private Document xmldoc;
	
	/**
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String formType  = request.getParameter("formType");
		String patientId = request.getParameter("patientId");
		HttpSession httpSession = request.getSession();
		
		Context context = (Context)httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null) {
			httpSession.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Your session has expired.");
			response.sendRedirect(request.getContextPath() + "/logout");
			return;
		}
		
		Patient patient = context.getPatientService().getPatient(Integer.valueOf(patientId));
		
		
		// ==xml document input=========
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		factory.setNamespaceAware(true);
		
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			xmldoc = builder.parse("../webapps/openmrs/formentry/forms/" + formType + ".xml");
		}
		catch (ParserConfigurationException e) {
			throw new ServletException(e);
		}
		catch (SAXException e) {
			throw new ServletException(e);
		}
		
		// ==setting patient specific data to xml document================
		
		PatientName pn = patient.getPatientName();
		PatientIdentifier pi = (PatientIdentifier)patient.getIdentifiers().toArray()[0];
		
		setElementText("patient.family_name", pn.getFamilyName());
		setElementText("patient.given_name", pn.getGivenName());
		setElementText("patient.middle_name", pn.getMiddleName());
		setElementText("patient.medical_record_number", pi.getIdentifier());
		setElementText("patient.patient_id", patientId);

		// ==setting misc data to xml document ================
		setElementText("enterer", context.getAuthenticatedUser().getUsername());
		setElementText("date_entered", new SimpleDateFormat("dd-MMM-yy").format(new Date()));
		setElementText("session", httpSession.getId());
		
		// ==xml document output========
		StringWriter writer = new StringWriter();
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		
		try {
			Transformer transformer = transformerFactory.newTransformer();
			transformer.transform(new DOMSource(xmldoc), new StreamResult(writer));
		}
		catch (TransformerConfigurationException e) {
			throw new ServletException(e);
		}
		catch (TransformerException e) {
			throw new ServletException(e);
		}
		
		response.setHeader("Content-Type", "application/ms-infopath.xml");
		response.getOutputStream().println(writer.toString());
		
	}

	private void setElementText(String tagName, String content) {
		NodeList nodes = xmldoc.getElementsByTagName(tagName);
		for (int i = 0; i < nodes.getLength(); i++) {
			nodes.item(i).setTextContent(content);
		}
	}
	
}

