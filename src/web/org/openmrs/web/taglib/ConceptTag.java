package org.openmrs.web.taglib;

import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;

public class ConceptTag extends BodyTagSupport {

	public static final long serialVersionUID = 1234324234333L;
	
	private final Log log = LogFactory.getLog(getClass());
	private Concept c = null;

	// Properties accessible through tag attributes
	private Integer conceptId;
	private String var;
	private String nameVar;
	private String numericVar;
    private String setMemberVar;
	private String locale;

	public int doStartTag() throws JspException {
		ConceptService cs = Context.getConceptService();
		
		// Search for a concept by id
		c = cs.getConcept(conceptId);
		if (c == null) {
			log.warn("ConceptTag is unable to find a concept with conceptId '" + conceptId + "'");
			return SKIP_BODY;
		}
		pageContext.setAttribute(var, c);
		log.debug("Found concept with id " + conceptId + ", set to variable: " + var);
		
		// If user specifies a locale in the tag, try to find a matching locale. Otherwise, use the user's default locale
		Locale loc = Context.getLocale();
		if (locale != null && !locale.equals("")) {
			Locale[] locales = Locale.getAvailableLocales();
			for (int i=0; i<locales.length; i++) {
				if(locale.equals(locales[i].toString())) {
					loc = locales[i];
					break;
				}
			}
		}
		ConceptName cName = c.getName(loc);
		pageContext.setAttribute(nameVar, cName);
		log.debug("Retrieved name " + cName.getName() + ", set to variable: " + nameVar);
		
		pageContext.setAttribute(numericVar, cs.getConceptNumeric(conceptId));
        
        // If the Concept is a Set, get members of that Set
        if (c.isSet() && setMemberVar != null) {
            pageContext.setAttribute(setMemberVar, Context.getConceptService().getConceptsInSet(c));
        }

		return EVAL_BODY_BUFFERED;
	}
	
	/**
	 * @see javax.servlet.jsp.tagext.Tag#doEndTag()
	 */
	public int doEndTag() throws JspException {
		try
        {
            if(bodyContent != null)
            	bodyContent.writeOut(bodyContent.getEnclosingWriter());
        }
        catch(java.io.IOException e)
        {
            throw new JspTagException("IO Error: " + e.getMessage());
        }
        return EVAL_PAGE;
	}

	/**
	 * @return the conceptId
	 */
	public Integer getConceptId() {
		return conceptId;
	}

	/**
	 * @param conceptId the conceptId to set
	 */
	public void setConceptId(Integer conceptId) {
		this.conceptId = conceptId;
	}

	/**
	 * @param var the var to set
	 */
	public void setVar(String var) {
		this.var = var;
	}
	
	/**
	 * @param nameVar the nameVar to set
	 */
	public void setNameVar(String nameVar) {
		this.nameVar = nameVar;
	}

	/**
	 * @return the locale
	 */
	public String getLocale() {
		return locale;
	}

	/**
	 * @param locale the locale to set
	 */
	public void setLocale(String locale) {
		this.locale = locale;
	}

	/**
	 * @return the numericVar
	 */
	public String getNumericVar() {
		return numericVar;
	}

	/**
	 * @param numericVar the numericVar to set
	 */
	public void setNumericVar(String numericVar) {
		this.numericVar = numericVar;
	}

    /**
     * @return the SetMemberVar
     */
    public String getSetMemberVar() {
        return setMemberVar;
    }

    /**
     * @param SetMemberVar the SetMemberVar to set
     */
    public void setSetMemberVar(String setMemberVar) {
        this.setMemberVar = setMemberVar;
    }
}
