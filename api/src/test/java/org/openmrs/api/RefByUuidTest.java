/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import org.junit.jupiter.api.Test;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class RefByUuidTest extends BaseContextSensitiveTest {

    @Autowired
    private List<RefByUuid> refByUuids;

    @Test
    public void getRefByUuid_shouldSupportAllGetByUuidReturnTypes() {
        for (RefByUuid refByUuid : refByUuids) {
            // setup
            Class<?> clazz = refByUuid.getClass();
            Method[] methods = clazz.getMethods();

            for (Method method : methods) {
                if (isGetByUuidMethod(method)) {
                    Class<?> returnType = method.getReturnType();
                    String dummyUuid = "some-random-uuid";

                    try {
                        // replay
                        Object result = refByUuid.getRefByUuid(returnType, dummyUuid);

                        // verify that no exception was thrown
                        assertTrue(true, "Expected null or valid instance for type: " + returnType.getName());
                    } catch (Exception e) {
                        fail("getRefByUuid threw for type: " + returnType.getName() + " — " + e.getMessage());
                    }
                }
            }
        }
    }

    @Test
    public void getRefByUuid_shouldThrowAPIExceptionForUnsuppportedType() {
        for (RefByUuid refByUuid : refByUuids) {
            // setup
            String dummyUuid = "some-random-uuid";

            // verify
            assertThrows(APIException.class, () -> {
                refByUuid.getRefByUuid(UnsupportedType.class, dummyUuid);}, 
                "Expected APIException for unsupported type: " + UnsupportedType.class.getName() + ", on RefByUuid class : " + AopUtils.getTargetClass(refByUuid).getSimpleName());
        }
    }

    @Test
    public void getRefTypes_shouldDeclareAllRefTypesFoundViaReflection() {
        for (RefByUuid refByUuid : refByUuids) {
            // setup
            Set<Class<?>> discoveredTypes = Arrays.stream(refByUuid.getClass().getMethods())
                    .filter(this::isGetByUuidMethod)
                    .map(Method::getReturnType)
                    .collect(Collectors.toSet());

            // replay
            List<Class<?>> declaredTypes = refByUuid.getRefTypes();
            Set<Class<?>> declaredTypeSet = Set.copyOf(declaredTypes);

            // verify
            assertEquals(discoveredTypes, declaredTypeSet,
                    () -> "Mismatch in declared vs discovered types for: " + refByUuid.getClass().getName()
                        + "\nDiscovered: " + discoveredTypes
                        + "\nDeclared: " + declaredTypeSet);
        }
    }

    private boolean isGetByUuidMethod(Method method) {
        return method.getName().startsWith("get")
                && method.getName().endsWith("ByUuid")
                && method.getParameterCount() == 1
                && method.getParameterTypes()[0] == String.class;
    }
    
    private class UnsupportedType {
    
    }
}
