package org.openmrs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;

public class BaseTest extends org.springframework.test.AbstractTransactionalSpringContextTests {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	protected String[] getConfigLocations() {
	      return new String[] {
	    		  "applicationContext-service.xml"
	      };
	   }

	/**
	 * This method is called before Spring is setup, so its used to set the runtime
	 * properties on Context.
	 * 
	 * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#contextKey()
	 */
	@Override
	protected Object contextKey() {
		Properties props = getRuntimeProperties();
		log.debug("props: " + props);
		Context.setRuntimeProperties(props);
		
		return super.contextKey();
	}

	public void startup() {
		Context.startup(getRuntimeProperties());
	}
	
	public void shutdown() {
		Context.shutdown();
	}

	/**
	 * used for runtime properties
	 * 
	 * @return
	 */
	public String getWebappName() {
		return "amrs";
	}
	
	/**
	 * Mimics org.openmrs.web.Listinger.getRuntimeProperties()
	 * 
	 * @return Properties
	 */
	public Properties getRuntimeProperties() {
		Properties props = new Properties();
		
		try {
			FileInputStream propertyStream = null;

			// Look for environment variable {WEBAPP.NAME}_RUNTIME_PROPERTIES_FILE
			String webapp = getWebappName().toUpperCase();
			log.debug("webapp: " + webapp);
			String env = webapp + "_RUNTIME_PROPERTIES_FILE";
			
			String filepath = System.getenv(env);

			if (filepath != null) {
				try {
					propertyStream = new FileInputStream(filepath);
				}
				catch (IOException e) { }
			}

			// env is the name of the file to look for in the directories
			String filename = webapp + "-runtime.properties";
			
			if (propertyStream == null) {
				if (OpenmrsConstants.OPERATING_SYSTEM_LINUX.equalsIgnoreCase(OpenmrsConstants.OPERATING_SYSTEM))
					filepath = System.getProperty("user.home") + File.separator + ".OpenMRS";
				else
					filepath = System.getProperty("user.home") + File.separator + 
							"Application Data" + File.separator + 
							"OpenMRS";
						
				filepath = filepath + File.separator + filename;
				try {
					propertyStream = new FileInputStream(filepath);
				}
				catch (IOException e) { }	
			}
			
			// look in current directory last
			if (propertyStream == null) {
				filepath = filename;
				try {
					propertyStream = new FileInputStream(filepath);
				}
				catch (IOException e) { }
			}
			
			if (propertyStream == null)
				throw new IOException("Could not open '" + filename + "' in user or local directory.");
			
			props.load(propertyStream);
			propertyStream.close();

		} catch (IOException e) {
		}
		return props;
	}

}
