package org.openmrs.web.taglib;

import java.io.IOException;

import javax.servlet.jsp.tagext.TagSupport;

public class CssTag extends TagSupport {

	public static final long serialVersionUID = 1L;

	public int doStartTag() {

		try {
			pageContext.getOut().println(
					"<link rel=\"stylesheet\" type=\"text/css\" "
							+ "href=\"/openmrs/openmrs.css\" />");
		} catch (IOException e) {
			// no CSS reference for you. tough luck.
		}

		return SKIP_BODY;
	}

}
