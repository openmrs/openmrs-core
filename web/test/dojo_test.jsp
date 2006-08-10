<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/formentry/index.htm" />

<openmrs_tag:userField formFieldName="mike" roles="Clinician;Informatics Manager" />
<br/><br/>
<openmrs_tag:userField formFieldName="joe" roles="System Developer;" />
