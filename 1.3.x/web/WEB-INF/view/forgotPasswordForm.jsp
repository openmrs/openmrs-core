<%@ include file="/WEB-INF/template/include.jsp" %>

<spring:message var="pageTitle" code="forgotPassword.title" scope="page"/>
<%@ include file="/WEB-INF/template/header.jsp" %>

<h2><spring:message code="forgotPassword.title"/></h2>

<form method="post" style="padding:15px;">
	<c:if test="${secretQuestion == null}">
		<spring:message code="forgotPassword.help"/><br/>
		<table>
			<tr>
				<td><spring:message code="User.username"/>:</td>
				<td align="left"><input type="text" name="uname" value="" id="username" size="25" maxlength="50"/></td>
			</tr>
			<tr>
				<td></td>
				<td align="left"><input type="submit" value='<spring:message code="forgotPassword.showSecretQuestion"/>' /></td>
			</tr>
		</table>
	</c:if>
	<c:if test="${secretQuestion != null}">
		<input type="hidden" name="uname" value="${uname}"/>
		<table>
			<tr>
				<td colspan="2">
					<i><spring:message code="User.secretQuestion.prompt"/></i><br/>
					<b>${secretQuestion}</b>
				</td>
			</tr>
			<tr>
				<td align="left"><spring:message code="general.answer"/>:</td>
				<td align="left"><input type="password" name="secretAnswer" value="" id="secretAnswer" size="25" autocomplete="off"></td>
			</tr>
			<tr>
				<td></td>
				<td><input type="submit" value="<spring:message code="forgotPassword.resetPassword"/>" /></td>
			</tr>
		</table>
	</c:if>
	<br/>
		
</form>	

<script type="text/javascript">
 document.getElementById('username').focus();
</script>

<openmrs:extensionPoint pointId="org.openmrs.forgotPassword" type="html" />
		
<%@ include file="/WEB-INF/template/footer.jsp" %>