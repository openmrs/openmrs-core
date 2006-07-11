package org.openmrs.api.db.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.api.db.ConceptDAO;
import org.openmrs.api.db.DAOContext;
import org.openmrs.api.db.EncounterDAO;
import org.openmrs.api.db.FormDAO;
import org.openmrs.api.db.NoteDAO;
import org.openmrs.api.db.ObsDAO;
import org.openmrs.api.db.OrderDAO;
import org.openmrs.api.db.PatientDAO;
import org.openmrs.api.db.PatientSetDAO;
import org.openmrs.api.db.TemplateDAO;
import org.openmrs.api.db.UserDAO;
import org.openmrs.formentry.db.FormEntryDAO;
import org.openmrs.formentry.db.hibernate.HibernateFormEntryDAO;
import org.openmrs.hl7.db.HL7DAO;
import org.openmrs.hl7.db.hibernate.HibernateHL7DAO;
import org.openmrs.notification.db.AlertDAO;
import org.openmrs.notification.db.hibernate.HibernateAlertDAO;
import org.openmrs.reporting.db.ReportDAO;
import org.openmrs.reporting.db.ReportObjectDAO;
import org.openmrs.reporting.db.hibernate.HibernateReportDAO;
import org.openmrs.reporting.db.hibernate.HibernateReportObjectDAO;
import org.openmrs.scheduler.db.SchedulerDAO;
import org.openmrs.scheduler.db.hibernate.HibernateSchedulerDAO;
import org.openmrs.util.Security;

public class HibernateDAOContext implements DAOContext {

	private final Log log = LogFactory.getLog(getClass());

	Context context;
	User user;

	// API related DAOs
	private AdministrationDAO administrationDAO;
	private ConceptDAO conceptDAO;
	private EncounterDAO encounterDAO;
	private FormDAO formDAO;
	private ObsDAO obsDAO;
	private OrderDAO orderDAO;
	private PatientDAO patientDAO;
	private PatientSetDAO patientSetDAO;
	private UserDAO userDAO;
	private HL7DAO hl7DAO;
	private FormEntryDAO formEntryDAO;
	private AlertDAO alertDAO;

	// Report DAOs
	private ReportDAO reportDAO;
	private ReportObjectDAO reportObjectDAO;

	// Messaging DAOs
	private TemplateDAO templateDAO;
	private NoteDAO noteDAO;

	// Scheduler DAOs
	private SchedulerDAO schedulerDAO;
	
	public HibernateDAOContext() {}
	  
	public HibernateDAOContext(Context c) {
		this.context = c;
	}
	
	/**
	 * Authenticate the user for this context.
	 * 
	 * @param username
	 * @param password
	 * 
	 * @see org.openmrs.api.context.Context#authenticate(String, String)
	 * @throws ContextAuthenticationException
	 */
	public User authenticate(String login, String password)
			throws ContextAuthenticationException {

		user = null;
		String errorMsg = "Invalid username and/or password";

		// Session session = getSession();
		Session session = HibernateUtil.currentSession();

		String loginWithoutDash = login;
		if (login.length() >= 3 && login.charAt(login.length() - 2) == '-')
			loginWithoutDash = login.substring(0, login.length() - 2)
					+ login.charAt(login.length() - 1);

		User candidateUser = null;
		try {
			candidateUser = (User) session
					.createQuery(
							"from User u where (u.username = ? or u.systemId = ? or u.systemId = ?) and u.voided = 0")
					.setString(0, login).setString(1, login).setString(2,
							loginWithoutDash).uniqueResult();
		} catch (HibernateException he) {
			// TODO Auto-generated catch block
			log.error("Got hibernate exception while logging in: '" + login + "'");
			log.error(he);
		} catch (Exception e) {
			log.error("Got regular exception while logging in: '" + login + "'");
			log.error(e);
		}

		if (candidateUser == null) {
			throw new ContextAuthenticationException("User not found: " + login);
		}

		String passwordOnRecord = (String) session.createSQLQuery(
				"select password from users where user_id = ?").addScalar(
				"password", Hibernate.STRING).setInteger(0,
				candidateUser.getUserId()).uniqueResult();
		String saltOnRecord = (String) session.createSQLQuery(
				"select salt from users where user_id = ?").addScalar("salt",
				Hibernate.STRING).setInteger(0, candidateUser.getUserId())
				.uniqueResult();

		String hashedPassword = Security.encodeString(password + saltOnRecord);

		if (hashedPassword != null && hashedPassword.equals(passwordOnRecord))
			user = candidateUser;

		if (user == null) {
			log
					.info("Failed login attempt (login=" + login + ") - "
							+ errorMsg);
			throw new ContextAuthenticationException(errorMsg);
		}

		return user;
	}

	/**
	 * Get the currently authenticated user
	 * 
	 * @see org.openmrs.api.context.Context#getAuthenticatedUser()
	 */
	public User getAuthenticatedUser() {
		Session session = HibernateUtil.currentSession();
		try {
			if (user != null)
				user = (User)session.merge(user);
		} catch (Exception e) {
			log.debug("Possible attempted locking of user to double open session or: "
					+ e.getMessage());
		}
		// session.merge(user);
		return user;
	}

	public void logout() {
		user = null;
	}

	public AdministrationDAO getAdministrationDAO() {
		if (administrationDAO == null)
			administrationDAO = new HibernateAdministrationDAO(context);
		return administrationDAO;
	}

