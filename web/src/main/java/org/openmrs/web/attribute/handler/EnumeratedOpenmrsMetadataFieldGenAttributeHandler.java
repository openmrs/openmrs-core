package org.openmrs.web.attribute.handler;

import org.openmrs.api.context.Context;
import org.openmrs.attribute.InvalidAttributeValueException;
import org.openmrs.attribute.handler.EnumeratedOpenmrsMetadata;
import org.openmrs.attribute.handler.EnumeratedOpenmrsMetadataAttributeHandler;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(0)
public class EnumeratedOpenmrsMetadataFieldGenAttributeHandler extends EnumeratedOpenmrsMetadataAttributeHandler implements FieldGenAttributeHandler<EnumeratedOpenmrsMetadata> {
	
	@Override
	public String getWidgetName() {
		return "org.openmrs."+hierarchy;
	}
	
	@Override
	public Map<String, Object> getWidgetConfiguration() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("allowed", allowedValues);
		return map;
	}
	
	@Override
	public EnumeratedOpenmrsMetadata getValue(HttpServletRequest request, String formFieldName) throws InvalidAttributeValueException {
		String stringValue=request.getParameter(formFieldName);
		if("Location".equals(hierarchy)){
			return Context.getLocationService().getLocation(Integer.valueOf(stringValue));
		}else if("Concept".equals(hierarchy)){
			return Context.getConceptService().getConcept(Integer.valueOf(stringValue));
		}
		throw new RuntimeException(hierarchy);	
	}
	
}
