package org.openmrs.oldreporting;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class DataSeries<K, V> {

	private String keyTitle;
	private String valueTitle;
	private Map<K, V> data;
	
	public DataSeries() { }
	
	public DataSeries(String keyTitle, String valueTitle) {
		this.keyTitle = keyTitle;
		this.valueTitle = valueTitle;
	}

	public DataSeries(DataTable table, String keyColumnName, String valueColumnName) {
		data = new HashMap<K, V>();
		for (DataRow row : table.getRows()) {
			data.put((K) row.get(keyColumnName), (V) row.get(valueColumnName));
		}
		try {
			data = new TreeMap<K, V>(data);
		} catch (Exception ex) { }
	}

	public V getValue(K key) {
		return data.get(key);
	}
	
	public void setValue(K key, V value) {
		data.put(key, value);
	}
	
	public Set<K> getKeys() {
		return data.keySet();
	}
	
	/**
	 * @return Returns the data.
	 */
	public Map<K, V> getData() {
		return data;
	}

	/**
	 * @param data The data to set.
	 */
	public void setData(Map<K, V> data) {
		this.data = data;
	}
	
	/**
	 * @return Returns the keyTitle.
	 */
	public String getKeyTitle() {
		return keyTitle;
	}

	/**
	 * @param keyTitle The keyTitle to set.
	 */
	public void setKeyTitle(String keyTitle) {
		this.keyTitle = keyTitle;
	}

	/**
	 * @return Returns the valueTitle.
	 */
	public String getValueTitle() {
		return valueTitle;
	}

	/**
	 * @param valueTitle The valueTitle to set.
	 */
	public void setValueTitle(String valueTitle) {
		this.valueTitle = valueTitle;
	}

	public String toString() {
		StringBuffer ret = new StringBuffer();
		ret.append("<table><tr><td>" + keyTitle + "</td><td>" + valueTitle + "</td></tr>");
		for (Map.Entry<K, V> e : data.entrySet()) {
			ret.append("<tr><td>" + e.getKey() + "</td><td>" + e.getValue() + "</td></tr>");
		}
		ret.append("</table>");
		return ret.toString();
	}
	
}
