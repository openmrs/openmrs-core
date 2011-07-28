/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.web.taglib.fieldgen;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ListHandler extends AbstractFieldGenHandler implements FieldGenHandler {
	
	private static final Log log = LogFactory.getLog(ListHandler.class);
	
	private static final String DEFAULT_URL = "list.field";
	
	@Override
	public void run() {
		setUrl(DEFAULT_URL);
		setParameter("list", fieldGenTag.getParameterMap().get("list"));
		setParameter("initialValue", fieldGenTag.getVal());
	}
}
