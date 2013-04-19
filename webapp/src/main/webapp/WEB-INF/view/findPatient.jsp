<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/findPatient.htm" />

<openmrs:message var="pageTitle" code="findPatient.title" scope="page"/>
<%@ include file="/WEB-INF/template/header.jsp" %>
<c:if test="${param.error_message != null}">
	<div class="error" style="text-align: center;">
		<p>${param.error_message}</p>
	</div>
</c:if>
<h2><openmrs:message code="Patient.search"/></h2>	

<br />

<openmrs:portlet id="findPatient" url="findPatient" parameters="size=full|postURL=patientDashboard.form|showIncludeVoided=false|viewType=shortEdit" />

<openmrs:extensionPoint pointId="org.openmrs.findPatient" type="html" />

<%@ include file="/WEB-INF/template/footer.jsp" %>
