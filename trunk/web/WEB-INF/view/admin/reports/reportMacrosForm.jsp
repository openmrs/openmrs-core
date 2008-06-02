<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:require privilege="Run Reports" otherwise="/login.htm" redirect="/admin/reports/reportSchemaXml.form" />

<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Report.macros.title" /></h2>

<form method="post">
	<table cellspacing="0" cellpadding="2" border="1">
		<tr>
			<th><spring:message code="Report.macros.title" /></th>
		</tr>
		<tr>
			<td>
				<spring:bind path="command.macros">
					<textarea name="<c:out value='${status.expression}'/>" rows="75" cols="150">${status.value}</textarea>
					<c:if test="${status.errorMessage != ''}"><br/><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
	</table>
    <br/><br/>
    &nbsp;&nbsp;&nbsp;&nbsp;
	<input type="submit" name='action' value='<spring:message code="general.save"/>' />
	&nbsp;&nbsp;&nbsp;&nbsp;
	<input type="button" value='<spring:message code="general.cancel"/>' onClick="window.location = 'reportSchemaXml.list';"/>
</form>
