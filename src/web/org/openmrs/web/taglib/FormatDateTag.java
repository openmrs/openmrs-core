package org.openmrs.web.taglib;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FormatDateTag extends TagSupport {

	public static final long serialVersionUID = 121341222L;
	
	private final Log log = LogFactory.getLog(getClass());

	private Date date;
	private String type;
	private String format;

	public int doStartTag() {
		
		if (type != null) {
			if (type.equals("long")) {
				format = "MMMMM dd, yyyy h:mm a";
			}
			else if (type.equals("medium")) {
				format = "dd-MM-yyyy h:mm a";
			}
			else if (type.equals("textbox")) {
				format = "MM-dd-yyyy";
			}
			else if (type.equals("xml")) {
				format = "dd-MMM-yyyy";
			}
			else {
				format = "dd-MM-yyyy";
			}
		}
		
		if (format == null || format.equals(""))
			format = "dd-MMM-yyyy";
		
		String datestr = "";
		
		try {
			if (date != null && !date.equals("")) {
				//Date d = DateFormat.getDateInstance(DateFormat.SHORT).parse(var);
				datestr = new SimpleDateFormat(format).format(date).toString();
			}
		}
		/*catch (ParseException e) {
			//date is unparsable
			log.error("unable to parse date obj: " + var);
			log.error(e);
			datestr = var.toString();
		}*/
		catch (IllegalArgumentException e) {
			//format or date is invalid
			log.error("var: " + date);
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
