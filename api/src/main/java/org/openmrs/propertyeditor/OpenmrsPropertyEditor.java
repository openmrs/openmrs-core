/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.OpenmrsObject;

/**
 * Base {@code PropertyEditor} for {@code OpenmrsObject}.
 * <p>
 * When setting the {@code value} from text it will try to get the {@code OpenmrsObject} via its id
 * and if that fails using its uuid.
 * </p>
 * 
 * @param <T> the openmrs object to convert to and from
 * @since 2.2.0
 * @see org.openmrs.OpenmrsObject
 */
public abstract class OpenmrsPropertyEditor<T extends OpenmrsObject> extends PropertyEditorSupport {
	
	protected abstract T getObjectById(Integer id);
	
	protected abstract T getObjectByUuid(String uuid);
	
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (StringUtils.isNotBlank(text)) {
			try {
				setValue(getObjectById(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				T o = getObjectByUuid(text);
				setValue(o);
				if (o == null) {
					throw new IllegalArgumentException("Failed to find object for value [" + text + "]", ex);
				}
			}
		} else {
			setValue(null);
		}
	}
	
	@Override
	public String getAsText() {
		T t = (T) getValue();
		if (t == null) {
			return "";
		} else {
			return t.getId().toString();
		}
	}
}
