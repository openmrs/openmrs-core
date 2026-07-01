/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.aop;

import java.util.Optional;
import java.util.function.Function;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.openmrs.OpenmrsObject;
import org.openmrs.aop.event.PurgeServiceEvent;
import org.openmrs.aop.event.RetireServiceEvent;
import org.openmrs.aop.event.SaveServiceEvent;
import org.openmrs.aop.event.UnretireServiceEvent;
import org.openmrs.aop.event.UnvoidServiceEvent;
import org.openmrs.aop.event.VoidServiceEvent;
import org.openmrs.event.EntityEvent;
import org.openmrs.event.EventPublisher;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Publishes {@link SaveServiceEvent}, {@link VoidServiceEvent}, {@link UnvoidServiceEvent},
 * {@link RetireServiceEvent}, and {@link UnretireServiceEvent}.
 * <p>
 * It is registered after transaction advice so that
 * {@link org.openmrs.event.outbox.OutboxEventListener},
 * {@link org.springframework.transaction.event.TransactionalEventListener} and
 * {@link org.springframework.context.event.EventListener} can run in the same transaction.
 *
 * @since 2.9.0
 */
@Aspect
@Order(50) // Guarantees execution after transaction advice
@Component
public class OpenmrsServiceEventAdvice {

	private final EventPublisher eventPublisher;

	public OpenmrsServiceEventAdvice(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	/**
	 * This intercepts method executions starting with "save" on OpenmrsService.
	 */
	@Around("(execution(* *.save*(..)) || execution(* *.create*(..))) && target(org.openmrs.api.OpenmrsService)")
	public Object interceptSaveOrCreate(ProceedingJoinPoint pjp) throws Throwable {
		return intercept(pjp, SaveServiceEvent::new);
	}

	/**
	 * This intercepts method executions starting with "void" on OpenmrsService.
	 */
	@Around("execution(* *.void*(..)) && target(org.openmrs.api.OpenmrsService)")
	public Object interceptVoid(ProceedingJoinPoint pjp) throws Throwable {
		return intercept(pjp, VoidServiceEvent::new);
	}

	/**
	 * This intercepts method executions starting with "unvoid" on OpenmrsService.
	 */
	@Around("execution(* *.unvoid*(..)) && target(org.openmrs.api.OpenmrsService)")
	public Object interceptUnvoid(ProceedingJoinPoint pjp) throws Throwable {
		return intercept(pjp, UnvoidServiceEvent::new);
	}

	/**
	 * This intercepts method executions starting with "retire" on OpenmrsService.
	 */
	@Around("execution(* *.retire*(..)) && target(org.openmrs.api.OpenmrsService)")
	public Object interceptRetire(ProceedingJoinPoint pjp) throws Throwable {
		return intercept(pjp, RetireServiceEvent::new);
	}

	/**
	 * This intercepts method executions starting with "unretire" on OpenmrsService.
	 */
	@Around("execution(* *.unretire*(..)) && target(org.openmrs.api.OpenmrsService)")
	public Object interceptUnretire(ProceedingJoinPoint pjp) throws Throwable {
		return intercept(pjp, UnretireServiceEvent::new);
	}

	/**
	 * This intercepts method executions starting with "purge" on OpenmrsService.
	 */
	@Around("execution(* *.purge*(..)) && target(org.openmrs.api.OpenmrsService)")
	public Object interceptPurge(ProceedingJoinPoint pjp) throws Throwable {
		return intercept(pjp, PurgeServiceEvent::new);
	}

	private Object intercept(ProceedingJoinPoint pjp, Function<OpenmrsObject, EntityEvent<?>> eventProducer)
	        throws Throwable {
		// Do not apply to proxies created by TransactionProxyFactoryBean (legacy) in addition to transactional advice so
		// that events are not emitted twice.
		if (AopUtils.isAopProxy(pjp.getTarget())) {
			return pjp.proceed();
		}

		getOpenmrsObjectArgument(pjp).ifPresent(object -> eventPublisher.publishEvent(eventProducer.apply(object)));
		return pjp.proceed();
	}

	private Optional<OpenmrsObject> getOpenmrsObjectArgument(ProceedingJoinPoint pjp) {
		if (pjp.getArgs() != null && pjp.getArgs().length > 0 && pjp.getArgs()[0] instanceof OpenmrsObject) {
			return Optional.of((OpenmrsObject) pjp.getArgs()[0]);
		} else {
			return Optional.empty();
		}
	}
}
