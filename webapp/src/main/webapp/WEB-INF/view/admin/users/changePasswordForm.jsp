<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:htmlInclude file="/scripts/validation.js" />
<%@ include file="/WEB-INF/template/header.jsp" %>

	<c:if test="${user.retired}">
		<div id="userFormRetired" class="retiredMessage">
			<div><openmrs:message code="User.retiredMessage"/></div>
		</div>
	</c:if>
	
	<c:if test="${user.person.dead}">
		<div id="userFormDeceased" class="retiredMessage">
			<div><openmrs:message code="User.userDeceased"/></div>
		</div>
	</c:if>
	
	<spring:hasBindErrors name="user">
        <openmrs_tag:errorNotify errors="${errors}" />
	</spring:hasBindErrors>
	
	<form id="thisChangePasswordForm" method="post" action="changePassword.form" autocomplete="off">
		<table>
			<tr>
				<td><openmrs:message code="options.login.password.new" /></td>
				<td><input type="password" name="password" value="" autocomplete="off"/>
					<openmrs:globalProperty key="security.passwordMinimumLength" var="passwordMinimumLength" />
					<openmrs:globalProperty key="security.passwordRequiresUpperAndLowerCase" var="shouldHaveUpperAndLowerCases" />
					<openmrs:globalProperty key="security.passwordRequiresDigit" var="shouldHaveDigit" />
					<openmrs:globalProperty key="security.passwordRequiresNonDigit" var="shouldHaveNonDigit" />
					<span class="description">
					<openmrs:message code="changePassword.hint.password.length" arguments="${passwordMinimumLength}" />
					<c:if test="${shouldHaveUpperAndLowerCases}">
					, <openmrs:message code="changePassword.hint.password.bothCasesRequired"/> 
					</c:if>
					<c:if test="${shouldHaveDigit}">
					, <openmrs:message code="changePassword.hint.password.digitRequired"/>
					</c:if>
					<c:if test="${shouldHaveNonDigit}">
					, <openmrs:message code="changePassword.hint.password.nonDigitRequired"/>
					</c:if>
					</span>
				</td>
			</tr>
			<tr>
				<td><openmrs:message code="options.login.password.confirm" /></td>
				<td><input type="password" name="confirmPassword" value="" autocomplete="off" /></td>
			</tr>
		</table>
		<br/>
		<br/>
		<p><openmrs:message code="options.login.secretQuestion.about"/></p>
		<table>
			<tr>
				<td><openmrs:message code="options.login.secretQuestionNew" /></td>
				<td><input type="text" name="question" value="" autocomplete="off"/></td>
			</tr>
			<tr>
				<td><openmrs:message code="options.login.secretAnswerNew" /></td>
				<td><input type="password" name="answer" value="" autocomplete="off"/></td>
			</tr>
			<tr>
				<td><openmrs:message code="options.login.secretAnswerConfirm" /></td>
				<td><input type="password" name="confirmAnswer" value="" autocomplete="off" /></td>
			</tr>
		</table>	
		<br/>
		</br/>
		<input type="submit" id="saveButton" name="action" value="<openmrs:message code="general.save"/>" />
	</form>
	
	<script type="text/javascript">
	 document.forms[0].elements[0].focus();
	</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>
