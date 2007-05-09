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
	
	public int doStartTag() {
		
		if (name == null)
			name = "list" + (int)(Math.random() * 100);
		if (currentItems == null)
			currentItems = new Vector<Object>();
		if (allItems == null)
			allItems = new Vector<Object>();
		
		String str = "\n<div id='" + name + "' class='listItemBox'>";
		
		for (Object item : allItems) {
			boolean checked = false;
			if (currentItems.contains(item))
				checked = true;
			String id = name + "." + item.toString().replace(" ", "");
			str += "<span class='listItem" + (checked ? " listItemChecked" : "") + "'>";
			str += "<input type='checkbox'";
			str += "  name='" + name + "'";
			str += "  id='" + id + "'";
			str += "  value='" + item + "'";
			str += "  onclick='this.parentNode.className=\"listItem \" + (this.checked == true ? \"listItemChecked\" : \"\");'";
			if (checked)
				str += "  checked='checked' ";
			str += " /><label for='" + id + "'>" + item + "</label>";
			str += "</span>\n";
		}
		
		str += "</div>\n\n";
		
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
	
}