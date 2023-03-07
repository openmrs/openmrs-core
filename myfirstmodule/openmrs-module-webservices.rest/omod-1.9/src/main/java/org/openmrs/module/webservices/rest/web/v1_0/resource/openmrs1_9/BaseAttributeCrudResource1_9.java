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

import java.util.Arrays;
import java.util.List;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.StringProperty;
import org.apache.commons.lang.StringUtils;
import org.openmrs.attribute.Attribute;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.customdatatype.NotYetPersistedException;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource;

/**
 * Subclass of {@link DelegatingSubResource} with helper methods specific to {@link Attribute}
 * 
 * @param <T> The type of the attribute this sub resource is associated to
 * @param <P> The parent/owning type for the type T
 * @param <PR> The Resource for the parent/owning type P
 */
public abstract class BaseAttributeCrudResource1_9<T extends Attribute<?, ?>, P, PR> extends DelegatingSubResource<T, P, DelegatingCrudResource<P>> {
	
	/**
	 * Sets value on the given attribute.
	 * 
	 * @param instance
	 * @param value
	 */
	@PropertySetter("value")
	public static void setValue(Attribute<?, ?> instance, String value) throws Exception {
		CustomDatatype<?> datatype = CustomDatatypeUtil.getDatatype(instance.getAttributeType().getDatatypeClassname(),
		    instance.getAttributeType().getDatatypeConfig());
		if (StringUtils.isNotEmpty(value)) // check empty instead of blank, because " " is meaningful
			instance.setValue(datatype.fromReferenceString(value));
	}
	
	/**
	 * Gets an attribute value, catching any {@link NotYetPersistedException} and returning null in
	 * that case
	 * 
	 * @param instance
	 * @return
	 */
	@PropertyGetter("value")
	public static Object getValue(Attribute<?, ?> instance) {
		try {
			return instance.getValue();
		}
		catch (NotYetPersistedException ex) {
			return null;
		}
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getRepresentationDescription(org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("display");
			description.addProperty("uuid");
			description.addProperty("attributeType", Representation.REF);
			description.addProperty("value");
			description.addProperty("voided");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("display");
			description.addProperty("uuid");
			description.addProperty("attributeType", Representation.REF);
			description.addProperty("value");
			description.addProperty("voided");
			description.addProperty("auditInfo");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("attributeType");
		description.addRequiredProperty("value");
		return description;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return new ModelImpl()
		        .property("attributeType", new StringProperty().example("uuid"))
		        .property("value", new StringProperty())
		        
		        .required("attributeType").required("value");
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model
			        .property("display", new StringProperty())
			        .property("uuid", new StringProperty())
			        .property("attributeType", new StringProperty()) //FIXME type
			        .property("value", new StringProperty()) //FIXME type
			        .property("voided", new BooleanProperty());
		}
		return model;
	}
	
	/**
	 * Gets the display string for an attribute.
	 * 
	 * @param attr the attribute.
	 * @return attribute type: value (for concise display purposes)
	 */
	@PropertyGetter("display")
	public String getDisplayString(T attr) {
		if (attr.getAttributeType() == null)
			return "";
		return attr.getAttributeType().getName() + ": " + attr.getValue();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getResourceVersion()
	 */
	@Override
	public String getResourceVersion() {
		return "1.9";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getPropertiesToExposeAsSubResources()
	 */
	@Override
	public List<String> getPropertiesToExposeAsSubResources() {
		return Arrays.asList("attributes");
	}
}
