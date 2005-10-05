<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<br><br>

<script>
 window.onload=getElementById('username').setFocus();
</script>

<form method="post" action="loginServlet" style="border: 1px solid black; padding:15px; width: 300px;">
	<table>
		<tr>
			<td>Username:</td>
			<td><input type="text" name="username" value="" id="username"></td>
		</tr>
		<tr>
			<td>Password:</td>
			<td><input type="password" name="password" value="" id="password"></td>
		</tr>
	</table>
	<br>
	
		<input type="hidden" name="redirect" value="${login_redirect}" />
	
	<input type="submit" value="Log in" />
</form>	
	
<%@ include file="/WEB-INF/template/footer.jsp" %>