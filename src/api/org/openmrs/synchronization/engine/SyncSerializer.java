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
