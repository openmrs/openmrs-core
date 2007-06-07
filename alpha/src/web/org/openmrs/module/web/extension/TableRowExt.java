package org.openmrs.module.web.extension;

import java.util.Map;

import org.openmrs.module.Extension;

public abstract class TableRowExt extends Extension {
	
	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	/**
	 * The map returns a listing of the rows to add to a table.  The map key will 
	 * be attempted to be used as a Spring message.  The map value will be the 
	 * html to insert into the table cell
	 * 
	 * In order to sort the links, you need to create a <code>TreeMap</code>.  A 
	 * utility method has been added to OpenMRS to save the order of the members 
	 * of the map: <code>org.openmrs.util.InsertedOrderComparator</code>
	 * With this comparator, the order of the links will be the order they were 
	 * inserted into the map.
	 * 
	 * @return Map<String, String> of <label for cell, cell content>
	 */
	public abstract Map<String, String> getRows();
		
}
