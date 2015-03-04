<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="formFieldId" required="false" %>
<%@ attribute name="searchLabel" required="false" %>
<%@ attribute name="initialValue" required="false" %>
<%@ attribute name="showAnswers" required="false" %> <%-- a concept id to show answers for --%>
<%@ attribute name="showOther" required="false" %>
<%@ attribute name="otherValue" required="false" %>
<%@ attribute name="onSelectFunction" required="false" %> <%-- Gets the full ConceptListItem object --%>
<%@ attribute name="includeClasses" required="false" %>
<%@ attribute name="excludeClasses" required="false" %>
<%@ attribute name="includeDatatypes" required="false" %>
<%@ attribute name="excludeDatatypes" required="false" %>

<openmrs:htmlInclude file="/dwr/interface/DWRConceptService.js" />
<openmrs:htmlInclude file="/scripts/jquery/autocomplete/OpenmrsAutoComplete.js" />
<openmrs:htmlInclude file="/scripts/jquery/autocomplete/jquery.ui.autocomplete.autoSelect.js" />

<c:if test="${empty formFieldId}">
	<c:set var="formFieldId" value="${formFieldName}_id" />
</c:if>
<c:set var="escapedFormFieldId" value="${fn:replace(formFieldName, '.', '')}" />
<c:set var="escapedFormFieldId" value="${fn:replace(escapedFormFieldId, '[', '')}" />
<c:set var="escapedFormFieldId" value="${fn:replace(escapedFormFieldId, ']', '')}" />
<c:set var="displayNameInputId" value="${formFieldId}_selection" />
<c:set var="otherInputId" value="${formFieldId}_other" />

<script type="text/javascript">
	
	$j(document).ready( function() {

		var includeC = <c:choose> <c:when test="${not empty includeClasses}"> "${includeClasses}".split(",") </c:when> <c:otherwise> null </c:otherwise> </c:choose>;
		var excludeC = <c:choose> <c:when test="${not empty excludeClasses}"> "${excludeClasses}".split(",") </c:when> <c:otherwise> null </c:otherwise> </c:choose>;
		var includeD = <c:choose> <c:when test="${not empty includeDatatypes}"> "${includeDatatypes}".split(",") </c:when> <c:otherwise> null </c:otherwise> </c:choose>;
		var excludeD = <c:choose> <c:when test="${not empty excludeDatatypes}"> "${excludeDatatypes}".split(",") </c:when> <c:otherwise> null </c:otherwise> </c:choose>;

		// the typical callback
		var callback = new CreateCallback({includeClasses:includeC, excludeClasses:excludeC, includeDatatypes:includeD, excludeDatatypes:excludeD}).conceptCallback();
		
		<c:if test="${not empty showAnswers}">
			//override the callback with one that actually goes to the answers
			callback = new CreateCallback({showAnswersFor: "${showAnswers}"}).conceptAnswersCallback();
		</c:if>
		
		// set up the autocomplete
		new AutoComplete("${displayNameInputId}", callback, {
			select: function(event, ui) {
				func${escapedFormFieldId}AutoCompleteOnSelect(ui.item.object, ui.item);
			},
            placeholder:'<openmrs:message code="Concept.search.placeholder" javaScriptEscape="true"/>',
            autoSelect: true
		});
		
		//Clear hidden value on losing focus with no valid entry
		$j("#${displayNameInputId}").autocomplete().blur(function(event, ui) {
			if (!event.target.value) {
				jquerySelectEscaped('${formFieldId}').val('');
			}
		});

		<c:if test="${not empty initialValue}">
			// fetch the concept object they passed the value in of and do the normal "select" stuff
			DWRConceptService.getConcept("${initialValue}", function(concept) { func${escapedFormFieldId}AutoCompleteOnSelect(concept); });
		</c:if>
		
		<c:if test="${not empty showAnswers}">
			// show the autocomplete and all answers on focus
			jquerySelectEscaped("${displayNameInputId}").autocomplete("option", "minLength", 0);
			jquerySelectEscaped("${displayNameInputId}").autocomplete().focus(function(event, ui) {
				if (event.target.value == "") {
					jquerySelectEscaped("${displayNameInputId}").autocomplete("search", ""); //trigger('keydown.autocomplete');
				}
			}); // trigger the drop down on focus
		</c:if>
		
	})
	
	function func${escapedFormFieldId}AutoCompleteOnSelect(concept, item) {

        var conceptId = concept ? concept.conceptId : null
		jquerySelectEscaped('${formFieldId}').val(conceptId);

		// if called with initialValue, show the name ourselves
		if (!item) {
			jquerySelectEscaped('${displayNameInputId}').val(concept.name);
			jquerySelectEscaped('${displayNameInputId}').autocomplete("option", "initialValue", concept.name);
		}

		<c:if test="${not empty showOther}">
			// if showOther is the concept that is selected, show a text field so user can enter that "other" data
			if (conceptId && conceptId == ${showOther}) {
				jquerySelectEscaped("${otherInputId}").show();
			}
			else
				jquerySelectEscaped("${otherInputId}").hide();
		</c:if>
		
		<c:if test="${not empty onSelectFunction}">
		if (concept) {
			// only call the onSelect if we got back a true object
			${onSelectFunction}(concept);
		}
		</c:if>
	}
</script>

<input type="text" id="${displayNameInputId}" />
<input type="hidden" name="${formFieldName}" id="${formFieldId}" />
<input type="text" name="${formFieldName}_other" id="${otherInputId}" style="display:none" value="${otherValue}"/>