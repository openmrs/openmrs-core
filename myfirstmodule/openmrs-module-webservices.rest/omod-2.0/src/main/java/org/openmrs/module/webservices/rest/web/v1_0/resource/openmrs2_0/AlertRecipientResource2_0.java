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
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.apache.commons.lang.StringUtils;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.notification.Alert;
import org.openmrs.notification.AlertRecipient;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub-resource for {@link AlertRecipient}
 */
@SubResource(parent = AlertResource2_0.class, path = "recipient", supportedClass = AlertRecipient.class, supportedOpenmrsVersions = {
		"2.0.* - 9.*" })
public class AlertRecipientResource2_0 extends DelegatingSubResource<AlertRecipient, Alert, AlertResource2_0> {

	private static final String UUID = "uuid";

	private static final String DISPLAY = "display";

	private static final String RECIPIENT = "recipient";

	private static final String ALERT_READ = "alertRead";

	private static final String DATE_CHANGED = "dateChanged";

	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty(UUID);
		description.addProperty(DISPLAY);
		description.addSelfLink();

		if (rep instanceof DefaultRepresentation) {
			description.addProperty(RECIPIENT, Representation.REF);
			description.addProperty(ALERT_READ);
			description.addProperty(DATE_CHANGED);
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
		} else if (rep instanceof FullRepresentation) {
			description.addProperty(RECIPIENT, Representation.DEFAULT);
			description.addProperty(ALERT_READ);
			description.addProperty(DATE_CHANGED);
		}

		return description;
	}

	@PropertyGetter(DISPLAY)
	public String getDisplayProperty(AlertRecipient instance) {
		return instance.getRecipient().getDisplayString();
	}

	@PropertySetter(RECIPIENT)
	public void setRecipientProperty(AlertRecipient instance, User recipient) {
		String recipientUuid = recipient.getUuid();
		if (!StringUtils.isEmpty(recipientUuid)) {
			User newRecipient = Context.getUserService().getUserByUuid(recipientUuid);
			if (newRecipient != null) {
				instance.setRecipient(newRecipient);
			}
		}
	}

	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty(RECIPIENT);
		return description;
	}

	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl modelImpl = ((ModelImpl) super.getGETModel(rep))
				.property(UUID, new StringProperty())
				.property(DISPLAY, new StringProperty());

		if (rep instanceof DefaultRepresentation) {
			modelImpl
					.property(RECIPIENT, new RefProperty("#/definitions/UserGetRef"))
					.property(ALERT_READ, new BooleanProperty())
					.property(DATE_CHANGED, new DateTimeProperty());
		}
		if (rep instanceof FullRepresentation) {
			modelImpl
					.property(RECIPIENT, new RefProperty("#/definitions/UserGet"))
					.property(ALERT_READ, new BooleanProperty())
					.property(DATE_CHANGED, new DateTimeProperty());
		}
		return modelImpl;
	}

	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl modelImpl = new ModelImpl()
				.property(RECIPIENT, new StringProperty().example("uuid"));

		if (rep instanceof FullRepresentation) {
			modelImpl
					.property(RECIPIENT, new RefProperty("#/definitions/UserCreate"));
		}
		return modelImpl;
	}

	@Override
	public Model getUPDATEModel(Representation rep) {
		return getCREATEModel(rep);
	}

	@Override
	public AlertRecipient newDelegate() {
		return new AlertRecipient();
	}

	@Override
	public Alert getParent(AlertRecipient instance) {
		return instance.getAlert();
	}

	@Override
	public void setParent(AlertRecipient instance, Alert alert) {
		instance.setAlert(alert);
	}

	@Override
	public AlertRecipient getByUniqueId(String uniqueId) {
		List<Alert> alerts = Context.getAlertService().getAllAlerts();

		for (Alert alert : alerts) {
			for (AlertRecipient recipient : alert.getRecipients()) {
				if (recipient.getUuid().equals(uniqueId)) {
					return recipient;
				}
			}
		}

		return null;
	}

	@Override
	public AlertRecipient save(AlertRecipient delegate) {
		delegate.getAlert().addRecipient(delegate);
		Context.getAlertService().saveAlert(delegate.getAlert());

		return delegate;
	}

	@Override
	protected void delete(AlertRecipient delegate, String reason, RequestContext context) throws ResponseException {
		purge(delegate, context);
	}

	@Override
	public void purge(AlertRecipient delegate, RequestContext context) throws ResponseException {
		delegate.getAlert().removeRecipient(delegate);
		Context.getAlertService().saveAlert(delegate.getAlert());
	}

	@Override
	public PageableResult doGetAll(Alert parent, RequestContext context) throws ResponseException {
		List<AlertRecipient> recipients = new ArrayList<>(parent.getRecipients());
		return new NeedsPaging<>(recipients, context);
	}
}
