package org.openmrs.synchronization.ingest;

import org.openmrs.serial.Item;
import org.openmrs.serial.Record;
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
