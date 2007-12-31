package org.openmrs.layout.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Generic class used by AddressTemplate and NameTemplate layouts

 */
public abstract class LayoutTemplate {

	protected final String LAYOUT_TOKEN = "<!-- openmrsToken -->";
	
	protected String displayName;
	protected String codeName;
	protected String country;
	protected Map<String,String> nameMappings;
	protected Map<String,String> sizeMappings;
	protected Map<String,String> elementDefaults;
	protected List<String> lineByLineFormat;
	protected int maxTokens = 0; // The largest number of tokens on one given line
	
	public abstract String getLayoutToken();
	public abstract String getNonLayoutToken();
	
	private String replaceTokens(String line) {
		LayoutSupport as = getLayoutSupportInstance();
		List<String> specialTokens = as.getSpecialTokens();
		
		for ( String token : specialTokens ) {
			line = line.replaceAll(token, this.LAYOUT_TOKEN);
		}
		
		return line;
	}
	
	private List<Map<String,String>> convertToTokens( String line, String[] nonTokens ) {
		List<Map<String,String>> ret = null;
		
		//int numTokens = 0;
		
		if ( line != null && nonTokens != null && nonTokens.length > 0 ) {
			int idxCurr = -1;
			
			for ( int i = 0; i < nonTokens.length; i++ ) {
				String nonToken = nonTokens[i];
				if ( idxCurr + 1 < line.length() ) idxCurr = line.indexOf(nonToken, idxCurr + 1);
				
				if ( ret == null ) ret = new Vector<Map<String,String>>();

				if ( i == 0 && idxCurr > 0 ) {
					// this means there is a token at the beginning - we'll have to grab it
					Map<String,String> currToken = new HashMap<String,String>();
					currToken.put("isToken", getLayoutToken());
					String realToken = line.substring(0, idxCurr);
					currToken.put("displayText", this.getNameMappings().get(realToken));
					currToken.put("displaySize", this.getSizeMappings().get(realToken));
					currToken.put("codeName", realToken );
					//numTokens++;

					ret.add(currToken);
				}
				
				if ( i < nonTokens.length - 1 ) {
					// this means we are still not at the last non-token, so let's add this non-token AND this token
					int idxNext = line.indexOf(nonTokens[i + 1], idxCurr + 1);

					Map<String,String> currNonToken = new HashMap<String,String>();
					currNonToken.put("isToken", getNonLayoutToken());
					currNonToken.put("displayText", nonToken);

					Map<String,String> currToken = new HashMap<String,String>();
					currToken.put("isToken", getLayoutToken());
					String realToken = line.substring(idxCurr + nonToken.length(), idxNext);
					currToken.put("displayText", this.getNameMappings().get(realToken));
					currToken.put("displaySize", this.getSizeMappings().get(realToken));
					currToken.put("codeName", realToken );
					//numTokens++;
					
					ret.add(currNonToken);
					ret.add(currToken);
				} else {
					// we are on the last non-token, so check if it is the end
					Map<String,String> currNonToken = new HashMap<String,String>();
					currNonToken.put("isToken", getNonLayoutToken());
					currNonToken.put("displayText", nonToken);

					ret.add(currNonToken);

					if ( idxCurr + nonToken.length() >= line.length() ) {
						// we are at the end, so we are done
					} else {
						// we need to add one last token at the end
						Map<String,String> currToken = new HashMap<String,String>();
						currToken.put("isToken", getLayoutToken());
						String realToken = line.substring(idxCurr + nonToken.length());
						currToken.put("displayText", this.getNameMappings().get(realToken));
						currToken.put("displaySize", this.getSizeMappings().get(realToken));
						currToken.put("codeName", realToken );
						//numTokens++;

						ret.add(currToken);
					}
				}
			}
		} else if ( line != null ) {
			if ( line.length() > 0 ) {
				// looks like we have a single token on a line by itself
				if ( ret == null ) ret = new Vector<Map<String,String>>();
				Map<String,String> currToken = new HashMap<String,String>();
				
				// adding a nontoken to match the code that does "more than a single token on a line"
				Map<String,String> currNonToken = new HashMap<String,String>();
				currNonToken.put("isToken", getNonLayoutToken());
				currNonToken.put("displayText", "");
				ret.add(currNonToken);
				
				currToken.put("isToken", getLayoutToken());
				String realToken = line;
				currToken.put("displayText", this.getNameMappings().get(realToken));
				currToken.put("displaySize", this.getSizeMappings().get(realToken));
				currToken.put("codeName", realToken );
				//numTokens++;

				ret.add(currToken);
			}
		}
		
		if ( this.maxTokens < ret.size() ) {
			this.maxTokens = ret.size();
		}

		return ret;
	}
	
	public List<List<Map<String,String>>> getLines() {
		List<List<Map<String,String>>> ret = null;
		
		if ( this.lineByLineFormat != null ) {
			for ( String line : this.lineByLineFormat ) {
				if ( ret == null ) ret = new Vector<List<Map<String,String>>>();
				String tokenizedLine = replaceTokens(line);
				String[] nonTokens = tokenizedLine.split(this.LAYOUT_TOKEN);
				List<Map<String,String>> lineTokens = convertToTokens(line, nonTokens);
				ret.add(lineTokens);
			}
			
			return ret;
		} else {
			return ret;
		}
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
	 * @return the lineByLineFormat
	 */
	public List<String> getLineByLineFormat() {
		return lineByLineFormat;
	}

	/**
	 * @param lineByLineFormat the lineByLineFormat to set
	 */
	public void setLineByLineFormat(List<String> lineByLineFormat) {
		this.lineByLineFormat = lineByLineFormat;
	}

	/**
	 * @return the maxTokens
	 */
	public int getMaxTokens() {
		if (maxTokens == -1)
			getLines(); // initialize the maxTokens variable
		
		return maxTokens;
	}

	/**
	 * @param maxTokens the maxTokens to set
	 */
	public void setMaxTokens(int maxTokens) {
		this.maxTokens = maxTokens;
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
	public void setNameMappings(Map<String, String> nameMappings) {
		this.nameMappings = nameMappings;
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
	public void setSizeMappings(Map<String, String> sizeMappings) {
		this.sizeMappings = sizeMappings;
	}
	
	public abstract LayoutSupport getLayoutSupportInstance();
	
}