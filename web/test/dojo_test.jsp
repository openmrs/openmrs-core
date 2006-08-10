<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/formentry/index.htm" />

<openmrs:htmlInclude file="/scripts/dojoConfig.js"></openmrs:htmlInclude>
<openmrs:htmlInclude file="/scripts/dojo/dojo.js"></openmrs:htmlInclude>
<openmrs:htmlInclude file="/scripts/dojoUserSearchIncludes.js"></openmrs:htmlInclude>

<openmrs_tag:userField formFieldName="mike" roles="Clinician;Informatics Manager" />
<br/><br/>
<openmrs_tag:userField formFieldName="joe" roles="System Developer;" />
