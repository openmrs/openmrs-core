package org.openmrs.synchronization.engine;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Stub to be replaced by real serializer.
 */
public class SyncSerializer {

    public SyncSerializer() {}
    
    static public byte [] Serialize(Serializable s) throws IOException {
        ByteArrayOutputStream bstream = null;
        ObjectOutputStream ostream = null;
        
        bstream = new ByteArrayOutputStream();
        ostream = new ObjectOutputStream(bstream);
        ostream.writeObject(s);
        ostream.flush();
        ostream.close();
        bstream.close();
        
        return bstream.toByteArray(); 
    }

    static public Object Deserialize(byte[] state) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bstream = null;
        ObjectInputStream istream = null;
        Object o = null;
        
        bstream = new ByteArrayInputStream(state);
        istream = new ObjectInputStream(bstream);
        o = istream.readObject();
        istream.close();
        bstream.close();
        
        return o; 
    }

}
