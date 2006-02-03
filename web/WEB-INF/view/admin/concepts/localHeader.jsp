<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Add Concepts,Edit Concepts,Delete Concepts,View Concepts">
		<li <c:if test="<%= request.getRequestURI().contains("concepts/index") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/dictionary">
				<spring:message code="Concept.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Add Users">
		<li <c:if test="<%= request.getRequestURI().contains("Proposal") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/concepts/conceptProposal.list">
				<spring:message code="ConceptProposal.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Edit Users">
		<li <c:if test="<%= request.getRequestURI().contains("Word") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/concepts/conceptWord.form">
				<spring:message code="ConceptWord.manage"/>
			</a>
		</li>
		<li <c:if test="<%= request.getRequestURI().contains("SetDerived") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/concepts/conceptSetDerived.form">
				<spring:message code="ConceptSetDerived.manage"/>
			</a>
		</li>	
	</openmrs:hasPrivilege>
	<li <c:if test="<%= request.getRequestURI().contains("Class") %>">class="active"</c:if>>
		<a href="${pageContext.request.contextPath}/admin/concepts/conceptClass.list">
			<spring:message code="ConceptClass.manage"/>
		</a>
	</li>
	<li <c:if test="<%= request.getRequestURI().contains("Datatype") %>">class="active"</c:if>>
		<a href="${pageContext.request.contextPath}/admin/concepts/conceptDatatype.list">
			<spring:message code="ConceptDatatype.manage"/>
		</a>
	</li>
</ul>