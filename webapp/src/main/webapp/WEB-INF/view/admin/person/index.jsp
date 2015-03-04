<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Persons" otherwise="/login.htm" redirect="/admin/person/index.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/dwr/interface/DWRPersonService.js"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables_jui.css"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js"/>
<openmrs:htmlInclude file="/scripts/jquery-ui/js/openmrsSearch.js" />

<script type="text/javascript">
	$j(document).ready(function() {
		new OpenmrsSearch("findPersons", true, doPersonSearch, doSelectionHandler,
				[	{fieldName:"givenName", header:omsgs.givenName},
					{fieldName:"middleName", header:omsgs.middleName},
					{fieldName:"familyName", header:omsgs.familyName},
					{fieldName:"age", header:omsgs.age},
					{fieldName:"gender", header:omsgs.gender},
					{fieldName:"birthdateString", header:omsgs.birthdate}
				],
				{
                    searchLabel: '<openmrs:message code="Person.searchBox" javaScriptEscape="true"/>',
                    searchPlaceholder:'<openmrs:message code="Person.search.placeholder" javaScriptEscape="true"/>'
                });
	});
	function doSelectionHandler(index, data) {
		document.location = "person.form?personId=" + data.personId;
	}
	
	//searchHandler for the Search widget
	function doPersonSearch(text, resultHandler, getMatchCount, opts) {
		DWRPersonService.findCountAndPeople(text, includeVoidedPersons(), "", opts.start, opts.length, false, resultHandler);
	}
    function includeVoidedPersons() {
        return document.getElementById("includeVoided").checked;
    }
</script>

<h2><openmrs:message code="Person.title"/></h2>

<a href="${pageContext.request.contextPath}/admin/person/addPerson.htm?viewType=edit"><openmrs:message code="Person.create"/></a><br/><br/>

<div>
	<b class="boxHeader"><openmrs:message code="Person.find"/></b>
	<div class="box">
        <div class="searchWidgetContainer" id="findPersons">
        </div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>