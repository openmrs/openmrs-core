
package org.openmrs.synchronization.engine;

import java.util.List;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.Person;
import org.openmrs.api.PersonService; 
import org.openmrs.api.context.Context;
import org.openmrs.api.AdministrationService; 

/**
 * SyncSource to sync openmrs tables based on last_changed_local.
 */
public class NoHistorySyncSource implements SyncSource {

    private final Log log = LogFactory.getLog(getClass());
    
    // constructor(s)
    public NoHistorySyncSource() {}

    // properties

    //Public Methods
    //sync point helpers
    public SyncPoint<Long> getLastSyncLocal() {
        long val = 0L;
        
        String sVal = Context.getAdministrationService().getGlobalProperty("LastSyncLocal");  
        val = Long.parseLong(sVal);
        return new SyncPoint<Long>(val);
    }
    public void setLastSyncLocal(SyncPoint p) {
        //TODO
        return;
    }
    public SyncPoint<Long> getLastSyncRemote() {
        long val = 0L;

        String sVal = Context.getAdministrationService().getGlobalProperty("LastSyncRemote");
        val = Long.parseLong(sVal);
        return new SyncPoint<Long>(val);
    }
    public void setLastSyncRemote(SyncPoint p) {
        //TODO
        return;
    }
    
    //gets the 'next' syncpoint: in case of sequence implementation, just call: getnextval();
    //for timestamp, just do get current()
    public SyncPoint<Long> moveSyncPoint() {
        
        List<List<Object>> resultset = Context.getAdministrationService().executeSQL("select sequence_nextval('seq_changed_local') from dual",true);
        Object o = resultset.get(0).get(0);
        if (!(o instanceof Long ))
            throw new ClassCastException();
        
        return new SyncPoint<Long>((Long)o);
    }
          
    //change set methods
    public List<SyncItem> getDeleted(SyncPoint from , SyncPoint to) throws SyncException {
        
        List<SyncItem> deleted = new ArrayList<SyncItem>();
   
        
        return deleted;
    }
    
    public List<SyncItem> getChanged(SyncPoint from , SyncPoint to) throws SyncException {
        
        List<SyncItem> changed = new ArrayList<SyncItem>();
        
        //TODO: temporarily get patients and persons that have changed
        
        try {
   
            long fromL = (Long)from.getValue();
            long toL = (Long)to.getValue();
            
            //get patients
            List<Patient> patients = Context.getPatientService().getPatientsByLastChangedLocal(fromL, toL);
            
            //get persons
            //TODO
            
            //serialize into syncItems
            for(Patient p: patients) {
                SyncItem item = new SyncItemImpl();
                item.setKey(new SyncItemKey(p.getGuid()));
                item.setByteContent(SyncSerializer.Serialize(p));
                item.setState(SyncItem.SyncItemState.UPDATED);
                changed.add(item);
            }
        }
        catch(Exception e) {
            log.error("error in getChanged ",e);
        }
        
        return changed;
    }
    
    public SyncItem addSyncItem(SyncItem syncInstance) throws SyncException {
        
        return syncInstance;
    }
    
    public SyncItem updateSyncItem(SyncItem syncInstance) throws SyncException  {
        
        return syncInstance;
      }
    
    public void removeSyncItem(SyncItemKey itemKey) throws SyncException {
       
       return;
    }

   public void beginSync() throws SyncException {
       //TODO
   }

   public void endSync() throws SyncException {
       //TODO
   }

   /**
   * Commit changes on the sync
   */
   public void commitSync() throws SyncException {
       //TODO
   }
        
}
