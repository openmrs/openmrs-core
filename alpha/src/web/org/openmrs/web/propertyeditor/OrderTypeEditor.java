package org.openmrs.web.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.OrderType;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class OrderTypeEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	Context context;
	
	public OrderTypeEditor(Context c) {
		this.context = c;
	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		if (context != null) {
			OrderService os = context.getOrderService(); 
			if (StringUtils.hasText(text)) {
				try {
					setValue(os.getOrderType(Integer.valueOf(text)));
				}
				catch (Exception ex) {
					log.error(ex);
					throw new IllegalArgumentException("Order type not found: " + ex.getMessage());
				}
			}
			else {
				setValue(null);
			}
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
