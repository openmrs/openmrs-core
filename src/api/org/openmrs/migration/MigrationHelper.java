package org.openmrs.migration;

import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MigrationHelper {

    static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	
    static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static Date parseDate(String s) throws ParseException {
		if (s == null || s.length() == 0) {
			return null;
		} else {
			return df.parse(s);
		}
	}
	
	public static Document parseXml(String xml) throws ParserConfigurationException {
		DocumentBuilder builder = factory.newDocumentBuilder();
		try {
			return builder.parse(new InputSource(new StringReader(xml)));
		} catch (IOException ex) {
			return null;
		} catch (SAXException e) {
			return null;
		}
	}

	private static void findNodesNamed(Node node, String lookForName, Collection<Node> ret) {
		if (node.getNodeName().equals(lookForName)) {
			ret.add(node);
		} else {
			NodeList list = node.getChildNodes();
			for (int i = 0; i < list.getLength(); ++i) {
				findNodesNamed(list.item(i), lookForName, ret);
			}
		}
	}
	
	/**
	 * Takes XML like:
	 * <something>
	 *   <user date_changed="2001-03-06 08:46:53.0"
	 *         date_created="2001-03-06 08:46:53.0"
	 *         username="hamish@mit.edu"
	 *         first_name="Hamish"
	 *         last_name="Fraser" 
	 *         user_id="2001"/>
	 * </something> 
	 * Returns the number of users added
	 */
	public static int importUsers(Context context, Document document) throws ParseException {
		int ret = 0;
		Random rand = new Random();
		UserService us = context.getUserService();
		
		List<Node> toAdd = new ArrayList<Node>();
		findNodesNamed(document, "user", toAdd);
		for (Node node : toAdd) {
			Element e = (Element) node;
			String username = e.getAttribute("username");
			if (username == null || username.length() == 0) {
				throw new IllegalArgumentException("each <user /> element must define a user_name attribute");
			}
			if (us.getUserByUsername(username) != null) {
				continue;
			}
			User user = new User();
			user.setFirstName(e.getAttribute("first_name"));
			user.setLastName(e.getAttribute("last_name"));
			user.setUsername(username);
			user.setDateCreated(parseDate(e.getAttribute("date_created")));
			user.setDateChanged(parseDate(e.getAttribute("date_changed")));

			// Generate a temporary password: 8-12 random characters
			String pass = null;
			{
				int length = rand.nextInt(4) + 8;
				char[] password = new char[length];
				for (int x = 0; x < length; x++) {
					int randDecimalAsciiVal = rand.nextInt(93) + 33;
					password[x] = (char) randDecimalAsciiVal;
				}
				pass = new String(password);
			}
			us.createUser(user, pass);
			++ret;
		}
		return ret;
	}
		
	/**
	 * Takes XML like:
	 * <something>
	 *   <location name="Cerca-la-Source"/>
	 * </something> 
	 * returns the number of locations added
	 */
	public static int importLocations(Context context, Document document) {
		int ret = 0;
		EncounterService es = context.getEncounterService();
		AdministrationService as = context.getAdministrationService();
		List<Node> toAdd = new ArrayList<Node>();
		findNodesNamed(document, "location", toAdd);
		for (Node node : toAdd) {
			Element e = (Element) node;
			String name = e.getAttribute("name");
			if (name == null || name.length() == 0) {
				throw new IllegalArgumentException("each <location /> element must define a name attribute");
			}
			if (es.getLocationByName(name) != null) {
				continue;
			}
			Location location = new Location();
			location.setName(name);			

			as.createLocation(location);
			++ret;
		}
		return ret;
	}
}
	
