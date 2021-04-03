/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps Response for GZipFilter
 * 
 * @author Matt Raible, cmurphy@intechtual.com
 */
public class GZIPResponseWrapper extends HttpServletResponseWrapper {
	
	private static final Logger log = LoggerFactory.getLogger(GZIPResponseWrapper.class);
	
	protected HttpServletResponse origResponse;
	
	protected ServletOutputStream stream = null;
	
	protected PrintWriter writer = null;
	
	protected int error = 0;
	
	public GZIPResponseWrapper(HttpServletResponse response) {
		super(response);
		origResponse = response;
	}
	
	public ServletOutputStream createOutputStream() throws IOException {
		return new GZIPResponseStream(origResponse);
	}
	
	public void finishResponse() {
		try {
			if (writer != null) {
				writer.close();
			} else {
				if (stream != null && !((GZIPResponseStream)stream).closed()) {
					stream.close();
				}
			}
		}
		catch (IOException e) {
			log.error("Error during closing writer or stream", e);
		}
	}
	
	@Override
	public void flushBuffer() throws IOException {
		if (stream != null) {
			stream.flush();
		}
	}
	
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (writer != null) {
			throw new IllegalStateException("getWriter() has already been called!");
		}
		
		if (stream == null) {
			stream = createOutputStream();
		}
		
		return stream;
	}
	
	@Override
	public PrintWriter getWriter() throws IOException {
		// From cmurphy@intechtual.com to fix:
		// https://appfuse.dev.java.net/issues/show_bug.cgi?id=59
		if (this.origResponse != null && this.origResponse.isCommitted()) {
			return super.getWriter();
		}
		
		if (writer != null) {
			return writer;
		}
		
		if (stream != null) {
			throw new IllegalStateException("getOutputStream() has already been called!");
		}
		
		stream = createOutputStream();
		writer = new PrintWriter(new OutputStreamWriter(stream, origResponse.getCharacterEncoding()));
		
		return writer;
	}
	
	/**
	 * @see javax.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
	 */
	@Override
	public void sendError(int error, String message) throws IOException {
		super.sendError(error, message);
		this.error = error;
		
		log.debug("sending error: {} [{}]", error, message);
	}
	
	public void setContentLength(int length) {
		//Intentionally left blank to ignore whatever length the caller sets, because
		//we are going to zip the response and hence end up with a smaller length.
		//Without this empty method, the base class's setContentLength() method will be
		//called, leading to the browser's waiting for more data than what we actually
		//have for the compressed output, hence slowing down the response. TRUNK-5978
	}
}
