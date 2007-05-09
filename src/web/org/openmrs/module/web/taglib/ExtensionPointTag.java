package org.openmrs.module.web.taglib;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.Extension;
import org.openmrs.module.ModuleFactory;
import org.openmrs.util.OpenmrsUtil;

public class ExtensionPointTag extends TagSupport implements BodyTag {

	// private variables
	public static final long serialVersionUID = 12323003L;
	private final Log log = LogFactory.getLog(getClass());
	private Iterator<Extension> extensions;
	private Map<String, String> parameterMap;
	private BodyContent bodyContent = null;
	
	// tag attributes
	private String pointId;
	private String parameters = "";
	private String type = "html"; // all tags using this should default to 'html' media type
	
	// tag helpers
	private Boolean firstIteration = true;
	
	// methods
	public int doStartTag() {
		log.debug("Starting tag for extension point: " + pointId);
		
		// "zero out" extension list
		extensions = null;
		
		parameterMap = OpenmrsUtil.parseParameterList(parameters);
		
		List<Extension> extensionList = null;
		
		if (type != null && type.length() > 0) {
			try {
				Extension.MEDIA_TYPE mediaType = Enum.valueOf(Extension.MEDIA_TYPE.class, type);
				log.debug("Getting extensions: " + pointId + " : " + mediaType);
				extensionList = ModuleFactory.getExtensions(pointId, mediaType);
			}
			catch (IllegalArgumentException e) {
				log.warn("extension point type: '" + type + "' is invalid. Must be enum of Extension.MEDIA_TYPE", e);
			}
		}
		else {
			log.debug("Getting extensions: " + pointId);
			extensionList = ModuleFactory.getExtensions(pointId);
		}
		
		if (extensionList != null) {
			log.debug("Found " + extensionList.size() + " extensions");
			extensions = extensionList.iterator();
		}
		
		if (extensions == null || extensions.hasNext() == false) {
			extensions = null;
			return SKIP_BODY;
		}
		else {
			firstIteration = true;
			return EVAL_BODY_BUFFERED;
		}
		
	}
	
	/**
	 * @see javax.servlet.jsp.tagext.BodyTag#doInitBody()
	 */
	public void doInitBody() throws JspException {
		getBodyContent().clearBody();
		pageContext.removeAttribute("extension");
		return;
	}

	/**
	 * @see javax.servlet.jsp.tagext.IterationTag#doAfterBody()
	 */
	public int doAfterBody() throws JspException {
		if(extensions.hasNext()) {
			if (firstIteration) {
				// for some reason the body is getting evaluated after the doInitBody() call
				// and before this.  Instead of hacking in duplicated logic, I use this hack
				bodyContent.clearBody();
				firstIteration = false;
			}
        	Extension ext = extensions.next();
        	String overrideContent = ext.getOverrideContent(getBodyContentString());
			if (overrideContent == null) {
				iterate(ext);
			}
			else {
				try {
					bodyContent.getEnclosingWriter().write(overrideContent);
				}
				catch (IOException io) {
					log.warn("Cannot write override content of extension: " + ext.toString(), io);
				}
				if (!extensions.hasNext())
					return SKIP_BODY;
			}
			return EVAL_BODY_BUFFERED;
        }
		
		return SKIP_BODY;
	}
	
	private void iterate(Extension ext) {
		if (ext != null) {
			ext.initialize(parameterMap);
			log.debug("Adding ext: " + ext.getExtensionId() + " to pageContext class: " + ext.getClass());
			pageContext.setAttribute("extension", ext);
		}
		else {
			pageContext.removeAttribute("extension");
		}
	}

	/**
	 * @see javax.servlet.jsp.tagext.Tag#doEndTag()
	*/
	public int doEndTag() throws JspException {
		try
        {
			if(getBodyContent() != null) {
				log.debug("Ending tag: " + bodyContent.getString());
				if (extensions != null)
					getBodyContent().writeOut(bodyContent.getEnclosingWriter());
				bodyContent.clearBody();
			}
			else {
				// the tag doesn't have a body, so initBody and doAfterBody have 
				// not been called.  Do iterations now
				while (extensions != null && extensions.hasNext()) {
					Extension ext = extensions.next();
					ext.initialize(parameterMap);
					String overrideContent = ext.getOverrideContent("");
					if (overrideContent != null)
						pageContext.getOut().write(overrideContent);
				}
			}
        }
        catch(java.io.IOException e)
        {
            throw new JspTagException("IO Error while ending tag for point: " + pointId, e);
        }
        release();
        return EVAL_PAGE;
	}
	
	@Override
	public void release() {
		extensions = null;
		pointId = null;
		type = null;
		if (bodyContent != null)
			bodyContent.clearBody();
		bodyContent = null;
		super.release();
	}

	public String getPointId() {
		return pointId;
	}

	public void setPointId(String pointId) {
		this.pointId = pointId;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public void setBodyContent(BodyContent b) {
		this.bodyContent = b;
	}
	
	public BodyContent getBodyContent() {
		return bodyContent;
	}
	
	public String getBodyContentString() {
		if (bodyContent == null)
			return "";
		else
			return bodyContent.getString();
	}
	
}
