<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="searchLabel" required="false" %> <%-- deprecated --%>
<%@ attribute name="searchLabelCode" required="false" %> <%-- deprecated --%>
<%@ attribute name="roles" required="false" %>
<%@ attribute name="initialValue" required="false" %> <%-- This should be a userId --%>
<%@ attribute name="linkUrl" required="false" %> <%-- deprecated --%>
<%@ attribute name="callback" required="false" %> <%-- gets the relType, UserListItem sent back --%>

<openmrs:htmlInclude file="/dwr/interface/DWRUserService.js" />
<openmrs:htmlInclude file="/scripts/jquery/autocomplete/OpenmrsAutoComplete.js" />
<openmrs:htmlInclude file="/scripts/jquery/autocomplete/jquery.ui.autocomplete.autoSelect.js" />

<c:if test="${empty formFieldId}">
	<c:set var="formFieldId" value="${formFieldName}_id" />
</c:if>
<c:set var="displayNameInputId" value="${formFieldId}_selection" />

<script type="text/javascript">
	
	$j(document).ready( function() {

		// set up the autocomplete
		new AutoComplete("${displayNameInputId}", new CreateCallback({roles:"${roles}"}).userCallback(), {
			select: function(event, ui) {
				jquerySelectEscaped("${formFieldId}").val(ui.item.object.userId);
					
				<c:if test="${not empty callback}">
				if (ui.item.object) {
					// only call the callback if we got a true selection, not a click on an error field
					${callback}("${formFieldName}", ui.item.object);
				}
				</c:if>
			},
            placeholder:'<openmrs:message code="User.search.placeholder" javaScriptEscape="true"/>'
		});

		//Clear hidden value on losing focus with no valid entry
		$j("#${displayNameInputId}").autocomplete().blur(function(event, ui) {
			if (!event.target.value) {
				jquerySelectEscaped('${formFieldId}').val('');
			}
		});
		
		// get the name of the person that they passed in the id for
		<c:if test="${not empty initialValue}">
			jquerySelectEscaped("${formFieldId}").val("${initialValue}");
			DWRUserService.getUser("${initialValue}", function(user) {
				jquerySelectEscaped("${displayNameInputId}").val(user.personName);
				jquerySelectEscaped("${displayNameInputId}").autocomplete("option", "initialValue", user.personName);
				<c:if test="${not empty callback}">
					${callback}("${formFieldName}", user);
			</c:if>
			});
		</c:if>
		
	})
</script>

<input type="text" id="${displayNameInputId}" />
<input type="hidden" name="${formFieldName}" id="${formFieldId}" />
