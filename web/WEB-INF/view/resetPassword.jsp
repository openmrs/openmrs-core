<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<br/>

<form method="post" action="passwordServlet" class="box">
	<table>
		<tr>
			<td><spring:message code="User.username"/>:</td>
			<td><input type="text" name="uname" id="username"></td>
		</tr>
		<tr>
			<td><spring:message code="User.secretQuestion"/>:</td>
			<td><input type="text" name="secretQuestion" id="secretQuestion"></td>
		</tr>
		<tr>
			<td><spring:message code="User.secretAnswer"/>:</td>
			<td><input type="password" name="secretAnswer" id="secretAnswer"></td>
		</tr>
		<tr>
			<td></td>
			<td><a href="<%= request.getContextPath() %>/resetPassword.htm">I forgot my password</a></td>
		</tr>
	</table>
	<br>
		<input type="hidden" name="redirect" value="${__openmrs_login_redirect}" />
	
	<input type="submit" value="<spring:message code="auth.login"/>" />
</form>	

<script>
 document.getElementById('username').focus();
</script>
	
<%@ include file="/WEB-INF/template/footer.jsp" %>