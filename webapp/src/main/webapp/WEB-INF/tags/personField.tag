<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="formFieldId" required="false" %>
<%@ attribute name="displayFieldId" required="false" %>
<%@ attribute name="searchLabel" required="false" %> <%-- deprecated --%>
<%@ attribute name="searchLabelCode" required="false" %> <%-- deprecated --%>
<%@ attribute name="searchLabelArguments" required="false" %> <%-- deprecated --%>
<%@ attribute name="roles" required="false" %>
<%@ attribute name="initialValue" required="false" %> <%-- This should be a personId --%>
<%@ attribute name="linkUrl" required="false" %> <%-- deprecated --%>
<%@ attribute name="callback" required="false" %> <%-- gets the relType, PersonListItem sent back --%>
<%@ attribute name="canAddNewPerson" required="false" type="java.lang.Boolean" %>
<%@ attribute name="useOnKeyDown" required="false" %> <%-- deprecated --%>

<openmrs:htmlInclude file="/dwr/interface/DWRPersonService.js" />
<openmrs:htmlInclude file="/scripts/jquery/autocomplete/OpenmrsAutoComplete.js" />
<openmrs:htmlInclude file="/scripts/jquery/autocomplete/jquery.ui.autocomplete.autoSelect.js" />

<style>
.not-found-link {
	font-style: italic;
	background-color: #e0e0e0;
	margin-top: 0.25em;
}
</style>

<c:if test="${empty formFieldId}">
	<c:set var="formFieldId" value="${formFieldName}_id" />
</c:if>
<c:if test="${empty displayFieldId}">
	<c:set var="displayFieldId" value="${formFieldId}_selection" />
</c:if>
<c:if test="${empty canAddNewPerson}">
	<c:set var="canAddNewPerson" value="${ false }" />
</c:if>

<script type="text/javascript">
	$j(document).ready( function() {
		var opts_${ displayFieldId } = {
			<c:if test="${ canAddNewPerson }">
				afterResults: [ {
									value: ' ',
									label: '<span class="not-found-link"><openmrs:message code="Person.notFoundCreate"/></span>',
									onClick: function() {
										$j('#${ formFieldId }_addDialog').dialog('open');
									}
								} ],
			</c:if>
			roles:"${roles}"
		};
		// set up the autocomplete
		new AutoComplete("${displayFieldId}", new CreateCallback(opts_${ displayFieldId }).personCallback(), {
			select: function(event, ui) {
				if (ui.item.onClick) {
					// need to use a setTimeout because otherwise when you open the dialog with the keyboard, when it closes the modal overlay doesn't disappear
					setTimeout(function() {  
						ui.item.onClick();
					}, 100);
					return;
				}
				jquerySelectEscaped("${formFieldId}").val(ui.item.object.personId);
					
				<c:if test="${not empty callback}">
				if (ui.item.object) {
					// only call the callback if we got a true selection, not a click on an error field
					${callback}("${formFieldName}", ui.item.object);
				}
				</c:if>
			},
            placeholder:'<openmrs:message code="Person.search.placeholder" javaScriptEscape="true"/>' 
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
			DWRPersonService.getPerson("${initialValue}", function(person) {
				jquerySelectEscaped("${displayFieldId}").val(person.personName);
				jquerySelectEscaped("${displayFieldId}").autocomplete("option", "initialValue", person.personName);
				<c:if test="${not empty callback}">
					${callback}("${formFieldName}", person);
				</c:if>
			});
		</c:if>
		
	})
</script>

<input type="text" id="${displayFieldId}" />
<input type="hidden" name="${formFieldName}" id="${formFieldId}" />

<c:if test="${ canAddNewPerson }">
	<div id="${ formFieldId }_addDialog">
		<table>
			<tr>
				<td><openmrs:message code="PersonName.givenName"/></td>
				<td><openmrs:message code="PersonName.middleName"/></td>
				<td><openmrs:message code="PersonName.familyName"/></td>
			</tr>
			<tr>
				<td><input id="${ formFieldId }_add_given_name" type="text"/></td>
				<td><input id="${ formFieldId }_add_middle_name" type="text"/></td>
				<td><input id="${ formFieldId }_add_family_name" type="text"/></td>
			</tr>
		</table>
		
		<openmrs:message code="Person.gender"/>:
		<openmrs:forEachRecord name="gender">
			<input type="radio" name="${ formFieldId }_add_gender" id="${ formFieldId }_add_gender_${record.key}" value="${record.key}" />
			<label for="${ formFieldId }_add_gender_${record.key}">
				<openmrs:message code="Person.gender.${record.value}"/>
			</label>
		</openmrs:forEachRecord>
		<br/>

		<openmrs:message code="Person.birthdate"/>:
		<openmrs_tag:dateField formFieldName="${ formFieldId }_add_birthdate" startValue="" />
		<span style="margin-left: 3em;"><openmrs:message code="Person.age"/>:</span>
		<input type="text" size="3" maxLength="3" id="${ formFieldId }_add_age"/>
		
	</div>
	<script>
		$j(function() {
			$j('#${ formFieldId }_addDialog').dialog({
				autoOpen: false,
				modal: true,
				width: '75%',
				title: '<openmrs:message code="Person.notFoundCreate"/>',
				open: function(event, ui) {
					$j('#${ formFieldId }_add_given_name').focus();
				},
				buttons: {
					'<openmrs:message code="Person.create"/>': function() {
						var given = $j('#${ formFieldId }_add_given_name').val();
						var middle = $j('#${ formFieldId }_add_middle_name').val();
						var family = $j('#${ formFieldId }_add_family_name').val();
						var gender = $j('input:radio[name="${ formFieldId }_add_gender"]:checked').val();
						var birthdate = $j('#${ formFieldId }_add_birthdate').val();
						var age = $j('#${ formFieldId }_add_age').val();
						DWRPersonService.createPerson(given, middle, family, birthdate, '<openmrs:datePattern/>', age, gender, function(result) {
							if (typeof result == 'string') {
								window.alert(result);
							} else { // result is a person list item
								$j('#${ formFieldId }_addDialog').dialog('close');
								
								// set the underlying field value and display
								jquerySelectEscaped("${ displayFieldId }").val(result.personName);
								jquerySelectEscaped("${ formFieldId }").val(result.personId);
							}
						});
						
					},
					'<openmrs:message code="general.cancel"/>': function() {
						$j(this).dialog('close');
					}
				},
				close: function() {
					// clear fields
					$j('#${ formFieldId }_add_given_name')
						.add('#${ formFieldId }_add_middle_name')
						.add('#${ formFieldId }_add_family_name')
						.add('#${ formFieldId }_add_birthdate')
						.add('#${ formFieldId }_add_age')
							.val('');
					$j('input:radio[name="${ formFieldId }_add_gender"]:checked').removeAttr('checked');
				}
			});
		});
	</script>
</c:if>