<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="View Encounters" otherwise="/login.htm"
	redirect="/admin/encounters/encounter.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<script type="text/javascript">
	var $j = jQuery.noConflict();
	$j(document).ready(function() {
		toggleEncounterVisitHandler();
		
		$j("#enableVisits").click(toggleEncounterVisitHandler);
	});
	function toggleEncounterVisitHandler() {
		if ($j("#enableVisits").is(":checked")) {
			$j("#encounterVisitHandler").removeAttr("disabled").removeClass(
					"disabled");
		} else {
			$j("#encounterVisitHandler").attr("disabled", "disabled").addClass(
					"diabled");
		}
	}
</script>

<h2>
	<spring:message code="Encounter.manage.visits" />
</h2>

<spring:hasBindErrors name="encounterVisitHandlerForm">
	<spring:message code="fix.error" />
	<br />
</spring:hasBindErrors>

<b class="boxHeader"><spring:message
		code="Encounter.visits.configure" /> </b>
<div class="box">
	<form:form method="post" commandName="encounterVisitHandlerForm">
		<table cellpadding="3" cellspacing="0">
			<tr>
				<th><spring:message code="Encounter.visits.enable" />
				</th>
				<td><form:checkbox path="enableVisits" id="enableVisits" /> <form:errors
						path="enableVisits" cssClass="error" />
				</td>
			</tr>
			<tr>
				<th><spring:message code="Encounter.visits.handler.choose" />
				</th>
				<td><form:select path="encounterVisitHandler"
						id="encounterVisitHandler">
						<form:options items="${encounterVisitHandlers}"
							itemLabel="displayName" itemValue="class.name" />
					</form:select> <form:errors path="encounterVisitHandler" cssClass="error" />
				</td>
			</tr>
		</table>
		<input type="submit" value='<spring:message code="general.save"/>'>
	</form:form>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>