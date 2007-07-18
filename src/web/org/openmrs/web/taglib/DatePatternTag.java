package org.openmrs.web.taglib;

import java.text.SimpleDateFormat;

import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.OpenmrsUtil;

/**
 * Returns a string like mm/dd/yyyy for the current user
 */
public class DatePatternTag extends TagSupport {

	private static final long serialVersionUID = 122L;

	private final Log log = LogFactory.getLog(getClass());

	/**
	 * Does the actual working of printing the date pattern
	 * 
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() {

		SimpleDateFormat dateFormat = OpenmrsUtil.getDateFormat();

		try {
			pageContext.getOut().write(
			        dateFormat.toLocalizedPattern().toLowerCase());

		} catch (Exception e) {
			log.error("error getting date pattern", e);
		}

		return SKIP_BODY;
	}

}
