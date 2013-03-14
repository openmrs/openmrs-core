package org.openmrs.web;

import org.apache.xerces.impl.dv.util.Base64;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;

public class BasicAuthInterceptor implements HandlerInterceptor {
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		// skip if we're already authenticated, or it's not an HTTP request
		if (!Context.isAuthenticated() && request instanceof HttpServletRequest) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			String basicAuth = httpRequest.getHeader("Authorization");
			if (basicAuth != null) {
				// this is "Basic ${base64encode(username + ":" + password)}"
				try {
					basicAuth = basicAuth.substring(6); // remove the leading "Basic "
					String decoded = new String(Base64.decode(basicAuth), Charset.forName("UTF-8"));
					String[] userAndPass = decoded.split(":");
					Context.authenticate(userAndPass[0], userAndPass[1]);
				}
				catch (ContextAuthenticationException ex) {
					requestAuthentication(response);
					return false;
				}
			} else {
				requestAuthentication(response);
				return false;
			}
		}
		return true;
	}

	private void requestAuthentication(HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.addHeader("WWW-Authenticate", "Basic realm=\"OpenMRS\"");
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		// do nothing
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		// do nothing
	}
}
