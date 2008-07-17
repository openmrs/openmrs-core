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
package org.openmrs.test.synchronization.engine;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.List;
import java.util.UUID;

import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptWord;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptSynonym;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;

/**
 *
 */
public class SyncConceptTest extends SyncBaseTest {

	@Override
    public String getInitialDataset() {
	    return "org/openmrs/test/synchronization/engine/include/SyncCreateTest.xml";
    }

	public void testAddNameToConcept() throws Exception {
		runSyncTest(new SyncTestHelper() {
			ConceptService cs = Context.getConceptService();
			int numNamesBefore;
			public void runOnChild() {
				Concept wt = cs.getConceptByName("WEIGHT");
				numNamesBefore = wt.getNames().size();
				wt.addName(new ConceptName("POIDS", null, "Weight in french", Locale.FRENCH));
				cs.saveConcept(wt);
			}
			public void runOnParent() {
				Concept wt = cs.getConceptByName("WEIGHT");
				assertNotNull(wt);
				assertEquals("Should be one more name than before", numNamesBefore + 1, wt.getNames().size());
				assertEquals("Incorrect french name", wt.getName(Locale.FRENCH).getName(), "POIDS");
			}
		});
	}
	

	public void testCreateCodedConcept() throws Exception {
		runSyncTest(new SyncTestHelper() {
			private int conceptId = 0;
			public void runOnChild() {
				ConceptService cs = Context.getConceptService();
				
				 //this doesn't work with in-mem DB
				//conceptId = cs.getNextAvailableId();
				conceptId = 99999;
				Concept coded = new Concept(conceptId);
				coded.setDatatype(cs.getConceptDatatypeByName("Coded"));
				coded.setConceptClass(cs.getConceptClassByName("Question"));
				coded.setSet(false);
		        coded.setSynonyms(new HashSet<ConceptSynonym>());
				coded.addName(new ConceptName("CODED", "SOME CODE", "A coded concept", Context.getLocale()));
				coded.addAnswer(new ConceptAnswer(cs.getConceptByName("OTHER NON-CODED")));
				coded.addAnswer(new ConceptAnswer(cs.getConceptByName("NONE")));
				cs.createConcept(coded);
			}
			public void runOnParent() {								
				ConceptService cs = Context.getConceptService();

				Concept c = cs.getConcept(conceptId);
				log.info("names: " + c.getNames().size());
				assertNotNull("Failed to create coded", c);
				assertEquals(c.getConceptClass().getConceptClassId(), cs.getConceptClassByName("Question").getConceptClassId());
				assertEquals(c.getDatatype().getConceptDatatypeId(), cs.getConceptDatatypeByName("Coded").getConceptDatatypeId());
				
				//NOTE: this doesn't work in junit/in-mem DB; MUST test by running in UI :(
				//java.util.Collection<ConceptAnswer> answers = c.getAnswers();
				//assertEquals(2, answers.size());
			}
		});
	}	
	
