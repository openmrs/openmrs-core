<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="localHeader.jsp"%>

<openmrs:require anyPrivilege="View RESTWS, Manage RESTWS" otherwise="/login.htm" redirect="/module/webservices/rest/help.form" />

<h2><spring:message code="webservices.rest.help.title" /></h2>

<spring:message code="webservices.rest.help.wikiTitle"/>: 
<a href="https://wiki.openmrs.org/x/xoAaAQ">https://wiki.openmrs.org/display/projects/Webservices.rest+Module</a>

<br/><br/>

<style type="text/css">
	table.resourceData, table.resourceData th, table.resourceData td
	{
		border: 1px solid black;
	}
	
	table.resourceData tr.d0 td {
	background-color: #FCF6CF;
	}
	
	table.resourceData tr.d1 td {
		background-color: #FEFEF2;
	}
	
</style>

<table class="resourceData">
  <tr>
   <th>Resource</th>
   <th>Url</th>
   <th>Representations</th>
  </tr>
  <jsp:include page="resources.jsp" />
</table>


<%@ include file="/WEB-INF/template/footer.jsp"%>