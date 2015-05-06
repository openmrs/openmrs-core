/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.taglib;

import java.io.IOException;
import java.util.Collection;
import java.util.Random;
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
	
	private Collection<Object> inheritedItems;
	
	private Collection<Object> descendantItems;
	
	public int doStartTag() {
		Random gen = new Random();
		if (name == null) {
			name = "list" + (gen.nextInt() * 100);
		}
		if (currentItems == null) {
			currentItems = new Vector<Object>();
		}
		if (inheritedItems == null) {
			inheritedItems = new Vector<Object>();
		}
		if (descendantItems == null) {
			descendantItems = new Vector<Object>();
		}
		if (allItems == null) {
			allItems = new Vector<Object>();
		}
		String str = "\n<div id='" + name + "' class='listItemBox'>";
		
		for (Object item : allItems) {
			if (descendantItems.contains(item)) {
				continue;
			}
			boolean checked = false;
			boolean inherited = false;
			if (currentItems.contains(item)) {
				checked = true;
			}
			if (inheritedItems.contains(item)) {
				inherited = true;
			}
			String id = name + "." + item.toString().replace(" ", "");
			if (inherited) {
				str += "<span class='listItem listItemChecked'>";
				str += "<input type='checkbox' name=''";
				str += " checked='checked'";
				str += " disabled='disabled'";
			} else {
				str += "<span class='listItem" + (checked ? " listItemChecked" : "") + "'>";
				str += "<input type='checkbox'";
				str += " name='" + name + "'";
				str += " id='" + id + "'";
				str += " value='" + item + "'";
				str += " onclick='this.parentNode.className=\"listItem \" + (this.checked == true ? \"listItemChecked\" : \"\");'";
				if (checked) {
					str += "  checked='checked' ";
				}
			}
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
	
	public Collection<Object> getInheritedItems() {
		return currentItems;
	}
	
	public void setInheritedItems(Collection<Object> inheritedItems) {
		this.inheritedItems = inheritedItems;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Collection<Object> getDescendantItems() {
		return descendantItems;
	}
	
	public void setDescendantItems(Collection<Object> descendantItems) {
		this.descendantItems = descendantItems;
	}
	
}
