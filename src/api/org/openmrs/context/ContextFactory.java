package org.openmrs.context;

import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Factory for obtaining an OpenMRS <code>context</code>.
 * 
 * @see Context
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public class ContextFactory {

	private static Context context = null;

	public static Context getContext() {
		if (context == null)
			createContext();
		return context;
	}

	private static void createContext() {
		//Resource res = new ClassPathResource("openmrs-servlet.xml");
		//XmlBeanFactory factory = new XmlBeanFactory(res);

		//context = (Context) factory.getBean("defaultContext");
		context = (Context) new HibernateContext();
	}
}
