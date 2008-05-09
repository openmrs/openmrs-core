<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:require privilege="Run Reports" otherwise="/login.htm" redirect="/admin/reports/reportSchemaXml.form" />

<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Report.manageSchema.title" /></h2>

<table>
  <tr>
   <openmrs:extensionPoint pointId="org.openmrs.admin.reports.reportSchemaXml" type="html">
    <c:forEach items="${extension.links}" var="link">
        <td <c:if test="${fn:endsWith(pageContext.request.requestURI, link.key)}">class="active"</c:if> >
            <a href="${pageContext.request.contextPath}/${link.key}?reportSchemaId=${param.reportSchemaId}"><spring:message code="${link.value}"/></a>
        &nbsp;</td>
    </c:forEach>
   </openmrs:extensionPoint>
  </tr>
</table>
 
<spring:hasBindErrors name="reportSchemaXml">
	<spring:message code="fix.error"/>
	<div class="error">
		<c:forEach items="${errors.allErrors}" var="error">
			<spring:message code="${error.code}" text="${error.code}"/><br/>
		</c:forEach>
	</div>
	<br />
</spring:hasBindErrors>


<form method="post">
	<spring:nestedPath path="reportSchemaXml">
		<table cellspacing="0" cellpadding="2" border="1">
			<tr>
				<th><spring:message code="Report.manageSchema.xml" /></th>
			</tr>
			<tr>
				<td>
					<spring:bind path="xml">
						<textarea name="<c:out value='${status.expression}'/>" rows="50" cols="100">${status.value}</textarea>
					</spring:bind>
				</td>
			</tr>
		</table>
	</spring:nestedPath>
	
    <br/><br/>

    &nbsp;&nbsp;&nbsp;&nbsp;
	<input type="submit" name='action' value='<spring:message code="general.save"/>' />
	&nbsp;&nbsp;&nbsp;&nbsp;
	<input type="button" value='<spring:message code="general.cancel"/>' onClick="window.location = 'reportSchemaXml.list';"/>

</form>
