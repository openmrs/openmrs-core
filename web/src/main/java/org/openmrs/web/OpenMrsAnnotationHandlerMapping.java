package org.openmrs.web;

import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

public class OpenMrsAnnotationHandlerMapping extends DefaultAnnotationHandlerMapping {
	
	@Override
	protected HandlerExecutionChain getHandlerExecutionChain(Object handler, HttpServletRequest request) {
		HandlerExecutionChain chain = super.getHandlerExecutionChain(handler, request);
		HandlerInterceptor[] interceptors = detectInterceptors(chain.getHandler().getClass());
		chain.addInterceptors(interceptors);
		return chain;
	}
	
	protected HandlerInterceptor[] detectInterceptors(Class handlerClass) {
		
		Interceptors interceptorAnnot = AnnotationUtils.findAnnotation(handlerClass, Interceptors.class);
		List interceptors = new ArrayList();
		if (interceptorAnnot != null) {
			Class[] interceptorClasses = interceptorAnnot.value();
			if (interceptorClasses != null) {
				for (Class interceptorClass : interceptorClasses) {
					if (!HandlerInterceptor.class.isAssignableFrom(interceptorClass)) {
						raiseIllegalInterceptorValue(handlerClass, interceptorClass);
					}
					interceptors.add((HandlerInterceptor) BeanUtils.instantiateClass(interceptorClass));
				}
			}
		}
		return (HandlerInterceptor[]) interceptors.toArray(new HandlerInterceptor[0]);
	}
	
	protected void raiseIllegalInterceptorValue(Class handlerClass, Class interceptorClass) {
		throw new IllegalArgumentException(interceptorClass + " specified on " + handlerClass + " does not implement "
		        + HandlerInterceptor.class.getName());
		
	}
}
