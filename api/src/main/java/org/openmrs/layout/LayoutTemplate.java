/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic class used by AddressTemplate and NameTemplate layouts
 *
 * @since 1.12
 */
public abstract class LayoutTemplate {

	protected static final String LAYOUT_TOKEN = "<!-- openmrsToken -->";

	protected String displayName;

	protected String codeName;

	protected String country;

	protected Map<String, String> nameMappings;

	protected Map<String, String> sizeMappings;

	protected Map<String, String> elementDefaults;

	protected Map<String, String> elementRegex;

	protected Map<String, String> elementRegexFormats;

	protected List<String> lineByLineFormat;

	protected List<String> requiredElements;

	// The largest number of tokens on one given line
	protected int maxTokens = 0;

	protected String startDate;

	protected String endDate;

	/**
	 * Tokenizing the line-by-line format is a pure function of {@link #lineByLineFormat},
	 * {@link #nameMappings}, {@link #sizeMappings} and the special tokens declared by the owning
	 * {@link LayoutSupport}, none of which change once the template has been wired up, so the result is
	 * memoized. Held in a single immutable snapshot object so readers never observe a half-built cache
	 * without needing a lock on the read path, and marked transient so serializers that round-trip
	 * templates to XML ignore it.
	 */
	private transient volatile TokenizedLines tokenizedLines;

	public LayoutTemplate() {
	}

	/**
	 * Very crude way of setting just one line of template. This just puts something on
	 * {@link #setLineByLineFormat(List)} with this string
	 *
	 * @param simpleTemplate first template line
	 */
	public LayoutTemplate(String simpleTemplate) {
		setLineByLineFormat(Collections.singletonList(simpleTemplate));
	}

	public abstract String getLayoutToken();

	public abstract String getNonLayoutToken();

	private String replaceTokens(String line, List<String> specialTokens) {
		for (String token : specialTokens) {
			line = line.replaceAll(token, LAYOUT_TOKEN);
		}
		return line;
	}

	private List<Map<String, String>> convertToTokens(String line, String[] nonTokens) {
		List<Map<String, String>> ret = null;
		if (line != null && nonTokens != null && nonTokens.length > 0) {
			int idxCurr = -1;

			for (int i = 0; i < nonTokens.length; i++) {
				String nonToken = nonTokens[i];
				if (idxCurr + 1 < line.length()) {
					idxCurr = line.indexOf(nonToken, idxCurr + 1);
				}

				if (ret == null) {
					ret = new ArrayList<>();
				}

				if (i == 0 && idxCurr > 0) {
					// this means there is a token at the beginning - we'll have to grab it
					ret.add(token(line.substring(0, idxCurr)));
				}

				if (i < nonTokens.length - 1) {
					// this means we are still not at the last non-token, so let's add this non-token AND this token
					int idxNext = line.indexOf(nonTokens[i + 1], idxCurr + 1);

					ret.add(nonToken(nonToken));
					//HERE:  real Token is wrong...
					ret.add(token(line.substring(idxCurr + nonToken.length(), idxNext)));
				} else {
					// we are on the last non-token, so check if it is the end
					ret.add(nonToken(nonToken));
					if (idxCurr + nonToken.length() < line.length()) {
						// we need to add one last token at the end
						ret.add(token(line.substring(idxCurr + nonToken.length())));
					}
				}
			}
		} else if (line != null && !line.isEmpty()) {
			// looks like we have a single token on a line by itself
			ret = new ArrayList<>(2);

			// adding a nontoken to match the code that does "more than a single token on a line"
			ret.add(nonToken(""));
			ret.add(token(line));
		}

		return ret == null ? null : Collections.unmodifiableList(ret);
	}

	private Map<String, String> token(String realToken) {
		Map<String, String> currToken = new HashMap<>();
		currToken.put("isToken", getLayoutToken());
		currToken.put("displayText", this.getNameMappings().get(realToken));
		currToken.put("displaySize", this.getSizeMappings().get(realToken));
		currToken.put("codeName", realToken);
		return Collections.unmodifiableMap(currToken);
	}

	private Map<String, String> nonToken(String displayText) {
		Map<String, String> currNonToken = new HashMap<>();
		currNonToken.put("isToken", getNonLayoutToken());
		currNonToken.put("displayText", displayText);
		return Collections.unmodifiableMap(currNonToken);
	}

	public List<List<Map<String, String>>> getLines() {
		return tokenize().lines;
	}

	/**
	 * Returns the memoized tokenization, computing it if this is the first call or if the cache has
	 * been invalidated. Readers take no lock: the snapshot is immutable and published through a
	 * volatile field, so a reader either sees a fully built snapshot or none at all. Rebuilding is
	 * serialized on this template, both so that two threads cannot interleave their updates of
	 * {@link #maxTokens} and so that a concurrent setter cannot have its value overwritten by a rebuild
	 * that started before it.
	 *
	 * @return the current tokenization snapshot, never null
	 */
	private TokenizedLines tokenize() {
		TokenizedLines cached = tokenizedLines;
		if (cached != null && cached.isValid()) {
			return cached;
		}

		synchronized (this) {
			cached = tokenizedLines;
			if (cached != null && cached.isValid()) {
				return cached;
			}

			List<List<Map<String, String>>> ret = null;
			int newMaxTokens = this.maxTokens;
			LayoutSupport<?> support = null;
			int configurationVersion = 0;

			if (this.lineByLineFormat != null) {
				support = getLayoutSupportInstance();
				// read the version before the tokens it describes: stamping a snapshot with a version read
				// afterwards could label tokens that are already stale as current
				configurationVersion = support.getConfigurationVersion();
				List<String> specialTokens = nonUniqueStringsGoLast(support.getSpecialTokens());
				ret = new ArrayList<>(this.lineByLineFormat.size());
				for (String line : this.lineByLineFormat) {
					String tokenizedLine = replaceTokens(line, specialTokens);
					String[] nonTokens = tokenizedLine.split(LAYOUT_TOKEN);
					List<Map<String, String>> lineTokens = convertToTokens(line, nonTokens);
					if (lineTokens != null && newMaxTokens < lineTokens.size()) {
						newMaxTokens = lineTokens.size();
					}
					ret.add(lineTokens);
				}
				ret = Collections.unmodifiableList(ret);
			}

			this.maxTokens = newMaxTokens;
			cached = new TokenizedLines(ret, newMaxTokens, support, configurationVersion);
			tokenizedLines = cached;
			return cached;
		}
	}

