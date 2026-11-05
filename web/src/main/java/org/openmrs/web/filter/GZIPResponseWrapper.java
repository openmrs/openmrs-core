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

import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

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

	private final HttpServletRequest request;

	protected ServletOutputStream stream = null;

	protected PrintWriter writer = null;

	protected int error = 0;

	public GZIPResponseWrapper(HttpServletResponse response) {
		this(null, response);
	}

	public GZIPResponseWrapper(HttpServletRequest request, HttpServletResponse response) {
		super(response);
		this.request = request;
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
				if (stream != null && !((GZIPResponseStream) stream).closed()) {
					stream.close();
				}
			}
		} catch (IOException e) {
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
		if (writer != null) {
			return writer;
		}

		// From cmurphy@intechtual.com to fix:
		// https://appfuse.dev.java.net/issues/show_bug.cgi?id=59
		if (this.origResponse != null && this.origResponse.isCommitted()) {
			return super.getWriter();
		}

		if (stream != null) {
			throw new IllegalStateException("getOutputStream() has already been called!");
		}

		stream = createOutputStream();
		writer = new PrintWriter(new OutputStreamWriter(stream, origResponse.getCharacterEncoding()));

		return writer;
	}

	/**
	 * @see jakarta.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
	 */
	@Override
	public void sendError(int error, String message) throws IOException {
		if (isIncluded()) {
			super.sendError(error, message);
			return;
		}
		discardBufferedOutput();
		super.sendError(error, message);
		this.error = error;

		log.debug("sending error: {} [{}]", error, message);
	}

	@Override
	public void sendError(int error) throws IOException {
		sendError(error, null);
	}

	@Override
	public void setContentLength(int length) {
		if (isIncluded()) {
			super.setContentLength(length);
		}
	}

	@Override
	public void setContentLengthLong(long length) {
		if (isIncluded()) {
			super.setContentLengthLong(length);
		}
	}

	private boolean isIncluded() {
		return request != null && request.getDispatcherType() == DispatcherType.INCLUDE;
	}

	private void discardBufferedOutput() {
		writer = null;
		if (stream instanceof GZIPResponseStream) {
			((GZIPResponseStream) stream).discardBufferedOutput();
		}
	}
}
