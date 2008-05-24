package org.openmrs.test;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.ConnectionReleaseMode;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.jdbc.JDBCContext;
import org.hibernate.transaction.TransactionFactory;

/**
 * RollbackTransactionFactory is used for testing-environment where database
 * modifications must be rolled back.
 * 
 * To enable Rollback-only transaction factory you have to add
 * hibernate.transaction.factory_class=com.ahtik.RollbackTransactionFactory to
 * your hibernate properties.
 * 
 * @author ahti.kitsik@gmail.com
 * 
 * Found on http://ahtik.com/blog/2006/05/18/writing-junit-tests-that-rollback-even-after-transaction-commit/
 */
public class RollbackTransactionFactory implements TransactionFactory {

	private final static Logger LOG = Logger.getLogger(RollbackTransactionFactory.class);

	public void configure(Properties props) throws HibernateException {
		// Configuring is not required
	}

	/**
	 * @see org.hibernate.transaction.TransactionFactory#areCallbacksLocalToHibernateTransactions()
	 */
	public boolean areCallbacksLocalToHibernateTransactions() {
		return true;
	}

	/**
	 * @see org.hibernate.transaction.TransactionFactory#createTransaction(org.hibernate.jdbc.JDBCContext,
	 *      org.hibernate.transaction.TransactionFactory.Context)
	 */
	public Transaction createTransaction(JDBCContext jdbcContext,
	        Context context) throws HibernateException {
		RollbackTransaction tx = new RollbackTransaction(jdbcContext, context);
		tx.begin();
		LOG.debug("Returning Rollback-only transaction factory when starting transaction! Used only in testing!!");
		return tx;
	}

	/**
	 * @see org.hibernate.transaction.TransactionFactory#getDefaultReleaseMode()
	 */
	public ConnectionReleaseMode getDefaultReleaseMode() {
		return ConnectionReleaseMode.AFTER_TRANSACTION;
	}

	/**
	 * @see org.hibernate.transaction.TransactionFactory#isTransactionInProgress(org.hibernate.jdbc.JDBCContext,
	 *      org.hibernate.transaction.TransactionFactory.Context,
	 *      org.hibernate.Transaction)
	 */
	public boolean isTransactionInProgress(JDBCContext jdbcContext,
	        Context transactionContext, Transaction transaction) {
		return transaction != null && transaction.isActive();
	}

	/**
	 * @see org.hibernate.transaction.TransactionFactory#isTransactionManagerRequired()
	 */
	public boolean isTransactionManagerRequired() {
		return false;
	}

}