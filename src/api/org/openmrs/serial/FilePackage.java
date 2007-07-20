package org.openmrs.serial;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
* Represents a set of serialized objects with a disposition
*/
public class FilePackage extends Package
{
    public FilePackage()
    {
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
			System.out.println("writeFile exception " + e.getMessage()
									+ " on file " + fname);
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
