package org.openmrs.web.attribute.handler;

import org.apache.commons.lang.StringUtils;
import org.openmrs.attribute.InvalidAttributeValueException;
import org.openmrs.attribute.handler.CivilStatusAttributeHandler;
import org.openmrs.attribute.handler.CivilStatusAttributeHandler.CivilStatus;
import org.openmrs.attribute.handler.RaceAttributeHandler;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
@Order(0)
public class CivilStatusFieldGenAttributeHandler extends CivilStatusAttributeHandler implements FieldGenAttributeHandler<CivilStatus> {
	
	@Override
	public String getWidgetName() {
		return CivilStatus.class.getName();
	}
	
	@Override
	public Map<String, Object> getWidgetConfiguration() {
		return null;
	}
	
	@Override
	public CivilStatus getValue(HttpServletRequest request, String formFieldName) throws InvalidAttributeValueException {
		String value = request.getParameter(formFieldName);
		if (StringUtils.isBlank(value)) {
			return null;
		} else {
			return CivilStatus.valueOf(value);
		}
	}
	
}
