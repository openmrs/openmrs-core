<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Add Reports,Edit Reports,Delete Reports,View Reports">
		<li <c:if test="<%= request.getRequestURI().contains("report") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/reports/report.list">
				<spring:message code="Report.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Add Report Objects,Edit Report Objects,Delete Report Objects,View Report Objects">
		<li <c:if test="<%= request.getRequestURI().contains("reportObject") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/reports/reportObject.list">
				<spring:message code="ReportObject.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
</ul>