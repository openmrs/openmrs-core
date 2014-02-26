package org.openmrs.api.handler;

import org.openmrs.api.context.Context;

/**
 * This base class should be used by specific implementations of {@link EncounterVisitHandler}.
 * <p>
 * It delivers a default implementation for {@link EncounterVisitHandler#getDisplayName()}.
 * 
 * @since 1.9
 */
public abstract class BaseEncounterVisitHandler implements EncounterVisitHandler {
	
	/**
	 * Delegates to {@link EncounterVisitHandler#getDisplayName(java.util.Locale)} with
	 * {@link Context#getLocale()} as a parameter.
	 * 
	 * @return a displayable string so that users can pick between different assignment handlers
	 */
	@Override
	public String getDisplayName() {
		return getDisplayName(Context.getLocale());
	}
	
}
