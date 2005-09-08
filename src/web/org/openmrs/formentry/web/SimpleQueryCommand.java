package org.openmrs.formentry.web;

import java.util.List;

public class SimpleQueryCommand {

	private String q;
	private List results;

	/**
	 * @return Returns the q.
	 */
	public String getQ() {
		return q;
	}

	/**
	 * @param q
	 *            The q to set.
	 */
	public void setQ(String q) {
		this.q = q;
	}

	/**
	 * @return Returns the results.
	 */
	public List getResults() {
		return results;
	}

	/**
	 * @param results
	 *            The results to set.
	 */
	public void setResults(List results) {
		this.results = results;
	}

}
