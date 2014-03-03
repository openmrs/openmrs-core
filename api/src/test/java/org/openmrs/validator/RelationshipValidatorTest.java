package org.openmrs.validator;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Relationship;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.validation.MapBindingResult;

public class RelationshipValidatorTest extends BaseContextSensitiveTest {
	
	@Test
	public void validate_shouldFailIf_StartDate_After_EndDate() throws Exception {
		Relationship r = new Relationship(1);
		r.setStartDate(Context.getDateFormat().parse("18/02/2012"));
		r.setEndDate(Context.getDateFormat().parse("18/02/2001"));
		Map<String, String> map = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(map, Relationship.class.getName());
		new RelationshipValidator().validate(r, errors);
		Assert.assertEquals(true, errors.hasErrors());
	}
}
