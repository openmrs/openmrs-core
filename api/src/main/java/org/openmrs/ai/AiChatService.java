/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.ai;

import java.util.List;

import org.openmrs.Patient;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.util.PrivilegeConstants;

/**
 * Provider-agnostic interface for AI-powered clinical chart queries.
 * <p>
 * This interface defines the contract that AI provider modules must implement to enable
 * natural language querying of patient clinical data. Implementations may connect to
 * external AI services (e.g., OpenAI, Anthropic), local language models, or custom
 * clinical NLP systems.
 * <p>
 * OpenMRS core defines only this interface. Concrete implementations are provided by
 * separate AI provider modules, following the standard OpenMRS module pattern.
 *
 * @since 3.0.0
 */
public interface AiChatService extends OpenmrsService {
	
	/**
	 * Queries the patient's clinical chart using natural language and returns an AI-generated
	 * response.
	 *
	 * @param patient the patient whose chart to query
	 * @param question the natural language question from the clinician
	 * @return the AI-generated response with source references
	 * @throws AiServiceUnavailableException if no AI provider module is registered
	 * @throws APIException if an error occurs while processing the query
	 * <strong>Should</strong> return a response for a valid patient and question
	 * <strong>Should</strong> throw AiServiceUnavailableException if no provider is available
	 */
	@Authorized({ PrivilegeConstants.QUERY_PATIENT_CHART_AI })
	AiChatResponse queryPatientChart(Patient patient, String question) throws APIException;
	
	/**
	 * Queries the patient's clinical chart with conversation history for multi-turn interactions.
	 *
	 * @param patient the patient whose chart to query
	 * @param question the natural language question from the clinician
	 * @param conversationHistory previous messages in this conversation for context
	 * @return the AI-generated response with source references
	 * @throws AiServiceUnavailableException if no AI provider module is registered
	 * @throws APIException if an error occurs while processing the query
	 * <strong>Should</strong> use conversation history for context in the response
	 */
	@Authorized({ PrivilegeConstants.QUERY_PATIENT_CHART_AI })
	AiChatResponse queryPatientChart(Patient patient, String question, List<AiChatMessage> conversationHistory)
	        throws APIException;
	
	/**
	 * Checks whether an AI service provider is currently available and configured.
	 *
	 * @return true if an AI provider module is registered and ready to handle queries
	 */
	boolean isAvailable();
}
