package org.openmrs.test.jupiter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.api.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { BaseTestConfig.class })
@TestPropertySource("classpath:common.properties")
@ActiveProfiles("test")
public class PatientServiceTest {
	
	@Autowired
	PatientService patientService;
	
	@Before
	public void init() throws Exception {
	}
	
	@Test
	public void getAllPatients_shouldGetAllPatients() throws Exception {
		Assert.assertEquals(1, patientService.getAllPatients().size());
	}
	
}
