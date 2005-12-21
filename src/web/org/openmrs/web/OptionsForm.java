package org.openmrs.web;

public class OptionsForm {

	private String defaultLocation = "";
	private String defaultLanguage = "";
	private Boolean showRetiredMessage = true;
	
	private String oldPassword = "";
	private String newPassword = "";
	private String confirmPassword = "";
	
	private String secretQuestionPassword = "";
	private String secretQuestionNew = "";
	private String secretAnswerNew = "";
	private String secretAnswerConfirm = "";
	
	public OptionsForm() {}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	public void setDefaultLanguage(String defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
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

	public String getSecretAnswerConfirm() {
		return secretAnswerConfirm;
	}

	public void setSecretAnswerConfirm(String secretAnswerConfirm) {
		this.secretAnswerConfirm = secretAnswerConfirm;
	}

		

}