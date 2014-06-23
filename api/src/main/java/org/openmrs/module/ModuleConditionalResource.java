/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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
	
	private String openmrsVersion;
	
	private List<ModuleAndVersion> modules = new ArrayList<ModuleAndVersion>();
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getOpenmrsVersion() {
		return openmrsVersion;
	}
	
	public void setOpenmrsVersion(String openmrsVersion) {
		this.openmrsVersion = openmrsVersion;
	}
	
	public List<ModuleAndVersion> getModules() {
		return modules;
	}
	
	public void setModules(List<ModuleAndVersion> modules) {
		this.modules = modules;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		
		ModuleConditionalResource that = (ModuleConditionalResource) o;
		
		if (modules != null ? !modules.equals(that.modules) : that.modules != null)
			return false;
		if (openmrsVersion != null ? !openmrsVersion.equals(that.openmrsVersion) : that.openmrsVersion != null)
			return false;
		if (path != null ? !path.equals(that.path) : that.path != null)
			return false;
		
		return true;
	}
	
	@Override
	public String toString() {
		return "ModuleConditionalResource{" + "path='" + path + '\'' + ", openmrsVersion='" + openmrsVersion + '\''
		        + ", modules=" + modules + '}';
	}
	
	@Override
	public int hashCode() {
		int result = path != null ? path.hashCode() : 0;
		result = 31 * result + (openmrsVersion != null ? openmrsVersion.hashCode() : 0);
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
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			
			ModuleAndVersion that = (ModuleAndVersion) o;
			
			if (moduleId != null ? !moduleId.equals(that.moduleId) : that.moduleId != null)
				return false;
			if (version != null ? !version.equals(that.version) : that.version != null)
				return false;
			
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
