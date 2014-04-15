<fieldset>
	<legend><openmrs:message code="Concept.resources" /></legend>
	<a href="index.htm?phrase=<openmrs:format concept="${command.concept}" />"
		target="_similar_terms" onclick="addName(this)"><openmrs:message code="dictionary.similarConcepts" /></a><br/>
	<a href="http://www2.merriam-webster.com/cgi-bin/mwmednlm?book=Medical&va=<openmrs:format concept="${command.concept}" />"
		target="_blank" onclick="addName(this)">Merriam Webster&reg;</a><br/>
	<a href="http://www.google.com/search?q=<openmrs:format concept="${command.concept}" />"
		target="_blank" onclick="addName(this)">Google&trade;</a><br/>
	<a href="http://www.utdol.com/application/vocab.asp?submit=Go&search=<openmrs:format concept="${command.concept}" />"
		target="_blank" onclick="addName(this)">UpToDate&reg;</a><br/>
	<a href="http://dictionary.reference.com/search?submit=Go&q=<openmrs:format concept="${command.concept}" />"
		target="_blank" onclick="addName(this)">Dictionary.com&reg;</a><br/>
	<a href="http://search.atomz.com/search/?sp-a=sp1001878c&sp-q=<openmrs:format concept="${command.concept}" />"
		target="_blank" onclick="addName(this)">Lab Tests Online</a><br/>
	<a href="http://en.wikipedia.org/wiki/<openmrs:format concept="${command.concept}" />"
		target="_blank"><openmrs:message code="Concept.wikipedia" /></a>
</fieldset>

<fieldset>
	<legend><openmrs:message code="Concept.usage" /></legend>
	<c:if test="${command.concept.conceptId!=null}">
	<h4><openmrs:message code="dictionary.numobs" arguments="${command.numberOfObsUsingThisConcept}" /></h4>

	<c:if test="${fn:length(command.questionsAnswered) > 0}">
		<h4><openmrs:message code="dictionary.questionsAnswered" /></h4><ul>
		<c:forEach items="${command.questionsAnswered}" var="question">
			<li><a href="concept.htm?conceptId=${question.conceptId}"><openmrs:format concept="${question}" /></a></li>
		</c:forEach></ul>
	</c:if>
	
	<c:if test="${fn:length(command.containedInSets) > 0}">
		<h4><openmrs:message code="dictionary.containedInSets" /></h4><ul>
		<c:forEach items="${command.containedInSets}" var="set">
			<li><a href="concept.htm?conceptId=${set.conceptSet.conceptId}"><openmrs:format concept="${set.conceptSet}" /></a><br/></li>
		</c:forEach></ul>
	</c:if>

	<c:forEach items="${command.conceptUsage}" var="conceptUsageExt">
		<openmrs:hasPrivilege privilege="${conceptUsageExt.requiredPrivilege}">
		<c:if test="${fn:length(conceptUsageExt.conceptUsage) > 0}">
			<h4><openmrs:message code="${conceptUsageExt.header}" /></h4>
			<ul>
				<c:forEach items="${conceptUsageExt.conceptUsage}" var="usage">
					<li><a href="${pageContext.request.contextPath}${usage.url}">
							<c:if test="${usage.strike}"><strike></c:if>
								${usage.label}
							<c:if test="${usage.strike}"></strike></c:if>
						</a>
					</li>
				</c:forEach>
			</ul>
		</c:if>
		</openmrs:hasPrivilege>
	</c:forEach>

	<openmrs:extensionPoint pointId="org.openmrs.concept.usage" type="html" requiredClass="org.openmrs.module.web.extension.ConceptUsageExtension">
		<openmrs:hasPrivilege privilege="${extension.requiredPrivilege}">
		<c:if test="${fn:length(extension.conceptUsage) > 0}">
			<h4>${extension.header}</h4>
			<ul><c:forEach items="${extension.conceptUsage}" var="usage">
			<li><a href="<openmrs_tag:url value="${usage.url}"/>">${usage.label}</a></br></li>
			</c:forEach></ul>
		</c:if>
		</openmrs:hasPrivilege>
	</openmrs:extensionPoint>
	</c:if>
</fieldset>
