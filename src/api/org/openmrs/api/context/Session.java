package org.openmrs.context;

public interface Session {

	public Object getAttribute(String name);

	public void removeAttribute(String name);

	public void setAttribute(String name, Object value);
}
