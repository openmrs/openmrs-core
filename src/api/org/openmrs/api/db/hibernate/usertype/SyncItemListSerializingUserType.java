package org.openmrs.api.db.hibernate.usertype;

import java.io.Serializable;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
import org.openmrs.synchronization.engine.SyncItem;
import org.openmrs.serial.FilePackage;
import org.openmrs.serial.Item;
import org.openmrs.serial.IItem;
import org.openmrs.serial.Package;
import org.openmrs.serial.Record;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.load.Persister;

public class SyncItemListSerializingUserType implements UserType {

    private static final int[] SQL_TYPES = {Types.CLOB};
    
    /**
     * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable, java.lang.Object)
     */
    public Object assemble(Serializable cached, Object owner)
            throws HibernateException {
        return cached;
    }

    /**
     * @see org.hibernate.usertype.UserType#deepCopy(java.lang.Object)
     */
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    /**
     * @see org.hibernate.usertype.UserType#disassemble(java.lang.Object)
     */
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    /**
     * @see org.hibernate.usertype.UserType#equals(java.lang.Object, java.lang.Object)
     */
    public boolean equals(Object x, Object y) throws HibernateException {
        //TODO: might need something more detailed here; p206
        return x == y;
    }

    /**
     * @see org.hibernate.usertype.UserType#hashCode(java.lang.Object)
     */
    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    /**
     * @see org.hibernate.usertype.UserType#isMutable()
     */
    public boolean isMutable() {
        return false;
    }

    /**
     * @see org.hibernate.usertype.UserType#nullSafeGet(java.sql.ResultSet, java.lang.String[], java.lang.Object)
     */
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
            throws HibernateException, SQLException {
        if (rs.wasNull()) {
            return null;
        } else {
            Clob clob = rs.getClob(names[0]);
            
            if (clob == null) {
                return null;
            } else {
                //FIXME: length conversion from long to int might be a problem in theory. UTF8 as well. Better off with the Reader?
                String content = clob.getSubString(1, (int)clob.length());
             
                SyncItemList list = new SyncItemList();
                
                /* SIMPLE code
                Serializer serializer = new Persister();
                SyncItemList list = new SyncItemList();
                try {
                    list = serializer.read(list, content);
                } catch (Exception e) {
                    throw new HibernateException("Could not deserialize object from storage", e);
                }
                */
                
                if (list != null) {
                    return list.getItems();
                } else {
                    return null;
                }
            }
        }
    }

    /**
     * @see org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement, java.lang.Object, int)
     */
    @SuppressWarnings("unchecked")
    public void nullSafeSet(PreparedStatement ps, Object value, int index)
            throws HibernateException, SQLException {
        if (value == null) {
            ps.setNull(index, Types.CLOB);
        } else {
            SyncItemList items = new SyncItemList((List<SyncItem>) value);
                     
            /* Simple code 
            Serializer serializer = new Persister();
            StringWriter writer = new StringWriter();
            try {
                serializer.write(items, writer);
            } catch (Exception e) {
                throw new HibernateException("Failed to serialize object for storage", e);
            }
            */

            try {                
                FilePackage pkg = new FilePackage();
                Record record = pkg.createRecordForWrite("SyncItemList");
                Item top = record.getRootItem();
                ((IItem)items).save(record, top);
                
                ps.setClob(index, Hibernate.createClob(record.toStringAsDocumentFragement()));
            }
            catch(Exception e) {
                throw new HibernateException("Failed to serialize object for storage", e);
            }
        }
    }

    /**
     * @see org.hibernate.usertype.UserType#replace(java.lang.Object, java.lang.Object, java.lang.Object)
     */
    public Object replace(Object original, Object target, Object owner)
            throws HibernateException {
        return original;
    }

    /**
     * @see org.hibernate.usertype.UserType#returnedClass()
     */
    @SuppressWarnings("unchecked")
    public Class returnedClass() {
        return List.class;
    }

    /**
     * @see org.hibernate.usertype.UserType#sqlTypes()
     */
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    // Workaround for missing access to SyncRecord so that Simple can serialize the list
    @Root(name="SyncItems")
    private class SyncItemList implements IItem {
        @SuppressWarnings("unused")
        @ElementList(inline=true)
        private List<SyncItem> items = null;
        
        public SyncItemList() { }
        
        public SyncItemList(List<SyncItem> items) {
            this.items = items;
        }
        
        public List<SyncItem> getItems() {
            return items;
        }

        public Item save(Record xml, Item parent) throws Exception {
                        
            //serialize the collection - write directly to parent
            Item itemsCollection = xml.createItem(parent,"items");
            if (items != null)
            {
                Iterator<SyncItem> iterator = items.iterator();
                while (iterator.hasNext()) {
                    iterator.next().save(xml,itemsCollection);
                }
            };

            return parent;
        }

        public void load(Record xml, Item me) throws Exception {
            // TODO
        }
        
    }
}
