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

import java.lang.reflect.Field;
import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.synchronization.Synchronizable;
import org.openmrs.synchronization.SyncConstants;
import org.openmrs.synchronization.SyncItemState;
import org.openmrs.synchronization.SyncUtil;
import org.w3c.dom.NodeList;

@Deprecated
public class SyncItemIngest {

	private static Log log = LogFactory.getLog(SyncItemIngest.class);

	public static final String UTF8 = "UTF-8";

	public static SyncImportItem processSyncItem(String incoming) {

		SyncImportItem ret = new SyncImportItem();
		ret.setContent(incoming);
		ret.setState(SyncItemState.UNKNOWN);

		try {
			Object o = null;
			String className = null;
			boolean isUpdateNotCreate = false;
			ArrayList<Field> allFields = null;
			NodeList nodes = null;
			
			try {
                if (log.isDebugEnabled())
                    log.debug("STARTING TO PROCESS: " + incoming);
                
				o = SyncUtil.getRootObject(incoming);
				className = o.getClass().getName();
				allFields = SyncUtil.getAllFields(o);  // get fields, both in class and superclass - we'll need to know what type each field is
				nodes = SyncUtil.getChildNodes(incoming);  // get all child nodes of the root object
			} catch (Exception e) {
				throw new SyncItemIngestException(SyncConstants.ERROR_ITEM_BADXML_ROOT, null, incoming);
			}

			if ( o != null && className != null && allFields != null && nodes != null ) {
				String guid = SyncUtil.getAttribute(nodes, "guid", allFields);
				Object objOld = SyncUtil.getOpenmrsObj(className, guid);
				if ( objOld != null ) {
					o = objOld;
					isUpdateNotCreate = true;
				}
				
				for ( int i = 0; i < nodes.getLength(); i++ ) {
					try {
						SyncUtil.setProperty(o, nodes.item(i), allFields);
					} catch ( Exception e ) {
						throw new SyncItemIngestException(SyncConstants.ERROR_ITEM_UNSET_PROPERTY, nodes.item(i).getNodeName() + "," + className, incoming);
					}
				}
				// now try to commit this fully inflated object
				try {
					SyncUtil.updateOpenmrsObject(o, guid, isUpdateNotCreate);
					ret.setState(SyncItemState.SYNCHRONIZED);
				} catch ( Exception e ) {
					throw new SyncItemIngestException(SyncConstants.ERROR_ITEM_NOT_COMMITTED, className, incoming);
				}
                
                if (log.isDebugEnabled())
                    log.debug("We now have an object " + o.getClass().getName() + " to INSERT with possible GUID of " + ((Synchronizable)o).getGuid());
                
			} else {
				throw new SyncItemIngestException(SyncConstants.ERROR_ITEM_NOCLASS, className, incoming);
			}
		} catch (SyncItemIngestException siie) {
			ret.setErrorMessage(siie.getItemError());
			ret.setErrorMessageArgs(siie.getItemErrorArgs());
			ret.setState(SyncItemState.CONFLICT);
		} catch (Exception e) {
			ret.setErrorMessage(SyncConstants.ERROR_ITEM_NOT_PROCESSED);
		}		
		
		return ret;
	}
}
