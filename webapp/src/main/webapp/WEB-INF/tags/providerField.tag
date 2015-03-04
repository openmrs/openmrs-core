<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="initialValue" required="false" %> <%-- This should be a providerId --%>
<%@ attribute name="callback" required="false" %> <%-- gets the ProviderListItem sent back --%>
<%@ attribute name="formFieldId" required="false" description="The unique id to assign to the formField" %>

<openmrs:htmlInclude file="/dwr/interface/DWRProviderService.js" />
<openmrs:htmlInclude file="/scripts/jquery/autocomplete/OpenmrsAutoComplete.js" />
<openmrs:htmlInclude file="/scripts/jquery/autocomplete/jquery.ui.autocomplete.autoSelect.js" />

<c:if test="${empty formFieldId}">
	<c:set var="formFieldId" value="${formFieldName}_id" />
</c:if>
<c:set var="displayNameInputId" value="${formFieldId}_selection" />

<script type="text/javascript">
	
	$j(document).ready( function() {

		// set up the autocomplete
		new AutoComplete("${displayNameInputId}", new CreateCallback().providerCallback(), {
			select: function(event, ui) {
				jquerySelectEscaped("${formFieldId}").val(ui.item.object.providerId);
					
				<c:if test="${not empty callback}">
				if (ui.item.object) {
					// only call the callback if we got a true selection, not a click on an error field
					${callback}("${formFieldName}", ui.item.object);
				}
				</c:if>
			},
            placeholder:'<openmrs:message code="Provider.search.placeholder" javaScriptEscape="true"/>'
		});

		//Clear hidden value on losing focus with no valid entry
		$j("#${displayNameInputId}").autocomplete().blur(function(event, ui) {
			if (!event.target.value) {
				jquerySelectEscaped('${formFieldId}').val('');
			}
		});
		
		// get the name of the privider that they passed in the id for
		<c:if test="${not empty initialValue}">
			jquerySelectEscaped("${formFieldId}").val("${initialValue}");
			DWRProviderService.getProvider("${initialValue}", function(provider) {
				jquerySelectEscaped("${displayNameInputId}").val(provider.name);
				jquerySelectEscaped("${displayNameInputId}").autocomplete("option", "initialValue", provider.name);
				<c:if test="${not empty callback}">
					${callback}("${formFieldName}", provider);
			</c:if>
			});
		</c:if>
		
	})
</script>

<input type="text" id="${displayNameInputId}" />
<input type="hidden" name="${formFieldName}" id="${formFieldId}" />
