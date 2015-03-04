<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="formFieldId" required="false" %>
<%@ attribute name="displayFieldId" required="false" %>
<%@ attribute name="searchLabel" required="false" %> <%-- deprecated --%>
<%@ attribute name="searchLabelCode" required="false" %> <%-- deprecated --%>
<%@ attribute name="initialValue" required="false" %> <%-- This should be an encounterId --%>
<%@ attribute name="linkUrl" required="false" %> <%-- deprecated --%>
<%@ attribute name="callback" required="false" %> <%-- gets back an encounterId --%>

<openmrs:htmlInclude file="/dwr/interface/DWREncounterService.js" />
<openmrs:htmlInclude file="/scripts/jquery/autocomplete/OpenmrsAutoComplete.js" />
<openmrs:htmlInclude file="/scripts/jquery/autocomplete/jquery.ui.autocomplete.autoSelect.js" />

<c:if test="${empty formFieldId}">
	<c:set var="formFieldId" value="${formFieldName}_id" />
</c:if>
<c:if test="${empty displayFieldId}">
	<c:set var="displayFieldId" value="${formFieldId}_selection" />
</c:if>

<script type="text/javascript">
	
	$j(document).ready( function() {

		// set up the autocomplete
		new AutoComplete("${displayFieldId}", new CreateCallback({maxresults:100}).encounterCallback(), {
			select: function(event, ui) {
				jquerySelectEscaped("${formFieldId}").val(ui.item.object.encounterId);
					
				<c:if test="${not empty callback}">
				if (ui.item.object) {
					// only call the callback if we got a true selection, not a click on an error field
					${callback}(ui.item.object.encounterId);
				}
				</c:if>
			},
            placeholder:'<openmrs:message code="Encounter.search.placeholder" javaScriptEscape="true"/>'
		});

		//Clear hidden value on losing focus with no valid entry
		$j("#${displayFieldId}").autocomplete().blur(function(event, ui) {
			if (!event.target.value) {
				jquerySelectEscaped('${formFieldId}').val('');
			}
		});
		
		// get the name of the person that they passed in the id for
		<c:if test="${not empty initialValue}">
			jquerySelectEscaped("${formFieldId}").val("${initialValue}");
			DWREncounterService.getEncounter("${initialValue}", function(enc) {
				jquerySelectEscaped("${displayFieldId}").val(enc.location + " - " + enc.encounterDateString);
				jquerySelectEscaped("${displayFieldId}").autocomplete("option", "initialValue", enc.location + " - " + enc.encounterDateString);
				<c:if test="${not empty callback}">
					${callback}(enc.encounterId);
				</c:if>
			});
		</c:if>
		
	})
</script>

<input type="text" id="${displayFieldId}" size="45"/>
<input type="hidden" name="${formFieldName}" id="${formFieldId}" />