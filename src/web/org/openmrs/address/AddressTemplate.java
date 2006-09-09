package org.openmrs.address;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AddressTemplate {

	private final String ADDR_TOKEN = "<!-- openmrsToken -->";
	public static final String IS_ADDR_TOKEN = "IS_ADDR_TOKEN";
	public static final String IS_NOT_ADDR_TOKEN = "IS_NOT_ADDR_TOKEN";
	
	private String displayName;
	private String codeName;
	private String country;
	private Map<String,String> nameMappings;
	private Map<String,String> sizeMappings;
	private Map<String,String> elementDefaults;
	private List<String> lineByLineFormat;
	private int maxTokens = 0;
	
	private static Log log = LogFactory.getLog(AddressTemplate.class);

	private String replaceTokens(String line) {
		AddressSupport as = AddressSupport.getInstance();
		List<String> specialTokens = as.getSpecialTokens();
		
		for ( String token : specialTokens ) {
			line = line.replaceAll(token, this.ADDR_TOKEN);
		}
		
		return line;
	}
	
	private List<Map<String,String>> convertToTokens( String line, String[] nonTokens ) {
		List<Map<String,String>> ret = null;
		
		int numTokens = 0;
		
		if ( line != null && nonTokens != null && nonTokens.length > 0 ) {
			int idxCurr = -1;
			
			for ( int i = 0; i < nonTokens.length; i++ ) {
				String nonToken = nonTokens[i];
				if ( idxCurr + 1 < line.length() ) idxCurr = line.indexOf(nonToken, idxCurr + 1);
				
				if ( ret == null ) ret = new ArrayList<Map<String,String>>();

				if ( i == 0 && idxCurr > 0 ) {
					// this means there is a token at the beginning - we'll have to grab it
					Map<String,String> currToken = new HashMap<String,String>();
					currToken.put("isToken", AddressTemplate.IS_ADDR_TOKEN);
					String realToken = line.substring(0, idxCurr);
					currToken.put("displayText", this.getNameMappings().get(realToken));
					currToken.put("displaySize", this.getSizeMappings().get(realToken));
					currToken.put("codeName", realToken );
					numTokens++;

					ret.add(currToken);
				}
				
				if ( i < nonTokens.length - 1 ) {
					// this means we are still not at the last non-token, so let's add this non-token AND this token
					int idxNext = line.indexOf(nonTokens[i + 1], idxCurr + 1);

					Map<String,String> currNonToken = new HashMap<String,String>();
					currNonToken.put("isToken", AddressTemplate.IS_NOT_ADDR_TOKEN);
					currNonToken.put("displayText", nonToken);

					Map<String,String> currToken = new HashMap<String,String>();
					currToken.put("isToken", AddressTemplate.IS_ADDR_TOKEN);
					String realToken = line.substring(idxCurr + nonToken.length(), idxNext);
					currToken.put("displayText", this.getNameMappings().get(realToken));
					currToken.put("displaySize", this.getSizeMappings().get(realToken));
					currToken.put("codeName", realToken );
					numTokens++;
					
					ret.add(currNonToken);
					ret.add(currToken);
				} else {
					// we are on the last non-token, so check if it is the end
					Map<String,String> currNonToken = new HashMap<String,String>();
					currNonToken.put("isToken", AddressTemplate.IS_NOT_ADDR_TOKEN);
					currNonToken.put("displayText", nonToken);

					ret.add(currNonToken);

					if ( idxCurr + nonToken.length() >= line.length() ) {
						// we are at the end, so we are done
					} else {
						// we need to add one last token at the end
						Map<String,String> currToken = new HashMap<String,String>();
						currToken.put("isToken", AddressTemplate.IS_ADDR_TOKEN);
						String realToken = line.substring(idxCurr + nonToken.length());
						currToken.put("displayText", this.getNameMappings().get(realToken));
						currToken.put("displaySize", this.getSizeMappings().get(realToken));
						currToken.put("codeName", realToken );
						numTokens++;

						ret.add(currToken);
					}
				}
			}
		} else if ( line != null ) {
			if ( line.length() > 0 ) {
				// looks like we have a single token on a line by itself
				Map<String,String> currToken = new HashMap<String,String>();
				currToken.put("isToken", AddressTemplate.IS_ADDR_TOKEN);
				String realToken = line;
				currToken.put("displayText", this.getNameMappings().get(realToken));
				currToken.put("codeName", realToken );
				numTokens++;

				if ( ret == null ) ret = new ArrayList<Map<String,String>>();
				ret.add(currToken);
			}
		}
		
		if ( this.maxTokens < numTokens ) {
			this.maxTokens = numTokens;
		}

		return ret;
	}
	
	public List<List<Map<String,String>>> getLines() {
		List<List<Map<String,String>>> ret = null;
		
		if ( this.lineByLineFormat != null ) {
			for ( String line : this.lineByLineFormat ) {
				if ( ret == null ) ret = new ArrayList<List<Map<String,String>>>();
				List<Map<String,String>> lineTokens = new ArrayList<Map<String,String>>();
				String tokenizedLine = replaceTokens(line);
				String[] nonTokens = tokenizedLine.split(this.ADDR_TOKEN);
				lineTokens = convertToTokens(line, nonTokens);
				ret.add(lineTokens);
			}
			
			return ret;
		} else {
			return ret;
		}
	}
	
	/**
	 * @return Returns the codeName.
	 */
	public String getCodeName() {
		return codeName;
	}
	/**
	 * @param codeName The codeName to set.
	 */
	public void setCodeName(String codeName) {
		this.codeName = codeName;
	}
	/**
	 * @return Returns the displayName.
	 */
	public String getDisplayName() {
		return displayName;
	}
	/**
	 * @param displayName The displayName to set.
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	/**
	 * @return Returns the elementDefaults.
	 */
	public Map<String, String> getElementDefaults() {
		return elementDefaults;
	}
	/**
	 * @param elementDefaults The elementDefaults to set.
	 */
	public void setElementDefaults(Map<String, String> elementDefaults) {
		this.elementDefaults = elementDefaults;
	}
	/**
	 * @return Returns the lineByLineFormat.
	 */
	public List<String> getLineByLineFormat() {
		return lineByLineFormat;
	}
	/**
	 * @param lineByLineFormat The lineByLineFormat to set.
	 */
	public void setLineByLineFormat(List<String> lineByLineFormat) {
		this.lineByLineFormat = lineByLineFormat;
	}
	/**
	 * @return Returns the nameMappings.
	 */
	public Map<String, String> getNameMappings() {
		return nameMappings;
	}
	/**
	 * @param nameMappings The nameMappings to set.
	 */
	public void setNameMappings(Map<String, String> nameMappings) {
		this.nameMappings = nameMappings;
	}
	/**
	 * @return Returns the sizeMappings.
	 */
	public Map<String, String> getSizeMappings() {
		return sizeMappings;
	}
	/**
	 * @param sizeMappings The sizeMappings to set.
	 */
	public void setSizeMappings(Map<String, String> sizeMappings) {
		this.sizeMappings = sizeMappings;
	}

	/**
	 * @return Returns the maxTokens.
	 */
	public int getMaxTokens() {
		return maxTokens;
	}

	public String getCountry() {
		return this.country;
	}

	/**
	 * @param country The country to set.
	 */
	public void setCountry(String country) {
		this.country = country;
	}

}
