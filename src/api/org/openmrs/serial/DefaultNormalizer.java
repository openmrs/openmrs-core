package org.openmrs.serial;

public class DefaultNormalizer extends Normalizer
{
    public String toString(Object o) {return o.toString();}
    public void fromString(Object o, String s) {}
}