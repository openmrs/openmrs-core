<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><openmrs:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Manage Concepts,View Concepts">
		<li <c:if test='<%= request.getRequestURI().contains("concepts/index") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/dictionary">
				<openmrs:message code="Concept.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Concepts">
		<li <c:if test='<%= request.getRequestURI().contains("conceptDrug") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/concepts/conceptDrug.list">
				<openmrs:message code="ConceptDrug.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="View Concept Proposals">
		<li <c:if test='<%= request.getRequestURI().contains("Proposal") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/concepts/conceptProposal.list">
				<openmrs:message code="ConceptProposal.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Concept Classes">
		<li <c:if test='<%= request.getRequestURI().contains("Class") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/concepts/conceptClass.list">
				<openmrs:message code="ConceptClass.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Concept Datatypes">
		<li <c:if test='<%= request.getRequestURI().contains("Datatype") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/concepts/conceptDatatype.list">
				<openmrs:message code="ConceptDatatype.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Concept Sources">
		<li <c:if test='<%= request.getRequestURI().contains("conceptSource") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/concepts/conceptSource.list">
				<openmrs:message code="ConceptSource.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Concept Stop Words">
		<li <c:if test='<%= request.getRequestURI().contains("conceptStopWord") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/concepts/conceptStopWord.list">
				<openmrs:message code="ConceptStopWord.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:globalProperty key="concept_map_type_management.enable" defaultValue="false" var="allowConceptMapTypeManagement"/>
	<c:if test='${allowConceptMapTypeManagement}'>
	<openmrs:hasPrivilege privilege="Manage Concept Map Types">
		<li <c:if test='<%= request.getRequestURI().contains("conceptMapType") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/concepts/conceptMapTypeList.list">
				<openmrs:message code="ConceptMapType.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	</c:if>
	<openmrs:hasPrivilege privilege="Manage Concept Reference Terms">
		<li <c:if test='<%= request.getRequestURI().contains("conceptReferenceTerm") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/concepts/conceptReferenceTerms.htm">
				<openmrs:message code="ConceptReferenceTerm.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.localHeader" type="html">
			<c:forEach items="${extension.links}" var="link">
				<li <c:if test="${fn:endsWith(pageContext.request.requestURI, link.key)}">class="active"</c:if> >
					<a href="<openmrs_tag:url value="${link.key}"/>"><openmrs:message code="${link.value}"/></a>
				</li>
			</c:forEach>
	</openmrs:extensionPoint>
</ul>