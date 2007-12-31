package org.openmrs.module.web.extension;

import java.util.Map;

import org.openmrs.module.Extension;

public abstract class AdministrationSectionExt extends Extension {
	
	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	/**
	 * The title is used as the larger text above the links on the admin
	 * screen.  The returned String can be either straight up plain
	 * text or a Spring message code.
	 * 
	 * @return String title
	 */
	public abstract String getTitle();
	
	/**
	 * Returns the required privilege in order to see this section.  Can be a 
	 * comma delimited list of privileges.  
	 * If the default empty string is returned, only an authenticated 
	 * user is required
	 * 
	 * @return Privilege string
	 */
	public String getRequiredPrivilege() {
		return "";
	}
	
	/**
	 * The links are appear under the <code>getTitle<code> heading on the 
	 * admin screen.  Links can be either absolute or relative.  Title of the 
	 * links can be either plain text or Spring message codes.
	 * 
	 * In order to sort the links, you should use a <code>LinkedHashMap</code>.
	 * 
	 * @return Map<String, String> of <link, title>
	 */
	public abstract Map<String, String> getLinks();
		
}
