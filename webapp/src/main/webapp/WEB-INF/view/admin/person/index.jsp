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
		new OpenmrsSearch("findPersons", false, doPersonSearch, doSelectionHandler, 
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
    var includeVoided=false;
	function doSelectionHandler(index, data) {
		document.location = "person.form?personId=" + data.personId;
	}
	
	//searchHandler for the Search widget
	function doPersonSearch(text, resultHandler, getMatchCount, opts) {
		DWRPersonService.findCountAndPeople(text, includeVoided, "", opts.start, opts.length, false, resultHandler);
	}
    function includeVoidedPersons() {
        var includeVoidedcheckbox=document.getElementsByName('filter');
        includeVoided=includeVoidedcheckbox[0].checked;
    }
</script>

<h2><openmrs:message code="Person.title"/></h2>

<a href="${pageContext.request.contextPath}/admin/person/addPerson.htm?viewType=edit"><openmrs:message code="Person.create"/></a><br/><br/>

<div>
	<b class="boxHeader"><openmrs:message code="Person.find"/></b>
	<div class="box">
        <div class="searchWidgetContainer" id="findPersons">
            <div class="includeVoidedPersons">
<<<<<<< HEAD
                <input type="checkbox" name="filter" onclick="includeVoidedPersons()"/><openmrs:message code="Person.includeVoided"/>
=======
                <input type="checkbox" name="filter" onclick="includeVoidedPersons()"/>Include Voided
>>>>>>> b53d10e292fc0ceabe50f628000dcc42f81f36a7
            </div>
        </div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>