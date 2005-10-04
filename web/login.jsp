<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<br><br>

<form method="post" style="border: 1px solid black; padding:15px; width: 300px;">
	<table>
		<tr>
			<td>Username:</td>
			<td><input type="text" name="username" value=""></td>
		</tr>
		<tr>
			<td>Password:</td>
			<td><input type="password" name="password" value=""></td>
		</tr>
	</table>
	<br>
	<input type="submit" value="Log in">
</form>

<request:existsAttribute name="username">
	Submitted...<br>
	<%
		Context context = (Context)request.getAttribute("context");
		try {
			context.authenticate(request.getParameter("username"), request.getParameter("password"));
			out.write("Authenticated!<br><br>");
			out.write("Welcome " + context.getAuthenticatedUser().getUsername());
		}
		catch (Exception e) {
			out.write("ERROR: " + e);
		}
	%>
</request:existsAttribute>
		
	
<%@ include file="/WEB-INF/template/footer.jsp" %>