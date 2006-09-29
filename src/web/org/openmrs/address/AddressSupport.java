package org.openmrs.address;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;

public class AddressSupport {

	private static AddressSupport singleton;
	
	private static Log log = LogFactory.getLog(AddressSupport.class);
	
	private String defaultAddressFormat;
	private List<AddressTemplate> addressTemplates;
	private List<String> specialTokens;
	
	public AddressSupport() {
		if (singleton == null)
			singleton = this;
	}
	
	public static AddressSupport getInstance() {
		if (singleton == null)
			throw new RuntimeException("Not Yet Instantiated");
		else {
			return singleton;			
		}
	}

	/**
	 * @return Returns the addressTemplates.
	 */
	public List<AddressTemplate> getAddressTemplates() {
		return addressTemplates;
	}

	/**
	 * @param addressTemplates The addressTemplates to set.
	 */
	public void setAddressTemplates(List<AddressTemplate> addressTemplates) {
		this.addressTemplates = addressTemplates;
	}

	/**
	 * @return Returns the defaultAddressTemplate.
	 */
	public AddressTemplate getDefaultAddressTemplate() {
		return getAddressTemplateByName(getDefaultAddressFormat());
	}

	public AddressTemplate getAddressTemplateByName(String templateName) {
		if ( this.addressTemplates != null && templateName != null ) {
			AddressTemplate ret = null;
			
			for ( AddressTemplate at : this.addressTemplates ) {
				if ( at != null ) {
					if ( templateName.equalsIgnoreCase(at.getDisplayName())
							|| templateName.equalsIgnoreCase(at.getCodeName())
							|| templateName.equalsIgnoreCase(at.getCountry())) {
						ret = at;
						log.debug("Found Address Template named " + at.getDisplayName());
					}
				}
			}
			
			return ret;
		} else {
			log.debug("No Address Templates defined");
			return null;
		}
	}

	public AddressTemplate getAddressTemplateByCodeName(String templateName) {
		if ( this.addressTemplates != null && templateName != null ) {
			AddressTemplate ret = null;
			
			for ( AddressTemplate at : this.addressTemplates ) {
				if ( at != null ) {
					if ( templateName.equalsIgnoreCase(at.getCodeName())) {
						ret = at;
						log.debug("Found Address Template named " + at.getDisplayName());
					}
				}
			}
			
			return ret;
		} else {
			log.debug("No Address Templates defined");
			return null;
		}
	}

	public AddressTemplate getAddressTemplateByCountry(String templateName) {
		if ( this.addressTemplates != null && templateName != null ) {
			AddressTemplate ret = null;
			
			for ( AddressTemplate at : this.addressTemplates ) {
				if ( at != null ) {
					if ( templateName.equalsIgnoreCase(at.getCountry())) {
						ret = at;
						log.debug("Found Address Template named " + at.getDisplayName());
					}
				}
			}
			
			return ret;
		} else {
			log.debug("No Address Templates defined");
			return null;
		}
	}

	public AddressTemplate getAddressTemplateByDisplayName(String templateName) {
		if ( this.addressTemplates != null && templateName != null ) {
			AddressTemplate ret = null;
			
			for ( AddressTemplate at : this.addressTemplates ) {
				if ( at != null ) {
					if ( templateName.equalsIgnoreCase(at.getDisplayName())) {
						ret = at;
						log.debug("Found Address Template named " + at.getDisplayName());
					}
				}
			}
			
			return ret;
		} else {
			log.debug("No Address Templates defined");
			return null;
		}
	}

	/**
	 * @return Returns the specialTokens.
	 */
	public List<String> getSpecialTokens() {
		return specialTokens;
	}

	/**
	 * @param specialTokens The specialTokens to set.
	 */
	public void setSpecialTokens(List<String> specialTokens) {
		this.specialTokens = specialTokens;
	}

	/**
	 * @return Returns the defaultAddressFormat.
	 */
	public String getDefaultAddressFormat() {
		String ret = Context.getAdministrationService().getGlobalProperty("address.format");
		return (ret != null && ret.length() > 0) ? ret : defaultAddressFormat;
	}

	/**
	 * @param defaultAddressFormat The defaultAddressFormat to set.
	 */
	public void setDefaultAddressFormat(String defaultAddressFormat) {
		this.defaultAddressFormat = defaultAddressFormat;
	}
}
