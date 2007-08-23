package org.openmrs.serial;

public class DefaultNormalizer extends Normalizer
{
    public String toString(Object o) {return o.toString();}
    public Object fromString(Class clazz, String s) { return null;}
}