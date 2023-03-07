/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openmrs.GlobalProperty;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.StringProperty;

/**
 * {@link Resource} for {@link GlobalProperty}, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/systemsetting", supportedClass = GlobalProperty.class, supportedOpenmrsVersions = {
        "1.9.* - 9.*" })
public class SystemSettingResource1_9 extends DelegatingCrudResource<GlobalProperty> {
	
	public static final String GENERAL = "General Settings";
	
	/**
	 * @see DelegatingCrudResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addSelfLink();
			return description;
		} else if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("property");
			description.addProperty("value");
			description.addProperty("description");
			description.addProperty("display");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("property");
			description.addProperty("value");
			description.addProperty("description");
			description.addProperty("display");
			description.addProperty("datatypeClassname");
			description.addProperty("datatypeConfig");
			description.addProperty("preferredHandlerClassname");
			description.addProperty("handlerConfig");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("property");
		description.addProperty("description");
		description.addProperty("datatypeClassname");
		description.addProperty("datatypeConfig");
		description.addProperty("preferredHandlerClassname");
		description.addProperty("handlerConfig");
		description.addProperty("value");
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getUpdatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = getCreatableProperties();
		description.removeProperty("property");
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = ((ModelImpl) super.getGETModel(rep));
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model
			        .property("uuid", new StringProperty())
			        .property("property", new StringProperty())
			        .property("value", new StringProperty())
			        .property("description", new StringProperty())
			        .property("display", new StringProperty());
		}
		if (rep instanceof FullRepresentation) {
			model
			        .property("datatypeClassname", new StringProperty())
			        .property("datatypeConfig", new StringProperty())
			        .property("preferredHandlerClassname", new StringProperty())
			        .property("handlerConfig", new StringProperty());
		}
		return model;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return new ModelImpl()
		        .property("property", new StringProperty())
		        .property("description", new StringProperty())
		        .property("datatypeClassname", new StringProperty())
		        .property("datatypeConfig", new StringProperty())
		        .property("preferredHandlerClassname", new StringProperty())
		        .property("handlerConfig", new StringProperty())
		        .property("value", new StringProperty())
		        
		        .required("property");
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		Model model = getCREATEModel(rep);
		model.getProperties().remove("property");
		return model;
	}
	
	/**
	 * @see DelegatingCrudResource#newDelegate()
	 */
	@Override
	public GlobalProperty newDelegate() {
		return new GlobalProperty();
	}
	
	/**
	 * @see DelegatingCrudResource#save(java.lang.Object)
	 */
	@Override
	public GlobalProperty save(GlobalProperty property) {
		return Context.getAdministrationService().saveGlobalProperty(property);
	}
	
	/**
	 * Fetches a global property by uuid, if no match is found, it tries to look up one with a
	 * matching name with the assumption that the passed parameter is a global property name
	 * 
	 * @see DelegatingCrudResource#getByUniqueId(java.lang.String)
	 */
	@Override
	public GlobalProperty getByUniqueId(String uuid) {
		GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyByUuid(uuid);
		if (gp == null) {
			//We assume the caller is fetching by name
			gp = Context.getAdministrationService().getGlobalPropertyObject(uuid);
		}
		
		return gp;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#delete(java.lang.Object,
	 *      java.lang.String, org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected void delete(GlobalProperty property, String reason, RequestContext context) throws ResponseException {
		if (property == null) {
			return;
		}
		Context.getAdministrationService().purgeGlobalProperty(property);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(GlobalProperty property, RequestContext context) throws ResponseException {
		if (property == null) {
			return;
		}
		Context.getAdministrationService().purgeGlobalProperty(property);
	}
	
	/**
	 * Get all the global properties
	 * 
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	protected NeedsPaging<GlobalProperty> doGetAll(RequestContext context) {
		List<GlobalProperty> allGlobalPropertys = Context.getAdministrationService().getAllGlobalProperties();
		return new NeedsPaging<GlobalProperty>(allGlobalPropertys, context);
	}
	
	/**
	 * GlobalProperty searches support the following additional query parameters:
	 * 
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(RequestContext)
	 */
	@Override
	protected PageableResult doSearch(RequestContext context) {
		AdministrationService service = Context.getAdministrationService();
		List<GlobalProperty> searchResults;
		searchResults = service.getGlobalPropertiesByPrefix(context.getParameter("q"));
		return new NeedsPaging<GlobalProperty>(searchResults, context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getResourceVersion()
	 */
	@Override
	public String getResourceVersion() {
		return "1.9";
	}
	
	/**
	 * Gets the display name of the global property delegate
	 * 
	 * @param instance the delegate instance to get the display name off
	 * @return string as "section - name = value"
	 */
	@PropertyGetter("display")
	public static String getDisplayString(GlobalProperty globalProperty) {
		return getSection(globalProperty) + " - " + getName(globalProperty) + " = " + globalProperty.getValue();
	}
	
	/**
	 * @return the section
	 */
	private static String getSection(GlobalProperty globalProperty) {
		String section = GENERAL;
		int sectionEnd = globalProperty.getProperty().indexOf(".");
		if (sectionEnd > 0) {
			section = globalProperty.getProperty().substring(0, sectionEnd);
			section = beautify(section);
		}
		
		return section;
	}
	
	/**
	 * @return the name
	 */
	private static String getName(GlobalProperty globalProperty) {
		String name = globalProperty.getProperty();
		int sectionEnd = globalProperty.getProperty().indexOf(".");
		if (sectionEnd > 0) {
			name = globalProperty.getProperty().substring(sectionEnd + 1);
		}
		
		name = beautify(name);
		
		return name;
	}
	
	/**
	 * Beautifies a string
	 * 
	 * @param section
	 * @return
	 */
	private static String beautify(String section) {
		section = section.replace("_", " ");
		section = section.replace(".", " ");
		
		String[] sections = StringUtils.splitByCharacterTypeCamelCase(section);
		section = StringUtils.join(sections, " ");
		
		sections = StringUtils.split(section);
		for (int i = 0; i < sections.length; i++) {
			sections[i] = StringUtils.capitalize(sections[i]);
		}
		section = StringUtils.join(sections, " ");
		
		return section;
	}
	
	/**
	 * Gets the value of the global property delegate
	 * 
	 * @param instance the delegate instance to get the value off
	 * @return value object
	 */
	@PropertyGetter("value")
	public static Object getValue(GlobalProperty globalProperty) {
		if (StringUtils.isNotEmpty(globalProperty.getDatatypeClassname())
		        && StringUtils.isNotEmpty(globalProperty.getDatatypeConfig())) {
			return globalProperty.getValue();
		} else {
			return globalProperty.getPropertyValue();
		}
		
	}
	
	/**
	 * Sets value for given property.
	 * 
	 * @param property
	 * @param value
	 */
	@PropertySetter("value")
	public static void setValue(GlobalProperty property, String value) throws Exception {
		if (StringUtils.isNotEmpty(property.getDatatypeClassname())) {
			CustomDatatype customDataType = CustomDatatypeUtil.getDatatype(property.getDatatypeClassname(),
			    property.getDatatypeConfig());
			if (customDataType != null) {
				try {
					property.setValue(customDataType.fromReferenceString(value));
				}
				catch (Exception ex) {
					throw new APIException("Exception in converting value to custom data type", ex);
				}
			} else {
				throw new APIException("Custom data type is null as per provided parameters");
			}
		} else {
			property.setPropertyValue(value);
		}
	}
}
