/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.docs.swagger;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

//Describes an operation available on a single path
public class Operation {
	
	public static String OPERATION_GET_ALL_METHOD = "doGetAll";
	
	public static String OPERATION_GET_BY_ID = "getByUniqueId";
	
	public static String OPERATION_VOID_RETIRE_METHOD = "delete";
	
	public static String OPERATION_DELETE_METHOD = "purge";
	
	public static String OPERATION_CREATE_METHOD = "create";
	
	public static String OPERATION_UPDATE_METHOD = "save";
	
	@JsonIgnore
	private String name;
	
	//A verbose explanation of the operation behavior
	private String description;
	
	//A short summary of what the operation does
	private String summary;
	
	//A list of MIME types the operation can produce
	private List<String> produces;
	
	/*A list of tags for API documentation control. Tags can be used for logical grouping of operations by resources or any other qualifier.*/
	private List<String> tags;
	
	//A list of parameters that are applicable for this operation
	private List<Parameter> parameters;
	
	//The list of possible responses as they are returned from executing this operation.
	private Map<String, Response> responses;
	
	//Custom field added for specifying if the operation is a search handler
	private String isSearchHandler = "false";
	
	//Unique identifier for the operation
	private String operationId;
	
	public Operation() {
		
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @return the produces
	 */
	public List<String> getProduces() {
		return produces;
	}
	
	/**
	 * @param produces the produces to set
	 */
	public void setProduces(List<String> produces) {
		this.produces = produces;
	}
	
	/**
	 * @return the parameters
	 */
	public List<Parameter> getParameters() {
		return parameters;
	}
	
	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}
	
	/**
	 * @return the responses
	 */
	public Map<String, Response> getResponses() {
		return responses;
	}
	
	/**
	 * @param responses the responses to set
	 */
	public void setResponses(Map<String, Response> responses) {
		this.responses = responses;
	}
	
	/**
	 * @return the tags
	 */
	public List<String> getTags() {
		return tags;
	}
	
	/**
	 * @param tags the tags to set
	 */
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	
	/**
	 * @return the isSearchHandler
	 */
	public String getIsSearchHandler() {
		return isSearchHandler;
	}
	
	/**
	 * @param isSearchHandler the isSearchHandler to set
	 */
	public void setIsSearchHandler(String isSearchHandler) {
		this.isSearchHandler = isSearchHandler;
	}
	
	/**
	 * @return the summary
	 */
	public String getSummary() {
		return summary;
	}
	
	/**
	 * @param summary the summary to set
	 */
	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	public String getOperationId() {
		return operationId;
	}
	
	public void setOperationId(String operationId) {
		this.operationId = operationId;
	}
}
