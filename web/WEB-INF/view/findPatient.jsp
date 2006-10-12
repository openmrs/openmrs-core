<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/findPatient.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<h2><spring:message code="Patient.search"/></h2>	

<br />

<openmrs:portlet id="findPatient" url="findPatient" parameters="size=full|postURL=patientDashboard.form|showIncludeVoided=false" />

<%@ include file="/WEB-INF/template/footer.jsp" %>
