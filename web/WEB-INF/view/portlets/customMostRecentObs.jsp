<%@ include file="/WEB-INF/template/include.jsp" %>
<%--
	allowNew=true/false (defaults false)
--%>

<c:set var="allowNew" value="${model.allowNew == 'true'}"/>

<%-- <openmrs:globalProperty var="conceptsToDisplay" key="${model.globalPropertyKey}" /> --%>
<c:if test="${not empty model.conceptIds}">

	<openmrs:htmlInclude file="/dwr/interface/DWRObsService.js" />
	<openmrs:htmlInclude file="/dwr/engine.js" />
	<openmrs:htmlInclude file="/dwr/util.js" />

	<table>
	<c:forTokens var="conceptId" items="${model.conceptIds}" delims="," >
		<tr>
			<td><openmrs_tag:concept conceptId="${conceptId}"/>:</td>
			<td>
				<b>
				<openmrs_tag:mostRecentObs concept="${conceptId}" observations="${model.patientObs}" locale="${model.locale}" labelIfNone="general.none" showDate="true"/>
				</b>
			</td>
			<c:if test="${allowNew}">
				<td>
					<c:set var="thisConcept" value="${model.conceptMapByStringIds[conceptId]}"/>
					<a href="javascript:showHideDiv('newCustomObs_${conceptId}')">
						<spring:message code="general.new"/>
					</a>
				</td>
				<td class="dashedAndHighlighted" id="newCustomObs_${conceptId}" style="display:none">
					<input type="text" id="value_${conceptId}"/>
					<spring:message code="general.onDate"/>
					<openmrs:fieldGen type="java.util.Date" formFieldName="date_${conceptId}" val="" parameters="noBind=true" />
					<input type="button" value="<spring:message code="general.save"/>" onClick="handleAddCustomObs(${conceptId})"/>
					<input type="button" value="<spring:message code="general.cancel"/>" onClick="showHideDiv('newCustomObs_${conceptId}')"/>
				</td>
			</c:if>
		</tr>
	</c:forTokens>
	</table>
</c:if>

<script type="text/javascript">
	function handleAddCustomObs(conceptId) {
			var encounterId = null;
			var valueText = DWRUtil.getValue(document.getElementById('value_' + conceptId));
			var obsDate = DWRUtil.getValue(document.getElementById('date_' + conceptId));
			var patientId = ${model.patient.patientId};
			var conceptNameId = null;
			//alert("Adding obs for encounter (" + encounterId + "): " + conceptId + " = " + valueText + " " + obsDate);  
			DWRObsService.createObs(patientId, encounterId, conceptId, conceptNameId, valueText, obsDate, refreshPage());
		}
</script>