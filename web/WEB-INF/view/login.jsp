<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<br/>

<form method="post" action="loginServlet" class="box" style="padding:15px; width: 300px;">
	<table>
		<tr>
			<td><spring:message code="User.username"/>:</td>
			<td><input type="text" name="uname" value="" id="username" size="25" maxlength="50"></td>
		</tr>
		<c:if test="${forgotPassword == null || forgotPassword == false}">
			<tr>
				<td><spring:message code="User.password"/>:</td>
				<td><input type="password" name="pw" value="" id="password" size="25"></td>
			</tr>
		<!--	<tr>
				<td></td>
				<td><input type="checkbox" name="forgotPassword" id="forgotPassword" /><label for="forgotPassword"><spring:message code="User.password.forgot"/></label></td>
			</tr> -->
		</c:if>
		<c:if test="${forgotPassword == true}">
			<tr>
				<td colspan="2">
					<spring:message code="User.secretQuestion.prompt"/><br/>
					${secretQuestion}
				</td>
			</tr>
			<tr>
				<td align="left"><spring:message code="general.answer"/>:</td>
				<td align="left"><input type="secretAnswer" name="secretAnswer" value="" id="secretAnswer"></td>
			</tr>
		</c:if>
	</table>
	<br>
		<input type="hidden" name="redirect" value="${__openmrs_login_redirect}" />
	
	<input type="submit" value="<spring:message code="auth.login"/>" />
</form>	

<script type="text/javascript">
 document.getElementById('username').focus();
</script>
	
<%@ include file="/WEB-INF/template/footer.jsp" %>