<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:globalProperty var="conceptsToDisplay" key="${model.globalPropertyKey}" />
<c:if test="${not empty conceptsToDisplay}">
	<table>
	<c:forTokens var="conceptId" items="${conceptsToDisplay}" delims="," >
		<tr>
			<td><openmrs_tag:concept conceptId="${conceptId}"/>:</td>
			<td><openmrs_tag:mostRecentObs concept="${conceptId}" observations="${model.patientObs}" locale="${model.locale}" labelIfNone="general.none" /></td>
		</tr>
	</c:forTokens>
	</table>
</c:if>