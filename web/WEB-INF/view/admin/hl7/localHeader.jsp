<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="View HL7 Inbound Messages">
		<li <c:if test='<%= request.getRequestURI().contains("hl7InQueue") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/hl7/hl7InQueue.list" class="retired">
				<spring:message code="Hl7inQueue.title"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="View HL7 Inbound Messages">
		<li <c:if test='<%= request.getRequestURI().contains("hl7Deleted") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/hl7/hl7Deleted.form" class="retired">
				<spring:message code="Hl7inQueueRestore.title"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="View HL7 Inbound Messages">
		<li <c:if test='<%= request.getRequestURI().contains("hl7InError") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/hl7/hl7InError.list" class="retired">
				<spring:message code="Hl7inError.title"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:extensionPoint pointId="org.openmrs.admin.hl7.localHeader" type="html">
			<c:forEach items="${extension.links}" var="link">
				<li <c:if test='${fn:endsWith(pageContext.request.requestURI, link.key)}'>class="active"</c:if> >
					<a href="${pageContext.request.contextPath}/${link.key}">
						<spring:message code="${link.value}"/>
					</a>
				</li>
			</c:forEach>
	</openmrs:extensionPoint>
</ul>