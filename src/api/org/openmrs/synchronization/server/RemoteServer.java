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
package org.openmrs.synchronization.server;

/**
 *
 */
public class RemoteServer {
	
	private Integer serverId;
	private String nickname;
	private String address;
	private RemoteServerType serverType;
	private String username;
	private String password;
	
	public String getAddress() {
    	return address;
    }

	public void setAddress(String address) {
    	this.address = address;
    }

	public Boolean getIsSSL() {
    	return this.address.startsWith("https");
    }

	public String getNickname() {
    	return nickname;
    }

	public void setNickname(String nickname) {
    	this.nickname = nickname;
    }

	public String getPassword() {
    	return password;
    }

	public void setPassword(String password) {
    	this.password = password;
    }

	public Integer getServerId() {
    	return serverId;
    }

	public void setServerId(Integer serverId) {
    	this.serverId = serverId;
    }
	
	public RemoteServerType getServerType() {
    	return serverType;
    }
	
	public void setServerType(RemoteServerType serverType) {
    	this.serverType = serverType;
    }
	
	public String getUsername() {
    	return username;
    }
	
	public void setUsername(String username) {
    	this.username = username;
    }
	
	
}
