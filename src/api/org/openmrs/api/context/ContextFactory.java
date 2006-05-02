package org.openmrs.api.context;

/**
 * Factory for obtaining an OpenMRS <code>context</code>.
 * 
 * @see Context
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public class ContextFactory {

	public static Context getContext() {
		return createContext();
	}

	private static Context createContext() {
		// Resource res = new ClassPathResource("openmrs-servlet.xml");
		// XmlBeanFactory factory = new XmlBeanFactory(res);

		// context = (Context) factory.getBean("defaultContext");
		return new Context();
	}
}
