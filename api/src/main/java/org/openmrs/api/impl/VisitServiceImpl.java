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
package org.openmrs.api.impl;

import java.util.List;

import org.openmrs.VisitType;
import org.openmrs.api.VisitService;
import org.openmrs.api.db.VisitDAO;

/**
 * Default implementation of the {@link VisitService}. This class should not be used on its own. The
 * current OpenMRS implementation should be fetched from the Context.
 * 
 * @since 1.9
 */
public class VisitServiceImpl extends BaseOpenmrsService implements VisitService {
	
	private VisitDAO dao;
	
	/**
	 * Method used to inject the visit data access object.
	 * 
	 * @param dao the visit data access object.
	 */
	public void setVisitDAO(VisitDAO dao) {
		this.dao = dao;
	}
	
	public VisitDAO getVisitDAO() {
		return dao;
	}
	
	/**
	 * @see org.openmrs.api.VisitService#getAllVisitTypes()
	 */
	public List<VisitType> getAllVisitTypes() {
		return getVisitDAO().getAllVisitTypes();
	}
	
	/**
	 * @see org.openmrs.api.VisitService#getVisitType(java.lang.Integer)
	 */
	public VisitType getVisitType(Integer visitTypeId) {
		return getVisitDAO().getVisitType(visitTypeId);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#getVisitTypeByUuid(java.lang.String)
	 */
	public VisitType getVisitTypeByUuid(String uuid) {
		return getVisitDAO().getVisitTypeByUuid(uuid);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#getVisitTypes(java.lang.String)
	 */
	public List<VisitType> getVisitTypes(String fuzzySearchPhrase) {
		return getVisitDAO().getVisitTypes(fuzzySearchPhrase);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#saveVisitType(org.openmrs.VisitType)
	 */
	public VisitType saveVisitType(VisitType visitType) {
		return getVisitDAO().saveVisitType(visitType);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#retireVisitType(org.openmrs.VisitType, java.lang.String)
	 */
	public VisitType retireVisitType(VisitType visitType, String reason) {
		visitType.setRetired(true);
		visitType.setRetireReason(reason);
		return saveVisitType(visitType);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#unretireVisitType(org.openmrs.VisitType)
	 */
	public VisitType unretireVisitType(VisitType visitType) {
		visitType.setRetired(false);
		return saveVisitType(visitType);
	}
	
	/**
	 * @see org.openmrs.api.VisitService#purgeVisitType(org.openmrs.VisitType)
	 */
	public void purgeVisitType(VisitType visitType) {
		getVisitDAO().purgeVisitType(visitType);
	}
}
