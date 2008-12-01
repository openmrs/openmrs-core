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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.SynchronizationService;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.serialization.TimestampNormalizer;
import org.openmrs.synchronization.SyncStatistic;
import org.openmrs.synchronization.SyncConstants;
import org.openmrs.synchronization.SyncUtil;
import org.openmrs.synchronization.SyncUtilTransmission;
import org.openmrs.synchronization.engine.SyncSource;
import org.openmrs.synchronization.engine.SyncSourceJournal;
import org.openmrs.synchronization.engine.SyncTransmission;
import org.openmrs.synchronization.filter.SyncClass;
import org.openmrs.synchronization.filter.SyncServerClass;
import org.openmrs.synchronization.server.RemoteServer;
import org.openmrs.synchronization.server.RemoteServerType;
import org.openmrs.synchronization.server.ServerConnectionState;
import org.openmrs.web.WebConstants;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class SynchronizationStatsListController extends SimpleFormController {

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
    
	@Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {

		log.debug("in processFormSubmission");
		
    	ModelAndView result = new ModelAndView(new RedirectView(getSuccessView()));
    	
        if (!Context.isAuthenticated())
            throw new APIAuthenticationException("Not authenticated!");
        
        HttpSession httpSession = request.getSession();
        String success = "";
        String error = "";
        MessageSourceAccessor msa = getMessageSourceAccessor();
        
        String action = ServletRequestUtils.getStringParameter(request, "action", "");
        
        log.debug("action is " + action);        
        
        if (!success.equals(""))
            httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success);
        
        if (!error.equals(""))
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error);
        
		return result;
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
        Map<String,Object> obj = new HashMap<String,Object>();
        List<RemoteServer> serverList = new ArrayList<RemoteServer>();

        // only fill the Object if the user has authenticated properly
        if (Context.isAuthenticated()) {
            SynchronizationService ss = Context.getSynchronizationService();

            serverList.addAll(ss.getRemoteServers());
            obj.put("serverList", serverList);
            
            SyncSource source = new SyncSourceJournal();
            obj.put("localServerGuid",source.getSyncSourceGuid());
            obj.put("localServerSyncStatus", source.getSyncStatus());
            
            //now add server stats
            Map<RemoteServer,Set<SyncStatistic>> stats = ss.getSyncStatistics(null, null);
            obj.put("syncStats", stats);
        }

        return obj;
    }

	@SuppressWarnings("unchecked")
    @Override
    protected Map referenceData(HttpServletRequest request, Object obj, Errors errors) throws Exception {
		Map<String,Object> ret = new HashMap<String,Object>();

		if ( Context.isAuthenticated() ) {
	        //cast
	        Map<String,Object> ref = (Map<String,Object>)obj; 
	        
			// the parent server
	        List<RemoteServer> serverList = (ArrayList<RemoteServer>)ref.get("serverList");
	        RemoteServer parent = null;
	        
	        for ( RemoteServer server : serverList ) {
	        	if ( server.getServerType().equals(RemoteServerType.PARENT)) {
	        		parent = server;
	        	}
	        }
			
	        // testConnection error messages
	        MessageSourceAccessor msa = getMessageSourceAccessor();
	        Map<String,String> connectionState = new HashMap<String,String>();
	        connectionState.put(ServerConnectionState.OK.toString(), msa.getMessage("SynchronizationConfig.server.connection.status.ok"));
	        connectionState.put(ServerConnectionState.AUTHORIZATION_FAILED.toString(), msa.getMessage("SynchronizationConfig.server.connection.status.noAuth"));
	        connectionState.put(ServerConnectionState.CONNECTION_FAILED.toString(), msa.getMessage("SynchronizationConfig.server.connection.status.noConnection"));
	        connectionState.put(ServerConnectionState.CERTIFICATE_FAILED.toString(), msa.getMessage("SynchronizationConfig.server.connection.status.noCertificate"));
	        connectionState.put(ServerConnectionState.MALFORMED_URL.toString(), msa.getMessage("SynchronizationConfig.server.connection.status.badUrl"));
	        connectionState.put(ServerConnectionState.NO_ADDRESS.toString(), msa.getMessage("SynchronizationConfig.server.connection.status.noAddress"));
	        
	        // taskConfig for automated syncing
	        TaskDefinition parentSchedule = new TaskDefinition();
	        String repeatInterval = "";
	        if ( parent != null ) {
	        	Collection<TaskDefinition> tasks = Context.getSchedulerService().getRegisteredTasks();
	        	if ( tasks != null ) {
	        		String serverId = parent.getServerId().toString();
	            	for ( TaskDefinition task : tasks ) {
	            		if ( task.getTaskClass().equals(SyncConstants.SCHEDULED_TASK_CLASS) ) {
	            			if ( serverId.equals(task.getProperty(SyncConstants.SCHEDULED_TASK_PROPERTY_SERVER_ID)) ) {
	            				parentSchedule = task;
	            				Long repeat = parentSchedule.getRepeatInterval() / 60;
	            				repeatInterval = repeat.toString();
	            				if ( repeatInterval.indexOf(".") > -1 ) repeatInterval = repeatInterval.substring(0, repeatInterval.indexOf("."));
	            			}
	            		}
	            	}
	        	}
	        }
            
	        
	        ret.put("connectionState", connectionState.entrySet());
			ret.put("parent", parent);
	        ret.put("parentSchedule", parentSchedule);
	        ret.put("repeatInterval", repeatInterval);
            ret.put("syncDateDisplayFormat", TimestampNormalizer.DATETIME_DISPLAY_FORMAT);
            
            //sync status staff
            ret.put("localServerSyncStatusValue",SyncUtil.getSyncStatus());
	        ret.put("localServerSyncStatusText", msa.getMessage("SynchronizationConfig.syncStatus.status." + ref.get("localServerSyncStatus").toString()));
            ret.put("localServerSyncStatusMsg", msa.getMessage("SynchronizationConfig.syncStatus.status." + ref.get("localServerSyncStatus").toString() + ".info" , new String[] {SyncConstants.RUNTIMEPROPERTY_SYNC_STATUS}));
	        ret.put("localServerGuid", ref.get("localServerGuid"));
	        ret.put("localServerId", Context.getSynchronizationService().getServerId());
	        ret.put("localServerName", Context.getSynchronizationService().getServerName());           
	        ret.put("localServerAdminEmail", SyncUtil.getAdminEmail());           
		}
        
	    return ret;
    }

}