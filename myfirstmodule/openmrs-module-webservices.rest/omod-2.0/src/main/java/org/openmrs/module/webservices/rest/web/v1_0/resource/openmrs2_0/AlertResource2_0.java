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
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.notification.Alert;
import org.openmrs.notification.AlertRecipient;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link Resource} for {@link Alert}, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/alert", supportedClass = Alert.class, supportedOpenmrsVersions = { "2.0.* - 9.*" })
public class AlertResource2_0 extends DelegatingCrudResource<Alert> {

	private static final String ALERT_ID = "alertId";

	private static final String TEXT = "text";

	private static final String DATE_TO_EXPIRE = "dateToExpire";

	private static final String SATISFIED_BY_ANY = "satisfiedByAny";

	private static final String ALERT_READ = "alertRead";

	private static final String CREATOR = "creator";

	private static final String DATE_CREATED = "dateCreated";

	private static final String CHANGED_BY = "changedBy";

	private static final String DATE_CHANGED = "dateChanged";

	private static final String RECIPIENTS = "recipients";

	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("uuid");
		description.addProperty("display");
		description.addProperty(ALERT_ID);
		description.addSelfLink();

		if (rep instanceof DefaultRepresentation) {
			description.addProperty(TEXT);
			description.addProperty(SATISFIED_BY_ANY);
			description.addProperty(DATE_TO_EXPIRE);
			description.addProperty(ALERT_READ);
			description.addProperty(RECIPIENTS, Representation.REF);
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
		} else if (rep instanceof FullRepresentation) {
			description.addProperty(TEXT);
			description.addProperty(SATISFIED_BY_ANY);
			description.addProperty(ALERT_READ);
			description.addProperty(DATE_TO_EXPIRE);
			description.addProperty(CREATOR, Representation.DEFAULT);
			description.addProperty(DATE_CREATED);
			description.addProperty(CHANGED_BY, Representation.DEFAULT);
			description.addProperty(DATE_CHANGED);
			description.addProperty(RECIPIENTS, Representation.DEFAULT);
		}

		return description;
	}

	@PropertyGetter("display")
	public String getDisplayProperty(Alert alert) {
		return alert.getText();
	}

	@PropertySetter(RECIPIENTS)
	public static void setRecipientsProperty(Alert alert, Set<AlertRecipient> newRecipients)
			throws ResourceDoesNotSupportOperationException {

		if (newRecipients == null || newRecipients.isEmpty()) {
			throw new ResourceDoesNotSupportOperationException("At least one recipient is required");
		}

		Set<AlertRecipient> oldRecipients = alert.getRecipients();

		// build newRecipientsUuids
		Set<String> newRecipientsUuids = new HashSet<>();
		for (AlertRecipient newRecipient : newRecipients) {
			newRecipientsUuids.add(newRecipient.getRecipient().getUuid());
		}

		// find deleted recipients between old and new set
		Set<AlertRecipient> recipientsToDelete = new HashSet<>();
		for (AlertRecipient oldRecipient : oldRecipients) {
			String userUuid = oldRecipient.getRecipient().getUuid();
			if (!newRecipientsUuids.contains(userUuid)) {
				recipientsToDelete.add(oldRecipient);
			} else {
				newRecipientsUuids.remove(userUuid);
			}
		}

		for (AlertRecipient recipientToDelete : recipientsToDelete) {
			alert.removeRecipient(recipientToDelete);
		}

		// find new newRecipients between old and new set
		for (AlertRecipient newRecipient : newRecipients) {
			String userUuid = newRecipient.getRecipient().getUuid();
			if (newRecipientsUuids.contains(userUuid)) {
				alert.addRecipient(newRecipient);
			}
		}
	}

	@Override
	public List<String> getPropertiesToExposeAsSubResources() {
		return Collections.singletonList(RECIPIENTS);
	}

	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl modelImpl = ((ModelImpl) super.getGETModel(rep))
				.property("uuid", new StringProperty())
				.property("display", new StringProperty())
				.property(ALERT_ID, new IntegerProperty());

		if (rep instanceof DefaultRepresentation) {
			modelImpl
					.property(TEXT, new StringProperty())
					.property(SATISFIED_BY_ANY, new BooleanProperty())
					.property(ALERT_READ, new BooleanProperty())
					.property(DATE_TO_EXPIRE, new DateProperty())
					.property(RECIPIENTS, new ArrayProperty(new RefProperty("#/definitions/AlertRecipientGetRef")));
		}
		if (rep instanceof FullRepresentation) {
			modelImpl
					.property(TEXT, new StringProperty())
					.property(SATISFIED_BY_ANY, new BooleanProperty())
					.property(ALERT_READ, new BooleanProperty())
					.property(DATE_TO_EXPIRE, new DateProperty())
					.property(CREATOR, new ObjectProperty())
					.property(DATE_CREATED, new DateTimeProperty())
					.property(CHANGED_BY, new ObjectProperty())
					.property(DATE_CHANGED, new DateTimeProperty())
					.property(RECIPIENTS, new ArrayProperty(new RefProperty("#/definitions/AlertRecipientGetRef")));
		}
		return modelImpl;
	}

	@Override
	public Model getCREATEModel(Representation rep) {
		return new ModelImpl()
				.property(TEXT, new StringProperty())
				.property(RECIPIENTS, new ArrayProperty(new RefProperty("#/definitions/AlertRecipientCreate")))
				.property(SATISFIED_BY_ANY, new BooleanProperty())
				.property(DATE_TO_EXPIRE, new DateProperty());
	}

	@Override
	public Model getUPDATEModel(Representation rep) {
		return new ModelImpl()
				.property(TEXT, new StringProperty())
				.property(RECIPIENTS, new ArrayProperty(new RefProperty("#/definitions/AlertRecipientCreate")))
				.property(SATISFIED_BY_ANY, new BooleanProperty())
				.property(DATE_TO_EXPIRE, new DateProperty());
	}

	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty(TEXT);
		description.addRequiredProperty(RECIPIENTS);
		description.addProperty(SATISFIED_BY_ANY);
		description.addProperty(DATE_TO_EXPIRE);
		return description;
	}

	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		return getCreatableProperties();
	}

	@Override
	public Alert newDelegate() {
		Alert alert = new Alert();
		alert.setRecipients(new HashSet<>()); // to prevent NullPointerException
		return alert;
	}

	@Override
	public Alert getByUniqueId(String uniqueId) {
		boolean includeRetired = true;
		List<Alert> alerts = Context.getAlertService().getAllAlerts(includeRetired);
		for (Alert alert : alerts) {
			if (alert.getUuid().equals(uniqueId)) {
				return alert;
			}
		}
		return null;
	}

	@Override
	public Alert save(Alert alert) {
		return Context.getAlertService().saveAlert(alert);
	}

	@Override
	public void purge(Alert alert, RequestContext context) throws ResponseException {
		if (alert == null) {
			return;
		}

		Context.getAlertService().purgeAlert(alert);
	}

	@Override
	protected void delete(Alert alert, String reason, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}

	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		List<Alert> alerts = Context.getAlertService().getAllAlerts();
		return new NeedsPaging<>(alerts, context);
	}

	@Override
	protected PageableResult doSearch(RequestContext context) {
		// include expired
		String includeExpiredStr = context.getRequest().getParameter("includeExpired");
		if (includeExpiredStr != null) {
			boolean includeExpired = Boolean.parseBoolean(includeExpiredStr);
			return new NeedsPaging<>(Context.getAlertService().getAllAlerts(includeExpired), context);
		}

		return new NeedsPaging<>(Context.getAlertService().getAllAlerts(), context);
	}
}
