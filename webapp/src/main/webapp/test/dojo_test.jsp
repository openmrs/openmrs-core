<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:require privilege="View Users" otherwise="/login.htm" redirect="/test/dojo_test.htm" />

<openmrs_tag:userField formFieldName="mike" roles="Clinician;Informatics Manager" />
<br/><br/>
<openmrs_tag:userField formFieldName="joe" roles="System Developer;" />
