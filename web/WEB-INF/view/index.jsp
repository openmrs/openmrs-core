<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:portlet url="welcome" parameters="showName=true" />

<br/><br/>

<openmrs:portlet url="findPatient" size="full" parameters="postURL=formentry/patientSummary.form" />

<br /><br />

<%@ include file="/WEB-INF/template/footer.jsp" %> 