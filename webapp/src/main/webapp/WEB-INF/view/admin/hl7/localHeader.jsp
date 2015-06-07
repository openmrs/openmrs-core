<%@page import="org.openmrs.api.context.Context"%>
<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><openmrs:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Add HL7 Source,View HL7 Source,Update HL7 Source,Purge HL7 Source">
		<li <c:if test='<%= request.getRequestURI().contains("hl7Source") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/hl7/hl7Source.list">
				<openmrs:message code="Hl7Source.manage.title"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="View HL7 Inbound Messages">
		<li <c:if test='<%= request.getRequestURI().contains("hl7InQueueList") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/hl7/hl7InQueuePending.htm">
				<openmrs:message code="Hl7inQueue.pending.title"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="View HL7 Inbound Messages">
		<li <c:if test='<%= request.getRequestURI().contains("hl7OnHoldList") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/hl7/hl7InQueueHeld.htm">
				<openmrs:message code="Hl7inQueue.held.title"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="View HL7 Inbound Messages">
		<li <c:if test='<%= request.getRequestURI().contains("hl7InError") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/hl7/hl7InErrorList.htm">
				<openmrs:message code="Hl7inError.title"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="View HL7 Inbound Archive">
		<li <c:if test='<%= request.getRequestURI().contains("hl7InArchives") %>'>class="active"</c:if>>						
			<a id="hl7_archive_link" href="${pageContext.request.contextPath}/admin/hl7/hl7InArchives.htm">
				<openmrs:message code="Hl7InArchive.title"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="View HL7 Inbound Archive">
		<li <c:if test='<%= request.getRequestURI().contains("hl7InArchiveMigration") %>'>class="active"</c:if>>						
			<a id="hl7_archive_link" href="${pageContext.request.contextPath}/admin/hl7/hl7InArchiveMigration.htm">
				<openmrs:message code="Hl7InArchive.migrate.title"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:extensionPoint pointId="org.openmrs.admin.hl7.localHeader" type="html">
			<c:forEach items="${extension.links}" var="link">
				<li <c:if test='${fn:endsWith(pageContext.request.requestURI, link.key)}'>class="active"</c:if> >
					<a href="<openmrs_tag:url value="${link.key}"/>">
						<openmrs:message code="${link.value}"/>
					</a>
				</li>
			</c:forEach>
	</openmrs:extensionPoint>
</ul>
