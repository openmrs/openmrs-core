/*****************************************************************************
 * Adapted from the Java Plug-in Framework (JPF) - LGPL - Copyright (C) 2004-2006 Dmitry Olshansky
 */
package org.openmrs.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Library {
	private Log log = LogFactory.getLog(Library.class);
	
	private final ModelLibrary model;
	private List<String> exports;
	private Module module;

	public Library(final Module mod, final ModelLibrary aModel) throws ModuleException {
		model = aModel;
		module = mod;
		if ((model.getPath() == null)
				|| (model.getPath().trim().length() == 0)) {
			throw new ModuleException(mod.getName(), "libraryPathIsBlank"); 
		}
		exports = new ArrayList<String>(model.getExports().size());
		for (Iterator it = model.getExports().iterator(); it.hasNext();) {
			String exportPrefix = (String) it.next();
			if ((exportPrefix == null) || (exportPrefix.trim().length() == 0)) {
				throw new ModuleException(mod.getName(), "exportPrefixIBlank"); 
			}
			exportPrefix = exportPrefix.replace('\\', '.').replace('/', '.');
			if (exportPrefix.startsWith(".")) { 
				exportPrefix = exportPrefix.substring(1);
			}
			exports.add(exportPrefix);
		}
		exports = Collections.unmodifiableList(exports);
		if (log.isDebugEnabled()) {
			log.debug("object instantiated: " + this); 
		}
	}

	/**
	 * @see org.java.module.registry.Library#getPath()
	 */
	public String getPath() {
		return model.getPath();
	}
	
	/**
	 * @see org.java.module.registry.Library#getExports()
	 */
	public Collection getExports() {
		return exports;
	}

	/**
	 * @see org.java.module.registry.Library#isCodeLibrary()
	 */
	public boolean isCodeLibrary() {
		return model.isCodeLibrary();
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "{Library: module uid=" + module + "}";
	}

	/**
	 * @see org.java.module.registry.Library#getVersion()
	 */
	public String getVersion() {
		return model.getVersion();
	}	
}

final class ModelLibrary {
    private String id;
    private String path;
    private boolean isCodeLibrary;
    private List exports = new Vector();
    private String version;
    
    ModelLibrary() {
        // no-op
    }
    
    String getId() {
        return id;
    }
    
    void setId(final String value) {
        id = value;
    }
    
    boolean isCodeLibrary() {
        return isCodeLibrary;
    }
    
    void setCodeLibrary(final String value) {
        isCodeLibrary = "code".equals(value); 
    }
    
    String getPath() {
        return path;
    }
    
    void setPath(final String value) {
        path = value;
    }
    
    List getExports() {
        return exports;
    }
    
    String getVersion() {
        return version;
    }
    
    void setVersion(final String value) {
        version = value;
    }
}
