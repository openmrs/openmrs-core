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

/* The class describes the RESTful API in accordance with the Swagger specification and is represented as JSON objects and conform to the JSON standards */
public class SwaggerSpecification {
	
	//Specifies the Swagger Specification version being used
	private String swagger = "2.0";
	
	// Provides metadata about the API
	private Info info;
	
	//The host (name or ip) serving the API
	private String host;
	
	//The base path on which the API is served
	private String basePath;
	
	//Allows adding meta data to a single tag that is used by the Operation Object.
	private List<Tag> tags;
	
	//The transfer protocol of the API
	private List<String> schemes;
	
	//A list of MIME types the APIs can consume
	private List<String> consumes;
	
	//A list of MIME types the APIs can produce
	private List<String> produces;
	
	//The available paths and operations for the API.
	private Paths paths;
	
	//The security definitions
	private SecurityDefinitions securityDefinitions;
	
	//An object to hold data types produced and consumed by operations.
	private Definitions definitions;
	
	public SwaggerSpecification() {
		
	}
	
	/**
	 * @return the info
	 */
	public Info getInfo() {
		return info;
	}
	
	/**
	 * @param info the info to set
	 */
	public void setInfo(Info info) {
		this.info = info;
	}
	
	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}
	
	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}
	
	/**
	 * @return the basePath
	 */
	public String getBasePath() {
		return basePath;
	}
	
	/**
	 * @param basePath the basePath to set
	 */
	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}
	
	/**
	 * @return the schemes
	 */
	public List<String> getSchemes() {
		return schemes;
	}
	
	/**
	 * @param schemes the schemes to set
	 */
	public void setSchemes(List<String> schemes) {
		this.schemes = schemes;
	}
	
	/**
	 * @return the consumes
	 */
	public List<String> getConsumes() {
		return consumes;
	}
	
	/**
	 * @param consumes the consumes to set
	 */
	public void setConsumes(List<String> consumes) {
		this.consumes = consumes;
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
	 * @return the paths
	 */
	public Paths getPaths() {
		return paths;
	}
	
	/**
	 * @param paths the paths to set
	 */
	public void setPaths(Paths paths) {
		this.paths = paths;
	}
	
	public SecurityDefinitions getSecurityDefinitions() {
		return securityDefinitions;
	}
	
	public void setSecurityDefinitions(SecurityDefinitions securityDefinitions) {
		this.securityDefinitions = securityDefinitions;
	}
	
	/**
	 * @return the definitions
	 */
	public Definitions getDefinitions() {
		return definitions;
	}
	
	/**
	 * @param definitions the definitions to set
	 */
	public void setDefinitions(Definitions definitions) {
		this.definitions = definitions;
	}
	
	/**
	 * @return the swagger
	 */
	public String getSwagger() {
		return swagger;
	}
	
	/**
	 * @return the tags
	 */
	public List<Tag> getTags() {
		return tags;
	}
	
	/**
	 * @param tags the tags to set
	 */
	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}
}
