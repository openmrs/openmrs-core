package org.openmrs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.openmrs.reporting.DataTable;
import org.openmrs.reporting.TableRow;

public class DataEntryStatistic {

	private User user;
	private String entryType;
	private Integer numberOfEntries;
	
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

	// convenience utility methods

	public static DataTable tableByUserAndType(List<DataEntryStatistic> stats) {
		Set<User> users = new HashSet<User>();
		SortedSet<String> types = new TreeSet<String>();
		Map<String, Integer> totals = new HashMap<String, Integer>();
		for (DataEntryStatistic s : stats) {
			users.add(s.getUser());
			types.add(s.getEntryType());
			String temp = s.getUser().getUserId() + "." + s.getEntryType();
			Integer soFar = totals.get(temp);
			totals.put(temp, soFar == null ? s.getNumberOfEntries() : (soFar + s.getNumberOfEntries()));
		}
		DataTable ret = new DataTable();
		ret.addColumn("User");
		ret.addColumns(types);
		for (User u : users) {
			TableRow tr = new TableRow();
			tr.put("User", u);
			for (String entryType : types) {
				Integer i = totals.get(u.getUserId() + "." + entryType);
				tr.put(entryType, i == null ? 0 : i);
			}
			ret.addRow(tr);
		}
		return ret;
	}
}
