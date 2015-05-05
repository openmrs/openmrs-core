/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class dedicated for DataTables. It is a response that can be translated to JSON.
 * 
 * @see http://datatables.net/
 */
public class DatatableResponse {
	
	private Integer sEcho;
	
	private Integer iTotalRecords;
	
	private Integer iTotalDisplayRecords;
	
	private String[] sColumns;
	
	private List<String[]> aaData;
	
	public DatatableResponse(DatatableRequest request) {
		sEcho = request.getsEcho();
	}
	
	/**
	 * An unaltered copy of sEcho sent from the client side. This parameter will change with each
	 * draw (it is basically a draw count) - so it is important that this is implemented. Note that
	 * it strongly recommended for security reasons that you 'cast' this parameter to an integer in
	 * order to prevent Cross Site Scripting (XSS) attacks.
	 * 
	 * @return the sEcho
	 */
	public Integer getsEcho() {
		return sEcho;
	}
	
	/**
	 * @see #setiTotalRecords(Integer)
	 * @return the iTotalRecords
	 */
	public Integer getiTotalRecords() {
		return iTotalRecords;
	}
	
	/**
	 * Total records, before filtering (i.e. the total number of records in the database).
	 * 
	 * @param iTotalRecords the iTotalRecords to set
	 */
	public void setiTotalRecords(Integer iTotalRecords) {
		this.iTotalRecords = iTotalRecords;
	}
	
	/**
	 * @see #setiTotalDisplayRecords(Integer)
	 * @return the iTotalDisplayRecords
	 */
	public Integer getiTotalDisplayRecords() {
		return iTotalDisplayRecords;
	}
	
	/**
	 * Total records, after filtering (i.e. the total number of records after filtering has been
	 * applied - not just the number of records being returned in this result set).
	 * 
	 * @param iTotalDisplayRecords the iTotalDisplayRecords to set
	 */
	public void setiTotalDisplayRecords(Integer iTotalDisplayRecords) {
		this.iTotalDisplayRecords = iTotalDisplayRecords;
	}
	
	/**
	 * @see #setsColumns(String...)
	 * @return the sColumns
	 */
	public String[] getsColumns() {
		return sColumns;
	}
	
	/**
	 * Optional - this is a string of column names, comma separated (used in combination with sName)
	 * which will allow DataTables to reorder data on the client-side if required for display. Note
	 * that the number of column names returned must exactly match the number of columns in the
	 * table. For a more flexible JSON format, please consider using mDataProp.
	 * 
	 * @param sColumns the sColumns to set
	 */
	public void setsColumns(String... sColumns) {
		if (aaData != null && aaData.get(0).length != sColumns.length) {
			throw new IllegalArgumentException("Must contain " + aaData.get(0).length + " columns whereas "
			        + sColumns.length + " given");
		}
		this.sColumns = sColumns;
	}
	
	/**
	 * The data in a 2D array. Note that you can change the name of this parameter with
	 * sAjaxDataProp.
	 * 
	 * @return the aaData
	 */
	public String[][] getAaData() {
		if (aaData != null) {
			return aaData.toArray(new String[aaData.size()][]);
		} else {
			return new String[0][];
		}
	}
	
	/**
	 * Adds the given row to the data. Consecutive calls must contain the same amount of columns.
	 * 
	 * @param row
	 */
	public void addRow(String... row) {
		if (sColumns != null && sColumns.length != row.length) {
			throw new IllegalArgumentException("Must contain " + sColumns.length
			        + " columns as declared in sColumns whereas " + row.length + " given");
		}
		
		if (aaData != null && aaData.get(0).length != row.length) {
			throw new IllegalArgumentException("Must contain " + aaData.get(0).length + " columns whereas " + row.length
			        + " given");
		}
		
		if (aaData == null) {
			aaData = new ArrayList<String[]>();
		}
		
		for (int i = 0; i < row.length; i++) {
			if (row[i] == null) {
				row[i] = "";
			}
		}
		
		aaData.add(row);
	}
	
	/**
	 * Adds the given row to the data. 
	 * 
	 * @param row
	 */
	public void addRow(Map<String, String> row) {
		if (sColumns == null) {
			setsColumns(row.keySet().toArray(new String[0]));
		}
		
		String[] rowed = new String[sColumns.length];
		for (int i = 0; i < rowed.length; i++) {
			rowed[i] = row.get(sColumns[i]);
		}
		
		addRow(rowed);
	}
}
