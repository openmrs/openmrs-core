/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows to specify a conditionally loaded resource in a module based on
 * OpenMRS version and/or modules listed as required or aware of.
 *
 * @since 1.10, 1.9.8, 1.8.5, 1.7.5
 */
public class ModuleConditionalResource {
	
	private String path;
	
	private String openmrsPlatformVersion;
	
	private List<ModuleAndVersion> modules = new ArrayList<ModuleAndVersion>();
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	/**
	 * @deprecated Since 1.11.3, 1.10.2, 1.9.9 use {@link #getOpenmrsPlatformVersion()}.
	 */
	@Deprecated
	public String getOpenmrsVersion() {
		return openmrsPlatformVersion;
	}
	
	/**
	 * @deprecated Since 1.11.3, 1.10.2, 1.9.9 use {@link #setOpenmrsPlatformVersion(String)}.
	 */
	@Deprecated
	public void setOpenmrsVersion(String openmrsVersion) {
		this.openmrsPlatformVersion = openmrsVersion;
	}
	
	/**
	 * @since 1.11.3, 1.10.2, 1.9.9
	 */
    public String getOpenmrsPlatformVersion() {
    	return openmrsPlatformVersion;
    }

    /**
	 * @since 1.11.3, 1.10.2, 1.9.9
	 */
    public void setOpenmrsPlatformVersion(String openmrsPlatformVersion) {
    	this.openmrsPlatformVersion = openmrsPlatformVersion;
    }

	public List<ModuleAndVersion> getModules() {
		return modules;
	}
	
	public void setModules(List<ModuleAndVersion> modules) {
		this.modules = modules;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ModuleConditionalResource that = (ModuleConditionalResource) o;
		
		if (modules != null ? !modules.equals(that.modules) : that.modules != null) {
			return false;
		}
		if (openmrsPlatformVersion != null ? !openmrsPlatformVersion.equals(that.openmrsPlatformVersion) : that.openmrsPlatformVersion != null) {
			return false;
		}
		if (path != null ? !path.equals(that.path) : that.path != null) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		return "ModuleConditionalResource{" + "path='" + path + '\'' + ", openmrsPlatformVersion='" + openmrsPlatformVersion + '\''
		        + ", modules=" + modules + '}';
	}
	
	@Override
	public int hashCode() {
		int result = path != null ? path.hashCode() : 0;
		result = 31 * result + (openmrsPlatformVersion != null ? openmrsPlatformVersion.hashCode() : 0);
		result = 31 * result + (modules != null ? modules.hashCode() : 0);
		return result;
	}
	
	public static class ModuleAndVersion {
		
		private String moduleId;
		
		private String version;
		
		public String getModuleId() {
			return moduleId;
		}
		
		public void setModuleId(String moduleId) {
			this.moduleId = moduleId;
		}
		
		public String getVersion() {
			return version;
		}
		
		public void setVersion(String version) {
			this.version = version;
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			ModuleAndVersion that = (ModuleAndVersion) o;
			
			if (moduleId != null ? !moduleId.equals(that.moduleId) : that.moduleId != null) {
				return false;
			}
			if (version != null ? !version.equals(that.version) : that.version != null) {
				return false;
			}
			
			return true;
		}
		
		@Override
		public int hashCode() {
			int result = moduleId != null ? moduleId.hashCode() : 0;
			result = 31 * result + (version != null ? version.hashCode() : 0);
			return result;
		}
		
		@Override
		public String toString() {
			return "ModuleAndVersion{" + "moduleId='" + moduleId + '\'' + ", version='" + version + '\'' + '}';
		}
	}
}
