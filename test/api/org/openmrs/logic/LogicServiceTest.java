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
package org.openmrs.logic;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.logic.result.Result;
import org.openmrs.logic.rule.AgeRule;
import org.openmrs.logic.rule.HIVPositiveRule;
import org.openmrs.logic.rule.ReferenceRule;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * TODO: add more tests to this test case
 */
public class LogicServiceTest extends BaseContextSensitiveTest {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * @see org.springframework.test.AbstractTransactionalSpringContextTests#onSetUpInTransaction()
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		executeDataSet("org/openmrs/logic/include/LogicStandardDatasets.xml");
		executeDataSet("org/openmrs/logic/include/LogicTests-patients.xml");
		executeDataSet("org/openmrs/logic/include/LogicBasicTest.concepts.xml");
	}
	
	/**
	 * TODO make this test use assert statements instead of printing to stdout
	 */
	@Test
	public void shouldObservationRule() {
		LogicService logicService = Context.getLogicService();
		Cohort patients = new Cohort();
		Map<Integer, Result> result = null;
		
		patients.addMember(2);
		
		try {
			Result r = logicService.eval(new Patient(2), "CD4 COUNT");
			Assert.assertNotNull(r);
			Assert.assertEquals(1, r.size());
			
			result = logicService.eval(patients, "CD4 COUNT");
			Assert.assertNotNull(result);
			
			result = logicService.eval(patients, new LogicCriteria("CD4 COUNT").lt(170));
			Assert.assertNotNull(result);
			
			result = logicService.eval(patients, new LogicCriteria("CD4 COUNT").gt(185));
			Assert.assertNotNull(result);
			
			result = logicService.eval(patients, new LogicCriteria("CD4 COUNT").equalTo(190));
			Assert.assertNotNull(result);
			
			Calendar cal = Calendar.getInstance();
			cal.set(2006, 3, 11);
			result = logicService.eval(patients, new LogicCriteria("CD4 COUNT").before(cal.getTime()));
			Assert.assertNotNull(result);
			
			result = logicService.eval(patients, new LogicCriteria("CD4 COUNT").lt(190).before(cal.getTime()));
			Assert.assertNotNull(result);
			
			result = logicService.eval(patients, new LogicCriteria("CD4 COUNT").after(cal.getTime()));
			Assert.assertNotNull(result);
			
			result = logicService.eval(patients, new LogicCriteria("CD4 COUNT").last());
			Assert.assertNotNull(result);
			
			result = logicService.eval(patients, new LogicCriteria("CD4 COUNT").last().before(cal.getTime()));
			Assert.assertNotNull(result);
			
			result = logicService.eval(patients, new LogicCriteria("CD4 COUNT").not().lt(150));
			Assert.assertNotNull(result);
			
			result = logicService.eval(patients, new LogicCriteria("CD4 COUNT").not().not().lt(150));
			Assert.assertNotNull(result);
			
			patients.addMember(2);
			patients.addMember(3);
			result = logicService.eval(patients, new LogicCriteria("CD4 COUNT").lt(200));
			Assert.assertNotNull(result);
			
			patients.addMember(39);
			result = logicService.eval(patients, new LogicCriteria("PROBLEM ADDED").contains("HUMAN IMMUNODEFICIENCY VIRUS"));
			Assert.assertNotNull(result);
			
			result = logicService.eval(patients, "CD4 COUNT");
			Assert.assertNotNull(result);
			
			result = logicService.eval(patients, new LogicCriteria("CD4 COUNT").within(Duration.months(1)));
			Assert.assertNotNull(result);
			
		}
		catch (LogicException e) {
			log.error("testObservationRule: Error generated", e);
		}
	}
	
	/**
	 * TODO make this test use assert statements instead of printing to stdout
	 */
	@Test
	public void shouldDemographicsRule() {
		LogicService logicService = Context.getLogicService();
		Cohort patients = new Cohort();
		Map<Integer, Result> result;
		
		patients.addMember(2);
		
		try {
			result = logicService.eval(patients, "BIRTHDATE");
			Assert.assertNotNull(result);
			
			result = logicService.eval(patients, "AGE");
			Assert.assertNotNull(result);
			
			result = logicService.eval(patients, new LogicCriteria("AGE").gt(10));
			Assert.assertNotNull(result);
			
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, 2000);
			result = logicService.eval(patients, new LogicCriteria("BIRTHDATE").after(cal.getTime()));
			Assert.assertNotNull(result);
			
			result = logicService.eval(patients, new LogicCriteria("BIRTHDATE").before(cal.getTime()));
			Assert.assertNotNull(result);
			
			// test the index date functionality
			Calendar index = Calendar.getInstance();
			index.set(1970, 0, 1);
			result = logicService.eval(patients, "BIRTHDATE");
			Assert.assertNotNull(result);
			
			result = logicService.eval(patients, new LogicCriteria("BIRTHDATE").within(Duration.years(5)));
			Assert.assertNotNull(result);
		}
		catch (LogicException e) {
			log.error("testDemographicsRule: Error generated", e);
		}
		
	}
	
	/**
	 * TODO make this test use assert statements instead of printing to stdout
	 */
	@Test
	public void shouldHIVPositiveRule() {
		LogicService logicService = Context.getLogicService();
		Cohort patients = new Cohort();
		Map<Integer, Result> result = null;
		
		patients.addMember(2);
		
		try {
			
			result = logicService.eval(patients, new LogicCriteria("PROBLEM ADDED").contains("HUMAN IMMUNODEFICIENCY VIRUS").first());
			Assert.assertNotNull(result);
			
			result = logicService.eval(patients, new LogicCriteria("PROBLEM ADDED").contains("HIV INFECTED").first());
			Assert.assertNotNull(result);
			
			result = logicService.eval(patients, new LogicCriteria("PROBLEM ADDED").contains("ASYMPTOMATIC HIV INFECTION").first());
			Assert.assertNotNull(result);
			
			result = logicService.eval(patients, new LogicCriteria("HIV VIRAL LOAD").first());
			Assert.assertNotNull(result);
			
			result = logicService.eval(patients, new LogicCriteria("HIV VIRAL LOAD, QUALITATIVE").first());
			Assert.assertNotNull(result);
			
			result = logicService.eval(patients, new LogicCriteria("CD4 COUNT").lt(200).first());
			Assert.assertNotNull(result);
			
			result = logicService.eval(patients, "HIV POSITIVE");
			Assert.assertNotNull(result);
		}
		catch (LogicException e) {
			log.error("Error generated", e);
		}
	}
	
	/**
	 * TODO make this test use assert statements instead of printing to stdout
	 */
	@Test
	public void shouldReferenceRule() {
		LogicService logicService = Context.getLogicService();
		Cohort patients = new Cohort();
		Map<Integer, Result> result = null;
		
		patients.addMember(2);
		
		try {
			
			result = logicService.eval(patients, "%%obs.CD4 COUNT");
			Assert.assertNotNull(result);
			
			result = logicService.eval(patients, new LogicCriteria("%%obs.CD4 COUNT").first());
			Assert.assertNotNull(result);
			
			result = logicService.eval(patients, "%%person.BIRTHDATE");
			Assert.assertNotNull(result);
			
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, 1980);
			cal.set(Calendar.MONTH, 0);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			result = logicService.eval(patients, new LogicCriteria("%%person.BIRTHDATE").before(cal.getTime()));
			Assert.assertNotNull(result);
		}
		catch (LogicException e) {
			log.error("Error generated", e);
		}
	}

	/**
     * @see {@link LogicService#addRule(String,Rule)}
     * 
     */
    @Test
    @Verifies(value = "should not fail when another rule is registered on the same token", method = "addRule(String,Rule)")
    public void addRule_shouldNotFailWhenAnotherRuleIsRegisteredOnTheSameToken() throws Exception {
    	LogicService logicService = Context.getLogicService();
    	Rule hivRule = logicService.getRule("HIV POSITIVE");
    	Assert.assertNotNull(hivRule);
    	
    	Rule ageRule = logicService.getRule("AGE");
    	Assert.assertNotNull(ageRule);
    	
    	logicService.addRule("HIV POSITIVE", ageRule);
    	Rule storedHivRule = logicService.getRule("HIV POSITIVE");
    	Assert.assertNotNull(storedHivRule);
    	Assert.assertTrue(AgeRule.class.isAssignableFrom(storedHivRule.getClass()));
    }

	/**
     * @see {@link LogicService#addRule(String,Rule)}
     * 
     */
    @Test
    @Verifies(value = "should persist the rule and associate it with the token", method = "addRule(String,Rule)")
    public void addRule_shouldPersistTheRuleAndAssociateItWithTheToken() throws Exception {
    	LogicService logicService = Context.getLogicService();
    	
    	Rule hivRule = logicService.getRule("HIV POSITIVE");
    	Assert.assertNotNull(hivRule);
    	
    	logicService.addRule("ANOTHER HIV POSITIVE", hivRule);
    	
    	Rule anotherHivRule = logicService.getRule("ANOTHER HIV POSITIVE");
    	Assert.assertNotNull(anotherHivRule);
    	Assert.assertTrue(HIVPositiveRule.class.isAssignableFrom(anotherHivRule.getClass()));
    }

	/**
     * @see {@link LogicService#addRule(String,null,Rule)}
     * 
     */
    @Test
    @Verifies(value = "should not fail when no tags is specified", method = "addRule(String,null,Rule)")
    public void addRule_shouldNotFailWhenNoTagsIsSpecified() throws Exception {
    	LogicService logicService = Context.getLogicService();
    	
    	Rule ageRule = logicService.getRule("AGE");
    	Assert.assertNotNull(ageRule);
    	
    	logicService.addRule("ANOTHER AGE RULE", null, ageRule);
    	
    	Rule anotherAgeRule = logicService.getRule("ANOTHER AGE RULE");
    	Assert.assertNotNull(anotherAgeRule);
    	Assert.assertTrue(AgeRule.class.isAssignableFrom(anotherAgeRule.getClass()));
    }

	/**
     * @see {@link LogicService#addRule(String,null,Rule)}
     * 
     */
    @Test
    @Verifies(value = "should persist rule with the tags", method = "addRule(String,null,Rule)")
    public void addRule_shouldPersistRuleWithTheTags() throws Exception {
    	LogicService logicService = Context.getLogicService();
    	
    	String[] newTags = {"tags1", "tags2", "tags3"};
    	
    	Rule rule = logicService.getRule("HIV POSITIVE");
    	Assert.assertNotNull(rule);
    	
    	logicService.addRule("ANOTHER HIV POSITIVE", newTags, rule);
    	int tagsCount = logicService.getTokenTags("HIV POSITIVE").size();
    	
    	Assert.assertEquals(newTags.length, tagsCount);
    }

	/**
     * @see {@link LogicService#addTokenTag(String,String)}
     * 
     */
    @Test
    @Verifies(value = "should add tag for a token", method = "addTokenTag(String,String)")
    public void addTokenTag_shouldAddTagForAToken() throws Exception {
    	LogicService logicService = Context.getLogicService();
    	
    	int initialTags = logicService.getTokenTags("HIV POSITIVE").size();
    	
    	logicService.addTokenTag("HIV POSITIVE", "a tag");
    	
    	int finalTags = logicService.getTokenTags("HIV POSITIVE").size();
    	
    	Assert.assertEquals(initialTags + 1, finalTags);
    }

	/**
     * @see {@link LogicService#findTags(String)}
     * 
     */
    @Test
    @Verifies(value = "should return set of tags matching input tag partially", method = "getTags(String)")
    public void getTags_shouldReturnSetOfTagsMatchingInputTagPartially() throws Exception {
    	LogicService logicService = Context.getLogicService();
    	
    	String[] similarTags = {"tags01", "tags02", "tags03", "tags04", "tags05"};
    	Rule rule = logicService.getRule("AGE");
    	Assert.assertNotNull(rule);
    	
    	logicService.addRule("ANOTHER AGE", similarTags, rule);
    	
    	List<String> tags = logicService.getTags("tags");
    	
    	Assert.assertEquals(similarTags.length, tags.size());
    }

	/**
     * @see {@link LogicService#findToken(String)}
     * 
     */
    @Test
    @Verifies(value = "should return all registered token matching the input fully", method = "getTokens(String)")
    public void getTokens_shouldReturnAllRegisteredTokenMatchingTheInputFully() throws Exception {
    	LogicService logicService = Context.getLogicService();
    	List<String> tokens = logicService.getTokens("AGE");
    	Assert.assertEquals(1, tokens.size());
    }

	/**
     * @see {@link LogicService#findToken(String)}
     * 
     */
    @Test
    @Verifies(value = "should return all registered token matching the input partially", method = "getTokens(String)")
    public void getTokens_shouldReturnAllRegisteredTokenMatchingTheInputPartially() throws Exception {
    	LogicService logicService = Context.getLogicService();
    	
    	int REGISTERED_TOKEN_COUNTER = 5;
    	String TOKEN_NAME = "AGE";
    	
    	Rule rule = logicService.getRule(TOKEN_NAME);
    	Assert.assertNotNull(rule);
    	
    	for (int i = 0; i < REGISTERED_TOKEN_COUNTER; i++) {
    		TOKEN_NAME = TOKEN_NAME + " ANOTHER";
    		logicService.addRule(TOKEN_NAME, rule);
        }
    	
    	List<String> tokens = logicService.getTokens("ANOTHER");
    	Assert.assertEquals(REGISTERED_TOKEN_COUNTER, tokens.size());
    }

	/**
     * @see {@link LogicService#findToken(String)}
     * 
     */
    @Test
    @Verifies(value = "should not fail when input is null", method = "getTokens(String)")
    public void getTokens_shouldNotFailWhenInputIsNull() throws Exception {
    	Collection<String> tokens = Context.getLogicService().getTokens(null);
    	Assert.assertNotNull(tokens);
    }

	/**
     * @see {@link LogicService#getRule(String)}
     * 
     */
    @Test
    @Verifies(value = "should return Rule associated with the input token", method = "getRule(String)")
    public void getRule_shouldReturnRuleAssociatedWithTheInputToken() throws Exception {
    	Rule ageRule = Context.getLogicService().getRule("AGE");
    	Assert.assertNotNull(ageRule);
    	Assert.assertTrue(AgeRule.class.isAssignableFrom(ageRule.getClass()));
    }

	/**
     * @see {@link LogicService#getRule(String)}
     * 
     */
    @Test(expected = LogicException.class)
    @Verifies(value = "should fail when no Rule is associated with the input token", method = "getRule(String)")
    public void getRule_shouldFailWhenNoRuleIsAssociatedWithTheInputToken() throws Exception {
    	Rule rule = Context.getLogicService().getRule("UNKNOWN RULE");
    	Assert.assertNull(rule);
    }

	/**
     * @see {@link LogicService#getRule(String)}
     * 
     */
    @Test
    @Verifies(value = "should return ReferenceRule", method = "getRule(String)")
    public void getRule_shouldReturnReferenceRule() throws Exception {
    	Rule rule = Context.getLogicService().getRule("%%person.birthdate");
    	Assert.assertNotNull(rule);
    	Assert.assertTrue(rule.getClass().isAssignableFrom(ReferenceRule.class));
    }

	/**
     * @see {@link LogicService#getTagsByToken(String)}
     * 
     */
    @Test
    @Verifies(value = "should return set of tags for a certain token", method = "getTokenTags(String)")
    public void getTokenTags_shouldReturnSetOfTagsForACertainToken() throws Exception {
    	LogicService logicService = Context.getLogicService();
    	Rule ageRule = logicService.getRule("AGE");
    	
    	String[] setOfTags = {"birth", "date", "born"};
    	
    	logicService.addRule("ANOTHER AGE RULE", setOfTags, ageRule);
    	
    	Set<String> retrievedTags = logicService.getTokenTags("ANOTHER AGE RULE");
    	Assert.assertEquals(setOfTags.length, retrievedTags.size());
    	
    	logicService.addTokenTag("ANOTHER AGE RULE", "tag");
    	
    	retrievedTags = logicService.getTokenTags("ANOTHER AGE RULE");
    	Assert.assertEquals(setOfTags.length + 1, retrievedTags.size());
    }

	/**
     * @see {@link LogicService#getTokens()}
     * 
     */
    @Test
    @Verifies(value = "should return all registered token", method = "getAllTokens()")
    public void getAllTokens_shouldReturnAllRegisteredToken() throws Exception {
    	Collection<String> tokens = Context.getLogicService().getAllTokens();
    	Assert.assertEquals(16, tokens.size());
    }

	/**
     * @see {@link LogicService#getTokensByTag(String)}
     * 
     */
    @Test
    @Verifies(value = "should return set of token associated with a tag", method = "getTokensWithTag(String)")
    public void getTokensWithTag_shouldReturnSetOfTokenAssociatedWithATag() throws Exception {
    	LogicService logicService = Context.getLogicService();
    	Rule ageRule = logicService.getRule("AGE");
    	
    	String[] setOfTags = {"birth", "date", "born"};
    	
    	logicService.addRule("ANOTHER AGE RULE", setOfTags, ageRule);
    	
    	Collection<String> tokens = logicService.getTokensWithTag("birth");
    	Assert.assertEquals(1, tokens.size());
    }

	/**
     * @see {@link LogicService#removeRule(String)}
     * 
     */
    @Test (expected = LogicException.class)
    @Verifies(value = "should remove rule", method = "removeRule(String)")
    public void removeRule_shouldRemoveRule() throws Exception {
    	LogicService logicService = Context.getLogicService();
    	Rule ageRule = logicService.getRule("AGE");
    	Assert.assertNotNull(ageRule);
    	
    	logicService.removeRule("AGE");
    	Rule afterDeleteAgeRule = logicService.getRule("AGE");
    	Assert.assertNull(afterDeleteAgeRule);
    }

	/**
     * @see {@link LogicService#removeTokenTag(String,String)}
     * 
     */
    @Test
    @Verifies(value = "should remove tag from a token", method = "removeTokenTag(String,String)")
    public void removeTokenTag_shouldRemoveTagFromAToken() throws Exception {
    	LogicService logicService = Context.getLogicService();
    	Rule ageRule = logicService.getRule("AGE");
    	
    	String[] setOfTags = {"birth", "date", "born"};
    	
    	logicService.addRule("ANOTHER AGE RULE", setOfTags, ageRule);
    	
    	Collection<String> retrievedTags = logicService.getTokenTags("ANOTHER AGE RULE");
    	Assert.assertEquals(setOfTags.length, retrievedTags.size());
    	
    	logicService.removeTokenTag("ANOTHER AGE RULE", "date");
    	
    	retrievedTags = logicService.getTokenTags("ANOTHER AGE RULE");
    	Assert.assertEquals(setOfTags.length - 1, retrievedTags.size());
    }

	/**
     * @see {@link LogicService#updateRule(String,Rule)}
     * 
     */
    @Test
    @Verifies(value = "should update Rule when another Rule is registered under the same token", method = "updateRule(String,Rule)")
    public void updateRule_shouldUpdateRuleWhenAnotherRuleIsRegisteredUnderTheSameToken() throws Exception {
    	LogicService logicService = Context.getLogicService();
    	Rule hivRule = logicService.getRule("HIV POSITIVE");
    	Assert.assertNotNull(hivRule);
    	
    	Rule ageRule = logicService.getRule("AGE");
    	Assert.assertNotNull(ageRule);
    	
    	logicService.updateRule("HIV POSITIVE", ageRule);
    	Rule storedHivRule = logicService.getRule("HIV POSITIVE");
    	Assert.assertNotNull(storedHivRule);
    	Assert.assertTrue(AgeRule.class.isAssignableFrom(storedHivRule.getClass()));
    }
}
