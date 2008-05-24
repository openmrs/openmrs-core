package org.openmrs.test;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.TransactionException;
import org.hibernate.jdbc.JDBCContext;
import org.hibernate.transaction.JDBCTransaction;
import org.hibernate.transaction.TransactionFactory;

/**
 * Transaction that works exactly like JDBCTransaction except that commits are
 * always ignored and instead of commit session.flush() is called.
 * 
 * @author ahti.kitsik@gmail.com
 * 
 * Found on http://ahtik.com/blog/2006/05/18/writing-junit-tests-that-rollback-even-after-transaction-commit/
 * 
 */
public class RollbackTransaction extends JDBCTransaction {

	private final static Logger LOG = Logger.getLogger(RollbackTransaction.class);

	private final JDBCContext jdbcContext;
	private final TransactionFactory.Context transactionContext;

	private boolean toggleAutoCommit;

	private boolean rolledBack;

	private boolean committed;

	private boolean begun;

	private boolean commitFailed;

	private static final Log log = LogFactory.getLog(RollbackTransaction.class);

	/**
	 * @param jdbcContext
	 * @param transactionContext
	 */
	public RollbackTransaction(JDBCContext jdbcContext, TransactionFactory.Context transactionContext) {
		super(jdbcContext, transactionContext);
		this.jdbcContext = jdbcContext;
		this.transactionContext = transactionContext;
	}

	public void begin() throws HibernateException {

		log.debug("begin");

		try {
			toggleAutoCommit = jdbcContext.connection().getAutoCommit();
			if (log.isDebugEnabled())
				log.debug("current autocommit status:" + toggleAutoCommit);
			if (toggleAutoCommit) {
				log.debug("disabling autocommit");
				jdbcContext.connection().setAutoCommit(false);
			}
		} catch (SQLException e) {
			log.error("Begin failed", e);
			throw new TransactionException("Begin failed with SQL exception: ",
			                               e);
		}

		begun = true;
	}

	public void commit() throws HibernateException {
		LOG.debug("Executing fake-commit (only session.flush() is invoked.");
		// Avoiding commit but will flush to emulated proper db writing for
		// active connection.
		if (!begun)
			throw new TransactionException("Transaction not successfully started");
		log.debug("commit");

		if (!transactionContext.isFlushModeNever())
			transactionContext.managedFlush();

		committed = true;
		jdbcContext.afterTransactionCompletion(true, this);
		toggleAutoCommit();
	}

	public void rollback() throws HibernateException {

		if (!begun)
			throw new TransactionException("Transaction not successfully started");

		log.debug("rollback");

		if (!commitFailed) {
			try {
				jdbcContext.connection().rollback();
				rolledBack = true;
			} catch (SQLException e) {
				log.error("Rollback failed", e);
				throw new TransactionException("Rollback failed with SQL exception: ",
				                               e);
			} finally {
				jdbcContext.afterTransactionCompletion(false, this);
				toggleAutoCommit();
			}
		}
	}

	private void toggleAutoCommit() {
		try {
			if (toggleAutoCommit) {
				log.debug("re-enabling autocommit");
				jdbcContext.connection().setAutoCommit(true);
			}
		} catch (Exception sqle) {
			log.error("Could not toggle autocommit", sqle);
			// swallow it (the transaction _was_ successful or successfully
			// rolled back)
		}
	}

}