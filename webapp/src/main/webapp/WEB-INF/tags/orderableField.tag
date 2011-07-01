<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="initialValue" required="false" %> <%-- This should be an orderable identifier --%>
<%@ attribute name="callback" required="false" %> <%-- gets the OrderableListItem sent back --%>

<openmrs:htmlInclude file="/dwr/interface/DWROrderService.js" />
<openmrs:htmlInclude file="/scripts/jquery/autocomplete/OpenmrsAutoComplete.js" />
<openmrs:htmlInclude file="/scripts/jquery/autocomplete/jquery.ui.autocomplete.autoSelect.js" />

<c:if test="${empty formFieldId}">
	<c:set var="formFieldId" value="${formFieldName}_id" />
</c:if>
<c:set var="displayNameInputId" value="${formFieldId}_selection" />

<script type="text/javascript">
	
	$j(document).ready( function() {

		// set up the autocomplete
		new AutoComplete("${displayNameInputId}", new CreateCallback().orderableCallback(), {
			select: function(event, ui) {
				jquerySelectEscaped("${formFieldId}").val(ui.item.object.uniqueIdentifier);
					
				<c:if test="${not empty callback}">
				if (ui.item.object) {
					// only call the callback if we got a true selection, not a click on an error field
					${callback}("${formFieldName}", ui.item.object);
				}
				</c:if>
			},
            placeholder:'<spring:message code="Orderable.search.placeholder" javaScriptEscape="true"/>'
		});

		// get the name of the orderable that they passed in the id for
		<c:if test="${not empty initialValue}">
			jquerySelectEscaped("${formFieldId}").val("${initialValue}");
			DWROrderService.getOrderable("${initialValue}", function(orderable) {
				jquerySelectEscaped("${displayNameInputId}").val(orderable.name);
				<c:if test="${not empty callback}">
					${callback}("${formFieldName}", orderable);
			</c:if>
			});
		</c:if>
		
	})
</script>

<input type="text" id="${displayNameInputId}" />
<input type="hidden" name="${formFieldName}" id="${formFieldId}" />
