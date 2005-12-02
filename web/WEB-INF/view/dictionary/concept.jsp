<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<h2><spring:message code="Concept.title" /></h2>

<c:if test="${concept.conceptId != null}">
	<a href="concept.htm?conceptId=${concept.conceptId - 1}" valign="middle">&laquo; Previous</a> |
	<openmrs:hasPrivilege privilege="Edit Dictionary" converse="false">
		<a href="concept.form?conceptId=${concept.conceptId}" valign="middle">Edit</a> |
	</openmrs:hasPrivilege>
	<a href="concept.htm?conceptId=${concept.conceptId + 1}" valign="middle">Next &raquo;</a>
</c:if>

<br/>
<c:if test="${concept.retired}">
	<div class="retiredMessage"><spring:message code="Concept.retiredMessage"/></div>
</c:if>
<br/>

<table>
	<tr>
		<td><spring:message code="general.id"/></td>
		<td>${concept.conceptId}</td>
	</tr>
	<tr>
		<td title="<spring:message code="Concept.name.help"/>">
			<spring:message code="general.name" />
		</td>
		<td><spring:bind path="conceptName.name">
				${status.value}
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td title="<spring:message code="Concept.shortName.help"/>">
			<spring:message code="Concept.shortName" />
		</td>
		<td><spring:bind path="conceptName.shortName">
			${status.value}
		</spring:bind></td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.description" /></td>
		<td valign="top"><spring:bind path="concept.description">
			${status.value}
		</spring:bind></td>
	</tr>
	<tr>
		<td valign="top" title="<spring:message code="Concept.synonyms.help"/>">
			<spring:message code="Concept.synonyms" />
		</td>
		<td valign="top">
			<c:forEach items="${conceptSynonyms}" var="syn">${syn}<br/></c:forEach>
		</td>
	</tr>
	<tr>
		<td  title="<spring:message code="Concept.conceptClass.help"/>">
			<spring:message code="Concept.conceptClass" />
		</td>
		<td valign="top">
			${concept.conceptClass.name}
		</td>
	</tr>
	<c:if test="${concept.conceptClass != null && concept.conceptClass.set}">
		<tr id="setOptions">
			<td valign="top"><spring:message code="Concept.conceptSets"/></td>
			<td valign="top">
				<c:forEach items="${conceptSets}" var="set">${set.value[1]} (${set.value[0]})<br/></c:forEach>
			</td>
		</tr>
	</c:if>
	<tr>
		<td title="<spring:message code="Concept.datatype.help"/>">
			<spring:message code="Concept.datatype" />
		</td>
		<td valign="top">
			${concept.datatype.name}
		</td>
	</tr>
	<c:if test="${concept.datatype != null && concept.datatype.name == 'Coded'}">
		<tr>
			<td valign="top"><spring:message code="Concept.answers"/></td>
			<td>
				<c:forEach items="${conceptAnswers}" var="answer">${answer.value} (${answer.key})<br/></c:forEach>
			</td>
		</tr>
	</c:if>
	<c:if test="${concept.numeric}">
		<tr>
			<td valign="top"><spring:message code="ConceptNumeric.name"/></td>
			<td>
				<spring:nestedPath path="concept.conceptNumeric">
					<table border="0">
						<tr>
							<th></th>
							<th><spring:message code="ConceptNumeric.low"/></th>
							<th><spring:message code="ConceptNumeric.high"/></th>
						</tr>
						<tr>
							<th valign="middle"><spring:message code="ConceptNumeric.absolute"/></th>
							<td valign="middle">
								<spring:bind path="lowAbsolute">
									${status.value}
								</spring:bind>
							</td>
							<td valign="middle">
								<spring:bind path="hiAbsolute">
									${status.value}
								</spring:bind>
							</td>
						</tr>
						<tr>
							<th valign="middle"><spring:message code="ConceptNumeric.critical"/></th>
							<td valign="middle">
								<spring:bind path="lowCritical">
									${status.value}
								</spring:bind>
							</td>
							<td valign="middle">
								<spring:bind path="hiCritical">
									${status.value}
								</spring:bind>
							</td>
						</tr>
						<tr>
							<th valign="middle"><spring:message code="ConceptNumeric.normal"/></th>
							<td valign="middle">
								<spring:bind path="lowNormal">
									${status.value}
								</spring:bind>
							</td>
							<td valign="middle">
								<spring:bind path="hiNormal">
									${status.value}
								</spring:bind>
							</td>
						</tr>
						<tr>
							<td></td>
							<td colspan="2"><small><em>(<spring:message code="ConceptNumeric.inclusive"/>)</em></small>
							</td>
						</tr>
						<tr>
							<td><spring:message code="ConceptNumeric.units"/></td>
							<td colspan="2">
								<spring:bind path="units">
									${status.value}
								</spring:bind>
							</td>
						</tr>
						<tr>
							<td><spring:message code="ConceptNumeric.precise"/></td>
							<td colspan="2">
								<spring:bind path="precise">
									<c:if test="${status.value}">Yes</c:if>
									<c:if test="${!status.value}">No</c:if>
								</spring:bind>
							</td>
						</tr>
					</table>
				</spring:nestedPath>
			</td>
	</c:if>
	<tr>
		<td><spring:message code="Concept.version" /></td>
		<td><spring:bind path="concept.version">
			${status.value}
		</spring:bind></td>
	</tr>
	<tr>
		<td><spring:message code="general.retired" /></td>
		<td><spring:bind path="concept.retired">
			${status.value}
		</spring:bind></td>
	</tr>
	<c:if test="${!(concept.creator == null)}">
		<tr>
			<td><spring:message code="general.createdBy" /></td>
			<td>
				${concept.creator.firstName} ${concept.creator.lastName} -
				<openmrs:formatDate date="${concept.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
	<c:if test="${!(concept.changedBy == null)}">
		<tr>
			<td><spring:message code="general.changedBy" /></td>
			<td>
				${concept.changedBy.firstName} ${concept.changedBy.lastName} -
				<openmrs:formatDate date="${concept.dateChanged}" type="long" />
			</td>
		</tr>
	</c:if>
</table>

<%@ include file="/WEB-INF/template/footer.jsp"%>
