/**
 * 
 */
package org.openmrs.scheduler.db.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.scheduler.Schedule;
import org.openmrs.scheduler.TaskConfig;
import org.openmrs.scheduler.db.SchedulerDAO;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * @author Justin Miranda
 *
 */
public class HibernateSchedulerDAO implements SchedulerDAO {

	/** 
	 * Context 
	 * 
	 * TODO need to remove
	 */
	private Context context;
	
	/**
	 * Logger
	 */
	private static final Log log = LogFactory.getLog( HibernateSchedulerDAO.class );

	/**
	 * Default Public constructor
	 */
	public HibernateSchedulerDAO() { }
	
	
	/**
	 * Constructor 
	 * 
	 * @param context
	 */
	public HibernateSchedulerDAO(Context context) { 
		this.context = context;
	}
	
	/**
	 * Creates a new task.
	 * 
	 * @param task to be created
	 * @throws DAOException
	 */
	public void createTask(TaskConfig task) throws DAOException {
		Session session = HibernateUtil.currentSession();

		try {
			//add all data minus the password as a new user
			HibernateUtil.beginTransaction();
			session.save(task);
			HibernateUtil.commitTransaction();		
		}
		catch (Exception e) {
			log.error("Rolling back transaction", e);			
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}			
		
	}

	/**
	 * Get task by internal identifier
	 * 
	 * @param taskId internal task identifier
	 * @return task with given internal identifier
	 * @throws DAOException
	 */
	public TaskConfig getTask(Integer taskId) throws DAOException { 
		Session session = HibernateUtil.currentSession();
		TaskConfig task = (TaskConfig) session.get(TaskConfig.class, taskId);
		
		if (task == null) {
			log.warn("Task '" + taskId + "' not found");
			throw new ObjectRetrievalFailureException(TaskConfig.class, taskId);
		}
		return task;		
	}

	/**
	 * Update task 
	 * 
	 * @param task to be updated
	 * @throws DAOException
	 */
	public void updateTask(TaskConfig task) throws DAOException { 

		Session session = HibernateUtil.currentSession();			
		try {
			HibernateUtil.beginTransaction();
			session.merge(task);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			log.error("Rolling back transaction", e);
			HibernateUtil.rollbackTransaction();
			throw new APIException(e); 
		}		
	}

	/**
	 * Find all tasks with a given identifier
	 * 
	 * @param identifier
	 * @return set of tasks matching identifier
	 * @throws DAOException
	 */
	public List<TaskConfig> getTasks() throws DAOException { 
		Session session = HibernateUtil.currentSession();			
		List<TaskConfig> tasks = session.createCriteria(TaskConfig.class).list();
		return tasks;
		
	}


	/**
	 * Delete task from database. 
	 * 
	 * @param task task to be deleted
	 * @throws DAOException
	 */
	public void deleteTask(Integer taskId) throws DAOException { 
		TaskConfig taskConfig = getTask( taskId ); 
		deleteTask( taskConfig );
	}
	
	/**
	 * Delete task from database. 
	 * 
	 * @param task task to be deleted
	 * @throws DAOException
	 */
	public void deleteTask(TaskConfig taskConfig) throws DAOException { 
		Session session = HibernateUtil.currentSession();
		HibernateUtil.beginTransaction();
		session.delete( taskConfig );
		HibernateUtil.commitTransaction();
	}

	
	
	
	/**
	 * Creates a new schedule.
	 * 
	 * @param schedule to be created
	 * @throws DAOException
	 */
	//public void createSchedule(Schedule schedule) throws DAOException;

	/**
	 * Get schedule by internal identifier
	 * 
	 * @param scheduleId internal schedule identifier
	 * @return schedule with given internal identifier
	 * @throws DAOException
	 */
	public Schedule getSchedule(Integer scheduleId) throws DAOException {
		Session session = HibernateUtil.currentSession();
		Schedule schedule = (Schedule) session.get(Schedule.class, scheduleId);
		
		if (schedule == null) {
			log.error("Schedule '" + scheduleId + "' not found");
			throw new ObjectRetrievalFailureException(Schedule.class, scheduleId);
		}
		return schedule;
	}

	/**
	 * Update a schedule.
	 * 
	 * @param schedule to be updated
	 * @throws DAOException
	 */
	//public void updateSchedule(Schedule schedule) throws DAOException;

	/**
	 * Get all schedules.
	 * 
	 * @return set of all schedules in the database
	 * @throws DAOException
	 */
	//public Set<Schedule> getAllSchedules() throws DAOException;
	
	/**
	 * Delete schedule from database. 
	 * 
	 * @param schedule schedule to be deleted
	 * @throws DAOException
	 */
	//public void deleteSchedule(Schedule schedule) throws DAOException;	
	
}
