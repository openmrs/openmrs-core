<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="formFieldId" required="false" %>
<%@ attribute name="initialValue" required="false" %> <%-- This should be a conceptReferenceTermId --%>
<%@ attribute name="formFieldSize" required="false" %>
<%@ attribute name="callback" required="false" %> <%-- gets the relType, ConceptReferenceTermListItem sent back --%>

<openmrs:htmlInclude file="/dwr/interface/DWRConceptService.js" />
<openmrs:htmlInclude file="/scripts/jquery/autocomplete/OpenmrsAutoComplete.js" />

<c:if test="${empty formFieldId}">
	<c:set var="formFieldId" value="${formFieldName}_id" />
</c:if>
<c:set var="displayNameInputId" value="${formFieldId}_selection" />

<script type="text/javascript">
	
	$j(document).ready( function() {

		// set up the autocomplete
		new AutoComplete("${displayNameInputId}", new CreateCallback().conceptReferenceTermCallback(), {
			select: function(event, ui) {
				jquerySelectEscaped("${formFieldId}").val(ui.item.object.conceptReferenceTermId);
					
				<c:if test="${not empty callback}">
				if (ui.item.object) {
					// only call the callback if we got a true selection, not a click on an error field
					${callback}("${formFieldName}", ui.item.object);
				}
				</c:if>
			},
			placeholder:'<openmrs:message code="ConceptReferenceTerm.searchBox.placeholder" javaScriptEscape="true"/>'
		});
		
		//Clear hidden value on losing focus with no valid entry
		$j("#${displayNameInputId}").autocomplete().blur(function(event, ui) {
			if (!event.target.value) {
				jquerySelectEscaped('${formFieldId}').val('');
			}
		});

		// get the code of the term that they passed in the id for
		<c:if test="${not empty initialValue}">
			jquerySelectEscaped("${formFieldId}").val("${initialValue}");
			DWRConceptService.getConceptReferenceTerm("${initialValue}", function(conceptReferenceTerm) {
				jquerySelectEscaped("${displayNameInputId}").val(conceptReferenceTerm.code);
				jquerySelectEscaped("${displayNameInputId}").autocomplete("option", "initialValue", conceptReferenceTerm.code);
				<c:if test="${not empty callback}">
					${callback}("${formFieldName}", conceptReferenceTerm);
				</c:if>
			});
		</c:if>
		
	})
</script>

<input type="text" id="${displayNameInputId}" size="${formFieldSize}" />
<input type="hidden" name="${formFieldName}" id="${formFieldId}" />