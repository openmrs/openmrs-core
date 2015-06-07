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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

/**
 * Class dedicated for DataTables. It is a request that can be extracted from
 * {@link HttpServletRequest} with {@link #parseRequest(HttpServletRequest)}.
 * 
 * @see http://datatables.net/
 */
public class DatatableRequest {
	
	private Integer iDisplayStart;
	
	private Integer iDisplayLength;
	
	private Integer iColumns;
	
	private String sSearch;
	
	private Boolean bRegex;
	
	private Integer sEcho;
	
	private Integer iSortingCols;
	
	private Boolean[] bSearchableCol;
	
	private String[] sSearchCol;
	
	private Boolean[] bRegexCol;
	
	private Boolean[] bSortableCol;
	
	private Integer[] iSortCol;
	
	private String[] sSortDirCol;
	
	private String[] mDataPropCol;
	
	/**
	 * Display start point in the current data set.
	 * 
	 * @return the iDisplayStart
	 */
	public Integer getiDisplayStart() {
		return iDisplayStart;
	}
	
	/**
	 * @see #getiDisplayStart()
	 * @param iDisplayStart the iDisplayStart to set
	 */
	public void setiDisplayStart(Integer iDisplayStart) {
		this.iDisplayStart = iDisplayStart;
	}
	
	/**
	 * Number of records that the table can display in the current draw. It is expected that the
	 * number of records returned will be equal to this number, unless the server has fewer records
	 * to return.
	 * 
	 * @return the iDisplayLength
	 */
	public Integer getiDisplayLength() {
		return iDisplayLength;
	}
	
	/**
	 * @see #getiDisplayLength()
	 * @param iDisplayLength the iDisplayLength to set
	 */
	public void setiDisplayLength(Integer iDisplayLength) {
		this.iDisplayLength = iDisplayLength;
	}
	
	/**
	 * Number of columns being displayed (useful for getting individual column search info).
	 * 
	 * @return the iColumns
	 */
	public Integer getiColumns() {
		return iColumns;
	}
	
	/**
	 * @see #getiColumns()
	 * @param iColumns the iColumns to set
	 */
	public void setiColumns(Integer iColumns) {
		this.iColumns = iColumns;
	}
	
	/**
	 * Global search field.
	 * 
	 * @return the sSearch
	 */
	public String getsSearch() {
		return sSearch;
	}
	
	/**
	 * @see #getsSearch()
	 * @param sSearch the sSearch to set
	 */
	public void setsSearch(String sSearch) {
		this.sSearch = sSearch;
	}
	
	/**
	 * True if the global filter should be treated as a regular expression for advanced filtering,
	 * false if not.
	 * 
	 * @return the bRegex
	 */
	public Boolean getbRegex() {
		return bRegex;
	}
	
	/**
	 * @see #getbRegex()
	 * @param bRegex the bRegex to set
	 */
	public void setbRegex(Boolean bRegex) {
		this.bRegex = bRegex;
	}
	
	/**
	 * Indicator for if a column is flagged as searchable or not on the client-side.
	 * 
	 * @return the bSearchableCol
	 */
	public Boolean[] getbSearchableCol() {
		return bSearchableCol;
	}
	
	/**
	 * @see #getbSearchableCol()
	 * @param bSearchableCol the bSearchable to set
	 */
	public void setbSearchableCol(Boolean[] bSearchableCol) {
		if (bSearchableCol == null) {
			this.bSearchableCol = new Boolean[0];
		} else {
			this.bSearchableCol = Arrays.copyOf(bSearchableCol, bSearchableCol.length);
		}
	}
	
	/**
	 * Individual column filter.
	 * 
	 * @return the sSearchCol
	 */
	public String[] getsSearchCol() {
		return sSearchCol;
	}
	
	/**
	 * @see #getsSearchCol()
	 * @param sSearchCol the sSearchColumn to set
	 */
	public void setsSearchCol(String[] sSearchCol) {
		if (sSearchCol == null) {
			this.sSearchCol = new String[0];
		} else {
			this.sSearchCol = Arrays.copyOf(sSearchCol, sSearchCol.length);
		}
	}
	
	/**
	 * True if the individual column filter should be treated as a regular expression for advanced
	 * filtering, false if not.
	 * 
	 * @return the bRegexCol
	 */
	public Boolean[] getbRegexCol() {
		return bRegexCol;
	}
	
	/**
	 * @see #getbRegexCol()
	 * @param bRegexCol the bRegexColumn to set
	 */
	public void setbRegexCol(Boolean[] bRegexCol) {
		if (bRegexCol == null) {
			this.bRegexCol = new Boolean[0];
		} else {
			this.bRegexCol = Arrays.copyOf(bRegexCol, bRegexCol.length);
		}
	}
	
	/**
	 * Indicator for if a column is flagged as sortable or not on the client-side.
	 * 
	 * @return the bSortableCol
	 */
	public Boolean[] getbSortableCol() {
		return bSortableCol;
	}
	
	/**
	 * @see #getbSortableCol()
	 * @param bSortableCol the bSortable to set
	 */
	public void setbSortableCol(Boolean[] bSortableCol) {
		if (bSortableCol == null) {
			this.bSortableCol = new Boolean[0];
		} else {
			this.bSortableCol = Arrays.copyOf(bSortableCol, bSortableCol.length);
		}
	}
	
