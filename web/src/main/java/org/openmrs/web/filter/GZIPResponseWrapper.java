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
import java.util.Collection;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps Response for GZipFilter. The wrapper buffers the response until it knows whether it is worth
 * compressing, and never lets the uncompressed length reach the client for a response it compresses.
 * 
 * @author Matt Raible, cmurphy@intechtual.com
 */
public class GZIPResponseWrapper extends HttpServletResponseWrapper {
	
	private static final Logger log = LoggerFactory.getLogger(GZIPResponseWrapper.class);
	
	protected HttpServletResponse origResponse;
	
	protected ServletOutputStream stream = null;
	
	protected PrintWriter writer = null;
	
	protected int error = 0;
	
	private final boolean headRequest;
	
	/**
	 * The length the caller asked for, held back until it is known whether the response will be
	 * compressed. A compressed response must never carry the uncompressed length, but an uncompressed
	 * one should keep the length it was given.
	 */
	private Long pendingContentLength = null;
	
	/**
	 * Whether the {@code Content-Encoding: gzip} header was set by this wrapper (as opposed to the
	 * application), so that it can be treated as ours when deciding whether compression is still
	 * allowed and when a reset keeps the headers.
	 */
	private boolean compressionHeadersWritten;
	
	public GZIPResponseWrapper(HttpServletResponse response) {
		this(null, response);
	}
	
	public GZIPResponseWrapper(HttpServletRequest request, HttpServletResponse response) {
		super(response);
		origResponse = response;
		headRequest = request != null && "HEAD".equalsIgnoreCase(request.getMethod());
		addVaryHeader();
	}
	
	public ServletOutputStream createOutputStream() throws IOException {
		return new GZIPResponseStream(this, origResponse);
	}
	
