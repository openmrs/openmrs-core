package org.openmrs.web.taglib;

import java.io.BufferedInputStream;
import java.io.IOException;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class TemplateTag extends BodyTagSupport {

	public static final long serialVersionUID = 1L;

	private String title;

	public int doStartTag() throws JspTagException {
		include("/WEB-INF/template/header.html", pageContext.getOut());
		pageContext.setAttribute("templateTitle", title);
		return EVAL_BODY_BUFFERED;
	}

	public int doAfterBody() throws JspTagException {
		try {
			getPreviousOut().print(this.getBodyContent().getString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		include("/WEB-INF/template/footer.html", getPreviousOut());
		return EVAL_PAGE;
	}

	private void include(String path, JspWriter out) throws JspTagException {
		BufferedInputStream is = new BufferedInputStream(pageContext.getServletContext()
				.getResourceAsStream(path));
		byte[] buffer = new byte[1024];
		int numBytes;
		try {
			while ((numBytes = is.read(buffer)) > 0) {
				String s = new String(buffer, 0, numBytes);
				out.print(s);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new JspTagException(e);
		}
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
