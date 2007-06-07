package org.openmrs.web.taglib.fieldgen;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.OrderType;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;

public class OrderTypeHandler extends AbstractFieldGenHandler implements FieldGenHandler {

	private String defaultUrl = "orderType.field";
	
	public void run() {
		setUrl(defaultUrl);

		if ( fieldGenTag != null ) {
			String initialValue = "";
			OrderType ot = (OrderType)this.fieldGenTag.getVal();
			if ( ot != null ) if ( ot.getOrderTypeId() != null ) initialValue = ot.getOrderTypeId().toString();

			String optionHeader = "";
			if ( this.fieldGenTag.getParameterMap() != null ) {
				optionHeader = (String) this.fieldGenTag.getParameterMap().get("optionHeader");
			}
			if ( optionHeader == null ) optionHeader = "";
			
			//HttpServletRequest request = (HttpServletRequest)this.fieldGenTag.getPageContext().getRequest();
			

			OrderService os = Context.getOrderService();
			List<OrderType> orderTypes = os.getOrderTypes();
			if ( orderTypes == null ) orderTypes = new ArrayList<OrderType>();
			
			setParameter("initialValue", initialValue);
			setParameter("optionHeader", optionHeader);
			setParameter("orderTypes", orderTypes);
		}
	}
}
