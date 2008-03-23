/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.reporting.DataTable;
import org.openmrs.reporting.TableRow;

public class DataEntryStatistic {

	protected final Log log = LogFactory.getLog(getClass());

	private User user;
	private String entryType;
	private Integer numberOfEntries;
	private Integer numberOfObs;
	private Object groupBy = null;
	
	public DataEntryStatistic() { }

	public String toString() {
		return user + " entered " + numberOfEntries + " of " + entryType;
	}
	
	public Integer getNumberOfEntries() {
		return numberOfEntries;
	}

	public void setNumberOfEntries(Integer numberOfEntries) {
		this.numberOfEntries = numberOfEntries;
	}

	public String getEntryType() {
		return entryType;
	}

	public void setEntryType(String entryType) {
		this.entryType = entryType;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public Object getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(Object groupBy) {
		this.groupBy = groupBy;
	}
	
	public Integer getNumberOfObs() {
		return numberOfObs;
	}

	public void setNumberOfObs(Integer numberOfObs) {
		this.numberOfObs = numberOfObs;
	}

	// convenience utility methods

	public static DataTable tableByUserAndType(List<DataEntryStatistic> stats, Boolean hideAverageObs) {
		Set<User> users = new HashSet<User>();
		SortedSet<String> types = new TreeSet<String>();
		Set<Object> groups = new HashSet<Object>();
		Map<String, Integer> totals = new HashMap<String, Integer>();
		Map<String, Integer> totalObs = new HashMap<String, Integer>();
		for (DataEntryStatistic s : stats) {
			users.add(s.getUser());
			types.add(s.getEntryType());
			groups.add(s.getGroupBy());
			String temp = s.getUser().getUserId() + "." + s.getEntryType() + "." + s.getGroupBy();
			Integer soFar = totals.get(temp);
			totals.put(temp, soFar == null ? s.getNumberOfEntries() : (soFar + s.getNumberOfEntries()));
			totalObs.put(temp, soFar == null ? s.getNumberOfObs() : (soFar + s.getNumberOfObs()));
		}
		DataTable table = new DataTable();
		table.addColumn("User");
		table.addColumns(types);
		for (Object group : groups) {
			Map<String, Integer> groupTotals = new HashMap<String, Integer>();
			for (User u : users) {
				TableRow tr = new TableRow();
				tr.put("User", u);
				Integer rowTotal = 0;
				for (String entryType : types) {
					Integer i = totals.get(u.getUserId() + "." + entryType + "." + group);
					Integer j = totalObs.get(u.getUserId() + "." + entryType + "." + group);
					if (i == null) i = 0;
					if (j == null) j = 0;
					String averageObs = "";
					if (!hideAverageObs && i > 0 && j > 0 ) {
						DecimalFormat df = new DecimalFormat("###,###.##");
						float obss = j;
						float encs = i;
						float avgObs = obss / encs;
						averageObs += " (avg. " + df.format(avgObs) + " obs per enc)";
					}
					tr.put(entryType, i + averageObs);
					Integer groupTotalSoFar = groupTotals.get(entryType);
					groupTotalSoFar = groupTotalSoFar == null ? i : groupTotalSoFar + i;
					groupTotals.put(entryType, groupTotalSoFar);
					rowTotal += i;
				}
				if (rowTotal > 0)
					table.addRow(tr);
			}
			// add grouping totals
			TableRow totalTR = new TableRow();
			totalTR.put("User", "--" + (group == null ? "Total" : group.toString()) + "--");
			for (String entryType : types) {
				totalTR.put(entryType, groupTotals.get(entryType));
			}
			table.addRow(totalTR);
		}
		return table;
	}


}
