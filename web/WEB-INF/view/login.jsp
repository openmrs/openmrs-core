<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<br/>

<form method="post" action="loginServlet" class="box" style="padding:15px; width: 300px;">
	<table>
		<tr>
			<td><spring:message code="User.username"/>:</td>
			<td><input type="text" name="uname" value="" id="username"></td>
		</tr>
		<tr>
			<td><spring:message code="User.password"/>:</td>
			<td><input type="password" name="pw" value="" id="password"></td>
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