package org.openmrs.serial;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class Normalizer
{
    protected final Log log = LogFactory.getLog(Normalizer.class);

    public abstract String toString(Object o);
    public abstract void fromString(Object o, String s);
}
