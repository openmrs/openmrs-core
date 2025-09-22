/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import org.openmrs.api.DomainService;
import org.openmrs.api.OpenmrsService;
import org.openmrs.api.RefByUuid;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.serialization.UuidReferenceModule;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@code DomainServiceImpl}, an implementaion of {@code DomainService}, is a central utility that 
 * provides runtime lookup of {@link org.openmrs.OpenmrsObject} instances using their UUIDs.
 * <p>
 * It dynamically inspects all registered {@link OpenmrsService} beans to identify
 * public methods of the form {@code getXByUuid(String uuid)}, and maps their
 * return types to internal {@link DomainFetcher}s.
 * </p>
 *
 * This is particularly useful for deserialization scenarios where a UUID string
 * needs to be resolved into a fully initialized domain object (e.g., during JSON parsing).
 *
 * @see DomainService
 * @see UuidReferenceModule
 */

 @Service("domainService")
@Transactional
public class DomainServiceImpl extends BaseOpenmrsService implements DomainService {

    private final Map<Class<?>, DomainFetcher> domainFetchers = new HashMap<>();

    /**
     * Constructs the {@code DomainService} and scans the dynamically provided OpenMRS services
     * for eligible {@code getXByUuid(String)} methods to populate the internal lookup.
     *
     * @param serviceContext application context providing available {@code OpenmrsService} implementations
     */
    @Autowired
    public DomainServiceImpl(@Qualifier ("serviceContext") ServiceContext serviceContext) {
        List<OpenmrsService> services = serviceContext.getRegisteredComponents(OpenmrsService.class);
        for (OpenmrsService service : services) {
            if (this.getClass().isAssignableFrom(service.getClass())) {
                continue;
            }
            for (Method method : service.getClass().getMethods()) {
                if (method.getName().startsWith("get") &&
                    method.getName().endsWith("ByUuid") &&
                    method.getParameterCount() == 1 &&
                    method.getParameterTypes()[0].equals(String.class)) {

                    Class<?> domainKey = method.getReturnType();
                    if (!domainFetchers.containsKey(domainKey)) {
                        domainFetchers.put(domainKey, new DomainFetcher(method.getReturnType(), service, method));
                    }
                }
            }
        }
    }

    /**
     * Retrieves an object of the given type using its UUID.
     *
     * @param type the expected type of the domain object
     * @param uuid the UUID of the domain object to fetch
     * @param <T>  a type that matches the domain object class
     * @return an instance of the requested type
     * @throws RuntimeException if no fetcher exists for the given type
     * @throws ClassCastException if the fetched object is not of the expected type
     */
    @Transactional(readOnly = true)
    public <T> T fetchByUuid(Class<T> type, String uuid) {
        Object result = null;

        DomainFetcher fetcher = domainFetchers.get(type);
        if (fetcher != null) {
            result = fetcher.fetch(uuid);
        } else {
            throw new RuntimeException("No suitable fetcher found for domain: " + type);
        }

        if (!type.isInstance(result)) {
            throw new ClassCastException("Expected: " + type + ", but got: " + (result != null ? result.getClass() : "null"));
        }
        return type.cast(result);
    }

    /**
     * Returns a list of all domain types that are currently registered and
     * resolvable by this service.
     *
     * @return a list of registered domain classes
     */
    public List<Class<?>> getDomainTypes() {
        Set<Class<?>> types = new HashSet<>();
        for (DomainFetcher fetcher : domainFetchers.values()) {
            types.add(fetcher.getReturnType());
        }
        return new ArrayList<>(types);
    }

    /**
     * Internal helper that holds metadata and invocation logic for a specific
     * {@code getXByUuid(String)} method.
     */
    private class DomainFetcher {

        private final Class<?> returnType;
        private final OpenmrsService service;
        private final Method method;

        public DomainFetcher(Class<?> returnType, OpenmrsService service, Method method) {
            this.returnType = returnType;
            this.service = service;
            this.method = method;
        }

        /**
         * Returns the type of object this fetcher is responsible for.
         *
         * @return the domain class handled by this fetcher
         */
        public Class<?> getReturnType() {
            return returnType;
        }

        /**
         * Returns the service that provides the fetch logic.
         *
         * @return the underlying {@code OpenmrsService}
         */
        public OpenmrsService getService() {
            return service;
        }

        /**
         * Returns the actual method used to fetch the object by UUID.
         *
         * @return the fetch method
         */
        public Method getMethod() {
            return method;
        }

        /**
         * Fetches a domain object by its UUID.
         *
         * <p>If the service implements {@link RefByUuid}, delegates to 
         * {@link RefByUuid#getRefByUuid(Class, String)}; otherwise, invokes the 
         * underlying method reflectively.</p>
         *
         * @param uuid the UUID of the object to fetch
         * @return the resolved object, or {@code null} if not found
         * @throws RuntimeException if the invocation fails
         */
        public Object fetch(String uuid) {
            try {
                if (RefByUuid.class.isAssignableFrom(getService().getClass())) {
                    return ((RefByUuid) getService()).getRefByUuid(getReturnType(), uuid);
                } else {
                    return getMethod().invoke(service, uuid);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch " + returnType.getSimpleName() + " by UUID", e);
            }
        }

        @Override
        public String toString() {

            return "DomainFetcher{" +
                    "returnType=" + (returnType != null ? returnType.getSimpleName() : "null") +
                    ", service=" + (service != null ? AopUtils.getTargetClass(service).getSimpleName() : "null") +
                    ", method=" + (method != null ? method.getName() : "null") +
                    '}';
        }
    }
}