/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api

import org.openmrs.Person
import org.openmrs.Privilege
import org.openmrs.Role
import org.openmrs.User
import org.openmrs.annotation.Authorized
import org.openmrs.annotation.Logging
import org.openmrs.notification.MessageException
import org.openmrs.util.PrivilegeConstants
import java.util.Locale

/**
 * Contains methods pertaining to Users in the system.
 *
 * Use:
 * ```
 * val users = Context.getUserService().getAllUsers()
 * ```
 *
 * @see org.openmrs.api.context.Context
 */
interface UserService : OpenmrsService {

    companion object {
        const val ADMIN_PASSWORD_LOCKED_PROPERTY = "admin_password_locked"
    }

    /**
     * Create user with given password.
     *
     * @param user the user to create
     * @param password the password for created user
     * @return created user
     * @throws APIException if creation fails
     */
    @Authorized(PrivilegeConstants.ADD_USERS)
    @Logging(ignoredArgumentIndexes = [1])
    @Throws(APIException::class)
    fun createUser(user: User, password: String): User

    /**
     * Change user password.
     *
     * @param user the user to update password
     * @param oldPassword the user password to update
     * @param newPassword the new user password
     * @throws APIException for not existing user and if old password is weak
     * @since 1.12
     */
    @Authorized(PrivilegeConstants.EDIT_USER_PASSWORDS)
    @Logging(ignoredArgumentIndexes = [1, 2])
    @Throws(APIException::class)
    fun changePassword(user: User, oldPassword: String?, newPassword: String)

    /**
     * Get user by internal user identifier.
     *
     * @param userId internal identifier
     * @return requested user
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_USERS)
    @Throws(APIException::class)
    fun getUser(userId: Int?): User?

    /**
     * Get user by the given uuid.
     *
     * @param uuid the uuid to search for
     * @return user or null
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_USERS)
    @Throws(APIException::class)
    fun getUserByUuid(uuid: String): User?

    /**
     * Get user by username (user's login identifier).
     *
     * @param username User's identifier used for authentication
     * @return requested user
     */
    @Authorized(PrivilegeConstants.GET_USERS)
    fun getUserByUsername(username: String): User?

    /**
     * Gets a user by username or email.
     *
     * @param usernameOrEmail User's email address or username
     * @return requested user or null if not found
     */
    @Authorized(PrivilegeConstants.GET_USERS)
    fun getUserByUsernameOrEmail(usernameOrEmail: String): User?

    /**
     * Gets a user with the specified activation key.
     *
     * @param activationKey User's activation key for password reset
     * @return requested User with associated activation key
     */
    @Authorized(PrivilegeConstants.GET_USERS)
    fun getUserByActivationKey(activationKey: String): User?

    /**
     * true/false if username or systemId is already in db in username or system_id columns.
     *
     * @param user User to compare
     * @return boolean
     * @throws APIException if check fails
     */
    @Authorized(PrivilegeConstants.GET_USERS)
    @Throws(APIException::class)
    fun hasDuplicateUsername(user: User): Boolean

    /**
     * Get users by role granted.
     *
     * @param role Role that the Users must have to be returned
     * @return users with requested role
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_USERS)
    @Throws(APIException::class)
    fun getUsersByRole(role: Role): List<User>

    /**
     * Updates a given user in the database.
     *
     * @param user the user to save
     * @return the saved user
     * @throws APIException if saving fails
     */
    @Authorized(PrivilegeConstants.EDIT_USERS)
    @Throws(APIException::class)
    fun saveUser(user: User): User

    /**
     * Deactivate a user account so that it can no longer log in.
     *
     * @param user the user to retire
     * @param reason the reason for retirement
     * @return the retired user
     * @throws APIException if retirement fails
     */
    @Authorized(PrivilegeConstants.EDIT_USERS)
    @Throws(APIException::class)
    fun retireUser(user: User, reason: String): User

    /**
     * Clears retired flag for a user.
     *
     * @param user the user to unretire
     * @return the unretired user
     * @throws APIException if unretirement fails
     */
    @Authorized(PrivilegeConstants.EDIT_USERS)
    @Throws(APIException::class)
    fun unretireUser(user: User): User

    /**
     * Completely remove a user from the database (not reversible).
     *
     * @param user the User to remove from the database.
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.PURGE_USERS)
    @Throws(APIException::class)
    fun purgeUser(user: User)

    /**
     * Completely remove a user from the database (not reversible).
     *
     * @param user the User to remove from the database.
     * @param cascade true to delete associated content
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.PURGE_USERS)
    @Throws(APIException::class)
    fun purgeUser(user: User, cascade: Boolean)

    /**
     * Returns all privileges currently possible for any User.
     *
     * @return Global list of privileges
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.MANAGE_PRIVILEGES)
    @Throws(APIException::class)
    fun getAllPrivileges(): List<Privilege>

    /**
     * Returns all roles currently possible for any User.
     *
     * @return Global list of roles
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.MANAGE_ROLES)
    @Throws(APIException::class)
    fun getAllRoles(): List<Role>

    /**
     * Save the given role in the database.
     *
     * @param role Role to update
     * @return the saved role
     * @throws APIException if saving fails
     */
    @Authorized(PrivilegeConstants.MANAGE_ROLES)
    @Throws(APIException::class)
    fun saveRole(role: Role): Role

