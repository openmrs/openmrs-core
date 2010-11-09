package org.openmrs.web.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.LoginCredential;
import org.openmrs.api.db.UserDAO;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class OptionsFormControllerTest extends BaseWebContextSensitiveTest {
	private User user;
	private UserDAO userDao;
	private OptionsFormController controller;

	@Before
	public void setUp() {
		Context.authenticate("admin", "test");		
		user = Context.getAuthenticatedUser();		
		controller = (OptionsFormController) applicationContext.getBean("optionsForm");
		userDao = (UserDAO) applicationContext.getBean("userDAO");
	}
	
	@Test
	public void shouldChangeSecretQuestionAndAnswer() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		request.setParameter("secretQuestionPassword", "test");		
		request.setParameter("secretQuestionNew", "test_question");
		
		String answer = "test_answer";
		request.setParameter("secretAnswerNew", answer);
		request.setParameter("secretAnswerConfirm", answer);
		
		HttpServletResponse response = new MockHttpServletResponse();
		controller.handleRequest(request, response);
		
		LoginCredential loginCredential = userDao.getLoginCredential(user);
		assertEquals(answer, loginCredential.getSecretAnswer());
	}
	
	@Test
	public void shouldRejectEmptySecretAnswer() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		request.setParameter("secretQuestionPassword", "test");		
		request.setParameter("secretQuestionNew", "test_question");
		
		String emptyAnswer = "";
		request.setParameter("secretAnswerNew", emptyAnswer);
		request.setParameter("secretAnswerConfirm", emptyAnswer);
		
		HttpServletResponse response = new MockHttpServletResponse();
		controller.handleRequest(request, response);
		
		LoginCredential loginCredential = userDao.getLoginCredential(user);
		assertNull(loginCredential.getSecretAnswer());
	}
	
	@Test
	public void shouldRejectEmptySecretQuestion() throws Exception {
		LoginCredential loginCredential = userDao.getLoginCredential(user);
		String originalQuestion = loginCredential.getSecretQuestion();

		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		request.setParameter("secretQuestionPassword", "test");		
		request.setParameter("secretQuestionNew", "");
		
		String emptyAnswer = "test_answer";
		request.setParameter("secretAnswerNew", emptyAnswer);
		request.setParameter("secretAnswerConfirm", emptyAnswer);
		
		HttpServletResponse response = new MockHttpServletResponse();
		controller.handleRequest(request, response);
		
		loginCredential = userDao.getLoginCredential(user);
		assertEquals(originalQuestion, loginCredential.getSecretQuestion());
	}
}
