package org.openmrs.api.context;

import org.openmrs.User;

/**
 * Factory for obtaining an OpenMRS <code>context</code>.
 * 
 * @see Context
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public class ContextFactory {
	
	// a context that can never be authenticated
	private final static Context singletonAnonymousContext = new Context() {
			public void authenticate(String username, String password) { }
			public User getAuthenticatedUser() { return null; }
			public boolean isAuthenticated() { return false; }
		};

	public static Context getContext() {
		return createContext();
	}

	private static Context createContext() {
		// Resource res = new ClassPathResource("openmrs-servlet.xml");
		// XmlBeanFactory factory = new XmlBeanFactory(res);

		// context = (Context) factory.getBean("defaultContext");
		return new Context();
	}
		
	public static Context getAnonymousContext() {
		return singletonAnonymousContext;
	}
	
}
