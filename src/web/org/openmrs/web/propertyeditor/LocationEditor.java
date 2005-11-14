/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmrs.web.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.api.PatientService;
import org.openmrs.context.Context;
import org.springframework.util.StringUtils;

/**
 * Editor for <code>java.lang.Class</code>, to directly populate a Class property
 * instead of using a String class name property as bridge.
 *
 * <p>Also supports "java.lang.String[]"-style array class names,
 * in contrast to the standard <code>Class.forName</code> method.
 * Delegates to ClassUtils for actual class name resolution.
 *
 * @author Juergen Hoeller
 * @since 13.05.2003
 * @see java.lang.Class#forName
 * @see org.springframework.util.ClassUtils#forName
 */
public class LocationEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	Context context;
	
	public LocationEditor(Context c) {
		this.context = c;
	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		if (context != null) {
			PatientService ps = context.getPatientService(); 
			if (StringUtils.hasText(text)) {
				try {
					setValue(ps.getLocation(Integer.valueOf(text)));
				}
				catch (Exception ex) {
					throw new IllegalArgumentException("Location not found: " + ex.getMessage());
				}
			}
			else {
				setValue(null);
			}
		}
	}

	public String getAsText() {
		Location t = (Location) getValue();
		if (t == null) {
			return "";
		}
		else {
			return t.getLocationId().toString();
		}
	}

}
