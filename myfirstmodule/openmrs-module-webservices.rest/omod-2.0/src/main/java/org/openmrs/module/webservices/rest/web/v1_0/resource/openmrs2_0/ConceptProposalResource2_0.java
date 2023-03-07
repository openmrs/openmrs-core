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

import org.openmrs.Concept;
import org.openmrs.ConceptProposal;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.DuplicateConceptNameException;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.IllegalRequestException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.notification.Alert;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link Resource} for {@link ConceptProposal}, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/conceptproposal", supportedClass = ConceptProposal.class, supportedOpenmrsVersions = {
		"2.0.* - 9.*" })
public class ConceptProposalResource2_0 extends DelegatingCrudResource<ConceptProposal> {

	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("encounter", Representation.REF);
			description.addProperty("originalText");
			description.addProperty("finalText");
			description.addProperty("state");
			description.addProperty("comments");
			description.addProperty("occurrences");
			description.addProperty("creator", Representation.REF);
			description.addProperty("dateCreated");
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("encounter", Representation.DEFAULT);
			description.addProperty("obsConcept", Representation.DEFAULT);
			description.addProperty("obs", Representation.DEFAULT);
			description.addProperty("mappedConcept", Representation.DEFAULT);
			description.addProperty("originalText");
			description.addProperty("finalText");
			description.addProperty("state");
			description.addProperty("comments");
			description.addProperty("occurrences");
			description.addProperty("creator", Representation.DEFAULT);
			description.addProperty("dateCreated");
			description.addProperty("changedBy", Representation.DEFAULT);
			description.addProperty("dateChanged");
			description.addSelfLink();
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("encounter", Representation.REF);
			description.addProperty("originalText");
			description.addProperty("state");
			description.addProperty("occurrences");
			description.addProperty("creator", Representation.REF);
			description.addProperty("dateCreated");
			description.addSelfLink();
			return description;
		}
		return null;
	}

	@PropertyGetter("occurrences")
	public Integer getOccurrencesProperty(ConceptProposal proposal) {
		Map<String, List<ConceptProposal>> proposalsMap = getProposalsMapByOriginalText(false);
		return proposalsMap.get(proposal.getOriginalText()).size();
	}

	@PropertyGetter("display")
	public String getDisplayProperty(ConceptProposal proposal) {
		return proposal.toString();
	}

	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("originalText");
		description.addProperty("mappedConcept");
		description.addProperty("encounter");
		description.addProperty("obsConcept");
		return description;
	}

	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("finalText");
		description.addProperty("encounter");
		description.addProperty("obsConcept");
		description.addProperty("mappedConcept");
		description.addProperty("comments");
		return description;
	}

	@Override
	public ConceptProposal getByUniqueId(String uniqueId) {
		return Context.getConceptService().getConceptProposalByUuid(uniqueId);
	}

	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<>(
				getAllProposalsWithUniqueOriginalTexts(Boolean.valueOf(context.getParameter("includeCompleted"))), context);
	}

	@Override
	protected PageableResult doSearch(RequestContext context) {
		boolean includeCompleted = Boolean.parseBoolean(context.getParameter("includeCompleted"));
		String sortOrder = context.getParameter("sortOrder");
		String sortOn = context.getParameter("sortOn");

		List<ConceptProposal> sortedProposals = getAllProposalsWithUniqueOriginalTexts(includeCompleted);

		if (sortOn != null) {
			if (sortOn.equals("originalText")) {
				sortedProposals.sort(Comparator.comparing(ConceptProposal::getOriginalText));
				if (sortOrder != null && sortOrder.equals("desc")) {
					Collections.reverse(sortedProposals);
				}
			} else if (sortOn.equals("occurrences")) {
				Map<String, List<ConceptProposal>> proposalsMap = getProposalsMapByOriginalText(includeCompleted);
				sortedProposals = proposalsMap
						.entrySet()
						.stream()
						.sorted(Comparator.comparingInt(list -> list.getValue().size()))
						.map(entry -> entry.getValue().get(0))
						.collect(Collectors.toList());
				if (sortOrder != null && sortOrder.equals("desc")) {
					Collections.reverse(sortedProposals);
				}
			}
		}

		return new NeedsPaging<>(sortedProposals, context);
	}

	@Override
	protected void delete(ConceptProposal delegate, String reason, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}

	@Override
	public void purge(ConceptProposal delegate, RequestContext context) throws ResponseException {
		Context.getConceptService().purgeConceptProposal(delegate);
	}

	@Override
	public ConceptProposal newDelegate() {
		return new ConceptProposal();
	}

	@Override
	public ConceptProposal save(ConceptProposal delegate) {
		return Context.getConceptService().saveConceptProposal(delegate);
	}

	@Override
	public Object update(String uuid, SimpleObject requestBody, RequestContext context) throws ResponseException {
		ConceptService service = Context.getConceptService();
		ConceptProposal proposal = service.getConceptProposalByUuid(uuid);

		String action = requestBody.get("action");
		if (action == null) {
			return super.update(uuid, requestBody, context);
		}

		if (action.equals("ignore")) {
			ignoreProposal(proposal, service);
		} else if (action.equals("convert")) {
			convertProposal(proposal, service, requestBody);
		}

		return null;
	}

	private void ignoreProposal(ConceptProposal proposal, ConceptService service) {
		List<ConceptProposal> proposalsWithSameOriginalText = service.getConceptProposals(proposal.getOriginalText());
		Set<User> uniqueProposers = new HashSet<>();

		// ignore (reject) proposals
		for (ConceptProposal conceptProposal : proposalsWithSameOriginalText) {
			conceptProposal.rejectConceptProposal();
			conceptProposal.setComments(proposal.getComments());

			uniqueProposers.add(conceptProposal.getCreator());

			service.saveConceptProposal(conceptProposal);
		}

		createAlert(
				"ConceptProposal.alert.ignored",
				new String[] { proposal.getOriginalText(), proposal.getComments() },
				Context.getLocale(),
				uniqueProposers);
	}

	private void convertProposal(ConceptProposal proposal, ConceptService service, SimpleObject requestBody) {
		// get mapped concept
		Concept mappedConcept = proposal.getMappedConcept();
		if (mappedConcept == null) {
			throw new IllegalRequestException("Mapped concept not set");
		}

		if (!StringUtils.hasText(proposal.getFinalText())) {
			proposal.setFinalText(proposal.getOriginalText());
		}

		// set proposal to appropriate state
		String proposalAction = requestBody.get("actionToTakeOnConvert");
		if (proposalAction.equals("saveAsMapped")) {
			proposal.setState(OpenmrsConstants.CONCEPT_PROPOSAL_CONCEPT);
		} else if (proposalAction.equals("saveAsSynonym")) {
			proposal.setState(OpenmrsConstants.CONCEPT_PROPOSAL_SYNONYM);
		}

		String conceptNameLocaleStr = requestBody.get("conceptNameLocale");
		if (conceptNameLocaleStr == null) {
			throw new IllegalRequestException("Concept name locale not set.");
		}
		Locale conceptNameLocale = LocaleUtility.fromSpecification(conceptNameLocaleStr);

		// map all proposals with same original name
		Set<User> uniqueProposers = new HashSet<>();
		List<ConceptProposal> proposalsWithSameOriginalText = service.getConceptProposals(proposal.getOriginalText());
		proposalsWithSameOriginalText.add(proposal);
		for (ConceptProposal conceptProposal : proposalsWithSameOriginalText) {
			// the obs concept differs, skip this proposal
			if (conceptProposal.getObsConcept() != null && !conceptProposal.getObsConcept().equals(proposal.getObsConcept())) {
				continue;
			}
			uniqueProposers.add(conceptProposal.getCreator());
			try {
				service.mapConceptProposalToConcept(conceptProposal, mappedConcept, conceptNameLocale);
			}
			catch (DuplicateConceptNameException e) {
				throw new IllegalRequestException("Duplicate concept name found", e);
			}
		}

		Locale locale = Context.getLocale();
		String mappedName = mappedConcept.getName(locale).getName();
		if (proposal.getState().equals(OpenmrsConstants.CONCEPT_PROPOSAL_SYNONYM)) {
			createAlert(
					"ConceptProposal.alert.synonymAdded",
					new String[] { proposal.getFinalText(), mappedName, proposal.getComments() },
					locale,
					uniqueProposers);
		} else {
			createAlert(
					"ConceptProposal.alert.mappedTo",
					new String[] { proposal.getFinalText(), mappedName, proposal.getComments() },
					locale,
					uniqueProposers);
		}
	}

	private void createAlert(String messageCode, Object[] args, Locale locale, Collection<User> recipients) {
		try {
			// allow this user to create alerts temporarily
			Context.addProxyPrivilege(PrivilegeConstants.MANAGE_ALERTS);
			String message = Context.getMessageSourceService().getMessage(messageCode, args, locale);
			Context.getAlertService().saveAlert(new Alert(message, recipients));
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_ALERTS);
		}
	}

	private List<ConceptProposal> getAllProposalsWithUniqueOriginalTexts(Boolean includeCompleted) {
		Map<String, List<ConceptProposal>> proposalsMap = getProposalsMapByOriginalText(includeCompleted);
		List<ConceptProposal> proposalsWithUniqueOriginalTexts = new ArrayList<>();

		proposalsMap.forEach((originalText, proposals) -> proposalsWithUniqueOriginalTexts.add(proposals.get(0)));

		return proposalsWithUniqueOriginalTexts;
	}

	private Map<String, List<ConceptProposal>> getProposalsMapByOriginalText(Boolean includeCompleted) {
		List<ConceptProposal> proposals = Context.getConceptService().getAllConceptProposals(includeCompleted);
		Map<String, List<ConceptProposal>> map = new HashMap<>();

		for (ConceptProposal proposal : proposals) {
			String originalText = proposal.getOriginalText();
			if (map.containsKey(originalText)) {
				map.get(originalText).add(proposal);
			} else {
				List<ConceptProposal> list = new ArrayList<>();
				list.add(proposal);
				map.put(originalText, list);
			}
		}

		return map;
	}

}
