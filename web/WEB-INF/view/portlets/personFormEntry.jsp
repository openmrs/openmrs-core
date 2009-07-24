<%@ include file="/WEB-INF/template/include.jsp" %>
<%--
Parameters:
	showDecoration (boolean): whether or not to put this in a box
--%>

<openmrs:htmlInclude file="/scripts/jquery/jquery-1.3.2.min.js" />
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables.css" />
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js" />

<c:if test="${model.showDecoration}">
	<div class="boxHeader${model.patientVariation}"><spring:message code="FormEntry.fillOutForm"/></div>
	<div class="box${model.patientVariation}">
</c:if>

<c:if test="${!model.anyUpdatedFormEntryModules}">
	<span class="error"><spring:message code="FormEntry.noModulesInstalled"/></span>
	<br/><br/>
</c:if>

<c:if test="${model.anyUpdatedFormEntryModules}">
	<script type="text/javascript">
		var $j = jQuery.noConflict();
		$j(document).ready(function() {
			$j("#formEntryTable${model.portletUUID}").dataTable({
				"bPaginate": false,
				"bSort": false,
				"bAutoWidth": false
			});
		});
	</script>
	<table id="formEntryTable${model.portletUUID}">
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
							<a href="${formUrl}">${entry.key.name}</a>
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
</c:if>

<c:if test="${model.showDecoration}">
	</div>
</c:if>