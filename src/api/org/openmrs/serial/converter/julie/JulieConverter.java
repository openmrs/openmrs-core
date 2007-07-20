/**
 * Auto generated file comment
 */
package org.openmrs.serial.converter.julie;

import org.openmrs.serial.Item;
import org.openmrs.serial.Record;

/**
 *
 */
public interface JulieConverter {

    public Item save(Record xml, Item parent) throws Exception;
    public void load(Record xml, Item me) throws Exception;
}
