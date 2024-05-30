package org.openmrs.scheduler.tasks;

import org.apache.commons.lang.StringUtils;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.util.DateUtil;
import org.openmrs.util.OpenmrsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.openmrs.util.OpenmrsConstants.GP_NUMBER_OF_DAYS_TO_AUTO_RETIRE_USERS;

/**
 * A scheduled task that automatically retires user after the set number-of-days of inactivity. 
 * The inactivity duration is set as a global property 
 * Documentation: https://openmrs.atlassian.net/wiki/spaces/docs/pages/25520634/Administering+Scheduled+Tasks
 * {@link org.openmrs.util.OpenmrsConstants#GP_NUMBER_OF_DAYS_TO_AUTO_RETIRE_USERS}
 */
public class AutoRetireUsersTask extends AbstractTask {
	
	private static final Logger log = LoggerFactory.getLogger(AutoRetireUsersTask.class);

	/**
	 * @see org.openmrs.scheduler.tasks.AbstractTask#execute()
	 */
	@Override
	public void execute() {
		if (!isExecuting) {
			log.debug("Starting auto-retiring users");
			
			startExecuting();
			
			try {
				UserService userService = Context.getUserService();
				Set<User> usersToRetire = getUsersToRetire(userService);
				
				usersToRetire.forEach(user -> userService.retireUser(user, "Retire reason"));
			} catch (Exception e) {
				log.error("Error occurred while auto-retiring users: ", e);
			} finally {
				log.debug("Auto-retiring users task ended");
				stopExecuting();
			}
		}
	}

	private Set<User> getUsersToRetire(UserService userService) {
		List<User> allUsers = userService.getAllUsers();
		String numberOfDaysToRetire = Context.getAdministrationService().getGlobalProperty(GP_NUMBER_OF_DAYS_TO_AUTO_RETIRE_USERS);
		long numberOfMillisecondsToRetire = DateUtil.daysToMilliseconds(Integer.parseInt(numberOfDaysToRetire));

		return allUsers.stream()
			.filter(user -> !user.isRetired() && userInactivityExceedsDaysToRetire(user, numberOfMillisecondsToRetire))
			.collect(Collectors.toSet());
	}

	private boolean userInactivityExceedsDaysToRetire(User user, long numberOfMillisecondsToRetire) {
		String lastLoginTimeString = user.getUserProperty(OpenmrsConstants.USER_PROPERTY_LAST_LOGIN_TIMESTAMP);

		if (StringUtils.isNotBlank(lastLoginTimeString)) {
			long lastLoginTime = Long.parseLong(lastLoginTimeString);

			return System.currentTimeMillis() - lastLoginTime >= numberOfMillisecondsToRetire;
		}
		return false;
	}
}
