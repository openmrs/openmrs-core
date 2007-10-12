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
package org.openmrs.synchronization.ingest;

import org.openmrs.serialization.Item;
import org.openmrs.serialization.Record;
import org.openmrs.synchronization.engine.SyncTransmission;

public class SyncDeserializer {

	public static SyncTransmission xmlToSyncTransmission(String incoming) {

		SyncTransmission st = null;
		
		try {
			Record xml = Record.create(incoming);
			Item root = xml.getRootItem();
			st = new SyncTransmission();
			st.load(xml, root);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return st;
	}

	public static SyncTransmissionResponse xmlToSyncTransmissionResponse(String incoming) {

		SyncTransmissionResponse str = null;
		
		try {
			Record xml = Record.create(incoming);
			Item root = xml.getRootItem();
			str = new SyncTransmissionResponse();
			str.load(xml, root);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return str;
	}

}
