package org.openmrs.web.taglib;

import java.io.IOException;
import java.util.Collection;
import java.util.Vector;

import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ListPickerTag extends TagSupport {

	public static final long serialVersionUID = 1122112233L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	private String name;
	private Collection<Object> allItems;
	private Collection<Object> currentItems;
	private String contextPath = null;
	
	public int doStartTag() {
		
		if (contextPath == null)
			contextPath = "/openmrs";
		if (currentItems == null)
			currentItems = new Vector();
		if (allItems == null)
			allItems = new Vector();
		
		contextPath += "/scripts/dragndrop";
		
		String str = "";
		
		if (pageContext.getAttribute("ListPicker") == null) {
			str += "\n<link rel='stylesheet' href='" + contextPath + "/lists.css' type='text/css' />";
			str += "\n<script language='JavaScript' type='text/javascript' src='" + contextPath + "/coordinates.js'></script>";
			str += "\n<script language='JavaScript' type='text/javascript' src='" + contextPath + "/drag.js'></script>";
			str += "\n<script language='JavaScript' type='text/javascript' src='" + contextPath + "/dragdrop.js'></script>";
			str += "\n<script language='JavaScript' type='text/javascript' src='" + contextPath + "/custom.js'></script>";
			
			pageContext.setAttribute("ListPicker", true);
		}
		
		
		str += "\n<div id='" + name + "'>";
		str += "\n<select name='" + name + "' id='savedItems_" + name + "' class='savedItems' multiple>";
		for (Object s : currentItems) {
			str += "<option selected>" + s + "</option>\n";
		}
		str += "\n</select>\n\n";
		
		str += "<table><tr>\n";
		
		str += "<td valign='top'>Current:<br><ul id='currentItems_" + name + "' class='sortable boxy currentItems'>\n";
		for (Object s : currentItems) {
			str += "<li id='" + s + "'>" + s + "</li>\n";
			allItems.remove(s);
		}
		
		str += "</ul>\n";
		str += "<div class='drophere'>Drop Here</div>\n";
		str += "</td>";
		
		str += "<td style='vertical-align: top; padding-left: 30px;'>All: <br><ul id='allItems_" + name + "' class='sortable boxy allItems'>\n";
		for (Object s : allItems) {
			str += "<li id='" + s + "'>" + s + "</li>\n";
		}
		
		str += "<div class='clear'></div>";
		str += "</ul></td>\n";
		
		str += "</tr></table>\n";
		str += "</div>\n\n";
		
		str += "<script type='text/javascript'>\n";

		str += "  var oldonload = window.onload;\n";
		str += "  if (typeof window.onload != 'function') {\n";
		str += "  	window.onload = function() { init('" + name + "'); };\n";
		str += "  } else {\n";
		str += "  	window.onload = function() {\n";
		str += "  		oldonload();\n";
		str += "  		init('" + name + "');\n";
		str += "  	}\n";
		str += "  }\n";
		
		str += "</script>\n";
		
		try {
			pageContext.getOut().write(str);
		}
		catch (IOException e) {
			log.error(e);
		}

		return SKIP_BODY;
	}

	public Collection<Object> getAllItems() {
		return allItems;
	}

	public void setAllItems(Collection<Object> allItems) {
		this.allItems = allItems;
	}

	public Collection<Object> getCurrentItems() {
		return currentItems;
	}

	public void setCurrentItems(Collection<Object> currentItems) {
		this.currentItems = currentItems;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

}
