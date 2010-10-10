package org.openmrs.reporting;


import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.event.TreeSelectionEvent;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.api.PatientSetService.BooleanOperator;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.report.EvaluationContext;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

public class InversePatientFilterTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link InversePatientFilter#filter(Cohort,EvaluationContext)}
	 * 
	 */
	@Test
	@Verifies(value = "should not fail with compound patient filter", method = "filter(Cohort,EvaluationContext)")
	public void filter_shouldNotFailWithCompoundPatientFilter() throws Exception {
		PatientFilter a = new DrugOrderPatientFilter();
		PatientFilter b = new EncounterPatientFilter();
		List<PatientFilter> filters = Arrays.asList(a, b);
		
		CompoundPatientFilter f = new CompoundPatientFilter();
		f.setOperator(BooleanOperator.AND);
		f.setFilters(filters);
		helper(f);
	}
	
	/**
	 * @see {@link InversePatientFilter#filter(Cohort,EvaluationContext)}
	 * 
	 */
	@Test
	@Verifies(value = "should not fail with drug order filter", method = "filter(Cohort,EvaluationContext)")
	public void filter_shouldNotFailWithDrugOrderFilter() throws Exception {
		PatientFilter f = new DrugOrderFilter();
		helper(f);
	}

	/**
	 * @see {@link InversePatientFilter#filter(Cohort,EvaluationContext)}
	 * 
	 */
	@Test
	@Verifies(value = "should not fail with drug order patient filter", method = "filter(Cohort,EvaluationContext)")
	public void filter_shouldNotFailWithDrugOrderPatientFilter() throws Exception {
		PatientFilter f = new DrugOrderPatientFilter();
		helper(f);
	}
	
	/**
	 * @see {@link InversePatientFilter#filter(Cohort,EvaluationContext)}
	 * 
	 */
	@Test
	@Verifies(value = "should not fail with drug order stop filter", method = "filter(Cohort,EvaluationContext)")
	public void filter_shouldNotFailWithDrugOrderStopFilter() throws Exception {
		PatientFilter f = new DrugOrderStopFilter();
		helper(f);
	}
	
	/**
	 * @see {@link InversePatientFilter#filter(Cohort,EvaluationContext)}
	 * 
	 */
	@Test
	@Verifies(value = "should not fail with encounter patient filter", method = "filter(Cohort,EvaluationContext)")
	public void filter_shouldNotFailWithEncounterPatientFilter() throws Exception {
		PatientFilter f = new EncounterPatientFilter();
		helper(f);
	}
	
	/**
	 * @see {@link InversePatientFilter#filter(Cohort,EvaluationContext)}
	 * 
	 */
	@Test
	@Verifies(value = "should not fail with location patient filter", method = "filter(Cohort,EvaluationContext)")
	public void filter_shouldNotFailWithLocationPatientFilter() throws Exception {
		LocationPatientFilter f = new LocationPatientFilter();
		f.setLocation(new Location(1));
		helper(f);
	}
	
	/**
	 * @see {@link InversePatientFilter#filter(Cohort,EvaluationContext)}
	 * 
	 */
	@Test
	@Verifies(value = "should not fail with obs patient filter", method = "filter(Cohort,EvaluationContext)")
	public void filter_shouldNotFailWithObsPatientFilter() throws Exception {
		ObsPatientFilter f = new ObsPatientFilter();
		f.setQuestion(new Concept(5089));
		f.setTimeModifier(TimeModifier.ANY);
		helper(f);
	}
	
	/**
	 * @see {@link InversePatientFilter#filter(Cohort,EvaluationContext)}
	 * 
	 */
	@Test
	@Verifies(value = "should not fail with patient characteristic filter", method = "filter(Cohort,EvaluationContext)")
	public void filter_shouldNotFailWithPatientCharacteristicFilter() throws Exception {
		PatientFilter f = new PatientCharacteristicFilter();
		helper(f);
	}
	
	/**
	 * @see {@link InversePatientFilter#filter(Cohort,EvaluationContext)}
	 * 
	 */
	@Test
	@Verifies(value = "should not fail with person attribute filter", method = "filter(Cohort,EvaluationContext)")
	public void filter_shouldNotFailWithPersonAttributeFilter() throws Exception {
		PatientFilter f = new PersonAttributeFilter();
		helper(f);
	}
	
	/**
	 * @see {@link InversePatientFilter#filter(Cohort,EvaluationContext)}
	 * 
	 */
	@Test
	@Verifies(value = "should not fail with program patient filter", method = "filter(Cohort,EvaluationContext)")
	public void filter_shouldNotFailWithProgramPatientFilter() throws Exception {
		ProgramPatientFilter f = new ProgramPatientFilter();
		f.setProgram(new Program(1));
		helper(f);
	}
	
	/**
	 * @see {@link InversePatientFilter#filter(Cohort,EvaluationContext)}
	 * 
	 */
	@Test
	@Verifies(value = "should not fail with program state patient filter", method = "filter(Cohort,EvaluationContext)")
	public void filter_shouldNotFailWithProgramStatePatientFilter() throws Exception {
		PatientFilter f = new ProgramStatePatientFilter();
		helper(f);
	}
	
	private void helper(PatientFilter f) {
	    InversePatientFilter inv = new InversePatientFilter(f);
	    Cohort normal = f.filter(null, null);
	    Cohort inverse = inv.filter(null, null);
	    Cohort together = Cohort.union(normal, inverse);
	    Cohort everyone = Context.getPatientSetService().getAllPatients();
	    Assert.assertEquals(everyone.size(), normal.size() + inverse.size());
	    assertEqual(everyone, together);
    }

	private void assertEqual(Cohort a, Cohort b) {
	    Assert.assertEquals(a.size(), b.size());
	    SortedSet<Integer> aMembers = new TreeSet<Integer>(a.getMemberIds());
	    SortedSet<Integer> bMembers = new TreeSet<Integer>(b.getMemberIds());
	    Assert.assertEquals(aMembers, bMembers);
    }
}