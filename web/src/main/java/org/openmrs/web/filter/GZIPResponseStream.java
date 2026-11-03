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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;

/**
 * Wraps Response Stream for GZipFilter. Content is buffered in memory until either the response
 * finishes (when it is compressed only if it is big enough to be worth it) or the buffer overflows
 * (when it is streamed through gzip, or passed straight through for a response that must not be
 * compressed).
 * 
 * @author Matt Raible
 * @version $Revision: 1.3 $ $Date: 2004/05/16 02:17:00 $
 */
public class GZIPResponseStream extends ServletOutputStream {
	
	// default size of the in-memory buffer
	private static final int DEFAULT_BUFFER_SIZE = 50000;
	
	// responses below this size are not worth compressing
	private static final int MIN_COMPRESS_SIZE = 1024;
	
	// abstraction of the output stream used for compression
	protected OutputStream bufferedOutput;
	
	// state keeping variable for if close() has been called
	protected boolean closed;
	
	// reference to original response
	protected HttpServletResponse response;
	
	// reference to the output stream to the client's browser
	protected ServletOutputStream output;
	
	private final GZIPResponseWrapper wrapper;
	
	private final int bufferSize;
	
	// set when the response has been abandoned (for example by sendError) so that nothing more is
	// written to the client
	private boolean discarded;
	
	public GZIPResponseStream(HttpServletResponse response) throws IOException {
		this(null, response, DEFAULT_BUFFER_SIZE);
	}
	
	GZIPResponseStream(GZIPResponseWrapper wrapper, HttpServletResponse response) throws IOException {
		this(wrapper, response, DEFAULT_BUFFER_SIZE);
	}
	
	GZIPResponseStream(GZIPResponseWrapper wrapper, HttpServletResponse response, int bufferSize) throws IOException {
		super();
		closed = false;
		this.wrapper = wrapper;
		this.response = response;
		this.output = response.getOutputStream();
		this.bufferSize = bufferSize;
		bufferedOutput = new ByteArrayOutputStream();
	}
	
	@Override
	public void close() throws IOException {
		// verify the stream is yet to be closed
		if (closed) {
			throw new IOException("This output stream has already been closed");
		}
		
		if (discarded) {
			closed = true;
			return;
		}
		
		// if we buffered everything in memory, decide now whether to gzip it
		if (bufferedOutput instanceof ByteArrayOutputStream) {
			ByteArrayOutputStream baos = (ByteArrayOutputStream) bufferedOutput;
			byte[] bytes = baos.toByteArray();
			
			if (isCompressionAllowed() && bytes.length >= MIN_COMPRESS_SIZE) {
				byte[] compressedBytes = gzip(bytes);
				if (wrapper != null) {
					wrapper.writeCompressedHeaders(compressedBytes.length);
				} else {
					response.setHeader("Content-Encoding", "gzip");
					response.setContentLength(compressedBytes.length);
				}
				output.write(compressedBytes);
			} else {
				applyPendingContentLength();
				output.write(bytes);
			}
			
			output.flush();
			output.close();
			closed = true;
		}
		// if things were not buffered in memory, finish the GZIP stream and response
		else if (bufferedOutput instanceof GZIPOutputStream) {
			// cast to appropriate type
			GZIPOutputStream gzipstream = (GZIPOutputStream) bufferedOutput;
			
			// finish the compression
			gzipstream.finish();
			
			// finish the response
			output.flush();
			output.close();
			closed = true;
		}
		// the response was passed straight through to the client
		else {
			output.flush();
			output.close();
			closed = true;
		}
	}
	
	@Override
	public void flush() throws IOException {
		if (closed) {
			throw new IOException("Cannot flush a closed output stream");
		}
		
		if (discarded) {
			return;
		}
		
		if (bufferedOutput instanceof GZIPOutputStream) {
			bufferedOutput.flush();
			output.flush();
		} else if (bufferedOutput == output) {
			output.flush();
		} else {
			// still buffering in memory, so there is nothing to send yet
			bufferedOutput.flush();
		}
	}
	
	@Override
	public void write(int b) throws IOException {
		if (closed) {
			throw new IOException("Cannot write to a closed output stream");
		}
		
		if (discarded) {
			return;
		}
		
		// make sure we aren't over the buffer's limit
		if (bufferedOutput instanceof ByteArrayOutputStream) {
			ByteArrayOutputStream baos = (ByteArrayOutputStream) bufferedOutput;
			if ((baos.size() + 1) > bufferSize) {
				switchToStreaming();
			} else {
				// write the byte to the temporary output
				bufferedOutput.write(b);
				return;
			}
		}
		
		bufferedOutput.write(b);
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		
		if (closed) {
			throw new IOException("Cannot write to a closed output stream");
		}
		
		if (discarded) {
			return;
		}
		
		// make sure we aren't over the buffer's limit
		if (bufferedOutput instanceof ByteArrayOutputStream) {
			ByteArrayOutputStream baos = (ByteArrayOutputStream) bufferedOutput;
			if ((baos.size() + len) > bufferSize) {
				switchToStreaming();
			} else {
				// write the content to the buffer
				bufferedOutput.write(b, off, len);
				return;
			}
		}
		
		bufferedOutput.write(b, off, len);
	}
	
	/**
	 * Moves past the in-memory buffer: either into a gzip stream (for a response that may be
	 * compressed) or straight to the client (for one that may not). From here on the response is
	 * committed and its length is unknown, so the uncompressed length can never be sent.
	 */
	private void switchToStreaming() throws IOException {
		ByteArrayOutputStream baos = (ByteArrayOutputStream) bufferedOutput;
		byte[] bytes = baos.toByteArray();
		
		if (isCompressionAllowed()) {
			if (wrapper != null) {
				wrapper.writeStreamingGzipHeaders();
			} else {
				response.setHeader("Content-Encoding", "gzip");
			}
			
			GZIPOutputStream gzipstream = new GZIPOutputStream(output, bufferSize);
			gzipstream.write(bytes);
			bufferedOutput = gzipstream;
		} else {
			applyPendingContentLength();
			output.write(bytes);
			bufferedOutput = output;
		}
	}
	
	private boolean isCompressionAllowed() {
		return wrapper == null || wrapper.isCompressionAllowed();
	}
	
	private void applyPendingContentLength() {
		if (wrapper != null) {
			wrapper.applyPendingContentLength();
		}
	}
	
	private static byte[] gzip(byte[] bytes) throws IOException {
		ByteArrayOutputStream compressedContent = new ByteArrayOutputStream();
		try (GZIPOutputStream gzipstream = new GZIPOutputStream(compressedContent)) {
			gzipstream.write(bytes);
		}
		return compressedContent.toByteArray();
	}
	
	public boolean closed() {
		return this.closed;
	}
	
	/**
	 * Discards everything buffered so far, leaving the stream usable for a fresh response.
	 */
	void resetBuffer() {
		if (bufferedOutput instanceof ByteArrayOutputStream) {
			((ByteArrayOutputStream) bufferedOutput).reset();
		}
	}
	
	public void reset() {
		resetBuffer();
	}
	
	/**
	 * Abandons the response. Used by {@code sendError}, whose error page is written by the container
	 * directly to the real response, so anything buffered here must be thrown away rather than sent.
	 */
	void discard() {
		discarded = true;
		resetBuffer();
	}
	
	@Override
	public boolean isReady() {
		throw new UnsupportedOperationException("Asynchonous operation is not supported.");
	}
	
	@Override
	public void setWriteListener(WriteListener writeListener) {
		throw new UnsupportedOperationException("Asynchonous operation is not supported.");
	}
}
