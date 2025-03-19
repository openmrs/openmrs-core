/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.context;

import org.openmrs.notification.MessagePreparator;
import org.openmrs.notification.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

	private final MessageSender messageSender;
	private final MessagePreparator messagePreparator;

	@Autowired
	public MessageService(MessageSender messageSender, MessagePreparator messagePreparator) {
		this.messageSender = messageSender;
		this.messagePreparator = messagePreparator;
	}

	public MessageSender getMessageSender() {
		return messageSender;
	}

	public MessagePreparator getMessagePreparator() {
		return messagePreparator;
	}
}

