package org.openmrs.web.taglib.fieldgen;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.OrderType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.openmrs.web.taglib.FieldGenTag;

public class OrderTypeHandler extends AbstractFieldGenHandler implements FieldGenHandler {

	private String defaultUrl = "orderType.field";
	
	public void run() {
		setUrl(defaultUrl);

		/*
		if ( fieldGenTag != null ) {
			String startVal = this.fieldGenTag.getStartVal();
			String formFieldName = this.fieldGenTag.getFormFieldName();
			String emptySelectMessage = this.fieldGenTag.getEmptySelectMessage();
			String showBlankOption = this.fieldGenTag.getArgs().get("showBlank");
			String onChange = this.fieldGenTag.getArgs().get("onChange");
			
			HttpSession session = this.fieldGenTag.getPageContext().getSession();
			//HttpServletRequest request = (HttpServletRequest)this.fieldGenTag.getPageContext().getRequest();
			Context context = (Context)session.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);

			OrderService os = context.getOrderService();
			List<OrderType> orderTypes = os.getOrderTypes();
			if ( orderTypes == null ) orderTypes = new ArrayList<OrderType>();
			
			if ( orderTypes.size() > 0 ) {
				output = "<select name=\"" + formFieldName + "\"";
				if ( onChange != null ) if ( onChange.length() > 0 ) output += " onChange=\"" + onChange + ";\"";
				output += ">";
				if ( "true".equals(showBlankOption) ) output += "<option value=\"\"></option>";
				for ( OrderType ot : orderTypes ) {
					String currId = ot.getOrderTypeId().toString();
					output += "<option value=\"" + currId + "\"";
					output += (currId.equals(startVal)) ? " selected" : "";
					output += ">" + ot.getName() + "</option>";
				}
				output += "</select>";
			} else {
				output = emptySelectMessage;
			} 
		}
		
		return output;
		*/
	}
}
