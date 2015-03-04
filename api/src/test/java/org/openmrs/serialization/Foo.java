/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.serialization;

import java.util.List;
import java.util.Map;

public class Foo {
	
	private String attributeString;
	
	private int attributeInt;
	
	private List<String> attributeList;
	
	private Map<Integer, String> attributeMap;
	
	public Foo() {
		
	}
	
	public Foo(String attributeString, int attributeInt) {
		this.attributeString = attributeString;
		this.attributeInt = attributeInt;
	}
	
	public String getAttributeString() {
		return attributeString;
	}
	
	public int getAttributeInt() {
		return attributeInt;
	}
	
	public List<String> getAttributeList() {
		return attributeList;
	}
	
	public void setAttributeList(List<String> attributeList) {
		this.attributeList = attributeList;
	}
	
	public Map<Integer, String> getAttributeMap() {
		return attributeMap;
	}
	
	public void setAttributeMap(Map<Integer, String> attributeMap) {
		this.attributeMap = attributeMap;
	}
	
}
