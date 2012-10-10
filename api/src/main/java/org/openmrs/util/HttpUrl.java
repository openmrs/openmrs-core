package org.openmrs.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/*
*  This is a wrapper around the Url class. It's main purpose is to abstract URL
*  so that you can mock http calls as the URL class cannot be mocked.
* */
public class HttpUrl {
	
	private final URL url;
	
	public HttpUrl(String url) throws MalformedURLException {
		if (url == null) {
			throw new MalformedURLException("Url cannot be null");
		} else if (!url.startsWith("http://")) {
			throw new MalformedURLException("Not a valid http url");
		}
		
		this.url = new URL(url);
	}
	
	public HttpURLConnection openConnection() throws IOException {
		return (HttpURLConnection) url.openConnection();
	}
	
	@Override
	public String toString() {
		return url.toString();
	}
}