	public void finishResponse() {
		if (error != 0) {
			// sendError() writes the container's error page straight to the real response, so there is
			// nothing buffered here to finish
			return;
		}
		
		try {
			if (writer != null) {
				writer.close();
			} else if (stream != null) {
				if (!((GZIPResponseStream) stream).closed()) {
					stream.close();
				}
			} else {
				// no body was written (for example a HEAD request that only sets the file size), so
				// honour the pending length instead of dropping it
				applyPendingContentLength();
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
		discardBufferedOutput();
		this.error = error;
		
		log.debug("sending error: {} [{}]", error, message);
		
		super.sendError(error, message);
	}
	
	/**
	 * @see javax.servlet.http.HttpServletResponse#sendError(int)
	 */
	@Override
	public void sendError(int error) throws IOException {
		discardBufferedOutput();
		this.error = error;
		
		log.debug("sending error: {}", error);
		
		super.sendError(error);
	}
	
	private void discardBufferedOutput() {
		pendingContentLength = null;
		if (stream instanceof GZIPResponseStream) {
			// mark the stream abandoned before flushing the writer, so its pending bytes are dropped
			// rather than streamed (and committing the response) on their way out
			((GZIPResponseStream) stream).discard();
		}
		flushWriter();
		
		if (compressionHeadersWritten && !origResponse.isCommitted()) {
			// the container writes the error page straight to the real response without compressing
			// it, so the Content-Encoding we announced for the abandoned body must not survive.
			// reset() is the only portable way to remove a header, but sendError must preserve
			// cookies, so they are put back.
			String[] cookies = origResponse.getHeaders("Set-Cookie").toArray(new String[0]);
			super.reset();
			for (String cookie : cookies) {
				origResponse.addHeader("Set-Cookie", cookie);
			}
			compressionHeadersWritten = false;
			addVaryHeader();
		}
	}
	
	@Override
	public void reset() {
		super.reset();
		resetStream(false);
		pendingContentLength = null;
		compressionHeadersWritten = false;
		addVaryHeader();
	}
	
	@Override
	public void resetBuffer() {
		super.resetBuffer();
		// resetBuffer clears the body but keeps headers, so a pending Content-Length stays. The
		// compression header is kept too when it was already written for a streaming response.
		resetStream(true);
	}
	
	private void resetStream(boolean keepCompressionHeader) {
		if (stream instanceof GZIPResponseStream) {
			GZIPResponseStream gzipStream = (GZIPResponseStream) stream;
			gzipStream.beginDiscard();
			flushWriter();
			gzipStream.resetBuffer(keepCompressionHeader);
			gzipStream.endDiscard();
		} else {
			flushWriter();
		}
	}
	
	/**
	 * Pushes anything the writer is still holding into the stream so that resetting or discarding the
	 * stream also discards it.
	 */
	private void flushWriter() {
		if (writer != null) {
			// PrintWriter.flush() never throws; any error is kept by the writer and surfaces when the
			// response is finished
			writer.flush();
		}
	}
	
	/**
	 * The length is held back instead of forwarded so that a compressed response cannot advertise the
	 * uncompressed size. See {@link #applyPendingContentLength()}.
	 */
	@Override
	public void setContentLength(int length) {
		pendingContentLength = (long) length;
	}
	
	/**
	 * Tomcat's DefaultServlet uses this method for large files, so ignoring it (as the wrapper used to)
	 * let the uncompressed length through on a compressed response. TRUNK-5978.
	 */
	@Override
	public void setContentLengthLong(long length) {
		pendingContentLength = length;
	}
	
	@Override
	public void setHeader(String name, String value) {
		if (isContentLength(name)) {
			setPendingContentLength(value);
		} else {
			super.setHeader(name, value);
		}
	}
	
	@Override
	public void addHeader(String name, String value) {
		if (isContentLength(name)) {
			setPendingContentLength(value);
		} else {
			super.addHeader(name, value);
		}
	}
	
	@Override
	public void setIntHeader(String name, int value) {
		if (isContentLength(name)) {
			pendingContentLength = (long) value;
		} else {
			super.setIntHeader(name, value);
		}
	}
	
	@Override
	public void addIntHeader(String name, int value) {
		if (isContentLength(name)) {
			pendingContentLength = (long) value;
		} else {
			super.addIntHeader(name, value);
		}
	}
	
	private static boolean isContentLength(String name) {
		return name != null && "Content-Length".equalsIgnoreCase(name);
	}
	
	private void setPendingContentLength(String value) {
		if (value == null) {
			pendingContentLength = null;
			return;
		}
		
		try {
			pendingContentLength = Long.parseLong(value.trim());
		}
		catch (NumberFormatException e) {
			// an invalid length is left for the container to reject when it is applied
			pendingContentLength = null;
		}
	}
	
	/**
	 * Adds {@code Vary: Accept-Encoding} unless it is already advertised, so that caches and CDNs do
	 * not serve a gzipped response to a client that did not ask for one.
	 */
	void addVaryHeader() {
		Collection<String> varyHeaders = origResponse.getHeaders("Vary");
		for (String vary : varyHeaders) {
			if (vary != null && vary.toLowerCase().contains("accept-encoding")) {
				return;
			}
		}
		
		origResponse.addHeader("Vary", "Accept-Encoding");
	}
	
	/**
	 * Whether the response may still be compressed. The decision is made as late as possible because
	 * the status, headers and request method all have to be taken into account.
	 */
	boolean isCompressionAllowed() {
		return error == 0 && !headRequest
		        && (compressionHeadersWritten || !origResponse.containsHeader("Content-Encoding"))
		        && isCompressibleStatus(origResponse.getStatus());
	}
	
	private static boolean isCompressibleStatus(int status) {
		return status != HttpServletResponse.SC_NO_CONTENT && status != HttpServletResponse.SC_NOT_MODIFIED
		        && status != HttpServletResponse.SC_PARTIAL_CONTENT;
	}
	
	/**
	 * Forwards the length the caller asked for. Only used when the response is sent uncompressed.
	 */
	void applyPendingContentLength() {
		addVaryHeader();
		if (pendingContentLength != null) {
			super.setContentLengthLong(pendingContentLength);
			pendingContentLength = null;
		}
	}
	
	/**
	 * Announces a compressed response whose length is already known because it was buffered in memory.
	 */
	void writeCompressedHeaders(long compressedLength) {
		addVaryHeader();
		origResponse.setHeader("Content-Encoding", "gzip");
		origResponse.setContentLengthLong(compressedLength);
		compressionHeadersWritten = true;
	}
	
	/**
	 * Announces a compressed response that is streamed. Its length is not known until the stream is
	 * finished, so it is left to the container to send the response chunked.
	 */
	void writeStreamingGzipHeaders() {
		addVaryHeader();
		origResponse.setHeader("Content-Encoding", "gzip");
		compressionHeadersWritten = true;
	}
}
