<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<%@ page import="org.openmrs.web.WebConstants" %>
<%
	pageContext.setAttribute("redirect", session.getAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR));
	session.removeAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR); 
%>

<br/>

<form method="post" action="loginServlet" class="box" style="padding:15px; width: 300px;">
	<table>
		<tr>
			<td><spring:message code="User.username"/>:</td>
			<td><input type="text" name="uname" value="<request:parameter name="username"/>" id="username" size="25" maxlength="50"></td>
		</tr>
		<request:existsParameter name="forgotPassword" value="false">
			<tr>
				<td><spring:message code="User.password"/>:</td>
				<td><input type="password" name="pw" value="" id="password" size="25"></td>
			</tr>
			<tr>
				<td></td>
				<td><input type="checkbox" name="forgotPassword" value="true" id="forgotPassword" /><label for="forgotPassword"><spring:message code="User.password.forgot"/></label></td>
			</tr>
		</request:existsParameter>
		<request:existsParameter name="forgotPassword">
			<tr>
				<td colspan="2">
					<i><spring:message code="User.secretQuestion.prompt"/></i><br/>
					<b><request:parameter name="secretQuestion"/></b>
				</td>
			</tr>
			<tr>
				<td align="left"><spring:message code="general.answer"/>:</td>
				<td align="left"><input type="password" name="secretAnswer" value="" id="secretAnswer" size="25"></td>
			</tr>
		</request:existsParameter>
	</table>
	<br>
	
	<input type="hidden" name="redirect" value="${redirect}" />
	<input type="hidden" name="referer" value='<request:header name="referer" />' />
	
	<input type="submit" value="<spring:message code="auth.login"/>" />
</form>	

<script type="text/javascript">
 document.getElementById('username').focus();
</script>
	
<%@ include file="/WEB-INF/template/footer.jsp" %>