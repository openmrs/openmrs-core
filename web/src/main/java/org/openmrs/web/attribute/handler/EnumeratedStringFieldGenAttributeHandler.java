package org.openmrs.web.attribute.handler;

import org.openmrs.attribute.InvalidAttributeValueException;
import org.openmrs.attribute.handler.EnumeratedStringHandler;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(0)
public class EnumeratedStringFieldGenAttributeHandler extends EnumeratedStringHandler implements FieldGenAttributeHandler<String> {
	
	@Override
	public String getWidgetName() {
		return "java.util.List";
	}
	
	@Override
	public Map<String, Object> getWidgetConfiguration() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("list", values);
		return map;
	}
	
	@Override
	public String getValue(HttpServletRequest request, String formFieldName) throws InvalidAttributeValueException {
		return request.getParameter(formFieldName);
	}
	
}
