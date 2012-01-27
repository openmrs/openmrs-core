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
package org.openmrs.web.controller.customdatatype;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.CustomDatatypeHandler;
import org.openmrs.web.attribute.handler.HtmlDisplayableDatatypeHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for accessing custom datatype values
 */
@Controller
public class DownloadCustomValueController {
	
	private final Log log = LogFactory.getLog(getClass());
	
	@RequestMapping(method = RequestMethod.GET, value = "**/downloadCustomValue.form")
	public void downloadCustomValue(HttpServletResponse response, @RequestParam("handler") String handlerClassname,
	        @RequestParam(value = "datatype", required = true) String datatypeClassname,
	        @RequestParam(value = "value", required = true) String valueReference) throws IOException {
		
		// get the handler
		Class handlerClass = null;
		HtmlDisplayableDatatypeHandler handler = null;
		
		// only try if the handlerClassname is not empty
		if (StringUtils.isNotBlank(handlerClassname)) {
			try {
				handlerClass = Context.loadClass(handlerClassname);
				handler = (HtmlDisplayableDatatypeHandler) handlerClass.newInstance();
			}
			catch (ClassNotFoundException ex) {
				log.warn("could not find handler class " + handlerClassname, ex);
			}
			catch (InstantiationException ex) {
				log.warn("could not instantiate handler class " + handlerClassname, ex);
			}
			catch (IllegalAccessException ex) {
				log.warn("could not access handler class " + handlerClassname, ex);
			}
		}
		
		// get the datatype
		Class datatypeClass = null;
		CustomDatatype datatype = null;
		try {
			datatypeClass = Context.loadClass(datatypeClassname);
			datatype = (CustomDatatype) datatypeClass.newInstance();
		}
		catch (ClassNotFoundException ex) {
			log.error("could not find datatype class " + datatypeClassname, ex);
		}
		catch (InstantiationException ex) {
			log.error("could not instantiate datatype class " + datatypeClassname, ex);
		}
		catch (IllegalAccessException ex) {
			log.error("could not access datatype class " + datatypeClassname, ex);
		}
		
		// die if not enough information
		if (datatype == null || StringUtils.isBlank(valueReference)) {
			throw new IOException("datatype and value are required parameters");
		}
		
		// render the output
		String data = null;
		if (handler != null) {
			data = handler.toHtml(datatype, valueReference); // the real fix for this class is TRUNK-3039
		} else {
			data = datatype.fromReferenceString(valueReference).toString();
		}
		
		if (data == null) {
			// resource does not exist; error out.
			log.error("The custom value with the reference " + valueReference + " cannot be found.");
			response.sendError(404);
			
		} else {
			// set the header
			// TODO make this work with non-text datatypes
			response.setHeader("Content-Type", "text/plain; charset=utf-8");
			
			// give it a filename
			String filename = "openmrs-custom-value_" + valueReference + ".txt";
			response.setHeader("Content-Disposition", "attachment; filename=" + filename);
			
			// write the resource as a string
			OutputStream out = response.getOutputStream();
			out.write(data.getBytes());
			out.flush();
		}
	}
}
