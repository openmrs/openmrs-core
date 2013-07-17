/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC. All Rights Reserved.
 */
package org.openmrs.web.attribute.handler;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.openmrs.customdatatype.datatype.ProgramDatatype;
import org.openmrs.messagesource.MessageSourceService;
import org.springframework.stereotype.Component;

/**
 * Handler for the program custom datatype
 */
@Component
public class ProgramFieldGenDatatypeHandler implements FieldGenDatatypeHandler<ProgramDatatype, Program> {
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatypeHandler#setHandlerConfiguration(java.lang.String)
	 */
	@Override
	public void setHandlerConfiguration(String arg0) {
		// not used
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.FieldGenDatatypeHandler#getWidgetName()
	 */
	@Override
	public String getWidgetName() {
		return "program";
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.FieldGenDatatypeHandler#getValue(org.openmrs.customdatatype.CustomDatatype,
	 *      javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	@Override
	public Program getValue(org.openmrs.customdatatype.datatype.ProgramDatatype datatype, HttpServletRequest request,
	                        String formFieldName) throws InvalidCustomValueException {
		String result = request.getParameter(formFieldName);
		if (StringUtils.isBlank(result))
			return null;
		try {
			return Context.getProgramWorkflowService().getProgramByUuid(result);
		}
		catch (Exception e) {
			throw new InvalidCustomValueException("Invalid program: " + result);
		}
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.HtmlDisplayableDatatypeHandler#toHtmlSummary(org.openmrs.customdatatype.CustomDatatype,
	 *      java.lang.String)
	 */
	@Override
	public CustomDatatype.Summary toHtmlSummary(CustomDatatype<Program> datatype, String valueReference) {
		Program program = Context.getProgramWorkflowService().getProgramByUuid(valueReference);
		return new CustomDatatype.Summary(program.getName(), true);
	}
	
	@Override
	public Map<String, Object> getWidgetConfiguration() {
		MessageSourceService mss = Context.getMessageSourceService();
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("isNullable", "false");
		ret.put("label", mss.getMessage("general.true"));
		return ret;
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.HtmlDisplayableDatatypeHandler#toHtml(org.openmrs.customdatatype.CustomDatatype,
	 *      java.lang.String)
	 */
	@Override
	public String toHtml(CustomDatatype<Program> datatype, String valueReference) {
		Program program = Context.getProgramWorkflowService().getProgramByUuid(valueReference);
		return program.getName();
	}
	
}
