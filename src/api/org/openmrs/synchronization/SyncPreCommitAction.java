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
package org.openmrs.synchronization;

/**
 * Implements simple struct in absence of support for tuples in java in order to express
 * a notion of 'action' that may need to take prior the commit of the change during ingest.
 * SyncPreCommitAction is tuple<PreCommitActionName, Object>.
 * 
 */
public class SyncPreCommitAction {

	private PreCommitActionName name;
	private Object param;

	/**
	 * helper enum that is to indicate the know types of additional actions we expect to execute just before
	 * sync record will be committed 
	 */
	public enum PreCommitActionName {
		REBUILDXSN
	}
	
	public SyncPreCommitAction(PreCommitActionName actionName,Object actionParam) {
		this.name = actionName;
		this.param = actionParam;
	}
	
	public PreCommitActionName getName() {
		return this.name;
	}
	public void setName(PreCommitActionName actionName) {
		this.name = actionName;
	}
	public Object getParam() {
		return this.param;
	}
	public void setParam(Object actionParam) {
		this.param = actionParam;
	}
	
}
