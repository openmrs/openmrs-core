/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web;

import org.openmrs.PersonName;
import org.openmrs.web.controller.OptionsFormController;

/**
 * This is the model/backing object for the "My Profile" page. This lets logged in users set
 * personal preferences, update their own information, etc.
 * 
 * @see OptionsFormController
 */
public class OptionsForm {
	
	private String defaultLocation = "";
	
	private String defaultLocale = "";
	
	private String proficientLocales = "";
	
	private Boolean showRetiredMessage = true;
	
	private Boolean verbose = false;
	
	private String username = "";
	
	private String oldPassword = "";
	
	private String newPassword = "";
	
	private String confirmPassword = "";
	
	private String secretQuestionPassword = "";
	
	private String secretQuestionNew = "";
	
	private String secretQuestionCopy = "";
	
	private String secretAnswerNew = "";
	
	private String secretAnswerConfirm = "";
	
	private String notification = "";
	
	private String notificationAddress = "";
	
	private PersonName personName = new PersonName();
	
	public OptionsForm() {
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getConfirmPassword() {
		return confirmPassword;
	}
	
	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}
	
	public String getDefaultLocale() {
		return defaultLocale;
	}
	
	public void setDefaultLocale(String defaultLocale) {
		this.defaultLocale = defaultLocale;
	}
	
	public String getDefaultLocation() {
		return defaultLocation;
	}
	
	public void setDefaultLocation(String defaultLocation) {
		this.defaultLocation = defaultLocation;
	}
	
	public Boolean getShowRetiredMessage() {
		return showRetiredMessage;
	}
	
	public void setShowRetiredMessage(Boolean showRetiredMessage) {
		this.showRetiredMessage = showRetiredMessage;
	}
	
	public String getNewPassword() {
		return newPassword;
	}
	
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	
	public String getOldPassword() {
		return oldPassword;
	}
	
	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}
	
	public String getSecretQuestionPassword() {
		return secretQuestionPassword;
	}
	
	public void setSecretQuestionPassword(String secretQuestionPassword) {
		this.secretQuestionPassword = secretQuestionPassword;
	}
	
	public String getSecretAnswerNew() {
		return secretAnswerNew;
	}
	
	public void setSecretAnswerNew(String secretAnswerNew) {
		this.secretAnswerNew = secretAnswerNew;
	}
	
	public String getSecretQuestionNew() {
		return secretQuestionNew;
	}
	
	public void setSecretQuestionNew(String secretQuestionNew) {
		this.secretQuestionNew = secretQuestionNew;
	}
	
	public String getSecretQuestionCopy() {
		return secretQuestionCopy;
	}
	
	public void setSecretQuestionCopy(String secretQuestionCopy) {
		this.secretQuestionCopy = secretQuestionCopy;
	}
	
	public String getSecretAnswerConfirm() {
		return secretAnswerConfirm;
	}
	
	public void setSecretAnswerConfirm(String secretAnswerConfirm) {
		this.secretAnswerConfirm = secretAnswerConfirm;
	}
	
	/**
	 * @return Returns the notifications.
	 */
	public String getNotification() {
		return notification;
	}
	
	/**
	 * @param notifications The notifications to set.
	 */
	public void setNotification(String notification) {
		this.notification = notification;
	}
	
	/**
	 * @return Returns the notification address.
	 */
	public String getNotificationAddress() {
		return notificationAddress;
	}
	
	/**
	 * @param notificationAddress The notification address to set.
	 */
	public void setNotificationAddress(String notificationAddress) {
		this.notificationAddress = notificationAddress;
	}
	
	public Boolean getVerbose() {
		return verbose;
	}
	
	public void setVerbose(Boolean verbose) {
		this.verbose = verbose;
	}
	
	/**
	 * Sets the locales within which the user is proficient.
	 * 
	 * @param proficientLocales a comma-separated list of locales
	 */
	public void setProficientLocales(String proficientLocales) {
		this.proficientLocales = proficientLocales;
	}
	
	/**
	 * Returns the locales within which the user is proficient.
	 */
	public String getProficientLocales() {
		return proficientLocales;
	}
	
	/**
	 * @return the personName
	 */
	public PersonName getPersonName() {
		return personName;
	}
	
	/**
	 * @param personName the personName to set
	 */
	public void setPersonName(PersonName personName) {
		this.personName = personName;
	}
}
