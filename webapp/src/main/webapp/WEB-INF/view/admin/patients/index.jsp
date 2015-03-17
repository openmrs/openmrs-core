<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/admin/patients/index.htm" />
<openmrs:message var="pageTitle" code="patient.title" scope="page"/>
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/dwr/interface/DWRPatientService.js"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables_jui.css"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js"/>
<openmrs:htmlInclude file="/scripts/jquery-ui/js/openmrsSearch.js" />

<openmrs:globalProperty key="patient.listingAttributeTypes" var="attributesToList"/>

<script type="text/javascript">
	$j(document).ready(function() {
		new OpenmrsSearch("findPatients", true, doPatientSearch, doSelectionHandler,
				[	{fieldName:"identifier", header:omsgs.identifier},
					{fieldName:"givenName", header:omsgs.givenName},
					{fieldName:"middleName", header:omsgs.middleName},
					{fieldName:"familyName", header:omsgs.familyName},
					{fieldName:"age", header:omsgs.age},
					{fieldName:"gender", header:omsgs.gender},
					{fieldName:"birthdateString", header:omsgs.birthdate}
				],
				{
                    searchLabel: '<openmrs:message code="Patient.searchBox" javaScriptEscape="true"/>',
                    searchPlaceholder:'<openmrs:message code="Patient.searchBox.placeholder" javaScriptEscape="true"/>',
                    attributes: [
                      <c:forEach var="attribute" items="${fn:split(attributesToList, ',')}" varStatus="varStatus">
                      <c:if test="${fn:trim(attribute) != ''}">
                          <c:set var="attributeName" value="${fn:trim(attribute)}" />
						  <c:choose>
						    <c:when test="${varStatus.index == 0}">
								{name:"${attributeName}", header:"<openmrs:message code="PersonAttributeType.${fn:replace(attributeName, ' ', '')}" text="${attributeName}"/>"}
						    </c:when>
						    <c:otherwise>
								,{name:"${attributeName}", header:"<openmrs:message code="PersonAttributeType.${fn:replace(attributeName, ' ', '')}" text="${attributeName}"/>"}
						    </c:otherwise>
						  </c:choose>
                    	</c:if>
                      </c:forEach>
                    ]
                });
	});
	
	function doSelectionHandler(index, data) {
		document.location = "patient.form?patientId=" + data.patientId;
	}
	
	//searchHandler for the Search widget
	function doPatientSearch(text, resultHandler, getMatchCount, opts) {
		DWRPatientService.findCountAndPatientsWithVoided(text, opts.start, opts.length, getMatchCount, includeVoidedPatients(), resultHandler);
	}

	function includeVoidedPatients() {
	    return document.getElementById("includeVoided").checked;
	}
</script>

<h2><openmrs:message code="Patient.title"/></h2>

<a href="${pageContext.request.contextPath}/admin/person/addPerson.htm?personType=patient&viewType=edit"><openmrs:message code="Patient.create"/></a><br/><br/>

<div>
	<b class="boxHeader"><openmrs:message code="Patient.find"/></b>
	<div class="box">
		<div class="searchWidgetContainer" id="findPatients"></div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>