<%@ include file="/WEB-INF/template/include.jsp" %>
<%--
	allowNew=true/false (defaults false)
--%>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

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
				
				<c:choose>
					<c:when test="${thisConcept.datatype.hl7Abbreviation == 'DT'}">		
				 		<input type="text" size="10" value="" onClick="showCalendar(this)" id="value_${conceptId}" />
					</c:when>
					<c:otherwise>
						<input type="text" id="value_${conceptId}"/>	
					</c:otherwise>
				</c:choose>	
				
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
			var valueText = dwr.util.getValue(document.getElementById('value_' + conceptId));
			var obsDate = dwr.util.getValue(document.getElementById('date_' + conceptId));
			var patientId = ${model.patient.patientId};
			DWRObsService.createObs(patientId, encounterId, conceptId, valueText, obsDate, refreshPage);
		}
</script>