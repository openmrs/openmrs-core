<%@ include file="/WEB-INF/template/include.jsp" %>
<%--
Parameters:
	showDecoration (boolean): whether or not to put this in a box
	showLastThreeEncounters (boolean): whether or not to show a snippet of encounters
--%>

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
		$j(document).ready(function() {
			/* the parent selector here only only allows one datatable call per formEntryTable.
			   without that selector, the .dialog() call for the popup was calling this twice */
			$j("#formEntryTableParent${model.id} > #formEntryTable${model.id}").dataTable({
				"bPaginate": false,
				"bSort": false,
				"bAutoWidth": false
			});
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
	<table id="formEntryTable${model.id}">
		<thead>
			<tr>
				<th><spring:message code="general.name"/></th>
				<th><spring:message code="Form.version"/></th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="entry" items="${model.formToEntryUrlMap}">
				<openmrs:hasPrivilege privilege="${entry.value.requiredPrivilege}">
					<c:url var="formUrl" value="${entry.value.formEntryUrl}">
						<c:param name="personId" value="${model.personId}"/>
						<c:param name="patientId" value="${model.patientId}"/>
						<c:param name="formId" value="${entry.key.formId}"/>
					</c:url>
					<tr>
						<td>
							<a href="${formUrl}" onclick="startDownloading();">${entry.key.name}</a>
						</td>
						<td>
							${entry.key.version}
							<c:if test="${!entry.key.published}"><i>(<spring:message code="Form.unpublished"/>)</i></c:if>
						</td>
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