	public void setAdministrationDAO(AdministrationDAO dao) {
		this.administrationDAO = dao;
	}

	public ConceptDAO getConceptDAO() {
		if (conceptDAO == null)
			conceptDAO = new HibernateConceptDAO(context);
		return conceptDAO;
	}

	public void setConceptDAO(ConceptDAO dao) {
		this.conceptDAO = dao;
	}

	public EncounterDAO getEncounterDAO() {
		if (encounterDAO == null)
			encounterDAO = new HibernateEncounterDAO(context);
		return encounterDAO;
	}

	public void setEncounterDAO(EncounterDAO dao) {
		this.encounterDAO = dao;
	}

	public FormDAO getFormDAO() {
		if (formDAO == null)
			formDAO = new HibernateFormDAO(context);
		return formDAO;
	}

	public void setFormDAO(FormDAO dao) {
		this.formDAO = dao;
	}

	public ObsDAO getObsDAO() {
		if (obsDAO == null)
			obsDAO = new HibernateObsDAO(context);
		return obsDAO;
	}

	public void setObsDAO(ObsDAO dao) {
		this.obsDAO = dao;
	}

	public OrderDAO getOrderDAO() {
		if (orderDAO == null)
			orderDAO = new HibernateOrderDAO(context);
		return orderDAO;
	}

	public void setOrderDAO(OrderDAO dao) {
		this.orderDAO = dao;
	}

	public PatientDAO getPatientDAO() {
		if (patientDAO == null)
			patientDAO = new HibernatePatientDAO(context);
		return patientDAO;
	}

	public void setPatientDAO(PatientDAO dao) {
		this.patientDAO = dao;
	}

	public PatientSetDAO getPatientSetDAO() {
		if (patientSetDAO == null)
			patientSetDAO = new HibernatePatientSetDAO(context);
		return patientSetDAO;
	}

	public void setPatientSetDAO(PatientSetDAO dao) {
		this.patientSetDAO = dao;
	}

	public UserDAO getUserDAO() {
		if (userDAO == null)
			userDAO = new HibernateUserDAO(context);
		return userDAO;
	}

	public void setUserDAO(UserDAO dao) {
		this.userDAO = dao;
	}

	public FormEntryDAO getFormEntryDAO() {
		if (formEntryDAO == null)
			formEntryDAO = new HibernateFormEntryDAO(context);
		return formEntryDAO;
	}

	public void setFormEntryDAO(FormEntryDAO dao) {
		this.formEntryDAO = dao;
	}  

	public HL7DAO getHL7DAO() {
		if (hl7DAO == null)
			hl7DAO = new HibernateHL7DAO(context);
		return hl7DAO;
	}

	public void setHL7DAO(HL7DAO dao) {
		this.hl7DAO = dao;
	}

	public ReportDAO getReportDAO() {
		if (reportDAO == null)
			reportDAO = new HibernateReportDAO(context);
		return reportDAO;
	}

	public void setReportDAO(ReportDAO dao) {
		this.reportDAO = dao;
	}

	public ReportObjectDAO getReportObjectDAO() {
		if (reportObjectDAO == null)
			reportObjectDAO = new HibernateReportObjectDAO(context);
		return reportObjectDAO;
	}

	public void setReportObjectDAO(ReportObjectDAO dao) {
		this.reportObjectDAO = dao;
	}

	public NoteDAO getNoteDAO() {
		if (noteDAO == null)
			noteDAO = new HibernateNoteDAO(context);		
		return noteDAO;
	}

	public void setNoteDAO(NoteDAO dao) {
		this.noteDAO = dao;
	}

	public SchedulerDAO getSchedulerDAO() {
		if (schedulerDAO == null)
			schedulerDAO = new HibernateSchedulerDAO(context);		
		return schedulerDAO;
	}

	public void setSchedulerDAO(SchedulerDAO dao) {
		this.schedulerDAO = dao;
	}	
	
	public TemplateDAO getTemplateDAO() {
		if (templateDAO == null)
			templateDAO = new HibernateTemplateDAO(context);				
		return templateDAO;
	}

	public void setTemplateDAO(TemplateDAO dao) {
		this.templateDAO = dao;
	}

	public AlertDAO getAlertDAO() {
		if (alertDAO == null)
			alertDAO = new HibernateAlertDAO();
		return alertDAO;
	}

	public void setAlertDAO(AlertDAO dao) {
		this.alertDAO = dao;
	}
	
	/**
	 * @see org.openmrs.api.context.Context#openSession()
	 */
	public void openSession() {

		log.debug("HibernateContext: Starting Transaction");
		// if (session == null)
		HibernateUtil.currentSession();

	}

	/**
	 * @see org.openmrs.api.context.Context#closeSession()
	 */
	public void closeSession() {

		log.debug("HibernateContext: Ending Transaction");
		/*
		 * TODO tomcat loops adinfinitum at this point after several redeploys
		 * (during development). Update #1: threadlocal incorrectly configured?
		 * Update #2: or it seems to be an issue with connections being left
		 * around |fixed| Update #3: Memory leak ?
		 */
		HibernateUtil.closeSession();
		// session = null;

	}
	
	public static void startup() {
		HibernateUtil.startup();
	}
	
	public static void shutdown() {
		HibernateUtil.shutdown();
	}

}