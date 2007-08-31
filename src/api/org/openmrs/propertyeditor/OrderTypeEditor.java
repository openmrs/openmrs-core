package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.OrderType;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class OrderTypeEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	public OrderTypeEditor() {	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		OrderService os = Context.getOrderService(); 
		if (StringUtils.hasText(text)) {
			try {
				setValue(os.getOrderType(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				log.error("Error setting text: " + text, ex);
				throw new IllegalArgumentException("Order type not found: " + ex.getMessage());
			}
		}
		else {
			setValue(null);
		}
	}

	public String getAsText() {
		OrderType ot = (OrderType)getValue();
		if (ot == null) {
			return "";
		} else {
			Integer orderTypeId = ot.getOrderTypeId();
			if ( orderTypeId == null ) {
				return "";
			} else {
				return orderTypeId.toString();
			}
		}
	}

}
