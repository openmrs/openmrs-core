package org.openmrs.serial;

import java.util.HashMap;

/**
* Represents a set of serialized objects with a disposition
*/
public class Package
{
    protected HashMap <String,Record> m_records = new HashMap<String, Record>();

    // it is anticipated that there is either
    // 1) a set of properties on this Package that Records (and Record
    // users) can query to control behavior
    // 2) speciality packages might be derived from this
    // 3) both
    //
    // Example: The base Package can't save it's content records.
    // A network-package might stream to a receiving server those records.
    // A file package might write to disk.

    /** empty constructor
     */
    public Package()
    {
    }

    /** Create a record for writing info
     *@param name
     *@return the record
     *@todo make more reasonable exceptions
     */
    public Record createRecordForWrite(String name) throws Exception
    {
        Record r = Record.getEmpty();
        r.setName(name);
        m_records.put(name, r);
        r.setPackage(this);
        return r;
    }

    /** Create a record from string
     *@param data
     *@return the record
     *@todo make more reasonable exceptions
     */
    public Record createRecordFromString(String data) throws Exception
    {
        Record r = Record.create(new StringBuffer(data));
        m_records.put(r.getName(), r);
        r.setPackage(this);
        return r;
    }

    /** Remove a record from the package.
     *@param name
     *@return the record, may be null if no record by name found
     */
    public Record removeRecord(String name)
    {
        return m_records.remove(name);
    }

    /** Attach a record to a package
     *@param name
     *@return any previous record replaced in the package by this one
     *
     * Any previous package is disconnected from this record (bad idea?)
     */
    public Record attachRecord(Record r)
    {
        r.getPackage().removeRecord(r.getName());
        r.setPackage(this);
        return m_records.put(r.getName(), r);
    }

    /** Save the package as files in a folder
     * @param uri for saving
     * @return success/failure
     * @throws exception
     */
    public boolean savePackage(String path) throws Exception
    {
        throw new Exception("Unsupported function");
    }

}