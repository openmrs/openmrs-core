package org.openmrs.web;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class FormUploadServlet extends HttpServlet {

	private static final long serialVersionUID = -3545085468235057302L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String formName = "";
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			String xml = IOUtils.toString(request.getInputStream());
			Document doc = db.parse(IOUtils.toInputStream(xml));
			NodeList formElemList = doc.getElementsByTagName("form");
			if (formElemList != null && formElemList.getLength() > 0) {
				Element formElem = (Element)formElemList.item(0);
				formName = formElem.getAttribute("name");
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ServletOutputStream out = response.getOutputStream();
		if (formName.equalsIgnoreCase("x")) {
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setContentType("text/html;charset=utf-8");
			out.println("<html><head><title>FormEntry Error</title></head>"
					+ "<body><h1>Details</h1>"
					+ "<p>form name = \"" + formName + "\"</p>"
					+ "</body></html>");			
		}
	}

	// for testing
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}
	
	
}
