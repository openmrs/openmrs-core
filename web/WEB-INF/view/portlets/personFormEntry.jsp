<%@ include file="/WEB-INF/template/include.jsp" %>
<%--
Parameters:
	showDecoration (boolean): whether or not to put this in a box
	showLastThreeEncounters (boolean): whether or not to show a snippet of encounters
	returnUrl (String): where to go back to when a form has been cancelled or successfully filled out
--%>

<style type="text/css">
	.EncounterTypeClass {
		color: lightgrey;
	}
</style>
		
<openmrs:htmlInclude file="/scripts/jquery/jquery-1.3.2.min.js" />
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables.css" />
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js" />

<%-- hack because calling a portlet clears parameters --%>
<c:set var="showDecorationProp" value="${model.showDecoration}" />

<c:if test="${model.showLastThreeEncounters}">
	<openmrs:hasPrivilege privilege="View Encounters">
		<openmrs:portlet url="patientEncounters" id="patientDashboardEncounters" patientId="${patient.patientId}" parameters="num=3|hideHeader=true|title=Encounter.last.encounters|hideFormEntry=true" />
		<br/>
	</openmrs:hasPrivilege>
</c:if>

<c:if test="${showDecorationProp}">
	<div class="boxHeader${model.patientVariation}"><spring:message code="FormEntry.fillOutForm"/></div>
	<div class="box${model.patientVariation}">
</c:if>

<c:if test="${!model.anyUpdatedFormEntryModules}">
	<span class="error"><spring:message code="FormEntry.noModulesInstalled"/></span>
	<br/><br/>
</c:if>

<c:if test="${model.anyUpdatedFormEntryModules}">
	
	<%--
		goBackOnEntry == 'true' means have the browser go back to the find patient page after starting to enter a form
	--%>
	<openmrs:globalProperty key="FormEntry.patientForms.goBackOnEntry" var="goBackOnEntry" defaultValue="false"/>
	
	<script type="text/javascript">	
		var $j = jQuery.noConflict();

		<%-- global var and datatable filter for showRetired --%>
		var showRetiredFormsForEntry${model.id} = false;
		$j.fn.dataTableExt.afnFiltering.push(
			function( oSettings, aData, iDataIndex ) {
				if (oSettings.sTableId != 'formEntryTable${model.id}')
					return true;
				else
					return showRetiredFormsForEntry${model.id} || aData[4] == 'false';
			}
		);

		$j(document).ready(function() {
			/* the parent selector here only only allows one datatable call per formEntryTable.
			   without that selector, the .dialog() call for the popup was calling this twice */
			var oTable${model.id} = $j("#formEntryTableParent${model.id} > #formEntryTable${model.id}").dataTable({
				"bPaginate": false,
				"bAutoWidth": false,
				"aaSorting": [[0, 'asc']],
				"aoColumns":
					[
						{ "iDataSort": 1 },
						{ "bVisible": false, "sType": "numeric" },
						null,
						{ "sClass": "EncounterTypeClass" },
						{ "bVisible": false }
					]
			});
			oTable${model.id}.fnDraw(); <%-- trigger filter-and-draw of datatable now --%>

			<%-- trigger filter-and-draw of the datatable whenever the showRetired checkbox changes --%>
			$j('#showRetired${model.id}').click(function() {
				showRetiredFormsForEntry${model.id} = this.checked;
				oTable${model.id}.fnDraw();
			});

			<%-- move the showRetired checkbox inside the flow of the datatable after the filter --%>
			$j('#handleForShowRetired${model.id}').appendTo($j('#formEntryTable${model.id}_filter'));
		});
	
		function startDownloading() {
			<c:if test="${goBackOnEntry}">
				timeOut = setTimeout("goBackToPatientSearch()", 30000);
			</c:if>
		}
		
		function goBackToPatientSearch() {
			document.location='findPatient.htm';
		}
	</script>
	<div id="formEntryTableParent${model.id}">
	<span id="handleForShowRetired${model.id}">
		&nbsp;&nbsp;&nbsp;&nbsp;
		<input type="checkbox" id="showRetired${model.id}"/> <spring:message code="SearchResults.includeRetired"/>
	</span>
	<table id="formEntryTable${model.id}" cellspacing="0" cellpadding="3">
		<thead>
			<tr>
				<th><spring:message code="general.name"/></th>
				<th><!-- Hidden column for sorting previous column --></th>
				<th><spring:message code="Form.version"/></th>
				<th class="EncounterTypeClass"><spring:message code="Encounter.type"/></th>
				<th><!-- Hidden column for retired --></th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="entry" items="${model.formToEntryUrlMap}" varStatus="rowCounter">
				<openmrs:hasPrivilege privilege="${entry.value.requiredPrivilege}">
					<c:url var="formUrl" value="${entry.value.formEntryUrl}">
						<c:param name="personId" value="${model.personId}"/>
						<c:param name="patientId" value="${model.patientId}"/>
						<c:param name="returnUrl" value="${model.returnUrl}"/>
						<c:param name="formId" value="${entry.key.formId}"/>
					</c:url>
					<tr<c:if test="${entry.key.retired}"> class="retired"</c:if>>
						<td>
							<a href="${formUrl}" onclick="startDownloading();">${entry.key.name}</a>
						</td>
						<td>
							${rowCounter.count}
						</td>
						<td>
							${entry.key.version}
							<c:if test="${!entry.key.published}"><i>(<spring:message code="Form.unpublished"/>)</i></c:if>
						</td>
						<td>
							${entry.key.encounterType.name}
						</td>
						<td>${entry.key.retired}</td>
					</tr>
				</openmrs:hasPrivilege>
			</c:forEach>
		</tbody>
	</table>
	</div>
</c:if>

<c:if test="${showDecorationProp}">
	</div>
</c:if>