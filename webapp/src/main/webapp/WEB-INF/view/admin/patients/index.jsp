<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/admin/patients/index.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/dwr/interface/DWRPatientService.js"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables_jui.css"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js"/>
<openmrs:htmlInclude file="/scripts/jquery-ui/js/openmrsSearch.js" />

<script type="text/javascript">
	$j(document).ready(function() {
		new OpenmrsSearch("findPatients", false, doPatientSearch, doSelectionHandler, 
				[	{fieldName:"identifier", header:omsgs.identifier},
					{fieldName:"givenName", header:omsgs.givenName},
					{fieldName:"middleName", header:omsgs.middleName},
					{fieldName:"familyName", header:omsgs.familyName},
					{fieldName:"age", header:omsgs.age},
					{fieldName:"gender", header:omsgs.gender},
					{fieldName:"birthdateString", header:omsgs.birthdate},
				],
				{searchLabel: '<spring:message code="Patient.searchBox" javaScriptEscape="true"/>'});
	});
	
	function doSelectionHandler(index, data) {
		document.location = "patient.form?patientId=" + data.patientId;
	}
	
	//searchHandler for the Search widget
	function doPatientSearch(text, resultHandler, getMatchCount, opts) {
		DWRPatientService.findCountAndPatients(text, opts.start, opts.length, getMatchCount, resultHandler);
	}
</script>

<h2><spring:message code="Patient.title"/></h2>

<a href="${pageContext.request.contextPath}/admin/person/addPerson.htm?personType=patient&viewType=edit"><spring:message code="Patient.create"/></a><br/><br/>

<div>
	<b class="boxHeader"><spring:message code="Patient.find"/></b>
	<div class="box">
		<div class="searchWidgetContainer" id="findPatients"></div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>