    /**
     * Completely remove a role from the database.
     *
     * @param role Role to delete from the database
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.PURGE_ROLES)
    @Throws(APIException::class)
    fun purgeRole(role: Role)

    /**
     * Save the given privilege in the database.
     *
     * @param privilege Privilege to update
     * @return the saved privilege
     * @throws APIException if saving fails
     */
    @Authorized(PrivilegeConstants.MANAGE_PRIVILEGES)
    @Throws(APIException::class)
    fun savePrivilege(privilege: Privilege): Privilege

    /**
     * Completely remove a privilege from the database.
     *
     * @param privilege Privilege to delete
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.PURGE_PRIVILEGES)
    @Throws(APIException::class)
    fun purgePrivilege(privilege: Privilege)

    /**
     * Returns role object with given string role.
     *
     * @param r the role name
     * @return Role object for specified string
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_ROLES)
    @Throws(APIException::class)
    fun getRole(r: String): Role?

    /**
     * Get Role by its UUID.
     *
     * @param uuid the uuid
     * @return role or null
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_ROLES)
    @Throws(APIException::class)
    fun getRoleByUuid(uuid: String): Role?

    /**
     * Returns Privilege in the system with given String privilege.
     *
     * @param p the privilege name
     * @return Privilege
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PRIVILEGES)
    @Throws(APIException::class)
    fun getPrivilege(p: String): Privilege?

    /**
     * Get Privilege by its UUID.
     *
     * @param uuid the uuid
     * @return privilege or null
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PRIVILEGES)
    @Throws(APIException::class)
    fun getPrivilegeByUuid(uuid: String): Privilege?

    /**
     * Returns all users in the system.
     *
     * @return Global list of users
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_USERS)
    @Throws(APIException::class)
    fun getAllUsers(): List<User>

    /**
     * Changes the current user's password.
     *
     * @param oldPassword current password
     * @param newPassword new password
     * @throws APIException if change fails
     */
    @Authorized
    @Logging(ignoredArgumentIndexes = [0, 1])
    @Throws(APIException::class)
    fun changePassword(oldPassword: String, newPassword: String)

    /**
     * Changes password of User passed in.
     *
     * @param user user whose password is to be changed
     * @param newPassword new password to set
     * @throws APIException if change fails
     */
    @Authorized(PrivilegeConstants.EDIT_USER_PASSWORDS)
    @Throws(APIException::class)
    fun changePassword(user: User, newPassword: String)

    /**
     * Changes the current user's password directly.
     *
     * @param user the user whose password you want to change
     * @param hashedPassword - the already hashed password to store
     * @param salt - the salt which should be used with this hashed password
     * @throws APIException if change fails
     * @since 1.5
     */
    @Authorized(PrivilegeConstants.EDIT_USER_PASSWORDS)
    @Throws(APIException::class)
    fun changeHashedPassword(user: User, hashedPassword: String, salt: String)

    /**
     * Changes the passed user's secret question and answer.
     *
     * @param u User to change
     * @param question the question
     * @param answer the answer
     * @throws APIException if change fails
     * @since 1.5
     */
    @Authorized(PrivilegeConstants.EDIT_USER_PASSWORDS)
    @Logging(ignoredArgumentIndexes = [1, 2])
    @Throws(APIException::class)
    fun changeQuestionAnswer(u: User, question: String, answer: String)

    /**
     * Changes the current user's secret question and answer.
     *
     * @param pw user's password
     * @param q question
     * @param a answer
     * @throws APIException if change fails
     */
    @Authorized
    @Logging(ignoreAllArgumentValues = true)
    @Throws(APIException::class)
    fun changeQuestionAnswer(pw: String, q: String, a: String)

    /**
     * Returns secret question for the given user.
     *
     * @param user the user
     * @return the secret question
     * @throws APIException if retrieval fails
     * @since 2.0
     */
    @Throws(APIException::class)
    fun getSecretQuestion(user: User): String?

    /**
     * Compares answer against the user's secret answer.
     *
     * @param u user
     * @param answer the answer to check
     * @return true if the answer matches
     * @throws APIException if check fails
     */
    @Logging(ignoredArgumentIndexes = [1])
    @Throws(APIException::class)
    fun isSecretAnswer(u: User, answer: String): Boolean

