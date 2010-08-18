<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Add Concepts,Edit Concepts,Delete Concepts,View Concepts">
		<li <c:if test='<%= request.getRequestURI().contains("concepts/index") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/dictionary">
				<spring:message code="Concept.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Add Concepts,Edit Concepts">
		<li <c:if test='<%= request.getRequestURI().contains("conceptDrug") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/concepts/conceptDrug.list">
				<spring:message code="ConceptDrug.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="View Concept Proposals">
		<li <c:if test='<%= request.getRequestURI().contains("Proposal") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/concepts/conceptProposal.list">
				<spring:message code="ConceptProposal.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Edit Concepts">
		<li <c:if test='<%= request.getRequestURI().contains("Word") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/concepts/conceptWord.form">
				<spring:message code="ConceptWord.manage"/>
			</a>
		</li>
		<li <c:if test='<%= request.getRequestURI().contains("SetDerived") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/concepts/conceptSetDerived.form">
				<spring:message code="ConceptSetDerived.manage"/>
			</a>
		</li>	
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Concept Classes">
		<li <c:if test='<%= request.getRequestURI().contains("Class") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/concepts/conceptClass.list">
				<spring:message code="ConceptClass.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Concept Datatypes">
		<li <c:if test='<%= request.getRequestURI().contains("Datatype") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/concepts/conceptDatatype.list">
				<spring:message code="ConceptDatatype.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Concept Sources">
		<li <c:if test='<%= request.getRequestURI().contains("conceptSource") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/concepts/conceptSource.list">
				<spring:message code="ConceptSource.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:extensionPoint pointId="org.openmrs.admin.concepts.localHeader" type="html">
			<c:forEach items="${extension.links}" var="link">
				<li <c:if test="${fn:endsWith(pageContext.request.requestURI, link.key)}">class="active"</c:if> >
					<a href="${pageContext.request.contextPath}/${link.key}"><spring:message code="${link.value}"/></a>
				</li>
			</c:forEach>
	</openmrs:extensionPoint>
</ul>