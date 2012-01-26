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
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.attribute.handler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.openmrs.customdatatype.datatype.LongFreeTextDatatype;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

/**
 * Handler for the {@link LongFreeTextDatatype} that displays as a file upload input.
 * @since 1.9
 */
@Component
public class LongFreeTextFileUploadHandler implements WebDatatypeHandler<LongFreeTextDatatype, String> {
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatypeHandler#setHandlerConfiguration(java.lang.String)
	 */
	@Override
	public void setHandlerConfiguration(String handlerConfig) {
		// no configuration options are supported
	}
	
	/**
	 * @see org.openmrs.customdatatype.CustomDatatypeHandler#render(org.openmrs.customdatatype.CustomDatatype, java.lang.String, java.lang.String)
	 */
	@Override
	public String render(LongFreeTextDatatype datatype, String referenceString, String view) {
		
		// send the entire file's contents if the view is "download"
		if (CustomDatatype.VIEW_DOWNLOAD.equals(view)) {
			return datatype.render(referenceString, view);
		}
		
		// otherwise, build a link to it
		StringBuilder sb = new StringBuilder();
		sb.append("<a href=\"");
		sb.append("downloadCustomValue.form");
		sb.append("?handler=");
		sb.append(this.getClass().getName());
		sb.append("&datatype=");
		sb.append(LongFreeTextDatatype.class.getName());
		sb.append("&value=");
		sb.append(referenceString);
		sb.append("\">");
		sb.append(Context.getMessageSourceService().getMessage("general.download", null, Context.getLocale()));
		sb.append("</a>");
		return sb.toString();
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.WebDatatypeHandler#getWidgetHtml(org.openmrs.customdatatype.CustomDatatype, java.lang.String, java.lang.Object)
	 */
	@Override
	public String getWidgetHtml(LongFreeTextDatatype datatype, String formFieldName, String startingValue) {
		return "<input type=\"file\" name=\"" + formFieldName + "\"/>";
		/* TODO add something like this
		var form = jq('#${ id }').closest('form');
		if (form.length) {
			form.attr('method', 'post');
			form.attr('enctype', 'multipart/form-data');
		}
		*/
	}
	
	/**
	 * @see org.openmrs.web.attribute.handler.WebDatatypeHandler#getValue(org.openmrs.customdatatype.CustomDatatype, javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	@Override
	public String getValue(LongFreeTextDatatype datatype, HttpServletRequest request, String formFieldName)
	        throws InvalidCustomValueException {
		if (request instanceof MultipartRequest) {
			MultipartFile file = ((MultipartRequest) request).getFile(formFieldName);
			try {
				return new String(file.getBytes());
			}
			catch (IOException e) {
				throw new InvalidCustomValueException("Error handling file upload as a String", e);
			}
		} else {
			throw new IllegalArgumentException(
			        "Programming error: file upload handler can only be used in a form with enctype='multipart/form-data'");
		}
	}
	
}
