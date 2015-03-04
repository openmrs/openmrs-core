/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.comparator;

import java.util.Comparator;

import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.collections.comparators.NullComparator;
import org.openmrs.PatientIdentifierType;

/**
 * Orders {@link PatientIdentifierType} by retired (true last), required (true first), name and
 * id.
 * 
 * @since 1.9.2, 1.8.5
 */
public class PatientIdentifierTypeDefaultComparator implements Comparator<PatientIdentifierType> {
	
	private final ComparatorChain comparatorChain;
	
	public PatientIdentifierTypeDefaultComparator() {
		comparatorChain = new ComparatorChain();
		
		final NullComparator nullHigherComparator = new NullComparator();
		final NullComparator nullLowerComparator = new NullComparator(false);
		
		//Retired higher
		comparatorChain.addComparator(new Comparator<PatientIdentifierType>() {
			
			@Override
			public int compare(PatientIdentifierType o1, PatientIdentifierType o2) {
				return nullLowerComparator.compare(o1.getRetired(), o2.getRetired());
			}
		});
		
		//Required lower
		comparatorChain.addComparator(new Comparator<PatientIdentifierType>() {
			
			@Override
			public int compare(PatientIdentifierType o1, PatientIdentifierType o2) {
				return nullLowerComparator.compare(o1.getRequired(), o2.getRequired());
			}
		}, true);
		
		//By name
		comparatorChain.addComparator(new Comparator<PatientIdentifierType>() {
			
			@Override
			public int compare(PatientIdentifierType o1, PatientIdentifierType o2) {
				String o1Name = (o1.getName() != null) ? o1.getName().toLowerCase() : null;
				String o2Name = (o2.getName() != null) ? o2.getName().toLowerCase() : null;
				
				return nullHigherComparator.compare(o1Name, o2Name);
			}
		});
		
		//By id
		comparatorChain.addComparator(new Comparator<PatientIdentifierType>() {
			
			@Override
			public int compare(PatientIdentifierType o1, PatientIdentifierType o2) {
				return nullHigherComparator.compare(o1.getPatientIdentifierTypeId(), o2.getPatientIdentifierTypeId());
			}
		});
	}
	
	/**
	 * Orders by retired (true last), required (true first), name and id.
	 * 
	 * @should order properly
	 */
	@Override
	public int compare(PatientIdentifierType pit1, PatientIdentifierType pit2) {
		return comparatorChain.compare(pit1, pit2);
	}
}
