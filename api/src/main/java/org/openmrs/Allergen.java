/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import org.apache.commons.lang3.StringUtils;

/**
 * Represent allergen
 */
public class Allergen {
	
	private AllergenType allergenType;
	
	private Concept codedAllergen;
	
	private String nonCodedAllergen;
	
	private static String OTHER_NON_CODED_CONCEPT_UUID;
	
	/**
	 * Default constructor
	 */
	public Allergen(){
	}
	
	/**
	 * @param allergenType the allergenType to set
	 * @param codedAllergen the codedAllergen to set
	 * @param nonCodedAllergen the nonCodedAllergen to set
	 */
	public Allergen(AllergenType allergenType, Concept codedAllergen, String nonCodedAllergen) {
		this.allergenType = allergenType;
		this.codedAllergen = codedAllergen;
		this.nonCodedAllergen = nonCodedAllergen;
	}
	
	/**
	 * @return Returns the allergenType
	 */
	public AllergenType getAllergenType() {
		return allergenType;
	}
	
	/**
	 * @param allergenType the allergenType to set
	 */
	public void setAllergenType(AllergenType allergenType) {
		this.allergenType = allergenType;
	}
	
    /**
     * @return the codedAllergen
     */
    public Concept getCodedAllergen() {
    	return codedAllergen;
    }
	
    
	/**
	 * Sets other non coded concept uuid constant.
	 * 
	 * @param otherNonCodedConceptUuid
	 * @since 2.0
	 */
    public static void setOtherNonCodedConceptUuid(String otherNonCodedConceptUuid) {
		OTHER_NON_CODED_CONCEPT_UUID = otherNonCodedConceptUuid;
    }
    
	/**
	 * Returns other non coded concept uuid constant.
	 * 
	 * @return other non coded concept uuid constant
	 * @since 2.0
	 */
	public static String getOtherNonCodedConceptUuid() {
		return OTHER_NON_CODED_CONCEPT_UUID;
	}

    /**
     * @param codedAllergen the codedAllergen to set
     */
    public void setCodedAllergen(Concept codedAllergen) {
		this.codedAllergen = codedAllergen;
		if (codedAllergen != null && !codedAllergen.getUuid().equals(getOtherNonCodedConceptUuid())) {
			nonCodedAllergen = null;
		}
    }
	
    /**
     * @return the nonCodedAllergen
     */
    public String getNonCodedAllergen() {
    	return nonCodedAllergen;
    }

    /**
     * @param nonCodedAllergen the nonCodedAllergen to set
     */
    public void setNonCodedAllergen(String nonCodedAllergen) {
		this.nonCodedAllergen = nonCodedAllergen;
		if (StringUtils.isNotBlank(nonCodedAllergen) && codedAllergen != null
				&& !codedAllergen.getUuid().equals(getOtherNonCodedConceptUuid())) {
			codedAllergen = null;	
		}
    }

	public boolean isCoded(){
		return codedAllergen != null && !codedAllergen.getUuid().equals(getOtherNonCodedConceptUuid());
	}

	@Override
    public String toString() {
	    if (StringUtils.isNotBlank(nonCodedAllergen)) {
	    	return nonCodedAllergen;
	    }
	    return codedAllergen.getName().getName();
    }
	
	/**
	 * Checks if this allergen is the same as the given one
	 * 
	 * @param allergen the given allergen to test with
	 * 
	 * @should return true for same coded allergen
	 * @should return false for different coded allergen
	 * @should return true for same non coded allergen
	 * @should return false for different non coded allergen
	 * 
	 * @return true if the same, else false
	 */
	public boolean isSameAllergen(Allergen allergen) {
		if (isCoded()) {
			return allergen.getCodedAllergen() != null && codedAllergen.equals(allergen.getCodedAllergen());
		}  else {
			return nonCodedAllergen != null && allergen.getNonCodedAllergen() != null && nonCodedAllergen
					.equalsIgnoreCase(allergen.getNonCodedAllergen());
		}

	}
}
