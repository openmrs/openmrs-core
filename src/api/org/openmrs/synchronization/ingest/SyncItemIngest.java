package org.openmrs.synchronization.ingest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.serial.Item;
import org.openmrs.serial.Record;
import org.openmrs.synchronization.ISynchronizable;
import org.openmrs.synchronization.SyncUtil;
import org.openmrs.synchronization.engine.SyncTransmission;
import org.openmrs.synchronization.engine.SyncItem.SyncItemState;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SyncItemIngest {

	private static Log log = LogFactory.getLog(SyncItemIngest.class);

	public static final String UTF8 = "UTF-8";

	public static SyncTransmission xmlToSyncTransmission(String incoming) {

		SyncTransmission st = null;
		
		try {
			Record xml = Record.create(incoming);
			Item root = xml.getRootItem();
			st = new SyncTransmission();
			st.load(xml, root);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return st;
	}
			
	public static String processSyncItem(String incoming, SyncItemState state) throws SyncItemIngestException {

		String ret = null;
		
		try {
			Record xml = Record.create(incoming);
			Item root = xml.getRootItem();
			String className = root.getNode().getNodeName();
			
			log.debug("Trying to process SyncItem with the name " + className + ", and state of " + state + "...");
			Object o = SyncUtil.newObject(className);
			
			if ( o != null ) {
				// get fields, both in class and superclass - we'll need to know what type each field is
				ArrayList<Field> allFields = SyncUtil.getAllFields(o);
				
				NodeList nodes = root.getNode().getChildNodes();
				
				for ( int i = 0; i < nodes.getLength(); i++ ) {
					Node n = nodes.item(i);
					String propName = n.getNodeName();
					Object propVal = SyncUtil.valForField(propName, n.getTextContent(), allFields);

					// invoke setter method on this object
					String methodName = "set" + SyncUtil.propCase(propName);
					Object[] setterParams = new Object[1];
					setterParams[0] = propVal;
					Method m = SyncUtil.getSetterMethod(o.getClass(), propName, propVal.getClass());

					if ( m != null ) {
						Object voidObj = m.invoke(o, setterParams);
						log.debug("Successfully called set" + SyncUtil.propCase(propName) + "(" + propVal + ")" );
						log.debug(" - object is type " + propVal.getClass().getName());
					} else {
						throw new NoSuchMethodException("There was no " + methodName + "() method in object of class " + className);
					}
				}

				String guid = ((ISynchronizable)o).getGuid();
				
				log.debug("We now have an object " + o.getClass().getName() + " to INSERT with possible GUID of " + guid);

				boolean isUpdateNotCreate = !state.equals(SyncItemState.NEW);
				
				ret = SyncUtil.updateOpenmrsObject(o, guid, isUpdateNotCreate);
				
			} else {
				throw new NullPointerException("Object of classname " + className + " could not be created while processing SyncItem");
			}
			
		} catch (Exception e) {
			throw new SyncItemIngestException(e, incoming);
		}		
		
		return ret;
	}
}
