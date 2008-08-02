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

	public void testSaveConceptCoded() throws Exception {
		runSyncTest(new SyncTestHelper() {
			private int conceptId = 99999;
			public void runOnChild() {
				ConceptService cs = Context.getConceptService();
				
				//this doesn't work with in-mem DB
				//conceptId = cs.getNextAvailableId();
				Concept coded = new Concept(conceptId);
				coded.setDatatype(cs.getConceptDatatypeByName("Coded"));
				coded.setConceptClass(cs.getConceptClassByName("Question"));
				coded.setSet(false);
		        coded.setSynonyms(new HashSet<ConceptSynonym>());
				coded.addName(new ConceptName("CODED", "SOME CODE", "A coded concept", Context.getLocale()));
				coded.addAnswer(new ConceptAnswer(cs.getConceptByName("OTHER NON-CODED")));
				coded.addAnswer(new ConceptAnswer(cs.getConceptByName("NONE")));
				cs.saveConcept(coded);
			}
			public void runOnParent() {								
				ConceptService cs = Context.getConceptService();

				Concept c = cs.getConcept(conceptId);
				log.info("names: " + c.getNames().size());
				assertNotNull("Failed to create coded concept", c);
				assertEquals(c.getConceptClass().getConceptClassId(), cs.getConceptClassByName("Question").getConceptClassId());
				assertEquals(c.getDatatype().getConceptDatatypeId(), cs.getConceptDatatypeByName("Coded").getConceptDatatypeId());
				
				//NOTE: this doesn't work in junit/in-mem DB; MUST test by running in UI :(
				//java.util.Collection<ConceptAnswer> answers = c.getAnswers();
				//assertEquals(2, answers.size());
			}
		});
	}		
	public void testSaveConceptNumeric() throws Exception {
		runSyncTest(new SyncTestHelper() {
			ConceptService cs;
			private int conceptId=99999;
			
			public void runOnChild() {
				cs = Context.getConceptService();
				//this doesn't work with in-mem DB
				//conceptIdNum = cs.getNextAvailableId();
				ConceptNumeric cn = new ConceptNumeric(conceptId);
				cn.addName(new ConceptName("SOMETHING NUMERIC", "SUM NUM", "A numeric concept", Context.getLocale()));
				cn.setDatatype(cs.getConceptDatatypeByName("Numeric"));
				cn.setConceptClass(cs.getConceptClassByName("Question"));
				cn.setSet(false);
				cn.setPrecise(true);
				cn.setLowAbsolute(0d);
				cn.setHiCritical(100d);
				cs.saveConcept(cn);
			}
			public void runOnParent() {
				//Concept c = cs.getConceptByName("SOMETHING NUMERIC");
				// assertNotNull("Failed to create numeric", c);
				assertEquals(cs.getConcept(conceptId).getName().getName(), "SOMETHING NUMERIC");
				ConceptNumeric cn = cs.getConceptNumeric(conceptId);
				assertEquals("Concept numeric absolute low values do not match", 0d, cn.getLowAbsolute());
				assertEquals("Concept nuermic high critical values do not match", 100d, cn.getHiCritical());
				assertEquals("Concept numeric datatypes does not match", "Numeric", cn.getDatatype().getName());
				assertEquals("Concept numeric classes does not match", "Question", cn.getConceptClass().getName());
				
			}
		});
	}
	
	

	
	
	
	public void testSaveConceptSet() throws Exception {
		runSyncTest(new SyncTestHelper() {
			ConceptService cs;
			private int conceptNumericId=99997;
			private int conceptCodedId=99998;
			private int conceptSetId=99999;
			
			private String guid = "";
			
			public void runOnChild() {
				cs = Context.getConceptService();

				ConceptNumeric cn = new ConceptNumeric(conceptNumericId);
				cn.addName(new ConceptName("SOMETHING NUMERIC", "SUM NUM", "A numeric concept", Context.getLocale()));
				cn.setDatatype(cs.getConceptDatatypeByName("Numeric"));
				cn.setConceptClass(cs.getConceptClassByName("Question"));
				cn.setSet(false);
				cn.setPrecise(true);
				cn.setLowAbsolute(0d);
				cn.setHiCritical(100d);
				cs.saveConcept(cn);
				
				Concept coded = new Concept(conceptCodedId);
				coded.addName(new ConceptName("SOMETHING CODED", "SUM CODE", "A coded concept", Context.getLocale()));
				coded.setDatatype(cs.getConceptDatatypeByName("Coded"));
				coded.setConceptClass(cs.getConceptClassByName("Question"));
				coded.setSet(false);
				coded.setSynonyms(new HashSet<ConceptSynonym>());
				
				Concept other = cs.getConceptByName("OTHER NON-CODED");
				assertNotNull("Failed to get concept OTHER NON-CODED", other);

				Concept none = cs.getConceptByName("NONE");
				assertNotNull("Failed to get concept NONE", none);
				
				coded.addAnswer(new ConceptAnswer(other));
				coded.addAnswer(new ConceptAnswer(none));
				coded.addAnswer(new ConceptAnswer(cn));
				cs.saveConcept(coded);
			
				
				//ConceptSet conceptSet = new ConceptSet();
				
				
				Concept set = new Concept(conceptSetId);
				
				log.info("Locale: " + Context.getLocale());
				
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
				
				guid = set.getGuid();
				log.info("GUID:  " + set.getGuid());
			}
			public void runOnParent() {
				Concept c = cs.getConceptByName("SOMETHING NUMERIC");
				assertNotNull("Failed to create numeric", c);
			
				Concept set = cs.getConcept(conceptSetId);
								
				set = cs.getConceptByName("A CONCEPT SET");
				
				
				assertEquals("Concept names do not match", "SOMETHING NUMERIC", cs.getConcept(conceptNumericId).getName().getName());
				
				
				ConceptNumeric cn = cs.getConceptNumeric(conceptNumericId);
				assertEquals("Concept numeric absolute low values do not match", 0d, cn.getLowAbsolute());
				assertEquals("Concept numeric critical high values do not match", 100d, cn.getHiCritical());
				assertEquals("Concept numeric datatypes do not match", "Numeric", cn.getDatatype().getName());
				assertEquals("Concept numeric classes do not match", "Question", cn.getConceptClass().getName());
				
				//doesn't work in junit/in-mem DB; tested manually only
				//Set<String> answers = new HashSet<String>();
				//for (ConceptAnswer a : c.getAnswers())
				//	answers.add(a.getAnswerConcept().getName().getName());
				
				// Test the coded concept 			
				Concept conceptCoded = cs.getConcept(conceptCodedId);
				assertNotNull("Failed to save coded concept - Could not retrieve concept by ID", conceptCoded);

				conceptCoded = cs.getConceptByName("SOMETHING CODED");
				assertNotNull("Failed to save coded concept - Could not retrieve concept by name", conceptCoded);
					
				
				// Test the concept set 
				
				Concept conceptSet = cs.getConcept(conceptSetId);
				assertNotNull("Failed to save concept set - Could not retrieve concept by ID", conceptSet);
				
				conceptSet = cs.getConceptByName("A CONCEPT SET");
				assertNotNull("Failed to create coded concept - Could not retrieve code concept by name", conceptSet);

				
				assertEquals("Failed to create concept set - Concept set should have two elements", conceptSet.getConceptSets().size(), 2);
				
			
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
				cs.saveConcept(weight);
				
				Concept coded = cs.getConceptByName("CAUSE OF DEATH");
				assertNotNull(coded);
				Concept malaria = new Concept(99999);
				malaria.addName(new ConceptName("MALARIA", null, "A disease", Context.getLocale()));
				malaria.setDatatype(cs.getConceptDatatypeByName("N/A"));
				malaria.setConceptClass(cs.getConceptClassByName("Diagnosis"));
				malaria.setSynonyms(new HashSet<ConceptSynonym>());
				cs.saveConcept(malaria);
				numAnswersBefore = coded.getAnswers().size();
				coded.addAnswer(new ConceptAnswer(malaria));
				coded.addSynonym("DEATH REASON", Context.getLocale());
				cs.saveConcept(coded);
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
	

	
}