	private synchronized void invalidateTokenizedLines() {
		tokenizedLines = null;
	}

	/**
	 * @return the codeName
	 */
	public String getCodeName() {
		return codeName;
	}

	/**
	 * @param codeName the codeName to set
	 */
	public void setCodeName(String codeName) {
		this.codeName = codeName;
	}

	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return the elementDefaults
	 */
	public Map<String, String> getElementDefaults() {
		return elementDefaults;
	}

	/**
	 * @param elementDefaults the elementDefaults to set
	 */
	public void setElementDefaults(Map<String, String> elementDefaults) {
		this.elementDefaults = elementDefaults;
	}

	/**
	 * Get the element regular expressions. These can be used to enforce that an element matches a
	 * regex.
	 *
	 * @return the elementRegex
	 */
	public Map<String, String> getElementRegex() {
		return elementRegex;
	}

	/**
	 * Set the element regular expressions. These can be used to enforce that an element matches a
	 * regex.
	 *
	 * @param elementRegex the elementRegex to set
	 */
	public void setElementRegex(Map<String, String> elementRegex) {
		this.elementRegex = elementRegex;
	}

	/**
	 * Get the element formats. These can be used to display an example format that an element should
	 * look like.
	 *
	 * @return the elementFormats
	 */
	public Map<String, String> getElementRegexFormats() {
		return elementRegexFormats;
	}

	/**
	 * Set the element formats. These can be used to display an example format that an element should
	 * look like.
	 *
	 * @param elementRegexFormats the elementFormats to set
	 */
	public void setElementRegexFormats(Map<String, String> elementRegexFormats) {
		this.elementRegexFormats = elementRegexFormats;
	}

	/**
	 * @return the lineByLineFormat
	 */
	public List<String> getLineByLineFormat() {
		return lineByLineFormat;
	}

	/**
	 * @param lineByLineFormat the lineByLineFormat to set
	 */
	public synchronized void setLineByLineFormat(List<String> lineByLineFormat) {
		this.lineByLineFormat = lineByLineFormat;
		invalidateTokenizedLines();
	}

	/**
	 * @return the requiredElements
	 */
	public List<String> getRequiredElements() {
		return requiredElements;
	}

	/**
	 * @param requiredElements the requiredElements to set
	 */
	public void setRequiredElements(List<String> requiredElements) {
		this.requiredElements = requiredElements;
	}

	/**
	 * @return the maxTokens
	 */
	public int getMaxTokens() {
		return tokenize().maxTokens;
	}

	/**
	 * @param maxTokens the maxTokens to set
	 */
	public synchronized void setMaxTokens(int maxTokens) {
		this.maxTokens = maxTokens;
		invalidateTokenizedLines();
	}

	/**
	 * @return the nameMappings
	 */
	public Map<String, String> getNameMappings() {
		return nameMappings;
	}

	/**
	 * @param nameMappings the nameMappings to set
	 */
	public synchronized void setNameMappings(Map<String, String> nameMappings) {
		this.nameMappings = nameMappings;
		invalidateTokenizedLines();
	}

	/**
	 * @return the sizeMappings
	 */
	public Map<String, String> getSizeMappings() {
		return sizeMappings;
	}

	/**
	 * @param sizeMappings the sizeMappings to set
	 */
	public synchronized void setSizeMappings(Map<String, String> sizeMappings) {
		this.sizeMappings = sizeMappings;
		invalidateTokenizedLines();
	}

	public abstract LayoutSupport<?> getLayoutSupportInstance();

	public List<String> nonUniqueStringsGoLast(List<String> strListArg) {
		List<String> dup = new ArrayList<>();
		// copy the list so we don't get concurrentmodification exceptions
		List<String> strList = new ArrayList<>(strListArg);
		for (String s : strList) {
			for (String sInner : strList) {
				if (sInner.contains(s) && s.length() < sInner.length() && !dup.contains(s)) {
					dup.add(s);
				}
			}
		}
		if (dup.size() > 1) {
			dup = nonUniqueStringsGoLast(dup);
		}
		strList.removeAll(dup);
		strList.addAll(dup);
		return strList;
	}

	/**
	 * Immutable snapshot of everything derived from a single tokenization pass. The support instance
	 * and its configuration version at the time of the pass are carried along so that a reader can tell
	 * whether the snapshot still reflects the current special tokens without having to resolve the
	 * support singleton itself, which for {@link org.openmrs.layout.address.AddressTemplate} means
	 * entering a synchronized block.
	 */
	private record TokenizedLines(List<List<Map<String, String>>> lines, int maxTokens, LayoutSupport<?> support,
													  int configurationVersion) {

		/**
		 * A snapshot built without a support is one for a template that has no line-by-line format: there
		 * is nothing for the special tokens to affect, so it stays valid until a setter clears it.
		 */
		private boolean isValid() {
			return support == null || support.getConfigurationVersion() == configurationVersion;
		}
	}

}
