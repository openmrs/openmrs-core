<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Run Reports">
		<li <c:if test='<%= request.getRequestURI().contains("runReport") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/reports/runReport.list">
				<spring:message code="Report.list.title"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Reports">
		<li <c:if test='<%= request.getRequestURI().contains("reportSchemaXml") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/reports/reportSchemaXml.list">
				<spring:message code="Report.manage.title"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Reports">
		<li <c:if test='<%= request.getRequestURI().contains("reportMacros") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/reports/reportMacros.form">
				<spring:message code="Report.macros.title"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Add Report Objects,Edit Report Objects,Delete Report Objects,View Report Objects">
		<li <c:if test='<%= request.getRequestURI().contains("dataExport") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/reports/dataExport.list">
				<spring:message code="DataExport.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Add Report Objects,Edit Report Objects,Delete Report Objects,View Report Objects">
		<li <c:if test='<%= request.getRequestURI().contains("rowPerObsDataExport") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/reports/rowPerObsDataExport.list">
				<spring:message code="RowPerObsDataExport.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Add Cohorts,Edit Cohorts,Delete Cohorts,View Cohorts">
		<li <c:if test='<%= request.getRequestURI().contains("cohortList") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/reports/cohorts.list">
				<spring:message code="Cohort.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Add Patient Searches,Edit Patient Searches,Delete Patient Searches,View Patient Searches">
		<li <c:if test='<%= request.getRequestURI().contains("patientSearch") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/reports/patientSearch.list">
				<spring:message code="PatientSearch.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Add Report Objects,Edit Report Objects,Delete Report Objects,View Report Objects">
		<li <c:if test='<%= request.getRequestURI().contains("reportObject") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/reports/reportObject.list">
				<spring:message code="ReportObject.manage"/>
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