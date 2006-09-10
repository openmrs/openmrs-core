<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/findPatient.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<h2><spring:message code="Patient.search"/></h2>	

<br />
<br />

<openmrs:portlet id="findPatient" url="findPatient" parameters="size=full|postURL=patientDashboard.form" />

<span class="addLink"><a href="<%= request.getContextPath() %>/admin/patients/addPatient.htm">+ <spring:message code="Patient.add.new" /></a></span>

<%@ include file="/WEB-INF/template/footer.jsp" %>

