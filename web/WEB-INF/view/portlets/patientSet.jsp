<%@ include file="/WEB-INF/template/include.jsp" %>

<script language="JavaScript">
	function toggleLayer(whichLayer) {
        var style2 = document.getElementById(whichLayer).style;
     	if (style2.display == "none") {
            style2.display = "";
        } else {
            style2.display = "none";
        }
	}
	function showLayer(whichLayer) {
        var style2 = document.getElementById(whichLayer).style;
        style2.display = "";
	}
	function hideLayer(whichLayer) {
        var style2 = document.getElementById(whichLayer).style;
        style2.display = "none";
	}
</script>

<span onMouseOver="javascript:showLayer('_patientSetBox')" onMouseOut="javascript:hideLayer('_patientSetBox')">
<c:choose>
	<c:when test="${fn:length(model.patientSet.patientIds) == 0}">
		<c:if test="${model.size == 'full'}">
			<spring:message code="PatientSet.noPatients"/>
		</c:if>
	</c:when>
	<c:otherwise>
		<span style="border: 1px black dashed; background-color: yellow; display: block">
			<spring:message code="PatientSet.setOfN" arguments="${fn:length(model.patientSet.patientIds)}"/>
			<c:if test="${model.allowClear != 'false' && model.currentUrl != null}">
				<c:url var="clearUrl" value="patientSet.form">
					<c:param name="method" value="clearPatientSet"/>
					<c:param name="url" value="${model.currentUrl}"/>
				</c:url>
				<a href="${clearUrl}" style="color: red">[X]</a>
			</c:if>
		</span>
		<div id="_patientSetBox"
			 style="border: 1px solid black;
			 		background-color: #ffffaa;
			 		position: absolute;
			 		z-index: 1;
			 		<c:if test="${model.size == 'compact'}">display: none;</c:if>
			 		right: 0px;
			 		width: 400px;">
			<c:forEach var="patientId" items="${model.patientSet.patientIds}" varStatus="status">
				<c:if test="${status.count < 30}">
					<c:if test="${model.linkUrl != null}"><a href="${model.linkUrl}?patientId=${patientId}"></c:if>
					<openmrs:patientWidget size="full" patientId="${patientId}" />
					<c:if test="${model.linkUrl != null}"></a></c:if>
					<c:if test="${model.currentUrl != null && model.allowRemove != 'false'}">
						<c:url var="removeUrl" value="patientSet.form">
							<c:param name="method" value="removeFromSet"/>
							<c:param name="patientId" value="${patientId}"/>
							<c:param name="url" value="${model.currentUrl}"/>
						</c:url>
						<a href="${removeUrl}" style="color: red">[X]</a>
					</c:if>
					<br/>
				</c:if>
				<c:if test="${status.count == 30 && !status.last}">
					<spring:message code="general.nMore" arguments="${fn:length(model.patientSet.patientIds) - 30}"/>
				</c:if>
				
			</c:forEach>
		</div>
	</c:otherwise>
</c:choose>
</span>