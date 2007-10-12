/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.synchronization.engine;

import java.util.Comparator;

/**
 * Sync point: abstract concept representing a point in a continuous series.
 * As a result, all it needs to establish is order based on type T. Practically
 * T is either Long (to represent sequence) or timestamp.
 *
 */
public class SyncPoint<T> implements Comparator<T> {

    private T value;
  
    public SyncPoint(){}
    
    public SyncPoint(T value) {
        this.value = value;
    }
    
    public T getValue() {
        return value;
    }
  
    public void setValue(T value) {
        this.value = value;
    }
    
    public int compare(T v1,T v2) {
        if (v1 instanceof Long && v2 instanceof Long) {
            if ((Long)v1 < (Long)v2)
                return -1;
            else if ((Long)v1 > (Long)v2)
                return 1;
            else
                return 0;
        }
        else
            throw new ClassCastException();
    }
    
}