	/**
	 * Number of columns to sort on.
	 * 
	 * @return the iSortingCols
	 */
	public Integer getiSortingCols() {
		return iSortingCols;
	}
	
	/**
	 * @see #getiSortingCols()
	 * @param iSortingCols the iSortingCols to set
	 */
	public void setiSortingCols(Integer iSortingCols) {
		this.iSortingCols = iSortingCols;
	}
	
	/**
	 * Column being sorted on (you will need to decode this number for your database).
	 * 
	 * @return the iSortCol
	 */
	public Integer[] getiSortCol() {
		return iSortCol;
	}
	
	/**
	 * @see #getiSortCol()
	 * @param iSortCol the iSortCol to set
	 */
	public void setiSortCol(Integer[] iSortCol) {
		if (iSortCol == null) {
			this.iSortCol = new Integer[0];
		} else {
			this.iSortCol = Arrays.copyOf(iSortCol, iSortCol.length);
		}
	}
	
	/**
	 * Direction to be sorted - "desc" or "asc".
	 * 
	 * @return the sSortDirCol
	 */
	public String[] getsSortDirCol() {
		return sSortDirCol;
	}
	
	/**
	 * @see #getsSortDirCol()
	 * @param sSortDirCol the sSortDir to set
	 */
	public void setsSortDirCol(String[] sSortDirCol) {
		if (sSortDirCol == null) {
			this.sSortDirCol = new String[0];
		} else {
			this.sSortDirCol = Arrays.copyOf(sSortDirCol, sSortDirCol.length);
		}
	}
	
	/**
	 * The value specified by mDataProp for each column. This can be useful for ensuring that the
	 * processing of data is independent from the order of the columns.
	 * 
	 * @return the mDataPropCol
	 */
	public String[] getmDataPropCol() {
		return mDataPropCol;
	}
	
	/**
	 * @see #getmDataPropCol()
	 * @param mDataPropCol the mDataProp to set
	 */
	public void setmDataPropCol(String[] mDataPropCol) {
		if (mDataPropCol == null) {
			this.mDataPropCol = new String[0];
		} else {
			this.mDataPropCol = Arrays.copyOf(mDataPropCol, mDataPropCol.length);
		}
	}
	
	/**
	 * Information for DataTables to use for rendering.
	 * 
	 * @return the sEcho
	 */
	public Integer getsEcho() {
		return sEcho;
	}
	
	/**
	 * @see #getsEcho()
	 * @param sEcho the sEcho to set
	 */
	public void setsEcho(Integer sEcho) {
		this.sEcho = sEcho;
	}
	
	/**
	 * Creates {@link DatatableRequest} from parameters found in the given request.
	 * 
	 * @param request
	 * @return {@link DatatableRequest}
	 */
	public static DatatableRequest parseRequest(HttpServletRequest request) {
		DatatableRequest d = new DatatableRequest();
		
		d.setiDisplayStart(Integer.valueOf(request.getParameter("iDisplayStart")));
		d.setiDisplayLength(Integer.valueOf(request.getParameter("iDisplayLength")));
		d.setiColumns(Integer.valueOf(request.getParameter("iColumns")));
		d.setsSearch(request.getParameter("sSearch"));
		d.setbRegex(Boolean.valueOf(request.getParameter("bRegex")));
		d.setsEcho(Integer.valueOf(request.getParameter("sEcho")));
		d.setiSortingCols(Integer.valueOf(request.getParameter("iSortingCols")));
		
		String[] values = parseColumns("bSearchable", request);
		d.setbSearchableCol(convertToBoolean(values));
		
		values = parseColumns("sSearch", request);
		d.setsSearchCol(values);
		
		values = parseColumns("bRegex", request);
		d.setbRegexCol(convertToBoolean(values));
		
		values = parseColumns("bSortable", request);
		d.setbSortableCol(convertToBoolean(values));
		
		values = parseColumns("iSortCol", request);
		d.setiSortCol(convertToInteger(values));
		
		values = parseColumns("sSortDir", request);
		d.setsSortDirCol(values);
		
		values = parseColumns("mDataProp", request);
		d.setmDataPropCol(values);
		
		return d;
	}
	
	private static Boolean[] convertToBoolean(String[] values) {
		Boolean[] result = new Boolean[values.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = Boolean.valueOf(values[i]);
		}
		return result;
	}
	
	private static Integer[] convertToInteger(String[] values) {
		Integer[] result = new Integer[values.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = Integer.valueOf(values[i]);
		}
		return result;
	}
	
	private static String[] parseColumns(String type, HttpServletRequest request) {
		@SuppressWarnings("unchecked")
		Map<String, String[]> parameterMap = request.getParameterMap();
		
		Map<Integer, String> parameters = new HashMap<Integer, String>();
		type = type + "_";
		
		for (Entry<String, String[]> parameter : parameterMap.entrySet()) {
			if (parameter.getKey().startsWith(type)) {
				Integer index = Integer.valueOf(parameter.getKey().replace(type, ""));
				String value = parameter.getValue()[0];
				parameters.put(index, value);
			}
		}
		
		String[] values = new String[parameters.size()];
		for (Entry<Integer, String> parameter : parameters.entrySet()) {
			values[parameter.getKey()] = parameter.getValue();
		}
		return values;
	}
}
