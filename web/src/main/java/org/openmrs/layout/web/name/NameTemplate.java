/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.layout.web.name;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.openmrs.PersonName;
import org.openmrs.api.APIException;
import org.openmrs.layout.web.LayoutSupport;
import org.openmrs.layout.web.LayoutTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NameTemplate extends LayoutTemplate {
	
	public String getLayoutToken() {
		return "IS_NAME_TOKEN";
	}
	
	public String getNonLayoutToken() {
		return "IS_NOT_NAME_TOKEN";
	}
	
	public String format(PersonName personName) {
		
		List<String> personNameLines = new ArrayList<String>();
		List<List<Map<String, String>>> lines = getLines();
		String layoutToken = getLayoutToken();
		
		try {
			for (List<Map<String, String>> line : lines) {
				String nameLine = "";
				Boolean hasToken = false;
				for (Map<String, String> lineToken : line) {
					if (lineToken.get("isToken").equals(layoutToken)) {
						String tokenValue = BeanUtils.getProperty(personName, lineToken.get("codeName"));
						if (StringUtils.isNotBlank(tokenValue)) {
							hasToken = true;
							nameLine += tokenValue;
						}
					} else {
						nameLine += lineToken.get("displayText");
					}
				}
				// only display a line if there's at least one token within it we've been able to resolve
				if (StringUtils.isNotBlank(nameLine) && hasToken) {
					personNameLines.add(nameLine);
				}
			}
			// bit of hack, but we ignore the "line-by-line" format and just delimit a "line" with blank space
			return StringUtils.join(personNameLines, " ");
		}
		catch (Exception e) {
			throw new APIException("Unable to format personName " + personName.getId() + " using name template", e);
		}
	}
	
	@Override
	public LayoutSupport<?> getLayoutSupportInstance() {
		return NameSupport.getInstance();
	}
	
}
