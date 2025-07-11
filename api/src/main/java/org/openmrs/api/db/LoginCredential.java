/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.OpenmrsObject;
import org.openmrs.User;
import org.openmrs.util.Security;

/**
 * This class holds the minimal amount of data necessary to change a user's password without using a
 * PreparedStatement or putting the password in the User class. This should never be used by
 * anything except for UserDAO and UserService methods that change passwords.
 * 
 * @since 1.5
 */
@Entity
@Table(name = "users")
public class LoginCredential extends BaseOpenmrsObject implements OpenmrsObject {
	
	@Id
	@Column(name = "user_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer userId;
	
	@Column(name = "password")
	private String hashedPassword;
	
	@Column(name = "salt")
	private String salt;
	
	@Column(name = "secret_question")
	private String secretQuestion;
	
	@Column(name = "secret_answer")
	private String secretAnswer;
	
	@ManyToOne
	@JoinColumn(name = "changed_by")
	private User changedBy;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "date_changed")
	private Date dateChanged;
	
	@Column(name = "activation_key")
	private String activationKey;
	
	public LoginCredential() {
	}
	
	/**
	 * @return the changedBy
	 */
	public User getChangedBy() {
		return changedBy;
	}
	
	/**
	 * @param changedBy the changedBy to set
	 */
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}
	
	/**
	 * @return the dateChanged
	 */
	public Date getDateChanged() {
		return dateChanged;
	}
	
	/**
	 * @param dateChanged the dateChanged to set
	 */
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
	
	/**
	 * @return the password
	 */
	public String getHashedPassword() {
		return hashedPassword;
	}
	
	/**
	 * @param password the password to set
	 */
	public void setHashedPassword(String password) {
		this.hashedPassword = password;
	}
	
	/**
	 * @return the salt
	 */
	public String getSalt() {
		return salt;
	}
	
	/**
	 * @param salt the salt to set
	 */
	public void setSalt(String salt) {
		this.salt = salt;
	}
	
	/**
	 * @return the userId
	 */
	public Integer getUserId() {
		return userId;
	}
	
	/**
	 * @param userId the userId to set
	 */
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	
	/**
	 * @return the secretAnswer
	 */
	public String getSecretAnswer() {
		return secretAnswer;
	}
	
	/**
	 * @param secretAnswer the secretAnswer to set
	 */
	public void setSecretAnswer(String secretAnswer) {
		this.secretAnswer = secretAnswer;
	}
	
	/**
	 * @return the secretQuestion
	 */
	public String getSecretQuestion() {
		return secretQuestion;
	}
	
	/**
	 * @param secretQuestion the secretQuestion to set
	 */
	public void setSecretQuestion(String secretQuestion) {
		this.secretQuestion = secretQuestion;
	}
	
	/**
	 * @param pw
	 * @return Whether pw is the correct cleartext password for this user
	 */
	public boolean checkPassword(String pw) {
		return Security.hashMatches(getHashedPassword(), pw + getSalt());
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return userId;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setUserId(id);
	}
	
	/**
	 * @returns the activation key
	 */
	public String getActivationKey() {
		return activationKey;
	}
	
 	/**
	 * @param activationKey the activation key to set.
	 */
	public void setActivationKey(String activationKey) {
		this.activationKey = activationKey;
	}
}
