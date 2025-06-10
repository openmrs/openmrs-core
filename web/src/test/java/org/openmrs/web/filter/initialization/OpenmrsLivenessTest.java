package org.openmrs.web.filter.initialization;

import static org.hamcrest.MatcherAssert.assertThat;

import javax.servlet.ServletException;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.web.filter.StartupFilter;
import org.openmrs.web.filter.startuperror.StartupErrorFilter;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.hamcrest.CoreMatchers.is;


public class OpenmrsLivenessTest {
	
	private StartupFilter startupFilter;
	
	@BeforeEach
	public void setUp() {
		startupFilter = new StartupErrorFilter();
	}
	
	@Test
	public void shouldReturnServiceUnavailableIfOpenmrsNotStarted() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain mockFilterChain=new MockFilterChain();
		request.setMethod("GET");
		request.setServletPath("/health/alive");
		startupFilter.doFilter(request,response,mockFilterChain);
		assertThat(response.getStatus(), is(HttpStatus.SERVICE_UNAVAILABLE.value()));
	}
	
}
