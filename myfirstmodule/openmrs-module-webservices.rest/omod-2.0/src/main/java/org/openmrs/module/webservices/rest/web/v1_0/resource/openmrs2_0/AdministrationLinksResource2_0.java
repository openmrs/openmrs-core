/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_0;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.Extension;
import org.openmrs.module.web.extension.AdministrationSectionExt;
import org.openmrs.module.webservices.helper.ModuleFactoryWrapper;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingReadableResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.wrapper.AdministrationSectionLinks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Resource(name = RestConstants.VERSION_1 + "/administrationlinks", supportedClass = AdministrationSectionLinks.class,
		supportedOpenmrsVersions = { "2.0.* - 9.*" })
public class AdministrationLinksResource2_0 extends BaseDelegatingReadableResource<AdministrationSectionLinks> {

	private static final String UUID = "uuid";

	private static final String DISPLAY = "display";

	private static final String ADMIN_LIST_POINT_ID = "org.openmrs.admin.list";

	private static final String MODULE_TITLE = "title";

	private static final String LINKS = "administrationLinks";

	private ModuleFactoryWrapper moduleFactoryWrapper = new ModuleFactoryWrapper();

	@Override
	public AdministrationSectionLinks newDelegate() {
		return null;
	}

	@Override
	public AdministrationSectionLinks getByUniqueId(String moduleId) {
		// Assumes that moduleId is an id of a module that user wants to get admin links for.
		// AdministrationLinksResource has no id by itself as it's not an OpenMRS data object.

		AdministrationSectionLinks administrationLinks = getAdministrationLinksForModule(moduleId);
		if (administrationLinks == null) {
			throw new ObjectNotFoundException(
					"Module with id: " + moduleId + " doesn't have any administration links registered.");
		}

		return administrationLinks;
	}

	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty(UUID);
			description.addProperty(DISPLAY);
			description.addProperty(MODULE_TITLE);
			description.addProperty(LINKS);
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty(UUID);
			description.addProperty(DISPLAY);
			description.addProperty(MODULE_TITLE);
			description.addProperty(LINKS);
			description.addSelfLink();
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty(UUID);
			description.addProperty(DISPLAY);
			description.addProperty(MODULE_TITLE);
			description.addProperty(LINKS);
			description.addSelfLink();
			return description;
		}
		return null;
	}

	@PropertyGetter(UUID)
	public static String getUuid(AdministrationSectionLinks instance) {
		return instance.getModuleId();
	}

	@PropertyGetter(DISPLAY)
	public static String getDisplay(AdministrationSectionLinks instance) {
		return instance.getTitle();
	}

	@PropertyGetter(LINKS)
	public static Map<String, String> getLinks(AdministrationSectionLinks instance) {
		return instance.getLinks();
	}

	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model
					.property(MODULE_TITLE, new StringProperty())
					.property(LINKS, new ArrayProperty(new ObjectProperty()));
		}

		return model;
	}

	@Override
	public NeedsPaging<AdministrationSectionLinks> doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<>(getAllAdministrationLinks(), context);
	}

	private AdministrationSectionLinks getAdministrationLinksForModule(String moduleId) {
		MessageSourceService messageSourceService = Context.getMessageSourceService();

		List<Extension> adminListsExtensions = moduleFactoryWrapper.getExtensions(ADMIN_LIST_POINT_ID);

		for (Extension adminListExtension : adminListsExtensions) {
			if (adminListExtension instanceof AdministrationSectionExt && adminListExtension.getModuleId()
					.equals(moduleId)) {
				return mapAdminListExtension(adminListExtension, messageSourceService);
			}
		}

		return null;
	}

	private List<AdministrationSectionLinks> getAllAdministrationLinks() {
		MessageSourceService messageSourceService = Context.getMessageSourceService();
		List<AdministrationSectionLinks> modulesWithLinksList = new ArrayList<>();

		List<Extension> adminListsExtensions = moduleFactoryWrapper.getExtensions(ADMIN_LIST_POINT_ID);

		for (Extension adminListExtension : adminListsExtensions) {
			if (adminListExtension instanceof AdministrationSectionExt) {
				modulesWithLinksList.add(mapAdminListExtension(adminListExtension, messageSourceService));
			}
		}

		return modulesWithLinksList;
	}

	private AdministrationSectionLinks mapAdminListExtension(Extension extension,
			MessageSourceService messageSourceService) {
		AdministrationSectionExt adminListExtension = (AdministrationSectionExt) extension;

		// map module title message key to its value
		String title = messageSourceService.getMessage(adminListExtension.getTitle());

		// map link titles to their values
		Map<String, String> links = adminListExtension.getLinks();
		for (Map.Entry<String, String> link : links.entrySet()) {
			link.setValue(messageSourceService.getMessage(link.getValue()));
		}

		AdministrationSectionLinks administrationSectionLinks = new AdministrationSectionLinks();
		administrationSectionLinks.setModuleId(adminListExtension.getModuleId());
		administrationSectionLinks.setTitle(title);
		administrationSectionLinks.setLinks(links);

		return administrationSectionLinks;
	}
}
