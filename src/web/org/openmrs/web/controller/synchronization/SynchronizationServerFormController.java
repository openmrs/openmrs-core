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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PersonName;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.TaskConfig;
import org.openmrs.serialization.TimestampNormalizer;
import org.openmrs.synchronization.SyncConstants;
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

public class SynchronizationServerFormController extends SimpleFormController {

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
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse reponse, Object obj, BindException errors) throws Exception {

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
        
        if ( "save".equals(action) ) {       
            RemoteServer server = (RemoteServer)obj;
        	String address = ServletRequestUtils.getStringParameter(request, "address", "");
        	String nickname = ServletRequestUtils.getStringParameter(request, "nickname", "");
        	String username = ServletRequestUtils.getStringParameter(request, "username", "");
        	String password = ServletRequestUtils.getStringParameter(request, "password", "");
            String type = ServletRequestUtils.getStringParameter(request, "type", RemoteServerType.CHILD.toString());
            Integer serverId = ServletRequestUtils.getIntParameter(request, "serverId", 0);
    		String[] startedParams = request.getParameterValues("started");
    		boolean started = false;
    		if ( startedParams != null ) {
	    		for ( String startedParam : startedParams ) {
	    			if ( startedParam.equals("true") ) started = true;
	    		}
    		}
        	Integer repeatInterval = ServletRequestUtils.getIntParameter(request, "repeatInterval", 0) * 60;  // interval really is in seconds, to * 60 to convert to minutes 
        	
            if ( nickname.length() == 0 ) error = msa.getMessage("SynchronizationConfig.server.error.nicknameRequired");
        	//if ( password.length() == 0 ) error = msa.getMessage("SynchronizationConfig.server.error.passwordRequired");
        	//if ( username.length() == 0 ) error = msa.getMessage("SynchronizationConfig.server.error.usernameRequired");
        	if ( address.length() == 0 && !type.equals(RemoteServerType.CHILD.toString())) error = msa.getMessage("SynchronizationConfig.server.error.addressRequired");
        	if ( started && repeatInterval < 1 ) error = msa.getMessage("SynchronizationConfig.server.error.invalidRepeat");
        	if ( type.equals(RemoteServerType.CHILD.toString()) ) {
                if ( serverId == null ) {
                    String passwordRetype = ServletRequestUtils.getStringParameter(request, "passwordRetype", "");
                    log.warn("username: " + username + ", password: " + password + ", passwordretype is " + passwordRetype);
                    if ( passwordRetype.length() == 0 ) error = msa.getMessage("SynchronizationConfig.server.error.passwordRetypeRequired");
                    if ( password.length() == 0 ) error = msa.getMessage("SynchronizationConfig.server.error.passwordRequired");
                    if ( username.length() == 0 ) error = msa.getMessage("SynchronizationConfig.server.error.usernameRequired");
                    if ( !passwordRetype.equals(password) ) error = msa.getMessage("SynchronizationConfig.server.error.passwordMismatch");
                }
            }
            
        	if ( error.length() == 0 ) {
            	server = Context.getSynchronizationService().getRemoteServer(serverId);

            	if ( server == null ) {
            		server = new RemoteServer();
                    server.setServerType(RemoteServerType.valueOf(type));
                    server.setNickname(nickname);

                    // just in case - we want to make sure there is ONLY ever 1 parent
                    if ( server.getServerType().equals(RemoteServerType.PARENT)) {
                        RemoteServer parent = Context.getSynchronizationService().getParentServer();
                        if ( parent != null ) {
                            server = parent;
                        }
                    }
            	}
                
                if ( type.equals(RemoteServerType.CHILD.toString()) ) {
                    if ( server.getServerId() == null ) {
                        // create a new user
                        User user = new User();
                        user.setGender(SyncConstants.DEFAULT_CHILD_SERVER_USER_GENDER);
                        user.setUsername(username);

                        PersonName name = new PersonName();
                        name.setFamilyName(nickname);
                        name.setGivenName(msa.getMessage(SyncConstants.DEFAULT_CHILD_SERVER_USER_NAME));
                        user.addName(name);

                        String defaultRole = Context.getAdministrationService().getGlobalProperty("synchronization.default_role");
                        if ( defaultRole != null ) {
                            Role role = Context.getUserService().getRole(defaultRole);
                            if ( role != null ) user.addRole(role);
                        }
                        
                        // create in database
                        try {
                            Context.getUserService().createUser(user, password);
                            server.setChildUsername(user.getUsername());
                        } catch ( Exception e ) {
                            log.error("Unable to create new user to associate with child server");
                            e.printStackTrace();
                            error = msa.getMessage("SynchronizationConfig.child.error.uniqueUsername");
                        }
                    }
                    
                    address = "N/A";
                    username = "N/A";
                    password = "N/A";
                }
                
                if ( error.length() == 0 ) {
                    server.setAddress(address);
                    server.setPassword(password);
                    server.setUsername(username);

                    String[] classIdsTo = ServletRequestUtils.getStringParameters(request, "toDefault");
                    String[] classIdsFrom = ServletRequestUtils.getStringParameters(request, "fromDefault");
                    Set<String> idsTo = new HashSet<String>();
                    Set<String> idsFrom = new HashSet<String>();
                    if ( classIdsTo != null ) idsTo.addAll(Arrays.asList(classIdsTo));
                    if ( classIdsFrom != null ) idsFrom.addAll(Arrays.asList(classIdsFrom));
                    log.warn("idsTo contains " + idsTo.size() + " items");
                    log.warn("idsFrom contains " + idsFrom.size() + " items");

                    Set<SyncServerClass> serverClasses = server.getServerClasses();
                    if ( serverClasses == null ) {
                        List<SyncClass> syncClasses = Context.getSynchronizationService().getSyncClasses();
                        serverClasses = new HashSet<SyncServerClass>();
                        if ( syncClasses != null ) {
                            log.warn("SYNCCLASSES IS SIZE: " + syncClasses.size());
                            for ( SyncClass syncClass : syncClasses ) {
                                //log.warn("PROCESSING SYNC CLASS: " + syncClass.getName());
                                SyncServerClass serverClass = new SyncServerClass(server, syncClass);
                                if ( idsTo.contains(syncClass.getSyncClassId().toString()) ) {
                                    serverClass.setSendTo(true);
                                    log.warn(syncClass.getSyncClassId() + " in idsTo, so server " + server.getNickname() + " will send " + syncClass.getName());
                                } else {
                                    serverClass.setSendTo(false);
                                    log.warn(syncClass.getSyncClassId() + " not in idsTo, so server " + server.getNickname() + " will NOT send " + syncClass.getName());
                                }
                                if ( idsFrom.contains(syncClass.getSyncClassId().toString()) ) {
                                    serverClass.setReceiveFrom(true);
                                    log.warn(syncClass.getSyncClassId() + " in idsFrom, so server " + server.getNickname() + " will receive " + syncClass.getName());
                                } else {
                                    serverClass.setReceiveFrom(false);
                                    log.warn(syncClass.getSyncClassId() + " not in idsFrom, so server " + server.getNickname() + " will NOT receive " + syncClass.getName());
                                }
                                serverClasses.add(serverClass);
                            }
                            server.setServerClasses(serverClasses);
                        } else {
                            log.warn("NOT GETTING SYNCCLASSES");
                        }
                    } else {
                        for ( SyncServerClass serverClass : serverClasses ) {
                            if ( idsTo.contains(serverClass.getSyncClass().getSyncClassId().toString()) ) {
                                serverClass.setSendTo(true);
                                log.warn(serverClass.getSyncClass().getSyncClassId() + " in idsTo, so server " + server.getNickname() + " will send " + serverClass.getSyncClass().getName());
                            } else {
                                serverClass.setSendTo(false);
                                log.warn(serverClass.getSyncClass().getSyncClassId() + " not in idsTo, so server " + server.getNickname() + " will NOT send " + serverClass.getSyncClass().getName());
                            }
                            if ( idsFrom.contains(serverClass.getSyncClass().getSyncClassId().toString()) ) {
                                serverClass.setReceiveFrom(true);
                                log.warn(serverClass.getSyncClass().getSyncClassId() + " in idsFrom, so server " + server.getNickname() + " will receive " + serverClass.getSyncClass().getName());
                            } else {
                                serverClass.setReceiveFrom(false);
                                log.warn(serverClass.getSyncClass().getSyncClassId() + " not in idsFrom, so server " + server.getNickname() + " will NOT receive " + serverClass.getSyncClass().getName());
                            }
                        }
                        server.setServerClasses(serverClasses);
                    }

                    if ( server.getServerId() == null ) {
                        Context.getSynchronizationService().createRemoteServer(server);
                    } else {
                        Context.getSynchronizationService().updateRemoteServer(server);
                    }
                    
                    // also set TaskConfig for scheduling
                    if ( server.getServerId() != null ) {
                        TaskConfig serverSchedule = null;
                        Collection<TaskConfig> tasks = Context.getSchedulerService().getAvailableTasks();
                        if ( tasks != null ) {
                            for ( TaskConfig task : tasks ) {
                                if ( task.getSchedulableClass().equals(SyncConstants.SCHEDULED_TASK_CLASS) ) {
                                    if ( serverId.toString().equals(task.getProperty(SyncConstants.SCHEDULED_TASK_PROPERTY_SERVER_ID)) ) {
                                        serverSchedule = task;
                                    } else {
                                        log.warn("not equal comparing " + serverId + " to " + task.getProperty(SyncConstants.SCHEDULED_TASK_PROPERTY_SERVER_ID));
                                    }
                                } else {
                                    log.warn("not equal comparing " + task.getSchedulableClass() + " to " + SyncConstants.SCHEDULED_TASK_CLASS);
                                }
                            }
                        } else {
                            log.warn("tasks is null");
                        }

                        Map<String,String> props = new HashMap<String,String>();
                        props.put(SyncConstants.SCHEDULED_TASK_PROPERTY_SERVER_ID, serverId.toString());
                        if ( serverSchedule != null ) {
                            if (log.isInfoEnabled())
                                log.info("Sync scheduled task exists, and started is " + started + " and interval is " + repeatInterval);
                            try {
                                Context.getSchedulerService().stopTask(serverSchedule);
                            } catch (Exception e) {
                                log.warn("Sync task had run wild, couldn't stop it because it wasn't really running",e);
                                // nothing to do - means something was wrong or not yet started
                                //TODO: is this right? should we report error here on 'STRICT'?
                            }
                            serverSchedule.setStarted(started);
                            serverSchedule.setRepeatInterval((long)repeatInterval);
                            serverSchedule.setStartOnStartup(started);
                            serverSchedule.setProperties(props);
                            if ( started ) {
                                serverSchedule.setStartTime(new Date());
                            }
                            Context.getSchedulerService().updateTask(serverSchedule);
                            if ( started ) {
                                Context.getSchedulerService().scheduleTask(serverSchedule);
                            }
                        } else {
                            if (log.isInfoEnabled())
                                log.info("Sync scheduled task does not exists, and started is " + started + " and interval is " + repeatInterval);
                            if ( started ) {
                                serverSchedule = new TaskConfig();
                                serverSchedule.setName(server.getNickname() + " " + msa.getMessage("SynchronizationConfig.server.scheduler"));
                                serverSchedule.setDescription(msa.getMessage("SynchronizationConfig.server.scheduler.description"));
                                serverSchedule.setRepeatInterval((long)repeatInterval);
                                serverSchedule.setStartTime(new Date());
                                serverSchedule.setSchedulableClass(SyncConstants.SCHEDULED_TASK_CLASS);
                                serverSchedule.setStarted(started);
                                serverSchedule.setStartOnStartup(started);
                                serverSchedule.setProperties(props);
                                Context.getSchedulerService().createTask(serverSchedule);
                                Context.getSchedulerService().scheduleTask(serverSchedule);
                            }
                        }
                    }
                    success = msa.getMessage("SynchronizationConfig.server.saved");             
                } else {
                    result = new ModelAndView(this.getFormView(), "server", server);
                }

        	} else {
        	    result = new ModelAndView(this.getFormView(), "server", server);
            }
        }
        
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
        String type = ServletRequestUtils.getStringParameter(request, "type", RemoteServerType.CHILD.toString());
        RemoteServer server = null;

        log.warn("IN FORMBACKING, type is " + type);
        
        // only fill the Object if the user has authenticated properly
        if (Context.isAuthenticated()) {
            Integer serverId = ServletRequestUtils.getIntParameter(request, "serverId", 0);
            server = Context.getSynchronizationService().getRemoteServer(serverId);
        }

        if ( server == null ) {
            server = new RemoteServer();
            server.setServerType(RemoteServerType.valueOf(type));
            if (Context.isAuthenticated()) {
                Set<SyncServerClass> serverClasses = new HashSet<SyncServerClass>();
                List<SyncClass> classes = Context.getSynchronizationService().getSyncClasses();
                if ( classes != null ) {
                    for ( SyncClass syncClass : classes ) {
                        SyncServerClass serverClass = new SyncServerClass(server, syncClass);
                        serverClasses.add(serverClass);
                    }
                }
                server.setServerClasses(serverClasses);
            }
        }
        
        return server;
    }

	@SuppressWarnings("unchecked")
    @Override
    protected Map referenceData(HttpServletRequest request, Object obj, Errors errors) throws Exception {
		Map<String,Object> ret = new HashMap<String,Object>();

		if ( Context.isAuthenticated() ) {
	        //cast
	        RemoteServer server = (RemoteServer)obj; 

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
	        TaskConfig serverSchedule = new TaskConfig();
	        String repeatInterval = "";
	        if ( server != null ) {
                if ( server.getServerId() != null ) {
                    Collection<TaskConfig> tasks = Context.getSchedulerService().getAvailableTasks();
                    if ( tasks != null ) {
                        String serverId = server.getServerId().toString();
                        for ( TaskConfig task : tasks ) {
                            if ( task.getSchedulableClass().equals(SyncConstants.SCHEDULED_TASK_CLASS) ) {
                                if ( serverId.equals(task.getProperty(SyncConstants.SCHEDULED_TASK_PROPERTY_SERVER_ID)) ) {
                                    serverSchedule = task;
                                    Long repeat = serverSchedule.getRepeatInterval() / 60;
                                    repeatInterval = repeat.toString();
                                    if ( repeatInterval.indexOf(".") > -1 ) repeatInterval = repeatInterval.substring(0, repeatInterval.indexOf("."));
                                }
                            }
                        }
                    }
                }
	        }
            
            Map<String,List<SyncServerClass>> syncClassGroups = new HashMap<String,List<SyncServerClass>>();
            Map<String,List<SyncServerClass>> syncClassGroupsLeft = new HashMap<String,List<SyncServerClass>>();
            Map<String,List<SyncServerClass>> syncClassGroupsRight = new HashMap<String,List<SyncServerClass>>();
            Map<String,Boolean> syncClassGroupTo = new HashMap<String,Boolean>();
            Map<String,Boolean> syncClassGroupFrom = new HashMap<String,Boolean>();

            Set<SyncServerClass> serverClasses = server.getServerClasses();
            if ( serverClasses != null ) {
                //log.warn("SYNCCLASSES IS SIZE: " + syncClasses.size());
                for ( SyncServerClass serverClass : serverClasses ) {
                    String type = serverClass.getSyncClass().getType().toString();
                    List<SyncServerClass> currList = syncClassGroups.get(type);
                    if ( currList == null ) {
                        currList = new ArrayList<SyncServerClass>();
                        syncClassGroupTo.put(type, false);
                        syncClassGroupFrom.put(type, false);
                    }
                    currList.add(serverClass);
                    syncClassGroups.put(type, currList);
                    if ( serverClass.getSendTo() ) syncClassGroupTo.put(type, true); 
                    if ( serverClass.getReceiveFrom() ) syncClassGroupFrom.put(type, true); 
                    //log.warn("Added type " + type + " to list, size is now " + currList.size());
                }

                /*
                 * This algorithm is nicer in theory
                int countLeft = 0;
                int countRight = 0;
                for ( Iterator<Map.Entry<String, List<SyncClass>>> it = syncClassGroups.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<String, List<SyncClass>> entry = it.next();
                    if ( countLeft > countRight ) {
                        syncClassGroupsRight.put(entry.getKey(), entry.getValue());
                        countRight += entry.getValue().size();
                    } else {
                        syncClassGroupsLeft.put(entry.getKey(), entry.getValue());
                        countLeft += entry.getValue().size();
                    }
                }
                */
                // but this one simply is a better end-product
                for ( Iterator<Map.Entry<String, List<SyncServerClass>>> it = syncClassGroups.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<String, List<SyncServerClass>> entry = it.next();
                    if ( entry.getKey().equals("REQUIRED") || entry.getKey().equals("PATIENT") ) {
                        syncClassGroupsLeft.put(entry.getKey(), entry.getValue());
                    } else {
                        syncClassGroupsRight.put(entry.getKey(), entry.getValue());
                    }
                }

            } else {
                //log.warn("SYNCCLASSES CAME BACK NULL");
            }
	        
            ret.put("syncClassGroups", syncClassGroups);
            ret.put("syncClassGroupsLeft", syncClassGroupsLeft);
            ret.put("syncClassGroupsRight", syncClassGroupsRight);
            ret.put("syncClassGroupTo", syncClassGroupTo);
            ret.put("syncClassGroupFrom", syncClassGroupFrom);
	        ret.put("connectionState", connectionState.entrySet());
	        ret.put("serverSchedule", serverSchedule);
	        ret.put("repeatInterval", repeatInterval);
            ret.put("syncDateDisplayFormat", TimestampNormalizer.DATETIME_DISPLAY_FORMAT);
            ret.put("type", ServletRequestUtils.getStringParameter(request, "type", server.getServerType().toString()));
		}
        
	    return ret;
    }

}