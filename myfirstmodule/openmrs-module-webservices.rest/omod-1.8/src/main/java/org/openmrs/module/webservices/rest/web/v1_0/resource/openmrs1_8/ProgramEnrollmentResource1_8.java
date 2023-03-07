/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/programenrollment", supportedClass = PatientProgram.class, supportedOpenmrsVersions = { "1.8.* - 1.9.*" }, order = 1)
public class ProgramEnrollmentResource1_8 extends DataDelegatingCrudResource<PatientProgram> {
	
	@Override
	public PatientProgram getByUniqueId(String uniqueId) {
		return Context.getProgramWorkflowService().getPatientProgramByUuid(uniqueId);
	}
	
	@PropertyGetter("display")
	public String getDisplayString(PatientProgram patientProgram) {
		return patientProgram.getProgram().getName();
	}
	
	@Override
	protected void delete(PatientProgram delegate, String reason, RequestContext context) throws ResponseException {
		if (delegate.isVoided()) {
			// DELETE is idempotent, so we return success here
			return;
		}
		Context.getProgramWorkflowService().voidPatientProgram(delegate, reason);
	}
	
	@Override
	protected PatientProgram undelete(PatientProgram delegate, RequestContext context) throws ResponseException {
		if (delegate.isVoided()) {
			delegate = Context.getProgramWorkflowService().unvoidPatientProgram(delegate);
		}
		return delegate;
	}
	
	@Override
	public void purge(PatientProgram delegate, RequestContext context) throws ResponseException {
		Context.getProgramWorkflowService().purgePatientProgram(delegate);
	}
	
	@Override
	public PatientProgram newDelegate() {
		return new PatientProgram();
	}
	
	@Override
	public PatientProgram save(PatientProgram delegate) {
		return Context.getProgramWorkflowService().savePatientProgram(delegate);
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("patient", Representation.REF);
			description.addProperty("program", Representation.REF);
			description.addProperty("display");
			description.addProperty("dateEnrolled");
			description.addProperty("dateCompleted");
			description.addProperty("location", Representation.REF);
			description.addProperty("voided");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("patient");
			description.addProperty("program");
			description.addProperty("display");
			description.addProperty("dateEnrolled");
			description.addProperty("dateCompleted");
			description.addProperty("location");
			description.addProperty("voided");
			description.addSelfLink();
			description.addProperty("auditInfo");
			return description;
		} else {
			return null;
		}
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription d = new DelegatingResourceDescription();
		d.addRequiredProperty("patient");
		d.addRequiredProperty("program");
		d.addRequiredProperty("dateEnrolled");
		
		d.addProperty("dateCompleted");
		d.addProperty("location");
		d.addProperty("voided");
		return d;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model
			        .property("uuid", new StringProperty())
			        .property("display", new StringProperty())
			        .property("dateEnrolled", new DateProperty())
			        .property("dateCompleted", new DateProperty())
			        .property("voided", new BooleanProperty());
		}
		if (rep instanceof DefaultRepresentation) {
			model
			        .property("patient", new RefProperty("#/definitions/PatientGetRef"))
			        .property("program", new RefProperty("#/definitions/ProgramGetRef"))
			        .property("location", new RefProperty("#/definitions/LocationGetRef"));
		} else if (rep instanceof FullRepresentation) {
			model
			        .property("patient", new RefProperty("#/definitions/PatientGet"))
			        .property("program", new RefProperty("#/definitions/ProgramGet"))
			        .property("location", new RefProperty("#/definitions/LocationGet"));
		}
		return model;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl()
		        .property("patient", new StringProperty().example("uuid"))
		        .property("program", new StringProperty().example("uuid"))
		        .property("dateEnrolled", new DateProperty())
		        .property("dateCompleted", new DateProperty())
		        .property("location", new StringProperty().example("uuid"))
		        .property("voided", new BooleanProperty())
		        
		        .required("patient").required("program").required("dateEnrolled");
		if (rep instanceof FullRepresentation) {
			model
			        .property("patient", new RefProperty("#/definitions/PatientCreate"))
			        .property("program", new RefProperty("#/definitions/ProgramCreate"))
			        .property("location", new RefProperty("#/definitions/LocationCreate"));
		}
		return model;
	}
	
	@Override
	public Model getUPDATEModel(Representation rep) {
		return new ModelImpl()
		        .property("dateEnrolled", new DateProperty())
		        .property("dateCompleted", new DateProperty()); //FIXME missing props
		
	}
	
	/**
	 * Gets all the programs (excluding voided) of the given patient
	 * 
	 * @param context
	 * @return all programs of the given patient
	 */
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String patientUuid = context.getRequest().getParameter("patient");
		if (patientUuid != null) {
			PatientService patientService = Context.getPatientService();
			Patient patient = patientService.getPatientByUuid(patientUuid);
			if (patient == null) {
				return new EmptySearchResult();
			}
			
			List<PatientProgram> patientPrograms = Context.getProgramWorkflowService().getPatientPrograms(patient, null,
			    null, null, null, null, false);
			return new NeedsPaging<PatientProgram>(patientPrograms, context);
		}
		return super.doSearch(context);
	}
}
