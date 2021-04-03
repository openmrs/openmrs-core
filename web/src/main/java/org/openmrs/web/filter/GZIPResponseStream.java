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
 * Wraps Response Stream for GZipFilter
 * 
 * @author Matt Raible
 * @version $Revision: 1.3 $ $Date: 2004/05/16 02:17:00 $
 */
public class GZIPResponseStream extends ServletOutputStream {
	
	// abstraction of the output stream used for compression
	protected OutputStream bufferedOutput;
	
	// state keeping variable for if close() has been called
	protected boolean closed;
	
	// reference to original response
	protected HttpServletResponse response;
	
	// reference to the output stream to the client's browser
	protected ServletOutputStream output;
	
	// default size of the in-memory buffer
	private int bufferSize = 50000;
	
	public GZIPResponseStream(HttpServletResponse response) throws IOException {
		super();
		closed = false;
		this.response = response;
		this.output = response.getOutputStream();
		bufferedOutput = new ByteArrayOutputStream();
	}
	
	@Override
	public void close() throws IOException {
		// verify the stream is yet to be closed
		if (closed) {
			throw new IOException("This output stream has already been closed");
		}
		
		// if we buffered everything in memory, gzip it
		if (bufferedOutput instanceof ByteArrayOutputStream) {
			// get the content
			ByteArrayOutputStream baos = (ByteArrayOutputStream) bufferedOutput;
			
			// prepare a gzip stream
			ByteArrayOutputStream compressedContent = new ByteArrayOutputStream();
			GZIPOutputStream gzipstream = new GZIPOutputStream(compressedContent);
			byte[] bytes = baos.toByteArray();
			gzipstream.write(bytes);
			gzipstream.finish();
			
			// get the compressed content
			byte[] compressedBytes = compressedContent.toByteArray();
			
			// set appropriate HTTP headers
			response.setContentLength(compressedBytes.length);
			response.addHeader("Content-Encoding", "gzip");
			output.write(compressedBytes);
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
	}
	
	@Override
	public void flush() throws IOException {
		if (closed) {
			throw new IOException("Cannot flush a closed output stream");
		}
		
		bufferedOutput.flush();
	}
	
	@Override
	public void write(int b) throws IOException {
		if (closed) {
			throw new IOException("Cannot write to a closed output stream");
		}
		
		// make sure we aren't over the buffer's limit
		checkBufferSize(1);
		
		// write the byte to the temporary output
		bufferedOutput.write((byte) b);
	}
	
	private void checkBufferSize(int length) throws IOException {
		// check if we are buffering too large of a file
		if (bufferedOutput instanceof ByteArrayOutputStream) {
			ByteArrayOutputStream baos = (ByteArrayOutputStream) bufferedOutput;
			
			if ((baos.size() + length) > bufferSize) {
				// files too large to keep in memory are sent to the client without Content-Length specified
				response.addHeader("Content-Encoding", "gzip");
				
				// get existing bytes
				byte[] bytes = baos.toByteArray();
				
				// make new gzip stream using the response output stream
				GZIPOutputStream gzipstream = new GZIPOutputStream(output);
				gzipstream.write(bytes);
				
				// we are no longer buffering, send content via gzipstream
				bufferedOutput = gzipstream;
			}
		}
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
		
		// make sure we aren't over the buffer's limit
		checkBufferSize(len);
		
		// write the content to the buffer
		bufferedOutput.write(b, off, len);
	}
	
	public boolean closed() {
		return this.closed;
	}
	
	public void reset() {
		//noop
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
