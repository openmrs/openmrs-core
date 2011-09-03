<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Encounter Visits"
	otherwise="/login.htm"
	redirect="/admin/encounters/encounterVisitHandler.form" />

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
		<p>
			<b><spring:message code="Encounter.visits.enable" />
			</b>
			<form:checkbox path="enableVisits" id="enableVisits" />
			<form:errors path="enableVisits" cssClass="error" />
		</p>

		<p>
			<b><spring:message code="Encounter.visits.handler.choose" />
			</b> <br />
			<form:select path="encounterVisitHandler" id="encounterVisitHandler">
				<form:options items="${encounterVisitHandlers}"
					itemLabel="displayName" itemValue="class.name" />
			</form:select>
			<form:errors path="encounterVisitHandler" cssClass="error" />
		</p>
		<input type="submit" value='<spring:message code="general.save"/>'>
	</form:form>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>