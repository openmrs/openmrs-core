/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_11;

import java.util.ArrayList;
import java.util.List;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.Drug;
import org.openmrs.DrugIngredient;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * {@link Resource} for DrugIngredient, supporting standard CRUD operations
 */
@SubResource(parent = DrugResource1_11.class, path = "ingredient", supportedClass = DrugIngredient.class, supportedOpenmrsVersions = {
        "1.11.* - 9.*" })
public class DrugIngredientResource1_11 extends DelegatingSubResource<DrugIngredient, Drug, DrugResource1_11> {
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("display");
			description.addProperty("uuid");
			description.addProperty("ingredient", Representation.REF);
			description.addProperty("strength");
			description.addProperty("units", Representation.REF);
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("display");
			description.addProperty("uuid");
			description.addProperty("ingredient", Representation.REF);
			description.addProperty("strength");
			description.addProperty("units", Representation.REF);
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
		description.addProperty("ingredient");
		description.addProperty("strength");
		description.addProperty("units");
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getUpdatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getUpdatableProperties() {
		return getCreatableProperties();
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl modelImpl = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			modelImpl
			        .property("uuid", new StringProperty())
			        .property("display", new StringProperty())
			        .property("strength", new DoubleProperty());
		}
		if (rep instanceof DefaultRepresentation) {
			modelImpl
			        .property("ingredient", new RefProperty("#/definitions/ConceptGetRef"))
			        .property("units", new RefProperty("#/definitions/ConceptGetRef"));
		} else if (rep instanceof FullRepresentation) {
			modelImpl
			        .property("ingredient", new RefProperty("#/definitions/ConceptGet"))
			        .property("units", new RefProperty("#/definitions/ConceptGet"));
		}
		return modelImpl;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return new ModelImpl()
		        .property("ingredient", new StringProperty().example("uuid"))
		        .property("strength", new DoubleProperty())
		        .property("units", new StringProperty().example("uuid"))
		        
		        .required("ingredient");
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return getCREATEModel(rep);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource#getParent(java.lang.Object)
	 */
	@Override
	public Drug getParent(DrugIngredient instance) {
		return instance.getDrug();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource#setParent(java.lang.Object,
	 *      java.lang.Object)
	 */
	@Override
	public void setParent(DrugIngredient instance, Drug drug) {
		instance.setDrug(drug);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.api.SubResource#doGetAll(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public NeedsPaging<DrugIngredient> doGetAll(Drug parent, RequestContext context) throws ResponseException {
		List<DrugIngredient> ingredients = new ArrayList<DrugIngredient>();
		if (parent != null) {
			ingredients.addAll(parent.getIngredients());
		}
		return new NeedsPaging<DrugIngredient>(ingredients, context);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#purge(java.lang.Object,
	 *      org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public void purge(DrugIngredient ingredient, RequestContext context) throws ResponseException {
		Drug drug = ingredient.getDrug();
		drug.getIngredients().remove(ingredient);
		ingredient.setDrug(null);
		Context.getConceptService().saveDrug(drug);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#save(java.lang.Object)
	 */
	@Override
	public DrugIngredient save(DrugIngredient newIngredient) {
		// make sure that the ingredient has actually been added to the drug
		boolean needToAdd = true;
		for (DrugIngredient di : newIngredient.getDrug().getIngredients()) {
			if (di.equals(newIngredient)) {
				needToAdd = false;
				break;
			}
		}
		if (needToAdd) {
			newIngredient.getDrug().getIngredients().add(newIngredient);
		}
		
		Context.getConceptService().saveDrug(newIngredient.getDrug());
		return newIngredient;
	}
	
	/**
	 * Gets the display string for a concept name.
	 * 
	 * @param conceptName the concept name object.
	 * @return the display string.
	 */
	@PropertyGetter("display")
	public String getDisplayString(DrugIngredient ingredient) {
		return ingredient.getIngredient().getName().getName();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#newDelegate()
	 */
	@Override
	public DrugIngredient newDelegate() {
		return new DrugIngredient();
	}
	
	@Override
	public DrugIngredient getByUniqueId(String uniqueId) {
		return Context.getConceptService().getDrugIngredientByUuid(uniqueId);
	}
	
	@Override
	protected void delete(DrugIngredient delegate, String reason, RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException("Cannot void DrugIngredient. Use purge.");
	}
}
