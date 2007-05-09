package org.openmrs.module.web.extension;

import org.openmrs.module.Extension;

public abstract class LinkExt extends Extension {
	
	public MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}

	/**
	 * @return The message code of the label of this link
	 */
	public abstract String getLabel();

	/**
	 * @return The url that this link should go to
	 */
	public abstract String getUrl();
	
	/**
	 * @return The privilege the user must have to see this link
	 */
	public abstract String getRequiredPrivilege();

	/**
	 * This extension does not have a place for any default content to go
	 */
	public final String getOverrideContent(String bodyContent) {
		return null;
	}

}