    /**
     * Return a list of users sorted by personName if any part of the search matches
     * first/last/system id and the user has one at least one of the given roles assigned to them.
     *
     * @param nameSearch string to compare to the beginning of user's given/middle/family/family2 names
     * @param roles all the Roles the user must contain
     * @param includeVoided true/false whether to include voided users
     * @return list of users matching the given attributes
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_USERS)
    @Throws(APIException::class)
    fun getUsers(nameSearch: String?, roles: @JvmSuppressWildcards List<Role>?, includeVoided: Boolean): List<User>

    /**
     * Search for a list of users by exact first name and last name.
     *
     * @param givenName the given name
     * @param familyName the family name
     * @param includeRetired whether to include retired users
     * @return List of users matching criteria
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_USERS)
    @Throws(APIException::class)
    fun getUsersByName(givenName: String, familyName: String, includeRetired: Boolean): List<User>

    /**
     * Get all user accounts that belong to a given person.
     *
     * @param person the person
     * @param includeRetired whether to include retired accounts
     * @return all user accounts that belong to person
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_USERS)
    @Throws(APIException::class)
    fun getUsersByPerson(person: Person, includeRetired: Boolean): List<User>

    /**
     * Adds the key/value pair to the given user.
     *
     * @param user the user
     * @param key the property key
     * @param value the property value
     * @return the user that was passed in and added to
     * @throws APIException if operation fails
     */
    @Authorized
    @Throws(APIException::class)
    fun setUserProperty(user: User, key: String, value: String): User?

    /**
     * Removes the property denoted by key from the user's properties.
     *
     * @param user the user
     * @param key the property key
     * @return the user that was passed in and removed from
     * @throws APIException if operation fails
     */
    @Authorized
    @Throws(APIException::class)
    fun removeUserProperty(user: User, key: String): User?

    /**
     * Get/generate/find the next system id to be doled out.
     *
     * @return new system id
     */
    @Authorized
    fun generateSystemId(): String

    /**
     * Return a batch of users of a specific size sorted by personName.
     *
     * @param name string to compare to the beginning of user's given/middle/family/family2 names
     * @param roles all the Roles the user must contain
     * @param includeRetired true/false whether to include voided users
     * @param start beginning index for the batch
     * @param length number of users to return in the batch
     * @return list of matching users of a size based on the specified arguments
     * @throws APIException if retrieval fails
     * @since 1.8
     */
    @Authorized(PrivilegeConstants.GET_USERS)
    @Throws(APIException::class)
    fun getUsers(name: String?, roles: @JvmSuppressWildcards List<Role>?, includeRetired: Boolean, start: Int?, length: Int?): List<User>

    /**
     * Return the number of users with a matching name or system id and have at least one of the
     * given roles assigned to them.
     *
     * @param name patient name
     * @param roles all the Roles the user must contain
     * @param includeRetired Specifies whether voided users should be included
     * @return the number of users matching the given attributes
     * @since 1.8
     */
    @Authorized(PrivilegeConstants.GET_USERS)
    fun getCountOfUsers(name: String?, roles: @JvmSuppressWildcards List<Role>?, includeRetired: Boolean): Int?

    /**
     * Saves the current key/value as a user property for the current user.
     *
     * @param key the authenticated user's property
     * @param value value of the property
     * @return the saved user
     * @since 1.10
     */
    @Authorized
    fun saveUserProperty(key: String, value: String): User

    /**
     * Replaces all user properties with the given map of properties for the current user.
     *
     * @param properties the authenticated user's properties
     * @return the saved user
     * @since 1.10
     */
    @Authorized
    fun saveUserProperties(properties: @JvmSuppressWildcards Map<String, String>): User

    /**
     * Change user password given the answer to the secret question.
     *
     * @param secretAnswer the answer to secret question
     * @param pw the new password
     * @throws APIException if change fails
     */
    @Authorized
    @Throws(APIException::class)
    fun changePasswordUsingSecretAnswer(secretAnswer: String, pw: String)

    /**
     * Sets a user's activation key.
     *
     * @param user The user for which the activation key will be set
     * @return the user with the activation key set
     * @throws MessageException if setting fails
     */
    @Authorized(PrivilegeConstants.EDIT_USER_PASSWORDS)
    @Throws(MessageException::class)
    fun setUserActivationKey(user: User): User

    /**
     * Change user password given the activation key.
     *
     * @param activationKey the activation for password reset
     * @param newPassword the new password
     */
    fun changePasswordUsingActivationKey(activationKey: String, newPassword: String)

    /**
     * Gets the default Locale of the given user.
     *
     * @param user the User whose Locale to retrieve
     * @return the default Locale of the given user, or the system locale if unspecified
     * @since 2.3.6, 2.4.6, 2.5.4, 2.6.0
     */
    @Authorized
    fun getDefaultLocaleForUser(user: User): Locale

    /**
     * Retrieves the last login time of the user in Unix Timestamp.
     *
     * @param user the subject user
     * @return timestamp representing last login time (e.g. 1717414410587)
     * @since 2.7.0
     */
    @Authorized
    fun getLastLoginTime(user: User): String
}
