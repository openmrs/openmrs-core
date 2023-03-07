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
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.StringProperty;
import liquibase.changelog.ChangeSet;
import org.openmrs.module.webservices.docs.swagger.core.property.EnumProperty;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingReadableResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.GenericRestException;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.util.DatabaseUpdater;

import java.util.List;

/**
 * {@link Resource} for {@link DatabaseUpdater.OpenMRSChangeSet}, supporting Read operation.
 */
@Resource(name = RestConstants.VERSION_1
		+ "/databasechange", supportedClass = DatabaseUpdater.OpenMRSChangeSet.class, supportedOpenmrsVersions = {
		"2.0.* - 9.*" })
public class DatabaseChangeResource2_0 extends BaseDelegatingReadableResource<DatabaseUpdater.OpenMRSChangeSet> {

	private static final String UUID = "uuid";

	private static final String DISPLAY = "display";

	private static final String AUTHOR = "author";

	private static final String COMMENTS = "comments";

	private static final String DESCRIPTION = "description";

	private static final String RUN_STATUS = "runStatus";

	private static final String RAN_DATE = "ranDate";

	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty(UUID);
			description.addProperty(DISPLAY);
			description.addProperty(AUTHOR);
			description.addProperty(DESCRIPTION);
			description.addProperty(RUN_STATUS);
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty(UUID);
			description.addProperty(DISPLAY);
			description.addProperty(AUTHOR);
			description.addProperty(DESCRIPTION);
			description.addProperty(RUN_STATUS);
			description.addProperty(COMMENTS);
			description.addProperty(RAN_DATE);
			description.addSelfLink();
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty(UUID);
			description.addProperty(DISPLAY);
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		}
		return null;
	}

	@Override
	public PageableResult doGetAll(RequestContext context) {
		return new NeedsPaging<>(getDatabaseChanges(), context);
	}

	@Override
	public DatabaseUpdater.OpenMRSChangeSet getByUniqueId(String uniqueId) {
		DatabaseUpdater.OpenMRSChangeSet databaseChange = getDatabaseChangeById(uniqueId);

		if (databaseChange == null) {
			throw new ObjectNotFoundException("Database change with id: " + uniqueId + " doesn't exist.");
		}

		return databaseChange;
	}

	@PropertyGetter(UUID)
	public static String getUuid(DatabaseUpdater.OpenMRSChangeSet instance) {
		return instance.getId();
	}

	@PropertyGetter(DISPLAY)
	public static String getDisplay(DatabaseUpdater.OpenMRSChangeSet instance) {
		return instance.getAuthor() + " " + instance.getDescription();
	}

	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation) {
			model
					.property(UUID, new StringProperty())
					.property(DISPLAY, new StringProperty())
					.property(AUTHOR, new StringProperty())
					.property(DESCRIPTION, new StringProperty())
					.property(RUN_STATUS, new EnumProperty(ChangeSet.RunStatus.class));
		} else if (rep instanceof FullRepresentation) {
			model
					.property(UUID, new StringProperty())
					.property(DISPLAY, new StringProperty())
					.property(AUTHOR, new StringProperty())
					.property(DESCRIPTION, new StringProperty())
					.property(RUN_STATUS, new EnumProperty(ChangeSet.RunStatus.class))
					.property(COMMENTS, new StringProperty())
					.property(RAN_DATE, new DateProperty());
		} else if (rep instanceof RefRepresentation) {
			model
					.property(UUID, new StringProperty())
					.property(DISPLAY, new StringProperty());
		}
		return model;
	}

	@Override
	public DatabaseUpdater.OpenMRSChangeSet newDelegate() {
		return null;
	}

	private List<DatabaseUpdater.OpenMRSChangeSet> getDatabaseChanges() {
		List<DatabaseUpdater.OpenMRSChangeSet> databaseChanges;
		try {
			databaseChanges = DatabaseUpdater.getDatabaseChanges();
		}
		catch (Exception e) {
			throw new GenericRestException("Exception while getting database changes", e);
		}
		return databaseChanges;
	}

	private DatabaseUpdater.OpenMRSChangeSet getDatabaseChangeById(String id) {
		return getDatabaseChanges().stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
	}
}


