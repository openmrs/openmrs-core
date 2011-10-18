<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="View Encounters" otherwise="/login.htm"
	redirect="/admin/encounters/encounter.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>

<div id="displayEncounter">
	<a
		href="${pageContext.request.contextPath}/patientDashboard.form?patientId=${encounter.patient.patientId}"><spring:message
			code="PatientDashboard.backToPatientDashboard" /></a>

	<c:set var="viewEncounterUrl"
		value="${pageContext.request.contextPath}/admin/encounters/encounterDisplay.list?encounterId=${encounter.encounterId}&inPopup=true" />
	<c:if test="${formToViewUrlMap[encounter.form] != null }">
		<c:url var="viewEncounterUrl"
			value="${formToViewUrlMap[encounter.form]}">
			<c:param name="encounterId" value="${encounter.encounterId}" />
			<c:param name="inPopup" value="true" />
		</c:url>
	</c:if>

	<br /> <br /> <input type="button" id="viewEncounterUrlLink"
		onclick="displayEncounterUrl('${viewEncounterUrl}')"
		value="<spring:message code="Encounter.view" />" />

	<openmrs:hasPrivilege privilege="Edit Encounters">
		<c:set var="editEncounterUrl"
			value="${pageContext.request.contextPath}/admin/encounters/encounter.form?encounterId=${encounter.encounterId}&inPopup=true" />
		<c:if test="${formToEditUrlMap[encounter.form] != null }">
			<c:url var="editEncounterUrl"
				value="${formToEditUrlMap[encounter.form]}">
				<c:param name="encounterId" value="${encounter.encounterId}" />
				<c:param name="inPopup" value="true" />
			</c:url>
		</c:if>

		<input type="button" id="editEncounterUrlLink"
			onclick="displayEncounterUrl('${editEncounterUrl}')"
			value="<spring:message code="Encounter.edit" />" />
	</openmrs:hasPrivilege>

	<div id="displayEncounterLoading">
		<spring:message code="general.loading" />
	</div>
	<iframe id="displayEncounterIframe" width="100%" height="100%"
		marginWidth="0" marginHeight="0" frameBorder="0" scrolling="auto"></iframe>
</div>

<script type="text/javascript">
	$j(document).ready(function() {
		$j("#displayEncounterIframe").load(function() {
			$j('#displayEncounterLoading').hide();
		});

		$j("#viewEncounterUrlLink").click(function() {
			$j(this).hide();
			$j("#editEncounterUrlLink").show();
		});

		$j("#editEncounterUrlLink").click(function() {
			$j(this).hide();
			$j("#viewEncounterUrlLink").show();
		});

		$j("#viewEncounterUrlLink").click();
	});

	function displayEncounterUrl(url) {
		if(url.indexOf("${pageContext.request.contextPath}/") == -1){
			url = "${pageContext.request.contextPath}/" + url;
		}
			
		$j('#displayEncounterLoading').show();
		$j("#displayEncounterIframe").attr("height", $j(window).height() - 180); 
		$j("#displayEncounterIframe").attr("src", url);
	}
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>