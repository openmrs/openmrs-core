<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Add Reports,Edit Reports,Delete Reports,View Reports">
		<li <c:if test="<%= request.getRequestURI().contains("/reportList") %>">class="active"</c:if>>
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
	<openmrs:hasPrivilege privilege="Add Data Exports,Edit Data Exports,Delete Data Exports,View Data Exports">
		<li <c:if test="<%= request.getRequestURI().contains("dataExport") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/reports/dataExport.list">
				<spring:message code="DataExport.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Add Data Exports,Edit Data Exports,Delete Data Exports,View Data Exports">
		<li <c:if test="<%= request.getRequestURI().contains("summaryForm") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/reports/summaryForm.htm">
				<spring:message code="PatientSummary.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Add Cohorts,Edit Cohorts,Delete Cohorts,View Cohorts">
		<li <c:if test="<%= request.getRequestURI().contains("cohortList") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/reports/cohorts.list">
				<spring:message code="Cohort.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:extensionPoint pointId="org.openmrs.admin.reports.localHeader" type="html">
			<c:forEach items="${extension.links}" var="link">
				<li <c:if test="${fn:endsWith(pageContext.request.requestURI, link.key)}">class="active"</c:if> >
					<a href="${pageContext.request.contextPath}/${link.key}"><spring:message code="${link.value}"/></a>
				</li>
			</c:forEach>
	</openmrs:extensionPoint>
</ul>