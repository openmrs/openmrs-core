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
