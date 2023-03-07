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

import io.swagger.models.Contact;
import io.swagger.models.ExternalDocs;
import io.swagger.models.Info;
import io.swagger.models.License;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.SecurityRequirement;
import io.swagger.models.Swagger;
import io.swagger.models.auth.BasicAuthDefinition;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import io.swagger.util.Json;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.atteo.evo.inflector.English;
import org.openmrs.api.context.Context;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.webservices.docs.SearchHandlerDoc;
import org.openmrs.module.webservices.docs.SearchQueryDoc;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchParameter;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubclassHandler;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.util.ReflectionUtils;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SwaggerSpecificationCreator {
	
	private static Swagger swagger;
	
	private String host;
	
	private String basePath;
	
	private List<Scheme> schemes;
	
	private String baseUrl;
	
	private static List<SearchHandlerDoc> searchHandlerDocs;
	
	PrintStream originalErr;
	
	PrintStream originalOut;
	
	private QueryParameter subclassTypeParameter = new QueryParameter().name("t")
	        .description("The type of Subclass Resource to return")
	        .type("string");
	
	Map<Integer, Level> originalLevels = new HashMap<Integer, Level>();
	
	private Logger log = Logger.getLogger(this.getClass());
	
	public SwaggerSpecificationCreator() {
	}
	
	public SwaggerSpecificationCreator host(String host) {
		this.host = host;
		return this;
	}
	
	public SwaggerSpecificationCreator basePath(String basePath) {
		this.basePath = basePath;
		return this;
	}
	
	public SwaggerSpecificationCreator scheme(Scheme scheme) {
		if (schemes == null) {
			this.schemes = new ArrayList<Scheme>();
		}
		if (!schemes.contains(scheme)) {
			this.schemes.add(scheme);
		}
		return this;
	}
	
	/**
	 * Regenerate the swagger spec from scratch
	 */
	private void BuildJSON() {
		synchronized (this) {
			log.info("Initiating Swagger specification creation");
			toggleLogs(RestConstants.SWAGGER_LOGS_OFF);
			try {
				initSwagger();
				addPaths();
				addDefaultDefinitions();
				//				addSubclassOperations(); //FIXME uncomment after fixing the method
			}
			catch (Exception e) {
				log.error("Error while creating Swagger specification", e);
			}
			finally {
				toggleLogs(RestConstants.SWAGGER_LOGS_ON);
				log.info("Swagger specification creation complete");
			}
		}
	}
	
	public String getJSON() {
		if (isCached()) {
			log.info("Returning a cached copy of Swagger specification");
			initSwagger();
		} else {
			swagger = new Swagger();
			BuildJSON();
		}
		return createJSON();
	}
	
	private void addDefaultDefinitions() {
		// schema of the default response
		// received from fetchAll and search operations
		swagger.addDefinition("FetchAll", new ModelImpl()
		        .property("results", new ArrayProperty()
		                .items(new ObjectProperty()
		                        .property("uuid", new StringProperty())
		                        .property("display", new StringProperty())
		                        .property("links", new ArrayProperty()
		                                .items(new ObjectProperty()
		                                        .property("rel", new StringProperty().example("self"))
		                                        .property("uri", new StringProperty(StringProperty.Format.URI)))))));
	}
	
	private void toggleLogs(boolean targetState) {
		if (Context.getAdministrationService().getGlobalProperty(RestConstants.SWAGGER_QUIET_DOCS_GLOBAL_PROPERTY_NAME)
		        .equals("true")) {
			if (targetState == RestConstants.SWAGGER_LOGS_OFF) {
				// turn off the log4j loggers
				List<Logger> loggers = Collections.<Logger> list(LogManager.getCurrentLoggers());
				loggers.add(LogManager.getRootLogger());
				for (Logger logger : loggers) {
					originalLevels.put(logger.hashCode(), logger.getLevel());
					logger.setLevel(Level.OFF);
				}
				
				// silence stderr and stdout
				originalErr = System.err;
				System.setErr(new PrintStream(new OutputStream() {
					
					public void write(int b) {
						// noop
					}
				}));
				
				originalOut = System.out;
				System.setOut(new PrintStream(new OutputStream() {
					
					public void write(int b) {
						// noop
					}
				}));
			} else if (targetState == RestConstants.SWAGGER_LOGS_ON) {
				List<Logger> loggers = Collections.<Logger> list(LogManager.getCurrentLoggers());
				loggers.add(LogManager.getRootLogger());
				for (Logger logger : loggers) {
					logger.setLevel(originalLevels.get(logger.hashCode()));
				}
				
				System.setErr(originalErr);
				System.setOut(originalOut);
			}
		}
	}
	
	private void initSwagger() {
		final Info info = new Info()
		        .version(OpenmrsConstants.OPENMRS_VERSION_SHORT)
		        .title("OpenMRS API Docs")
		        .description("OpenMRS RESTful API documentation generated by Swagger")
		        .contact(new Contact().name("OpenMRS").url("http://openmrs.org"))
		        .license(new License().name("MPL-2.0 w/ HD").url("http://openmrs.org/license"));
		
		swagger
		        .info(info)
		        .host(this.host)
		        .basePath(this.basePath)
		        .schemes(this.schemes)
		        .securityDefinition("basic_auth", new BasicAuthDefinition())
		        .security(new SecurityRequirement().requirement("basic_auth"))
		        .consumes("application/json")
		        .produces("application/json")
		        .externalDocs(new ExternalDocs()
		                .description("Find more info on REST Module Wiki")
		                .url("https://wiki.openmrs.org/x/xoAaAQ"));
	}
	
	private List<ModuleVersion> getModuleVersions() {
		List<ModuleVersion> moduleVersions = new ArrayList<ModuleVersion>();
		
		for (Module module : ModuleFactory.getLoadedModules()) {
			moduleVersions.add(new ModuleVersion(module.getModuleId(), module.getVersion()));
		}
		
		return moduleVersions;
	}
	
	private boolean testOperationImplemented(OperationEnum operation, DelegatingResourceHandler<?> resourceHandler) {
		Method method;
		try {
			switch (operation) {
				case get:
					method = ReflectionUtils.findMethod(resourceHandler.getClass(), "getAll", RequestContext.class);
					
					if (method == null) {
						return false;
					} else {
						method.invoke(resourceHandler, new RequestContext());
					}
					
					break;
				case getSubresource:
					method = ReflectionUtils.findMethod(resourceHandler.getClass(), "getAll", String.class,
					    RequestContext.class);
					
					if (method == null) {
						return false;
					} else {
						method.invoke(resourceHandler, RestConstants.SWAGGER_IMPOSSIBLE_UNIQUE_ID, new RequestContext());
					}
					
					break;
				case getWithUUID:
				case getSubresourceWithUUID:
					method = ReflectionUtils.findMethod(resourceHandler.getClass(), "getByUniqueId", String.class);
					
					if (method == null) {
						return false;
					} else {
						method.invoke(resourceHandler, RestConstants.SWAGGER_IMPOSSIBLE_UNIQUE_ID);
					}
					
					break;
				case getWithDoSearch:
					method = ReflectionUtils.findMethod(resourceHandler.getClass(), "search", RequestContext.class);
					
					if (method == null) {
						return false;
					} else {
						method.invoke(resourceHandler, new RequestContext());
					}
					
					break;
				case postCreate:
					method = ReflectionUtils.findMethod(resourceHandler.getClass(), "create", SimpleObject.class,
					    RequestContext.class);
					
					if (method == null) {
						return false;
					} else {
						try {
							// to avoid saving data to the database, we pass a null SimpleObject
							method.invoke(resourceHandler, null, new RequestContext());
						}
						catch (ResourceDoesNotSupportOperationException re) {
							return false;
						}
						catch (Exception ee) {
							// if the resource doesn't immediate throw ResourceDoesNotSupportOperationException
							// then we need to check if it's thrown in the save() method
							resourceHandler.save(null);
						}
					}
					
					break;
				case postSubresource:
					method = ReflectionUtils.findMethod(resourceHandler.getClass(), "create", String.class,
					    SimpleObject.class, RequestContext.class);
					
					if (method == null) {
						return false;
					} else {
						try {
							// to avoid saving data to the database, we pass a null SimpleObject
							method.invoke(resourceHandler, null, RestConstants.SWAGGER_IMPOSSIBLE_UNIQUE_ID,
							    new RequestContext());
						}
						catch (InvocationTargetException e) {
							if (e.getCause() instanceof ResourceDoesNotSupportOperationException) {
								return false;
							}
							resourceHandler.save(null);
						}
						catch (Exception ee) {
							// if the resource doesn't immediate throw ResourceDoesNotSupportOperationException
							// then we need to check if it's thrown in the save() method
							resourceHandler.save(null);
						}
					}
					
					break;
				case postUpdate:
					method = ReflectionUtils.findMethod(resourceHandler.getClass(), "update", String.class,
					    SimpleObject.class, RequestContext.class);
					
					if (method == null) {
						return false;
					} else {
						method.invoke(resourceHandler, RestConstants.SWAGGER_IMPOSSIBLE_UNIQUE_ID,
						    buildPOSTUpdateSimpleObject(resourceHandler), new RequestContext());
					}
					
					break;
				case postUpdateSubresouce:
					method = ReflectionUtils.findMethod(resourceHandler.getClass(), "update", String.class, String.class,
					    SimpleObject.class, RequestContext.class);
					
					if (method == null) {
						return false;
					} else {
						method.invoke(resourceHandler, RestConstants.SWAGGER_IMPOSSIBLE_UNIQUE_ID,
						    RestConstants.SWAGGER_IMPOSSIBLE_UNIQUE_ID, buildPOSTUpdateSimpleObject(resourceHandler),
						    new RequestContext());
					}
					
					break;
				case delete:
					method = ReflectionUtils.findMethod(resourceHandler.getClass(), "delete", String.class, String.class,
					    RequestContext.class);
					
					if (method == null) {
						return false;
					} else {
						method.invoke(resourceHandler, RestConstants.SWAGGER_IMPOSSIBLE_UNIQUE_ID, "",
						    new RequestContext());
					}
					
					break;
				case deleteSubresource:
					method = ReflectionUtils.findMethod(resourceHandler.getClass(), "delete", String.class, String.class,
					    String.class, RequestContext.class);
					
					if (method == null) {
						return false;
					} else {
						method.invoke(resourceHandler, RestConstants.SWAGGER_IMPOSSIBLE_UNIQUE_ID,
						    RestConstants.SWAGGER_IMPOSSIBLE_UNIQUE_ID, "", new RequestContext());
					}
					break;
				case purge:
					method = ReflectionUtils.findMethod(resourceHandler.getClass(), "purge", String.class,
					    RequestContext.class);
					
					if (method == null) {
						return false;
					} else {
						method.invoke(resourceHandler, RestConstants.SWAGGER_IMPOSSIBLE_UNIQUE_ID, new RequestContext());
					}
					
					break;
				case purgeSubresource:
					method = ReflectionUtils.findMethod(resourceHandler.getClass(), "purge", String.class, String.class,
					    RequestContext.class);
					
					if (method == null) {
						return false;
					} else {
						method.invoke(resourceHandler, RestConstants.SWAGGER_IMPOSSIBLE_UNIQUE_ID,
						    RestConstants.SWAGGER_IMPOSSIBLE_UNIQUE_ID, new RequestContext());
					}
			}
			return true;
		}
		catch (Exception e) {
			return !(e instanceof ResourceDoesNotSupportOperationException)
			        && !(e.getCause() instanceof ResourceDoesNotSupportOperationException);
		}
	}
	
	private void sortResourceHandlers(List<DelegatingResourceHandler<?>> resourceHandlers) {
		Collections.sort(resourceHandlers, new Comparator<DelegatingResourceHandler<?>>() {
			
			@Override
			public int compare(DelegatingResourceHandler<?> left, DelegatingResourceHandler<?> right) {
				return isSubclass(left).compareTo(isSubclass(right));
			}
			
			private Boolean isSubclass(DelegatingResourceHandler<?> resourceHandler) {
				return resourceHandler.getClass().getAnnotation(SubResource.class) != null;
			}
		});
	}
	
	private SimpleObject buildPOSTUpdateSimpleObject(DelegatingResourceHandler<?> resourceHandler) {
		SimpleObject simpleObject = new SimpleObject();
		
		for (String property : resourceHandler.getUpdatableProperties().getProperties().keySet()) {
			simpleObject.put(property, property);
		}
		
		return simpleObject;
	}
	
	private Path buildFetchAllPath(Path path,
	        DelegatingResourceHandler<?> resourceHandler, String resourceName, String resourceParentName) {
		
		Operation getOperation = null;
		if (resourceParentName == null) {
			if (testOperationImplemented(OperationEnum.get, resourceHandler)) {
				
				getOperation = createOperation(resourceHandler, "get", resourceName, null,
				    OperationEnum.get);
			}
		} else {
			if (testOperationImplemented(OperationEnum.getSubresource, resourceHandler)) {
				getOperation = createOperation(resourceHandler, "get", resourceName, resourceParentName,
				    OperationEnum.getSubresource);
			}
		}
		
		if (getOperation != null) {
			path.setGet(getOperation);
		}
		
		return path;
	}
	
	private Path buildGetWithUUIDPath(Path path,
	        DelegatingResourceHandler<?> resourceHandler, String resourceName, String resourceParentName) {
		
		Operation getOperation = null;
		
		if (testOperationImplemented(OperationEnum.getWithUUID, resourceHandler)) {
			if (resourceParentName == null) {
				getOperation = createOperation(resourceHandler, "get", resourceName, null,
				    OperationEnum.getWithUUID);
			} else {
				getOperation = createOperation(resourceHandler, "get", resourceName, resourceParentName,
				    OperationEnum.getSubresourceWithUUID);
			}
		}
		
		if (getOperation != null) {
			path.get(getOperation);
		}
		return path;
	}
	
	private Path buildCreatePath(Path path,
	        DelegatingResourceHandler<?> resourceHandler, String resourceName, String resourceParentName) {
		
		Operation postCreateOperation = null;
		
		if (resourceParentName == null) {
			if (testOperationImplemented(OperationEnum.postCreate, resourceHandler)) {
				postCreateOperation = createOperation(resourceHandler, "post", resourceName, null,
				    OperationEnum.postCreate);
			}
		} else {
			if (testOperationImplemented(OperationEnum.postSubresource, resourceHandler)) {
				postCreateOperation = createOperation(resourceHandler, "post", resourceName, resourceParentName,
				    OperationEnum.postSubresource);
			}
		}
		
		if (postCreateOperation != null) {
			path.post(postCreateOperation);
		}
		return path;
	}
	
	private Path buildUpdatePath(Path path,
	        DelegatingResourceHandler<?> resourceHandler, String resourceName, String resourceParentName) {
		
		Operation postUpdateOperation = null;
		
		if (resourceParentName == null) {
			if (testOperationImplemented(OperationEnum.postUpdate, resourceHandler)) {
				postUpdateOperation = createOperation(resourceHandler, "post", resourceName, resourceParentName,
				    OperationEnum.postUpdate);
			}
		} else {
			if (testOperationImplemented(OperationEnum.postUpdateSubresouce, resourceHandler)) {
				postUpdateOperation = createOperation(resourceHandler, "post", resourceName, resourceParentName,
				    OperationEnum.postUpdateSubresouce);
			}
		}
		
		if (postUpdateOperation != null) {
			path.post(postUpdateOperation);
		}
		return path;
	}
	
	private Path buildDeletePath(Path path,
	        DelegatingResourceHandler<?> resourceHandler, String resourceName, String resourceParentName) {
		
		Operation deleteOperation = null;
		
		if (resourceParentName == null) {
			if (testOperationImplemented(OperationEnum.delete, resourceHandler)) {
				deleteOperation = createOperation(resourceHandler, "delete", resourceName, resourceParentName,
				    OperationEnum.delete);
			}
		} else {
			if (testOperationImplemented(OperationEnum.deleteSubresource, resourceHandler)) {
				deleteOperation = createOperation(resourceHandler, "delete", resourceName, resourceParentName,
				    OperationEnum.deleteSubresource);
			}
		}
		
		if (deleteOperation != null) {
			path.delete(deleteOperation);
		}
		return path;
	}
	
	private Path buildPurgePath(Path path, DelegatingResourceHandler<?> resourceHandler,
	        String resourceName, String resourceParentName) {
		
		if (path.getDelete() != null) {
			// just add optional purge parameter
			Operation deleteOperation = path.getDelete();
			
			deleteOperation.setSummary("Delete or purge resource by uuid");
			deleteOperation.setDescription("The resource will be voided/retired unless purge = 'true'");
			
			QueryParameter purgeParam = new QueryParameter().name("purge").type("boolean");
			deleteOperation.parameter(purgeParam);
		} else {
			// create standalone purge operation with required
			Operation purgeOperation = null;
			
			if (resourceParentName == null) {
				if (testOperationImplemented(OperationEnum.purge, resourceHandler)) {
					purgeOperation = createOperation(resourceHandler, "delete", resourceName, null,
					    OperationEnum.purge);
				}
			} else {
				if (testOperationImplemented(OperationEnum.purgeSubresource, resourceHandler)) {
					purgeOperation = createOperation(resourceHandler, "delete", resourceName, resourceParentName,
					    OperationEnum.purgeSubresource);
				}
			}
			
			if (purgeOperation != null) {
				path.delete(purgeOperation);
			}
		}
		
		return path;
	}
	
	private void addIndividualPath(String resourceParentName, String resourceName, Path path,
	        String pathSuffix) {
		if (!path.getOperations().isEmpty()) {
			if (resourceParentName == null) {
				swagger.path("/" + resourceName + pathSuffix, path);
			} else {
				swagger.path("/" + resourceParentName + "/{parent-uuid}/" + resourceName + pathSuffix, path);
			}
		}
	}
	
	private String buildSearchParameterDependencyString(Set<SearchParameter> dependencies) {
		StringBuffer sb = new StringBuffer();
		
		sb.append("Must be used with ");
		
		List<String> searchParameterNames = new ArrayList<String>();
		for (SearchParameter dependency : dependencies) {
			searchParameterNames.add(dependency.getName());
		}
		sb.append(StringUtils.join(searchParameterNames, ", "));
		
		String ret = sb.toString();
		int ind = ret.lastIndexOf(", ");
		
		if (ind > -1) {
			ret = new StringBuilder(ret).replace(ind, ind + 2, " and ").toString();
		}
		
		return ret;
	}
	
	private void addSearchOperations(DelegatingResourceHandler<?> resourceHandler, String resourceName,
	        String resourceParentName, Path getAllPath) {
		if (resourceName == null) {
			return;
		}
		boolean hasDoSearch = testOperationImplemented(OperationEnum.getWithDoSearch, resourceHandler);
		boolean hasSearchHandler = hasSearchHandler(resourceName, resourceParentName);
		boolean wasNew = false;
		
		if (hasSearchHandler || hasDoSearch) {
			Operation operation;
			// query parameter
			Parameter q = new QueryParameter().name("q")
			        .description("The search query")
			        .type("string");
			
			if (getAllPath.getOperations().isEmpty() || getAllPath.getGet() == null) {
				// create search-only operation
				operation = new Operation();
				operation.tag(resourceParentName == null ? resourceName : resourceParentName);
				operation.produces("application/json").produces("application/xml");
				
				// if the path has no operations, add a note that at least one search parameter must be specified
				operation.setSummary("Search for " + resourceName);
				operation.setDescription("At least one search parameter must be specified");
				
				// representations query parameter
				Parameter v = new QueryParameter().name("v")
				        .description("The representation to return (ref, default, full or custom)")
				        .type("string")
				        ._enum(Arrays.asList("ref", "default", "full", "custom"));
				
				// This implies that the resource has no custom SearchHandler or doGetAll, but has doSearch implemented
				// As there is only one query param 'q', mark it as required
				if (!hasSearchHandler) {
					q.setRequired(true);
				}
				
				operation.setParameters(buildPagingParameters());
				operation.parameter(v).parameter(q);
				operation.addResponse("200", new Response()
				        .description(resourceName + " response")
				        .schema(new RefProperty("#/definitions/FetchAll")));
				if (((BaseDelegatingResource<?>) resourceHandler).hasTypesDefined()) {
					operation.parameter(subclassTypeParameter);
				}
				// since the path has no existing get operations then it is considered new
				wasNew = true;
			} else {
				operation = getAllPath.getGet();
				operation.setSummary("Fetch all non-retired " + resourceName + " resources or perform search");
				operation.setDescription("All search parameters are optional");
				operation.parameter(q);
			}
			
			Map<String, Parameter> parameterMap = new HashMap<String, Parameter>();
			
			if (hasSearchHandler) {
				// FIXME: this isn't perfect, it doesn't cover the case where multiple parameters are required together
				// FIXME: See https://github.com/OAI/OpenAPI-Specification/issues/256
				for (SearchHandler searchHandler : Context.getService(RestService.class).getAllSearchHandlers()) {
					
					String supportedResourceWithVersion = searchHandler.getSearchConfig().getSupportedResource();
					String supportedResource = supportedResourceWithVersion.substring(supportedResourceWithVersion
					        .indexOf('/') + 1);
					
					if (resourceName.equals(supportedResource)) {
						for (SearchQuery searchQuery : searchHandler.getSearchConfig().getSearchQueries()) {
							// parameters with no dependencies
							for (SearchParameter requiredParameter : searchQuery.getRequiredParameters()) {
								Parameter p = new QueryParameter().type("string");
								p.setName(requiredParameter.getName());
								parameterMap.put(requiredParameter.getName(), p);
							}
							// parameters with dependencies
							for (SearchParameter optionalParameter : searchQuery.getOptionalParameters()) {
								Parameter p = new QueryParameter().type("string");
								p.setName(optionalParameter.getName());
								p.setDescription(buildSearchParameterDependencyString(searchQuery.getRequiredParameters()));
								parameterMap.put(optionalParameter.getName(), p);
							}
						}
					}
				}
			}
			
			for (Parameter p : parameterMap.values()) {
				operation.parameter(p);
			}
			operation.setOperationId("getAll" + getOperationTitle(resourceHandler, true));
			
			if (wasNew) {
				getAllPath.setGet(operation);
			}
		}
	}
	
	private void addPaths() {
		// get all registered resource handlers
		List<DelegatingResourceHandler<?>> resourceHandlers = Context.getService(RestService.class).getResourceHandlers();
		sortResourceHandlers(resourceHandlers);
		
		// generate swagger JSON for each handler
		for (DelegatingResourceHandler<?> resourceHandler : resourceHandlers) {
			
			// get name and parent if it's a subresource
			Resource annotation = resourceHandler.getClass().getAnnotation(Resource.class);
			
			String resourceParentName = null;
			String resourceName = null;
			
			if (annotation != null) {
				// top level resource
				resourceName = annotation.name().substring(annotation.name().indexOf('/') + 1);
			} else {
				// subresource
				SubResource subResourceAnnotation = resourceHandler.getClass().getAnnotation(SubResource.class);
				
				if (subResourceAnnotation != null) {
					Resource parentResourceAnnotation = subResourceAnnotation.parent().getAnnotation(Resource.class);
					
					resourceName = subResourceAnnotation.path();
					resourceParentName = parentResourceAnnotation.name().substring(
					    parentResourceAnnotation.name().indexOf('/') + 1);
				}
			}
			
			// subclass operations are handled separately in another method
			if (resourceHandler instanceof DelegatingSubclassHandler)
				continue;
			
			// set up paths
			Path rootPath = new Path();
			Path uuidPath = new Path();
			
			/////////////////////////
			// GET all             //
			/////////////////////////
			Path rootPathGetAll = buildFetchAllPath(rootPath, resourceHandler, resourceName,
			    resourceParentName);
			addIndividualPath(resourceParentName, resourceName, rootPathGetAll, "");
			
			/////////////////////////
			// GET search          //
			/////////////////////////
			addSearchOperations(resourceHandler, resourceName, resourceParentName, rootPathGetAll);
			
			/////////////////////////
			// POST create         //
			/////////////////////////
			Path rootPathPostCreate = buildCreatePath(rootPathGetAll, resourceHandler, resourceName,
			    resourceParentName);
			addIndividualPath(resourceParentName, resourceName, rootPathPostCreate, "");
			
			/////////////////////////
			// GET with UUID       //
			/////////////////////////
			Path uuidPathGetAll = buildGetWithUUIDPath(uuidPath, resourceHandler, resourceName,
			    resourceParentName);
			addIndividualPath(resourceParentName, resourceName, uuidPathGetAll, "/{uuid}");
			
			/////////////////////////
			// POST update         //
			/////////////////////////
			Path uuidPathPostUpdate = buildUpdatePath(uuidPathGetAll, resourceHandler, resourceName,
			    resourceParentName);
			addIndividualPath(resourceParentName, resourceName, uuidPathPostUpdate, "/{uuid}");
			
			/////////////////////////
			// DELETE              //
			/////////////////////////
			Path uuidPathDelete = buildDeletePath(uuidPathPostUpdate, resourceHandler, resourceName,
			    resourceParentName);
			
			/////////////////////////
			// DELETE (purge)      //
			/////////////////////////
			Path uuidPathPurge = buildPurgePath(uuidPathDelete, resourceHandler, resourceName,
			    resourceParentName);
			addIndividualPath(resourceParentName, resourceName, uuidPathPurge, "/{uuid}");
		}
	}
	
	private void addSubclassOperations() {
		// FIXME: this needs to be improved a lot
		List<DelegatingResourceHandler<?>> resourceHandlers = Context.getService(RestService.class).getResourceHandlers();
		for (DelegatingResourceHandler<?> resourceHandler : resourceHandlers) {
			
			if (!(resourceHandler instanceof DelegatingSubclassHandler))
				continue;
			
			Class<?> resourceClass = ((DelegatingSubclassHandler<?, ?>) resourceHandler).getSuperclass();
			String resourceName = resourceClass.getSimpleName().toLowerCase();
			
			// 1. add non-optional enum property to model
			Path path = swagger.getPath("/" + resourceName);
			if (path == null)
				continue;
			
			// FIXME: implement other operations when required
			Operation post = path.getPost();
			if (post == null)
				continue;
			
			Model definition = swagger.getDefinitions().get(StringUtils.capitalize(resourceName) + "Create");
			if (definition == null)
				continue;
			
			Map<String, Property> properties = definition.getProperties();
			
			// 2. merge subclass properties into definition
			for (Map.Entry<String, Property> prop : resourceHandler.getGETModel(Representation.FULL).getProperties()
			        .entrySet()) {
				if (properties.get(prop.getKey()) == null) {
					properties.put(prop.getKey(), prop.getValue());
				}
			}
			
			// 3. update description
			post.setDescription("Certain properties may be required depending on type");
		}
	}
	
	@Deprecated
	private List<org.openmrs.module.webservices.docs.swagger.Parameter> getParametersListForSearchHandlers(
	        String resourceName, String searchHandlerId, int queryIndex) {
		List<org.openmrs.module.webservices.docs.swagger.Parameter> parameters = new ArrayList<org.openmrs.module.webservices.docs.swagger.Parameter>();
		String resourceURL = getResourceUrl(getBaseUrl(), resourceName);
		for (SearchHandlerDoc searchDoc : searchHandlerDocs) {
			if (searchDoc.getSearchHandlerId().equals(searchHandlerId) && searchDoc.getResourceURL().equals(resourceURL)) {
				SearchQueryDoc queryDoc = searchDoc.getSearchQueriesDoc().get(queryIndex);
				for (SearchParameter requiredParameter : queryDoc.getRequiredParameters()) {
					org.openmrs.module.webservices.docs.swagger.Parameter parameter = new org.openmrs.module.webservices.docs.swagger.Parameter();
					parameter.setName(requiredParameter.getName());
					parameter.setIn("query");
					parameter.setDescription("");
					parameter.setRequired(true);
					parameters.add(parameter);
				}
				for (SearchParameter optionalParameter : queryDoc.getOptionalParameters()) {
					org.openmrs.module.webservices.docs.swagger.Parameter parameter = new org.openmrs.module.webservices.docs.swagger.Parameter();
					parameter.setName(optionalParameter.getName());
					parameter.setIn("query");
					parameter.setDescription("");
					parameter.setRequired(false);
					parameters.add(parameter);
				}
				break;
			}
		}
		return parameters;
	}
	
	private String createJSON() {
		return Json.pretty(swagger);
	}
	
	private Parameter buildRequiredUUIDParameter(String name, String desc) {
		return new PathParameter().name(name).description(desc).type("string");
	}
	
	private List<Parameter> buildPagingParameters() {
		List<Parameter> params = new ArrayList<Parameter>();
		
		Parameter limit = new QueryParameter().name("limit")
		        .description("The number of results to return").type("integer");
		
		Parameter startIndex = new QueryParameter().name("startIndex")
		        .description("The offset at which to start").type("integer");
		
		params.add(limit);
		params.add(startIndex);
		
		return params;
	}
	
	private Parameter buildPOSTBodyParameter(String resourceName, String resourceParentName,
	        OperationEnum operationEnum) {
		BodyParameter bodyParameter = new BodyParameter();
		bodyParameter.setRequired(true);
		
		switch (operationEnum) {
			case postCreate:
			case postSubresource:
				bodyParameter.setName("resource");
				bodyParameter.setDescription("Resource to create");
				break;
			case postUpdate:
			case postUpdateSubresouce:
				bodyParameter.setName("resource");
				bodyParameter.setDescription("Resource properties to update");
		}
		
		bodyParameter.schema(new RefModel(getSchemaRef(resourceName, resourceParentName, operationEnum)));
		
		return bodyParameter;
	}
	
	private String getSchemaName(String resourceName, String resourceParentName, OperationEnum operationEnum) {
		
		String suffix = "";
		
		switch (operationEnum) {
			case get:
			case getSubresource:
			case getWithUUID:
			case getSubresourceWithUUID:
				suffix = "Get";
				break;
			case postCreate:
			case postSubresource:
				suffix = "Create";
				break;
			case postUpdate:
			case postUpdateSubresouce:
				suffix = "Update";
				break;
		}
		
		String modelRefName;
		
		if (resourceParentName == null) {
			modelRefName = StringUtils.capitalize(resourceName) + suffix;
		} else {
			modelRefName = StringUtils.capitalize(resourceParentName) + StringUtils.capitalize(resourceName) + suffix;
		}
		
		// get rid of slashes in model names
		String[] split = modelRefName.split("\\/");
		StringBuilder ret = new StringBuilder();
		for (String s : split) {
			ret.append(StringUtils.capitalize(s));
		}
		
		return ret.toString();
	}
	
	private String getSchemaRef(String resourceName, String resourceParentName, OperationEnum operationEnum) {
		return "#/definitions/" + getSchemaName(resourceName, resourceParentName, operationEnum);
	}
	
	private String getOperationTitle(DelegatingResourceHandler<?> resourceHandler, Boolean pluralize) {
		StringBuilder ret = new StringBuilder();
		English inflector = new English();
		
		// get rid of slashes
		String simpleClassName = resourceHandler.getClass().getSimpleName();
		
		// get rid of 'Resource' and version number suffixes
		simpleClassName = simpleClassName.replaceAll("\\d_\\d{1,2}$", "");
		simpleClassName = simpleClassName.replaceAll("Resource$", "");
		
		// pluralize if require
		if (pluralize) {
			String[] words = simpleClassName.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
			String suffix = words[words.length - 1];
			
			for (int i = 0; i < words.length - 1; i++) {
				ret.append(words[i]);
			}
			
			ret.append(inflector.getPlural(suffix));
		} else {
			ret.append(simpleClassName);
		}
		
		return ret.toString();
	}
	
	private void createDefinition(OperationEnum operationEnum, String resourceName, String resourceParentName,
	        DelegatingResourceHandler<?> resourceHandler) {
		
		String definitionName = getSchemaName(resourceName, resourceParentName, operationEnum);
		Model model = null;
		Model modelRef = null;
		Model modelFull = null;
		
		if (definitionName.endsWith("Get")) {
			model = resourceHandler.getGETModel(Representation.DEFAULT);
			modelRef = resourceHandler.getGETModel(Representation.REF);
			modelFull = resourceHandler.getGETModel(Representation.FULL);
		} else if (definitionName.endsWith("Create")) {
			model = resourceHandler.getCREATEModel(Representation.DEFAULT);
			modelFull = resourceHandler.getCREATEModel(Representation.FULL);
		} else if (definitionName.endsWith("Update")) {
			model = resourceHandler.getUPDATEModel(Representation.DEFAULT);
		}
		
		if (model != null) {
			swagger.addDefinition(definitionName, model);
		}
		if (modelRef != null) {
			swagger.addDefinition(definitionName + "Ref", modelRef);
		}
		if (modelFull != null) {
			swagger.addDefinition(definitionName + "Full", modelFull);
		}
	}
	
	/**
	 * @param resourceHandler
	 * @param operationName get, post, delete
	 * @param resourceName
	 * @param resourceParentName
	 * @param representation
	 * @param operationEnum
	 * @return
	 */
	private Operation createOperation(DelegatingResourceHandler<?> resourceHandler, String operationName,
	        String resourceName, String resourceParentName, OperationEnum operationEnum) {
		
		Operation operation = new Operation()
		        .tag(resourceParentName == null ? resourceName : resourceParentName)
		        .consumes("application/json").produces("application/json");
		
		// create definition
		if (operationName == "post" || operationName == "get") {
			createDefinition(operationEnum, resourceName, resourceParentName, resourceHandler);
		}
		
		// create all possible responses
		// 200 response (Successful operation)
		Response response200 = new Response().description(resourceName + " response");
		
		// 201 response (Successfully created)
		Response response201 = new Response().description(resourceName + " response");
		
		// 204 delete success
		Response response204 = new Response().description("Delete successful");
		
		// 401 response (User not logged in)
		Response response401 = new Response().description("User not logged in");
		
		// 404 (Object with given uuid doesn't exist)
		Response response404 = new Response()
		        .description("Resource with given uuid doesn't exist");
		
		// create all possible query params
		// representations query parameter
		Parameter v = new QueryParameter().name("v")
		        .description("The representation to return (ref, default, full or custom)")
		        .type("string")
		        ._enum(Arrays.asList("ref", "default", "full", "custom"));
		
		if (operationEnum == OperationEnum.get) {
			
			operation.setSummary("Fetch all non-retired");
			operation.setOperationId("getAll" + getOperationTitle(resourceHandler, true));
			operation.addResponse("200", response200.schema(new RefProperty("#/definitions/FetchAll")));
			operation.setParameters(buildPagingParameters());
			operation.parameter(v);
			if (((BaseDelegatingResource<?>) resourceHandler).hasTypesDefined()) {
				operation.parameter(subclassTypeParameter);
			}
			
		} else if (operationEnum == OperationEnum.getWithUUID) {
			
			operation.setSummary("Fetch by uuid");
			operation.setOperationId("get" + getOperationTitle(resourceHandler, false));
			operation.parameter(v);
			operation.parameter(buildRequiredUUIDParameter("uuid", "uuid to filter by"));
			operation.addResponse("200",
			    response200.schema(new RefProperty(getSchemaRef(resourceName, resourceParentName, OperationEnum.get))));
			operation.addResponse("404", response404);
			
		} else if (operationEnum == OperationEnum.postCreate) {
			
			operation.setSummary("Create with properties in request");
			operation.setOperationId("create" + getOperationTitle(resourceHandler, false));
			operation.parameter(buildPOSTBodyParameter(resourceName, resourceParentName, OperationEnum.postCreate));
			operation.addResponse("201", response201);
			
		} else if (operationEnum == OperationEnum.postUpdate) {
			
			operation.setSummary("Edit with given uuid, only modifying properties in request");
			operation.setOperationId("update" + getOperationTitle(resourceHandler, false));
			operation.parameter(buildRequiredUUIDParameter("uuid", "uuid of resource to update"));
			operation.parameter(buildPOSTBodyParameter(resourceName, resourceParentName, OperationEnum.postUpdate));
			operation.addResponse("201", response201);
			
		} else if (operationEnum == OperationEnum.getSubresource) {
			
			operation.setSummary("Fetch all non-retired " + resourceName + " subresources");
			operation.setOperationId("getAll" + getOperationTitle(resourceHandler, true));
			operation.setParameters(buildPagingParameters());
			operation.parameter(buildRequiredUUIDParameter("parent-uuid", "parent resource uuid"));
			operation.parameter(v);
			operation.addResponse("200", response200.schema(new ObjectProperty()
			        .property("results", new ArrayProperty(
			                new RefProperty(getSchemaRef(resourceName, resourceParentName, OperationEnum.get))))));
			
		} else if (operationEnum == OperationEnum.postSubresource) {
			
			operation.setSummary("Create " + resourceName + " subresource with properties in request");
			operation.setOperationId("create" + getOperationTitle(resourceHandler, false));
			operation.parameter(buildRequiredUUIDParameter("parent-uuid", "parent resource uuid"));
			operation.parameter(buildPOSTBodyParameter(resourceName, resourceParentName, OperationEnum.postSubresource));
			operation.addResponse("201", response201);
			
		} else if (operationEnum == OperationEnum.postUpdateSubresouce) {
			
			operation.setSummary("edit " + resourceName
			        + " subresource with given uuid, only modifying properties in request");
			operation.setOperationId("update" + getOperationTitle(resourceHandler, false));
			operation.parameter(buildRequiredUUIDParameter("parent-uuid", "parent resource uuid"));
			operation.parameter(buildRequiredUUIDParameter("uuid", "uuid of resource to update"));
			operation
			        .parameter(buildPOSTBodyParameter(resourceName, resourceParentName, OperationEnum.postUpdateSubresouce));
			operation.addResponse("201", response201);
			
		} else if (operationEnum == OperationEnum.getSubresourceWithUUID) {
			operation.setSummary("Fetch " + resourceName + " subresources by uuid");
			operation.setOperationId("get" + getOperationTitle(resourceHandler, false));
			operation.parameter(buildRequiredUUIDParameter("parent-uuid", "parent resource uuid"));
			operation.parameter(buildRequiredUUIDParameter("uuid", "uuid to filter by"));
			operation.parameter(v);
			operation.addResponse("200", response200.schema(new RefProperty(getSchemaRef(resourceName, resourceParentName,
			    OperationEnum.getSubresourceWithUUID))));
			operation.addResponse("404", response404);
			
		} else if (operationEnum == OperationEnum.delete) {
			
			operation.setSummary("Delete resource by uuid");
			operation.setOperationId("delete" + getOperationTitle(resourceHandler, false));
			operation.parameter(buildRequiredUUIDParameter("uuid", "uuid to delete"));
			operation.response(204, response204);
			operation.response(404, response404);
			
		} else if (operationEnum == OperationEnum.deleteSubresource) {
			operation.setSummary("Delete " + resourceName + " subresource by uuid");
			operation.setOperationId("delete" + getOperationTitle(resourceHandler, false));
			operation.parameter(buildRequiredUUIDParameter("parent-uuid", "parent resource uuid"));
			operation.parameter(buildRequiredUUIDParameter("uuid", "uuid to delete"));
			operation.response(204, response204);
			operation.response(404, response404);
			
		} else if (operationEnum == OperationEnum.purge) {
			
			operation.setSummary("Purge resource by uuid");
			operation.setOperationId("purge" + getOperationTitle(resourceHandler, false));
			operation.parameter(buildRequiredUUIDParameter("uuid", "uuid to delete"));
			operation.response(204, response204);
			
		} else if (operationEnum == OperationEnum.purgeSubresource) {
			
			operation.setSummary("Purge " + resourceName + " subresource by uuid");
			operation.setOperationId("purge" + getOperationTitle(resourceHandler, false));
			operation.parameter(buildRequiredUUIDParameter("parent-uuid", "parent resource uuid"));
			operation.parameter(buildRequiredUUIDParameter("uuid", "uuid to delete"));
			operation.response(204, response204);
		}
		
		operation.response(401, response401);
		
		return operation;
	}
	
	private static List<SearchHandlerDoc> fillSearchHandlers(List<SearchHandler> searchHandlers, String url) {
		
		List<SearchHandlerDoc> searchHandlerDocList = new ArrayList<SearchHandlerDoc>();
		String baseUrl = url.replace("/rest", "");
		
		for (int i = 0; i < searchHandlers.size(); i++) {
			if (searchHandlers.get(i) != null) {
				SearchHandler searchHandler = searchHandlers.get(i);
				SearchHandlerDoc searchHandlerDoc = new SearchHandlerDoc(searchHandler, baseUrl);
				searchHandlerDocList.add(searchHandlerDoc);
			}
		}
		
		return searchHandlerDocList;
	}
	
	private String getResourceUrl(String baseUrl, String resourceName) {
		//Set the root url.
		return baseUrl + "/v1/" + resourceName;
	}
	
	public boolean hasSearchHandler(String resourceName, String resourceParentName) {
		if (resourceParentName != null) {
			resourceName = RestConstants.VERSION_1 + "/" + resourceParentName + "/" + resourceName;
		} else {
			resourceName = RestConstants.VERSION_1 + "/" + resourceName;
		}
		
		List<SearchHandler> searchHandlers = Context.getService(RestService.class).getAllSearchHandlers();
		for (SearchHandler searchHandler : searchHandlers) {
			if (searchHandler.getSearchConfig().getSupportedResource().equals(resourceName)) {
				return true;
			}
		}
		return false;
	}
	
	public String getBaseUrl() {
		return baseUrl;
	}
	
	public Swagger getSwagger() {
		return swagger;
	}
	
	/**
	 * @return true if and only if swagger is not null, and its paths are also set.
	 */
	public static boolean isCached() {
		return swagger != null && swagger.getPaths() != null;
	}
	
	public static void clearCache() {
		swagger = null;
	}
	
}
