/**
 * Auto generated file comment
 */
package org.openmrs.api.db.hibernate;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

/**
 * Empty, non-synchronizing interceptor to replace synchronizing interceptor from SessionFactory
 */
public class HibernateNonSynchronizingInterceptor extends EmptyInterceptor {

    protected final Log log = LogFactory.getLog(HibernateNonSynchronizingInterceptor.class);
    
    @Override
    public void afterTransactionBegin(Transaction transaction) {
        log.debug("afterTransactionBegin (Non-Synchronizing)" + transaction);
        super.afterTransactionBegin(transaction);
    }

    @Override
    public void afterTransactionCompletion(Transaction transaction) {
        log.debug("afterTransactionCompletion (Non-Synchronizing)" + transaction);
        super.afterTransactionCompletion(transaction);
    }

    @Override
    public void beforeTransactionCompletion(Transaction transaction) {
        log.debug("beforeTransactionCompletion (Non-Synchronizing)" + transaction);
        super.beforeTransactionCompletion(transaction);
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id,
            Object[] currentState, Object[] aobj, String[] as, Type[] atype) {
        log.debug("onFlushDirty (Non-Synchronizing)" + as);
        return super.onFlushDirty(entity, id, currentState, aobj, as, atype);
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state,
            String[] as, Type[] atype) {
        log.debug("onSave (Non-Synchronizing)" + as);
        return super.onSave(entity, id, state, as, atype);
    }

}
