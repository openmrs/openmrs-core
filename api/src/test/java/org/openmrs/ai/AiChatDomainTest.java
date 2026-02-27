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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Arrays;
import java.util.Date;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for AI chat domain objects.
 */
public class AiChatDomainTest {
	
	@Test
	public void aiChatMessage_shouldCreateWithRoleAndContent() {
		AiChatMessage message = new AiChatMessage(AiChatMessage.Role.USER, "What are this patient's risks?");
		
		assertThat(message.getRole(), is(AiChatMessage.Role.USER));
		assertThat(message.getContent(), is("What are this patient's risks?"));
		assertThat(message.getTimestamp(), is(notNullValue()));
	}
	
	@Test
	public void aiChatResponse_shouldCreateWithContent() {
		AiChatResponse response = new AiChatResponse("The patient has a history of diabetes.");
		response.setSourceReferences(Arrays.asList("obs-uuid-1", "condition-uuid-2"));
		
		assertThat(response.getContent(), is("The patient has a history of diabetes."));
		assertThat(response.getSourceReferences().size(), is(2));
		assertThat(response.getTimestamp(), is(notNullValue()));
	}
	
	@Test
	public void aiChatMessage_shouldSupportAllRoles() {
		AiChatMessage userMsg = new AiChatMessage(AiChatMessage.Role.USER, "question");
		AiChatMessage assistantMsg = new AiChatMessage(AiChatMessage.Role.ASSISTANT, "answer");
		AiChatMessage systemMsg = new AiChatMessage(AiChatMessage.Role.SYSTEM, "context");
		
		assertThat(userMsg.getRole(), is(AiChatMessage.Role.USER));
		assertThat(assistantMsg.getRole(), is(AiChatMessage.Role.ASSISTANT));
		assertThat(systemMsg.getRole(), is(AiChatMessage.Role.SYSTEM));
	}
	
	@Test
	public void aiServiceUnavailableException_shouldContainMessage() {
		AiServiceUnavailableException exception = new AiServiceUnavailableException("Custom message");
		assertThat(exception.getMessage(), is("Custom message"));
	}
	
	@Test
	public void aiServiceUnavailableException_shouldHaveDefaultMessage() {
		AiServiceUnavailableException exception = new AiServiceUnavailableException();
		assertThat(exception.getMessage(), is(notNullValue()));
	}
}