	public void testCreateConcepts() throws Exception {
		runSyncTest(new SyncTestHelper() {
			ConceptService cs;
			private int conceptIdNum= 0;
			private int conceptIdCoded= 0;
			private int conceptIdSet= 0;
			
			public void runOnChild() {
				cs = Context.getConceptService();
				//this doesn't work with in-mem DB
				//conceptIdNum = cs.getNextAvailableId();
				//conceptIdCoded = cs.getNextAvailableId();
				//conceptIdSet = cs.getNextAvailableId();
				conceptIdNum = 99997;
				conceptIdCoded = 99998;
				conceptIdSet = 99999;
	
				ConceptNumeric cn = new ConceptNumeric(conceptIdNum);
				cn.addName(new ConceptName("SOMETHING NUMERIC", "SUM NUM", "A numeric concept", Context.getLocale()));
				cn.setDatatype(cs.getConceptDatatypeByName("Numeric"));
				cn.setConceptClass(cs.getConceptClassByName("Question"));
				cn.setSet(false);
				cn.setPrecise(true);
				cn.setLowAbsolute(0d);
				cn.setHiCritical(100d);
				cs.saveConcept(cn);
				
				Concept coded = new Concept(conceptIdCoded);
				coded.addName(new ConceptName("SOMETHING CODED", "SUM CODE", "A coded concept", Context.getLocale()));
				coded.setDatatype(cs.getConceptDatatypeByName("Coded"));
				coded.setConceptClass(cs.getConceptClassByName("Question"));
				coded.setSet(false);
				coded.setSynonyms(new HashSet<ConceptSynonym>());
				coded.addAnswer(new ConceptAnswer(cs.getConceptByName("OTHER NON-CODED")));
				coded.addAnswer(new ConceptAnswer(cs.getConceptByName("NONE")));
				coded.addAnswer(new ConceptAnswer(cn));
				cs.saveConcept(coded);
				
				Concept set = new Concept(conceptIdSet);
				set.addName(new ConceptName("A CONCEPT SET", "SET", "A set of concepts", Context.getLocale()));
				set.setDatatype(cs.getConceptDatatypeByName("N/A"));
				set.setConceptClass(cs.getConceptClassByName("ConvSet"));
				set.setSet(true);
				set.setSynonyms(new HashSet<ConceptSynonym>());
				Set<ConceptSet> cset = new HashSet<ConceptSet>();
				cset.add(new ConceptSet(coded, 1d));
				cset.add(new ConceptSet(cn, 2d));
				set.setConceptSets(cset);
				cs.saveConcept(set);
			}
			public void runOnParent() {
				//Concept c = cs.getConceptByName("SOMETHING NUMERIC");
//				assertNotNull("Failed to create numeric", c);
				assertEquals(cs.getConcept(99997).getName().getName(), "SOMETHING NUMERIC");
				ConceptNumeric cn = cs.getConceptNumeric(99997);
				assertEquals(cn.getLowAbsolute(), 0d);
				assertEquals(cn.getHiCritical(), 100d);
				assertEquals(cn.getDatatype().getName(), "Numeric");
				assertEquals(cn.getConceptClass().getName(), "Question");
				
				Concept c = cs.getConceptByName("SOMETHING CODED");
				assertNotNull("Failed to create coded", c);
				
				//doesn't work in junit/in-mem DB; tested manually only
				//Set<String> answers = new HashSet<String>();
				//for (ConceptAnswer a : c.getAnswers())
				//	answers.add(a.getAnswerConcept().getName().getName());
								
				c = cs.getConceptByName("A CONCEPT SET");
				assertNotNull("Failed to create set", c);
				assertEquals(c.getConceptSets().size(), 2);
			}
		});
	}

	public void testEditConcepts() throws Exception {
		runSyncTest(new SyncTestHelper() {
			ConceptService cs;
			int numAnswersBefore;
			public void runOnChild() {
				cs = Context.getConceptService();
				Concept wt = cs.getConceptByName("WEIGHT");
				ConceptNumeric weight = cs.getConceptNumeric(wt.getConceptId());
				weight.setHiCritical(200d);
				cs.updateConcept(weight);
				
				Concept coded = cs.getConceptByName("CAUSE OF DEATH");
				assertNotNull(coded);
				Concept malaria = new Concept(99999);
				malaria.addName(new ConceptName("MALARIA", null, "A disease", Context.getLocale()));
				malaria.setDatatype(cs.getConceptDatatypeByName("N/A"));
				malaria.setConceptClass(cs.getConceptClassByName("Diagnosis"));
				malaria.setSynonyms(new HashSet<ConceptSynonym>());
				cs.createConcept(malaria);
				numAnswersBefore = coded.getAnswers().size();
				coded.addAnswer(new ConceptAnswer(malaria));
				coded.addSynonym("DEATH REASON", Context.getLocale());
				cs.updateConcept(coded);
			}
			public void runOnParent() {
				Concept wt = cs.getConceptByName("WEIGHT");
				ConceptNumeric weight = cs.getConceptNumeric(wt.getConceptId());
				assertEquals("Failed to change property on a numeric concept", weight.getHiCritical(), 200d);
				
				Concept malaria = cs.getConceptByName("MALARIA");
				assertNotNull("Implicit create of concept referenced in answer failed", malaria);
				
				Concept coded = cs.getConceptByName("CAUSE OF DEATH");
				assertEquals("Adding answer failed", numAnswersBefore + 1, coded.getAnswers().size());
				boolean found = false;
				for (ConceptSynonym syn : coded.getSynonyms())
					if (syn.getSynonym().equals("DEATH REASON"))
						found = true;
				assertTrue("Synonym not created", found);
			}
		});
	}

}
