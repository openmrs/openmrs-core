<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Encounters" otherwise="/login.htm" />

<link href="<%= request.getContextPath() %>/openmrs.css" type="text/css" rel="stylesheet" />
<link href="<%= request.getContextPath() %>/style.css" type="text/css" rel="stylesheet" />
<openmrs:htmlInclude file="/openmrs.js" />

<div class="boxHeader">
	Encounter: <b>${model.encounter.encounterType.name}<b>
		on <b>${model.encounter.encounterDatetime}</b>
		at <b>${model.encounter.location}</b>
	<br/>
	Form: <b>${model.form.name}</b>
</div>

<div class="box">
	<table class="encounterFormTable">
		<c:forEach var="fieldAndObsList" items="${model.obsByField}">
			<c:if test="${model.showBlankFields || not empty fieldAndObsList.value}">
				<tr class="encounterFormTable">
					<td>${fieldAndObsList.key.pageNumber}.${fieldAndObsList.key.fieldNumber}</td>
					<td>${fieldAndObsList.key.field.name}</td>
					<td align="right">
						<c:forEach var="obs" items="${fieldAndObsList.value}">
							${obs.valueAsString[model.locale]}
						</c:forEach>
					</td>
				</tr>
			</c:if>
		</c:forEach>
		<c:forEach var="obs" items="${model.otherObs}">
			<tr>
				<td></td>
				<td>
					<openmrs_tag:concept conceptId="${obs.concept.conceptId}"/>
				</td>
				<td align="right">
					${obs.valueAsString[model.locale]}
				</td>
			</tr>
		</c:forEach>
	</table>
</div>