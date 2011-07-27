package org.openmrs.web.attribute.handler;

import org.apache.commons.lang.StringUtils;
import org.openmrs.attribute.InvalidAttributeValueException;
import org.openmrs.attribute.handler.CivilStatusAttributeHandler;
import org.openmrs.attribute.handler.CivilStatusAttributeHandler.CivilStatus;
import org.openmrs.attribute.handler.GenderAttributeHandler;
import org.openmrs.attribute.handler.GenderAttributeHandler.Gender;
import org.openmrs.attribute.handler.RaceAttributeHandler;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
@Order(0)
public class GenderFieldGenAttributeHandler extends GenderAttributeHandler implements FieldGenAttributeHandler<Gender> {
	
	@Override
	public String getWidgetName() {
		return Gender.class.getName();
	}
	
	@Override
	public Map<String, Object> getWidgetConfiguration() {
		return null;
	}
	
	@Override
	public Gender getValue(HttpServletRequest request, String formFieldName) throws InvalidAttributeValueException {
		String value = request.getParameter(formFieldName);
		if (StringUtils.isBlank(value)) {
			return null;
		} else {
			return Gender.valueOf(value);
		}
	}
	
}
