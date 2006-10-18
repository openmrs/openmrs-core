<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Data Entry Statistics" otherwise="/login.htm" redirect="/admin/maintenace/dataEntryStats.list" />

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:htmlInclude file="/scripts/validation.js" />


<%@ include file="/WEB-INF/template/header.jsp" %>

<%@ include file="localHeader.jsp" %>

<h2><spring:message code="DataEntryStatistics.title"/></h2>

<form method="post">
<spring:message code="general.fromDate"/>:
	<spring:bind path="command.fromDate">			
		<input type="text" name="${status.expression}" size="10" 
			   value="${status.value}" onClick="showCalendar(this)" />
		<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
	</spring:bind>

<br/>
<spring:message code="general.toDate"/>:
	<spring:bind path="command.toDate">			
		<input type="text" name="${status.expression}" size="10" 
			   value="${status.value}" onClick="showCalendar(this)" />
		<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
	</spring:bind>

<br/>
<input type="submit" value="<spring:message code="general.view"/>" />
</form>

<p/>

<c:out value="${command.table.htmlTable}" escapeXml="false"/>

<%@ include file="/WEB-INF/template/footer.jsp" %>
