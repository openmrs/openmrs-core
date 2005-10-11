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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class InfoPathServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String familyName = "";
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(request.getInputStream());
			Element elem = (Element)doc.getElementsByTagName("my:myFields").item(0);
			Element elemName = (Element)elem.getElementsByTagName("my:name").item(0);
			Element elemFamilyName = (Element)elemName.getElementsByTagName("my:familyName").item(0);
			familyName = elemFamilyName.getTextContent();
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
		if (familyName.equalsIgnoreCase("smith")) {
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setContentType("text/html;charset=utf-8");
			out.println("<html><head><title>FormEntry Error</title></head>"
					+ "<body><h1>Details</h1>"
					+ "<p>Family name \"" + familyName + "\" not acceptable</p>"
					+ "</body></html>");			
		}
	}

	// for testing
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}
	
	
}
