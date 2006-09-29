package org.openmrs.api.db.hibernate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.util.ConfigHelper;
import org.openmrs.api.context.Context;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

public class HibernateSessionFactoryBean extends LocalSessionFactoryBean {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	public SessionFactory newSessionFactory(Configuration config) throws HibernateException {
		log.debug("Configuring hibernate sessionFactory properties");
		
		Properties properties = Context.getRuntimeProperties();
		
		// loop over runtime properties and override each in the configuration
		for (Object key : properties.keySet()) {
			String prop = (String)key;
			String value = (String)properties.get(key);
			log.debug("Setting property: " + prop + ":" + value);
			config.setProperty(prop, value);
			if (!prop.startsWith("hibernate"))
				config.setProperty("hibernate." + prop, value);
		}
		
		// load in the default hibernate properties
		try {
			InputStream propertyStream = ConfigHelper.getResourceAsStream("/hibernate.default.properties");
			Properties props = new Properties();
			props.load(propertyStream);
			propertyStream.close();
	
			// Only load in the default properties if they don't exist
			config.mergeProperties(props);
		}
		catch (IOException e) {
			log.fatal("Unable to load default hibernate properties", e);
		}
		
		return config.buildSessionFactory();
		
	}
	
}
