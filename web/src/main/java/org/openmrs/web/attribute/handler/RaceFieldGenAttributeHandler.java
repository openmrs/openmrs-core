package org.openmrs.web.attribute.handler;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.openmrs.attribute.InvalidAttributeValueException;
import org.openmrs.attribute.handler.Race;
import org.openmrs.attribute.handler.RaceAttributeHandler;


public class RaceFieldGenAttributeHandler extends RaceAttributeHandler
		implements FieldGenAttributeHandler<Race> {

	@Override
	public String getWidgetName() {
		return "org.openmrs.attribute.handler.Race";
	}

	@Override
	public Map<String, Object> getWidgetConfiguration() {
		return null;
	}

	@Override
	public Race getValue(HttpServletRequest request, String formFieldName)
			throws InvalidAttributeValueException {
		String value = request.getParameter(formFieldName);
		if(StringUtils.isBlank(value)){
			return null;
		}else{
			return Race.valueOf(value);
		}
	}

}
