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
package org.openmrs.serialization;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
* Represents a set of serialized objects with a disposition
*/
public class FilePackage extends Package
{
    private static Log log = LogFactory.getLog(FilePackage.class);
    private String contentsBeforeWrite;
    
    public FilePackage()
    {
    }

    public String getContentsBeforeWrite() {
    	return contentsBeforeWrite;
    }

	public void setContentsBeforeWrite(String contentsBeforeWrite) {
    	this.contentsBeforeWrite = contentsBeforeWrite;
    }

	/** Save the package as files in a folder
     * @param root folder which the package will be saved within
     * @return success/failure
     * @throws io, other exceptions
     */
    public boolean savePackage(String path) throws IOException, Exception
    {
        boolean result = false;

        File f = new File(path);
        if (!f.isDirectory())
        {
            File p = new File(path);
            p.mkdirs();
        }

		Iterator<Map.Entry<String, Record>> it = m_records.entrySet().iterator();
        while (it!=null && it.hasNext())
        {
            Map.Entry<String, Record> me = it.next();
            Record r = (Record)me.getValue();

            byte bits[] = r.toString().getBytes();
            writeFile(path + "/" + r.getName() + ".xml", bits, bits.length);
        }

        return result;
    }

    public String savePackage(String path, boolean writeFileToo) throws IOException, Exception
    {
        String result = null;

        if ( writeFileToo ) {
            File f = new File(path);
            if (!f.isDirectory())
            {
                File p = new File(path);
                p.mkdirs();
            }
        }

		Iterator<Map.Entry<String, Record>> it = m_records.entrySet().iterator();
        while (it!=null && it.hasNext())
        {
            Map.Entry<String, Record> me = it.next();
            Record r = (Record)me.getValue();

            result = r.toString();
            
            
            if ( writeFileToo ) {
                byte bits[] = result.getBytes();
                writeFile(path + "/" + r.getName() + ".xml", bits, bits.length);
            }
        }

        return result;
    }

	private boolean writeFile(String fname, byte bits[], int len)
	{
		FileOutputStream fos = null;
		DataOutputStream dos = null;

		try {
			// IO time
			File f = new File(fname);
			fos = new FileOutputStream(f);
			dos = new DataOutputStream(fos);
			dos.write(bits, 0, len);
		}
		catch (Exception e) {
			log.error("Could not write file: " + fname, e);
			return false;
		}
		finally {
			try {
				dos.close();
			} catch (Exception ee) {}
			try {
				fos.close();
			} catch (Exception ee) {}
		}
		return true;
	}

}
