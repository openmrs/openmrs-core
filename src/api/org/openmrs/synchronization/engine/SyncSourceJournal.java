package org.openmrs.synchronization.engine;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openmrs.api.context.Context;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.SynchronizationService;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.serial.TimestampNormalizer;
/**
 * SyncSource to sync OpenMRS tables based on last_changed_local.
 */
public class SyncSourceJournal implements SyncSource {

    private final Log log = LogFactory.getLog(getClass());

    
    // constructor(s)
    public SyncSourceJournal() {
    }

    // properties

    // Public Methods

    /**
     * Std. method for retrieving last sync local; uses global prop, note in case of journal sync, we could just
     * infer this from the status of the journal: last sync local is the date of the last entry with status of
     * pending, or new
     */
    public SyncPoint<Date> getLastSyncLocal() {
        Date val = null;
        String sVal = Context.getSynchronizationService().getGlobalProperty(
                SyncConstants.LAST_SYNC_LOCAL);
        try {
            val = (sVal == null || "".equals(sVal)) ? null : new SimpleDateFormat(TimestampNormalizer.DATETIME_MASK)
                    .parse(sVal);
        } catch (ParseException e) {
            log.error("Error DateFormat parsing " + sVal, e);
            throw new SyncException("Error DateFormat parsing " + sVal, e);
        }
        return new SyncPoint<Date>(val);
    }
    public void setLastSyncLocal(SyncPoint p) {
        String sVal = null;

        sVal = (p.getValue() == null || "".equals(sVal)) ? null : new SimpleDateFormat(TimestampNormalizer.DATETIME_MASK)
                .format(p.getValue());
        // use getSynchronizationService to avoid logging this changes to the journal
        Context.getSynchronizationService().setGlobalProperty(
                SyncConstants.LAST_SYNC_LOCAL, sVal);

        return;
    }

    /*
     * Last sync remote: timestamp of the last data *received* from parent
     */
    public SyncPoint<Date> getLastSyncRemote() {
        Date val = null;

        String sVal = Context.getSynchronizationService().getGlobalProperty(
                SyncConstants.LAST_SYNC_REMOTE);
        try {

            val = (sVal == null || "".equals(sVal)) ? null : new SimpleDateFormat(TimestampNormalizer.DATETIME_MASK)
                    .parse(sVal);
        } catch (ParseException e) {
            log.error("error DateFormat parsing " + sVal, e);
        }
        return new SyncPoint<Date>(val);
    }
    public void setLastSyncRemote(SyncPoint p) {
        String sVal = null;

        sVal = (p.getValue() == null || "".equals(sVal)) ? null : new SimpleDateFormat(TimestampNormalizer.DATETIME_MASK)
                .format(p.getValue());
        // use getSynchronizationService to avoid logging this changes to the journal
        Context.getSynchronizationService().setGlobalProperty( 
                SyncConstants.LAST_SYNC_REMOTE, sVal);

        return;
    }

    // gets the 'next' SyncPoint: in case of timestamp implementation, just get current date/time
    public SyncPoint<Date> moveSyncPoint() {
        
        return new SyncPoint<Date>(new Date());
    }

    // no op: journal has delete records; get 'changed' returns deleted also
    public List<SyncRecord> getDeleted(SyncPoint from, SyncPoint to)
            throws SyncException {
        List<SyncRecord> deleted = new ArrayList<SyncRecord>();

        return deleted;
    }

    // state-based version
    // no op: journal has delete records; get 'changed' returns deleted also
    public List<SyncRecord> getDeleted()
            throws SyncException {
        List<SyncRecord> deleted = new ArrayList<SyncRecord>();

        return deleted;
    }

    // retrieve journal records > 'from' && <= 'to' && record status = 'new' or
    // 'failed'
    public List<SyncRecord> getChanged(SyncPoint from, SyncPoint to)
            throws SyncException {
        List<SyncRecord> changed = new ArrayList<SyncRecord>();

        try {

            Date fromDate = (Date) from.getValue();
            Date toDate = (Date) to.getValue();
            
            //handle nulls
            if (fromDate == null) fromDate = new Date(0L);
            if (toDate == null) toDate = new Date(0L);
            
            SynchronizationService syncService = Context.getSynchronizationService();           
            changed = syncService.getSyncRecordsBetween(fromDate, toDate);

        } catch (Exception e) {
            // TODO
            log.error("error in getChanged ", e);
        }

        return changed;
    }

    // state-based version
    // retrieve journal records > 'from' && <= 'to' && record status = 'new' or
    // 'failed'
    public List<SyncRecord> getChanged() throws SyncException {
        List<SyncRecord> changed = new ArrayList<SyncRecord>();

        try {
            SyncRecordState[] states = {
            	SyncRecordState.NEW,	
            	SyncRecordState.PENDING_SEND,
            	SyncRecordState.SEND_FAILED,
            	SyncRecordState.SENT
            };
            SynchronizationService syncService = Context.getSynchronizationService();           
            changed = syncService.getSyncRecords(states);

        } catch (Exception e) {
            // TODO
            log.error("error in getChanged ", e);
        }

        return changed;
    }
    
    /*
     * no-op for journal sync -- all changes (deletes, inserts, updates are received in transactional order
     * via applyChanged
     */
    public void applyDeleted(List<SyncRecord> records) throws SyncException {
        
        return;
    }
    
    public void applyChanged(List<SyncRecord> records) throws SyncException {
        
        //TODO - process the changeset
        
        return;
    }

}
