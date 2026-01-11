/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util

import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

/**
 * This is a wrapper around the Url class. It's main purpose is to abstract URL
 * so that you can mock http calls as the URL class cannot be mocked.
 */
class HttpUrl(url: String?) {
	
	private val url: URL
	
	init {
		when {
			url == null -> throw MalformedURLException("Url cannot be null")
			!url.startsWith("http://") && !url.startsWith("https://") -> 
				throw MalformedURLException("Not a valid http(s) url")
		}
		this.url = URL(url)
	}
	
	@Throws(IOException::class)
	fun openConnection(): HttpURLConnection {
		return url.openConnection() as HttpURLConnection
	}
	
	override fun toString(): String {
		return url.toString()
	}
}
