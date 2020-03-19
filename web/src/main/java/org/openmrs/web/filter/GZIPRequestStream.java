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
import java.util.zip.GZIPInputStream;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

/**
 * Wraps Request Stream for GZipFilter
 *
 */
public class GZIPRequestStream extends ServletInputStream {
	
	//reference to the gzipped input stream
	protected GZIPInputStream zipInput;
	
	public GZIPRequestStream(HttpServletRequest request) throws IOException {
		super();
		this.zipInput = new GZIPInputStream(request.getInputStream());
	}
	
	@Override
	public int read(byte[] buf, int off, int len) throws IOException {
		return zipInput.read(buf, off, len);
	}
	
	@Override
	public int read() throws IOException {
		return zipInput.read();
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		return zipInput.read(b);
	}

	@Override
	public boolean isFinished() {
		throw new UnsupportedOperationException("Asynchonous operation is not supported.");
	}

	@Override
	public boolean isReady() {
		throw new UnsupportedOperationException("Asynchonous operation is not supported.");
	}

	@Override
	public void setReadListener(ReadListener readListener) {
		throw new UnsupportedOperationException("Asynchonous operation is not supported.");
	}
}
