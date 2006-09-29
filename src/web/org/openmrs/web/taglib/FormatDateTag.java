package org.openmrs.web.taglib;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;

public class FormatDateTag extends TagSupport {

	public static final long serialVersionUID = 121341222L;
	
	private final Log log = LogFactory.getLog(getClass());

	private Date date;
	private String type;
	private String format;

	public int doStartTag() {
		
		DateFormat dateFormat = null;
		
		if (type == null)
			type = "";
		
		if (format != null && format.length() > 0) {
			dateFormat = new SimpleDateFormat(format);
		}
		else if (type.equals("xml")) {
			dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		}
		else {
			log.debug("context locale: " + Context.getLocale());
			
			if (type.equals("long")) {
				dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Context.getLocale());
			}
			else if (type.equals("medium")) {
				dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Context.getLocale());
			}
			else if (type.equals("textbox")) {
				dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Context.getLocale());
			}
			else {
				dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Context.getLocale());
			}
		}
		
		if (dateFormat == null)
			dateFormat = new SimpleDateFormat("MM-dd-yyyy");
		
		String datestr = "";
		
		try {
			if (date != null && !date.equals("")) {
				datestr = dateFormat.format(date).toString();
			}
		}
		catch (IllegalArgumentException e) {
			//format or date is invalid
			log.error("date: " + date);
			log.error("format: " + format);
			log.error(e);
			datestr = date.toString();
		}
		
		try {
			pageContext.getOut().write(datestr);
		}
		catch (IOException e) {
			log.error(e);
		}
		
		return SKIP_BODY;
	}
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
