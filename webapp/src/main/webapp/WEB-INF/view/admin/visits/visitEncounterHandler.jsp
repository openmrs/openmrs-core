<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Configure Visits"
	otherwise="/login.htm"
	redirect="/admin/visits/visitEncounterHandler.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<script type="text/javascript">
	var $j = jQuery.noConflict();
	$j(document).ready(function() {
		toggleVisitEncounterHandler();
		
		$j("#enableVisits").click(toggleVisitEncounterHandler);
	});
	function toggleVisitEncounterHandler() {
		if ($j("#enableVisits").is(":checked")) {
			$j("#visitEncounterHandler").removeAttr("disabled").removeClass(
					"disabled");
		} else {
			$j("#visitEncounterHandler").attr("disabled", "disabled").addClass(
					"diabled");
		}
	}
</script>

<h2>
	<spring:message code="Visit.configure" />
</h2>

<spring:hasBindErrors name="visitEncounterHandlerForm">
	<spring:message code="fix.error" />
	<br />
</spring:hasBindErrors>

<b class="boxHeader"><spring:message
		code="Visit.configure" /> </b>
<div class="box">
	<form:form method="post" commandName="visitEncounterHandlerForm">
		<p>
			<b><spring:message code="Encounter.visits.enable" />
			</b>
			<form:checkbox path="enableVisits" id="enableVisits" />
			<form:errors path="enableVisits" cssClass="error" />
		</p>

		<p>
			<b><spring:message code="Encounter.visits.handler.choose" />
			</b> <br />
			<form:select path="visitEncounterHandler" id="visitEncounterHandler">
				<form:options items="${visitEncounterHandlers}"
					itemLabel="displayName" itemValue="class.name" />
			</form:select>
			<form:errors path="visitEncounterHandler" cssClass="error" />
		</p>
		<input type="submit" value='<spring:message code="general.save"/>'>
	</form:form>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>