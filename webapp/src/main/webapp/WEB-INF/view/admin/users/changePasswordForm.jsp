<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:htmlInclude file="/scripts/validation.js" />
<%@ include file="/WEB-INF/template/header.jsp" %>

	<c:if test="${user.retired}">
		<div id="userFormRetired" class="retiredMessage">
			<div><spring:message code="User.retiredMessage"/></div>
		</div>
	</c:if>
	
	<c:if test="${user.person.dead}">
		<div id="userFormDeceased" class="retiredMessage">
			<div><spring:message code="User.userDeceased"/></div>
		</div>
	</c:if>
	
	<spring:hasBindErrors name="user">
		<spring:message code="fix.error"/>
		<div class="error">
			<c:forEach items="${errors.allErrors}" var="error">
				<spring:message code="${error.code}" text="${error.code}"/><br/><!-- ${error} -->
			</c:forEach>
		</div>
		<br />
	</spring:hasBindErrors>
	
	<form id="thisChangePasswordForm" method="post" action="changePassword.form" autocomplete="off">
		<table>
			<tr>
				<td><spring:message code="options.login.password.new" /></td>
				<td><input type="password" name="password" value="" autocomplete="off"/>
					<openmrs:globalProperty key="security.passwordMinimumLength" var="passwordMinimumLength" />
					<openmrs:globalProperty key="security.passwordRequiresUpperAndLowerCase" var="shouldHaveUpperAndLowerCases" />
					<openmrs:globalProperty key="security.passwordRequiresDigit" var="shouldHaveDigit" />
					<openmrs:globalProperty key="security.passwordRequiresNonDigit" var="shouldHaveNonDigit" />
					<span class="description">
					<spring:message code="changePassword.hint.password.length" arguments="${passwordMinimumLength}" />
					<c:if test="${shouldHaveUpperAndLowerCases}">
					, <spring:message code="changePassword.hint.password.bothCasesRequired"/> 
					</c:if>
					<c:if test="${shouldHaveDigit}">
					, <spring:message code="changePassword.hint.password.digitRequired"/>
					</c:if>
					<c:if test="${shouldHaveNonDigit}">
					, <spring:message code="changePassword.hint.password.nonDigitRequired"/>
					</c:if>
					</span>
				</td>
			</tr>
			<tr>
				<td><spring:message code="options.login.password.confirm" /></td>
				<td><input type="password" name="confirmPassword" value="" autocomplete="off" /></td>
			</tr>
		</table>
		<br/>
		<br/>
		<p><spring:message code="options.login.secretQuestion.about"/></p>
		<table>
			<tr>
				<td><spring:message code="options.login.secretQuestionNew" /></td>
				<td><input type="text" name="question" value="" autocomplete="off"/></td>
			</tr>
			<tr>
				<td><spring:message code="options.login.secretAnswerNew" /></td>
				<td><input type="password" name="answer" value="" autocomplete="off"/></td>
			</tr>
			<tr>
				<td><spring:message code="options.login.secretAnswerConfirm" /></td>
				<td><input type="password" name="confirmAnswer" value="" autocomplete="off" /></td>
			</tr>
		</table>	
		<br/>
		</br/>
		<input type="submit" id="saveButton" name="action" value="<spring:message code="general.save"/>" />
	</form>
	
	<script type="text/javascript">
	 document.forms[0].elements[0].focus();
	</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>
