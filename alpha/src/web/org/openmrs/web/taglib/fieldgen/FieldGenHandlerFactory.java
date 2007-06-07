package org.openmrs.web.taglib.fieldgen;

import java.util.Map;

public class FieldGenHandlerFactory {
	
	private Map<String,String> handlers;
	
	private static FieldGenHandlerFactory singleton;
	
	public FieldGenHandlerFactory() {
		singleton = this;
	}
	
	public static FieldGenHandlerFactory getSingletonInstance() {
		if (singleton == null)
			throw new RuntimeException("Not Yet Instantiated");
		else
			return singleton;
	}

	/**
	 * @return Returns the handlers.
	 */
	public Map<String,String> getHandlers() {
		return handlers;
	}

	/**
	 * @param handlers The handlers to set.
	 */
	public void setHandlers(Map<String,String> handlers) {
		this.handlers = handlers;
	}
	
	public String getHandlerByClassName(String className) {
		if ( className != null ) {
			if ( handlers.containsKey(className) ) {
				return handlers.get(className);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
}
