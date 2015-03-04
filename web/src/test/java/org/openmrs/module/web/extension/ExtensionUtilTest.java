/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.web.extension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.module.Extension;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.web.extension.provider.Link;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ModuleFactory.class)
public class ExtensionUtilTest {
	
	/**
	 * @see ExtensionUtil#getFormsModulesCanAddEncounterToVisit()
	 * @verifies return empty set if there is no AddEncounterToVisitExtension
	 */
	@Test
	public void getModulesAddEncounterToVisitLinks_shouldReturnEmptySetIfThereIsNoAddEncounterToVisitExtension()
	        throws Exception {
		//given
		mockStatic(ModuleFactory.class);
		when(ModuleFactory.getExtensions("org.openmrs.module.web.extension.AddEncounterToVisitExtension")).thenReturn(null);
		
		//when
		Set<Link> links = ExtensionUtil.getAllAddEncounterToVisitLinks();
		
		//then
		assertNotNull(links);
		assertEquals(0, links.size());
	}
	
	/**
	 * @see ExtensionUtil#getFormsModulesCanAddEncounterToVisit()
	 * @verifies return forms if there are AddEncounterToVisitExtensions
	 */
	@Test
	public void getFormsModulesCanAddEncounterToVisit_shouldReturnFormsIfThereAreAddEncounterToVisitExtensions()
	        throws Exception {
		//given
		AddEncounterToVisitExtension ext1 = mock(AddEncounterToVisitExtension.class);
		Set<Link> links1 = new HashSet<Link>();
		Link link1 = new Link();
		link1.setLabel("a");
		links1.add(link1);
		Link link2 = new Link();
		link2.setLabel("b");
		links1.add(link2);
		when(ext1.getAddEncounterToVisitLinks()).thenReturn(links1);
		
		AddEncounterToVisitExtension ext2 = mock(AddEncounterToVisitExtension.class);
		Set<Link> links2 = new HashSet<Link>();
		Link link3 = new Link();
		link3.setLabel("aa");
		links2.add(link3);
		when(ext2.getAddEncounterToVisitLinks()).thenReturn(links2);
		
		List<Extension> extensions = new ArrayList<Extension>();
		extensions.add(ext1);
		extensions.add(ext2);
		
		mockStatic(ModuleFactory.class);
		when(ModuleFactory.getExtensions("org.openmrs.module.web.extension.AddEncounterToVisitExtension")).thenReturn(
		    extensions);
		
		//when
		Set<Link> allAddEncounterToVisitLinks = ExtensionUtil.getAllAddEncounterToVisitLinks();
		
		//then
		assertTrue(allAddEncounterToVisitLinks.contains(link1));
		assertTrue(allAddEncounterToVisitLinks.contains(link2));
		assertTrue(allAddEncounterToVisitLinks.contains(link3));
	}
}
