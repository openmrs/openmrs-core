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
	<c:forTokens var="conceptIds" items="${model.conceptIds}" delims="," >
		<tr>
			<td><openmrs_tag:concept conceptId="${conceptIds}"/>:</td>
			<td>
				<b>
				<openmrs_tag:mostRecentObs concept="${conceptIds}" observations="${model.patientObs}" locale="${model.locale}" labelIfNone="general.none" showDate="true" showEditLink="true"/>
				</b>
			</td>
			<c:if test="${allowNew}">
				<td>
					<c:set var="thisConcept" value="${model.conceptMapByStringIds[conceptIds]}"/>
					<a href="javascript:showHideDiv('newCustomObs_${conceptIds}')">
						<openmrs:message code="general.new"/>
					</a>
				</td>
				<td class="dashedAndHighlighted" id="newCustomObs_${conceptIds}" style="display:none">
				
				<openmrs:format conceptId="${conceptIds}"/>
				<c:choose>
					<c:when test="${thisConcept.datatype.hl7Abbreviation == 'DT'}">		
				 		<input type="text" size="10" value="" onfocus="showCalendar(this)" id="value_${conceptIds}_id" />
					</c:when>
					<c:when test="${thisConcept.datatype.hl7Abbreviation == 'CWE'}">
						<openmrs:fieldGen type="org.openmrs.Concept" formFieldName="value_${conceptIds}" val="" parameters="noBind=true|showAnswers=${conceptIds}" />
					</c:when>
					<c:when test="${thisConcept.datatype.hl7Abbreviation == 'BIT'}">
					<script>
						$j(function() {
							var booleanConcepts = ["Yes", "No", "False", "True", "0", "1"];
							$j( "#value_${conceptIds}_id" ).autocomplete({ source: booleanConcepts });
						});
					</script>
					<input type="text" id="value_${conceptIds}_id"/>
					</c:when>
					<c:otherwise>
						<input type="text" id="value_${conceptIds}_id"/>
					</c:otherwise>
				</c:choose>	
				
					<openmrs:message code="general.onDate"/>
					<openmrs:fieldGen type="java.util.Date" formFieldName="date_${conceptIds}" val="" parameters="noBind=true" />
					<input type="button" value="<openmrs:message code="general.save"/>" onClick="handleAddCustomObs(${conceptIds})"/>
					<input type="button" value="<openmrs:message code="general.cancel"/>" onClick="showHideDiv('newCustomObs_${conceptIds}')"/>
				</td>
			</c:if>
		</tr>
	</c:forTokens>
	</table>
</c:if>

<script type="text/javascript">
	function handleAddCustomObs(conceptId) {
			var encounterId = null;
			var valueText = dwr.util.getValue(document.getElementById('value_' + conceptId+'_id'));
			var obsDate = dwr.util.getValue(document.getElementById('date_' + conceptId));
            var patientId = <c:out value="${model.patient.patientId}" />;
			DWRObsService.createObs(patientId, encounterId, conceptId, valueText, obsDate, refreshPage);
		}
</script>

