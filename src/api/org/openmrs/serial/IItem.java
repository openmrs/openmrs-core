package org.openmrs.serial;

import org.openmrs.serial.Item;
import org.openmrs.serial.Record;

public interface IItem 
{
    public Item save(Record xml, Item parent) throws Exception;
    public void load(Record xml, Item me) throws Exception;
}
