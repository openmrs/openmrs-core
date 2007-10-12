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
package org.openmrs.web.controller.synchronization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.SynchronizationService;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.Item;
import org.openmrs.serialization.Record;
import org.openmrs.serialization.TimestampNormalizer;
import org.openmrs.synchronization.engine.SyncItem;
import org.openmrs.synchronization.engine.SyncRecord;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SynchronizationHistoryListController extends SimpleFormController {

    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    /**
     * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest,
     *      org.springframework.web.bind.ServletRequestDataBinder)
     */
    protected void initBinder(HttpServletRequest request,
            ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
    }


    /**
     * 
     * This is called prior to displaying a form for the first time. It tells
     * Spring the form/command object to load into the request
     * 
     * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
     */
    protected Object formBackingObject(HttpServletRequest request)
            throws ServletException {
        // default empty Object
        List<SyncRecord> recordList = new ArrayList<SyncRecord>();

        // only fill the Object if the user has authenticated properly
        if (Context.isAuthenticated()) {
        	SynchronizationService ss = Context.getSynchronizationService();
            recordList.addAll(ss.getSyncRecords());
        }

        return recordList;
    }

	@Override
    protected Map referenceData(HttpServletRequest request, Object obj, Errors errors) throws Exception {
		Map<String,Object> ret = new HashMap<String,Object>();
		
		Map<String,String> recordTypes = new HashMap<String,String>();
		Map<Object,String> itemTypes = new HashMap<Object,String>();
		Map<Object,String> itemGuids = new HashMap<Object,String>();
		//Map<String,String> itemInfo = new HashMap<String,String>();
		//Map<String,String> itemInfoKeys = new HashMap<String,String>();
        List<SyncRecord> recordList = (ArrayList<SyncRecord>)obj;

        //itemInfoKeys.put("Patient", "gender,birthdate");
        //itemInfoKeys.put("PersonName", "name");
        //itemInfoKeys.put("User", "username");
        
        // warning: right now we are assuming there is only 1 item per record
        for ( SyncRecord record : recordList ) {
			for ( SyncItem item : record.getItems() ) {
				String syncItem = item.getContent();
				Record xml = Record.create(syncItem);
				Item root = xml.getRootItem();
				String className = root.getNode().getNodeName().substring("org.openmrs.".length());
				itemTypes.put(item.getKey().getKeyValue(), className);
				if ( recordTypes.get(record.getGuid()) == null ) recordTypes.put(record.getGuid(), className);
				//String itemInfoKey = itemInfoKeys.get(className);
				
				// now we have to go through the item child nodes to find the real GUID that we want
				NodeList nodes = root.getNode().getChildNodes();
				for ( int i = 0; i < nodes.getLength(); i++ ) {
					Node n = nodes.item(i);
					String propName = n.getNodeName();
					if ( propName.equalsIgnoreCase("guid") ) {
						itemGuids.put(item.getKey().getKeyValue(), n.getTextContent());
					}
					/*
					if ( propName.equalsIgnoreCase(itemInfoKey) ) {
						itemInfo.put(record.getGuid(), n.getTextContent());
					}
					*/
				}
			}
        	
        }
 
        ret.put("recordTypes", recordTypes);
        ret.put("itemTypes", itemTypes);
        ret.put("itemGuids", itemGuids);
        //ret.put("itemInfo", itemInfo);
        ret.put("parent", Context.getSynchronizationService().getParentServer());
        ret.put("syncDateDisplayFormat", TimestampNormalizer.DATETIME_DISPLAY_FORMAT);
        
	    return ret;
    }

}