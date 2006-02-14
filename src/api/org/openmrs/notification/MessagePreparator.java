package org.openmrs.notification;

import java.util.Map;
import org.openmrs.domain.Message;
import org.openmrs.domain.Template;


public interface MessagePreparator { 
	public Message prepare(Template template);

} 