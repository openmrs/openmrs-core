/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.customdatatype;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.CustomDatatypeHandler;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.customdatatype.DownloadableDatatypeHandler;
import org.openmrs.web.attribute.handler.HtmlDisplayableDatatypeHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for accessing custom datatype values
 */
@Controller
public class CustomValueController {
	
	private final Log log = LogFactory.getLog(getClass());
	
	private static final String HTML_START = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\"><head></head><body>";
	
	private static final String HTML_END = "</body></html>";
	
	/**
	 * Displays a custom value, as HTML, in its own page.
	 *
	 * @param handlerClassname
	 * @param datatypeClassname
	 * @param valueReference
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "**/viewCustomValue.form")
	@ResponseBody
	public String viewCustomValue(@RequestParam("handler") String handlerClassname,
	        @RequestParam(value = "datatype", required = true) String datatypeClassname,
	        @RequestParam(value = "value", required = true) String valueReference) throws IOException {
		
		CustomDatatype<?> datatype = CustomDatatypeUtil.getDatatype(datatypeClassname, null);
		HtmlDisplayableDatatypeHandler handler = (HtmlDisplayableDatatypeHandler) CustomDatatypeUtil.getHandler(datatype,
		    handlerClassname, null);
		
		// die if not enough information
		if (datatype == null || StringUtils.isBlank(valueReference)) {
			throw new IOException("datatype and value are required parameters");
		}
		
		String html = handler.toHtml(datatype, valueReference);
		return HTML_START + html + HTML_END;
	}
	
	/**
	 * Downloads a custom value
	 *
	 * @param response
	 * @param handlerClassname
	 * @param datatypeClassname
	 * @param valueReference
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "**/downloadCustomValue.form")
	public void downloadCustomValue(HttpServletResponse response, @RequestParam("handler") String handlerClassname,
	        @RequestParam(value = "datatype", required = true) String datatypeClassname,
	        @RequestParam(value = "value", required = true) String valueReference) throws IOException {
		
		// get the datatype and handler
		CustomDatatype datatype = CustomDatatypeUtil.getDatatype(datatypeClassname, null);
		CustomDatatypeHandler<?, ?> handler = CustomDatatypeUtil.getHandler(datatype, handlerClassname, null);
		
		if (!(handler instanceof DownloadableDatatypeHandler)) {
			throw new IllegalArgumentException(handler.getClass().getName() + " does not support downloading");
		}
		
		// die if not enough information
		if (datatype == null || StringUtils.isBlank(valueReference)) {
			throw new IOException("datatype and value are required parameters");
		}
		
		// render the output
		DownloadableDatatypeHandler<?> downloadHandler = (DownloadableDatatypeHandler<?>) handler;
		response.setHeader("Content-Type", downloadHandler.getContentType(datatype, valueReference));
		
		String filename = downloadHandler.getFilename(datatype, valueReference);
		if (filename == null) {
			filename = "openmrs-custom-value_" + valueReference + ".txt";
		}
		response.setHeader("Content-Disposition", "attachment; filename=" + filename);
		
		// write the resource as a string
		OutputStream out = response.getOutputStream();
		downloadHandler.writeToStream(datatype, valueReference, out);
		out.flush();
	}
}
